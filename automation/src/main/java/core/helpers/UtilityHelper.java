package core.helpers;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.zeroturnaround.zip.ZipUtil;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.webapi.SlackWebApiClient;
import core.support.configReader.Config;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.drivers.AbstractDriverTestNG;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;
import java8.util.concurrent.ThreadLocalRandom;

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
	 * generates random number between two numbers, min, max
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	protected static int generateRandomNumber(int min, int max) {
		int random = ThreadLocalRandom.current().nextInt(min, max + 1);
		return random;
	}

	/**
	 * normalizes string removes space, new line chars, quotes
	 * 
	 * @param value
	 * @return
	 */
	protected static String stringNormalize(String value) {
		value = StringUtils.normalizeSpace(value);
		value = value.trim().replace("\n", "").replace("\r", "").replace("\"", "");
		value = value.replaceAll("\\r|\\n", "");

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
		value = value.replaceAll("\\r|\\n", "");
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
		excuteCommand(KILL + serviceName);
	}

	protected static void killMacProcess(String serviceName) {
		excuteCommand("killall " + serviceName);
	}

	/**
	 * run command and return results as array list will run bash on linux or mac
	 * will run batch command on windows
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	protected static ArrayList<String> excuteCommand(String command) {
		ArrayList<String> results = new ArrayList<String>();

		if (Helper.isMac() || Helper.isUnix()) {
			results = runCommand(new String[] { "/bin/sh", "-c", command });
		} else if (Helper.isWindows()) {
			results = runCommand("cmd /c start " + command);
		}

		return results;
	}

	/**
	 * run script file and return results as array list will run bash on linux or
	 * mac will run batch command on windows
	 * 
	 * @param filePath path from the root directory ( where pom.xml is )
	 * @return the results as arraylist
	 */
	protected static ArrayList<String> excuteCommandFromFile(String filePath) {
		filePath = Helper.getRootDir() + filePath;
		File file = new File(filePath);
		if (!file.isFile()) {
			throw new IllegalArgumentException("The file " + filePath + " does not exist");
		}

		ArrayList<String> results = new ArrayList<String>();

		if (Helper.isMac() || Helper.isUnix()) {
			results = runCommand(new String[] { "/bin/sh", "-c", filePath });
		} else if (Helper.isWindows()) {
			results = runCommand("cmd /c start " + filePath);
		}

		return results;
	}

	private static ArrayList<String> runCommand(String... cmd) {
		ArrayList<String> results = new ArrayList<String>();
		Process pr = null;
		boolean success = false;
		int retry = 3;
		String path = CrossPlatformProperties.getPath();
		String[] env = { "PATH=" + path };

		do {
			retry--;
			try {
				Runtime run = Runtime.getRuntime();
				pr = run.exec(cmd, env);
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
		if (results.isEmpty())
			TestLog.ConsoleLogDebug(
					"shell command:  '" + cmd + "' did not return results. please check your path: " + path);
		return results;
	}

	/**
	 * Copies directory And all content from dirFrom to dirTo overwrites the content
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
			zip.putNextEntry(new ZipEntry(path + File.separator + folder.getName()));
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
				addFileToZip(folder.getName(), srcFolder + File.separator + fileName, zip);
			} else {
				addFileToZip(path + File.separator + folder.getName(), srcFolder + File.separator + fileName, zip);
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
	 * @param pattern regular expression pattern
	 * @return
	 */
	protected static String getValueFromPattern(String str, String pattern) {
		return getValuesFromPattern(str, pattern).get(0);
	}

	/**
	 * 
	 * @param str
	 * @param pattern regular expression pattern
	 * @return
	 */
	protected static List<String> getValuesFromPattern(String str, String pattern) {
		List<String> tagValues = new ArrayList<String>();

		try {
			Pattern TAG_REGEX = Pattern.compile(pattern);

			Matcher matcher = TAG_REGEX.matcher(str);
			while (matcher.find()) {
				tagValues.add(matcher.group(1));
			}
		} catch (Exception e) {
			e.getMessage();
		}
		return tagValues;
	}

	/**
	 * get current project root directory, where pom.xml is
	 * 
	 * @return
	 */
	protected static String getRootDir() {
		Path currentWorkingDir = Paths.get("").toAbsolutePath();
		boolean isRoot = false;
		File root = new File(currentWorkingDir.toString());
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.getName().contains("pom"))
				isRoot = true;
		}
		if (!isRoot)
			Helper.assertFalse(
					"invalid root directory. is annotations processor turned off? " + root.getAbsolutePath());
		return currentWorkingDir.normalize().toString() + File.separator;
	}

	/**
	 * sends slack notification token: generate at:
	 * https://api.slack.com/custom-integrations/legacy-tokens for channel id: right
	 * click And channel And copy link. the id is attached to the link see
	 * properties file for values to use
	 */
	public static void slackNotificationWithFile(String title, String comment, String filePath) {
		String token = Config.getValue("slack.slackToken");
		String channelId = Config.getValue("slack.channelId");

		SlackWebApiClient webApiClient = SlackClientFactory.createWebApiClient(token);
		File testfile = new File(filePath);

		try {
			webApiClient.auth();
			if (testfile != null && testfile.exists()) {
				webApiClient.uploadFile(testfile, title, comment, channelId);
			}
		} catch (Exception e) {
			TestLog.ConsoleLog("slack notification error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * zips directory path starts from root pom directory
	 * 
	 * @param sourceDir:    root path + dir
	 * @param destFilePath: usage: root + dir + outputFilePath
	 * @return
	 */
	public static String zipDir(String sourceDir, String destFilePath) {
		String destFile = destFilePath + ".zip";
		ZipUtil.pack(new File(sourceDir), new File(destFile));
		// wait for zip file to be created
		Helper.wait.waitForSeconds(3);
		return destFile;
	}

	protected static ArrayList<File> getFileListByType(String directoryPath, String type) {
		return getFileListByType(directoryPath, type, false);
	}

	/**
	 * gets the list of files tye: file type. eg. ".csv"
	 * 
	 * @return
	 */
	protected static ArrayList<File> getFileListByType(String directoryPath, String type, boolean includeSubtype) {
		ArrayList<File> filteredFiles = new ArrayList<File>();
		ArrayList<File> testFiles = new ArrayList<File>();
		if (includeSubtype)
			testFiles = getFileList(directoryPath, testFiles);
		else
			testFiles = getFileList(directoryPath);

		for (File file : testFiles) {
			if (file.isFile() && file.getName().endsWith(type)) {
				filteredFiles.add(file);
			}
		}
		return filteredFiles;
	}

	/**
	 * returns the list of files in directory
	 * 
	 * @param directoryPath
	 * @return
	 */
	protected static ArrayList<File> getFileList(String directoryPath, ArrayList<File> files) {
		File directory = new File(directoryPath);

		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null)
			for (File file : fList) {
				if (file.isFile()) {
					files.add(file);
				} else if (file.isDirectory()) {
					getFileList(file.getAbsolutePath(), files);
				}
			}
		return files;
	}

	/**
	 * get file from file path
	 * 
	 * @param directoryPath
	 * @return
	 */
	protected static File getFile(String directoryPath) {
		File file = new File(directoryPath);
		if (!file.exists())
			Helper.assertFalse("test files not found at path: " + directoryPath);
		return file;
	}

	/**
	 * get file by name
	 * 
	 * @param path
	 * @param filename
	 * @return
	 */
	protected static File getFileByName(String path, String filename) {
		List<File> files = Helper.getFileList(path);
		for (File file : files) {
			String simplename = file.getName().split("\\.")[0];
			if (simplename.equals(filename))
				return file;
		}
		Helper.assertFalse("file: <" + filename + "> not found at path: " + path);
		return null;
	}

	/**
	 * returns the list of files in directory
	 * 
	 * @param directoryPath
	 * @return
	 */
	protected static ArrayList<File> getFileList(String directoryPath) {
		File folder = new File(directoryPath);
		File[] listOfFiles = folder.listFiles();
		ArrayList<File> testFiles = new ArrayList<File>();

		// fail test if no csv files found
		if (listOfFiles == null) {
			Helper.assertFalse("test files not found at path: " + directoryPath);
		}
		testFiles = new ArrayList<>(Arrays.asList(listOfFiles));
		return testFiles;
	}

	/**
	 * gets list of files including from sub folder based on type. eg. ".csv"
	 * 
	 * @return
	 */
	protected static List<File> getFileListWithSubfolders(String directoryName, String type, List<File> files) {
		File directory = new File(directoryName);

		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null)
			for (File file : fList) {
				if (file.isFile() && file.getName().endsWith(type)) {
					files.add(file);
				} else if (file.isDirectory()) {
					getFileListWithSubfolders(file.getAbsolutePath(), type, files);
				}
			}
		return files;
	}

	/**
	 * gets file content as String
	 * 
	 * @param absolutePath
	 * @return
	 */
	protected static String readFileContent(String absolutePath) {
		return getFileContent(absolutePath);
	}

	/**
	 * gets file content as String
	 * 
	 * @param absolutePath
	 * @return
	 */
	protected static String getFileContent(String absolutePath) {
		String content = StringUtils.EMPTY;
		absolutePath = absolutePath.replaceAll("'", "");
		File file = new File(absolutePath);

		// fail if file does not exist
		if (!file.exists())
			Helper.assertFalse("file not found at path: " + absolutePath);

		try {
			content = new String(Files.readAllBytes(Paths.get(absolutePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * create directories and files based on absolute path set permissions to
	 * rw-r--r-- set executable to true
	 * 
	 * @param path
	 */
	protected static void createFileFromPath(String absolutePath) {

		File file = new File(absolutePath);
		Path pathToFile = Paths.get(absolutePath);

		try {
			Files.createDirectories(pathToFile.getParent());
			file.createNewFile(); // if file already exists will do nothing
		} catch (IOException e) {
			e.printStackTrace();
		}

		// set permissions
		file.setReadable(true);
		file.setWritable(true);
		file.setExecutable(true);
	}

	/**
	 * Create file with path starting from root directory (where pom.xml is) and
	 * write to it. eg. writeFile("something","", "myFile", "txt");
	 * 
	 * @param value     value in file
	 * @param directory directory from root
	 * @param filename  name of the file
	 * @param type      type of file
	 */
	protected static void writeFile(String value, String directory, String filename, String type) {
		String fullPath = Helper.getRootDir() + File.separator + directory + File.separator + filename + "." + type;
		writeFile(value, fullPath);
	}

	/**
	 * Create file (where pom.xml is) and write to it
	 * 
	 * @param value
	 * @param absolutePath
	 */
	protected static void writeFile(String value, String absolutePath) {
		Helper.createFileFromPath(absolutePath);

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(absolutePath), "utf-8"))) {
			writer.write(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * delete file
	 * 
	 * @param absolutePath
	 */
	protected static void deleteFile(String absolutePath) {
		File file = new File(absolutePath);
		file.delete();
	}

	/**
	 * appends to existing file
	 * 
	 * @param value
	 * @param absolutePath
	 */
	protected static void appendToFile(String value, String absolutePath) {

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(absolutePath, true) // Set true for append mode
			);
			writer.write(value);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * appends to existing file
	 * 
	 * @param value
	 * @param directory
	 * @param filename
	 * @param type
	 */
	protected static void appendToFile(String value, String directory, String filename, String type) {
		String fullPath = Helper.getRootDir() + File.separator + directory + File.separator + filename + "." + type;
		appendToFile(value, fullPath);
	}

	/**
	 * captures screenshot And attaches to extent test report
	 * 
	 */
	protected static void captureExtentReportScreenshot() {
		Date now = new Date();

		String format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.ENGLISH).format(now);
		String extentReportImageFullPath = ExtentManager.getScreenshotsFolderFullPath()
				+ TestObject.getTestInfo().testName + "-" + format1 + ".png";

		String extentReportImageRelativePath = ExtentManager.getScreenshotsFolderRelativePath()
				+ TestObject.getTestInfo().testName + "-" + format1 + ".png";

		try {
			File scrFile = ((TakesScreenshot) AbstractDriverTestNG.getWebDriver()).getScreenshotAs(OutputType.FILE);
			// now copy the screenshot to desired location using copyFile method
			FileUtils.copyFile(scrFile, new File(extentReportImageFullPath));

			// for hmtl report, use relative path (we need to be able to email the report)
			if (Config.getValue(ExtentManager.REPORT_TYPE).equals(ExtentManager.HTML_REPORT_TYPE))
				AbstractDriver.getStep().get().info("").addScreenCaptureFromPath(extentReportImageRelativePath);
			else
				AbstractDriver.getStep().get().info("").addScreenCaptureFromPath(extentReportImageFullPath);

		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * Gets a picture of specific element displayed on the page
	 * 
	 * @param element The element
	 * @return File
	 * @throws Exception
	 */
	public static File captureElementPicture(EnhancedBy element) {
		return captureElementPicture(element, 0);
	}

	/**
	 * Gets a picture of specific element displayed on the page
	 * 
	 * @param element The element
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
			if (Helper.mobile.isMobile()) {
				actualTestWidth = AbstractDriverTestNG.getWebDriver().manage().window().getSize().width;
				actualTestHeight = AbstractDriverTestNG.getWebDriver().manage().window().getSize().height;
			}

			// get the ratio of the screenshot taken vs actual size of the screen
			// the image needs to be resize to match the test screen, so we can match the
			// element position
			double widthRatio = (double) originalImageWidth / actualTestWidth;
			double heightRatio = (double) originalImageHeight / actualTestHeight;

			// get the width And height of the WebElement using getSize()
			int elementWidth = targetElement.get(index).getSize().getWidth();
			int elementHeight = targetElement.get(index).getSize().getHeight();

			// create a rectangle using width And height
			Rectangle rect = new Rectangle(elementWidth, elementHeight);

			// get the location of WebElement in a Point.
			// this will provide X & Y co-ordinates of the WebElement
			Point p = targetElement.get(index).getLocation();

			// create image for element using its location And size.
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

	/**
	 * highlights the web element use on clicks and send key elements applicable to
	 * web driver
	 * 
	 * @param by
	 * @param index
	 */
	protected static void highLightWebElement(EnhancedBy by, int index) {
		// return if not web
		if (!Helper.isWebDriver())
			return;

		// return if not enabled
		if (!Config.getBooleanValue("web.element.highlight.enable"))
			return;

		double duration = Config.getDoubleValue("web.element.highlight.waitDurationInSeconds");

		JavascriptExecutor jsEx = (JavascriptExecutor) AbstractDriver.getWebDriver();
		EnhancedWebElement targetElement = Element.findElements(by);
		jsEx.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px dashed red;');",
				targetElement.get(index));
		Helper.waitForSeconds(duration);
		jsEx.executeScript("arguments[0].setAttribute('style','border: solid 2px white');", targetElement.get(index));
	}

	/**
	 * returns true if OS is mac
	 * 
	 * @return
	 */
	protected static boolean isMac() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.contains("mac");
	}

	/**
	 * returns true if OS is windows
	 * 
	 * @return
	 */
	protected static boolean isWindows() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.contains("win");
	}

	/**
	 * returns true if OS is unix or linux
	 * 
	 * @return
	 */
	protected static boolean isUnix() {
		String osName = System.getProperty("os.name");
		return (osName.indexOf("nix") >= 0 || osName.indexOf("linux") >= 0 || osName.indexOf("nux") >= 0
				|| osName.indexOf("aix") > 0);
	}

	/**
	 * is the string value UDID
	 * 
	 * @param value
	 * @return
	 */
	protected static boolean isUUID(String value) {
		try {
			UUID.fromString(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * returns if string is boolean
	 * 
	 * @param value
	 * @return
	 */
	protected static boolean isBoolean(String value) {
		value = value.toLowerCase();
		return (value.equals("true") || value.equals("false"));
	}

	/**
	 * return if string is a number
	 * 
	 * @param str
	 * @return
	 */
	protected static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * execute javascript
	 * 
	 * @param script
	 */
	protected static Object executeJs(String script, Object... args) {
		JavascriptExecutor js = (JavascriptExecutor) AbstractDriver.getWebDriver();
		Object value = js.executeScript(script, args);
		return value;
	}

	/**
	 * execute javascript
	 * 
	 * @param script
	 */
	@SuppressWarnings("unchecked")
	protected static List<String> executeJsWithListReturn(String script, Object... args) {
		Object valueObject = executeJs(script, args);
		List<String> value = (List<String>) valueObject;
		return value;
	}

	/**
	 * execute javascript
	 * 
	 * @param script
	 */
	protected static String executeJsWithStringReturn(String script, Object... args) {
		Object valueObject = executeJs(script, args);
		String value = (String) valueObject;
		return value;
	}

	/**
	 * get numeric value from string
	 * 
	 * @param value
	 * @param isFailOnNoInt
	 * @return
	 */
	protected static int getIntFromString(String value, boolean isFailOnNoInt) {
		double doubleVal = getDoubleFromString(value, isFailOnNoInt);
		return (int) doubleVal;
	}

	/**
	 * get numeric value from string
	 * 
	 * @param value
	 * @param isFailOnNoInt
	 * @return
	 */
	protected static double getDoubleFromString(String value, boolean isFailOnNoInt) {
		if (!isStringContainNumber(value)) {
			if (isFailOnNoInt)
				Helper.assertFalse("numeric value not found from String: " + value);
			else
				return -1;
		}
		// remove all non numeric characters
		value = value.replaceAll("[^\\d.]", "");

		// remove . if value starts with ".". eg. ".1"
		if (value.startsWith("."))
			value = value.substring(1);

		if (value.isEmpty())
			return -1;

		Scanner st = new Scanner(value);
		while (!st.hasNextDouble()) {
			st.next();
		}
		double valueDouble = st.nextDouble();
		st.close();

		return valueDouble;
	}

	/**
	 * does string have only numeric value
	 * 
	 * @param value
	 * @return
	 */
	protected static boolean isStringContainOnlyNumber(String value) {
		boolean isNumb = NumberUtils.isCreatable(value);
		return isNumb;
	}

	/**
	 * does string have int value
	 * 
	 * @param value
	 * @return
	 */
	protected static boolean isStringContainNumber(String value) {
		value = value.replaceAll("[^\\d.]", "");
		value = value.replace(".", "");
		if (StringUtils.isBlank(value))
			return false;
		return true;
	}

	/**
	 * remove surrounding double quotes from the string
	 * 
	 * @param value
	 * @return
	 */
	protected static String removeSurroundingQuotes(String value) {
		if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
			value = value.substring(1, value.length() - 1);
		}
		return value;
	}

	/**
	 * converts url string to url object
	 * 
	 * @param url
	 * @return
	 */
	protected static URL convertToUrl(String url) {
		URL aURL = null;

		try {
			aURL = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Helper.assertFalse(e.getMessage());
		}
		return aURL;
	}

	/**
	 * check if able to connect to source url
	 * 
	 * @param source
	 * @return
	 */
	public static boolean isUrlAbleToConnect(URL source, Proxy proxy) {
		try {
			HttpURLConnection con = null;
			HttpURLConnection.setFollowRedirects(false);
			if (proxy != null)
				con = (HttpURLConnection) source.openConnection(proxy);
			else
				con = (HttpURLConnection) source.openConnection();
			con.setConnectTimeout(1000);
			con.setReadTimeout(1000);
			con.setRequestMethod("HEAD");
			if (con.getResponseCode() < 400)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * checks if proxy is required first attempt without proxy, second with proxy,
	 * checking the validity of the proxy connection
	 * 
	 * @param source
	 * @return
	 */
	public static boolean setProxyAutoDetection(URL source) {

		Proxy proxy = null;

		String PROXY_ENABLED = TestObject.PROXY_ENABLED;
		String PROXY_AUTO_DETECT = TestObject.PROXY_AUTO_DETECT;
		String PROXY_HOST = TestObject.PROXY_HOST;
		String PROXY_PORT = TestObject.PROXY_PORT;
		String PROXY_USERNAME = TestObject.PROXY_USER;
		String PROXY_PASSWORD = TestObject.PROXY_PASS;

		String host = Config.getValue(PROXY_HOST);
		int port = Config.getIntValue(PROXY_PORT);
		String username = Config.getValue(PROXY_USERNAME);
		String password = Config.getValue(PROXY_PASSWORD);
		boolean isProxyAutoDetect = Config.getBooleanValue(PROXY_AUTO_DETECT);

		// return if auto detect proxy is disabled or already set
		if (!isProxyAutoDetect)
			return false;

		// return false if connection can be established without proxy
		boolean isValidConnection = isUrlAbleToConnect(source, null);
		if (isValidConnection) {
			Config.putValue(PROXY_ENABLED, false);
			return false;
		}

		// set username/password for proxy authenticator
		if (!username.isEmpty() && !password.isEmpty()) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});
		}

		// set and download through proxy if enabled
		if (!host.isEmpty() && port != -1)
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

		isValidConnection = isUrlAbleToConnect(source, proxy);

		// if connection was established using proxy, then return true
		if (isValidConnection) {
			TestLog.ConsoleLog("proxy detected, switching proxy on, host: " + host + " port: " + port);
			Config.putValue(PROXY_ENABLED, true);
			return true;
		} else
			Config.putValue(PROXY_ENABLED, false);
		return false;
	}
}