package core.apiCore.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.util.TextUtils;

import com.microsoft.azure.servicebus.primitives.StringUtil;

import core.helpers.Helper;
import core.helpers.StopWatchHelper;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;

public class MessageQueueHelper {
	
	public static final String EXPECTED_MESSAGE_COUNT = "EXPECTED_MESSAGE_COUNT";
	
	/**
	 * generate message id if the request body is set
	 * 
	 * @param requestBody
	 * @return 
	 * @return
	 */
	public static String generateMessageId(ServiceObject serviceObject, String messageIdPrefix) {
		String messageId = StringUtil.EMPTY;
		
		// get unique identifier for request body to match outbound message
		if (!serviceObject.getRequestBody().isEmpty()) 
			messageId = messageIdPrefix + "-" + UUID.randomUUID().toString();
		
		return messageId;
	}
	
	/**
	 * log per interval stating the wait time for a message from message queue
	 * @param interval
	 * @param watch
	 * @param lastLogged
	 * @return
	 */
	public static long logPerInterval(int interval, StopWatchHelper watch, long lastLogged, int receivedMessageCount) {
		long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
		if (passedTimeInSeconds > interval && passedTimeInSeconds - lastLogged > interval) {
			TestLog.logPass("waiting: " + watch.time(TimeUnit.SECONDS) + " seconds");
			TestLog.logPass("received: " + receivedMessageCount + " relavent message(s)");

			Helper.waitForSeconds(1);
			lastLogged = passedTimeInSeconds;
		}
		return lastLogged;
	}
	
	/**
	 * validate expected message count from received message
	 * @param request
	 * @param filteredMessages
	 * @return
	 */
	public static List<String> validateExpectedMessageCount(String request, List<String> filteredMessages) {
		List<String> errorMessages = new ArrayList<String>();
		
		if(filteredMessages.isEmpty()) {
			errorMessages.add("no messages received");
			return errorMessages;
		}
		
		int expectedMessageCount = 1;
		
		// get a map of key values in request
		Map<String, String> params = getKeyValueFromString(request, ";" , ":");
		
		// get expected message count if set
		if(params.containsKey(EXPECTED_MESSAGE_COUNT))
		{
			expectedMessageCount =  Helper.getIntFromString(params.get(EXPECTED_MESSAGE_COUNT), true);
		}
		
		// get actual message count 
		int actualMessageCount = filteredMessages.size();
		
		// compare expected with actual message count
		//TestLog.logPass("verifying message count: " + "Response message count received " + filteredMessages.size() + " out of " + expectedMessageCount + " expected messages");
		if(expectedMessageCount != actualMessageCount) {
			String errorMessage = "Response received " + filteredMessages.size() + " out of " + expectedMessageCount + ".\n Received messages: \n";
			errorMessages.add(errorMessage + String.join("\n ", filteredMessages));
		}
		return errorMessages;
	}
	
	/**
	 * separated based on key value if key value exists
	 * eg. key:value; key1:value1
	 * @param value
	 * @param entriesSeparator eg. ";"
	 * @param separator eg ":"
	 * @return 
	 */
	public static Map<String, String> getKeyValueFromString(String value, String entriesSeparator, String separator) {
		Map<String, String> map = new HashMap<String, String>();
		
		// remove all spaces
		value = value.replaceAll("\\s+","");
		
		String[] entries = value.split(entriesSeparator);
		for(String entry : entries) {
			if (!TextUtils.isEmpty(entry) && entry.contains(separator)) {
	            String[] keyValue = entry.split(separator);
	            map.put(keyValue[0], keyValue[1]);
	        }
		}
		return map;
	}

}
