package core.support.annotation.template.manager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

import javax.tools.JavaFileObject;

import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;

public class sourceChangeDetector {
	
	public static final String SOURCE_CHANGE_DETECTOR_CLASS_NAME = "SourceChangeDetector";

	public static void writeModuleBaseClass() {
		try {
			writeSourceChangeDetectorClass();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * content from core.support.annotation.helper.utils.dirChangeDetector.java
	 * detects if src directory src/java/main changes. if true, delete annotation/marker/marker.java
	 *  file to trigger new annotaiton run in ant file
	 * @param panelMap
	 * @throws IOException
	 */
	private static void writeSourceChangeDetectorClass() throws IOException {
		Logger.debug("start generating source change detector class");

		// returns module.android.panel

		String sourceChangeePath = "marker." + SOURCE_CHANGE_DETECTOR_CLASS_NAME;
	
		// create file: module.appManager.java
		JavaFileObject file = FileCreatorHelper.createFileAbsolutePath(sourceChangeePath);
		BufferedWriter bw = new BufferedWriter(file.openWriter());

		/**
		 * package test.module;

			import core.uiCore.drivers.AbstractDriverTestNG;
			import moduleManager.AutonomxManager;
			
			public class ModuleBase extends AbstractDriverTestNG {
				protected AutonomxManager app = new AutonomxManager();
			}
		 */
		

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + "marker" +";\n");
		bw.newLine();
		bw.newLine();
		bw.append("import java.io.File;\n" + 
				"import java.io.IOException;\n" + 
				"import java.net.URISyntaxException;\n" + 
				"import java.nio.file.Files;\n" + 
				"import java.nio.file.Paths;\n" + 
				"import java.util.ArrayList;\n" + 
				"import java.util.Arrays;\n" + 
				"import java.util.List;\n" + 
				"\n" + 
				"import org.apache.commons.io.FileUtils;\n" + 
				"import org.apache.commons.lang.StringUtils;\n" + 
				"\n" + 
				"\n" + 
				"public class SourceChangeDetector {\n" + 
				"\n" + 
				"	public static String GENERATED_SOURCE_DIR = getRootDir() + \"target\" + File.separator + \"generated-sources\"\n" + 
				"			+ File.separator;\n" + 
				"\n" + 
				"	public static String SOURCE_DIR = getRootDir() + \"src\" + File.separator + \"main\";\n" + 
				"	public static String RESOURCE_DIR = getRootDir() + \"resources\" + File.separator;\n" + 
				"	public static String MARKER_FILE = GENERATED_SOURCE_DIR + \"annotations\" + File.separator + \"marker\" + File.separator + \"marker.java\";\n" + 
				"	public static String MAVEN_STATUS_DIR = getRootDir() + \"target\" + File.separator + \"maven-status\";\n" + 
				"\n" + 
				"	\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		hasSourceChanged();\n" + 
				"		System.out.println(\"<<<<<<<<< running test file change class >>>>>>>>>\");\n" + 
				"	}\n" + 
				"\n" + 
				"	public static boolean hasSourceChanged() {\n" + 
				"		\n" + 
				"		// has source directory changed\n" + 
				"		boolean hasSourceChanged = hasSourceDirChanged();\n" + 
				"\n" + 
				"		// is marker file available\n" + 
				"		boolean isMarker = new File(MARKER_FILE).exists();\n" + 
				"\n" + 
				"		// if change detected\n" + 
				"		if (hasSourceChanged || !isMarker) {\n" + 
				"			System.out.println(\"************ Changes detected, initiating new source generation ************\");\n" + 
				"\n" + 
				"			// delete maven status dir and marker (to indicated maven needs to generate new code)\n" + 
				"			deleteFile(MARKER_FILE);\n" + 
				"			deleteFile(MAVEN_STATUS_DIR);\n" + 
				"\n" + 
				"			return true;\n" + 
				"		}\n" + 
				"\n" + 
				"		return false;\n" + 
				"	}\n" + 
				"	\n" + 
				"	private static boolean hasSourceDirChanged() {\n" + 
				"		// get list of all files in src and resource directories\n" + 
				"		ArrayList<String> sourceListStringArray = getSourceFileList();\n" + 
				"\n" + 
				"		String targetFile = GENERATED_SOURCE_DIR + \"src_dir.txt\";\n" + 
				"		String oldFileList = StringUtils.EMPTY;\n" + 
				"		boolean hasSourceChanged = true;\n" + 
				"\n" + 
				"		if (new File(targetFile).exists()) {\n" + 
				"			oldFileList = getFileContent(targetFile);\n" + 
				"			ArrayList<String> oldDirList = new ArrayList<String>(Arrays.asList(oldFileList.split(\",\")));\n" + 
				"			hasSourceChanged = hasChangeDetected(oldDirList, sourceListStringArray);\n" + 
				"		}\n" + 
				"\n" + 
				"		return hasSourceChanged;\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * get list of all files in src and resource directories get src and resource\n" + 
				"	 * directory list, String format separated by \",\"\n" + 
				"	 * \n" + 
				"	 * @return\n" + 
				"	 */\n" + 
				"	private static ArrayList<String> getSourceFileList() {\n" + 
				"\n" + 
				"		System.out.println(\"sourceDir : \" + SOURCE_DIR);\n" + 
				"		System.out.println(\"keywordDir : \" + RESOURCE_DIR);\n" + 
				"\n" + 
				"		// get all files including sub directories\n" + 
				"		ArrayList<String> sourceList = getFileList(SOURCE_DIR);\n" + 
				"		ArrayList<String> keywordList = getFileList(RESOURCE_DIR);\n" + 
				"		sourceList.addAll(keywordList);\n" + 
				"		ArrayList<String> sourceListStringArray = getFileString(sourceList);\n" + 
				"\n" + 
				"		return sourceListStringArray;\n" + 
				"	}\n" + 
				"\n" + 
				"	private static boolean hasChangeDetected(ArrayList<String> oldDirList, ArrayList<String> newDirList) {\n" + 
				"		String oldDir = String.join(\", \", oldDirList);\n" + 
				"		String newDir = String.join(\", \", newDirList);\n" + 
				"		List<String> differenceList = listDifference(oldDirList, newDirList);\n" + 
				"		boolean hasChanged = !oldDir.equals(newDir);\n" + 
				"\n" + 
				"		if (hasChanged) {\n" + 
				"			System.out.println(\"changes in (src, or keyword) directory detected: \\n\" + \" changes: \");\n" + 
				"			differenceList.forEach(System.out::println);\n" + 
				"		}\n" + 
				"\n" + 
				"		return hasChanged;\n" + 
				"	}\n" + 
				"\n" + 
				"	private static ArrayList<String> listDifference(ArrayList<String> oldDirList, ArrayList<String> newDirList) {\n" + 
				"		List<String> newList = new ArrayList<>(newDirList);\n" + 
				"		newDirList.removeAll(oldDirList);\n" + 
				"		oldDirList.removeAll(newList);\n" + 
				"\n" + 
				"		newDirList.addAll(oldDirList);\n" + 
				"\n" + 
				"		return newDirList;\n" + 
				"	}\n" + 
				"\n" + 
				"	private static ArrayList<String> getFileList(String directory) {\n" + 
				"		ArrayList<String> array = new ArrayList<String>();\n" + 
				"		File file = new File(directory);\n" + 
				"		array = getFileList(file, array);\n" + 
				"		return array;\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * convert the file list to a string with absolute file names, separated by \",\"\n" + 
				"	 * \n" + 
				"	 * @param list\n" + 
				"	 * @return\n" + 
				"	 */\n" + 
				"	private static ArrayList<String> getFileString(ArrayList<String> list) {\n" + 
				"		ArrayList<String> dirList = new ArrayList<String>();\n" + 
				"\n" + 
				"		for (String file : list) {\n" + 
				"			dirList.add(file);\n" + 
				"		}\n" + 
				"		return dirList;\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * gets all files in a directory to get all files: File curDir = new File(\".\");\n" + 
				"	 * getAllFiles(curDir);\n" + 
				"	 * \n" + 
				"	 * @param curDir target directory\n" + 
				"	 * @return the list of all files in given directory\n" + 
				"	 */\n" + 
				"	private static ArrayList<String> getFileList(File curDir, ArrayList<String> array) {\n" + 
				"		File[] filesList = curDir.listFiles();\n" + 
				"		for (File f : filesList) {\n" + 
				"			if (f.isDirectory())\n" + 
				"				getFileList(f, array);\n" + 
				"			if (f.isFile()) {\n" + 
				"				array.add(f.getPath());\n" + 
				"			}\n" + 
				"		}\n" + 
				"		return array;\n" + 
				"	}\n" + 
				"\n" + 
				"	private static String getRootDir() {\n" + 
				"		String root = \"\";\n" + 
				"		try {\n" + 
				"			root = new File(SourceChangeDetector.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile()\n" + 
				"					.getParent();\n" + 
				"		} catch (URISyntaxException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"		return root + File.separator;\n" + 
				"	}\n" + 
				"\n" + 
				"	private static String getFileContent(String absolutePath) {\n" + 
				"		String content = \"\";\n" + 
				"		File file = new File(absolutePath);\n" + 
				"\n" + 
				"		// return empty if file does not exist\n" + 
				"		if (!file.exists())\n" + 
				"			return content;\n" + 
				"\n" + 
				"		try {\n" + 
				"			content = new String(Files.readAllBytes(Paths.get(absolutePath)));\n" + 
				"		} catch (IOException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"\n" + 
				"		return content;\n" + 
				"	}\n" + 
				"\n" + 
				"	private static void deleteFile(String absolutePath) {\n" + 
				"		File file = new File(absolutePath);\n" + 
				"		if(file.isDirectory())\n" + 
				"			deleteDirectory(absolutePath);\n" + 
				"		else\n" + 
				"			deleteSingleFile(absolutePath);\n" + 
				"	}\n" + 
				"\n" + 
				"	private static void deleteDirectory(String absolutePath) {\n" + 
				"		File file = new File(absolutePath);\n" + 
				"		try {\n" + 
				"			FileUtils.deleteDirectory(file);\n" + 
				"		} catch (IOException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"	private static void deleteSingleFile(String absolutePath) {\n" + 
				"		File file = new File(absolutePath);\n" + 
				"		file.delete();\n" + 
				"	}\n" + 
				"}");
		
		
		bw.flush();
		bw.close();

		Logger.debug("complete generating module base class");
	}
}