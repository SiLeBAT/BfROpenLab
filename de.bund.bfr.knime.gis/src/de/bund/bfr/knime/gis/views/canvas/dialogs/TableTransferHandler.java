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

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;

import com.google.common.base.Joiner;

public class TableTransferHandler extends TransferHandler implements UIResource {

	private static final long serialVersionUID = 1L;

	private JTable table;

	public TableTransferHandler(JTable table) {
		this.table = table;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if (!table.getRowSelectionAllowed() && !table.getColumnSelectionAllowed()) {
			return null;
		}

		int[] rows = table.getRowSelectionAllowed() ? table.getSelectedRows() : getAll(table.getRowCount());
		int[] cols = table.getColumnSelectionAllowed() ? table.getSelectedColumns() : getAll(table.getColumnCount());

		if (rows.length == 0 || cols.length == 0) {
			return null;
		}

		List<String> lines = new ArrayList<>();
		List<String> columnNames = new ArrayList<>();

		for (int col : cols) {
			columnNames.add(table.getColumnName(col));
		}

		lines.add(Joiner.on("\t").join(columnNames));

		for (int row : rows) {
			List<String> values = new ArrayList<>();

			for (int col : cols) {
				Object obj = table.getValueAt(row, col);

				values.add(obj != null ? obj.toString() : "");
			}

			lines.add(Joiner.on("\t").join(values));
		}

		return new StringSelection(Joiner.on("\n").join(lines));
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

	private static int[] getAll(int n) {
		int[] all = new int[n];

		for (int i = 0; i < n; i++) {
			all[i] = i;
		}

		return all;
	}
}
