package core.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import core.support.configReader.PropertiesReader;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

/**
 * local: in project directory web: in web config directory mobile: in mobile
 * config directory
 * 
 * @author Ehsan Matean
 *
 */

public class LocalizationHelper {
	public static String DEFAULT_LOCAL = "english";

	protected static String localize(String key) {
		String localFile = CrossPlatformProperties.getLocalizationFile();
		String translation = "";
		if (localFile.endsWith(".csv")) {
			translation = localizeFromCsv(key);
		} else if (localFile.endsWith(".json")) {
			translation = localizeFromJson(key);
		}
		if (translation != null && !translation.isEmpty() )
			return translation;
		return key;
	}

	protected static String localizeFromCsv(String value) {
		// get local value
		String translation = TestObject.getTestInfo().languageMap.get(value);

		return translation;
	}

	/**
	 * sets the language for csv localization file
	 * 
	 * @param language
	 */
	protected static void localizationSetupCsv(String language) {
		TestObject.getTestInfo().language = language;
		loadCsvFileContent();
	}

	/**
	 * gets local language from json file specified in properties file
	 * 
	 * @param key
	 * @return
	 */
	protected static String localizeFromJson(String key) {
		if (TestObject.getTestInfo().languageMap.isEmpty()) {
			String localFile = CrossPlatformProperties.getLocalizationFile();
			localizationLoadJson(localFile);
		}
		String local = TestObject.getTestInfo().languageMap.get(key);
		return local;
	}

	/**
	 * loads localization map from a json file to the test object data
	 * 
	 * @param fileName
	 */
	protected static void localizationLoadJson(String fileName) {

		String filePath = PropertiesReader.getLocalResourcePath() + fileName;
		TestObject.getTestInfo().languageMap = jsonFileToMap(filePath);
		TestObject.getTestInfo().config.put(CrossPlatformProperties.LOCALIZATION_FILE, fileName);
	}
	
	@SuppressWarnings("serial")
	protected static Map<String, String> jsonStringToMap(String jsonString) {
		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Gson gson = new Gson();
		return gson.fromJson(jsonString, type);
	}

	@SuppressWarnings({ "unchecked" })
	protected static Map<String, String> jsonFileToMap(String filePath) {
		JSONObject json = null;
		try {
			BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
			String line = null;
			StringBuilder sb = new StringBuilder();

			while ((line = buff.readLine()) != null) {
				sb.append(line);
			}
			json = new JSONObject(sb.toString());
			buff.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Gson().fromJson(json.toString(), HashMap.class);
	}

	/**
	 * loads the content of the csv file
	 * 
	 * @return
	 * @throws Exception
	 * 
	 */
	protected static void loadCsvFileContent() {

		if (TestObject.getTestInfo().languageMap.isEmpty()) {
			String localFile = CrossPlatformProperties.getLocalizationFile();
			String language = TestObject.getTestInfo().language;
			File csvFile = getCsvFile(localFile);
			CSVParser parser;
			try {
				parser = CSVParser.parse(csvFile, StandardCharsets.ISO_8859_1,
						CSVFormat.EXCEL.withFirstRecordAsHeader());
				String translation = "";

				// adds list of language to th local list to keep track
				// TestObject.getTestInfo().languageMap.put(COLUMNS_KEY,
				// getColumnHeaders(parser));

				// add english as key And target local as value to the localList
				for (CSVRecord csvRecord : parser) {

					String key = csvRecord.get(DEFAULT_LOCAL);
					translation = csvRecord.get(language);
					TestObject.getTestInfo().languageMap.put(key, translation);

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * gets the list of the column in csv file
	 * 
	 * @param parser
	 * @return
	 */
	protected static List<String> getColumnHeaders(CSVParser parser) {
		List<String> columns = new ArrayList<String>();
		Map<String, Integer> header = parser.getHeaderMap();
		for (Entry<String, Integer> entry : header.entrySet()) {
			columns.add(entry.getKey());
		}
		return columns;
	}

	protected static File getCsvFile(String filename) {
		String filePath = PropertiesReader.getLocalResourcePath() + filename;

		File csvFile = new File(filePath);
		if (csvFile.isFile() && csvFile.toString().endsWith(".csv")) {
			return csvFile;
		} else {
			Helper.assertTrue("csv file not found at: " + filename, false);
		}
		return null;
	}
}
