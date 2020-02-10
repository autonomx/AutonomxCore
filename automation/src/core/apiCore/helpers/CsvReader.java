package core.apiCore.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;

import core.apiCore.TestDataProvider;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

public class CsvReader {
	
	public static int SERVICE_CSV_COLUMN_COUNT = 20;
	public static String SERVICE_CSV_FILE_PREFIX = "TestCases_";
	public static final String ACTION_KEY = "action";
	public static final String SERVICE_CSV_SEPARATOR = "service.csv.separator";
	public static final String SERVICE_CSV_DATAPROVIDER_COUNT = "service.csv.dataprovider.count";

	
	
	// option to run service calls more than once. default 1
	public static final String OPTION_RUN_COUNT = "RUN_COUNT";
	public static final String SERVICE_RUN_COUNT = "service.run.count";
	public static final String SERVICE_RUN_CURRENT_COUNT = "service.run.current.count";
	public static final String SERVICE_RUN_PREFIX = "_run_";
	public static final String SERVICE_STEP_PREFIX = "_step";

	
	
	enum VALID_TEST_FILE_TYPES {
		 csv
		}
	
	/**
	 * gets the tests from csv file based on the current test file index
	 * 
	 * @return
	 */
	public synchronized static List<Object[]> getTestCasesFromCsvFile() {
		int index = getCurrentTestInvocation();
		// if single test case is specified, Then only load that file
		String testCaseFile = Config.getValue(TestDataProvider.TEST_CASE_FILE);
		if (!testCaseFile.isEmpty())
			index = getCsvFileIndex(testCaseFile);

		// get current csv file
		String csvFileName = CsvReader.getCsvFileFromIndex(index);

		// get test rows from csv file
		List<Object[]> csvList = getCsvTestListForTestRunner(csvFileName);
		
		// update csv test list with action csv file values
		List<Object[]> updatedCsvList = new ArrayList<Object[]>();
		updatedCsvList.addAll(addActionCsvTests(csvList));
		
		// updated test cases based on run count
		updatedCsvList = setTestRerun(updatedCsvList);
		
		// get map of test cases with test steps
		Map<String, List<Object[]>> testStepMap = getTestStepMap(csvList);
		
		// update test cases base on test steps (combining test steps with _step#number postfix. eg. verifyUser_step1, verifyUser_step2
		updatedCsvList = setTestSteps(updatedCsvList, testStepMap);
		
		// get the test cases based on specifications from config. eg. single file name, or single test case, or all
		List<Object[]> testCaseList = updateCsvFileFromFile(updatedCsvList, csvFileName, testCaseFile, testStepMap);
		return testCaseList;
	}
	
	/**
	 * update test cases base on test steps (combining test steps with _step#number postfix.
	 *  eg. verifyUser_step1, verifyUser_step2
	 * @param testCaseList
	 * @return
	 */
	public static List<Object[]> setTestSteps(List<Object[]> csvList, Map<String, List<Object[]>> testStepMap){
		List<Object[]> testCases = new ArrayList<>();

		String currentTestname = StringUtils.EMPTY;
		
		for (int i = 0; i < csvList.size(); i++) {
			Object[] csvRow = csvList.get(i);
			String testname = getTestname(csvRow);
			if(testStepMap.get(testname) != null && !currentTestname.equals(testname)) {
				csvRow[1] = testname; // set test name without test step
				testCases.add(csvRow);
				currentTestname = testname;
			}else if(testStepMap.get(testname) == null)		
				testCases.add(csvRow);
		}
		return testCases;
	}
	
	/**
	 * get test name from csv row without test step identifier
	 * @param csvRow
	 * @return
	 */
	private static String getTestname(Object[] csvRow) {
		if(!csvRow[1].toString().contains(SERVICE_STEP_PREFIX))
			return csvRow[1].toString();
		String testname = csvRow[1].toString().split(SERVICE_STEP_PREFIX)[0];
		return testname;
	}
	
	/**
	 * get test name without test step identifier
	 * @param csvRow
	 * @return
	 */
	public static String getTestname(String fullTestname) {
		String testname = fullTestname.toString().split(SERVICE_STEP_PREFIX)[0];
		return testname;
	}
	
