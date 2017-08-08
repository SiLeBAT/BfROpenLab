/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.exports.ExcelExport;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;

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
		int retVal = JOptionPane.showConfirmDialog(
				DBKernel.mainFrame,
				"Export in full text?\n\nYes:\nAll fields will be saved in full text mode (corresponding to their visibility).\n\nNo:\nThe gray \"foreign fields\" will be saved as they are in the db (IDs -> linking numbers).",
				"Excel Export - Which mode?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		String retValStr = ""; // Bei Gelegenheit mal aktivieren (parsen!) hier!
		/*
		 * (String)JOptionPane.showInputDialog(
		 * DBKernel.mainFrame,
		 * "Welche Zeilen sollen exportiert werden (Default: alle, Syntax: 1;3;5-12)?",
		 * "Excel Export - Was genau?",
		 * JOptionPane.PLAIN_MESSAGE,
		 * null,
		 * null,
		 * "");
		 */
		String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
		Locale oldLocale = JComponent.getDefaultLocale();
		JComponent.setDefaultLocale(Locale.US);
		JFileChooser fc = new JFileChooser(lastOutDir);
		JComponent.setDefaultLocale(oldLocale);
		ExcelExport xls = new ExcelExport();
		fc.setFileFilter(xls);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setMultiSelectionEnabled(false);
		fc.setSelectedFile(new File(myDB.getActualTable().getTablename() + ".xls"));
		fc.setDialogTitle("Export");
		int returnVal = fc.showSaveDialog(progressBar1);// MainFrame
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			if (selectedFile != null) {
				DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
				DBKernel.prefs.prefsFlush();
				if (selectedFile.exists()) {
					returnVal = JOptionPane.showConfirmDialog(progressBar1, "Replace file?", "Excel file exists already", JOptionPane.YES_NO_CANCEL_OPTION);
					if (returnVal == JOptionPane.NO_OPTION) {
						actionPerformed(null);
						return;
					} else if (returnVal == JOptionPane.YES_OPTION)
					;
					else return;
				}
				xls.doExport(selectedFile.getAbsolutePath(), myDB, progressBar1, retVal == JOptionPane.YES_OPTION, retValStr);
			}
		}
	}
}
