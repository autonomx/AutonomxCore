package core.apiCore.interfaces;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.MessageQueueHelper;
import core.helpers.StopWatchHelper;
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
	
	public static final String MESSAGE_ID_PREFIX = "kafkaTestMsgID";

	

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
		
		// replace parameters for request body, including template file (json, xml, or other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

		// generate message id
		String messageId = MessageQueueHelper.generateMessageId(serviceObject, MESSAGE_ID_PREFIX);
		
		// send message
		sendKafkaMessage(serviceObject, messageId);
		
		// receive messages
		getConsumerMessages(messageId);
	}

	/**
	 * setup producer
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
	 * send kafka message
	 * 
	 * @param serviceObject
	 */
	public static void sendKafkaMessage(ServiceObject serviceObject, String messageId) {
		
		// return if request is empty
		if(serviceObject.getRequestBody().isEmpty()) return;

		String messageBody = serviceObject.getRequestBody();
		try {
			
			final ProducerRecord<byte[], byte[]> record =
                    new ProducerRecord<>(Config.getValue(KFAKA_TOPIC), messageId.getBytes(),
                    		messageBody.getBytes());
			
			
			RecordMetadata metadata = producer.send(record).get();
			
		     TestLog.ConsoleLog("sent record(key=%s value=%s) " +
                     "meta(partition=%d, offset=%d)\n",
             record.key(), record.value(), metadata.partition(),
             metadata.offset());

		} catch (Exception e) {
			e.printStackTrace();
		}

		TestLog.ConsoleLog("Sent message: " + serviceObject.getRequestBody());
	}
	
	/**
	 * gets message from outbound queue
	 * Adds messages to ouboutMessage hashmap
	 * 
	 * @param receiver
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void getOutboundMessages() {
		Properties props = new Properties();
		props.put("bootstrap.servers", Config.getValue(KAFKA_SERVER_URL));
		props.put("group.id", "KafkaExampleConsumer");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");  
		props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
		
		KafkaConsumer consumer = new KafkaConsumer(props);
        consumer.subscribe(Collections.singletonList(Config.getValue(KFAKA_TOPIC)));
        
		final ConsumerRecords<Long, String> consumerRecords =
                 consumer.poll(Duration.ofMillis(10));
		
		consumerRecords.forEach(record -> {
			TestLog.logPass("Consumer Record:(%d, %s, %d, %d)\n",
                    record.key(), record.value(),
                    record.partition(), record.offset());
            outboundMessages.put(record, true);
            TestLog.logPass("global message size in outbound list: " + outboundMessages.size());
        });
        consumer.commitAsync();
        consumer.close();
	}

	public static void getConsumerMessages(String messageId) {
		
		CopyOnWriteArrayList<ConsumerRecord<Long, String>> filteredMessages = new CopyOnWriteArrayList<>();

		// kafka will run for maxRetrySeconds to retrieve matching outbound message
		int maxRetrySeconds = Config.getIntValue(KAFKA_TIMEOUT_SECONDS);
		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		long lastLogged = 0;
		int interval = 10; // log every 10 seconds
		do {
			lastLogged = MessageQueueHelper.logPerInterval(interval, watch, lastLogged);

			getOutboundMessages();
			
			filteredMessages.addAll(
					filterOUtboundMessage(messageId));


			printMessage();
		} while (passedTimeInSeconds < 3);
	}
	
	/**
	 * filter outbound message based on messageId
	 * @param msgId
	 * @return
	 */
	public static Collection<ConsumerRecord<Long, String>> filterOUtboundMessage(String messageId) {

		
		// filter messages for the current test
		CopyOnWriteArrayList<ConsumerRecord<Long, String>> filteredMessages = new CopyOnWriteArrayList<ConsumerRecord<Long, String>>();
		filteredMessages.addAll(findMessagesBasedOnMessageId(messageId));

		return filteredMessages;
	}
	
	/**
	 * find message based on record id
	 * @param messageId
	 * @return
	 */
	public static CopyOnWriteArrayList<ConsumerRecord<Long, String>> findMessagesBasedOnMessageId(String messageId) {
		CopyOnWriteArrayList<ConsumerRecord<Long, String>> filteredMessages = new CopyOnWriteArrayList<ConsumerRecord<Long, String>>();

		for (Entry<ConsumerRecord<Long, String>, Boolean> entry : outboundMessages.entrySet()) {
			ConsumerRecord<Long, String> message = entry.getKey();

			
			if (entry.getValue().equals(true)
					&& message.toString().contains(messageId)) {


				filteredMessages.add(message);
				outboundMessages.put(message, false);
			}
		}

		return filteredMessages;
	}
	 
	 public static void printMessage() {
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