package core.apiCore.interfaces;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringSerializer;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.JsonHelper;
import core.apiCore.helpers.MessageQueueHelper;
import core.helpers.Helper;
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

	

	public static Map<ConsumerRecord<String, String>, Boolean> outboundMessages = new ConcurrentHashMap<ConsumerRecord<String, String>, Boolean>();

	/**
	 * interface for database calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static void testKafkaInterface(ServiceObject serviceObject) throws Exception {
		
		// replace parameters for request body, including template file (json, xml, or other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

		// generate message id
		String messageId = MessageQueueHelper.generateMessageId(serviceObject, MESSAGE_ID_PREFIX);
		
		// send message
		sendKafkaMessage(serviceObject, messageId);
		
		// receive messages
		receiveAndValidateMessages(serviceObject, messageId);
	}

	/**
	 * send kafka message
	 * 
	 * @param serviceObject
	 */
	public static void sendKafkaMessage(ServiceObject serviceObject, String messageId) {
		
		// return if request is empty
		if(serviceObject.getRequestBody().isEmpty()) return;
		
		KafkaProducer<String, String> producer = null;

		String messageBody = serviceObject.getRequestBody();
		try {
			
			Properties properties = new Properties();
			properties.put("bootstrap.servers", Config.getValue(KAFKA_SERVER_URL));
			//properties.put("client.id", Config.getValue(KAFKA_CLIENT_ID));
			properties.put("key.serializer", StringSerializer.class);
			properties.put("value.serializer", StringSerializer.class);
			properties.put("retries", "3");

			producer = new KafkaProducer<String, String>(properties);
			
			final ProducerRecord<String, String> record =
                    new ProducerRecord<>(Config.getValue(KFAKA_TOPIC), messageId.toString(),
                    		messageBody);
			
			
			RecordMetadata metadata = producer.send(record).get();
			
		     TestLog.ConsoleLog("sent record(key=%s value=%s) " +
                     "meta(partition=%d, offset=%d)\n",
             record.key(), record.value(), metadata.partition(),
             metadata.offset());

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		producer.close();

		TestLog.ConsoleLog("Sent message: " + serviceObject.getRequestBody());
	}
	
	/**
	 * 1) gets messages, adds them to the outboundMessages
	 * 2) filters based on the message key
	 * 3) validates based on expected response requirements
	 * @param messageId
	 */
	public static void receiveAndValidateMessages(ServiceObject serviceObject, String messageId) {
		
		CopyOnWriteArrayList<ConsumerRecord<String, String>> filteredMessages = new CopyOnWriteArrayList<>();
		List<String> errorMessages = new ArrayList<String>();
		
		// kafka will run for maxRetrySeconds to retrieve matching outbound message
		int maxRetrySeconds = Config.getIntValue(KAFKA_TIMEOUT_SECONDS);
		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		long lastLogged = 0;
		int interval = 10; // log every 10 seconds
		
		do {
			lastLogged = MessageQueueHelper.logPerInterval(interval, watch, lastLogged, filteredMessages.size());

			// gets messages and stores them in outboundMessages hashmap
			getOutboundMessages();
			
			// filters based on message id
			filteredMessages.addAll(filterOutboundMessage(messageId));
			
			// validate message count
			errorMessages = MessageQueueHelper.validateExpectedMessageCount(serviceObject.getExpectedResponse(), getMessageList(filteredMessages));
			
			// validates messages. At this point we have received all the relevant messages. no need to retry
			if(errorMessages.isEmpty()) {
				errorMessages = validateMessages(serviceObject, filteredMessages);
				break;
			}

			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
			
		} while (!errorMessages.isEmpty() && passedTimeInSeconds < maxRetrySeconds);
		
		if (!errorMessages.isEmpty()) {
			String errorString = StringUtils.join(errorMessages, "\n error: ");
			TestLog.ConsoleLog(errorString);
			Helper.assertFalse(StringUtils.join(errorMessages, "\n error: "));
		}
	}
	
	/**
	 * @return 
	 * 
	 */
	public static List<String> validateMessages(ServiceObject serviceObject, CopyOnWriteArrayList<ConsumerRecord<String, String>> filteredMessages) {
		
		List<String> errorMessages = new ArrayList<String>();
		if(filteredMessages.isEmpty()) {
			errorMessages.add("no messages received");
			return errorMessages;
		}
		
		List<String> messageList = getMessageList(filteredMessages);
		List<String> headerList = getHeaderList(filteredMessages);
		List<String> topicList = getTopicList(filteredMessages);

		// separate expected response to each section we want to validate: messageBody, header, topic
		String expectedMessage = DataHelper.removeSectionFromExpectedResponse(DataHelper.VERIFY_HEADER_PART_INDICATOR,serviceObject.getExpectedResponse());
		expectedMessage = DataHelper.removeSectionFromExpectedResponse(DataHelper.VERIFY_TOPIC_PART_INDICATOR, expectedMessage);
		String expectedHeader = DataHelper.getSectionFromExpectedResponse(DataHelper.VERIFY_HEADER_PART_INDICATOR,serviceObject.getExpectedResponse());
		String expectedTopic = DataHelper.getSectionFromExpectedResponse(DataHelper.VERIFY_TOPIC_PART_INDICATOR,serviceObject.getExpectedResponse());

		
		errorMessages = JsonHelper.validateExpectedValues2(messageList, expectedMessage);
		errorMessages = JsonHelper.validateExpectedValues2(headerList, expectedHeader);
		errorMessages = JsonHelper.validateExpectedValues2(topicList, expectedTopic);

		
		return errorMessages;
	}
	
	/**
	 * inserts kafka topics to array list of strings
	 * @param filteredMessages
	 * @return
	 */
	public static List<String> getTopicList(CopyOnWriteArrayList<ConsumerRecord<String, String>> filteredMessages){
		List<String> topics = new ArrayList<String>();
		for(ConsumerRecord<String, String> record : filteredMessages) {
			topics.add(record.topic().toString());
		}
		return topics;
	}
	
	/**
	 * inserts kafka headers to array list of strings
	 * @param filteredMessages
	 * @return
	 */
	public static List<String> getHeaderList(CopyOnWriteArrayList<ConsumerRecord<String, String>> filteredMessages){
		List<String> headers = new ArrayList<String>();
		for(ConsumerRecord<String, String> record : filteredMessages) {
			for(Header header: record.headers()) {
				headers.add(header.value().toString());
			}
		}
		return headers;
	}
	
	/**
	 * inserts kafka messages to array list of strings
	 * @param filteredMessages
	 * @return
	 */
	public static List<String> getMessageList(CopyOnWriteArrayList<ConsumerRecord<String, String>> filteredMessages){
		List<String> messages = new ArrayList<String>();
		for(ConsumerRecord<String, String> message : filteredMessages) {
			messages.add(message.value());
		}
		return messages;
	}
	
	/**
	 * gets message from outbound queue Adds messages to ouboutMessage hashmap
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
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		KafkaConsumer consumer = new KafkaConsumer(props);
		consumer.subscribe(Collections.singletonList(Config.getValue(KFAKA_TOPIC)));

		final int giveUp = 5;
		int noRecordsCount = 0;
		ConsumerRecords<String, String> consumerRecords = null;
		
		do {
			if (!outboundMessages.isEmpty())
				break;
			//TestLog.ConsoleLog("attempt: " + noRecordsCount);
			consumerRecords = consumer.poll(Duration.ofMillis(1000));

			if (consumerRecords.count() == 0) {
				noRecordsCount++;
				continue;
			}

			// print all received message on this thread
			consumerRecords.forEach(record -> {
				TestLog.logPass("received consumer record: key: " + record.key() + ", value: " + record.value() + ", partition: "
						+ record.partition() + ", offset: " + record.offset());
				outboundMessages.put(record, true);
			});
			TestLog.logPass("global message size in outbound list: " + outboundMessages.size());
			consumer.commitAsync();
		} while (consumerRecords.isEmpty() && noRecordsCount < giveUp);

		consumer.close();
	}
	
	/**
	 * filter outbound message based on messageId
	 * @param msgId
	 * @return
	 */
	public static Collection<ConsumerRecord<String, String>> filterOutboundMessage(String messageId) {

		
		// filter messages for the current test
		CopyOnWriteArrayList<ConsumerRecord<String, String>> filteredMessages = new CopyOnWriteArrayList<ConsumerRecord<String, String>>();
		filteredMessages.addAll(findMessagesBasedOnMessageId(messageId));

		return filteredMessages;
	}
	
	/**
	 * find message based on record id
	 * @param messageId
	 * @return
	 */
	public static CopyOnWriteArrayList<ConsumerRecord<String, String>> findMessagesBasedOnMessageId(String messageId) {
		CopyOnWriteArrayList<ConsumerRecord<String, String>> filteredMessages = new CopyOnWriteArrayList<ConsumerRecord<String, String>>();

		for (Entry<ConsumerRecord<String, String>, Boolean> entry : outboundMessages.entrySet()) {
			ConsumerRecord<String, String> message = entry.getKey();

			
			if (entry.getValue().equals(true)
					&& message.key().contains(messageId)) {


				filteredMessages.add(message);
				outboundMessages.put(message, false);
			}
		}

		return filteredMessages;
	}
	 
	 public static void printMessage() {
		 for (Entry<ConsumerRecord<String, String>, Boolean> entry : outboundMessages.entrySet()) {
				String message = entry.getKey().value();
				TestLog.ConsoleLog("outbound messages: " + message);
		 }
	 }
}