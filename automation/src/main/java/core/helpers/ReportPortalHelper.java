package core.helpers;


import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.FileUtils;

import core.apiCore.interfaces.RestApiInterface;
import core.support.configReader.Config;
import core.support.listeners.ParamOverrideTestNgService;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;

public class ReportPortalHelper {

	final String uri = ParamOverrideTestNgService.ENDPOINT;
	final String uuid = ParamOverrideTestNgService.UUID;
	final String launch = ParamOverrideTestNgService.LAUNCH;
	final String project = ParamOverrideTestNgService.PROJECT;
	final String reportEnabled = ParamOverrideTestNgService.REPORT_PORTAL_ENABLE;
	final String launchUUID = ParamOverrideTestNgService.LAUNCH_UUID;
	final String launchId = ParamOverrideTestNgService.LAUNCH_ID;


	/**
	 * update launcher attributes
	 * attribute format: key1:value1;key2:value2;
	 * @param target
	 */
	public void updateLaunchAttributes(String attributeString) {
		if(!Config.getBooleanValue(reportEnabled))
			return;
		
		if(attributeString.isEmpty())
			return;
		
		String[] attributes = attributeString.split(";");
		for(String attribute : attributes) {
			updateLaunchAttribute(attribute);
		}	
	}
	
	private void updateLaunchAttribute(String attributeString) {
		String launchId = getCurrentLaunchId();
		
		if(launchId.isEmpty())
			return;
		
		String[] attribute = attributeString.split(":");
		if(attribute.length != 2)
			Helper.assertFalse("attribute should be valid key:value format. attribute: " + attributeString);

		String key = attribute[0];
		String value = attribute[1];

		
		String body = "{\r\n"
				+ "  \"attributes\": [\r\n"
				+ "    {\r\n"
				+ "      \"action\": \"CREATE\",\r\n"
				+ "      \"from\": {\r\n"
				+ "        \"key\": \"string\",\r\n"
				+ "        \"value\": \"string\"\r\n"
				+ "      },\r\n"
				+ "      \"to\": {\r\n"
				+ "        \"key\": \""+key+"\",\r\n"
				+ "        \"value\": \""+value+"\"\r\n"
				+ "      }\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"description\": {\r\n"
				+ "    \"action\": \"DELETE\",\r\n"
				+ "    \"comment\": \"string\"\r\n"
				+ "  },\r\n"
				+ "  \"ids\": [\r\n"
				+ "    "+launchId+"\r\n"
				+ "  ]\r\n"
				+ "}";
	
		String endpoint = Config.getGlobalValue(uri) + "/api/v1/"+ Config.getGlobalValue(project) +"/launch/info?access_token=" + Config.getGlobalValue(uuid);
		ServiceObject service = new ServiceObject()
				.withUriPath(endpoint)
				.withContentType("application/json")
				.withMethod("PUT")
				.withRequestBody(body)
				.withRespCodeExp("200");
				
		RestApiInterface.RestfullApiInterface(service);
	}
	
	/**
	 * update attributes. format: key:value;key2:value2;
	 * @param testname
	 * @param attributeString
	 */
	public void updateTestAttributes(String testname, String attributeString) {
		
		if(!Config.getBooleanValue(reportEnabled))
			return;
		
		if(testname.isEmpty() || attributeString.isEmpty())
			return;

		String[] attributes = attributeString.split(";");
		for(String attribute : attributes) {
			updateTestAttribute(testname, attribute);
		}	
	}
	
	/**
	 * update attributes. format: key:value;
	 * @param testname
	 * @param attributeString
	 */
	private void updateTestAttribute(String testname, String attributeString) {
		String launchId = getCurrentLaunchId();
		
		if(launchId.isEmpty())
			return;
		
		String testId = getTestId(testname, launchId);
		
		if(testId.isEmpty()) {
			TestLog.ConsoleLogWarn("test not found in recent launcher(s). test name: " +  testname);
			return;
		}
		
		String[] attribute = attributeString.split(":");
		if(attribute.length != 2)
			Helper.assertFalse("attribute should be valid key:value format. attribute: " + attributeString);

		String key = attribute[0];
		String value = attribute[1];

		
		String body = "{\r\n"
				+ "  \"attributes\": [\r\n"
				+ "    {\r\n"
				+ "      \"action\": \"CREATE\",\r\n"
				+ "      \"from\": {\r\n"
				+ "        \"key\": \"string\",\r\n"
				+ "        \"value\": \"string\"\r\n"
				+ "      },\r\n"
				+ "      \"to\": {\r\n"
				+ "        \"key\": \""+key+"\",\r\n"
				+ "        \"value\": \""+value+"\"\r\n"
				+ "      }\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"description\": {\r\n"
				+ "    \"action\": \"DELETE\",\r\n"
				+ "    \"comment\": \"string\"\r\n"
				+ "  },\r\n"
				+ "  \"ids\": [\r\n"
				+ "    "+testId+"\r\n"
				+ "  ]\r\n"
				+ "}";
	
		String endpoint = Config.getGlobalValue(uri) + "/api/v1/"+ Config.getGlobalValue(project) +"/item/info?access_token=" + Config.getGlobalValue(uuid);
		ServiceObject service = new ServiceObject()
				.withUriPath(endpoint)
				.withContentType("application/json")
				.withMethod("PUT")
				.withRequestBody(body)
				.withRespCodeExp("200");
				
		RestApiInterface.RestfullApiInterface(service);
	}
	
