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
/**
 * 
 */
package org.hsh.bfr.db.gui.dbtable.editoren;

import javax.swing.JComponent;
import quick.dbtable.*;

import org.freixas.jcalendar.DateEvent;
import org.freixas.jcalendar.DateListener;
import org.freixas.jcalendar.JCalendarCombo;
import java.util.Calendar;
import java.util.Locale;

public class MyCellEditorDate extends JCalendarCombo implements CellComponent, DateListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 676858444625729903L;
	private boolean dateChanged = false;

	public MyCellEditorDate() {
		super(Calendar.getInstance(Locale.GERMAN), Locale.GERMAN, JCalendarCombo.DISPLAY_DATE, true); //  | JCalendarCombo.DISPLAY_TIME
		dateChanged = false;
		this.addDateListener(this);
	}
	
  public void setValue(Object value) {
  	if (value != null && value.toString().length() > 0) {
  		this.setDate((java.util.Date) value);
  	}
  	else {
  		dateChanged = false;  		
  	}
  }

  public Object getValue() {
    return dateChanged ? this.getDate() : null;
  }

  public JComponent getComponent() {
    return this;
  }

  public void addActionListener(java.awt.event.ActionListener listener) {
    this.addActionListener(listener);
  }

@Override
public void dateChanged(DateEvent arg0) {
	dateChanged = true;
}
}
