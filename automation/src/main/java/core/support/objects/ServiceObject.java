package core.support.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import core.helpers.Helper;
import core.support.logger.TestLog;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ServiceObject {

	private String TestSuite = StringUtils.EMPTY;
	private String TestCaseID = StringUtils.EMPTY;
	private String RunFlag = StringUtils.EMPTY;
	private String Description = StringUtils.EMPTY;
	private String InterfaceType = StringUtils.EMPTY;
	private String UriPath = StringUtils.EMPTY;
	private String ContentType = StringUtils.EMPTY;
	private String Method = StringUtils.EMPTY;
	private String Option = StringUtils.EMPTY;
	private String RequestHeaders = StringUtils.EMPTY;
	private String TemplateFile = StringUtils.EMPTY;
	private String RequestBody = StringUtils.EMPTY;
	private String OutputParams = StringUtils.EMPTY;
	private String RespCodeExp = StringUtils.EMPTY;
	private String ExpectedResponse = StringUtils.EMPTY;
	private String TcComments = StringUtils.EMPTY;
	private String tcName = StringUtils.EMPTY;
	private String tcIndex = StringUtils.EMPTY; // format index:testCount eg. 1:6
	private String testType = StringUtils.EMPTY;
	private Object serviceSteps = null;
	private String parent = StringUtils.EMPTY; // name of the parent object to inherit from
	private Response response = null; // rest api response
	private RequestSpecification request = null; // rest api request
	private List<String> errorMessages = new ArrayList<String>();
	private Map<String, List<String>> headerMap = new HashMap<String, List<String>>();

	public ServiceObject setServiceObject(String TestSuite, String TestCaseID, String RunFlag, String Description,
			String InterfaceType, String UriPath, String ContentType, String Method, String Option,
			String RequestHeaders, String TemplateFile, String RequestBody, String OutputParams, String RespCodeExp,
			String ExpectedResponse, String TcComments, String tcName, String tcIndex, String testType,
			Object serviceSteps) {
		this.TestSuite = TestSuite;
		this.TestCaseID = TestCaseID;
		this.RunFlag = RunFlag;
		this.Description = Description;
		this.InterfaceType = InterfaceType;
		this.UriPath = UriPath;
		this.ContentType = ContentType;
		this.Method = Method;
		this.Option = Option;
		this.RequestHeaders = RequestHeaders;
		this.TemplateFile = TemplateFile;
		this.RequestBody = RequestBody;
		this.OutputParams = OutputParams;
		this.RespCodeExp = RespCodeExp;
		this.ExpectedResponse = ExpectedResponse;
		this.TcComments = TcComments;
		this.tcName = tcName;
		this.tcIndex = tcIndex;
		this.testType = testType;
		this.serviceSteps = serviceSteps;

		return this;
	}

	public ServiceObject setServiceObject(Object[] testData) {
		

		List<String> header = getMatchingHeader(testData);
		
			
		this.TestSuite = getArrayValue(testData, header.indexOf("TestSuite"));
		this.TestCaseID = getArrayValue(testData, header.indexOf("TestCaseID"));
		this.RunFlag = getArrayValue(testData, header.indexOf("RunFlag"));
		this.Description = getArrayValue(testData, header.indexOf("Description"));
		this.InterfaceType = getArrayValue(testData, header.indexOf("InterfaceType"));
		this.UriPath = getArrayValue(testData, header.indexOf("UriPath"));
		this.ContentType = getArrayValue(testData, header.indexOf("ContentType"));
		this.Method = getArrayValue(testData, header.indexOf("Method"));
		this.Option = getArrayValue(testData, header.indexOf("Option"));
		this.RequestHeaders = getArrayValue(testData, header.indexOf("RequestHeaders"));
		this.TemplateFile = getArrayValue(testData, header.indexOf("TemplateFile"));
		this.RequestBody = getArrayValue(testData, header.indexOf("RequestBody"));
		this.OutputParams = getArrayValue(testData, header.indexOf("OutputParams"));
		this.RespCodeExp = getArrayValue(testData, header.indexOf("RespCodeExp"));
		this.ExpectedResponse = getArrayValue(testData, header.indexOf("ExpectedResponse"));
		this.TcComments = getArrayValue(testData, header.indexOf("TcComments"));
		this.tcName = getArrayValue(testData, 16);
		this.tcIndex = getArrayValue(testData, 17);
		this.testType = getArrayValue(testData, 18);
		this.serviceSteps = getObjectValue(testData, 19);

		return this;
	}
	
	private static List<String> getMatchingHeader(Object[] testData) {
		Map<String, List<String>> headerMap = TestObject.getGlobalTestInfo().serviceObject.getHeaderMap();
		List<String> header = new ArrayList<String>();
		
		for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
		    String key = entry.getKey();
		    if(key.isEmpty()) continue;
		    
		    List<String> value = entry.getValue();
		    int suiteIndex = value.indexOf("TestSuite");
		    String testDataSuite = getArrayValue(testData, suiteIndex);
		    if(testDataSuite.equals(key)) {
		    	header = value;
		    	break;
		    }
		}
		
		if(header.isEmpty())
			Helper.assertFalse("did not find matching test suite name");
		
		return header;
	}

	private static String getArrayValue(Object[] testData, int index) {
		if (index >= testData.length)
			return StringUtils.EMPTY;

		String value = testData[index].toString();

		if (StringUtils.isBlank(value))
			return StringUtils.EMPTY;
		return value;
	}

	private Object getObjectValue(Object[] testData, int index) {
		if (index >= testData.length)
			return StringUtils.EMPTY;

		Object value = testData[index];

		if (StringUtils.isBlank(value.toString()))
			return StringUtils.EMPTY;
		return value;
	}