	public void updateTestIssue(String testname, String issueName, String comment) {
		if(!Config.getBooleanValue(reportEnabled))
			return;
		
		if(testname.isEmpty() || issueName.isEmpty())
			return;
		
		String launchId = getCurrentLaunchId();
		
		if(launchId.isEmpty())
			return;
		
		String testId = getTestId(testname, launchId);
		String issueLocator = getIssueId(issueName);
		
		String body = "{\r\n"
				+ "  \"issues\": [\r\n"
				+ "    {\r\n"
				+ "      \"issue\": {\r\n"
				+ "        \"autoAnalyzed\": true,\r\n"
				+ "        \"comment\": \""+ comment +"\",\r\n"
				+ "        \"ignoreAnalyzer\": false,\r\n"
				+ "        \"issueType\": \""+ issueLocator +"\"\r\n"
				+ "      },\r\n"
				+ "      \"testItemId\": "+testId+"\r\n"
				+ "    }\r\n"
				+ "  ]\r\n"
				+ "}";
	
		String endpoint = Config.getGlobalValue(uri) + "/api/v1/" + Config.getGlobalValue(project) + "/launch/latest?filter.eq.name=" + Config.getGlobalValue(launch) + "&access_token=" + Config.getGlobalValue(uuid);
		endpoint = Config.getGlobalValue(uri) + "/api/v1/"+ Config.getGlobalValue(project) +"/item?access_token=" + Config.getGlobalValue(uuid);
		ServiceObject service = new ServiceObject()
				.withUriPath(endpoint)
				.withContentType("application/json")
				.withMethod("PUT")
				.withRequestBody(body)
				.withRespCodeExp("200");
				
		RestApiInterface.RestfullApiInterface(service);
		
	}
	
	/**
	 * get the latest launch id which represents the current run
	 * @return
	 */
	private String getCurrentLaunchId() {
		String launchUuid = Config.getGlobalValue(launchUUID);
		if(launchUuid.isEmpty()) {
			TestLog.ConsoleLogWarn("report portal launch uuid is empty");
			return "";
		}
		
		if(!Config.getGlobalValue(launchId).isEmpty())
			return Config.getGlobalValue(launchId);
		
		String endpoint = Config.getGlobalValue(uri) + "/api/v1/"+ Config.getGlobalValue(project) +"/launch?filter.eq.uuid=" + launchUuid + "&access_token=" + Config.getGlobalValue(uuid);

		ServiceObject service = new ServiceObject()
				.withUriPath(endpoint)
				.withContentType("application/json")
				.withMethod("GET")
				.withRespCodeExp("200")
				.withOutputParams(".id:<$launchId>;");
				
		RestApiInterface.RestfullApiInterface(service);
		String launchIdValue = Config.getValue("launchId");
		
		Config.setGlobalValue(launchId,launchIdValue);
	
		return launchIdValue;
	}
	
	
	/**
	 * get test id based on testname
	 * @return
	 */
	private String getTestId(String testname, String launchId) {
		String endpoint = Config.getGlobalValue(uri) + "/api/v1/" + Config.getGlobalValue(project) + "/item?filter.eq.launchId=" + launchId + "&access_token=" + Config.getGlobalValue(uuid);
		ServiceObject service = new ServiceObject()
				.withUriPath(endpoint)
				.withContentType("application/json")
				.withMethod("GET")
				.withRespCodeExp("200")
				.withOutputParams("$..content[?(@.name =~ /.*"+ testname +"/i)].id:<$testId>;");
				
		RestApiInterface.RestfullApiInterface(service);
		String testId = Config.getValue("testId");
	
		return testId;
	}
	
	
	/**
	 * get issue id based on issue name
	 * @return
	 */
	private String getIssueId(String issueName) {
		if(issueName.trim() == "")
			return "";
		
		String endpoint = Config.getGlobalValue(uri) + "/api/v1/project/" + Config.getGlobalValue(project) + "?access_token=" +  Config.getGlobalValue(uuid);
		ServiceObject service = new ServiceObject()
				.withUriPath(endpoint)
				.withContentType("application/json")
				.withMethod("GET")
				.withRespCodeExp("200")
				.withOutputParams("$..[?(@.longName =~ /.*"+ issueName +"/i)].locator:<$issueLocator>;");
				
		RestApiInterface.RestfullApiInterface(service);
		String issueLocator = Config.getValue("issueLocator");
	
		return issueLocator;
	}
	
