/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
package de.bund.bfr.knime.ui;

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class DoubleCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	public DoubleCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	public void setValue(Object value) {
		setText((value == null) ? "" : NumberFormat.getInstance(Locale.US)
				.format(value));
	}
}
