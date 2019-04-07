package core.apiCore.interfaces;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.XmlHelper;
import core.helpers.Helper;
import core.helpers.StopWatchHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

@SuppressWarnings("unused")
public class ServiceBusInterface {

	public enum SBEnv {
		DEFAULT, SAP, TOPIC_DLQ, HOST1, HOST2, ALERT
	}

	private static final String SERVICE_BUS_PREFIX = "SB_";

	
	private static final String CONNECTION_STR = "serviceBusConnectionString";
	private static final String INBOUND_QUEUE = "inboundQueue";
	private static final String OUTBOUND_TOPIC = "outboundTopic";
	private static final String OUTBOUND_SUB1 = "outboundSub1";
	private static final String OUTBOUND_SUB2 = "outboundSub2";
	private static final String OUTBOUND_ALERT = "outboundAlert";
	private static final String CONNECTION_SAP_STR = "SapServiceBusConnectionString";
	private static final String INBOUND_SAP_QUEUE = "SapInboundQueue";
	private static final String OUTBOUND_SAP_TOPIC = "SapOutboundTopic";
	private static final String EVENT_TOPIC = "eventTopic";
    private static final String HOST_FORM_SUBMITTED = "UserRequest"; 
	private static final String EVENT_SUB = "eventSub";
	private static final String DEAD_LETTER_QUEUE_SUFFIX = "/$DeadLetterQueue";

	private static final String CONF_MSG_TIME_OUT = "serviceBusMessageReceiveMaxTime";
	private static final String EMPTY_CHECK = "emptyCheck";
	private static final String TEMPLATE_FILES_PATH = "templateFilesPath";
	private static final String TEST_DATA_CLEANUP = "systemCleanup";
	private static final String PURGE_SB_RECEIVERS = "purgeServiceBusReceivers";
	private static final String AND_TOKEN = "&&";
	private static final String HOST1 = "HOST1";
	private static final String HOST2 = "HOST2";
	private static final String HOST_ALERT = "ALERT";
	private static final String MSG_ID_XPATH = "/Envelope/Body/Message/Header/MsgID";
	private static final String FORM_SUBMITTED = "FormSubmitted";
	public static final String MESSAGE_ID_PREFIX = "apiTestMsgID";
    private static final String NO_CHECK_NEEDED = "noCheckNeeded";

	private IMessageReceiver receiver1 = null;
	private IMessageReceiver receiver2 = null;
	private IMessageReceiver receiverAlert = null;
	private IMessageSender sender;
	
	public static Map<String, serviceBus> sbInstance = new ConcurrentHashMap<String, serviceBus>();

	public static Map<IMessage, Boolean> outboundMessages = new ConcurrentHashMap<IMessage, Boolean>();
	private static final int SERVICE_BUS_TIMEOUT_SECONDS = 120;
	
	
	/**
	 * getting an instance of service bus retrying And catching interrupt exceptions
	 * from other threads
	 * 
	 * @param type
	 * @return
	 */
	public synchronized static serviceBus getInstance(String host) {
		serviceBus servicebus = null;
		int retry = 3;
		do {
			retry--;
			servicebus = getSbInstance(host);
		} while (servicebus == null && retry > 0);

		Helper.assertTrue("service bus instance could not be created", servicebus.isInitiated);
		return servicebus;
	}
	
	
	/**
	 * info: SB_type_key = value
	 * SB_DEFAULT_connectionStr = ""
	 * SB_DEFAULT_inboundQueue = ""
	 * SB_DEFAULT_outboundTopic = ""
	 * SB_DEFAULT_host1 = host1
	 * SB_DEFAULT_host2 = host2
	 * SB_DEFAULT_host3 = alert
	 * @param host
	 * @return
	 */
	private synchronized static serviceBus getSbInstance(String type) {
		serviceBus instance = sbInstance.get(type);
		if (instance.isInitiated)
			return instance;

		instance.inbound = instance.inboundQueue;

		TestLog.logPass("inbound = {0}", instance.inbound);
		TestLog.logPass("connectionStr = {0}", instance.connectionStr);
		
		instance = setSender(instance);
		
		// for each host, set receiver
		for (String host : instance.hosts) {
			instance.outbound = instance.outboundTopic + "/subscriptions/" + host;

			TestLog.logPass("outbound = {0}", instance.outbound);

			instance = setReceiver(host, instance);
		}
		
		if(instance.sender != null && !instance.receivers.isEmpty())
			instance.isInitiated = true;

		return instance;
	}
	
