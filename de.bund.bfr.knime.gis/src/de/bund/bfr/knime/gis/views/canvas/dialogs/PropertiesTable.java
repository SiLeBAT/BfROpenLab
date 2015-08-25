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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.PropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.ui.BooleanObjectCellRenderer;
import de.bund.bfr.knime.ui.DoubleCellRenderer;

public class PropertiesTable extends JTable {

	private static final long serialVersionUID = 1L;

	private List<? extends Element> elements;

	public PropertiesTable(List<? extends Element> elements, PropertySchema schema, Set<String> idColumns) {
		this.elements = elements;

		List<String> columnNames = new ArrayList<>();
		List<Class<?>> columnTypes = new ArrayList<>();
		List<List<Object>> columnValueTuples = new ArrayList<>();

		for (Map.Entry<String, Class<?>> entry : schema.getMap().entrySet()) {
			columnNames.add(entry.getKey());
			columnTypes.add(entry.getValue());
		}

		for (Element element : elements) {
			List<Object> tuple = new ArrayList<>();

			for (String property : columnNames) {
				tuple.add(element.getProperties().get(property));
			}

			columnValueTuples.add(tuple);
		}

		setModel(new PropertiesTableModel(columnNames, columnTypes, columnValueTuples));
		setRowSorter(new IdSorter(getModel(), idColumns));
		setRowHeight(new JCheckBox().getPreferredSize().height);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setDefaultRenderer(Boolean.class, new BooleanObjectCellRenderer());
		setDefaultRenderer(Double.class, new DoubleCellRenderer());
		setTransferHandler(new TableTransferHandler(this));
		UI.packColumns(this, 200);
	}

	public Set<Element> getSelectedElements() {
		Set<Element> selected = new LinkedHashSet<>();

		for (int index : getSelectedRows()) {
			selected.add(elements.get(convertRowIndexToModel(index)));
		}

		return selected;
	}

	private static class PropertiesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<String> columnNames;
		private List<Class<?>> columnTypes;
		private List<List<Object>> columnValueTuples;

		public PropertiesTableModel(List<String> columnNames, List<Class<?>> columnTypes,
				List<List<Object>> columnValueTuples) {
			this.columnNames = columnNames;
			this.columnTypes = columnTypes;
			this.columnValueTuples = columnValueTuples;
		}

		@Override
		public int getColumnCount() {
			return columnNames.size();
		}

		@Override
		public int getRowCount() {
			return columnValueTuples.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return columnValueTuples.get(rowIndex).get(columnIndex);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			columnValueTuples.get(rowIndex).set(columnIndex, aValue);
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public String getColumnName(int column) {
			return columnNames.get(column);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnTypes.get(columnIndex);
		}
	}
}
