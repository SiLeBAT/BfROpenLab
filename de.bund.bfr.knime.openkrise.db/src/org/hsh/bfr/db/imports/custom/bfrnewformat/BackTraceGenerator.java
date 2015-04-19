package org.hsh.bfr.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class BackTraceGenerator {

	XSSFWorkbook workbook;
	XSSFSheet sheet;
	XSSFCellStyle defaultStyle;

	public BackTraceGenerator() {
		try {
			InputStream myxls = this.getClass().getResourceAsStream("/org/hsh/bfr/db/imports/custom/bfrnewformat/BfR_Format_Backtrace.xlsx");
			workbook = new XSSFWorkbook(myxls);
			// Create a blank sheet
			//sheet = workbook.createSheet("BackTracing");
			sheet = workbook.getSheet("BackTracing");
			defaultStyle = workbook.createCellStyle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void save(String filename) {
		try {
			filename = "C:/Users/Armin/Desktop/Backtrace_request.xlsx";
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(filename));
			workbook.write(out);
			out.close();
			System.out.println(filename + " written successfully on disk.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
