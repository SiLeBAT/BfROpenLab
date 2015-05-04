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
 * Created by JFormDesigner on Thu Jun 17 07:37:46 CEST 2010
 */

package de.bund.bfr.knime.openkrise.db.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import quick.dbtable.Filter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.actions.BackupAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.BlobAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.ChangeLogAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.ChangeMasterAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.ExportAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.FindAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.FocusLeft;
import de.bund.bfr.knime.openkrise.db.gui.actions.FocusRight;
import de.bund.bfr.knime.openkrise.db.gui.actions.ImportAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.InfoAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.MergeAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.PlausibleAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.RestoreAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.SelectionAction;
import de.bund.bfr.knime.openkrise.db.gui.actions.UserAction;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBForm;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBPanel;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.GuiMessages;
import de.bund.bfr.knime.openkrise.db.gui.dbtree.MyDBTree;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.BackTraceGenerator;

/**
 * @author Armin Weiser
 */
public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MyDBTable myDB;
	private MyDBTable topTable = null;

	MainFrame(final MyList myList) {
		this.myList = myList;
		myDB = myList.getMyDBTable();
		//myDBTree = myList.getMyDBTree();
		this.myDBPanel1 = new MyDBPanel(myDB, myList.getMyDBTree());
		//this.myDBTreePanel = new MyDBTreePanel(myDBTree);
		initComponents();
		addBindings();
		//this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH);
	}

	public MyList getMyList() {
		return myList;
	}

	public MyDBTable getTopTable() {
		return topTable;
	}

	public void setTopTable(MyDBTable topTable) {
		this.topTable = topTable;
	}

	public Object openNewWindow(final MyTable theNewTable, final Object value, final Object headerValue, final String mnTable, final String mnID, final MyDBForm dbForm) {
		return openNewWindow(theNewTable, value, headerValue, mnTable, mnID, dbForm, null);
	}

	public Object openNewWindow(final MyTable theNewTable, final Object value, final Object headerValue, final String mnTable, final String mnID, final MyDBForm dbForm, final JDialog owner) {
		Object result = null;
		String titel = (headerValue == null) ? theNewTable.getTablename() : (DBKernel.getLanguage().equals("en") ? "Choose " + GuiMessages.getString((String) headerValue) + "..." : headerValue + " auswählen...");
		//JDialog f = new JDialog(DBKernel.mainFrame, titel, dbForm != null);
		JDialog f = new JDialog(owner == null ? DBKernel.mainFrame : owner, titel);
		f.setModal(dbForm != null || owner != null);
		if (dbForm != null || owner != null) f.setModalityType(JDialog.ModalityType.DOCUMENT_MODAL); // DOCUMENT_MODAL APPLICATION_MODAL

		MyDBTable newDBTable = new MyDBTable();
		try {
			boolean disableButtons = false;
			newDBTable.initConn(DBKernel.getDBConnection());
			MyTable myT = null;
			if (dbForm != null) {
				myT = dbForm.getActualTable();
			}
			newDBTable.setTable(theNewTable);

			final MyDBPanel myP = new MyDBPanel(newDBTable, null, disableButtons);
			if (value != null && value instanceof Integer) {
				newDBTable.setSelectedID((Integer) value);
				myP.setFirstSelectedID((Integer) value);
			} else if (value != null && value instanceof Double && theNewTable != null && theNewTable.getTablename().equals("DoubleKennzahlen")) {
				Integer intVal = (int) Math.round((Double) value);
				newDBTable.setSelectedID(intVal);
				myP.setFirstSelectedID(intVal);
			} else {
				newDBTable.clearSelection();
			}

			f.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(final WindowEvent e) {
					myP.getSuchfeld().requestFocus();
				}
			});
			if (dbForm != null || owner != null) {
				myP.setParentDialog(f, true);
			}

			f.setMinimumSize(new Dimension(1000, 500)); // sonst ist der OK Knopf möglicherweise nicht zu sehen...
			f.getContentPane().add(myP);
			f.pack();
			if (dbForm == null) {
				f.setSize(DBKernel.mainFrame.getRightSize());
			} else {
				Dimension dim = dbForm.getSize();
				try {
					dim.width = (DBKernel.mainFrame.getSize().width + dim.width) / 2;
				} catch (Exception e) {
				}
				f.setSize(dim);
			}
			f.setLocationRelativeTo(f.getOwner());

			MyDBTable myDBTable2 = null;
			if (myT != null && myT.getListMNs() != null) {
				if (headerValue != null && mnTable != null && mnTable.length() > 0) { // headerValue.toString().equals("Kits")
					if (!mnTable.equals("INT")) {
						MyTable myMNTable = DBKernel.myDBi.getTable(mnTable);
						String tname = myT.getTablename();
						tname = myMNTable.getForeignFieldName(myT);
						/*
						 * if (tname.equals("GeschaetzteModelle")) { tname =
						 * "GeschaetztesModell"; }
						 */
						Object[][] o = new Object[1][2];
						o[0][0] = tname;
						o[0][1] = mnID; // dbTable.getValueAt(row, 0);
						//if (tname == "GeschaetztesModell") myDBTable2 = myP.setListVisible(true, this.getTable(mnTable), o, dbTable, row);
						//else
						myDBTable2 = myP.setListVisible(true, DBKernel.myDBi.getTable(mnTable), o);
					}
					myP.setParentDialog(f, false);
				}
			}

			//DBKernel.topTable = newDBTable;
			f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			f.setVisible(true);

			newDBTable.checkUnsavedStuff();
			if (myDBTable2 != null) {
				myDBTable2.checkUnsavedStuff();
			}

			if (myP.isSavePressed()) {
				result = myP.getSelectedID();
			}
			if (result instanceof Integer && (Integer) result < 0) {
				result = null;
				//DBKernel.topTable = dbTable;
			}
		} catch (Exception e) {
			result = value;
			MyLogger.handleException(e);
		}

		return result;
	}

	public Object openNewWindow(final MyTable theNewTable, final Object value, final Object headerValue, final MyDBTable dbTable, final Integer row, final Integer col) {
		return openNewWindow(theNewTable, value, headerValue, dbTable, row, col, null);
	}

	private Object openNewWindow(final MyTable theNewTable, final Object value, final Object headerValue, final MyDBTable dbTable, final Integer row, final Integer col, final Object[][] conditions) {
		return openNewWindow(theNewTable, value, headerValue, dbTable, row, col, conditions, false);
	}

	public Object openNewWindow(final MyTable theNewTable, final Object value, final Object headerValue, final MyDBTable dbTable, final Integer row, final Integer col, final Object[][] conditions, boolean fromMMC) {
		return openNewWindow(theNewTable, value, headerValue, dbTable, row, col, conditions, fromMMC, null);
	}

	public Object openNewWindow(final MyTable theNewTable, final Object value, final Object headerValue, final MyDBTable dbTable, final Integer row, final Integer col, final Object[][] conditions, boolean fromMMC, Filter mf) {
		return openNewWindow(theNewTable, value, headerValue, dbTable, row, col, conditions, fromMMC, mf, null);
	}

	public Object openNewWindow(final MyTable theNewTable, final Object value, final Object headerValue, final MyDBTable dbTable, final Integer row, final Integer col, final Object[][] conditions, boolean fromMMC, Filter mf, Component parent) {
		return openNewWindow(theNewTable, value, headerValue, dbTable, row, col, conditions, fromMMC, mf, parent, null);
	}

	public Object openNewWindow(final MyTable theNewTable, final Object value, final Object headerValue, final MyDBTable dbTable, final Integer row, final Integer col, final Object[][] conditions, boolean fromMMC, Filter mf, Component parent,
			String level1Expansion) {
		Object result = null;
		String titel = (headerValue == null) ? theNewTable.getTablename() : (DBKernel.getLanguage().equals("en") ? "Choose " + GuiMessages.getString((String) headerValue) + "..." : headerValue + " auswählen...");
		//JDialog.setDefaultLookAndFeelDecorated(true);
		Window parentFrame = null;
		if (parent == null) {
			parentFrame = DBKernel.mainFrame;
		} else {
			Window parentWindow = SwingUtilities.windowForComponent(parent);
			if (parentWindow != null) {
				parentFrame = parentWindow;
			}
		}
		@SuppressWarnings("unused")
		boolean isRO = DBKernel.isReadOnly();
		JDialog f = new JDialog(parentFrame, titel, JDialog.ModalityType.MODELESS); // !isRO && 
		if (dbTable != null || fromMMC) f.setModalityType(JDialog.ModalityType.DOCUMENT_MODAL); // DOCUMENT_MODAL APPLICATION_MODAL

		MyDBTable newDBTable = new MyDBTable();
		MyDBTree newDBTree = null;
		boolean isHierarchic = DBKernel.showHierarchic(theNewTable.getTablename());
		try {
			if (dbTable == null) {
				newDBTable.initConn(DBKernel.getDBConnection());
			} else {
				newDBTable.initConn(dbTable.getConnection());
			}

			MyTable myT = null;
			if (dbTable != null) {
				myT = dbTable.getActualTable();
			}
			boolean disableButtons = defineTable4NewDBTable(myT, dbTable, value, headerValue, theNewTable, newDBTable, row, col, conditions);
			if (isHierarchic) {
				newDBTree = new MyDBTree();
				String[] showOnly = null;
				/*
				 * if (myT != null &&
				 * myT.getTablename().equals("Versuchsbedingungen") &&
				 * headerValue != null &&
				 * headerValue.toString().equals("Matrix")) { showOnly = new
				 * String[] {"TOP", "ADV", "GS1", "Nährmedien"}; } else if (myT
				 * != null && myT.getTablename().equals("Zutatendaten") &&
				 * headerValue != null &&
				 * headerValue.toString().equals("Matrix")) { showOnly = new
				 * String[] {"TOP", "ADV", "GS1"}; } else if (myT != null &&
				 * myT.getTablename().equals("Aufbereitungsverfahren") &&
				 * headerValue != null &&
				 * headerValue.toString().equals("Matrix")) { showOnly = new
				 * String[] {"TOP", "ADV", "GS1"}; }
				 */
				newDBTree.setTable(theNewTable, showOnly);
			}

			final MyDBPanel myP = new MyDBPanel(newDBTable, newDBTree, disableButtons);

			if (mf != null) {
				newDBTable.setReadOnly(true);
				newDBTable.setFilter(mf);
				myP.disableAdding();
				myP.setDefaultFilter(mf);
				//myP.disableFilter();
			}

			if (value != null && value instanceof Integer) {
				newDBTable.setSelectedID((Integer) value);
				myP.setFirstSelectedID((Integer) value);
			} else if (value != null && value instanceof Double && theNewTable != null && theNewTable.getTablename().equals("DoubleKennzahlen")) {
				Integer intVal = (int) Math.round((Double) value);
				newDBTable.setSelectedID(intVal);
				myP.setFirstSelectedID(intVal);
			} else {
				newDBTable.clearSelection();
			}

			f.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(final WindowEvent e) {
					myP.getSuchfeld().requestFocus();
				}
			});
			if (dbTable != null || fromMMC) {
				myP.setParentDialog(f, true);
			}
			myP.setTreeVisible(isHierarchic, level1Expansion);
			f.setMinimumSize(new Dimension(1000, 500)); // sonst ist der OK Knopf möglicherweise nicht zu sehen...
			f.getContentPane().add(myP);
			f.pack();
			if (dbTable == null) {
				f.setSize(DBKernel.mainFrame.getRightSize());
			} else {
				Dimension dim = dbTable.getSize();
				try {
					dim.width = (DBKernel.mainFrame.getSize().width + dim.width) / 2;
				} catch (Exception e) {
				}
				f.setSize(dim);
			}
			f.setLocationRelativeTo(f.getOwner());

			MyDBTable myDBTable2 = null;
			if (myT != null && myT.getListMNs() != null) {
				String[] mnTable = myT.getMNTable();
				if (headerValue != null && mnTable != null && col != null && col > 0 && col - 1 < mnTable.length && mnTable[col - 1] != null && mnTable[col - 1].length() > 0) { // headerValue.toString().equals("Kits")
					if (!mnTable[col - 1].equals("INT")) {
						String mntname = mnTable[col - 1];
						MyTable myMNTable = DBKernel.myDBi.getTable(mntname);
						String tname = myT.getTablename();
						// Bitte auch schauen in MyDBTable, ca. Zeile 451 (insertNull)
						/*
						 * if (tname.equals("GeschaetzteModelle")) { tname =
						 * "GeschaetztesModell"; //System.err.println(myT + "\t"
						 * + theNewTable + "\t" +
						 * myMNTable.getForeignFieldName(myT)); } else if
						 * (tname.equals("Modellkatalog")) { tname = "Modell"; }
						 */
						//System.err.println(tname + "\t" + myMNTable.getForeignFieldName(myT));
						tname = myMNTable.getForeignFieldName(myT);
						Object[][] o2 = new Object[1][2];
						o2[0][0] = tname;
						o2[0][1] = dbTable.getValueAt(row, 0);
						//System.err.println(myT + "\t" + theNewTable + "\t" + myMNTable.getForeignFieldName(myT));
						// looking for: tname.equals("GeschaetztesModell") && !mntname.equals("GeschaetztesModell_Referenz") ... o1[0][0] = "Modell"; o1[0][1] = dbTable.getValueAt(row, 3);
						/*
						for (MyTable myTLeft : theNewTable.getForeignFields()) {
							for (MyTable myTOrigin : myT.getForeignFields()) {
								if (myTLeft != null && myTOrigin != null && myTLeft.equals(myTOrigin) && !myTLeft.getTablename().equals("DoubleKennzahlen")) {
									Object[][] o1 = new Object[1][2];
									o1[0][0] = theNewTable.getForeignFieldName(myTLeft);
									o1[0][1] = dbTable.getValueAt(row, myT.getForeignFieldIndex(myTOrigin) + 1);
									myDBTable2 = myP.setListVisible(true, myMNTable, o1, o2, row);
									//System.err.println(theNewTable.getForeignFieldName(myTLeft) + "\t" + myT.getForeignFieldIndex(myTOrigin));
									break;
								}
							}
						}
						*/
						for (int i = 0; i < theNewTable.getForeignFields().length; i++) {
							MyTable myTLeft = theNewTable.getForeignFields()[i];
							if (myTLeft != null && !myTLeft.getTablename().equals("DoubleKennzahlen") && (theNewTable.getMNTable() == null || theNewTable.getMNTable()[i] == null)) {
								for (int j = 0; j < myT.getForeignFields().length; j++) {
									MyTable myTOrigin = myT.getForeignFields()[j];
									if (myTOrigin != null && myTLeft.equals(myTOrigin) && (myT.getMNTable() == null || myT.getMNTable()[j] == null)) {
										Object[][] o1 = new Object[1][2];
										o1[0][0] = theNewTable.getForeignFieldName(myTLeft);
										o1[0][1] = dbTable.getValueAt(row, myT.getForeignFieldIndex(myTOrigin) + 1);
										myDBTable2 = myP.setListVisible(true, myMNTable, o1, o2, row);
										//System.err.println(theNewTable.getForeignFieldName(myTLeft) + "\t" + myT.getForeignFieldIndex(myTOrigin));
										break;
									}
								}
							}
						}
						if (myDBTable2 == null) myDBTable2 = myP.setListVisible(true, myMNTable, o2);
						/*
						 * if (tname.equals("GeschaetztesModell") &&
						 * !mntname.equals("GeschaetztesModell_Referenz")) {
						 * Object[][] o1 = new Object[1][2]; o1[0][0] =
						 * "Modell"; o1[0][1] = dbTable.getValueAt(row, 3);
						 * 
						 * if (mntname.equals("GueltigkeitsBereiche")) {
						 * o1[1][0] = "Parametertyp"; o1[1][1] = 1; } else { //
						 * GeschaetzteParameter o1[1][0] = "Parametertyp";
						 * o1[1][1] = 2; }
						 * 
						 * myDBTable2 = myP.setListVisible(true, myMNTable, o1,
						 * o2, row); } else { myDBTable2 =
						 * myP.setListVisible(true, myMNTable, o2); }
						 */
					}
					myP.setParentDialog(f, false);
				}
			}

			topTable = newDBTable;
			f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			f.setVisible(true);

			newDBTable.checkUnsavedStuff();
			if (myDBTable2 != null) {
				myDBTable2.checkUnsavedStuff();
			}

			if (myP.isSavePressed()) {
				result = myP.getSelectedID();
			}
			//MyLogger.handleMessage(result);
			if (result instanceof Integer && (Integer) result < 0) {
				if (mf == null) result = null;
			}
			//MyLogger.handleMessage(result);
			if (dbTable != null) dbTable.getActualTable().doMNs();
			topTable = dbTable;
		} catch (Exception e) {
			result = value;
			MyLogger.handleException(e);
		}

		return result;
	}

	private boolean defineTable4NewDBTable(MyTable myT, MyDBTable dbTable, final Object value, final Object headerValue, final MyTable theNewTable, MyDBTable newDBTable, final Integer row, final Integer col, final Object[][] conditions) {
		boolean disableButtons = false;
		/*
		 * String tn = ""; if (myT != null) { tn = myT.getTablename(); } if (myT
		 * != null && tn.equals("Zutatendaten") && headerValue != null &&
		 * headerValue.toString().equals("Vorprozess")) { Object[][] o = new
		 * Object[1][2]; o[0][0] = "Zielprozess"; o[0][1] =
		 * dbTable.getValueAt(row, 1); newDBTable.setTable(theNewTable, o);
		 * disableButtons = true; } else if (myT != null &&
		 * tn.equals("GeschaetzteParameterCovCor") && headerValue != null &&
		 * (headerValue.toString().equals("param1") ||
		 * headerValue.toString().equals("param2"))) { Object[][] o = new
		 * Object[1][2]; o[0][0] = "GeschaetztesModell"; o[0][1] =
		 * dbTable.getValueAt(row, 3); newDBTable.setTable(theNewTable, o); }
		 */
		if (myT != null) {
			Object[][] o = null;
			String[] dff = myT.getDeepForeignFields();
			// Zutat.Empfänger=Produkt.Artikel.Station
			// Vorprozess.Prozessdaten=Prozess_Verbindungen.Ausgangsprozess WHERE Prozess_Verbindungen.Zielprozess=Prozessdaten; AND " + DBKernel.delimitL("Zutat_Produkt") + "='Produkt'
			if (dff != null && dff[col - 1] != null) {
				StringTokenizer tokADD = new StringTokenizer(dff[col - 1], ";");
				String sAdd = tokADD.nextToken();
				String justAdd = null;
				if (tokADD.hasMoreTokens()) justAdd = tokADD.nextToken();
				if (dff[col - 1].indexOf(";") < 0) sAdd = dff[col - 1];
				StringTokenizer tokWhere = new StringTokenizer(sAdd, " WHERE ");
				String strLeft = tokWhere.nextToken();
				if (sAdd.indexOf(" WHERE ") < 0) strLeft = sAdd;
				StringTokenizer tok = new StringTokenizer(strLeft, "=");
				if (tok.hasMoreTokens()) {
					String left = tok.nextToken();
					if (tok.hasMoreTokens()) {
						String right = tok.nextToken();
						tok = new StringTokenizer(left, ".");
						if (tok.hasMoreTokens()) {
							tok.nextToken();// Zutat
							if (tok.hasMoreTokens()) {
								o = new Object[1][2];
								o[0][0] = tok.nextToken(); // Empfänger
								tok = new StringTokenizer(right, ".");
								if (tok.hasMoreTokens()) {
									String field = tok.nextToken(); // Produkt
									Integer i = myT.getFieldIndex(field);
									if (i == null && tokWhere.hasMoreTokens()) {
										MyTable myOT = DBKernel.myDBi.getTable(field);
										if (myOT != null) { // Prozess_Verbindungen
											String strRight = tokWhere.nextToken(); // Prozess_Verbindungen.Zielprozess=Prozessdaten
											StringTokenizer tok2 = new StringTokenizer(strRight, "=");
											if (tok2.hasMoreTokens()) {
												String left2 = tok2.nextToken(); // Prozess_Verbindungen.Zielprozess
												if (tok2.hasMoreTokens()) {
													String right2 = tok2.nextToken(); // Prozessdaten
													tok2 = new StringTokenizer(left2, ".");
													if (tok2.hasMoreTokens()) {
														if (field.equals(tok2.nextToken())) {// Prozess_Verbindungen
															if (tok2.hasMoreTokens()) {
																String field2Where = tok2.nextToken(); // Zielprozess
																Integer i2 = myT.getFieldIndex(right2);
																if (i2 != null) {
																	Object o2 = dbTable.getValueAt(row, i2 + 1);
																	if (tok.hasMoreTokens()) {
																		String field1 = tok.nextToken();
																		String sql = "SELECT " + DBKernel.delimitL(field1) + " FROM " + DBKernel.delimitL(field) + " WHERE " + DBKernel.delimitL(field2Where) + "=" + o2;
																		ResultSet rs = DBKernel.getResultSet(sql, false);
																		try {
																			if (rs != null && rs.first()) {
																				List<Object> l = new ArrayList<>();
																				do {
																					if (rs.getObject(field1) != null) {
																						l.add(rs.getObject(field1));
																					}
																				} while (rs.next());
																				Object t = o[0][0];
																				o = new Object[l.size()][3];
																				for (int ii = 0; ii < o.length; ii++) {
																					o[ii][0] = t;
																					o[ii][1] = l.get(ii);
																					o[ii][2] = justAdd;
																				}
																			}
																		} catch (Exception e) {
																			MyLogger.handleException(e);
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									} else {
										Object o1 = dbTable.getValueAt(row, i + 1);
										if (o1 != null) {
											MyTable myT1 = myT.getForeignFields()[i];
											while (tok.hasMoreTokens()) {
												String field1 = tok.nextToken(); // Artikel / Station
												i = myT1.getFieldIndex(field1);
												o1 = DBKernel.getValue(myT1.getTablename(), "ID", o1 + "", field1);
												myT1 = myT1.getForeignFields()[i];
											}
											o[0][1] = o1;
										}
									}
								}
								if (o[0][1] != null) newDBTable.setTable(theNewTable, o, "OR");
							}
						}
					}
				}
			}
			if (!theNewTable.equals(myT) && (o == null || o[0][1] == null)) {
				Integer i1 = theNewTable.getForeignFieldIndex(myT);
				if (i1 != null) {
					if (theNewTable.getMNTable() != null && theNewTable.getMNTable()[i1] != null && theNewTable.getMNTable()[i1].equals("INT")) {
						Integer i2 = myT.getForeignFieldIndex(theNewTable);
						if (i2 != null) {
							o = new Object[1][2];
							o[0][0] = "ID";
							o[0][1] = dbTable.getValueAt(row, i2 + 1);
							newDBTable.setTable(theNewTable, o);
						}
					} else {
						String fn = theNewTable.getFieldNames()[i1];
						if (fn != null) {
							o = new Object[1][2];
							o[0][0] = fn;
							o[0][1] = dbTable.getValueAt(row, 0);
							newDBTable.setTable(theNewTable, o);
						}
					}
				} else {
					for (int i = 0; i < theNewTable.getForeignFields().length; i++) {
						MyTable myTLeft = theNewTable.getForeignFields()[i];
						if (myTLeft != null && !myTLeft.getTablename().equals("DoubleKennzahlen") && (theNewTable.getMNTable() == null || theNewTable.getMNTable()[i] == null)) {
							for (int j = 0; j < myT.getForeignFields().length; j++) {
								MyTable myTOrigin = myT.getForeignFields()[j];
								if (myTOrigin != null && myTLeft.equals(myTOrigin) && (myT.getMNTable() == null || myT.getMNTable()[j] == null)) {
									o = new Object[1][2];
									o[0][0] = theNewTable.getForeignFieldName(myTLeft);
									o[0][1] = dbTable.getValueAt(row, myT.getForeignFieldIndex(myTOrigin) + 1);
									newDBTable.setTable(theNewTable, o);
									if (newDBTable.getRowCount() == 0) {
										o = null;
									}
									else {
										break;
									}
								}
							}
						}
					}
				}
			}
			if (o == null || o[0][1] == null) newDBTable.setTable(theNewTable, conditions);
		} else {
			newDBTable.setTable(theNewTable, conditions);
		}
		return disableButtons;
	}

	private void addBindings() {
		InputMap inputMap = toolBar1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = toolBar1.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "CTRL+F");
		FindAction finda = new FindAction(myDBPanel1);
		actionMap.put("CTRL+F", finda);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK), "CTRL+Left");
		FocusLeft focusLeft = new FocusLeft();
		actionMap.put("CTRL+Left", focusLeft);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK), "CTRL+Right");
		FocusRight focusRight = new FocusRight(myDB);
		actionMap.put("CTRL+Right", focusRight);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK), "F1");
		ImportAction impa = new ImportAction(button7.getName(), button7.getIcon(), button7.getToolTipText(), progressBar1);
		actionMap.put("F1", impa);
		button7.setAction(impa);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_DOWN_MASK), "F2");
		ExportAction expa = new ExportAction(button6.getName(), button6.getIcon(), button6.getToolTipText(), progressBar1, myDB);
		actionMap.put("F2", expa);
		button6.setAction(expa);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.CTRL_DOWN_MASK), "F3");
		UserAction usa = new UserAction(button10.getName(), button10.getIcon(), button10.getToolTipText());
		actionMap.put("F3", usa);
		button10.setAction(usa);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK), "F4");
		PlausibleAction pla = new PlausibleAction(button8.getName(), button8.getIcon(), button8.getToolTipText(), progressBar1, myDB);
		actionMap.put("F4", pla);
		button8.setAction(pla);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.CTRL_DOWN_MASK), "F5");
		BlobAction bla = new BlobAction(button5.getName(), button5.getIcon(), button5.getToolTipText());
		actionMap.put("F5", bla);
		button5.setAction(bla);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.CTRL_DOWN_MASK), "F6");
		ChangeLogAction cla = new ChangeLogAction(button3.getName(), button3.getIcon(), button3.getToolTipText());
		actionMap.put("F6", cla);
		button3.setAction(cla);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_DOWN_MASK), "F7");
		MergeAction ma = new MergeAction(button4.getName(), button4.getIcon(), button4.getToolTipText());
		actionMap.put("F7", ma);
		button4.setAction(ma);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, InputEvent.CTRL_DOWN_MASK), "F8");
		BackupAction ba = new BackupAction(button1.getName(), button1.getIcon(), button1.getToolTipText());
		actionMap.put("F8", ba);
		button1.setAction(ba);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.CTRL_DOWN_MASK), "F9");
		RestoreAction ra = new RestoreAction(button2.getName(), button2.getIcon(), button2.getToolTipText(), myDB);
		actionMap.put("F9", ra);
		button2.setAction(ra);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.CTRL_DOWN_MASK), "F10");
		SelectionAction sa = new SelectionAction(button12.getName(), button12.getIcon(), button12.getToolTipText(), myList);
		actionMap.put("F10", sa);
		button12.setAction(sa);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK), "F11");
		InfoAction ia = new InfoAction(button9.getName(), button9.getIcon(), button9.getToolTipText());
		actionMap.put("F11", ia);
		button9.setAction(ia);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.CTRL_DOWN_MASK), "F12");
		ChangeMasterAction cma = new ChangeMasterAction("Change Master", null, "Change Master");
		actionMap.put("F12", cma);
	}

	public JProgressBar getProgressBar() {
		return progressBar1;
	}

	/*
	 * public void setRC() { // myDBPanel1 setRC(myDBPanel1); } public void
	 * setRC(JComponent comp) { // myDBPanel1 oder MyDBForm
	 * splitPane1.setRightComponent(comp); }
	 */
	public Dimension getRightSize() {
		return splitPane1.getRightComponent().getSize();
	}

	@Override
	public void setVisible(final boolean doVisible) {
		boolean isAdmin = DBKernel.isAdmin();
		boolean isRO = false;
		try {
			if (DBKernel.getDBConnection() != null) isRO = DBKernel.getDBConnection().isReadOnly();
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		boolean isEnabable = isAdmin && !isRO;
		button8.getAction().setEnabled(true);
		button10.getAction().setEnabled(isEnabable);
		button1.getAction().setEnabled(!DBKernel.isServerConnection);
		button12.getAction().setEnabled(false);
		button2.getAction().setEnabled(!DBKernel.isServerConnection);
		button3.getAction().setEnabled(isEnabable);
		button5.getAction().setEnabled(isEnabable);
		button7.getAction().setEnabled(!isRO);
		button6.getAction().setEnabled(!isRO);
		button11.setEnabled(isEnabable);
		button13.setEnabled(isEnabable);
		button4.getAction().setEnabled(isEnabable && DBKernel.debug && !DBKernel.isKNIME && !DBKernel.isServerConnection);
		super.setVisible(doVisible);
	}

	private void thisWindowClosing(final WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			if (myDB != null) {
				myDB.checkUnsavedStuff();
			}
			this.setVisible(false);
			if (DBKernel.isKNIME) {

			} else {
				if (!DBKernel.isServerConnection) {
					MyLogger.handleMessage("vor closeDBConnections COMPACT: " + DBKernel.getFileSize(DBKernel.HSHDB_PATH + "DB.data"));
				}
				DBKernel.closeDBConnections(true); // lieber false???
				if (!DBKernel.isServerConnection) {
					MyLogger.handleMessage("nach closeDBConnections COMPACT: " + DBKernel.getFileSize(DBKernel.HSHDB_PATH + "DB.data"));
				}
				this.dispose();
				System.exit(0);
			}
		}
	}

	private void button11ActionPerformed(final ActionEvent e) {
		
		// reset Database:
		int retVal = JOptionPane.showConfirmDialog(this, "Are you sure that you want to reset the database?\nAll data will be lost!!!", "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (retVal == JOptionPane.YES_OPTION) {
			DBKernel.mainFrame.dispose();
			//DBKernel.mainFrame = null;
			//Login login = new Login(true);
			Login.dropDatabase();
			//this.setVisible(false);
			//DBKernel.mainFrame = null;
			DBKernel.openDBGUI();
		}
		
	}
	private void button13ActionPerformed(final ActionEvent e) {
		String sql = "Select DISTINCT(" + DBKernel.delimitL("Betriebsart") + ") from " + DBKernel.delimitL("Station") + " WHERE " + DBKernel.delimitL("Betriebsart") + " IS NOT NULL";
		List<String> businessTypes = new ArrayList<>();
		ResultSet rs = DBKernel.getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				do  {
					businessTypes.add(rs.getString(1));
				} while (rs.next());
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		BackTraceDialog dialog = new BackTraceDialog((JButton) e.getSource(), businessTypes);
		
		dialog.setVisible(true);
		
		if (dialog.isApproved()) {
			String folder = DBKernel.HSHDB_PATH;
			if (!folder.endsWith(System.getProperty("file.separator"))) folder += System.getProperty("file.separator");
			if (this.myDB != null) this.myDB.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			new BackTraceGenerator(folder + "openrequests", dialog.getSelected());
			if (this.myDB != null) this.myDB.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}		
	}
	private static class BackTraceDialog extends JDialog implements ActionListener {
				
		private static final long serialVersionUID = 1L;
		
		private boolean approved;
		private List<String> selected;
		
		private Map<String, JCheckBox> boxes;
		private JButton okButton;
		private JButton cancelButton;
		
		public BackTraceDialog(Component parent, List<String> businessTypes) {
			super(SwingUtilities.getWindowAncestor(parent), "Businesses for Back Tracing", DEFAULT_MODALITY_TYPE);			
			boxes = new LinkedHashMap<>();
			approved = false;
			selected = null;
			
			JPanel boxPanel = new JPanel();
			
			boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
			
			for (String type : businessTypes) {
				JCheckBox box = new JCheckBox(type, true);
				
				boxPanel.add(box);
				boxes.put(type, box);
			}
			
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			
			setLayout(new BorderLayout());
			add(UI.createHorizontalPanel(new JLabel("Select Business Types for Backtracing")), BorderLayout.NORTH);
			add(UI.createWestPanel(boxPanel), BorderLayout.CENTER);
			add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)),
					BorderLayout.SOUTH);
			pack();
			setLocationRelativeTo(parent);
			setResizable(false);
		}

		public boolean isApproved() {
			return approved;
		}

		public List<String> getSelected() {
			return selected;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				approved = true;
				selected = new ArrayList<>();
				
				for (Map.Entry<String, JCheckBox> entry : boxes.entrySet()) {
					if (entry.getValue().isSelected()) {
						selected.add(entry.getKey());
					}
				}
				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			}
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		ResourceBundle bundle = ResourceBundle.getBundle("de.bund.bfr.knime.openkrise.db.gui.PanelProps_" + DBKernel.getLanguage());
		toolBar1 = new JToolBar();
		button7 = new JButton();
		button6 = new JButton();
		button10 = new JButton();
		button8 = new JButton();
		button5 = new JButton();
		button3 = new JButton();
		button4 = new JButton();
		button1 = new JButton();
		button2 = new JButton();
		button12 = new JButton();
		button9 = new JButton();
		button11 = new JButton();
		button13 = new JButton();
		progressBar1 = new JProgressBar();
		splitPane1 = new JSplitPane();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		//myList = new MyList();
		//myDBPanel1 = new MyDBPanel();

		//======== this ========
		setTitle(bundle.getString("MainFrame.this.title"));
		setIconImage(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Database.gif")).getImage());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout("default:grow", "default, default:grow"));

		//======== toolBar1 ========
		{
			toolBar1.setFloatable(false);

			//---- button7 ----
			button7.setToolTipText(bundle.getString("MainFrame.button7.toolTipText"));
			button7.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Downloads folder.gif")));
			toolBar1.add(button7);
			toolBar1.addSeparator();

			//---- button6 ----
			button6.setToolTipText(bundle.getString("MainFrame.button6.toolTipText"));
			button6.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/3d bar chart.gif")));
			toolBar1.add(button6);
			toolBar1.addSeparator();

			//---- button10 ----
			button10.setToolTipText(bundle.getString("MainFrame.button10.toolTipText"));
			button10.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Database.gif")));
			button10.setVisible(false);
			toolBar1.add(button10);
			toolBar1.addSeparator();

			//---- button8 ----
			button8.setToolTipText("Similarity search");
			button8.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Eye.gif")));
			toolBar1.add(button8);
			toolBar1.addSeparator();

			//---- button5 ----
			button5.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Script.gif")));
			button5.setToolTipText(bundle.getString("MainFrame.button5.toolTipText"));
			button5.setVisible(false);
			toolBar1.add(button5);

			//---- button3 ----
			button3.setToolTipText(bundle.getString("MainFrame.button3.toolTipText"));
			button3.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/View.gif")));
			button3.setVisible(false);
			toolBar1.add(button3);
			toolBar1.addSeparator();

			//---- button4 ----
			button4.setToolTipText(bundle.getString("MainFrame.button4.toolTipText"));
			button4.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Filter.gif")));
			button4.setAlignmentX(1.0F);
			toolBar1.add(button4);
			toolBar1.addSeparator();

			//---- button1 ----
			button1.setToolTipText(bundle.getString("MainFrame.button1.toolTipText"));
			button1.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Record.gif")));
			toolBar1.add(button1);

			//---- button2 ----
			button2.setToolTipText(bundle.getString("MainFrame.button2.toolTipText"));
			button2.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Target.gif")));
			toolBar1.add(button2);

			//---- button12 ----
			button12.setToolTipText(bundle.getString("MainFrame.button12.toolTipText"));
			button12.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Find.gif")));
			toolBar1.add(button12);
			
			toolBar1.addSeparator();

			//---- button9 ----
			button9.setToolTipText(bundle.getString("MainFrame.button9.toolTipText"));
			button9.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Info.gif")));
			toolBar1.add(button9);

			toolBar1.addSeparator();

			//---- button11 ----
			button11.setToolTipText(bundle.getString("MainFrame.button11.text"));
			//button11.setVisible(false);
			ImageIcon ico = new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/edit_clear.gif"));
			ico.setImage(ico.getImage().getScaledInstance(30,30,Image.SCALE_DEFAULT)); 
			button11.setIcon(ico);
			button11.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button11ActionPerformed(e);
				}
			});
			toolBar1.add(button11);

			//---- button13 ----
			button13.setToolTipText("Missing data");
			ico = new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/generate_tables.gif"));
			ico.setImage(ico.getImage().getScaledInstance(30,30,Image.SCALE_DEFAULT)); 
			button13.setIcon(ico);
			button13.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button13ActionPerformed(e);
				}
			});
			toolBar1.add(button13);

			//---- progressBar1 ----
			progressBar1.setVisible(false);
			toolBar1.add(progressBar1);
		}
		contentPane.add(toolBar1, CC.xy(1, 1));

		//======== splitPane1 ========
		{
			splitPane1.setDividerLocation(250);

			//======== panel2 ========
			{
				panel2.setLayout(new FormLayout("default:grow", "default, $lgap, fill:default:grow"));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(myList);
				}
				panel2.add(scrollPane1, CC.xywh(1, 1, 1, 3));
			}
			splitPane1.setLeftComponent(panel2);
			splitPane1.setRightComponent(myDBPanel1);
		}
		contentPane.add(splitPane1, CC.xy(1, 2, CC.DEFAULT, CC.FILL));
		setSize(1020, 700);
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JToolBar toolBar1;
	private JButton button7;
	private JButton button6;
	private JButton button10;
	private JButton button8;
	private JButton button5;
	private JButton button3;
	private JButton button4;
	private JButton button1;
	private JButton button2;
	private JButton button12;
	private JButton button9;
	private JButton button11;
	private JButton button13;
	private JProgressBar progressBar1;
	private JSplitPane splitPane1;
	private JPanel panel2;
	private JScrollPane scrollPane1;
	private MyList myList;
	private MyDBPanel myDBPanel1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

}
