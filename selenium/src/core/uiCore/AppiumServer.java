package core.uiCore;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

public class AppiumServer {

	public static String ANDROID_HOME = "androidHome";
	public static String JAVA_HOME = "javaHome";
	public static String APPIUM_LOGGING = "appiumLogging";
	public static String APPIUM_LOGGING_LEVEL = "appiumLogginLevel";
	public static AppiumDriverLocalService service = null;
	
	/**
	 * start appium service with random ports
	 * 
	 * @param driverObject
	 * @return
	 * @throws MalformedURLException
	 */
	public static AppiumDriverLocalService startAppiumServer(DriverObject driverObject)
			throws MalformedURLException {

		 // run only one instance of appium server^M
		if(service != null && service.isRunning()) return service;
		
		Map<String, String> env = setEnvVariables();

		AppiumServiceBuilder builder = new AppiumServiceBuilder().usingAnyFreePort().withEnvironment(env)
				.withIPAddress("127.0.0.1")
				.withArgument(GeneralServerFlag.SESSION_OVERRIDE);

		// if logging set to true, set the logging level
		if (Config.getBooleanValue(APPIUM_LOGGING))
			builder.withArgument(GeneralServerFlag.LOG_LEVEL, Config.getValue(APPIUM_LOGGING_LEVEL));
		else
			builder.withArgument(GeneralServerFlag.LOG_LEVEL, "error");


		
	    try {
	        service = AppiumDriverLocalService.buildService(builder);
	        service.start();
	    } catch (NullPointerException e) {
	        e.printStackTrace();
	    }

		if (service == null || !service.isRunning()) {
			throw new AppiumServerHasNotBeenStartedLocallyException("An appium server node is not started!");
		}

		// disable appium logging
		disableAppiumConsoleLogging(service);

		TestLog.And("Appium server has been initiated successfully");

		return service;
	}
	
	public static Map<String, String> setEnvVariables() {
		Map<String, String> env = new HashMap<>(System.getenv());
		// Note: android home and java home may need to be set on osx environment

		// set android home
		if (!Config.getValue(ANDROID_HOME).isEmpty())
			env.put("ANDROID_HOME", Config.getValue(ANDROID_HOME));

		// set java home
		if (!Config.getValue(JAVA_HOME).isEmpty())
			env.put("JAVA_HOME", Config.getValue(JAVA_HOME));

		// set path
		if (!CrossPlatformProperties.getPath().isEmpty())
			env.put("PATH", CrossPlatformProperties.getPath());
		
		return env;
	}

	/**
	 * disabled appium console logging completely
	 * 
	 * @param service
	 */
	@SuppressWarnings("unchecked")
	public static void disableAppiumConsoleLogging(AppiumDriverLocalService service) {
		// if appium logging enabled, do not disable logging
		if (Config.getValue(APPIUM_LOGGING).equals("true"))
			return;

		Field streamField = null;
		Field streamsField = null;
		try {
			streamField = AppiumDriverLocalService.class.getDeclaredField("stream");
			streamField.setAccessible(true);
			streamsField = Class.forName("io.appium.java_client.service.local.ListOutputStream")
					.getDeclaredField("streams");
			streamsField.setAccessible(true);
		} catch (ClassNotFoundException | NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			((ArrayList<OutputStream>) streamsField.get(streamField.get(service))).clear(); // remove System.out logging
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}