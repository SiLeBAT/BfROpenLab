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
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.bund.bfr.knime.UI;

public class StationDialog extends JDialog implements ActionListener {

	public static void main(String[] args)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Station b1 = new Station("1", "Bäcker Müller");
		Station b2 = new Station("2", "Bäcker Meier");
		Station b3 = new Station("3", "Megashop");

		JFrame frame = new JFrame("Test");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 200);
		frame.setVisible(true);

		StationDialog dialog = new StationDialog(frame, "Test", Arrays.asList(b1, b2, b3));

		dialog.setVisible(true);
	}

	private static final long serialVersionUID = 1L;

	private boolean approved;
	private Station selected;

	private JButton okButton;
	private JButton cancelButton;

	public StationDialog(Component parent, String title, List<Station> stations) {
		super(SwingUtilities.getWindowAncestor(parent), title, DEFAULT_MODALITY_TYPE);
		approved = false;
		selected = null;

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JTable table = new JTable(new StationTableModel(stations));
		JScrollPane pane = new JScrollPane();

		pane.setRowHeaderView(createSelectTable(stations.size()));
		pane.setViewportView(table);

		setLayout(new BorderLayout());
		add(UI.createHorizontalPanel(new JLabel(title)), BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		setResizable(false);
	}

	public boolean isApproved() {
		return approved;
	}

	public Station getSelected() {
		return selected;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			approved = true;
			selected = null;
			dispose();
		} else if (e.getSource() == cancelButton) {
			dispose();
		}
	}

	private static JTable createSelectTable(int n) {
		JTable table = new JTable(n, 1);

		table.setDefaultRenderer(Object.class, new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				return new JButton("Select");
			}
		});

		table.setDefaultEditor(Object.class, new TableCellEditor() {

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
				return new JButton("Select");
			}
		});

		return table;
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
			return stations.size();
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
