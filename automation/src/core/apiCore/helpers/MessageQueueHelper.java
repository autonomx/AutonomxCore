package core.apiCore.helpers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;
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
	public static long logPerInterval(int interval, StopWatchHelper watch, long lastLogged) {
		long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
		if (passedTimeInSeconds > interval && passedTimeInSeconds - lastLogged > interval) {
			TestLog.logPass("waiting: " + watch.time(TimeUnit.SECONDS) + " seconds");
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
	public static String validateExpectedMessageCount(String request, List<String> filteredMessages) {
		String errorMessage = StringUtils.EMPTY;
		
		int expectedMessageCount = 1;
		
		// remove all space
		request = request.replaceAll("\\s+","");
		Map<String, String> params = Splitter
			    .on(",")
			    .withKeyValueSeparator(":")
			    .split(request);
		
		// get expected message count if set
		if(params.containsKey(EXPECTED_MESSAGE_COUNT))
		{
			expectedMessageCount =  Helper.getIntFromString(params.get(EXPECTED_MESSAGE_COUNT), true);
		}
		
		// get actual message count 
		int actualMessageCount = filteredMessages.size();
		
		// compare expected with actual message count
		if(expectedMessageCount != actualMessageCount) {
			errorMessage = "Response received " + filteredMessages.size() + " out of " + expectedMessageCount + ".\n Missing messages: \n";
			errorMessage =  errorMessage + String.join("\n", filteredMessages);
		}
		return errorMessage;
	}

}