	/**
	 * get map of test cases with test steps
	 * removes test step prefix from test case id
	 * @param csvList
	 * @return
	 */
	public static Map<String, List<Object[]>> getTestStepMap(List<Object[]> csvList){
		Map<String, List<Object[]>> testStepMap = new HashMap<String, List<Object[]>>();

		// get map of test cases with test steps
		for (int i = 0; i < csvList.size(); i++) {
			Object[] csvRow = csvList.get(i);	
			if(csvRow[1].toString().contains(SERVICE_STEP_PREFIX)) {
				String testname = getTestname(csvRow);
				List<Object[]> rowList = testStepMap.get(testname);
				if(rowList == null) rowList = new ArrayList<Object[]>();
				csvRow[1] = testname; // set test name without test step
				rowList.add(csvRow);
				testStepMap.put(testname, rowList);
			}
		}
		
		return testStepMap;
	}
	
	/**
	 * set test cases based on run count
	 * @param testCaseList
	 * @return
	 */
	public static List<Object[]> setTestRerun(List<Object[]> testCaseList){
		List<Object[]> updatedTestCases = new ArrayList<>();
		
		for (Object[] testCase : testCaseList) {
			ServiceObject serviceObject = mapToServiceObject(testCase); 
			
			// set run count based on option value
			evaluateOption(serviceObject);
			int runCount = Config.getIntValue(SERVICE_RUN_COUNT);
			
			String testid = testCase[1].toString();
			// add test cases based on run count
			for(int i = 1; i <= runCount; i++) {
				
				// add test number to test case id if multirun. index 1 is test id 
				if(runCount > 1) 				
					testCase[1] = testid + SERVICE_RUN_PREFIX + i;
					Object[] testCaseUpdated =  Arrays.copyOf( testCase, testCase.length );
				
				updatedTestCases.add(testCaseUpdated);
			}
		}
		return updatedTestCases;
	}
	
	/**
	 * update csv test list with additional meta data (csv file name, test index, service type, test count)
	 * if test case is set then single test is returned
	 * @param csvList
	 * @param csvFileName
	 * @param testCaseFile
	 * @return
	 */
	public static List<Object[]> updateCsvFileFromFile(List<Object[]> csvList, String csvFileName, String testCaseFile, Map<String, List<Object[]>> testStepMap) {
		List<Object[]> testCases = new ArrayList<>();
		
		for (int i = 0; i < csvList.size(); i++) {
			// add test name, test index, and test type 
			Object[] obj = { csvFileName, String.valueOf(i) + ":" + String.valueOf(csvList.size()), TestObject.testType.service.name()};
			
			Object[] csvRow = ArrayUtils.addAll(csvList.get(i), obj);			
			
			// add test steps map to the csv row
			String testname = getTestname(csvRow);
			if(testStepMap!= null && testStepMap.get(testname) != null) {
				ArrayList<Object> list = new ArrayList<>(Arrays.asList(csvRow));
				list.add(testStepMap);
				Object[] arr = list.toArray(new Object[list.size()]);
				csvRow = ArrayUtils.addAll(arr);	
			}else {
				int dataCount = Config.getIntValue(SERVICE_CSV_DATAPROVIDER_COUNT);
				if(dataCount >= 20) // including test step object
					csvRow = ArrayUtils.addAll(csvRow, StringUtils.EMPTY);
			}
			
			// for single test case selection. Both test case file And test case have to be
			// set
			String testCase = Config.getValue(TestDataProvider.TEST_CASE);
			if (testCaseFile.isEmpty() || testCase.isEmpty())
				testCases.add(csvRow);
			else if (csvRow[1].equals(testCase)) {
				testCases.add(csvRow);
				return testCases;
			}
		}
		return testCases;
	}
	
	/**
	 * update csv test list with tests from action csv files 
	 * @param testData
	 * @return 
	 */
	public static List<Object[]> addActionCsvTests(List<Object[]> csvList) {
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TestDataProvider.TEST_DATA_ACTION_PATH);
		
		List<Object[]> updateDataList = new ArrayList<Object[]>();
		boolean hasActionKey = false;
				
		for(Object[] dataRow : csvList) {
			hasActionKey = false;
			ServiceObject serviceObject = CsvReader.mapToServiceObject(dataRow);
			List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getMethod());
			String runFlag = serviceObject.getRunFlag();
			
			// move to next row if run flag is not Y
			if(!runFlag.equals("Y")) break;
			
