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
package org.hsh.bfr.db.imports.custom;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.MyTable;
import org.hsh.bfr.db.ParseCarverXML;
import org.hsh.bfr.db.gui.InfoBox;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.imports.MyImporter;


/**
 * @author Weiser
 *
 */
public class MyProzessXMLImporter extends FileFilter implements MyImporter {
	  /**
	  This is the one of the methods that is declared in 
	  the abstract class
	 */
	private String errorMessage = "";
	
		public boolean accept(File f) {
		  if (f.isDirectory()) return true;
		
		  String extension = getExtension(f);
		  if ((extension.equals("xml") || extension.equalsIgnoreCase("PEX"))) return true; 
		  return false;
		}
		  
		public String getDescription() {
		    return "Carver Datei (*.xml; *.pex)";
		}
		
		private String getExtension(File f) {
		  String s = f.getName();
		  int i = s.lastIndexOf('.');
		  if (i > 0 &&  i < s.length() - 1) return s.substring(i+1).toLowerCase();
		  return "";
		}


		public String doImport(final String filename, final JProgressBar progress, final boolean showResults) {
			errorMessage = "";
	  	Runnable runnable = new Runnable() {
	      public void run() {
			try {
		    	MyLogger.handleMessage("Importing PEX-File: " + filename);
	      		if (progress != null) {
	      			progress.setVisible(true);
	      			progress.setStringPainted(true);
	      			progress.setString("Importiere Carver-Datei...");
	      			progress.setMinimum(0);
	      		}

	      		File xmlFile = new File(filename);
			    int numSuccess = 0;
	      		if (xmlFile.exists()) {
	      			Vector<Integer> importedCarverIDs = new Vector<>();
			    	ParseCarverXML pcxml = new ParseCarverXML(filename);
			    	//String[] pn = pcxml.getProcessNames();
			    	//int[] pid = pcxml.getProcessIDs();
			    	Vector<Integer[]> org_dst = pcxml.getOrgDst();
			    	LinkedHashMap<Integer, Integer> index_processID = pcxml.getCarverIDProcessID();
			    	LinkedHashMap<Integer, String> index_processName = pcxml.getCarverIDProcessName();
			    	
	      			progress.setMaximum(index_processID.size());
				    try {				    	
				        String sql = "INSERT INTO " + DBKernel.delimitL("ProzessWorkflow") +
		      			" (" + DBKernel.delimitL("XML") + ") VALUES (?)";
					    PreparedStatement psmt = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				      	psmt.setString(1, xmlFile.getName());
				      	int xmlID = 0;
					    if (psmt.executeUpdate() > 0) {
					    	ResultSet rs = psmt.getGeneratedKeys();
					        if (rs.next()) {
					        	xmlID = rs.getInt(1);
						        DBKernel.insertBLOB("ProzessWorkflow", "XML", xmlFile, xmlID);      	
					        }
					        else {
					        	System.err.println("getGeneratedKeys failed!");
					        }					  
					        rs.close();
					    }
					    psmt.close();
					    if (xmlID > 0) {
						    sql = "INSERT INTO " + DBKernel.delimitL("Prozessdaten") +
			      			" (" + DBKernel.delimitL("Workflow") + "," + DBKernel.delimitL("Prozess_CARVER") +
			      			"," + DBKernel.delimitL("ProzessDetail") + ") VALUES (?,?,?)";
						    psmt = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
						    int i=0;
						    Vector<Integer> indexReihenfolge = getReihenfolge(org_dst);
						    cleanReihenfolgeListe(indexReihenfolge, index_processID, org_dst);
					    	LinkedHashMap<Integer, Integer> index_ProzessdatenID = new LinkedHashMap<>();
					    	//for (Map.Entry<Integer, Integer> entry : index_processID.entrySet()) {
						    for (int ii=0;ii<indexReihenfolge.size();ii++) {
								if (progress != null) progress.setValue(i);
							    psmt.clearParameters();
							    //psmt.setString(1, xmlFile.getName());
							    psmt.setInt(1, xmlID);
							    Integer key = indexReihenfolge.get(ii); // entry.getKey()
							    Integer value = index_processID.get(key); // entry.getValue()
								//System.out.println(key + "\t" +  value);
						        Integer foreignID = DBKernel.getID("ProzessElemente", "Prozess_ID", ""+value);
						      	if (foreignID != null) psmt.setInt(2, foreignID);
						      	else psmt.setNull(2, java.sql.Types.INTEGER);
						      	String strValue = index_processName.get(key);
							    psmt.setString(3, strValue);
							    if (psmt.executeUpdate() > 0) {
							    	numSuccess++;	
							    	ResultSet rs = psmt.getGeneratedKeys();
							    	if (rs.next()) {
							    		if (i==0) DBKernel.insertBLOB("Prozessdaten", "Workflow", xmlFile, rs.getInt(1));
							    		index_ProzessdatenID.put(key, rs.getInt(1));
							    		importedCarverIDs.add(value);
							    	}
							    	else {
							    		System.err.println("getGeneratedKeys failed!");
							    	}
							    	rs.close();							    		
							    }
							    i++;
					    	}
						    psmt.close();
						    
						    sql = "INSERT INTO " + DBKernel.delimitL("Prozess_Verbindungen") +
			      			" (" + DBKernel.delimitL("Ausgangsprozess") + "," + DBKernel.delimitL("Zielprozess") + ") VALUES (?,?)";
						    psmt = DBKernel.getDBConnection().prepareStatement(sql);
						    Integer[] int2;
						    for (int ii=0;ii<indexReihenfolge.size();ii++) {
						    	for (i=0;i<org_dst.size();i++) {
							    	int2 = org_dst.get(i);
							    	if (int2[0] == indexReihenfolge.get(ii)) {
								    	psmt.clearParameters();
								    	psmt.setInt(1, index_ProzessdatenID.get(int2[0]));
								    	Integer pid = index_ProzessdatenID.get(int2[1]);
								    	if (pid != null) {
									    	psmt.setInt(2, pid);
									    	psmt.execute();							    		
								    	}
							    	}
						    	}
						    }
						    psmt.close();
					    }
					  }
				    catch (Exception e) {
				    	MyLogger.handleException(e);
				    }
				    
				    // Output für IFE-Burchardi
		      		HSSFWorkbook wb = new HSSFWorkbook();
			    	HSSFSheet sheet = wb.createSheet(xmlFile.getName());
    		    	HSSFRow row = sheet.createRow(0); 
		    		HSSFCell cell = row.createCell(0); cell.setCellValue("ID");
		    		cell = row.createCell(1); cell.setCellValue("Prozesselement");	    	
				    for (int i=0;i<importedCarverIDs.size();i++) {
			    		ResultSet rs2 = DBKernel.getResultSet("SELECT " + DBKernel.delimitL("ProzessElement") + " FROM " + DBKernel.delimitL("ProzessElemente") +
			    				" WHERE " + DBKernel.delimitL("Prozess_ID") + "=" + importedCarverIDs.get(i), false);
			    		try {
			    			if (rs2 != null && rs2.last() && rs2.getRow() == 1) {
			    		    	row = sheet.createRow(i+1); 
					    		cell = row.createCell(0); cell.setCellValue(importedCarverIDs.get(i));	    			
					    		cell = row.createCell(1); cell.setCellValue(rs2.getString(1));	    			
			    			}
			    		}
			    		catch (Exception e) {MyLogger.handleException(e);}				    	
				    }
			    	if (DBKernel.getUsername().equals("burchardi")) {
					    try {
					    	FileOutputStream fileOut = new FileOutputStream(filename + "_ife.xls"); // Der Export hier ist ja nur für IFE gedacht!
					    	wb.write(fileOut);
					    	fileOut.close();
					    }
					    catch (Exception e) {
					    	e.printStackTrace();
					    	errorMessage += "Export Problem:\t" + e.getMessage();
				  			//JOptionPane.showMessageDialog(progress, e.getMessage(), "Export Problem", JOptionPane.OK_OPTION);
					    }
			    	}
	      		}

	    			if (progress != null) {
	    				progress.setVisible(false);
	  	  			// Refreshen:
	    				DBKernel.myDBi.getTable("ProzessWorkflow").doMNs();
	    				DBKernel.myDBi.getTable("Prozessdaten").doMNs();
	    				MyDBTable myDB = DBKernel.mainFrame.getMyList().getMyDBTable();
	    				MyTable myActT = myDB.getActualTable();
	    				if (myActT != null) {
		    				String actTablename = myActT.getTablename();
		    				if (actTablename.equals("Prozessdaten") || actTablename.equals("ProzessWorkflow") || actTablename.equals("Prozess_Verbindungen")) {
		    			    	System.err.println("WW101");
		    			    	myActT.doMNs();
		    			    	System.err.println("WW102: " + actTablename);
		    					myDB.myRefresh();
		    					//myDB.setTable(myActT);
		    			    	System.err.println("WW103");
		    				}
	    				}
	    			}
			    	System.err.println("WW11");
    				String log = numSuccess + " erfolgreiche Importe.\n";
    				if (!errorMessage.isEmpty()) log += "\n...\n\n" + errorMessage;
	    			if (showResults) {
	    				progress.setValue(0);
	    				progress.setVisible(true);
	    				progress.setString(log);
	    				int initialDelay = 500;
	    				final Timer timer = new Timer();
	    				final String timerLog = log;
	    				TimerTask task = new TimerTask() {
	    				  public void run() {
	    					  progress.setVisible(false);
	    					  timer.cancel();
	    					  InfoBox ib = new InfoBox(timerLog, true, new Dimension(400, 300), null, false);
	  	    				ib.setVisible(true);
	    				  }
	    				};
	    				timer.schedule(task, initialDelay);
	    			}
	    			else {
	    				MyLogger.handleMessage("MyProzessXMLImporter (" + filename + "):\n" + log);
	    			}
    				System.out.println("MyProzessXMLImporter - Fin");
			    }
			    catch (Exception e) {MyLogger.handleException(e);}
	      }
	    };
	    
	    Thread thread = new Thread(runnable);
	    thread.start();
	    try {
	    	System.err.println("WW");
				thread.join();
				System.err.println("QQ");
			}
	    catch (InterruptedException e) {
	    	MyLogger.handleException(e);
			}
	    return "";
	  }
		
