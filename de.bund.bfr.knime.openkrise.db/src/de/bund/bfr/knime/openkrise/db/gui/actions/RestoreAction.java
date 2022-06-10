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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import de.bund.bfr.knime.openkrise.db.Backup;
import de.bund.bfr.knime.openkrise.db.BackupMyDBI;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;

/**
 * @author Armin
 *
 */
public class RestoreAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -759785558573347371L;
	private MyDBTable myDB;

	public RestoreAction(String name, Icon icon, String toolTip, MyDBTable myDB) {
  	this.myDB = myDB;
    putValue(Action.NAME, name);
    putValue(Action.SHORT_DESCRIPTION, toolTip);
    putValue(Action.SMALL_ICON, icon);
  }    

  public void actionPerformed(ActionEvent e) {
	  try {
		  if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		  if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) BackupMyDBI.doRestore(myDB);
		  else Backup.doRestore(myDB);
	  }
	  finally {
		  if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getDefaultCursor());
	  }
	}
}
