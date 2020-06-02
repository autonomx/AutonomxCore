package core.support.listeners;

import java.util.ArrayList;
import java.util.List;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import core.helpers.Helper;
import core.helpers.excelHelper.ExcelObject;
import core.support.logger.TestLog;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriverTestNG;

public class RetryTest implements IRetryAnalyzer {

	public static final String[] PageErrors = { "MultipleFailureException", "WebDriverException", "GridException",
			"SessionNotFoundException", "UnreachableBrowserException", "LoginException" };

	public enum ReportType {
		pass, info, warning, debug, fail, code
	}

	// needs to be removed. used only for headcheck login tests
	public static int userIndex = 0;
	public static int login_success_withError = 0;
	public static List<ExcelObject> exceList = new ArrayList<ExcelObject>();
	public static List<String> errorList = new ArrayList<String>();

	public int retryCount = 1;
	public static boolean enableRetry = true;
	private ExtentTest test;
	private ExtentTest step;

	public RetryTest() {
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

		// update retry count from config
		int maxRetryCount = CrossPlatformProperties.getRetryCount();
		
		setExtendReport();
		TestObject.getTestInfo().withCaughtThrowable(iTestResult.getThrowable());

		// if the max retry has not been reached, log the failure And quite the browser
		maxRetryCount = processTestResult(maxRetryCount);

		// if the max retry has not been reached, increment test count and continue to
		// retry the test
		if (TestObject.getTestInfo().runCount < maxRetryCount + 1) {
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
	 * if the max retry has not been reached, log the failure And quite the browser
	 * 
	 * @return
	 */
	public int processTestResult(int maxRetryCount) {
		logReport(ReportType.info, "run " + (TestObject.getTestInfo().runCount) + " failed ", null);

		logReport(ReportType.code, TestObject.getTestInfo().caughtThrowable.toString(), null);

		// handle exception by adding extra retries
		maxRetryCount = errorHandling(TestObject.getTestInfo().caughtThrowable, maxRetryCount);

		// capture error screenshot
		Helper.captureExtentReportScreenshot();

		logError("run " + (TestObject.getTestInfo().runCount) + " failed");

		if (TestObject.getTestInfo().runCount == maxRetryCount + 1) {
			logReport(ReportType.fail, "giving up after " + (maxRetryCount + 1) + " failures", null);
			logError("giving up after " + (maxRetryCount + 1) + " failures");
		}

		return maxRetryCount;
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
		case code:
			Markup m = MarkupHelper.createCodeBlock(value);
			getStep().info(m);
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
	 * error handling When test fails if any of the defined errors specified by
	 * PageErrors exists, Then the test will be retried
	 * 
	 * @param t
	 * @return
	 */
	public int errorHandling(Throwable t, int maxRetryCount) {
		if (pageHasError(t)) {
			return ++maxRetryCount;
		}
		return maxRetryCount;
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