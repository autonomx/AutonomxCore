package core.apiCore.interfaces;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.XmlHelper;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;

/**
 * @author ehsan.matean
 *
 */
public class RabbitMqInterface {

	private static final String RABBIT_MQ_HOST = "rabbitMq.host";
	private static final String RABBIT_MQ_VIRTUAL_HOST = "rabbitMq.virtualhost";
	private static final String RABBIT_MQ_USER = "rabbitMq.user";
	private static final String RABBIT_MQ_PASS = "rabbitMq.assword";
	private static final String RABBIT_MQ_EXCHANGE = "rabbitMq.xchange";
	private static final String RABBIT_MQ_QUEUE = "rabbitMq.defaultQueue";


	public static Connection connection = null;
	public static Channel channel;

	/**
	 * interface for database calls
	 * 
	 * @param apiObject
	 * @return
	 * @throws Exception
	 */
	public static void testRabbitMqInterface(ServiceObject apiObject) throws Exception {

		// connect to rabbitMq
		connectRabbitMq(apiObject);

		// send message
		sendRabbitMqMessage(apiObject);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public synchronized static void connectRabbitMq(ServiceObject apiObject) {
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
		requestBody = XmlHelper.getRequestBodyFromXmlTemplate(serviceObject);;
		serviceObject.withRequestBody(requestBody);

		// send message
		sendMessage(serviceObject);
	}

	/**
	 * send rabbitMq message
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