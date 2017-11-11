package core.helpers.CsvHelper;


public class CsvObject{
	String csvFile;
	String[] value;
	int row;
	int column;
	
	public CsvObject withCsvFile(String csvFile) {
		this.csvFile = csvFile;
		return this;
	}
	
	public CsvObject withValue(String[] value) {
		this.value = value;
		return this;
	}
	
	public CsvObject withRow(int row) {
		this.row = row;
		return this;
	}
	
	public CsvObject withColumn(int column) {
		this.column = column;
		return this;
	}
}