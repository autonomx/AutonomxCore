package core.support.rules;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.appcenter.appium.EnhancedAndroidDriver;

import core.helpers.Helper;
import core.helpers.StopWatchHelper;
import core.support.configReader.PropertiesReader;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

public class RetryTest implements TestRule {

	public static final String[] PageErrors = { "MultipleFailureException", "WebDriverException", "GridException",
			"SessionNotFoundException", "UnreachableBrowserException", "loginException" };

	public enum ReportType {
		pass, info, warning, debug, fail
	}

	private int retryCount;
	private int testRun;
	public static final int RETRYCOUNTER = CrossPlatformProperties.getRetryCount();
	public static boolean enableRetry = true;
	private ExtentTest test;
	private ExtentTest step;
	private ExtentReports extent;
	public WebDriver webDriver;

	public RetryTest(int retryCount) {
		this.retryCount = retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public int getRetryCount() {
		return this.retryCount;
	}

	public int getCurrentTestRun() {
		return this.testRun;
	}

	public void setExtendReport(ExtentTest test, ExtentTest step, ExtentReports extent) {
		this.test = test;
		this.step = step;
		this.extent = extent;
	}

	public ExtentTest getTest() {
		return test;
	}

	public ExtentTest getStep() {
		return step;
	}

	public void setWebDriver(WebDriver driver) {
		this.webDriver = driver;
	}

	public Statement apply(Statement base, Description description) {
		return statement(base, description);
	}

	private Statement statement(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				setTestId(description);

				long passedTimeInSeconds = 0;
				StopWatchHelper watch = StopWatchHelper.start();

				// implement retry logic here
				for (testRun = 1; testRun <= retryCount; testRun++) {
					try {
						base.evaluate();
						TestObject.getTestInfo().isTestPass = true;

						// if test pass, first run is false
						TestObject.getTestInfo().isFirstRun = false;
						TestObject.getTestInfo().withIsForcedRestart(false);

						// driver is now available
						if (webDriver != null) {
							DriverObject.setDriverAvailabiltity(webDriver, true);
						}
						break;
					} catch (Throwable t) {
						// print page source
						getPageSource();

						t.printStackTrace();
						TestObject.getTestInfo().caughtThrowable = t;
						errorHandling(TestObject.getTestInfo().caughtThrowable);
						takeAppcenterScreenshot(t.getClass().getName());
					}
					processTestResult(description);
				}
				passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
				TestLog.ConsoleLog("test duration: " + passedTimeInSeconds + " seconds");

				takeAppcenterScreenshot("test finished: " + passedTimeInSeconds + " seconds");
				if (!CrossPlatformProperties.isSingleSignIn())
					DriverObject.quitTestDrivers();
				writeToTestReport();
				return;
			}
		};

	}
	
	/**
	 print page source
	 */
	public void getPageSource() {
		if(webDriver != null)
			TestLog.ConsoleLog(webDriver.getPageSource());

	}

	public void setTestId(final Description description) {
		TestObject.setTestName(description.getMethodName());
		String className = description.getClassName().substring(description.getClassName().lastIndexOf(".") + 1);
		TestObject.setTestId(className + "-" + TestObject.currentTestName.get());
	}

	public void takeAppcenterScreenshot(String label) {
		if (PropertiesReader.isUsingCloud()) {
			try {
				((EnhancedAndroidDriver<?>) webDriver).label(label);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void processTestResult(Description description) throws Throwable {
		if (TestObject.getTestInfo().isTestPass) {
			randomFailStack(TestObject.getTestInfo().failTrace, TestObject.getTestInfo().description);
		} else {
			logReport(ReportType.info, "run " + (testRun) + " failed ", null);
			logReport(ReportType.debug, null, TestObject.getTestInfo().caughtThrowable);
			Helper.captureExtentReportScreenshot();
			TestLog.ConsoleLogError("run " + (testRun) + " failed");
			DriverObject.quitTestDrivers();

			if (testRun == retryCount) {
				logReport(ReportType.fail, "giving up after " + retryCount + " failures", null);
				TestLog.ConsoleLogError("giving up after " + retryCount + " failures");
				writeToTestReport();
				throw TestObject.getTestInfo().caughtThrowable;
			}
		}
	}

	public void writeToTestReport() {
		if (extent != null) {
			new File(ExtentManager.getReportHTMLFullPath()).mkdirs();
			extent.flush();
		}
		// for writing file structure of test directory incase no access
		// File curDir = new File(".");
		// getAllFiles(curDir);
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
			setRetryCount(RetryTest.RETRYCOUNTER + 3);
		} else {
			setRetryCount(RetryTest.RETRYCOUNTER);
		}
	}

	public void randomFailStack(ArrayList<String> FailTrace, Description description) {
		if (FailTrace.size() > 0) {
			TestLog.ConsoleLog("And finally test passed after " + retryCount + " failures");
			getStep().log(Status.PASS, "And finally test passed after " + testRun + " failures");
		} else {
			TestLog.ConsoleLog("And finally test passed without failures");
			getStep().log(Status.PASS, "And finally test passed without failures");
		}
	}

	/**
	 * gets the stack trace of the failure
	 *
	 * @param throwable
	 * @return
	 */
	public static String getStackTrace(Throwable throwable) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		return writer.toString();
	}

	public static ArrayList<String> getAllFiles(File curDir) {
		ArrayList<String> array = new ArrayList<String>();
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory())
				getAllFiles(f);
			if (f.isFile()) {
				TestLog.ConsoleLog("All files: " + f.getPath() + " : " + f.getName());
				array.add("All files: " + f.getPath() + " : " + f.getName());
			}
		}
		return array;

	}
}
