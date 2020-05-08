package core.apiCore.interfaces;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringSerializer;

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
public class KafkaInterface {

	public static final String KAFKA_SERVER_URL = "kafka.bootstrap.servers";
	public static final String KAFKA_CLIENT_ID = "kafka.clientId";
	public static final String KFAKA_TOPIC = "kafka.topic";
	public static final String KFAKA_OUTBOUND_TOPIC = "kafka.outbound.topic";
	public static final String KAFKA_GROUP_ID = "kafka.group.id";
	public static final String KAFKA_TIMEOUT_SECONDS = "kafka.timeout.seconds";
	public static final String KAFKA_MESSAGE_ID_PREFIX = "kafka.msgId.prefix";
	public static Map<ConsumerRecord<String, String>, Boolean> outboundMessages = new ConcurrentHashMap<ConsumerRecord<String, String>, Boolean>();

	/**
	 * interface for database calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static void testKafkaInterface(ServiceObject serviceObject) throws Exception {

		// evaluate options
		evaluateOption(serviceObject);

		// replace parameters for request body, including template file (json, xml, or
		// other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

		// generate message id
		String messageId = MessageQueueHelper.generateMessageId(serviceObject,
				Config.getValue(KAFKA_MESSAGE_ID_PREFIX));

		// send message
		sendKafkaMessage(serviceObject, messageId);

		// receive messages
		MessageQueueHelper.receiveAndValidateMessages(serviceObject, messageId, messageType.KAFKA);
	}

	/**
	 * send kafka message
	 * 
	 * @param serviceObject
	 */
	public static void sendKafkaMessage(ServiceObject serviceObject, String messageId) {

		// return if request is empty
		if (serviceObject.getRequestBody().isEmpty())
			return;

		KafkaProducer<String, String> producer = null;

		String messageBody = serviceObject.getRequestBody();
		try {

			Properties properties = new Properties();
			properties.put("bootstrap.servers", Config.getValue(KAFKA_SERVER_URL));
			// properties.put("client.id", Config.getValue(KAFKA_CLIENT_ID));
			properties.put("key.serializer", StringSerializer.class);
			properties.put("value.serializer", StringSerializer.class);
			properties.put("retries", "3");

			producer = new KafkaProducer<String, String>(properties);

			final ProducerRecord<String, String> record = new ProducerRecord<>(Config.getValue(KFAKA_TOPIC),
					messageId.toString(), messageBody);

			producer.send(record).get();

			TestLog.logPass("sent messageId : " + messageId + "\n message : " + messageBody);

		} catch (Exception e) {
			e.printStackTrace();
		}

		producer.close();
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
		props.put("group.id", Config.getValue(KAFKA_GROUP_ID));
		props.put("auto.offset.reset", "earliest");
		props.put("enable.auto.commit", "true");

		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		String topic = Config.getValue(KFAKA_TOPIC);
		String outboundTopic = Config.getValue(KFAKA_OUTBOUND_TOPIC);

		// set outbound topic if defined
		if (!outboundTopic.isEmpty())
			topic = outboundTopic;

		KafkaConsumer consumer = new KafkaConsumer(props);
		consumer.subscribe(Collections.singletonList(topic));

		final int giveUp = 5;
		int noRecordsCount = 0;
		ConsumerRecords<String, String> consumerRecords = null;

		do {
			// TestLog.ConsoleLog("attempt: " + noRecordsCount);
			consumerRecords = consumer.poll(Duration.ofMillis(3000));

			if (consumerRecords.count() == 0) {
				noRecordsCount++;
				continue;
			}

			// add received message on this thread to outboundMessages
			consumerRecords.forEach(record -> {

				List<String> headers = new ArrayList<String>();
				for (Header header : record.headers()) {
					headers.add(header.value().toString());
				}

				MessageObject message = new MessageObject().withMessageType(messageType.KAFKA)
						.withMessageId(record.key()).withMessage(record.value()).withTopic(record.topic())
						.withHeader(headers);

				TestLog.logPass("Received messageId '" + message.getMessageId() + "\n with message content: "
						+ message.getMessage());
				MessageObject.outboundMessages.put(message, true);
			});
			TestLog.logPass("global message size in outbound list: " + outboundMessages.size());
			consumer.commitAsync();
		} while (consumerRecords.isEmpty() && noRecordsCount < giveUp);

		consumer.close();
	}

	public static void evaluateOption(ServiceObject serviceObject) {

		// set default queue and exchange values. will be overwritten if values are set
		// in csv
		setDefaultTopic();

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
				Config.putValue(KFAKA_TOPIC, keyword.value, false);
				break;
			case "outbound_topic":
				Config.putValue(KFAKA_OUTBOUND_TOPIC, keyword.value, false);
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
	 * set default topic values
	 */
	private static void setDefaultTopic() {

		String defaultTopic = TestObject.getGlobalTestInfo().config.get(KFAKA_TOPIC).toString();
		String ouboundTopic = TestObject.getGlobalTestInfo().config.get(KFAKA_OUTBOUND_TOPIC).toString();
		Config.putValue(KFAKA_TOPIC, defaultTopic, false);
		Config.putValue(KFAKA_OUTBOUND_TOPIC, ouboundTopic, false);
		Config.putValue(MessageQueueHelper.RESPONSE_IDENTIFIER, StringUtils.EMPTY, false);
	}
}