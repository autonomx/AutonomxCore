package core.apiCore.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;

import core.apiCore.ServiceManager;
import core.apiCore.TestDataProvider;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

public class CsvReader {
	
	public static int SERVICE_CSV_COLUMN_COUNT = 20;
	public static int SERVICE_CSV_VISIBLE_COLUMN_COUNT = 16;

	public static String SERVICE_CSV_FILE_PREFIX = "TestCases_";
	public static final String ACTION_KEY = "action";
	public static final String SERVICE_CSV_SEPARATOR = "service.csv.separator";
	public static final String SERVICE_CSV_DATAPROVIDER_COUNT = "service.csv.dataprovider.count";
	public static final String SERVICE_CSV_INCLUDE_SUB_DIR = "api.csv.include.subdir";
	public static final String SERVICE_INCLUDE_LIST= "api.includeTests";
	public static final String SERVICE_EXCLUDE_LIST= "api.excludeTests";

	
	
	// option to run service calls more than once. default 1
	public static final String OPTION_RUN_COUNT = "RUN_COUNT";
	public static final String SERVICE_RUN_COUNT = "service.run.count";
	public static final String SERVICE_RUN_CURRENT_COUNT = "service.run.current.count";
	public static final String SERVICE_RUN_PREFIX = "_run_";
	public static final String SERVICE_STEP_PREFIX = "step";

	
	enum TEST_FILTER_TYPES {
		 INCLUDE, EXCLUDE
	}
	
	enum VALID_TEST_FILE_TYPES {
		 csv
		}
	
	/**
	 * gets the tests from csv file based on the current test file index
	 * 
	 * @return
	 */
	public synchronized static List<Object> getTestCasesFromCsvFile() {
		int index = getCurrentTestInvocation();

		// set starting test index based on first test in include list
		index = setTestRangeIndex();
		
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
		List<Object[]> testCaseList = updateCsvFileFromFile(updatedCsvList, csvFileName, testStepMap);
		
		//print out warning for duplicated test names
		detectDuplicateTests(testCaseList);
		
		List<Object> tests = new ArrayList<Object>();
		for(Object[] object : testCaseList) {
			tests.add(object);
		}
		
		return tests;
	}
	
	/**
	 * sets the index of the file specified in include and exclude tests
	 * 
	 * @param index
	 * @return
	 */
	public static int setTestRangeIndex() {
		int index = 0;
		
		// set csv list to include all tests at the beginning of test run
		if(TestDataProvider.TEST_CSV_LIST.isEmpty()) {
			TestDataProvider.TEST_CSV_LIST = filterTests();
		}
		
		if(TestDataProvider.TEST_CSV_LIST.isEmpty())
			Helper.assertFalse("no tests available after fitlering");
		
		// get the index of the first test in csv list and remove from list
		if(!TestDataProvider.TEST_CSV_LIST.isEmpty()) {
			File testFile = TestDataProvider.TEST_CSV_LIST.get(0);
			TestDataProvider.TEST_CSV_LIST.remove(testFile);
			index = getCsvFileIndex(testFile.getName());
		}
				
		return index;
	}
	
	public static ArrayList<File> filterTests(){
		ArrayList<File> files = getTestDataCsvFileList();
		
		//filter csv tests based on include test list
		files = filterIncludeTestFiles(files);
	
		// filters csv tests based on include test list
		files = filterExcludeTestFiles(files);
		
		return files;
	}
	
	/**
	 * filters csv tests based on include test list
	 * format: api.includeTests = "TestCases_UserValidation.csv:getAdminToken-updateUser;TestCases_UserValidation2.csv"
	 */
	private static ArrayList<File> filterIncludeTestFiles(ArrayList<File> files) {
		String includeTests = Config.getValue(SERVICE_INCLUDE_LIST);
		if(includeTests.isEmpty()) return files;
		
		ArrayList<File> filterFiles = new ArrayList<File>();
		
		List<KeyValue> filterList = DataHelper.getValidationMap(includeTests);
		for(KeyValue fitler : filterList) {
			String testCsv = fitler.key;
			for(File file : files) {
				if(file.getName().equals(testCsv)) {
					filterFiles.add(file);
					break;
				}
			}
		}
		
		return filterFiles;
	}
	
