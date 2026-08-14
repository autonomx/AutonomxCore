package core.support.listeners;

import static core.support.CoreSupport.getGlobalConfigBoolean;
import static core.support.CoreSupport.getGlobalConfigInt;
import static core.support.CoreSupport.getGlobalConfigString;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.IClass;
import org.testng.IClassListener;
import org.testng.IConfigurationListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import core.helpers.ConfigHelper;
import core.helpers.DataHelper;
import core.helpers.UtilityHelper;
import core.helpers.ZipHelper;
import core.support.CoreSupport;
import core.support.Log;
import core.support.TestLog;
import core.support.objects.TestObject;
import core.uiCore.WebDriverSetup;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.drivers.AbstractDriverTestNG;
import core.utils.ExtentManager;
import core.utils.ReportPortalManager;
import core.utils.ScreenRecorderHelper;

public class TestListener implements ITestListener, IClassListener, ISuiteListener, IConfigurationListener {

	public static boolean isTestNG = false;
	public static final String PARALLEL_TEST_TYPE = "global.parallel.type";
	public static final String CONSOLE_PAGESOURCE_ON_FAIL = "console.pageSource.onFail";
	public static final String GLOBAL_SKIP_TESTS = "global.skipTests";
	public static final String GLOBAL_SKIP_TESTS_MESSAGE = "skipped on purpose";

	public static final String FAILED_RERUN_SUITE_NAME = "failed_rerun_suite";
	public static final String FAILED_RERUN_OPTION = "global.ui.rerun.failed.after.suite ";
	public static List<String> FAILED_RERUN_SUITE_PASSED_TESTS = new ArrayList<String>();

	
	public TestListener() {
		TestLog.setupLog4j();
	}
	
	// Before starting all tests, below method runs.
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(ITestContext iTestContext) {
		isTestNG = true;
		Object webDriver = AbstractDriverTestNG.getWebDriver();
		if (webDriver != null)
			iTestContext.setAttribute("WebDriver", webDriver);

		// print out suite console logs if batch logging is enabled
		String testId = getSuiteName(iTestContext.getSuite().getName().toString()) + TestObject.BEFORE_SUITE_PREFIX;
		TestLog.printBatchToConsole(testId);

		// shuts down webdriver processes
		cleanupProcessess();
		// Retry analyzers are assigned by AnnotationTransformer.
		iTestContext.setAttribute("platform", "");

// sets parallel run for default user. overwritten by suite xml settings
		setParallelRun(iTestContext);
		
		// overwrite existing report
		ExtentManager.clearTestReport(iTestContext.getSuite().getName());

		// delete old reports
		ExtentManager.clearOldTestReports();

		// delete screen recorder temp directory
		ScreenRecorderHelper.deleteScreenRecorderTempDir();
	}

	@Override
	public void onTestStart(ITestResult result) {
		String testId = result.getMethod().getTestClass().getRealClass().getName() + "." + result.getMethod().getMethodName();
		TestLog.setTestId(testId);
		TestLog.logInfo("Starting test: " + result.getName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		TestLog.logPass("Test passed: " + result.getName());
	}

	@Override
	public void onTestFailure(ITestResult result) {
		TestLog.logFail("Test failed: " + result.getName(), result.getThrowable());
		if (getGlobalConfigBoolean(CONSOLE_PAGESOURCE_ON_FAIL, false)) {
			try {
				TestLog.logInfo("Page source: " + AbstractDriverTestNG.getWebDriver().getPageSource());
			} catch (Exception e) {
				TestLog.logInfo("Unable to retrieve page source: " + e.getMessage());
			}
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		TestLog.logInfo("Test skipped: " + result.getName());
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

	@Override
	public void onFinish(ITestContext context) {
	}

	@Override
	public void onBeforeClass(ITestClass testClass) {
	}

	@Override
	public void onAfterClass(ITestClass testClass) {
	}

	@Override
	public void onStart(ISuite suite) {
	}

	@Override
	public void onFinish(ISuite suite) {
		try {
			cleanupProcessess();
		} catch (Exception e) {
			TestLog.logInfo("Unable to clean up processes: " + e.getMessage());
		}
	}

	@Override
	public void onConfigurationSuccess(ITestResult itr) {
	}

	@Override
	public void onConfigurationFailure(ITestResult itr) {
		TestLog.logFail("Configuration failed: " + itr.getName(), itr.getThrowable());
	}

	@Override
	public void onConfigurationSkip(ITestResult itr) {
	}

	public static void setParallelRun(ITestContext iTestContext) {
		String parallelType = getGlobalConfigString(PARALLEL_TEST_TYPE, "");
		if (parallelType == null || parallelType.trim().isEmpty())
			return;

		try {
			iTestContext.getCurrentXmlTest().setParallel(parallelType);
		} catch (Exception e) {
			TestLog.logInfo("Unable to set parallel test type: " + e.getMessage());
		}
	}

	public static String getSuiteName(String suiteName) {
		if (suiteName == null)
			return "";
		return suiteName.replaceAll("[^a-zA-Z0-9_-]", "_");
	}

	public static void cleanupProcessess() {
		try {
			WebDriverSetup.cleanupDriverProcesses();
		} catch (Exception e) {
			TestLog.logInfo("Unable to clean up webdriver processes: " + e.getMessage());
		}
	}

	public static void zipReportDirectory() {
		try {
			String reportDirectory = CoreSupport.getReportDirectory();
			if (reportDirectory != null && new File(reportDirectory).exists()) {
				String zipFile = reportDirectory + ".zip";
				ZipHelper.zipDirectory(reportDirectory, zipFile);
			}
		} catch (Exception e) {
			TestLog.logInfo("Unable to zip report directory: " + e.getMessage());
		}
	}

	public static void processFailedRerunSuite(ISuite suite) {
		if (!getGlobalConfigBoolean(FAILED_RERUN_OPTION, false))
			return;

		try {
			String suiteName = suite.getName();
			if (FAILED_RERUN_SUITE_NAME.equalsIgnoreCase(suiteName))
				return;

			List<String> failedTests = new ArrayList<String>();
			suite.getResults().forEach((key, value) -> {
				value.getTestContext().getFailedTests().getAllResults().forEach(result -> {
					failedTests.add(result.getMethod().getQualifiedName());
				});
			});

			if (!failedTests.isEmpty()) {
				TestLog.logInfo("Failed tests available for rerun: " + failedTests);
			}
		} catch (Exception e) {
			TestLog.logInfo("Unable to process failed rerun suite: " + e.getMessage());
		}
	}
}