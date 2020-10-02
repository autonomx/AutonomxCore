package core.support.listeners;

import java.util.Iterator;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

import core.helpers.Helper;

public class TestResultListener extends TestListenerAdapter {
   
    
    @Override
    public void onConfigurationFailure(ITestResult result) {
    	Reporter.setCurrentTestResult(result);
    	
    	// print stack trace
    	Helper.page.printStackTrace(result.getThrowable());
    }
    
    @Override
    public void onTestFailure(ITestResult tr) {
    	
    	// print stack trace
    	Helper.page.printStackTrace(tr.getThrowable());
    }
    
    @Override
    public void onTestSkipped(ITestResult tr) {
    	
    	// print stack trace
    	Helper.page.printStackTrace(tr.getThrowable());
    }
    

    @Override
    public void onFinish(ITestContext context) {
      Iterator<ITestResult> failedTestCases = context.getSkippedTests().getAllResults().iterator();
    
      
      while (failedTestCases.hasNext()) {
         ITestResult failedTestCase = failedTestCases.next();
         ITestNGMethod method = failedTestCase.getMethod();
         //String testname = failedTestCase.getTestClass().getRealClass().getSimpleName() + "." + method.getMethodName();
         if (context.getFailedTests().getResults(method).size() > 1) {
             failedTestCases.remove();
         } else {
             if (context.getPassedTests().getResults(method).size() > 0) {
                 failedTestCases.remove();
             }
         }
     }
    }
}
