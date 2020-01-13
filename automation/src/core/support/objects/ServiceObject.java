 package core.support.objects;

import org.apache.commons.lang.StringUtils;

public class ServiceObject implements Cloneable {

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
	private String parent = StringUtils.EMPTY; // name of the parent object to inherit from

	public ServiceObject setServiceObject(String TestSuite, String TestCaseID, String RunFlag, String Description,
			String InterfaceType, String UriPath, String ContentType, String Method, String Option,
			String RequestHeaders, String TemplateFile, String RequestBody, String OutputParams, String RespCodeExp,
			 String ExpectedResponse, String TcComments,
			String tcName, String tcIndex, String testType) {
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
		
		return this;
	}
	
	public ServiceObject setServiceObject(Object[] testData) {
		
		this.TestSuite = getArayValue(testData, 0);
		this.TestCaseID = getArayValue(testData, 1);
		this.RunFlag = getArayValue(testData, 2);
		this.Description = getArayValue(testData, 3);
		this.InterfaceType = getArayValue(testData, 4);
		this.UriPath = getArayValue(testData, 5);
		this.ContentType = getArayValue(testData, 6);
		this.Method = getArayValue(testData, 7);
		this.Option = getArayValue(testData, 8);
		this.RequestHeaders = getArayValue(testData, 9);
		this.TemplateFile = getArayValue(testData, 10);
		this.RequestBody = getArayValue(testData, 11);
		this.OutputParams = getArayValue(testData, 12);
		this.RespCodeExp = getArayValue(testData, 13);
		this.ExpectedResponse = getArayValue(testData, 14);
		this.TcComments = getArayValue(testData, 15);
		this.tcName = getArayValue(testData, 16);
		this.tcIndex = getArayValue(testData, 17);
		this.testType = getArayValue(testData, 18);
		
		return this;
	}
	
	private String getArayValue(Object[] testData, int index) {
		if(index >= testData.length)
			return StringUtils.EMPTY;
		
		String value = testData[index].toString();
		
		if(StringUtils.isBlank(value))
			return StringUtils.EMPTY;
		return value;
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
		return this.RunFlag.trim();
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
		return this.InterfaceType.trim();
	}
	
	public ServiceObject withUriPath(String UriPath){
		this.UriPath = UriPath;
		return this;
	}
	
	public String getUriPath(){
		return this.UriPath.trim();
	}
	
	public ServiceObject withContentType(String ContentType){
		this.ContentType = ContentType;
		return this;
	}
	
	public String getContentType(){
		return this.ContentType.trim();
	}
	
	public ServiceObject withMethod(String Method){
		this.Method = Method;
		return this;
	}
	
	public String getMethod(){
		return normalize(this.Method);
	}
	
	public ServiceObject withOption(String Option){
		this.Option = Option;
		return this;
	}
	
	public ServiceObject withParent(String parent){
		this.parent = parent;
		return this;
	}
	
	public String getParent(){
		if(StringUtils.isBlank(this.parent))
			return TestObject.DEFAULT_TEST;
		return this.parent;
	}
	
	public String getOption(){
		return this.Option.trim();
	}
	
	public ServiceObject withRequestHeaders(String RequestHeaders){
		this.RequestHeaders = RequestHeaders;
		return this;
	}
	
	public String getRequestHeaders(){
		return normalize(this.RequestHeaders);
	}
	
	public ServiceObject withTemplateFile(String TemplateFile){
		this.TemplateFile = TemplateFile;
		return this;
	}
	
	public String getTemplateFile(){
		return this.TemplateFile.trim();
	}
	
	public ServiceObject withRequestBody(String RequestBody){
		this.RequestBody = RequestBody;
		return this;
	}
	
	public String getRequestBody(){
		return normalize(this.RequestBody);
	}
	
	public ServiceObject withOutputParams(String OutputParams){
		this.OutputParams = OutputParams;
		return this;
	}
	
	public String getOutputParams(){
		return normalize(this.OutputParams);
	}
	
	public ServiceObject withRespCodeExp(String RespCodeExp){
		this.RespCodeExp = RespCodeExp;
		return this;
	}
	
	public String getRespCodeExp(){
		return this.RespCodeExp.trim();
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
		this.tcName = tcName.trim();
		return this;
	}
	
	public String getTcName(){
		return this.tcName.trim();
	}
	
	public String getTcType(){
		return this.testType;
	}
	
	public ServiceObject withTcIndex(String tcIndex){
		this.tcIndex = tcIndex;
		return this;
	}
	
	public String getTcIndex(){
		return this.tcIndex.split(":")[0];
	}
	
	public String getTcCount(){
		return this.tcIndex.split(":")[1];
	}
	
	public static String normalize(String value) {
		// remove new lines
		value = value.replaceAll("\\R+", " ");
		
		// reduces spaces to single space. eg. "    " to " "
		value = value.trim().replaceAll(" +", " ");
		return value.replaceAll("[\\u2018\\u2019]", "'").replaceAll("[\\u201C\\u201D]", "\"");
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}