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
package de.bund.bfr.knime.nls.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.chart.ChartUtils;
import de.bund.bfr.knime.chart.NamedShape;
import de.bund.bfr.knime.ui.Dialogs;

public class ChartSelectionPanel extends JPanel implements ItemListener, CellEditorListener {

	private static final long serialVersionUID = 1L;

	private List<SelectionListener> listeners;

	private JTable selectTable;
	private JCheckBox selectAllBox;

	private int selectColumnWidth;

	public ChartSelectionPanel(List<String> ids, Map<String, List<String>> stringValues,
			Map<String, List<Double>> doubleValues) {
		listeners = new ArrayList<>();

		selectAllBox = new JCheckBox("Select All");
		selectAllBox.setSelected(false);
		selectAllBox.addItemListener(this);

		selectTable = new JTable(new SelectTableModel(ids, stringValues, doubleValues,
				ChartUtils.createColorList(ids.size()), ChartUtils.createShapeList(ids.size())));
		selectTable.setRowSelectionAllowed(false);
		selectTable.setColumnSelectionAllowed(false);
		selectTable.getTableHeader().setResizingAllowed(false);
		selectTable.setRowHeight((new JComboBox<>()).getPreferredSize().height);
		selectTable.setRowSorter(new SelectTableRowSorter((SelectTableModel) selectTable.getModel()));
		selectTable.getColumn(NlsChartUtils.ID).setMinWidth(0);
		selectTable.getColumn(NlsChartUtils.ID).setMaxWidth(0);
		selectTable.getColumn(NlsChartUtils.ID).setPreferredWidth(0);
		selectTable.getColumn(NlsChartUtils.SELECTED).setCellEditor(new CheckBoxEditor());
		selectTable.getColumn(NlsChartUtils.SELECTED).setCellRenderer(new CheckBoxRenderer());
		selectTable.getColumn(NlsChartUtils.SELECTED).getCellEditor().addCellEditorListener(this);
		selectTable.getColumn(NlsChartUtils.COLOR).setCellEditor(new ColorEditor());
		selectTable.getColumn(NlsChartUtils.COLOR).setCellRenderer(new ColorRenderer());
		selectTable.getColumn(NlsChartUtils.SHAPE)
				.setCellEditor(new DefaultCellEditor(new JComboBox<>(NamedShape.values())));
		selectTable.getColumn(NlsChartUtils.COLOR).getCellEditor().addCellEditorListener(this);
		selectTable.getColumn(NlsChartUtils.SHAPE).getCellEditor().addCellEditorListener(this);
		selectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		for (int c = 0; c < selectTable.getColumnCount(); c++) {
			TableColumn col = selectTable.getColumnModel().getColumn(c);

			if (col.getPreferredWidth() == 0) {
				continue;
			}

			TableCellRenderer renderer = col.getHeaderRenderer();
			Component comp = selectTable.getTableHeader().getDefaultRenderer()
					.getTableCellRendererComponent(selectTable, col.getHeaderValue(), false, false, 0, 0);
			int width = comp.getPreferredSize().width;

			for (int r = 0; r < selectTable.getRowCount(); r++) {
				renderer = selectTable.getCellRenderer(r, c);
				comp = renderer.getTableCellRendererComponent(selectTable, selectTable.getValueAt(r, c), false, false,
						r, c);
				width = Math.max(width, comp.getPreferredSize().width);
			}

			col.setPreferredWidth(width + 10);
		}

		selectColumnWidth = selectTable.getColumn(NlsChartUtils.SELECTED).getPreferredWidth();

		setLayout(new BorderLayout());
		add(UI.createWestPanel(selectAllBox), BorderLayout.NORTH);
		add(new JScrollPane(selectTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
	}

	public boolean isSelectAll() {
		return selectAllBox.isSelected();
	}

	public void setSelectAll(boolean selectAll) {
		selectAllBox.setSelected(selectAll);
	}

	public List<String> getSelectedIds() {
		List<String> selectedIds = new ArrayList<>();

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			if ((Boolean) selectTable.getValueAt(i, 1)) {
				selectedIds.add((String) selectTable.getValueAt(i, 0));
			}
		}

		return selectedIds;
	}

	public void setSelectedIds(List<String> selectedIds) {
		Set<String> idSet = new LinkedHashSet<>(selectedIds);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			if (idSet.contains(selectTable.getValueAt(i, 0))) {
				selectTable.setValueAt(true, i, 1);
			} else {
				selectTable.setValueAt(false, i, 1);
			}
		}

		listeners.forEach(l -> l.selectionChanged());
	}

