/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.core;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;

import de.bund.bfr.knime.pmmlite.core.data.DataFactory;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint;
import de.bund.bfr.knime.ui.DoubleCellRenderer;
import de.bund.bfr.knime.ui.TableTransferHandler;

public class DataTable extends JScrollPane {

	private static final long serialVersionUID = 1L;

	private JTable table;

	private int rowCount;
	private boolean xEditable;
	private boolean yEditable;
	private String xName;
	private List<String> yNames;

	public DataTable(List<TimeSeriesPoint> data) {
		this(data.size(), false, false, PmmUtils.TIME, Arrays.asList(PmmUtils.CONCENTRATION));
		setData(data);
	}

	public DataTable(int rowCount, boolean xEditable, boolean yEditable, String xName, List<String> yNames) {
		this.rowCount = rowCount;
		this.xEditable = xEditable;
		this.yEditable = yEditable;
		this.xName = xName;
		this.yNames = yNames;

		table = new JTable(new DataTableModel()) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean result = super.editCellAt(row, column, e);
				Component editor = getEditorComponent();

				if (editor instanceof JTextComponent) {
					((JTextComponent) editor).selectAll();
				}

				return result;
			}
		};

		table.setModel(new DataTableModel());
		table.setDefaultRenderer(Double.class, new DoubleCellRenderer());
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setCellSelectionEnabled(true);
		table.setTransferHandler(new TableTransferHandler());

		setViewportView(table);
	}

	public void addCellEditorListener(CellEditorListener listener) {
		table.getDefaultEditor(Double.class).addCellEditorListener(listener);
	}

	public void stopEditing() {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
	}

	public List<TimeSeriesPoint> getData() {
		List<TimeSeriesPoint> data = new ArrayList<>();

		for (int i = 0; i < table.getRowCount(); i++) {
			if (getX(i) != null && getY(i, PmmUtils.CONCENTRATION) != null) {
				TimeSeriesPoint p = DataFactory.eINSTANCE.createTimeSeriesPoint();

				p.setTime(getX(i));
				p.setConcentration(getY(i, PmmUtils.CONCENTRATION));
				data.add(p);
			}
		}

		return data;
	}

	public void setData(List<TimeSeriesPoint> data) {
		clear();

		for (int i = 0; i < table.getRowCount(); i++) {
			if (i < data.size()) {
				setX(i, data.get(i).getTime());
				setY(i, PmmUtils.CONCENTRATION, data.get(i).getConcentration());
			}
		}

	}

	public void clear() {
		for (int i = 0; i < table.getRowCount(); i++) {
			for (int j = 0; j < table.getColumnCount(); j++) {
				table.setValueAt(null, i, j);
			}
		}
	}

	public Double getX(int row) {
		return (Double) table.getValueAt(row, 0);
	}

	public void setX(int row, Double x) {
		table.setValueAt(x, row, 0);
	}

	public Double getY(int row, String name) {
		return (Double) table.getValueAt(row, yNames.indexOf(name) + 1);
	}

	public void setY(int row, String name, Double y) {
		table.setValueAt(y, row, yNames.indexOf(name) + 1);
	}

	private class DataTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<Double> xList;
		private List<List<Double>> yLists;

		public DataTableModel() {
			xList = new ArrayList<>(rowCount);
			yLists = new ArrayList<>();

			for (int i = 0; i < yNames.size(); i++) {
				yLists.add(new ArrayList<>());
			}

			for (int i = 0; i < rowCount; i++) {
				xList.add(null);

				for (int j = 0; j < yNames.size(); j++) {
					yLists.get(j).add(null);
				}
			}
		}

		@Override
		public int getRowCount() {
			return rowCount;
		}

		@Override
		public int getColumnCount() {
			return yNames.size() + 1;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return xName;
			default:
				return yNames.get(column - 1);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return Double.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return xEditable;
			default:
				return yEditable;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return xList.get(rowIndex);
			default:
				return yLists.get(columnIndex - 1).get(rowIndex);
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				xList.set(rowIndex, (Double) aValue);
				break;
			default:
				yLists.get(columnIndex - 1).set(rowIndex, (Double) aValue);
				break;
			}

			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
}
