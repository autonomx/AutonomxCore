package core.support.listeners;

import org.apache.commons.lang.StringUtils;
import org.testng.ITestResult;
import org.testng.reporters.JUnitReportReporter;
import core.apiCore.driver.ApiTestDriver;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

public class JUnitReporter extends JUnitReportReporter {
	
	@Override
	protected String getTestName(ITestResult tr) {
		ServiceObject service = null;
		String name = tr.getMethod().getMethodName();
		
		// check if service object for the last test is a service test
		if(TestObject.isValidTestId(name))
			service = TestObject.getTestInfo(name).serviceObject;
		
		if(service != null)
		  name = tr.getName();
	    return name;
	}
}
