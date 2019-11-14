package core.apiCore.interfaces;

import java.util.List;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.XmlHelper;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

/**
 * @author ehsan.matean
 *
 */
public class RabbitMqInterface {

	private static final String RABBIT_MQ_HOST = "rabbitMq.host";
	private static final String RABBIT_MQ_VIRTUAL_HOST = "rabbitMq.virtualhost";
	private static final String RABBIT_MQ_USER = "rabbitMq.user";
	private static final String RABBIT_MQ_PASS = "rabbitMq.password";
	public static final String RABBIT_MQ_EXCHANGE = "rabbitMq.exchange";
	public static final String RABBIT_MQ_QUEUE = "rabbitMq.defaultQueue";
	

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

		// send message
		sendRabbitMqMessage(serviceObject);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public synchronized static void connectRabbitMq(ServiceObject serviceObject) {
		if (channel == null) {
			try {
				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost(Config.getValue(RABBIT_MQ_HOST));
				factory.setUsername(Config.getValue(RABBIT_MQ_USER));
				factory.setPassword(Config.getValue(RABBIT_MQ_PASS));
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
	 * 
	 * @param apiObject
	 * @return
	 * @throws Exception
	 */
	public static void sendRabbitMqMessage(ServiceObject serviceObject) throws Exception {

		// replace parameters for request body
		String requestBody = DataHelper.replaceParameters(serviceObject.getRequestBody());
		serviceObject.withRequestBody(requestBody);

		// Get request body using template and/or requestBody data column
		requestBody = XmlHelper.getRequestBodyFromXmlTemplate(serviceObject);

		serviceObject.withRequestBody(requestBody);

		// send message
		sendMessage(serviceObject);
	}

	/**
	 * send rabbitMq message
	 * 
	 * @param apiObject
	 */
	public static void sendMessage(ServiceObject serviceObject) {
		TestLog.ConsoleLog("rabbitMq request body: " + serviceObject.getRequestBody());

		String exchange = Config.getValue(RABBIT_MQ_EXCHANGE);
		String queueName = Config.getValue(RABBIT_MQ_QUEUE);
		try {
			channel.basicPublish(exchange, queueName, null, serviceObject.getRequestBody().getBytes());
		} catch (Exception e) {
			throw new RuntimeException("Could not send message. ", e);
		}
	}

	public static void evaluateOption(ServiceObject serviceObject) {
		
		// set default queue and exchange values. will be overwritten if values are set in csv
		setDefaultQueueAndExchange();
		
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

			case "EXCHANGE":
				Config.putValue(RABBIT_MQ_EXCHANGE, keyword.value);
				break;
			case "QUEUE":
				Config.putValue(RABBIT_MQ_QUEUE, keyword.value);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * set default queue and exchange values
	 */
	private static void setDefaultQueueAndExchange() {

		String defaultExchange = TestObject.getDefaultTestInfo().config.get(RABBIT_MQ_EXCHANGE).toString();
		String defaultQueue = TestObject.getDefaultTestInfo().config.get(RABBIT_MQ_QUEUE).toString();
		Config.putValue(RABBIT_MQ_EXCHANGE, defaultExchange);
		Config.putValue(RABBIT_MQ_QUEUE, defaultQueue);
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
}