		private Vector<Integer> getReihenfolge(Vector<Integer[]> org_dst) {
			Vector<Integer> indexReihenfolge = new Vector<>();
			Integer[] int2 = org_dst.get(0);
			int[] valsA = new int[2];
			int[] valsE = new int[2];
			valsA[0] = -1; valsA[1] = int2[0];
			valsE[0] = -1; valsE[1] = int2[0];
			int anfang = int2[0], ende = int2[0];
			do {				
				valsA[0] = -1; ende = valsE[1];
				findEnde(org_dst, ende, 0, valsA, true);
				valsE[0] = -1; anfang = valsA[1]; 
				findEnde(org_dst, anfang, 0, valsE, false);
			} while (anfang != valsA[1] || ende != valsE[1]);
			
			indexReihenfolge.add(ende);
			int wegPfeile = getReihenfolgePrev(org_dst, indexReihenfolge);
			// jetzt evtl. andere Enden überprüfen!
		    for (int i=0;i<org_dst.size();i++) {
		    	int2 = org_dst.get(i);
		    	int j=0;
			    for (;j<org_dst.size();j++) {
			    	if (int2[1] == org_dst.get(j)[0]) break;
			    }
			    if (j == org_dst.size() && int2[1] != ende) {
			    	System.err.println("NewEnde: " + int2[1]);
					Vector<Integer> indexReihenfolgeNewEnde = new Vector<>();
					indexReihenfolgeNewEnde.add(int2[1]);
					getReihenfolgePrev(org_dst, indexReihenfolgeNewEnde);
					for (j=indexReihenfolgeNewEnde.size()-1;j>=0;j--) {
						int k;
						for (k=indexReihenfolge.size()-1;k>=0;k--) {
							if (indexReihenfolgeNewEnde.get(j) == indexReihenfolge.get(k)) {
								if (k == indexReihenfolge.size()-1) errorMessage += "Ups, 2.Ende... hier ist was schiefgelaufen!\nBitte die Carver Datei (*.pex,*.xml) an Armin senden!\n";
								System.err.println(indexReihenfolgeNewEnde.get(j));
								
								for (int l=1;j+l < indexReihenfolgeNewEnde.size();l++) {
									indexReihenfolge.add(k+l, indexReihenfolgeNewEnde.get(j+l));
								}
								break;
							}
						}
						if (k > 0) break;
					}
					if (j < 0) errorMessage += "Ups, weiteres Ende nicht gefunden... hier ist was schiefgelaufen!\nBitte die Carver Datei (*.pex,*.xml) an Armin senden!\n";
			    }
		    }

			if (DBKernel.debug) MyLogger.handleMessage(indexReihenfolge.size() + "\t" + org_dst.size() + "\t" + wegPfeile);
			if (indexReihenfolge.size() != org_dst.size() + 1 - wegPfeile) {
				errorMessage += "Ups, hier ist was schiefgelaufen!\nBitte die Carver Datei (*.pex,*.xml) an Armin senden!\n";
			}
			return indexReihenfolge;
		}
		private void findEnde(Vector<Integer[]> org_dst, int aktuell, int step, int[] vals, boolean anfang) {			
			Integer[] int2;
			boolean found = false;
		    for (int i=0;i<org_dst.size();i++) {
		    	int2 = org_dst.get(i);
		    	if (int2[anfang?1:0] == aktuell) {
		    		found = true;
		    		findEnde(org_dst, int2[anfang?0:1], step+1, vals, anfang);
		    	}
		    }
		    if (!found && step > vals[0]) {
		    	vals[0] = step; vals[1] = aktuell;
		    }
		}
		private int getReihenfolgePrev(Vector<Integer[]> org_dst, Vector<Integer> indexReihenfolge) {
			int wegPfeile = 0; // Ein Knoten kann mehrere Pfeile haben, die ihn verlassen ..., z.B. Salami_Test_Britta, gleich der erste Knoten "Wareneingang" führt zum einen zu "Speck (Meat, Raw)" zum anderen zu "Schweinefleich , roh (Meat, Raw)"
			if (indexReihenfolge != null) {
				Vector<Vector<Integer>> rhflgn = new Vector<>();
				int firstIndex = indexReihenfolge.get(0);
				Integer[] int2;
			    for (int i=0;i<org_dst.size();i++) {
			    	int2 = org_dst.get(i);
			    	if (firstIndex == int2[1]) {
			    		Vector<Integer> rhflge = new Vector<>();
			    		rhflge.add(int2[0]);
			    		rhflgn.add(rhflge);
			    		wegPfeile += getReihenfolgePrev(org_dst, rhflge);
			    	}
			    }
		    	LinkedHashMap<Integer, String> alreadyAdded = new LinkedHashMap<>();
			    for (int i=0;i<rhflgn.size();i++) {
			    	boolean pfeilAlreadySeen = false;
			    	Vector<Integer> rhflge = rhflgn.get(i);
			    	int offset = 0;
			    	for (int ii=0;ii<i;ii++) {
			    		if (rhflgn.get(ii).size() > rhflge.size()) offset += rhflgn.get(ii).size();
			    	}
			    	for (int j=0;j<rhflge.size();j++) {
			    		if (!alreadyAdded.containsKey(rhflge.get(j))) {
				    		alreadyAdded.put(rhflge.get(j), "");
				    		indexReihenfolge.add(offset + j, rhflge.get(j));	
			    		}
			    		else if (!pfeilAlreadySeen) {
				    		wegPfeile++;
				    		pfeilAlreadySeen = true;
			    			if (DBKernel.debug) System.err.println("Already contained... " + rhflge.get(j));
			    		}
			    		else {
			    			// ist das jetzt korrekt????????
			    			// wenn ich es nicht mache, dann klappen manche Imports von Wese nicht mehr, z.B. Rinderzunge, gepökelt.PEX oder Vollmilch pasteurisiert standardisiert.PEX (Mails vom 20.03.2012)
			    			offset--;
			    		}
			    	}
			    }
			}
			return wegPfeile;
		}
		