	public Map<String, Color> getColors() {
		Map<String, Color> paints = new LinkedHashMap<>(selectTable.getRowCount());

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			paints.put((String) selectTable.getValueAt(i, 0), (Color) selectTable.getValueAt(i, 2));
		}

		return paints;
	}

	public void setColors(Map<String, Color> colors) {
		for (int i = 0; i < selectTable.getRowCount(); i++) {
			Color color = colors.get(selectTable.getValueAt(i, 0));

			if (color != null) {
				selectTable.setValueAt(color, i, 2);
			}
		}
	}

	public Map<String, NamedShape> getShapes() {
		Map<String, NamedShape> shapes = new LinkedHashMap<>(selectTable.getRowCount());

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			shapes.put((String) selectTable.getValueAt(i, 0), (NamedShape) selectTable.getValueAt(i, 3));
		}

		return shapes;
	}

	public void setShapes(Map<String, NamedShape> shapes) {
		for (int i = 0; i < selectTable.getRowCount(); i++) {
			NamedShape shape = shapes.get(selectTable.getValueAt(i, 0));

			if (shape != null) {
				selectTable.setValueAt(shape, i, 3);
			}
		}
	}

	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		int width = selectAllBox.isSelected() ? 0 : selectColumnWidth;

		selectTable.getColumn(NlsChartUtils.SELECTED).setMinWidth(width);
		selectTable.getColumn(NlsChartUtils.SELECTED).setMaxWidth(width);
		selectTable.getColumn(NlsChartUtils.SELECTED).setPreferredWidth(width);
		listeners.forEach(l -> l.selectionChanged());
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		listeners.forEach(l -> l.selectionChanged());
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	public static interface SelectionListener {

		void selectionChanged();
	}

	private static class SelectTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<Boolean> selections;

		private int columnCount;

		private List<String> ids;
		private Map<String, List<String>> stringColumns;
		private Map<String, List<Double>> doubleColumns;
		private List<Color> colors;
		private List<NamedShape> shapes;

		private int idIndex;
		private int selectedIndex;
		private int colorIndex;
		private int shapeIndex;
		private Map<Integer, String> stringByIndex;
		private Map<Integer, String> doubleByIndex;

		public SelectTableModel(List<String> ids, Map<String, List<String>> stringColumns,
				Map<String, List<Double>> doubleColumns, List<Color> colors, List<NamedShape> shapes) {
			this.ids = ids;
			this.stringColumns = KnimeUtils.nullToEmpty(stringColumns);
			this.doubleColumns = KnimeUtils.nullToEmpty(doubleColumns);
			this.colors = colors;
			this.shapes = shapes;

			selections = new ArrayList<>(Collections.nCopies(ids.size(), false));
			stringByIndex = new LinkedHashMap<>();
			doubleByIndex = new LinkedHashMap<>();

			columnCount = 0;

			idIndex = columnCount++;
			selectedIndex = columnCount++;
			colorIndex = columnCount++;
			shapeIndex = columnCount++;

			for (String column : this.stringColumns.keySet()) {
				stringByIndex.put(columnCount++, column);
			}

			for (String column : this.doubleColumns.keySet()) {
				doubleByIndex.put(columnCount++, column);
			}
		}

		@Override
		public int getColumnCount() {
			return columnCount;
		}

		@Override
		public String getColumnName(int column) {
			if (column == idIndex) {
				return NlsChartUtils.ID;
			} else if (column == selectedIndex) {
				return NlsChartUtils.SELECTED;
			} else if (column == colorIndex) {
				return NlsChartUtils.COLOR;
			} else if (column == shapeIndex) {
				return NlsChartUtils.SHAPE;
			} else if (stringByIndex.containsKey(column)) {
				return stringByIndex.get(column);
			} else if (doubleByIndex.containsKey(column)) {
				return doubleByIndex.get(column);
			}

			return null;
		}

		@Override
		public int getRowCount() {
			return ids.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == idIndex) {
				return ids.get(row);
			} else if (column == selectedIndex) {
				return selections.get(row);
			} else if (column == colorIndex) {
				return colors.get(row);
			} else if (column == shapeIndex) {
				return shapes.get(row);
			} else if (stringByIndex.containsKey(column)) {
				return stringColumns.get(stringByIndex.get(column)).get(row);
			} else if (doubleByIndex.containsKey(column)) {
				return doubleColumns.get(doubleByIndex.get(column)).get(row);
			}

			return null;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column == idIndex) {
				return String.class;
			} else if (column == selectedIndex) {
				return Boolean.class;
			} else if (column == colorIndex) {
				return Color.class;
			} else if (column == shapeIndex) {
				return String.class;
			} else if (stringByIndex.containsKey(column)) {
				return String.class;
			} else if (doubleByIndex.containsKey(column)) {
				return Double.class;
			}

			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == selectedIndex) {
				selections.set(row, (Boolean) value);
			} else if (column == colorIndex) {
				colors.set(row, (Color) value);
			} else if (column == shapeIndex) {
				shapes.set(row, (NamedShape) value);
			}

			fireTableCellUpdated(row, column);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == selectedIndex || column == colorIndex || column == shapeIndex;
		}
	}

	private static class ColorRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ColorRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setBackground((Color) color);

			return this;
		}
	}

	private static class ColorEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton colorButton;

		public ColorEditor() {
			colorButton = new JButton();
			colorButton.addActionListener(e -> {
				Color newColor = Dialogs.showColorChooser(colorButton, "Choose Color", colorButton.getBackground());

				if (newColor != null) {
					colorButton.setBackground(newColor);
					stopCellEditing();
				} else {
					cancelCellEditing();
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			colorButton.setBackground((Color) value);

			return colorButton;
		}

		@Override
		public Object getCellEditorValue() {
			return colorButton.getBackground();
		}

	}

	private static class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

		private static final long serialVersionUID = -8337460338388283099L;

		public CheckBoxRenderer() {
			super();
			setHorizontalAlignment(SwingConstants.CENTER);
			setBorderPainted(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			int statusColumn = -1;

			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumnName(i).equals(NlsChartUtils.STATUS)) {
					statusColumn = i;
					break;
				}
			}

			if (statusColumn != -1) {
				String statusValue = (String) table.getValueAt(row, statusColumn);

				if (statusValue.equals(Plotable.Status.OK.toString())) {
					setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
					setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
				} else if (statusValue.equals(Plotable.Status.FAILED.toString())) {
					setForeground(Color.RED);
					setBackground(Color.RED);
				} else if (statusValue.equals(Plotable.Status.NO_COVARIANCE.toString())) {
					setForeground(Color.YELLOW);
					setBackground(Color.YELLOW);
				}
			} else {
				setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
				setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			}

			setSelected((value != null && ((Boolean) value).booleanValue()));
			setBorder(hasFocus ? UI.TABLE_CELL_FOCUS_BORDER : UI.TABLE_CELL_BORDER);

			return this;
		}
	}

	private static class CheckBoxEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 1L;

		public CheckBoxEditor() {
			super(new JCheckBox());
			((JCheckBox) getComponent()).setHorizontalAlignment(SwingConstants.CENTER);
		}
	}

	private static class SelectTableRowSorter extends TableRowSorter<SelectTableModel> {

		public SelectTableRowSorter(SelectTableModel model) {
			super(model);
		}

		@Override
		public void toggleSortOrder(int column) {
			List<? extends SortKey> sortKeys = getSortKeys();

			if (sortKeys.size() > 0) {
				if (sortKeys.get(0).getColumn() == column && sortKeys.get(0).getSortOrder() == SortOrder.DESCENDING) {
					setSortKeys(null);
					return;
				}
			}

			super.toggleSortOrder(column);
		}
	}
}
