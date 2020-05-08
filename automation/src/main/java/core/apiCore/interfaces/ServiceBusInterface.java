package core.apiCore.interfaces;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.MessageQueueHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.MessageObject;
import core.support.objects.MessageObject.messageType;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

/**
 * @author ehsan.matean
 *
 */
public class ServiceBusInterface {

	public static final String SERVICEBUS_CONNECTION_STR = "servicebus.connectionString";
	public static final String SERVICEBUS_QUEUE = "servicebus.queue";
	public static final String SERVICEBUS_OUTBOUND_QUEUE = "servicebus.outbound.queue";
	public static final String SERVICEBUS_TOPIC = "servicebus.topic";
	public static final String SERVICEBUS_OUTBOUND_TOPIC = "servicebus.outbound.topic";
	public static final String SERVICEBUS_HOST = "servicebus.host";
	public static final String SERVICEBUS_MESSAGE_ID_PREFIX = "servicebus.msgId.prefix";

	public static Connection connection = null;
	public static Channel channel;

	/**
	 * interface for database calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static void testServicebusInterface(ServiceObject serviceObject) throws Exception {

		// evaluate additional options
		evaluateOption(serviceObject);

		// replace parameters for request body, including template file (json, xml, or
		// other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

		// generate message id
		String messageId = MessageQueueHelper.generateMessageId(serviceObject,
				Config.getValue(SERVICEBUS_MESSAGE_ID_PREFIX));

		// send message
		sendServiceBusMessage(serviceObject, messageId);

		// receive messages
		MessageQueueHelper.receiveAndValidateMessages(serviceObject, messageId, messageType.SERVICEBUS);
	}

	/**
	 * send rabbitMq message
	 * 
	 * @param apiObject
	 * @throws ServiceBusException
	 * @throws InterruptedException
	 */
	public static void sendServiceBusMessage(ServiceObject serviceObject, String messageId)
			throws InterruptedException, ServiceBusException {
		TestLog.ConsoleLog("rabbitMq request body: " + serviceObject.getRequestBody());

		// Create a QueueClient instance and then asynchronously send messages.
		// Close the sender once the send operation is complete.

		String connectionString = Config.getValue(SERVICEBUS_CONNECTION_STR);
		String topic = Config.getValue(SERVICEBUS_TOPIC);
		String queue = Config.getValue(SERVICEBUS_QUEUE);

		QueueClient sendQueueClient = new QueueClient(new ConnectionStringBuilder(connectionString, queue),
				ReceiveMode.PEEKLOCK);
		TopicClient sendClient = new TopicClient(new ConnectionStringBuilder(connectionString, topic));

		if (!queue.isEmpty())
			sendMessagesAsync(serviceObject, messageId, sendClient).thenRunAsync(() -> sendQueueClient.closeAsync());
		else if (!topic.isEmpty())
			sendMessagesAsync(serviceObject, messageId, sendClient).thenRunAsync(() -> sendClient.closeAsync());
	}

