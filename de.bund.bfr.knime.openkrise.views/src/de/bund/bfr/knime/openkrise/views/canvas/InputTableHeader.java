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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;

import de.bund.bfr.knime.openkrise.TracingColumns;

public class InputTableHeader extends JPanel {

	private static final long serialVersionUID = 1L;

	public InputTableHeader() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(getTableHeaderComponent(TracingColumns.WEIGHT));
		add(getTableHeaderComponent(TracingColumns.CROSS_CONTAMINATION));
		add(getTableHeaderComponent(TracingColumns.KILL_CONTAMINATION));
		add(getTableHeaderComponent(TracingColumns.OBSERVED));
	}

	private static Component getTableHeaderComponent(String name) {
		JTable table = new JTable(new Object[1][1], new Object[] { name });

		return table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, name, false, false, 0,
				0);
	}
}
