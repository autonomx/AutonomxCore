package core.support.rules;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import core.support.logger.ExtentManager;
import core.support.objects.DriverObject;

public class JunitTestListener extends RunListener {
	@Override
	public void testRunStarted(Description description) throws Exception {
		// Called before any tests have been run.
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		DriverObject.quitAllDrivers();
		ExtentManager.printReportLink();
		// ExtentManager.launchReportAfterTest();
	}
}