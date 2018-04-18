/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.UnitDialog;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;

public class UnitTable<T extends NameableWithUnit> extends JTable {

	public static final String NAME_COLUMN = "Name";
	public static final String UNIT_COLUMN = "Unit";

	private static final long serialVersionUID = 1L;

	private UnitTableModel<T> model;

	public UnitTable(List<T> elements) {
		model = new UnitTableModel<>(elements);
		setModel(model);
		getColumn(UNIT_COLUMN).setCellRenderer(new UnitRenderer());
		getColumn(UNIT_COLUMN).setCellEditor(new UnitEditor());
	}

	public List<T> getElements() {
		return model.getElements();
	}

	public void setElements(List<T> elements) {
		model.setElements(elements);
		repaint();
	}

	private static class UnitTableModel<T extends NameableWithUnit> extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<T> elements;

		public UnitTableModel(List<T> elements) {
			this.elements = elements;
		}

		public List<T> getElements() {
			return elements;
		}

		public void setElements(List<T> elements) {
			this.elements = elements;
		}

		@Override
		public int getRowCount() {
			return elements.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			T element = elements.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return element.getName();
			case 1:
				return element.getUnit();
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
			}
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return NAME_COLUMN;
			case 1:
				return UNIT_COLUMN;
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + column);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
				return PmmUnit.class;
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				elements.get(rowIndex).setUnit((PmmUnit) aValue);
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}

	}

	private static class UnitRenderer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			PmmUnit unit = (PmmUnit) value;

			if (unit != null) {
				return new JLabel(unit.toString());
			}

			return new JLabel("");
		}
	}

	private static class UnitEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		private static final long serialVersionUID = 1L;

		private PmmUnit unit;
		private JButton unitButton;

		public UnitEditor() {
			unit = new PmmUnit.Builder().build();
			unitButton = new JButton();
			unitButton.addActionListener(this);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			unit = (PmmUnit) value;
			unitButton.setText(unit.toString());

			return unitButton;
		}

		@Override
		public Object getCellEditorValue() {
			return unit;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			UnitDialog dialog = new UnitDialog(unitButton, unit);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				unit = dialog.getUnit();
				unitButton.setText(unit != null ? unit.toString() : "");
				stopCellEditing();
			} else {
				cancelCellEditing();
			}
		}
	}
}
