package core.helpers.csvHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import core.helpers.Helper;

public class CsvHelper {

	/**
	 * gets single cell data in csv file
	 * 
	 * @param csv - requires. csv.row, csv.column, csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public String getCellData(CsvObject csv) throws Exception {
		CSVReader readcsv;
		String cellValue = "";
		readcsv = new CSVReader(new FileReader(csv.path + File.separator + csv.csvFile));
		List<String[]> myData = readcsv.readAll();
		cellValue = myData.get(csv.row)[csv.column];
		readcsv.close();

		return cellValue;
	}

	/**
	 * gets all csv data in list of string arrays
	 * 
	 * @param csv - requires. csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public List<String[]> getAllCsvData(String filePath, CsvObject csv) {
		return getAllCsvData(filePath + File.separator + csv, 0, true);
	}

	/**
	 * gets all csv data in list of string arrays
	 * 
	 * @param csv - requires. csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public List<String[]> getAllCsvData(String filePath) {
		return getAllCsvData(filePath, 0, true);
	}

	/**
	 * gets all csv data in list of string arrays
	 * 
	 * @param csv - requires. csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public List<String[]> getAllCsvData(String filePath, int startingRow, boolean skipEmptyRows) {
		List<String[]> csvList = new ArrayList<String[]>();
		CSVReader readcsv;
		CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true)
				.withIgnoreLeadingWhiteSpace(true).build();
		try {

			readcsv = new CSVReaderBuilder(new FileReader(filePath)).withSkipLines(startingRow).withCSVParser(parser)
					.build();

			// add csv rows. skip if skipEmptyRows is true
			String[] line;
			while ((line = readcsv.readNext()) != null) {
				if (!skipEmptyRows)
					csvList.add(line);
				else if (!line[0].isEmpty())
					csvList.add(line);

			}
			readcsv.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return csvList;
	}

	/**
	 * gets all csv data files skipping first row as header
	 * 
	 * @param filePath
	 * @return
	 */
	public List<String[]> getAllCsvDataFirstRowAsHeader(String filePath) {
		return getAllCsvData(filePath, 1, true);
	}

	/**
	 * file path: csv.path + csv.csvFile using csvObject
	 * 
	 * @param csv - required: csv.csvFile, csv.value value: String [] record =
	 *            "3,David,Feezor,USA,40".split(",");
	 * @throws Exception
	 */
	public void writeNewCsv(CsvObject csv) {

		String csvPath = csv.path + File.separator + csv.csvFile;
		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter(csvPath));
			writer.writeNext(csv.value);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * adds row to csv file file path: csv.path + csv.csvFile using csvObject eg.
	 * csv.value: String [] record = "3,David,Feezor,USA,40".split(",");
	 * 
	 * @param csv
	 * @throws Exception
	 */
	public void AddRow(CsvObject csv) {

		String csvPath = csv.path + File.separator + csv.csvFile;
		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter(csvPath, true));
			writer.writeNext(csv.value);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * update cell based on row and column. csv object must containe filename, row
	 * and column
	 * 
	 * @param csv   csv file to update
	 * @param value value to write
	 */
	public void writeToCell(CsvObject csv, String value) {
		String csvPath = csv.path + File.separator + csv.csvFile;
		writeToCell(csv, value, csvPath);
	}

	/**
	 * update cell based on row and column. csv object must contain filename, row
	 * and column
	 * 
	 * @param csv     csv file to update
	 * @param value   value to write
	 * @param csvPath path to the csv data relative to root directory
	 */
	public void writeToCell(CsvObject csv, String value, String csvPath) {

		try {

			// Read existing file
			CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true)
					.withIgnoreLeadingWhiteSpace(true).build();

			CSVReader reader = new CSVReaderBuilder(new FileReader(csvPath)).withSkipLines(0).withCSVParser(parser)
					.build();

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
	 * @param csv - required: csv.csvFile, csv.value value: String [] record =
	 *            "3,David,Feezor,USA,40".split(",");
	 * @throws Exception
	 */
	public void appendCsv(CsvObject csv) {
		String csvPath = csv.path + File.separator + csv.csvFile;
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(csvPath, true));
			writer.writeNext(csv.value);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get row index in csv file that equal id value
	 * 
	 * @param id
	 * @param filePath
	 * @return
	 */
	public int getRowIndex(String id, String filePath) {
		List<String[]> rows = getAllCsvData(filePath, 0, false);

		for (int i = 0; i < rows.size(); i++) {
			String rowId = rows.get(i)[0];
			if (rowId.equals(id))
				return i;
		}
		Helper.assertFalse("row not found: '" + id + "' at path: " + filePath);
		return -1;
	}

	/**
	 * get column index by column value
	 * 
	 * @param column
	 * @param filePath
	 * @return
	 */
	public int getColumnIndex(String column, String filePath) {
		List<String[]> rows = getAllCsvData(filePath, 0, false);
		int columnSize = rows.get(0).length;
		for (int i = 0; i < columnSize; i++) {
			String columnValue = rows.get(0)[i].trim();
			if (columnValue.equals(column))
				return i;
		}
		Helper.assertFalse("column not found: '" + column + "' at path: " + filePath);
		return -1;
	}
}