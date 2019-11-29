package core.apiCore.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import core.apiCore.helpers.DataHelper;
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
		
		// replace parameters for request body, including template file (json, xml, or other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

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
	 * send rabbitMq message
	 * 
	 * @param apiObject
	 */
	public static void sendRabbitMqMessage(ServiceObject serviceObject) {
		TestLog.ConsoleLog("rabbitMq request body: " + serviceObject.getRequestBody());

        // set basic properties
        BasicProperties props = evaluateRequestHeaders(serviceObject);
		
		String exchange = Config.getValue(RABBIT_MQ_EXCHANGE);
		String queueName = Config.getValue(RABBIT_MQ_QUEUE);
		try {
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
		Map<String,Object>  map = new HashMap<String,Object>(); 
	
		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getRequestHeaders());
		
		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional request headers
			switch (keyword.key) {

			default:
				map.put(keyword.key, keyword.value);
				break;
			}
		}
		
		props = props.builder().headers(map).build();
		return props;
	}

	public static void evaluateOption(ServiceObject serviceObject) {
		
		// set default queue and exchange values. will be overwritten if values are set in csv
		setDefaultQueueAndExchange();
		
		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return;
		}

		// replace parameters for  options
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		
		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key.toLowerCase()) {

			case "exchange":
				Config.putValue(RABBIT_MQ_EXCHANGE, keyword.value);
				break;
			case "queue":
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