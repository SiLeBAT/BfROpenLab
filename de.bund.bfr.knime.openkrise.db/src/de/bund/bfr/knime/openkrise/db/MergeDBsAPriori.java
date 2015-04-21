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
package de.bund.bfr.knime.openkrise.db;

import java.awt.Cursor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;

public class MergeDBsAPriori {

	private Hashtable<String, Integer> idConverter;
	/*
	// 2) Könntest du vielleicht bei dem Eintrag 3605 Escherichia coli 0104:H4 eine Änderung in O104:H4 vornehmen?
	select * from "ChangeLog" WHERE "Tabelle" = 'Matrices' ID=33 checken -> rückgängig machen!!! Fleisch warmblütiger Tiere auch tiefgefroren - In Agenzien_Matrices 2 Einträge ändern!
	select * from "ChangeLog" WHERE "Tabelle" = 'Agenzien' ID=20 (10 Änderungen notwendig),63 (0 Änderungen notwendig),178 (0 Änderung notwendig),756 (14 Änderungen notwendig),3605 (0104)
	
	Literatur
	Agenzien_Matrices
	Krankheitsbilder
	Risikogruppen
	Symptome
	Krankheitsbilder_Risikogruppen
	Krankheitsbilder_Symptome

	DoubleKennzahlen
	ICD10_Kodes
	*/
	public MergeDBsAPriori () {
	    int retVal = JOptionPane.showConfirmDialog(DBKernel.mainFrame, "Sicher?",
	    		"DBs zusammenführen?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (retVal == JOptionPane.YES_OPTION && DBKernel.isAdmin()) {
			try {
				DBKernel.mainFrame.getMyList().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	
				String folder = "C:/Dokumente und Einstellungen/Weiser/Desktop/silebat_146/KHB/";
				MyTable[] myTs = new MyTable[]{
						DBKernel.myDBi.getTable("Literatur"),
						DBKernel.myDBi.getTable("Agenzien_Matrices"),
						DBKernel.myDBi.getTable("Krankheitsbilder"),
						DBKernel.myDBi.getTable("Risikogruppen"),
						DBKernel.myDBi.getTable("Symptome"),
						DBKernel.myDBi.getTable("Krankheitsbilder_Risikogruppen"),
						DBKernel.myDBi.getTable("Krankheitsbilder_Symptome")};
				Integer[] myFromIDs = new Integer[]{242, null, null, null, null, null, null};
				idConverter = new Hashtable<>();
				go4It(folder, "defad", "de6!§5ddy", myTs, myFromIDs);

				LinkedHashMap<String, MyTable> myTables = DBKernel.myDBi.getAllTables();
				for(String key : myTables.keySet()) {
					myTables.get(key).doMNs();
				}
				
				MyDBTable myDB = DBKernel.mainFrame.getMyList().getMyDBTable();
				myDB.setTable(myDB.getActualTable());
				
				JOptionPane.showMessageDialog(DBKernel.mainFrame, "Fertig!", "DBs zusammenführen", JOptionPane.INFORMATION_MESSAGE);		
			}
			finally {
				DBKernel.mainFrame.getMyList().setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	private void go4It(final String dbPath, String username, String password, MyTable[] myTs, Integer[] myFromIDs) {
		System.out.println(dbPath);
		//boolean dl = DBKernel.dontLog;
		//DBKernel.dontLog = true;
		try {
			Connection conn = null;
			if (username != null && username != password) conn = DBKernel.getDBConnection(dbPath, username, password, true);
			else conn = DBKernel.getDefaultAdminConn(dbPath, true);
		    Statement anfrage = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		    checkeDoppeltVergebeneDKZs(anfrage);
		    go4Tables(anfrage, myTs, myFromIDs);
			go4Dateispeicher(anfrage);
			anfrage.execute("SHUTDOWN");
			anfrage.close();
			conn.close();
		}
		catch (Exception e) {
			MyLogger.handleException(e);
		}		
		//DBKernel.dontLog = dl;		
	}
	private void go4Tables(final Statement anfrage, MyTable[] myTs, Integer[] myFromIDs) {
		int i=0;
		for (MyTable myT : myTs) {
			try {
				ResultSet rs = DBKernel.getResultSet(anfrage, "SELECT * FROM " + DBKernel.delimitL(myT.getTablename()) +
						(myFromIDs[i] != null ? " WHERE " + DBKernel.delimitL("ID") + ">=" + myFromIDs[i] : ""), false);
				i++;
				if (rs != null && rs.first()) {
					do {
						PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(myT.getInsertSQL1(), Statement.RETURN_GENERATED_KEYS);
						doFields(ps, myT, rs, anfrage, null);
						try {
							if (ps.executeUpdate() > 0) {
								idConverter.put(myT.getTablename() + "_" + rs.getInt("ID"), DBKernel.getLastInsertedID(ps));						
							}
						}
						catch (Exception e) {e.printStackTrace();}
						ps.close();
					} while (rs.next());
				}
				rs.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private String doFields(final PreparedStatement ps, final MyTable myT, ResultSet rs, final Statement anfrage, String sql) {
		try {
			String[] fn = myT.getFieldNames();
			String[] ft = myT.getFieldTypes();
			MyTable[] foreigns = myT.getForeignFields();
			String[] mnTable = myT.getMNTable();
			int i=1;
			for (;i<=ft.length;i++) { // ID wird hier nicht benötigt
				Object o = rs.getObject(i+1);
				if (ft[i-1].equals("DOUBLE")) {
					if (o == null) {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + " IS NULL";
						else ps.setNull(i, java.sql.Types.DOUBLE);
					}
					else {
						double dbl = rs.getDouble(i+1);
						if (foreigns[i-1] != null && (mnTable == null || mnTable[i-1] == null)) { // Fremdtabelle=DoubleKennzahlen
							dbl = handleForeignKey((int) dbl, foreigns[i-1], anfrage, true);
						}
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + "=" + dbl;
						else ps.setDouble(i, dbl);
					}																	
				}
				else if (ft[i-1].equals("INTEGER")) {
					if (o == null) {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + " IS NULL";
						else ps.setNull(i, java.sql.Types.INTEGER);
					}
					else {
						int id = rs.getInt(i+1);
						if (foreigns[i-1] != null && (mnTable == null || mnTable[i-1] == null)) { // Fremdtabelle
							id = handleForeignKey(id, foreigns[i-1], anfrage, false);
						}
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + "=" + id;
						else ps.setInt(i, id);
					}
				}
				else if ((ft[i-1].startsWith("VARCHAR(") || ft[i-1].startsWith("CHAR(") || ft[i-1].startsWith("BLOB("))) {
					if (o == null) {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + " IS NULL";
						else ps.setNull(i, java.sql.Types.VARCHAR);
					} else {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + "='" + rs.getString(i+1).replace("'", "''") + "'";
						else ps.setString(i, rs.getString(i+1));
					}											
				}
				else if (ft[i-1].equals("BOOLEAN")) {
					if (o == null) {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + " IS NULL";
						else ps.setNull(i, java.sql.Types.BOOLEAN);
					}
					else {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + "=" + rs.getBoolean(i+1);
						else ps.setBoolean(i, rs.getBoolean(i+1));
					}											
				}
				else if (ft[i-1].equals("DATE")) {
					if (o == null) {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + " IS NULL";
						else ps.setNull(i, java.sql.Types.DATE);
					}
					else {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + "='" + rs.getString(i+1) + "'"; // besser rs.getDate(i+1) ???
						else ps.setDate(i, rs.getDate(i+1));
					}
				}
				else if (ft[i-1].equals("BIGINT")) {
					if (o == null) {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + " IS NULL";
						else ps.setNull(i, java.sql.Types.BIGINT);
					}
					else {
						if (sql != null) sql += " AND " + DBKernel.delimitL(fn[i-1]) + "=" + rs.getLong(i+1);
						else ps.setLong(i, rs.getLong(i+1));
					}											
				}
				else if (ps != null) {
					MyLogger.handleMessage("Parameter not dwefined... " + myT.getTablename() + "\t" + i);
				}
			}
			if (!myT.getHideScore()) { // INTEGER
				Object o = rs.getObject(i+1);
				if (o == null) {
					if (sql != null) sql += " AND " + DBKernel.delimitL("Guetescore") + " IS NULL";
					else ps.setNull(i, java.sql.Types.INTEGER);
				}
				else {
					if (sql != null) sql += " AND " + DBKernel.delimitL("Guetescore") + "=" + rs.getInt(i+1);
					else ps.setInt(i, rs.getInt(i+1));
				}						
				i++;
			}
			if (!myT.getHideKommentar()) { // VARCHAR(1023)
				Object o = rs.getObject(i+1);
				if (o == null) {
					if (sql != null) sql += " AND " + DBKernel.delimitL("Kommentar") + " IS NULL";
					else ps.setNull(i, java.sql.Types.VARCHAR);
				}
				else {
					if (sql != null) sql += " AND " + DBKernel.delimitL("Kommentar") + "='" + rs.getString(i+1).replace("'", "''") + "'";
					else ps.setString(i, rs.getString(i+1));
				}
				i++;
			}
			if (!myT.getHideTested()) { // BOOLEAN
				Object o = rs.getObject(i+1);
				if (o == null) {
					if (sql != null) sql += " AND " + DBKernel.delimitL("Geprueft") + " IS NULL";
					else ps.setNull(i, java.sql.Types.BOOLEAN);
				}
				else {
					if (sql != null) sql += " AND " + DBKernel.delimitL("Geprueft") + "=" + rs.getBoolean(i+1);
					else ps.setBoolean(i, rs.getBoolean(i+1));
				}			
				i++;
			}		
			
		}
		catch (Exception e) {
			MyLogger.handleMessage(myT.getTablename());
			MyLogger.handleException(e);
		}
		return sql;
	}
	private Integer handleForeignKey(Integer foreignID, MyTable foreignTable, final Statement anfrage, boolean forceNewEntry) {
  	    Integer cid = convertID(foreignTable.getTablename(), foreignID);
		if (cid != null) return cid;
		Integer result = null;
		ResultSet rs1 = DBKernel.getResultSet(anfrage, "SELECT * FROM " + DBKernel.delimitL(foreignTable.getTablename()) + " WHERE " + DBKernel.delimitL("ID") + "=" + foreignID, false);
		ResultSet rs2 = DBKernel.getResultSet("SELECT * FROM " + DBKernel.delimitL(foreignTable.getTablename()) + " WHERE " + DBKernel.delimitL("ID") + "=" + foreignID, false);
		
		boolean gleich = forceNewEntry ? false : compareDBEntries(rs1, rs2);
		if (forceNewEntry || !gleich) {
			if (!forceNewEntry) System.err.println("\t" + foreignID + "\t" + foreignTable.getTablename());
			result = findID(foreignTable, rs1, anfrage);
			if (result == null) {
				PreparedStatement ps = null;
				try {
					ps = DBKernel.getDBConnection().prepareStatement(foreignTable.getInsertSQL1(), Statement.RETURN_GENERATED_KEYS);
					doFields(ps, foreignTable, rs1, anfrage, null);						
					if (ps.executeUpdate() > 0) {
						result = DBKernel.getLastInsertedID(ps);
					}
					ps.close();
				}
				catch (Exception e) {
					System.err.println(ps); e.printStackTrace();
				}
			}
		}
		else {
			result = foreignID;
		}
		if (result == null) {
			handleForeignKey(foreignID, foreignTable, anfrage, forceNewEntry);
		}
		idConverter.put(foreignTable.getTablename() + "_" + foreignID, result);						
		return result;
	}
	private Integer findID(MyTable myT, ResultSet rs, final Statement anfrage) {
		Integer result = null;
		try {
			String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(myT.getTablename()) + " WHERE TRUE ";// + myT.getUpdateSQL1().substring(myT.getUpdateSQL1().indexOf(" SET ") + 5);
			sql = doFields(null, myT, rs, anfrage, sql);
			ResultSet rsq = DBKernel.getResultSet(sql, false);
			if (rsq != null && rsq.first()) {
				result = rsq.getInt("ID");
				if (rsq.next()) {
					System.err.println("Mehr als einen Eintrag????\n" + sql);
				}
			}
			rsq.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	private boolean compareDBEntries(ResultSet rs1, ResultSet rs2) {
		boolean result = false;
		try {
			int i=0;
			if (rs1 != null && rs1.first() && rs2 != null && rs2.first()) {
				if (rs1.getMetaData().getColumnCount() != rs2.getMetaData().getColumnCount()) return false;
				for (i=1;i<=rs1.getMetaData().getColumnCount();i++) { // i startet bei 1
					Object o1 = rs1.getObject(i);
					Object o2 = rs2.getObject(i);
					if (o1 == null && o2 == null || o1 != null && o2 != null && o1.toString().equals(o2.toString())) {
						continue;
					}
					else {
						System.err.print("verschieden:\t" + o1 + "\t" + o2);
						break;
					}
				}			
			}
			if (i == rs1.getMetaData().getColumnCount() + 1) {
				result = true;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
    private void checkeDoppeltVergebeneDKZs(final Statement anfrage) {
    	System.err.println("checkeDoppeltVergebeneDKZs - Start");
		LinkedHashMap<String, MyTable> myTables = DBKernel.myDBi.getAllTables();
		Hashtable<Integer, String> hash = new Hashtable<>();
		for(String key : myTables.keySet()) {
			MyTable myT = myTables.get(key);
			String tn = myT.getTablename();
			System.out.println(tn);
			MyTable[] foreignFields = myT.getForeignFields();
			if (foreignFields != null) {
				for (int i=0; i<foreignFields.length; i++) {
					if (foreignFields[i] != null && foreignFields[i].getTablename().equals("DoubleKennzahlen")) {
						ResultSet rs = DBKernel.getResultSet(anfrage, "SELECT " + DBKernel.delimitL(myT.getFieldNames()[i]) +
								"," + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(myT.getTablename()), false);
						try {
						    if (rs != null && rs.first()) {
						    	do {
							    	if (rs.getObject(1) != null) {
							    		Integer dkzID = rs.getInt(1);
							    		if (hash.containsKey(dkzID)) {
											System.err.println("DKZ doppelt vergeben: " + dkzID + "\t" + tn + "_" + rs.getString(2) + "\t" + hash.get(dkzID));
										} else {
											hash.put(dkzID, tn + "_" + rs.getString(2));
										}
							    	}
						    	} while(rs.next());
						    	rs.close();
						    }
						}
						catch (Exception e) {e.printStackTrace();}
					}
				}
			}
		}
    	System.err.println("checkeDoppeltVergebeneDKZs - Fin");
    }
	private void go4Dateispeicher(final Statement anfrage) {
	    try {	    	
		    String sql = "SELECT * FROM " + DBKernel.delimitL("DateiSpeicher") + " ORDER BY " + DBKernel.delimitL("Zeitstempel") + " ASC";
	        ResultSet rs = DBKernel.getResultSet(anfrage, sql, false);
				if (rs.first()) {
				      sql = "INSERT INTO " + DBKernel.delimitL("DateiSpeicher") +
				      " (" + DBKernel.delimitL("Zeitstempel") + "," + DBKernel.delimitL("Tabelle") + "," + DBKernel.delimitL("Feld") + "," +
				      DBKernel.delimitL("TabellenID") + "," +
				      DBKernel.delimitL("Dateiname") + "," + DBKernel.delimitL("Dateigroesse") + "," + DBKernel.delimitL("Datei") + ")" +
				      " VALUES (?,?,?,?,?,?,?);";
			    	PreparedStatement psmt = DBKernel.getDBConnection().prepareStatement(sql);
					do {
						String tablename = rs.getString("Tabelle");
						Integer tID = rs.getInt("TabellenID");
						int blobSize = rs.getInt("Dateigroesse");
				  	    Integer cid = convertID(tablename, tID);
				  	  if (cid != null) { // nur, wenn vorher mal etwas Passendes inserted wurde!
					  	    psmt.clearParameters();
					  	    psmt.setTimestamp(1, rs.getTimestamp("Zeitstempel"));
					  	    psmt.setString(2, tablename);
					  	    psmt.setString(3, rs.getString("Feld"));
					    	psmt.setInt(4, cid);
					  	    psmt.setString(5, rs.getString("Dateiname"));
					  	    psmt.setInt(6, blobSize);
				            // Get as a BLOB
				            Blob aBlob = rs.getBlob("Datei");
				            byte[] b = aBlob.getBytes(1, (int) aBlob.length());
					  	    InputStream bais = new ByteArrayInputStream(b);
					  	    psmt.setBinaryStream(7, bais, b.length);
					  	    psmt.executeUpdate();
					  	    bais.close();
				  	  }
					} while (rs.next());
			  	    psmt.close();
				}  
				rs.close();
	    }
	    catch (Exception e) {
	    	MyLogger.handleException(e);
	    }
	}
	private Integer convertID(final String tablename, final Integer id) {
		Integer result = null;
		if (id != null) {
	    	if (idConverter.containsKey(tablename + "_" + id)) {
				result = idConverter.get(tablename + "_" + id);
			}
		}
		return result;
	}
}
