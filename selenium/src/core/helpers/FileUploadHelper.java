package core.helpers;

import java.io.File;
import java.util.List;

import core.driver.AbstractDriver;
import core.logger.TestLog;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;

public class FileUploadHelper {

	/**
	 * uploads file by specifying file location relative to main path
	 * 
	 * @param location
	 * @param fileButton
	 */
	public static void uploadFile(String location, EnhancedBy fileButton) {
		File file = new File("");
		String path = file.getAbsolutePath() + location;
		FormHelper.setField(path, fileButton);
		TestLog.logPass("I upload file at location '" + location + "'");
	}
	
	/**
	 * 
	 * @param location eg. "/jenkins.png" in the seleniumCl folder
	 * @param fileButton the button to press when selection the file explorer to find the image
	 * @param accept the accept button.
	 */
	public static void uploadAndAcceptFile(String location, EnhancedBy fileButton, EnhancedBy accept) {
		File file = new File("");
		String path = file.getAbsolutePath() + location;
	
		EnhancedWebElement fieldElement = Element.findElements(fileButton);
		fieldElement.click();
		AbstractDriver.getWebDriver().switchTo().activeElement().sendKeys(path);
		
		EnhancedWebElement acceptElement = Element.findElements(accept);
		acceptElement.click();
		TestLog.logPass("I upload file at location '" + location + "'");
	}

	public static void uploadImages(List<String> locations, EnhancedBy imageButton, EnhancedBy images) {
		for (String location : locations) {
			uploadImage(location, imageButton, images);
		}
	}

	/**
	 * sets the image based on location
	 * 
	 * @param location
	 * @param imageButton
	 * @param images
	 *            : uploaded image
	 */
	public static void uploadImage(String location, EnhancedBy imageButton, EnhancedBy images) {
		int imageCount = ListHelper.getListCount(images);
		File file = new File("");
		String path = file.getAbsolutePath() + location;
		FormHelper.setField(path, imageButton);
		WaitHelper.waitForAdditionalElementsToLoad(images, imageCount);
		TestLog.logPass("I upload image at location '" + location + "'");
	}
}