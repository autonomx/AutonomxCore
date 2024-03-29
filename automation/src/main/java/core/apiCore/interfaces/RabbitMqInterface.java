package core.apiCore.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.MessageQueueHelper;
import core.helpers.Helper;
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
public class RabbitMqInterface {

	public static final String RABBIT_MQ_HOST = "rabbitMQ.host";
	public static final String RABBIT_MQ_PORT = "rabbitMQ.port";
	public static final String RABBIT_MQ_VIRTUAL_HOST = "rabbitMQ.virtualhost";
	public static final String RABBIT_MQ_USER = "rabbitMQ.user";
	public static final String RABBIT_MQ_PASS = "rabbitMQ.password";
	public static final String RABBIT_MQ_EXCHANGE = "rabbitMQ.exchange";
	public static final String RABBIT_MQ_OUTBOUND_EXCHANGE = "rabbitMQ.outbound.exchange";
	public static final String RABBIT_MQ_EXCHANGE_TYPE = "rabbitMQ.exchange.type";

	public static final String RABBIT_MQ_QUEUE = "rabbitMQ.queue";
	public static final String RABBIT_MQ_OUTBOUND_QUEUE = "rabbitMQ.outbound.queue";
	public static final String RABBIT_MQ_QUEUE_DURABLE = "rabbitMQ.Queue.durable";
	public static final String RABBIT_MQ_DECLARE_QUEUE = "rabbitMQ.queue.declare";

	public static final String RABBIT_MQ_MESSAGE_ID_PREFIX = "rabbitMQ.msgId.prefix";

	public static Connection connection = null;
	public static Channel channel;

	/**
	 * interface for database calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static void testRabbitMqInterface(ServiceObject serviceObject) throws Exception {

		// connect to rabbitMq
		connectRabbitMq(serviceObject);

		// evaluate additional options
		evaluateOption(serviceObject);

		// replace parameters for request body, including template file (json, xml, or
		// other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

		// generate message id
		String messageId = MessageQueueHelper.generateMessageId(serviceObject,
				Config.getValue(RABBIT_MQ_MESSAGE_ID_PREFIX));

		// send message
		sendRabbitMqMessage(serviceObject, messageId);

		// receive messages
		MessageQueueHelper.receiveAndValidateMessages(serviceObject, messageId, messageType.RABBITMQ);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public synchronized static void connectRabbitMq(ServiceObject serviceObject) {
		if (channel == null) {
			try {
				ConnectionFactory factory = new ConnectionFactory();
				int port = Config.getIntValue(RABBIT_MQ_PORT);
				String username = Config.getValue(RABBIT_MQ_USER);
				String password = Config.getValue(RABBIT_MQ_PASS);
				String virtualHost = Config.getValue(RABBIT_MQ_VIRTUAL_HOST);

				factory.setHost(Config.getValue(RABBIT_MQ_HOST));
				if (port != -1)
					factory.setPort(port);
				if (!username.isEmpty())
					factory.setUsername(Config.getValue(RABBIT_MQ_USER));
				if (!password.isEmpty())
					factory.setPassword(Config.getValue(RABBIT_MQ_PASS));
				if (!virtualHost.isEmpty())
					factory.setVirtualHost(Config.getValue(RABBIT_MQ_VIRTUAL_HOST));

				connection = factory.newConnection();
				channel = connection.createChannel();
				Helper.waitForSeconds(1);
			} catch (Exception e) {
				e.printStackTrace();
				TestLog.ConsoleLog("Connection failed: " + e.getMessage());
				throw new RuntimeException("Could not connect. ", e);
			}
		}
	}

	/**
	 * send rabbitMq message
	 * 
	 * @param apiObject
	 */
	public static void sendRabbitMqMessage(ServiceObject serviceObject, String messageId) {
		TestLog.ConsoleLog("rabbitMq request body: " + serviceObject.getRequestBody());

		// set basic properties
		BasicProperties props = evaluateRequestHeaders(serviceObject);
		props = props.builder().correlationId(messageId).messageId(messageId).build();

		String exchange = Config.getValue(RABBIT_MQ_EXCHANGE);
		String queueName = Config.getValue(RABBIT_MQ_QUEUE);
		String exchangeType = Config.getValue(RABBIT_MQ_EXCHANGE_TYPE);

		try {
			if (!exchangeType.isEmpty())
				channel.exchangeDeclare(exchange, exchangeType);

			channel.basicPublish(exchange, queueName, props, serviceObject.getRequestBody().getBytes());
		} catch (Exception e) {
			throw new RuntimeException("Could not send message. ", e);
		}
	}

