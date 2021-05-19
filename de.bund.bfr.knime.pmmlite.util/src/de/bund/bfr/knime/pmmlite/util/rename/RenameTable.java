/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.util.rename;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import com.google.common.base.Strings;

import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.ui.TableTransferHandler;

public class RenameTable extends JScrollPane {

	private static final long serialVersionUID = 1L;

	private static String ORIG_NAME = "Original Name";
	private static String NEW_NAME = "New Name";

	private JTable table;
	private RenameTableModel tableModel;

	public RenameTable(List<? extends Identifiable> objects) {
		tableModel = new RenameTableModel(objects);
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setCellSelectionEnabled(true);
		table.setTransferHandler(new TableTransferHandler());

		setViewportView(table);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	public void stopEditing() {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
	}

	public Map<String, String> getNewNames() {
		return tableModel.getNewNames();
	}

	public void setNewNames(Map<String, String> newNames) {
		tableModel.setNewNames(newNames);
	}

	public void clear() {
		setNewNames(new LinkedHashMap<>(0));
	}

	private static class RenameTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<? extends Identifiable> objects;
		private Map<String, String> newNames;

		public RenameTableModel(List<? extends Identifiable> objects) {
			this.objects = objects;
			newNames = new LinkedHashMap<>();
		}

		public Map<String, String> getNewNames() {
			return newNames;
		}

		public void setNewNames(Map<String, String> newNames) {
			this.newNames = newNames;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return objects.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Identifiable obj = objects.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return obj.getName();
			case 1:
				return newNames.get(obj.getId());
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
			}
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return ORIG_NAME;
			case 1:
				return NEW_NAME;
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + column);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				String value = Strings.emptyToNull(Strings.nullToEmpty((String) aValue).trim());

				newNames.put(objects.get(rowIndex).getId(), value);
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}
}
