package org.testng.reporters;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.SetMultiMap;
import org.testng.collections.Sets;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import core.apiCore.driver.ApiTestDriver;
import core.support.listeners.TestListener;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

public class JUnitReportReporter implements IReporter {
	
  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String defaultOutputDirectory) {

    Map<Class<?>, Set<ITestResult>> results = Maps.newHashMap();
    ListMultiMap<Object, ITestResult> befores = Maps.newListMultiMap();
    ListMultiMap<Object, ITestResult> afters = Maps.newListMultiMap();
    SetMultiMap<Class<?>, ITestNGMethod> mapping = new SetMultiMap<>(false);
    for (ISuite suite : suites) {
    	
      // get failed rerun passed test list
      getFailedRerunPassedTests(suite);
     
      Map<String, ISuiteResult> suiteResults = suite.getResults();
      addMapping(mapping, suite.getExcludedMethods());
      for (ISuiteResult sr : suiteResults.values()) {
        ITestContext tc = sr.getTestContext();
        addResults(tc.getPassedTests().getAllResults(), results);
        addResults(tc.getFailedTests().getAllResults(), results);
        addResults(tc.getSkippedTests().getAllResults(), results);
        addResults(tc.getFailedConfigurations().getAllResults(), results);
        for (ITestResult tr : tc.getPassedConfigurations().getAllResults()) {
          if (tr.getMethod().isBeforeMethodConfiguration()) {
            befores.put(tr.getInstance(), tr);
          }
          if (tr.getMethod().isAfterMethodConfiguration()) {
            afters.put(tr.getInstance(), tr);
          }
        }
      }
    }

    // TODO: set timestamp
    for (Map.Entry<Class<?>, Set<ITestResult>> entry : results.entrySet()) {
      Class<?> cls = entry.getKey();
      Properties p1 = new Properties();
      p1.setProperty(XMLConstants.ATTR_NAME, cls.getName());
      p1.setProperty(XMLConstants.ATTR_TIMESTAMP, StringUtils.EMPTY);

      List<TestTag> testCases = Lists.newArrayList();
      int failures = 0;
      int errors = 0;
      int skipped = 0;
      int testCount = 0;
      float totalTime = 0;

      Collection<ITestResult> iTestResults = sort(entry.getValue());

      for (ITestResult tr : iTestResults) {

        long time = tr.getEndMillis() - tr.getStartMillis();

        time += getNextConfiguration(befores, tr);
        time += getNextConfiguration(afters, tr);

        Throwable t = tr.getThrowable();
        switch (tr.getStatus()) {
          case ITestResult.SKIP:
          case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
            skipped++;
            break;

          case ITestResult.FAILURE:
            if (t instanceof AssertionError) {
              failures++;
            } else {
              errors++;
            }
            break;
        }

        totalTime += time;
        testCount++;
        TestTag testTag = createTestTagFor(tr, cls);
        testTag.properties.setProperty(XMLConstants.ATTR_TIME, "" + formatTime(time));
        testCases.add(testTag);
      }
      int ignored = getDisabledTestCount(mapping.get(entry.getKey()));

      for (ITestNGMethod eachMethod : mapping.get(entry.getKey())) {
        testCases.add(createIgnoredTestTagFor(eachMethod));
      }

      p1.setProperty(XMLConstants.ATTR_FAILURES, Integer.toString(failures));
      p1.setProperty(XMLConstants.ATTR_ERRORS, Integer.toString(errors));
      p1.setProperty(XMLConstants.SKIPPED, Integer.toString(skipped + ignored));
      p1.setProperty(XMLConstants.ATTR_NAME, cls.getName());
      p1.setProperty(XMLConstants.ATTR_TESTS, Integer.toString(testCount + ignored));
      p1.setProperty(XMLConstants.ATTR_TIME, "" + formatTime(totalTime));
      try {
        p1.setProperty(XMLConstants.ATTR_HOSTNAME, InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException e) {
        // ignore
      }

      //
      // Now that we have all the information we need, generate the file
      //
      XMLStringBuffer xsb = new XMLStringBuffer();
      xsb.addComment("Generated by " + getClass().getName());

      xsb.push(XMLConstants.TESTSUITE, p1);
      for (TestTag testTag : testCases) {
        if (putElement(xsb, XMLConstants.TESTCASE, testTag.properties, testTag.childTag != null)) {
          Properties p = new Properties();
          safeSetProperty(p, XMLConstants.ATTR_MESSAGE, testTag.message);
          safeSetProperty(p, XMLConstants.ATTR_TYPE, testTag.type);

          if (putElement(xsb, testTag.childTag, p, testTag.stackTrace != null)) {
            xsb.addCDATA(testTag.stackTrace);
            xsb.pop(testTag.childTag);
          }
          xsb.pop(XMLConstants.TESTCASE);
        }
        if (putElement(xsb, XMLConstants.SYSTEM_OUT, new Properties(), testTag.sysOut != null)) {
          xsb.addString(testTag.sysOut);
          xsb.pop(XMLConstants.SYSTEM_OUT);
        }
      }
      xsb.pop(XMLConstants.TESTSUITE);

      String outputDirectory = defaultOutputDirectory + File.separator + "junitreports";
      Utils.writeUtf8File(outputDirectory, getFileName(cls), xsb.toXML());
    }
  }
  
    /**
     * get list of qualified test names for tests passed in rerun failed test suite
     * @param suite
     */
	private static void getFailedRerunPassedTests(ISuite suite) {

		if(!TestObject.SUITE_NAME.equals(TestListener.FAILED_RERUN_SUITE_NAME))
			return;
		
		// applicable to UI tests only
		if(!ApiTestDriver.isRunningUITest())
			return;
		
		String testname = StringUtils.EMPTY;
		String classname = StringUtils.EMPTY;
		String testId = StringUtils.EMPTY;

		// only run if suite is failed rerun suite
		if (!suite.getName().equals(TestListener.FAILED_RERUN_SUITE_NAME))
			return;

		Map<String, ISuiteResult> suiteResults = suite.getResults();

		for (ISuiteResult sr : suiteResults.values()) {
			ITestContext tc = sr.getTestContext();
			for (ITestResult tr : tc.getPassedTests().getAllResults()) {
				testname = tr.getMethod().getMethodName();
				classname = tr.getTestClass().getRealClass().getSimpleName();
				testId = classname + "-" + testname;
				TestListener.FAILED_RERUN_SUITE_PASSED_TESTS.add(testId);
			}
		}
	}

  private static Collection<ITestResult> sort(Set<ITestResult> results) {
    List<ITestResult> sortedResults = new ArrayList<>(results);
    sortedResults.sort(Comparator.comparingInt(o -> o.getMethod().getPriority()));
    return Collections.unmodifiableList(sortedResults);
  }

  private static int getDisabledTestCount(Set<ITestNGMethod> methods) {
    int count = 0;
    for (ITestNGMethod method : methods) {
      if (!method.getEnabled()) {
        count = count + 1;
      }
    }
    return count;
  }

  private TestTag createIgnoredTestTagFor(ITestNGMethod method) {
    TestTag testTag = new TestTag();
    Properties p2 = new Properties();
    p2.setProperty(XMLConstants.ATTR_CLASSNAME, method.getRealClass().getName());
    p2.setProperty(XMLConstants.ATTR_NAME, method.getMethodName());
    testTag.childTag = XMLConstants.SKIPPED;
    testTag.properties = p2;
    return testTag;
  }

  private TestTag createTestTagFor(ITestResult tr, Class<?> cls) {
    TestTag testTag = new TestTag();

    Properties p2 = new Properties();
    p2.setProperty(XMLConstants.ATTR_CLASSNAME, cls.getName());
    p2.setProperty(XMLConstants.ATTR_NAME, getTestName(tr));
    int status = tr.getStatus();
    if (status == ITestResult.SKIP || status == ITestResult.SUCCESS_PERCENTAGE_FAILURE) {
      testTag.childTag = XMLConstants.SKIPPED;
    } else if (status == ITestResult.FAILURE) {
      handleFailure(testTag, tr.getThrowable());
    }
    List<String> output = Reporter.getOutput(tr);
    if (!output.isEmpty()) {
      testTag.sysOut = String.join(" ", output);
    }
    testTag.properties = p2;
    return testTag;
  }

  private static void handleFailure(TestTag testTag, Throwable t) {
    testTag.childTag = t instanceof AssertionError ? XMLConstants.FAILURE : XMLConstants.ERROR;
    if (t != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      testTag.message = t.getMessage();
      testTag.type = t.getClass().getName();
      testTag.stackTrace = sw.toString();
    }
  }

  /** Put a XML start or empty tag to the XMLStringBuffer depending on hasChildElements parameter */
  private boolean putElement(
      XMLStringBuffer xsb, String tagName, Properties attributes, boolean hasChildElements) {
    if (hasChildElements) {
      xsb.push(tagName, attributes);
    } else {
      xsb.addEmptyElement(tagName, attributes);
    }
    return hasChildElements;
  }

  /** Set property if value is non-null */
  private void safeSetProperty(Properties p, String key, String value) {
    if (value != null) {
      p.setProperty(key, value);
    }
  }

  /**
   * Add the time of the configuration method to this test method.
   *
   * <p>The only problem with this method is that the timing of a test method might not be added to
   * the time of the same configuration method that ran before it but since they should all be
   * equivalent, this should never be an issue.
   */
  private long getNextConfiguration(
      ListMultiMap<Object, ITestResult> configurations, ITestResult tr) {
    long result = 0;

    List<ITestResult> confResults = configurations.get(tr.getInstance());
    Map<ITestNGMethod, ITestResult> seen = Maps.newHashMap();
    for (ITestResult r : confResults) {
      if (!seen.containsKey(r.getMethod())) {
        result += r.getEndMillis() - r.getStartMillis();
        seen.put(r.getMethod(), r);
      }
    }
    confResults.removeAll(seen.values());

    return result;
  }

  protected String getFileName(Class<?> cls) {
    return "TEST-" + cls.getName() + ".xml";
  }

  protected String getTestName(ITestResult tr) {
	  ServiceObject service = null;
		String name = tr.getMethod().getMethodName();

		// check if service object for the last test is a service test
		if (TestObject.isValidTestId(name))
			service = TestObject.getTestInfo(name).serviceObject;

		if (service != null)
			name = tr.getName();
		return name;
  }

  private String formatTime(float time) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    // JUnitReports wants points here, regardless of the locale
    symbols.setDecimalSeparator('.');
    DecimalFormat format = new DecimalFormat("#.###", symbols);
    format.setMinimumFractionDigits(3);
    return format.format(time / 1000.0f);
  }

  private static class TestTag {
    Properties properties;
    String message;
    String type;
    String stackTrace;
    String childTag;
    String sysOut;
  }
  
  /**
   * set test method name format: class-method
   * service tests are already formatted
   * @param allResults
   * @param out
   */
	private void addResults(Set<ITestResult> allResults, Map<Class<?>, Set<ITestResult>> out) {
		for (ITestResult tr : allResults) {

			String testname = tr.getMethod().getMethodName();
			String classname = tr.getTestClass().getRealClass().getSimpleName();
			String testId = classname + "-" + testname;
			
			// if failed test passed in rerun, set to pass
			for(String name : TestListener.FAILED_RERUN_SUITE_PASSED_TESTS) {
				if(testId.equals(name))
					tr.setStatus(1);
			}
			
			// service tests method names are already formatted 
			if(!testname.contains("-")) {			
				try {
					Field methodName = org.testng.internal.BaseTestMethod.class.getDeclaredField("m_methodName");
					methodName.setAccessible(true);
					methodName.set(tr.getMethod(), classname + "-" + testname);
				} catch (Exception e) {
					e.getMessage();
				}
			}

			Class<?> cls = tr.getMethod().getTestClass().getRealClass();
			Set<ITestResult> l = out.computeIfAbsent(cls, k -> Sets.newHashSet());
			l.add(tr);
		}
	}

//  private void addResults(Set<ITestResult> allResults, Map<Class<?>, Set<ITestResult>> out) {
//    for (ITestResult tr : allResults) {
//      Class<?> cls = tr.getMethod().getTestClass().getRealClass();
//      Set<ITestResult> l = out.computeIfAbsent(cls, k -> Sets.newHashSet());
//      l.add(tr);
//    }
//  }

  private void addMapping(
      SetMultiMap<Class<?>, ITestNGMethod> mapping, Collection<ITestNGMethod> methods) {
    for (ITestNGMethod method : methods) {
      if (!method.getEnabled()) {
        mapping.put(method.getRealClass(), method);
      }
    }
  }
}
