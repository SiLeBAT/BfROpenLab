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
package de.bund.bfr.knime.openkrise.util.tracing;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import de.bund.bfr.knime.ui.DoubleCellRenderer;

public class InputTable extends JTable {

	private static final long serialVersionUID = 1L;

	private static final String INPUT_COLUMN = "Input";

	public InputTable(final Class<?> type, final int rowCount) {
		super(new DefaultTableModel(new Object[] { INPUT_COLUMN }, rowCount) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return type;
			}
		});

		setRowHeight(new JCheckBox().getPreferredSize().height);
		setDefaultRenderer(Double.class, new DoubleCellRenderer());
		setTransferHandler(null);
	}

	@Override
	public boolean editCellAt(int row, int column, EventObject e) {
		boolean result = super.editCellAt(row, column, e);
		Component editor = getEditorComponent();

		if (editor instanceof JTextComponent) {
			((JTextComponent) editor).selectAll();
		}

		return result;
	}
}
