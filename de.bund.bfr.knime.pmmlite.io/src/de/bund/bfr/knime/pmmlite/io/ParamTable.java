/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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

import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.bund.bfr.knime.pmmlite.core.models.Parameter;

public class ParamTable extends JTable {

	private static final long serialVersionUID = 1L;

	public ParamTable(List<Parameter> params) {
		super(new ParamTableModel(params));
	}

	private static class ParamTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<Parameter> params;

		public ParamTableModel(List<Parameter> params) {
			this.params = params;
		}

		@Override
		public int getRowCount() {
			return params.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Parameter param = params.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return param.getName();
			case 1:
				return param.getMin();
			case 2:
				return param.getMax();
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
			}
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Name";
			case 1:
				return "Min";
			case 2:
				return "Max";
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
				return Double.class;
			case 2:
				return Double.class;
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex != 0;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Parameter param = params.get(rowIndex);

			switch (columnIndex) {
			case 0:
				break;
			case 1:
				param.setMin((Double) aValue);
				break;
			case 2:
				param.setMax((Double) aValue);
				break;
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
			}

			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
}
