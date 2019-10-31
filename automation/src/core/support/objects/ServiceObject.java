package core.support.objects;

import org.apache.commons.lang.StringUtils;

import core.helpers.Helper;

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
	private String tcIndex = StringUtils.EMPTY;
	private String testType = StringUtils.EMPTY;
	private String parentClass = StringUtils.EMPTY; // name of the test class. eg. ServiceTestRunner
	private String tcCount = StringUtils.EMPTY; // number of tests in csv file

	public ServiceObject setServiceObject(String TestSuite, String TestCaseID, String RunFlag, String Description,
			String InterfaceType, String UriPath, String ContentType, String Method, String Option,
			String RequestHeaders, String TemplateFile, String RequestBody, String OutputParams, String RespCodeExp,
			 String ExpectedResponse, String TcComments,
			String tcName, String tcIndex, String testType, String tcCount) {
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
		this.TcComments = TcComments;
		this.tcName = tcName;
		this.tcIndex = tcIndex;
		this.testType = testType;
		this.tcCount = tcCount;
		
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
		return Helper.stringNormalize(this.Method);
	}
	
	public ServiceObject withOption(String Option){
		this.Option = Option;
		return this;
	}
	
	public ServiceObject withParentClass(String parentClass){
		this.parentClass = parentClass;
		return this;
	}
	
	public String getParentClass(){
		return this.parentClass;
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
	
	public String getTcType(){
		return this.testType;
	}
	
	public ServiceObject withTcIndex(String tcIndex){
		this.tcIndex = tcIndex;
		return this;
	}
	
	public String getTcIndex(){
		return this.tcIndex;
	}
	
	public ServiceObject withTcCount(String tcCount){
		this.tcCount = tcCount;
		return this;
	}
	
	public String getTcCount(){
		return this.tcCount;
	}
//-----------------------------------------------------------------------------------------------------------------------	
	public static String replaceQuotes(String value) {
		return value.replaceAll("[\\u2018\\u2019]", "'").replaceAll("[\\u201C\\u201D]", "\"");
	}
}