package core.driver;

import java.io.IOException;

import core.logger.PropertiesReader;

public class BaseUrl {


	/**
	 * set url through maven using -D command
	 * eg. -DcraigslistSite = "www.test.com"
	 * @return
	 */
	public static String getUrl(String app, String defaultUrl) {
	   if (!getUrlFromMaven(app).isEmpty()) { return getUrlFromMaven(app); }
	   if (!getUrlFromProperties(app).isEmpty()) { return getUrlFromProperties(app); }
	   return defaultUrl; 
	}
	
	public static String getUrlFromMaven(String app) {
		String value = System.getProperty(app, "");
		return value;
	}
	
	public static String getUrlFromProperties(String app) {
		try {
			return PropertiesReader.Property().getProperty(app, "");
		} catch (IOException e) {
			e.getMessage();
		}
		return "";
	}	
}
