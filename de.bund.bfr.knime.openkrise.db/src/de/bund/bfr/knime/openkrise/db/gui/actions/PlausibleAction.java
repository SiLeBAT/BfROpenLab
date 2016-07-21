/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.openkrise.db.gui.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.PlausibleDialog4Krise;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyIDFilter;

/**
 * @author Armin
 *
 */
public class PlausibleAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5441488921384865553L;
	private MyDBTable myDB;
	private boolean useLevenshtein = false;

	public PlausibleAction(final String name, final Icon icon, final String toolTip, final JProgressBar progressBar1, final MyDBTable myDB) {
	  	this.myDB = myDB;
	    putValue(Action.NAME, name);
	    putValue(Action.SHORT_DESCRIPTION, toolTip);
	    putValue(Action.SMALL_ICON, icon);
	    this.setEnabled(false);
	}    

	@Override
	public void actionPerformed(final ActionEvent e) {
	  	final PlausibleDialog4Krise pd4 = new PlausibleDialog4Krise(DBKernel.mainFrame); 
	  	pd4.setVisible(true);
	  	if (pd4.okPressed) {
		  	Runnable runnable = new Runnable() {
		        @Override
				public void run() {
		  		    try {		  
	        			go4ISM(pd4);
						IWorkbenchWindow eclipseWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();							
						if (eclipseWindow != null) {						
							MessageDialog.openInformation(eclipseWindow.getShell(), "Similarity Search!", "Finished!");
						} else {
							JOptionPane pane = new JOptionPane("Finished!", JOptionPane.INFORMATION_MESSAGE);
							JDialog dialog = pane.createDialog("Similarity Search!");
							dialog.setAlwaysOnTop(true);
							dialog.setVisible(true);
						}
				    }
				    catch (Exception e) {MyLogger.handleException(e);}
		      }
		    };
		    
		    Thread thread = new Thread(runnable);
		    thread.start();
	  	}
	}
	private void go4ISM(PlausibleDialog4Krise pd4) throws SQLException {
		useLevenshtein = false;
		DBKernel.sendRequest("DROP FUNCTION IF EXISTS LD", false, true);
		if (DBKernel.sendRequest(
	    		"CREATE FUNCTION LD(x VARCHAR(255), y VARCHAR(255))\n" +
	    		(useLevenshtein ? "RETURNS INT\n" : "RETURNS DOUBLE\n") + 
	    		"NO SQL\n" +
	    		"LANGUAGE JAVA\n" +
	    		"PARAMETER STYLE JAVA\n" +
	    		(useLevenshtein ? "EXTERNAL NAME 'CLASSPATH:de.bund.bfr.knime.openkrise.db.Levenshtein.LD'" :
	    		"EXTERNAL NAME 'CLASSPATH:de.bund.bfr.knime.openkrise.db.StringSimilarity.diceCoefficientOptimized'") // diceCoefficientOptimized compareStringsStrikeAMatch
	    		, false, true) &&
	    		DBKernel.sendRequest(
	    				"GRANT EXECUTE ON FUNCTION LD TO " +
	    		DBKernel.delimitL("WRITE_ACCESS") + "," + DBKernel.delimitL("SUPER_WRITE_ACCESS"),
	    		false, true)) {
			DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			myDB.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			myDB.getMyDBPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			LinkedHashMap<String[], LinkedHashSet<String[]>> vals1 =
					pd4.cs.isSelected() ?
							checkTable4ISM("Station", new String[]{"Name","PLZ","Strasse","Hausnummer","Ort"},
								new int[]{(Integer)pd4.sn.getValue(),(Integer)pd4.sz.getValue(),(Integer)pd4.ss.getValue(),(Integer)pd4.snum.getValue(),(Integer)pd4.sc.getValue()}, null, null, null) 		//"Station", "Kontaktadresse", new String[]{"FallErfuellt","AnzahlFaelle"});
							:
							null;

			LinkedHashMap<String[], LinkedHashSet<String[]>> vals2 =
					pd4.cp.isSelected() ?
							checkTable4ISM("Produktkatalog", new String[]{"Station","Bezeichnung"},
								new int[]{(Integer)pd4.ps.getValue(),(Integer)pd4.pd.getValue()}, null, null, null)
							:
							null;

			LinkedHashMap<String[], LinkedHashSet<String[]>> vals3 =
					pd4.cl.isSelected() ?
							checkTable4ISM("Chargen", new String[]{"Artikel","ChargenNr"},
								new int[]{(Integer)pd4.la.getValue(),(Integer)pd4.ll.getValue()}, null, null, null)
							:
							null;

			LinkedHashMap<String[], LinkedHashSet<String[]>> vals4 =
					pd4.cd.isSelected() ?
							checkTable4ISM("Lieferungen", new String[]{"Charge","dd_day","dd_month","dd_year","Empfänger"},
								new int[]{(Integer)pd4.dl.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dr.getValue()}, null, null, null)
							:
							null;
							/*
							if (pd4.selS.isSelected() && DBKernel.mainFrame.getTopTable().getActualTable().getTablename().equals("Station")) {
								Integer stationId = DBKernel.mainFrame.getTopTable().getSelectedID();
								System.err.println(stationId);
								checkTables4Id(stationId);
							}
*/
			DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			myDB.getMyDBPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			myDB.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
			int v1 = vals1 == null ? 0 : vals1.size();
			int v2 = vals2 == null ? 0 : vals2.size();
			int v3 = vals3 == null ? 0 : vals3.size();
			int v4 = vals4 == null ? 0 : vals4.size();
			int total = v1 + v2 + v3 + v4;
			if (vals1 == null || showAndFilterVals("Station", vals1, 0, 0, total)) {
				if (vals2 == null || showAndFilterVals("Produktkatalog", vals2, 0, v1, total)) {
					if (vals3 == null || showAndFilterVals("Chargen", vals3, 0, v1 + v2, total)) {
						if (vals4 != null) showAndFilterVals("Lieferungen", vals4, 0, v1 + v2 + v3, total);
					}
				}
			}			
		}
		DBKernel.sendRequest("DROP FUNCTION IF EXISTS LD", false, true);
	}
	@SuppressWarnings("unused")
	private boolean checkTables4Id(Integer stationId)  {
		String sql = "SELECT " + DBKernel.delimitL("Artikelnummer") + "," + DBKernel.delimitL("Bezeichnung") + "," + DBKernel.delimitL("ChargenNr") + "," +
				DBKernel.delimitL("MHD_day") + "," + DBKernel.delimitL("MHD_month") + "," + DBKernel.delimitL("MHD_year") + "," +
				DBKernel.delimitL("pd_day") + "," + DBKernel.delimitL("pd_month") + "," + DBKernel.delimitL("pd_year") + "," +
				DBKernel.delimitL("dd_day") + "," + DBKernel.delimitL("dd_month") + "," + DBKernel.delimitL("dd_year") + "," +
				DBKernel.delimitL("Unitmenge") + "," + DBKernel.delimitL("UnitEinheit") + "," + DBKernel.delimitL("numPU") + "," + DBKernel.delimitL("typePU") + "," +
				DBKernel.delimitL("Station") +
				" FROM " + DBKernel.delimitL("Lieferungen") +
    			" LEFT JOIN " + DBKernel.delimitL("Chargen") +
    			" ON " + DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("Charge") + "=" + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID") +
    			" LEFT JOIN " + DBKernel.delimitL("Produktkatalog") +
    			" ON " + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("Artikel") + "=" + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("ID") +
    			" WHERE " + DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("Empfänger") + "=" + stationId;
		try {
			System.err.println("Deliveries - Inbound");
			System.err.println("Recipient\tArtikelnummer\tBezeichnung\tChargenNr\tMHD_day\tMHD_month\tMHD_year\tpd_day\tpd_month\tpd_year\tdd_day\tdd_month\tdd_year\tUnitmenge\tUnitEinheit\tnumPU\ttypePU");
			ResultSet rs = DBKernel.getResultSet(sql, false);
			if (rs != null && rs.first()) {
				do  {
					System.err.println(rs.getInt("Station") + "\t" + rs.getObject("Artikelnummer") + "\t" + rs.getObject("Bezeichnung") + "\t" + rs.getObject("ChargenNr") + "\t" +
							rs.getObject("MHD_day") + "\t" + rs.getObject("MHD_month") + "\t" + rs.getObject("MHD_year") + "\t" +
							rs.getObject("pd_day") + "\t" + rs.getObject("pd_month") + "\t" + rs.getObject("pd_year") + "\t" +
							rs.getObject("dd_day") + "\t" + rs.getObject("dd_month") + "\t" + rs.getObject("dd_year") + "\t" +
							rs.getObject("Unitmenge") + "\t" + rs.getObject("UnitEinheit") + "\t" + rs.getObject("numPU") + "\t" + rs.getObject("typePU"));
				} while (rs.next());
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		sql = "SELECT " + DBKernel.delimitL("Artikelnummer") + "," + DBKernel.delimitL("Bezeichnung") + "," + DBKernel.delimitL("ChargenNr") + "," +
				DBKernel.delimitL("MHD_day") + "," + DBKernel.delimitL("MHD_month") + "," + DBKernel.delimitL("MHD_year") + "," +
				DBKernel.delimitL("pd_day") + "," + DBKernel.delimitL("pd_month") + "," + DBKernel.delimitL("pd_year") + "," +
				DBKernel.delimitL("dd_day") + "," + DBKernel.delimitL("dd_month") + "," + DBKernel.delimitL("dd_year") + "," +
				DBKernel.delimitL("Unitmenge") + "," + DBKernel.delimitL("UnitEinheit") + "," + DBKernel.delimitL("numPU") + "," + DBKernel.delimitL("typePU") + "," +
				DBKernel.delimitL("Empfänger") +
				" FROM " + DBKernel.delimitL("Produktkatalog") +
    			" LEFT JOIN " + DBKernel.delimitL("Chargen") +
    			" ON " + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("Artikel") + "=" + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("ID") +
    			" LEFT JOIN " + DBKernel.delimitL("Lieferungen") +
    			" ON " + DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("Charge") + "=" + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID") +
    			" WHERE " + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("Station") + "=" + stationId;
		try {
			System.err.println("Deliveries - Outbound\n" + sql);
			System.err.println("Artikelnummer\tBezeichnung\tChargenNr\tMHD_day\tMHD_month\tMHD_year\tpd_day\tpd_month\tpd_year\tdd_day\tdd_month\tdd_year\tUnitmenge\tUnitEinheit\tnumPU\ttypePU\tRecipient");
			ResultSet rs = DBKernel.getResultSet(sql, false);
			if (rs != null && rs.first()) {
				do  {
					System.err.println(rs.getObject("Artikelnummer") + "\t" + rs.getObject("Bezeichnung") + "\t" + rs.getObject("ChargenNr") + "\t" +
							rs.getObject("MHD_day") + "\t" + rs.getObject("MHD_month") + "\t" + rs.getObject("MHD_year") + "\t" +
							rs.getObject("pd_day") + "\t" + rs.getObject("pd_month") + "\t" + rs.getObject("pd_year") + "\t" +
							rs.getObject("dd_day") + "\t" + rs.getObject("dd_month") + "\t" + rs.getObject("dd_year") + "\t" +
							rs.getObject("Unitmenge") + "\t" + rs.getObject("UnitEinheit") + "\t" + rs.getObject("numPU") + "\t" + rs.getObject("typePU") + "\t" +
							rs.getInt("Empfänger"));
				} while (rs.next());
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		/*
		LinkedHashSet<Integer> filterIDs = new LinkedHashSet<Integer>();
		MyTable theTable = DBKernel.myDBi.getTable("Produktkatalog");
		MyIDFilter mf = new MyIDFilter(filterIDs);
		Object val = DBKernel.myList.openNewWindow(
				theTable,
				null,
				(Object) (""),
				null,
				1,
				1,
				null,
				true, mf);
		if (val == null) {
			return false;
		}
		*/
		return true;
	}
	private boolean showAndFilterVals(String tablename, LinkedHashMap<String[], LinkedHashSet<String[]>> vals, int idColumn,
			int lfd, int total) {
		int i=0;
		for (String[] p : vals.keySet()) {
			i++;
			try {
				if (p[idColumn] == null) {
					System.err.println("p - " + p[1]);
				}
				else {					
					Integer pID = Integer.parseInt(p[idColumn]);
					LinkedHashSet<Integer> filterIDs = new LinkedHashSet<>();
					filterIDs.add(pID);
					LinkedHashSet<String[]> lhs = vals.get(p);
					for (String[] sa : lhs) {
						if (sa[idColumn] == null) {
							System.err.println("sa - " + sa[1]);
						}
						else {					
							Integer cID = Integer.parseInt(sa[idColumn]);
							filterIDs.add(cID);
						}
					}
					if (filterIDs.size() > 1) {
						MyTable theTable = DBKernel.myDBi.getTable(tablename);
						MyIDFilter mf = new MyIDFilter(filterIDs);
						Object val = DBKernel.mainFrame.openNewWindow(
								theTable,
								null,
								(Object) ("[" + (lfd + i) + "/" + total + "] - " + tablename),
								null,
								1,
								1,
								null,
								true, mf);
						if (val == null) {
							return false;
						}
					}
				}
			}
			catch (Exception e) {e.printStackTrace();}
		}		
		return true;
	}
	private LinkedHashMap<String[], LinkedHashSet<String[]>> checkTable4ISM(String tablename, String[] fieldnames, int[] maxScores,
			String otherTable, String otherTableField, String[] otherTableDesires) throws SQLException {
		LinkedHashMap<String[], LinkedHashSet<String[]>> ldResult = new LinkedHashMap<>();
		if (maxScores.length != fieldnames.length) {
			System.err.println("fieldnames and simScores with different size...");
			return null;
		}
		/*
		System.err.print(tablename);
		for (int i=0;i<fieldnames.length;i++) System.err.print("\t" + fieldnames[i]);
		for (int i=0;i<maxScores.length;i++) System.err.print("\t" + maxScores[i]);
		System.err.println();
		*/
		String sql = "SELECT " + DBKernel.delimitL("ID");
		for (int i=0;i<fieldnames.length;i++) sql += "," + DBKernel.delimitL(fieldnames[i]);
		sql += " FROM " + DBKernel.delimitL(tablename);
        ResultSet rs = DBKernel.getResultSet(sql, false);
        if (rs != null && rs.first()) {
        	do {
        		
        		String[] resRowFirst = new String[fieldnames.length + 1 + (otherTableDesires == null ? 0 : otherTableDesires.length + 1)];

        		// Firstly - fieldnames
        		int id = rs.getInt("ID");
        		resRowFirst[0] = id+"";
        		String result = ""+id;
        		Object[] fieldVals = new Object[fieldnames.length];
        		boolean go4Row = false;
        		for (int i=0;i<fieldnames.length;i++) {
        			fieldVals[i] = rs.getObject(fieldnames[i]);
        			if (fieldVals[i] != null) fieldVals[i] = fieldVals[i].toString().replace("'", "''");
        			result += "\t" + fieldVals[i];
        			resRowFirst[i+1] = fieldVals[i]+"";
        			if (fieldVals[i] != null && !fieldVals[i].toString().trim().isEmpty()) go4Row = true;
        		}
        		if (!go4Row) continue;
        		
        		// Firstly - otherTableDesires
        		if (otherTable != null) {
        			result += " (" + otherTable + ": ";
        			sql = "SELECT " + DBKernel.delimitL("ID");
            		for (int i=0;i<otherTableDesires.length;i++) sql += "," + DBKernel.delimitL(otherTableDesires[i]);
            		sql += " FROM " + DBKernel.delimitL(otherTable) + " WHERE " + DBKernel.delimitL(otherTableField) + "=" + id;
        			ResultSet rs3 = DBKernel.getResultSet(sql, false);
        			if (rs3 != null && rs3.first()) {
                    	do {
                    		result += rs3.getInt("ID");
                    		if (resRowFirst[fieldnames.length+1] == null || resRowFirst[fieldnames.length+1].isEmpty()) resRowFirst[fieldnames.length+1] = rs3.getInt("ID")+"";
                    		else resRowFirst[fieldnames.length+1] += "," + rs3.getInt("ID")+"";
                    		for (int i=0;i<otherTableDesires.length;i++) {
                    			result += "\t" + rs3.getString(otherTableDesires[i]);
                    			if (resRowFirst[fieldnames.length+2+i] == null || resRowFirst[fieldnames.length+2+i].isEmpty()) resRowFirst[fieldnames.length+2+i] = rs3.getString(otherTableDesires[i]);
                    			else resRowFirst[fieldnames.length+2+i] += "," + rs3.getString(otherTableDesires[i]);
                    		}
                    	} while(rs3.next());
        			}
        			result += ")";
        		}
    			
        		result += "\n";
        		
                sql = "SELECT " + DBKernel.delimitL("ID");
        		for (int i=0;i<fieldnames.length;i++) sql += "," + DBKernel.delimitL(fieldnames[i]);
        		for (int i=0;i<fieldnames.length;i++) {
        			if (useLevenshtein) {
            			sql += (fieldVals[i] == null) ? ",0 AS SCORE" + i : "," + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255))))" + " AS SCORE" + i;
        			}
        			else {
        				if (maxScores[i] > 0) {
                			sql += (fieldVals[i] == null) ? ",1 AS SCORE" + i : "," + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255))))" + " AS SCORE" + i;        				        					
        				}
        			}
        		}
                sql += " FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + " > " + id;
        		for (int i=0;i<fieldnames.length;i++) {
        			if (useLevenshtein) {
            			sql += (fieldVals[i] == null) ? " AND TRUE" : " AND " + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255)))) <= " + maxScores[i];
        			}
        			else {
        				if (maxScores[i] > 0) {
                			sql += (fieldVals[i] == null) ? " AND TRUE" : " AND " + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255)))) >= " + (maxScores[i] / 100.0);
                			//sql += (gentle && fieldVals[i] == null) ? " AND TRUE" : " AND SCORE" + i + " >= 0.7";
        				}
        			}
        		}
                //sql += " ORDER BY SCORE ASC";
                ResultSet rs2 = DBKernel.getResultSet(sql, false);
                if (rs2 != null && rs2.first()) {
                	LinkedHashSet<String[]> resSetOther = new LinkedHashSet<>(); 
                	do {
                		
                		String[] resRowOther = new String[fieldnames.length + 1 + (otherTableDesires == null ? 0 : otherTableDesires.length + 1)];

                		// Match - fieldnames
                		result += rs2.getInt("ID");
                		resRowOther[0] = rs2.getInt("ID")+"";
                		for (int i=0;i<fieldnames.length;i++) {
                			result += "\t" + rs2.getString(fieldnames[i]);
                			resRowOther[i+1] = rs2.getString(fieldnames[i]);
                		}
                		for (int i=0;i<fieldnames.length;i++) {
                			if (useLevenshtein || maxScores[i] > 0) result += "\t" + rs2.getDouble("SCORE" + i);
                		}
                		
                		// Match - otherTableDesires
                		if (otherTable != null) {
                			result += " (" + otherTable + ": ";
                			sql = "SELECT " + DBKernel.delimitL("ID");
                    		for (int i=0;i<otherTableDesires.length;i++) sql += "," + DBKernel.delimitL(otherTableDesires[i]);
                    		sql += " FROM " + DBKernel.delimitL(otherTable) + " WHERE " + DBKernel.delimitL(otherTableField) + "=" + rs2.getInt("ID");
                    		ResultSet rs3 = DBKernel.getResultSet(sql, false);
                			if (rs3 != null && rs3.first()) {
                            	do {
                            		result += rs3.getInt("ID");
                            		if (resRowOther[fieldnames.length+1] == null || resRowOther[fieldnames.length+1].isEmpty()) resRowOther[fieldnames.length+1] = rs3.getInt("ID")+"";
                            		else resRowOther[fieldnames.length+1] += "," + rs3.getInt("ID")+"";
                            		for (int i=0;i<otherTableDesires.length;i++) {
                            			result += "\t" + rs3.getString(otherTableDesires[i]);
                            			if (resRowOther[fieldnames.length+2+i] == null || resRowOther[fieldnames.length+2+i].isEmpty()) resRowOther[fieldnames.length+2+i] = rs3.getString(otherTableDesires[i]);
                            			else resRowOther[fieldnames.length+2+i] += "," + rs3.getString(otherTableDesires[i]);
                            		}
                            	} while(rs3.next());
                			}
                			result += ")";
                		}
                		
                		resSetOther.add(resRowOther);
                		
                		result += "\n";
                		
                	} while(rs2.next());
                    ldResult.put(resRowFirst, resSetOther);
                    if (ldResult.size() == 0) System.err.println(result);
                }
        	} while(rs.next());
        }		
		return ldResult;
	}

}
