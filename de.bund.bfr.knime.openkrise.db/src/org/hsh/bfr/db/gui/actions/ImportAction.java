/*******************************************************************************
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Thöns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * Jörgen Brandt (BfR)
 * Annemarie Käsbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
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
 ******************************************************************************/
package org.hsh.bfr.db.gui.actions;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.gui.InfoBox;
import org.hsh.bfr.db.imports.GeneralXLSImporter;
import org.hsh.bfr.db.imports.MyImporter;
import org.hsh.bfr.db.imports.custom.LieferkettenImporterEFSA;
import org.hsh.bfr.db.imports.custom.MyProzessXMLImporter;
import org.hsh.bfr.db.imports.custom.MyRisImporter;

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
	  fc.addChoosableFileFilter(new MyProzessXMLImporter());
	  MyRisImporter myRis = new MyRisImporter();
	  fc.addChoosableFileFilter(myRis);
	  if (DBKernel.isAdmin()) fc.addChoosableFileFilter(new GeneralXLSImporter()); //  && !DBKernel.isKNIME
	  fc.setFileFilter(myRis);
	  if (DBKernel.isKrise) {LieferkettenImporterEFSA efsa = new LieferkettenImporterEFSA(); fc.addChoosableFileFilter(efsa); fc.setFileFilter(efsa);}
	  
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
