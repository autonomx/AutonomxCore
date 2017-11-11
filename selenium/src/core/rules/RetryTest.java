package core.rules;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;

import core.helpers.ExcelHelper.ExcelObject;
import core.logger.PropertiesReader;

public class RetryTest implements TestRule {

	public static final String[] PageErrors = { "MultipleFailureException", "WebDriverException", "GridException",
			"SessionNotFoundException", "UnreachableBrowserException" };

	public enum ReportType {
		pass, info, warning, debug, fail
	}
	
	// needs to be removed. used only for headcheck login tests
	public static int userIndex = 0;
	public static int login_success_withError = 0;
	public static List<ExcelObject> exceList = new ArrayList<ExcelObject>();
	public static List<String> errorList = new ArrayList<String>();
	
	private int retryCount;
	private int testRun;
	public static final int RETRYCOUNTER = PropertiesReader.getRetryCount();
	public static boolean enableRetry = true;
	private ExtentTest test;
	private ExtentTest step;
	private ExtentReports extent;
	private Logger log;
	private WebDriver webDriver;

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

	public void setLogger(Logger log) {
		this.log = log;
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
				TestObject test = new TestObject();
				test.description = description;
				
				// implement retry logic here
				for (testRun = 1; testRun <= retryCount; testRun++) {
					try {
						base.evaluate();
						test.isTestPass = true;
						break;
					} catch (Throwable t) {
						test.caughtThrowable = t;
						errorHandling(test.caughtThrowable);
					}
					processTestResult(test);
				}
				quitWebDriver();
				writeToTestReport();
				return;
			}
		};
	}
	
	public void processTestResult(TestObject test) throws Throwable {
		if (test.isTestPass) {
			randomFailStack(test.failTrace, test.description);
		} else {
			logReport(ReportType.info, "run " + (testRun) + " failed ", null);
			logReport(ReportType.debug, null,  test.caughtThrowable);
			captureScreenshot(test.description);
			logError("run " + (testRun) + " failed");
			quitWebDriver();
			
			if (testRun == retryCount) {
				logReport(ReportType.fail, "giving up after " + retryCount + " failures", null);
				logError("giving up after " + retryCount + " failures");
				writeToTestReport();
				throw test.caughtThrowable;
			}
		}
	}
	
	public void writeToTestReport() {
		if(extent != null)
			extent.flush();
	}
	
	public void logReport(ReportType type, String value, Throwable t) {
		
		if(getStep() == null) return;
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
		if (log != null)
			log.error(error);
	}

	/**
	 * quits webdriver if it's running
	 */
	public void quitWebDriver() {
		if (webDriver != null) {
			try {
				webDriver.quit();
			}catch(Exception e) {
				e.getMessage();
			}
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
			setRetryCount(RetryTest.RETRYCOUNTER + 1);
		}
	}

	public void randomFailStack(ArrayList<String> FailTrace, Description description) {
		if (FailTrace.size() > 0) {
			log.info("And finally test passed after " + retryCount + " failures");
			getStep().log(Status.PASS, "And finally test passed after " + testRun + " failures");
		} else {
			log.info("And finally test passed without failures");
			getStep().log(Status.PASS, "And finally test passed without failures");
		}
	}
	
	

	/**
	 * captures screenshot on failure attaches to extent test report
	 * 
	 * @param description
	 */
	public void captureScreenshot(Description description) {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.ENGLISH).format(now);
		String extentReportImage = "./extentReport/screenshots/" + description.getMethodName() + "-" + format1 + ".png";

		try {
			File scrFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
			// now copy the screenshot to desired location using copyFile method
			FileUtils.copyFile(scrFile, new File(extentReportImage));
			step.log(Status.INFO, "Screenshot ",
					MediaEntityBuilder.createScreenCaptureFromPath(extentReportImage).build());
		} catch (Exception e) {
			// System.out.println("Error in the captureAndDisplayScreenShot
			// method: " + e.getMessage());
		}
	}

	/**
	 *  gets the stack trace of the failure
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
}