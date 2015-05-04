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
import org.apache.poi.xssf.usermodel.XSSFCell;
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
				" AND (" + DBKernel.delimitL("Station") + "." + DBKernel.delimitL("Betriebsart") + " IS NULL " + backtracingBusinessesSQL + ")";
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
				if (rs.getObject("Station.ID") != null) {cell = row.getCell(1); cell.setCellValue(getStationLookup(rs, "Station"));}
				row = sheetTracing.getRow(5);
				if (rs.getObject("Produktkatalog.Bezeichnung") != null) {cell = row.getCell(0); cell.setCellValue(rs.getString("Produktkatalog.Bezeichnung"));}
				if (rs.getObject("Chargen.ChargenNr") != null) {cell = row.getCell(1); cell.setCellValue(rs.getString("Chargen.ChargenNr"));}
				if (rs.getObject("Lieferungen.dd_day") != null) {cell = row.getCell(2); cell.setCellValue(rs.getInt("Lieferungen.dd_day"));}
				if (rs.getObject("Lieferungen.dd_month") != null) {cell = row.getCell(3); cell.setCellValue(rs.getInt("Lieferungen.dd_month"));}
				if (rs.getObject("Lieferungen.dd_year") != null) {cell = row.getCell(4); cell.setCellValue(rs.getInt("Lieferungen.dd_year"));}
				if (rs.getObject("Lieferungen.ad_day") != null) {cell = row.getCell(5); cell.setCellValue(rs.getInt("Lieferungen.ad_day"));}
				if (rs.getObject("Lieferungen.ad_month") != null) {cell = row.getCell(6); cell.setCellValue(rs.getInt("Lieferungen.ad_month"));}
				if (rs.getObject("Lieferungen.ad_year") != null) {cell = row.getCell(7); cell.setCellValue(rs.getInt("Lieferungen.ad_year"));}
				if (rs.getObject("Lieferungen.numPU") != null) {cell = row.getCell(8); cell.setCellValue(rs.getDouble("Lieferungen.numPU"));}
				if (rs.getObject("Lieferungen.typePU") != null) {cell = row.getCell(9); cell.setCellValue(rs.getString("Lieferungen.typePU"));}
				if (rs.getObject("Lieferungen.Empfänger") != null) {cell = row.getCell(10); cell.setCellValue(getStationLookup(rs.getString("Lieferungen.Empfänger")));}
				if (rs.getObject("Lieferungen.Serial") != null) {cell = row.getCell(11); cell.setCellValue(rs.getString("Lieferungen.Serial"));}
				else if (rs.getObject("Lieferungen.ID") != null) {cell = row.getCell(11); cell.setCellValue(rs.getString("Lieferungen.ID"));}

				//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
				save(workbook, outputFolder + "/Backtrace_request_" + rs.getInt("Lieferungen.ID") + ".xlsx");
				result++;
				myxls.close();
			} while (rs.next());
		}
		return result;
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
