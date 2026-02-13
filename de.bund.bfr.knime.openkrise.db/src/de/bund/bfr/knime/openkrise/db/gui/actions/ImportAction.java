/*******************************************************************************
 * Copyright (c) 2014-2025 German Federal Institute for Risk Assessment (BfR)
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
import java.util.function.BiConsumer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import de.bund.bfr.knime.UserCancelException;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.gui.NewInfoBox;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.imports.GeneralXLSImporter;
import de.bund.bfr.knime.openkrise.db.imports.MyImporter;
import de.bund.bfr.knime.openkrise.db.imports.custom.LieferkettenImporterEFSA;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.TraceImporter;
import de.bund.bfr.knime.ui.ProgressDialog;


/**
 * @author Armin
 *
 */
public class ImportAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static class TaskResult <T> {
		public Throwable throwable = null;
		public boolean canceled = false;
		public T result = null;
	}
	
	private JProgressBar progressBar1;

	public ImportAction(String name, Icon icon, String toolTip, JProgressBar progressBar1) {
		this.progressBar1 = progressBar1;
		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, toolTip);
		putValue(Action.SMALL_ICON, icon);
	}
	
	private static void addFileChooserFilter(JFileChooser fc) {
//		  if (!DBKernel.isKrise) fc.addChoosableFileFilter(new LieferkettenImporterNew());	  	  	  
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
	}
	
	
	private void commitTA() {
		DBKernel.sendRequest("COMMIT", false);
		DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
		DBKernel.myDBi.getTable("Station").doMNs();
		DBKernel.myDBi.getTable("Produktkatalog").doMNs();
		DBKernel.myDBi.getTable("Chargen").doMNs();
		DBKernel.myDBi.getTable("Lieferungen").doMNs();
	}
	
	private void rollbackTA() {
		System.out.println("rolling back ...");
		DBKernel.sendRequest("ROLLBACK", false);
		DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
		System.out.println("rolling back complete");
	}
	
	private void refreshTableView() {
		MyDBTable myDB = DBKernel.mainFrame.getMyList().getMyDBTable();
		if (myDB.getActualTable() != null) {
			String actTablename = myDB.getActualTable().getTablename();
			if (actTablename.equals("Produktkatalog") || actTablename.equals("Lieferungen") || actTablename.equals("Station")
					|| actTablename.equals("Chargen")) {
				myDB.setTable(myDB.getActualTable());
			}
		}
	}
	
	private void reportTraceImportResult(TraceImporter traceImporter, TaskResult<Boolean> taskResult) {
		String errors = traceImporter.getLogMessages();
		String warnings = traceImporter.getLogWarnings();
		boolean success = errors.isEmpty();
		boolean result = taskResult.result;
		if (!result && taskResult.canceled) {
			// show nothing
		} else if (!result && taskResult.throwable != null) {
			JOptionPane.showMessageDialog(DBKernel.mainFrame, taskResult.throwable.getMessage(), "Import failed!",  JOptionPane.ERROR_MESSAGE);
		} else if (result && success && warnings.isEmpty()) {
			JOptionPane.showMessageDialog(DBKernel.mainFrame, "Import successful!", "Import successful", JOptionPane.INFORMATION_MESSAGE);
		} else if (!success) {
			JOptionPane.showOptionDialog(DBKernel.mainFrame, "Errors occured, no files were imported!\nPlease correct errors and try again", "Import failed",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] {"Show Details"}, null);
			NewInfoBox.show(DBKernel.mainFrame, "Errors and Warnings", "<html>" + errors + warnings + "</html>");
		} else if (!result) {
			JOptionPane.showOptionDialog(DBKernel.mainFrame, "Import successful! But some warnings occurred, please check", "Import with Warnings",
					JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] {"Show Details"}, null);
			NewInfoBox.show(DBKernel.mainFrame, "Warnings", "<html>" + warnings + "</html>");
		}
	}

	private void runSharedPreImportOps() {
		if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	private void runSharedPostImportOps() {
		if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		MyLogger.handleMessage("Importing - FinFin!");
	}	
	
	private void handleTraceImport(final TraceImporter traceImporter, File[] selectedFiles) {
		if (selectedFiles == null || selectedFiles.length == 0) return;
		
		final File[] sortedFiles = sortFilesByDate(selectedFiles);
		if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
		
		ProgressDialog progressDialog = ProgressDialog.showDialog(DBKernel.mainFrame, "Progress");
		
		BiConsumer<Boolean, TaskResult<Boolean>> postTaskCallback = (result, taskResult) -> {
			System.out.println("postTaskCallback entered (result: " + result + ") ...");
			// post task things
			if (result) {
				commitTA();
				if (progressBar1 != null) {
					// Refreshen:
					refreshTableView();
				}
			} else {
				rollbackTA();
			}
			progressDialog.close();
		
			reportTraceImportResult(traceImporter, taskResult);
			runSharedPostImportOps();
		};
		
		final TaskResult<Boolean> taskResult = new TaskResult<>();
		
		SwingWorker<Boolean, Object> sw = new SwingWorker<>() {

			@Override
			protected Boolean doInBackground() throws Exception {
				System.out.println("doInBackground entered ...");
				taskResult.result = false;
				try {
					int iFile = -1;
					for (File selectedFile : sortedFiles) {
						iFile++;
						DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
						DBKernel.prefs.prefsFlush();
						String infix = sortedFiles.length == 1  ? "" : " (file " + (iFile + 1) + " of " + sortedFiles.length + ")";
						progressDialog.setMessage("Importing '" + selectedFile.getName() + "'" + infix + " ...");
						progressDialog.setProgress(0);
						boolean result = traceImporter.importFile(selectedFile.getAbsolutePath(), DBKernel.mainFrame, progressDialog);
						MyLogger.handleMessage("Importing - Fin!");
						if (!result) return false;
					}
					taskResult.result = true;
					return true;
				} 
				catch(UserCancelException e) {
					taskResult.canceled = true;
				} catch(Exception | Error e) {
					taskResult.throwable = e;
				}
				return false;
			}
			
			@Override
			protected void done() {
				System.out.println("done entered ...");
				try {
					postTaskCallback.accept(taskResult.result, taskResult);
				}
				catch (Exception e) {
					System.out.println("postTaskCallback failed");
					e.printStackTrace();
				}
			}
			
		};
		
		sw.execute();
		return;
	}
	
	
	private void handleNonTraceImport(MyImporter myImporter, File[] selectedFiles) {
		
		boolean ir = true;
		for (File selectedFile : selectedFiles) {
			if (selectedFile != null) {
				boolean lir = doTheImport(myImporter, selectedFile, false);
				ir = ir && lir;
			}
		}

	
		if (myImporter instanceof LieferkettenImporterEFSA) {
			LieferkettenImporterEFSA efsaImporter = (LieferkettenImporterEFSA) myImporter;
			efsaImporter.mergeIDs();
			String log = efsaImporter.getLogMessages();
			Font f = new Font("Arial", Font.PLAIN, 10);
			InfoBox ib = new InfoBox(log, true, new Dimension(1000, 750), f);
			ib.setVisible(true);
	
		} 
		
		runSharedPostImportOps();
	}

	public void actionPerformed(ActionEvent e) {
		String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
		Locale oldLocale = JComponent.getDefaultLocale();
		JComponent.setDefaultLocale(Locale.US);
		JFileChooser fc = new JFileChooser(lastOutDir);
		JComponent.setDefaultLocale(oldLocale);
		
		addFileChooserFilter(fc);

		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Import");
		try {
			int returnVal = fc.showOpenDialog(progressBar1); // this
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				if (fc.getFileFilter() instanceof MyImporter) {
					MyImporter myImporter = (MyImporter) fc.getFileFilter();
					File[] selectedFiles = fc.getSelectedFiles();
					
					if (selectedFiles != null && selectedFiles.length > 0) {
						
						runSharedPreImportOps();
						
						if (myImporter instanceof TraceImporter) {
							handleTraceImport((TraceImporter) myImporter, selectedFiles);
						} else {
							handleNonTraceImport(myImporter, selectedFiles);
						}
					}
				}
			}
		} catch (Exception e1) {
			MyLogger.handleMessage(fc + "\t" + lastOutDir);
			MyLogger.handleException(e1);	
		}
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
