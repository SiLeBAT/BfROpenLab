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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

//import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyCellEditorDate;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.BLOBEditor;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyBlobSizeRenderer;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyCheckBoxEditor;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyComboBoxEditor;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyImageCell;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyJavaTypeRenderer;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyLabelRenderer;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyMNRenderer;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyTextareaEditor;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyTextareaRenderer;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.MyTableHeaderCellRenderer;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.MyTableRowModel;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.TableRowHeaderRenderer;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.TableRowHeaderResizer;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyBooleanSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyComboSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyDatetimeSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyDblKZSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyDoubleSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyIntegerSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyLongSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyOtherSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyStringSorter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter.MyTableModel4Sorter;
import quick.dbtable.Column;
import quick.dbtable.DBTable;
import quick.dbtable.Filter;

/**
 * @author Armin
 *
 */
public class MyDBTable extends DBTable implements RowSorterListener, KeyListener, ListSelectionListener, MouseListener  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -129822283276967826L;
	private MyTable actualTable = null;
	public LinkedHashMap<Object, String>[] hashBox = null;
	private MyCellPropertiesModel cpm = new MyCellPropertiesModel(this);
	private MyDBPanel myDBPanel1 = null;
	private Color defaultBgColor = this.getTable().getTableHeader().getBackground();
	private MyTableModel4Sorter sorterModel = null;
	private TableRowSorter<TableModel> sorter = null;
	private MyDBTableErrorListener dberrlis = new MyDBTableErrorListener();
	private boolean bigbigTable = false;
	private Object[][] filterConditions = null;
	private Filter theFilter = null;
	/*
	private Vector<MyMNRenderer> myDblmnr = new Vector<MyMNRenderer>();
	private boolean doEFSA = false;
	*/
			
	public MyDBTable(){
		//this.addFocusListener(this);
	}
	void refreshSort() {
		if (sorter != null) {
			sorterModel.initArray();
			sorter.sort();			
		}
	}
	public boolean initConn(final String username, final String password) {
		Connection conn = null;
		try {
			conn = DBKernel.getDBConnection(username, password);
			if (conn != null) {
				DBKernel.getTempSA(DBKernel.HSHDB_PATH);
				conn = DBKernel.getDBConnection(username, password);
			}
		}
		catch (Exception e) {
			MyLogger.handleException(e);
		}
		return initConn(conn);
	}
	public boolean initConn(final Connection conn) {
		boolean result = false;
		if (DBKernel.mainFrame != null) DBKernel.mainFrame.setTopTable(this);
		if (conn != null) {
			this.setConnection(conn);
			this.setSortEnabled(false);	
			this.autoCommit = true;
			//this.enableExcelCopyPaste(); //Lieber nicht, das gibt nur Probleme mit FremdKeys und so...
			//this.debug = true;
			this.setDBTableLocale(Locale.GERMAN);
			this.getTable().setRowHeight(50);
			
			this.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		    boolean hasMouseListener = false;
		    for (int i=0;i<this.getTable().getMouseListeners().length;i++) {
		    	if (this.getTable().getMouseListeners()[i] instanceof MyDBTable) {
		    		hasMouseListener = true;
		    		break;
		    	}
		    }
		    if (!hasMouseListener) {
				this.getTable().addMouseListener(this);
			}
	
		    boolean hasKeyListener = false;
		    for (int i=0;i<this.getTable().getKeyListeners().length;i++) {
		    	if (this.getTable().getKeyListeners()[i] instanceof MyDBTable) {
		    		hasKeyListener = true;
		    		break;
		    	}
		    }
		    if (!hasKeyListener) {
		    	removeExternalKeystrokes();
		    	this.addKeyListener(this);
		    }

		    if (!hasMouseListener && !hasKeyListener) {			    
			    this.getTable().getColumnModel().getSelectionModel().addListSelectionListener(this);
			    this.getTable().getSelectionModel().addListSelectionListener(this);		    		    
		    }
		    
		    //this.doNotUseDatabaseSort = true;
			
			result = true;
		}
		return result;
	}
	private void removeExternalKeystrokes() {
		// Ich will ale Keys lieber selbst im Griff haben, also weg:
		// Java vergibt da einige Default InputMaps!!!
		// siehe hier: http://download.oracle.com/javase/1.4.2/docs/api/javax/swing/doc-files/Key-Metal.html
		
		// Quicktable
		this.getTable().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0));
		this.getTable().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
		this.getTable().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
		this.getTable().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
		this.getTable().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));	
	}
	public MyCellPropertiesModel getMyCellPropertiesModel() {
		return cpm;
	}

	public boolean setTable() {
		if (actualTable != null) {
			return setTable(actualTable);
		} else {
			return false;
		}
	}
	public boolean setTable(final MyTable myT) {
		return setTable(myT, filterConditions); // null
	}
	public boolean setTable(final MyTable myT, final Object[][] conditions) {
		return setTable(myT, conditions, "AND");
	}
	  @SuppressWarnings("unchecked")
	public boolean setTable(final MyTable myT, final Object[][] conditions, String andOrDefault) {
		boolean result = true;
		if (DBKernel.mainFrame != null) {
			DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		bigbigTable = false; //myT.getTablename().equals("ChangeLog") || myT.getTablename().equals("Messwerte") || myT.getTablename().equals("Versuchsbedingungen"); // false;
		checkUnsavedStuff();
		actualTable = myT;
		if (this.getMyCellPropertiesModel() instanceof MyCellPropertiesModel) {
			(this.getMyCellPropertiesModel()).getModifiedCellsColl().clear();
		}
		this.filterConditions = conditions;
		String where = "";
		String order = "";
		if (conditions != null) {
			where = "WHERE ";
			for (int i=0;i<conditions.length;i++) {
				if (i>0) {
					where += " " + andOrDefault + " ";
				}
				where += DBKernel.delimitL(conditions[i][0].toString()) + (conditions[i][1] == null ? " IS NULL" : "=" + conditions[i][1]) + (conditions[i].length > 2 && conditions[i][2] != null ? conditions[i][2] : "");
			}	
			order = " ORDER BY " + DBKernel.delimitL("ID") + " ASC";	
			/*
			if (conditions[0][0].equals("Zielprozess")) {
				ResultSet rs = DBKernel.getResultSet("SELECT " + DBKernel.delimitL("Ausgangsprozess") + " FROM " +
						DBKernel.delimitL("Prozess_Verbindungen") +	" " + where, false);
				where = "";
				try {
					if (rs != null && rs.first()) {
						do {
							if (rs.getObject("Ausgangsprozess") != null) {
								where += " OR " + DBKernel.delimitL("Prozessdaten") + "=" + rs.getInt("Ausgangsprozess");
							}
						} while (rs.next());
						if (where.length() > 2) {
							where = " WHERE (" + where.substring(3) + ") AND " + DBKernel.delimitL("Zutat_Produkt") + "='Produkt'";
						}
					}
				}
				catch (Exception e) {MyLogger.handleException(e); where = "";}
				if (where.length() == 0) {
					where = " WHERE 1=0";
				}
			}
			*/
		}
		//if (DBKernel.debug) System.out.println(myT.getMetadata() + "\n" + where + order);
		if (actualTable.getForeignFields() != null) {
			hashBox = new LinkedHashMap[actualTable.getForeignFields().length];
		} else {hashBox = null;}
		this.clearAllSettings();
		cpm.setFoundVector(null);
		if (myT.getTablename().equals("ChangeLog")) {
			Integer lastID = DBKernel.getLastID("ChangeLog");
			if (lastID != null) where = " WHERE " + DBKernel.delimitL("ID") + " > " + (lastID - 1000) + " "; // 230000
		}
		this.setSelectSql(myT.getSelectSQL() + " " + where + order);
		this.setCellPropertiesModel(cpm);
		//long ttt = System.currentTimeMillis();
		try {
			this.createColumnModelFromQuery();
			//if (DBKernel.debug) {System.out.println("createColumnModelFromQuery: " + (System.currentTimeMillis() - ttt));ttt = System.currentTimeMillis();} 
			if (!myT.isReadOnly() && !this.getConnection().isReadOnly()) {this.setEditable(true);} //  && where.trim().length() == 0
			prepareColumns();
			//if (DBKernel.debug) {System.out.println("prepareColumns: " + (System.currentTimeMillis() - ttt)); }
			if (myT.isReadOnly() || this.getConnection().isReadOnly()) {this.setEditable(false);} //  || where.trim().length() > 0
			this.addDatabaseChangeListener(new MyDataChangeListener(this));
		    this.addDBTableErrorListener(dberrlis);
			this.addTableCellListener(new MyTableCellListener(this));
			this.addUpdateSql(myT.getUpdateSQL1(), myT.getUpdateSQL2());
			this.addInsertSql(myT.getInsertSQL1(), myT.getInsertSQL2());
			this.addDeleteSql(myT.getDeleteSQL1(), myT.getDeleteSQL2());
			this.setRowCountSql(myT.getRowCountSQL() + " " + where);
			//this.getTable().getTableHeader().addMouseListener(new ColumnFitAdapter());
			//this.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			this.refresh();
			//if (DBKernel.debug) {System.out.println("refresh: " + (System.currentTimeMillis() - ttt));ttt = System.currentTimeMillis();}
			if (!bigbigTable) {
				if (sorterModel != null) {
					//sorterModel.initArray();
				}
			}
			//if (DBKernel.debug) {System.out.println("sorterModel: " + (System.currentTimeMillis() - ttt)); ttt = System.currentTimeMillis();}
			//AutoFitTableColumns.autoResizeTable(this.getTable(), true, false);
			//this.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
			/*
			if (myT.isFirstTime()) {
		    this.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		    TableColumnAdjuster tca = new TableColumnAdjuster(this.getTable());
				tca.setOnlyAdjustLarger(true); tca.setColumnDataIncluded(false);
				tca.setColumnHeaderIncluded(true); tca.setDynamicAdjustment(false);
				tca.adjustColumns();
			}
			*/
		}
	  	catch (Exception e) {
	  		result = false;
	  		MyLogger.handleException(e);
		}
		if (myDBPanel1 != null) {
			myDBPanel1.getSuchfeld().setText("");
			myDBPanel1.handleSuchfeldChange(null);
		}
	  	updateRowHeader(!bigbigTable);
		// sortieren nach ID und damit nach Zeitstempel, das neueste zuoberst!
		// Das gilt erstmal für die beiden ReadOnly Tabellen: ChangeLog und DateiSpeicher
		if (sorter != null) {
			if (myT.isReadOnly()) {
				List<SortKey> sortKeys = new ArrayList<>();
		  		sortKeys.add(new SortKey(1, SortOrder.DESCENDING));
		  		sorter.setSortKeys(sortKeys);
		  		sorter.sort();
				//this.sortByColumn(1, false);
			} 
			/*
			else if (myT.getTablename().equals("ComBaseImport")) { // nur temporär, kann irgendwann wieder weg
				List<SortKey> sortKeys = new ArrayList<SortKey>();
		  		sortKeys.add(new SortKey(3, SortOrder.DESCENDING));
		  		sorter.setSortKeys(sortKeys);
		  		sorter.sort();
			}
			*/
		}
		if (!bigbigTable) {actualTable.restoreProperties(this); syncTableRowHeights();}			
		//if (DBKernel.debug) {System.out.println("syncTableRowHeights: " + (System.currentTimeMillis() - ttt));}
		if (DBKernel.mainFrame != null) {
			DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		if (myDBPanel1 != null && this.getRowCount() > 0) {
			myDBPanel1.getSpinner().setValue(this.getTable().getRowHeight(0));
		}
		
		addScrollerListeners4Rendering();
		return result;
	}
	public void setMyDBPanel(final MyDBPanel myDBPanel) {
		myDBPanel1 = myDBPanel;
	}
	public MyDBPanel getMyDBPanel() {
		return myDBPanel1;
	}
	public MyTable getActualTable() {
		return actualTable;
	}

	public String getVisibleCellContent(final int row, final int col) {
		String result = null;
	    MyTable[] foreignFields = actualTable.getForeignFields();
	    if (row >= this.getRowCount()) {
			return "";
		}
	    Object o = this.getValueAt(row, col);
	    if (o != null) { // sonst Fehler in z.B. Methoden bei der Suchfunktion
	  		if (col > 0 && foreignFields != null && foreignFields.length > col-1 && foreignFields[col-1] != null && hashBox[col-1] != null) {
	  			result = hashBox[col-1].get(o).toString();
	  		}
	  		else {
	  			result = o.toString();
	  		}
	    }
		return result;
		
		/*
		 Alternativ könnte folgender Code funktionieren. Der würde nicht so viel Speicher verbrauchen (hashBox)
		        c = this.getTable().getCellRenderer(i, j).getTableCellRendererComponent(this.getTable(), this.getValueAt(i, j), false, false, i, j);
		        if (c instanceof JLabel) {
		          result[i][j - beginCol] = ( (JLabel) c).getText();
		        }
		        else {
		          result[i][j - beginCol] = this.getValueAt(i, j);
		        }

		 */
	}
	void insertNull(final int selRow, final int selCol) {
		if (!this.actualTable.isReadOnly() && selCol > 0 && selRow >= 0 && this.getRowCount() > 0) {
    	String[] mnTable = actualTable.getMNTable();
    	MyTable[] myFs = actualTable.getForeignFields();
	    	if (mnTable != null && mnTable.length > selCol - 1 && mnTable[selCol - 1] != null) {
	    		String sql = "";
				if (mnTable[selCol - 1].equals("INT")) {
					sql = "";				// todo - oder auch nicht... Lieber nicht löschen! Is gut so! Wenn da was gelöscht werden soll, dann sollte das Fremdfenster geöffnet werden und dort die Zeile (Beispiel: Zutat) gelöscht werden!!!
				}
				else {
					MyTable myMNTable = DBKernel.myDBi.getTable(mnTable[selCol - 1]);
					/*
					System.err.println(tablename + "\t" + mnTable[selCol - 1] + "\t" + myMNTable.getForeignFieldName(this.getActualTable()));
					String tablename = this.getActualTable().getTablename();
					if (tablename.equals("Modellkatalog")) sql = "DELETE FROM " + DBKernel.delimitL(mnTable[selCol - 1]) + " WHERE " + DBKernel.delimitL("Modell") + "=" + this.getValueAt(selRow, 0);
					else if (tablename.equals("GeschaetzteModelle")) sql = "DELETE FROM " + DBKernel.delimitL(mnTable[selCol - 1]) + " WHERE " + DBKernel.delimitL("GeschaetztesModell") + "=" + this.getValueAt(selRow, 0);
					else sql = "DELETE FROM " + DBKernel.delimitL(mnTable[selCol - 1]) + " WHERE " + DBKernel.delimitL(tablename) + "=" + this.getValueAt(selRow, 0);									
					*/
					sql = "DELETE FROM " + DBKernel.delimitL(mnTable[selCol - 1]) + " WHERE " + DBKernel.delimitL(myMNTable.getForeignFieldName(this.getActualTable())) + "=" + this.getValueAt(selRow, 0);
				}
				if (sql.length() > 0) {
					DBKernel.sendRequest(sql, false);
				}
			}
	    	else if (myFs != null && myFs.length > selCol - 1 && myFs[selCol - 1] != null && myFs[selCol - 1].getTablename().equals("DoubleKennzahlen")) {
	    		DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("DoubleKennzahlen") +
	    				" WHERE " + DBKernel.delimitL("ID") + "=" + this.getValueAt(selRow, selCol), false);
	    	}
			else {
				DBKernel.setForeignNullAt(this.getActualTable().getTablename(), this.getColumn(selCol).getHeaderValue().toString(), this.getValueAt(selRow, 0));				
			}
			//this.getActualTable().saveProperties(this);
	    	int selID = this.getSelectedID();
			//System.err.println("id "+selID);
			myRefresh(selRow, selCol);
			int newSelID = this.getSelectedID();
			if (newSelID != selID) {
				//System.err.println("id "+selID + "\t" + newSelID);
				this.setSelectedID(selID);
			}
			//this.getActualTable().restoreProperties(this);
		}
	}
	void deleteRow() {
		String tablename = this.getActualTable().getTablename();
		int selRow = this.getSelectedRow();
		if (!this.actualTable.isReadOnly() && this.getRowCount() > 0 && selRow >= 0 && selRow < this.getRowCount()) {
			int id = this.getSelectedID();
			List<String> fkids = DBKernel.getUsageListOfID(tablename, id);
			int numForeignCounts = fkids.size();//DBKernel.getUsagecountOfID(tablename, id);
			if (numForeignCounts > 0) {
				String info = "Please delete referencing data sets first.\n" + "numForeignCounts=" + numForeignCounts + " for Table " + tablename + ", ID " + id + "\n";
				for (String str : fkids) info += "\n" + str;
    			InfoBox ib = new InfoBox(info, true, new Dimension(700, 500), null, true);
    			ib.setVisible(true);    				    			
			}
			else {
				this.getActualTable().saveProperties(this);
				
				if (this.getTable().getRowSorter() != null && this.getTable().getRowSorter().getSortKeys().size() > 0) {
					try {this.delete(new int[]{this.getTable().convertRowIndexToModel(selRow)});}
					catch (Exception e1) {System.err.println("strangeDeleteRowBehaviour: " + e1.getMessage());}
				}
				else {
					this.delete();				
				}
					
				this.save();
				this.myRefresh(selRow);
				if (myDBPanel1 != null) {
					myDBPanel1.refreshTree();
				} 
				this.getActualTable().restoreProperties(this); this.syncTableRowHeights();
			}			
		}
	}

	void insertNewRow(final boolean copySelected, final Vector<Object> vecIn) {
		MyTable myT = this.getActualTable();
		if (!this.actualTable.isReadOnly()) {
			//JScrollPane scroller = getScroller();
			this.getActualTable().saveProperties(this);
			// Filter und Sorter ausschalten!!!
			String filterText = "";
			boolean ichChecked = false;
			if (myDBPanel1 != null) {
				if (sorter != null) {
					sorter.setSortKeys(null);
					//sorter.sort();
				}
				filterText = myDBPanel1.getSuchfeld().getText();
				if (filterText.trim().length() > 0) {
					myDBPanel1.getSuchfeld().setText("");
				}
				ichChecked = myDBPanel1.getSuchIchCheckBox().isSelected();
				if (ichChecked) {
					myDBPanel1.getSuchIchCheckBox().setSelected(false);
				}
				myDBPanel1.handleSuchfeldChange(null);
			}
			Object oldID = null;
			if (copySelected) {
				Vector<Object> vec = new Vector<>();
				int row = this.getSelectedRow();
				if (row < 0) {
					return;
				}
				try {
					oldID = this.getValueAt(row, 0);					
				}
				catch (Exception e) {MyLogger.handleException(e);System.err.println("oldID = (Integer) this.getValueAt(row, 0)\t" + row);}
				for (int i=0;i<this.getColumnCount();i++) {
					String colName = this.getColumn(i).getColumnName();
					if (colName.equals("ID") || colName.equals("Geprueft")) {
						vec.add(null); // colName.equals("Guetescore") || colName.equals("Kommentar") || 
					} else {
						vec.add(this.getValueAt(row, i));
					}
				}
				this.insert(vec);
			}
			else if (vecIn != null) {
				this.insert(vecIn);
			}
			else {
				this.insertEmptyRecord();
			}
			// Hier muss man höllisch aufpassen, dass auch bei geschalteten Filtern und Sortern die korrekte ID bzw. Row rauskommt!!!!
			int newSelRow = this.getSelectedRow();
			if (filterConditions != null) {
				for (int i=0;i<filterConditions.length;i++) {
					if (filterConditions[i][1] != null) {
						for (int j=0;j<this.getColumnCount();j++) {
							if (this.getColumn(j).getColumnName().equals(filterConditions[i][0])) {
								this.setValueAt(filterConditions[i][1], newSelRow, j);
								break;
							}
						}						
					}
				}
			}
			this.save();
			Object newID = this.getValueAt(newSelRow, 0);
			if (myDBPanel1 != null) {
				if (ichChecked) {
					myDBPanel1.getSuchIchCheckBox().setSelected(true);
				}
				if (filterText.trim().length() > 0) {
					myDBPanel1.getSuchfeld().setText(filterText);
				}
				myDBPanel1.handleSuchfeldChange(null);
				//this.updateRowHeader(!bigbigTable);
			}
			if (copySelected) {
				if (newID != null && oldID != null) {
					copyDetails(myT, (Integer) oldID, (Integer) newID);
				} else {
					MyLogger.handleMessage("id != null && oldID != null " + newID + "\t" + oldID);
				}
			}
			//System.out.println(id);
			this.myRefresh(this.getRowCount()-1);
  		// evtl. HashBox neu setzen, sonst wird nicht refresht
  		MyTable[] foreignFields = actualTable.getForeignFields();
  		String[] mnTable = actualTable.getMNTable();
  		if (foreignFields != null) {
			DBKernel.refreshHashTables();
  			for (int i=0;i<foreignFields.length;i++) {
    			if (foreignFields[i] != null && (mnTable == null || mnTable[i] == null)) {
    				hashBox[i] = DBKernel.fillHashtable(foreignFields[i], "", "\n", "\n", !bigbigTable); //" | " " ; "
    				Column c = this.getColumn(i+1); 
    				c.setUserCellRenderer(new MyComboBoxEditor(hashBox[i], true));
    			}
  			}
  		}
			this.getActualTable().restoreProperties(this); this.syncTableRowHeights();
			if (newID instanceof Integer) {
				this.setSelectedID((Integer)newID);
			} else {
				this.selectCell(this.getRowCount()-1, this.getSelectedColumn());
			}
		}
		adjustColumns();
	}
	void refreshHashbox() {
  		MyTable[] foreignFields = actualTable.getForeignFields();
  		String[] mnTable = actualTable.getMNTable();
  		if (foreignFields != null) {
			DBKernel.refreshHashTables();
  			for (int i=0;i<foreignFields.length;i++) {
    			if (foreignFields[i] != null && (mnTable == null || mnTable[i] == null)) {
    				hashBox[i] = DBKernel.fillHashtable(foreignFields[i], "", "\n", "\n", !bigbigTable); //" | " " ; "
    				Column c = this.getColumn(i+1); 
    				c.setUserCellRenderer(new MyComboBoxEditor(hashBox[i], true));
    			}
  			}
  		}		
	}
	private void copyDetails(final MyTable myT, final Integer oldID, final Integer id) {
		try {
			String tablename = myT.getTablename();
			copyKennzahlen(myT, oldID, id);
		    
		    String[] mnTable = myT.getMNTable();
		    if (mnTable != null) {
			    for (int i=0;i<mnTable.length;i++) {
			    	if (mnTable[i] != null) {
			    		if (mnTable[i].equals("INT") && myT.getForeignFields()[i] != null) { // z.B. Messwerte auch kopieren
			    			String fTablename = myT.getForeignFields()[i].getTablename();
			    			String tname = fTablename.startsWith("Codes_") ? "Basis" : tablename;
			    			MyTable[] ffTs = myT.getForeignFields()[i].getForeignFields();
			    			int ii=0;
			    			for (MyTable myFT : ffTs) {
			    				if (myFT != null && myFT.getTablename().equals(tablename)) {
			    					tname = myT.getForeignFields()[i].getFieldNames()[ii];
			    					break;
			    				}
			    				ii++;
			    			}
				    		ResultSet rs = DBKernel.getResultSet("SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(fTablename) +
						    		" WHERE " + DBKernel.delimitL(tname) + "=" + oldID + " ORDER BY " + DBKernel.delimitL("ID") + " ASC", false);
						    if (rs != null && rs.first()) {
						    	do  {
						    		Integer oldfID = rs.getInt("ID");
						    		MyTable myfT = DBKernel.myDBi.getTable(fTablename);
						    		System.out.println(tablename + "-" + fTablename + " - oldfID: " + oldfID + "\toldID = " + oldID);
						    		ResultSet rs2 = DBKernel.getResultSet("SELECT * FROM " + DBKernel.delimitL(fTablename) + " WHERE " + DBKernel.delimitL("ID") + "=" + oldfID, false);
						    		if (rs2 != null && rs2.first()) {
								    	do  {
								    		Integer newfID = copyRow(rs2, fTablename, tname, id);								    		
								    		System.out.println(tablename + "-" + fTablename + " - newfID: " + newfID + "\tparentID = " + id + "\ttname = " + tname);
								    		myfT.doMNs();
								    		copyDetails(myfT, oldfID, newfID);
								    	} while (rs2.next());
						    		}
						    		else {
						    			System.err.println("Urrghhhh, wasn denn nu los???? " + oldfID + "\t" + fTablename);
						    		}
						    	} while (rs.next());
						    }
						}					    		
			    		else {//if (!mnTable[i].equals("DBL")) { // wurde ja schon bei copyKennzahlen gemacht, oder?
				    		MyTable myMNT = DBKernel.myDBi.getTable(mnTable[i]);
				    		String sql = "SELECT * FROM " + DBKernel.delimitL(mnTable[i]) + " WHERE " +
				    				DBKernel.delimitL(DBKernel.delimitL(myMNT.getForeignFieldName(this.getActualTable()))) + "=" + oldID;
				    		/*
				    		if (tablename.equals("GeschaetzteModelle")) {
				    			sql += DBKernel.delimitL("GeschaetztesModell");
				    		}
				    		else {
				    			sql += DBKernel.delimitL(tablename);
				    		}
				    		*/
				    		ResultSet rs = DBKernel.getResultSet(sql, false);
				    		//System.err.println(mnTable[i] + "\t" + tablename + "\t" + myMNT.getFieldNames()[0] + "\t" + myMNT.getFieldNames()[1]);
						    if (rs != null && rs.first()) {
						    	do  {
						    		Integer newID = copyRow(rs, mnTable[i], tablename, id);
								    copyKennzahlen(myMNT, rs.getInt(1), newID);
						    	} while (rs.next());
						    }
				    	}
			    	}
			    }				    	
		    }
		}
		catch (Exception e) {MyLogger.handleException(e);}		
	}
	private Integer copyRow(final ResultSet rs, final String tablename, final String parentTable, final Integer parentID) {
		Integer result = null;
		try {
			String columns = "";
			String vals = "";
			for (j=2;j<=rs.getMetaData().getColumnCount();j++) {
				if (rs.getObject(j) != null) {
	    			columns += "," + DBKernel.delimitL(rs.getMetaData().getColumnName(j));
	    			String fname = rs.getMetaData().getColumnName(j);
	    			if (fname.equals(parentTable)) {
						vals += ",'" + parentID + "'";
					} else {
						vals += ",'" + rs.getString(j) + "'";
					}		
				}
			}
			if (columns.length() > 0) {
				columns = columns.substring(1);
				vals = vals.substring(1);
				PreparedStatement psmt = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL(tablename) +
	    				" (" + columns + ") VALUES (" + vals + ")", Statement.RETURN_GENERATED_KEYS);
				if (psmt.executeUpdate() > 0) {
					result = DBKernel.getLastInsertedID(psmt);
				}
			}		
		}
		catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	private void copyKennzahlen(final MyTable myT, final Integer oldID, final Integer newID) throws Exception {
		copyKennzahlen(myT, oldID, newID, null);
	}
	private void copyKennzahlen(final MyTable myT, final Integer oldID, final Integer newID, final Integer toRow) throws Exception {
		MyTable[] foreignFields = myT.getForeignFields();
		if (foreignFields != null) {
			for (int i=0; i<foreignFields.length; i++) {
				if (foreignFields[i] != null && foreignFields[i].getTablename().equals("DoubleKennzahlen")) {
					ResultSet rs = DBKernel.getResultSet("SELECT " + DBKernel.delimitL(myT.getFieldNames()[i]) +
							" FROM " + DBKernel.delimitL(myT.getTablename()) +
			    		" WHERE " + DBKernel.delimitL("ID") + "=" + oldID, false);
				    if (rs != null && rs.first()) {
				    	if (rs.getObject(1) != null) {
				    		Integer oldfID = rs.getInt(1);
				    		ResultSet rs2 = DBKernel.getResultSet("SELECT * FROM " + DBKernel.delimitL("DoubleKennzahlen") +
						    		" WHERE " + DBKernel.delimitL("ID") + "=" + oldfID, false);
				    		if (rs2 != null && rs2.first()) {
						    		Integer newfID = copyRow(rs2, "DoubleKennzahlen", "", null);
						    		/*
						    		System.err.println("UPDATE " + DBKernel.delimitL(myT.getTablename()) +
						    				" SET " + DBKernel.delimitL(myT.getFieldNames()[i]) + "=" + newfID +
						    				" WHERE " + DBKernel.delimitL("ID") + "=" + newID);
						    		*/
						    		DBKernel.sendRequest("UPDATE " + DBKernel.delimitL(myT.getTablename()) +
						    				" SET " + DBKernel.delimitL(myT.getFieldNames()[i]) + "=" + newfID +
						    				" WHERE " + DBKernel.delimitL("ID") + "=" + newID, false);
						    		// Es ist hier essentiell, dass im DBTable der Wert gesetzt wird, weil sonst ein späterer "myRefresh" dafür sorgt, dass die DB Einträge wieder mit NULL überschrieben werden
						    		// Alternativ könnte man this.save(); an der richtigen Stelle setzen?!?
						    		if (toRow != null && actualTable.getTablename().equals(myT.getTablename())) {
										this.setValueAt(newfID, toRow, i+1);
									}
				    		}
				    		else {
								MyLogger.handleMessage("aaahhUrrghhhh, wasn denn nu los???? " + oldfID + "\t" + "DoubleKennzahlen");
				    		}
				    	}
				    }
				}
			}
		}
	}
	
	private void myPrint() {
	  	// Zuerst DBTable aktualisieren:
	    try {this.refresh();}
	    catch (SQLException e1) {MyLogger.handleException(e1);}
	    /*
	    FormatTable ft = FormatGeneral.getDefaultSettings(java.util.Locale.GERMAN).getFormatTable();
	    FormatGeneral fg = FormatGeneral.getDefaultSettings(java.util.Locale.GERMAN);
	    ft.addDedicatedRow(new DedicatedRow(0, Color.WHITE, new Font("Dialog", Font.PLAIN, 11), Color.BLACK)); // einen Default setzen, sonst Fehler von KBTablePrinter
	    fg.setFormatTable(ft);
        KBTablePrinter.showPreviewFrame(getColumnTitles(), getVisibleData(), fg, false, false, false);
        */
	    this.printPreview();		
	}
	/*
	private Object[][] getVisibleData() {
		Object[][] result = new Object[this.getRowCount()][this.getColumnCount()];
		int i, j;
		for (i = 0; i < this.getRowCount(); i++) {
			for (j = 0; j < this.getColumnCount(); j++) {
				result [i][j] = this.getVisibleCellContent(i, j);
		    }
		}
		return result;
	}
	private String[] getColumnTitles() {
		String[] result = new String[this.getColumnCount()];
		for (int j = 0; j < this.getColumnCount(); j++) {
		      result[j] = this.getColumn(j).getHeaderValue().toString();
		}
		return result;
	}
	*/
	public void myRefresh() {
		myRefresh(this.getSelectedRow());
	}
	void myRefresh(final int row) {
		if (!bigbigTable)
		 {
			myRefresh(row, this.getSelectedColumn()); // bei bigbigTable muss erstmal nicht nen autoupdate nach afterUpdate von MyDataChangeListener gemacht werden.. is sonst zu lahm
		}
	}
	
	public void setFilter(Filter mf) {
		theFilter = mf;
    	if (theFilter != null) {
    		this.filter(theFilter);
    		//this.setReadOnly(true);
    	}
	}
    	
	private void myRefresh(final int row, final int col) {
		JScrollPane scroller = getScroller();
		int scrollVal = (scroller == null) ? -1 : scroller.getVerticalScrollBar().getValue();
		int hscrollVal = (scroller == null) ? -1 : scroller.getHorizontalScrollBar().getValue();
//		System.err.println(row+" - "+getSelectedID());
		int id = getSelectedID();
	    try {
	    	this.refresh();
	    	
	    	if (theFilter != null) {
	    		this.filter(theFilter);
	    		//this.setReadOnly(true);
	    	}
	    	
	    }
	    catch (Exception e1) {
	    	MyLogger.handleException(e1);
	    }
		if (myDBPanel1 != null) {
			myDBPanel1.handleSuchfeldChange(null);
		}
	    this.updateRowHeader(!bigbigTable);
	    if (col >= 0 && col < this.getColumnCount()) {
			this.getTable().setColumnSelectionInterval(col, col);
		}
	    
	    if (row >= 0 && row < this.getRowCount()) {
	    	this.setRowSelectionInterval(row, row);
	    	this.goTo(row);
	    }
	    setSelectedID(id);

	    if (sorterModel != null) {
			sorterModel.initArray();
		}
	    if (scrollVal >= 0) {
			this.getScroller().getVerticalScrollBar().setValue(scrollVal);
		}
	    if (hscrollVal >= 0) {
			this.getScroller().getHorizontalScrollBar().setValue(hscrollVal);
		}
		this.getTable().requestFocus();
	}
	private void initSorter() {
		// Sorter initialisieren
		if (actualTable.isReadOnly()) {
			sorterModel = new MyTableModel4Sorter(this); 
			sorter = new TableRowSorter<TableModel>(sorterModel); //this.getTable().getModel());//new MyTableModel4Sorter(this)); //
			sorter.setMaxSortKeys(1); // eins genügt wohl
			sorter.addRowSorterListener(this);
			sorter.setSortsOnUpdates(false); // lieber nicht, danach ist alles immer so unübersichtlich.
			this.getTable().setRowSorter(sorter);      
      			
			sorter.setComparator(1, new MyIntegerSorter()); // ID
		}
		else {
			sorterModel = null;
			sorter = null;
			this.getTable().setRowSorter(null);
		}
      		
	}
	private void prepareColumns() {
		Column c = this.getColumn(0);
		c.setReadOnly(true);

		if (actualTable != null) {
			this.getTable().getTableHeader().setReorderingAllowed(false);

			initSorter();

			TableColumnModel tcm = this.getTable().getTableHeader().getColumnModel();
			tcm.getColumn(0).setHeaderRenderer(new MyTableHeaderCellRenderer(this, defaultBgColor, null));
			String[] fieldTypes = actualTable.getFieldTypes();
			String[] fieldComments = actualTable.getFieldComments();
			if (fieldTypes != null) {
				ResourceBundle bundle = ResourceBundle.getBundle("de.bund.bfr.knime.openkrise.db.gui.PanelProps_" + DBKernel.getLanguage());
				for (int i=0; i<fieldTypes.length; i++) {
					MyTableHeaderCellRenderer mthcr = null;
					c = this.getColumn(i+1);
					c.setReadOnly(false);
					if (fieldTypes[i].equals("OTHER")) {
				      c.setUserCellRenderer(new MyJavaTypeRenderer());
				      mthcr = new MyTableHeaderCellRenderer(this, defaultBgColor, fieldComments[i]);
					    tcm.getColumn(i+1).setHeaderRenderer(mthcr);
					    if (sorter != null) {
							sorter.setComparator(i+2, new MyOtherSorter());
						}
				  }
				else if (fieldTypes[i].equals("BOOLEAN")) {
			      c.setUserCellEditor(new MyCheckBoxEditor(bundle.getString("Haekchen vorhanden = JA"), this, false));
			      c.setUserCellRenderer(new MyCheckBoxEditor(bundle.getString("Haekchen vorhanden = JA"), this, false));
			      mthcr = new MyTableHeaderCellRenderer(this, defaultBgColor, fieldComments[i]);
				    tcm.getColumn(i+1).setHeaderRenderer(mthcr);
				    if (sorter != null) {
						sorter.setComparator(i+2, new MyBooleanSorter());
					}
				  }
					else if (fieldTypes[i].startsWith("BLOB(")) {
						if (actualTable.getTablename().equals("DateiSpeicher")) {
							c.setVisible(false);
						}
						else {
					    	String endungen = (actualTable.getDefaults() != null && actualTable.getDefaults()[i] != null ? actualTable.getDefaults()[i] : "*"); // "*.pdf, *.doc"
					    	/*
					    	if (this.getActualTable().getTablename().equals("ProzessWorkflow")) {
								endungen = "*.xml";
							}
					    	else if (this.getActualTable().getTablename().equals("PMMLabWorkflows")) {
								endungen = "*.zip";
							}
							*/
					    	BLOBEditor be = new BLOBEditor(endungen, this, i+1);
						    c.setCellEditor(be);				    	
						    if (sorter != null) {
								sorter.setComparator(i+2, new MyStringSorter());
							}
					    }
					    mthcr = new MyTableHeaderCellRenderer(this, Color.GRAY, fieldComments[i]);
					    tcm.getColumn(i+1).setHeaderRenderer(mthcr);
					  }
					else if (fieldTypes[i].startsWith("VARCHAR(")) {
						c.setUserCellEditor(new MyTextareaEditor(this, actualTable, i));							
						c.setUserCellRenderer(new MyTextareaRenderer());
						mthcr = new MyTableHeaderCellRenderer(this, defaultBgColor, fieldComments[i]);
					    tcm.getColumn(i+1).setHeaderRenderer(mthcr);
					    if (sorter != null) {
							sorter.setComparator(i+2, new MyStringSorter());
						}
					}
					else if (actualTable.getFieldNames()[i].equals("Dateigroesse")) { // Dateigroesse  && actualTable.getTablename().equals("DateiSpeicher")
				      c.setUserCellRenderer(new MyBlobSizeRenderer());
				      mthcr = new MyTableHeaderCellRenderer(this, defaultBgColor, fieldComments[i]);
					    tcm.getColumn(i+1).setHeaderRenderer(mthcr);
					    if (sorter != null) {
							sorter.setComparator(i+2, new MyIntegerSorter());
						}
					}
					else if (fieldTypes[i].startsWith("DATE")) { // DATE, DATETIME
					    c.setUserCellRenderer(new MyImageCell(fieldTypes[i].equals("DATETIME") ? MyImageCell.DATETIME : MyImageCell.DATE));
					    //c.setUserCellEditor(new MyCellEditorDate());
						mthcr = new MyTableHeaderCellRenderer(this, defaultBgColor, fieldComments[i]); tcm.getColumn(i+1).setHeaderRenderer(mthcr);
						if (sorter != null) {
							sorter.setComparator(i+2, new MyDatetimeSorter());
						}
					}
					else { // INTEGER, DOUBLE
						c.setUserCellRenderer(new MyLabelRenderer());
						String tooltip = fieldComments[i];
				    	String fname = actualTable.getFieldNames()[i];
						if (tooltip == null) {
							tooltip = fname;
						}
					    
					    if (fieldTypes[i].equals("DATETIME")) {
						    c.setUserCellRenderer(new MyImageCell(MyImageCell.DATETIME));
							if (sorter != null) {
								sorter.setComparator(i+2, new MyDatetimeSorter());
							}
					    }
					    else if (fieldTypes[i].equals("DOUBLE")) {
					    	c.setScale(32);
					    	c.setPrecision(64);
					    	//System.err.println(c.getScale() + "\t" + c.getPrecision());
					    	if (actualTable.getForeignFields() != null && actualTable.getForeignFields().length > i &&
					    			actualTable.getForeignFields()[i] != null) { // Es gibt hier einen Fremdtable!
					    		if (!actualTable.getForeignFields()[i].getTablename().equals("DoubleKennzahlen")) {
					    			System.err.println("Wasn jetzt los? keine DoubleKennzahlen???");
					    		}
					    		if (sorter != null) {
									sorter.setComparator(i+2, new MyDblKZSorter());
								}			
						    	//tooltip += "\nHier sind mehrere Einträge/Kennzahlen möglich!";
					    	}
					    	else {
					    		if (sorter != null) {
									sorter.setComparator(i+2, new MyDoubleSorter());
								}
					    	}
					    }
					    else if (fieldTypes[i].equals("INTEGER")) {
					    	if (actualTable.getForeignFields() != null && actualTable.getForeignFields().length > i &&
					    			actualTable.getForeignFields()[i] != null) { // Es gibt hier einen Fremdtable!
					    		if (sorter != null) {
									sorter.setComparator(i+2, new MyComboSorter(hashBox, i));
								}			
					    	}
					    	else {
					    		if (sorter != null) {
									sorter.setComparator(i+2, new MyIntegerSorter());
								}				    		
					    	}
					    	String[] mnTable = actualTable.getMNTable();
					    	if (mnTable != null && i < mnTable.length && mnTable[i] != null && mnTable[i].equals("INT")) {
						    	c.setUserCellRenderer(new MyMNRenderer(this, i));
						    	c.setReadOnly(true);					    		
						    	//tooltip += "\nHier sind mehrere Einträge möglich!";
					    	}
					    }
					    else if (fieldTypes[i].equals("BIGINT")) {
					    	if (sorter != null) {
								sorter.setComparator(i+2, new MyLongSorter());
							}	
					    }
						mthcr = new MyTableHeaderCellRenderer(this, defaultBgColor, tooltip);
					    tcm.getColumn(i+1).setHeaderRenderer(mthcr);
					}
				}
				int extraFields = 0;
				if (!actualTable.getHideScore()) {
					extraFields++;
				      c = this.getColumn(fieldTypes.length+extraFields); // Guetescore
				      c.setReadOnly(false);
				      Hashtable<Integer, ImageIcon> h = new Hashtable<>();
				      h.put(new Integer(1), new ImageIcon(this.getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/green.gif")));
				      h.put(new Integer(2), new ImageIcon(this.getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/yellow.gif")));
				      h.put(new Integer(3), new ImageIcon(this.getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/red.gif")));
				      this.setCellComponent(c, Column.IMAGE_CELL, h);
					    tcm.getColumn(fieldTypes.length+extraFields).setHeaderRenderer(new MyTableHeaderCellRenderer(this, defaultBgColor, "Hier kann eine SUBJEKTIVE Einschaetzung der Guete des Datensatzes (des Experiments, der Methode, ...) abgegeben werden\nACHTUNG: nicht vergessen diese Einschaetzung zu kommentieren im Feld Kommentar"));
					    if (sorter != null) {
							sorter.setComparator(fieldTypes.length+extraFields+1, new MyIntegerSorter());
						}
				}
				if (!actualTable.getHideKommentar()) {
					extraFields++;
				      c = this.getColumn(fieldTypes.length+extraFields); // Kommentar
				      c.setReadOnly(false); // Kommentar
				      c.setUserCellEditor(new MyTextareaEditor(this, actualTable, null)); c.setUserCellRenderer(new MyTextareaRenderer());
					    tcm.getColumn(fieldTypes.length+extraFields).setHeaderRenderer(new MyTableHeaderCellRenderer(this, defaultBgColor, null));
					    if (sorter != null) {
							sorter.setComparator(fieldTypes.length+extraFields+1, new MyStringSorter());
						}
				}

				if (!actualTable.getHideTested()) {
					extraFields++;
				      c = this.getColumn(fieldTypes.length+extraFields); // Geprueft
				      c.setReadOnly(false); 
				      c.setUserCellEditor(new MyCheckBoxEditor(bundle.getString("Haekchen vorhanden = Datensatz wurde von einer zweiten Person auf Richtigkeit ueberprueft"), this, true));
				      c.setUserCellRenderer(new MyCheckBoxEditor(bundle.getString("Haekchen vorhanden = Datensatz wurde von einer zweiten Person auf Richtigkeit ueberprueft"), this, true));
					    tcm.getColumn(fieldTypes.length+extraFields).setHeaderRenderer(new MyTableHeaderCellRenderer(this, defaultBgColor, "Datensaetze koennen von einem anderen Benutzer auf Richtigkeit hin geprueft werden.\nDies erhoeht die Guete des Eintrages."));
					    if (sorter != null) {
							sorter.setComparator(fieldTypes.length+extraFields+1, new MyBooleanSorter());
						}
				}
		    

				LinkedHashMap<Object, String>[] foreignHashs = actualTable.getForeignHashs();
				if (foreignHashs != null) {
					for (int i=0; i<foreignHashs.length; i++) {
						if (foreignHashs[i] != null) {
							c = this.getColumn(i+1); 
							c.setUserCellEditor(new MyComboBoxEditor(foreignHashs[i], false)); c.setUserCellRenderer(new MyComboBoxEditor(foreignHashs[i], true));
						}
					}
				}					    

				MyTable[] foreignFields = actualTable.getForeignFields();
				if (foreignFields != null) {
					//long ttt = System.currentTimeMillis();
					DBKernel.refreshHashTables();
					for (int i=0; i<foreignFields.length; i++) {
						if (foreignFields[i] != null) {
							c = this.getColumn(i+1); 
							c.setReadOnly(true);
							String[] mnTable = actualTable.getMNTable();
							if (mnTable != null && i < mnTable.length && mnTable[i] != null && mnTable[i].length() > 0) {
								c.setUserCellRenderer(new MyMNRenderer(this, i));																
							}
							else {
								hashBox[i] = DBKernel.fillHashtable(foreignFields[i], "", "\n", "\n", !bigbigTable); //" | " " ; "
								//c.setUserCellEditor(new MyComboBoxEditor(hashBox[i], false));
								c.setUserCellRenderer(new MyComboBoxEditor(hashBox[i], true));								
							}
							if (!foreignFields[i].getTablename().equals("DoubleKennzahlen")) {
								tcm.getColumn(i+1).setHeaderRenderer(new MyTableHeaderCellRenderer(this, Color.LIGHT_GRAY, (fieldComments[i] == null ? actualTable.getFieldNames()[i] : fieldComments[i])));	//  + "\n<rechte Maustaste oder Ctrl+Enter>"							
							}
							//if (DBKernel.debug) {System.out.println("foreignFields (" + foreignFields[i].getTablename() + "): " + (System.currentTimeMillis() - ttt));ttt = System.currentTimeMillis();} 
						}
					}
					//if (DBKernel.debug) {System.out.println("foreignFields (Teilmenge von prepareColymns): " + (System.currentTimeMillis() - ttt));}
				}			
			}						
		}		
		adjustColumns();
	}
	void adjustColumns() {
		this.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnAdjuster tca = new TableColumnAdjuster(this.getTable());
		tca.setColumnDataIncluded(false);
		tca.setColumnHeaderIncluded(true);
		tca.setDynamicAdjustment(false);
		//tca.setOnlyAdjustLarger(true);
		tca.adjustColumns();		
	}

	private void updateRowHeader(final boolean setVisible) {
		
		JScrollPane scroller = getScroller();
	    if (scroller != null) {
	    	if (setVisible) {
	        	int dataSize = this.getDataArray().length;
			      JTable rowHeader = new JTable(new MyTableRowModel(dataSize));
			      rowHeader.setFocusable(false);
			      LookAndFeel.installColorsAndFont(rowHeader, "TableHeader.background", "TableHeader.foreground", "TableHeader.font");
			
			      rowHeader.setIntercellSpacing(new Dimension(0, 0));
			      Dimension d = rowHeader.getPreferredScrollableViewportSize();
				    if (dataSize >= 10000) {
						d.width = 48;
					} else if (dataSize >= 1000) {
						d.width = 36;
					} else {
						d.width = 30;
					}
			      rowHeader.setPreferredScrollableViewportSize(d);
			      rowHeader.setRowHeight(this.getTable().getRowHeight());
			      rowHeader.setDefaultRenderer(Object.class, new TableRowHeaderRenderer());//new MyTableHeaderCellRenderer(this, defaultBgColor, ""));//new TableRowHeaderRenderer());
			    
			      scroller.setRowHeaderView(rowHeader);
				    
			      new TableRowHeaderResizer(rowHeader, this.getTable());
			}
	    	else {
	    		scroller.setRowHeaderView(null);
	    	}
    	}
	}
	public void checkUnsavedStuff() {
		checkUnsavedStuff(true);
	}
	void checkUnsavedStuff(final boolean saveProps) {
		//if (actualTable == null || actualTable.isReadOnly()) System.err.println(" readonly, but saved??? " + actualTable);
		// eigentlich würde es genügen, wenn man nur this.save() ausführt. this.save() hat selbst eine Routine, die checkt, ob was geändert wurde oder nicht, d.h. es wird nicht in jedem Fall abgespeichert
		if (theFilter != null) return;
		if (this.getEditingColumn() >= 0 && this.getEditingRow() >= 0) {
			this.save();
		}		
		else if (actualTable != null) {
			this.save();
		}
		if (this.getMyCellPropertiesModel() instanceof MyCellPropertiesModel) {
			int num =(this.getMyCellPropertiesModel()).getModifiedCellsColl().size(); 
			if (num > 0) {
				MyLogger.handleMessage(actualTable.getTablename() + ": Nicht alles konnte abgespeichert werden.\n" + num + " (rot markierte) Änderungen gehen verloren... Hmmm, stimmt das?");
        		//InfoBox ib = new InfoBox("Nicht alles konnte abgespeichert werden.\n" + num + " (rot markierte) Änderungen gehen verloren.", true, new Dimension(300, 300), null, true);
        		//ib.setVisible(true);    				  										        			
			}
		}
		if (saveProps && actualTable != null && !bigbigTable) {
			actualTable.saveProperties(this);
		}
		if (myDBPanel1 != null) {
			myDBPanel1.checkUnsavedStuffInForm();
		}			
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		if (myDBPanel1 != null) {
			if (e.getFirstIndex() > 0 || e.getValueIsAdjusting()) { // Wenn ich diese Abfrage nicht mache, dann gibt es Probleme bei getSelectedID: es wird die vor "setSelectedID" selektierte row zurückgegeben. Dies ist z.B. der Fall, wenn im dbTree etwas selektiert wird. Beispiel: Matrices -> Favoriten -> Kuhmilch. Dann wird im Endeffekt eine andere "virtuelle" Kuhm,ilch selektiert im dbTree und zwar unter BLS
				//System.out.println(e + "\t" + e.getFirstIndex());
				myDBPanel1.setSelectedID(getSelectedID());
				myDBPanel1.setBLOBEnabled(isFilledBlobField(this.getTable().getSelectedRow(), this.getTable().getSelectedColumn()));							
			}
		}
	}
	public int getSelectedID() {
		int result = -1;
		int row = this.getSelectedRow(); 
		if (row >= 0 && this.getRowCount() > 0 && row < this.getRowCount()) {
			try {
				Object o = this.getValueAt(row, 0);
				if (o instanceof Integer) {
					result = (Integer) o;
				}
			}
			catch (Exception e) {MyLogger.handleException(e);}
		}
		return result;
	}
	public boolean setSelectedID(final int id) {
		return setSelectedID(id, false);
	}
	private boolean setSelectedID(final int id, final boolean force) {
		if (id > 0 && (force || id != getSelectedID())) {
			for (int row=0;row<this.getRowCount();row++) {
				// evtl. sollte hier ein Thread eingebaut werden - wegen Gefahr zu langsam...
				Object o = this.getValueAt(row, 0);
				if (o instanceof Integer && ((Integer) o) == id) {
					int col = 0, colScroll = 0;
					if (this.getSelectedColumn() > 0) {
						col = this.getSelectedColumn();
					}
					JScrollPane scroller = this.getScroller();
					if (scroller != null) {
						colScroll = scroller.getHorizontalScrollBar().getValue();
					}		
					this.setSelectedRowCol(row, col, row, colScroll, force);		// , force ist neu hier... ich weiss nicht, ob hier was schiefgehen kann... 27.12.2010
					//this.selectCell(row, 0);
					//this.goTo(row);
					//getScroller().getVerticalScrollBar().setValue(row-1);
					return true;
				}
			}
			/*
			Vector<Integer> columnVector = new Vector<Integer>();
			columnVector.addElement(new Integer(1));
			this.find(0, 0, id+"", columnVector, true);			
			find Methode ist Kacke, weil da nach Strings gesucht wird. Soll die ID auf 1 gesetzt werden, dann findet die ID 10...
			*/
			return false;
		}
		return true;
	}
	
	public void setSelectedRowCol(final int row, int col, final int verticalScrollerPosition, final int horizontalScrollerPosition, final boolean forceCol) {
		if (row >= 0) {
			JScrollPane scroller = this.getScroller();
			if (scroller != null) {
				scroller.getVerticalScrollBar().setValue(verticalScrollerPosition);
			}
			if (col < 0)
			 {
				col = 0;
				//System.out.println("restoreProperties\t" + selectedRow + "\t" + bigTable.convertRowIndexToModel(selectedRow));
				//bigTable.changeSelection(selectedRow, selectedCol, false, false);
			}

			if (col > 0 && !forceCol) {
				this.selectCell(row, col-1);
				final MyDBTable myDB = this;
				char ch = KeyEvent.VK_TAB;
				KeyEvent ke = new KeyEvent(myDB, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_TAB, ch);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ke);
				// this.selectCell(row, col); ist identisch mit folgenden 3 Zeilen!!!
				//this.setRowSelectionInterval(row, row);
				//this.getTable().setColumnSelectionInterval(col, col);
				//this.getTable().requestFocus();
				/// Dummerweise ist z.B. die Combobox dann sogleich im EditorModus... also obige Toolkit-Lösung!
			}
			else {
				//this.setRowSelectionInterval(row, row);
				//this.getTable().setColumnSelectionInterval(col, col);
				//System.err.println("row3: " + row + "\t" + col);
				try {
					// hier gibts leider manchmal Abstürze, wenn ein Filter gesetzt ist und gleichzeitig der Sorter gedrückt wird
					this.selectCell(row, col);					
				}
				catch (Exception e) {MyLogger.handleException(e);}
				//System.err.println("row4: " + row + "\t" + col);
			}
			if (scroller != null) {
				scroller.getHorizontalScrollBar().setValue(horizontalScrollerPosition);
			}
		}		
	}
	public boolean isFilledBlobField(final int row, final int col) {
		boolean result = false;
		if (row >= 0 && row < this.getRowCount()) {
			if (actualTable != null) {
				if (actualTable.getTablename().equals("DateiSpeicher")) {
					if (this.getColumnCount() > 7) {
						result = (this.getValueAt(row, 7) != null);
					}
				}
				else {
					Vector<Integer> myBLOBs = actualTable.getMyBLOBs();
					for (int i=0;i<myBLOBs.size();i++) {
						if (col - 1 == myBLOBs.get(i) && this.getValueAt(row, col) != null) {
							result = true;
							break;
						}
					}			
				}
			}
		}
		return result;
	}
	void extractBLOB() {
		extractBLOB(this.getSelectedRow(), this.getSelectedColumn());
	}
	public void extractBLOB(final int row, final int col) {
		// extract BLOB
		if (this.getActualTable().getTablename().equals("DateiSpeicher")) {
			DBKernel.getPaper(0, "", "", (Integer) this.getValueAt(row, 0));			
		}
		else {
			if (row >= 0 && row < this.getRowCount()) {
				int tableID = (Integer) this.getValueAt(row, 0);
				//System.out.println(row + "\t" + col + "\t" + myDBTable1.getActualTable().getFieldNames()[col-1] + "\t" + tableID);
				DBKernel.getPaper(tableID, this.getActualTable().getTablename(), this.getActualTable().getFieldNames()[col-1], -1);			
			}
		}		
	}
  private void checkForeignWindow2Open(final int row, final int col) {
    MyTable[] myTs = actualTable.getForeignFields();
    //System.out.println(lastClickedRow + "\t" + lastClickedCol + "\t" + myTs.length);
    if (col > 0 && col <= myTs.length && myTs[col-1] != null &&
    		!myTs[col-1].getTablename().equals("DoubleKennzahlen")) { 
		JScrollPane scroller = getScroller();
		//int scrollVal = (scroller == null) ? -1 : scroller.getVerticalScrollBar().getValue();
		int hscrollVal = (scroller == null) ? -1 : scroller.getHorizontalScrollBar().getValue();

		//MyLogger.handleMessage("checkForeignWindow2Open1 : " + row);
		Object newVal = DBKernel.mainFrame.openNewWindow(myTs[col-1], this.getValueAt(row, col), this.getColumn(col).getHeaderValue(), this, row, col);
		//MyLogger.handleMessage("checkForeignWindow2Open2 : " + row + "\t" + newVal);
    	if (!this.actualTable.isReadOnly()) {
	      	if (newVal != null) {
	          	String[] mnTable = actualTable.getMNTable();
	        	if (col > 0 && mnTable != null && col-1 < mnTable.length && mnTable[col - 1] != null && (mnTable[col - 1].equals("INT"))) { // mnTable[col - 1].equals("DBL") || 
	        		refreshMNs();
	        	}
	        	else {
		      		this.setValueAt(newVal, row, col);    
		      		// evtl. HashBox neu setzen, sonst wird nicht refresht
		      		MyTable[] foreignFields = actualTable.getForeignFields();
		      		if (foreignFields != null) {
		      			if (foreignFields[col-1] != null) {
	  						hashBox[col-1] = DBKernel.fillHashtable(foreignFields[col-1], "", "\n", "\n", !bigbigTable, true); //" | " " ; "
	  						Column c = this.getColumn(col); 
	  						c.setUserCellRenderer(new MyComboBoxEditor(hashBox[col-1], true));
	  					}
	    			}
	        	}
	      	}
	      	else if (actualTable.getListMNs() != null) {
	      		refreshMNs();
	      	}
	    }
		//MyLogger.handleMessage("checkForeignWindow2Open3 : " + row + "\t" + newVal);

    	//int[] rh = getRowHeights(this.getTable());
  		this.save();
  		// Ist die Refresherei überhaupt notwendig? Naja, setRowHeights wird nicht aufgerufen und damit gehen die RowHeights verloren...
  		// Damit kann ich aber erst mal leben.
  		// Nagut, die hashBox geht auch verloren...
  		/*
  		try {
  			this.refresh();
  			syncTableRowHeights(row);
  		}
  		catch (SQLException e) {}
  		*/
  		/*
  		this.setTable(actualTable);
      this.getTable().setRowSelectionInterval(row, row); this.goTo(row);
      this.getTable().setColumnSelectionInterval(col, col);
    	setRowHeights(this.getTable(), rh);
    	syncTableRowHeights();
    	*/
  		int sr = this.getSelectedID();//this.getSelectedRow();
  		int sc = this.getSelectedColumn();

		//MyLogger.handleMessage("checkForeignWindow2Open4 sr: " + sr + "\t" + this.getSelectedRow());
  		try {
	    	this.refresh();
	    }
	    catch (Exception e1) {
	    	MyLogger.handleException(e1);
	    }
		//MyLogger.handleMessage("checkForeignWindow2Open5 sr: " + sr + "\t" + this.getSelectedRow());
  		if (myDBPanel1 != null) {
			myDBPanel1.handleSuchfeldChange(null);
		}
		//MyLogger.handleMessage("checkForeignWindow2Open6 sr: " + sr + "\t" + this.getSelectedRow());
	    this.updateRowHeader(!bigbigTable);
	    if (sc >= 0 && sc < this.getColumnCount()) {
			this.getTable().setColumnSelectionInterval(sc, sc);
		}
	    
    	/*
	    if (sr >= 0 && sr < this.getRowCount()) {
	    	this.setRowSelectionInterval(sr, sr);
	    	this.goTo(sr);
	    }
	*/
	    if (sorterModel != null) {
			sorterModel.initArray();
		}
	    //if (scrollVal >= 0) this.getScroller().getVerticalScrollBar().setValue(scrollVal);
	    if (hscrollVal >= 0) {
			this.getScroller().getHorizontalScrollBar().setValue(hscrollVal);
		}
		//this.getTable().requestFocus();
		//MyLogger.handleMessage("checkForeignWindow2Open7 sr: " + sr + "\t" + this.getSelectedRow());
    	this.setSelectedID(sr);
		//MyLogger.handleMessage("checkForeignWindow2Open8 sr: " + sr + "\t" + this.getSelectedRow());
    }  	
  }
  private void refreshMNs() {
  	int selID = this.getSelectedID();
  	this.getActualTable().doMNs();
		this.myRefresh();
		//MyLogger.handleMessage("refreshMNs sel vs sel: " + this.getSelectedID() + "\t" + selID);
		if (this.getSelectedID() != selID) {
			this.setSelectedID(selID);
		}
		syncTableRowHeights();	  
  }

	@Override
	public void sorterChanged(final RowSorterEvent e) {
		if (e == null || e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
			if (this.getTable().getCellEditor() != null) {
				this.getTable().getCellEditor().stopCellEditing();
			}			
		}
		if (e == null || e.getType() == RowSorterEvent.Type.SORTED) {
			//System.out.println("SORTED");
			/*
			if (this.getSelectedRow() >= 0) {
				System.out.println(this.getTable().convertRowIndexToModel(this.getSelectedRow()) + "\t" +
						this.getTable().convertRowIndexToView(this.getSelectedRow()) + "\t" + this.getSelectedRow());
			}
			*/
			if (myDBPanel1 != null) {
				//myDBPanel1.handleSuchfeldChange(null, false); // handleSuchfeldChange hier oben stehen lassen??? Oder lieber runter?? doFilter = false, weil sonst StackOverflow!
				syncTableRowHeights();
				int selID = this.getSelectedID();
				this.setSelectedID(selID, true);
				myDBPanel1.handleSuchfeldChange(null, false); // doFilter = false, weil sonst StackOverflow!
			}		
		}
	}
	public void syncTableRowHeights() {
		JScrollPane scroller = getScroller();
		if (scroller != null && scroller.getRowHeader() != null && scroller.getRowHeader().getView() instanceof JTable) {
			JTable jTable = (JTable) scroller.getRowHeader().getView();
			JTable bigTable = this.getTable(); 
			for (int i=0;i<bigTable.getRowCount();i++) {
			  	int newHeight = bigTable.getRowHeight(i);			
			  	if (newHeight > 0) {
					jTable.setRowHeight(i, newHeight);
				}
			}							
		}
	}
	public JScrollPane getScroller() {
	    JScrollPane scroller = null;
	    for (int i=0;i<this.getComponentCount();i++) {
	      if (this.getComponent(i) instanceof JScrollPane) {
	        scroller = (JScrollPane) this.getComponent(i);
	        break;
	      }
	    }		
	    return scroller;
	}
	private void addScrollerListeners4Rendering() {
		final MyDBTable myDB = this;
		JScrollPane scroller = getScroller();
		final JScrollBar scrollBarVertical = scroller.getVerticalScrollBar();
	    scrollBarVertical.addAdjustmentListener(new AdjustmentListener() {
	        @Override
			public void adjustmentValueChanged(final AdjustmentEvent ae) {
	          if (scrollBarVertical.getValueIsAdjusting()) {
	        	  //DBKernel.scrolling = true; //System.out.println("Value of vertical scroll bar: " + ae.getValue());
	          }
	    	  else {
	    		  //DBKernel.scrolling = false;
	    		  myDB.repaint();
	    	  }
	        }
	      });	
	    
		final JScrollBar scrollbarHorizontal = scroller.getHorizontalScrollBar();
		   scrollbarHorizontal.addAdjustmentListener(new AdjustmentListener() {
			      @Override
				public void adjustmentValueChanged(final AdjustmentEvent ae) {
			    	  if (scrollbarHorizontal.getValueIsAdjusting()) {
			    		  //DBKernel.scrolling = true; //System.out.println("Value of horizontal scroll bar: " + ae.getValue());
			    	  }
			    	  else {
			    		  //DBKernel.scrolling = false;
			    		  myDB.repaint();
			    	  }
			      }
			    });
	}
	/*
	private int[] getRowHeights(final JTable table) {
		int[] result = new int[table.getRowCount()];
		for (int i=0;i<table.getRowCount();i++) {
	  	result[i] = table.getRowHeight(i);		
		}	
		return result;
	}
	private void setRowHeights(final JTable table, final int[] rh) {
		for (int i=0;i<table.getRowCount();i++) {
	  	table.setRowHeight(i, rh[i]);		
		}			
	}
	private int packColumn(final JTable table, final TableCellRenderer renderer, final String title, final int margin) {
	    int width = 0;
	
	    // Get width of column header
	    Component comp = renderer.getTableCellRendererComponent(table, title, false, false, 0, 0);
	    width = comp.getPreferredSize().width;
	
	    // Add margin
	    width += 2*margin;
	    
	    return width;
	}
	*/
	  private void myCopyToClipboard() { 
		    int row = this.getTable().getSelectedRow(); 
		    int column = this.getTable().getSelectedColumn();
			if (row >= 0 && row < this.getRowCount() && column >= 0 && column < this.getColumnCount()) {
				 String excelStr = null;
				if (this.getTable().getValueAt(row, column) != null) {
		      excelStr = this.getTable().getValueAt(row, column).toString();			      
				}
				StringSelection sel  = new StringSelection(excelStr); 					
			  Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel); 									
			}
	  } 
	  
	  private void myPasteFromClipboard() { 
		    int row = this.getTable().getSelectedRow(); 
		    int column = this.getTable().getSelectedColumn();
			if (row >= 0 && row < this.getRowCount() && column >= 0 && column < this.getColumnCount()) {
			      String pasteString = ""; 
			      try { 
			              pasteString = (String)(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor)); 
			      }
			      catch (Exception e) { 
			    	  MyLogger.handleException(e);
			              return; 
			      } 
			      if (actualTable.getFieldTypes()[column - 1].startsWith("DATE")) {
					  SimpleDateFormat dateFormat2 = new SimpleDateFormat(actualTable.getFieldTypes()[column - 1].equals("DATE") ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm:ss");
					  try {
					      this.getTable().setValueAt(dateFormat2.parse(pasteString), row, column); 	   
					  }
					  catch (ParseException e) {
						e.printStackTrace();
					  }
			      }
			      else {
				      this.getTable().setValueAt(pasteString, row, column); 	   			    	  
			      }
			    	int selID = this.getSelectedID();
					this.myRefresh(row, column);
					if (this.getSelectedID() != selID) {
						this.setSelectedID(selID);
					}
			}
	} 

	private void cancelEditing() { 
	      if (this.getTable().getCellEditor() != null) { 
	    	  this.getTable().getCellEditor().cancelCellEditing(); 
	  } 
	} 
	/*
	private int getRowFromID(final int id) {
		int result = -1;
		if (id > 0) {
			for (int row=0;row<this.getRowCount();row++) {
				// evtl. sollte hier ein Thread eingebaut werden - wegen Gefahr zu langsam...
				Object o = this.getValueAt(row, 0);
				if (o instanceof Integer && ((Integer) o) == id) {
					result = row;
					break;
				}
			}
		}
		return result;
	}
	*/
	/*
	void copyProzessschritt() {
		Integer id = getSelectedID();
		if (id >= 0) {
	    int retVal = JOptionPane.showConfirmDialog(this, "Sicher?\nDie aktuellen Parameter des selektierten Prozessschrittes könnten in der Folge überschrieben werden!",
	    		"Prozessschritt kopieren?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (retVal == JOptionPane.YES_OPTION) {
				Object carverID = DBKernel.getValue("Prozessdaten", "ID", getSelectedID()+"", "Prozess_CARVER");
				Object[][] cond = new Object[1][2]; cond[0][0] = "Prozess_CARVER"; cond[0][1] = carverID;
				Object fromID = DBKernel.myList.openNewWindow(actualTable, null, "Datensatz zur Parameterübernahme", this, null, null, cond);
				copyParameters(fromID, getSelectedID());
				this.myRefresh();
				// evtl. HashBox neu setzen, sonst wird nicht refresht
				MyTable[] foreignFields = actualTable.getForeignFields();
				String[] mnTable = actualTable.getMNTable();
		  		if (foreignFields != null) {
					DBKernel.refreshHashTables();
		  			for (int i=0;i<foreignFields.length;i++) {
		    			if (foreignFields[i] != null && (mnTable == null || mnTable[i] == null)) {
		    				hashBox[i] = DBKernel.fillHashtable(foreignFields[i], "", "\n", "\n", !bigbigTable); //" | " " ; "
		    				Column c = this.getColumn(i+1); 
		    				c.setUserCellRenderer(new MyComboBoxEditor(hashBox[i], true));
		    			}
		  			}
		  		}
			}		
		}
	}
	private void copyParameters(final Object fromID, final Object toID) {
		if (fromID != null && toID != null) {
			// KapazitaetEinheit, KapazitaetEinheitBezug, DauerEinheit
			//int fromRow = this.getRowFromID((Integer)fromID);
			int toRow = this.getRowFromID((Integer)toID);
			if (actualTable.getFieldNames()[6].equals("KapazitaetEinheit")) {
				Object val = DBKernel.getValue(actualTable.getTablename(), "ID", fromID.toString(), "KapazitaetEinheit");
				if (val != null) {
					this.setValueAt(val, toRow, 7);
				} else {
					this.insertNull(toRow, 7);
				}
			} else {
				MyLogger.handleMessage("KapazitaetEinheit ist nicht Column Number 6....");
			}
			if (actualTable.getFieldNames()[7].equals("KapazitaetEinheitBezug")) {
				Object val = DBKernel.getValue(actualTable.getTablename(), "ID", fromID.toString(), "KapazitaetEinheitBezug");
				if (val != null) {
					this.setValueAt(val, toRow, 8);
				} else {
					this.insertNull(toRow, 8);
				}
			} else {
				MyLogger.handleMessage("KapazitaetEinheitBezug ist nicht Column Number 7....");
			}
			if (actualTable.getFieldNames()[9].equals("DauerEinheit")) { 
				Object val = DBKernel.getValue(actualTable.getTablename(), "ID", fromID.toString(), "DauerEinheit");
				if (val != null) {
					this.setValueAt(val, toRow, 10);
				} else {
					this.insertNull(toRow, 10);
				}
			} else {
				MyLogger.handleMessage("DauerEinheit ist nicht Column Number 9....");
			}
			// Zutaten???
			this.save(); // jetzt ja eigentlich nicht mehr notwendigm da ja toRow an copyKennzahlen übergeben wird - doppelt gemoppelt, ok, hält besser
			// alle Kenzahlen
			MyTable pd = DBKernel.myDBi.getTable("Prozessdaten");
			try {
				copyKennzahlen(pd,(Integer)fromID,(Integer)toID, toRow);
			}
			catch (Exception e1) {e1.printStackTrace();}
			//manageKZ("Prozessdaten", fromID, toID);
			// Sonstiges
			DBKernel.myDBi.getTable("Prozessdaten_Sonstiges").doMNs();
			DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Prozessdaten_Sonstiges") +
					" WHERE " + DBKernel.delimitL("Prozessdaten") + "=" + toID, false);				
			DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("Prozessdaten_Sonstiges") +
					" SELECT NULL," + toID + "," + DBKernel.delimitL("SonstigeParameter") + "," +
					DBKernel.delimitL("Wert") + "," +
					DBKernel.delimitL("Einheit") + "," + DBKernel.delimitL("Ja_Nein") + "," + DBKernel.delimitL("Kommentar") +
					" FROM " + DBKernel.delimitL("Prozessdaten_Sonstiges") +
					" WHERE " + DBKernel.delimitL("Prozessdaten") + "=" + fromID, false);
			ResultSet rs = DBKernel.getResultSet("SELECT " + DBKernel.delimitL("ID") + " FROM " +
					DBKernel.delimitL("Prozessdaten_Sonstiges") +	" WHERE " + DBKernel.delimitL("Prozessdaten") + "=" + toID, false);
			try {
				if (rs != null && rs.first()) {
					MyTable ps = DBKernel.myDBi.getTable("Prozessdaten_Sonstiges");
					do {
						try {
							copyKennzahlen(ps,rs.getInt("ID"),rs.getInt("ID"));
						}
						catch (Exception e1) {e1.printStackTrace();}
					} while (rs.next());
				}
			}
			catch (Exception e) {MyLogger.handleException(e);}
			DBKernel.myDBi.getTable("Prozessdaten_Sonstiges").doMNs();
		}
	}
	*/
	@Override
	public void keyPressed(final KeyEvent keyEvent) {
  	//System.out.println(keyEvent.getKeyCode() + "\t" + keyEvent.getKeyChar() + "\t" + KeyEvent.VK_F + "\t" + keyEvent.isControlDown());
    if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_LEFT) { //Ctrl+<-, Aussredem geht auch F8
    	keyEvent.consume();
    	DBKernel.mainFrame.getMyList().requestFocus();
    	return;
    }
    else if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_F) { // Ctrl+F
    	//System.out.println("Ctrl+F");
    	keyEvent.consume();
    	if (myDBPanel1 != null) {
    		myDBPanel1.getSuchfeld().grabFocus();
    	}
    	return;
    }
    /*
    else if (keyEvent.getKeyCode() == KeyEvent.VK_B && (keyEvent.isAltDown() || keyEvent.isControlDown())) {
    	keyEvent.consume();
    	SendMail.main(null);
    	return;
    }
    */
    else if (keyEvent.getKeyCode() == KeyEvent.VK_P && (keyEvent.isAltDown() || keyEvent.isControlDown())) {
    	keyEvent.consume();
    	myPrint();
    	return;
    }
    else if (keyEvent.getKeyCode() == KeyEvent.VK_INSERT) {
    	if (getMyDBPanel().addingDisabled()) keyEvent.consume();
    	else this.insertNewRow(keyEvent.isAltDown() || keyEvent.isControlDown(), null);
    }
    else if (keyEvent.getKeyCode() == KeyEvent.VK_DELETE) {
    	keyEvent.consume();
    }
    /*
    else if (keyEvent.getKeyCode() == KeyEvent.VK_W && (keyEvent.isAltDown() || keyEvent.isControlDown())) {
			if (this.getActualTable().getTablename().equals("Prozessdaten")) {
				copyProzessschritt();
	    	keyEvent.consume();
	     	return;
			}
    }
    */
    else if (keyEvent.getKeyCode() == KeyEvent.VK_K && (keyEvent.isAltDown() || keyEvent.isControlDown())) {
		keyEvent.consume();
    }
    else if (keyEvent.getKeyCode() == KeyEvent.VK_O && (keyEvent.isAltDown() || keyEvent.isControlDown())) {
		if (myDBPanel1 != null) {
			myDBPanel1.button11ActionPerformed(null);
		}
    	keyEvent.consume();
     	return;
	}
    else if (keyEvent.getKeyCode() == KeyEvent.VK_Q && (keyEvent.isAltDown() || keyEvent.isControlDown())) {
		if (myDBPanel1 != null) {
			myDBPanel1.button10ActionPerformed(null);
		}
    	keyEvent.consume();
     	return;
	}
  }
  @Override
public void keyReleased(final KeyEvent keyEvent) {
      if (keyEvent.getKeyCode()==KeyEvent.VK_C) { // Copy                        
          cancelEditing(); 
          myCopyToClipboard(); 
	  }
      else if (keyEvent.getKeyCode()==KeyEvent.VK_V) { // Paste 
	          cancelEditing(); 
	          myPasteFromClipboard();           
	  } 
  }

  @Override
public void keyTyped(final KeyEvent keyEvent) {
  	char ch = keyEvent.getKeyChar();
  	//System.out.println(ch + "\t" + keyEvent.isConsumed());
    if (ch == KeyEvent.VK_TAB || ch == KeyEvent.VK_ENTER && !keyEvent.isAltDown() && !keyEvent.isControlDown()) {
		return;
	}
    int row = this.getTable().getSelectedRow(); 
    int column = this.getTable().getSelectedColumn();
    if (ch == KeyEvent.VK_ESCAPE) {
		keyEvent.consume();
    	this.clearSelection();
    	return;
    }
    else if (ch == KeyEvent.VK_ENTER) {
		keyEvent.consume();
    	if (column > 0 && this.getColumn(column).getReadOnly()) { // readonly ComboBox Verschnitt
    		checkForeignWindow2Open(row, column);
    		return;
    	}
    	else if (isFilledBlobField(row, column)) {
        	if (myDBPanel1 != null) {
				this.extractBLOB();
			}
        	return;    		
    	}
    }
    else if (ch == KeyEvent.VK_DELETE) {
		keyEvent.consume();
		if (keyEvent.isAltDown() || keyEvent.isControlDown()) {
			insertNull(row, column);        	
		}
		else {
			deleteRow();
		}
		return;
    }
    //mdt.getTable().editCellAt(row, column); // habe ich jetzt nach unten geschoben, sonst geht BLOBEditor nicht...
    TableCellEditor ed = this.getTable().getCellEditor();
    if (ed == null) {
  	  	if (column > 0 && this.getColumn(column).getReadOnly()) { // readonly ComboBox Verschnitt
  	  		Rectangle jTableRechteck = this.getTable().getCellRect(row, column, false);
  	  		JScrollPane scroller = getScroller();
  	  		int hscrollVal = (scroller == null) ? 0 : scroller.getHorizontalScrollBar().getValue();
  			int vscrollVal = (scroller == null) ? 0 : scroller.getVerticalScrollBar().getValue();
  	  		checkOtherEditor2Open(row, column, this.getLocationOnScreen().x + jTableRechteck.x - hscrollVal, this.getLocationOnScreen().y + jTableRechteck.y - vscrollVal, ch);
  	  	}
    	return;
    }
    Component comp = ed.getTableCellEditorComponent(this.getTable(), ed.getCellEditorValue(), true, row, column);    	
  	if (comp == null) {
		return;
	}
  	if (comp instanceof JScrollPane) {
      	this.getTable().editCellAt(row, column);
        comp = ((JScrollPane) comp).getViewport().getView();
        JTextArea ta = (JTextArea) comp;
      	//System.out.println("12e");  			
        if (ta.getFont().canDisplay(ch)) {
			ta.append(""+ch);
		}
		else {
			ta.append(""); // +System.currentTimeMillis()
		}
        keyEvent.consume();
    }
    else if (comp instanceof JComboBox) {
    	if (ch == KeyEvent.VK_ENTER) {
    		checkForeignWindow2Open(row, column);        	        		
    	}
    	else if (ch == KeyEvent.VK_BACK_SPACE) {
    		insertNull(row, column);        	
    	}
    }
    else if (comp instanceof JTextField) {  // Zahlen bspw. in JTextField
    	final JTextField tf = (JTextField) comp;
		tf.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent e) {
				//System.err.println(System.currentTimeMillis() + "_focusGained_suchfeld");
			  	tf.select(0,0);
				tf.setCaretPosition(tf.getText().length());
			}
		});
    	tf.setText(ch+"");
      	keyEvent.consume();
    }
    else if (comp instanceof MyCheckBoxEditor) { // einfach nix machen, Haken wird gesetzt bei Space
    }
    else {
      	System.out.println("12e:" + comp);  	
    }
  	comp.requestFocusInWindow();      
  }
	@Override
	public void mouseClicked(final MouseEvent e) {
	    int lastClickedRow;// = e.getY()/this.getTable().getRowHeight();
	    int val = 0;
	    for (lastClickedRow = 0; lastClickedRow < this.getTable().getRowCount(); lastClickedRow++) {
	    	val += this.getTable().getRowHeight(lastClickedRow);
	    	if (val >= e.getY()) {
				break;
			}
	    }
	    this.setRowSelectionInterval(lastClickedRow, lastClickedRow);
		int lastClickedCol = getLastClickedCol(e);

		if (this.getColumn(lastClickedCol).getScale() != -1) { // bei GeschaetzteModelle
		    if (SwingUtilities.isLeftMouseButton(e)) {
		    	if (e.getClickCount() > 1) {
				      checkForeignWindow2Open(lastClickedRow, lastClickedCol);	    		
		    	}
		    	else {
		    		checkOtherEditor2Open(lastClickedRow, lastClickedCol, e.getXOnScreen(), e.getYOnScreen(), ' ');
		    	}
		  	}
		    else if (lastClickedCol == 0 && SwingUtilities.isRightMouseButton(e)) {
		    	merging();
		    }
		  	else {
		      checkForeignWindow2Open(lastClickedRow, lastClickedCol);
		  	}
		}
	}
	public void setReadOnly(boolean ro) {
		if (ro) {
			for (int i=0; i<this.getColumnCount(); i++) {		
				Column c = this.getColumn(i);
				c.setReadOnly(ro);
			}			
		}
	}
	private void merging() {
		int oldID = this.getSelectedID();
		String msg = "Bitte die ID des Datensatzes eingeben, der ID '" + oldID + "' ersetzen soll (" + oldID + " -> ?):";
		String titel = "ID '" + oldID + "' ersetzen durch andere ID!";
		if (DBKernel.getLanguage().equalsIgnoreCase("en")) {
			msg = "Please insert the ID of the dataset that shall replace ID='" + oldID + "' (" + oldID + " -> ?):";
			titel = "Replacing dataset '" + oldID + "'!";
		}
    	String response = JOptionPane.showInputDialog(this,
    			  msg,
    			  titel,
    			  JOptionPane.QUESTION_MESSAGE);
    	try {
    		int newID = Integer.parseInt(response);
    		if (oldID == newID) {
    			InfoBox ib = new InfoBox(DBKernel.mainFrame, "IDs identical: " + oldID, true, new Dimension(400,200), null, true);
    			ib.setVisible(true);
    		}
    		else if (!DBKernel.hasID(actualTable.getTablename(), newID)) {
    			InfoBox ib = new InfoBox(DBKernel.mainFrame, "The defined ID does not exist...", true, new Dimension(400,200), null, true);
    			ib.setVisible(true);
    		}
    		else {
	    		int reallyDoIt = JOptionPane.showConfirmDialog(this,
	    				"ID " + oldID + " will be replaced by " + newID + ". Correct?",
		    			  "Replace data via ID merging???",
		    			  JOptionPane.YES_NO_OPTION);
	    		if (reallyDoIt == JOptionPane.YES_OPTION) {
	    			if (DBKernel.mergeIDs(this.getConnection(), actualTable.getTablename(), oldID, newID)) {
		    			InfoBox ib = new InfoBox(DBKernel.mainFrame,
		    					"ID " + oldID + " was successfully replaced by " + newID + "!",
		    					true, new Dimension(400,200), null, true);
		    			ib.setVisible(true);
				    	this.setTable();
				    	
				    	if (theFilter != null) {
				    		this.filter(theFilter);
				    		//this.setReadOnly(true);
				    	}
				    	
	    			}
	    			else {
		    			InfoBox ib = new InfoBox(DBKernel.mainFrame,
		    					"Hmmm.... something went wrong...",
		    					true, new Dimension(400,200), null, true);
		    			ib.setVisible(true);
	    			}
	    		}
    		}
    	}
    	catch (Exception ee) {}		
	}
	private void checkOtherEditor2Open(final int lastClickedRow, final int lastClickedCol, final int x, final int y, final char ch) {
		if (lastClickedCol > 0) {
		}		
	}
	private int getLastClickedCol(final MouseEvent e) {
	      int lastClickedCol;
	      int val = 0;
	      for (lastClickedCol = 0; lastClickedCol < this.getTable().getColumnCount(); lastClickedCol++) {
	      	val += this.getTable().getColumnModel().getColumn(lastClickedCol).getWidth();
	      	if (val >= e.getX()) {
				break;
			}
	      }		
	      return lastClickedCol;
	}
	@Override
	public void mouseEntered(final MouseEvent e) {
	}
	@Override
	public void mouseExited(final MouseEvent e) {
	}
	@Override
	public void mousePressed(final MouseEvent e) {
	}
	@Override
	public void mouseReleased(final MouseEvent e) {
	}

}
