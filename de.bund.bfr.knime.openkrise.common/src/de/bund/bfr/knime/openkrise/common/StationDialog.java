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
package de.bund.bfr.knime.openkrise.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.StringTextField;
import de.bund.bfr.knime.ui.TextListener;

public class StationDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final String SELECT = "Select";
	private static final int ROW_HEIGHT = new JButton(SELECT).getPreferredSize().height;

	private boolean approved;
	private Station selected;

	private List<Station> stations;
	private JScrollPane pane;
	private JTable table;
	private TableRowSorter<StationTableModel> rowSorter;

	public StationDialog(Component parent, String title, List<Station> stations) {
		super(SwingUtilities.getWindowAncestor(parent), title, DEFAULT_MODALITY_TYPE);
		this.stations = stations;
		approved = false;
		selected = null;

		StationTableModel model = new StationTableModel(stations);

		table = new JTable(model);
		table.setRowHeight(ROW_HEIGHT);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		rowSorter = new TableRowSorter<>(model);
		table.setRowSorter(rowSorter);
		UI.packColumns(table);
		pane = new JScrollPane();
		pane.setRowHeaderView(createSelectTable(stations == null ? 0 : stations.size()));
		pane.setViewportView(table);
		pane.getRowHeader().setPreferredSize(pane.getRowHeader().getView().getPreferredSize());

		final StringTextField searchField = new StringTextField(true, 30);

		searchField.addTextListener(new TextListener() {

			@Override
			public void textChanged(Object source) {
				searchChanged(searchField.getText());
			}
		});

		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancelButtonPressed();
			}
		});

		setLayout(new BorderLayout());
		add(UI.createHorizontalPanel(new JLabel("Enter Search Query:"), searchField), BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(cancelButton)), BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
	}

	public boolean isApproved() {
		return approved;
	}

	public Station getSelected() {
		return selected;
	}

	private void selectButtonPressed(int index) {
		approved = true;
		selected = stations.get(table.convertRowIndexToModel(index));
		dispose();
	}

	private void cancelButtonPressed() {
		dispose();
	}

	private void searchChanged(String search) {
		final List<String> searchStrings = split(search);

		rowSorter.setRowFilter(new RowFilter<StationTableModel, Integer>() {

			@Override
			public boolean include(javax.swing.RowFilter.Entry<? extends StationTableModel, ? extends Integer> entry) {
				for (String s : searchStrings) {
					boolean found = false;

					for (int i = 0; i < entry.getValueCount(); i++) {
						if (entry.getStringValue(i).toLowerCase().contains(s.toLowerCase())) {
							found = true;
							break;
						}
					}

					if (!found) {
						return false;
					}
				}

				return true;
			}
		});
		pane.setRowHeaderView(createSelectTable(table.getRowCount()));
	}

	private JTable createSelectTable(int n) {
		JTable table = new JTable(n, 1);

		table.setRowHeight(ROW_HEIGHT);
		table.setDefaultEditor(Object.class, new SelectEditor());
		table.setDefaultRenderer(Object.class, new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				return new JButton(SELECT);
			}
		});

		return table;
	}

	private static List<String> split(String searchString) {
		List<String> list = new ArrayList<>();
		List<String> current = null;

		for (String s : Splitter.onPattern("\\s").trimResults().omitEmptyStrings()
				.split(searchString.replace("\"", " \" "))) {
			if (s.equals("\"")) {
				if (current != null) {
					list.add(Joiner.on(" ").join(current));
					current = null;
				} else {
					current = new ArrayList<>();
				}
			} else {
				if (current != null) {
					current.add(s);
				} else {
					list.add(s);
				}
			}
		}

		if (current != null) {
			list.add(Joiner.on(" ").join(current));
		}

		return list;
	}

	private class SelectEditor implements TableCellEditor {

		@Override
		public boolean stopCellEditing() {
			return true;
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return false;
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			return true;
		}

		@Override
		public Object getCellEditorValue() {
			return null;
		}

		@Override
		public void cancelCellEditing() {
		}

		@Override
		public void addCellEditorListener(CellEditorListener l) {
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			selectButtonPressed(row);
			return new JButton(SELECT);
		}
	}

	private static class StationTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<Station> stations;
		private List<String> columnNames;
		private List<Method> getters;

		public StationTableModel(List<Station> stations) {
			this.stations = stations;
			columnNames = new ArrayList<>();
			getters = new ArrayList<>();

			for (Map.Entry<String, Method> entry : Station.PROPERTIES.entrySet()) {
				columnNames.add(entry.getKey());
				getters.add(entry.getValue());
			}
		}

		@Override
		public int getRowCount() {
			return stations == null ? 0 : stations.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.size();
		}

		@Override
		public String getColumnName(int column) {
			return columnNames.get(column);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			try {
				return getters.get(columnIndex).invoke(stations.get(rowIndex));
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
