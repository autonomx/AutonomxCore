package core.support.listeners;

import java.util.ArrayList;
import java.util.List;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import core.helpers.Helper;
import core.helpers.excelHelper.ExcelObject;
import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.drivers.AbstractDriverTestNG;

public class RetryTest implements IRetryAnalyzer {

	public static final String[] PageErrors = { "MultipleFailureException", "WebDriverException", "GridException",
			"SessionNotFoundException", "UnreachableBrowserException", "loginException" };

	public enum ReportType {
		pass, info, warning, debug, fail
	}

	// needs to be removed. used only for headcheck login tests
	public static int userIndex = 0;
	public static int login_success_withError = 0;
	public static List<ExcelObject> exceList = new ArrayList<ExcelObject>();
	public static List<String> errorList = new ArrayList<String>();

	private int maxRetryCount = 1;
	public int retryCount = 1;
	public static boolean enableRetry = true;
	private ExtentTest test;
	private ExtentTest step;

	public RetryTest() {
	}

	public RetryTest(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public void setExtendReport(ExtentTest test, ExtentTest step) {
		this.test = test;
		this.step = step;
	}

	public void setExtendReport() {
		this.test = TestObject.getTestInfo().testScenerio;
		this.step = AbstractDriverTestNG.step.get();
	}

	public ExtentTest getTest() {
		return test;
	}

	public ExtentTest getStep() {
		return step;
	}

	@Override
	public boolean retry(ITestResult iTestResult) {
		// ITestResult iTestResult
		setExtendReport();
		TestObject.getTestInfo().withCaughtThrowable(iTestResult.getThrowable());
		processTestResult();

		if (TestObject.getTestInfo().runCount < maxRetryCount) {
			TestObject.getTestInfo().incremenetRunCount();
			return true;
		}

		return false;
	}

	public String getTestId(ITestResult iTestResult) {
		String className = iTestResult.getInstanceName().substring(iTestResult.getInstanceName().lastIndexOf(".") + 1);
		String testId = className + "-" + iTestResult.getName();
		return testId;
	}

	/**
	 * 
	 * if the max retry has not been reached, log the failure and quite the browser
	 */
	public void processTestResult() {
		logReport(ReportType.info, "run " + (TestObject.getTestInfo().runCount) + " failed ", null);
		logReport(ReportType.debug, null, TestObject.getTestInfo().caughtThrowable);
		Helper.captureExtentReportScreenshot();
		logError("run " + (TestObject.getTestInfo().runCount) + " failed");

		if (TestObject.getTestInfo().runCount == maxRetryCount) {
			logReport(ReportType.fail, "giving up after " + maxRetryCount + " failures", null);
			logError("giving up after " + maxRetryCount + " failures");
		}
		DriverObject.quitTestDrivers();
	}

	public void logReport(ReportType type, String value, Throwable t) {

		if (getStep() == null)
			return;
		switch (type) {
		case pass:
			getStep().pass(value);
			break;
		case info:
			getStep().info(value);
			break;
		case warning:
			getStep().warning(value);
			break;
		case debug:
			getStep().debug(t);
			break;
		case fail:
			getStep().fail(value);
			break;
		default:
			break;
		}
	}

	public void logError(String error) {
		TestLog.ConsoleLog(error);
	}

	/**
	 * returns true if any of the error types specified is caught
	 * 
	 * @param t
	 * @return
	 */
	public boolean pageHasError(Throwable t) {
		for (String error : PageErrors) {
			if (t.getClass().toString().contains(error))
				return true;
		}
		return false;
	}

	/**
	 * error handling when test fails if any of the defined errors specified by
	 * PageErrors exists, then the test will be retried
	 * 
	 * @param t
	 */
	public void errorHandling(Throwable t) {
		if (pageHasError(t)) {
			setMaxRetryCount(maxRetryCount + 1);
		}
	}

	public void randomFailStack(ArrayList<String> FailTrace) {
		if (FailTrace.size() > 0) {
			TestLog.ConsoleLog("And finally test passed after " + TestObject.getTestInfo().runCount + " failures");
			getStep().log(Status.PASS,
					"And finally test passed after " + TestObject.getTestInfo().runCount + " failures");
		} else {
			TestLog.ConsoleLog("And finally test passed without failures");
			getStep().log(Status.PASS, "And finally test passed without failures");
		}
	}
}