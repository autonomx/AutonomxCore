package core.apiCore.interfaces;

import org.apache.commons.lang3.ArrayUtils;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;

import core.apiCore.helpers.DataHelper;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.ApiObject;

public class AzureInterface {
	private static final String AZURE_ACCOUNT_KEY = "azure.key";
	private static final String AZURE_ACCOUNT = "azure.account";
	private static final String UPLOAD_TO_FILE_SHARE = "azure.uploadToFileShare";

	public static CloudStorageAccount conn = null;

	/**
	 * /* (String TestSuite, String TestCaseID, String RunFlag, String Description,
	 * String InterfaceType, String UriPath, String ContentType, String Method,
	 * String Option, String RequestHeaders, String TemplateFile, String
	 * RequestBody, String OutputParams, String RespCodeExp, String
	 * ExpectedResponse, String ExpectedResponse, String NotExpectedResponse,
	 * String TcComments, String tcName, String tcIndex)
	 *
	 * interface for azure storage api calls
	 * 
	 * @param apiObject
	 * @return
	 * @throws Exception
	 */
	public static void AzureClientInterface(ApiObject apiObject) throws Exception {
		connectAzure();
		switch (apiObject.Method) {
		case UPLOAD_TO_FILE_SHARE:
			azureUploadToFileShare(apiObject);
			break;
		default:
			Helper.assertTrue("method not selected", false);
			break;
		}
	}

	public synchronized static void connectAzure() throws Exception {
		if (conn == null) {
			String account = Config.getValue(AZURE_ACCOUNT);
			String key = Config.getValue(AZURE_ACCOUNT_KEY);

			String storageConnectionString = "DefaultEndpointsProtocol=http;" + "AccountName=" + account + ";"
					+ "AccountKey=" + key;

			// Use the CloudStorageAccount object to connect to your storage account
			conn = CloudStorageAccount.parse(storageConnectionString);

		}
	}

	public static void azureUploadToFileShare(ApiObject apiObject) throws Exception {
		TestLog.logPass("calling method: " + apiObject.Method);

		CloudFileDirectory cloudFileDirectory = getCloudfileDirectory(apiObject);


		String filePath = DataHelper.getTemplateFile(apiObject.TemplateFile);
	
		// Define the path to a local file.
		TestLog.logPass("uploading file: " + apiObject.TemplateFile);
		CloudFile cloudFile = cloudFileDirectory.getFileReference(apiObject.TemplateFile);
		cloudFile.uploadFromFile(filePath);
	}

	/**
	 * sets the location of the azure file directory from option column
	 * 
	 * @param apiObject
	 * @return
	 * @throws Exception
	 */
	public static CloudFileDirectory getCloudfileDirectory(ApiObject apiObject) throws Exception {

		String destination = apiObject.Option.split(":")[1];
		TestLog.logPass("uploading to dir: " + destination);

		String[] dirs = destination.split("/");

		// Create the Azure Files client.
		CloudFileClient fileClient = conn.createCloudFileClient();

		// Get a reference to the file share
		CloudFileShare share = fileClient.getShareReference(dirs[0]);

		// remove root dir from path
		dirs = ArrayUtils.removeElement(dirs, dirs[0]);

		// Get a reference to the root directory for the share.
		CloudFileDirectory cloudFileDirectory = share.getRootDirectoryReference();

		for (String dir : dirs) {
			cloudFileDirectory = cloudFileDirectory.getDirectoryReference(dir);
			cloudFileDirectory.createIfNotExists();
		}

		return cloudFileDirectory;
	}
}
