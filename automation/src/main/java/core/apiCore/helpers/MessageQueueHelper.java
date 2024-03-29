package core.apiCore.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;

import core.apiCore.ServiceManager;
import core.apiCore.interfaces.KafkaInterface;
import core.apiCore.interfaces.RabbitMqInterface;
import core.apiCore.interfaces.ServiceBusInterface;
import core.helpers.Helper;
import core.helpers.StopWatchHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.MessageObject;
import core.support.objects.MessageObject.messageType;
import core.support.objects.ServiceObject;

public class MessageQueueHelper {

	public static final String RESPONSE_IDENTIFIER = "response.identifier";

	
	/**
	 * 1) gets messages, adds them to the outboundMessages 2) filters based on the
	 * message key 3) validates based on expected response requirements
	 * 
	 * @param messageId
	 * @throws Exception
	 */
	public static void receiveAndValidateMessages(ServiceObject serviceObject, String messageId, messageType messageType) throws Exception {

		// evaluate options
		evaluateOption(serviceObject);
		
		// return if no validation required
		if(serviceObject.getExpectedResponse().isEmpty())
			return;
		
		CopyOnWriteArrayList<MessageObject> filteredMessages = new CopyOnWriteArrayList<>();
		List<String> errorMessages = new ArrayList<String>();

		// message queue will run for maxRetrySeconds to retrieve matching outbound message
		int maxRetrySeconds = Config.getIntValue(ServiceManager.SERVICE_RESPONSE_TIMEOUT_SECONDS);
		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		long lastLogged = 0;
		int interval = 10; // log every 10 seconds

		do {
			lastLogged = MessageQueueHelper.logPerInterval(interval, watch, lastLogged, filteredMessages.size());

			// gets messages and stores them in outboundMessages hashmap
			getOutboundMessages(messageType);

			// filters based on message id
			filteredMessages.addAll(MessageQueueHelper.filterOutboundMessage(messageId));

			// validate message count
			errorMessages = validateExpectedMessageCount(serviceObject.getExpectedResponse(),
					getMessageList(filteredMessages));

			// validates messages. will retry on error if expected number of messages not set
			if (errorMessages.isEmpty()) {
				printAllFilteredMessages(filteredMessages);
				errorMessages.addAll((validateMessages(serviceObject, filteredMessages)));
				
				// if expected fixed number of messages, at this point we have met that requirement
				if(isExpectingMessageCount(serviceObject.getExpectedResponse()))
					break;
			}

			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);

		} while (!errorMessages.isEmpty() && passedTimeInSeconds < maxRetrySeconds);

		// save matching filtered messages to variable specified
		saveOutBoundValues(serviceObject, filteredMessages, errorMessages);
		