	@SuppressWarnings("rawtypes")
	static CompletableFuture<Void> sendQeueMessagesAsync(ServiceObject serviceObject, String messageId,
			QueueClient sendClient) {

		List<CompletableFuture> tasks = new ArrayList<>();
		Message message = new Message(serviceObject.getRequestBody().getBytes());
		message.setContentType(serviceObject.getContentType());
		message.setLabel(messageId);
		message.setMessageId(messageId);
		message.setTimeToLive(Duration.ofMinutes(2));
		TestLog.logPass("Message sending: Id = " + message.getMessageId() + "\n message: " + message);
		tasks.add(sendClient.sendAsync(message).thenRunAsync(() -> {
			System.out.printf("Message acknowledged: Id = " + message.getMessageId());
		}));

		return CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[tasks.size()]));
	}

	@SuppressWarnings("rawtypes")
	static CompletableFuture<Void> sendMessagesAsync(ServiceObject serviceObject, String messageId,
			TopicClient sendClient) {

		List<CompletableFuture> tasks = new ArrayList<>();
		Message message = new Message(serviceObject.getRequestBody().getBytes());
		message.setContentType(serviceObject.getContentType());
		message.setLabel(messageId);
		message.setMessageId(messageId);
		message.setTimeToLive(Duration.ofMinutes(2));
		TestLog.logPass("Message sending: Id = " + message.getMessageId() + "\n message: " + message);
		tasks.add(sendClient.sendAsync(message).thenRunAsync(() -> {
			System.out.printf("\n\tMessage acknowledged: Id = %s", message.getMessageId());
		}));

		return CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[tasks.size()]));
	}

	public static void evaluateOption(ServiceObject serviceObject) {

		// set default queue and exchange values. will be overwritten if values are set
		// in csv
		resetOptions();

		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return;
		}

		// store value to config directly using format: value:<$key> separated by colon
		// ';'
		DataHelper.saveDataToConfig(serviceObject.getOption());

		// replace parameters for options
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key.toLowerCase()) {

			case "topic":
				Config.putValue(SERVICEBUS_TOPIC, keyword.value);
				break;
			case "outbound_topic":
				Config.putValue(SERVICEBUS_OUTBOUND_TOPIC, keyword.value);
				break;
			case "host":
				Config.putValue(SERVICEBUS_HOST, keyword.value);
				break;
			case "queue":
				Config.putValue(SERVICEBUS_QUEUE, keyword.value);
				break;
			case "response_identifier":
				Config.putValue(MessageQueueHelper.RESPONSE_IDENTIFIER, keyword.value);
			default:
				break;
			}
		}
	}

	/**
	 * set default queue, topic and host
	 */
	private static void resetOptions() {

		String defaultTopic = TestObject.getGlobalTestInfo().config.get(SERVICEBUS_TOPIC).toString();
		String outboundTopic = TestObject.getGlobalTestInfo().config.get(SERVICEBUS_OUTBOUND_TOPIC).toString();
		String defaultQueue = TestObject.getGlobalTestInfo().config.get(SERVICEBUS_QUEUE).toString();
		String defaultHost = TestObject.getGlobalTestInfo().config.get(SERVICEBUS_HOST).toString();

		Config.putValue(SERVICEBUS_TOPIC, defaultTopic, false);
		Config.putValue(SERVICEBUS_OUTBOUND_TOPIC, outboundTopic, false);
		Config.putValue(SERVICEBUS_QUEUE, defaultQueue, false);
		Config.putValue(SERVICEBUS_HOST, defaultHost, false);
		Config.putValue(MessageQueueHelper.RESPONSE_IDENTIFIER, StringUtils.EMPTY, false);
	}

	/**
	 * close connection
	 */
	public static void closeConnection() {
		try {
			channel.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * gets message from outbound queue Adds messages to ouboutMessage hashmap
	 * 
	 * @param receiver
	 * @return
	 * @throws Exception
	 */
	public static void getOutboundMessages() throws Exception {
		String connectionString = Config.getValue(SERVICEBUS_CONNECTION_STR);
		String topic = Config.getValue(SERVICEBUS_TOPIC);
		String outboundTopic = Config.getValue(SERVICEBUS_OUTBOUND_TOPIC);
		String host = Config.getValue(SERVICEBUS_HOST);

		// set outbound topic if defined
		if (!outboundTopic.isEmpty())
			topic = outboundTopic;

		String entityPath = topic + "/subscriptions/" + host;
		SubscriptionClient subscription1Client = new SubscriptionClient(
				new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

		registerMessageHandlerOnClient(subscription1Client);
	}

	@SuppressWarnings("deprecation")
	static void registerMessageHandlerOnClient(SubscriptionClient receiveClient) throws Exception {

		// register the RegisterMessageHandler callback
		IMessageHandler messageHandler = new IMessageHandler() {
			// callback invoked when the message handler loop has obtained a message
			public CompletableFuture<Void> onMessageAsync(IMessage message) {

				MessageObject messageObject = new MessageObject().withMessageType(messageType.SERVICEBUS)
						.withMessageId(message.getMessageId()).withCorrelationId(message.getCorrelationId())
						.withMessage(message.getMessageBody().getValueData().toString()).withLabel(message.getLabel());

				TestLog.logPass("Received messageId '" + message.getMessageId() + "\n with message content: "
						+ message.getMessageBody().getValueData());
				MessageObject.outboundMessages.put(messageObject, true);

				return receiveClient.completeAsync(message.getLockToken());
			}

			@Override
			public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
				System.out.printf(exceptionPhase + "-" + throwable.getMessage());
			}

		};

		receiveClient.registerMessageHandler(messageHandler,
				// callback invoked when the message handler has an exception to report
				// 1 concurrent call, messages are auto-completed, auto-renew duration
				new MessageHandlerOptions(1, false, Duration.ofMinutes(1)));

	}

}