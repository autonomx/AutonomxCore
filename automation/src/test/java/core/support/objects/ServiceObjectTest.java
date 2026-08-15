package core.support.objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.Test;

public class ServiceObjectTest {

	@Test
	public void normalizationCollapsesWhitespaceAndNormalizesSmartQuotes() {
		String value = "  POST   ‘quoted’   “double”  ";

		assertEquals(ServiceObject.normalize(value), "POST 'quoted' \"double\"");
	}

	@Test
	public void builderGettersTrimAndNormalizeTransportFields() {
		ServiceObject service = new ServiceObject()
				.withTestCaseID("  TC-1  ")
				.withRunFlag(" Y ")
				.withInterfaceType(" REST ")
				.withUriPath(" /users/1 ")
				.withContentType(" application/json ")
				.withMethod("  POST   ")
				.withRequestHeaders("  Authorization:   Bearer token  ")
				.withOutputParams("  user.id   user.name ")
				.withRespCodeExp(" 201 ")
				.withTcName(" create user ");

		assertEquals(service.getTestCaseID(), "TC-1");
		assertEquals(service.getRunFlag(), "Y");
		assertEquals(service.getInterfaceType(), "REST");
		assertEquals(service.getUriPath(), "/users/1");
		assertEquals(service.getContentType(), "application/json");
		assertEquals(service.getMethod(), "POST");
		assertEquals(service.getRequestHeaders(), "Authorization: Bearer token");
		assertEquals(service.getOutputParams(), "user.id user.name");
		assertEquals(service.getRespCodeExp(), "201");
		assertEquals(service.getTcName(), "create user");
	}

	@Test
	public void parentDefaultsToFrameworkDefaultAndCanBeOverridden() {
		ServiceObject service = new ServiceObject();

		assertEquals(service.getParent(), TestObject.DEFAULT_TEST);
		assertSame(service.withParent("parent-test"), service);
		assertEquals(service.getParent(), "parent-test");
	}

	@Test
	public void tcIndexExposesCurrentIndexAndTotalCount() {
		ServiceObject service = new ServiceObject().withTcIndex("3:8");

		assertEquals(service.getTcIndex(), "3");
		assertEquals(service.getTcCount(), "8");
	}

	@Test
	public void headerMapRetainsPerSuiteHeaderOrder() {
		ArrayList<String> header = new ArrayList<String>(Arrays.asList("TestSuite", "TestCaseID", "Method"));
		ServiceObject service = new ServiceObject();

		assertSame(service.withHeaderMap("users", header), service);
		assertSame(service.getHeaderMap().get("users"), header);
	}

	@Test
	public void explicitServiceObjectMappingRetainsAllCoreFieldsAndSteps() {
		HashMap<String, List<Object>> steps = new HashMap<String, List<Object>>();
		steps.put("validate", Arrays.<Object>asList("status", 200));
		ServiceObject service = new ServiceObject().setServiceObject(
				"suite",
				"TC-2",
				"Y",
				"create user",
				"REST",
				"/users",
				"application/json",
				"POST",
				"option",
				"Authorization: Bearer token",
				"user.json",
				"{ \"name\": \"Alice\" }",
				"id",
				"201",
				"created",
				"comment",
				"create-user",
				"2:4",
				"positive",
				steps);

		assertEquals(service.getTestSuite(), "suite");
		assertEquals(service.getTestCaseID(), "TC-2");
		assertEquals(service.getRunFlag(), "Y");
		assertEquals(service.getDescription(), "create user");
		assertEquals(service.getInterfaceType(), "REST");
		assertEquals(service.getUriPath(), "/users");
		assertEquals(service.getContentType(), "application/json");
		assertEquals(service.getMethod(), "POST");
		assertEquals(service.getOption(), "option");
		assertEquals(service.getRequestHeaders(), "Authorization: Bearer token");
		assertEquals(service.getTemplateFile(), "user.json");
		assertEquals(service.getRequestBody(), "{ \"name\": \"Alice\" }");
		assertEquals(service.getOutputParams(), "id");
		assertEquals(service.getRespCodeExp(), "201");
		assertEquals(service.getExpectedResponse(), "created");
		assertEquals(service.getTcComments(), "comment");
		assertEquals(service.getTcName(), "create-user");
		assertEquals(service.getTcIndex(), "2");
		assertEquals(service.getTcCount(), "4");
		assertEquals(service.getTcType(), "positive");
		assertSame(service.getServiceSteps(), steps);
	}

	@Test
	public void requestResponseAndErrorsDefaultToEmptyState() {
		ServiceObject service = new ServiceObject();

		assertNull(service.getRequest());
		assertNull(service.getResponse());
		assertEquals(service.getErrorMessages().size(), 0);
		assertNull(service.getServiceSteps());
	}
}
