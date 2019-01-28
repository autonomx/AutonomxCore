package core.support.objects;

public class ApiObject {

	public String TestSuite;
	public String TestCaseID;
	public String RunFlag;
	public String Description;
	public String InterfaceType;
	public String UriPath;
	public String ContentType;
	public String Method;
	public String Option;
	public String RequestHeaders;
	public String TemplateFile;
	public String RequestBody;
	public String OutputParams;
	public String RespCodeExp;
	public String ExpectedResponse;
	public String PartialExpectedResponse;
	public String NotExpectedResponse;
	public String TcComments;
	public String tcName;
	public String tcIndex;
	public String tcClass;

	public ApiObject setApiObject(String TestSuite, String TestCaseID, String RunFlag, String Description,
			String InterfaceType, String UriPath, String ContentType, String Method, String Option,
			String RequestHeaders, String TemplateFile, String RequestBody, String OutputParams, String RespCodeExp,
			String ExpectedResponse, String PartialExpectedResponse, String NotExpectedResponse, String TcComments,
			String tcName, String tcIndex) {
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
		this.RequestBody = replaceQuotes(RequestBody);
		this.OutputParams = OutputParams;
		this.RespCodeExp = RespCodeExp;
		this.ExpectedResponse = replaceQuotes(ExpectedResponse);
		this.PartialExpectedResponse = replaceQuotes(PartialExpectedResponse);
		this.NotExpectedResponse = replaceQuotes(NotExpectedResponse);
		this.TcComments = TcComments;
		this.tcName = tcName;
		this.tcIndex = tcIndex;

		return this;
	}

	public static String replaceQuotes(String value) {
		return value.replaceAll("[\\u2018\\u2019]", "'").replaceAll("[\\u201C\\u201D]", "\"");
	}
}