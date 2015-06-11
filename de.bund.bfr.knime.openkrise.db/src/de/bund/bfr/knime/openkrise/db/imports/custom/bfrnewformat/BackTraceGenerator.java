package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
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
		LinkedHashSet<String> se = getStationExtra();
		XSSFRow row = sheetStations.getRow(0);
		int j=0;
		for (String e : se) {
			if (e != null && !e.isEmpty()) {
				XSSFCell cell = row.getCell(11+j);
				if (cell == null) cell = row.createCell(11+j);
				cell.setCellValue(e);
				j++;
			}
		}
		
		String sql = "Select * from " + DBKernel.delimitL("Station");
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			int rownum = 1;
			do {
				row = sheetStations.getRow(rownum);
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
				//cell = row.getCell(10); evaluator.evaluateFormulaCell(cell);

				if (rs.getObject("ID") != null) {
					sql = "Select * from " + DBKernel.delimitL("ExtraFields") + " WHERE " + DBKernel.delimitL("tablename") + "='Station' AND " + DBKernel.delimitL("id") + "=" + rs.getInt("ID");
					ResultSet rs2 = DBKernel.getResultSet(sql, false);
					if (rs2 != null && rs2.first()) {
						do {
							String s = rs2.getString("attribute");
							j=0;
							for (String e : se) {
								if (s.equals(e)) {
									cell = row.getCell(11+j);
									if (cell == null) cell = row.createCell(11+j);
									cell.setCellValue(rs2.getString("value"));
									break;
								}
								j++;
							}
						} while (rs2.next());
					}	
				}
			} while (rs.next());
		}
	}
	private void fillLookup(XSSFWorkbook workbook, XSSFSheet sheetLookup) throws SQLException {
		String sql = "Select * from " + DBKernel.delimitL("LookUps") + " WHERE " + DBKernel.delimitL("type") + "='Sampling'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		int rownum = 1;
		if (rs != null && rs.first()) {
			do {
				XSSFRow row = sheetLookup.getRow(rownum);
				if (row == null) row = sheetLookup.createRow(rownum);
				XSSFCell cell = row.getCell(0);
				if (cell == null) cell = row.createCell(0);
				cell.setCellValue(rs.getString("value"));
				rownum++;
			} while (rs.next());
		}
		Name reference = workbook.createName();
		reference.setNameName("Sampling");
		String referenceString = sheetLookup.getSheetName() + "!$A$2:$A$" + (rownum);
		reference.setRefersToFormula(referenceString);				
		
		sql = "Select * from " + DBKernel.delimitL("LookUps") + " WHERE " + DBKernel.delimitL("type") + "='TypeOfBusiness'";
		rs = DBKernel.getResultSet(sql, false);
		rownum = 1;
		if (rs != null && rs.first()) {
			do {
				XSSFRow row = sheetLookup.getRow(rownum);
				if (row == null) row = sheetLookup.createRow(rownum);
				XSSFCell cell = row.getCell(1);
				if (cell == null) cell = row.createCell(1);
				cell.setCellValue(rs.getString("value"));
				rownum++;
			} while (rs.next());
		}
		reference = workbook.createName();
		reference.setNameName("ToB");
		referenceString = sheetLookup.getSheetName() + "!$B$2:$B$" + (rownum);
		reference.setRefersToFormula(referenceString);				
		
		sql = "Select * from " + DBKernel.delimitL("LookUps") + " WHERE " + DBKernel.delimitL("type") + "='Treatment'";
		rs = DBKernel.getResultSet(sql, false);
		rownum = 1;
		if (rs != null && rs.first()) {
			do {
				XSSFRow row = sheetLookup.getRow(rownum);
				if (row == null) row = sheetLookup.createRow(rownum);
				XSSFCell cell = row.getCell(2);
				if (cell == null) cell = row.createCell(2);
				cell.setCellValue(rs.getString("value"));
				rownum++;
			} while (rs.next());
		}
		reference = workbook.createName();
		reference.setNameName("Treatment");
		referenceString = sheetLookup.getSheetName() + "!$C$2:$C$" + (rownum);
		reference.setRefersToFormula(referenceString);				
		
		sql = "Select * from " + DBKernel.delimitL("LookUps") + " WHERE " + DBKernel.delimitL("type") + "='Units'";
		rs = DBKernel.getResultSet(sql, false);
		rownum = 1;
		if (rs != null && rs.first()) {
			do {
				XSSFRow row = sheetLookup.getRow(rownum);
				if (row == null) row = sheetLookup.createRow(rownum);
				XSSFCell cell = row.getCell(3);
				if (cell == null) cell = row.createCell(3);
				cell.setCellValue(rs.getString("value"));
				rownum++;
			} while (rs.next());
		}
		reference = workbook.createName();
		reference.setNameName("Units");
		referenceString = sheetLookup.getSheetName() + "!$D$2:$D$" + (rownum);
		reference.setRefersToFormula(referenceString);			
	}
	private String getStationLookup(String stationID) throws SQLException {
		String sql = "Select * from " + DBKernel.delimitL("Station") + " WHERE " + DBKernel.delimitL("ID") + "=" + stationID;
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			return getStationLookup(rs);
		}
		return null;
	}
	private String getStationLookup(ResultSet rs) throws SQLException {
		String result = rs.getString("Station.Serial");// + ", ";
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
	private LinkedHashSet<String> getStationExtra() throws SQLException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String sql = "Select * from " + DBKernel.delimitL("ExtraFields") + " WHERE " + DBKernel.delimitL("tablename") + "='Station'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				result.add(rs.getString("attribute"));
			} while (rs.next());
		}	
		return result;
	}
	private LinkedHashSet<String> getLotExtra() throws SQLException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String sql = "Select * from " + DBKernel.delimitL("ExtraFields") + " WHERE " + DBKernel.delimitL("tablename") + "='Chargen'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				String s = rs.getString("attribute");
				if (!s.equals("Production Date") && !s.equals("Best before date") && !s.equals("Treatment of product during production") && !s.equals("Sampling")) result.add(s);
			} while (rs.next());
		}	
		return result;
	}
	private LinkedHashSet<String> getDeliveryExtra() throws SQLException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String sql = "Select * from " + DBKernel.delimitL("ExtraFields") + " WHERE " + DBKernel.delimitL("tablename") + "='Lieferungen'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				result.add(rs.getString("attribute"));
			} while (rs.next());
		}	
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
				XSSFSheet sheetLookup = workbook.getSheet("LookUp");
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				fillStations(sheetStations, evaluator);
				fillLookup(workbook, sheetLookup);
				LinkedHashSet<String> le = getLotExtra();
				LinkedHashSet<String> de = getDeliveryExtra();

				// Station in Focus
				XSSFRow row = sheetTracing.getRow(4);
				XSSFCell cell;
				String sid = null;
				if (rs.getObject("Station.Serial") != null) {
					sid = getStationLookup(rs);
					cell = row.getCell(1); cell.setCellValue(sid);
					cell = row.getCell(2); evaluator.evaluateFormulaCell(cell);
				}
				
				// Products Out
				row = sheetTracing.getRow(7);
				int j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}
				
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheetTracing);
				LinkedHashMap<String, Lot> lotNumbers = new LinkedHashMap<>();
				int rowIndex = 9;
				row = sheetTracing.getRow(rowIndex);
				String ln = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de);
				if (!lotNumbers.containsKey(ln)) {
					Lot l = new Lot();
					l.setNumber(ln);
					if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
					if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
					lotNumbers.put(ln, l);
				}
				
				while (rs.next()) {
					if (rs.getObject("Station.Serial") == null) break;
					String sl = getStationLookup(rs);
					if (!sl.equals(sid)) break;
					rowIndex++;
					row = copyRow(workbook, sheetTracing, 9, rowIndex);
					ln = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de);
					if (!lotNumbers.containsKey(ln)) {
						Lot l = new Lot();
						l.setNumber(ln);
						if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
						if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
						lotNumbers.put(ln, l);
					}
				}
				rs.previous();

				// Lot Information
				row = sheetTracing.getRow(rowIndex + 3);
				j=0;
				for (String e : le) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(17+j);
						if (cell == null) cell = row.createCell(17+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += 5;
				int i=0;
				row = sheetTracing.getRow(rowIndex);
				for (Lot lot : lotNumbers.values()) {
					if (lot != null && !lot.getNumber().isEmpty()) {
						if (i > 0) row = copyRow(workbook, sheetTracing, rowIndex, rowIndex + i);
						cell = row.getCell(0); cell.setCellValue(lot.getNumber());
						if (lot.getUnitNumber() != null) {
							cell = row.getCell(1); cell.setCellValue(lot.getUnitNumber());
						}
						if (lot.getUnitUnit() != null) {
							cell = row.getCell(2); cell.setCellValue(lot.getUnitUnit());							
						}
						insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 1);
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 2, "=Units");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 15, "=Treatment");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 16, "=Sampling");
						i++;
					}
				}
				
				Name reference = workbook.createName();
				reference.setNameName("LotNumbers");
				String referenceString = sheetTracing.getSheetName() + "!$A$" + (rowIndex+1) + ":$A$" + (rowIndex+i);
				reference.setRefersToFormula(referenceString);				
				
				// Ingredients for Lot(s)
				row = sheetTracing.getRow(rowIndex + i + 2);
				j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += i+4;
				for (i=0;i<86;i++) {
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 2, "1", "31");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 3, "1", "12");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 4, "1900", "3000");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 5, "1", "31");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 6, "1", "12");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 7, "1900", "3000");
					insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 8);
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 9, "=Units");
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 10, "=StationIDs");
					row = sheetTracing.getRow(rowIndex+i);
					cell = row.getCell(11);
					cell.setCellFormula("INDEX(Companies,MATCH(K" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
					evaluator.evaluateFormulaCell(cell);
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 12, "=LotNumbers");
				}
				
				//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
				save(workbook, outputFolder + "/Backtrace_request_" + rs.getString("Station.Serial") + "_" + rs.getString("Station.Name") + ".xlsx");
				result++;
				myxls.close();
			} while (rs.next());
		}
		return result;
	}
	
	private void insertDropBox(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col, String ref) {
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint(ref);
		CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
		XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.setSuppressDropDownArrow(true);
		validation.setShowPromptBox(true);
		sheetTracing.addValidationData(validation);
	}
	private void insertCondition(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col, String min, String max) {
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createIntegerConstraint(OperatorType.BETWEEN, min, max);
		// dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"0000011", "0000021", "0000031"});
		CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
		XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.setSuppressDropDownArrow(true);
		validation.setShowPromptBox(true);
		sheetTracing.addValidationData(validation);
	}
	private void insertDecCondition(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col) {
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createDecimalConstraint(OperatorType.GREATER_OR_EQUAL, "0", "");
		// dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"0000011", "0000021", "0000031"});
		CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
		XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.setSuppressDropDownArrow(true);
		validation.setShowPromptBox(true);
		sheetTracing.addValidationData(validation);
	}
	
	private String fillRow(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, ResultSet rs, XSSFRow row, FormulaEvaluator evaluator, LinkedHashSet<String> de) throws SQLException {
		String result = null;
		
		XSSFCell cell;
		cell = row.getCell(0);
		if (rs.getObject("Produktkatalog.Bezeichnung") != null) cell.setCellValue(rs.getString("Produktkatalog.Bezeichnung"));
		else cell.setCellValue("");
		cell = row.getCell(1);
		if (rs.getObject("Chargen.ChargenNr") != null) cell.setCellValue(rs.getString("Chargen.ChargenNr"));
		else cell.setCellValue("(autoLot" + row.getRowNum() + ")");
		result = cell.getStringCellValue();
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 2, "1", "31");
		cell = row.getCell(2);
		if (rs.getObject("Lieferungen.dd_day") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_day"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 3, "1", "12");
		cell = row.getCell(3);
		if (rs.getObject("Lieferungen.dd_month") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_month"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 4, "1900", "3000");
		cell = row.getCell(4);
		if (rs.getObject("Lieferungen.dd_year") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_year"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 5, "1", "31");
		cell = row.getCell(5);
		if (rs.getObject("Lieferungen.ad_day") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_day"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 6, "1", "12");
		cell = row.getCell(6);
		if (rs.getObject("Lieferungen.ad_month") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_month"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 7, "1900", "3000");
		cell = row.getCell(7);
		if (rs.getObject("Lieferungen.ad_year") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_year"));
		else cell.setCellValue("");
		insertDecCondition(dvHelper, sheetTracing, row.getRowNum(), 8);
		cell = row.getCell(8);
		if (rs.getObject("Lieferungen.numPU") != null) cell.setCellValue(rs.getDouble("Lieferungen.numPU"));
		else cell.setCellValue("");
		insertDropBox(dvHelper, sheetTracing, row.getRowNum(), 9, "=Units");
		cell = row.getCell(9);
		if (rs.getObject("Lieferungen.typePU") != null) cell.setCellValue(rs.getString("Lieferungen.typePU"));
		else cell.setCellValue("");
		cell = row.getCell(10);
		if (rs.getObject("Lieferungen.Empfänger") != null) cell.setCellValue(getStationLookup(rs.getString("Lieferungen.Empfänger")));
		else cell.setCellValue("");
		cell = row.getCell(11);
		cell.setCellFormula("INDEX(Companies,MATCH(K" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
		evaluator.evaluateFormulaCell(cell);
		cell = row.getCell(12);
		if (rs.getObject("Lieferungen.Serial") != null) cell.setCellValue(rs.getString("Lieferungen.Serial"));
		else if (rs.getObject("Lieferungen.ID") != null) cell.setCellValue(rs.getString("Lieferungen.ID"));
		else cell.setCellValue("");
		
		if (rs.getObject("Lieferungen.ID") != null) {
			String sql = "Select * from " + DBKernel.delimitL("ExtraFields") + " WHERE " + DBKernel.delimitL("tablename") + "='Lieferungen' AND " + DBKernel.delimitL("id") + "=" + rs.getInt("Lieferungen.ID");
			ResultSet rs2 = DBKernel.getResultSet(sql, false);
			if (rs2 != null && rs2.first()) {
				do {
					String s = rs2.getString("attribute");
					int j=0;
					for (String e : de) {
						if (s.equals(e)) {
							cell = row.getCell(13+j);
							if (cell == null) cell = row.createCell(13+j);
							cell.setCellValue(rs2.getString("value"));
							break;
						}
						j++;
					}
				} while (rs2.next());
			}	
		}
		
		return result;
	}
	   private XSSFRow copyRow(XSSFWorkbook workbook, XSSFSheet worksheet, int sourceRowNum, int destinationRowNum) {
	        XSSFRow sourceRow = worksheet.getRow(sourceRowNum);
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1, true, false);
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
	            
	            newCell.setCellStyle(newCellStyle);

	            // Set the cell data type
	            newCell.setCellType(oldCell.getCellType());

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
