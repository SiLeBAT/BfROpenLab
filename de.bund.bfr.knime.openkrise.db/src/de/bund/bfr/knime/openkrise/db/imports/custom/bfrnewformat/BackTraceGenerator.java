package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.bund.bfr.knime.openkrise.db.DBKernel;

public class BackTraceGenerator {

	public BackTraceGenerator(String outputFolder, List<String> business2Backtrace) {
		try {
			File f = new File(outputFolder);
			if (f.exists()) {
				for (int i=1;;i++) {
					f = new File(outputFolder + "_" + i);
					if (!f.exists() || i > 1000) break;
				}
			}
			String message = "";
			if (f.exists()) message = "Too many output folders... Please check and delete folders '" + outputFolder + "*'";
			else {
				f.mkdirs();
				int numFilesGenerated = getBacktraceRequests(f.getAbsolutePath(), business2Backtrace);
				//XSSFCellStyle defaultStyle = workbook.createCellStyle();
				if (numFilesGenerated == 0) message = "No new Templates generated. All done?";
				else message = numFilesGenerated + " new pre-filled templates generated, available in folder '" + f.getAbsolutePath() + "'";
			}

			IWorkbenchWindow eclipseWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (eclipseWindow != null) {						
				MessageDialog.openInformation(eclipseWindow.getShell(), "Template generation",  message);
			} else {
				JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
				JDialog dialog = pane.createDialog("Template generation");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void fillStations(XSSFSheet sheetStations, FormulaEvaluator evaluator) throws SQLException {
		String sql = "Select * from " + DBKernel.delimitL("Station");
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			int rownum = 1;
			do {
				XSSFRow row = sheetStations.getRow(rownum);
				if (row == null) row = sheetStations.createRow(rownum);
				rownum++;
				XSSFCell cell;
				if (rs.getObject("Serial") != null) {cell = row.createCell(0); cell.setCellValue(rs.getString("Serial"));}
				else if (rs.getObject("ID") != null) {cell = row.createCell(0); cell.setCellValue(rs.getString("ID"));}
				if (rs.getObject("Name") != null) {cell = row.createCell(1); cell.setCellValue(rs.getString("Name"));}
				if (rs.getObject("Strasse") != null) {cell = row.createCell(2); cell.setCellValue(rs.getString("Strasse"));}
				if (rs.getObject("Hausnummer") != null) {cell = row.createCell(3); cell.setCellValue(rs.getString("Hausnummer"));}
				if (rs.getObject("PLZ") != null) {cell = row.createCell(4); cell.setCellValue(rs.getString("PLZ"));}
				if (rs.getObject("Ort") != null) {cell = row.createCell(5); cell.setCellValue(rs.getString("Ort"));}
				if (rs.getObject("District") != null) {cell = row.createCell(6); cell.setCellValue(rs.getString("District"));}
				if (rs.getObject("Bundesland") != null) {cell = row.createCell(7); cell.setCellValue(rs.getString("Bundesland"));}
				if (rs.getObject("Land") != null) {cell = row.createCell(8); cell.setCellValue(rs.getString("Land"));}
				if (rs.getObject("Betriebsart") != null) {cell = row.createCell(9); cell.setCellValue(rs.getString("Betriebsart"));}
				cell = row.getCell(10); evaluator.evaluateFormulaCell(cell);
			} while (rs.next());
		}
	}
	private String getStationLookup(String stationID) throws SQLException {
		String sql = "Select * from " + DBKernel.delimitL("Station") + " WHERE " + DBKernel.delimitL("ID") + "=" + stationID;
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			return getStationLookup(rs, "Station");
		}
		return null;
	}
	private String getStationLookup(ResultSet rs, String sTable) throws SQLException {
		String result = rs.getString(sTable + ".ID");// + ", ";
		/*
		if (rs.getObject(sTable + ".Name") != null) result += rs.getString(sTable + ".Name");
		result += ", ";
		if (rs.getObject(sTable + ".Strasse") != null) result += rs.getString(sTable + ".Strasse");
		result += " ";
		if (rs.getObject(sTable + ".Hausnummer") != null) result += rs.getString(sTable + ".Hausnummer");
		result += ", ";
		if (rs.getObject(sTable + ".Ort") != null) result += rs.getString(sTable + ".Ort");
		result += ", ";
		if (rs.getObject(sTable + ".Land") != null) result += rs.getString(sTable + ".Land");
		*/
		return result;
	}
	private int getBacktraceRequests(String outputFolder, List<String> business2Backtrace) throws SQLException, IOException {
		int result = 0;
		String backtracingBusinessesSQL = "";
		for (String s : business2Backtrace) {
			backtracingBusinessesSQL += " OR " + DBKernel.delimitL("Station") + "." + DBKernel.delimitL("Betriebsart") + " = '" + s + "'";
		}
		String sql = "Select * from " + DBKernel.delimitL("Lieferungen") +
				" LEFT JOIN " + DBKernel.delimitL("Chargen") +
				" ON " + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID") + "=" + DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("Charge") +
				" LEFT JOIN " + DBKernel.delimitL("ChargenVerbindungen") +
				" ON " + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID") + "=" + DBKernel.delimitL("ChargenVerbindungen") + "." + DBKernel.delimitL("Produkt") +
				" LEFT JOIN " + DBKernel.delimitL("Produktkatalog") +
				" ON " + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("ID") + "=" + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("Artikel") +
				" LEFT JOIN " + DBKernel.delimitL("Station") +
				" ON " + DBKernel.delimitL("Station") + "." + DBKernel.delimitL("ID") + "=" + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("Station") +
				" WHERE " + DBKernel.delimitL("ChargenVerbindungen") + "." + DBKernel.delimitL("Zutat") + " IS NULL " +
				" AND (" + DBKernel.delimitL("Station") + "." + DBKernel.delimitL("Betriebsart") + " IS NULL " + backtracingBusinessesSQL + ")" +
				" ORDER BY " + DBKernel.delimitL("Station") + "." + DBKernel.delimitL("ID") + " ASC";
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Backtrace.xlsx");
				XSSFWorkbook workbook = new XSSFWorkbook(myxls);
				XSSFSheet sheetTracing = workbook.getSheet("BackTracing");
				XSSFSheet sheetStations = workbook.getSheet("Stations");
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				fillStations(sheetStations, evaluator);

				XSSFRow row = sheetTracing.getRow(0);
				XSSFCell cell;
				String sid = null;
				if (rs.getObject("Station.ID") != null) {
					sid = getStationLookup(rs, "Station");
					cell = row.getCell(1); cell.setCellValue(sid);
					cell = row.getCell(2); evaluator.evaluateFormulaCell(cell);
				}
				
				int rowIndex = 5;
				row = sheetTracing.getRow(rowIndex);
				insertRow(rs, row);
				if (sheetTracing.getRow(1) != null) {cell = sheetTracing.getRow(1).getCell(10); evaluator.evaluateFormulaCell(cell);}
				
				while (rs.next()) {
					if (rs.getObject("Station.ID") == null) break;
					String sl = getStationLookup(rs, "Station");
					if (!sl.equals(sid)) break;
					rowIndex++;
					row = copyRow(workbook, sheetTracing, 5, rowIndex);
					insertRow(rs, row);
				}
				rs.previous();

				//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
				save(workbook, outputFolder + "/Backtrace_request_" + rs.getInt("Lieferungen.ID") + ".xlsx");
				result++;
				myxls.close();
			} while (rs.next());
		}
		return result;
	}
	private void insertRow(ResultSet rs, XSSFRow row) throws SQLException {
		XSSFCell cell;
		cell = row.getCell(0);
		if (rs.getObject("Produktkatalog.Bezeichnung") != null) cell.setCellValue(rs.getString("Produktkatalog.Bezeichnung"));
		else cell.setCellValue("");
		cell = row.getCell(1);
		if (rs.getObject("Chargen.ChargenNr") != null) cell.setCellValue(rs.getString("Chargen.ChargenNr"));
		else cell.setCellValue("");
		cell = row.getCell(2);
		if (rs.getObject("Lieferungen.dd_day") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_day"));
		else cell.setCellValue("");
		cell = row.getCell(3);
		if (rs.getObject("Lieferungen.dd_month") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_month"));
		else cell.setCellValue("");
		cell = row.getCell(4);
		if (rs.getObject("Lieferungen.dd_year") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_year"));
		else cell.setCellValue("");
		cell = row.getCell(5);
		if (rs.getObject("Lieferungen.ad_day") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_day"));
		else cell.setCellValue("");
		cell = row.getCell(6);
		if (rs.getObject("Lieferungen.ad_month") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_month"));
		else cell.setCellValue("");
		cell = row.getCell(7);
		if (rs.getObject("Lieferungen.ad_year") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_year"));
		else cell.setCellValue("");
		cell = row.getCell(8);
		if (rs.getObject("Lieferungen.numPU") != null) cell.setCellValue(rs.getDouble("Lieferungen.numPU"));
		else cell.setCellValue("");
		cell = row.getCell(9);
		if (rs.getObject("Lieferungen.typePU") != null) cell.setCellValue(rs.getString("Lieferungen.typePU"));
		else cell.setCellValue("");
		cell = row.getCell(10);
		if (rs.getObject("Lieferungen.Empfänger") != null) cell.setCellValue(getStationLookup(rs.getString("Lieferungen.Empfänger")));
		else cell.setCellValue("");
		cell = row.getCell(11);
		if (rs.getObject("Lieferungen.Serial") != null) cell.setCellValue(rs.getString("Lieferungen.Serial"));
		else if (rs.getObject("Lieferungen.ID") != null) cell.setCellValue(rs.getString("Lieferungen.ID"));
		else cell.setCellValue("");
	}
	   private XSSFRow copyRow(XSSFWorkbook workbook, XSSFSheet worksheet, int sourceRowNum, int destinationRowNum) {
	        XSSFRow sourceRow = worksheet.getRow(sourceRowNum);
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
            XSSFRow newRow = worksheet.createRow(destinationRowNum);

	        // Loop through source columns to add to new row
	        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
	            // Grab a copy of the old/new cell
	            XSSFCell oldCell = sourceRow.getCell(i);
	            XSSFCell newCell = newRow.createCell(i);

	            // If the old cell is null jump to next cell
	            if (oldCell == null) {
	                newCell = null;
	                continue;
	            }

	            // Copy style from old cell and apply to new cell
	            XSSFCellStyle newCellStyle = workbook.createCellStyle();
	            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
	            ;
	            newCell.setCellStyle(newCellStyle);

	            // If there is a cell comment, copy
	            if (oldCell.getCellComment() != null) {
	                newCell.setCellComment(oldCell.getCellComment());
	            }

	            // If there is a cell hyperlink, copy
	            if (oldCell.getHyperlink() != null) {
	                newCell.setHyperlink(oldCell.getHyperlink());
	            }

	            // Set the cell data type
	            newCell.setCellType(oldCell.getCellType());

	            // Set the cell data value
	            switch (oldCell.getCellType()) {
	                case XSSFCell.CELL_TYPE_BLANK:
	                    newCell.setCellValue(oldCell.getStringCellValue());
	                    break;
	                case XSSFCell.CELL_TYPE_BOOLEAN:
	                    newCell.setCellValue(oldCell.getBooleanCellValue());
	                    break;
	                case XSSFCell.CELL_TYPE_ERROR:
	                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
	                    break;
	                case XSSFCell.CELL_TYPE_FORMULA:
	                    newCell.setCellFormula(oldCell.getCellFormula());
	                    break;
	                case XSSFCell.CELL_TYPE_NUMERIC:
	                    newCell.setCellValue(oldCell.getNumericCellValue());
	                    break;
	                case XSSFCell.CELL_TYPE_STRING:
	                    newCell.setCellValue(oldCell.getRichStringCellValue());
	                    break;
	            }
	        }

	        // If there are are any merged regions in the source row, copy to new row
	        for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
	            CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
	            if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
	                CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
	                        (newRow.getRowNum() +
	                                (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()
	                                        )),
	                        cellRangeAddress.getFirstColumn(),
	                        cellRangeAddress.getLastColumn());
	                worksheet.addMergedRegion(newCellRangeAddress);
	            }
	        }
	        
	        return newRow;
	    }
	   private void save(XSSFWorkbook workbook, String filename) {
		try {
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
