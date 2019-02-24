package core.apiCore.helpers;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import au.com.bytecode.opencsv.CSVReader;
import core.apiCore.dataProvider;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.objects.ApiObject;
import core.support.objects.TestObject;

public class csvReader {

	/**
	 * gets the tests from csv file based on the current test file index
	 * 
	 * @return
	 */
	public synchronized static List<Object[]> getTestCasesFromCsvFile() {
		List<Object[]> testCases = new ArrayList<>();
		int index = getCurrentTestInvocation();

		// if single test case is specified, then only load that file
		String testCaseFile = Config.getValue(dataProvider.TEST_CASE_FILE);
		if (!testCaseFile.isEmpty())
			index = getCsvFileIndex(testCaseFile);

		String csvFileName = csvReader.getCsvFileFromIndex(index);

		List<String[]> csvList = getCsvTestListForTestRunner(csvFileName);
		for (int i = 0; i < csvList.size(); i++) {
			// add testname and test index
			String[] obj = { csvFileName, String.valueOf(i) };
			String[] csvRow = (String[]) ArrayUtils.addAll(csvList.get(i), obj);

			// for single test case selection. Both test case file and test case have to be
			// set
			String testCase = Config.getValue(dataProvider.TEST_CASE);
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
	 * maps list of testcases to api object map
	 * 
	 * @param testCases
	 * @return
	 */
	public static Map<String, ApiObject> mapToApiObject(List<String[]> testCases) {
		Map<String, ApiObject> apiMap = new ConcurrentHashMap<String, ApiObject>();
		for (String[] testCase : testCases) {
			// add parameters to ApiObject
			ApiObject apiObject = new ApiObject().setApiObject(testCase[0], testCase[1], testCase[2], testCase[3],
					testCase[4], testCase[5], testCase[6], testCase[7], testCase[8], testCase[9], testCase[10],
					testCase[11], testCase[12], testCase[13], testCase[14], testCase[15],
					"", "");
			apiMap.put(apiObject.TestCaseID, apiObject);
		}
		return apiMap;
	}

	/**
	 * gets all test cases from testcase path based on prefix
	 */
	public static void getAllTestCases(String testPath, String prefix) {
		ArrayList<File> csvFiles = Helper.getFileList(testPath, prefix);
		for (int i = 0; i < csvFiles.size(); i++) {
			List<String[]> testCases = getCsvTestList(csvFiles.get(i));
			Map<String, ApiObject> apiMap = mapToApiObject(testCases);
			TestObject.getTestInfo().apiMap.putAll(apiMap);
		}
	}

	/**
	 * gets all keywords and stores them in apiMap in testObject
	 */
	public synchronized static void getAllKeywords() {
		String testFolderPath = Config.getValue(dataProvider.API_KEYWORD_PATH);
		String csvTestPath = PropertiesReader.getLocalRootPath() + testFolderPath;
		csvReader.getAllTestCases(csvTestPath, ".csv");
	}

	/**
	 * gets csv file index does not increment when retry
	 * 
	 * @return
	 */
	public static int getCurrentTestInvocation() {

		// if test class (based on csv file) has initiated, get the current csv file
		if(TestObject.getTestInfo().testCsvFileName != null) 
		{
			String activeTest = TestObject.getTestInfo().testCsvFileName;
			return getCsvFileIndex(activeTest);
		}
		return dataProvider.csvFileIndex.getAndIncrement();
	}

	/**
	 * returns current csv test file
	 * 
	 * @return
	 */
	public static String getCsvFileFromIndex(int index) {
		ArrayList<File> testCsvList = getCsvFileList();
		return testCsvList.get(index).getName();
	}

	public static int getCsvFileCount() {
		return getCsvFileList().size();
	}

	/**
	 * returns a list of all csv test files
	 * 
	 * @return
	 */
	public static ArrayList<File> getCsvFileList() {
		String csvTestPath = PropertiesReader.getLocalRootPath() + dataProvider.TEST_DATA_PATH;
		ArrayList<File> csvFiles = Helper.getFileList(csvTestPath, ".csv");
		return csvFiles;
	}

	/**
	 * returns the index of the file from the list of csv files
	 * 
	 * @param fileName
	 * @return
	 */
	public static int getCsvFileIndex(String fileName) {
		Helper.assertTrue("csv file is null", fileName != null);

		ArrayList<File> csvFiles = getCsvFileList();

		OptionalInt indexOpt = IntStream.range(0, csvFiles.size())
				.filter(i -> fileName.contains(csvFiles.get(i).getName())).findFirst();

		Helper.assertTrue("csv file: " + fileName + " not found", indexOpt.isPresent());
		return indexOpt.getAsInt();
	}

	/**
	 * gets csv tests list for api tests
	 * 
	 * @param csvFile
	 * @return
	 */
	public static List<String[]> getCsvTestListForTestRunner(String csvFile) {
		List<String[]> csvList = new ArrayList<String[]>();
		ArrayList<File> testCsvFileList = getCsvFileList();
		int fileIndex = getFileIndex(testCsvFileList, csvFile);
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

			// only add tests that have runFlag set to Y and testCaseID is set
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