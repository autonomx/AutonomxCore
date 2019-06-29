package core.helpers.csvHelper;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CsvHelper {

	public static final String Path_TestData = "testData/";

	/**
	 * gets single cell data in csv file
	 * 
	 * @param csv
	 *            - requires. csv.row, csv.column, csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public String getCellData(CsvObject csv) throws Exception {
		CSVReader readcsv;
		String cellValue = "";
		readcsv = new CSVReader(new FileReader(Path_TestData + csv.csvFile));
		List<String[]> myData = readcsv.readAll();
		cellValue = myData.get(csv.row)[csv.column];
		readcsv.close();

		return cellValue;
	}

	/**
	 * gets all csv data in list of string arrays
	 * 
	 * @param csv
	 *            - requires. csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public List<String[]> getAllCsvData(String filePath, CsvObject csv) {
		List<String[]> csvList = new ArrayList<String[]>();
		CSVReader readcsv;
		try {
			readcsv = new CSVReader(new FileReader(filePath + csv.csvFile));
			csvList = readcsv.readAll();
			readcsv.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csvList;
	}
	
	/**
	 * gets all csv data in list of string arrays
	 * 
	 * @param csv
	 *            - requires. csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public List<String[]> getAllCsvData(String filePath) {
		List<String[]> csvList = new ArrayList<String[]>();
		CSVReader readcsv;
		try {
			readcsv = new CSVReader(new FileReader(filePath));
			csvList = readcsv.readAll();
			readcsv.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csvList;
	}
	
	/**
	 * gets all csv data files skipping first row as header
	 * @param filePath
	 * @return
	 */
	public List<String[]> getAllCsvDataFirstRowAsHeader(String filePath) {
		//filePath = "/Users/Shared/Jenkins/Documents/Selenium/automation-client/automation/resources/testData/webApp/User.csv";
		List<String[]> csvList = new ArrayList<String[]>();
		CSVReader readcsv;
		try {
			readcsv = new CSVReader( new FileReader(filePath), CSVParser.DEFAULT_SEPARATOR,
		               CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
			csvList = readcsv.readAll();
			readcsv.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csvList;
	}

	/**
	 * 
	 * @param csv
	 *            - required: csv.csvFile, csv.value value: String [] record =
	 *            "3,David,Feezor,USA,40".split(",");
	 * @throws Exception
	 */
	public void writeNewCsv(CsvObject csv) throws Exception {

		String csvPath = Path_TestData + csv.csvFile;
		CSVWriter writer = new CSVWriter(new FileWriter(csvPath));
		writer.writeNext(csv.value);
		writer.close();
	}
	
	/**
	 * update cell based on row and column. csv object must containe filename, row and column
	 * @param csv csv file to update
	 * @param value value to write 
	 */
	public void writeToCell(CsvObject csv, String value) {
		String csvPath = csv.path + csv.csvFile;
		writeToCell(csv, value, csvPath);
	}
	
	/**
	 * update cell based on row and column. csv object must containe filename, row and column
	 * @param csv csv file to update
	 * @param value value to write 
	 * @param csvPath path to the csv data relative to root directory
	 */
	public void writeToCell(CsvObject csv, String value, String csvPath) {

		try {

			// Read existing file
			CSVReader reader = new CSVReader(new FileReader(csvPath), ',');
			List<String[]> csvBody = reader.readAll();
			// get CSV row column and replace with by using row and column
			csvBody.get(csv.row)[csv.column] = value;
			reader.close();

			// Write to CSV file which is open
			CSVWriter writer = new CSVWriter(new FileWriter(csvPath));
			writer.writeAll(csvBody);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param csv
	 *            - required: csv.csvFile, csv.value value: String [] record =
	 *            "3,David,Feezor,USA,40".split(",");
	 * @throws Exception
	 */
	public void appendCsv(CsvObject csv) throws Exception {

		String csvPath = Path_TestData + csv.csvFile;
		CSVWriter writer = new CSVWriter(new FileWriter(csvPath, true));
		writer.writeNext(csv.value);
		writer.close();
	}
}