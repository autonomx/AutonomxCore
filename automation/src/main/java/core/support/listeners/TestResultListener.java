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
         if (context.getFailedTests().getResults(method).size() > 1) {
             System.out.println("skipped test case remove from report:" + failedTestCase.getTestClass().toString());
             failedTestCases.remove();
         } else {
             if (context.getPassedTests().getResults(method).size() > 0) {
                 System.out.println("skipped retries removed from passing test:" + failedTestCase.getTestClass().toString());
                 failedTestCases.remove();
             }
         }
     }
    }
}
