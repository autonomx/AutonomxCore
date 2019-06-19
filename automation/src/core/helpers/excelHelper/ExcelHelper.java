package core.helpers.excelHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHelper {

	public static final String Path_TestData = "testData/";

	/**
	 * gets the excel file And the work sheet
	 * 
	 * @param Path
	 * @param SheetName
	 * @throws Exception
	 */
	public static ExcelObject setExcelFile(ExcelObject excel) throws Exception {
		// Open the Excel file
		FileInputStream ExcelFile = new FileInputStream(Path_TestData + excel.file);
		// Access the required test data sheet
		excel.withExcelWBook(new XSSFWorkbook(ExcelFile));
		excel.withExcelWSheet(excel.ExcelWBook.getSheet(excel.sheetName));

		return excel;
	}

	/**
	 * returns all the column data as array list of string
	 * 
	 * @param colNum
	 * @return
	 * @throws Exception
	 */
	public static List<String> getColumData(ExcelObject excel) throws Exception {

		excel = setExcelFile(excel);

		List<String> columnData = new ArrayList<String>();
		String cellData;

		for (Row row : excel.ExcelWSheet) { // For each Row.
			Cell cell = row.getCell(excel.column); // Get the Cell at the Index / Column you want.
			cellData = cell.getStringCellValue();
			if (cellData != null) {
				columnData.add(cellData);
			}
		}
		return columnData;
	}

	/**
	 * This method is to read the test data from the Excel cell, in this we are
	 * passing parameters as Row num And Col num
	 * 
	 * @param RowNum
	 * @param ColNum
	 * @return
	 * @throws Exception
	 */

	public static String getCellData(ExcelObject excel) throws Exception {
		setExcelFile(excel);

		XSSFCell Cell = excel.ExcelWSheet.getRow(excel.row).getCell(excel.column);
		String CellData = Cell.getStringCellValue();
		return CellData;

	}

	/**
	 * This method is to write in the Excel cell, Row num And Col num are the
	 * parameters
	 * 
	 * @param excel
	 *            - required: excel.row, excel.column, excel.value, excel.file
	 * @throws Exception
	 */
	public static void setCellData(ExcelObject excel) throws Exception {

		setExcelFile(excel);
		XSSFCell Cell;
		XSSFRow Row;

		Row = excel.ExcelWSheet.getRow(excel.row);
		Cell = Row.getCell(excel.column, MissingCellPolicy.RETURN_BLANK_AS_NULL);
		if (Cell == null) {
			Cell = Row.createCell(excel.column);
			Cell.setCellValue(excel.value);
		} else {
			Cell.setCellValue(excel.value);
		}

		// Constant variables Test Data path And Test Data file name
		FileOutputStream fileOut = new FileOutputStream(Path_TestData + excel.file);
		excel.ExcelWBook.write(fileOut);
		fileOut.flush();
		fileOut.close();

	}

	/**
	 * 
	 * @param excelObjects
	 *            - contains data info
	 * @throws Exception
	 */
	public static void setCellData(List<ExcelObject> excelObjects) throws Exception {
		setExcelFile(excelObjects.get(0));
		XSSFCell Cell;
		XSSFRow Row;

		for (ExcelObject excel : excelObjects) {
			Row = excel.ExcelWSheet.getRow(excel.row);
			Cell = Row.getCell(excel.column, MissingCellPolicy.RETURN_BLANK_AS_NULL);
			if (Cell == null) {
				Cell = Row.createCell(excel.column);
				Cell.setCellValue(excel.value);
			} else {
				Cell.setCellValue(excel.value);
			}
		}
		// Constant variables Test Data path And Test Data file name
		FileOutputStream fileOut = new FileOutputStream(Path_TestData + excelObjects.get(0).file);
		excelObjects.get(0).ExcelWBook.write(fileOut);
		fileOut.flush();
		fileOut.close();
	}
}