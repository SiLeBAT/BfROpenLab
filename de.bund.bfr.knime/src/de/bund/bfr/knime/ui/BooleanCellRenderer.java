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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class BooleanCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JPanel panel = new JPanel();
		Boolean v = (Boolean) value;
		Color color;

		if (isSelected) {
			color = UIManager.getDefaults()
					.getColor("List.selectionBackground");
		} else {
			color = table.getBackground();
		}

		panel.setBackground(color);

		if (v != null) {
			JCheckBox box = new JCheckBox();

			if (v.booleanValue()) {
				box.setSelected(true);
			} else {
				box.setSelected(false);
			}

			box.setBackground(color);

			panel.setLayout(new GridBagLayout());
			panel.add(box, new GridBagConstraints(0, 0, 1, 1, 0, 0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
		}

		return panel;
	}

}
