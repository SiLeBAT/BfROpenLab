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
package de.bund.bfr.knime.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;

import com.google.common.base.Joiner;

public class TableTransferHandler extends TransferHandler implements UIResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected Transferable createTransferable(JComponent c) {
		if (!(c instanceof JTable)) {
			return null;
		}

		JTable table = (JTable) c;

		if (!table.getRowSelectionAllowed() && !table.getColumnSelectionAllowed()) {
			return null;
		}

		int[] rows = table.getRowSelectionAllowed() ? table.getSelectedRows() : getAll(table.getRowCount());
		int[] cols = table.getColumnSelectionAllowed() ? table.getSelectedColumns() : getAll(table.getColumnCount());

		if (rows.length == 0 || cols.length == 0) {
			return null;
		}

		List<String> lines = new ArrayList<>();

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
	public boolean importData(JComponent c, Transferable tranferable) {
		if (!(c instanceof JTable)) {
			return false;
		}

		JTable table = (JTable) c;
		int startRow = table.getSelectedRows()[0];
		int startCol = table.getSelectedColumns()[0];
		String s = null;

		try {
			s = (String) tranferable.getTransferData(DataFlavor.stringFlavor);
		} catch (IOException | UnsupportedFlavorException ex) {
			return false;
		}

		String[] rows = s.split("\n");

		for (int i = 0; i < rows.length; i++) {
			String[] cells = rows[i].split("\t");

			for (int j = 0; j < cells.length; j++) {
				int row = startRow + i;
				int col = startCol + j;

				if (row >= table.getRowCount() || col >= table.getColumnCount() || !table.isCellEditable(row, col)) {
					continue;
				}

				String value = cells[j].trim();

				try {
					if (table.getColumnClass(col) == String.class) {
						table.setValueAt(value, row, col);
					} else if (table.getColumnClass(col) == Integer.class) {
						table.setValueAt(Integer.parseInt(cells[j]), row, col);
					} else if (table.getColumnClass(col) == Double.class) {
						table.setValueAt(Double.parseDouble(cells[j].replace(",", ".")), row, col);
					} else if (table.getColumnClass(col) == Boolean.class) {
						table.setValueAt(Boolean.parseBoolean(s), row, col);
					}
				} catch (NumberFormatException ex) {
				}
			}
		}

		table.repaint();

		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	private static int[] getAll(int n) {
		int[] all = new int[n];

		for (int i = 0; i < n; i++) {
			all[i] = i;
		}

		return all;
	}
}