		private void cleanReihenfolgeListe(Vector<Integer> indexReihenfolge, LinkedHashMap<Integer, Integer> index_processID, Vector<Integer[]> org_dst) {
		    for (int ii=0;ii<indexReihenfolge.size();ii++) {
		    	Integer key = indexReihenfolge.get(ii);
		    	Integer carverID = index_processID.get(key);
		    	//System.out.println(key + "\t" + carverID + "\t" + isBlacklisted(carverID));
		    	if (isBlacklisted(carverID)) {
			    	for (int i=0;i<org_dst.size();i++) {
				    	Integer[] int2 = org_dst.get(i);
				    	if (int2[1] == indexReihenfolge.get(ii)) {
				    		for (int iii=0;iii<org_dst.size();iii++) {
						    	Integer[] int22 = org_dst.get(iii);
						    	if (int22[0] == int2[1]) {
						    		org_dst.add(new Integer[]{int2[0],int22[1]});
						    	}
				    		}
				    		org_dst.remove(i);
				    		i--;
				    	}
			    	}
		    		indexReihenfolge.remove(ii);
		    		index_processID.remove(key);
		    		ii--;
		    	}
		    }
		}
		private boolean isBlacklisted(int carverID) {
			boolean result = false;
			if (carverID >= 1 && carverID <= 31 || carverID == 332 ||
					carverID >= 217 && carverID <= 220 ||
					carverID >= 295 && carverID <= 307 ||
					carverID >= 338 && carverID <= 340) result = true; // Material
			else if (carverID >= 280 && carverID <= 290 || carverID == 342) result = true; // Verpackung
			//else if (carverID >= 259 && carverID <= 268) result = true; // Transport/Verteilung
			else if (carverID >= 416 && carverID <= 449) result = true; // Transport Gesellschaft
			return result;
		}
}
