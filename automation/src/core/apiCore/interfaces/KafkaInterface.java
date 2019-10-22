package core.apiCore.interfaces;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.XmlHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;

/**
 * @author ehsan.matean
 *
 */
public class KafkaInterface {

	public static final String KAFKA_SERVER_URL = "kafka.server.url";
	public static final String KAFKA_SERVER_PORT = "kafka.server.port";
	public static final String KAFKA_CLIENT_ID = "kafka.client.id";
	public static final String KFAKA_TOPIC = "kafka.topic";

	private static KafkaProducer<byte[], byte[]> producer;

	/**
	 * interface for database calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static void testRabbitMqInterface(ServiceObject serviceObject) throws Exception {

		// connect to producer
		setupProducer(serviceObject);

		// send message
		sendKafkaMessage(serviceObject);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public synchronized static void setupProducer(ServiceObject apiObject) {

		Properties properties = new Properties();
		properties.put("bootstrap.servers",
				Config.getValue(KAFKA_SERVER_URL) + ":" + Config.getValue(KAFKA_SERVER_PORT));
		properties.put("client.id", Config.getValue(KAFKA_CLIENT_ID));
		properties.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
		properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		properties.put("retries", "3");

		producer = new KafkaProducer<byte[], byte[]>(properties);
	}

	/**
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static void sendKafkaMessage(ServiceObject serviceObject) throws Exception {

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
	 * send kafka message
	 * 
	 * @param serviceObject
	 */
	public static void sendMessage(ServiceObject serviceObject) {

		String messageBody = serviceObject.getRequestBody();
		try {
			producer.send(new ProducerRecord<byte[], byte[]>(Config.getValue(KFAKA_TOPIC), messageBody.getBytes()))
					.get();

		} catch (Exception e) {
			e.printStackTrace();
		}

		TestLog.ConsoleLog("Sent message: " + serviceObject.getRequestBody());
	}

	/**
	 * close connection
	 */
	public static void closeConnection() {
		producer.close();
	}
}