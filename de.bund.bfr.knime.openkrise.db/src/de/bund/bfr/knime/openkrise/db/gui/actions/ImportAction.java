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
package de.bund.bfr.knime.openkrise.db.gui.actions;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.imports.GeneralXLSImporter;
import de.bund.bfr.knime.openkrise.db.imports.MyImporter;
import de.bund.bfr.knime.openkrise.db.imports.custom.LieferkettenImporterEFSA;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.BackTraceImporter;

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
	  JFileChooser fc = new JFileChooser(lastOutDir);
//	  if (!DBKernel.isKrise) fc.addChoosableFileFilter(new LieferkettenImporterNew());	  	  	  
	  if (DBKernel.isAdmin()) fc.addChoosableFileFilter(new GeneralXLSImporter()); //  && !DBKernel.isKNIME	  
	  if (DBKernel.isKrise) {
		  BackTraceImporter bti = new BackTraceImporter(); fc.addChoosableFileFilter(bti); fc.setFileFilter(bti);
		  LieferkettenImporterEFSA efsa = new LieferkettenImporterEFSA(); fc.addChoosableFileFilter(efsa); fc.setFileFilter(efsa);
	  }
	  
	  //fc.addChoosableFileFilter(new LieferkettenImporter());	  
	  //fc.addChoosableFileFilter(new MethodenADVImporterDOC());
	  //fc.addChoosableFileFilter(new SymptomeImporterDOC());
	  fc.setAcceptAllFileFilterUsed(false);
	  fc.setMultiSelectionEnabled(DBKernel.isKrise);
	  fc.setDialogTitle("Import");
	  try {
		  int returnVal = fc.showOpenDialog(progressBar1); // this
		  if (returnVal == JFileChooser.APPROVE_OPTION) {
		  		if (fc.getFileFilter() instanceof MyImporter) {
		  			MyImporter mi = (MyImporter) fc.getFileFilter();
				  	File[] selectedFiles = fc.getSelectedFiles();
				  	if (selectedFiles != null && selectedFiles.length > 0) {
				  		for (File selectedFile : selectedFiles) {
						  	if (selectedFile != null) {
						  		doTheImport(mi, selectedFile, false);
						  	}
				  		}
			  		}
				  	else {
					  	File selectedSingleFile = fc.getSelectedFile();
				  		if (selectedSingleFile != null) {
					  		doTheImport(mi, selectedSingleFile, true);
				  		}
				  	}
					if (mi instanceof LieferkettenImporterEFSA) {
						LieferkettenImporterEFSA efsa = (LieferkettenImporterEFSA) mi;
						efsa.mergeIDs();
						String log = efsa.getLogMessages();
						Font f = new Font("Arial", Font.PLAIN, 10);
						InfoBox ib = new InfoBox(log, true, new Dimension(1000, 750), f);
						ib.setVisible(true);
						
					}
					else if (mi instanceof BackTraceImporter) {
						BackTraceImporter bti = (BackTraceImporter) mi;
						String log = bti.getLogMessages();
						Font f = new Font("Arial", Font.PLAIN, 12);
						InfoBox ib = new InfoBox(log, true, new Dimension(1000, 750), f);
						ib.setVisible(true);
						
					}
			  	}
		  }	  
	  }
	  catch (Exception e1) {
		  MyLogger.handleMessage(fc + "\t" + lastOutDir);
		  MyLogger.handleException(e1);
	  }
		MyLogger.handleMessage("Importing - FinFin!");
	}
  private void doTheImport(MyImporter mi, File selectedFile, boolean showResults) {
		DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
		DBKernel.prefs.prefsFlush();
		mi.doImport(selectedFile.getAbsolutePath(), progressBar1, showResults);
		MyLogger.handleMessage("Importing - Fin!");
  }
}