	public static BasicProperties evaluateRequestHeaders(ServiceObject serviceObject) {

		// if no RequestHeaders specified
		if (serviceObject.getRequestHeaders().isEmpty()) {
			return new BasicProperties();
		}

		BasicProperties props = new BasicProperties();
		Map<String, Object> map = new HashMap<String, Object>();

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getRequestHeaders());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional request headers
			switch (keyword.key) {

			default:
				keyword.value = DataHelper.replaceParameters(keyword.value.toString());
				map.put(keyword.key, keyword.value);
				break;
			}
		}

		KeyValue.printKeyValue(keywords, "header");
		props = props.builder().headers(map).build();
		return props;
	}

	public static void evaluateOption(ServiceObject serviceObject) {

		// set default queue and exchange values. will be overwritten if values are set
		// in csv
		resetOptions();

		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return;
		}


		// replace parameters for options
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key.toLowerCase()) {

			case "exchange":
				Config.putValue(RABBIT_MQ_EXCHANGE, keyword.value, false);
				break;
			case "queue":
				Config.putValue(RABBIT_MQ_QUEUE, keyword.value, false);
				break;
			case "outbound_exchange":
				Config.putValue(RABBIT_MQ_OUTBOUND_EXCHANGE, keyword.value, false);
				break;
			case "outbound_queue":
				Config.putValue(RABBIT_MQ_OUTBOUND_QUEUE, keyword.value, false);
				break;
			case "response_identifier":
				Config.putValue(MessageQueueHelper.RESPONSE_IDENTIFIER, keyword.value, false);
			default:
				break;
			}
		}
		
		KeyValue.printKeyValue(keywords, "option");
	}

	/**
	 * set default queue and exchange values
	 */
	private static void resetOptions() {

		String defaultExchange = TestObject.getGlobalTestInfo().config.get(RABBIT_MQ_EXCHANGE).toString();
		String outboundExchange = TestObject.getGlobalTestInfo().config.get(RABBIT_MQ_OUTBOUND_EXCHANGE).toString();
		String defaultQueue = TestObject.getGlobalTestInfo().config.get(RABBIT_MQ_QUEUE).toString();
		String outboundQueue = TestObject.getGlobalTestInfo().config.get(RABBIT_MQ_OUTBOUND_QUEUE).toString();

		Config.putValue(RABBIT_MQ_EXCHANGE, defaultExchange, false);
		Config.putValue(RABBIT_MQ_QUEUE, defaultQueue, false);
		Config.putValue(RABBIT_MQ_OUTBOUND_QUEUE, outboundQueue, false);
		Config.putValue(RABBIT_MQ_OUTBOUND_EXCHANGE, outboundExchange, false);
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
		String queueName = Config.getValue(RABBIT_MQ_QUEUE);
		String outboundQueue = Config.getValue(RABBIT_MQ_OUTBOUND_QUEUE);
		boolean queueDurable = Config.getBooleanValue(RABBIT_MQ_QUEUE_DURABLE);
		boolean isDeclareQueue = Config.getBooleanValue(RABBIT_MQ_DECLARE_QUEUE);

		// set outbound queue if defined
		if (!outboundQueue.isEmpty())
			queueName = outboundQueue;

		if (isDeclareQueue)
			channel.queueDeclare(queueName, queueDurable, false, false, null);

		String exchangeName = Config.getValue(RABBIT_MQ_EXCHANGE);
		String exchangeOutboundName = Config.getValue(RABBIT_MQ_OUTBOUND_EXCHANGE);
		String exchangeType = Config.getValue(RABBIT_MQ_EXCHANGE_TYPE);

		// set outbound exchange if defined
		if (!exchangeOutboundName.isEmpty())
			exchangeName = exchangeOutboundName;

		if (!exchangeType.isEmpty())
			channel.exchangeDeclare(exchangeName, exchangeType);
		if (!exchangeName.isEmpty())
			channel.queueBind(queueName, exchangeName, "");
		
		// get messages in batches
		getBatchMessages(queueName, 10);
	}
	
	/**
	 * attempt to get a number of messages from the queue
	 * @param queueName
	 * @param maxMessages
	 * @throws Exception
	 */
	public static void getBatchMessages(String queueName, int maxMessages) throws Exception {
		int currentMessageCount = 0;
		do {
			GetResponse delivery = channel.basicGet(queueName, true);
	
			if (delivery != null) {
				String messageString = new String(delivery.getBody(), "UTF-8");
	
				MessageObject message = new MessageObject().withMessageType(messageType.RABBITMQ)
						.withMessageId(delivery.getProps().getMessageId())
						.withCorrelationId(delivery.getProps().getCorrelationId()).withMessage(messageString);
	
				if (message != null) {
					TestLog.logPass(
							"Received message with Id: " + message.getMessageId() + " message: " + message.getMessage());
					MessageObject.outboundMessages.put(message, true);
				}
			}else
				break;
			currentMessageCount++;
		}while(currentMessageCount < maxMessages);
	}
}