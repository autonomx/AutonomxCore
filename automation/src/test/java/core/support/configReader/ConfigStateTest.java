package core.support.configReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

public class ConfigStateTest {

	private static final String TEST_ID = "ConfigStateTest-unit";

	@BeforeMethod
	public void setUpConfigContext() {
		TestObject.currentTestId.set(TEST_ID);
		TestObject.testInfo.put(TEST_ID, new TestObject().withTestId(TEST_ID));
		TestObject.testInfo.put(TestObject.DEFAULT_TEST, new TestObject().withTestId(TestObject.DEFAULT_TEST));
	}

	@AfterMethod
	public void cleanUpConfigContext() {
		TestObject.currentTestId.remove();
		TestObject.currentTestName.remove();
		TestObject.testInfo.clear();
	}

	@Test
	public void putAndGetValueRoundTripThroughCurrentTestContext() {
		Config.putValue("service.url", "https://example.test", false);

		assertEquals(Config.getValue("service.url"), "https://example.test");
		assertEquals(TestObject.getTestInfo().config.get("service.url"), "https://example.test");
	}

	@Test
	public void nullValuesAreStoredAsLiteralNullString() {
		Config.putValue("nullable", null, false);

		assertEquals(Config.getValue("nullable"), "null");
	}

	@Test
	public void typedAccessorsConvertStoredStringValues() {
		Config.putValue("integer", "42", false);
		Config.putValue("decimal", "12.5", false);
		Config.putValue("enabled", "true", false);

		assertEquals(Config.getIntValue("integer"), 42);
		assertEquals(Config.getDoubleValue("decimal"), 12.5d);
		assertTrue(Config.getBooleanValue("enabled"));
	}

	@Test
	public void typedAccessorsReturnDocumentedDefaultsForMissingValues() {
		assertEquals(Config.getIntValue("missing-int"), -1);
		assertEquals(Config.getDoubleValue("missing-double"), -1d);
		assertFalse(Config.getBooleanValue("missing-boolean"));
	}

	@Test
	public void listAccessorSplitsAndTrimsCommaSeparatedValues() {
		Config.putValue("regions", " west,central , east ", false);

		ArrayList<String> regions = Config.getValueList("regions");
		assertEquals(regions.size(), 3);
		assertEquals(regions.get(0), "west");
		assertEquals(regions.get(1), "central");
		assertEquals(regions.get(2), "east");
	}

	@Test
	public void objectAccessorPreservesObjectIdentity() {
		Object marker = new Object();
		Config.putValue("marker", marker, false);

		assertSame(Config.getObjectValue("marker"), marker);
		assertNull(Config.getObjectValue("missing-marker"));
	}

	@Test
	public void missingValueIsTrackedOnceRequested() {
		assertEquals(Config.getValue("not.present"), "");
		assertTrue(TestObject.getTestInfo().missingConfigVars.contains("not.present"));
	}

	@Test
	public void globalAccessorsReadDefaultTestContextInsteadOfCurrentTest() {
		TestObject.getGlobalTestInfo().config.put("global.flag", "true");
		TestObject.getGlobalTestInfo().config.put("global.count", "7");
		TestObject.getGlobalTestInfo().config.put("global.object", Integer.valueOf(99));

		assertTrue(Config.getGlobalBooleanValue("global.flag"));
		assertEquals(Config.getGlobalIntValue("global.count"), 7);
		assertEquals(Config.getGlobalValue("global.count"), "7");
		assertEquals(Config.getGlobalObjectValue("global.object"), Integer.valueOf(99));
	}

	@Test
	public void parentValueReadsFromServiceParentContext() {
		String parentId = "ConfigStateTest-parent";
		TestObject parent = new TestObject().withTestId(parentId);
		parent.config.put("feature.enabled", Boolean.TRUE);
		TestObject.testInfo.put(parentId, parent);
		TestObject.getTestInfo().serviceObject.withParent(parentId);

		assertTrue(Config.getParentValue("feature.enabled"));
		assertFalse(Config.getParentValue("missing.feature"));
	}

	@Test
	public void crossPlatformPropertiesDelegateToTypedConfigValues() {
		Config.putValue("global.parallelTestCount", "6", false);
		Config.putValue("global.parallelTestType", "methods", false);
		Config.putValue("global.timeoutSeconds", "45", false);
		Config.putValue("global.timeout.implicit.Seconds", "4", false);
		Config.putValue("report.audioCommentary", "true", false);
		Config.putValue("report.audioCommentaryType", "brief", false);
		Config.putValue("environment.path", "/tmp/environment", false);
		Config.putValue("report.enableBatchLogging", "true", false);
		Config.putValue("localize.file", "messages.csv", false);
		Config.putValue("language", "en", false);
		Config.putValue("global.isSingleSignIn", "true", false);

		assertEquals(CrossPlatformProperties.getParallelTests(), 6);
		assertEquals(CrossPlatformProperties.getParallelTestType(), "methods");
		assertEquals(CrossPlatformProperties.getGlobalTimeout(), 45);
		assertEquals(CrossPlatformProperties.getGlobalTimeoutImplicitWait(), 4);
		assertTrue(CrossPlatformProperties.getAudioCommentary());
		assertEquals(CrossPlatformProperties.getAudioCommentaryType(), "brief");
		assertEquals(CrossPlatformProperties.getPath(), "/tmp/environment");
		assertTrue(CrossPlatformProperties.getEnableBatchLogging());
		assertEquals(CrossPlatformProperties.getLocalizationFile(), "messages.csv");
		assertEquals(CrossPlatformProperties.getLanguage(), "en");
		assertTrue(CrossPlatformProperties.isSingleSignIn());
	}

	@Test
	public void simpleDriverTypeNormalizesWebVariantsAndOtherValues() {
		CrossPlatformProperties properties = new CrossPlatformProperties();

		assertEquals(properties.getSimpleDriverType("LOCAL_WEBDRIVER"), "web");
		assertEquals(properties.getSimpleDriverType("ANDROID"), "android");
	}
}
