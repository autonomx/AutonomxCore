package core.support.annotation.template.dataObject;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;

import core.apiCore.helpers.DataHelper;
import core.helpers.Helper;
import core.support.annotation.helper.DataObjectHelper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.PackageHelper;

public class CsvDataObject {
	
	public static JavaFileObject CSV_File_Object = null;
	public static String MODULE_ROOT = "module";
	public static String DATA_ROOT = "data";
	public static String ID_COLUMN = "@id";
	
	  public static void writeCsvDataClass() throws Exception {
		  List<File> files = DataObjectHelper.getAllCsvDataFiles(); 

		  // return if no data files
		  if(files.isEmpty()) return;
		  
		  writeCsvObjectClasses(files);  
	  }

	public static void writeCsvObjectClasses(List<File> files) throws Exception {
		
		for( File file : files) {
			writeCsvObjectClass(file);	
		}
	}
	
	/**
package module.webApp.data;

import core.helpers.Helper;

public class User {
	
	private String username;
	private String password;
	
	public User withUserName(String username) {
		this.username = username;
		return this;
	}
	
	public User withPassword(String password) {
		this.password = password;
		return this;
	}
	
	public String getUserName() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public User admin() {
		User user = new User().withUserName("admin" + Helper.generateRandomString(4)).withPassword("password1");
		return user;
	}
	
	@DataProvider(name = "DataRunner")
	public synchronized Iterator<String[]> dataProvider() {
		List<String[]> testCases = Helper.csv_getAllCsvDataFirstRowAsHeader(PropertiesReader.getLocalRootPath() + "⁨src/main/java/module/webApp/data/User.csv");
		return testCases.iterator();		
	}
}
	 * @param file
	 * @throws Exception 
	 */
	
	public static void writeCsvObjectClass(File file) throws Exception {
		String module = PackageHelper.getModuleFromFullPath(file);

		// create file: data.webApp.user.java
		String csvName =  file.getName().replaceFirst("[.][^.]+$", "");
		String filePath = PackageHelper.DATA_PATH + "." + module + "." + csvName;
		JavaFileObject fileObject = FileCreatorHelper.createFileAbsolutePath(filePath);
		
		List<String[]> csvDataWithHeader = Helper.csv_getAllCsvData(file.getAbsolutePath());
		
		boolean hasIdColumn = hasIdColumn(csvDataWithHeader);
		List<String[]> csvDataOnly = Helper.csv_getAllCsvDataFirstRowAsHeader(file.getAbsolutePath());
		
		BufferedWriter bw = new BufferedWriter(fileObject.openWriter());

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + DATA_ROOT +"." + module + ";\n");
		bw.newLine();
		bw.newLine();
		bw.append("import org.apache.commons.lang3.StringUtils;"+ "\n");
		bw.append("import core.support.objects.TestObject;" + "\n");
		bw.append("import org.testng.annotations.DataProvider;"+ "\n");
		bw.newLine();
		bw.newLine();
		
		bw.append("public class " + csvName + " {" + "\n");
		bw.newLine();
		bw.newLine();

		// if id column exists, start with index 1, else start with index 0
		int firstIndex = 0;
		if(hasIdColumn) 
			firstIndex = 1;

