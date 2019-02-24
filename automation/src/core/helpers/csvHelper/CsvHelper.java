package core.helpers.csvHelper;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

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
	public static String getCellData(CsvObject csv) throws Exception {
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
	public static List<String[]> getAllCsvData(String filePath, CsvObject csv) {
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
	 * 
	 * @param csv
	 *            - required: csv.csvFile, csv.value value: String [] record =
	 *            "3,David,Feezor,USA,40".split(",");
	 * @throws Exception
	 */
	public static void writeNewCsv(CsvObject csv) throws Exception {

		String csvPath = Path_TestData + csv.csvFile;
		CSVWriter writer = new CSVWriter(new FileWriter(csvPath));
		writer.writeNext(csv.value);
		writer.close();
	}

	/**
	 * 
	 * @param csv
	 *            - required: csv.csvFile, csv.value value: String [] record =
	 *            "3,David,Feezor,USA,40".split(",");
	 * @throws Exception
	 */
	public static void appendCsv(CsvObject csv) throws Exception {

		String csvPath = Path_TestData + csv.csvFile;
		CSVWriter writer = new CSVWriter(new FileWriter(csvPath, true));
		writer.writeNext(csv.value);
		writer.close();
	}
}