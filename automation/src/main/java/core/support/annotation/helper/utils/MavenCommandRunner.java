package core.support.annotation.helper.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import core.helpers.Helper;
import core.support.configReader.Config;
import net.lingala.zip4j.ZipFile;

public class MavenCommandRunner {
	
	public static String MAVEN_PATH = StringUtils.EMPTY;
	public static String MAVEN_URL = "https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip";
	public static String MAVEN_DOWNLOAD_DESTINATION = getRootDir()+ ".." + File.separator + "runner" + File.separator + "utils" + File.separator + "maven" + File.separator;
    private static final int DOWNLOAD_BUFFER = 16 * 1024;

	static String MAVEN_PROPERTY = "maven.home";
	static String MAVEN_URL_PROPERTY = "maven.url";

	/**
	 * process of setting maven:
	 * 1. set maven path from config, if exists
	 * 2. use mvn -version shell command to get maven path
	 * 3. if not available, download maven into runner/utils/maven folder
	 * 4. run maven through maven invoker
	 * 5. if maven invoker failed, run using shell command "mvn command"
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Root Path: " + getRootDir());
		
		// set maven path from config value: maven.home
		setMavenPathFromConfig();
		
		//set maven path using mvn -version command
		setMavenPath();
		
		// if no maven path found, download in utils folder
		downloadMavenIfNotExist();
		
		// run maven invoker. user maven home path
		boolean isSuccess = runMavenInvoker(args);
		
		// if not successful, run mvn command from shell
		if(!isSuccess)
			excuteCommand("mvn " + Arrays.toString(args).replaceAll("^.|.$", ""));
	}
	
	/**
	 * set maven path if set from config file
	 */
	public static void setMavenPathFromConfig() {
		String path = Config.getValue(MAVEN_PROPERTY);
		if(path.isEmpty()) return;
		
		File mavenFolderPath = new File(path);
		if(isFileInFolderPath(mavenFolderPath, "bin")) {
			MAVEN_PATH = path;
		}
	}
	
	
	/**
	 * download maven if path is not found
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static void downloadMavenIfNotExist() throws Exception {
		if (!MAVEN_PATH.isEmpty())
			return;

		File mavenDestinationPath = new File(MAVEN_DOWNLOAD_DESTINATION);

		if (!isMavenDownloaded(mavenDestinationPath)) {

			// use url from maven property if not set
			String urlProperty = Config.getValue(MAVEN_URL_PROPERTY);
			if (!urlProperty.isEmpty())
				MAVEN_URL = urlProperty;

			System.out.println("<<Downloading maven... " + MAVEN_URL + ">>");
			// delete folder first
			FileUtils.deleteDirectory(mavenDestinationPath);

			// create directory
			mavenDestinationPath.mkdir();
			// download
			String zipPath = mavenDestinationPath.getAbsolutePath() + File.separator + "download.zip";
			FileUtils.copyURLToFile(new URL(MAVEN_URL), new File(zipPath));
		
			
			copyURLToFile(new URL(MAVEN_URL), new File(zipPath));
			// unzip
			new ZipFile(zipPath).extractAll(MAVEN_DOWNLOAD_DESTINATION);
			FileUtils.forceDelete(new File(zipPath));
		}

		// set maven home path
		String mavenPath = MAVEN_DOWNLOAD_DESTINATION + getMavenDownloadHome(mavenDestinationPath);
		System.out.println("Setting maven path to: " + mavenPath);
		MAVEN_PATH = mavenPath;
	}
	
	
	public static void copyURLToFile(URL source, File destination) throws IOException {
		Proxy proxy = null;

		String host = Config.getValue("proxy.host");
		int port = Config.getIntValue("proxy.port");
		String username = Config.getValue("proxy.username");
		String password = Config.getValue("proxy.password");

		if (!username.isEmpty() && !password.isEmpty()) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});
		}

		if (!host.isEmpty() && port != -1)
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

		if (proxy == null) {
			FileUtils.copyURLToFile(source, destination);
		} else {
			downloadUsingProxy(source, destination, proxy);
		}
	}

    /**
     */
    private static void downloadUsingProxy(URL source, File destination, Proxy proxy) throws IOException {
        try(OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destination));
            InputStream inputStream = source.openConnection(proxy).getInputStream()) {

            byte[] buffer = new byte[DOWNLOAD_BUFFER];
            int len;
            while ((len = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, len);
            }
        }
    }
	
	/**
	 * gets maven downloaded folder name
	 * eg. apache-maven-3.6.2
	 * @param mavenDestinationPath
	 * @return
	 */
	private static String getMavenDownloadHome(File mavenDestinationPath) {
		String mavenHomePath = StringUtils.EMPTY;
		File[] fileList = mavenDestinationPath.listFiles();
		if (fileList.length == 0)
			return mavenHomePath;

		for (File file : fileList) {
			if (file.getName().toLowerCase().contains("maven"))
				return file.getName();
		}
		return mavenHomePath;
	}
	
	/**
	 * returns if maven has downloaded properly
	 * @param mavenDestinationPath
	 * @return
	 */
	private static boolean isMavenDownloaded(File mavenDestinationPath) {
		File[] fileList = mavenDestinationPath.listFiles();
		if (fileList == null || fileList.length == 0)
			return false;

		String mavenHome = getMavenDownloadHome(mavenDestinationPath);
		File mavenPath = new File(mavenDestinationPath.getAbsolutePath() + File.separator + mavenHome + File.separator
				+ "bin" + File.separator + "mvn");
		if (mavenPath.exists())
			return true;
		return false;
	}
	
	
	/**
	 * 	Maven home: /usr/local/Cellar/maven/3.6.2/libexec
	 * get path of maven from "mvn -version"
	 * @param results
	 * @return
	 */
	public static void setMavenPath() {
		
		// if maven path is set using config, skip
		if(!MAVEN_PATH.isEmpty()) return;
		
		ArrayList<String> results = Helper.executeCommand("mvn -version");
		System.out.println("maven -version results: " + results);
		
		String resultsString = Arrays.toString(results.toArray());

		if(results.isEmpty()) return;
			
		String[] resultArray = resultsString.split(",");
		for(String result : resultArray) {
			if(result.contains("Maven home:")) {
				MAVEN_PATH = result.split(":")[1].trim();
			}
		}
		System.out.println("maven path: " + MAVEN_PATH);
	}
	
	/**
	 * run command based on windows or mac/linux environment
	 * @param command
	 * @return
	 */
	protected static ArrayList<String> excuteCommand(String command){
    	System.out.println("<<executing maven command through command line>>");

	    ArrayList<String> results = new ArrayList<String>();

	    if(isMac() || isUnix()){
	    	results = runCommand(new String[] {"/bin/sh", "-c", command});
	    }else if(isWindows()){
	    	results = runCommand("cmd /c start " + command);
	    }
	    
	    return results;
	}
	
	/**
	 * run command using command line
	 * @param cmd
	 * @return
	 */
	private static ArrayList<String> runCommand(String... cmd) {
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
				System.out.println("shell command:  '" + cmd + "' output: " + e.getMessage());
			} finally {
				if (pr != null)
					pr.destroy();
			}
		} while (!success && retry > 0);
		if(results.isEmpty())
			System.out.println("shell command:  '" + Arrays.toString(cmd)  + "' did not return results. please check your path: ");
		return results;
	}
	
	/**
	 * returns true if OS is mac
	 * @return
	 */
	protected static boolean isMac() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.contains("mac");
	}
	
	/**
	 * returns true if OS is windows
	 * @return
	 */
	protected static boolean isWindows() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.contains("win");
	}
	
	/**
	 * returns true if OS is unix or linux
	 * @return
	 */
	protected static boolean isUnix() {
		String osName = System.getProperty("os.name");
		return (osName.indexOf("nix") >= 0 || osName.indexOf("linux") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0 );
	}
	
	/**
	 * run maven command through maven invoker
	 * requires maven home path
	 * @param args
	 * @return
	 */
	private static boolean runMavenInvoker(String[] args) {
		
		ArrayList<String> goals = new ArrayList<String>();

		for (int i = 0; i < args.length; i++) {
			goals.add(args[i]);
		}
		if (goals.isEmpty())
			goals.add("compile");

		InvocationRequest request = new DefaultInvocationRequest();
		String pomLocation = getRootDir() + "pom.xml";
		request.setPomFile(new File(pomLocation));
		request.setGoals(goals);

		Invoker invoker = new DefaultInvoker();

		// get maven home path (root path of maven)
		File mavenFile = verifyAndGetMavenHomePath();

		System.out.println("runMavenInvoker: " + MAVEN_PATH);
		invoker.setMavenHome(mavenFile);

		try {
			invoker.execute(request);
		} catch (MavenInvocationException e) {
			System.out.println("<<maven invoker has failed>>");
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	/**
	 * verify maven bin path exists in maven path or parent folder
	 * set maven path to the correct value
	 * @return
	 */
	public static File verifyAndGetMavenHomePath() {
		
		File mavenFolderPath = new File(MAVEN_PATH.trim());
		if(isFileInFolderPath(mavenFolderPath, "bin")) {
				return mavenFolderPath;
		}
		
		// check parent folder for bin folder. mvn -version returns maven path with inner folder
		mavenFolderPath = mavenFolderPath.getParentFile();
		if(isFileInFolderPath(mavenFolderPath, "bin")) {
			return mavenFolderPath;
		}
		
		MAVEN_PATH = mavenFolderPath.getAbsolutePath();
		return mavenFolderPath;
	}
	
	/**
	 * get current project root directory, where pom.xml is
	 * 
	 * @return
	 */
	public static String getRootDir() {	
		File currentWorkingDir = new File(".");
		File root = null;

		if(isFileInFolderPath(currentWorkingDir, "pom.xml"))
			root = currentWorkingDir;
		else if(isFileInFolderPath(new File("../"), "pom.xml")) {
			root = new File("../");
		}	
		return root.getAbsolutePath() + File.separator;
	}
	
	/**
	 * checks if maven is installed
	 * installed = if maven/bin folder exists
	 * @param folderPath
	 * @return
	 */
	private static boolean isFileInFolderPath(File folderPath, String exepctedFile) {
		
			File[] fileList = folderPath.listFiles();
			if(fileList == null) return false;
		
			for(File file: fileList) {
				if(file.getName().toLowerCase().contains(exepctedFile))
					return true;
			}
			return false;
	}

	
	public static void executeMavenCommandEmbedded() {
//		ArrayList<String> goals = new ArrayList<String>();
//
//		String root = new File(".").getAbsolutePath();
//		MavenCli cli = new MavenCli(new ClassWorld("maven",Thread.currentThread().getContextClassLoader()));
//		System.setProperty("maven.multiModuleProjectDirectory", root);
//
//	
//		if(goals.isEmpty()) goals.add("compile");
//
//		String[] goalsArays = goals.toArray(new String[goals.size()]);
//		cli.doMain(goalsArays, ".", System.out, System.err);
	}
	

}
