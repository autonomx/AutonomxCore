package core.support.listeners;

import org.testng.ITestResult;
import org.testng.reporters.JUnitReportReporter;

import core.helpers.Helper;
import core.support.objects.TestObject;

public class JUnitReporter extends JUnitReportReporter {
	
	
	@Override
	protected String getTestName(ITestResult tr) {
		String name = tr.getName();
		System.out.println(name);
	    return name;
	}
	
	protected String getTestName2(ITestResult tr) {
		// Get the Parameters
		Object[] params = tr.getParameters();

		// Add the method name to the string
		StringBuilder sb = new StringBuilder();
		sb.append(tr.getMethod().getMethodName() + " (");

		// Add the parameters to the String
		String result;
		if (params.length >= 1) {
			for (int i = 0; i < params.length; i++ ) {
				Object param = params[i];

				if (param != null) {
					sb.append(param.toString());
					sb.append(", ");
				}
			}
			// Remove the last ', '
			result = sb.substring(0, sb.length() - 2);
		} else {
			result = sb.toString();
		}
		System.out.println(result + ")");
		return (result + ")");
	}
}
