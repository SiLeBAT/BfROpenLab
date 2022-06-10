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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren;

import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;

import quick.dbtable.CellComponent;

/**
 * @author Armin
 *
 */
public class MyBlobSizeRenderer extends JLabel implements CellComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DecimalFormat df = new DecimalFormat("0.00");

	public MyBlobSizeRenderer() {
		this.setHorizontalAlignment(JLabel.CENTER);
		this.setVerticalAlignment(JLabel.TOP);
	}
	public void setValue(Object value) { 
		if (value == null) this.setText(""); 
		else if (value instanceof Integer) {
			int size = (Integer) value;
			if (size < 1024) this.setText(size + " B");
			else if (size < 1024*1024) this.setText(df.format(size / 1024.0) + " kB");
			else this.setText(df.format(size / 1024.0 / 1024) + " MB");
		}
		else this.setText(value.toString()); 
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
