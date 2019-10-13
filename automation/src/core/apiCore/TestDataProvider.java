package core.apiCore;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;

import core.apiCore.helpers.CsvReader;
import core.support.configReader.Config;

public class TestDataProvider {

	public static String TEST_DATA_PATH;
	public static final String TEST_DATA_TEMPLATE_PATH = "api.templatePath";

	public static final String TEST_DATA_PARALLEL_PATH = "api.parallelTestcasePath";
	public static final String TEST_CASE_FILE = "api.testCaseFile";
	public static final String TEST_CASE = "api.testCase";
	public static final String API_KEYWORD_PATH = "api.keywordPath"; // directory to keyword csv files

	public static AtomicInteger csvFileIndex = new AtomicInteger(0);
	public static ThreadLocal<Logger> log = new ThreadLocal<Logger>();

	@DataProvider(name = "parallelRun")
	public synchronized Iterator<Object[]> providerParallel() {

		TestDataProvider.TEST_DATA_PATH = Config.getValue(TestDataProvider.TEST_DATA_PARALLEL_PATH);
		return CsvReader.getTestCasesFromCsvFile().iterator();
	}
}