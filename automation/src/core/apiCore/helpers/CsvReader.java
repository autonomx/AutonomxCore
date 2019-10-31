package core.apiCore.helpers;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import com.opencsv.CSVReader;

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
		List<String[]> csvList = getCsvTestListForTestRunner(csvFileName);
		
		// update csv test list with action csv file values
		List<String[]> updatedCsvList = new ArrayList<String[]>();
		updatedCsvList.addAll(addActionCsvTests(csvList));
		
		return updateCsvFileFromFile(updatedCsvList, csvFileName, testCaseFile);
	}
	
	/**
	 * update csv test list with additional meta data (csv file name, test index, service type, test count)
	 * if test case is set then single test is returned
	 * @param csvList
	 * @param csvFileName
	 * @param testCaseFile
	 * @return
	 */
	public static List<Object[]> updateCsvFileFromFile(List<String[]> csvList, String csvFileName, String testCaseFile) {
		List<Object[]> testCases = new ArrayList<>();
		
		for (int i = 0; i < csvList.size(); i++) {
			// add test name, test index, and test type 
			String[] obj = { csvFileName, String.valueOf(i), TestObject.testType.service.name(), String.valueOf(csvList.size())};
			String[] csvRow = (String[]) ArrayUtils.addAll(csvList.get(i), obj);			
			
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
	public static List<String[]> addActionCsvTests(List<String[]> csvList) {
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TestDataProvider.TEST_DATA_ACTION_PATH);
		
		List<String[]> updateDataList = new ArrayList<String[]>();
		boolean hasActionKey = false;
				
		for(String[] dataRow : csvList) {
			hasActionKey = false;
			ServiceObject serviceObject = CsvReader.mapToServiceObject(dataRow);
			List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getMethod());
			for (KeyValue keyword : keywords) {		
				if(keyword.key.equals(ACTION_KEY)) {
					List<String[]> tests = getCsvTestListForTestRunner(csvTestPath, keyword.value.toString());
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
	public static Map<String, ServiceObject> mapToApiObject(List<String[]> testCases) {
		Map<String, ServiceObject> serviceMap = new ConcurrentHashMap<String, ServiceObject>();
		for (String[] testCase : testCases) {
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
		if(testData.length == 16) {
			return new ServiceObject().setServiceObject(testData[0].toString(), testData[1].toString(), testData[2].toString(),
					testData[3].toString(), testData[4].toString(), testData[5].toString(), testData[6].toString(), testData[7].toString(), testData[8].toString(), testData[9].toString(),
					testData[10].toString(), testData[11].toString(), testData[12].toString(), testData[13].toString(), testData[14].toString(), testData[15].toString(), "", "", "", "");
			
		}
		else if (testData.length == 20){
		return  new ServiceObject().setServiceObject(testData[0].toString(), testData[1].toString(), testData[2].toString(),
				testData[3].toString(), testData[4].toString(), testData[5].toString(), testData[6].toString(), testData[7].toString(), testData[8].toString(), testData[9].toString(),
				testData[10].toString(), testData[11].toString(), testData[12].toString(), testData[13].toString(), testData[14].toString(), testData[15].toString(), testData[16].toString(), testData[17].toString(), testData[18].toString(), testData[19].toString());

		}
		
		Helper.assertFalse("test data length does not match requirements: " + testData.length + " test data: " + Arrays.toString(testData) );
		return new ServiceObject();
	}

	/**
	 * gets all test cases from testcase path based on prefix
	 */
	public static void getAllTestCases(String testPath, String prefix) {
		ArrayList<File> csvFiles = Helper.getFileListByType(testPath, prefix);
		for (int i = 0; i < csvFiles.size(); i++) {
			List<String[]> testCases = getCsvTestList(csvFiles.get(i));
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
	public static List<String[]> getCsvTestListForTestRunner(String csvFile) {
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
	public static List<String[]> getCsvTestListForTestRunner(String csvDir, String csvFile) {
		List<String[]> csvList = new ArrayList<String[]>();
		ArrayList<File> testCsvFileList = getCsvFileList(csvDir);
		int fileIndex = getFileIndex(testCsvFileList, csvFile);
		if(fileIndex == -1)
			Helper.assertFalse("csv file not found. csv file: " + csvFile + " at location: " + csvDir);
		csvList = getCsvTestList(testCsvFileList.get(fileIndex));
		return csvList;
	}

	public static List<String[]> getCsvTestList(File file) {
		List<String[]> csvList = new ArrayList<String[]>();

		try {
			CSVReader reader = new CSVReader(new FileReader(file.getPath()));

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
}