	private static serviceBus setSender(serviceBus instance) {

		try {
		instance.sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
				new ConnectionStringBuilder(instance.connectionStr, instance.inbound));
		} catch (InterruptedException | ServiceBusException ex) {
			TestLog.ConsoleLogWarn(ex.getMessage());
			Thread.interrupted();
		}
		return instance;
	}
	
	private static serviceBus setReceiver(String host, serviceBus instance) {
		try {

			instance.receivers.put(host, ClientFactory.createMessageReceiverFromConnectionStringBuilder(
					new ConnectionStringBuilder(instance.connectionStr, instance.outbound),
					ReceiveMode.RECEIVEANDDELETE));

		} catch (InterruptedException | ServiceBusException ex) {
			TestLog.ConsoleLogWarn(ex.getMessage());
			Thread.interrupted();

		}
		return instance;
	}
	/**
	 * info: SB_type_key = value
	 * SB_DEFAULT_connectionStr = ""
	 * SB_DEFAULT_inboundQueue = ""
	 * SB_DEFAULT_outboundTopic = ""
	 * SB_DEFAULT_host1 = host1
	 * SB_DEFAULT_host2 = host2
	 * SB_DEFAULT_host3 = alert
	 */
	private void setupServiceBusInstance() {
		serviceBus instance = null;
		for (Entry<String, String> entry : TestObject.getTestInfo().config.entrySet()) {
			if(entry.getKey().contains(SERVICE_BUS_PREFIX)) {
				String[] values = entry.getKey().split("_");
				String type = values[1];
				String key = values[2];
				String value = entry.getValue();
				
				instance = sbInstance.get(type) == null ? new ServiceBusInterface().new serviceBus() : sbInstance.get(type);
				switch (key) {
				case "connectionStr":
					instance.connectionStr = key;
					break;
				case "inboundQueue":
					 instance.inboundQueue = key;
					 break;
				case "outboundTopic":
					 instance.outboundTopic = key;
					 break;
				}
				if(key.contains("host") && !value.isEmpty()) {
				  instance.hosts.add(value);
				}
					
				sbInstance.put(type, instance);
			}	
		}
	}
	
	/**
	 * Inject message to inbound queue And dequeues from outbound queue And do
	 * comparisons
	 *
	 * @param type
	 * @param contentType
	 * @param attachmentReady
	 * @param hostSelector
	 * @param templateFile
	 * @param requestBody
	 * @param expStr
	 * @param outputParams
	 * @param partialExpStr
	 * @param notExpStr
	 */
	public static void testServiceBus(ServiceObject apiObject) {

		serviceBus serviceBus = getInstance(apiObject.getOption());

		// replace parameters
		apiObject.withRequestBody(DataHelper.replaceParameters(apiObject.getRequestBody()));
		apiObject.withExpectedResponse( DataHelper.replaceParameters(apiObject.getExpectedResponse()));

		// Get request body using template And/or requestBody data column
		apiObject.withRequestBody(getRequestBodyFromTemplate(apiObject.getRequestBody(), apiObject.getTemplateFile(), apiObject.getContentType()));
		apiObject.withRequestBody(DataHelper.replaceParameters(apiObject.getRequestBody()));

		// get unique identifier for request body to match outbound message
		String msgID = generateMessageId(apiObject.getRequestBody());

		// send the message through service bus
		sendMessage(apiObject.getRequestBody(), serviceBus, msgID);

		// receives And verifies the outbound message against the expected results
		boolean isTestPass = receiveAndVerifyOutboundMessage(serviceBus, msgID, apiObject.getOption(), apiObject.getRequestBody(), apiObject.getOutputParams(),
				 apiObject.getExpectedResponse());	
		if(msgID.isEmpty())
			Helper.assertTrue("correct messages not received. SB Verification test, please investigate previous test for proper outbound message", isTestPass);
		Helper.assertTrue("correct messages not received", isTestPass);

	}
	
	/**
	 * generate message id if the request body is set
	 * @param requestBody
	 * @return
	 */
	public static String generateMessageId(String requestBody) {
		// get unique identifier for request body to match outbound message
		if (!requestBody.isEmpty()) {
			return MESSAGE_ID_PREFIX + "-" + UUID.randomUUID().toString();
		}else {
			return "";
		}
	}

	/**
	 * sends message through service bus
	 * 
	 * @param requestBody
	 * @param serviceBus
	 */
	public static void sendMessage(String requestBody, serviceBus serviceBus, String msgID) {
		if (!requestBody.isEmpty()) {
			// Create message And send to inbound queue
			IMessage msgToInboundQueue = new Message(requestBody.getBytes());

			msgToInboundQueue.setMessageId(msgID);

			TestLog.logPass("Request Message: {0} \r\n", requestBody);
			sendMessage(serviceBus, msgToInboundQueue);
		}
	}

	/**
	 * gets the request body from the template file
	 * 
	 * @param requestBody
	 * @param templateFile
	 * @param contentType
	 * @return
	 */
	public static String getRequestBodyFromTemplate(String requestBody, String templateFile, String contentType) {


		// Get request body using template And/or requestBody data column
		if (!templateFile.isEmpty()) {
			String templateFilePath = DataHelper.getTemplateFile(templateFile);

			// contents of templateFile become the requestBody
			if (requestBody.isEmpty()) {
				//TODO: uncomment And fix
			//	requestBody = DataObjectHelper.convertTemplateToString(templateFilePath);
			} else {
				// contents of requestBody replace values in templateFile
				//TODO: uncomment And fix
			//	requestBody = Utils.requestBodyFromTemplateFile(templateFilePath, requestBody, contentType);
			}
		}
		return requestBody;
	}
	
	/**
	 * format:
	 *  host: Host1
	 *  
	 *  gets the receiver based on the host from hashmap
	 * @param serviceBus
	 * @param options
	 */
	public static IMessageReceiver getReceiver(serviceBus serviceBus, String hostSelector) {
		if(!serviceBus.hosts.contains(hostSelector)) Helper.assertFalse("host receiver not available: " + hostSelector);
		return serviceBus.receivers.get(hostSelector);	
	}

	public static boolean receiveAndVerifyOutboundMessage(serviceBus serviceBus, String msgId, String options,
			String requestBody, String outputParams, String partialExpStr) {
		String outboundQueueMsg = "";
		Collection<IMessage> msgFromOutboundQueue;
		CopyOnWriteArrayList<IMessage> filteredMessages = new CopyOnWriteArrayList<>();

		// gets the host from the options
		String hostSelector = DataHelper.getTagValue(options, "host");

		IMessageReceiver receiver = getReceiver(serviceBus, hostSelector);

		int expectedMessageCount = getExpectedMessageCount(partialExpStr);
		TestLog.logPass("requestIdentifier: " + msgId);

		// return pass if no response check is required by setting all 3 fields as SKIP
		// or is empty
		if (isNoResponseExpected(outputParams,partialExpStr)) {
			return true;
		}

		int maxRetrySeconds = SERVICE_BUS_TIMEOUT_SECONDS;
		boolean isTestPass = false;
		boolean isPartialExpStr = false;
		// is checking for partial expected
		if (!partialExpStr.isEmpty())
			isPartialExpStr = true;

		// servicebus will run for maxRetrySeconds to retrieve matching outbound message
		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		long lastLogged = 0;
		int interval = 10; // log every 10 seconds 
		do {
			lastLogged = logPerInterval(interval, watch, lastLogged);

			msgFromOutboundQueue = getOutboundMessages(receiver);

			// adds to the master list of outbound messages And filters messages for the
			// current test.
             
			filteredMessages.addAll(
					filterOUtboundMessage(hostSelector, requestBody, partialExpStr,  msgFromOutboundQueue, msgId));

			// check for empty results, if expected by the test
			if (isEmptyResultsExpectedAndVerified(filteredMessages, partialExpStr)) {
				return true;
			}

			// TODO: remove if not needed.
			// if the expected message cannot be matched with the inbound, Then do not use
			// filter
			/*
			 * if (filteredMessages.isEmpty() && msgFromOutboundQueue != null) { for
			 * (Entry<IMessage, Boolean> entry : outboundMessages.entrySet()) { IMessage
			 * message = entry.getKey(); filteredMessages.add(message); } }
			 */
			// verify the message exists
			Iterator<IMessage> messages = filteredMessages.iterator();
			while (messages.hasNext()) {
				IMessage message = messages.next();
				TestLog.logPass("filteredMessages: " + filteredMessages.size());

				outboundQueueMsg = new String(message.getBody());

				// log messages received after filtering
				TestLog.logPass("Message received: {0}", outboundQueueMsg);

				if (isNoResponseExpected(outboundQueueMsg, outputParams, partialExpStr)) {
					return true;
				}

		
				// verifies first partial expected string And removes that partial message from
				// partialExpStr
				partialExpStr = comparePartialExpected(outboundQueueMsg, message, receiver,
						partialExpStr);
				// verifies if partialExpStr is empty
				boolean comparePartialExpected = isPartialExpect(partialExpStr, isPartialExpStr);

				if (comparePartialExpected ) {
					isTestPass = true;
					XmlHelper.addOutputParamValuesToConfig(outputParams, outboundQueueMsg);
					break;
				}

			}
			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
		} while (!isTestPass && passedTimeInSeconds < maxRetrySeconds);

		// fail test if no messages are received And isTestPass is false
		if (!isTestPass && filteredMessages.isEmpty()) {
			if(msgId.isEmpty())
				Helper.assertTrue("No messages received. SB verification test, please investigate previous test for proper outbound messag", false);
			Helper.assertTrue("No messages received. msgId: " + msgId, false);
		}
		
		int receivedMessageCount = filteredMessages.size();
		//TODO: additional testing required for message count verification
		//Assert.assertEquals(receivedMessageCount, expectedMessageCount,"wrong number of outbound messages received");
		return isTestPass;

	}
	
	public static int getExpectedMessageCount(String partialExpStr) {
		
		if(!partialExpStr.isEmpty()) {
			 String[] values = partialExpStr.split("&&");
			 return values.length;
		}
		return 1;
	}

	public static long logPerInterval(int interval, StopWatchHelper watch, long lastLogged) {
		long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
		if (passedTimeInSeconds > interval && passedTimeInSeconds - lastLogged > interval) {
			TestLog.logPass("waiting: " + watch.time(TimeUnit.SECONDS) + " seconds");
			lastLogged = passedTimeInSeconds;
		}
		return lastLogged;
	}

	/**
	 * returns true if
	 * 
	 * @param filteredMessages
	 * @param expStr
	 * @param partialExpStr
	 * @param notExpStr
	 * @return
	 */
	public static boolean isEmptyResultsExpectedAndVerified(Collection<IMessage> filteredMessages,
			String partialExpStr) {

		boolean isPartialExpStrEmpty = partialExpStr.equalsIgnoreCase(Config.getValue(EMPTY_CHECK));
		String outboundQueueMsg = "";
		if (isPartialExpStrEmpty) {
			if (!filteredMessages.isEmpty())
				outboundQueueMsg = new String(filteredMessages.iterator().next().getBody());
			Helper.assertTrue("No outbound messages should be received, But got message of length: "
					+ String.valueOf(outboundQueueMsg.length()),
					filteredMessages.isEmpty());
			return true;
		}
		return false;
	}

	public static boolean isNoResponseExpected(String outboundQueueMsg, String outputParams,
			String partialExpStr) {
		if (noCheckNeeded(partialExpStr)) {
			// Put outboundQueueMsg to outputParams
			// Verify the outbound message from other test frameworks
            if (!outputParams.isEmpty() && (outboundQueueMsg.contains(FORM_SUBMITTED) || outboundQueueMsg.contains(HOST_FORM_SUBMITTED))) {                    

				DataHelper.addOutputMessageToConfigParams(outputParams, outboundQueueMsg);
			}
			// Not checking anything in outbound queue, so add in small
			// wait until host order created And also SQL DB order table updated
            Helper.waitForSeconds(3);
			return true;
		}
		return false;
	}
	
	

	public static boolean isNoResponseExpected(String outputParams,String partialExpStr) {
		if ( noCheckNeeded(partialExpStr)) {
			Helper.waitForSeconds(10);
			return true;
		}
		return false;
	}
	
    /** Returns true if content is empty or contains the noCheckNeeded token
    * 
    * @param expStrContent
    * @return boolean 
    */
   public static boolean noCheckNeeded(String expStrContent){      
   	
       return (StringUtils.isEmpty(expStrContent) || expStrContent.equalsIgnoreCase(Config.getValue(NO_CHECK_NEEDED)));
   } 


	/**
	 * filter outbound messages based on messageId, order number or useId/bu if
	 * message comes from service bus And has request body, Then match message id if
	 * not from service bus, try to match either order number or user id/bu
	 * 
	 * @param requestBody
	 * @param expStr
	 * @param partialExpStr
	 * @param notExpStr
	 * @param msgFromOutboundQueue
	 * @param msgId
	 * @return
	 */
	public static Collection<IMessage> filterOUtboundMessage(String hostSelector, String requestBody, String partialExpStr,
			 Collection<IMessage> msgFromOutboundQueue, String msgId) {

		if (StringUtils.isEmpty(hostSelector)) {
			hostSelector = HOST1;
		}
		
		// filter messages for the current test
		CopyOnWriteArrayList<IMessage> filteredMessages = new CopyOnWriteArrayList<IMessage>();

		// if request body exists, filter on message id
		// if request body exists And no message found, return all messages with no
		// message id
		if (!requestBody.isEmpty()) {
			filteredMessages = findMessages(hostSelector, msgId);

			if (filteredMessages.isEmpty() && !requestBody.contains("MsgID")) {
				TestLog.logPass("requestIdentifier: no message id" );
				filteredMessages = findMessagesNotContaining(hostSelector, "correlationid");
			}
			return filteredMessages;

		}

		// if request body is empty, get order number or task number from expected
		// results
		if (requestBody.isEmpty()) {
			filteredMessages = FilterBasedOnIdentifierInExpectedMessages(hostSelector, "OrderNumber",  partialExpStr);
			if (filteredMessages.isEmpty())
				filteredMessages = FilterBasedOnIdentifierInExpectedMessages(hostSelector, "TaskNumber", partialExpStr);
			if (!filteredMessages.isEmpty()) {
				TestLog.logPass("requestIdentifier: " + "OrderNumber or TaskNumber");
				return filteredMessages;
			}
		}

		// if request body is empty, And no order number exists, filter based on user id
		// And bu
		//TODO: uncomment And fix
/*
		if (requestBody.isEmpty()) {
			String userIdetifier = BusinessUnitObject.getApiUserBaseId("1");
			String userIdentifier = "PAR_US" + userIdetifier;
			String buIdentifier = "PAR_BU" + userIdetifier;
			filteredMessages = findMessages(hostSelector, userIdentifier);
			if (filteredMessages.isEmpty())
				filteredMessages = findMessages(hostSelector, buIdentifier);
			if (!filteredMessages.isEmpty()) {
				TestLog.logPass("requestIdentifier: " + "User info: " + userIdentifier + " bu: " + buIdentifier);
				return filteredMessages;
			}
		}
		*/
		return filteredMessages;
	}

	/**
	 * add message from outbound to message list, including the time of adding the
	 * message boolean value added indicating the message is available to be
	 * verified
	 * 
	 * @param msgFromOutboundQueue
	 */
	public static void addMessages(String hostSelector, Collection<IMessage> msgFromOutboundQueue) {
		for (IMessage message : msgFromOutboundQueue) {
						
			//TODO: not all outbound messages contain host information. need to get correct host for outbound message
			if(hostSelector.toLowerCase().contains(HOST1.toLowerCase())) hostSelector = HOST1;
			else if(hostSelector.toLowerCase().contains(HOST2.toLowerCase())) hostSelector = HOST2;
			
            message.setLabel(hostSelector);
			outboundMessages.put(message, true);
			TestLog.logPass("global message size in outbound list: " + outboundMessages.size());
		}
	}

	/**
	 * find the identifier value in the expected messages eg. key = OrderNumber,
	 * will return the order number value if exists in the expected messages the key
	 * value is used to identify the outbound message
	 * 
	 * @param key
	 * @param expStr
	 * @param partialExpStr
	 * @param notExpStr
	 * @return
	 */
	public static CopyOnWriteArrayList<IMessage> FilterBasedOnIdentifierInExpectedMessages(String hostSelector, String key, 
			String partialExpStr) {
		String orderValue = "";
		CopyOnWriteArrayList<IMessage> filteredMessages = new CopyOnWriteArrayList<IMessage>();

		
		 if (!partialExpStr.isEmpty())
			orderValue = DataHelper.getTagValue(partialExpStr, key);
		if (!orderValue.isEmpty())
			filteredMessages = findMessages(hostSelector, orderValue);
		return filteredMessages;
	}

	public static CopyOnWriteArrayList<IMessage> findMessages(String hostSelector, String requestIdentifier) {
		CopyOnWriteArrayList<IMessage> filteredMessages = new CopyOnWriteArrayList<IMessage>();
		String outboundQueueMsg;

		for (Entry<IMessage, Boolean> entry : outboundMessages.entrySet()) {
			IMessage message = entry.getKey();

			outboundQueueMsg = new String(message.getBody());
            String messageHost = message.getLabel();
            
//            boolean isMessageFound1 = entry.getValue().equals(true) && hostSelector.equalsIgnoreCase(messageHost) && outboundQueueMsg.contains(requestIdentifier);
//            boolean isMessageFound2 = entry.getValue().equals(true) &&  outboundQueueMsg.contains(requestIdentifier);
//            if(isMessageFound1 != isMessageFound2) {
//				ApiLogger.log(Level.SEVERE,"wrong message rejected : "+ hostSelector + " messageHost: " + messageHost + " requestIdentifier: " + requestIdentifier );
//
//            }
            
			if (entry.getValue().equals(true) &&  outboundQueueMsg.toLowerCase().contains(requestIdentifier.toLowerCase())) {
//				if(!hostSelector.equalsIgnoreCase(messageHost)) {
//					ApiLogger.log(Level.SEVERE,"host not matching : "+ hostSelector + " messageHost: " + messageHost + " requestIdentifier: " + requestIdentifier );
//				}
				
				filteredMessages.add(message);
				outboundMessages.put(message, false);
			}
		}

		return filteredMessages;
	}

	/**
	 * finds message in outbound that do not contain the specified identifier
	 * matching is done with lower case
	 * 
	 * @param requestIdentifier
	 * @return
	 */
	public static CopyOnWriteArrayList<IMessage> findMessagesNotContaining(String hostSelector, String requestIdentifier) {
		CopyOnWriteArrayList<IMessage> filteredMessages = new CopyOnWriteArrayList<IMessage>();
		String outboundQueueMsg;

		for (Entry<IMessage, Boolean> entry : outboundMessages.entrySet()) {
			IMessage message = entry.getKey();

			outboundQueueMsg = new String(message.getBody());
            String messageHost = message.getLabel();

			if (entry.getValue().equals(true) && !outboundQueueMsg.toLowerCase().contains(requestIdentifier.toLowerCase())) {
				filteredMessages.add(message);
				outboundMessages.put(message, false);
			}
		}

		return filteredMessages;
	}

	/**
	 * returns true if string is an int
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	public static boolean isFromCurrentTest(String outboundQueueMsg) {

		return true;
	}

	/**
	 * sends message to inbound queue
	 * 
	 * @param serviceBus
	 * @param msgToInboundQueue
	 */
	public static void sendMessage(serviceBus serviceBus, IMessage msgToInboundQueue) {
		try {
			serviceBus.sender.send(msgToInboundQueue);
		} catch (InterruptedException | ServiceBusException ex) {
			TestLog.ConsoleLogWarn(ex.getMessage());
		}
	}

	/**
	 * gets message from outbound queue
	 * 
	 * @param receiver
	 * @return
	 */
	public static Collection<IMessage> getOutboundMessages(IMessageReceiver receiver) {

		// get outbounded messages using batch receive
		Collection<IMessage> msgFromOutboundQueue = new ArrayList<IMessage>();
		try {
			msgFromOutboundQueue = receiver.receiveBatch(500, Duration.ofSeconds(5));

		} catch (InterruptedException | ServiceBusException ex) {
			ex.getMessage();
		}

		// print correlation id of message received
		if (msgFromOutboundQueue != null) {
			for (IMessage message : msgFromOutboundQueue) {
				String correlationId = message.getCorrelationId();

				if (correlationId != null)
					TestLog.logPass("messageId received. correlationId: " + correlationId);
				else {
					String outboundMessage = new String(message.getBody());
					if (outboundMessage.contains(MESSAGE_ID_PREFIX)) {
						correlationId = DataHelper.getTagValue(outboundMessage, "MsgCorrelationID");
						TestLog.logPass("messageId received. correlationId: " + correlationId);
					}

				}
			}
		}

		// add outbound messages to the global message list
		if (msgFromOutboundQueue != null)
			addMessages(receiver.getEntityPath(), msgFromOutboundQueue);

		return msgFromOutboundQueue;
	}

	/**
	 * compares outbound queue message to expected message
	 * 
	 * @param outboundQueueMsg
	 * @param expStr
	 * @return
	 */
	public static boolean compareExpected(String outboundQueueMsg, String expStr) {
		//
		// Compare expected
		//

		if (!noCheckNeeded(expStr)) {

			// Empty check
			if (expStr.equalsIgnoreCase(Config.getValue(EMPTY_CHECK))) {
				Helper.assertTrue("No outbound messages should be received, But got message of length: "
						+ String.valueOf(outboundQueueMsg.length()),
						outboundQueueMsg.isEmpty());
			} else {
				// Assert.assertFalse(outboundQueueMsg.isEmpty(), "No messages received.");

				expStr = DataHelper.replaceParameters(expStr);

				Diff diffWithExpected = DiffBuilder.compare(Input.fromString(outboundQueueMsg))
						.withTest(Input.fromString(expStr)).ignoreComments().ignoreWhitespace().checkForSimilar()
						.withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes)).build();
				if (diffWithExpected.hasDifferences()) {
					TestLog.ConsoleLogWarn(diffWithExpected.toString());
				}
				// Assert.assertFalse(diffWithExpected.hasDifferences(),
				// diffWithExpected.toString());
				return diffWithExpected.hasDifferences();
			}

		}
		return false;
	}

	public static boolean isPartialExpect(String partialExpStr, boolean isPartialExpStr) {

		if (isPartialExpStr)
			return partialExpStr.isEmpty() || partialExpStr.equalsIgnoreCase(Config.getValue(EMPTY_CHECK));
		return false;
	}

	/**
	 * compares outbound queue message to partially expected message
	 * 
	 * @param outboundQueueMsg
	 * @param msgFromOutboundQueue
	 * @param receiver
	 * @param outputParams
	 * @param partialExpStr
	 * @return
	 */
	public static String comparePartialExpected(String outboundQueueMsg, IMessage msgFromOutboundQueue,
			IMessageReceiver receiver, String partialExpStr) {

		//
		// Compare partial expected
		//
		if (!noCheckNeeded(partialExpStr)) {

			// Empty check
			if (partialExpStr.equalsIgnoreCase(Config.getValue(EMPTY_CHECK))) {
				// Assert.assertTrue(outboundQueueMsg.isEmpty(), "No outbound messages should be
				// received, But got message of length: " +
				// String.valueOf(outboundQueueMsg.length()));
			} else {
				// Assert.assertFalse(outboundQueueMsg.isEmpty(), "No messages received.");

				// partialExpStr = Utils.replaceParameters(partialExpStr);

				// Split up into the different messages that we want to check in the outbound
				// queue.
				// Note that these messages is allowed to arrive in any order, But they all
				// need to show up for the test to pass.
				LinkedList<String> partialExpStrList = new LinkedList<>(Arrays.asList(partialExpStr.split(AND_TOKEN)));
				Iterator<String> expectedStringsIter = partialExpStrList.iterator();
			
				//TODO: uncomment And fix
/*
				XmlConverter saxXmlReader = XmlConverter.getInstance();
				boolean testPassed = false;
				ArrayList<String> failedXPaths = new ArrayList<>();

				// For each expected string, see if it matches the outbound queue message.
				// If a match is found, remove from it from the list of expected strings
				// to check for on the next iteration And read the next message in the queue.
				// If the outbound queue message doesn't match any expected strings, Then the
				// test case fails.
				// The test pass When all of the expected strings have matched to their
				// corresonding
				// outbound queue message.
				String expectedString = (String) expectedStringsIter.next();
				ArrayList<String> xPathList = saxXmlReader.getXPaths(expectedString);

				// Assert.assertNotNull(xPathList, "Cannot obtain xpaths list from string: " +
				// expectedString);
				boolean foundMatch = true;
				failedXPaths = new ArrayList<>();

				if (xPathList == null)
					return partialExpStr;

				Iterator<String> xPathIter = xPathList.iterator();
				while (xPathIter.hasNext()) {
					String xpath = xPathIter.next();
					// TEMPORARY skip test for WorkOrderNumber
					if (xpath.startsWith(
							"//*[local-name()='Message' And namespace-uri()='urn:soi.ventyx.com:message:V1_3'][1]/*[local-name()='Payload' And namespace-uri()='urn:soi.ventyx.com:message:V1_3'][1]/*[local-name()='WorkOrder' And namespace-uri()='urn:soi.ventyx.com:payload:V1_3'][1]/*[local-name()='WorkOrderRecord' And namespace-uri()='urn:soi.ventyx.com:payload:V1_3'][1]/*[local-name()='OrderNumber' And namespace-uri()='urn:soi.ventyx.com:payload:V1_3']")) {
						continue;
					}
					HasXPathMatcher xpathMatcher = new HasXPathMatcher(xpath);
					boolean xpathMatch = false;
					try {
						xpathMatch = xpathMatcher.matches(outboundQueueMsg);
					} catch (Exception e) {
						e.getMessage();
					}
					if (!xpathMatch) {
						foundMatch = false;
						failedXPaths.add(xpath);
						break;
					}

				}

				// If found, we remove the expected string from collection.
				if (foundMatch) {
					partialExpStrList.remove(expectedString);
					expectedStringsIter = partialExpStrList.iterator();
					return String.join("&&", partialExpStrList);

				}
				*/
			}

		}
		return partialExpStr;
	}

	public static boolean compareNotExpected(String outboundQueueMsg, String notExpStr) {
		//
		// Compare not expected
		//

		if (!noCheckNeeded(notExpStr)) {

			// Empty check
			if (notExpStr.equalsIgnoreCase(Config.getValue(EMPTY_CHECK))) {
				if (!outboundQueueMsg.isEmpty())
					return true;
				// Assert.assertFalse(outboundQueueMsg.isEmpty(), "Outbound messages was
				// expected, But got none.");
			} else {
				// Assert.assertFalse(outboundQueueMsg.isEmpty(), "No messages received.");

				notExpStr = DataHelper.replaceParameters(notExpStr);
				//TODO: uncomment And fix
/*
				XmlConverter saxXmlReader = XmlConverter.getInstance();
				ArrayList<String> xPathList = saxXmlReader.getXPaths(notExpStr);
				Helper.assertTrue("Cannot obtain xpaths list from string: " + notExpStr, xPathList != null);
				for (String xpath : xPathList) {

					// TEMPORARY skip test for WorkOrderNumber
					if (!xpath.startsWith(
							"//*[local-name()='Message' And namespace-uri()='urn:soi.ventyx.com:message:V1_3'][1]/*[local-name()='Payload' And namespace-uri()='urn:soi.ventyx.com:message:V1_3'][1]/*[local-name()='WorkOrder' And namespace-uri()='urn:soi.ventyx.com:payload:V1_3'][1]/*[local-name()='WorkOrderRecord' And namespace-uri()='urn:soi.ventyx.com:payload:V1_3'][1]/*[local-name()='OrderNumber' And namespace-uri()='urn:soi.ventyx.com:payload:V1_3']")) {
						assertThat(outboundQueueMsg, not(HasXPathMatcher.hasXPath(xpath)));
					}
				}
				*/
			}
			
		}
		return false;
	}

	/**
	 * Disabled, due to parallelization optimization. we cannot have purges in middle of test runs
	 */
	public static void purgeQueues() {
		TestLog.logPass("purgeServiceBusReceivers is disabled. Queued messages are not purged.");
	}

	/**
	 * Purge by type. eg. DEFAULT
	 */
	public static void purgeOutboundQueues() {
		TestLog.logPass("purging queue messages from service bus by types");
		
		String[] types = Config.getValue(PURGE_SB_RECEIVERS).split(",");
		
		for(String type : types) {
			purgeReceivers(sbInstance.get(type));
		}
	}

	/**
	 * Purge the receivers.
	 */
	private static void purgeReceivers(serviceBus serviceBus) {	
		try {
			
			for (Entry<String, IMessageReceiver> entry : serviceBus.receivers.entrySet()) {		
				if (entry.getValue() != null) {
					while (entry.getValue().receiveBatch(100, Duration.ofSeconds(1)) != null) {
					}
				}
			}	
		} catch (ServiceBusException | InterruptedException ex) {
			TestLog.ConsoleLogWarn(ex.getMessage());
		}
	}
	
	class serviceBus {
		boolean isInitiated = false;
		Map<String, IMessageReceiver> receivers = new ConcurrentHashMap<String, IMessageReceiver>();
		IMessageSender sender = null;
		String connectionStr = "";
		String inboundQueue = "";
		String inbound = "";
		String outboundTopic = "";
		String outbound = "";	
		List<String> hosts = new ArrayList<String>();
	}	
	

}
