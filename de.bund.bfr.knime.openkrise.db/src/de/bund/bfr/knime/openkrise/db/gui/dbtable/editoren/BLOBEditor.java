/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

/**
 * <p>Title: Turniere</p>
 * <p>Description: </p>
 * <p>Company: HSH</p>
 * @author Armin Weiser
 * @version 1.0
 */

import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import java.awt.*;
import java.util.*;

import javax.swing.event.*;




import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;

import java.io.*;

public class BLOBEditor extends JFileChooser implements TableCellEditor {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//static final long serialVersionUID = 8847671986526504938L;
  
  private Vector<CellEditorListener> listeners = new Vector<>();
  private Point screenLoc;
  private MyDBTable table = null;
  private String endungen = "";
  private int selectedColumn;
  
  private EventObject sourceEvent = null;
  private long lastBLOBChosen = 0;

  
  public BLOBEditor(String endungen, MyDBTable table, int selectedColumn) {
    super();
    //System.out.println("BLOBEditor");
    this.table = table;
    
    this.selectedColumn = selectedColumn;
    this.endungen = endungen;
    this.setAcceptAllFileFilterUsed(false);
    this.setMultiSelectionEnabled(false);
    this.addChoosableFileFilter(getImageFilter());
    this.setCurrentDirectory(new File(DBKernel.prefs.get("LAST_OUTPUT_DIR", "")));
    try {
      this.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (DBKernel.debug) System.out.println("lbs1: " + (System.currentTimeMillis() - lastBLOBChosen));
          if (e.getID() == ActionEvent.ACTION_PERFORMED && System.currentTimeMillis() - lastBLOBChosen > 1000) { // eine Sekunde sollte ausreichen, in meinen Tests warens immer ca. 220 ms. Falls nicht: was kann schon passieren? Doppelter Eintrag im DateiSpeicher -> nicht so dramatisch!
        	  lastBLOBChosen = System.currentTimeMillis();
        	  stopCellEditing(); 
          }
        }
      } );
      this.addAncestorListener(new AncestorListener() {
    	  public void ancestorAdded(AncestorEvent e) {
    		  }
    		  public void ancestorRemoved(AncestorEvent e) { // oben rechts das x gedrückt zum Schliessen
    			  if (DBKernel.debug) System.out.println("lbs2: " + (System.currentTimeMillis() - lastBLOBChosen));
    			  if (System.currentTimeMillis() - lastBLOBChosen > 1000) {
                	  lastBLOBChosen = System.currentTimeMillis();
        			  stopCellEditing();     				  
    			  }
    		  }
    		  public void ancestorMoved(AncestorEvent e) {
    		  }
    		  });
    }
    catch(Exception e) {
    	MyLogger.handleException(e);
    }
  }

  private javax.swing.filechooser.FileFilter getImageFilter() {
    //System.out.println("FileFilter");
    return new javax.swing.filechooser.FileFilter () {
      public boolean accept(File f) {
        boolean result = f.isDirectory();
        if (!result) {
          String path = f.getAbsolutePath();
          String token;
          StringTokenizer tok = new StringTokenizer(endungen, ",");
          while (tok.hasMoreTokens()) {
            token = tok.nextToken().trim().substring(1);
            result = result || path.toLowerCase().endsWith(token);
          }
        }
        return result;
      }
      public String getDescription() {
          return endungen;
      }
    };
  }