		if (!errorMessages.isEmpty()) {
			String errorString = StringUtils.join(errorMessages, "\n error: ");
			TestLog.ConsoleLog(errorString);
			Helper.assertFalse(StringUtils.join(errorMessages, "\n error: "));
		}
	}
	
	public static void saveOutBoundValues(ServiceObject serviceObject, CopyOnWriteArrayList<MessageObject> filteredMessages,  List<String> errorMessages) {
		if (filteredMessages.isEmpty() || !errorMessages.isEmpty() || serviceObject.getOutputParams().isEmpty())
			return;
		
		// get all the filtered messages
		ArrayList<String> messages = new ArrayList<String>();
		for(MessageObject message : filteredMessages) {
			messages.add(message.getMessage());
		}
		
		String outputParam = serviceObject.getOutputParams();
		// replace parameters for outputParam
		outputParam = DataHelper.replaceParameters(outputParam);

		List<KeyValue> keywords = DataHelper.getValidationMap(outputParam);
		for (KeyValue keyword : keywords) {

			// fail if value is wrong format
			if (!keyword.key.toString().startsWith("<") || !keyword.key.toString().contains("$")
					|| !keyword.key.toString().endsWith(">"))
				Helper.assertFalse("variable placement must of format path: <$variable>. invalid value: "
						+ keyword.key.toString());

			String key = (String) keyword.key;
			key = key.replace("$", "").replace("<", "").replace(">", "").trim();
			// gets json value. if list, returns string separated by comma
			String value = Helper.convertListToString(messages);

			Config.putValue(key, value, false);
			TestLog.logPass("output parameter: " + key + " value: " + value);
		}
		
	}
	
	/**
	 * generate message id if the request body is set
	 * 
	 * @param requestBody
	 * @return
	 * @return
	 */
	public static String generateMessageId(ServiceObject serviceObject, String messageIdPrefix) {
		String messageId = StringUtils.EMPTY;

		// get unique identifier for request body to match outbound message
		if (!serviceObject.getRequestBody().isEmpty())
			messageId = messageIdPrefix + "-" + UUID.randomUUID().toString();

		return messageId;
	}

	/**
	 * log per interval stating the wait time for a message from message queue
	 * 
	 * @param interval
	 * @param watch
	 * @param lastLogged
	 * @return
	 */
	public static long logPerInterval(int interval, StopWatchHelper watch, long lastLogged, int receivedMessageCount) {
		long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
		if (passedTimeInSeconds > interval && passedTimeInSeconds - lastLogged > interval) {
			TestLog.logPass("waiting: " + watch.time(TimeUnit.SECONDS) + " seconds");
			TestLog.logPass("received: " + receivedMessageCount + " relevant message(s)");

			Helper.waitForSeconds(1);
			lastLogged = passedTimeInSeconds;
		}
		return lastLogged;
	}

	/**
	 * validate expected message count from received message format:
	 * EXPECTED_MESSAGE_COUNT:1;
	 * 
	 * @param request
	 * @param filteredMessages
	 * @return
	 */
	public static List<String> validateExpectedMessageCount(String request, List<String> filteredMessages) {
		List<String> errorMessages = new ArrayList<String>();

		if (filteredMessages.isEmpty()) {
			errorMessages.add("no messages received");
			return errorMessages;
		}

		int expectedMessageCount = -1;

		// get a map of key values in request
		Map<String, String> params = getKeyValueFromString(request, ";", ":");

		// get expected message count if set
		if (params.containsKey(DataHelper.EXPECTED_MESSAGE_COUNT)) {
			expectedMessageCount = Helper.getIntFromString(params.get(DataHelper.EXPECTED_MESSAGE_COUNT), true);
		}

		// get actual message count
		int actualMessageCount = filteredMessages.size();

		// compare expected with actual message count
		// TestLog.logPass("verifying message count: " + "Response message count
		// received " + filteredMessages.size() + " out of " + expectedMessageCount + "
		// expected messages");
		if (expectedMessageCount != -1 && expectedMessageCount != actualMessageCount) {
			String errorMessage = "Response received " + filteredMessages.size() + " out of " + expectedMessageCount
					+ ".\n Received messages: \n";
			errorMessages.add(errorMessage + String.join("\n ", filteredMessages));
		}
		return errorMessages;
	}
	
	/**
	 * is fixed messaged count expected
	 * eg. EXPECTED_MESSAGE_COUNT:2
	 * @param expectedResponse
	 * @return
	 */
	public static boolean isExpectingMessageCount(String expectedResponse) {
		int expectedMessageCount = -1;

		// get a map of key values in request
		Map<String, String> params = getKeyValueFromString(expectedResponse, ";", ":");

		// get expected message count if set
		if (params.containsKey(DataHelper.EXPECTED_MESSAGE_COUNT)) {
			expectedMessageCount = Helper.getIntFromString(params.get(DataHelper.EXPECTED_MESSAGE_COUNT), true);
		}
		if(expectedMessageCount != -1)
			return true;
		
		return false;
	}

	/**
	 * separated based on key value if key value exists eg. key:value; key1:value1
	 * 
	 * @param value
	 * @param entriesSeparator eg. ";"
	 * @param separator        eg ":"
	 * @return
	 */
	public static Map<String, String> getKeyValueFromString(String value, String entriesSeparator, String separator) {
		Map<String, String> map = new HashMap<String, String>();

		// remove all spaces
		value = value.replaceAll("\\s+", "");

		String[] entries = value.split(entriesSeparator);
		for (String entry : entries) {
			if (!TextUtils.isEmpty(entry) && entry.contains(separator)) {
				String[] keyValue = entry.split(separator);
				map.put(keyValue[0], keyValue[1]);
			}
		}
		return map;
	}

	/**
	 * find message based on unique identifier passed in through options
	 * identifiers are separated by comma 
	 * separators have And relationship
	 * @param messageId
	 * @return
	 */
	public static CopyOnWriteArrayList<MessageObject> findMessagesBasedOnResponseIdentifier() {
		CopyOnWriteArrayList<MessageObject> filteredMessages = new CopyOnWriteArrayList<MessageObject>();
		List<String> identifierList = Config.getValueList(MessageQueueHelper.RESPONSE_IDENTIFIER);

		// return if identifier is empty
		if (identifierList.isEmpty())
			return filteredMessages;

		for (Entry<MessageObject, Boolean> entry : MessageObject.outboundMessages.entrySet()) {
			String receiveMessage = Optional.ofNullable(entry.getKey().getMessage()).orElse("");

			boolean isMessageMatch = isIdentifierMatch(receiveMessage, identifierList);
			if (entry.getValue().equals(true) && isMessageMatch) {

				filteredMessages.add(entry.getKey());
				MessageObject.outboundMessages.put(entry.getKey(), false);
			}

		}

		return filteredMessages;
	}
	
	private static boolean isIdentifierMatch(String receiveMessage, List<String> identifierList) {
		// return if identifier is empty
		if (identifierList.isEmpty())
			return false;

		for (String identifier : identifierList) {
			if (!receiveMessage.contains(identifier))
				return false;
		}
		return true;
	}

	/**
	 * find message based on record id
	 * 
	 * @param messageId
	 * @return
	 */
	public static CopyOnWriteArrayList<MessageObject> findMessagesBasedOnMessageId(String messageId) {
		CopyOnWriteArrayList<MessageObject> filteredMessages = new CopyOnWriteArrayList<MessageObject>();

		// return if message id is not set. message id is empty when no message is sent
		if (messageId.isEmpty())
			return filteredMessages;

		for (Entry<MessageObject, Boolean> entry : MessageObject.outboundMessages.entrySet()) {
			String receivedMessageId = Optional.ofNullable(entry.getKey().getMessageId()).orElse("");
			String receivedCorrelationId = Optional.ofNullable(entry.getKey().getCorrelationId()).orElse("");

			boolean isMessageMatch = receivedMessageId.contains(messageId) || receivedCorrelationId.contains(messageId);
			if (entry.getValue().equals(true) && isMessageMatch) {

				filteredMessages.add(entry.getKey());
				MessageObject.outboundMessages.put(entry.getKey(), false);
			}
		}

		return filteredMessages;
	}

	/**
	 * filter outbound message based on messageId
	 * 
	 * @param msgId
	 * @return
	 */
	public static CopyOnWriteArrayList<MessageObject> filterOutboundMessage(String messageId) {

		// filter messages for the current test
		CopyOnWriteArrayList<MessageObject> filteredMessages = new CopyOnWriteArrayList<MessageObject>();

		// filter based on message Id
		CopyOnWriteArrayList<MessageObject> filterByMessageId = MessageQueueHelper
				.findMessagesBasedOnMessageId(messageId);

		// if message id set (message is sent in same test), use filtered by message id,
		// else use identifier from options
		if (!filterByMessageId.isEmpty())
			filteredMessages.addAll(filterByMessageId);
		else {
			CopyOnWriteArrayList<MessageObject> filterByMessageIdentifier = MessageQueueHelper
					.findMessagesBasedOnResponseIdentifier();
			filteredMessages.addAll(filterByMessageIdentifier);
		}

		return filteredMessages;
	}

	public static void getOutboundMessages(messageType messageType) throws Exception {
		switch (messageType) {
		case KAFKA:
			KafkaInterface.getOutboundMessages();
			break;
		case RABBITMQ:
			RabbitMqInterface.getOutboundMessages();
			break;
		case SERVICEBUS:
			ServiceBusInterface.getOutboundMessages();
			break;
		case TEST:
			break;
		default:
		}

	}

	/**
	 * print all messages ids
	 */
	public static void printAllMessages() {
		TestLog.ConsoleLog("Printing All received messages");
		for (Entry<MessageObject, Boolean> entry : MessageObject.outboundMessages.entrySet()) {
			String messageId = entry.getKey().getMessageId();
			Boolean messageAvailable = entry.getValue();

			TestLog.ConsoleLog("received messagesId: '" + messageId + "'. was message read: " + !messageAvailable);
		}
	}

	public static void printAllFilteredMessages(CopyOnWriteArrayList<MessageObject> filteredMessages) {
		for (MessageObject message : filteredMessages) {
			String messageId = message.getMessageId();
			String messageContent = message.getMessage();
			if(!message.isLogged) {
				TestLog.logPass("Printing relevant received messages");
				TestLog.logPass("received messagesId: '" + messageId + "' with message content: \n" +  messageContent );
			}
			message.isLogged = true;
		}
	}

	/**
	 * inserts filtered messages to array list of strings
	 * 
	 * @param filteredMessages
	 * @return
	 */
	public static List<String> getMessageList(CopyOnWriteArrayList<MessageObject> filteredMessages) {
		List<String> messages = new ArrayList<String>();
		for (MessageObject message : filteredMessages) {
			messages.add(message.getMessage());
		}
		return messages;
	}

	/**
	 * inserts filtered headers to array list of strings
	 * 
	 * @param filteredMessages
	 * @return
	 */
	public static List<String> getHeaderList(CopyOnWriteArrayList<MessageObject> filteredMessages) {
		List<String> messages = new ArrayList<String>();
		for (MessageObject message : filteredMessages) {
			messages.addAll(message.getHeader());
		}
		return messages;
	}

	/**
	 * inserts filtered topics to array list of strings
	 * 
	 * @param filteredMessages
	 * @return
	 */
	public static List<String> getTopicList(CopyOnWriteArrayList<MessageObject> filteredMessages) {
		List<String> messages = new ArrayList<String>();
		for (MessageObject message : filteredMessages) {
			messages.add(message.getTopic());
		}
		return messages;
	}

	/**
	 * validate message based on message, header, or topic valites json, xml, or
	 * text response
	 * 
	 * @return
	 * 
	 */
	public static List<String> validateMessages(ServiceObject serviceObject,
			CopyOnWriteArrayList<MessageObject> filteredMessages) {

		List<String> errorMessages = new ArrayList<String>();
		if (filteredMessages.isEmpty()) {
			errorMessages.add("no messages received");
			return errorMessages;
		}

		List<String> messageList = getMessageList(filteredMessages);
		List<String> headerList = getHeaderList(filteredMessages);
		List<String> topicList = getTopicList(filteredMessages);

		// separate expected response to each section we want to validate: messageBody,
		// header, topic
		String expectedMessage = DataHelper.removeSectionFromExpectedResponse(DataHelper.VERIFY_HEADER_PART_INDICATOR,
				serviceObject.getExpectedResponse());
		expectedMessage = DataHelper.removeSectionFromExpectedResponse(DataHelper.VERIFY_TOPIC_PART_INDICATOR,
				expectedMessage);
		expectedMessage = DataHelper.removeSectionFromExpectedResponse(DataHelper.EXPECTED_MESSAGE_COUNT,
				expectedMessage);
		String expectedHeader = DataHelper.getSectionFromExpectedResponse(DataHelper.VERIFY_HEADER_PART_INDICATOR,
				serviceObject.getExpectedResponse());
		String expectedTopic = DataHelper.getSectionFromExpectedResponse(DataHelper.VERIFY_TOPIC_PART_INDICATOR,
				serviceObject.getExpectedResponse());

		if (!expectedMessage.isEmpty()) {
			//TestLog.logPass("validating message list:"); TODO: will appear per retry
			errorMessages = DataHelper.validateExpectedValues(messageList, expectedMessage);
		}

		if (!expectedHeader.isEmpty()) {
			//TestLog.logPass("validating header list:");
			errorMessages.addAll(DataHelper.validateExpectedValues(headerList, expectedHeader));
		}

		if (!expectedTopic.isEmpty()) {
			//TestLog.logPass("validating topic list:");
			errorMessages.addAll(DataHelper.validateExpectedValues(topicList, expectedTopic));
		}

		return errorMessages;
	}

	public static void evaluateOption(ServiceObject serviceObject) {

		// reset validation timeout. will be overwritten by option value if set
		resetValidationTimeout();

		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return;
		}


		// replace parameters for request body
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key) {
			case ServiceManager.OPTION_NO_VALIDATION_TIMEOUT:
				Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED, false);
				break;
			case ServiceManager.OPTION_WAIT_FOR_RESPONSE:
				Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED, true);
				Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_SECONDS, keyword.value);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * reset validation timeout
	 */
	private static void resetValidationTimeout() {
		// reset validation timeout option
		String defaultValidationTimeoutIsEnabled = Config
				.getGlobalValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED);
		String defaultValidationTimeoutIsSeconds = Config
				.getGlobalValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_SECONDS);

		Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED, defaultValidationTimeoutIsEnabled);
		Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_SECONDS, defaultValidationTimeoutIsSeconds);
	}
}
