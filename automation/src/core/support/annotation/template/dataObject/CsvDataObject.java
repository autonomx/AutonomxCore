package core.support.annotation.template.dataObject;

import java.io.BufferedWriter;
import java.io.File;
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
		
		List<String[]> csvData = Helper.csv_getAllCsvData(file.getAbsolutePath());
		
		BufferedWriter bw = new BufferedWriter(fileObject.openWriter());

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  and Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + DATA_ROOT +"." + module + ";\n");
		bw.newLine();
		bw.newLine();
		bw.append("import org.apache.commons.lang3.StringUtils;");
		bw.newLine();
		bw.newLine();

		
		bw.append("public class " + csvName + " {" + "\n");
		bw.newLine();
		bw.newLine();

		
		// first row are columns names
		// private String username;
		// first column is id
		for(int i = 1; i<csvData.get(0).length; i++) {
			String column = csvData.get(0)[i];
			bw.append("private String " + column + " = StringUtils.EMPTY;\n" );
		}
		bw.newLine();
		bw.newLine();

		
		
//		public User withUsername(String username) {
//			this.username = username;
//			return this;
//		}
		// first column is id
		for(int i = 1; i<csvData.get(0).length; i++) {
			String column = csvData.get(0)[i];
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
		for(int i = 1; i<csvData.get(0).length; i++) {
			String column = csvData.get(0)[i];
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
		for(int rowIndex = 1; rowIndex < csvData.size(); rowIndex++) {
			String key = csvData.get(rowIndex)[0];
			bw.append("public " + csvName + " " + key +  "() {" + "\n");
			bw.append("    " + csvName + " " + csvName.toLowerCase() + " = new " + csvName + "()" + "\n");
			for(int columnIndex = 1; columnIndex < csvData.get(0).length; columnIndex++ ) {
				String column = StringUtils.capitalize(csvData.get(0)[columnIndex]);
				String value = csvData.get(rowIndex)[columnIndex];
				// replace keyword values . <_@Rand4>. same as service level tests
				value = DataHelper.replaceParameters(value); 
				bw.append("             .with" + column + "(\"" + value + "\")");
				
				// if last line
				if(columnIndex == csvData.get(0).length -1) {
					bw.append(";");
				}
				bw.append("\n");
			}
			bw.append("    return " + csvName.toLowerCase() + ";" +"\n");
			bw.append("}"+ "\n");
			bw.newLine();
			bw.newLine();
		}
		
		
		

		bw.append("}\n");

		bw.flush();
		bw.close();		
	}
}
