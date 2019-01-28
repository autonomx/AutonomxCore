package core.apiCore.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import core.apiCore.dataProvider;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.TestObject;

public class dataHelper {

	/**
	 * replaces placeholder values with values from config properties
	 * 
	 * @param source
	 * @return
	 */
	public static String replaceParameters(String source) {
		List<String> parameters = Helper.getValuesFromPattern(source, "<(.+?)>");
		String value;
		int length = 0;
		for (String parameter : parameters) {
			if (parameter.contains("@_TIME")) {
				length = getIntFromString(parameter);
				value = TestObject.getTestInfo().startTime.substring(0, length);
			} else if (parameter.contains("@_RAND")) {
				length = getIntFromString(parameter);
				value = TestObject.getTestInfo().randStringIdentifier.substring(0, length);
			} else {
				value = Config.getValue(parameter.replace("@", ""));
			}
			if (value == null)
				Helper.assertTrue("parameter value not found: " + parameter, false);

			TestLog.logPass("replacing value '" + parameter + "' with: " + value + "");
			source = source.replaceAll("<" + parameter + ">", Matcher.quoteReplacement(value));
		}

		return source;
	}

	public static int getIntFromString(String value) {
		return Integer.parseInt(value.replaceAll("[\\D]", ""));
	}

	/**
	 * gets the map of the validation requirements
	 * 
	 * @param expected
	 * @return
	 */
	public static List<KeyValue> getValidationMap(String expected) {
		// get hashmap of json path and verification
		List<KeyValue> keywords = new ArrayList<KeyValue>();
		expected = expected.replace("_VERIFY_JSON_PART_", "");
		String[] keyVals = expected.split(";");
		for (String keyVal : keyVals) {
			String[] parts = keyVal.split(":", 2);
			String key = Helper.stringRemoveLines(parts[0]);
			String value = Helper.stringRemoveLines(parts[1]);

			KeyValue keyword = new KeyValue(key, value);
			keywords.add(keyword);
		}
		return keywords;
	}

	/**
	 * replaces output parameter with response values eg. $token with id form
	 * response
	 * 
	 * @param response
	 * @param outputParam
	 * @throws SQLException
	 */
	public static void saveOutboundSQLParameters(ResultSet resSet, String outputParam) throws SQLException {
		if (outputParam.isEmpty())
			return;

		String[] keyVals = outputParam.split(",");
		for (String keyVal : keyVals) {
			String[] parts = keyVal.split(":", 2);
			String key = parts[1].replace("$", "").replace("<", "").replace(">", "");
			String value = resSet.getString(parts[0]);
			Config.putValue(key, value);
			TestLog.logPass("replacing value: " + key + " with: " + value);
		}
	}
	
	public static String getTagValue(String requestBody, String tag) {
		String value = "";
		try {
			String patternString = ":" + tag + ">(.+?)</";
			final Pattern pattern = Pattern.compile(patternString);
			final Matcher matcher = pattern.matcher(requestBody);
			matcher.find();
			value = matcher.group(1);
		} catch (Exception e) {
			e.getMessage();
		}
		return value;
	}
	
	public static String getTemplateFile(String file) {
		String templatePath = Config.getValue(dataProvider.TEST_DATA_TEMPLATE_PATH);
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;

		return templateTestPath + file;	
	}
	
	public static String convertTemplateToString(String templateFilePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(templateFilePath)));
		String line;
		StringBuilder sb = new StringBuilder();

		while((line=br.readLine())!= null){
		    sb.append(line.trim());
		}
		br.close();
		
		return sb.toString();
	}
	
	   /**
     * In outputParams get the params enclosed by <$> look up their values
     * in outboundMsg and then add them to ConfigurationParams
     *
     * @param outputParams
     * @param outboundMsg
     */
    public static void addOutputMessageToConfigParams(String outputParams, String outboundMsg) {
        
        //Copy responseBody into the variable
         String key = StringUtils.substringBetween(outputParams, "<$", ">");
         Config.putValue(key, outboundMsg);
        
        TestLog.logPass("Get Service Bus Outbound Message:{0}", Config.getValue(key));
    }
}
