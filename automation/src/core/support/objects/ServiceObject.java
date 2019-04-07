package core.support.objects;

public class ServiceObject {

	private String TestSuite = "";
	private String TestCaseID = "";
	private String RunFlag = "";
	private String Description = "";
	private String InterfaceType = "";
	private String UriPath = "";
	private String ContentType = "";
	private String Method = "";
	private String Option = "";
	private String RequestHeaders = "";
	private String TemplateFile = "";
	private String RequestBody = "";
	private String OutputParams = "";
	private String RespCodeExp = "";
	private String ExpectedResponse = "";
	private String TcComments = "";
	private String tcName = "";
	private String tcIndex = "";
	private String tcClass = "";

	public ServiceObject setApiObject(String TestSuite, String TestCaseID, String RunFlag, String Description,
			String InterfaceType, String UriPath, String ContentType, String Method, String Option,
			String RequestHeaders, String TemplateFile, String RequestBody, String OutputParams, String RespCodeExp,
			 String PartialExpectedResponse, String TcComments,
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
		this.ExpectedResponse = replaceQuotes(PartialExpectedResponse);
		this.TcComments = TcComments;
		this.tcName = tcName;
		this.tcIndex = tcIndex;

		return this;
	}
	
// getters setters
//-----------------------------------------------------------------------------------------------------------------------	
	public ServiceObject withTestSuite(String testSuite){
		this.TestSuite = testSuite;
		return this;
	}
	
	public String getTestSuite(){
		return this.TestSuite;
	}
	
	public ServiceObject withTestCaseID(String testCaseID){
		this.TestCaseID = testCaseID;
		return this;
	}
	
	public String getTestCaseID(){
		return this.TestCaseID;
	}
	
	public ServiceObject withRunFlag(String runFlag){
		this.RunFlag = runFlag;
		return this;
	}
	
	public String getRunFlag(){
		return this.RunFlag;
	}
	
	public ServiceObject withDescription(String Description){
		this.Description = Description;
		return this;
	}
	
	public String getDescription(){
		return this.Description;
	}
	
	public ServiceObject withInterfaceType(String InterfaceType){
		this.InterfaceType = InterfaceType;
		return this;
	}
	
	public String getInterfaceType(){
		return this.InterfaceType;
	}
	
	public ServiceObject withUriPath(String UriPath){
		this.UriPath = UriPath;
		return this;
	}
	
	public String getUriPath(){
		return this.UriPath;
	}
	
	public ServiceObject withContentType(String ContentType){
		this.ContentType = ContentType;
		return this;
	}
	
	public String getContentType(){
		return this.ContentType;
	}
	
	public ServiceObject withMethod(String Method){
		this.Method = Method;
		return this;
	}
	
	public String getMethod(){
		return this.Method;
	}
	
	public ServiceObject withOption(String Option){
		this.Option = Option;
		return this;
	}
	
	public String getOption(){
		return this.Option;
	}
	
	public ServiceObject withRequestHeaders(String RequestHeaders){
		this.RequestHeaders = RequestHeaders;
		return this;
	}
	
	public String getRequestHeaders(){
		return this.RequestHeaders;
	}
	
	public ServiceObject withTemplateFile(String TemplateFile){
		this.TemplateFile = TemplateFile;
		return this;
	}
	
	public String getTemplateFile(){
		return this.TemplateFile;
	}
	
	public ServiceObject withRequestBody(String RequestBody){
		this.RequestBody = RequestBody;
		return this;
	}
	
	public String getRequestBody(){
		return this.RequestBody;
	}
	
	public ServiceObject withOutputParams(String OutputParams){
		this.OutputParams = OutputParams;
		return this;
	}
	
	public String getOutputParams(){
		return this.OutputParams;
	}
	
	public ServiceObject withRespCodeExp(String RespCodeExp){
		this.RespCodeExp = RespCodeExp;
		return this;
	}
	
	public String getRespCodeExp(){
		return this.RespCodeExp;
	}
	
	public ServiceObject withExpectedResponse(String ExpectedResponse){
		this.ExpectedResponse = ExpectedResponse;
		return this;
	}
	
	public String getExpectedResponse(){
		return this.ExpectedResponse;
	}
	
	public ServiceObject withTcComments(String TcComments){
		this.TcComments = TcComments;
		return this;
	}
	
	public String getTcComments(){
		return this.TcComments;
	}
	
	public ServiceObject withTcName(String tcName){
		this.tcName = tcName;
		return this;
	}
	
	public String getTcName(){
		return this.tcName;
	}
	
	public ServiceObject withTcIndex(String tcIndex){
		this.tcIndex = tcIndex;
		return this;
	}
	
	public String getTcIndex(){
		return this.tcIndex;
	}
	
	public ServiceObject withTcClass(String tcClass){
		this.tcClass = tcClass;
		return this;
	}
	
	public String getTcClass(){
		return this.tcClass;
	}
//-----------------------------------------------------------------------------------------------------------------------	
	public static String replaceQuotes(String value) {
		return value.replaceAll("[\\u2018\\u2019]", "'").replaceAll("[\\u201C\\u201D]", "\"");
	}
}