package core.apiCore.helpers;

import java.util.Collection;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.primitives.StringUtil;

import core.helpers.StopWatchHelper;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;

public class MessageQueueHelper {
	
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
	
	public static long logPerInterval(int interval, StopWatchHelper watch, long lastLogged) {
		long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
		if (passedTimeInSeconds > interval && passedTimeInSeconds - lastLogged > interval) {
			TestLog.logPass("waiting: " + watch.time(TimeUnit.SECONDS) + " seconds");
			lastLogged = passedTimeInSeconds;
		}
		return lastLogged;
	}

}
