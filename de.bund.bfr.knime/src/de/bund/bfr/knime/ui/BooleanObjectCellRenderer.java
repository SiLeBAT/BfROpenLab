/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import de.bund.bfr.knime.UI;

public class BooleanObjectCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JPanel panel = new JPanel();
		Color color = isSelected ? table.getSelectionBackground() : table.getBackground();

		panel.setBackground(color);
		panel.setBorder(hasFocus ? UI.TABLE_CELL_FOCUS_BORDER : UI.TABLE_CELL_BORDER);

		if (value instanceof Boolean) {
			JCheckBox box = new JCheckBox();

			box.setSelected((Boolean) value);
			box.setBackground(color);

			panel.setLayout(new GridBagLayout());
			panel.add(box, UI.centerConstraints(0, 0));
		}

		return panel;
	}
}