	/**
	 * filters csv tests based on exclude test list
	 * format: api.excludeTests = "TestCases_UserValidation.csv:getAdminToken-updateUser;TestCases_UserValidation2.csv"
	 */
	private static ArrayList<File> filterExcludeTestFiles(ArrayList<File> files) {
		String excludeTests = Config.getValue(SERVICE_EXCLUDE_LIST);
		if (excludeTests.isEmpty())
			return files;

		List<KeyValue> filterList = DataHelper.getValidationMap(excludeTests);
		Iterator<File> i = files.iterator();
		while (i.hasNext()) {
			File csvFile = i.next(); // must be called before you can call i.remove()
			for (KeyValue fitler : filterList) {
				String testCsv = fitler.key;
				String testFilter = fitler.value.toString();
				if (csvFile.getName().equals(testCsv) && testFilter.isEmpty())
					i.remove();

			}
		}
		return files;
	}

	/**
	 * print out warning for duplicated test names
	 * @param testCaseList
	 */
	public static void detectDuplicateTests(List<Object[]> testCaseList) {
		
		ArrayList<String> ids = new ArrayList<String>();
		for(Object[] test : testCaseList) {
			ServiceObject serviceObject = mapToServiceObject(test); 
			ids.add(serviceObject.getTestCaseID());
		}
		
		final Set<String> dups = new LinkedHashSet<String>();
	    final Set<String> set = new LinkedHashSet<String>();
	    for (final String id: ids)
	        if (!set.add(id))
	            dups.add(id);
	    
	    if(!dups.isEmpty())
	    	TestLog.logWarning("Duplicate test case ids detected. please use unique names: " + Arrays.toString(dups.toArray()));
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
		if(!isContainTestStep(csvRow[1].toString()))
			return csvRow[1].toString();
		String testname = getTestname(csvRow[1].toString());
		return testname;
	}
	
	/**
	 * get test name without test step identifier
	 * @param csvRow
	 * @return
	 */
	public static String getTestname(String fullTestname) {
		String testname = fullTestname.toString().split("(?i)_"+ SERVICE_STEP_PREFIX)[0];
		if(isContainTestStep(testname))
			testname = fullTestname.toString().split("(?i)-"+ SERVICE_STEP_PREFIX)[0];
		return testname;
	}
	
