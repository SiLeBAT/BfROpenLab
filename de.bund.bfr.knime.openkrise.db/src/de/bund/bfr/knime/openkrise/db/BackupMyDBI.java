/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.GuiMessages;

/**
 * @author Armin
 * 
 */
public class BackupMyDBI extends FileFilter {

	public static boolean dbBackup() {
		return dbBackup(DBKernel.mainFrame);
	}

	public static boolean dbBackup(final JFrame frame) {
		String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
		Locale oldLocale = JComponent.getDefaultLocale();
		JComponent.setDefaultLocale(Locale.US);
		JFileChooser fc = new JFileChooser(lastOutDir);
		JComponent.setDefaultLocale(oldLocale);
		BackupMyDBI bkp = new BackupMyDBI();
		fc.setFileFilter(bkp);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setMultiSelectionEnabled(false);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Calendar c1 = Calendar.getInstance();
		fc.setSelectedFile(new File(DBKernel.myDBi.getDbUsername() + "_" + DBKernel.softwareVersion + "_" + sdf.format(c1.getTime()) + ".tar.gz"));
		fc.setDialogTitle("Backup");
		int returnVal = fc.showSaveDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			if (selectedFile != null) {
				DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
				DBKernel.prefs.prefsFlush();
				if (selectedFile.exists()) {
					returnVal = JOptionPane.showConfirmDialog(frame, GuiMessages.getString("Soll die Datei ersetzt werden?"), GuiMessages.getString("Backup Datei bereits vorhanden"), JOptionPane.YES_NO_CANCEL_OPTION);
					if (returnVal == JOptionPane.NO_OPTION) {
						return dbBackup(frame);
					} else if (returnVal == JOptionPane.YES_OPTION) {
						;
					} else {
						return false;
					}
				}
				dbBackup(frame, selectedFile, false);
			}
		} else if (returnVal == JFileChooser.CANCEL_OPTION) {
			return false;
		}
		return true;
	}

	private static void dbBackup(final JFrame frame, final File backupFile, final boolean silent) {
		if (backupFile != null && backupFile.getParentFile().exists()) {
			try {
				if (backupFile.exists()) backupFile.delete();
				System.gc();
				String filename = backupFile.getAbsolutePath();
				if (!filename.endsWith(".tar.gz")) filename += ".tar.gz";

				MyDBTable myDB = (DBKernel.mainFrame.getMyList() == null ? null : DBKernel.mainFrame.getMyList().getMyDBTable());
				if (myDB != null) myDB.checkUnsavedStuff();
				
				String answerErr = DBKernel.myDBi.dbBackup(filename);
				if (!silent) {
					if (answerErr.length() == 0) {
						JOptionPane.showMessageDialog(frame, "In '" + filename + "' " + GuiMessages.getString("wurde erfolgreich ein Backup der Datenbank erstellt!"), "Backup", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(frame, GuiMessages.getString("Das Backup der Datenbank ist fehlgeschlagen!") + "\n" + GuiMessages.getString("Die Fehlermeldung lautet") + ":\n" + answerErr, "Backup", JOptionPane.ERROR_MESSAGE);
					}
				}
				System.gc();

				if (myDB != null) {
					myDB.initConn(DBKernel.myDBi.getConn());
					if (myDB.getActualTable() != null) {
						myDB.getActualTable().restoreProperties(myDB);
					}
					myDB.syncTableRowHeights();
				}

			} catch (Exception e) {
				MyLogger.handleException(e);
			}
		}
		if (DBKernel.isKNIME) {
			DBKernel.mainFrame.dispose();
			DBKernel.openDBGUI();
		}
	}

	public static void doRestore(final MyDBTable myDB) {
		String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
		Locale oldLocale = JComponent.getDefaultLocale();
		JComponent.setDefaultLocale(Locale.US);
		JFileChooser fc = new JFileChooser(lastOutDir);
		JComponent.setDefaultLocale(oldLocale);
		BackupMyDBI bkp = new BackupMyDBI();
		fc.setFileFilter(bkp);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Restore");
		int returnVal = fc.showOpenDialog(DBKernel.mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			if (selectedFile != null) {
				DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
				DBKernel.prefs.prefsFlush();
			}
			doRestore(myDB, selectedFile, false, true);
		}
	}

	public static boolean doRestore(final MyDBTable myDB, final File scriptFile, final boolean silent, boolean doReconnect) {
		boolean result = true;
		if (scriptFile != null && scriptFile.exists()) {
			if (!silent) {
				int returnVal = JOptionPane.showConfirmDialog(DBKernel.mainFrame,
								GuiMessages.getString("Die Datenbank wird geloescht!") + "\n" + GuiMessages.getString("Vielleicht sollten Sie vorher nochmal ein Backup machen...") + "\n"
										+ GuiMessages.getString("Soll das Backup wirklich eingespielt werden?"), GuiMessages.getString("Datenbank loeschen"), JOptionPane.YES_NO_OPTION);
				if (returnVal != JOptionPane.YES_OPTION) return result;
			}

			// Also los!
			if (myDB != null) myDB.checkUnsavedStuff();
			
			String answerErr = DBKernel.myDBi.dbRestore(scriptFile.getAbsolutePath());
			
			try {
				if (doReconnect && !DBKernel.isKNIME) {
					Connection conn = DBKernel.myDBi.getConn();
					if (conn != null) {
						if (myDB != null) {
							myDB.initConn(conn);
							myDB.setTable();
						}
					} else {
						result = false;
					}
				}
				if (!silent && answerErr.length() == 0) {
					JOptionPane.showMessageDialog(DBKernel.mainFrame, GuiMessages.getString("Fertig!"), "Restore", JOptionPane.INFORMATION_MESSAGE);
					if (myDB != null && !DBKernel.isKNIME) {
						myDB.myRefresh();
					}
				}
			} catch (Exception e) {
				if (answerErr.length() > 0) {
					answerErr += "\n";
				}
				answerErr += e.getMessage();
				MyLogger.handleException(e);
			}
			if (!silent && answerErr.length() > 0) {
				JOptionPane.showMessageDialog(DBKernel.mainFrame, GuiMessages.getString("Das Wiederherstellen der Datenbank ist fehlgeschlagen!") + "\n" + GuiMessages.getString("Die Fehlermeldung lautet") + ":\n" + answerErr, "Restore",
						JOptionPane.ERROR_MESSAGE);
			}
			System.gc();
		}
		if (doReconnect && DBKernel.isKNIME) {
			DBKernel.mainFrame.dispose();
			DBKernel.openDBGUI();
		}
		return result;
	}

	@Override
	public boolean accept(final File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		if (extension.equals("tar.gz") || extension.equals("gz.zip")) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return GuiMessages.getString("Backup Datei") + " (*.tar.gz;*.zip)";
	}

	private String getExtension(final File f) {
		String s = f.getName();
		int i = s.lastIndexOf('.');
		int j = s.lastIndexOf('.', i - 1);
		if (j > 0 && j < s.length() - 1) {
			return s.substring(j + 1).toLowerCase();
		} else if (i > 0 && i < s.length() - 1) {
			return s.substring(i + 1).toLowerCase();
		}
		return "";
	}

}
