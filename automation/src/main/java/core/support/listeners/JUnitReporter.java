package core.support.listeners;

import org.testng.ITestResult;
import org.testng.reporters.JUnitReportReporter;

public class JUnitReporter extends JUnitReportReporter {
	
	@Override
	protected String getTestName(ITestResult tr) {
		String name = tr.getName();
	    return name;
	}
}
