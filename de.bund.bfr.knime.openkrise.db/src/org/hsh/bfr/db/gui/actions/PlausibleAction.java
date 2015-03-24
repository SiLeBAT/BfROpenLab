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
package org.hsh.bfr.db.gui.actions;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JProgressBar;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.MyTable;
import org.hsh.bfr.db.PlausibilityChecker;
import org.hsh.bfr.db.gui.InfoBox;
import org.hsh.bfr.db.gui.PlausibleDialog;
import org.hsh.bfr.db.gui.PlausibleDialog4Krise;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.gui.dbtable.editoren.MyIDFilter;

/**
 * @author Armin
 *
 */
public class PlausibleAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5441488921384865553L;
	private JProgressBar progress;
	private MyDBTable myDB;

	public PlausibleAction(final String name, final Icon icon, final String toolTip, final JProgressBar progressBar1, final MyDBTable myDB) {
	  	this.progress = progressBar1;
	  	this.myDB = myDB;
	    putValue(Action.NAME, name);
	    putValue(Action.SHORT_DESCRIPTION, toolTip);
	    putValue(Action.SMALL_ICON, icon);
	    this.setEnabled(false);
	}    

	@Override
	public void actionPerformed(final ActionEvent e) {
	  	final PlausibleDialog pd = new PlausibleDialog(DBKernel.mainFrame); 
	  	final PlausibleDialog4Krise pd4 = new PlausibleDialog4Krise(DBKernel.mainFrame); 
	  	if (DBKernel.isKrise) pd4.setVisible(true);
	  	else pd.setVisible(true);
	  	final Vector<int[]> ids = parseIDs(pd.textField1.getText());
	  	if (DBKernel.isKrise && pd4.okPressed || !DBKernel.isKrise && pd.okPressed && (!pd.radioButton3.isSelected() || ids != null)) {
		  	Runnable runnable = new Runnable() {
		        @Override
				public void run() {
		  		    try {		  
		        		if (DBKernel.isKrise) {
		        			go4ISM(pd4);
		        		}
		        		else {
			        		LinkedHashMap<String, MyTable> myTables = DBKernel.myDBi.getAllTables();
			        		if (progress != null) {
			        			progress.setVisible(true);
			        			progress.setStringPainted(true);
			        			progress.setString("Plausibilitätstests laufen...");
			        			progress.setMinimum(0);
			        			progress.setMaximum(myTables.size());
			        			progress.setValue(0);
			        		}
			        		int lfd = 0;
			        	  	boolean showOnlyDataFromCurrentUser = pd.checkBox1.isSelected();
			        		//String result = "";
		        			String selectedTN = myDB.getActualTable().getTablename();
			        		Vector<String> result = new Vector<>();
			        		for(String key : myTables.keySet()) {
			        			MyTable myT = myTables.get(key);
			        			String tn = myT.getTablename();
			        	  		if (pd.radioButton1.isSelected()) { // Alle Datensätze
			        	  			go4Table(tn, result, -1, -1, myT, showOnlyDataFromCurrentUser);
			        	  		}
			        	  		else if (pd.radioButton2.isSelected()) { // Nur sichtbare Tabelle
			        	  			if (tn.equals(selectedTN)) {
			        	  				go4Table(tn, result, -1, -1, myT, showOnlyDataFromCurrentUser);
			        	  			}
			        	  		}
			        	  		else if (pd.radioButton3.isSelected()) { // nur IDs der sichtbaren Tabelle
			        	  			if (tn.equals(selectedTN)) {
			        	  				go4Table(tn, result, ids.get(0)[0], ids.get(0)[1], myT, showOnlyDataFromCurrentUser);
			        	  			}
			        	  		}
			        	  		else if (pd.radioButton4.isSelected()) { // nur selektierte Zeile der sichtbaren Tabelle
				        			int selID = myDB.getSelectedID();
			        	  			if (tn.equals(selectedTN)) {
			        	  				go4Table(tn, result, selID, selID, myT, showOnlyDataFromCurrentUser);
			        	  			}
			        	  		}
			        	  		/*
			        	  		if (analysedIDs != null && tn.equals("Versuchsbedingungen")) {// erstmal nur für Messwerte bzw. Versuchsbedingungen
	    	  						go4Table("Messwerte", result, -1, -1, DBKernel.myDBi.getTable("Messwerte"),
	    	  								null, showOnlyDataFromCurrentUser); // analysedIDs oder WHERE Versuchsbedingungen=5 oder so ähnlich vielleicht???
			        	  		}
			        	  		*/
			        	  		progress.setValue(++lfd);		
			        		}	        		
			        		
			    			if (progress != null) {
			    				progress.setVisible(false);
			    			}
			    			String log = "Alles tutti!";
			        		if (result.size() > 0) {
			        			Collections.sort(result);
			        			log = vectorToString(result, "\n");
			        		}
			        		InfoBox ib = new InfoBox(log, true, new Dimension(800, 500), null, false);
			        		ib.setVisible(true);   
		        		}
				    }
				    catch (Exception e) {MyLogger.handleException(e);}
		      }
		    };
		    
		    Thread thread = new Thread(runnable);
		    thread.start();
	  	}
	  	else {
	  		doSpecialThings();
	  	}
	}
	private void go4ISM(PlausibleDialog4Krise pd4) throws SQLException {
		DBKernel.sendRequest("DROP FUNCTION IF EXISTS LD", false, true);
		if (DBKernel.sendRequest(
	    		"CREATE FUNCTION LD(x VARCHAR(255), y VARCHAR(255))\n" +
	    		"RETURNS INT\n" + 
	    		"NO SQL\n" +
	    		"LANGUAGE JAVA\n" +
	    		"PARAMETER STYLE JAVA\n" +
	    		"EXTERNAL NAME 'CLASSPATH:org.hsh.bfr.db.Levenshtein.LD'"
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
							checkTable4ISM("Station", new String[]{"Name","PLZ","Strasse","Hausnummer","Ort","VATnumber"},
								new int[]{(Integer)pd4.sn.getValue(),(Integer)pd4.sz.getValue(),(Integer)pd4.ss.getValue(),(Integer)pd4.snum.getValue(),(Integer)pd4.sc.getValue(),(Integer)pd4.sv.getValue()}, null, null, null, pd4.gentle.isSelected()) 		//"Station", "Kontaktadresse", new String[]{"FallErfuellt","AnzahlFaelle"});
							:
							null;

			LinkedHashMap<String[], LinkedHashSet<String[]>> vals2 =
					pd4.cp.isSelected() ?
							checkTable4ISM("Produktkatalog", new String[]{"Station","Bezeichnung","Artikelnummer"},
								new int[]{(Integer)pd4.ps.getValue(),(Integer)pd4.pd.getValue(),(Integer)pd4.pi.getValue()}, "Chargen", "Artikel", new String[]{"pd_day","pd_month","pd_year"}, pd4.gentle.isSelected())
							:
							null;

			LinkedHashMap<String[], LinkedHashSet<String[]>> vals3 =
					pd4.cl.isSelected() ?
							checkTable4ISM("Chargen", new String[]{"Artikel","ChargenNr","MHD_day","MHD_month","MHD_year","pd_day","pd_month","pd_year"},
								new int[]{(Integer)pd4.la.getValue(),(Integer)pd4.ll.getValue(),(Integer)pd4.lb.getValue(),(Integer)pd4.lb.getValue(),(Integer)pd4.lb.getValue(),(Integer)pd4.ld.getValue(),(Integer)pd4.ld.getValue(),(Integer)pd4.ld.getValue()}, null, null, null, pd4.gentle.isSelected())
							:
							null;

			LinkedHashMap<String[], LinkedHashSet<String[]>> vals4 =
					pd4.cd.isSelected() ?
							checkTable4ISM("Lieferungen", new String[]{"Charge","dd_day","dd_month","dd_year","Empfänger"},
								new int[]{(Integer)pd4.dl.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dr.getValue()}, null, null, null, pd4.gentle.isSelected())
							:
							null;
							
							if (pd4.selS.isSelected() && DBKernel.mainFrame.getTopTable().getActualTable().getTablename().equals("Station")) {
								Integer stationId = DBKernel.mainFrame.getTopTable().getSelectedID();
								System.err.println(stationId);
								checkTables4Id(stationId);
							}

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
		Integer lastShownID = -1;
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
					if (!filterIDs.contains(lastShownID)) {
						lastShownID = (Integer) filterIDs.toArray()[filterIDs.size() - 1];
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
			String otherTable, String otherTableField, String[] otherTableDesires, boolean gentle) throws SQLException {
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
        		for (int i=0;i<fieldnames.length;i++) {
        			fieldVals[i] = rs.getObject(fieldnames[i]);
        			if (fieldVals[i] != null) fieldVals[i] = fieldVals[i].toString().replace("'", "''");
        			result += "\t" + fieldVals[i];
        			resRowFirst[i+1] = fieldVals[i]+"";
        		}
        		
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
        		for (int i=0;i<fieldnames.length;i++) sql += (gentle && fieldVals[i] == null) ? ",0 AS SCORE" + i : "," + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255))))" + " AS SCORE" + i;
                sql += " FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + ">" + id;
        		for (int i=0;i<fieldnames.length;i++) sql += (gentle && fieldVals[i] == null) ? " AND TRUE" : " AND " + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255)))) <= " + maxScores[i];
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
                		for (int i=0;i<fieldnames.length;i++) result += "\t" + rs2.getDouble("SCORE" + i);
                		
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
	private void go4Table(final String tn, final Vector<String> result, final int id1, final int id2, final MyTable myT, final boolean showOnlyDataFromCurrentUser) {
		if (!tn.equals("Users")) {
			if (id1 < 0 && id2 < 0 &&
					!tn.equals("ChangeLog") && !tn.equals("DateiSpeicher") && !tn.equals("Users") &&
					!tn.equals("Matrices") && !tn.equals("Methoden") && !tn.equals("Tierkrankheiten") &&
					!tn.equals("Aufbereitungsverfahren_Kits") && !tn.equals("Nachweisverfahren_Kits") &&
					!tn.equals("Methoden_Normen") && !tn.equals("DoubleKennzahlen") &&
					!tn.equals("Messwerte_Sonstiges") && !tn.equals("Versuchsbedingungen_Sonstiges")) {
				String sql = PlausibilityChecker.getTableRepeats(myT);
				if (sql != null) {
					ResultSet rs = DBKernel.getResultSet(sql, false);
					try {
						if (rs != null && rs.first()) {
							do {
								String vals = "";
								for (int i=2;i<=rs.getMetaData().getColumnCount();i++) {
									vals += ", " + rs.getString(i);
								}
								if (vals.length() > 0) {
									vals = vals.substring(1);
									result.add(tn + " hat " + rs.getString(1) + " Datensätze mit denselben Werten: (" + vals + ")"); // Duplikate
								}
							} while (rs.next());
						}
					}
					catch (Exception e) {MyLogger.handleException(e);}
				}
			}
			LinkedHashMap<String, Vector<String[]>> sqlsAll = new LinkedHashMap<>();
			sqlsAll.put(null, PlausibilityChecker.getPlausibilityRow(null, myT, 0, "ID"));
			if (myT.getTablename().equals("Versuchsbedingungen")) {
				sqlsAll.put("Messwerte", PlausibilityChecker.getPlausibilityRow(null, DBKernel.myDBi.getTable("Messwerte"), 0, "Versuchsbedingungen"));
			}
			LinkedHashMap<Integer, Vector<String>> v = showOnlyDataFromCurrentUser ? DBKernel.getUsersFromChangeLog(tn, DBKernel.getUsername()) : null;   
			for (Map.Entry<String, Vector<String[]>> entry : sqlsAll.entrySet()) {
				String tblname = entry.getKey();
				Vector<String[]> sqls2 = entry.getValue();
				if (sqls2 != null) {
					for (int i=0;i<sqls2.size();i++) {
						String[] res = sqls2.get(i);
						ResultSet rs = DBKernel.getResultSet(res[0], false);
						try {
							if (rs != null && rs.first()) {
								do {
									Integer id = rs.getInt(1);
									String tn2 = tn;
									if (tn2.equals("Literatur") && id <= 231 || tn2.equals("Methoden") && id <= 1481 ||
											tn2.equals("Kontakte") && id <= 119 || tn2.equals("Messwerte") && id <= 18) {
										
									}
									else {
										if ((!showOnlyDataFromCurrentUser || v != null && v.containsKey(id)) &&
												(id1 < 0 && id2 < 0 || id >= id1 && id <= id2)) {
											String toAdd = tn2 + " (ID=" + getFormattedID(id) + "): ";
											if (tblname != null) {
												toAdd += tblname + " (ID=" + getFormattedID(rs.getInt(2)) + "): ";
											}
											toAdd += res[1];
		    								result.add(toAdd);
										}
									}
								} while (rs.next());
							}
						}
						catch (Exception e) {MyLogger.handleException(e);}
					}
				}
			}			
		}
	}
  	private String vectorToString(final Vector<String> vector, final String delimiter) {
	    StringBuilder vcTostr = new StringBuilder();
	    if (vector.size() > 0) {
	        vcTostr.append(vector.get(0));
	        for (int i=1; i<vector.size(); i++) {
	            vcTostr.append(delimiter);
	            vcTostr.append(vector.get(i));
	        }
	    }
	    return vcTostr.toString();
	}  
  	private String getFormattedID(final Object... id) {
  		if (id == null) {
			return null;
		}
  		return String.format("%5s", id);
  	}
  	private Vector<int[]> parseIDs(final String ids) { // 23-28
  		try  {
  			int i1 = Integer.parseInt(ids.substring(0, ids.indexOf("-")).trim());
  			int i2 = Integer.parseInt(ids.substring(ids.indexOf("-") + 1).trim());
  			if (i2 < i1) {
  				int temp = i2;
  				i2 = i1;
  				i1 = temp;
  			}
  			int[] res = new int[]{i1, i2};
  			Vector<int []> result = new Vector<>();
  			result.add(res);
  	  		return result;  			
  		}
  		catch (Exception e) {return null;}
  	}
  	@SuppressWarnings("unused")
	private void checkSonstigesInProzessketten() {
		ResultSet rs = DBKernel.getResultSet("SELECT " + DBKernel.delimitL("ID") + "," + DBKernel.delimitL("Prozess_ID") + "," + DBKernel.delimitL("ProzessElement") +
				" FROM " + DBKernel.delimitL("ProzessElemente") + " WHERE LCASE (" + DBKernel.delimitL("ProzessElement") + ") LIKE  '%onstige%'", false);
		try {
		    if (rs != null && rs.first()) {
		    	do {
		    		System.out.println(rs.getString("ID") + "\t" + rs.getString("Prozess_ID") + "\t" + rs.getString("ProzessElement"));
		    		ResultSet rs2 = DBKernel.getResultSet("SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("Prozessdaten") +
		    				" WHERE " + DBKernel.delimitL("Prozess_CARVER") + " = " + rs.getInt("Prozess_ID"), false);
		    		try {
		    		    if (rs2 != null && rs2.first()) {
		    		    	do {
		    		    		System.out.println("\t" + rs2.getString("ID"));
		    		    	} while (rs2.next());
		    		    }
		    		}
		    	    catch(Exception e2) {MyLogger.handleException(e2);}	
		    	} while (rs.next());
		    }
		}
	    catch(Exception e) {MyLogger.handleException(e);}	
  		
  	}
  	/*
  	@SuppressWarnings("unused")
	private void downloadCombase() {
  		String folder = "Q:/BfR/Desktop/combaseFetch/websites/";
  		String baseURL = "http://browser.combase.cc";
  		File dir = new File(folder);

  		int sumEntriesInCB = 0;
  		String[] children = dir.list();
	    for (int i=0; i<children.length; i++) {
	        // Get filename of file or directory
	        String filename = children[i];
	        File input = new File(folder + filename);
	        try {
				Document doc = Jsoup.parse(input, "UTF-8");
				Element table = doc.select("table").first();
				if (table != null) {
					Iterator<Element> tr = table.select("tr").iterator();
				    while (tr.hasNext()) {
						Iterator<Element> td = tr.next().select("td").iterator();
						if (td.hasNext()) {
					    	String source_id = td.next().text();
					    	td.next().text(); // String title_blabla = 
					    	int numRecords = Integer.parseInt(td.next().text());
					    	sumEntriesInCB += numRecords;
					    	Element e = td.next();
					    	e.text(); // String Summary_Details = 
					    	// Summary
					    	Element link = e.select("a").first();
					    	String url = link.attr("href");
					    	String summaryUrl = url.substring(url.lastIndexOf("/") + 1);
					    	System.out.println(baseURL + "/" + summaryUrl);
						    getCSVFile(folder, baseURL, summaryUrl, numRecords);
					    	// Details
					    	link = e.select("a").last();
					    	url = link.attr("href");
					    	System.out.println(baseURL + "/" + url.substring(url.lastIndexOf("/") + 1));
					    	Integer id = DBKernel.getID("Literatur", "Kommentar", source_id);
					    	if (id == null) {
								System.err.println(source_id + " nicht in DB");
							} else {
					    		int numInDB = DBKernel.getRowCount("Versuchsbedingungen", " WHERE " + DBKernel.delimitL("Referenz") + " = " + id);
					    		if (numRecords != numInDB) {
									System.err.println(source_id + "(" + id + "): " + numRecords + " in Combase vs. " + numInDB + " in DB...");
								}
					    	}
						}
					}
				}
	        }
	        catch (IOException e) {
				e.printStackTrace();
			}	        
	    }
	    System.out.println("sumEntriesInCB: " + sumEntriesInCB);  		
  	}

  	private void getCSVFile(final String folder2Save, final String baseURL, final String urlString, final int numRecords) {
		try {
			File csvFile = new File(folder2Save + urlString.substring(urlString.indexOf("?") + 1) + "&numRecords=" + numRecords + ".csv");
			if (csvFile.exists()) {
				System.out.println("exists");
			}
			else {
				System.err.println("exists not: " + csvFile.getName());
				
  	
  	
				/*
Sub doit()
Dim Count As Integer
Count = 0
    For Counter = 1 To 65000
        Set curCell = Worksheets(1).Cells(Counter, 1)
        If curCell.Value = "RecordID" Then Count = Count + 1
    Next Counter
    Debug.Print Count
End Sub

				Sausage fehlt und:
				
http://browser.combase.cc/ResultSummary.aspx?SourceID=O%27Mahony_01&Foodtype=Other%2fmix
				
				
				URL myUrl = new URL(baseURL + "/" + urlString);
				URLConnection urlConn = myUrl.openConnection();
				urlConn.setRequestProperty("Cookie", "DisclaimerSigned=Signed");
				urlConn.connect();
			    BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			    String html = "";
				String inputLine;
			    while ((inputLine = in.readLine()) != null)
				 {
					html += inputLine;//System.out.println(inputLine);
				}
			    in.close();
			    Document doc = Jsoup.parse(html, "UTF-8");
			    Element el = doc.select("#ContentPlaceHolder1_CombaseResultSummary1_lnkDownloadCSV").first();
			    System.out.println(el.attr("href"));
				myUrl = new URL(baseURL + "/" + el.attr("href"));
				urlConn = myUrl.openConnection();
				urlConn.setRequestProperty("Cookie", "DisclaimerSigned=Signed");
				urlConn.connect();
				saveUrl(folder2Save + urlString.substring(urlString.indexOf("?") + 1) + ".csv", urlConn.getInputStream());
				
  	
  	
  	
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
  	}
  	private void saveUrl(final String filename, final InputStream urlStream) throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
                in = new BufferedInputStream(urlStream);
                fout = new FileOutputStream(filename);

                byte data[] = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1)  {
                        fout.write(data, 0, count);
                }
        }
        finally {
                if (in != null) {
					in.close();
				}
                if (fout != null) {
					fout.close();
				}
        }
    }
  	@SuppressWarnings("unused")
	private void check4EmptyPKsMWs(final String tablename, final String nullFeldname) {
		ResultSet rs = DBKernel.getResultSet("SELECT * FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL(nullFeldname) + " IS NULL", false);
		try {
			if (rs != null && rs.first()) {
				do {
					String tt = "";
					LinkedHashMap<Integer, Vector<String>> v = DBKernel.getUsersFromChangeLog(tablename, rs.getInt("ID"));
				  	for (Map.Entry<Integer, Vector<String>> entry : v.entrySet()) {
						for (String entr : entry.getValue()) {
							tt += entry.getKey() + "\t" + entr + "\n"; 
						}
				  	}			
					System.err.println(tablename + "\t" + rs.getInt("ID") + "\n" + tt);
				} while (rs.next());
			}
		}
		catch (Exception e) {MyLogger.handleException(e);}
		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL(nullFeldname) + " IS NULL", false);
  	}
  	@SuppressWarnings("unused")
	private void checkAllEntriesIfOthersAlreadyEditedUpdates() {
		LinkedHashMap<String, MyTable> myTables = DBKernel.myDBi.getAllTables();

		for(String key : myTables.keySet()) {
			MyTable myT = myTables.get(key);
			String tn = myT.getTablename();
			if (!tn.equals("ChangeLog") && !tn.equals("DateiSpeicher") && !tn.equals("Infotabelle")) {
				System.err.println(tn);

				  String sql = "SELECT ARRAY_AGG(" + DBKernel.delimitL("Alteintrag") + " ORDER BY " + DBKernel.delimitL("Zeitstempel") + " ASC)," +
				    "ARRAY_AGG(" + DBKernel.delimitL("Zeitstempel") + " ORDER BY " + DBKernel.delimitL("Zeitstempel") + " ASC)," + 
				    "ARRAY_AGG(" + DBKernel.delimitL("Username") + " ORDER BY " + DBKernel.delimitL("Zeitstempel") + " ASC)," +
				    "MAX(" + DBKernel.delimitL("TabellenID") + ")" +
				    " FROM " + DBKernel.delimitL("ChangeLog") +
				    	" WHERE " + DBKernel.delimitL("Tabelle") + " = '" + tn + "'" +
				    	" GROUP BY " + DBKernel.delimitL("TabellenID");
				    ResultSet rs2 = DBKernel.getResultSet(sql, false);
				try {
				    if (rs2 != null && rs2.first()) {
				    	do {
						    Array a = rs2.getArray(1);
						    if (a != null) {
						    	Array un = rs2.getArray(3);
						    	if (un != null) {
								    Object[] usernamen = (Object[])un.getArray();				
								    for (int i=1;i<usernamen.length;i++) {
								    	if (!usernamen[i].equals(usernamen[0])) { // verschiedene Usernamen
								    		ResultSet rs3 = DBKernel.getResultSet("SELECT * FROM " + DBKernel.delimitL(tn) +
								    				" WHERE " + DBKernel.delimitL("ID") + "=" + rs2.getInt(4), false);
								    		if (rs3 != null && rs3.first()) {
											    Object[] ts = (Object[])rs2.getArray(2).getArray();				
											    Object[] alteintraege = (Object[])a.getArray();	
									    		for (int j=0;j<usernamen.length - 1;j++) {
									    			System.err.print(usernamen[j] + "\t" + ts[j]);
												    Object[] arr = (Object[]) alteintraege[j+1];
												    if (arr == null) {
												    	System.err.print("\t" + rs2.getInt(4) + "\tnull"); // das hier sollte nie passieren
												    }
												    else {
													    for (int k=0;k<arr.length;k++) {
												    		System.err.print("\t" + arr[k]);								    	
													    }											    	
												    }
												    System.err.println();
									    		}
									    		// Letzter Datensatz - in der Datenbank noch drin
									    		System.err.print(usernamen[usernamen.length - 1] + "\t" + ts[usernamen.length - 1]);
								    			for (int k=1;k<=rs3.getMetaData().getColumnCount();k++) {
								    				System.err.print("\t" + rs3.getObject(k));	
								    			}
								    			rs3.close();
									    		System.err.println();
									    		break;
								    		}
								    		else {
								    			// Datensatz bereits wieder gelöscht - System.err.print("\t" + rs2.getInt(4) + "\tnull"); 
								    		}
								    	}
								    }
						    	}
						    }				    		
				    	} while (rs2.next());
				    	rs2.close();
				    }
				}
				catch (Exception e) {e.printStackTrace();}
			}
		}
		System.out.println("Fertig");
  	}
  	*/
  	private void doSpecialThings() {
  		/*
  		DBKernel.dontLog = true;
  		DBKernel.importing = true;new MySQLImporter(1000000, true, false, true).doImport("", null, true);DBKernel.importing = false;
  		DBKernel.dontLog = false;
  		*/
  		//checkAllEntriesIfOthersAlreadyEditedUpdates();
  		
  		/*
		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Messwerte") +
				" WHERE " + DBKernel.delimitL("Versuchsbedingungen") + " <= 2 AND " + DBKernel.delimitL("ID") + " > 18", false);
  		DBKernel.dontLog = true;
  		check4EmptyPKsMWs("Messwerte", "Versuchsbedingungen");
  		//check4EmptyPKsMWs("Messwerte_Sonstiges", "Messwerte");
  		//check4EmptyPKsMWs("Versuchsbedingungen_Sonstiges", "Versuchsbedingungen");
  		check4EmptyPKsMWs("Prozessdaten", "Workflow");
  		//check4EmptyPKsMWs("ComBaseImport", "Referenz");
  		DBKernel.dontLog = false;
  		*/
  		/*
  		//DBKernel.dontLog = true;
  		DBKernel.importing = true;new MySQLImporter(10000, true, false, true).doImport("", null, true);DBKernel.importing = false;
  		//DBKernel.dontLog = false;
  		*/
  		
  		//checkSonstigesInProzessketten();
  		//downloadCombase();
  		/*
  		DBKernel.dontLog = true;
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Codes_Methoden"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Codes_Methodiken"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Methodiken"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Methoden"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_Kodes"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_MortL4"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_MortL3"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_MortL3Grp"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_MortL2"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_MortL1"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_MortL1Grp"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_MorbL"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_Gruppen"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ICD10_Kapitel"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Codes_Agenzien"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Codes_Matrices"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Users"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Kontakte"), false);
  		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Labore"), false);
  		//DBKernel.sendRequest("CREATE USER " + DBKernel.delimitL("SA") + " PASSWORD '' ADMIN", false);
  		//DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ComBaseImport"), false);
		//new GeneralXLSImporter().doImport("/org/hsh/bfr/db/res/" + "ComBaseImport.xls", null, false);						
  		DBKernel.sendRequest("DROP USER " + DBKernel.delimitL("defad"), false);
  		DBKernel.dontLog = false;
  		*/
  		/*
  		try {
	    	PreparedStatement ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Users") +
					" (" + DBKernel.delimitL("Username") + "," + DBKernel.delimitL("Vorname") + "," + DBKernel.delimitL("Name") + "," + DBKernel.delimitL("Zugriffsrecht") + ") VALUES (?,?,?,?)");
	    	ps.setString(1, "SA"); ps.setString(2, "S"); ps.setString(3, "A"); ps.setInt(4, Users.ADMIN); ps.execute();				  			
  		}
  		catch (Exception e1) {e1.printStackTrace();}
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Versuchsbedingungen") +
				" DROP CONSTRAINT " + DBKernel.delimitL("Versuchsbedingungen_fk_Luftfeuchtigkeit_12"), false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Versuchsbedingungen") +
				" DROP COLUMN " + DBKernel.delimitL("Luftfeuchtigkeit"), false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Messwerte") +
				" DROP CONSTRAINT " + DBKernel.delimitL("Messwerte_fk_Luftfeuchtigkeit_11"), false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Messwerte") +
				" DROP COLUMN " + DBKernel.delimitL("Luftfeuchtigkeit"), false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Modellkatalog") + " ALTER COLUMN " + DBKernel.delimitL("Name") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Modellkatalog") + " ALTER COLUMN " + DBKernel.delimitL("Notation") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Modellkatalog") + " ALTER COLUMN " + DBKernel.delimitL("Eingabedatum") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("ModellkatalogParameter") + " ALTER COLUMN " + DBKernel.delimitL("Modell") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("ModellkatalogParameter") + " ALTER COLUMN " + DBKernel.delimitL("Parametername") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("ModellkatalogParameter") + " ALTER COLUMN " + DBKernel.delimitL("Parametertyp") + " SET DEFAULT 1", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("ModellkatalogParameter") + " ALTER COLUMN " + DBKernel.delimitL("ganzzahl") + " SET DEFAULT FALSE", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Modell_Referenz") + " ALTER COLUMN " + DBKernel.delimitL("Modell") + " SET NOT NULL ", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Modell_Referenz") + " ALTER COLUMN " + DBKernel.delimitL("Literatur") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetzteModelle") + " ALTER COLUMN " + DBKernel.delimitL("Modell") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetzteModelle") + " ALTER COLUMN " + DBKernel.delimitL("manuellEingetragen") + " SET DEFAULT FALSE", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetztesModell_Referenz") + " ALTER COLUMN " + DBKernel.delimitL("GeschaetztesModell") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetztesModell_Referenz") + " ALTER COLUMN " + DBKernel.delimitL("Literatur") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetzteParameter") + " ALTER COLUMN " + DBKernel.delimitL("GeschaetztesModell") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetzteParameter") + " ALTER COLUMN " + DBKernel.delimitL("Parameter") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetzteParameterCovCor") + " ALTER COLUMN " + DBKernel.delimitL("param1") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetzteParameterCovCor") + " ALTER COLUMN " + DBKernel.delimitL("param2") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetzteParameterCovCor") + " ALTER COLUMN " + DBKernel.delimitL("GeschaetztesModell") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("GeschaetzteParameterCovCor") + " ALTER COLUMN " + DBKernel.delimitL("cor") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Sekundaermodelle_Primaermodelle") + " ALTER COLUMN " + DBKernel.delimitL("GeschaetztesPrimaermodell") + " SET NOT NULL", false);
		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Sekundaermodelle_Primaermodelle") + " ALTER COLUMN " + DBKernel.delimitL("GeschaetztesSekundaermodell") + " SET NOT NULL", false);
  		*/
  	}
}
