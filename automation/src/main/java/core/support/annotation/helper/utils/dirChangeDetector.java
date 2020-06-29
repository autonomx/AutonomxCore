package core.support.annotation.helper.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.helpers.Helper;
import core.support.configReader.PropertiesReader;

public class dirChangeDetector {

	 public static String GENERATED_SOURCE_DIR = Helper.getRootDir() + "target" + File.separator + "generated-sources" + File.separator;
	
	 public static  String SOURCE_DIR = Helper.getRootDir() + "src" + File.separator + "main";
	 public static	String RESOURCE_DIR = PropertiesReader.getLocalResourcePath();
	 /**
	 * detects changes in directories: files added, removed, name changed
	 * 
	 * @param args
	 * @return 
	 */
	public static boolean hasSourceChanged() {

		// get list of all files in src and resource directories
		ArrayList<String> sourceListStringArray =  getSourceFileList();

		String targetFile = GENERATED_SOURCE_DIR + "src_dir.txt";
		String oldFileList = Helper.getFileContent(targetFile, false);
		ArrayList<String> oldDirList = new ArrayList<String>(Arrays.asList(oldFileList.split(",")));

		boolean hasSourceChanged = hasChangeDetected(oldDirList, sourceListStringArray);
		
		String marker = GENERATED_SOURCE_DIR + "annotations" + File.separator + "marker" + File.separator + "marker.java";

		// do we have generated source directory
		boolean isMarkerExists = new File(marker).exists();
		
		// if change detected
		if (hasSourceChanged || !isMarkerExists) {
			String annotationsDir = GENERATED_SOURCE_DIR + "annotations";
			
			System.out.println("************ Changes detected, initiating new source generation ************");
			String mavenStatusDir = Helper.getRootDir() + "target" + File.separator + "maven-status";
			
			// delete generated sources and maven status dir (to indicated maven needs to generate new code)
			Helper.deleteFile(annotationsDir);
			Helper.deleteFile(mavenStatusDir);

			return true;
		}
		
		System.out.println("************ No changes in source and resource files detected ************");

		return false;
	}
	
	/**
	 * get list of all files in src and resource directories
	 * get src and resource directory list, String format separated by ","

	 * @return
	 */
	private static ArrayList<String> getSourceFileList() {

		System.out.println("sourceDir : " + SOURCE_DIR);
		System.out.println("keywordDir : " + RESOURCE_DIR);

		// get all files including sub directories
		ArrayList<File> sourceList = Helper.getFileList(SOURCE_DIR, true);
		ArrayList<File> keywordList = Helper.getFileList(RESOURCE_DIR, true);
		sourceList.addAll(keywordList);
		ArrayList<String> sourceListStringArray =  getFileString(sourceList);
		
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
	
	/**
	 * convert the file list to a string with absolute file names, separated by ","
	 * @param list
	 * @return
	 */
	private static ArrayList<String> getFileString(ArrayList<File> list) {
		ArrayList<String> dirList = new ArrayList<String>();
		
		for(File file : list) {
			dirList.add(file.getAbsolutePath()); 
		}
		return dirList;
	}

	private static ArrayList<String> listDifference(ArrayList<String> oldDirList, ArrayList<String> newDirList) {
		List<String> newList = new ArrayList<>(newDirList);
		newDirList.removeAll(oldDirList);
		oldDirList.removeAll(newList);

		newDirList.addAll(oldDirList);

		return newDirList;
	}
}