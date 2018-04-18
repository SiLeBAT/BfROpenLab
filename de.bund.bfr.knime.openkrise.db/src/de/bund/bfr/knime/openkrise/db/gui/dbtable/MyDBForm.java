/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
/*
 * Created by JFormDesigner on Thu Apr 19 22:09:03 CEST 2012
 */

package de.bund.bfr.knime.openkrise.db.gui.dbtable;

import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyTable;

/**
 * @author Armin Weiser
 */
@SuppressWarnings("rawtypes")
public class MyDBForm extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MyTable myT;
	private HashMap<String, JComponent> componentMap;
	private int initVal;
	
	public MyDBForm() {
		initComponents();	
		initCs();
	}
	private void initCs() {
		componentMap = new HashMap<>();
		componentMap.put("ID", textField1);
		componentMap.put("Referenz", textField2);
		componentMap.put("Agens", textField3);
		componentMap.put("AgensDetail", textField4);
		componentMap.put("Risikokategorie_CDC", comboBox1);
		componentMap.put("BioStoffV", comboBox2);
		componentMap.put("Krankheit", textField5);
		componentMap.put("Symptome", textArea1);
		componentMap.put("Zielpopulation", comboBox3);
		componentMap.put("Aufnahmeroute", comboBox4);
		componentMap.put("Krankheitsverlauf", comboBox5);
		componentMap.put("Risikogruppen", textArea2);
		componentMap.put("Inzidenz", textField6);
		componentMap.put("Inzidenz_Alter", textField7);
		componentMap.put("Inkubationszeit", textField8);
		componentMap.put("IZ_Einheit", comboBox6);
		componentMap.put("Symptomdauer", textField9);
		componentMap.put("SD_Einheit", comboBox7);
		componentMap.put("Infektionsdosis", textField10);
		componentMap.put("ID_Einheit", comboBox8);
		componentMap.put("Letalitaetsdosis50", textField11);
		componentMap.put("LD50_Einheit", comboBox9);
		componentMap.put("LD50_Organismus", comboBox10);
		componentMap.put("LD50_Aufnahmeroute", comboBox11);
		componentMap.put("Letalitaetsdosis100", textField12);
		componentMap.put("LD100_Einheit", comboBox12);
		componentMap.put("LD100_Organismus", comboBox13);
		componentMap.put("LD100_Aufnahmeroute", comboBox14);
		componentMap.put("Meldepflicht", comboBox15);
		componentMap.put("Morbiditaet", textField13);
		componentMap.put("Mortalitaet", textField14);
		componentMap.put("Letalitaet", textField15);
		componentMap.put("Therapie_Letal", comboBox22);
		componentMap.put("Ausscheidungsdauer", comboBox16);
		componentMap.put("ansteckend", comboBox17);
		componentMap.put("Therapie", comboBox18);
		componentMap.put("Antidot", comboBox19);
		componentMap.put("Impfung", comboBox20);
		componentMap.put("Todeseintritt", comboBox21);
		componentMap.put("Spaetschaeden", textArea4);
		componentMap.put("Komplikationen", textArea5);
		componentMap.put("Guetescore", textField18);
		componentMap.put("Kommentar", textArea3);
		componentMap.put("Geprueft", checkBox1);
		
		final Enumeration<String> strEnum = Collections.enumeration(componentMap.keySet());
		while(strEnum.hasMoreElements()) {
			JComponent c = componentMap.get(strEnum.nextElement());
			if (c != null) {
				if (c instanceof JTextField && !((JTextField) c).isEditable() || c instanceof JTextArea) {
					c.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							MyDBForm.this.mouseClicked(e);
						}
					});
				}
			}
		}		
	}
	private void initSB() {
		scrollBar1.setMinimum(1);
		scrollBar1.setMaximum(DBKernel.getRowCount(myT.getTablename(), null) + 1 + scrollBar1.getVisibleAmount());
	}
	boolean setTable(MyTable myT) {
		initVal = 0;
		this.myT = myT;
		initSB();
		initVal = 1;
		myT.restoreProperties(this);
		initVal = 2;

		return true;
	}
	public MyTable getActualTable() {
		return myT;
	}
	public void setSelectedID(int id) {
		String sql= "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(myT.getTablename());
		ResultSet rs = DBKernel.getResultSet(sql, false);
		try {
			int index = 1;
			if (rs != null && rs.first()) {
				do {
					if (id == rs.getInt("ID")) {
						break;
					}
					index++;
				} while (rs.next());
				if (scrollBar1.getValue() == index) fillWithData(myT.getTablename(), index); // refresh!
				else scrollBar1.setValue(index);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public int getSelectedID() {
		int result = -1;
		try {
			result = Integer.parseInt(textField1.getText());
		}
		catch (Exception e){}
		return result;
	}
	private void fillWithData(String tablename, int row) {
		String sql= "SELECT * FROM " + DBKernel.delimitL(tablename);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		try {
			if (rs != null && rs.absolute(row)) {
				textField1.setText(rs.getString("ID"));
				manageForeign(textField2, rs.getObject("Referenz"), getForeignVal("Literatur", rs.getInt("Referenz"), DBKernel.delimitL("Erstautor") + "," + DBKernel.delimitL("Jahr")));
				manageForeign(textField3, rs.getObject("Agens"), getForeignVal("Agenzien", rs.getInt("Agens"), DBKernel.delimitL("Agensname")));
				textField4.setText(rs.getString("AgensDetail"));
				comboBox1.setSelectedItem(rs.getString("Risikokategorie_CDC"));
				comboBox2.setSelectedItem(rs.getString("BioStoffV"));
				manageForeign(textField5, rs.getObject("Krankheit"), getForeignVal("ICD10_Kodes", rs.getInt("Krankheit"), DBKernel.delimitL("Titel")));
				getMNVals(textArea1, tablename, "Krankheitsbilder_Symptome", "Symptome", rs.getInt("Symptome"), DBKernel.delimitL("Bezeichnung_engl") + "," + DBKernel.delimitL("Bezeichnung")); //  + "," + DBKernel.delimitL("Bezeichnung")
				comboBox3.setSelectedItem(rs.getString("Zielpopulation"));
				comboBox4.setSelectedItem(rs.getString("Aufnahmeroute"));
				comboBox5.setSelectedItem(rs.getString("Krankheitsverlauf"));
				getMNVals(textArea2, tablename, "Krankheitsbilder_Risikogruppen", "Risikogruppen", rs.getInt("Risikogruppen"), DBKernel.delimitL("Bezeichnung"));
				manageForeign(textField6, rs.getObject("Inzidenz"), getForeignVal("DoubleKennzahlen", rs.getInt("Inzidenz"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				manageForeign(textField7, rs.getObject("Inzidenz_Alter"),getForeignVal("DoubleKennzahlen", rs.getInt("Inzidenz_Alter"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				manageForeign(textField8, rs.getObject("Inkubationszeit"), getForeignVal("DoubleKennzahlen", rs.getInt("Inkubationszeit"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				comboBox6.setSelectedItem(rs.getString("IZ_Einheit"));

				manageForeign(textField9, rs.getObject("Symptomdauer"), getForeignVal("DoubleKennzahlen", rs.getInt("Symptomdauer"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				comboBox7.setSelectedItem(rs.getString("SD_Einheit"));
				manageForeign(textField10, rs.getObject("Infektionsdosis"), getForeignVal("DoubleKennzahlen", rs.getInt("Infektionsdosis"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				comboBox8.setSelectedItem(rs.getString("ID_Einheit"));
				manageForeign(textField11, rs.getObject("Letalitaetsdosis50"), getForeignVal("DoubleKennzahlen", rs.getInt("Letalitaetsdosis50"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				comboBox9.setSelectedItem(rs.getString("LD50_Einheit"));
				comboBox10.setSelectedItem(rs.getString("LD50_Organismus"));
				comboBox11.setSelectedItem(rs.getString("LD50_Aufnahmeroute"));
				manageForeign(textField12, rs.getObject("Letalitaetsdosis100"), getForeignVal("DoubleKennzahlen", rs.getInt("Letalitaetsdosis100"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				comboBox12.setSelectedItem(rs.getString("LD100_Einheit"));
				comboBox13.setSelectedItem(rs.getString("LD100_Organismus"));
				comboBox14.setSelectedItem(rs.getString("LD100_Aufnahmeroute"));
				comboBox15.setSelectedItem(rs.getObject("Meldepflicht") == null ? null : (rs.getBoolean("Meldepflicht") ? "ja" : "nein"));
				manageForeign(textField13, rs.getObject("Morbiditaet"), getForeignVal("DoubleKennzahlen", rs.getInt("Morbiditaet"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				manageForeign(textField14, rs.getObject("Mortalitaet"), getForeignVal("DoubleKennzahlen", rs.getInt("Mortalitaet"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				manageForeign(textField15, rs.getObject("Letalitaet"), getForeignVal("DoubleKennzahlen", rs.getInt("Letalitaet"), DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Minimum") + "," + DBKernel.delimitL("Maximum")));
				comboBox22.setSelectedItem(rs.getObject("Therapie_Letal") == null ? null : (rs.getInt("Therapie_Letal") == 1 ? "mit Therapie" : rs.getInt("Therapie_Letal") == 2 ? "Keine Angabe" : "ohne Therapie"));
				comboBox16.setSelectedItem(rs.getString("Ausscheidungsdauer"));
				comboBox17.setSelectedItem(rs.getObject("ansteckend") == null ? null : (rs.getBoolean("ansteckend") ? "ja" : "nein"));
				comboBox18.setSelectedItem(rs.getObject("Therapie") == null ? null : (rs.getBoolean("Therapie") ? "ja" : "nein"));
				comboBox19.setSelectedItem(rs.getObject("Antidot") == null ? null : (rs.getBoolean("Antidot") ? "ja" : "nein"));
				comboBox20.setSelectedItem(rs.getObject("Impfung") == null ? null : (rs.getBoolean("Impfung") ? "ja" : "nein"));
				comboBox21.setSelectedItem(rs.getString("Todeseintritt"));
				textArea4.setText(rs.getString("Spaetschaeden"));
				textArea5.setText(rs.getString("Komplikationen"));
				textField18.setText(rs.getString("Guetescore"));
				textArea3.setText(rs.getString("Kommentar"));
				checkBox1.setSelected(rs.getBoolean("Geprueft"));
			}
			else {
				final Enumeration<String> strEnum = Collections.enumeration(componentMap.keySet());
				while(strEnum.hasMoreElements()) {
					JComponent c = componentMap.get(strEnum.nextElement());
					if (c != null) {
						c.setToolTipText(null);
						if (c instanceof JTextField) ((JTextField) c).setText(null);
						else if (c instanceof JComboBox) ((JComboBox) c).setSelectedItem(null);
						else if (c instanceof JTextArea) ((JTextArea) c).setText(null);
				    }
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void manageForeign(JTextField tf, Object id, String foreignVal) {
		if (id == null) {
			tf.setText(null);
			tf.setToolTipText(null);
		}
		else {
			tf.setText(foreignVal);
			tf.setToolTipText(id.toString());
		}
	}
	private void getMNVals(JTextArea textArea, String firstTablename, String mnTablename, String secondTablename, int id, String fields) {
		textArea.setText("");
		textArea.setToolTipText(id+"");
		String sql= "SELECT " + fields + " FROM " + DBKernel.delimitL(firstTablename) +
				" LEFT JOIN " + DBKernel.delimitL(mnTablename) +
				" ON " + DBKernel.delimitL(firstTablename) + "." + DBKernel.delimitL("ID") + "=" + DBKernel.delimitL(mnTablename) + "." + DBKernel.delimitL(firstTablename) +
				" LEFT JOIN " + DBKernel.delimitL(secondTablename) +
				" ON " + DBKernel.delimitL(secondTablename) + "." + DBKernel.delimitL("ID") + "=" + DBKernel.delimitL(mnTablename) + "." + DBKernel.delimitL(secondTablename) + 
				" WHERE " + DBKernel.delimitL(firstTablename) + "." + DBKernel.delimitL("ID") + "=" + id;
		ResultSet rs = DBKernel.getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				do {
					String row = "";
					for (int i=1;i<=rs.getMetaData().getColumnCount();i++) {
						if (rs.getObject(i) != null) {
							if (!row.isEmpty()) row += "\t";
							row += rs.getString(i);
						}
					}	
					textArea.append(row + "\n");
				} while (rs.next());
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private String getForeignVal(String tablename, int id, String fields) {
		String result = "";
		String sql= "SELECT " + fields + " FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + "=" + id;
		ResultSet rs = DBKernel.getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				boolean isDbl = fields.startsWith(DBKernel.delimitL("Wert") + ",");
				for (int i=1;i<=rs.getMetaData().getColumnCount();i++) {
					if (rs.getObject(i) != null) {
						if (!result.isEmpty()) result += "; ";
						if (isDbl) result += rs.getMetaData().getColumnName(i) + ": ";
						if (rs.getMetaData().getColumnType(i) == java.sql.Types.DOUBLE) result += DBKernel.getDoubleStr(rs.getDouble(i));
						else result += rs.getString(i);
					}
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	
	void save() {
		//System.err.println("2Save");
		//System.err.println(myT.getSelectSQL());
		//System.err.println(myT.getUpdateSQL1());
		//System.err.println(myT.getUpdateSQL2());
		//System.err.println(myT.getInsertSQL1());
		//System.err.println(myT.getInsertSQL2());
		//System.err.println(myT.getDeleteSQL1());
		//System.err.println(myT.getDeleteSQL2());
		if (newDS()) insertNewRow();
		else updateRow();
		myT.saveProperties(this);
	}
	private boolean newDS() {
		String id = ((JTextField) componentMap.get("ID")).getText();
		if (id == null || id.isEmpty()) return true;
		return false; // scrollBar1.getValue() == scrollBar1.getMaximum() - scrollBar1.getVisibleAmount();
	}
	private void updateRow() {
		if (DBKernel.isReadOnly()) return;
	      try {
	    	  // Geprüft erstmal nicht berücksichtigen, das gibt nur Ärger: dauernd Update der DB,
	    	  // weil hier so nur false gespeichert werden kann und nicht NULL. Es sind aber viele NULLen in der DB drin
	    	  String sql = myT.getUpdateSQL1().replace(",\"Geprueft\"=?", "");
	    	  //System.err.println(sql);
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql);
			managePs(ps);
			// Achtung hier "myT.getNumFields()-1" bis Geprüft wieder integriert ist!
			ps.setInt(myT.getNumFields()-1, getInt(((JTextField) componentMap.get("ID")).getText()));
			
			ps.execute();
			myT.doMNs();
			initVal = 1; initSB(); initVal = 2;
		}
	      catch (SQLException e) {
			e.printStackTrace();
		}
	      catch (Exception e) {
			e.printStackTrace();
		}					
	}
	private boolean insertNewRow() {
		if (DBKernel.isReadOnly()) return false;
		boolean allNull = true;
	      try {
	    	  // Geprüft erstmal nicht berücksichtigen, das gibt nur Ärger: dauernd Update der DB,
	    	  // weil hier so nur false gespeichert werden kann und nicht NULL. Es ind aber viele NULLen in der DB drin
	    	  String sql = myT.getInsertSQL1().replace(",\"Geprueft\"", "").replace(",?)", ")");
	    	  //System.err.println(sql);
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);			
			allNull = managePs(ps);
			
			if (!allNull) {
				if (ps.executeUpdate() > 0) {
					DBKernel.getLastInsertedID(ps);
				}
				myT.doMNs();
				initVal = 1; initSB(); initVal = 2;
			}
		}
	      catch (SQLException e) {
			e.printStackTrace();
		}
	      catch (Exception e) {
			e.printStackTrace();
		}			
	      return allNull;
	}
	void deleteRow() {
		if (!newDS()) {
			int retVal = JOptionPane.showConfirmDialog(this, "Sind Sie sicher, daß Sie diesen Datensatz löschen möchten?",
		    		"Löschen bestätigen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    if (retVal == JOptionPane.YES_OPTION) {
				DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL(myT.getTablename()) +
						" WHERE " + DBKernel.delimitL("ID") + " = " + getSelectedID(), false);
				initVal = 1; initSB(); initVal = 2;
		    }
		}
	}
	void gotoInsertNewRow() {
		scrollBar1.setValue(scrollBar1.getMaximum());
	}

	private boolean managePs(PreparedStatement ps) throws SQLException {
		boolean allNullVals = true;
		String[] fieldTypes = myT.getFieldTypes();
		String[] fieldNames = myT.getFieldNames();
		MyTable[] fieldFields = myT.getForeignFields();
		String mnTable[] = myT.getMNTable();
		int i;
	    for (i=0;i<fieldTypes.length;i++) {
	    	if (componentMap.get(fieldNames[i]) == null) {
	    		System.err.println("managePs, null:" + fieldNames[i]);
	    		if (myT.getFieldTypes()[i].equals("INTEGER")) ps.setNull(i+1, java.sql.Types.INTEGER);
	    		else if (myT.getFieldTypes()[i].startsWith("VARCHAR")) ps.setNull(i+1, java.sql.Types.VARCHAR);
	    		else if (myT.getFieldTypes()[i].equals("DOUBLE")) ps.setNull(i+1, java.sql.Types.DOUBLE);
	    		else if (myT.getFieldTypes()[i].equals("BOOLEAN")) ps.setNull(i+1, java.sql.Types.BOOLEAN);
	    		else System.err.println("Type not known");
	    	}
	    	else {
	    		// MN Table
				if (fieldFields[i] != null && (myT.getFieldTypes()[i].equals("INTEGER")) &&
						mnTable[i] != null) {
					allNullVals = handleInt(ps, i+1, ((JTextArea) componentMap.get(fieldNames[i])).getToolTipText()) && allNullVals;					
				}
				// Foreign Table
				else if (fieldFields[i] != null && (myT.getFieldTypes()[i].equals("INTEGER"))) {
					allNullVals = handleInt(ps, i+1, ((JTextField) componentMap.get(fieldNames[i])).getToolTipText()) && allNullVals;					
				}
				// DoubleKennzahlen
				else if (fieldFields[i] != null && myT.getFieldTypes()[i].equals("DOUBLE") &&
						fieldFields[i].getTablename().equals("DoubleKennzahlen")) {
					allNullVals = handleDbl(ps, i+1, ((JTextField) componentMap.get(fieldNames[i])).getToolTipText()) && allNullVals;					
				}
				// Strings: JTextArea und JTextField
				else if (myT.getFieldTypes()[i].startsWith("VARCHAR")) {
					allNullVals = handleStr(ps, i+1, componentMap.get(fieldNames[i])) && allNullVals;	
				}
				// mit Therapie, ohne Therapie, Achtung, das ist ein special case!!! Den gibts nur in diesem Table!!!!
				else if (myT.getFieldTypes()[i].equals("INTEGER")) {
					allNullVals = handleTherapie(ps, i+1, ((JComboBox) componentMap.get(fieldNames[i])).getSelectedItem()) && allNullVals;	
				}
				// ja, nein
				else if (myT.getFieldTypes()[i].equals("BOOLEAN")) {
					allNullVals = handleBool(ps, i+1, ((JComboBox) componentMap.get(fieldNames[i])).getSelectedItem()) && allNullVals;	
				}
				else {
					System.err.println("Type not known");
				}
	    	}
	    }
	    if (!myT.getHideScore()) allNullVals = handleInt(ps, ++i, ((JTextField) componentMap.get("Guetescore")).getText()) && allNullVals;
	    if (!myT.getHideKommentar()) allNullVals = handleStr(ps, ++i, componentMap.get("Kommentar")) && allNullVals;
	    // Geprüft mache ich erstmal nur Read-Only!! is zu bucklig wegen 1. null berücksichtigen, 2. die ganze abfragelogik von wegen ein anderer Nutzer darf nur usw...!
	    //if (!myT.getHideTested()) allNullVals = handleBool(ps, ++i, componentMap.get("Geprueft")) && allNullVals;
	    
	    return allNullVals;
	}
	private Integer getInt(String value) {
		Integer result = null;
		try {
			result = Integer.parseInt(value);
		}
		catch (Exception e) {}
		return result;
	}
	private Double getDbl(String value) {
		Double result = null;
		try {
			result = Double.parseDouble(value);
		}
		catch (Exception e) {}
		return result;
	}
	private boolean handleInt(PreparedStatement ps, int index, String val) throws SQLException {
		boolean nullVal = true;
		Integer iVal = getInt(val);
		if (val == null || iVal == null) {
			ps.setNull(index, java.sql.Types.INTEGER);
		}
		else {
			ps.setInt(index, iVal);
			nullVal = false;
		}
		return nullVal;
	}
	private boolean handleDbl(PreparedStatement ps, int index, String val) throws SQLException {
		boolean nullVal = true;
		Double iVal = getDbl(val);
		if (val == null || iVal == null) {
			ps.setNull(index, java.sql.Types.DOUBLE);
		}
		else {
			ps.setDouble(index, iVal);
			nullVal = false;
		}
		return nullVal;
	}
	private boolean handleStr(PreparedStatement ps, int index, JComponent c) throws SQLException {
		boolean nullVal = true;
		String val = null;
		if (c instanceof JTextField) val = ((JTextField) c).getText();
		else if (c instanceof JComboBox) val = ((JComboBox) c).getSelectedItem() == null ? null : ((JComboBox) c).getSelectedItem().toString();
		else if (c instanceof JTextArea) val = ((JTextArea) c).getText() == null ? null : ((JTextArea) c).getText();
		if (val == null || val.trim().isEmpty()) {
			ps.setNull(index, java.sql.Types.VARCHAR);
		}
		else {
			ps.setString(index, val);
			nullVal = false;
		}
		return nullVal;
	}
	private boolean handleBool(PreparedStatement ps, int index, Object val) throws SQLException {
		boolean nullVal = true;
		if (val instanceof JCheckBox) {
			ps.setBoolean(index, ((JCheckBox) val).isSelected());
			nullVal = false;
		}
		else {
			if (val == null || val.toString().trim().isEmpty()) {
				ps.setNull(index, java.sql.Types.BOOLEAN);
			}
			else {
				ps.setBoolean(index, val.toString().equalsIgnoreCase("ja"));
				nullVal = false;
			}
		}
		return nullVal;
	}
	private boolean handleTherapie(PreparedStatement ps, int index, Object val) throws SQLException {
		boolean nullVal = true;
		if (val == null || val.toString().trim().isEmpty()) {
			ps.setNull(index, java.sql.Types.INTEGER);
		}
		else {
			String str = val.toString();
			ps.setInt(index, str.equalsIgnoreCase("mit Therapie") ? 1 : str.equalsIgnoreCase("Keine Angabe") ? 2 : 0);
			nullVal = false;
		}
		return nullVal;
	}
	
	
	private void scrollBar1AdjustmentValueChanged(AdjustmentEvent e) {
		if (initVal > 0 && !e.getValueIsAdjusting()) {
			if (initVal > 1) { // Änderungen abspeichern!
				if (newDS()) insertNewRow();
				else updateRow();
			}
			if (scrollBar1.getValue() == scrollBar1.getMaximum() - scrollBar1.getVisibleAmount()) {
				label42.setText("Neuer Datensatz");
			}
			else {
				label42.setText("Datensatz " + scrollBar1.getValue() + " von " + (scrollBar1.getMaximum() - 1 - scrollBar1.getVisibleAmount()));
			}
			fillWithData(myT.getTablename(), scrollBar1.getValue() );			
		}
	}

	private void mouseClicked(MouseEvent e) {
		switch(e.getModifiers()) {
	      case InputEvent.BUTTON1_MASK: {
	        if (e.getClickCount() > 1) go4OtherWindow(e);
	        break;
	        }
	      case InputEvent.BUTTON2_MASK: {
	    	  go4OtherWindow(e);
	        break;
	        }
	      case InputEvent.BUTTON3_MASK: {
	    	  go4OtherWindow(e);
	        break;
	        }
	      }
	}
	private String getKey(JComponent comp) {
		String key = null;
		final Enumeration<String> strEnum = Collections.enumeration(componentMap.keySet());
		while(strEnum.hasMoreElements()) {
			key = strEnum.nextElement();
			JComponent c = componentMap.get(key);
			if (c != null) {
				if (c.equals(comp)) break;
			}
		}
		return key;
	}
	private MyTable getMyFT(String key) {
		if (key != null) {
			String[] myFields = myT.getFieldNames();
			int i=0;
			for (;i<myFields.length;i++) {
				if (myFields[i].equals(key)) {
					break;
				}
			}
			if (i < myFields.length) {
				return myT.getForeignFields()[i];
			}
		}
		return null;
	}
	private String getMyMNT(String key) {
		if (key != null) {
			String[] myFields = myT.getFieldNames();
			int i=0;
			for (;i<myFields.length;i++) {
				if (myFields[i].equals(key)) {
					break;
				}
			}
			if (i < myFields.length) {
				return myT.getMNTable()[i];
			}
		}
		return null;
	}
	private void go4OtherWindow(MouseEvent e) {
        JComponent c = (JComponent) e.getSource();
		String key = getKey(c);
		MyTable myFT = getMyFT(key);
		String myMNT = getMyMNT(key);
		if (myFT != null) {
			if (myFT.getTablename().equals("DoubleKennzahlen")) checkOtherEditor2Open(c, key, myFT, e.getLocationOnScreen().x, e.getLocationOnScreen().y);
			else checkForeignWindow2Open(c, key, myFT, myMNT);
		}
		save();
		fillWithData(myT.getTablename(), scrollBar1.getValue() );			
	}
	private void checkForeignWindow2Open(JComponent c, String key, MyTable myFT, String myMNT) {
		String id = ((JTextField) componentMap.get("ID")).getText();
		if (!id.isEmpty() || myMNT == null || myMNT.isEmpty()) { // sonst gibt es Fehler in der M:N Logik
			Object newVal = DBKernel.mainFrame.openNewWindow(myFT, getInt(c.getToolTipText()), key, myMNT, id, this);
			//System.err.println(key + "\t" + newVal + "\t" + c.getToolTipText());
			if (newVal != null) c.setToolTipText(newVal.toString());
		}
	}
	private void checkOtherEditor2Open(JComponent c, String key, MyTable myFT, int x, int y) {
		System.out.println(x + "\t" + y + "\t" + c.getX() + "\t" + c.getY());
		/*
		MyNewDoubleEditor mde = new MyNewDoubleEditor(c.getToolTipText(), key, x, y, ' ');		
		mde.setVisible(true);
		if (mde.savePressed()) {
			Double newD = mde.getNewValue();
			if (newD != null) {
				//System.err.println(DBKernel.getDoubleStr(newD));
				c.setToolTipText(newD.toString());
			}
		}
		*/
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		scrollPane3 = new JScrollPane();
		panel1 = new JPanel();
		label1 = new JLabel();
		textField1 = new JTextField();
		label13 = new JLabel();
		textField6 = new JTextField();
		label30 = new JLabel();
		textField13 = new JTextField();
		label2 = new JLabel();
		textField2 = new JTextField();
		label14 = new JLabel();
		textField7 = new JTextField();
		label31 = new JLabel();
		textField14 = new JTextField();
		label3 = new JLabel();
		textField3 = new JTextField();
		label19 = new JLabel();
		textField10 = new JTextField();
		label32 = new JLabel();
		textField15 = new JTextField();
		label4 = new JLabel();
		textField4 = new JTextField();
		label20 = new JLabel();
		comboBox8 = new JComboBox();
		label33 = new JLabel();
		comboBox22 = new JComboBox();
		label5 = new JLabel();
		comboBox1 = new JComboBox();
		label15 = new JLabel();
		textField8 = new JTextField();
		label39 = new JLabel();
		comboBox21 = new JComboBox();
		label6 = new JLabel();
		comboBox2 = new JComboBox();
		label16 = new JLabel();
		comboBox6 = new JComboBox();
		panel2 = new JPanel();
		label47 = new JLabel();
		comboBox10 = new JComboBox();
		label48 = new JLabel();
		comboBox11 = new JComboBox();
		label45 = new JLabel();
		textField11 = new JTextField();
		label46 = new JLabel();
		comboBox9 = new JComboBox();
		label9 = new JLabel();
		comboBox3 = new JComboBox();
		label17 = new JLabel();
		textField9 = new JTextField();
		label10 = new JLabel();
		comboBox4 = new JComboBox();
		label18 = new JLabel();
		comboBox7 = new JComboBox();
		label7 = new JLabel();
		textField5 = new JTextField();
		label34 = new JLabel();
		comboBox16 = new JComboBox();
		label29 = new JLabel();
		comboBox15 = new JComboBox();
		panel4 = new JPanel();
		label35 = new JLabel();
		comboBox17 = new JComboBox();
		label36 = new JLabel();
		comboBox18 = new JComboBox();
		label37 = new JLabel();
		comboBox19 = new JComboBox();
		label38 = new JLabel();
		comboBox20 = new JComboBox();
		label8 = new JLabel();
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea();
		panel3 = new JPanel();
		label51 = new JLabel();
		comboBox13 = new JComboBox();
		label52 = new JLabel();
		comboBox14 = new JComboBox();
		label49 = new JLabel();
		textField12 = new JTextField();
		label50 = new JLabel();
		comboBox12 = new JComboBox();
		label11 = new JLabel();
		comboBox5 = new JComboBox();
		label12 = new JLabel();
		scrollPane2 = new JScrollPane();
		textArea2 = new JTextArea();
		label40 = new JLabel();
		scrollPane5 = new JScrollPane();
		textArea4 = new JTextArea();
		label41 = new JLabel();
		scrollPane6 = new JScrollPane();
		textArea5 = new JTextArea();
		label44 = new JLabel();
		scrollPane4 = new JScrollPane();
		textArea3 = new JTextArea();
		label43 = new JLabel();
		textField18 = new JTextField();
		checkBox1 = new JCheckBox();
		label42 = new JLabel();
		scrollBar1 = new JScrollBar();

		//======== this ========
		setPreferredSize(new Dimension(900, 600));
		setBackground(new Color(255, 204, 255));
		setLayout(new FormLayout(
			"default:grow",
			"fill:default:grow, 2*($lgap, default)"));

		//======== scrollPane3 ========
		{
			scrollPane3.setBorder(Borders.DIALOG);
			scrollPane3.setBackground(new Color(255, 204, 255));

			//======== panel1 ========
			{
				panel1.setBorder(null);
				panel1.setBackground(new Color(255, 204, 255));
				panel1.setLayout(new FormLayout(
					"150px, $lcgap, 230px, $lcgap, 150px, $lcgap, 230px, $lcgap, 150px, $lcgap, 230px",
					"18*(default, $lgap), default"));
				((FormLayout)panel1.getLayout()).setColumnGroups(new int[][] {{1, 5, 9}, {3, 7, 11}});
				((FormLayout)panel1.getLayout()).setRowGroups(new int[][] {{1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 37}});

				//---- label1 ----
				label1.setText("ID");
				label1.setHorizontalAlignment(SwingConstants.RIGHT);
				label1.setBackground(new Color(208, 193, 251));
				label1.setOpaque(true);
				panel1.add(label1, CC.xy(1, 1));

				//---- textField1 ----
				textField1.setEditable(false);
				textField1.setEnabled(false);
				panel1.add(textField1, CC.xy(3, 1));

				//---- label13 ----
				label13.setText("Inzidenz");
				label13.setHorizontalAlignment(SwingConstants.RIGHT);
				label13.setOpaque(true);
				label13.setBackground(new Color(208, 193, 251));
				label13.setFont(label13.getFont().deriveFont(label13.getFont().getStyle() | Font.BOLD));
				panel1.add(label13, CC.xy(5, 1));

				//---- textField6 ----
				textField6.setEditable(false);
				panel1.add(textField6, CC.xy(7, 1));

				//---- label30 ----
				label30.setText("Morbiditaet");
				label30.setHorizontalAlignment(SwingConstants.RIGHT);
				label30.setBackground(new Color(208, 193, 251));
				label30.setOpaque(true);
				label30.setFont(label30.getFont().deriveFont(label30.getFont().getStyle() | Font.BOLD));
				panel1.add(label30, CC.xy(9, 1));

				//---- textField13 ----
				textField13.setEditable(false);
				panel1.add(textField13, CC.xy(11, 1));

				//---- label2 ----
				label2.setText("Referenz");
				label2.setHorizontalAlignment(SwingConstants.RIGHT);
				label2.setOpaque(true);
				label2.setBackground(new Color(208, 193, 251));
				panel1.add(label2, CC.xy(1, 3));

				//---- textField2 ----
				textField2.setEditable(false);
				panel1.add(textField2, CC.xy(3, 3));

				//---- label14 ----
				label14.setText("Inzidenz_Alter");
				label14.setHorizontalAlignment(SwingConstants.RIGHT);
				label14.setOpaque(true);
				label14.setBackground(new Color(208, 193, 251));
				panel1.add(label14, CC.xy(5, 3));

				//---- textField7 ----
				textField7.setEditable(false);
				panel1.add(textField7, CC.xy(7, 3));

				//---- label31 ----
				label31.setText("Mortalitaet");
				label31.setHorizontalAlignment(SwingConstants.RIGHT);
				label31.setBackground(new Color(208, 193, 251));
				label31.setOpaque(true);
				label31.setFont(label31.getFont().deriveFont(label31.getFont().getStyle() | Font.BOLD));
				panel1.add(label31, CC.xy(9, 3));

				//---- textField14 ----
				textField14.setEditable(false);
				panel1.add(textField14, CC.xy(11, 3));

				//---- label3 ----
				label3.setText("Agens");
				label3.setHorizontalAlignment(SwingConstants.RIGHT);
				label3.setOpaque(true);
				label3.setBackground(new Color(208, 193, 251));
				label3.setFont(label3.getFont().deriveFont(label3.getFont().getStyle() | Font.BOLD));
				panel1.add(label3, CC.xy(1, 5));

				//---- textField3 ----
				textField3.setEditable(false);
				panel1.add(textField3, CC.xy(3, 5));

				//---- label19 ----
				label19.setText("Infektionsdosis");
				label19.setHorizontalAlignment(SwingConstants.RIGHT);
				label19.setBackground(new Color(208, 193, 251));
				label19.setOpaque(true);
				label19.setFont(label19.getFont().deriveFont(label19.getFont().getStyle() | Font.BOLD));
				panel1.add(label19, CC.xy(5, 5));

				//---- textField10 ----
				textField10.setEditable(false);
				panel1.add(textField10, CC.xy(7, 5));

				//---- label32 ----
				label32.setText("Letalitaet");
				label32.setHorizontalAlignment(SwingConstants.RIGHT);
				label32.setBackground(new Color(208, 193, 251));
				label32.setOpaque(true);
				label32.setFont(label32.getFont().deriveFont(label32.getFont().getStyle() | Font.BOLD));
				panel1.add(label32, CC.xy(9, 5));

				//---- textField15 ----
				textField15.setEditable(false);
				panel1.add(textField15, CC.xy(11, 5));

				//---- label4 ----
				label4.setText("AgensDetail");
				label4.setHorizontalAlignment(SwingConstants.RIGHT);
				label4.setOpaque(true);
				label4.setBackground(new Color(208, 193, 251));
				panel1.add(label4, CC.xy(1, 7));
				panel1.add(textField4, CC.xy(3, 7));

				//---- label20 ----
				label20.setText("ID_Einheit");
				label20.setHorizontalAlignment(SwingConstants.RIGHT);
				label20.setBackground(new Color(208, 193, 251));
				label20.setOpaque(true);
				panel1.add(label20, CC.xy(5, 7));

				//---- comboBox8 ----
				comboBox8.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"Sporenzahl",
					"KBE pro g",
					"KBE pro ml",
					"PBE pro g",
					"PBE pro ml",
					"Milligramm",
					"Mikrogramm",
					"\u00b5g/kg/KG",
					"Anzahl"
				}));
				panel1.add(comboBox8, CC.xy(7, 7));

				//---- label33 ----
				label33.setText("Therapie_Letal");
				label33.setHorizontalAlignment(SwingConstants.RIGHT);
				label33.setBackground(new Color(208, 193, 251));
				label33.setOpaque(true);
				panel1.add(label33, CC.xy(9, 7));

				//---- comboBox22 ----
				comboBox22.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"mit Therapie",
					"ohne Therapie",
					"Keine Angabe"
				}));
				panel1.add(comboBox22, CC.xy(11, 7));

				//---- label5 ----
				label5.setText("Risikokategorie_CDC");
				label5.setHorizontalAlignment(SwingConstants.RIGHT);
				label5.setOpaque(true);
				label5.setBackground(new Color(208, 193, 251));
				label5.setFont(label5.getFont().deriveFont(label5.getFont().getStyle() | Font.BOLD));
				panel1.add(label5, CC.xy(1, 9));

				//---- comboBox1 ----
				comboBox1.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"A",
					"B",
					"C"
				}));
				panel1.add(comboBox1, CC.xy(3, 9));

				//---- label15 ----
				label15.setText("Inkubationszeit");
				label15.setHorizontalAlignment(SwingConstants.RIGHT);
				label15.setBackground(new Color(208, 193, 251));
				label15.setOpaque(true);
				label15.setFont(label15.getFont().deriveFont(label15.getFont().getStyle() | Font.BOLD));
				panel1.add(label15, CC.xy(5, 9));

				//---- textField8 ----
				textField8.setEditable(false);
				panel1.add(textField8, CC.xy(7, 9));

				//---- label39 ----
				label39.setText("Todeseintritt");
				label39.setHorizontalAlignment(SwingConstants.RIGHT);
				label39.setBackground(new Color(208, 193, 251));
				label39.setOpaque(true);
				panel1.add(label39, CC.xy(9, 9));

				//---- comboBox21 ----
				comboBox21.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"Sekunde",
					"Minute",
					"Stunde",
					"Tag",
					"Woche",
					"Monat",
					"Jahr"
				}));
				panel1.add(comboBox21, CC.xy(11, 9));

				//---- label6 ----
				label6.setText("BioStoffV");
				label6.setHorizontalAlignment(SwingConstants.RIGHT);
				label6.setOpaque(true);
				label6.setBackground(new Color(208, 193, 251));
				label6.setFont(label6.getFont().deriveFont(label6.getFont().getStyle() | Font.BOLD));
				panel1.add(label6, CC.xy(1, 11));

				//---- comboBox2 ----
				comboBox2.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"1",
					"1*",
					"1**",
					"2",
					"2*",
					"2**",
					"3",
					"3*",
					"3**",
					"4",
					"4*",
					"4**"
				}));
				panel1.add(comboBox2, CC.xy(3, 11));

				//---- label16 ----
				label16.setText("IZ_Einheit");
				label16.setHorizontalAlignment(SwingConstants.RIGHT);
				label16.setBackground(new Color(208, 193, 251));
				label16.setOpaque(true);
				panel1.add(label16, CC.xy(5, 11));

				//---- comboBox6 ----
				comboBox6.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"Sekunde",
					"Minute",
					"Stunde",
					"Tag",
					"Woche",
					"Monat",
					"Jahr"
				}));
				panel1.add(comboBox6, CC.xy(7, 11));

				//======== panel2 ========
				{
					panel2.setBorder(new TitledBorder(null, "LD50", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Segoe UI", Font.BOLD, 12)));
					panel2.setBackground(new Color(255, 204, 255));
					panel2.setLayout(new FormLayout(
						"150px, $lcgap, default:grow",
						"3*(default:grow, $lgap), default:grow"));

					//---- label47 ----
					label47.setText("Organismus");
					label47.setHorizontalAlignment(SwingConstants.RIGHT);
					label47.setBackground(new Color(208, 193, 251));
					label47.setOpaque(true);
					panel2.add(label47, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- comboBox10 ----
					comboBox10.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"Human",
						"Kaninchen",
						"Maus",
						"Ratte",
						"Meerschweinchen",
						"Primaten",
						"sonst. S\u00e4ugetier"
					}));
					panel2.add(comboBox10, new CellConstraints(3, 1, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- label48 ----
					label48.setText("Aufnahmeroute");
					label48.setHorizontalAlignment(SwingConstants.RIGHT);
					label48.setBackground(new Color(208, 193, 251));
					label48.setOpaque(true);
					panel2.add(label48, CC.xy(1, 3));

					//---- comboBox11 ----
					comboBox11.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"inhalativ",
						"oral",
						"dermal",
						"Blut/Serum/K\u00f6rperfl\u00fcssigkeit",
						"h\u00e4matogen",
						"transplazental",
						"kutan",
						"venerisch",
						"transkutan",
						"intraperitoneal",
						"intraven\u00f6s",
						"subkutan",
						"intramuskul\u00e4r",
						"Injektion"
					}));
					panel2.add(comboBox11, new CellConstraints(3, 3, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- label45 ----
					label45.setText("Dosis");
					label45.setHorizontalAlignment(SwingConstants.RIGHT);
					label45.setBackground(new Color(208, 193, 251));
					label45.setOpaque(true);
					panel2.add(label45, new CellConstraints(1, 5, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- textField11 ----
					textField11.setEditable(false);
					panel2.add(textField11, new CellConstraints(3, 5, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- label46 ----
					label46.setText("Einheit");
					label46.setHorizontalAlignment(SwingConstants.RIGHT);
					label46.setBackground(new Color(208, 193, 251));
					label46.setOpaque(true);
					panel2.add(label46, new CellConstraints(1, 7, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- comboBox9 ----
					comboBox9.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"Sporenzahl",
						"KBE pro g",
						"KBE pro ml",
						"PBE pro g",
						"PBE pro ml",
						"Milligramm",
						"Mikrogramm",
						"\u00b5g/kg/KG",
						"Anzahl"
					}));
					panel2.add(comboBox9, new CellConstraints(3, 7, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));
				}
				panel1.add(panel2, new CellConstraints(9, 11, 3, 11, CC.DEFAULT, CC.DEFAULT, new Insets(4, 4, 4, 4)));

				//---- label9 ----
				label9.setText("Zielpopulation");
				label9.setHorizontalAlignment(SwingConstants.RIGHT);
				label9.setOpaque(true);
				label9.setBackground(new Color(208, 193, 251));
				panel1.add(label9, CC.xy(1, 13));

				//---- comboBox3 ----
				comboBox3.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"Human",
					"Kaninchen",
					"Maus",
					"Ratte",
					"Meerschweinchen",
					"Primaten",
					"sonst. S\u00e4ugetier"
				}));
				panel1.add(comboBox3, CC.xy(3, 13));

				//---- label17 ----
				label17.setText("Symptomdauer");
				label17.setHorizontalAlignment(SwingConstants.RIGHT);
				label17.setBackground(new Color(208, 193, 251));
				label17.setOpaque(true);
				label17.setFont(label17.getFont().deriveFont(label17.getFont().getStyle() | Font.BOLD));
				panel1.add(label17, CC.xy(5, 13));

				//---- textField9 ----
				textField9.setEditable(false);
				panel1.add(textField9, CC.xy(7, 13));

				//---- label10 ----
				label10.setText("Aufnahmeroute");
				label10.setHorizontalAlignment(SwingConstants.RIGHT);
				label10.setOpaque(true);
				label10.setBackground(new Color(208, 193, 251));
				panel1.add(label10, CC.xy(1, 15));

				//---- comboBox4 ----
				comboBox4.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"inhalativ",
					"oral",
					"dermal",
					"Blut/Serum/K\u00f6rperfl\u00fcssigkeit",
					"h\u00e4matogen",
					"transplazental",
					"kutan",
					"venerisch",
					"transkutan",
					"intraperitoneal",
					"intraven\u00f6s",
					"subkutan",
					"intramuskul\u00e4r",
					"Injektion"
				}));
				panel1.add(comboBox4, CC.xy(3, 15));

				//---- label18 ----
				label18.setText("SD_Einheit");
				label18.setHorizontalAlignment(SwingConstants.RIGHT);
				label18.setBackground(new Color(208, 193, 251));
				label18.setOpaque(true);
				panel1.add(label18, CC.xy(5, 15));

				//---- comboBox7 ----
				comboBox7.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"Sekunde",
					"Minute",
					"Stunde",
					"Tag",
					"Woche",
					"Monat",
					"Jahr"
				}));
				panel1.add(comboBox7, CC.xy(7, 15));

				//---- label7 ----
				label7.setText("Krankheit");
				label7.setHorizontalAlignment(SwingConstants.RIGHT);
				label7.setOpaque(true);
				label7.setBackground(new Color(208, 193, 251));
				label7.setFont(label7.getFont().deriveFont(label7.getFont().getStyle() | Font.BOLD));
				panel1.add(label7, CC.xy(1, 17));

				//---- textField5 ----
				textField5.setEditable(false);
				panel1.add(textField5, CC.xy(3, 17));

				//---- label34 ----
				label34.setText("Ausscheidungsdauer");
				label34.setHorizontalAlignment(SwingConstants.RIGHT);
				label34.setBackground(new Color(208, 193, 251));
				label34.setOpaque(true);
				label34.setFont(label34.getFont().deriveFont(label34.getFont().getStyle() | Font.BOLD));
				panel1.add(label34, CC.xy(5, 17));

				//---- comboBox16 ----
				comboBox16.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"Sekunde",
					"Minute",
					"Stunde",
					"Tag",
					"Woche",
					"Monat",
					"Jahr"
				}));
				panel1.add(comboBox16, CC.xy(7, 17));

				//---- label29 ----
				label29.setText("Meldepflicht");
				label29.setHorizontalAlignment(SwingConstants.RIGHT);
				label29.setBackground(new Color(208, 193, 251));
				label29.setOpaque(true);
				label29.setFont(label29.getFont().deriveFont(label29.getFont().getStyle() | Font.BOLD));
				panel1.add(label29, CC.xy(1, 19));

				//---- comboBox15 ----
				comboBox15.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"ja",
					"nein"
				}));
				panel1.add(comboBox15, CC.xy(3, 19));

				//======== panel4 ========
				{
					panel4.setBorder(new TitledBorder("..."));
					panel4.setBackground(new Color(255, 204, 255));
					panel4.setLayout(new FormLayout(
						"150px, $lcgap, default:grow",
						"3*(default, $lgap), default"));

					//---- label35 ----
					label35.setText("Ansteckend");
					label35.setHorizontalAlignment(SwingConstants.RIGHT);
					label35.setBackground(new Color(208, 193, 251));
					label35.setOpaque(true);
					panel4.add(label35, CC.xy(1, 1));

					//---- comboBox17 ----
					comboBox17.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"ja",
						"nein"
					}));
					panel4.add(comboBox17, CC.xy(3, 1));

					//---- label36 ----
					label36.setText("Therapie");
					label36.setHorizontalAlignment(SwingConstants.RIGHT);
					label36.setBackground(new Color(208, 193, 251));
					label36.setOpaque(true);
					label36.setFont(label36.getFont().deriveFont(label36.getFont().getStyle() | Font.BOLD));
					panel4.add(label36, CC.xy(1, 3));

					//---- comboBox18 ----
					comboBox18.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"ja",
						"nein"
					}));
					panel4.add(comboBox18, CC.xy(3, 3));

					//---- label37 ----
					label37.setText("Antidot");
					label37.setHorizontalAlignment(SwingConstants.RIGHT);
					label37.setBackground(new Color(208, 193, 251));
					label37.setOpaque(true);
					label37.setFont(label37.getFont().deriveFont(label37.getFont().getStyle() | Font.BOLD));
					panel4.add(label37, CC.xy(1, 5));

					//---- comboBox19 ----
					comboBox19.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"ja",
						"nein"
					}));
					panel4.add(comboBox19, CC.xy(3, 5));

					//---- label38 ----
					label38.setText("Impfung");
					label38.setHorizontalAlignment(SwingConstants.RIGHT);
					label38.setBackground(new Color(208, 193, 251));
					label38.setOpaque(true);
					label38.setFont(label38.getFont().deriveFont(label38.getFont().getStyle() | Font.BOLD));
					panel4.add(label38, CC.xy(1, 7));

					//---- comboBox20 ----
					comboBox20.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"ja",
						"nein"
					}));
					panel4.add(comboBox20, CC.xy(3, 7));
				}
				panel1.add(panel4, CC.xywh(5, 21, 3, 11));

				//---- label8 ----
				label8.setText("Symptome");
				label8.setHorizontalAlignment(SwingConstants.RIGHT);
				label8.setOpaque(true);
				label8.setBackground(new Color(208, 193, 251));
				panel1.add(label8, CC.xywh(1, 21, 1, 5, CC.DEFAULT, CC.FILL));

				//======== scrollPane1 ========
				{

					//---- textArea1 ----
					textArea1.setEditable(false);
					textArea1.setRows(3);
					scrollPane1.setViewportView(textArea1);
				}
				panel1.add(scrollPane1, CC.xywh(3, 21, 1, 5));

				//======== panel3 ========
				{
					panel3.setBorder(new TitledBorder(null, "LD100", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Segoe UI", Font.BOLD, 12)));
					panel3.setBackground(new Color(255, 204, 255));
					panel3.setLayout(new FormLayout(
						"150px, $lcgap, default:grow",
						"3*(default:grow, $lgap), default:grow"));

					//---- label51 ----
					label51.setText("Organismus");
					label51.setHorizontalAlignment(SwingConstants.RIGHT);
					label51.setBackground(new Color(208, 193, 251));
					label51.setOpaque(true);
					panel3.add(label51, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- comboBox13 ----
					comboBox13.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"Human",
						"Kaninchen",
						"Maus",
						"Ratte",
						"Meerschweinchen",
						"Primaten",
						"sonst. S\u00e4ugetier"
					}));
					panel3.add(comboBox13, new CellConstraints(3, 1, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- label52 ----
					label52.setText("Aufnahmeroute");
					label52.setHorizontalAlignment(SwingConstants.RIGHT);
					label52.setBackground(new Color(208, 193, 251));
					label52.setOpaque(true);
					panel3.add(label52, CC.xy(1, 3));

					//---- comboBox14 ----
					comboBox14.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"inhalativ",
						"oral",
						"dermal",
						"Blut/Serum/K\u00f6rperfl\u00fcssigkeit",
						"h\u00e4matogen",
						"transplazental",
						"kutan",
						"venerisch",
						"transkutan",
						"intraperitoneal",
						"intraven\u00f6s",
						"subkutan",
						"intramuskul\u00e4r",
						"Injektion"
					}));
					panel3.add(comboBox14, new CellConstraints(3, 3, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- label49 ----
					label49.setText("Dosis");
					label49.setHorizontalAlignment(SwingConstants.RIGHT);
					label49.setBackground(new Color(208, 193, 251));
					label49.setOpaque(true);
					panel3.add(label49, new CellConstraints(1, 5, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- textField12 ----
					textField12.setEditable(false);
					panel3.add(textField12, new CellConstraints(3, 5, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- label50 ----
					label50.setText("Einheit");
					label50.setHorizontalAlignment(SwingConstants.RIGHT);
					label50.setBackground(new Color(208, 193, 251));
					label50.setOpaque(true);
					panel3.add(label50, new CellConstraints(1, 7, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));

					//---- comboBox12 ----
					comboBox12.setModel(new DefaultComboBoxModel(new String[] {
						" ",
						"Sporenzahl",
						"KBE pro g",
						"KBE pro ml",
						"PBE pro g",
						"PBE pro ml",
						"Milligramm",
						"Mikrogramm",
						"\u00b5g/kg/KG",
						"Anzahl"
					}));
					panel3.add(comboBox12, new CellConstraints(3, 7, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(3, 3, 3, 3)));
				}
				panel1.add(panel3, CC.xywh(9, 23, 3, 12));

				//---- label11 ----
				label11.setText("Krankheitsverlauf");
				label11.setHorizontalAlignment(SwingConstants.RIGHT);
				label11.setOpaque(true);
				label11.setBackground(new Color(208, 193, 251));
				label11.setFont(label11.getFont().deriveFont(label11.getFont().getStyle() | Font.BOLD));
				panel1.add(label11, CC.xy(1, 27));

				//---- comboBox5 ----
				comboBox5.setModel(new DefaultComboBoxModel(new String[] {
					" ",
					"akut",
					"chronisch",
					"perkaut",
					"subakut"
				}));
				panel1.add(comboBox5, CC.xy(3, 27));

				//---- label12 ----
				label12.setText("Risikogruppen");
				label12.setHorizontalAlignment(SwingConstants.RIGHT);
				label12.setOpaque(true);
				label12.setBackground(new Color(208, 193, 251));
				panel1.add(label12, CC.xywh(1, 29, 1, 5, CC.DEFAULT, CC.FILL));

				//======== scrollPane2 ========
				{

					//---- textArea2 ----
					textArea2.setEditable(false);
					textArea2.setRows(3);
					scrollPane2.setViewportView(textArea2);
				}
				panel1.add(scrollPane2, CC.xywh(3, 29, 1, 5));

				//---- label40 ----
				label40.setText("Spaetschaeden");
				label40.setHorizontalAlignment(SwingConstants.RIGHT);
				label40.setBackground(new Color(208, 193, 251));
				label40.setOpaque(true);
				label40.setFont(label40.getFont().deriveFont(label40.getFont().getStyle() | Font.BOLD));
				panel1.add(label40, CC.xy(1, 35, CC.DEFAULT, CC.FILL));

				//======== scrollPane5 ========
				{

					//---- textArea4 ----
					textArea4.setRows(6);
					textArea4.setLineWrap(true);
					scrollPane5.setViewportView(textArea4);
				}
				panel1.add(scrollPane5, CC.xy(3, 35));

				//---- label41 ----
				label41.setText("Komplikationen");
				label41.setHorizontalAlignment(SwingConstants.RIGHT);
				label41.setBackground(new Color(208, 193, 251));
				label41.setOpaque(true);
				label41.setFont(label41.getFont().deriveFont(label41.getFont().getStyle() | Font.BOLD));
				panel1.add(label41, CC.xy(5, 35, CC.DEFAULT, CC.FILL));

				//======== scrollPane6 ========
				{

					//---- textArea5 ----
					textArea5.setRows(6);
					textArea5.setLineWrap(true);
					scrollPane6.setViewportView(textArea5);
				}
				panel1.add(scrollPane6, CC.xy(7, 35));

				//---- label44 ----
				label44.setText("Kommentar");
				label44.setHorizontalAlignment(SwingConstants.RIGHT);
				label44.setOpaque(true);
				label44.setBackground(new Color(208, 193, 251));
				panel1.add(label44, CC.xy(9, 35, CC.DEFAULT, CC.FILL));

				//======== scrollPane4 ========
				{

					//---- textArea3 ----
					textArea3.setRows(6);
					textArea3.setMaximumSize(new Dimension(575, 2147483647));
					textArea3.setLineWrap(true);
					scrollPane4.setViewportView(textArea3);
				}
				panel1.add(scrollPane4, CC.xy(11, 35));

				//---- label43 ----
				label43.setText("Guetescore");
				label43.setHorizontalAlignment(SwingConstants.RIGHT);
				label43.setBackground(new Color(208, 193, 251));
				label43.setOpaque(true);
				panel1.add(label43, CC.xy(5, 37));
				panel1.add(textField18, CC.xy(7, 37));

				//---- checkBox1 ----
				checkBox1.setText("Geprueft");
				checkBox1.setEnabled(false);
				checkBox1.setBackground(new Color(208, 193, 251));
				panel1.add(checkBox1, CC.xy(11, 37));
			}
			scrollPane3.setViewportView(panel1);
		}
		add(scrollPane3, CC.xy(1, 1));

		//---- label42 ----
		label42.setText("3 von 25");
		label42.setHorizontalAlignment(SwingConstants.CENTER);
		add(label42, CC.xy(1, 3));

		//---- scrollBar1 ----
		scrollBar1.setOrientation(Adjustable.HORIZONTAL);
		scrollBar1.setVisibleAmount(0);
		scrollBar1.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				scrollBar1AdjustmentValueChanged(e);
			}
		});
		add(scrollBar1, CC.xy(1, 5));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JScrollPane scrollPane3;
	private JPanel panel1;
	private JLabel label1;
	private JTextField textField1;
	private JLabel label13;
	private JTextField textField6;
	private JLabel label30;
	private JTextField textField13;
	private JLabel label2;
	private JTextField textField2;
	private JLabel label14;
	private JTextField textField7;
	private JLabel label31;
	private JTextField textField14;
	private JLabel label3;
	private JTextField textField3;
	private JLabel label19;
	private JTextField textField10;
	private JLabel label32;
	private JTextField textField15;
	private JLabel label4;
	private JTextField textField4;
	private JLabel label20;
	private JComboBox comboBox8;
	private JLabel label33;
	private JComboBox comboBox22;
	private JLabel label5;
	private JComboBox comboBox1;
	private JLabel label15;
	private JTextField textField8;
	private JLabel label39;
	private JComboBox comboBox21;
	private JLabel label6;
	private JComboBox comboBox2;
	private JLabel label16;
	private JComboBox comboBox6;
	private JPanel panel2;
	private JLabel label47;
	private JComboBox comboBox10;
	private JLabel label48;
	private JComboBox comboBox11;
	private JLabel label45;
	private JTextField textField11;
	private JLabel label46;
	private JComboBox comboBox9;
	private JLabel label9;
	private JComboBox comboBox3;
	private JLabel label17;
	private JTextField textField9;
	private JLabel label10;
	private JComboBox comboBox4;
	private JLabel label18;
	private JComboBox comboBox7;
	private JLabel label7;
	private JTextField textField5;
	private JLabel label34;
	private JComboBox comboBox16;
	private JLabel label29;
	private JComboBox comboBox15;
	private JPanel panel4;
	private JLabel label35;
	private JComboBox comboBox17;
	private JLabel label36;
	private JComboBox comboBox18;
	private JLabel label37;
	private JComboBox comboBox19;
	private JLabel label38;
	private JComboBox comboBox20;
	private JLabel label8;
	private JScrollPane scrollPane1;
	private JTextArea textArea1;
	private JPanel panel3;
	private JLabel label51;
	private JComboBox comboBox13;
	private JLabel label52;
	private JComboBox comboBox14;
	private JLabel label49;
	private JTextField textField12;
	private JLabel label50;
	private JComboBox comboBox12;
	private JLabel label11;
	private JComboBox comboBox5;
	private JLabel label12;
	private JScrollPane scrollPane2;
	private JTextArea textArea2;
	private JLabel label40;
	private JScrollPane scrollPane5;
	private JTextArea textArea4;
	private JLabel label41;
	private JScrollPane scrollPane6;
	private JTextArea textArea5;
	private JLabel label44;
	private JScrollPane scrollPane4;
	private JTextArea textArea3;
	private JLabel label43;
	private JTextField textField18;
	private JCheckBox checkBox1;
	private JLabel label42;
	private JScrollBar scrollBar1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
