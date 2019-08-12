package core.support.annotation.helper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class dirChangeDetector {

	public static void main(String[] args) {
		
		// get src and keyword directory list, String format separated by ","
		String sourceDir = getRootDir() + "src" + File.separator + "main";
		String keywordDir = getRootDir() + "resources" + File.separator + "api";	
		
		
		System.out.println("sourceDir : " + sourceDir);
		System.out.println("keywordDir : " + keywordDir);

		String sourceList = getFileList(sourceDir);
		String keywordList = getFileList(keywordDir);
		String allNewFiles = sourceList + keywordList;
		
		
		String targetFile = getRootDir() + "target"+ File.separator +"generated-sources"+ File.separator +"src_dir.txt";
		String oldFileList = getFileContent(targetFile);
		
		boolean hasChanged = hasChangeDetected(oldFileList, allNewFiles);
		if(hasChanged) {
			String markerPath = getRootDir() + "target"+ File.separator +"generated-sources"+ File.separator + "annotations" + File.separator + "marker" + File.separator +  "marker.java";
			deleteFile(markerPath);
		}
		
	}
	
	private static boolean hasChangeDetected(String oldDir, String newDir) {
		boolean hasChanged = !oldDir.equals(newDir);
		if(hasChanged)
			System.out.println("changes in (src, or keyword) directory detected: \n");

		return hasChanged;
	}
	
	public static String getFileList(String directory) {
		ArrayList<String> fileList = getAllFiles(directory);
		String listString = String.join(",", fileList);
		return listString;
	}
	
	public static ArrayList<String> getAllFiles(String directory) {
		ArrayList<String> array = new ArrayList<String>();
		File file = new File(directory);
		array = getFileList(file, array);
		return array;
	}
	/**
	 * gets all files in a directory to get all files: File curDir = new File(".");
	 * getAllFiles(curDir);
	 * 
	 * @param curDir target directory
	 * @return the list of all files in given directory
	 */
	public static ArrayList<String> getFileList(File curDir, ArrayList<String> array) {
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
	
	protected static String getRootDir() {	
		String root = "";
		try {
			root = new File(dirChangeDetector.class.getProtectionDomain().getCodeSource().getLocation()
				    .toURI()).getParentFile().getParent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		};
		return root + File.separator;
	}
	
	protected static String getFileContent(String absolutePath) {
		String content = "";
		File file = new File(absolutePath);
		
		// return empty if file does not exist
		if(!file.exists())
			return content;
		
		try {
			content = new String(Files.readAllBytes(Paths.get(absolutePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	protected static void deleteFile(String absolutePath) {
		File file = new File(absolutePath);
		file.delete();
	}

}
