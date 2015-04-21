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
/*
 * Created by JFormDesigner on Wed Jun 30 01:12:18 CEST 2010
 */

package de.bund.bfr.knime.openkrise.db.gui.dbtable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.PlausibilityChecker;
import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyFilter;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.GuiMessages;
import de.bund.bfr.knime.openkrise.db.gui.dbtree.*;
import quick.dbtable.Filter;

/**
 * @author Armin Weiser
 */
public class MyDBPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LinkedHashMap<String, int[]> myFounds = new LinkedHashMap<>();	
	private int actualFindPos = 0;
	private JDialog parentDialog = null;
	private boolean savePressed = false;
	private boolean disableButtons = false;
	private boolean disableAdding = false;
	private Object[][] t1Conditions = null;
	private Object[][] t2Conditions = null;
	private int firstSelectedID = -1;
	private long tf1LastFocus = 0;
	private Integer gmRow = null;
	private MyDBForm myDBForm1;
	private boolean filterChangeAllowed = true;
	private Filter defaultFilter = null;
	
	public MyDBPanel() {
		this(null, null);
	}
	public MyDBPanel(MyDBTable myDB, MyDBTree myDBTree) {
		this(myDB, myDBTree, false);
	}
	public MyDBPanel(MyDBTable myDB, MyDBTree myDBTree, boolean disableButtons) {
		this.disableButtons = disableButtons;
		if (myDB == null) myDBTable1 = new MyDBTable();
		else myDBTable1 = myDB;
		if (myDBTree == null) myDBTree1 = new MyDBTree();
		else myDBTree1 = myDBTree;
		if (myDBForm1 == null) myDBForm1 = new MyDBForm();
		
		initComponents();
		panel1.addKeyListener(myDB);
		label1.addKeyListener(myDB);
		button6.addKeyListener(myDB);
		button7.addKeyListener(myDB);
		label2.addKeyListener(myDB);
		button1.addKeyListener(myDB);
		button10.addKeyListener(myDB);
		button11.addKeyListener(myDB);
		button5.addKeyListener(myDB);
		button4.addKeyListener(myDB);
		button2.addKeyListener(myDB);
		panel2.addKeyListener(myDB);

		myDBTable1.setMyDBPanel(this);
		myDBTree1.setMyDBPanel(this);
	  	if (myDBTable1.getRowCount() > 0) this.getSpinner().setValue(myDBTable1.getTable().getRowHeight(0));

		splitPane1.getLeftComponent().setVisible(false);
		//((com.jgoodies.looks.windows.WindowsSplitPaneUI) splitPane1.getUI()).getDivider().setVisible(false);	
		BasicSplitPaneUI splitPaneUI1 = (BasicSplitPaneUI)(splitPane1.getUI());
		splitPaneUI1.getDivider().setVisible(false);
		
		splitPane2.getRightComponent().setVisible(false);
	}
	public MyDBTable getMyDBTable() {
		return myDBTable1;
	}
	public MyDBTree getMyDBTree() {
		return myDBTree1;
	}
	public JScrollPane getTreeScroller() {
		return scrollPane1;
	}
	public JTextField getSuchfeld() {
		return textField1;
	}
	public JCheckBox getSuchIchCheckBox() {
		return checkBox1;
	}
	public JSpinner getSpinner() {
		return spinner1;
	}
	public void setBLOBEnabled(boolean enabled) {
		button5.setEnabled(enabled);
	}
	public void setFirstSelectedID(Integer id) {
		firstSelectedID = id;
	}
	public void setSelectedID(int id) {
		if (myDBTable1 != null) myDBTable1.setSelectedID(id);
		if (myDBTree1 != null) myDBTree1.setSelectedID(id);
		//if (myDBForm1 != null) myDBForm1.setSelectedID(id);
	}
	public int getSelectedID() {
		int result = -1;
		if (isFormVisible() && myDBForm1 != null) result = myDBForm1.getSelectedID();
		else if (myDBTable1 != null) result = myDBTable1.getSelectedID();
		return result;
	}
	void refreshTree() {
		if (isFormVisible()) {
			return;
		}
		if (myDBTree1 != null) myDBTree1.setTable(myDBTree1.getActualTable());
	}
	public void setTreeVisible(boolean visible, String level1Expansion) {
		/*
		if (isFormVisible()) {
			return;
		}
		*/
		if (level1Expansion != null) myDBTree1.expandPath(level1Expansion);
		splitPane1.getLeftComponent().setVisible(visible);
		//((WindowsSplitPaneUI) splitPane1.getUI()).getDivider().setVisible(visible);
		BasicSplitPaneUI splitPaneUI1 = (BasicSplitPaneUI)(splitPane1.getUI());
		splitPaneUI1.getDivider().setVisible(visible);
		splitPane1.setDividerLocation(visible ? 200 : 0);
		setListVisible(false, null, null);
	}
	public MyDBTable setListVisible(boolean visible, MyTable myT2, Object[][] t2Conditions) {
		return setListVisible(visible, myT2, null, t2Conditions, null);
	}
	public MyDBTable setListVisible(boolean visible, MyTable myT2, Object[][] t1Conditions, Object[][] t2Conditions, Integer row) {
		gmRow = row;
		this.t1Conditions = t1Conditions;
		this.t2Conditions = t2Conditions;
		splitPane2.getRightComponent().setVisible(visible);
		//((WindowsSplitPaneUI) splitPane2.getUI()).getDivider().setVisible(visible);
		BasicSplitPaneUI splitPaneUI2 = (BasicSplitPaneUI)(splitPane2.getUI());
		splitPaneUI2.getDivider().setVisible(visible);
		if (myT2 != null) {
			try {
				myDBTable2.initConn(DBKernel.getDBConnection());					
				if (t1Conditions != null) { // Geschaetztes Modell
					myDBTable1.setTable(myDBTable1.getActualTable(), t1Conditions);
					myDBTable2.setTable(myT2, t2Conditions);
					myDBTable2.getColumn(2).setScale(-1);
				}
				else {
					myDBTable2.setTable(myT2, t2Conditions);
				}
				myDBTable2.getColumn(0).setVisible(false);
				myDBTable2.getColumn(1).setVisible(false);
				myDBTable2.adjustColumns();
				splitPane2.setResizeWeight(visible ? 0.7 : 1);
				return myDBTable2;
			}
			catch (Exception e) {
				splitPane2.getRightComponent().setVisible(false);
				//((WindowsSplitPaneUI) splitPane2.getUI()).getDivider().setVisible(false);
				splitPaneUI2.getDivider().setVisible(false);
				MyLogger.handleException(e);
			}
		}
		return null;
	}
	public void setParentDialog(JDialog dialog, boolean panel2Visible) {
		this.parentDialog = dialog;
		if (dialog != null) {
			panel2.setVisible(true);	
			button10.setVisible(panel2Visible);
		}
	}
	public void setLeftComponent(MyTable myNewT) {
  		if (myNewT.isHasForm()) {
  			//MyDBForm form = new MyDBForm();
  			myDBForm1.setTable(myNewT);
			splitPane2.setLeftComponent(myDBForm1);
			splitPane2.getRightComponent().setVisible(false);	
			repaint(); // sonst ist der Switch-Knopf mitunter weg
		}
		else {
			if (isFormVisible()) {
				myDBForm1.save();
			}
			splitPane2.setLeftComponent(myDBTable1);
		}
	}
	private boolean isFormVisible() {
		return splitPane2.getLeftComponent() instanceof MyDBForm;
	}

  @Override
  public void paintComponent( Graphics g ) {
	  MyTable myT = myDBTable1.getActualTable();
	  	if (myDBTable1 != null && myT != null) {
	  		button1.setEnabled(!myT.isReadOnly());
	  		button2.setEnabled(!myT.isReadOnly());
	  		//button3.setEnabled(!myT.isReadOnly());
	  		button4.setEnabled(!myT.isReadOnly());
	  		
	  		//String tablename = myT.getTablename();
	  		//if (tablename.equals("ProzessWorkflow")) button1.setEnabled(false);
	  		/*
	  		if (tablename.equals("Prozessdaten")) {
	  			button1.setEnabled(false);
	  			button2.setEnabled(false);
	  		}
	  		*/
	
	  		boolean isRO = false;
			try {isRO = DBKernel.getDBConnection().isReadOnly();}
			catch (Exception e) {MyLogger.handleException(e);}
			
			if (disableAdding) button1.setEnabled(false);
			if (disableButtons) {
				button1.setEnabled(false);
				button2.setEnabled(false);
				button4.setEnabled(false);
			}
			if (isRO) {
				button1.setEnabled(false);
				button2.setEnabled(false);
				button4.setEnabled(false);
				//button10.setEnabled(false);
				//button11.setEnabled(false);			
			}
			if (isMN()) button2.setEnabled(false);
			//button12.setVisible(tablename.equals("Prozessdaten"));
			if (myT.isHasForm()) {
				button12.setVisible(true);
				button12.setText("Ansichtswitch");
			}
			/*
			else if (tablename.equals("Prozessdaten")) {
				button12.setVisible(true);
				button12.setText("Kopiere Parameter...");
			}
			*/
			else {
				button12.setVisible(false);
			}
	  	}
	  	super.paintComponent(g);
	}
  
  public void setDefaultFilter(Filter filter) {
	  defaultFilter = filter;
  }
  public void disableAdding() {
	  disableAdding = true;
  }
  boolean addingDisabled() {
	  return disableAdding;
  }

  public boolean isMN() {
  	return splitPane2.getRightComponent().isVisible();
  }
	private void button1ActionPerformed(ActionEvent e) {
		if (isFormVisible()) {
			myDBForm1.gotoInsertNewRow();
		}
		else {
			int modifiers = e.getModifiers();
			myDBTable1.insertNewRow(checkMod(modifiers, ActionEvent.CTRL_MASK) || checkMod(modifiers, ActionEvent.ALT_MASK), null);			
		}
	}
	private void refreshTable2() {
		if (isFormVisible()) {
			return;
		}
		if (isMN()) {
			MyTable myT2 = myDBTable2.getActualTable();
			setListVisible(true, myT2, t1Conditions, t2Conditions, gmRow);
		}
	}
    private boolean checkMod(int modifiers, int mask) {
    	return ((modifiers & mask) == mask);
    }

	private void button2ActionPerformed(ActionEvent e) {
		if (isFormVisible()) {
			myDBForm1.deleteRow();
		}
		else {
			myDBTable1.deleteRow();
		}
	}

	private void button4ActionPerformed(ActionEvent e) {
		if (isFormVisible()) {
			return;
		}
		int selCol = myDBTable1.getSelectedColumn(); 
		if (selCol > 0) {
			int selRow = myDBTable1.getSelectedRow();
			myDBTable1.insertNull(selRow, selCol);
			MyTable myT = myDBTable1.getActualTable();
			if (myT.getFieldTypes()[selCol - 1].startsWith("BLOB(")) {
				DBKernel.deleteBLOB(myT.getTablename(), myT.getFieldNames()[selCol - 1], myDBTable1.getSelectedID());
			}
		}
	}

	private void textField1KeyReleased(KeyEvent e) {
		handleSuchfeldChange(e);
	}
	public void handleSuchfeldChange(KeyEvent e) {
		if (isFormVisible()) {
			return;
		}
		handleSuchfeldChange(e, true);
	}
	void handleSuchfeldChange(final KeyEvent e, final boolean doFilter) {
		if (!filterChangeAllowed) return;
		if (isFormVisible()) {
			return;
		}
		if (e != null && e.isControlDown() && e.getKeyCode() == KeyEvent.VK_LEFT) { //Ctrl+<-, Ausserdem geht auch F8
	    	DBKernel.mainFrame.getMyList().requestFocus();
	    }
		else if (textField1.getText().length() == 0 && !checkBox1.isSelected()) {
			myFounds.clear(); initFindVector(myFounds, 0, "");
			myDBTree1.checkFilter("");
			if (doFilter) { //  && myDBTable1.getRowCount() > 0
				myDBTable1.checkUnsavedStuff(false);
				int id = myDBTable1.getSelectedID();
				if (defaultFilter != null)  myDBTable1.filter(defaultFilter);
				else myDBTable1.filter(new MyFilter(myDBTable1, textField1.getText(), null, e != null ? textField1 : null));				
				//myDBTable1.setReadOnly(true);
				myDBTable1.refreshSort();
				if (id < 0 || !myDBTable1.setSelectedID(id)) myDBTable1.clearSelection();
			}
		}
		else {
			if (doFilter) {
				myDBTable1.checkUnsavedStuff(false);
				LinkedHashMap<Integer, Vector<String>> v = null;
				if (checkBox1.isSelected()) v = DBKernel.getUsersFromChangeLog(myDBTable1.getActualTable().getTablename(), DBKernel.getUsername());
				int id = myDBTable1.getSelectedID();
				MyFilter mf = new MyFilter(myDBTable1, textField1.getText(), v, e != null ? textField1 : null);
				myDBTable1.filter(mf);
				//myDBTable1.setReadOnly(true);
				myDBTable1.refreshSort();
				if (id < 0 || !myDBTable1.setSelectedID(id)) myDBTable1.clearSelection();
			}			
		}		
		if (e != null) {
			if (!textField1.hasFocus()) textField1.requestFocus();
			tf1LastFocus = System.currentTimeMillis();
		}
	}

	private void button6ActionPerformed(ActionEvent e) {
		gotoFind(-1);
	}

	private void button7ActionPerformed(ActionEvent e) {
		gotoFind(1);
	}
	private void gotoFind(int increment) {
		if (isFormVisible()) {
			return;
		}
		
		if (myFounds != null) {
			actualFindPos += increment;
			int count = myFounds.size();
			if (actualFindPos > count) actualFindPos = 1;
			else if (actualFindPos < 1) actualFindPos = count;
	  	if (count > 0) {
	  		label2.setText(actualFindPos + " von " + count);
	  		label2.setBackground(new Color(150,255,150));
	  		int[] ij = (int[]) myFounds.values().toArray()[actualFindPos - 1]; // myFounds.get(actualFindPos - 1);
	  		myDBTable1.setRowSelectionInterval(ij[0], ij[0]); //.selectCell(ij[0], ij[1]);
	  		myDBTable1.getTable().setEditingColumn(ij[1]);
	  		myDBTable1.goTo(ij[0]);
	  		//textField1.requestFocus();
	  	}
	  	else {
	  		label2.setText("0 von 0");
	  		label2.setBackground(new Color(255,150,150));
	  	}
			this.repaint(); myDBTable1.repaint();
		}
		
	}
	private void initFindVector(LinkedHashMap<String, int[]> myFounds, int actualFindPos, String findString) {
		if (isFormVisible()) {
			return;
		}
		if (findString.toUpperCase().equals(textField1.getText().toUpperCase())) {
			if (myDBTable1.getMyCellPropertiesModel() instanceof MyCellPropertiesModel) {
				((MyCellPropertiesModel) myDBTable1.getMyCellPropertiesModel()).setFoundVector(myFounds);
			}
			this.myFounds = myFounds;
			this.actualFindPos = actualFindPos;
			int count = myFounds.size();
		  	button6.setVisible(count > 0);
		  	button7.setVisible(count > 0);
		  	label2.setVisible(textField1.getText().length() > 0);
		  	gotoFind(0);
		}
  }
	private void textField1FocusGained(FocusEvent e) {
		textField1.select(0, 0);
		textField1.setCaretPosition(textField1.getText().length());
	}

	private void button5ActionPerformed(ActionEvent e) {
		myDBTable1.extractBLOB();
	}

	private void button8ActionPerformed(ActionEvent e) {
		//myDBTable1.myPrint();
	  	String tt = "";
	  	tt += "ID\t" + GuiMessages.getString("Benutzer") + "\t" + GuiMessages.getString("Letzte Aenderung") + "\n"; 
	  	tt += "-----------------------------\n\n"; 
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			int modifiers = e.getModifiers();
			if (checkMod(modifiers, ActionEvent.CTRL_MASK) || checkMod(modifiers, ActionEvent.ALT_MASK)) {
				String theUser = DBKernel.getUsername();
				LinkedHashMap<Integer, Vector<String>> v = DBKernel.getUsersFromChangeLog(myDBTable1.getActualTable().getTablename(), theUser);
				if (v != null) {
					for (Map.Entry<Integer, Vector<String>> entry : v.entrySet()) {
						for (String entr : entry.getValue()) {
							tt += entry.getKey() + "\t" + entr + "\n"; 
						}
				  	}			
				}
			}
			else {
				Integer id = getSelectedID();
				LinkedHashMap<Integer, Vector<String>> v = DBKernel.getUsersFromChangeLog(myDBTable1.getActualTable().getTablename(), id);   
			  	for (Map.Entry<Integer, Vector<String>> entry : v.entrySet()) {
					for (String entr : entry.getValue()) {
						tt += id + "\t" + entr + "\n"; 
					}
			  	}			
			}
		}
		finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
		InfoBox ib;
		if (parentDialog != null) ib = new InfoBox(this.parentDialog, tt, true, new Dimension(1000, 600), null, false);
		else ib = new InfoBox(tt, true, new Dimension(1000, 600), null, false);
		ib.setVisible(true);    				
	}

	void button11ActionPerformed(ActionEvent e) {
		if (isFormVisible()) {
			return;
		}
		if (parentDialog != null) {
			if (button10.isVisible()) {
				if (firstSelectedID >= 0 && firstSelectedID != getSelectedID()) {
				    int retVal = JOptionPane.showConfirmDialog(parentDialog, // DBKernel.mainFrame
				    		GuiMessages.getString("Es wurde eine andere Auswahl getroffen") + "!\n" + GuiMessages.getString("Sicher, dass das so sein soll???"),
				    		GuiMessages.getString("Es wurde eine andere Auswahl getroffen") + "...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				    if (retVal == JOptionPane.NO_OPTION) {
				    	return;
				    }
				}
				savePressed = true; // wenn button10 nicht visible, dann haBEN WIR HIER EIN M:N Table, dann kann man ohnehin nicht abbrechen, dann wird die selectedID auch nicht benötigt! OK soll aber dennoch erhalten bleiben, um die Anwender nicht zu irritieren. Abbrechen ist mir zu bucklig zu implementieren!
			}
			else {
				  //if (myDBTable1.getActualTable().getTablename().equals("Messwerte")) { // erstmal nur für Messwerte, andere Tabellen können hinzugefügt werden, muss aber erst gecheckt werden, vor allem wegen der Performance!!! Die scheint bei M:N Tabellen sehr  schlecht zu sein.
					  String toShow = "";
					  Vector<String[]> plausibility = PlausibilityChecker.getPlausibilityRow(myDBTable1, myDBTable1.getActualTable(), -1, "ID");
					  if (plausibility != null && plausibility.size() == 1) {
						  String[] res = plausibility.get(0);
						  if (res[0] != null && res[0].trim().length() > 0) toShow = res[0] + "\n";
					  }
					  if (toShow.trim().length() > 0) {
						  InfoBox ib = new InfoBox(toShow, true, new Dimension(1000, 600), null, true);
						  ib.setVisible(true);
						  int retVal = JOptionPane.showConfirmDialog(this,
								  GuiMessages.getString("Die Plausibilitaetspruefung wurde nicht bestanden") + ".\n" +
								  GuiMessages.getString("Ihr solltet die eingegebenen Daten erst noch einmal ueberpruefen") + ".\n" +
								  GuiMessages.getString("Falls ihr das zu einem spaeteren Zeitpunkt machen wollt, koennt ihr das Fenster aber auch schliessen") + ".\n" +
								  GuiMessages.getString("Fenster schliessen?"),
								  GuiMessages.getString("Plausibilitaetstest nicht bestanden... Trotzdem schliessen?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						  if (retVal == JOptionPane.NO_OPTION) {
							  return;
						  }
					  }
				  //}
			}
			parentDialog.dispose();
		}
	}
	public boolean isSavePressed() {
		return savePressed;
	}
	void button10ActionPerformed(ActionEvent e) {
		if (isFormVisible()) {
			return;
		}
		if (parentDialog != null) parentDialog.dispose();
	}

	private void button3ActionPerformed(ActionEvent e) {
		if (isFormVisible()) {
			return;
		}
		int id = myDBTable1.getSelectedID();
		if (id > 0) {
			myDBTable1.save();
			refreshTable2();
			Vector<Object> vec = new Vector<>();
			Integer i1 = myDBTable2.getActualTable().getForeignFieldIndex(myDBTable1.getActualTable());
			if (i1 == null) System.err.println("i1 == null???? " + myDBTable1.getActualTable() + "\t" + myDBTable2.getActualTable());
				for (int i=0;i<myDBTable2.getColumnCount();i++) {
					if (i == 0) vec.add(null); // ID
					else if (i == 1) vec.add(null); // Basis ID wird in der Funktion insertNewRow eingetragen!
					else if (i1 != null && i == i1+1) vec.add(new Integer(id)); // Kit ID
					else vec.add(null);
				}				
			myDBTable2.insertNewRow(false, vec);
			try {myDBTable2.refresh();}
			catch (SQLException e1) {MyLogger.handleException(e1);}
		}
	}

	private void button9ActionPerformed(ActionEvent e) {
		if (isFormVisible()) {
			return;
		}
		myDBTable2.deleteRow();
	}

	private void spinner1StateChanged(ChangeEvent e) {
		if (isFormVisible()) {
			return;
		}
		try {
			int rh = (Integer) spinner1.getValue();
			myDBTable1.getTable().setRowHeight(rh);
			myDBTable1.syncTableRowHeights();
			myDBTable2.getTable().setRowHeight(rh);
			myDBTable2.syncTableRowHeights();
		}
		catch (Exception e1) {MyLogger.handleException(e1);}
	}

	private void button12ActionPerformed(ActionEvent e) {
		if (myDBTable1.getActualTable().isHasForm()) {
			if (isFormVisible()) {
				myDBForm1.save();
				try {
					myDBTable1.refresh(); //.setTable(myDBForm1.getActualTable());
					myDBTable1.refreshHashbox();
				}
				catch (SQLException e1) {
					e1.printStackTrace();
				}
				myDBTable1.setSelectedID(myDBForm1.getSelectedID());
				splitPane2.setLeftComponent(myDBTable1);
			}
			else {
				myDBTable1.checkUnsavedStuff();
	  			myDBForm1.setTable(myDBTable1.getActualTable());
	  			myDBForm1.setSelectedID(myDBTable1.getSelectedID());
				splitPane2.setLeftComponent(myDBForm1);
				//splitPane2.getRightComponent().setVisible(false);
			}
		}
		/*
		else if (myDBTable1.getActualTable().getTablename().equals("Prozessdaten")) {
			myDBTable1.copyProzessschritt();
		}		
		*/
	}
	void checkUnsavedStuffInForm() {
		if (isFormVisible()) {
			myDBForm1.save();
		}
	}

	private void checkBox1ActionPerformed(ActionEvent e) {
		if (isFormVisible()) {
			return;
		}
		handleSuchfeldChange(null);
	}

	private void textField1FocusLost(FocusEvent e) {
		// nach filtern nimmt sich manchmal der JTable den Focus, das nervt, also dann bitte wieder zurück!
		if (System.currentTimeMillis() - tf1LastFocus < 200) textField1.requestFocus();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		ResourceBundle bundle = ResourceBundle.getBundle("de.bund.bfr.knime.openkrise.db.gui.PanelProps_" + DBKernel.getLanguage());
		button12 = new JButton();
		panel1 = new JPanel();
		label1 = new JLabel();
		button6 = new JButton();
		textField1 = new JTextField();
		button7 = new JButton();
		label2 = new JLabel();
		checkBox1 = new JCheckBox();
		spinner1 = new JSpinner();
		button1 = new JButton();
		button8 = new JButton();
		button5 = new JButton();
		button4 = new JButton();
		button2 = new JButton();
		panel2 = new JPanel();
		button11 = new JButton();
		button10 = new JButton();
		splitPane1 = new JSplitPane();
		scrollPane1 = new JScrollPane();
		//myDBTree1 = new MyDBTree();
		splitPane2 = new JSplitPane();
		//myDBTable1 = new MyDBTable();
		panel3 = new JPanel();
		button3 = new JButton();
		button9 = new JButton();
		myDBTable2 = new MyDBTable();

		//======== this ========
		setLayout(new FormLayout(
			"2*(default), 3*(10dlu, default), 4*(default), 10dlu, default, 10dlu, default:grow, $lcgap, default",
			"default, $lgap, fill:default:grow"));
		((FormLayout)getLayout()).setColumnGroups(new int[][] {{4, 6, 8, 10, 12, 14}, {5, 7, 9, 11, 13, 15}});

		//---- button12 ----
		button12.setText(bundle.getString("MyDBPanel.button12.text"));
		button12.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button12ActionPerformed(e);
			}
		});
		add(button12, CC.xy(1, 1));

		//======== panel1 ========
		{
			panel1.setLayout(new FormLayout(
				"default, $lcgap, 23px, 120px, 2*(default), left:80px",
				"default"));
			((FormLayout)panel1.getLayout()).setColumnGroups(new int[][] {{3, 5}});

			//---- label1 ----
			label1.setText(bundle.getString("MyDBPanel.label1.text"));
			panel1.add(label1, CC.xy(1, 1));

			//---- button6 ----
			button6.setText(bundle.getString("MyDBPanel.button6.text"));
			button6.setVisible(false);
			button6.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button6ActionPerformed(e);
				}
			});
			panel1.add(button6, CC.xy(3, 1));

			//---- textField1 ----
			textField1.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					textField1KeyReleased(e);
				}
			});
			textField1.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					textField1FocusGained(e);
				}
				@Override
				public void focusLost(FocusEvent e) {
					textField1FocusLost(e);
				}
			});
			panel1.add(textField1, CC.xy(4, 1, CC.FILL, CC.DEFAULT));

			//---- button7 ----
			button7.setText(bundle.getString("MyDBPanel.button7.text"));
			button7.setVisible(false);
			button7.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button7ActionPerformed(e);
				}
			});
			panel1.add(button7, CC.xy(5, 1));

			//---- label2 ----
			label2.setText(bundle.getString("MyDBPanel.label2.text"));
			label2.setOpaque(true);
			panel1.add(label2, CC.xy(6, 1));

			//---- checkBox1 ----
			checkBox1.setText(bundle.getString("MyDBPanel.checkBox1.text"));
			checkBox1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					checkBox1ActionPerformed(e);
				}
			});
			panel1.add(checkBox1, CC.xy(7, 1));
		}
		add(panel1, CC.xy(2, 1));

		//---- spinner1 ----
		spinner1.setToolTipText(bundle.getString("MyDBPanel.spinner1.toolTipText"));
		spinner1.setModel(new SpinnerNumberModel(200, 20, 500, 10));
		spinner1.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				spinner1StateChanged(e);
			}
		});
		add(spinner1, CC.xy(4, 1));

		//---- button1 ----
		button1.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Add.gif")));
		button1.setToolTipText(bundle.getString("MyDBPanel.button1.toolTipText"));
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button1ActionPerformed(e);
			}
		});
		add(button1, CC.xy(6, 1));

		//---- button8 ----
		button8.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Info.gif")));
		button8.setToolTipText(bundle.getString("MyDBPanel.button8.toolTipText"));
		button8.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button8ActionPerformed(e);
			}
		});
		add(button8, CC.xy(8, 1));

		//---- button5 ----
		button5.setToolTipText(bundle.getString("MyDBPanel.button5.toolTipText"));
		button5.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Script.gif")));
		button5.setEnabled(false);
		button5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button5ActionPerformed(e);
			}
		});
		add(button5, CC.xy(10, 1));

		//---- button4 ----
		button4.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Disaster.gif")));
		button4.setToolTipText(bundle.getString("MyDBPanel.button4.toolTipText"));
		button4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button4ActionPerformed(e);
			}
		});
		add(button4, CC.xy(12, 1));

		//---- button2 ----
		button2.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Delete.gif")));
		button2.setToolTipText(bundle.getString("MyDBPanel.button2.toolTipText"));
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button2ActionPerformed(e);
			}
		});
		add(button2, CC.xy(14, 1));

		//======== panel2 ========
		{
			panel2.setVisible(false);
			panel2.setLayout(new FormLayout(
				"default, $lcgap",
				"$glue, 2*($lgap, default)"));

			//---- button11 ----
			button11.setText(bundle.getString("MyDBPanel.button11.text"));
			button11.setToolTipText(bundle.getString("MyDBPanel.button11.toolTipText"));
			button11.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button11ActionPerformed(e);
				}
			});
			panel2.add(button11, CC.xy(1, 3));

			//---- button10 ----
			button10.setText(bundle.getString("MyDBPanel.button10.text"));
			button10.setToolTipText(bundle.getString("MyDBPanel.button10.toolTipText"));
			button10.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button10ActionPerformed(e);
				}
			});
			panel2.add(button10, CC.xy(1, 5));
		}
		add(panel2, CC.xy(18, 1));

		//======== splitPane1 ========
		{
			splitPane1.setDividerLocation(80);

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(myDBTree1);
			}
			splitPane1.setLeftComponent(scrollPane1);

			//======== splitPane2 ========
			{
				splitPane2.setLeftComponent(myDBTable1);

				//======== panel3 ========
				{
					panel3.setLayout(new FormLayout(
						"default, default:grow",
						"2*(default:grow, fill:default:grow), fill:default:grow, $lgap, default"));

					//---- button3 ----
					button3.setText(bundle.getString("MyDBPanel.button3.text"));
					button3.setBackground(new Color(102, 255, 102));
					button3.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button3ActionPerformed(e);
						}
					});
					panel3.add(button3, CC.xy(1, 2));

					//---- button9 ----
					button9.setText(bundle.getString("MyDBPanel.button9.text"));
					button9.setBackground(new Color(255, 102, 102));
					button9.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button9ActionPerformed(e);
						}
					});
					panel3.add(button9, CC.xy(1, 4));
					panel3.add(myDBTable2, CC.xywh(2, 1, 1, 7));
				}
				splitPane2.setRightComponent(panel3);
			}
			splitPane1.setRightComponent(splitPane2);
		}
		add(splitPane1, CC.xywh(1, 3, 18, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JButton button12;
	private JPanel panel1;
	private JLabel label1;
	private JButton button6;
	private JTextField textField1;
	private JButton button7;
	private JLabel label2;
	private JCheckBox checkBox1;
	private JSpinner spinner1;
	private JButton button1;
	private JButton button8;
	private JButton button5;
	private JButton button4;
	private JButton button2;
	private JPanel panel2;
	private JButton button11;
	private JButton button10;
	private JSplitPane splitPane1;
	private JScrollPane scrollPane1;
	private MyDBTree myDBTree1;
	private JSplitPane splitPane2;
	private MyDBTable myDBTable1;
	private JPanel panel3;
	private JButton button3;
	private JButton button9;
	private MyDBTable myDBTable2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
