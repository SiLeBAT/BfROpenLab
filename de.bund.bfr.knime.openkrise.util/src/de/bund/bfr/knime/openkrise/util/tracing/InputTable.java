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
package de.bund.bfr.knime.openkrise.util.tracing;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;

import de.bund.bfr.knime.ui.DoubleCellRenderer;

public class InputTable extends JTable {

	public static final String INPUT_COLUMN = "Input";

	private static final long serialVersionUID = 1L;

	public InputTable(Class<?> type, int rowCount) {
		super(new InputTableModel(type, rowCount));
		setRowHeight(new JCheckBox().getPreferredSize().height);
		setDefaultRenderer(Double.class, new DoubleCellRenderer());
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

	private static class InputTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private Class<?> type;
		private Object[] values;

		public InputTableModel(Class<?> type, int rowCount) {
			this.type = type;
			values = new Object[rowCount];
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return values.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return values[rowIndex];
		}

		@Override
		public String getColumnName(int column) {
			return INPUT_COLUMN;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return type;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			values[rowIndex] = aValue;
			fireTableCellUpdated(rowIndex, columnIndex);
		}

	}
}
