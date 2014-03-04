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
package de.bund.bfr.knime;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.util.ColumnComboBoxRenderer;

public class ColumnComboBox extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JComboBox<DataColumnSpec> selectionBox;
	private JCheckBox enableBox;

	public ColumnComboBox(boolean optional, DataColumnSpec[] columns) {
		setLayout(new BorderLayout(5, 5));

		if (columns != null) {
			selectionBox = new JComboBox<DataColumnSpec>(columns);
		} else {
			selectionBox = new JComboBox<DataColumnSpec>();
		}

		add(selectionBox, BorderLayout.CENTER);

		if (optional) {
			enableBox = new JCheckBox();
			enableBox.setSelected(true);
			enableBox.addActionListener(this);
			selectionBox.setEnabled(false);
			add(enableBox, BorderLayout.WEST);
		}

		ColumnComboBoxRenderer renderer = new ColumnComboBoxRenderer();

		renderer.attachTo(selectionBox);
	}

	public ColumnComboBox(boolean optional) {
		this(optional, null);
	}

	public void removeAllColumns() {
		selectionBox.removeAllItems();
	}

	public void addColumn(DataColumnSpec column) {
		selectionBox.addItem(column);
	}

	public DataColumnSpec getSelectedColumn() {
		if (!selectionBox.isEnabled()) {
			return null;
		}

		return (DataColumnSpec) selectionBox.getSelectedItem();
	}

	public void setSelectedColumn(DataColumnSpec column) {
		selectionBox.setEnabled(true);
		selectionBox.setSelectedItem(column);
	}

	public String getSelectedColumnName() {
		DataColumnSpec selection = getSelectedColumn();

		if (selection != null) {
			return selection.getName();
		}

		return null;
	}

	public void setSelectedColumnName(String columnName) {
		for (int i = 0; i < selectionBox.getItemCount(); i++) {
			DataColumnSpec item = selectionBox.getItemAt(i);

			if (item != null && item.getName().equals(columnName)) {
				selectionBox.setEnabled(true);
				selectionBox.setSelectedIndex(i);
				break;
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (enableBox.isSelected()) {
			selectionBox.setEnabled(true);
		} else {
			selectionBox.setEnabled(false);
		}
	}
}
