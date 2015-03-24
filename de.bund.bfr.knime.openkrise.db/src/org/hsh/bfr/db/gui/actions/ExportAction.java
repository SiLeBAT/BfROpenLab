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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.exports.ExcelExport;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;

/**
 * @author Armin
 *
 */
public class ExportAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar1;
	private MyDBTable myDB;

	public ExportAction(String name, Icon icon, String toolTip, JProgressBar progressBar1, MyDBTable myDB) {
  	this.progressBar1 = progressBar1;
  	this.myDB = myDB;
    putValue(Action.NAME, name);
    putValue(Action.SHORT_DESCRIPTION, toolTip);
    putValue(Action.SMALL_ICON, icon);
  }    

  public void actionPerformed(ActionEvent e) {
		int retVal = JOptionPane.showConfirmDialog(DBKernel.mainFrame, "Soll der Export den Volltext beinhalten?\n\nJa:\nAlle Felder werden im Volltext abgespeichert.\n\nNein:\nFür (die grauen) \"Fremdfelder\" werden nur die VerlinkungsIDs abgespeichert.",
				"Excel Export - Wie?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		String retValStr = ""; // Bei Gelegenheit mal aktivieren (parsen!) hier!
		/*(String)JOptionPane.showInputDialog(
				DBKernel.mainFrame,
                "Welche Zeilen sollen exportiert werden (Default: alle, Syntax: 1;3;5-12)?",
                "Excel Export - Was genau?",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");*/
		String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
	  JFileChooser fc = new JFileChooser(lastOutDir);
	  ExcelExport xls = new ExcelExport();
	  fc.setFileFilter(xls);
	  fc.setAcceptAllFileFilterUsed(false);
	  fc.setMultiSelectionEnabled(false);
	  fc.setSelectedFile(new File(myDB.getActualTable().getTablename() + ".xls"));
	  fc.setDialogTitle("Export");
	  int returnVal = fc.showSaveDialog(progressBar1);// MainFrame
	  if(returnVal == JFileChooser.APPROVE_OPTION) {
	  	File selectedFile = fc.getSelectedFile();
	  	if (selectedFile != null) {
	  		DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
			DBKernel.prefs.prefsFlush();
	  		if(selectedFile.exists()) {
	  			returnVal = JOptionPane.showConfirmDialog(progressBar1, "Soll die Datei ersetzt werden?", "Excel Datei bereits vorhanden", JOptionPane.YES_NO_CANCEL_OPTION);
	  			if (returnVal == JOptionPane.NO_OPTION) {actionPerformed(null); return;}
	  			else if (returnVal == JOptionPane.YES_OPTION) ;
	  			else return;
	  		}
	  		xls.doExport(selectedFile.getAbsolutePath(), myDB, progressBar1, retVal == JOptionPane.YES_OPTION, retValStr);
	  	}
	  }
	}
}
