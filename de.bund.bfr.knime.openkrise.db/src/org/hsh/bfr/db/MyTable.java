/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package org.hsh.bfr.db;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.hsh.bfr.db.gui.dbtable.MyDBForm;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.gui.dbtable.editoren.MyMNSQLJoinCollector;

/**
 * @author Armin
 * 
 */
public class MyTable {

	private String tableName = null;
	private String[] fieldNames = null;
	private String[] fieldTypes = null;
	private String[] fieldComments = null;
	private MyTable[] foreignFields = null;
	private String[] mnTable = null;
	private Vector<String> listMNs = null;
	private String[][] uniqueFields = null;
	private String[] defaults = null;
	private LinkedHashMap<Object, String>[] foreignHashs = null;
	private LinkedList<String> fields2ViewInGui = null;
	private String[] deepForeignFields = null;
	private boolean hasForm = false;
	private char[][] allowedCharsInAdditionToLetterOrDigit;

	private boolean hideScore = false;
	private boolean hideTested = false;
	private boolean hideComment = false;
	private boolean readOnly = false;
	private boolean odsn = false; // "ON DELETE SET NULL" for FOREIGN Keys (in MyTable)
	private int child = -1; // Where to show in list

	private Callable<Void> caller4Trigger = null;
	private String[] mnSQL = null;

	// Parameter zum Abspeichern
	private LinkedHashMap<Integer, Integer> rowHeights = new LinkedHashMap<>();
	private int[] colWidths = null;
	private List<? extends SortKey> sortKeyList = null;
	private String searchString = "";
	private int selectedRow = -1;
	private int selectedCol = 0;
	private int verticalScrollerPosition = 0;
	private int horizontalScrollerPosition = 0;
	private int form_SelectedID = 0;

	MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, null, null);
	}

	MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, LinkedHashMap<Object, String>[] foreignHashs) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, null, foreignHashs);
	}

	MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields,
			LinkedHashMap<Object, String>[] foreignHashs) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, uniqueFields, foreignHashs, null);
	}

	MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields,
			LinkedHashMap<Object, String>[] foreignHashs, String[] mnTable) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, uniqueFields, foreignHashs, mnTable, null);
	}

	MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields,
			LinkedHashMap<Object, String>[] foreignHashs, String[] mnTable, String[] defaults) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, uniqueFields, foreignHashs, mnTable, defaults, null);
	}

	MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields,
			LinkedHashMap<Object, String>[] foreignHashs, String[] mnTable, String[] defaults, LinkedList<String> fields2ViewInGui) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, uniqueFields, foreignHashs, mnTable, defaults, fields2ViewInGui, null);
	}

	MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields,
			LinkedHashMap<Object, String>[] foreignHashs, String[] mnTable, String[] defaults, LinkedList<String> fields2ViewInGui, String[] deepForeignFields) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, uniqueFields, foreignHashs, mnTable, defaults, fields2ViewInGui, deepForeignFields, null);
	}

	MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields,
			LinkedHashMap<Object, String>[] foreignHashs, String[] mnTable, String[] defaults, LinkedList<String> fields2ViewInGui, String[] deepForeignFields,
			char[][] allowedCharsInAdditionToLetterOrDigit) {
		this.tableName = tableName; // GuiMessages.getString(tableName).trim();
		/*
		 * for (int i=0;i<fieldNames.length;i++) { fieldNames[i] =
		 * GuiMessages.getString(fieldNames[i]).trim(); }
		 */
		this.fieldNames = fieldNames;
		this.fieldTypes = fieldTypes;
		/*
		 * for (int i=0;i<fieldComments.length;i++) { fieldComments[i] =
		 * fieldComments[i] == null ? null :
		 * GuiMessages.getString(fieldComments[i]).trim(); }
		 */
		this.fieldComments = fieldComments;
		this.foreignFields = foreignFields;
		this.uniqueFields = uniqueFields;
		this.foreignHashs = foreignHashs;
		this.mnTable = mnTable;
		this.defaults = defaults;
		this.fields2ViewInGui = fields2ViewInGui;
		this.deepForeignFields = deepForeignFields;
		this.allowedCharsInAdditionToLetterOrDigit = allowedCharsInAdditionToLetterOrDigit;
		try {
			if (mnTable != null) {
				for (int i = 0; i < mnTable.length; i++) {
					if (mnTable[i] != null && mnTable[i].length() > 0) {
						if (listMNs == null) listMNs = new Vector<>();
						listMNs.add(fieldNames[i]);
					}
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		hideComment = tableName.equals("ChangeLog") || tableName.equals("DateiSpeicher") || tableName.equals("ComBaseImport") || tableName.equals("Nachweisverfahren_Kits")
				|| tableName.equals("Aufbereitungsverfahren_Kits") || tableName.equals("Methoden_Normen") || tableName.equals("Methodennormen")
				|| tableName.equals("Labore_Methodiken") || tableName.equals("Labore_Matrices") || tableName.equals("Labore_Agenzien")
				|| tableName.equals("Labore_Agenzien_Methodiken") || tableName.startsWith("ICD10_") || tableName.equals("DoubleKennzahlen")
				|| tableName.equals("SonstigeParameter") || tableName.equals("Einheiten") || tableName.equals("Infotabelle") || tableName.equals("ToxinUrsprung")
				|| tableName.equals("Prozessdaten_Messwerte") || tableName.equals("Verpackungsmaterial") || tableName.equals("ImportedCombaseData")
				|| tableName.equals("Parametertyp") || tableName.equals("Prozessdaten_Literatur") || tableName.equals("ProzessWorkflow_Literatur")
				|| tableName.equals("Produzent_Artikel") || tableName.equals("Artikel_Lieferung")
				|| tableName.equals("Lieferung_Lieferungen")
				// StatUp 
				|| tableName.equals("ModellkatalogParameter") || tableName.equals("Modell_Referenz") || tableName.equals("GeschaetztesModell_Referenz")
				|| tableName.equals("GeschaetzteParameter") || tableName.equals("GeschaetzteParameterCovCor") || tableName.equals("Sekundaermodelle_Primaermodelle")
				|| tableName.equals("VarParMaps") || tableName.equals("DataSource")
				|| tableName.equals("ExtraFields") || tableName.equals("ImportMetadata");

		hideTested = hideComment || tableName.equals("Users") || tableName.equals("Prozess_Verbindungen") || tableName.equals("Zutatendaten_Sonstiges")
				|| tableName.equals("Versuchsbedingungen_Sonstiges") || tableName.equals("Messwerte_Sonstiges") || tableName.equals("Prozessdaten_Sonstiges")
				|| tableName.equals("Krankheitsbilder_Symptome") || tableName.equals("Krankheitsbilder_Risikogruppen") || tableName.equals("Agens_Matrices")
				|| tableName.equals("Kontakte") || tableName.equals("Codes_Agenzien") || tableName.equals("Literatur") || tableName.equals("Codes_Matrices")
				|| tableName.equals("Methoden") || tableName.equals("Codes_Methoden") || tableName.equals("Methodiken") || tableName.equals("Codes_Methodiken")
				|| tableName.equals("Nachweisverfahren_Testanbieter") || tableName.equals("Produzent") || tableName.equals("Labore") || tableName.equals("Testanbieter")
				|| tableName.equals("Matrices") || tableName.equals("Agenzien") || tableName.equals("Einheiten") || tableName.equals("Symptome")
				|| tableName.equals("Risikogruppen")
				|| tableName.equals("Tierkrankheiten")
				|| tableName.equals("Zertifizierungssysteme")
				|| tableName.equals("ProzessElemente") //|| tableName.equals("Prozessdaten_Workflow")
				|| tableName.equals("GueltigkeitsBereiche") || tableName.equals("Kostenkatalog") || tableName.equals("Kostenkatalogpreise")
				|| tableName.equals("Prozessdaten_Kosten")
				|| tableName.equals("Zutatendaten_Kosten")
				|| tableName.equals("LinkedTestConditions")
				// StatUp
				|| tableName.equals("Modellkatalog")
				// Jans Tabellen
				|| tableName.equals("Exposition") || tableName.equals("Risikocharakterisierung") || tableName.equals("Verwendung") || tableName.equals("Transport")
				|| tableName.equals("Methoden_Software") || tableName.equals("Produkt")
				// Krise
				|| tableName.equals("LieferungVerbindungen") || tableName.equals("ChargenVerbindungen") || tableName.equals("Lieferungen") || tableName.equals("Produktkatalog")
				|| tableName.equals("Station") || tableName.equals("Chargen") || tableName.equals("Station_Agenzien") || tableName.equals("Produktkatalog_Matrices");

		hideScore = hideTested || tableName.equals("Messwerte") || tableName.equals("Kits") || tableName.equals("Zutatendaten");

		readOnly = tableName.equals("ChangeLog")
				|| tableName.equals("DateiSpeicher")
				||
				//tableName.equals("Matrices") || tableName.equals("Agenzien") || // tableName.equals("Einheiten") || 
				tableName.equals("ICD10_Kodes") || tableName.equals("Parametertyp")
				|| tableName.equals("DataSource")
				||
				//(!DBKernel.isAdmin() && (tableName.equals("Modellkatalog") || tableName.equals("ModellkatalogParameter") || tableName.equals("Modell_Referenz"))) ||
				tableName.equals("GeschaetzteModelle") || tableName.equals("GeschaetztesModell_Referenz") || tableName.equals("GeschaetzteParameter")
				|| tableName.equals("VarParMaps") || tableName.equals("GeschaetzteParameterCovCor") || tableName.equals("Sekundaermodelle_Primaermodelle")
				|| tableName.equals("GueltigkeitsBereiche") || tableName.equals("LinkedTestConditions") || tableName.equals("GlobalModels") ||
				//tableName.equals("Krankheitsbilder_Symptome") || tableName.equals("Krankheitsbilder_Risikogruppen") || 
				tableName.equals("Prozess_Verbindungen") || tableName.equals("ProzessWorkflow") || tableName.equals("Prozessdaten");

		odsn = true;
		if (tableName.equals("Modellkatalog") || tableName.equals("ModellkatalogParameter") || tableName.equals("Modell_Referenz") || tableName.equals("GeschaetzteModelle")
				|| tableName.equals("GeschaetztesModell_Referenz") || tableName.equals("GeschaetzteParameter") || tableName.equals("GeschaetzteParameterCovCor")
				|| tableName.equals("Sekundaermodelle_Primaermodelle") || tableName.equals("GueltigkeitsBereiche")) odsn = false;

		hasForm = tableName.equals("Krankheitsbilder");
		//doMNs();
	}

	public String getMNSql(int selectedColumn) {
		if (mnSQL == null) mnSQL = new String[mnTable.length];
		if (mnSQL[selectedColumn] != null && !mnSQL[selectedColumn].isEmpty()) return mnSQL[selectedColumn];

		String sql = "";
		boolean isINTmn = mnTable != null && selectedColumn < mnTable.length && mnTable[selectedColumn] != null && mnTable[selectedColumn].equals("INT");
		MyTable myFT = this.getForeignFields() == null ? null : this.getForeignFields()[selectedColumn];
		String myMN = this.getMNTable() == null ? null : this.getMNTable()[selectedColumn];
		if (myFT != null) {
			if (isINTmn) {
				sql = this.getSQL(myFT, myMN);
			} else {
				sql = this.getSQL(myFT, myMN);
			}
		}
		mnSQL[selectedColumn] = sql;
		return sql;
	}

	public Callable<Void> getCaller4Trigger() {
		return caller4Trigger;
	}

	public void setCaller4Trigger(Callable<Void> caller4Trigger) {
		this.caller4Trigger = caller4Trigger;
	}

	public void setChild(int child) {
		this.child = child;
	}

	public int getChild() {
		return child;
	}

	public LinkedList<String> getFields2ViewInGui() {
		return fields2ViewInGui;
	}

	public String[] getDeepForeignFields() {
		return deepForeignFields;
	}

	public String[] getDefaults() {
		return defaults;
	}

	public boolean isHasForm() {
		return hasForm;
	}

	public char[][] getAllowedCharsInAdditionToLetterOrDigit() {
		return allowedCharsInAdditionToLetterOrDigit;
	}

	public void saveProperties(MyDBForm myForm) {
		form_SelectedID = myForm.getSelectedID();
	}

	public void restoreProperties(MyDBForm myForm) {
		myForm.setSelectedID(form_SelectedID);
	}

	public void saveProperties(MyDBTable myDB) {
		JTable bigTable = myDB.getTable();
		JScrollPane scroller = myDB.getScroller();
		if (scroller != null) {
			verticalScrollerPosition = scroller.getVerticalScrollBar().getValue();
			horizontalScrollerPosition = scroller.getHorizontalScrollBar().getValue();
		} else {
			verticalScrollerPosition = 0;
			horizontalScrollerPosition = 0;
		}

		rowHeights.clear();
		for (int i = 0; i < bigTable.getRowCount(); i++) {
			Object o = bigTable.getValueAt(i, 0);
			if (o != null && o instanceof Integer) rowHeights.put((Integer) bigTable.getValueAt(i, 0), bigTable.getRowHeight(i));
		}
		colWidths = new int[bigTable.getColumnCount()];
		for (int i = 0; i < colWidths.length; i++) {
			colWidths[i] = bigTable.getColumnModel().getColumn(i).getWidth();
		}
		if (bigTable.getRowSorter() != null && bigTable.getRowSorter().getSortKeys().size() > 0) {
			sortKeyList = bigTable.getRowSorter().getSortKeys();
		}
		searchString = "";
		try {
			searchString = myDB.getMyDBPanel().getSuchfeld().getText();
		} catch (Exception e) {
		}
		if (bigTable.getRowCount() > 0) {
			selectedRow = bigTable.getSelectedRow();
			selectedCol = bigTable.getSelectedColumn();
			//System.out.println("saveProperties\t" + selectedRow);
		}
	}

	public void restoreProperties(MyDBTable myDB) {
		JTable bigTable = myDB.getTable();
		try {
			myDB.getMyDBPanel().getSuchfeld().setText(searchString);
			myDB.getMyDBPanel().handleSuchfeldChange(null);
		} catch (Exception e) {
		}
		if (sortKeyList != null && bigTable.getRowSorter() != null) {
			bigTable.getRowSorter().setSortKeys(sortKeyList);
			@SuppressWarnings("unchecked")
			TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) bigTable.getRowSorter();
			sorter.sort();

		}
		if (rowHeights != null) {
			for (int i = 0; i < bigTable.getRowCount(); i++) {
				if (bigTable.getRowCount() > i && rowHeights.containsKey(bigTable.getValueAt(i, 0))) {
					Integer rh = rowHeights.get(bigTable.getValueAt(i, 0));
					bigTable.setRowHeight(i, rh);
				}
			}
		}
		if (colWidths != null) {
			for (int i = 0; i < colWidths.length; i++) {
				if (bigTable.getColumnCount() > i) bigTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
			}
		}
		if (selectedRow >= 0) {
			myDB.setSelectedRowCol(selectedRow, selectedCol, verticalScrollerPosition, horizontalScrollerPosition, true);
		} else {
			myDB.selectCell(0, 0);
		}
	}

	public boolean isFirstTime() {
		return colWidths == null;
	}

	public String getTablename() {
		return tableName;
	}

	public boolean getHideScore() {
		return hideScore;
	}

	public boolean getHideTested() {
		return hideTested;
	}

	public boolean getHideKommentar() {
		return hideComment;
	}

	public boolean isReadOnly() {
		return readOnly || DBKernel.isReadOnly();
	}

	public Vector<Integer> getMyBLOBs() {
		Vector<Integer> myBLOBs = new Vector<>();
		for (int i = 0; i < fieldTypes.length; i++) {
			if (fieldTypes[i].startsWith("BLOB(")) {
				myBLOBs.add(i);
			}
		}
		return myBLOBs;
	}

	public String toString() {
		return getTablename();
	}

	public Vector<String> getListMNs() {
		return listMNs;
	}

	public String[] getMNTable() {
		return mnTable;
	}

	public MyTable[] getForeignFields() {
		return foreignFields;
	}

	void setForeignField(MyTable myT, int pos) {
		foreignFields[pos] = myT;
	}

	public LinkedHashMap<Object, String>[] getForeignHashs() {
		return foreignHashs;
	}

	public String[] getFieldTypes() {
		return fieldTypes;
	}

	public String[] getFieldNames() {
		return fieldNames;
	}

	public String[] getFieldComments() {
		return fieldComments;
	}

	public int getNumFields() {
		int add = 1; // ID
		if (!hideScore) add++;
		if (!hideComment) add++;
		if (!hideTested) add++;
		return fieldNames.length + add; // + ID + Kommentar + Guetescore + Geprueft
	}

	public String getRowCountSQL() {
		return "SELECT COUNT(*) FROM " + DBKernel.delimitL(tableName);
	}

	public String getSelectSQL() {
		String fieldDefs = DBKernel.delimitL("ID");
		for (int i = 0; i < fieldNames.length; i++) {
			fieldDefs += "," + DBKernel.delimitL(fieldNames[i]);
		}
		if (!hideScore) fieldDefs += "," + DBKernel.delimitL("Guetescore");
		if (!hideComment) fieldDefs += "," + DBKernel.delimitL("Kommentar");
		if (!hideTested) fieldDefs += "," + DBKernel.delimitL("Geprueft");
		return "SELECT " + fieldDefs + " FROM " + DBKernel.delimitL(tableName);
	}

	public String getInsertSQL1() {
		return "INSERT INTO " + DBKernel.delimitL(tableName) + " " + getInsertSql();
	}

	public String getInsertSQL2() {
		return getInsertSql2();
	}

	public String getUpdateSQL1() {
		return "UPDATE " + DBKernel.delimitL(tableName) + " SET " + getUpdateSql();
	}

	public String getUpdateSQL2() {
		return getUpdateSql2();
	}

	public String getDeleteSQL1() {
		return "DELETE FROM " + DBKernel.delimitL(tableName) + " WHERE " + DBKernel.delimitL("ID") + " = ?";
	}

	public String getDeleteSQL2() {
		return "1";
	}

	public List<String> getIndexSQL() {
		List<String> indexSQL = new ArrayList<>();
		for (int i = 0; i < fieldNames.length; i++) {
			if (foreignFields[i] != null) {
				if (mnTable == null || mnTable[i] == null || mnTable[i].length() == 0) {
					indexSQL.add("ALTER TABLE " + DBKernel.delimitL(tableName) + " ADD CONSTRAINT " + DBKernel.delimitL(tableName + "_fk_" + fieldNames[i] + "_" + i)
							+ " FOREIGN KEY (" + DBKernel.delimitL(fieldNames[i]) + ")" + " REFERENCES " + DBKernel.delimitL(foreignFields[i].getTablename()) + " ("
							+ DBKernel.delimitL("ID") + ") " + (odsn ? "ON DELETE SET NULL;" : ";"));
				}
			}
		}
		if (uniqueFields != null) {
			for (int i = 0; i < uniqueFields.length; i++) {
				String uFs = "";
				for (int j = 0; j < uniqueFields[i].length; j++) {
					uFs += "," + DBKernel.delimitL(uniqueFields[i][j]);
				}
				indexSQL.add("ALTER TABLE " + DBKernel.delimitL(tableName) + " ADD CONSTRAINT " + DBKernel.delimitL(tableName + "_uni_" + i) + " UNIQUE (" + uFs.substring(1)
						+ ");");
			}
		}
		return indexSQL;
	}

	public void createTable() {
		String fieldDefs = DBKernel.delimitL("ID") + " INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY";
		if (tableName.equals("ChangeLog")) {
			fieldDefs = DBKernel.delimitL("ID") + " INTEGER GENERATED BY DEFAULT AS SEQUENCE " + DBKernel.delimitL("ChangeLogSEQ") + " PRIMARY KEY";
		}
		//String uFsAll = "";
		for (int i = 0; i < fieldNames.length; i++) {
			if (fieldTypes[i].startsWith("BLOB(") && !tableName.equals("DateiSpeicher")) {
				fieldDefs += "," + DBKernel.delimitL(fieldNames[i]) + " " + "VARCHAR(255)";
			} else {
				String defolt = null;
				if (defaults != null && defaults[i] != null && !fieldTypes[i].startsWith("BLOB(")) defolt = defaults[i];
				fieldDefs += "," + DBKernel.delimitL(fieldNames[i]) + " " + fieldTypes[i] + (defolt == null ? "" : " " + defolt);
				if (!tableName.equals("DateiSpeicher") && !tableName.equals("ChangeLog")) {
					//uFsAll += "," + DBKernel.delimitL(fieldNames[i]);
				}
			}
		}
		if (!hideScore) fieldDefs += "," + DBKernel.delimitL("Guetescore") + " " + "INTEGER";
		if (!hideComment) fieldDefs += "," + DBKernel.delimitL("Kommentar") + " " + "VARCHAR(1023)";
		if (!hideTested) fieldDefs += "," + DBKernel.delimitL("Geprueft") + " " + "BOOLEAN";
		//createTable(tableName, fieldDefs, getIndexSQL(), true, true);

		try {
			if (tableName.equals("ChangeLog")) {
				DBKernel.sendRequest("CREATE SEQUENCE " + DBKernel.delimitL(tableName + "SEQ") + " AS INTEGER START WITH 1 INCREMENT BY 1", false);
				DBKernel.sendRequest("GRANT USAGE ON SEQUENCE " + DBKernel.delimitL("ChangeLogSEQ") + " TO " + DBKernel.delimitL("PUBLIC"), false);
			}

			Statement stmt = DBKernel.getDBConnection().createStatement();
			String sqlc = "CREATE CACHED TABLE " + DBKernel.delimitL(tableName) + " (" + fieldDefs + ");";
			stmt.execute(sqlc);
			List<String> indexSQL = getIndexSQL();
			for (String sql : indexSQL) {
				if (sql.length() > 0) {
					stmt.execute(sql);
				}
			}
			if (!tableName.equals("ChangeLog") && !tableName.equals("DateiSpeicher") && !tableName.equals("Infotabelle")) {
				stmt.execute("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_U") + " AFTER UPDATE ON " + DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL "
						+ DBKernel.delimitL(new MyTrigger().getClass().getName()));
				stmt.execute("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_D") + " AFTER DELETE ON " + DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL "
						+ DBKernel.delimitL(new MyTrigger().getClass().getName()));
				stmt.execute("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_I") + " AFTER INSERT ON " + DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL "
						+ DBKernel.delimitL(new MyTrigger().getClass().getName()));
			}
			stmt.close();
		} catch (Exception e) {
		}
	}

	private String getUpdateSql() {
		String result = "";
		for (int i = 0; i < fieldNames.length; i++) {
			result += DBKernel.delimitL(fieldNames[i]) + "=?,";
		}
		if (!hideScore) result += DBKernel.delimitL("Guetescore") + "=?,";
		if (!hideComment) result += DBKernel.delimitL("Kommentar") + "=?,";
		if (!hideTested) result += DBKernel.delimitL("Geprueft") + "=?,";
		if (result.length() > 0) result = result.substring(0, result.length() - 1); // letztes Komma weg!
		result += " WHERE " + DBKernel.delimitL("ID") + "=?";
		return result;
	}

	private String getUpdateSql2() {
		String result = "";
		for (int i = 2; i <= getNumFields(); i++) {// fieldNames.length+4 //  + Kommentar + Guetescore
			result += i + ",";
		}
		result += "1";
		return result;
	}

	private String getInsertSql() {
		String result = "";
		String qms = "";
		String columnName;
		for (int i = 0; i < fieldNames.length; i++) {
			columnName = fieldNames[i];
			result += DBKernel.delimitL(columnName) + ",";
			qms += "?,";
		}
		if (!hideScore) {
			result += DBKernel.delimitL("Guetescore") + ",";
			qms += "?,";
		}
		if (!hideComment) {
			result += DBKernel.delimitL("Kommentar") + ",";
			qms += "?,";
		}
		if (!hideTested) {
			result += DBKernel.delimitL("Geprueft") + ",";
			qms += "?,";
		}
		if (result.length() > 0) result = result.substring(0, result.length() - 1); // letztes Komma weg!
		if (qms.length() > 0) qms = qms.substring(0, qms.length() - 1); // letztes Komma weg!
		if (result.length() > 0) result = "(" + result + ") VALUES (" + qms + ")";
		return result;
	}

	private String getInsertSql2() {
		String result = "";
		for (int i = 2; i <= getNumFields(); i++) {// fieldNames.length+4 // + Kommentar + Guetescore
			result += i + ",";
		}
		if (result.length() > 0) result = result.substring(0, result.length() - 1);
		return result;
	}

	private void collectJoins(MyMNSQLJoinCollector mnsqlc) {
		if (this.getFields2ViewInGui() != null) {
			mnsqlc.getAlreadyJoined().add(this);
			for (String s : this.getFields2ViewInGui()) {
				MyTable mt2 = this.getForeignTable(s);
				if (mt2 == null) {
					Integer fi = this.getFieldIndex(s);
					if (fi == null) mnsqlc.setHasUnknownFields(true);
					String f = this.getAdd2Select(s, fi);
					mnsqlc.addToSelect("," + f);
				} else {
					if (!mnsqlc.getAlreadyJoined().contains(mt2)) {
						String join = " LEFT JOIN " + DBKernel.delimitL(mt2.getTablename()) + " ON " + DBKernel.delimitL(this.getTablename()) + "." + DBKernel.delimitL(s) + "="
								+ DBKernel.delimitL(mt2.getTablename()) + "." + DBKernel.delimitL("ID");
						mnsqlc.addToJoin(join);
						mt2.collectJoins(mnsqlc);
					}
				}
			}
			//mnsqlc.addToSelect(",'\t'");
		}
	}

	private String getAdd2Select(String fieldName, Integer fi) {
		if (fi == null) return "'" + fieldName + "'";
		String result = "";
		boolean isDbl = this.getFieldTypes()[fi].equals("DOUBLE");
		LinkedHashMap<Object, String> hash = this.getHash(fieldName);
		String field = DBKernel.delimitL(this.getTablename()) + "." + DBKernel.delimitL(fieldName);
		if (hash == null || hash.size() == 0) {
			result = field;
		} else {
			result = "TRIM(CASE " + field;
			for (Object key : hash.keySet()) {
				result += " WHEN " + key + " THEN '" + hash.get(key).trim() + "' ";
			}
			result += " ELSE 'unknown' END)";
		}

		if (isDbl) result = "CAST(" + result + " AS DECIMAL(20,2))";
		result = "CAST(" + result + " AS VARCHAR(127))";
		result = "IFNULL(" + result + ", '?')";

		return result;
	}

	private LinkedHashMap<Object, String> getHash(String fieldName) {
		if (fieldName != null && this.getForeignHashs() != null) {
			String[] fn = this.getFieldNames();
			for (int i = 0; i < fn.length; i++) {
				if (fieldName.equals(fn[i])) {
					return this.getForeignHashs()[i];
				}
			}
		}
		return null;
	}

	private String getSQL(MyTable myFT, String myMN) {
		String sql = "";
		MyTable mnT = (myMN == null || myMN.equals("INT") ? null : DBKernel.myDBi.getTable(myMN));
		String toSelect = DBKernel.delimitL(myFT.getTablename()) + "." + DBKernel.delimitL("ID");
		String toJoin = myFT.getMNJoin(mnT);
		toSelect += ",CONCAT_WS('\t'";
		MyMNSQLJoinCollector mnsqlc = new MyMNSQLJoinCollector(toSelect, toJoin);
		if (myFT != null) myFT.collectJoins(mnsqlc);
		if (mnT != null) mnT.collectJoins(mnsqlc);
		if (mnsqlc.hasUnknownFields()) {
			toSelect = mnsqlc.getToSelect();
			toJoin = mnsqlc.getToJoin();
			toSelect = toSelect.replace("CONCAT_WS('\t',", "CONCAT(") + ")";
			mnsqlc = new MyMNSQLJoinCollector(toSelect, toJoin);
		} else if (mnsqlc.getAddCounter() < 2) {
			toSelect = mnsqlc.getToSelect();
			toJoin = mnsqlc.getToJoin();
			toSelect = toSelect.replace(",CONCAT_WS('\t'", "");
			mnsqlc = new MyMNSQLJoinCollector(toSelect, toJoin);
		} else {
			mnsqlc.addToSelect(")");
		}
		String toWhere = "";
		if (mnT != null) {
			String fn = mnT.getForeignFieldName(this);
			if (fn != null) {
				toWhere = " WHERE " + DBKernel.delimitL(mnT.getTablename()) + "." + DBKernel.delimitL(fn) + "=";
			} else {
				System.err.println("mnF2 = null...\t" + mnT + "\t" + myFT + "\t" + this);
			}
		} else if (this != null) {
			String fn = myFT.getForeignFieldName(this);
			if (fn != null) {
				toWhere = " WHERE " + DBKernel.delimitL(myFT.getTablename()) + "." + DBKernel.delimitL(fn) + "=";
			} else {
				System.err.println("mnF2 = null...\t" + myFT + "\t" + this);
			}
		}
		sql = "SELECT " + mnsqlc.getToSelect() + " FROM " + DBKernel.delimitL(myFT.getTablename()) + mnsqlc.getToJoin() + toWhere;
		return sql;
	}

	private String getMNJoin(MyTable mnT) {
		String toJoin = "";
		if (mnT != null) {
			String mnF1 = mnT.getForeignFieldName(this);
			if (mnF1 != null) {
				toJoin += " LEFT JOIN " + DBKernel.delimitL(mnT.getTablename()) + " ON " + DBKernel.delimitL(mnT.getTablename()) + "." + DBKernel.delimitL(mnF1) + "="
						+ DBKernel.delimitL(this.getTablename()) + "." + DBKernel.delimitL("ID");
			} else {
				System.err.println("mnF1 = null....");
			}
		}
		return toJoin;
	}

	public String getForeignFieldName(MyTable foreignT) {
		String[] fn = this.getFieldNames();
		for (int i = 0; i < fn.length; i++) {
			MyTable mt = this.getForeignFields()[i];
			if (mt != null && mt.equals(foreignT)) return fn[i];
		}
		return null;
	}

	public Integer getForeignFieldIndex(MyTable foreignT) {
		String[] fn = this.getFieldNames();
		for (int i = 0; i < fn.length; i++) {
			MyTable mt = this.getForeignFields()[i];
			if (mt != null && mt.equals(foreignT)) return i;
		}
		return null;
	}

	public Integer getFieldIndex(String fieldName) {
		if (fieldName != null && this.getFieldNames() != null) {
			String[] fn = this.getFieldNames();
			for (int i = 0; i < fn.length; i++) {
				if (fieldName.equals(fn[i])) {
					return i;
				}
			}
		}
		return null;
	}

	private MyTable getForeignTable(String fieldName) {
		if (fieldName != null && this != null && this.getForeignFields() != null) {
			String[] fn = this.getFieldNames();
			for (int i = 0; i < fn.length; i++) {
				if (fieldName.equals(fn[i])) {
					return this.getForeignFields()[i];
				}
			}
		}
		return null;
	}

	public void doMNs() {
		boolean dl = DBKernel.dontLog;
		boolean dlmk = MainKernel.dontLog;
		DBKernel.dontLog = true;
		MainKernel.dontLog = true;
		Vector<String> listMNs = this.getListMNs();
		if (listMNs != null) {
			String tableName = this.getTablename();
			// hier soll immer die ID drin stehen, die wird dann zur Darstellung
			// der M:N Beziehung ausgelesen.
			// Mach einfach für alle Zeilen, dauert ja nicht lange, oder?
			for (int i = 0; i < listMNs.size(); i++) {
				String feldname = listMNs.get(i);
				DBKernel.sendRequest(
						"UPDATE " + DBKernel.delimitL(tableName) + " SET " + DBKernel.delimitL(feldname) + "=" + DBKernel.delimitL("ID") + " WHERE " + DBKernel.delimitL(feldname)
								+ " IS NULL OR " + DBKernel.delimitL(feldname) + "!=" + DBKernel.delimitL("ID"), true);
			}
		}
		DBKernel.dontLog = dl;
		MainKernel.dontLog = dlmk;
	}

	public String getMetadata() {
		String result = "--------------  " + tableName + "  --------------\n";
		for (int i = 0; i < fieldNames.length; i++) {
			result += fieldNames[i] + "\t" + (fieldComments[i] == null ? fieldNames[i] : fieldComments[i]) + "\t" + fieldTypes[i] + "\n";
		}
		if (uniqueFields != null) {
			for (int i = 0; i < uniqueFields.length; i++) {
				String uFs = "";
				for (int j = 0; j < uniqueFields[i].length; j++) {
					uFs += "," + uniqueFields[i][j];
				}
				result += uFs.substring(1) + "\t";
			}
		}
		return result;
	}
}
