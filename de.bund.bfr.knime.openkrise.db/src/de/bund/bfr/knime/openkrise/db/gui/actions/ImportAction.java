/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui.actions;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.gui.NewInfoBox;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.imports.GeneralXLSImporter;
import de.bund.bfr.knime.openkrise.db.imports.MyImporter;
import de.bund.bfr.knime.openkrise.db.imports.custom.LieferkettenImporterEFSA;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.TraceImporter;

/**
 * @author Armin
 *
 */
public class ImportAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar1;

	public ImportAction(String name, Icon icon, String toolTip, JProgressBar progressBar1) {
		this.progressBar1 = progressBar1;
		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, toolTip);
		putValue(Action.SMALL_ICON, icon);
	}

	public void actionPerformed(ActionEvent e) {
		String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
		Locale oldLocale = JComponent.getDefaultLocale();
		JComponent.setDefaultLocale(Locale.US);
		JFileChooser fc = new JFileChooser(lastOutDir);
		JComponent.setDefaultLocale(oldLocale);
		//	  if (!DBKernel.isKrise) fc.addChoosableFileFilter(new LieferkettenImporterNew());	  	  	  
		if (DBKernel.isAdmin()) fc.addChoosableFileFilter(new GeneralXLSImporter()); //  && !DBKernel.isKNIME	  

		LieferkettenImporterEFSA efsa = new LieferkettenImporterEFSA();
		fc.addChoosableFileFilter(efsa);
		fc.setFileFilter(efsa);
		TraceImporter bti = new TraceImporter();
		fc.addChoosableFileFilter(bti);
		fc.setFileFilter(bti);

		//fc.addChoosableFileFilter(new LieferkettenImporter());	  
		//fc.addChoosableFileFilter(new MethodenADVImporterDOC());
		//fc.addChoosableFileFilter(new SymptomeImporterDOC());
		fc.setAcceptAllFileFilterUsed(false);
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Import");
		try {
			int returnVal = fc.showOpenDialog(progressBar1); // this
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (fc.getFileFilter() instanceof MyImporter) {
					MyImporter mi = (MyImporter) fc.getFileFilter();
					File[] selectedFiles = fc.getSelectedFiles();
					if (selectedFiles != null && selectedFiles.length > 0) {
						if (selectedFiles.length > 1 && mi instanceof TraceImporter) selectedFiles = sortFilesByDate(selectedFiles);
						if (mi instanceof TraceImporter) DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
						boolean ir = true;
						for (File selectedFile : selectedFiles) {
							if (selectedFile != null) {
								boolean lir = doTheImport(mi, selectedFile, false);
								ir = ir && lir;
							}
						}
						if (mi instanceof TraceImporter) {
							if (ir) {
								DBKernel.sendRequest("COMMIT", false);
								DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
								DBKernel.myDBi.getTable("Station").doMNs();
								DBKernel.myDBi.getTable("Produktkatalog").doMNs();
								DBKernel.myDBi.getTable("Chargen").doMNs();
								DBKernel.myDBi.getTable("Lieferungen").doMNs();
								if (progressBar1 != null) {
									// Refreshen:
									MyDBTable myDB = DBKernel.mainFrame.getMyList().getMyDBTable();
									if (myDB.getActualTable() != null) {
										String actTablename = myDB.getActualTable().getTablename();
										if (actTablename.equals("Produktkatalog") || actTablename.equals("Lieferungen") || actTablename.equals("Station")
												|| actTablename.equals("Chargen")) {
											myDB.setTable(myDB.getActualTable());
										}
									}
									progressBar1.setVisible(false);
								}
							} else {
								DBKernel.sendRequest("ROLLBACK", false);
								DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
								if (progressBar1 != null) progressBar1.setVisible(false);
							}
						}
					} else {
						File selectedSingleFile = fc.getSelectedFile();
						if (selectedSingleFile != null) {
							doTheImport(mi, selectedSingleFile, true);
						}
					}
					if (mi instanceof LieferkettenImporterEFSA) {
						efsa = (LieferkettenImporterEFSA) mi;
						efsa.mergeIDs();
						String log = efsa.getLogMessages();
						Font f = new Font("Arial", Font.PLAIN, 10);
						InfoBox ib = new InfoBox(log, true, new Dimension(1000, 750), f);
						ib.setVisible(true);

					} else if (mi instanceof TraceImporter) {
						bti = (TraceImporter) mi;
						String errors = bti.getLogMessages();
						String warnings = bti.getLogWarnings();
						boolean success = errors.isEmpty();
						if (success && warnings.isEmpty()) {
							JOptionPane.showMessageDialog(DBKernel.mainFrame, "Import successful!", "Import successful", JOptionPane.INFORMATION_MESSAGE);
						} else if (!success) {
							JOptionPane.showOptionDialog(DBKernel.mainFrame, "Errors occured, no files were imported!\nPlease correct errors and try again", "Import failed",
									JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] {"Show Details"}, null);
							NewInfoBox.show(DBKernel.mainFrame, "Errors and Warnings", "<html>" + errors + warnings + "</html>");
						} else {
							JOptionPane.showOptionDialog(DBKernel.mainFrame, "Import successful! But some warnings occurred, please check", "Import with Warnings",
									JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] {"Show Details"}, null);
							NewInfoBox.show(DBKernel.mainFrame, "Warnings", "<html>" + warnings + "</html>");
						}
					}
				}
			}
		} catch (Exception e1) {
			MyLogger.handleMessage(fc + "\t" + lastOutDir);
			MyLogger.handleException(e1);
		}
		if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		MyLogger.handleMessage("Importing - FinFin!");
	}

	private boolean doTheImport(MyImporter mi, File selectedFile, boolean showResults) {
		DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
		DBKernel.prefs.prefsFlush();
		boolean result = mi.doImport(selectedFile.getAbsolutePath(), progressBar1, showResults);
		MyLogger.handleMessage("Importing - Fin!");
		return result;
	}

	private File[] sortFilesByDate(File[] files) {
		HashMap<Long, List<File>> hm = new HashMap<>();
		for (File f : files) {
			Long l = TraceImporter.getMillis(null, f.getAbsolutePath());
			if (!hm.containsKey(l)) hm.put(l, new ArrayList<File>());
			List<File> lf = hm.get(l);
			lf.add(f);
		}
		File[] result = new File[files.length];
		SortedSet<Long> keys = new TreeSet<Long>(hm.keySet());
		int i = 0;
		for (Long l : keys) {
			List<File> lf = hm.get(l);
			for (File f : lf) {
				result[i] = f;
				i++;
			}
		}
		return result;
	}
}
