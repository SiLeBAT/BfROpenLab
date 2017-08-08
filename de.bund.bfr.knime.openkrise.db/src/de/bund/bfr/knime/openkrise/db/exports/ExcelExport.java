/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.exports;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.util.Hashtable;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;

/**
 * @author Armin
 *
 */
public class ExcelExport extends FileFilter {
  /**
  This is the one of the methods that is declared in 
  the abstract class
 */
	private Hashtable<String, Integer> kzS = new Hashtable<>();
	private int colLfd = 0;
	private HSSFCellStyle cs = null;
	
	public boolean accept(File f) {
	  if (f.isDirectory()) return true;
	
	  String extension = getExtension(f);
	  if ((extension.equals("xls"))) return true; 
	  return false;
	}
	  
	public String getDescription() {
	    return "Excel Datei (*.xls)";
	}
	
	private String getExtension(File f) {
	  String s = f.getName();
	  int i = s.lastIndexOf('.');
	  if (i > 0 &&  i < s.length() - 1) return s.substring(i+1).toLowerCase();
	  return "";
	}

	public void doExport(final String filename, final MyDBTable myDB, final JProgressBar progress, final boolean exportFulltext, final String zeilen2Do) {
		//filename = "C:/Users/Armin/Documents/private/freelance/BfR/Data/100716/Matrices_BLS-Liste.xls";
  	Runnable runnable = new Runnable() {
      public void run() {
		    try (HSSFWorkbook wb = new HSSFWorkbook()) {
      		if (progress != null) {
      			progress.setVisible(true);
      			progress.setStringPainted(true);
      			progress.setString("Exporting Excel File...");
      			progress.setMinimum(0);
      			progress.setMaximum(myDB.getRowCount());
      			progress.setValue(0);
      		}
		      		
		    	HSSFSheet sheet = wb.createSheet(myDB.getActualTable().getTablename());
		    	// Create Titel
		    	cs = wb.createCellStyle();
		    	cs.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		    	HSSFFont font = wb.createFont();
		    	font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		    	cs.setFont(font);
		    	HSSFRow row0 = sheet.createRow(0); 
		    	//row0.setRowStyle(cs);
		    	colLfd = 0;
		    	for (int j=0;j < myDB.getColumnCount();j++) {
		    		if (myDB.getColumn(j).isVisible()) {
			    		HSSFCell cell = row0.createCell(colLfd); colLfd++;
			    		cell.setCellValue(myDB.getColumn(j).getColumnName()); cell.setCellStyle(cs);	    			
		    		}
		    	}
		    	//String[] mnTable = myDB.getActualTable().getMNTable();
		    	MyTable[] myFs = myDB.getActualTable().getForeignFields();
		    	for (int i = 1; i <= myDB.getRowCount(); i++) {
		    		if (progress != null) progress.setValue(i);
		    		//System.out.println(myDB.getValueAt(i, 0) + "_" + myDB.isVisible());
		    		HSSFRow rowi = sheet.createRow(i); 
		    		for (int j = 0; j < myDB.getColumnCount(); j++) {
		    			Object res = null;
		  	    	if (j>0 && myFs != null && myFs.length > j-1 && myFs[j-1] != null && myFs[j-1].getTablename().equals("DoubleKennzahlen")) {
				    	//if (j > 0 && mnTable != null && j-1 < mnTable.length && mnTable[j - 1] != null && mnTable[j - 1].equals("DBL")) {
				    		getDblVal(myDB, i-1, j, row0, rowi);
				    		/*
				    		getDblVal(myDB, i-1, j, "Einzelwert", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Wiederholungen", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Mittelwert", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Median", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Minimum", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Maximum", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Standardabweichung", row0, rowi);
					    	getDblVal(myDB, i-1, j, "LCL95", row0, rowi);
					    	getDblVal(myDB, i-1, j, "UCL95", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Verteilung", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Funktion (Zeit)", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Funktion (?)", row0, rowi);
					    	getDblVal(myDB, i-1, j, "Undefiniert (n.d.)", row0, rowi);
					    	*/
				    	}	
				    	else {
				    		if (exportFulltext) {
				    			res = myDB.getVisibleCellContent(i-1, j);				    						    			
				    		}
				    		else {
				    			res = myDB.getValueAt(i-1, j);		
				    		}
			    			//MyLogger.handleMessage(res);
					    	if (res != null) rowi.createCell(j).setCellValue(res.toString()); 
					    	else rowi.createCell(j);
				    	}
		    		}
		    	}		    	
		    	
			    try {
			    	FileOutputStream fileOut = new FileOutputStream(filename);
			    	wb.write(fileOut);
			    	fileOut.close();
			    }
			    catch (Exception e) {
		  			JOptionPane.showMessageDialog(progress, e.getMessage(), "Export Problem", JOptionPane.OK_OPTION);
			    }

    			if (progress != null) {
    				progress.setValue(myDB.getRowCount());
    				progress.setVisible(false);
    			}
		    }
		    catch (Exception e) {MyLogger.handleException(e);}
      }
    };
    
    Thread thread = new Thread(runnable);
    thread.start();
  }
	private void getDblVal(MyDBTable myDBTable, int row, int col, HSSFRow row0, HSSFRow rowi) {
		Object key = myDBTable.getValueAt(row, col);
		if (key != null) {
			try {
    		ResultSet rs = DBKernel.getResultSet("SELECT * FROM " + DBKernel.delimitL("DoubleKennzahlen") +
		    		" WHERE " + DBKernel.delimitL("ID") + "=" + key, false);
				if (rs != null && rs.first()) {
					String columnName = myDBTable.getActualTable().getFieldNames()[col-1];
					for (int i=2;i<=rs.getMetaData().getColumnCount();i++) {
						if (rs.getObject(i) != null) {
							if (row0 != null) {
								String kennzahl = rs.getMetaData().getColumnName(i);
								String colStr = columnName + "-" + kennzahl;
								int theCol;
								if (kennzahl.equals("Wert")) {//if (kennzahl.equals("Einzelwert")) {
									theCol = col;
								}
								else if (kzS.containsKey(colStr)) {
									theCol = kzS.get(colStr);
								}
								else {
									theCol = colLfd;
									kzS.put(colStr, theCol);	
							    	HSSFCell cell = row0.createCell(theCol); colLfd++;
							    	cell.setCellValue(colStr); cell.setCellStyle(cs); 
								}
								boolean is = DBKernel.kzIsString(kennzahl);
								boolean ib = DBKernel.kzIsBoolean(kennzahl);
								if (is) {
									rowi.createCell(theCol).setCellValue(rs.getString(i));
								}
								else if (ib) {
									rowi.createCell(theCol).setCellValue(rs.getBoolean(i));
								}
								else {
									rowi.createCell(theCol).setCellValue(DBKernel.getDoubleStr(rs.getObject(i)));
								}
							}
						}
					}
				}
			}
			catch (Exception e) {
				MyLogger.handleException(e);
			}	
		}
	}
}
