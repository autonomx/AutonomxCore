package core.helpers;

import java.io.File;
import java.util.List;

import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class FileUploadHelper {

	/**
	 * uploads file by specifying file location relative to main path
	 * 
	 * @param location
	 * @param fileButton
	 */
	protected static void uploadFile(String location, EnhancedBy fileButton) {
		File file = new File("");
		String path = file.getAbsolutePath() + location;
		Helper.form.setField(fileButton, path);
		TestLog.logPass("I upload file at location '" + location + "'");
	}

	/**
	 * 
	 * @param location
	 *            eg. "/jenkins.png" in the seleniumCl folder
	 * @param fileButton
	 *            the button to press When selection the file explorer to find the
	 *            image
	 * @param accept
	 *            the accept button.
	 */
	protected static void uploadAndAcceptFile(String location, EnhancedBy fileButton, EnhancedBy accept) {
		File file = new File("");
		String path = file.getAbsolutePath() + location;

		EnhancedWebElement fieldElement = Element.findElements(fileButton);
		fieldElement.click();
		AbstractDriver.getWebDriver().switchTo().activeElement().sendKeys(path);

		EnhancedWebElement acceptElement = Element.findElements(accept);
		acceptElement.click();
		TestLog.logPass("I upload file at location '" + location + "'");
	}

	protected static void uploadImages(List<String> locations, EnhancedBy imageButton, EnhancedBy images) {
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
	protected static void uploadImage(String location, EnhancedBy imageButton, EnhancedBy images) {
		int imageCount = Helper.list.getListCount(images);
		File file = new File("");
		String path = file.getAbsolutePath() + location;
		Helper.form.setField(imageButton, path);
		Helper.wait.waitForAdditionalElementsToLoad(images, imageCount);
		TestLog.logPass("I upload image at location '" + location + "'");
	}
}