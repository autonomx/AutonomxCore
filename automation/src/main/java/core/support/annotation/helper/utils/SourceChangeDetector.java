package core.support.annotation.helper.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;


public class SourceChangeDetector {

	public static String GENERATED_SOURCE_DIR = getRootDir() + "target" + File.separator + "generated-sources"
			+ File.separator;

	public static String SOURCE_DIR = getRootDir() + "src" + File.separator + "main";
	public static String RESOURCE_DIR = getRootDir() + "resources" + File.separator;
	public static String MARKER_FILE = GENERATED_SOURCE_DIR + "annotations" + File.separator + "marker" + File.separator + "marker.java";
	public static String MAVEN_STATUS_DIR = getRootDir() + "target" + File.separator + "maven-status";

	
	
	public static void main(String[] args) {
		hasSourceChanged();
		System.out.println("<<<<<<<<< running test file change class >>>>>>>>>");
	}

	public static boolean hasSourceChanged() {
		
		// has source directory changed
		boolean hasSourceChanged = hasSourceDirChanged();

		// is marker file available
		boolean isMarker = new File(MARKER_FILE).exists();

		// if change detected
		if (hasSourceChanged || !isMarker) {
			System.out.println("************ Changes detected, initiating new source generation ************");

			// delete maven status dir and marker (to indicated maven needs to generate new code)
			deleteFile(MARKER_FILE);
			deleteFile(MAVEN_STATUS_DIR);

			return true;
		}

		return false;
	}
	
	private static boolean hasSourceDirChanged() {
		// get list of all files in src and resource directories
		ArrayList<String> sourceListStringArray = getSourceFileList();

		String targetFile = GENERATED_SOURCE_DIR + "src_dir.txt";
		String oldFileList = StringUtils.EMPTY;
		boolean hasSourceChanged = true;

		if (new File(targetFile).exists()) {
			oldFileList = getFileContent(targetFile);
			ArrayList<String> oldDirList = new ArrayList<String>(Arrays.asList(oldFileList.split(",")));
			hasSourceChanged = hasChangeDetected(oldDirList, sourceListStringArray);
		}

		return hasSourceChanged;
	}

	/**
	 * get list of all files in src and resource directories get src and resource
	 * directory list, String format separated by ","
	 * 
	 * @return
	 */
	private static ArrayList<String> getSourceFileList() {

		System.out.println("sourceDir : " + SOURCE_DIR);
		System.out.println("keywordDir : " + RESOURCE_DIR);

		// get all files including sub directories
		ArrayList<String> sourceList = getFileList(SOURCE_DIR);
		ArrayList<String> keywordList = getFileList(RESOURCE_DIR);
		sourceList.addAll(keywordList);
		ArrayList<String> sourceListStringArray = getFileString(sourceList);

		return sourceListStringArray;
	}

	private static boolean hasChangeDetected(ArrayList<String> oldDirList, ArrayList<String> newDirList) {
		String oldDir = String.join(", ", oldDirList);
		String newDir = String.join(", ", newDirList);
		List<String> differenceList = listDifference(oldDirList, newDirList);
		boolean hasChanged = !oldDir.equals(newDir);

		if (hasChanged) {
			System.out.println("changes in (src, or keyword) directory detected: \n" + " changes: ");
			differenceList.forEach(System.out::println);
		}

		return hasChanged;
	}

	private static ArrayList<String> listDifference(ArrayList<String> oldDirList, ArrayList<String> newDirList) {
		List<String> newList = new ArrayList<>(newDirList);
		newDirList.removeAll(oldDirList);
		oldDirList.removeAll(newList);

		newDirList.addAll(oldDirList);

		return newDirList;
	}

	private static ArrayList<String> getFileList(String directory) {
		ArrayList<String> array = new ArrayList<String>();
		File file = new File(directory);
		array = getFileList(file, array);
		return array;
	}

	/**
	 * convert the file list to a string with absolute file names, separated by ","
	 * 
	 * @param list
	 * @return
	 */
	private static ArrayList<String> getFileString(ArrayList<String> list) {
		ArrayList<String> dirList = new ArrayList<String>();

		for (String file : list) {
			dirList.add(file);
		}
		return dirList;
	}

	/**
	 * gets all files in a directory to get all files: File curDir = new File(".");
	 * getAllFiles(curDir);
	 * 
	 * @param curDir target directory
	 * @return the list of all files in given directory
	 */
	private static ArrayList<String> getFileList(File curDir, ArrayList<String> array) {
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory())
				getFileList(f, array);
			if (f.isFile()) {
				array.add(f.getPath());
			}
		}
		return array;
	}

	private static String getRootDir() {
		String root = "";
		try {
			root = new File(SourceChangeDetector.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile()
					.getParent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return root + File.separator;
	}

	private static String getFileContent(String absolutePath) {
		String content = "";
		File file = new File(absolutePath);

		// return empty if file does not exist
		if (!file.exists())
			return content;

		try {
			content = new String(Files.readAllBytes(Paths.get(absolutePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}

	private static void deleteFile(String absolutePath) {
		File file = new File(absolutePath);
		if(file.isDirectory())
			deleteDirectory(absolutePath);
		else
			deleteSingleFile(absolutePath);
	}

	private static void deleteDirectory(String absolutePath) {
		File file = new File(absolutePath);
		try {
			FileUtils.deleteDirectory(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void deleteSingleFile(String absolutePath) {
		File file = new File(absolutePath);
		file.delete();
	}
}