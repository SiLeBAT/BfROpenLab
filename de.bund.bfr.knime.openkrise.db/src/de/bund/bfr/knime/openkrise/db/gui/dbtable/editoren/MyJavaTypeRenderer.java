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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren;

import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import quick.dbtable.CellComponent;

/**
 * @author Armin
 *
 */
public class MyJavaTypeRenderer extends JTextArea implements CellComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyJavaTypeRenderer() {
		this.setLineWrap(true);
		this.setWrapStyleWord(true);
	}
	
	public void setValue(Object value) { 
		Object[] o;
		if (value == null) this.setText(""); 
		else if (value instanceof Object[] && (o = (Object[]) value).length > 0) {
			String newText = o[0].toString();
			for (int i=1;i<o.length;i++) if (o[i] != null) newText += "\n" + o[i];
			this.setText(newText);				
		}
		else {
			this.setText(value.toString()); 
		}
	}

	public void addActionListener(ActionListener arg0) {
	}

	public JComponent getComponent() {
		return this;
	}

	public Object getValue() {
		return null;
	}

}
