package core.helpers;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.zeroturnaround.zip.ZipUtil;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.webapi.SlackWebApiClient;
import core.support.configReader.Config;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.TestObject;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.drivers.AbstractDriverTestNG;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class UtilityHelper {

	/**
	 * generates random string of length len
	 * 
	 * @param len
	 * @return
	 */
	protected static String generateRandomString(int len) {
		String AB = "0123456789abcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();

		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

	/**
	 * generates random int of length len
	 * 
	 * @param len
	 * @return
	 */
	protected static String generateRandomInteger(int len) {
		String AB = "0123456789";
		SecureRandom rnd = new SecureRandom();

		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

	/**
	 * normalizes string removes space, new line chars, quotes
	 * 
	 * @param value
	 * @return
	 */
	protected static String stringNormalize(String value) {
		value = value.trim().replace("\n", "").replace("\r", "").replace("\"", "");
		return value;
	}

	/**
	 * normalizes string removes space, new line chars
	 * 
	 * @param value
	 * @return
	 */
	protected static String stringRemoveLines(String value) {
		value = value.trim().replace("\n", "").replace("\r", "");
		return value;
	}

	/**
	 * returns true if process is running
	 * 
	 * @param serviceName
	 * @return
	 * @throws Exception
	 */
	protected static boolean isProcessRunning(String serviceName) throws Exception {
		String TASKLIST = "tasklist";

		Process p = Runtime.getRuntime().exec(TASKLIST);
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains(serviceName)) {
				return true;
			}
		}

		return false;

	}

	/**
	 * returns kills the process if running
	 * 
	 * @param serviceName
	 * @return
	 * @throws Exception
	 */
	protected static void killWindowsProcess(String serviceName) {
		String KILL = "taskkill /F /IM ";
		try {
			if (isProcessRunning(serviceName))
				Runtime.getRuntime().exec(KILL + serviceName);
		} catch (IOException e) {
			e.getMessage();
		} catch (Exception e) {
			e.getMessage();
		}

	}

	/**
	 * runs shell command and returns results as an array list
	 * 
	 * @param cmd
	 * @return
	 */
	protected static ArrayList<String> runShellCommand(String cmd) {
		ArrayList<String> results = new ArrayList<String>();
		Process pr = null;
		boolean success = false;
		int retry = 3;
		do {
			retry--;
			try {
				Runtime run = Runtime.getRuntime();
				pr = run.exec(cmd);
				pr.waitFor();
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line;
				while ((line = buf.readLine()) != null) {
					results.add(line);
				}
				success = true;
			} catch (Exception e) {
				TestLog.ConsoleLogDebug("shell command:  '" + cmd + "' output: " + e.getMessage());
			} finally {
				if (pr != null)
					pr.destroy();
			}
		} while (!success && retry > 0);
		return results;
	}

	/**
	 * Copies directory and all content from dirFrom to dirTo overwrites the content
	 * 
	 * @param dirFrom
	 * @param dirTo
	 */
	protected static void copyDirectory(String dirFrom, String dirTo) {
		File srcDir = new File(dirFrom);
		File destDir = new File(dirTo);
		try {
			FileUtils.copyDirectory(srcDir, destDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static void executeJavascript(String script) {
		JavascriptExecutor js = (JavascriptExecutor) AbstractDriver.getWebDriver();
		js.executeScript(script);
	}

	/**
	 * zip folder
	 * 
	 * @param srcFolder
	 * @param destZipFile
	 * @return
	 * @throws Exception
	 */
	protected static ZipOutputStream zipFolder(String srcFolder, String destZipFile) throws Exception {
		ZipOutputStream zip = createZip(destZipFile);
		zip.flush();
		zip.close();
		return zip;
	}

	protected static ZipOutputStream createZip(String destZipFile) throws FileNotFoundException {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;
		fileWriter = new FileOutputStream(destZipFile);
		zip = new ZipOutputStream(fileWriter);
		return zip;
	}

	protected static void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {

		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		} else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			in.close();
		}

	}

	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			} else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
			}
		}
	}

	/**
	 * gets a list of string from 'source' starting with 'value'
	 * 
	 * @param source
	 * @param value
	 * @return
	 */
	protected static List<String> getAllValuesStartringWith(String source, String value) {
		List<String> values = new ArrayList<String>();
		Pattern pattern = Pattern.compile(value + "\\w+");

		Matcher matcher = pattern.matcher(source);
		while (matcher.find()) {
			values.add(matcher.group());
		}
		return values;
	}

	/**
	 * 
	 * @param str
	 * @param pattern
	 *            regular expression pattern
	 * @return
	 */
	protected static String getValueFromPattern(String str, String pattern) {
		return getValuesFromPattern(str, pattern).get(0);
	}

	/**
	 * 
	 * @param str
	 * @param pattern
	 *            regular expression pattern
	 * @return
	 */
	protected static List<String> getValuesFromPattern(String str, String pattern) {
		Pattern TAG_REGEX = Pattern.compile(pattern);

		List<String> tagValues = new ArrayList<String>();
		Matcher matcher = TAG_REGEX.matcher(str);
		while (matcher.find()) {
			tagValues.add(matcher.group(1));
		}
		return tagValues;
	}

	/**
	 * get current directory
	 * 
	 * @return
	 */
	protected static String getCurrentDir() {
		String current = "";
		try {
			current = new java.io.File(".").getCanonicalPath() + "/";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return current;
	}

	/**
	 * sends slack notification token: generate at:
	 * https://api.slack.com/custom-integrations/legacy-tokens for channel id: right
	 * click and channel and copy link. the id is attached to the link see
	 * properties file for values to use
	 */
	public static void slackNotificationWithFile(String title, String comment, String filePath) {
		String token = Config.getValue("slackToken");
		String channelId = Config.getValue("channelId");

		SlackWebApiClient webApiClient = SlackClientFactory.createWebApiClient(token);
		File testfile = new File(filePath);

		webApiClient.auth();
		if (testfile != null && testfile.exists()) {
			webApiClient.uploadFile(testfile, title, comment, channelId);
		}
	}

	/**
	 * zips directory path starts from root pom directory
	 * 
	 * @param dir:
	 *            root path + dir
	 * @param outputFilePath:
	 *            usage: root + dir + outputFilePath
	 * @return
	 */
	public static String zipDir(String sourceDir, String destFilePath) {
		String destFile = destFilePath + ".zip";
		ZipUtil.pack(new File(sourceDir), new File(destFile));
		// wait for zip file to be created
		Helper.wait.waitForSeconds(3);
		return destFile;
	}

	/**
	 * gets the list of files tye: file type. eg. ".csv"
	 * 
	 * @return
	 */
	protected static ArrayList<File> getFileList(String directoryPath, String type) {
		File folder = new File(directoryPath);
		File[] listOfFiles = folder.listFiles();
		ArrayList<File> testFiles = new ArrayList<File>();

		// fail test if no csv files found
		if (listOfFiles == null)
			Helper.assertTrue("test files not found at: " + directoryPath, false);

		// filter files by suffix and add to testFiles list
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(type)) {
				testFiles.add(listOfFiles[i]);
				// System.out.println("File " + listOfFiles[i].getName());
			}
		}
		return testFiles;
	}

	/**
	 * captures screenshot and attaches to extent test report
	 * 
	 * @param description
	 */
	protected static void captureScreenshot() {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.ENGLISH).format(now);
		String extentReportImageFullPath = ExtentManager.getScreenshotsFolderFullPath()
				+ TestObject.getTestInfo().testName + "-" + format1 + ".png";
		
		String extentReportImageRelativePath = ExtentManager.getScreenshotsFolderRelativePath()
				+ TestObject.getTestInfo().testName + "-" + format1 + ".png";

		try {
			File scrFile = ((TakesScreenshot) AbstractDriverTestNG.getWebDriver()).getScreenshotAs(OutputType.FILE);
			// now copy the screenshot to desired location using copyFile method
			FileUtils.copyFile(scrFile, new File(extentReportImageFullPath));

			AbstractDriver.getStep().get().log(Status.INFO, "Screenshot ",
					MediaEntityBuilder.createScreenCaptureFromPath(extentReportImageRelativePath).build());

		} catch (Exception e) {
			// TestLog.ConsoleLog("Error in the captureAndDisplayScreenShot
			// method: " + e.getMessage());
		}
	}

	/**
	 * Gets a picture of specific element displayed on the page
	 * 
	 * @param element
	 *            The element
	 * @return File
	 * @throws Exception
	 */
	public static File captureElementPicture(EnhancedBy element) {
		return captureElementPicture(element, 0);
	}

	/**
	 * Gets a picture of specific element displayed on the page
	 * 
	 * @param element
	 *            The element
	 * @return File
	 * @throws Exception
	 */
	public static File captureElementPicture(EnhancedBy element, int index) {
		File screen = null;
		try {

			EnhancedWebElement targetElement = Element.findElements(element);

			// get the entire screenshot from the driver of passed WebElement
			screen = ((TakesScreenshot) AbstractDriverTestNG.getWebDriver()).getScreenshotAs(OutputType.FILE);

			// create an instance of buffered image from captured screenshot
			BufferedImage img = ImageIO.read(screen);
			int originalImageWidth = img.getWidth();
			int originalImageHeight = img.getHeight();

			// actual test screen size. screenshot size can be different
			int actualTestWidth = AbstractDriverTestNG.getWebDriver().findElement(By.tagName("body")).getRect().width;
			int actualTestHeight = AbstractDriverTestNG.getWebDriver().findElement(By.tagName("body")).getRect().height;
			
			// if mobile, use window getSize insetad. could be less accurate
			if(Helper.mobile.isMobile()) {
				actualTestWidth =  AbstractDriverTestNG.getWebDriver().manage().window().getSize().width;
				actualTestHeight =  AbstractDriverTestNG.getWebDriver().manage().window().getSize().height;
			}
			
			// get the ratio of the screenshot taken vs actual size of the screen
			// the image needs to be resize to match the test screen, so we can match the
			// element position
			double widthRatio = (double) originalImageWidth / actualTestWidth;
			double heightRatio = (double) originalImageHeight / actualTestHeight;

			// get the width and height of the WebElement using getSize()
			int elementWidth = targetElement.get(index).getSize().getWidth();
			int elementHeight = targetElement.get(index).getSize().getHeight();

			// create a rectangle using width and height
			Rectangle rect = new Rectangle(elementWidth, elementHeight);

			// get the location of WebElement in a Point.
			// this will provide X & Y co-ordinates of the WebElement
			Point p = targetElement.get(index).getLocation();

			// create image for element using its location and size.
			// this will give image data specific to the WebElement
			int xValue = (int) (p.getX() * widthRatio);
			int yValue = (int) (p.getY() * heightRatio);

			int widthValue = (int) (rect.width * widthRatio);
			int heightValue = (int) (rect.height * heightRatio);

			BufferedImage dest = img.getSubimage(xValue, yValue, widthValue, heightValue);

			// write back the image data for element in File object
			ImageIO.write(dest, "png", screen);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// return the File object containing image data
		return screen;
	}
}