// getters setters
//-----------------------------------------------------------------------------------------------------------------------	
	public ServiceObject withTestSuite(String testSuite) {
		this.TestSuite = testSuite;
		return this;
	}

	public String getTestSuite() {
		return this.TestSuite;
	}

	public ServiceObject withTestCaseID(String testCaseID) {
		this.TestCaseID = testCaseID;
		return this;
	}

	public String getTestCaseID() {
		return this.TestCaseID.trim();
	}

	public ServiceObject withRunFlag(String runFlag) {
		this.RunFlag = runFlag;
		return this;
	}

	public String getRunFlag() {
		return this.RunFlag.trim();
	}

	public ServiceObject withDescription(String Description) {
		this.Description = Description;
		return this;
	}

	public String getDescription() {
		return this.Description;
	}

	public ServiceObject withInterfaceType(String InterfaceType) {
		this.InterfaceType = InterfaceType;
		return this;
	}

	public String getInterfaceType() {
		return this.InterfaceType.trim();
	}

	public ServiceObject withUriPath(String UriPath) {
		this.UriPath = UriPath;
		return this;
	}

	public String getUriPath() {
		return this.UriPath.trim();
	}

	public ServiceObject withContentType(String ContentType) {
		this.ContentType = ContentType;
		return this;
	}

	public String getContentType() {
		return this.ContentType.trim();
	}

	public ServiceObject withMethod(String Method) {
		this.Method = Method;
		return this;
	}

	public String getMethod() {
		return normalize(this.Method);
	}

	public ServiceObject withOption(String Option) {
		this.Option = Option;
		return this;
	}

	public ServiceObject withParent(String parent) {
		this.parent = parent;
		return this;
	}

	public String getParent() {
		if (StringUtils.isBlank(this.parent))
			return TestObject.DEFAULT_TEST;
		return this.parent;
	}

	public String getOption() {
		return this.Option.trim();
	}

	public ServiceObject withRequestHeaders(String RequestHeaders) {
		this.RequestHeaders = RequestHeaders;
		return this;
	}

	public String getRequestHeaders() {
		return normalize(this.RequestHeaders);
	}

	public ServiceObject withTemplateFile(String TemplateFile) {
		this.TemplateFile = TemplateFile;
		return this;
	}

	public String getTemplateFile() {
		return this.TemplateFile.trim();
	}

	public ServiceObject withRequestBody(String RequestBody) {
		this.RequestBody = RequestBody;
		return this;
	}

	public ServiceObject withResponse(Response response) {
		this.response = response;
		return this;
	}

	public ServiceObject withRequest(RequestSpecification request) {
		this.request = request;
		return this;
	}

	public ServiceObject withErrorMessages(List<String> errorMessages) {
		this.errorMessages = errorMessages;
		return this;
	}

	public ServiceObject withOutputParams(String OutputParams) {
		this.OutputParams = OutputParams;
		return this;
	}

	public String getOutputParams() {
		return normalize(this.OutputParams);
	}

	public ServiceObject withRespCodeExp(String RespCodeExp) {
		this.RespCodeExp = RespCodeExp;
		return this;
	}

	public String getRespCodeExp() {
		return this.RespCodeExp.trim();
	}

	public ServiceObject withExpectedResponse(String ExpectedResponse) {
		this.ExpectedResponse = ExpectedResponse;
		return this;
	}

	public String getExpectedResponse() {
		return this.ExpectedResponse;
	}

	public ServiceObject withTcComments(String TcComments) {
		this.TcComments = TcComments;
		return this;
	}

	public String getTcComments() {
		return this.TcComments;
	}

	public ServiceObject withTcName(String tcName) {
		this.tcName = tcName.trim();
		return this;
	}

	public Response getResponse() {
		return this.response;
	}

	public RequestSpecification getRequest() {
		return this.request;
	}

	public List<String> getErrorMessages() {
		return this.errorMessages;
	}

	public String getRequestBody() {
		return normalize(this.RequestBody);
	}

	public String getTcName() {
		return this.tcName.trim();
	}

	public String getTcType() {
		return this.testType;
	}
	
	public ServiceObject withHeaderMap(String testcaseId, ArrayList<String> header) {
		this.headerMap.put(testcaseId, header);
		return this;
	}

	public Map<String, List<String>> getHeaderMap() {
		return this.headerMap;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, List<Object>> getServiceSteps() {
		if(this.serviceSteps == null) return null;
		if(this.serviceSteps.toString().isEmpty()) return null;
		
		HashMap<String, List<Object>> map = (HashMap<String, List<Object>>) this.serviceSteps;
		if (map != null && map.size() > 0) {
			return map;
		}
		return null;
	}

	public ServiceObject withTcIndex(String tcIndex) {
		this.tcIndex = tcIndex;
		return this;
	}

	public String getTcIndex() {
		return this.tcIndex.split(":")[0];
	}

	public String getTcCount() {
		return this.tcIndex.split(":")[1];
	}

public static String normalizeLog(String value) {
		
		value = TestLog.setMaxLength(value);
		
		// remove new lines. very slow operation
		value = value.replaceAll("\\R+", " ");

		// reduces spaces to single space. eg. " " to " "
		value = value.trim().replaceAll(" +", " ");
		return value.replaceAll("[\\u2018\\u2019]", "'").replaceAll("[\\u201C\\u201D]", "\"");
	}

	public static String normalize(String value) {

		// reduces spaces to single space. eg. " " to " "
		value = value.trim().replaceAll(" +", " ");
		return value.replaceAll("[\\u2018\\u2019]", "'").replaceAll("[\\u201C\\u201D]", "\"");
	}
}