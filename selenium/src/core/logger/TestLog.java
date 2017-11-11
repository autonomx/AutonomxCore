package core.logger;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.gherkin.model.And;
import com.aventstack.extentreports.gherkin.model.Given;
import com.aventstack.extentreports.gherkin.model.Then;
import com.aventstack.extentreports.gherkin.model.When;

import core.driver.AbstractDriver;

public class TestLog {
	
	public static void Given(String value)
	{
		AbstractDriver.log.get().info("Given " + value);
		AbstractDriver.step.set(AbstractDriver.test.get().createNode(Given.class, value));
	}
	
	public static void When(String value)
	{
		AbstractDriver.log.get().info("When " + value);
		AbstractDriver.step.set(AbstractDriver.test.get().createNode(When.class, value));
	}
	
	public static void And(String value)
	{
		AbstractDriver.log.get().info("And " + value);
		AbstractDriver.step.set(AbstractDriver.test.get().createNode(And.class, value));
	}
	
	public static void Then(String value)
	{
		AbstractDriver.log.get().info("Then " + value);
		AbstractDriver.step.set(AbstractDriver.test.get().createNode(Then.class, value));
	}

	public static void logPass(String value) {
		AbstractDriver.log.get().info(value);
		AbstractDriver.step.get().pass(value);
	}

	public static void logFail(String value) {
		AbstractDriver.log.get().error(value);
		AbstractDriver.step.get().fail(value);
	}

	public static void logWarning(String value) {
		AbstractDriver.log.get().info(value);
		AbstractDriver.step.get().warning(value);
	}
	
	/**
	 * removes handler for java.util.logging
	 * removes logs from third party jars such as webdriver
	 */
	public static void removeLogUtilHandler() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	} 

	public static void captureScreenshot() {
		String extentReportImage = "./extentReport/screenshots/" + System.currentTimeMillis() + ".png";
		File scrFile = ((TakesScreenshot) AbstractDriver.getWebDriver()).getScreenshotAs(OutputType.FILE);
		// Now you can do whatever you need to do with it, for example copy
		// somewhere
		try {
			// now copy the screenshot to desired location using copyFile method
			FileUtils.copyFile(scrFile, new File(extentReportImage));
			AbstractDriver.test.get().log(Status.INFO, "Screenshot from : " + extentReportImage,
					MediaEntityBuilder.createScreenCaptureFromPath(extentReportImage).build());
		} catch (IOException e) {
			System.out.println("Error in the captureAndDisplayScreenShot method: " + e.getMessage());
		}
	}
}