			for (KeyValue keyword : keywords) {		
				if(keyword.key.equals(ACTION_KEY)) {
					List<Object[]> tests = getCsvTestListForTestRunner(csvTestPath, keyword.value.toString());
					updateDataList.addAll(tests);
					hasActionKey = true;
				}
			}
			if(!hasActionKey)
				updateDataList.add(dataRow);
		}
		return updateDataList;
	}
	

	/**
	 * maps list of test cases to service object map
	 * 
	 * @param testCases
	 * @return
	 */
	public static Map<String, ServiceObject> mapToApiObject(List<Object[]> testCases) {
		Map<String, ServiceObject> serviceMap = new ConcurrentHashMap<String, ServiceObject>();
		for (Object[] testCase : testCases) {
			// add parameters to ServiceObject
			ServiceObject ServiceObject = mapToServiceObject(testCase); 
			serviceMap.put(ServiceObject.getTestCaseID(), ServiceObject);
		}
		return serviceMap;
	}
	
	/**
	 * test data array to service object
	 * @param testData
	 * @return
	 */
	public static ServiceObject mapToServiceObject(Object[] testData) {
		return new ServiceObject().setServiceObject(testData);
	}

	/**
	 * gets all test cases from testcase path based on prefix
	 */
	public static void getAllTestCases(String testPath, String prefix) {
		ArrayList<File> csvFiles = Helper.getFileListByType(testPath, prefix);
		for (int i = 0; i < csvFiles.size(); i++) {
			List<Object[]> testCases = getCsvTestList(csvFiles.get(i));
			Map<String, ServiceObject> apiMap = mapToApiObject(testCases);
			TestObject.getTestInfo().apiMap.putAll(apiMap);
		}
	}

	/**
	 * gets all keywords And stores them in apiMap in testObject
	 */
	public synchronized static void getAllKeywords() {
		String testFolderPath = Config.getValue(TestDataProvider.API_KEYWORD_PATH);
		String csvTestPath = PropertiesReader.getLocalRootPath() + testFolderPath;
		CsvReader.getAllTestCases(csvTestPath, ".csv");
	}

	/**
	 * gets csv file index does not increment When retry
	 * 
	 * @return
	 */
	public static int getCurrentTestInvocation() {

		// if test class (based on csv file) has initiated, get the current csv file
		if (isRunningServiceTest()) {
			String activeTest = TestObject.getTestInfo().testCsvFileName;
			return getCsvFileIndex(activeTest);
		}
		return TestDataProvider.csvFileIndex.getAndIncrement();
	}

	public static boolean isRunningServiceTest() {
		return !TestObject.getTestInfo().testCsvFileName.isEmpty();
	}

	/**
	 * returns current csv test file
	 * 
	 * @return
	 */
	public static String getCsvFileFromIndex(int index) {
		ArrayList<File> testCsvList = getTestDataCsvFileList();
		Helper.assertTrue("test not found at index: " + index, testCsvList.size() > index);
		return testCsvList.get(index).getName();
	}

	public static int getCsvFileCount() {
		return getTestDataCsvFileList().size();
	}

	/**
	 * returns a list of all csv test files
	 * 
	 * @return
	 */
	public static ArrayList<File> getTestDataCsvFileList() {
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TestDataProvider.TEST_DATA_PARALLEL_PATH);
		return getCsvFileList(csvTestPath);
	}
	
	/**
	 * returns a list of all csv test files
	 * 
	 * @param csvTestPath: full path to csv directory
	 * @return
	 */
	public static ArrayList<File> getCsvFileList(String csvTestPath) {
		ArrayList<File> csvFiles = Helper.getFileListByType(csvTestPath, ".csv");
		return csvFiles;
	}

	/**
	 * returns the index of the file from the list of csv files
	 * 
	 * @param fileName
	 * @return
	 */
	public static int getCsvFileIndex(String fileName) {
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TestDataProvider.TEST_DATA_PARALLEL_PATH);
		return getCsvFileIndex(csvTestPath, fileName);
	}
	
	/**
	 * returns the index of the file from the list of csv files
	 * 
	 * @param fileName
	 * @return
	 */
	public static int getCsvFileIndex(String csvDir, String fileName) {
		Helper.assertTrue("csv file is null", fileName != null);

		ArrayList<File> csvFiles = getCsvFileList(csvDir);

		OptionalInt indexOpt = IntStream.range(0, csvFiles.size())
				.filter(i -> fileName.contains(csvFiles.get(i).getName())).findFirst();

		Helper.assertTrue("csv file: " + fileName + " not found", indexOpt.isPresent());
		return indexOpt.getAsInt();
	}

	/**
	 * gets csv tests list for service tests
	 * 
	 * @param csvFile
	 * @return
	 */
	public static List<Object[]> getCsvTestListForTestRunner(String csvFile) {
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TestDataProvider.TEST_DATA_PARALLEL_PATH);
		
		return getCsvTestListForTestRunner(csvTestPath, csvFile);	
	}
	
	/**
	 * gets csv tests list for service tests
	 * 
	 * @param csvFile
	 * @return
	 */
	public static List<Object[]> getCsvTestListForTestRunner(String csvDir, String csvFile) {
		List<Object[]> csvList = new ArrayList<Object[]>();
		ArrayList<File> testCsvFileList = getCsvFileList(csvDir);
		int fileIndex = getFileIndex(testCsvFileList, csvFile);
		if(fileIndex == -1)
			Helper.assertFalse("csv file not found. csv file: " + csvFile + " at location: " + csvDir);
		csvList = getCsvTestList(testCsvFileList.get(fileIndex));
		return csvList;
	}

	/**
	 * reads csv file and returns the list of rows 
	 * @param file
	 * @return
	 */
	public static List<Object[]> getCsvTestList(File file) {
		List<Object[]> csvList = new ArrayList<Object[]>();

		try {
			
		    CSVReader reader = readCsvFile(file);

			// read header separately
			String[] header = reader.readNext();
			int runFlag = getColumnIndexByName("RunFlag", header);
			int testCaseID = getColumnIndexByName("TestCaseID", header);

			// only add tests that have runFlag set to Y And testCaseID is set
			String[] line;
			while ((line = reader.readNext()) != null) {
				
				if (line[runFlag].equals("Y") && !line[testCaseID].isEmpty()) {
					csvList.add(line);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return csvList;
	}
	
	public static CSVReader readCsvFile(File file) {
		CSVReader reader = null;
		try {
			// read csv file
			char separator = getCsvSeparator();				
			FileInputStream fis = new FileInputStream(file.getPath());
	        InputStreamReader isr = new InputStreamReader(fis, 
	                StandardCharsets.UTF_8);
	        RFC4180Parser parser = new RFC4180ParserBuilder().withSeparator(separator).build();
		    reader = new CSVReaderBuilder(isr).withCSVParser(parser)
	                 .build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return reader;
	}
	
	/**
	 * sets the separator for the csv file
	 * @return
	 */
	private static char getCsvSeparator() {
		String separatorString = Config.getValue(SERVICE_CSV_SEPARATOR);
		if(separatorString.isEmpty()) separatorString = ",";
		char separator = separatorString.charAt(0);
		return separator;
	}

	public static int getFileIndex(ArrayList<File> testCsvFileList, String csvFile) {
		for (int i = 0; i < testCsvFileList.size(); i++) {
			if (testCsvFileList.get(i).getName().equals(csvFile))
				return i;
		}
		return -1;
	}

	/**
	 * gets column name by the column name
	 * 
	 * @param column:
	 *            column name
	 * @param row:
	 *            header row
	 * @return
	 */
	public static int getColumnIndexByName(String column, String[] row) {
		for (int i = 0; i < row.length; i++) {
			if (row[i].equals(column)) {
				return i;
			}
		}
		Helper.assertTrue("column not found", false);
		return -1;
	}
	
	/**
	 * get the number of tests in csv file
	 * 
	 * @return
	 */
	public static int getCsvTestcount() {

		return getCsvTestListForTestRunner(TestObject.getTestInfo().testCsvFileName).size();
	}
	
	/**
	 * returns if file is valid service test type
	 * @param filename
	 * @return
	 */
	public static boolean isValidTestFileType(String filename) {
		for (VALID_TEST_FILE_TYPES types : VALID_TEST_FILE_TYPES.values()) {
			  if(filename.endsWith(types.name()))
				  return true;
			}
		return false;
	}
	
	/**
	 * set run count for individual test case
	 * @param serviceObject
	 * @return
	 */
	public static void evaluateOption(ServiceObject serviceObject) {

		// reset validation timeout. will be overwritten by option value if set
		resetOptions();

		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return;
		}
		
		// store value to config directly using format: value:<$key> separated by colon ';'
		DataHelper.saveDataToConfig(serviceObject.getOption());

		// replace parameters for request body
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key) {
			case OPTION_RUN_COUNT:
				Config.putValue(SERVICE_RUN_COUNT, keyword.value);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * reset option values to default from config
	 */
	private static void resetOptions() {
		// reset options
		Config.putValue(SERVICE_RUN_COUNT, 1);
	}
}