	private static boolean isContainTestStep(String testname) {
		if(testname.toLowerCase().contains("_"+SERVICE_STEP_PREFIX))
			return true;
		if(testname.toLowerCase().contains("-"+SERVICE_STEP_PREFIX))
			return true;
		return false;
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
			if(isContainTestStep(csvRow[1].toString())) {
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
	public static List<Object[]> updateCsvFileFromFile(List<Object[]> csvList, String csvFileName,  Map<String, List<Object[]>> testStepMap) {
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
			
			testCases.add(csvRow);
		}
		return testCases;
	}
	
	
	/**
	 * get list of test cases in range
	 * eg. test1-test5
	 * @param csvList
	 * @param range
	 * @return
	 */
	private static List<Object[]> getTestCasesInRange(List<Object[]> csvList, String[] range) {
		List<Object[]> testCases = new ArrayList<>();
		
		String startingTestId = range[0].trim();
		String endTestId = StringUtils.EMPTY;
		if(range.length == 2)
			endTestId = range[1].trim();
		
		if(range.length > 2)
			Helper.assertFalse("test case range must be specified. eg. test1-test3. existing value: " + Arrays.toString(range));
		
		boolean isWithinRange = false;

		// iterate through tests rows and add tests in between the test id range
		for (int i = 0; i < csvList.size(); i++) {
		
			ServiceObject service = CsvReader.mapToServiceObject(csvList.get(i));
		
			// if no range specified, only include the specified test case
			if(range.length == 1 && service.getTestCaseID().equals(startingTestId)) {
				testCases.add(csvList.get(i));
				return testCases;
			}
			
			// add tests that are within testcase id range. eg. test1-test10
			if(range.length == 2) {
				if(service.getTestCaseID().equals(startingTestId))
					isWithinRange = true;
				
				if(isWithinRange)
					testCases.add(csvList.get(i));
				
				if(service.getTestCaseID().equals(endTestId) && isWithinRange) {
					isWithinRange = false;
					break;
				}
			}
			
		}	
		return testCases;
	}
	
	/**
	 * update csv test list with tests from action csv files 
	 * eg. action:createUser.csv:createUser, updateUser
	 *  or action:createUser.csv
	 * @param testData
	 * @return 
	 */
	public static List<Object[]> addActionCsvTests(List<Object[]> csvList) {
		String csvTestPath = Helper.getFullPath(Config.getValue(TestDataProvider.TEST_DATA_ACTION_PATH));
		
		List<Object[]> updateDataList = new ArrayList<Object[]>();
		boolean hasActionKey = false;
				
		for(Object[] dataRow : csvList) {
			hasActionKey = false;
			ServiceObject serviceObject = CsvReader.mapToServiceObject(dataRow);
			
			// actions can be set at either method or options columns
			List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getMethod());
			keywords.addAll(DataHelper.getValidationMap(serviceObject.getOption()));
			
			String runFlag = serviceObject.getRunFlag();
			
			// move to next row if run flag is not Y
			if(!runFlag.equals("Y")) break;
			
			for (KeyValue keyword : keywords) {		
				if(keyword.key.equals(ACTION_KEY)) {
					
					
					List<KeyValue> TestIDMap = DataHelper.getValidationMap(keyword.value.toString());
					if(TestIDMap.isEmpty()) Helper.assertFalse("no csv file specified with action: " + keyword.key);
					
					String[] testIds = getTestCaseIds(keyword.value.toString());
					
					String actionCsvFile = TestIDMap.get(0).key;
					List<Object[]> tests = getCsvTestListForTestRunner(csvTestPath, actionCsvFile);
					
					// if test row is not External interface, add actions as addition to list of tests
					if(!serviceObject.getInterfaceType().equals(ServiceManager.EXTERNAL_INTERFACE))
						updateDataList.add(dataRow);
					
					// if testIds are set, get only the test specified
					if(testIds.length > 0)	
						updateDataList.addAll(getMatchingTestId(testIds, tests));
					else								
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
	 * gets a list of test case ids from testFile:testcaseId info
	 * Used for actions and test base
	 * testcase ids separated by ","
	 * format: testFile:testcaseId1,testcaseId2
	 * @param testFileInfo
	 * @return
	 */
	public static String[] getTestCaseIds(String testFileInfo) {
		List<KeyValue> TestIDMap = DataHelper.getValidationMap(testFileInfo);
		if(TestIDMap.isEmpty()) Helper.assertFalse("no csv file specified with action: " + testFileInfo);
		
		String[] testIds = TestIDMap.get(0).value.toString().split(",");
		testIds = DataHelper.removeEmptyElements(testIds);
		return testIds;
	}
	
	/**
	 * matches testIds with csv data rows TestId
	 * @param testIds
	 * @param testRows
	 * @return
	 */
	public static List<Object[]> getMatchingTestId(String[] testIds, List<Object[]> testRows) {	
		List<Object[]> updateDataList = new ArrayList<Object[]>();
		for(String testId : testIds) {
			for(Object[] row : testRows) {
				ServiceObject serviceObject = CsvReader.mapToServiceObject(row);
				if(testId.trim().equals(serviceObject.getTestCaseID())) {
					updateDataList.add(row);				
				}

			}
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
		String csvTestPath = Helper.getFullPath(testFolderPath);
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
		String csvTestPath = Helper.getFullPath(Config.getValue(TestDataProvider.TEST_DATA_PARALLEL_PATH));
		return getCsvFileList(csvTestPath);
	}
	
	/**
	 * returns a list of all csv test files
	 * 
	 * @param csvTestPath: full path to csv directory
	 * @return
	 */
	public static ArrayList<File> getCsvFileList(String csvTestPath) {
		boolean includeSubDirectories = Config.getBooleanValue(SERVICE_CSV_INCLUDE_SUB_DIR);
		ArrayList<File> csvFiles = Helper.getFileListByType(csvTestPath, ".csv", includeSubDirectories);
		return csvFiles;
	}

	/**
	 * returns the index of the file from the list of csv files
	 * 
	 * @param fileName
	 * @return
	 */
	public static int getCsvFileIndex(String fileName) {
		String csvTestPath = Helper.getFullPath(Config.getValue(TestDataProvider.TEST_DATA_PARALLEL_PATH));
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
	 * @param csvFile
	 * @return
	 */
	public static List<Object[]> getCsvTestListForTestRunner(String csvFile) {
		String csvTestPath = Helper.getFullPath(Config.getValue(TestDataProvider.TEST_DATA_PARALLEL_PATH));
		
		return getCsvTestListForTestRunner(csvTestPath, csvFile);	
	}
	
	/**
	 * gets csv tests list for service tests
	 * include and exclude tests
	 * eg. include: TestCases_UserValidation.csv:createUser-createUserNoToken, createUserInvalidToken;
	 * eg. exclude: TestCases_UserValidation.csv:createUser-createUserNoToken, createUserInvalidToken;
	 * @param csvInfo
	 * @return
	 */
	public static List<Object[]> getCsvTestListForTestRunner(String csvDir, String csvInfo) {
		List<Object[]> updateDataList = new ArrayList<Object[]>();
		ArrayList<File> testCsvFileList = getCsvFileList(csvDir);
		
		List<KeyValue> TestIDMap = DataHelper.getValidationMap(csvInfo);
		if(TestIDMap.isEmpty()) Helper.assertFalse("no csv file specified with test base: " + csvInfo);
		
		String[] testIds = getTestCaseIds(csvInfo);
		
		String csvFile = TestIDMap.get(0).key;
		
		int fileIndex = getFileIndex(testCsvFileList, csvFile);
		if(fileIndex == -1)
			Helper.assertFalse("csv file not found. csv file: " + csvFile + " at location: " + csvDir);
		List<Object[]> csvList = getCsvTestList(testCsvFileList.get(fileIndex));
		
		
		// if testIds are set, get only the test specified
		if(testIds.length > 0)	
			updateDataList.addAll(getMatchingTestId(testIds, csvList));
		else								
			updateDataList.addAll(csvList);
		
		
		return updateDataList;
	}

	/**
	 * reads csv file and returns the list of rows 
	 * include and exclude tests
	 * eg. include: TestCases_UserValidation.csv:createUser-createUserNoToken, createUserInvalidToken;
	 * eg. exclude: TestCases_UserValidation.csv:createUser-createUserNoToken, createUserInvalidToken;
	
	 * @param file
	 * @return
	 */
	public static List<Object[]> getCsvTestList(File file) {
		List<Object[]> csvList = new ArrayList<Object[]>();

		try {
			
		    CSVReader reader = readCsvFile(file);
		    
			// read header separately
			String[] header = reader.readNext();
			ArrayList<String> headerList = new ArrayList<String>();
			
			int runFlag = getColumnIndexByName("RunFlag", header);
			int testCaseIDIndex = getColumnIndexByName("TestCaseID", header);
			int testCaseSuiteIndex =  getColumnIndexByName("TestSuite", header);
			String testCaseSuite = StringUtils.EMPTY;

			// only add tests that have runFlag set to Y And testCaseID are set
			String[] line;
			while ((line = reader.readNext()) != null) {
				// limit array to size of values. eg. 15 column values
				String[] newline = Arrays.copyOfRange(line, 0, SERVICE_CSV_VISIBLE_COLUMN_COUNT);
				if (newline[runFlag].equals("Y") && !newline[testCaseIDIndex].isEmpty()) {
					csvList.add(newline);
					
					// set testSuit name. need to set only 1 time
					if(testCaseSuite.isEmpty()) {
						testCaseSuite = newline[testCaseSuiteIndex];
						headerList = new ArrayList<String>(Arrays.asList(header));
						headerList.add(testCaseSuite);
					}
				}
			}
			// store header column names, using test suite as key
			TestObject.getGlobalTestInfo().serviceObject.withHeaderMap(testCaseSuite,headerList);
					
						
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// filter based on include test cases
		csvList = setIncludeTestRange(file, csvList);
		
		// filter based on exclude test cases
		csvList = setExcludeTestRange(file, csvList);
			
		return csvList;
	}
	
	/**
	 * api.includeTests = "TestCases_UserValidation.csv:createUser-createUserNoToken, createUserInvalidToken;"
	 * @param file
	 * @param csvList
	 * @return
	 */
	private static List<Object[]> setIncludeTestRange(File file, List<Object[]> csvList) {
		String filterRequirements = StringUtils.EMPTY;
		
		// if no filter set, return original list
		filterRequirements = Config.getValue(SERVICE_INCLUDE_LIST);
		if(filterRequirements.isEmpty()) return csvList;
		
		// will include keyword tests
		if(file.getAbsolutePath().contains(File.separator + "keywords" + File.separator))
			return csvList;
		
		List<KeyValue> filterList = DataHelper.getValidationMap(filterRequirements);
		
		// does test name match test file
		for(KeyValue filter: filterList ) {
			if(filter.key.contentEquals(file.getName())){
				
				// if no test range
				if(filter.value.toString().isEmpty()) return csvList;
				csvList = getIncludeTestRange(csvList, filter.value.toString());
				return csvList;
				
			}	
		}	  	
		return new ArrayList<Object[]>();
	}
	
	/**
	 * format for range: createUser-createUserNoToken, createUserInvalidToken
	 * @param csvList
	 * @param range
	 */
	private static List<Object[]> getIncludeTestRange(List<Object[]> csvList, String testRange) {
		List<Object[]> testCases = new ArrayList<Object[]>();
		String[] rangeArray = testRange.split(",");
		
		// for test case selection. Both test case file And test case have to be set
		// iterate through range list and filter list 
		for(String rangeValue : rangeArray) {		
			String[] range = rangeValue.split("-");		
			testCases.addAll(getTestCasesInRange(csvList, range));
		}
		return testCases;
	}
	
	/**
	 * api.excludeTests = "TestCases_UserValidation.csv:createUser-createUserNoToken, createUserInvalidToken;"
	 * @param file
	 * @param csvList
	 * @return
	 */
	private static List<Object[]> setExcludeTestRange(File file, List<Object[]> csvList) {
		String filterRequirements = StringUtils.EMPTY;
		
		// if no filter set, return original list
		filterRequirements = Config.getValue(SERVICE_EXCLUDE_LIST);
		if(filterRequirements.isEmpty()) return csvList;
		
		List<KeyValue> filterList = DataHelper.getValidationMap(filterRequirements);
		
		// does test name match test file
		for(KeyValue filter: filterList ) {
			if(filter.key.contentEquals(file.getName())){
				
				// if no test range, exclude entire list
				if(filter.value.toString().isEmpty()) return new ArrayList<Object[]>();
				
				csvList = getExcludeTestRange(csvList, filter.value.toString());
				
			}	
		}	
	    	
		return csvList;
	}
	
	/**
	 * format for range: createUser-createUserNoToken, createUserInvalidToken
	 * @param csvList
	 * @param range
	 */
	private static List<Object[]> getExcludeTestRange(List<Object[]> csvList, String testRange) {
		List<Object[]> testCases = new ArrayList<Object[]>();
		String[] rangeArray = testRange.split(",");
		
		// for test case selection. Both test case file And test case have to be set
		// iterate through range list and filter list 
		for(String rangeValue : rangeArray) {		
			String[] range = rangeValue.split("-");		
			testCases.addAll(getTestCasesInRange(csvList, range));
		}
		
		// remove filtered list from the original list
		csvList.removeAll(testCases);
		
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
		Helper.assertTrue("column not found. Expecting: " + column + ". Perhaps spelling mistake on column names of the csv file? Existing columns: " + Arrays.toString(row), false);
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
		
		// in case we have filename:testCaseId format
		String[] filenames = filename.split(":");
		filename = filenames[0];
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
		Config.putValue(SERVICE_RUN_COUNT, 1, false);
	}
}