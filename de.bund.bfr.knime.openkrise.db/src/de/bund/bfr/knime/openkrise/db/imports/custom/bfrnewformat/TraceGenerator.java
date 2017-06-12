/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

import com.sun.org.apache.bcel.internal.util.ClassLoader;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

import de.bund.bfr.knime.openkrise.common.Station;

public class TraceGenerator {

	private JComponent parent;
	private boolean do2017Format = false;
	private boolean generateAllData = false;
	private boolean hasTOB = true;
	private String lang = "en";
	/*
- bis schlachthof zurück: z.b. bochumer fleischhandelsges.
- rki neue fälle: was konsumiert?
- überlegen wie verpacken, falls die KOBs nochmal in dieselbe firma ausrücken müssen
Erinnerung an die alten Template inhaber senden?
*/
	public TraceGenerator(File outputFolder, Station station, JComponent parent, boolean isForward, boolean do2017Format, boolean generateAllData) {
		this.parent = parent;
		this.do2017Format = do2017Format;
		this.generateAllData = generateAllData;
		hasTOB = getHasTOB();
		try {
			int numFilesGenerated = 0;
			try {
				numFilesGenerated = isForward ? getFwdStationRequests(outputFolder.getAbsolutePath(), station) : getBackStationRequests(outputFolder.getAbsolutePath(), station);
			}
			catch (Exception e) {e.printStackTrace();}

			String message = "";
			if (numFilesGenerated == 0) message = "No new Template generated. Maybe the selected station '" + station.getName() + "' has " + (isForward ? "no incoming deliveries? Or they are all already connected to outgoing lots?" : "no ougoing lots? Or they are all already connected to incoming deliveries?");
			else message = numFilesGenerated + " new pre-filled templates generated, available in folder '" + outputFolder.getAbsolutePath() + "'";

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
	public TraceGenerator(File outputFolder, List<String> business2Trace, boolean isForward, JComponent parent, boolean do2017Format, boolean generateAllData) {
		this.parent = parent;
		this.do2017Format = do2017Format;
		this.generateAllData = generateAllData;
		hasTOB = getHasTOB();
		try {
			int numFilesGenerated = 0;
			try {
				numFilesGenerated = isForward ? getFortraceRequests(outputFolder.getAbsolutePath(), business2Trace, null) : getBacktraceRequests(outputFolder.getAbsolutePath(), business2Trace, null);
			}
			catch (Exception e) {e.printStackTrace();}

			String message = "";
			if (numFilesGenerated == 0) message = "No new Templates generated. All done?";
			else message = numFilesGenerated + " new pre-filled templates generated, available in folder '" + outputFolder.getAbsolutePath() + "'";

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
		
		String sql = "Select * from " + MyDBI.delimitL("Station") + " ORDER BY " + MyDBI.delimitL("Serial") + " ASC";
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

				fillExtraFields("Station", rs.getObject("ID"), row, se, 11);
				/*
				if (rs.getObject("ID") != null) {
					sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Station' AND " + MyDBI.delimitL("id") + "=" + rs.getInt("ID");
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
				*/
			} while (rs.next());
		}
	}
	private void fillLookup(XSSFWorkbook workbook, XSSFSheet sheetLookup) throws SQLException {
		String sql = "Select * from " + MyDBI.delimitL("LookUps") + " WHERE " + MyDBI.delimitL("type") + "='Sampling'" + " ORDER BY " + MyDBI.delimitL("value") + " ASC";
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
		
		sql = "Select * from " + MyDBI.delimitL("LookUps") + " WHERE " + MyDBI.delimitL("type") + "='TypeOfBusiness'" + " ORDER BY " + MyDBI.delimitL("value") + " ASC";
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
		
		sql = "Select * from " + MyDBI.delimitL("LookUps") + " WHERE " + MyDBI.delimitL("type") + "='Treatment'" + " ORDER BY " + MyDBI.delimitL("value") + " ASC";
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
		
		sql = "Select * from " + MyDBI.delimitL("LookUps") + " WHERE " + MyDBI.delimitL("type") + "='Units'" + " ORDER BY " + MyDBI.delimitL("value") + " ASC";
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
		String sql = "Select * from " + MyDBI.delimitL("Station") + " WHERE " + MyDBI.delimitL("ID") + "=" + stationID;
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
		String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Station'";
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
		String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Chargen'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				String s = rs.getString("attribute");
				if (!s.equalsIgnoreCase("Production Date") && !s.equalsIgnoreCase("Best before date") && !s.equalsIgnoreCase("Treatment of product during production") && !s.equalsIgnoreCase("Sampling")) result.add(s);
			} while (rs.next());
		}	
		return result;
	}
	private LinkedHashSet<String> getDeliveryExtra() throws SQLException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Lieferungen'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				result.add(rs.getString("attribute"));
			} while (rs.next());
		}	
		return result;
	}

	private int getSimpleFwdStationRequests(String outputFolder, ResultSet rs) throws SQLException, IOException, InvalidFormatException {
		int result = 0;
		if (rs.getObject("Station.ID") != null) {
			String template;
			if (hasTOB) {				
				if (lang.equals("de")) template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Uptrace_Prod_simple_de.xlsx";
				else template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Uptrace_Prod_simple_tob_en.xlsx";
			}
			else {
				if (lang.equals("de")) template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Uptrace_Prod_simple_de.xlsx";
				else template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Uptrace_Prod_simple_en.xlsx";
			}
			InputStream myxls = this.getClass().getResourceAsStream(template);
			File file = getResourceAsFile(myxls);
			myxls.close();
			OPCPackage opcPackage = OPCPackage.open(file.getAbsolutePath());
			XSSFWorkbook workbook = new XSSFWorkbook(opcPackage);

//			XSSFWorkbook workbook = new XSSFWorkbook(myxls);
//			save(workbook, outputFolder + File.separator + "StationFwdtrace_request_" + (generateAllData ? "_all":"") + "_temp.xlsx", true);
			XSSFSheet sheetTracing = workbook.getSheet(XlsStruct.getPROD_FWD_SHEETNAME(lang));//"Herstellung - Vorwärtsverfolgun");

			// Station in Focus
			String sif = null;
			Integer id = null;
			XSSFRow row = sheetTracing.getRow(0);
			XSSFCell cell;

			id = rs.getInt("Station.ID");
			sif = rs.getString("Station.Name");
			cell = row.getCell(1); cell.setCellValue(sif);
			cell = row.getCell(2); cell.setCellValue(rs.getString("Station.Adresse"));
			int countryCellNum = 3;
			  for(int i = 0; i < sheetTracing.getNumMergedRegions(); i++) {
			        CellRangeAddress region = sheetTracing.getMergedRegion(i);
			        int rowNum = region.getFirstRow();
			        int colIndex = region.getFirstColumn();
			        if (rowNum == 0 && colIndex == 2) {
			        	countryCellNum = region.getLastColumn() + 1;
			        }
			    }
			  if (rs.getString("Station.Land") == null)  row.getCell(countryCellNum).setCellValue("");
			  else row.getCell(countryCellNum).setCellValue(rs.getString("Station.Land"));
			
			// Products Out
			int rowIndex = 6;
			String stationID = rs.getString("Station.ID");
			HashMap<Integer, HashSet<Integer>> furtherLots = new HashMap<>();
			HashSet<Integer> alreadyUsedDels = new HashSet<>();
			do {
				if (rs.getObject("Station.ID") == null || !rs.getString("Station.ID").equals(stationID)) break;
				Integer did = rs.getInt("Lieferungen.ID");
				if (!alreadyUsedDels.contains(did)) {
					alreadyUsedDels.add(did);
					copyRow(workbook, sheetTracing, rowIndex, rowIndex+1);
					row = sheetTracing.getRow(rowIndex);								
					fillRowSimple(sheetTracing, rs, row, true, false);
					rowIndex++;
					if (generateAllData) {
						Integer key = rs.getInt("ChargenVerbindungen.Produkt");
						if (!furtherLots.containsKey(key)) furtherLots.put(key, new HashSet<>());
						furtherLots.get(key).add(rowIndex);
					}
				}
			} while (rs.next());
			rs.previous();
			//copyStyle(workbook, sheetTracing, 6, 9);
			
			row = sheetTracing.getRow(rowIndex+4);
			cell = row.getCell(0);	
			if  (lang.equals("en")) cell.setCellValue("In Column A starting with Line Number " + (rowIndex+11) + " please enter the line number of the incoming good being the ingredient of this product. Afterwards, enter the product information in columns B to M.");
			else cell.setCellValue("In Spalte A ab Zeile " + (rowIndex+13) + " die Zeilennummer aus dem Wareneingang eintragen und ab Spalte B ein zugehöriges Produkt und die weiteren erfragten Angaben eintragen");
			
			//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
			if (generateAllData) {
				String sql = "Select * from " + MyDBI.delimitL("Lieferungen") +
						" LEFT JOIN " + MyDBI.delimitL("Chargen") +
						" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
						" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
						" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
						" LEFT JOIN " + MyDBI.delimitL("Station") +
						" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
						" WHERE " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + stationID +
						" ORDER BY " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ChargenNr") + " ASC";
				ResultSet rs2 = DBKernel.getResultSet(sql, false);
				if (rs2 != null && rs2.first()) {
					rowIndex += lang.equals("en") ? 10 : 12;
					int numCols = sheetTracing.getRow(rowIndex).getLastCellNum();
					do {
						//System.err.println(rs2.getString("Station.Name"));
						copyRow(workbook, sheetTracing, rowIndex, rowIndex+1);
						row = sheetTracing.getRow(rowIndex);								
			            //row = sheetTracing.createRow(rowIndex); for (int ii=0;ii<numCols;ii++) row.createCell(ii);
						fillRowSimple(sheetTracing, rs2, row, false, false);
						Integer cid = rs2.getInt("Chargen.ID");
						if (furtherLots.containsKey(cid)) {
							HashSet<Integer> tl = furtherLots.get(cid);
							boolean afterFirst = false;
							for (int tli : tl) {
								if (afterFirst) {
									rowIndex++;
						            row = sheetTracing.createRow(rowIndex); for (int ii=0;ii<numCols;ii++) row.createCell(ii);
									fillRowSimple(sheetTracing, rs2, row, false, false);
								}
								cell = row.getCell(0);
								cell.setCellValue(tli+"");
								afterFirst = true;
							}
						}
						rowIndex++;
					} while (rs2.next());
				}
			}
			String fn = "StationFwdtrace_request_" + sif + "_" + id + (generateAllData ? "_all":"") + ".xlsx";
			if (lang.equals("de")) fn = "Vorwaertsverfolgung_" + sif + "_" + id + (generateAllData ? "_all":"") + ".xlsx";
			if (save(workbook, outputFolder + File.separator + fn)) {
				result++;
			}
			myxls.close();
		}
		return result;
	}
	private boolean getHasTOB() {
		boolean result = false;
		ResultSet rsTOB = DBKernel.getResultSet("Select * from " + MyDBI.delimitL("Station") + " WHERE " + MyDBI.delimitL("Betriebsart") + " IS NOT NULL", false);
		try {
			if (rsTOB != null && rsTOB.first()) result = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	private int getFortraceRequests(String outputFolder, List<String> business2Trace, Integer stationId) throws SQLException, IOException, InvalidFormatException {
		int result = 0;
		String tracingBusinessesSQL = "";
		String tracingIdSQL = "";
		if (stationId != null) {
			tracingIdSQL += " AND " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " = " + stationId;
			tracingBusinessesSQL = " OR TRUE ";
		}
		else {
			if (business2Trace != null) {
				for (String s : business2Trace) {
					tracingBusinessesSQL += " OR " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Betriebsart") + " = '" + s + "'";
				}				
			}
		}
		String sql = "Select * from " + MyDBI.delimitL("Lieferungen") +
				" LEFT JOIN " + MyDBI.delimitL("Chargen") +
				" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
				" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
				" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
				" LEFT JOIN " + MyDBI.delimitL("Station") +
				" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") +
				" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
				" ON " + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") +
				" WHERE " +
				(do2017Format && generateAllData ? "" : MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Produkt") + " IS NULL AND ") +
				" (" + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Betriebsart") + " IS NULL " + tracingBusinessesSQL + ")" +
				tracingIdSQL +
				" ORDER BY " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " ASC," + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") + " ASC";
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				if (do2017Format) {
					result += getSimpleFwdStationRequests(outputFolder, rs);
				}
				else {
					InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Fortrace_sug.xlsx");
					XSSFWorkbook workbook = new XSSFWorkbook(myxls);
					XSSFSheet sheetTracing = workbook.getSheet("FwdTracing");
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
					if (rs.getObject("Lieferungen.Empfänger") != null) {
						sid = getStationLookup(rs.getString("Lieferungen.Empfänger"));
						cell = row.getCell(1); cell.setCellValue(sid);
						cell = row.getCell(2); evaluator.evaluateFormulaCell(cell);
					}
					
					// Ingredients for Lot(s)
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
					LinkedHashSet<String> deliveryNumbers = new LinkedHashSet<>();
					int rowIndex = 9;
					row = sheetTracing.getRow(rowIndex);
					String dn = fillRow(dvHelper, sheetTracing, rs, row, de, true, null);
					deliveryNumbers.add(dn);
					
					while (rs.next()) {
						if (rs.getObject("Station.Serial") == null) break;
						String sl = getStationLookup(rs);
						if (!sl.equals(sid)) break;
						rowIndex++;
						row = copyRow(workbook, sheetTracing, 9, rowIndex);
						dn = fillRow(dvHelper, sheetTracing, rs, row, de, true, null);
						deliveryNumbers.add(dn);
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
					for (String dns : deliveryNumbers) {
						if (!dns.isEmpty()) {
							if (i > 0) row = copyRow(workbook, sheetTracing, rowIndex, rowIndex + i);
							//todo cell = row.getCell(4); cell.setCellValue(dns);
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
					
					// Products Out
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
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 3, "1", "31");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 4, "1", "12");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 5, "1900", "3000");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 6, "1", "31");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 7, "1", "12");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 8, "1900", "3000");
						insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 9);
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 10, "=Units");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 11, "=StationIDs");
						//row = sheetTracing.getRow(rowIndex+i);
						//cell = row.getCell(12);
						//cell.setCellFormula("INDEX(Companies,MATCH(L" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
						//evaluator.evaluateFormulaCell(cell);
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 0, "=LotNumbers");
					}
					for (i=0;i<deliveryNumbers.size();i++) {
						insertDropBox(dvHelper, sheetTracing, 9+i, 0, "=LotNumbers");
					}
					
					//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
					if (save(workbook, outputFolder + File.separator + "Fwdtrace_request_" + getValidFileName(rs.getString("Station.Serial")) + ".xlsx")) { //  + "_" + getFormattedDate()
						result++;
					}
					myxls.close();
				}
			} while (rs.next());
		}
		return result;
	}
	@SuppressWarnings("unused")
	private String getFormattedDate() {
		long yourmilliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date resultdate = new Date(yourmilliseconds);
		return sdf.format(resultdate);		
	}
	
	private int getFwdStationRequests(String outputFolder, Station station) throws SQLException, IOException, NumberFormatException, InvalidFormatException {
		if (do2017Format) return getFortraceRequests(outputFolder, null, Integer.parseInt(station.getId2017()));
		int result = 0;
		String sql = "Select * from " + MyDBI.delimitL("Station") + " AS " + MyDBI.delimitL("S") +
				" LEFT JOIN " + MyDBI.delimitL("Lieferungen") +
				" ON " + MyDBI.delimitL("S") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") +
				" LEFT JOIN " + MyDBI.delimitL("Chargen") +
				" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
				" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
				" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
				" LEFT JOIN " + MyDBI.delimitL("Station") +
				" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
				" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
				" ON " + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") +
				" WHERE " + MyDBI.delimitL("S") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
				" AND " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " IS NOT NULL" +
				" ORDER BY " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Bezeichnung") + " ASC";
		/*
		String sql = "Select * from " + MyDBI.delimitL("Lieferungen") +
				" LEFT JOIN " + MyDBI.delimitL("Chargen") +
				" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
				" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
				" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
				" LEFT JOIN " + MyDBI.delimitL("Station") +
				" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") +
				" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
				" ON " + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") +
				" WHERE " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
				" ORDER BY " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Bezeichnung") + " ASC";
				*/
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
				InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Fortrace_sug.xlsx");
				XSSFWorkbook workbook = new XSSFWorkbook(myxls);
				XSSFSheet sheetTracing = workbook.getSheet("FwdTracing");
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
				String sid = station.getId();
				if (sid != null) {
					cell = row.getCell(1); cell.setCellValue(sid);
					cell = row.getCell(2); evaluator.evaluateFormulaCell(cell);
				}
				
				// Ingredients for Lot(s)
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
				LinkedHashSet<String> deliveryNumbers = new LinkedHashSet<>();
				List<Integer> dbLots = new ArrayList<>();
				int rowIndex = 9;
				row = sheetTracing.getRow(rowIndex);
				String dn = fillRow(dvHelper, sheetTracing, rs, row, de, true, null);
				deliveryNumbers.add(dn);
				dbLots.add(rs.getInt("ChargenVerbindungen.Produkt"));
				
				while (rs.next()) {
					if (rs.getObject("Station.Serial") == null) break;
					String sl = getStationLookup(rs);
					if (!sl.equals(sid)) break;
					rowIndex++;
					row = copyRow(workbook, sheetTracing, 9, rowIndex);
					dn = fillRow(dvHelper, sheetTracing, rs, row, de, true, null);
					deliveryNumbers.add(dn);
					dbLots.add(rs.getInt("ChargenVerbindungen.Produkt"));
				}

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
				sql = "Select * from " + MyDBI.delimitL("Station") +
						" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") + 
						" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
						" LEFT JOIN " + MyDBI.delimitL("Chargen") + 
						" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
						" LEFT JOIN " + MyDBI.delimitL("Lieferungen") +
						" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
						" WHERE " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
						" ORDER BY " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ChargenNr") + " ASC";
				rs = DBKernel.getResultSet(sql, false);
				int i=0;
				row = sheetTracing.getRow(rowIndex);
				LinkedHashMap<Integer, String> lotDb2Number = new LinkedHashMap<>();
				if (rs != null && rs.first()) {
					do {
						if (rs.getObject("Chargen.ID") != null && dbLots.contains(rs.getInt("Chargen.ID")) && !lotDb2Number.containsKey(rs.getInt("Chargen.ID"))) {
							if (i > 0) row = copyRow(workbook, sheetTracing, rowIndex, rowIndex + i);
							if (rs.getObject("Chargen.ChargenNr") != null) {cell = row.getCell(0); cell.setCellValue(rs.getString("Chargen.ChargenNr"));}
							if (rs.getObject("Chargen.Menge") != null) {cell = row.getCell(1); cell.setCellValue(rs.getDouble("Chargen.Menge"));}
							if (rs.getObject("Chargen.Einheit") != null) {cell = row.getCell(2); cell.setCellValue(rs.getString("Chargen.Einheit"));}							
							if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
								cell = row.getCell(3); cell.setCellValue(rs.getString("Produktkatalog.Bezeichnung"));
							}

							insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 1);
							insertDropBox(dvHelper, sheetTracing, rowIndex+i, 2, "=Units");
							insertDropBox(dvHelper, sheetTracing, rowIndex+i, 15, "=Treatment");
							insertDropBox(dvHelper, sheetTracing, rowIndex+i, 16, "=Sampling");
							i++;
							lotDb2Number.put(rs.getInt("Chargen.ID"), rs.getString("Chargen.ChargenNr"));
						}
					} while (rs.next());
				}
				if (i==0) i=1;
				
				Name reference = workbook.createName();
				reference.setNameName("LotNumbers");
				String referenceString = sheetTracing.getSheetName() + "!$A$" + (rowIndex+1) + ":$A$" + (rowIndex+i);
				reference.setRefersToFormula(referenceString);				

				for (int ii=0;ii<dbLots.size();ii++) {
					if (lotDb2Number.containsKey(dbLots.get(ii))) {
						row = sheetTracing.getRow(9+ii);
						cell = row.getCell(0);
						if (cell == null) cell = row.createCell(0);
						cell.setCellValue(lotDb2Number.get(dbLots.get(ii)));
					}
					insertDropBox(dvHelper, sheetTracing, 9+ii, 0, "=LotNumbers");
				}
						
				// Products Out
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

				if (rs != null && rs.first() && rs.getObject("Chargen.ChargenNr") != null) {
					boolean didOnce = false;
					do {
						if (didOnce) row = copyRow(workbook, sheetTracing, rowIndex-1, rowIndex);
						else row = sheetTracing.getRow(rowIndex);
						fillRow(dvHelper, sheetTracing, rs, row, de, false, null);
						rowIndex++;
						didOnce = true;
					} while (rs.next());
				}

				for (i=0;i<85;i++) {
					doFormats(dvHelper, sheetTracing, rowIndex+i, evaluator);
				}
				
				if (save(workbook, outputFolder + File.separator + "StationFwdtrace_request_" + getValidFileName(station.getId()) + ".xlsx")) { //  + "_" + getFormattedDate()
					result++;
				}
				myxls.close();
		}
		return result;
	}
	
	public static File getResourceAsFile(InputStream in) {
	    try {
	        File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
	        tempFile.deleteOnExit();

	        try (FileOutputStream out = new FileOutputStream(tempFile)) {
	            //copy stream
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            while ((bytesRead = in.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
	        }
	        return tempFile;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	private int getSimpleBackStationRequests(String outputFolder, ResultSet rs, boolean startTracing) throws SQLException, IOException, InvalidFormatException, URISyntaxException {
		int result = 0;
		if (rs.getObject("Station.ID") != null) {
			String template;
			if (hasTOB) {				
				if (lang.equals("de")) template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Backtrace_Prod_simple_tob_de.xlsx";
				else template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Backtrace_Prod_simple_tob_en.xlsx";
				if (startTracing) template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Backtrace_Start_simple_tob_en.xlsx";
			}
			else {
				if (lang.equals("de")) template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Backtrace_Prod_simple_de.xlsx";
				else template = "/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/FCL_Backtrace_Prod_simple_en.xlsx";
			}
			InputStream myxls = this.getClass().getResourceAsStream(template);
			File file = getResourceAsFile(myxls);
			myxls.close();
			OPCPackage opcPackage = OPCPackage.open(file.getAbsolutePath());
			XSSFWorkbook workbook = new XSSFWorkbook(opcPackage);
			//XSSFWorkbook workbook = new XSSFWorkbook(myxls);
			//save(workbook, outputFolder + File.separator + "StationBacktrace_request_" + (generateAllData ? "_all":"") + "_temp.xlsx", true);
			XSSFSheet sheetTracing = workbook.getSheet(startTracing ? XlsStruct.getBACK_SHEETNAME(lang) : XlsStruct.getPROD_BACK_SHEETNAME(lang)); //"Herstellung - Rückverfolgung");

			// Station in Focus
			String sif = null;
			Integer id = null;
			XSSFRow row = sheetTracing.getRow(0);
			XSSFCell cell;

			int countryCellNum = 3;
			for(int i = 0; i < sheetTracing.getNumMergedRegions(); i++) {
				CellRangeAddress region = sheetTracing.getMergedRegion(i);
				int rowNum = region.getFirstRow();
				int colIndex = region.getFirstColumn();
				if (rowNum == 0 && colIndex == 2) {
					countryCellNum = region.getLastColumn() + 1;
				}
			}
			id = rs.getInt("Station.ID");
			sif = rs.getString("Station.Name");
			cell = row.getCell(1); cell.setCellValue(sif);
			cell = row.getCell(2); cell.setCellValue(rs.getString("Station.Adresse"));
			if (rs.getString("Station.Land") == null)  row.getCell(countryCellNum).setCellValue("");
			else row.getCell(countryCellNum).setCellValue(rs.getString("Station.Land"));
			if (startTracing) {
				if (rs.getString("Station.Betriebsart") == null)  row.getCell(countryCellNum+1).setCellValue("");
				else row.getCell(countryCellNum+1).setCellValue(rs.getString("Station.Betriebsart"));
			}

			int rowIndex = 6;
			String stationID = rs.getString("Station.ID");
			HashMap<Integer, HashSet<Integer>> furtherDels = new HashMap<>();
			HashSet<Integer> alreadyUsedDels = new HashSet<>();
			if (startTracing) {
				rowIndex = 0;
			}
			else {
				// Products Out
				do {
					//System.err.println(rowIndex);
					if (rs.getObject("Station.ID") == null || !rs.getString("Station.ID").equals(stationID)) break;
					Integer did = rs.getInt("Lieferungen.ID");
					if (!alreadyUsedDels.contains(did)) {
						alreadyUsedDels.add(did);
						copyRow(workbook, sheetTracing, rowIndex, rowIndex+1);
						row = sheetTracing.getRow(rowIndex);								
						fillRowSimple(sheetTracing, rs, row, false, false);
						rowIndex++;
					}
					if (generateAllData) {
						if (rs.getObject("ChargenVerbindungen.Zutat") != null) {
							Integer key = rs.getInt("ChargenVerbindungen.Zutat");
							if (!furtherDels.containsKey(key)) furtherDels.put(key, new HashSet<>());
							furtherDels.get(key).add(rowIndex);
							//System.err.println(key + " -> " + rowIndex);
						}
					}
				} while (rs.next());
				rs.previous();
				
				row = sheetTracing.getRow(rowIndex+4);
				cell = row.getCell(0);
				if  (lang.equals("en")) cell.setCellValue("In Column A starting with Line Number " + (rowIndex+11) + " please enter the line number of the outgoing good being the product of this ingredient. Afterwards, enter the ingredient information in columns B to M.");
				else cell.setCellValue("In Spalte A ab Zeile " + (rowIndex+13) + " die Zeilennummer aus dem Warenausgang eintragen und ab Spalte B eine zugehörige Zutat (ggf. Tier) und die weiteren erfragten Angaben eintragen");
				
				//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
			}
			
			if (generateAllData || startTracing) {
				String sql = "Select * from " + MyDBI.delimitL("Lieferungen") +
						" LEFT JOIN " + MyDBI.delimitL("Chargen") +
						" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
						" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
						" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
						" LEFT JOIN " + MyDBI.delimitL("Station") +
						" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
						" WHERE " + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") + "=" + stationID +
						" ORDER BY " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ChargenNr") + " ASC";
				ResultSet rs2 = DBKernel.getResultSet(sql, false);
				if (rs2 != null && rs2.first()) {
					rowIndex += lang.equals("en") ? 10 : 12;
					int numCols = sheetTracing.getRow(rowIndex).getLastCellNum();
					do {
						//System.err.println(rs2.getString("Station.Name"));
						//copyRow(workbook, sheetTracing, rowIndex, rowIndex+1);
						//row = sheetTracing.getRow(rowIndex);								
			            row = sheetTracing.createRow(rowIndex); for (int ii=0;ii<numCols;ii++) row.createCell(ii);

						fillRowSimple(sheetTracing, rs2, row, true, startTracing);
						if (!startTracing) {
							Integer lid = rs2.getInt("Lieferungen.ID");
							if (furtherDels.containsKey(lid)) {
								HashSet<Integer> tl = furtherDels.get(lid);
								boolean afterFirst = false;
								for (int tli : tl) {
									if (afterFirst) {
										rowIndex++;
							            row = sheetTracing.createRow(rowIndex); for (int ii=0;ii<numCols;ii++) row.createCell(ii);
										fillRowSimple(sheetTracing, rs2, row, true, false);
									}
									cell = row.getCell(0);
									cell.setCellValue(tli+"");
									afterFirst = true;
								}
							}
						}
						rowIndex++;
					} while (rs2.next());
				}
			}
			String fn = "StationBacktrace_request_" + sif + "_" + id + (generateAllData ? "_all":"") + ".xlsx";
			if (lang.equals("de")) fn = "Rueckverfolgung_" + sif + "_" + id + (generateAllData ? "_all":"") + ".xlsx";
			if (startTracing) fn = "Start_Tracing_" + sif + ".xlsx";
			if (save(workbook, outputFolder + File.separator + fn)) {
				result++;
			}
			//myxls.close();
		}
		return result;
	}
	private int getBackStationRequests(String outputFolder, Station station) throws SQLException, IOException, NumberFormatException, InvalidFormatException, URISyntaxException {
		if (do2017Format) return getBacktraceRequests(outputFolder, null, Integer.parseInt(station.getId2017()));
		int result = 0;
		String sql = "Select * from " + MyDBI.delimitL("Station") +
					" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
					" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
					" LEFT JOIN " + MyDBI.delimitL("Chargen") + 
					" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
					" LEFT JOIN " + MyDBI.delimitL("Lieferungen") +
					" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
					" WHERE " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
					" ORDER BY " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ChargenNr") + " ASC";
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
				InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Backtrace_sug.xlsx");
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
				LinkedHashMap<Integer, String> lotDb2Number = new LinkedHashMap<>();
				int rowIndex = 9;
				row = sheetTracing.getRow(rowIndex);
				String ln = fillRow(dvHelper, sheetTracing, rs, row, de, false, null);
				if (!lotNumbers.containsKey(ln)) {
					Lot l = new Lot();
					l.setNumber(ln);
					if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
					if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
					if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
						Product p = new Product();
						p.setName(rs.getString("Produktkatalog.Bezeichnung"));
						l.setProduct(p);
					}
					l.setDbId(rs.getInt("Chargen.ID"));
					lotNumbers.put(ln, l);
				}
				lotDb2Number.put(rs.getInt("Chargen.ID"), ln);
				
				while (rs.next()) {
					if (rs.getObject("Station.Serial") == null) break;
					String sl = getStationLookup(rs);
					if (!sl.equals(sid)) break;
					rowIndex++;
					row = copyRow(workbook, sheetTracing, 9, rowIndex);
					ln = fillRow(dvHelper, sheetTracing, rs, row, de, false, null);
					if (!lotNumbers.containsKey(ln)) {
						Lot l = new Lot();
						l.setNumber(ln);
						if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
						if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
						if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
							Product p = new Product();
							p.setName(rs.getString("Produktkatalog.Bezeichnung"));
							l.setProduct(p);
						}
						l.setDbId(rs.getInt("Chargen.ID"));
						lotNumbers.put(ln, l);
					}
					lotDb2Number.put(rs.getInt("Chargen.ID"), ln);
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
						if (lot.getProduct() != null && lot.getProduct().getName() != null) {
							cell = row.getCell(3); cell.setCellValue(lot.getProduct().getName());
						}
						LinkedHashSet<String> le0 = new LinkedHashSet<>();
						le0.add("Production Date");
						le0.add("Best before date");
						le0.add("Treatment of product during production");
						le0.add("Sampling");
						le0.addAll(le);
						fillExtraFields("Chargen", lot.getDbId(), row, le0, 13);
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
				
				String sif = getValidFileName(rs.getString("Station.Serial")); //  + "_" + getFormattedDate()

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

				sql = "Select * from " + MyDBI.delimitL("Station") + " AS " + MyDBI.delimitL("S") +
						" LEFT JOIN " + MyDBI.delimitL("Lieferungen") +
						" ON " + MyDBI.delimitL("S") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") +
						" LEFT JOIN " + MyDBI.delimitL("Chargen") +
						" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
						" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
						" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
						" LEFT JOIN " + MyDBI.delimitL("Station") +
						" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
						" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
						" ON " + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") +
						" WHERE " + MyDBI.delimitL("S") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
						" AND " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " IS NOT NULL" +			
						" ORDER BY " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Bezeichnung") + " ASC";
				//System.out.println(sql);
				rs = DBKernel.getResultSet(sql, false);
				if (rs != null && rs.first()) {
					LinkedHashSet<String> deliveryNumbers = new LinkedHashSet<>();
					row = sheetTracing.getRow(rowIndex);
					String dn = fillRow(dvHelper, sheetTracing, rs, row, de, null, lotDb2Number);
					doFormats(dvHelper, sheetTracing, rowIndex, evaluator);
					deliveryNumbers.add(dn);
					
					boolean didOnce = false;
					while (rs.next()) {
						if (rs.getObject("Station.Serial") == null) break;
						String sl = getStationLookup(rs);
						if (!sl.equals(sid)) break;
						rowIndex++;
						if (didOnce) row = copyRow(workbook, sheetTracing, rowIndex-1, rowIndex);
						else row = sheetTracing.getRow(rowIndex);
						dn = fillRow(dvHelper, sheetTracing, rs, row, de, null, lotDb2Number);
						doFormats(dvHelper, sheetTracing, rowIndex, evaluator);
						deliveryNumbers.add(dn);		
						didOnce = true;
					}
					rowIndex++;
				}
				for (i=0;i<84;i++) {
					doFormats(dvHelper, sheetTracing, rowIndex+i, evaluator);
				}
				
				//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
				if (save(workbook, outputFolder + File.separator + "StationBacktrace_request_" + sif + ".xlsx")) {
					result++;
				}
				myxls.close();
		}
		return result;
	}
	private void doFormats(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int rowIndex, FormulaEvaluator evaluator) {
		insertCondition(dvHelper, sheetTracing, rowIndex, 3, "1", "31");
		insertCondition(dvHelper, sheetTracing, rowIndex, 4, "1", "12");
		insertCondition(dvHelper, sheetTracing, rowIndex, 5, "1900", "3000");
		insertCondition(dvHelper, sheetTracing, rowIndex, 6, "1", "31");
		insertCondition(dvHelper, sheetTracing, rowIndex, 7, "1", "12");
		insertCondition(dvHelper, sheetTracing, rowIndex, 8, "1900", "3000");
		insertDecCondition(dvHelper, sheetTracing, rowIndex, 9);
		insertDropBox(dvHelper, sheetTracing, rowIndex, 10, "=Units");
		insertDropBox(dvHelper, sheetTracing, rowIndex, 11, "=StationIDs");
		//XSSFRow row = sheetTracing.getRow(rowIndex);
		//XSSFCell cell = row.getCell(12);
		//cell.setCellFormula("INDEX(Companies,MATCH(L" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
		//evaluator.evaluateFormulaCell(cell);
		insertDropBox(dvHelper, sheetTracing, rowIndex, 0, "=LotNumbers");		
	}
	private int getBacktraceRequests(String outputFolder, List<String> business2Backtrace, Integer stationId) throws SQLException, IOException, InvalidFormatException, URISyntaxException {
		int result = 0;
		String sql;
			String backtracingBusinessesSQL = "";
			String backtracingIdSQL = "";
			if (stationId != null) {
				backtracingIdSQL += " AND " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " = " + stationId;
				backtracingBusinessesSQL = " OR TRUE ";
			}
			else {
				if (business2Backtrace != null) {
					for (String s : business2Backtrace) {
						backtracingBusinessesSQL += " OR " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Betriebsart") + " = '" + s + "'";
					}				
				}
			}
			sql = "Select * from " + MyDBI.delimitL("Lieferungen") +
					" LEFT JOIN " + MyDBI.delimitL("Chargen") +
					" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
					" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
					" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
					" LEFT JOIN " + MyDBI.delimitL("Station") +
					" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
					" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
					" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Produkt") +
					" WHERE " +
					(do2017Format && generateAllData ? "" : MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") + " IS NULL AND ") +
					" (" + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Betriebsart") + " IS NULL " + backtracingBusinessesSQL + ")" +
					backtracingIdSQL +
					" ORDER BY " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " ASC," + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Produkt") + " ASC";
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		boolean startTracing = false;
		if (do2017Format && generateAllData && (rs == null || !rs.first())) {
			sql = "Select * from " + MyDBI.delimitL("Station") +
			" WHERE " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + stationId + backtracingBusinessesSQL;	
			rs = DBKernel.getResultSet(sql, false);
			startTracing = true;
		}
		if (rs != null && rs.first()) {
			do {
				if (do2017Format) {
					result += getSimpleBackStationRequests(outputFolder, rs, startTracing);
				}
				else {
					InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Backtrace_sug.xlsx");
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
					String ln = fillRow(dvHelper, sheetTracing, rs, row, de, false, null);
					if (!lotNumbers.containsKey(ln)) {
						Lot l = new Lot();
						l.setNumber(ln);
						if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
						if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
						if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
							Product p = new Product();
							p.setName(rs.getString("Produktkatalog.Bezeichnung"));
							l.setProduct(p);
						}
						l.setDbId(rs.getInt("Chargen.ID"));
						lotNumbers.put(ln, l);
					}
					
					while (rs.next()) {
						if (rs.getObject("Station.Serial") == null) break;
						String sl = getStationLookup(rs);
						if (!sl.equals(sid)) break;
						rowIndex++;
						row = copyRow(workbook, sheetTracing, 9, rowIndex);
						ln = fillRow(dvHelper, sheetTracing, rs, row, de, false, null);
						if (!lotNumbers.containsKey(ln)) {
							Lot l = new Lot();
							l.setNumber(ln);
							if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
							if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
							if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
								Product p = new Product();
								p.setName(rs.getString("Produktkatalog.Bezeichnung"));
								l.setProduct(p);
							}
							l.setDbId(rs.getInt("Chargen.ID"));
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
							if (lot.getProduct() != null && lot.getProduct().getName() != null) {
								cell = row.getCell(3); cell.setCellValue(lot.getProduct().getName());
							}
							LinkedHashSet<String> le0 = new LinkedHashSet<>();
							le0.add("Production Date");
							le0.add("Best before date");
							le0.add("Treatment of product during production");
							le0.add("Sampling");
							le0.addAll(le);
							fillExtraFields("Chargen", lot.getDbId(), row, le0, 13);
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
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 3, "1", "31");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 4, "1", "12");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 5, "1900", "3000");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 6, "1", "31");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 7, "1", "12");
						insertCondition(dvHelper, sheetTracing, rowIndex+i, 8, "1900", "3000");
						insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 9);
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 10, "=Units");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 11, "=StationIDs");
						//row = sheetTracing.getRow(rowIndex+i);
						//cell = row.getCell(12);
						//cell.setCellFormula("INDEX(Companies,MATCH(L" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
						//evaluator.evaluateFormulaCell(cell);
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 0, "=LotNumbers");
					}
					
					//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
					if (save(workbook, outputFolder + File.separator + "Backtrace_request_" + getValidFileName(rs.getString("Station.Serial")) + ".xlsx")) { //  + "_" + getFormattedDate()
						result++;
					}
					myxls.close();
				}
			} while (rs.next());
		}
		return result;
	}
	
	private void insertDropBox(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col, String ref) {
		if (dvHelper != null) {
			XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint(ref);
			CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
			XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
			validation.setShowErrorBox(true);
			validation.setSuppressDropDownArrow(true);
			validation.setShowPromptBox(true);
			sheetTracing.addValidationData(validation);
		}
	}
	private void insertCondition(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col, String min, String max) {
		if (dvHelper != null) {
			XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createIntegerConstraint(OperatorType.BETWEEN, min, max);
			// dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"0000011", "0000021", "0000031"});
			CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
			XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
			validation.setShowErrorBox(true);
			validation.setSuppressDropDownArrow(true);
			validation.setShowPromptBox(true);
			sheetTracing.addValidationData(validation);
		}
	}
	private void insertDecCondition(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col) {
		if (dvHelper != null) {
			XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createDecimalConstraint(OperatorType.GREATER_OR_EQUAL, "0", "");
			// dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"0000011", "0000021", "0000031"});
			CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
			XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
			validation.setShowErrorBox(true);
			validation.setSuppressDropDownArrow(true);
			validation.setShowPromptBox(true);
			sheetTracing.addValidationData(validation);			
		}
	}
	
	private String fillRow(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, ResultSet rs, XSSFRow row, LinkedHashSet<String> de, Boolean isForward, LinkedHashMap<Integer, String> lotDb2Number) throws SQLException {
		String result = null;
		
		XSSFCell cell;
		if (isForward == null || isForward) {
			cell = row.getCell(1);
			if (rs.getObject("Produktkatalog.Bezeichnung") != null) cell.setCellValue(rs.getString("Produktkatalog.Bezeichnung"));
			else cell.setCellValue("");
			cell = row.getCell(2);
			if (rs.getObject("Chargen.ChargenNr") != null) cell.setCellValue(rs.getString("Chargen.ChargenNr"));
			else cell.setCellValue("(autoLot" + row.getRowNum() + ")");
			result = cell.getStringCellValue();
		}
		else {
			cell = row.getCell(0);
			if (rs.getObject("Chargen.ChargenNr") != null) cell.setCellValue(rs.getString("Chargen.ChargenNr"));
			else cell.setCellValue("(autoLot" + row.getRowNum() + ")");
			result = cell.getStringCellValue();
		}
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 3, "1", "31");
		cell = row.getCell(3);
		if (rs.getObject("Lieferungen.dd_day") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_day"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 4, "1", "12");
		cell = row.getCell(4);
		if (rs.getObject("Lieferungen.dd_month") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_month"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 5, "1900", "3000");
		cell = row.getCell(5);
		if (rs.getObject("Lieferungen.dd_year") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_year"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 6, "1", "31");
		cell = row.getCell(6);
		if (rs.getObject("Lieferungen.ad_day") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_day"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 7, "1", "12");
		cell = row.getCell(7);
		if (rs.getObject("Lieferungen.ad_month") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_month"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 8, "1900", "3000");
		cell = row.getCell(8);
		if (rs.getObject("Lieferungen.ad_year") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_year"));
		else cell.setCellValue("");
		insertDecCondition(dvHelper, sheetTracing, row.getRowNum(), 9);
		cell = row.getCell(9);
		if (rs.getObject("Lieferungen.numPU") != null) cell.setCellValue(rs.getDouble("Lieferungen.numPU"));
		else cell.setCellValue("");
		insertDropBox(dvHelper, sheetTracing, row.getRowNum(), 10, "=Units");
		cell = row.getCell(10);
		if (rs.getObject("Lieferungen.typePU") != null) cell.setCellValue(rs.getString("Lieferungen.typePU"));
		else cell.setCellValue("");
		
		cell = row.getCell(11);
		String stationBez = "Lieferungen.Empfänger";
		if (isForward == null || isForward) stationBez = "Produktkatalog.Station";
		if (rs.getObject(stationBez) != null) cell.setCellValue(getStationLookup(rs.getString(stationBez)));
		else cell.setCellValue("");
		//cell = row.getCell(12);
		//cell.setCellFormula("INDEX(Companies,MATCH(L" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
		//evaluator.evaluateFormulaCell(cell);
		
		if (isForward == null) {
			cell = row.getCell(0);
			if (rs.getObject("ChargenVerbindungen.Produkt") != null && lotDb2Number != null && lotDb2Number.containsKey(rs.getInt("ChargenVerbindungen.Produkt"))) cell.setCellValue(lotDb2Number.get(rs.getInt("ChargenVerbindungen.Produkt")));
			else cell.setCellValue("");
		}
		// DeliveryID
		cell = row.getCell(12);
		if (rs.getObject("Lieferungen.Serial") != null) cell.setCellValue(rs.getString("Lieferungen.Serial"));
		else if (rs.getObject("Lieferungen.ID") != null) cell.setCellValue(rs.getString("Lieferungen.ID"));
		else cell.setCellValue("");

		if (isForward == null || isForward) result = cell.getStringCellValue();
		
		fillExtraFields("Lieferungen", rs.getObject("Lieferungen.ID"), row, de, 13);
		/*
		// ExtraFields
		if (rs.getObject("Lieferungen.ID") != null) {
			String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Lieferungen' AND " + MyDBI.delimitL("id") + "=" + rs.getInt("Lieferungen.ID");
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
		*/
		
		return result;
	}
	private void fillExtraCell(XSSFCell cell, String tablename, String attribute, Object id) throws SQLException {
		// ExtraFields
		if (id != null) {
			String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='" + tablename  + "' AND " + MyDBI.delimitL("attribute") + "='" + attribute + "'  AND " + MyDBI.delimitL("id") + "=" + id;
			ResultSet rs2 = DBKernel.getResultSet(sql, false);
			if (rs2 != null && rs2.first()) {
				cell.setCellValue(rs2.getString("value"));
				if (rs2.next()) System.err.println("Achtung!!! Mehrere Extrafields mit denselben Parametern befüllt!!!!");
			}	
			else {
				cell.setCellValue("");
			}
		}		
	}
	private void fillRowSimple(XSSFSheet sheetTracing, ResultSet rs, XSSFRow row, Boolean isForward, boolean startTracing) throws SQLException {
		XSSFCell cell;
		
		// Product
		cell = row.getCell(1+(startTracing?3:0));
		if (rs.getObject("Produktkatalog.Bezeichnung") != null) cell.setCellValue(rs.getString("Produktkatalog.Bezeichnung"));
		else cell.setCellValue("");
		cell = row.getCell(2+(startTracing?3:0));
		fillExtraCell(cell, "Produktkatalog", XlsProduct.EAN("en"), rs.getString("Produktkatalog.ID"));
		
		// Charge
		cell = row.getCell(3+(startTracing?3:0));
		if (rs.getObject("Chargen.ChargenNr") != null) cell.setCellValue(rs.getString("Chargen.ChargenNr"));
		else cell.setCellValue("");
		cell = row.getCell(4+(startTracing?3:0));
		fillExtraCell(cell, "Chargen", XlsLot.MHD("en"), rs.getString("Chargen.ID"));
		
		// Delivery
		cell = row.getCell(5+(startTracing?3:0));
		if (rs.getObject(isForward ? "Lieferungen.ad_day" : "Lieferungen.ad_day") != null) cell.setCellValue(isForward ? rs.getString("Lieferungen.ad_day") : rs.getString("Lieferungen.ad_day"));
		else cell.setCellValue("");		
		cell = row.getCell(6+(startTracing?3:0));
		if (rs.getObject(isForward ? "Lieferungen.ad_month" : "Lieferungen.ad_month") != null) cell.setCellValue(rs.getString(isForward ? "Lieferungen.ad_month" : "Lieferungen.ad_month"));
		else cell.setCellValue("");		
		cell = row.getCell(7+(startTracing?3:0));
		if (rs.getObject(isForward ? "Lieferungen.ad_year" : "Lieferungen.ad_year") != null) cell.setCellValue(rs.getString(isForward ? "Lieferungen.ad_year" : "Lieferungen.ad_year"));
		else cell.setCellValue("");		
		cell = row.getCell(8+(startTracing?3:0));
		fillExtraCell(cell, "Lieferungen", "Amount", rs.getString("Lieferungen.ID"));
		fillStationSimple(row.getCell(startTracing?0:9), row.getCell(startTracing?1:10), row.getCell(startTracing?2:11), hasTOB ? row.getCell(startTracing?3:12) : null, rs.getObject(isForward ? "Produktkatalog.Station" : "Lieferungen.Empfänger"));
		cell = row.getCell(hasTOB ? 13 : 12);
		if (rs.getObject("Lieferungen.Kommentar") != null) cell.setCellValue(rs.getString("Lieferungen.Kommentar"));
		else cell.setCellValue("");		
	}
	private void fillStationSimple(XSSFCell cellName, XSSFCell cellAddress, XSSFCell countryAddress, XSSFCell cellTOB, Object stationID) throws SQLException {
		cellName.setCellValue("");
		cellAddress.setCellValue("");
		if (stationID != null) {
			String sql = "Select * from " + MyDBI.delimitL("Station") + " WHERE " + MyDBI.delimitL("ID") + "='" + stationID + "'";
			ResultSet rs = DBKernel.getResultSet(sql, false);
			if (rs != null && rs.first()) {
				if (rs.getObject("Name") != null) cellName.setCellValue(rs.getString("Name"));
				if (rs.getObject("Adresse") != null) cellAddress.setCellValue(rs.getString("Adresse"));
				if (rs.getObject("Land") != null) countryAddress.setCellValue(rs.getString("Land"));
				if (cellTOB != null && rs.getObject("Betriebsart") != null) cellTOB.setCellValue(rs.getString("Station.Betriebsart"));

			}
		}
	}
	private void fillExtraFields(String tablename, Object id, XSSFRow row, LinkedHashSet<String> de, int startCol) throws SQLException {
		// ExtraFields
		if (id != null && de != null) {
			String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='" + tablename  + "' AND " + MyDBI.delimitL("id") + "=" + id;
			ResultSet rs2 = DBKernel.getResultSet(sql, false);
			if (rs2 != null && rs2.first()) {
				do {
					String s = rs2.getString("attribute");
					int j=0;
					for (String e : de) {
						if (s.equalsIgnoreCase(e)) {
							XSSFCell cell = row.getCell(startCol+j);
							if (cell == null) cell = row.createCell(startCol+j);
							cell.setCellValue(rs2.getString("value"));
							break;
						}
						j++;
					}
				} while (rs2.next());
			}	
		}		
	}
	/*
	private void copyStyle(XSSFWorkbook workbook, XSSFSheet worksheet, int sourceRow, int lastRow) {
		
        // Copy style from old cell and apply to new cell
		XSSFRow row = worksheet.getRow(sourceRow);
		
        XSSFCellStyle newCellStyle = workbook.createCellStyle();
        newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
        
        newCell.setCellStyle(newCellStyle);

	}
	*/
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

	            newCell.setCellStyle(oldCell.getCellStyle());

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
	        
	        newRow.setHeight(sourceRow.getHeight());
	        
	        return newRow;
	    }
	   private boolean save(XSSFWorkbook workbook, String filename) {
		   return save(workbook, filename, false);
	   }
	   private boolean save(XSSFWorkbook workbook, String filename, boolean forceOverwrite) {
		try {
			File f = new File(filename);
			if (!forceOverwrite && f.exists()) {
				int returnVal = JOptionPane.showConfirmDialog(parent, "Replace file '" + filename + "'?", "Excel file '" + filename + "' exists already", JOptionPane.YES_NO_OPTION);
				if (returnVal == JOptionPane.NO_OPTION) return false;
				else if (returnVal == JOptionPane.YES_OPTION) ;
				else return false;
			}
			POIXMLProperties.CoreProperties coreProp = workbook.getProperties().getCoreProperties();
			coreProp.setCreator("FoodChain-Lab");
			coreProp.setCreated(new Nullable<Date>(new Date(System.currentTimeMillis())));
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(f);
			System.err.println("outb" + System.currentTimeMillis());
			workbook.write(out);
			System.err.println("outa" + System.currentTimeMillis());
			out.flush();
			out.close();
			System.out.println(filename + " written successfully on disk.");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	   private String getValidFileName(String fileName) {
		    String newFileName = fileName.replaceAll("[:\\\\/*?|<>]", "_");
		    if (newFileName.length()==0)
		        throw new IllegalStateException(
		                "File Name " + fileName + " results in an empty fileName!");
		    return newFileName;
		}	   
}
