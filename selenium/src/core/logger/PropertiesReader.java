package core.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {

	public static Properties Property() throws IOException {

		// Create FileInputStream Object
		FileInputStream fileInput = new FileInputStream(new File("properties/property.properties"));
		// Create Properties object
		Properties prop = new Properties();
		// load properties file
		prop.load(fileInput);
		return prop;
	}
	
	public String getDriverType() throws IOException {
		return PropertiesReader.Property().getProperty("webdriverType");
	}
	
	public static String getGridUrl() throws IOException {
		return "http://" + PropertiesReader.Property().getProperty("grid_url");
	}

	public static String getGridPort() throws IOException {
		return PropertiesReader.Property().getProperty("grid_port");
	}

	public static String getBrowser() throws IOException {
		return PropertiesReader.Property().getProperty("remote_browser");
	}
	
	public void setProperty(String type, String value) {
		try {
			PropertiesReader.Property().setProperty(type, value);
		} catch (IOException e) {
			e.getMessage();
		}
	}
	
	public static int getRetryCount() {
		int retry = 0;
		try {
			retry = Integer.parseInt(PropertiesReader.Property().getProperty("retry_count", "1"));
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retry;
	}
	
	public static int getParallelTests() {
		int parallelCount = 0;
		try {
			return Integer.parseInt(PropertiesReader.Property().getProperty("parallel_test_count", "2"));
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parallelCount;
	}
	
	public static int getGlobalTimeout() {
		int timeout_seconds = 0;
		try {
			return Integer.parseInt(PropertiesReader.Property().getProperty("global_timeout_seconds", "60"));
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeout_seconds;
	}
	
	/**
	 *
	 * set to true to append to existing report in case of rerunning test
	 * @return
	 */
	public static boolean isAppendReport() {
		boolean isAppend = false;
		try {
		   isAppend = ((PropertiesReader.Property().getProperty("isAppendReport", "false").equals("true")) ? true : false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isAppend;
	}
}