	public void reportPortalLog(String testname, String message) {
		if(!Config.getBooleanValue(reportEnabled))
			return;
		
		String launchId = getCurrentLaunchId();		
		if(launchId.isEmpty())
			return;
		
		String launchUuid = Config.getGlobalValue(launchUUID);
		if(launchUuid.isEmpty()) {
			System.out.println("report portal launch uuid is empty");
			return;
		}
		
		String testUuid = getTestId(testname, launchId);		
		if(testUuid.isEmpty()) {
			System.out.println("test not found in recent launcher. test name: " +  testname);
			return;
		}
		
		String date = Helper.date.getCurrentTime("yyyy-MM-dd'T'HH:mm:s");
		
		// send message
		String filename = "";
		String endpoint = uri + "/api/v1/" + project + "/log?access_token=" +  uuid;
		String body = "{\r\n"
				+ "  \"file\": {\r\n"
				+ "    \"name\": \"" + filename+ "\",\r\n"
				+ "	\"content-type\": \"image/png\"\r\n"
				+ "  },\r\n"
				+ "  \"itemUuid\": \""+testUuid+"\",\r\n"
				+ "  \"launchUuid\": \""+launchUuid+"\",\r\n"
				+ "  \"level\": \"error\",\r\n"
				+ "  \"message\": \""+message+"\",\r\n"
				+ "  \"time\": \""+date+"\"\r\n"
				+ "  }";
	
		ServiceObject service = new ServiceObject()
				.withUriPath(endpoint)
				.withContentType("application/json")
				.withMethod("POST")
				.withRequestBody(body);
				
		RestApiInterface.RestfullApiInterface(service);
		
	}
	
	public void reportPortalLogWithAttachment(String testname, String message, String status, String filePath) {
		if(!Config.getBooleanValue(reportEnabled))
			return;
		
		String launchId = getCurrentLaunchId();	
		if(launchId.isEmpty())
			return;
		
		String launchUuid = Config.getGlobalValue(launchUUID);
		if(launchUuid.isEmpty()) {
			System.out.println("report portal launch uuid is empty");
			return;
		}
		
		String testUuid = getTestId(testname, launchId);		
		if(testUuid.isEmpty()) {
			System.out.println("test not found in recent launcher. test name: " +  testname);
			return;
		}
		
		String endpoint = uri + "/api/v1/" + project + "/log?access_token=" +  uuid;
		File tempFile = null;
		File attachment = null;
		
		// create json_request_part
		try {
			
			// copy screenshot/file to template location
		    attachment = new File(filePath);
			FileUtils.copyFile(attachment, new File(File.separator + "test-output" + File.separator + File.separator + attachment.getName()));
			
			String date = Helper.date.getCurrentTime("yyyy-MM-dd'T'HH:mm:s");
			
			// send message
			String body = "[{\r\n"
					+ "  \"file\": {\r\n"
					+ "    \"name\": \"" + attachment.getName() + "\",\r\n"
					+ "	\"content-type\": \"image/png\"\r\n"
					+ "  },\r\n"
					+ "  \"itemUuid\": \""+testUuid+"\",\r\n"
					+ "  \"launchUuid\": \""+launchUuid+"\",\r\n"
					+ "  \"level\": \""+status+"\",\r\n"
					+ "  \"message\": \""+message+"\",\r\n"
					+ "  \"time\": \""+date+"\"\r\n"
					+ "  }]";
			
				tempFile = new File(File.separator + "test-output" + File.separator +  "reportPortal.json");
				FileOutputStream fos = new FileOutputStream(tempFile);
				 byte[] bytesArray = body.getBytes();
				 fos.write(bytesArray);
				 fos.flush();
				 fos.close();
				 
//			String content = Files.toString(tempFile, Charsets.UTF_8);
//			System.out.println(content.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		String body = "FILE:json_request_part:reportPortal.json,FILE:file:" + attachment.getName();
	
		ServiceObject service = new ServiceObject()
				.withUriPath(endpoint)
				.withContentType("multipart/form-data")
				.withMethod("POST")
				.withRequestBody(body);
				
		RestApiInterface.RestfullApiInterface(service);	
	}

	
}