		// first row are columns names
		// private String username;
		// first column is id
		for(int i = firstIndex; i<csvDataWithHeader.get(0).length; i++) {
			String column = csvDataWithHeader.get(0)[i];
			bw.append("private String " + column + " = StringUtils.EMPTY;\n" );
		}
		bw.newLine();
		bw.newLine();

		
		
//		public User withUsername(String username) {
//			this.username = username;
//			return this;
//		}
		// first column is id
		for(int i = firstIndex; i<csvDataWithHeader.get(0).length; i++) {
			String column = csvDataWithHeader.get(0)[i];
			bw.append("public " + csvName + " with" + StringUtils.capitalize(column) + "(String " +column + ") {\n" );
			bw.append("    this." + column + " = " + column + ";" + "\n");
			bw.append("    return this;" + "\n");
			bw.append("}" + "\n");
			bw.newLine();
			bw.newLine();
		}
		
		
//		public String getUsername() {
//			return username;
//		}
		// first column is id
		for(int i = firstIndex; i<csvDataWithHeader.get(0).length; i++) {
			String column = csvDataWithHeader.get(0)[i];
			bw.append("public String get" + StringUtils.capitalize(column) + "() {" + "\n");
			bw.append("    return " + column + ";" + "\n");
			bw.append("}"+ "\n");
			bw.newLine();
			bw.newLine();
		}
		
		
//		public User admin() {
//			User user = new User()
//				.withUserName("admin1");
//				.withPassword("password1");
//			return user;
//		}
		for(int rowIndex = 1; rowIndex < csvDataWithHeader.size(); rowIndex++) {
			String key = csvDataWithHeader.get(rowIndex)[0];
			bw.append("public " + csvName + " " + key +  "() {" + "\n");
			bw.append("    " + csvName + " " + csvName.toLowerCase() + " = new " + csvName + "()" + "\n");
			for(int columnIndex = firstIndex; columnIndex < csvDataWithHeader.get(0).length; columnIndex++ ) {
				String column = StringUtils.capitalize(csvDataWithHeader.get(0)[columnIndex]);
				String value = csvDataWithHeader.get(rowIndex)[columnIndex];
				// replace keyword values . <_@Rand4>. same as service level tests
				value = DataHelper.replaceParameters(value); 
				bw.append("             .with" + column + "(\"" + value + "\")");
				
				// if last line
				if(columnIndex == csvDataWithHeader.get(0).length -1) {
					bw.append(";");
				}
				bw.append("\n");
			}
			bw.append("    return " + csvName.toLowerCase() + ";" +"\n");
			bw.append("}"+ "\n");
			bw.newLine();
			bw.newLine();
		}
		
//		public synchronized Object[][] dataProvider() {
//			 return new Object[][] {
//							 		{ "testuser_1", "Test@123" },
//				 					 { "testuser_1", "Test@123" }
//								   };	 
//		}
		bw.append("@DataProvider(name = \"DataRunner\")" +"\n");
		bw.append("public synchronized Object[][] dataProvider() {"+"\n");
		
		bw.append("    return new Object[][] {	" +"\n");
		for(int rowIndex = 0; rowIndex < csvDataOnly.size(); rowIndex++) {
		    List<String> rowList = Arrays.asList(csvDataOnly.get(rowIndex)); 
		    
			String step1 = StringUtils.join(rowList, "\", \"");// Join with ", "
			String rowString = StringUtils.wrap(step1, "\"");// Wrap step1 with "
			
		    // if has id column, remove it
		    if(hasIdColumn) 
		    	rowString = removeFirstColumn(rowString);
		    
		    // replace parameters
		    rowString = DataHelper.replaceParameters(rowString); 

			bw.append(" 	{ " + rowString + " }" );
			
			// add comma at the end of each row
			if(rowIndex < csvDataOnly.size() -1) {
				bw.append("," + "\n" );
			}else
				bw.append("\n");
		}
		bw.append("   };" + "\n");

		
		
		bw.append("}" + "\n");
		bw.newLine();
		bw.newLine();
		
		bw.append("}\n");

		bw.flush();
		bw.close();		
	}
	
	/**
	 * removes the first column in the row. this is for the id colum that needs to be removed from data provider
	 * @param rowList
	 * @return
	 */
	private static String removeFirstColumn(String row){
			return row.substring(row.indexOf(",")+1, row.length());  
	}
	
	/**
	 * returns true if csv file has id column
	 * @param csvDataWithHeader
	 * @return
	 */
	private static boolean hasIdColumn(List<String[]> csvDataWithHeader) {
		boolean hasIdColumn = false;
		String idColumn = csvDataWithHeader.get(0)[0];
		if(idColumn.equals(ID_COLUMN)) {
			hasIdColumn = true; 
		}
		return hasIdColumn;
	}
}