/*
  private byte[] getBytes() {
    byte[] b = null;
    try {
      if (openPressed && this.getSelectedFile() != null) {
        //System.out.println(this.getSelectedFile());
        RandomAccessFile f = new RandomAccessFile(this.getSelectedFile().getAbsolutePath(), "r");
        if (f.length() > 0) {
          b = new byte[(int) f.length()];
          f.readFully(b);
        }
      }
    }
    catch (Exception e) {MyLogger.handleException(e);}
    return b;
  }
*/
  public Component getTableCellEditorComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               int row,
                                               int column) {
    //System.out.println("getTableCellEditorComponent");
    if (!isSelected) table.setRowSelectionInterval(row, row);
  	String filename = (this.getSelectedFile() == null) ? "" : this.getSelectedFile().getName();
    return new JLabel(filename);
  }
  public Object getCellEditorValue() {
    //System.out.println("getCellEditorValue\t" + table + "\t" + selectedColumn);
    return table.getValueAt(table.getSelectedRow(), selectedColumn);
  }


  public void removeCellEditorListener(CellEditorListener l) {
    //System.out.println("removeCellEditorListener");
    listeners.removeElement(l);
  }
  public void addCellEditorListener(CellEditorListener l) {
    //System.out.println("addCellEditorListener" + l);
    listeners.addElement(l);
    // alle KeyEvents außer Alt+Enter, Strg+Enter, damit soll was anders passieren, nämlich die abgespeicherte Datei geöffnet werden
    if (sourceEvent != null && sourceEvent instanceof KeyEvent &&
    		((KeyEvent) sourceEvent).getKeyChar() != KeyEvent.VK_ENTER) shouldSelectCell(sourceEvent);
  }
  public void cancelCellEditing() {
    //System.out.println("cancelCellEditing");
    if ( isShowing() ) {
        screenLoc = this.getLocationOnScreen();
        this.setVisible( false );
    }

    ChangeEvent ce = new ChangeEvent(this);
    for (int i=listeners.size()-1; i>=0; i--) {
      listeners.elementAt(i).editingCanceled(ce);
    }
  }
  public boolean stopCellEditing() {
    //System.out.println("stopCellEditing");
      ChangeEvent ce = new ChangeEvent(this);
      for (int i=listeners.size()-1; i>=0; i--) {
        listeners.elementAt(i).editingStopped(ce);
      }
      if ( isShowing() ) {
        screenLoc = getLocationOnScreen();
        this.setVisible(false);
      }
    if (table != null && selectedColumn >= 0 && this.getSelectedFile() != null) {
      //byte[] b = getBytes();
      try {
    	  /*
    	  if (table.getActualTable().getTablename().equals("ProzessWorkflow")) {
    		  MyProzessXMLImporter mpi = new MyProzessXMLImporter();
    		  mpi.doImport(this.getSelectedFile().getAbsolutePath(), DBKernel.mainFrame.getProgressBar(), true);
    	  }
    	  else {
    	        int id = Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString());
    	        DBKernel.insertBLOB(table.getActualTable().getTablename(), table.getColumn(selectedColumn).getHeaderValue().toString(), this.getSelectedFile(), id);      	
    	        table.setValueAt(this.getSelectedFile().getName(), table.getSelectedRow(), selectedColumn);    		  
    	        table.save();
    	  }
    	  */
	        int id = Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString());
	        DBKernel.insertBLOB(table.getActualTable().getTablename(), table.getColumn(selectedColumn).getHeaderValue().toString(), this.getSelectedFile(), id);      	
	        table.setValueAt(this.getSelectedFile().getName(), table.getSelectedRow(), selectedColumn);    		  
	        table.save();
      }
      catch (Exception e) {
    	  MyLogger.handleException(e);
      }
    }
    else {
    	//System.out.println("PEOBLEM");
    	table.fireTableDataChanged();
    	//table.selectCell(table.getSelectedRow(), selectedColumn);
    	/*
    	Object o = table.getValueAt(table.getSelectedRow(), selectedColumn);
    	if (o == null) {
        	int id = Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString());
        	table.setValueAt("w", table.getSelectedRow(), selectedColumn);
        	DBKernel.setForeignNullAt(table.getActualTable().getTablename(), table.getColumn(selectedColumn).getHeaderValue().toString(), id);
    	}
    	else {
        	table.setValueAt("W", table.getSelectedRow(), selectedColumn);
        	table.setValueAt(o, table.getSelectedRow(), selectedColumn);    		
    	}
    	*/
    }
    return true;
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    //System.out.println("shouldSelectCell");
    if (screenLoc == null) {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation( (screen.width - getPreferredSize().width) / 2,
                  (screen.height - getPreferredSize().height) / 2);
    }

    this.showOpenDialog(table);
    return true;
  }

  public boolean isCellEditable(EventObject anEvent) {
	  if (anEvent == null) { // null wird übergeben bei selectCell(row, column) in der Funktion setSelectedRowCol in MyDBTable
		  return false;
	  }
	  else {
		  	int row = table.getSelectedRow();
		  	if (anEvent instanceof MouseEvent) {
		  		MouseEvent e = (MouseEvent) anEvent;
		      int val = 0;
		      for (row = 0; row < table.getTable().getRowCount(); row++) {
		      	val += table.getTable().getRowHeight(row);
		      	if (val >= e.getY()) break;
		      }
		  	}
		  	else if (anEvent instanceof KeyEvent) {
		  		KeyEvent keyEvent = (KeyEvent) anEvent;
		  		char ch = keyEvent.getKeyChar();
		  		//System.out.println(ch + "\t" + ( ch == KeyEvent.VK_INSERT) + "\t" + keyEvent.getKeyCode());
		  		if (ch == KeyEvent.VK_DELETE || ch == KeyEvent.VK_ESCAPE || ch == KeyEvent.VK_TAB ||
		  				ch == KeyEvent.VK_INSERT || keyEvent.getKeyCode() == KeyEvent.VK_INSERT) return false; // bei mir: 155 = VK_INSERT
		  	}
		    if (table.isFilledBlobField(row, selectedColumn)) {
		    	table.extractBLOB(row, selectedColumn);
		    	return false;
		    }
		    else {
		      //System.out.println("isCellEditable");
		      sourceEvent = anEvent;
		      this.setSelectedFile(null);
		    return true;
		    }
	  }
  	
  	/*
    MouseEvent e = (MouseEvent) anEvent;

    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 1) {
      return true;
    }
    else {
      return false;
    }
    */
  }
}
