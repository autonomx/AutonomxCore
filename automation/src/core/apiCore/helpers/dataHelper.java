package core.apiCore.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import io.netty.util.internal.StringUtil;
import io.restassured.response.Response;

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
		String key = "";
		String position = "";
		String value = "";
		for (String keyVal : keyVals) {
			String[] parts = keyVal.split(":", 3);
			if(parts.length == 2) { // without position
				 key = Helper.stringRemoveLines(parts[0]);
				 position = StringUtil.EMPTY_STRING;
				 value = Helper.stringRemoveLines(parts[1]);
			}else if(parts.length == 3) { // with position
				 key = Helper.stringRemoveLines(parts[0]);
				 position = Helper.stringRemoveLines(parts[1]);
				 value = Helper.stringRemoveLines(parts[2]);
			}

			KeyValue keyword = new KeyValue(key, position, value);
			keywords.add(keyword);
		}
		return keywords;
	}
	
	/**
	 * get value in between tags >value<
	 * @param requestBody
	 * @param tag
	 * @return
	 */
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
