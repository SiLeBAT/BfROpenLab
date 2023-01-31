/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JProgressBar;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearchJFrame;

/**
 * @author Armin
 *
 */
public class PlausibleAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5441488921384865553L;
	private MyDBTable myDB;

	public PlausibleAction(final String name, final Icon icon, final String toolTip, final JProgressBar progressBar1, final MyDBTable myDB) {
	  	this.myDB = myDB;
	    putValue(Action.NAME, name);
	    putValue(Action.SHORT_DESCRIPTION, toolTip);
	    putValue(Action.SMALL_ICON, icon);
	    this.setEnabled(false);
	}    

	@Override
	public void actionPerformed(final ActionEvent e) {
	  
		final SimSearchJFrame simSearchFrame = new SimSearchJFrame(DBKernel.mainFrame);
		
		simSearchFrame.setVisible(true);
		
		return;  
	}
}
