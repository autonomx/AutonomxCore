package core.apiCore.interfaces;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import core.apiCore.helpers.DataHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;

/**
 * @author ehsan.matean
 *
 */
public class KafkaInterface {

	public static final String KAFKA_SERVER_URL = "kafka.bootstrap.servers";
	public static final String KAFKA_CLIENT_ID = "kafka.client.id";
	public static final String KFAKA_TOPIC = "kafka.topic";
	public static final String KAFKA_TIMEOUT_SECONDS = "kafka.timeout.seconds";


	private static KafkaProducer<byte[], byte[]> producer;
	public static Map<ConsumerRecord<Long, String>, Boolean> outboundMessages = new ConcurrentHashMap<ConsumerRecord<Long, String>, Boolean>();

	/**
	 * interface for database calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static void testKafkaInterface(ServiceObject serviceObject) throws Exception {

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
		properties.put("bootstrap.servers", Config.getValue(KAFKA_SERVER_URL));
		//properties.put("client.id", Config.getValue(KAFKA_CLIENT_ID));
		properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
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
		
		// return if request is empty
		if(serviceObject.getRequestBody().isEmpty()) return;

		// replace parameters for request body
		String requestBody = DataHelper.replaceParameters(serviceObject.getRequestBody());
		serviceObject.withRequestBody(requestBody);

		// Get request body using template and/or requestBody data column
	//	requestBody = XmlHelper.getRequestBodyFromXmlTemplate(serviceObject);
		
		serviceObject.withRequestBody(requestBody);

		// send message
		sendMessage(serviceObject);
		
		// receive messages
		getConsumerMessages();
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
	
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void getConsumerMessages() {
		

		Properties props = new Properties();
		props.put("bootstrap.servers", Config.getValue(KAFKA_SERVER_URL));
		props.put("group.id", "KafkaExampleConsumer");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");  
		props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
//		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
		//props.put("partition.assignment.strategy", "range");
		
		KafkaConsumer consumer = new KafkaConsumer(props);
	        consumer.subscribe(Collections.singletonList(Config.getValue(KFAKA_TOPIC)));
	        final int giveUp = 10;   int noRecordsCount = 0;
	        while (true) {
	            final ConsumerRecords<Long, String> consumerRecords =
	                    consumer.poll(Duration.ofMillis(10));
	            
	            TestLog.ConsoleLog("Received record count: " + consumerRecords.count());
	            if (consumerRecords.count()==0) {
	                noRecordsCount++;
	                if (noRecordsCount > giveUp) break;
	                else continue;
	            }
	            consumerRecords.forEach(record -> {
	                System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
	                        record.key(), record.value(),
	                        record.partition(), record.offset());
	                outboundMessages.put(record, true);
	            });
	            consumer.commitAsync();
	        }
	        
	        getMessage();
	        consumer.close(); 
	    }
	 
	 public static void getMessage() {
		 for (Entry<ConsumerRecord<Long, String>, Boolean> entry : outboundMessages.entrySet()) {
				String message = entry.getKey().value();
				TestLog.ConsoleLog("outbound messages: " + message);
		 }
	 }

	/**
	 * close connection
	 */
	public static void closeConnection() {
		producer.close();
	}
}