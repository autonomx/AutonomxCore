package core.support.annotation.template.dataObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.opencsv.CSVReader;

import core.apiCore.helpers.DataHelper;
import core.helpers.Helper;
import core.support.annotation.helper.DataObjectHelper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;
import data.webApp.User;

public class CsvDataObject {
	
	public static JavaFileObject CSV_File_Object = null;
	public static String MODULE_ROOT = "module";
	public static String DATA_ROOT = "data";
	public static String ID_COLUMN = "@id";
	
	public static void writeCsvDataClass() {
		try {
			writeCsvDataClassImplementation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeCsvDataClassImplementation() throws Exception {
		Logger.debug("<<<< start generating data object classes >>>>");

		List<File> files = DataObjectHelper.getAllCsvDataFiles();

		Logger.debug("csv data class count:  " + files.size());

		// return if no data files
		if (files.isEmpty())
			return;

		writeCsvObjectClasses(files);
		
		Logger.debug("<<<< completed generating data object classes >>>>");

	}

	private static void writeCsvObjectClasses(List<File> files) throws Exception {
		
		for( File file : files) {
			Logger.debug("writing csv data object:  " + file.getName());
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
		User login = new User().withUserName("admin" + Helper.generateRandomString(4)).withPassword("password1");
		return login;
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
	
	private static void writeCsvObjectClass(File file) throws Exception {
		String module = PackageHelper.getModuleFromFullPath(file);

		// create file: data.webApp.user.java
		String csvName =  file.getName().replaceFirst("[.][^.]+$", "");
		String filePath = PackageHelper.DATA_PATH + "." + module + "." + csvName;
		JavaFileObject fileObject = FileCreatorHelper.createFileAbsolutePath(filePath);
		
		List<String[]> csvDataWithHeader = Helper.csv.getAllCsvData(file.getAbsolutePath());
		
		boolean hasIdColumn = hasIdColumn(csvDataWithHeader);
		List<String[]> csvDataOnly = Helper.csv.getAllCsvDataFirstRowAsHeader(file.getAbsolutePath());
		
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
		bw.append("import org.testng.annotations.DataProvider;"+ "\n");
		bw.append("import core.helpers.Helper;"+ "\n");
		bw.append("import core.helpers.csvHelper.CsvObject;"+ "\n");
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
		bw.append("private String id = StringUtils.EMPTY;\n" );
		bw.newLine();
		bw.newLine();

		
		
//		public User withUsername(String username) {
//			this.username = username;
//			return this;
//		}
		// first column is id
		for(int i = firstIndex; i<csvDataWithHeader.get(0).length; i++) {
			String column = csvDataWithHeader.get(0)[i].trim();
			bw.append("	public " + csvName + " with" + StringUtils.capitalize(column) + "(String " +column + ") {\n" );
			bw.append("    	this." + column + " = " + column + ";" + "\n");
			bw.append("    	return this;" + "\n");
			bw.append("}" + "\n");
			bw.newLine();
			bw.newLine();
		}
		
		
//		public String getUsername() {
//			return username;
//		}
		// first column is id
		for(int i = firstIndex; i<csvDataWithHeader.get(0).length; i++) {
			String column = csvDataWithHeader.get(0)[i].trim();
			bw.append("	public String get" + StringUtils.capitalize(column) + "() {" + "\n");
			bw.append("    	return " + column + ";" + "\n");
			bw.append("}"+ "\n");
			bw.newLine();
			bw.newLine();
		}
		
		
		/*
		 * 	public User admin() {
		 	  User user = new User();
			  user.username = "autoAdmin1";
			  user.password = "autoPass1";
			  user.name = "dave";
			  user.id = "admin";
		    return user;
		}
		 */
		for(int rowIndex = 1; rowIndex < csvDataWithHeader.size(); rowIndex++) {
			String key = updateForDuplicateIds(csvDataWithHeader).get(rowIndex - 1).trim();
			key = normalizeMethodName(key);
			
			bw.append("	public " + csvName + " " + key +  "() {" + "\n");
			bw.append(" 	  " + csvName + " " + csvName.toLowerCase() + " = new " + csvName + "();" + "\n");
			for(int columnIndex = firstIndex; columnIndex < csvDataWithHeader.get(0).length; columnIndex++ ) {
				String column = csvDataWithHeader.get(0)[columnIndex].trim();
				////////////TODO fix if null
				String value = "";
				value = csvDataWithHeader.get(rowIndex)[columnIndex];
				
				// replace keyword values . <_@Rand4>. same as service level tests
				value = DataHelper.replaceParameters(value); 
				bw.append("	  " + csvName.toLowerCase() +"." + column + " = \"" + value + "\";" + "\n");
			}
			
			// if '@id' field does not exist, set id as empty
			String idValue = StringUtils.EMPTY;
			if(hasIdColumn ) idValue = key;
			
			bw.append("	  " + csvName.toLowerCase() +".id = \"" + idValue + "\";" + "\n");
			bw.append("    return " + csvName.toLowerCase() + ";" +"\n");
			bw.append("}"+ "\n");
			bw.newLine();
			bw.newLine();
		}
		
		
		
		/*
		// update value in csv file
		public synchronized void updateRate( String rate) {
	
			Helper.assertTrue("id cannot be empty, select row id. eg. Data.webApp.users().admin().updateName('bob'); " , !this.id.isEmpty());
	
			String path = "/Users/Shared/Jenkins/Documents/Selenium/mining/mining/automation/src/main/java/module/webApp/data";
			String fileName = "MinerList.csv";
			String value = rate;
			String[] valueArray = value.split(",");
			int columnIndex = Helper.csv.getColumnIndex("rate", path + "/" + fileName);
			int rowIndex = Helper.csv.getRowIndex(this.id, path + "/" + fileName );
			CsvObject csv = new CsvObject().withCsvPath(path).withCsvFile("MinerList.csv").withValue(valueArray).withRow(rowIndex).withColumn(columnIndex);
			Helper.csv.writeToCell(csv, rate, path + "/" + fileName );
		}
		 */
		
		// list of columns, excluding '@id' column
		List<String> headers = getColumnListFromCsv(file);

		for(int i = firstIndex; i < headers.size(); i++) {
			String column = headers.get(i).trim();
			bw.append("// update value in csv file" +"\n");
			bw.append("	public synchronized void update"+ StringUtils.capitalize(column) +"( String "+ column +") {" +"\n" +"\n");
			bw.append("		Helper.assertTrue(\"id cannot be empty, select row id. eg. Data.webApp.users().admin().updateName('bob'); \" , !this.id.isEmpty());" + "\n" + "\n");
			bw.append("		String path = \"" + file.getParent() +"\";" + "\n");
			bw.append("		String fileName = \"" + file.getName() +"\";" + "\n");
			bw.append("		String value = " + column + ";" +"\n");
			bw.append("		String[] valueArray = value.split(\",\");" +"\n");
			bw.append("		int columnIndex = Helper.csv.getColumnIndex(\""+ column +"\", path + \"/\" + fileName);" +"\n");
			bw.append("		int rowIndex = Helper.csv.getRowIndex(this.id, path + \"/\" + fileName );" +"\n");
			bw.append("		CsvObject csv = new CsvObject().withCsvPath(path).withCsvFile(\"" + file.getName() + "\").withValue(valueArray).withRow(rowIndex).withColumn(columnIndex);" + "\n");
			bw.append("		Helper.csv.writeToCell(csv, "+ column +", path + \"/\" + fileName );" +"\n");
			bw.append("	}\n ");
			bw.newLine();
		}
		
		bw.newLine();
		bw.newLine();
		
		
		
//		public synchronized Object[][] dataProvider() {
//			 return new Object[][] {
//							 		{ "testuser_1", "Test@123" },
//				 					{ "testuser_2", "Test@124" }
//								   };	 
//		}
		bw.append("	@DataProvider(name = \"DataRunner\", parallel = true)" +"\n");
		bw.append("	public synchronized Object[][] dataProvider() {"+"\n");
		
		bw.append("    return new Object[][] {	" +"\n");
		
		// add missing values if applicable
		csvDataOnly = normalizeRows(file);
		
		for(int rowIndex = 0; rowIndex < csvDataOnly.size(); rowIndex++) {
		    List<String> rowList = Arrays.asList(csvDataOnly.get(rowIndex)); 
		    		    
			String step1 = StringUtils.join(rowList, "\", \"");// Join with ", "
			String rowString = "";
			rowString = StringUtils.wrap(step1, "\"");// Wrap step1 with "
			
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
		
		/**
		 * public void addRow(String type, String hashrate) {
			String path = Helper.getRootDir() + "src" + File.separator + "main" + File.separator + "java" + File.separator + "module" + File.separator + "webApp" + File.separator + "data" + File.separator; 
			
			String[] value = "type,hashrate".split(",");
			CsvObject csv = new CsvObject().withCsvPath(path).withCsvFile("MinerList.csv").withValue(value);	
			Helper.csv.writeAddRow(csv);	
		}
		 */
		
		String headerParameters = getMethodParametersFromColumnHeaders(headers);
		bw.append("	public void addRow("+ headerParameters +") {" +"\n" +"\n");
		bw.append("		String path = \"" + file.getParent() +"\";" + "\n");
		bw.append("		String value = " + String.join(" + \",\" +", headers) + ";" +"\n");
		bw.append("		String[] valueArray = value.split(\",\");" +"\n");
		bw.append("		CsvObject csv = new CsvObject().withCsvPath(path).withCsvFile(\"" + file.getName() + "\").withValue(valueArray);" + "\n");
		bw.append("		Helper.csv.AddRow(csv);" +"\n");
		bw.append("	}\n");
		bw.append("}\n");

		bw.flush();
		bw.close();		
	}
	
	/**
	 * replaces duplicate ids with id + index
	 * allows for data with duplicate ids to compile
	 * user should replace duplicates with unique values
	 * @param csvDataWithHeader
	 * @return
	 */
	private static List<String> updateForDuplicateIds(List<String[]> csvDataWithHeader) {
		List<String> idList = new ArrayList<String>();
		int index = 1;
		// get list of ids
		for(int rowIndex = 1; rowIndex < csvDataWithHeader.size(); rowIndex++) {
			idList.add(csvDataWithHeader.get(rowIndex)[0]);
		}
		
		for (int i = 0; i < idList.size(); i++) {
		    if (Collections.frequency(idList, idList.get(i)) > 1) {
		        String updatedVal = idList.get(i) + "_duplidateReplaceWithUniqueID_" +  index++;
		        idList.set(i, updatedVal);
		    }
		}
		return idList; 
	}
	
	/**
	 * updates the method name to remove illegal characters 
	 * @param key
	 * @return
	 */
	private static String normalizeMethodName(String key) {
		// remove "-" , "."
		key = key.replace("-", "").replaceAll("\\.", "");
		
		if (NumberUtils.isDigits(key)) {
		    key = "method" + key; 
		}
		
		return key;
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
	
	/**
	 * updates the rows to add missing values if missing
	 * eg. @id, name, password
	 *          bob   -> becomes bob, ""
	 * @return 
	 */
	private static List<String[]> normalizeRows(File file) {
		List<String[]> csvDataOnly = Helper.csv.getAllCsvDataFirstRowAsHeader(file.getAbsolutePath());
		List<String[]> csvDataWithHeader = Helper.csv.getAllCsvData(file.getAbsolutePath());
		List<String[]> updatedList = new ArrayList<String[]>();
		
		int columnCount = csvDataWithHeader.get(0).length;
		for(String[] row : csvDataOnly){
			
			
			if(row.length < columnCount) {
				List<String> list = new ArrayList<String>();
				list.addAll(Arrays.asList(row));
				for(int i = 0; i < columnCount - row.length; i++) {
					list.add("");
				}
				row = list.toArray(new String[list.size()]);
			}
			updatedList.add(row);
		}
		return updatedList;
	}
	
	/**
	 * gets list of column headers
	 * replaces '@id' if exists with id
	 * @param file
	 * @return arraylist of column headers
	 */
	private static List<String> getColumnListFromCsv(File file){
		CSVReader reader;
		List<String> headerList = new ArrayList<String>();
		try {
			reader = new CSVReader(new FileReader(file.getAbsoluteFile()));
			// if the first line is the header
			headerList = Arrays.asList(reader.readNext());
			
			reader.close();
			
			// return list if empty
			if(headerList.isEmpty()) return headerList;
			
			// replace @id with id on the first column header
			if(headerList.get(0).equals(ID_COLUMN))
				headerList.set(0, ID_COLUMN.replace("@", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return headerList;
	}
	
	/**
	 * converts list of headers to method parameters
	 * eg. {"id","type"} becomes "String id, String type"
	 * @param headers
	 * @return
	 */
	private static String getMethodParametersFromColumnHeaders(List<String> headers) {
		String headerMethod = String.join(", String ", headers);
		return "String " + headerMethod;
	}
}
