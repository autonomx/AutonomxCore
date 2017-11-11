package core.helpers.ExcelHelper;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelObject{
	String file;
	String sheetName;
	String value;
	int row;
	int column;
	XSSFSheet ExcelWSheet;
	XSSFWorkbook ExcelWBook;
	
	public ExcelObject withExcelFile(String file) {
		this.file = file;
		return this;
	}
	
	public ExcelObject withSheetName(String sheetName) {
		this.sheetName = sheetName;
		return this;
	}
	
	public ExcelObject withValue(String value) {
		this.value = value;
		return this;
	}
	
	public ExcelObject withRow(int row) {
		this.row = row;
		return this;
	}
	
	public ExcelObject withColumn(int column) {
		this.column = column;
		return this;
	}
	
	public ExcelObject withExcelWSheet(XSSFSheet ExcelWSheet) {
		this.ExcelWSheet = ExcelWSheet;
		return this;
	}
	
	public ExcelObject withExcelWBook(XSSFWorkbook ExcelWBook) {
		this.ExcelWBook = ExcelWBook;
		return this;
	}
}