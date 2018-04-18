/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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

import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBPanel;

/**
 * @author Armin
 *
 */
public class FindAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MyDBPanel myDBPanel = null;
	
  public FindAction(MyDBPanel myDBPanel) {
  	this.myDBPanel = myDBPanel;
  }    

  public void actionPerformed(ActionEvent e) {
  	if (myDBPanel != null) {
  		myDBPanel.getSuchfeld().grabFocus();
  	}
  }
}
