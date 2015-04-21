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
 * Created by JFormDesigner on Thu Aug 12 23:40:52 CEST 2010
 */

package org.hsh.bfr.db.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.hsh.bfr.db.Backup;
import org.hsh.bfr.db.BackupMyDBI;
import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyDBI;
import org.hsh.bfr.db.MyDBTablesNew;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.UpdateChecker;
import org.hsh.bfr.db.VersionComprator;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.gui.dbtree.MyDBTree;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Armin Weiser
 */
public class Login extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean firstRun;

	public Login(final boolean firstRun) {
		this.firstRun = firstRun;
		initComponents();
		//DBKernel.prefs = Preferences.userNodeForPackage(this.getClass());
		String lastUser = DBKernel.prefs.get("LAST_USER_LOGIN", "");
		String lastDBPath = DBKernel.prefs.get("LAST_DB_PATH", DBKernel.HSHDB_PATH);
		textField1.setText(lastUser);
		textField2.setText(lastDBPath);
		/*
		 * if (DBKernel.debug &&
		 * lastUser.equals(DBKernel.getTempSA(lastDBPath))) {
		 * passwordField1.setText(DBKernel.getTempSAPass(lastDBPath));
		 * //this.setTitle(textField1.getFont().getName() + " - " +
		 * textField1.getFont().getSize()); }
		 */
	}

	public Login(String dbPath, String username, String password, boolean readOnly, Boolean autoUpdate) {
		this.firstRun = false;
		initComponents();
		textField1.setText(username);
		passwordField1.setText(password);
		textField2.setText(dbPath);
		checkBox1.setSelected(false);
		checkBox2.setSelected(readOnly);
		startTheDB(autoUpdate, false);
	}

	private void startTheDB(Boolean autoUpdate, boolean openTheGui) {
		MainFrame mf = null;
		DBKernel.myDBi = MyDBI.loadDB(textField2.getText() + System.getProperty("file.separator") + "DB.xml");
		if (DBKernel.myDBi != null) {
			DBKernel.HSHDB_PATH = textField2.getText();
			mf = loadDBNew(DBKernel.myDBi, textField2.getText(), autoUpdate, openTheGui, autoUpdate == null);
		} else {
			DBKernel.HSHDB_PATH = textField2.getText();
			if (DBKernel.isHsqlServer(DBKernel.HSHDB_PATH)) {
				DBKernel.isServerConnection = true;
			} else {
				DBKernel.isServerConnection = false;
				if (!DBKernel.HSHDB_PATH.endsWith(System.getProperty("file.separator"))) {
					DBKernel.HSHDB_PATH += System.getProperty("file.separator");
				}
			}
			mf = loadDB(autoUpdate, openTheGui, autoUpdate == null);
		}
		if (mf != null) {
			//DBKernel.saveUP2PrefsTEMP(DBKernel.HSHDB_PATH);
			/*
			  DBKernel.sendRequest("DELETE FROM " +
			  DBKernel.delimitL("Infotabelle") + " WHERE " +
			  DBKernel.delimitL("Parameter") + " = 'DBuuid'", false);
			  //DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("DateiSpeicher"), false);
			  DBKernel.sendRequest("DELETE FROM " +
			  DBKernel.delimitL("ChangeLog"), false);
			  DBKernel.sendRequest("DROP TABLE " +
			  DBKernel.delimitL("CACHE_TS") + " IF EXISTS", false, true);
			  DBKernel.sendRequest("DROP TABLE " +
			  DBKernel.delimitL("CACHE_selectEstModel") + " IF EXISTS", false,
			  true); DBKernel.sendRequest("DROP TABLE " +
			  DBKernel.delimitL("CACHE_selectEstModel1") + " IF EXISTS", false,
			  true); DBKernel.sendRequest("DROP TABLE " +
			  DBKernel.delimitL("CACHE_selectEstModel2") + " IF EXISTS", false,
			  true); DBKernel.sendRequest("CHECKPOINT DEFRAG", false);
			  */
			/*
			 * try { ResultSet rs = DBKernel.getResultSet("SELECT " +
			 * DBKernel.delimitL("GeschaetzteModelle") + "." +
			 * DBKernel.delimitL("ID") + "," +
			 * DBKernel.delimitL("Versuchsbedingungen") + "." +
			 * DBKernel.delimitL("Referenz") + " FROM " +
			 * DBKernel.delimitL("GeschaetzteModelle") + " LEFT JOIN " +
			 * DBKernel.delimitL("Versuchsbedingungen") + " ON " +
			 * DBKernel.delimitL("GeschaetzteModelle") + "." +
			 * DBKernel.delimitL("Versuchsbedingung") + "=" +
			 * DBKernel.delimitL("Versuchsbedingungen") + "." +
			 * DBKernel.delimitL("ID") + " WHERE " +
			 * DBKernel.delimitL("Versuchsbedingungen") + "." +
			 * DBKernel.delimitL("Referenz") + " IS NOT NULL", false); if (rs !=
			 * null && rs.first()) { do { DBKernel.sendRequest("INSERT INTO " +
			 * DBKernel.delimitL("GeschaetztesModell_Referenz") + " (" +
			 * DBKernel.delimitL("GeschaetztesModell") + "," +
			 * DBKernel.delimitL("Literatur") + ") VALUES (" + rs.getInt("ID") +
			 * "," + rs.getInt("Referenz") + ")", false); } while (rs.next()); }
			 * } catch (Exception e1) {e1.printStackTrace();}
			 */
		}

		//UpdateChecker.check4Updates_148_149(null);
		/*
		 * 
		 * DBKernel.sendRequest("DELETE FROM " +
		 * DBKernel.delimitL("ModellkatalogParameter") + " WHERE " +
		 * DBKernel.delimitL("Modell") + " >= 47 AND " +
		 * DBKernel.delimitL("Modell") + " <= 49", false);
		 * DBKernel.sendRequest("DELETE FROM " +
		 * DBKernel.delimitL("Modell_Referenz") + " WHERE " +
		 * DBKernel.delimitL("Modell") + " >= 47 AND " +
		 * DBKernel.delimitL("Modell") + " <= 49", false);
		 * DBKernel.sendRequest("DELETE FROM " +
		 * DBKernel.delimitL("Modellkatalog") + " WHERE " +
		 * DBKernel.delimitL("ID") + " >= 47 AND " + DBKernel.delimitL("ID") +
		 * " <= 49", false); DBKernel.sendRequest("DELETE FROM " +
		 * DBKernel.delimitL("Literatur") + " WHERE " + DBKernel.delimitL("ID")
		 * + " <= 239", false);
		 */
		//MyList myList = loadDB(); UpdateChecker.temporarily(myList);
		/*
		 * DBKernel.sendRequest("CREATE USER " +
		 * DBKernel.delimitL(DBKernel.getTempSA()) + " PASSWORD '" +
		 * DBKernel.getTempSAPass() + "' ADMIN", false);
		 * DBKernel.sendRequest("DROP USER " + DBKernel.delimitL("SA"), false);
		 */
		/*
		 * DBKernel.mergeIDs("Station", 786, 769); DBKernel.mergeIDs("Station",
		 * 770, 763); DBKernel.mergeIDs("Station", 766, 11);
		 * DBKernel.mergeIDs("Station", 473, 484); DBKernel.mergeIDs("Station",
		 * 783, 28); DBKernel.mergeIDs("Station", 784, 30);
		 */
	}

	private void okButtonActionPerformed(final ActionEvent e) {
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			startTheDB(null, true);
		} finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void cancelButtonActionPerformed(final ActionEvent e) {
		this.dispose();
	}

	private void changePasswort(final MyDBTable myDB, String username, final String newPassword) throws Exception {
		boolean isAdmin = DBKernel.isAdmin();
		if (isAdmin) {
			DBKernel.sendRequest("SET PASSWORD '" + newPassword + "';", false); // MD5.encode(newPassword, "UTF-8")
		} else {
			DBKernel.closeDBConnections(false);
			DBKernel.getDefaultAdminConn();
			DBKernel.sendRequest("ALTER USER " + DBKernel.delimitL(username) + " SET PASSWORD '" + newPassword + "';", false); // MD5.encode(newPassword, "UTF-8")
			DBKernel.closeDBConnections(false);
			myDB.initConn(username, newPassword); // MD5.encode(newPassword, "UTF-8")
		}
	}

	private MainFrame initGui(MyDBTable myDB) {
		DBKernel.myDBi = new MyDBTablesNew();
		MyDBTree myDBTree = new MyDBTree();
		MyList myList = new MyList(myDB, myDBTree);
		myList.addAllTables();
		MainFrame mf = new MainFrame(myList);
		mf.setTopTable(myDB);
		try {
			myDB.initConn(DBKernel.getDBConnection());
		} catch (Exception e) {
			e.printStackTrace();
		}
		DBKernel.mainFrame = mf;
		return mf;
	}

	private MainFrame loadDB(Boolean autoUpdate, boolean openTheGui, boolean beInteractive) {
		MainFrame mf = null;
		MyDBTable myDB = null;
		boolean doUpdates = false;
		try {
			// Datenbank schon vorhanden?
			boolean noDBThere = !DBKernel.isServerConnection && !DBKernel.DBFilesDa(DBKernel.HSHDB_PATH);

			myDB = new MyDBTable();
			// Login fehlgeschlagen
			String username = textField1.getText();
			String password = new String(passwordField1.getPassword());
			DBKernel.prefs.putBoolean("DB_READONLY", checkBox2.isSelected());
			DBKernel.prefs.prefsFlush();
			//MD5.encode(password, "UTF-8");
			if (!myDB.initConn(username, password)) {
				if (DBKernel.passFalse) {
					passwordField1.setBackground(Color.RED);
					passwordField2.setBackground(Color.WHITE);
					passwordField3.setBackground(Color.WHITE);
					passwordField1.requestFocus();
				}
				return mf;
			}

			// Login succeeded
			DBKernel.prefs.put("LAST_USER_LOGIN", username);
			DBKernel.prefs.put("LAST_DB_PATH", DBKernel.HSHDB_PATH);
			DBKernel.prefs.prefsFlush();
			MyLogger.handleMessage("HSHDB_PATH: " + DBKernel.HSHDB_PATH);
			// Datenbank erstellen
			if (noDBThere) {
			} else if ((!DBKernel.isServerConnection) && (beInteractive || autoUpdate)) {// UPDATE? // true || 
				int dbAlt = isDBVeraltet(beInteractive, null);
				if (dbAlt == JOptionPane.YES_OPTION) {
					doUpdates = true;
				} else if (dbAlt == JOptionPane.CANCEL_OPTION) {
					DBKernel.closeDBConnections(false);
					return mf;
				}
			} else {
				String dbVersion = DBKernel.getDBVersionFromDB();
				String softwareVersion = DBKernel.softwareVersion;
				VersionComprator cmp = new VersionComprator();
				int result = cmp.compare(dbVersion, softwareVersion);
				if (result != 0) {
					String msg = "Login rejected!\n";
					if (result < 0) msg += "Softwareversion (" + softwareVersion + ") neuer als DB-Version (" + dbVersion + ")???";
					else msg += "Bitte Software aktualisieren!!!";
					InfoBox ib = new InfoBox(this, msg, true, new Dimension(600, 120), null, true);
					ib.setVisible(true);
					return mf;
				}
			}

			// Passwort ändern
			if (checkBox1.isSelected()) {
				if (passwordField2.getPassword().length >= 0) {
					String newPassword = new String(passwordField2.getPassword());
					if (newPassword.length() == 0) { // Passwörter dürfen nicht leer sein!
						passwordField1.setBackground(Color.WHITE);
						passwordField2.setBackground(Color.RED);
						passwordField3.setBackground(Color.RED);
						passwordField2.requestFocus();
						return mf;
					}
					if (newPassword.equals(new String(passwordField3.getPassword()))) {
						changePasswort(myDB, username, newPassword);
					} else {
						passwordField1.setBackground(Color.WHITE);
						passwordField2.setBackground(Color.WHITE);
						passwordField3.setBackground(Color.RED);
						passwordField3.requestFocus();
						return mf;
					}
				} else {
					passwordField1.setBackground(Color.WHITE);
					passwordField2.setBackground(Color.RED);
					passwordField3.setBackground(Color.WHITE);
					passwordField2.requestFocus();
					return mf;
				}
			}

			// Login succeeded: DB erstellen/starten, GUI aufbauen
			// Datenbank füllen			
			if (noDBThere) {
				int answer = JOptionPane.NO_OPTION; // YES_OPTION
				if (beInteractive) {
					answer = JOptionPane.showConfirmDialog(
							this,
							"There is no database.\nYou have two opportunities:\n- creating an empty one <Yes>\n- creating the default one with some prefilled sample data <No>\nDo you wish to create the empty one?",
							"No database...", JOptionPane.YES_NO_OPTION);
				}
				if (answer == JOptionPane.YES_OPTION) {
					mf = initGui(myDB);
					DBKernel.myDBi.bootstrapDB();
				} else {
					File temp = DBKernel.getCopyOfInternalDB();
					if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) {
						if (BackupMyDBI.doRestore(myDB, temp, true, true)) {
							DBKernel.myDBi.addUserInCaseNotThere(username, password);
						}
						else {  // Passwort hat sich verändert innerhalb der 2 beteiligten Datenbanken...
							passwordField1.setBackground(Color.RED);
							passwordField2.setBackground(Color.WHITE);
							passwordField3.setBackground(Color.WHITE);
							passwordField1.requestFocus();
							return mf;
						}
					} else {
						if (!Backup.doRestore(myDB, temp, true)) { // Passwort hat sich verändert innerhalb der 2 beteiligten Datenbanken...
							passwordField1.setBackground(Color.RED);
							passwordField2.setBackground(Color.WHITE);
							passwordField3.setBackground(Color.WHITE);
							passwordField1.requestFocus();
							return mf;
						}
					}					

					IWorkbenchWindow eclipseWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					
					if (eclipseWindow != null && DBKernel.isKrise) {						
						MessageDialog.openInformation(eclipseWindow.getShell(), "Internal database created", "Internal database created in folder '" + DBKernel.HSHDB_PATH + "'");
					} else {
						JOptionPane pane = new JOptionPane("Internal database created in folder '" + DBKernel.HSHDB_PATH + "'", JOptionPane.INFORMATION_MESSAGE);
						JDialog dialog = pane.createDialog("Internal database created");
						dialog.setAlwaysOnTop(true);
						dialog.setVisible(true);
					}
										
					mf = initGui(myDB);
				}
			} else {
				mf = initGui(myDB);

				if (doUpdates) {
					if (doTheUpdates()) return loadDB(autoUpdate, openTheGui, beInteractive);
					else return mf;
				}
			}

			startMainFrame(mf, myDB, openTheGui);
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return mf;
	}

	private boolean doTheUpdates() {
		boolean dl = DBKernel.dontLog;
		DBKernel.dontLog = true;
		try {
			boolean isAdmin = DBKernel.isAdmin();
			if (!isAdmin) {
				DBKernel.closeDBConnections(false);
				DBKernel.getDefaultAdminConn();
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.0")) {
				UpdateChecker.check4Updates_170_171();
				DBKernel.setDBVersion("1.7.1");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.1")) {
				UpdateChecker.check4Updates_171_172();
				DBKernel.setDBVersion("1.7.2");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.2")) {
				UpdateChecker.check4Updates_172_173();
				DBKernel.setDBVersion("1.7.3");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.3")) {
				UpdateChecker.check4Updates_173_174();
				DBKernel.setDBVersion("1.7.4");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.4")) {
				UpdateChecker.check4Updates_174_175();
				DBKernel.setDBVersion("1.7.5");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.5")) {
				UpdateChecker.check4Updates_175_176();
				DBKernel.setDBVersion("1.7.6");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.6")) {
				UpdateChecker.check4Updates_176_177();
				DBKernel.setDBVersion("1.7.7");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.7")) {
				UpdateChecker.check4Updates_177_178();
				DBKernel.setDBVersion("1.7.8");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.8")) {
				UpdateChecker.check4Updates_178_179();
				DBKernel.setDBVersion("1.7.9");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.7.9")) {
				UpdateChecker.check4Updates_179_180();
				DBKernel.setDBVersion("1.8.0");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.8.0")) {
				UpdateChecker.check4Updates_180_181();
				DBKernel.setDBVersion("1.8.1");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.8.1")) {
				UpdateChecker.check4Updates_181_182();
				DBKernel.setDBVersion("1.8.2");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.8.2")) {
				UpdateChecker.check4Updates_182_1820();
				DBKernel.setDBVersion("1.8.2.0");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.8.2.0")) {
				UpdateChecker.check4Updates_1820_18200();
				DBKernel.setDBVersion("1.8.2.0.0");
			}
			if (DBKernel.getDBVersionFromDB().equals("1.8.2.0.0")) {
				UpdateChecker.check4Updates_182_183();
				DBKernel.setDBVersion("1.8.3");
			}

			DBKernel.closeDBConnections(false);
		} catch (Exception e) {
			e.printStackTrace();
			DBKernel.dontLog = dl;
			return false;
		}
		DBKernel.dontLog = dl;
		return true;
	}

	private void startMainFrame(MainFrame mf, MyDBTable myDB, boolean openTheGui) {
		if (mf != null) {
			if (!mf.getMyList().setSelection(DBKernel.prefs.get("LAST_SELECTED_TABLE", null))) {
				mf.getMyList().setSelection(null);
			}

			if (this.isVisible()) this.dispose();//this.setVisible(false);
			mf.pack();
			boolean full = Boolean.parseBoolean(DBKernel.prefs.get("LAST_MainFrame_FULL", "FALSE"));

	        // Determine the new location of the window
			int defaultW = 1000;
			int defaultH = 600;
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int defaultX = (dim.width-defaultW)/2;
			int defaultY = (dim.height-defaultH)/2;
			
	        int w = Integer.parseInt(DBKernel.prefs.get("LAST_MainFrame_WIDTH", "" + defaultW));
			int h = Integer.parseInt(DBKernel.prefs.get("LAST_MainFrame_HEIGHT", "" + defaultH));
			int x = Integer.parseInt(DBKernel.prefs.get("LAST_MainFrame_X", "" + defaultX));
			int y = Integer.parseInt(DBKernel.prefs.get("LAST_MainFrame_Y", "" + defaultY));
			mf.setPreferredSize(new Dimension(w, h));
			mf.setBounds(x, y, w, h);
			if (full) mf.setExtendedState(JFrame.MAXIMIZED_BOTH);
			else mf.setExtendedState(JFrame.NORMAL);
			if (openTheGui) {
				mf.setVisible(true);
				mf.toFront();
				myDB.grabFocus();//myDB.selectCell(0, 0);
				//getAllMetaData(myList);			
			}
		}
	}

	static void dropDatabase() {
		DBKernel.closeDBConnections(false);
		File f = new File(DBKernel.HSHDB_PATH);
		File[] files = f.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && files[i].getName().startsWith("DB.")) {
					files[i].delete();
				}
			}
			System.gc();
		}
	}

	private void thisWindowClosing(final WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			this.setVisible(false);
			if (firstRun) {
				DBKernel.closeDBConnections(false);
				this.dispose();
				System.exit(0);
			}
		}
	}

	private void textField1KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void passwordField1KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void this_keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			okButtonActionPerformed(null);
		}
	}

	private void checkBox1ActionPerformed(final ActionEvent e) {
		passwordField2.setEnabled(checkBox1.isSelected());
		passwordField3.setEnabled(checkBox1.isSelected());
	}

	private void passwordField2KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void passwordField3KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void checkBox1KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void thisWindowOpened(final WindowEvent e) {
		passwordField1.requestFocus();
	}

	private void button1ActionPerformed(final ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(textField2.getText()));
		chooser.setDialogTitle("Wähle Ordner der Datenbank");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			textField2.setText(chooser.getSelectedFile().getAbsolutePath());
		} else {
			MyLogger.handleMessage("No Selection ");
		}
	}

	private void textField2KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private MainFrame loadDBNew(MyDBI myDBi, String dbPath, Boolean autoUpdate, boolean openTheGui, boolean beInteractive) {
		MainFrame mf = null;
		MyDBTable myDB = null;
		boolean doUpdates = false;
		try {
			// Datenbank schon vorhanden?
			boolean noDBThere = !DBKernel.DBFilesDa(dbPath);

			myDB = new MyDBTable();
			// Login fehlgeschlagen
			String username = textField1.getText();
			String password = new String(passwordField1.getPassword());
			DBKernel.prefs.putBoolean("DB_READONLY", checkBox2.isSelected());
			DBKernel.prefs.prefsFlush();
			if (!myDBi.establishDBConnection(username, password, dbPath)) {
				if (myDBi.getPassFalse()) {
					passwordField1.setBackground(Color.RED);
					passwordField2.setBackground(Color.WHITE);
					passwordField3.setBackground(Color.WHITE);
					passwordField1.requestFocus();
				}
				return mf;
			}
			myDB.initConn(myDBi.getConn());

			DBKernel.prefs.put("LAST_USER_LOGIN", username);
			DBKernel.prefs.put("LAST_DB_PATH", dbPath);
			DBKernel.prefs.prefsFlush();
			MyLogger.handleMessage("DB_PATH: " + dbPath);
			// Datenbank erstellen
			if (noDBThere) {
			} else if (!myDBi.isServerConnection() && (beInteractive || autoUpdate)) {// UPDATE?
				int dbAlt = isDBVeraltet(beInteractive, myDBi);
				if (dbAlt == JOptionPane.YES_OPTION) {
					doUpdates = true;
				} else if (dbAlt == JOptionPane.CANCEL_OPTION) {
					myDBi.closeDBConnections(false);
					return mf;
				}
			} else {
				String dbVersion = myDBi.getDBVersionFromDB();
				String softwareVersion = myDBi.getSoftwareVersion();
				VersionComprator cmp = new VersionComprator();
				int result = cmp.compare(dbVersion, softwareVersion);
				if (result != 0) {
					String msg = "Login rejected!\n";
					if (result < 0) msg += "Softwareversion (" + softwareVersion + ") neuer als DB-Version (" + dbVersion + ")???";
					else msg += "Bitte Software aktualisieren!!!";
					InfoBox ib = new InfoBox(this, msg, true, new Dimension(600, 120), null, true);
					ib.setVisible(true);
					return mf;
				}
			}

			// Passwort ändern
			if (checkBox1.isSelected()) {
				if (passwordField2.getPassword().length >= 0) {
					String newPassword = new String(passwordField2.getPassword());
					if (newPassword.length() == 0) { // Passwörter dürfen nicht leer sein!
						passwordField1.setBackground(Color.WHITE);
						passwordField2.setBackground(Color.RED);
						passwordField3.setBackground(Color.RED);
						passwordField2.requestFocus();
						return mf;
					}
					if (newPassword.equals(new String(passwordField3.getPassword()))) {
						myDBi.changePasswort(newPassword);
						myDB.initConn(myDBi.getConn());
					} else {
						passwordField1.setBackground(Color.WHITE);
						passwordField2.setBackground(Color.WHITE);
						passwordField3.setBackground(Color.RED);
						passwordField3.requestFocus();
						return mf;
					}
				} else {
					passwordField1.setBackground(Color.WHITE);
					passwordField2.setBackground(Color.RED);
					passwordField3.setBackground(Color.WHITE);
					passwordField2.requestFocus();
					return mf;
				}
			}

			// Login succeeded: DB erstellen/starten, GUI aufbauen
			// Datenbank füllen			
			mf = initGuiNew(myDB);
			if (!myDBi.isServerConnection() && noDBThere) {
				myDBi.bootstrapDB();
			} else {
				if (doUpdates) {
					if (doTheUpdates()) return loadDBNew(myDBi, dbPath, autoUpdate, openTheGui, beInteractive);
					else return mf;
				}
			}

			startMainFrame(mf, myDB, openTheGui);
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return mf;
	}

	private int isDBVeraltet(boolean beInteractive, MyDBI myDBi) {
		int result = JOptionPane.NO_OPTION;

		String dbVersion = (myDBi == null ? DBKernel.getDBVersionFromDB() : myDBi.getDBVersionFromDB());
		MyLogger.handleMessage("DBVersion: " + dbVersion);
		if (dbVersion == null || !dbVersion.equals(myDBi == null ? DBKernel.softwareVersion : myDBi.getSoftwareVersion())) {
			if (beInteractive) result = askVeraltetDBBackup(myDBi);
			else result = JOptionPane.YES_OPTION;
		}
		return result;
	}

	private int askVeraltetDBBackup(MyDBI myDBi) {
		int result = JOptionPane.YES_OPTION;
		int retVal = JOptionPane.showConfirmDialog(this, "Die Datenbank ist veraltet und muss ersetzt werden.\nSoll zuvor ein Backup der alten Datenbank erstellt werden?",
				"Backup erstellen?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (retVal == JOptionPane.YES_OPTION) {
			if (myDBi != null && myDBi.getConn() != null) {
				if (!BackupMyDBI.dbBackup(this)) {
					result = JOptionPane.CANCEL_OPTION;
				}
			} else {
				if (!Backup.dbBackup(this)) {
					result = JOptionPane.CANCEL_OPTION;
				}
			}
		} else if (retVal == JOptionPane.NO_OPTION) {
			retVal = JOptionPane.showConfirmDialog(this, "Die Datenbank wirklich ohne Backup überschreiben?? Sicher?", "Sicher?", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (retVal == JOptionPane.YES_OPTION) {
				;
			} else {
				return askVeraltetDBBackup(myDBi);
			}
		} else {
			result = JOptionPane.CANCEL_OPTION;
		}
		return result;
	}

	private MainFrame initGuiNew(MyDBTable myDB) {
		MyDBTree myDBTree = new MyDBTree();
		MyList myList = new MyList(myDB, myDBTree);
		myList.addAllTables();
		MainFrame mf = new MainFrame(myList);
		mf.setTopTable(myDB);
		DBKernel.mainFrame = mf;
		return mf;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		ResourceBundle bundle = ResourceBundle.getBundle("org.hsh.bfr.db.gui.PanelProps");
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		textField1 = new JTextField();
		label2 = new JLabel();
		passwordField1 = new JPasswordField();
		button1 = new JButton();
		textField2 = new JTextField();
		checkBox2 = new JCheckBox();
		checkBox1 = new JCheckBox();
		label3 = new JLabel();
		passwordField2 = new JPasswordField();
		label4 = new JLabel();
		passwordField3 = new JPasswordField();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle(bundle.getString("Login.this.title"));
		setAlwaysOnTop(true);
		setIconImage(new ImageIcon(getClass().getResource("/org/hsh/bfr/db/gui/res/Database.gif")).getImage());
		setFont(new Font("Tahoma", Font.PLAIN, 13));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}

			@Override
			public void windowOpened(WindowEvent e) {
				thisWindowOpened(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG);
			dialogPane.setFont(new Font("Tahoma", Font.PLAIN, 13));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.setLayout(new FormLayout("default, 10dlu, default:grow", "5*(default, $lgap), default"));

				//---- label1 ----
				label1.setText(bundle.getString("Login.label1.text"));
				label1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.add(label1, CC.xy(1, 1));

				//---- textField1 ----
				textField1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				textField1.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						textField1KeyReleased(e);
					}
				});
				contentPanel.add(textField1, CC.xy(3, 1));

				//---- label2 ----
				label2.setText(bundle.getString("Login.label2.text"));
				label2.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.add(label2, CC.xy(1, 3));

				//---- passwordField1 ----
				passwordField1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				passwordField1.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						passwordField1KeyReleased(e);
					}
				});
				contentPanel.add(passwordField1, CC.xy(3, 3));

				//---- button1 ----
				button1.setText(bundle.getString("Login.button1.text"));
				button1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				contentPanel.add(button1, CC.xy(1, 5));

				//---- textField2 ----
				textField2.setFont(new Font("Tahoma", Font.PLAIN, 13));
				textField2.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						textField2KeyReleased(e);
					}
				});
				contentPanel.add(textField2, CC.xy(3, 5));

				//---- checkBox2 ----
				checkBox2.setText(bundle.getString("Login.checkBox2.text"));
				checkBox2.setSelected(true);
				contentPanel.add(checkBox2, CC.xy(1, 7));

				//---- checkBox1 ----
				checkBox1.setText(bundle.getString("Login.checkBox1.text"));
				checkBox1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				checkBox1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						checkBox1ActionPerformed(e);
					}
				});
				checkBox1.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						checkBox1KeyReleased(e);
					}
				});
				contentPanel.add(checkBox1, CC.xy(3, 7));

				//---- label3 ----
				label3.setText(bundle.getString("Login.label3.text"));
				label3.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.add(label3, CC.xy(1, 9));

				//---- passwordField2 ----
				passwordField2.setEnabled(false);
				passwordField2.setFont(new Font("Tahoma", Font.PLAIN, 13));
				passwordField2.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						passwordField2KeyReleased(e);
					}
				});
				contentPanel.add(passwordField2, CC.xy(3, 9));

				//---- label4 ----
				label4.setText(bundle.getString("Login.label4.text"));
				label4.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.add(label4, CC.xy(1, 11));

				//---- passwordField3 ----
				passwordField3.setEnabled(false);
				passwordField3.setFont(new Font("Tahoma", Font.PLAIN, 13));
				passwordField3.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						passwordField3KeyReleased(e);
					}
				});
				contentPanel.add(passwordField3, CC.xy(3, 11));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
				buttonBar.setFont(new Font("Tahoma", Font.PLAIN, 13));
				buttonBar.setLayout(new FormLayout("$glue, $button, $rgap, $button", "pref"));

				//---- okButton ----
				okButton.setText("OK");
				okButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, CC.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.setIcon(null);
				cancelButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				buttonBar.add(cancelButton, CC.xy(4, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(435, 245);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables

	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JTextField textField1;
	private JLabel label2;
	private JPasswordField passwordField1;
	private JButton button1;
	private JTextField textField2;
	private JCheckBox checkBox2;
	private JCheckBox checkBox1;
	private JLabel label3;
	private JPasswordField passwordField2;
	private JLabel label4;
	private JPasswordField passwordField3;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

}
