/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren;

import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.util.LinkedHashMap;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.MyTrigger;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import quick.dbtable.CellComponent;

/**
 * @author Armin
 *
 */
public class MyMNRenderer extends JTextArea implements CellComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MyTable myT = null;
	private int selectedColumn;
	private boolean isINTmn = false;
	private LinkedHashMap<Object, String> theValues = new LinkedHashMap<>();
	private LinkedHashMap<Object, Long> lastUpdate = new LinkedHashMap<>();
	
	public MyMNRenderer(MyDBTable myDB, int selectedColumn) {
		this.myT = myDB.getActualTable();
    	String[] mnTable = myT.getMNTable();
    	isINTmn = mnTable != null && selectedColumn < mnTable.length && mnTable[selectedColumn] != null && mnTable[selectedColumn].equals("INT");
		this.selectedColumn = selectedColumn;
		this.setLineWrap(true);
		this.setWrapStyleWord(true);
	}
	public void setValue(Object value) {
		String sql = "";
		String result = "";
		if (value == null) {
			this.setText("");
			return;
		}		
		else if (theValues.containsKey(value) && lastUpdate.containsKey(value) && lastUpdate.get(value) > MyTrigger.triggerFired) {
			this.setText(theValues.get(value));
			return;
		}
		else if (value instanceof Integer) {
			MyTable myFT = myT.getForeignFields() == null ? null : myT.getForeignFields()[selectedColumn];
    		if (myFT != null) {
    			sql = myT.getMNSql(selectedColumn);
    			if (!sql.endsWith("=")) {
    				System.err.println("!= = " + sql);
    			}
    			else sql += value;
    			if (isINTmn) sql += " ORDER BY " + DBKernel.delimitL("ID") + " ASC";
			}
			else {
				result = value.toString(); 			
			}
		}
		
		if (sql.length() > 0) {
			try {
				ResultSet rs = DBKernel.getResultSet(sql, false);
				//System.err.println(rs + "\t" + sql);
				if (rs != null && rs.first()) {
					do {
						int numCols = rs.getMetaData().getColumnCount();
						if (numCols == 1) {
							if (!result.isEmpty()) result += " ; ";
							result += rs.getString(1);
						}
						else { //numCols == 2
							if (!result.isEmpty()) result += "\n";
							if (rs.getObject(2) != null) result += rs.getString(2);
						}
						/*
						if (isINTmn) {
							String res = "";
							int numCols = rs.getMetaData().getColumnCount();
							for (int i=1;i<=numCols;i++) {
								if (rs.getString(i) != null) res += " ; " + rs.getString(i);
							}
							if (res.length() > 0) res = res.substring(3);
							if (numCols == 1) result += res + "; "; // es gibt wohl nur die ID
							else result += res + "\n";
						}
						else {
							int numCols = rs.getMetaData().getColumnCount(); 
							if (numCols == 1) {
								result += rs.getString(1) + "\n";
							}
							else {
								if (!result.isEmpty() && result.endsWith("\t")) result = result.substring(0, result.length() - 1) + "\n";
								for (int i=2;i<=numCols;i++) {
									if (rs.getObject(i) != null) {
										if (rs.getMetaData().getColumnType(i) == Types.DOUBLE) result += getDblOrString(rs.getObject(i));
										else result += rs.getString(i);
										result += "\t";
									}
								}
							}
						}
						*/
					} while (rs.next());					
				}
			}
			catch (Exception e) {
				MyLogger.handleException(e);
			} 					
		}
		this.setText(result);
		if (theValues.containsKey(value)) theValues.remove(value);
		theValues.put(value, result);
		if (lastUpdate.containsKey(value)) lastUpdate.remove(value);
		lastUpdate.put(value, System.currentTimeMillis());
	}

	public void addActionListener(ActionListener arg0) {
	}

	public JComponent getComponent() {
		return this;
	}

	public Object getValue() {
		return null;
	}
}
