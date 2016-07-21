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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.imports;

/**
 * 
 */

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.Levenshtein;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.gui.dbtree.MyDBTree;

/**
 * @author Armin
 * 
 */
public class GeneralXLSImporter extends FileFilter implements MyImporter {

	private boolean takecareofID = false; // in case of INSERTs!!!

	public GeneralXLSImporter() {
	}

	public GeneralXLSImporter(boolean takecareofID) {
		this.takecareofID = takecareofID;
	}

	/**
	 * This is the one of the methods that is declared in the abstract class
	 */
	public boolean accept(File f) {
		if (f.isDirectory()) return true;

		String extension = getExtension(f);
		if ((extension.equals("xls"))) return true;
		return false;
	}

	public String getDescription() {
		return "General Excel Importer (*.xls)";
	}

	private String getExtension(File f) {
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) return s.substring(i + 1).toLowerCase();
		return "";
	}

	public boolean doImport(final String filename, final JProgressBar progress, final boolean showResults) {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					if (progress != null) {
						progress.setVisible(true);
						progress.setStringPainted(true);
						progress.setString("Importiere Excel Datei...");
						progress.setMinimum(0);
					}

					InputStream is = null;
					System.out.println(filename);
					if (filename.startsWith("http://")) {
						URL url = new URL(filename);
						URLConnection uc = url.openConnection();
						is = uc.getInputStream();
					} else if (filename.startsWith("/de/bund/bfr/knime/openkrise/db/res/")) {
						is = this.getClass().getResourceAsStream(filename);
					} else {
						is = new FileInputStream(filename);
					}

					try (HSSFWorkbook wb = new HSSFWorkbook(new POIFSFileSystem(is))) {					
					HSSFSheet sheet;
					HSSFRow row;

					int numSuccess = 0;
					int numFailed = 0;
					String unusedFields = "";
					for (int i = 0; i < wb.getNumberOfSheets(); i++) {
						sheet = wb.getSheetAt(i);
						String tableName = sheet.getSheetName();
						MyTable myT = DBKernel.myDBi.getTable(tableName);
						if (myT != null) {
							int numRows = sheet.getLastRowNum();
							if (progress != null) {
								progress.setMaximum(numRows);
								progress.setValue(0);
							}

							row = sheet.getRow(0);
							String sql1 = "";
							String sql2 = "";
							String sql3 = "";
							Vector<String> codeSql1 = new Vector<>();
							Vector<String> codeSql2 = new Vector<>();
							LinkedHashMap<MyTable, Vector<Integer>> foreignTables = new LinkedHashMap<>();
							int numCols = row.getLastCellNum();
							String[] fieldNames = new String[numCols];
							String[] fieldTypes = new String[numCols];//getTypes(fieldNames, myT);
							String[] ffieldTypes = new String[numCols];
							MyTable[] myForeignTables = new MyTable[numCols];
							String[] kzS = new String[numCols];
							String[] dbFieldnames = new String[numCols];
							int lfdCol = 0;
							Hashtable<String, String> dbFieldNames = new Hashtable<>();
							for (int j = 0; j < numCols; j++) {
								String fieldName = row.getCell(j).getStringCellValue();
								fieldNames[j] = fieldName;
								int ffe;
								String dbFieldName = getDBFieldName(fieldName, myT, takecareofID);
								if (dbFieldName != null) {
									String ft = getForeignTable(dbFieldName, myT);
									if (ft != null && ft.equals("DoubleKennzahlen")) {
										kzS[j] = getKZ(fieldName, dbFieldName);
										dbFieldnames[j] = dbFieldName;
									} else if (!dbFieldNames.containsKey(dbFieldName)) {
										dbFieldNames.put(dbFieldName, dbFieldName);
										sql1 += DBKernel.delimitL(dbFieldName) + ",";
										sql2 += "?,";
										sql3 += DBKernel.delimitL(dbFieldName) + "=?,";
										lfdCol++;
									}
									fieldTypes[j] = getType(dbFieldName, myT, takecareofID);
								} else if ((ffe = foreignFieldExists(fieldName, myT)) >= 0) {
									if (!foreignTables.containsKey(myT.getForeignFields()[ffe])) foreignTables.put(myT.getForeignFields()[ffe], new Vector<Integer>());
									ffieldTypes[j] = getType(fieldName, myT.getForeignFields()[ffe], false);
									foreignTables.get(myT.getForeignFields()[ffe]).add(j);
									myForeignTables[j] = myT.getForeignFields()[ffe];
								} else if (DBKernel.showHierarchic(tableName) && fieldName.toLowerCase().endsWith("-code")) {
									codeSql1.add(DBKernel.delimitL("CodeSystem") + "," + DBKernel.delimitL("Code") + "," + DBKernel.delimitL("Basis"));
									codeSql2.add("'" + fieldName.substring(0, fieldName.length() - "-code".length()) + "',?,?");
								} else if (!fieldName.equalsIgnoreCase("id")) {
									unusedFields += "," + fieldName;
								}
							}
							if (sql1.length() > 0 && sql2.length() > 0) {
								String sql = "INSERT INTO " + DBKernel.delimitL(tableName) + " (" + sql1.substring(0, sql1.length() - 1) + ") VALUES ("
										+ sql2.substring(0, sql2.length() - 1) + ")";
								PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
								int idCol = lfdCol + 1;
								sql = "UPDATE " + DBKernel.delimitL(tableName) + " SET " + sql3.substring(0, sql3.length() - 1) + " WHERE " + DBKernel.delimitL("ID") + "=?";
								PreparedStatement psUpdate = DBKernel.getDBConnection().prepareStatement(sql);
								PreparedStatement[] psCodes = new PreparedStatement[codeSql1.size()];
								boolean doCode[] = new boolean[codeSql1.size()];
								int codesI;
								for (codesI = 0; codesI < codeSql1.size(); codesI++) {
									sql = "INSERT INTO " + DBKernel.delimitL(DBKernel.getCodesName(tableName)) + " (" + codeSql1.get(codesI) + ") VALUES (" + codeSql2.get(codesI)
											+ ")";
									psCodes[codesI] = DBKernel.getDBConnection().prepareStatement(sql);
								}
								LinkedHashMap<MyTable, PreparedStatement> psForeign = new LinkedHashMap<>();
								LinkedHashMap<MyTable, PreparedStatement> psForeignUpdate = new LinkedHashMap<>();
								for (Map.Entry<MyTable, Vector<Integer>> entry : foreignTables.entrySet()) {
									Vector<Integer> vs = entry.getValue();
									String ssql1 = "", ssql2 = "", ssql3 = "";
									for (int ii = 0; ii < vs.size(); ii++) {
										ssql1 += "," + DBKernel.delimitL(fieldNames[vs.get(ii)]);
										ssql2 += ",?";
										ssql3 += "," + DBKernel.delimitL(fieldNames[vs.get(ii)]) + "=?";
									}
									if (ssql1.length() > 0 && ssql2.length() > 0 && ssql3.length() > 0) {
										sql = "INSERT INTO " + DBKernel.delimitL(entry.getKey().getTablename()) + " (" + ssql1.substring(1) + ") VALUES (" + ssql2.substring(1)
												+ ")";
										psForeign.put(entry.getKey(), DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS));
										sql = "UPDATE " + DBKernel.delimitL(entry.getKey().getTablename()) + " SET " + ssql3.substring(1) + " WHERE " + DBKernel.delimitL("ID")
												+ "=?";
										psForeignUpdate.put(entry.getKey(), DBKernel.getDBConnection().prepareStatement(sql));
									}
								}

								LinkedHashMap<Object, String> hashBL = null;
								Iterator<Row> rows = sheet.rowIterator();
								int lfd = 0;
								while (rows.hasNext()) {
									row = (HSSFRow) rows.next();
									boolean setID = false;
									Integer lastID = null;
									ps.clearParameters();
									psUpdate.clearParameters();
									for (codesI = 0; codesI < codeSql1.size(); codesI++) {
										psCodes[codesI].clearParameters();
										doCode[codesI] = false;
									}
									LinkedHashMap<MyTable, Integer> lfdColsForeign = new LinkedHashMap<>();
									for (Map.Entry<MyTable, PreparedStatement> entry : psForeignUpdate.entrySet()) {
										MyTable myT1 = entry.getKey();
										psForeign.get(myT1).clearParameters();
										psForeignUpdate.get(myT1).clearParameters();
										lfdColsForeign.put(myT1, 0);
									}

									if (row.getRowNum() > 0) {
										lfdCol = 0;
										codesI = 0;
										Object[] kzVal = new Object[numCols];
										for (int j = 0; j < numCols; j++) {
											if (fieldTypes[j] != null) {
												/*
												 * if (fieldNames[j].equals(
												 * "Bundesland")) { hashBL =
												 * DBKernel
												 * .myDBi.getHashMap("County");
												 * } else { hashBL = null; }
												 */
												lfdCol++;
												if (fieldTypes[j].startsWith("VARCHAR(") || fieldTypes[j].startsWith("CHAR(") || fieldTypes[j].startsWith("BLOB(")
														&& !tableName.equals("DateiSpeicher")) manageString(ps, psUpdate, lfdCol, row.getCell(j), hashBL);
												else if (fieldTypes[j].equals("BOOLEAN")) manageBoolean(ps, psUpdate, lfdCol, row.getCell(j));
												else if (fieldTypes[j].equals("INTEGER")) manageInteger(ps, psUpdate, lfdCol, row.getCell(j));
												else if (fieldTypes[j].equals("BIGINT")) manageBigInteger(ps, psUpdate, lfdCol, row.getCell(j));
												else if (fieldTypes[j].equals("DATE")) manageDate(ps, psUpdate, lfdCol, row.getCell(j));
												else if (fieldTypes[j].equals("DOUBLE")) {
													if (kzS[j] != null) {
														lfdCol--;
														//System.err.println(dbFieldnames[j] + "\t" + kzS[j]);
														if (DBKernel.kzIsString(kzS[j])) kzVal[j] = manageString(null, null, lfdCol, row.getCell(j));
														else if (DBKernel.kzIsBoolean(kzS[j])) kzVal[j] = manageBoolean(null, null, lfdCol, row.getCell(j));
														else kzVal[j] = manageDouble(null, null, lfdCol, row.getCell(j));
													} else {
														manageDouble(ps, psUpdate, lfdCol, row.getCell(j));
													}
												} else System.err.println("Wasn hier los? Undefinierter Feldtyp???? ->\t" + fieldNames[j]);
											} else if (myForeignTables[j] != null && ffieldTypes[j] != null) {
												lfdColsForeign.put(myForeignTables[j], lfdColsForeign.get(myForeignTables[j]) + 1);
												if (ffieldTypes[j].startsWith("VARCHAR(") || ffieldTypes[j].startsWith("CHAR(") || ffieldTypes[j].startsWith("BLOB(")
														&& !tableName.equals("DateiSpeicher")) manageString(psForeign.get(myForeignTables[j]),
														psForeignUpdate.get(myForeignTables[j]), lfdColsForeign.get(myForeignTables[j]), row.getCell(j), hashBL);
												else if (ffieldTypes[j].equals("BOOLEAN")) manageBoolean(psForeign.get(myForeignTables[j]),
														psForeignUpdate.get(myForeignTables[j]), lfdColsForeign.get(myForeignTables[j]), row.getCell(j));
												else if (ffieldTypes[j].equals("INTEGER")) manageInteger(psForeign.get(myForeignTables[j]),
														psForeignUpdate.get(myForeignTables[j]), lfdColsForeign.get(myForeignTables[j]), row.getCell(j));
												else if (ffieldTypes[j].equals("BIGINT")) manageBigInteger(psForeign.get(myForeignTables[j]),
														psForeignUpdate.get(myForeignTables[j]), lfdColsForeign.get(myForeignTables[j]), row.getCell(j));
												else if (fieldTypes[j].equals("DATE")) manageDate(psForeign.get(myForeignTables[j]), psForeignUpdate.get(myForeignTables[j]),
														lfdColsForeign.get(myForeignTables[j]), row.getCell(j));
												else if (ffieldTypes[j].equals("DOUBLE")) {
													manageDouble(psForeign.get(myForeignTables[j]), psForeignUpdate.get(myForeignTables[j]),
															lfdColsForeign.get(myForeignTables[j]), row.getCell(j));
												} else System.err.println(fieldNames[j] + " Feldtype????");
											} else if (fieldNames[j].equals("ID")) {
												lastID = manageInteger(null, null, 0, row.getCell(j));
												if (lastID != null) {
													if (DBKernel.hasID(tableName, lastID.intValue())) {
														psUpdate.setInt(idCol, lastID.intValue());
														setID = true;
													}
												}
											} else if (DBKernel.showHierarchic(tableName) && fieldNames[j].toLowerCase().endsWith("-code")) {
												String code = manageString(psCodes[codesI], null, 1, row.getCell(j));
												if (code != null && code.length() > 0) doCode[codesI] = true;
												codesI++;
											} else {
												//System.out.println(fieldNames[j]);					      				
											}
										}
										try {
											if (setID) {
												psUpdate.execute();
											} else {
												if (ps.executeUpdate() > 0) {// execute()
													lastID = DBKernel.getLastInsertedID(ps);
												} else {
													System.err.println("W");
												}
											}
											numSuccess++;
											if (lastID != null) {
												for (int j = 0; j < numCols; j++) {
													if (dbFieldnames[j] != null && kzVal[j] != null) {
														DBKernel.insertDBL(myT.getTablename(), dbFieldnames[j], lastID, null, kzS[j], kzVal[j]);
													}
												}
												for (codesI = 0; codesI < codeSql1.size(); codesI++) {
													if (doCode[codesI]) {
														psCodes[codesI].setInt(2, lastID);
														try {
															psCodes[codesI].execute();
															numSuccess++;
														} catch (SQLException e1) {
															numFailed++;
															System.err.println(psCodes[codesI]);
														}
													}
												}

												for (Map.Entry<MyTable, PreparedStatement> entry : psForeign.entrySet()) {
													MyTable myT1 = entry.getKey();
													MyTable[] foreignTs = myT.getForeignFields();
													for (int ii = 0; ii < foreignTs.length; ii++) {
														if (foreignTs[ii] != null && foreignTs[ii].equals(myT1)) {
															if (psForeign.get(myT1).executeUpdate() > 0) { // INSERT
																int lID = DBKernel.getLastInsertedID(psForeign.get(myT1));
																// Das erstbeste Feld, das auf den Fremdtable verweist, wird mit dem Neueintrag verlinkt
																DBKernel.sendRequest(
																		"UPDATE " + DBKernel.delimitL(tableName) + " SET " + DBKernel.delimitL(myT.getFieldNames()[ii]) + "=" + lID
																				+ " WHERE " + DBKernel.delimitL("ID") + "=" + lastID, false);
															}
															break;
														}
													}
												}
												/*
												 * for (int j=0;j<numCols;j++) {
												 * if (myForeignTables[j] !=
												 * null && ffieldTypes[j] !=
												 * null) { MyTable[] foreignTs =
												 * myT.getForeignFields(); for
												 * (int
												 * ii=0;ii<foreignTs.length;
												 * ii++) { if (foreignTs[ii] !=
												 * null && foreignTs[ii].equals(
												 * myForeignTables[j])) { if
												 * (psForeign
												 * .get(myForeignTables
												 * [j]).executeUpdate() > 0) {
												 * // INSERT int lID =
												 * DBKernel.getLastInsertedID
												 * (psForeign
												 * .get(myForeignTables[j]));
												 * DBKernel
												 * .sendRequest("UPDATE " +
												 * DBKernel.delimitL(tableName)
												 * + " SET " +
												 * DBKernel.delimitL(
												 * myT.getFieldNames()[ii]) +
												 * "=" + lID + " WHERE " +
												 * DBKernel.delimitL("ID") + "="
												 * + lastID, false); } break; }
												 * } } }
												 */
											}
										} catch (Exception e1) {
											numFailed++;
											MyLogger.handleMessage(ps.toString());
											MyLogger.handleException(e1);
										}
									}
									if (progress != null) {
										lfd++;
										progress.setValue(lfd);
									}
								}
							}

							myT.doMNs();
							if (progress != null) {
								// Refreshen:
								MyDBTable myDB = DBKernel.mainFrame.getMyList().getMyDBTable();
								if (myDB.getActualTable() != null) {
									String actTablename = myDB.getActualTable().getTablename();
									if (actTablename.equals(tableName) || actTablename.equals(DBKernel.getCodesName(tableName))) {
										myDB.setTable(myDB.getActualTable());
									}
								}
								MyDBTree myTR = DBKernel.mainFrame.getMyList().getMyDBTree();
								if (myTR.getActualTable() != null) {
									String actTablename = myTR.getActualTable().getTablename();
									if (actTablename.equals(tableName) || actTablename.equals(DBKernel.getCodesName(tableName))) {
										myTR.setTable(myTR.getActualTable());
									}
								}
							}
						} else {
							System.err.println(tableName + " nicht in DB???");
						}
					}
					if (progress != null) {
						progress.setVisible(false);
					}
					String log = numSuccess + " erfolgreiche Importe.\n";
					log += numFailed + " fehlgeschlagene Importe.\n";
					if (unusedFields.length() > 0) log += "Unbekannte Felder: " + unusedFields.substring(1) + "\n";
					if (showResults) {
						InfoBox ib = new InfoBox(log, true, new Dimension(400, 300), null);
						ib.setVisible(true);
					} else {
						System.out.println("GeneralXLSImporter (" + filename + "):\n" + log);
					}
					} catch (Exception e) {
						MyLogger.handleException(e);
					}
				} catch (Exception e) {
					MyLogger.handleException(e);
				}
			}
		};

		Thread thread = new Thread(runnable);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			MyLogger.handleException(e);
		}
	    return true;
	}

	/*
	 * private String[] getTypes(String[] fieldNames, MyTable myT) { return
	 * getTypes(fieldNames, myT, null); }
	 * 
	 * private String[] getTypes(String[] fieldNames, MyTable myT, String[]
	 * knownTypes) { String[] result; if (knownTypes == null ||
	 * knownTypes.length != fieldNames.length) result = new
	 * String[fieldNames.length]; else result = knownTypes; for (int
	 * i=0;i<fieldNames.length;i++) { result[i] = getType(fieldNames[i], myT); }
	 * return result; }
	 */
	private String getType(String fieldName, MyTable myT, boolean takecareofID) {
		if (takecareofID && fieldName.equalsIgnoreCase("id")) return "INTEGER";
		String result = null;
		String[] tFieldNames = myT.getFieldNames();
		String[] tFieldTypes = myT.getFieldTypes();
		for (int j = 0; j < tFieldNames.length; j++) {
			if (fieldName.equals(tFieldNames[j])) {
				result = tFieldTypes[j];
				break;
			}
		}
		if (result == null) {
			if (fieldName.equals("Kommentar")) result = "VARCHAR(";
			else if (fieldName.equals("Guetescore")) result = "INTEGER";
			else if (fieldName.equals("Geprueft")) result = "BOOLEAN";
		}
		return result;
	}

	private String getForeignTable(String fieldName, MyTable myT) {
		String result = null;
		String[] tFieldNames = myT.getFieldNames();
		MyTable[] myFs = myT.getForeignFields();
		if (myFs == null) return null;
		for (int j = 0; j < tFieldNames.length; j++) {
			if (fieldName.equals(tFieldNames[j])) {
				if (myFs[j] != null) result = myFs[j].getTablename();
				break;
			}
		}
		/*
		 * if (result == null) { if (fieldName.equals("Kommentar")) result =
		 * "VARCHAR("; else if (fieldName.equals("Guetescore")) result =
		 * "INTEGER"; else if (fieldName.equals("Geprueft")) result = "BOOLEAN";
		 * }
		 */
		return result;
	}

	private String getDBFieldName(String fieldName, MyTable myT, boolean takecareofID) {
		if (takecareofID && fieldName.equalsIgnoreCase("id")) return "ID";
		String[] tFieldNames = myT.getFieldNames();
		MyTable[] myFs = myT.getForeignFields();
		for (int j = 0; j < tFieldNames.length; j++) {
			if (fieldName.equals(tFieldNames[j])) {
				return fieldName;
			}
			if (myFs != null && j < myFs.length && myFs[j] != null && myFs[j].getTablename().equals("DoubleKennzahlen")) {
				String dblf = getDBLField(fieldName, tFieldNames[j]);
				if (dblf != null) return dblf;
			}
		}
		if (fieldName.equals("Kommentar") || fieldName.equals("Guetescore") || fieldName.equals("Geprueft")) return fieldName;
		return null;
	}

	private String getDBLField(String xlsFieldName, String dbFieldName) {
		if (xlsFieldName.startsWith(dbFieldName + "-")) {
			if (xlsFieldName.startsWith(dbFieldName + "-Wert")) return dbFieldName;//"Einzelwert";
			if (xlsFieldName.startsWith(dbFieldName + "-Wiederholungen")) return dbFieldName;//"Wiederholungen";
			if (xlsFieldName.startsWith(dbFieldName + "-Exponent")) return dbFieldName;//"Exponent";
			if (xlsFieldName.startsWith(dbFieldName + "-Wert_typ")) return dbFieldName;//"Wert_typ";
			if (xlsFieldName.startsWith(dbFieldName + "-Minimum")) return dbFieldName;//"Minimum";
			if (xlsFieldName.startsWith(dbFieldName + "-Maximum")) return dbFieldName;//"Maximum";
			if (xlsFieldName.startsWith(dbFieldName + "-Standardabweichung")) return dbFieldName;//"Standardabweichung";
			if (xlsFieldName.startsWith(dbFieldName + "-LCL95")) return dbFieldName;//"LCL95";
			if (xlsFieldName.startsWith(dbFieldName + "-UCL95")) return dbFieldName;//"-UCL95";
			if (xlsFieldName.startsWith(dbFieldName + "-Verteilung")) return dbFieldName;//"Verteilung";
			if (xlsFieldName.startsWith(dbFieldName + "-Funktion (Zeit)")) return dbFieldName;//"Funktion (Zeit)";
			if (xlsFieldName.startsWith(dbFieldName + "-Funktion (x)")) return dbFieldName;//"Funktion (x)";
			if (xlsFieldName.startsWith(dbFieldName + "-x")) return dbFieldName;//"Funktion (x)";
			if (xlsFieldName.startsWith(dbFieldName + "-Undefiniert (n.d.)")) return dbFieldName;//"Undefiniert (n.d.)";
		} else if (xlsFieldName.equals(dbFieldName)) {
			return dbFieldName;//"Einzelwert";
		}
		return null;
	}

	private String getKZ(String xlsFieldName, String dbFieldName) {
		if (xlsFieldName.equals(dbFieldName)) {
			return "Wert"; //"Einzelwert";
		} else if (xlsFieldName.startsWith(dbFieldName + "-")) {
			return xlsFieldName.substring(xlsFieldName.lastIndexOf("-") + 1);
			/*
			 * if (xlsFieldName.startsWith(dbFieldName + "-Einzelwert")) return
			 * "Einzelwert"; if (xlsFieldName.startsWith(dbFieldName +
			 * "-Wiederholungen")) return "Wiederholungen"; if
			 * (xlsFieldName.startsWith(dbFieldName + "-Exponent")) return
			 * "Exponent"; if (xlsFieldName.startsWith(dbFieldName +
			 * "-Wert_typ")) return "Wert_typ"; if
			 * (xlsFieldName.startsWith(dbFieldName + "-Minimum")) return
			 * "Minimum"; if (xlsFieldName.startsWith(dbFieldName + "-Maximum"))
			 * return "Maximum"; if (xlsFieldName.startsWith(dbFieldName +
			 * "-Standardabweichung")) return "Standardabweichung"; if
			 * (xlsFieldName.startsWith(dbFieldName + "-LCL95")) return "LCL95";
			 * if (xlsFieldName.startsWith(dbFieldName + "-UCL95")) return
			 * "UCL95"; if (xlsFieldName.startsWith(dbFieldName +
			 * "-Verteilung")) return "Verteilung"; if
			 * (xlsFieldName.startsWith(dbFieldName + "-Funktion (Zeit)"))
			 * return "Funktion (Zeit)"; if (xlsFieldName.startsWith(dbFieldName
			 * + "-Funktion (x)")) return "Funktion (x)"; if
			 * (xlsFieldName.startsWith(dbFieldName + "-x")) return "x"; if
			 * (xlsFieldName.startsWith(dbFieldName + "-Undefiniert (n.d.)"))
			 * return "Undefiniert (n.d.)";
			 */
		}
		return null;
	}

	private int foreignFieldExists(String fieldName, MyTable myT) {
		MyTable[] foreignTs = myT.getForeignFields();
		for (int i = 0; i < foreignTs.length; i++) {
			if (foreignTs[i] != null) {
				if (getDBFieldName(fieldName, foreignTs[i], false) != null) return i;
			}
		}
		return -1;
	}

	private Integer manageInteger(PreparedStatement ps, PreparedStatement psUpdate, int lfdCol, HSSFCell cell) throws SQLException {
		Integer result = null;
		if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			if (cell.getStringCellValue().trim().length() > 0) {
				result = new Integer(cell.getStringCellValue());
				if (ps != null) ps.setInt(lfdCol, result);
				if (psUpdate != null) psUpdate.setInt(lfdCol, result);
				return result;
			}
		} else {
			result = new Integer((int) cell.getNumericCellValue());
			if (ps != null) ps.setInt(lfdCol, result);
			if (psUpdate != null) psUpdate.setInt(lfdCol, result);
			return result;
		}
		if (ps != null) ps.setNull(lfdCol, java.sql.Types.INTEGER);
		if (psUpdate != null) psUpdate.setNull(lfdCol, java.sql.Types.INTEGER);
		return result;
	}

	private Date manageDate(PreparedStatement ps, PreparedStatement psUpdate, int lfdCol, HSSFCell cell) throws SQLException {
		Date result = null;
		if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			if (cell.getStringCellValue().trim().length() > 0) {
				DateFormat formater = new SimpleDateFormat("yyyy-MM-dd"); // 2012-06-01   hh:mm:ss
				java.util.Date parsedUtilDate;
				try {
					parsedUtilDate = formater.parse(cell.getStringCellValue());
					result = new java.sql.Date(parsedUtilDate.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (result != null) {
					if (ps != null) ps.setDate(lfdCol, result);
					if (psUpdate != null) psUpdate.setDate(lfdCol, result);
					return result;
				}
			}
		} else {
			result = new Date(cell.getDateCellValue().getTime());
			if (ps != null) ps.setDate(lfdCol, result);
			if (psUpdate != null) psUpdate.setDate(lfdCol, result);
			return result;
		}
		if (ps != null) ps.setNull(lfdCol, java.sql.Types.DATE);
		if (psUpdate != null) psUpdate.setNull(lfdCol, java.sql.Types.DATE);
		return result;
	}

	private Long manageBigInteger(PreparedStatement ps, PreparedStatement psUpdate, int lfdCol, HSSFCell cell) throws SQLException {
		Long result = null;
		if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			if (cell.getStringCellValue().trim().length() > 0) {
				result = new Long(cell.getStringCellValue());
				if (ps != null) ps.setLong(lfdCol, result);
				if (psUpdate != null) psUpdate.setLong(lfdCol, result);
				return result;
			}
		} else {
			result = new Long((long) cell.getNumericCellValue());
			if (ps != null) ps.setLong(lfdCol, result);
			if (psUpdate != null) psUpdate.setLong(lfdCol, result);
			return result;
		}
		if (ps != null) ps.setNull(lfdCol, java.sql.Types.BIGINT);
		if (psUpdate != null) psUpdate.setNull(lfdCol, java.sql.Types.BIGINT);
		return result;
	}

	private Double manageDouble(PreparedStatement ps, PreparedStatement psUpdate, int lfdCol, HSSFCell cell) throws SQLException {
		Double dbl = null;
		if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			if (cell.getStringCellValue().trim().length() > 0 && !cell.getStringCellValue().equalsIgnoreCase("null")) {
				try {
					dbl = Double.parseDouble(cell.getStringCellValue());
					ps.setDouble(lfdCol, dbl);
					psUpdate.setDouble(lfdCol, dbl);
					return dbl;
				} catch (Exception e) {
				}
			}
		} else {
			dbl = cell.getNumericCellValue();
			try {
				ps.setDouble(lfdCol, dbl);
				psUpdate.setDouble(lfdCol, dbl);
			} catch (Exception e) {
			}
			return dbl;
		}
		try {
			ps.setNull(lfdCol, java.sql.Types.DOUBLE);
			psUpdate.setNull(lfdCol, java.sql.Types.DOUBLE);
		} catch (Exception e) {
		}
		return dbl;
	}

	private String manageString(PreparedStatement ps, PreparedStatement psUpdate, int lfdCol, HSSFCell cell) throws SQLException {
		return manageString(ps, psUpdate, lfdCol, cell, null);
	}

	private String manageString(PreparedStatement ps, PreparedStatement psUpdate, int lfdCol, HSSFCell cell, LinkedHashMap<Object, String> hashBL) throws SQLException {
		String result = null;
		if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
			if (ps != null) ps.setNull(lfdCol, java.sql.Types.VARCHAR);
			if (psUpdate != null) psUpdate.setNull(lfdCol, java.sql.Types.VARCHAR);
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			result = cell.getStringCellValue().trim();
			if (ps != null) ps.setString(lfdCol, result);
			if (psUpdate != null) psUpdate.setString(lfdCol, result);
		} else {
			result = cell.getStringCellValue().trim();
			if (hashBL != null) {
				int val, min = 1000;
				String newResult = result;
				for (Object o : hashBL.keySet()) {
					val = Levenshtein.LD(result, o.toString());
					if (val < min) {
						min = val;
						newResult = o.toString();
					}
				}
				if (!newResult.equals(result)) {
					if (DBKernel.debug) MyLogger.handleMessage("Levenshtein - not equal ... " + newResult + "\t" + result);
					result = newResult;
				}
			}
			if (ps != null) ps.setString(lfdCol, result);
			if (psUpdate != null) psUpdate.setString(lfdCol, result);
		}
		//ps.setNull(lfdCol, java.sql.Types.VARCHAR);		
		if (result != null && result.equals("?@lufa-itl.de")) {
			MyLogger.handleMessage(result);
		}
		return result;
	}

	private Boolean manageBoolean(PreparedStatement ps, PreparedStatement psUpdate, int lfdCol, HSSFCell cell) throws SQLException {
		Boolean result = null;
		if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
			if (ps != null) ps.setNull(lfdCol, java.sql.Types.BOOLEAN);
			if (psUpdate != null) psUpdate.setNull(lfdCol, java.sql.Types.BOOLEAN);
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
			result = cell.getNumericCellValue() != 0;
			if (ps != null) ps.setBoolean(lfdCol, result);
			if (psUpdate != null) psUpdate.setBoolean(lfdCol, result);
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			result = cell.getStringCellValue().equalsIgnoreCase("true");
			if (ps != null) ps.setBoolean(lfdCol, result);
			if (psUpdate != null) psUpdate.setBoolean(lfdCol, result);
		} else {
			result = cell.getBooleanCellValue();
			if (ps != null) ps.setBoolean(lfdCol, result);
			if (psUpdate != null) psUpdate.setBoolean(lfdCol, result);
		}
		//ps.setNull(lfdCol, java.sql.Types.BOOLEAN);		
		return result;
	}
}
