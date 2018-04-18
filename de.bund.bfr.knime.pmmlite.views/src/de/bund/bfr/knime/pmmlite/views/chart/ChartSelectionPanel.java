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
package de.bund.bfr.knime.pmmlite.views.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.chart.ChartUtils;
import de.bund.bfr.knime.chart.NamedShape;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.ui.DoubleCellRenderer;

public class ChartSelectionPanel extends JPanel implements ActionListener, CellEditorListener, ListSelectionListener {

	public static final String ID = "Row Identifier";
	public static final String SELECTED = "Is Selected";
	public static final String COLOR = "Color Value";
	public static final String SHAPE = "Shape Value";
	public static final String DATA = "Data Points";
	public static final String PARAMETERS = "Parameter Values";
	public static final String FORMULA = "Math Formula";
	public static final String COVARIANCES = "Covariance Matrix";
	public static final String STATUS = "Status Value";

	public static final String STD_ERROR = "std-Error";
	public static final String T_VALUE = "t-Value";
	public static final String P_VALUE = "p-Value";

	private static final long serialVersionUID = 1L;

	private static final int MIN_COLUMN_WIDTH = 15;
	private static final int MAX_COLUMN_WIDTH = 2147483647;
	private static final int PREFERRED_COLUMN_WIDTH = 75;

	private Builder builder;

	private List<String> visualizationColumns;
	private List<String> miscellaneousColumns;
	private List<String> qualityColumns;
	private List<String> conditionColumns;
	private List<String> parameterColumns;
	private List<String> properties;

	private Set<String> visibleColumns;

	private SelectTableModel selectTableModel;
	private JTable selectTable;

	private JScrollPane tableScrollPane;
	private JButton selectAllButton;
	private JButton unselectAllButton;
	private JButton invertSelectionButton;
	private JButton customizeColumnsButton;
	private JButton resizeColumnsButton;
	private Map<String, JComboBox<String>> comboBoxes;

	private JMenuItem selectItem;
	private JMenuItem unselectItem;

	public static class Builder {
		private List<String> ids;
		private boolean selectionsExclusive;
		private boolean hasConditionRanges;

		private Map<String, List<String>> stringValues;
		private Map<String, List<Double>> qualityValues;
		private List<String> qualityValueUnits;
		private Map<String, List<ConditionValue>> conditionValues;
		private Map<String, List<ParameterValue>> parameterValues;
		private Set<String> filterableColumns;
		private List<TimeSeries> data;
		private List<Map<String, ParameterValue>> parameters;
		private List<String> formulas;
		private List<Integer> colorCounts;
		private Set<String> visibleColumns;

		public Builder(List<String> ids, boolean selectionsExclusive, boolean hasConditionRanges) {
			this.ids = ids;
			this.selectionsExclusive = selectionsExclusive;
			this.hasConditionRanges = hasConditionRanges;
			stringValues = new LinkedHashMap<>();
			qualityValues = new LinkedHashMap<>();
			conditionValues = new LinkedHashMap<>();
			parameterValues = new LinkedHashMap<>();
			filterableColumns = new LinkedHashSet<>();
			visibleColumns = new LinkedHashSet<>();
		}

		public Builder stringValues(Map<String, List<String>> stringValues) {
			this.stringValues = stringValues;
			return this;
		}

		public Builder qualityValues(Map<String, List<Double>> qualityValues) {
			this.qualityValues = qualityValues;
			return this;
		}

		public Builder qualityValueUnits(List<String> qualityValueUnits) {
			this.qualityValueUnits = qualityValueUnits;
			return this;
		}

		public Builder conditionValues(Map<String, List<ConditionValue>> conditionValues) {
			this.conditionValues = conditionValues;
			return this;
		}

		public Builder parameterValues(Map<String, List<ParameterValue>> parameterValues) {
			this.parameterValues = parameterValues;
			return this;
		}

		public Builder filterableColumns(Set<String> filterableColumns) {
			this.filterableColumns = filterableColumns;
			return this;
		}

		public Builder data(List<TimeSeries> data) {
			this.data = data;
			return this;
		}

		public Builder parameters(List<Map<String, ParameterValue>> parameters) {
			this.parameters = parameters;
			return this;
		}

		public Builder formulas(List<String> formulas) {
			this.formulas = formulas;
			return this;
		}

		public Builder colorCounts(List<Integer> colorCounts) {
			this.colorCounts = colorCounts;
			return this;
		}

		public Builder visibleColumns(Set<String> visibleColumns) {
			this.visibleColumns = visibleColumns;
			return this;
		}

		public ChartSelectionPanel build() {
			return new ChartSelectionPanel(this);
		}
	}

	private ChartSelectionPanel(Builder builder) {
		this.builder = builder;

		if (builder.parameters != null) {
			properties = Arrays.asList(PARAMETERS, COVARIANCES);
		} else if (!builder.parameterValues.isEmpty()) {
			properties = Arrays.asList(STD_ERROR, T_VALUE, P_VALUE, COVARIANCES);
		} else {
			properties = new ArrayList<>();
		}

		visibleColumns = new LinkedHashSet<>();
		visualizationColumns = new ArrayList<>();
		visualizationColumns.add(COLOR);
		visualizationColumns.add(SHAPE);
		miscellaneousColumns = new ArrayList<>(builder.stringValues.keySet());
		qualityColumns = new ArrayList<>(builder.qualityValues.keySet());
		parameterColumns = new ArrayList<>(builder.parameterValues.keySet());
		conditionColumns = new ArrayList<>(builder.conditionValues.keySet());

		if (builder.qualityValueUnits != null) {
			qualityColumns.add(PmmUtils.UNIT);
		}

		if (builder.data != null) {
			miscellaneousColumns.add(DATA);
		}

		if (builder.formulas != null) {
			miscellaneousColumns.add(FORMULA);
		}

		comboBoxes = new LinkedHashMap<>();

		JPanel optionsPanel = new JPanel();

		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

		if (!builder.filterableColumns.isEmpty()) {
			JPanel optionsPanel1 = new JPanel();
			JPanel filterPanel = new JPanel();

			optionsPanel1.setLayout(new BoxLayout(optionsPanel1, BoxLayout.X_AXIS));
			filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));

			for (String column : builder.filterableColumns) {
				List<String> values = new ArrayList<>();
				Set<String> valueSet = new LinkedHashSet<>(builder.stringValues.get(column));

				valueSet.remove(null);
				values.addAll(valueSet);
				Collections.sort(values);
				values.add(0, null);

				JComboBox<String> box = new JComboBox<>(new Vector<>(values));

				box.addActionListener(this);
				filterPanel.add(new JLabel(column + ":"));
				filterPanel.add(Box.createHorizontalStrut(5));
				filterPanel.add(box);
				filterPanel.add(Box.createHorizontalStrut(5));
				comboBoxes.put(column, box);
			}

			optionsPanel1.add(UI.createTitledPanel(filterPanel, "Filter"));
			optionsPanel.add(UI.createWestPanel(optionsPanel1));
		}

		JPanel optionsPanel2 = new JPanel();

		optionsPanel2.setLayout(new BoxLayout(optionsPanel2, BoxLayout.X_AXIS));

		if (!builder.selectionsExclusive) {
			selectAllButton = new JButton("All");
			selectAllButton.addActionListener(this);
			unselectAllButton = new JButton("None");
			unselectAllButton.addActionListener(this);
			invertSelectionButton = new JButton("Invert");
			invertSelectionButton.addActionListener(this);

			optionsPanel2.add(UI.createTitledPanel(
					UI.createHorizontalPanel(selectAllButton, unselectAllButton, invertSelectionButton), "Select"));
		}

		JPanel columnPanel = new JPanel();

		customizeColumnsButton = new JButton("Customize");
		customizeColumnsButton.addActionListener(this);
		resizeColumnsButton = new JButton("Set Optimal Width");
		resizeColumnsButton.addActionListener(this);
		columnPanel.setBorder(BorderFactory.createTitledBorder("Columns"));
		columnPanel.add(customizeColumnsButton);
		columnPanel.add(resizeColumnsButton);
		optionsPanel2.add(columnPanel);

		optionsPanel.add(UI.createWestPanel(optionsPanel2));

		selectTableModel = new SelectTableModel();
		selectTable = new JTable(selectTableModel);
		selectTable.setComponentPopupMenu(createPopupMenu());
		selectTable.setSelectionMode(builder.selectionsExclusive ? ListSelectionModel.SINGLE_SELECTION
				: ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectTable.getSelectionModel().addListSelectionListener(this);
		selectTable.setRowHeight((new JComboBox<>()).getPreferredSize().height);
		selectTable.setRowSorter(SelectTableUtilities.createSelectTableRowSorter(selectTableModel, null));
		selectTable.getColumn(ID).setMinWidth(0);
		selectTable.getColumn(ID).setMaxWidth(0);
		selectTable.getColumn(ID).setPreferredWidth(0);
		selectTable.getColumn(SELECTED).setCellEditor(SelectTableUtilities.createCheckBoxEditor());
		selectTable.getColumn(SELECTED).setCellRenderer(SelectTableUtilities.createCheckBoxRenderer());
		selectTable.getColumn(SELECTED).getCellEditor().addCellEditorListener(this);
		selectTable.setDefaultRenderer(Double.class, new DoubleCellRenderer());

		if (builder.colorCounts == null) {
			selectTable.getColumn(COLOR).setCellEditor(SelectTableUtilities.createColorEditor());
			selectTable.getColumn(COLOR).setCellRenderer(SelectTableUtilities.createColorRenderer());
			selectTable.getColumn(SHAPE).setCellEditor(new DefaultCellEditor(new JComboBox<>(NamedShape.values())));
		} else {
			selectTable.getColumn(COLOR).setCellEditor(SelectTableUtilities.createColorListEditor());
			selectTable.getColumn(COLOR).setCellRenderer(SelectTableUtilities.createColorListRenderer());
			selectTable.getColumn(SHAPE).setCellEditor(SelectTableUtilities.createShapeListEditor());
			selectTable.getColumn(SHAPE).setCellRenderer(SelectTableUtilities.createShapeListRenderer());
		}

		selectTable.getColumn(COLOR).getCellEditor().addCellEditorListener(this);
		selectTable.getColumn(SHAPE).getCellEditor().addCellEditorListener(this);
		selectTable.getColumn(DATA).setCellEditor(SelectTableUtilities.createTimeSeriesEditor());
		selectTable.getColumn(DATA).setCellRenderer(SelectTableUtilities.createViewRenderer());
		selectTable.getColumn(FORMULA).setCellEditor(SelectTableUtilities.createFormulaEditor());
		selectTable.getColumn(FORMULA).setCellRenderer(SelectTableUtilities.createViewRenderer());
		selectTable.getColumn(PARAMETERS).setCellEditor(SelectTableUtilities.createParameterEditor());
		selectTable.getColumn(PARAMETERS).setCellRenderer(SelectTableUtilities.createViewRenderer());
		selectTable.getColumn(COVARIANCES).setCellEditor(SelectTableUtilities.createCovarianceEditor());
		selectTable.getColumn(COVARIANCES).setCellRenderer(SelectTableUtilities.createViewRenderer());
		selectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tableScrollPane = new JScrollPane(selectTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		setVisibleColumns(builder.visibleColumns);
		setLayout(new BorderLayout());
		add(optionsPanel, BorderLayout.NORTH);
		add(tableScrollPane, BorderLayout.CENTER);
	}

	public String getFocusedID() {
		List<String> ids = getFocusedIDs();

		return ids.size() == 1 ? ids.get(0) : null;
	}

	public String getSelectedID() {
		List<String> ids = getSelectedIDs();

		return ids.size() == 1 ? ids.get(0) : null;
	}

	public List<String> getFocusedIDs() {
		List<String> ids = new ArrayList<>();
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);

		for (int row : selectTable.getSelectedRows()) {
			ids.add((String) selectTable.getValueAt(row, idIndex));
		}

		return ids;
	}

	public List<String> getSelectedIDs() {
		List<String> selectedIDs = new ArrayList<>();
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int selectedIndex = selectTable.convertColumnIndexToView(selectTableModel.selectedIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			if ((Boolean) selectTable.getValueAt(i, selectedIndex)) {
				selectedIDs.add((String) selectTable.getValueAt(i, idIndex));
			}
		}

		return selectedIDs;
	}

	public void setSelectedIDs(List<String> selectedIDs) {
		Set<String> idSet = new LinkedHashSet<>(selectedIDs);
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int selectedIndex = selectTable.convertColumnIndexToView(selectTableModel.selectedIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			selectTable.setValueAt(idSet.contains(selectTable.getValueAt(i, idIndex)), i, selectedIndex);
		}

		fireSelectionChanged();
	}

	public void setSelectionToAll(boolean selected) {
		int selectedIndex = selectTable.convertColumnIndexToView(selectTableModel.selectedIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			selectTable.setValueAt(selected, i, selectedIndex);
		}

		fireSelectionChanged();
	}

	public Map<String, String> getFilters() {
		Map<String, String> filters = new LinkedHashMap<>();

		for (Map.Entry<String, JComboBox<String>> entry : comboBoxes.entrySet()) {
			filters.put(entry.getKey(), (String) entry.getValue().getSelectedItem());
		}

		return filters;
	}

	public void setFilters(Map<String, String> filters) {
		for (Map.Entry<String, String> entry : filters.entrySet()) {
			if (comboBoxes.containsKey(entry.getKey())) {
				if (entry.getValue() != null) {
					comboBoxes.get(entry.getKey()).setSelectedItem(entry.getValue());
				} else {
					comboBoxes.get(entry.getKey()).setSelectedItem("");
				}

			}
		}

		if (!filters.isEmpty()) {
			applyFilters();
		}
	}

	public Map<String, Color> getColors() {
		Map<String, Color> paints = new LinkedHashMap<>(selectTable.getRowCount());
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int colorIndex = selectTable.convertColumnIndexToView(selectTableModel.colorIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			paints.put((String) selectTable.getValueAt(i, idIndex), (Color) selectTable.getValueAt(i, colorIndex));
		}

		return paints;
	}

	public void setColors(Map<String, Color> colors) {
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int colorIndex = selectTable.convertColumnIndexToView(selectTableModel.colorIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			Color color = colors.get(selectTable.getValueAt(i, idIndex));

			if (color != null) {
				selectTable.setValueAt(color, i, colorIndex);
			}
		}
	}

	public Map<String, NamedShape> getShapes() {
		Map<String, NamedShape> shapes = new LinkedHashMap<>(selectTable.getRowCount());
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int shapeIndex = selectTable.convertColumnIndexToView(selectTableModel.shapeIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			shapes.put((String) selectTable.getValueAt(i, idIndex), (NamedShape) selectTable.getValueAt(i, shapeIndex));
		}

		return shapes;
	}

	public void setShapes(Map<String, NamedShape> shapes) {
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int shapeIndex = selectTable.convertColumnIndexToView(selectTableModel.shapeIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			NamedShape shape = shapes.get(selectTable.getValueAt(i, idIndex));

			if (shape != null) {
				selectTable.setValueAt(shape, i, shapeIndex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<Color>> getColorLists() {
		Map<String, List<Color>> paints = new LinkedHashMap<>(selectTable.getRowCount());
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int colorIndex = selectTable.convertColumnIndexToView(selectTableModel.colorIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			paints.put((String) selectTable.getValueAt(i, idIndex),
					(List<Color>) selectTable.getValueAt(i, colorIndex));
		}

		return paints;
	}

	public void setColorLists(Map<String, List<Color>> colorLists) {
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int colorIndex = selectTable.convertColumnIndexToView(selectTableModel.colorIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			List<Color> colorList = colorLists.get(selectTable.getValueAt(i, idIndex));

			if (colorList != null) {
				selectTable.setValueAt(colorList, i, colorIndex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<NamedShape>> getShapeLists() {
		Map<String, List<NamedShape>> shapes = new LinkedHashMap<>(selectTable.getRowCount());
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int shapeIndex = selectTable.convertColumnIndexToView(selectTableModel.shapeIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			shapes.put((String) selectTable.getValueAt(i, idIndex),
					(List<NamedShape>) selectTable.getValueAt(i, shapeIndex));
		}

		return shapes;
	}

	public void setShapeLists(Map<String, List<NamedShape>> shapeLists) {
		int idIndex = selectTable.convertColumnIndexToView(selectTableModel.idIndex);
		int shapeIndex = selectTable.convertColumnIndexToView(selectTableModel.shapeIndex);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			List<NamedShape> shapeList = shapeLists.get(selectTable.getValueAt(i, idIndex));

			if (shapeList != null) {
				selectTable.setValueAt(shapeList, i, shapeIndex);
			}
		}
	}

	public Set<String> getVisibleColumns() {
		return visibleColumns;
	}

	public void setVisibleColumns(Set<String> visibleColumns) {
		this.visibleColumns = visibleColumns;

		Set<String> columns = new LinkedHashSet<>();

		columns.addAll(visualizationColumns);
		columns.addAll(miscellaneousColumns);
		columns.addAll(qualityColumns);
		columns.add(PmmUtils.UNIT);
		columns.add(DATA);
		columns.add(FORMULA);
		columns.add(COVARIANCES);
		columns.add(PARAMETERS);

		for (String column : columns) {
			setColumnVisible(column, visibleColumns.contains(column));
		}

		for (String column : conditionColumns) {
			if (!builder.hasConditionRanges) {
				setColumnVisible(column, visibleColumns.contains(column));
				setColumnVisible(getMin(column), false);
				setColumnVisible(getMax(column), false);
			} else {
				setColumnVisible(column, false);
				setColumnVisible(getMin(column), visibleColumns.contains(column));
				setColumnVisible(getMax(column), visibleColumns.contains(column));
			}
		}

		for (String column : parameterColumns) {
			setColumnVisible(column, visibleColumns.contains(column));

			if (visibleColumns.contains(column)) {
				setColumnVisible(getError(column), visibleColumns.contains(STD_ERROR));
				setColumnVisible(getT(column), visibleColumns.contains(T_VALUE));
				setColumnVisible(getP(column), visibleColumns.contains(P_VALUE));
			} else {
				setColumnVisible(getError(column), false);
				setColumnVisible(getT(column), false);
				setColumnVisible(getP(column), false);
			}
		}
	}

	public Map<String, Integer> getColumnWidths() {
		Map<String, Integer> widths = new LinkedHashMap<>();
		Enumeration<TableColumn> columns = selectTable.getColumnModel().getColumns();

		while (columns.hasMoreElements()) {
			TableColumn column = columns.nextElement();

			if (column.getWidth() != 0) {
				widths.put((String) column.getHeaderValue(), column.getWidth());
			}
		}

		return widths;
	}

	public void setColumnWidths(Map<String, Integer> widths) {
		Enumeration<TableColumn> columns = selectTable.getColumnModel().getColumns();

		while (columns.hasMoreElements()) {
			TableColumn column = columns.nextElement();

			if (widths.containsKey(column.getHeaderValue())) {
				column.setPreferredWidth(widths.get(column.getHeaderValue()));
			}
		}
	}

	public void addSelectionListener(SelectionListener listener) {
		listenerList.add(SelectionListener.class, listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		listenerList.remove(SelectionListener.class, listener);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectAllButton) {
			setSelectionToAll(true);
		} else if (e.getSource() == unselectAllButton) {
			setSelectionToAll(false);
		} else if (e.getSource() == invertSelectionButton) {
			int selectedIndex = selectTable.convertColumnIndexToView(selectTableModel.selectedIndex);

			for (int i = 0; i < selectTable.getRowCount(); i++) {
				selectTable.setValueAt(!(Boolean) selectTable.getValueAt(i, selectedIndex), i, selectedIndex);
			}
		} else if (e.getSource() == customizeColumnsButton) {
			SelectTableDialog dialog = new SelectTableDialog(customizeColumnsButton, visualizationColumns,
					miscellaneousColumns, qualityColumns, conditionColumns, parameterColumns, properties,
					getVisibleColumns());

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setVisibleColumns(dialog.getColumnSelection());
			}
		} else if (e.getSource() == resizeColumnsButton) {
			UI.packColumns(selectTable);
			revalidate();
		} else if (e.getSource() == selectItem) {
			setSelectionToRows(true, selectTable.getSelectedRows());
		} else if (e.getSource() == unselectItem) {
			setSelectionToRows(false, selectTable.getSelectedRows());
		} else {
			applyFilters();
		}

		fireSelectionChanged();
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		fireSelectionChanged();
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		fireInfoSelectionChanged();
	}

	private void setSelectionToRows(boolean selected, int[] rows) {
		int selectedIndex = selectTable.convertColumnIndexToView(selectTableModel.selectedIndex);

		for (int i : rows) {
			selectTable.setValueAt(selected, i, selectedIndex);
		}

		fireSelectionChanged();
	}

	private void fireSelectionChanged() {
		Stream.of(getListeners(SelectionListener.class)).forEach(l -> l.selectionChanged(this));
	}

	private void fireInfoSelectionChanged() {
		Stream.of(getListeners(SelectionListener.class)).forEach(l -> l.focusChanged(this));
	}

	private void applyFilters() {
		Map<String, String> filters = new LinkedHashMap<>();

		for (Map.Entry<String, JComboBox<String>> entry : comboBoxes.entrySet()) {
			if (entry.getValue().getSelectedItem() != null) {
				filters.put(entry.getKey(), (String) entry.getValue().getSelectedItem());
			}
		}

		selectTable.setRowSorter(SelectTableUtilities.createSelectTableRowSorter(selectTable.getModel(), filters));
	}

	private void setColumnVisible(String column, boolean value) {
		if (value) {
			selectTable.getColumn(column).setMinWidth(MIN_COLUMN_WIDTH);
			selectTable.getColumn(column).setMaxWidth(MAX_COLUMN_WIDTH);

			if (selectTable.getColumn(column).getPreferredWidth() == 0) {
				selectTable.getColumn(column).setPreferredWidth(PREFERRED_COLUMN_WIDTH);
			}
		} else {
			selectTable.getColumn(column).setMinWidth(0);
			selectTable.getColumn(column).setMaxWidth(0);
			selectTable.getColumn(column).setPreferredWidth(0);
		}
	}

	private JPopupMenu createPopupMenu() {
		selectItem = new JMenuItem("Select");
		selectItem.addActionListener(this);
		unselectItem = new JMenuItem("Unselect");
		unselectItem.addActionListener(this);

		JPopupMenu menu = new JPopupMenu();

		menu.add(selectItem);
		menu.add(unselectItem);

		return menu;
	}

	private static String getMin(String cond) {
		return "Min " + cond;
	}

	private static String getMax(String cond) {
		return "Max " + cond;
	}

	private static String getError(String param) {
		return param + " std-Error";
	}

	private static String getT(String param) {
		return param + " t-Value";
	}

	private static String getP(String param) {
		return param + " p-Value";
	}

	public static interface SelectionListener extends EventListener {

		void selectionChanged(ChartSelectionPanel source);

		void focusChanged(ChartSelectionPanel source);
	}

	public static class ConditionValue {

		private boolean range;

		private Double value;
		private Double min;
		private Double max;

		public ConditionValue(Double value) {
			this.value = value;
			range = false;
		}

		public ConditionValue(Double min, Double max) {
			this.min = min;
			this.max = max;
			range = true;
		}

		public boolean isRange() {
			return range;
		}

		public Double getValue() {
			return value;
		}

		public Double getMin() {
			return min;
		}

		public Double getMax() {
			return max;
		}
	}

	private class SelectTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<Boolean> selections;

		private List<Color> colors;
		private List<List<Color>> colorLists;
		private List<NamedShape> shapes;
		private List<List<NamedShape>> shapeLists;

		private int columnCount;

		private Map<Integer, String> stringByIndex;
		private Map<Integer, String> qualityByIndex;
		private Map<Integer, String> conditionByIndex;
		private Map<Integer, String> conditionMinByIndex;
		private Map<Integer, String> conditionMaxByIndex;
		private Map<Integer, String> parameterByIndex;
		private Map<Integer, String> parameterErrorByIndex;
		private Map<Integer, String> parameterTByIndex;
		private Map<Integer, String> parameterPByIndex;

		private int idIndex;
		private int selectedIndex;
		private int colorIndex;
		private int shapeIndex;
		private int dataIndex;
		private int formulaIndex;
		private int qualityUnitIndex;
		private int parametersIndex;
		private int covariancesIndex;

		public SelectTableModel() {
			if (builder.colorCounts == null) {
				colors = ChartUtils.createColorList(builder.ids.size());
				shapes = ChartUtils.createShapeList(builder.ids.size());
			} else {
				colorLists = new ArrayList<>();
				shapeLists = new ArrayList<>();

				for (int n : builder.colorCounts) {
					colorLists.add(ChartUtils.createColorList(n));
					shapeLists.add(ChartUtils.createShapeList(n));
				}
			}

			selections = new ArrayList<>(Collections.nCopies(builder.ids.size(), false));
			stringByIndex = new LinkedHashMap<>();
			qualityByIndex = new LinkedHashMap<>();
			conditionByIndex = new LinkedHashMap<>();
			conditionMinByIndex = new LinkedHashMap<>();
			conditionMaxByIndex = new LinkedHashMap<>();
			parameterByIndex = new LinkedHashMap<>();
			parameterErrorByIndex = new LinkedHashMap<>();
			parameterTByIndex = new LinkedHashMap<>();
			parameterPByIndex = new LinkedHashMap<>();

			columnCount = 0;

			idIndex = columnCount++;
			selectedIndex = columnCount++;
			colorIndex = columnCount++;
			shapeIndex = columnCount++;

			for (String column : builder.stringValues.keySet()) {
				stringByIndex.put(columnCount++, column);
			}

			dataIndex = columnCount++;
			formulaIndex = columnCount++;

			for (String column : builder.qualityValues.keySet()) {
				qualityByIndex.put(columnCount++, column);
			}

			qualityUnitIndex = columnCount++;

			for (String cond : builder.conditionValues.keySet()) {
				conditionByIndex.put(columnCount++, cond);
				conditionMinByIndex.put(columnCount++, cond);
				conditionMaxByIndex.put(columnCount++, cond);
			}

			for (String param : builder.parameterValues.keySet()) {
				parameterByIndex.put(columnCount++, param);
				parameterErrorByIndex.put(columnCount++, param);
				parameterTByIndex.put(columnCount++, param);
				parameterPByIndex.put(columnCount++, param);
			}

			parametersIndex = columnCount++;
			covariancesIndex = columnCount++;
		}

		@Override
		public int getColumnCount() {
			return columnCount;
		}

		@Override
		public String getColumnName(int column) {
			if (column == idIndex) {
				return ID;
			} else if (column == selectedIndex) {
				return SELECTED;
			} else if (column == colorIndex) {
				return COLOR;
			} else if (column == shapeIndex) {
				return SHAPE;
			} else if (stringByIndex.containsKey(column)) {
				return stringByIndex.get(column);
			} else if (qualityByIndex.containsKey(column)) {
				return qualityByIndex.get(column);
			} else if (conditionByIndex.containsKey(column)) {
				return conditionByIndex.get(column);
			} else if (conditionMinByIndex.containsKey(column)) {
				return getMin(conditionMinByIndex.get(column));
			} else if (conditionMaxByIndex.containsKey(column)) {
				return getMax(conditionMaxByIndex.get(column));
			} else if (parameterByIndex.containsKey(column)) {
				return parameterByIndex.get(column);
			} else if (parameterErrorByIndex.containsKey(column)) {
				return getError(parameterErrorByIndex.get(column));
			} else if (parameterTByIndex.containsKey(column)) {
				return getT(parameterTByIndex.get(column));
			} else if (parameterPByIndex.containsKey(column)) {
				return getP(parameterPByIndex.get(column));
			} else if (column == dataIndex) {
				return DATA;
			} else if (column == formulaIndex) {
				return FORMULA;
			} else if (column == qualityUnitIndex) {
				return PmmUtils.UNIT;
			} else if (column == parametersIndex) {
				return PARAMETERS;
			} else if (column == covariancesIndex) {
				return COVARIANCES;
			}

			return null;
		}

		@Override
		public int getRowCount() {
			return builder.ids.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == idIndex) {
				return builder.ids.get(row);
			} else if (column == selectedIndex) {
				return selections.get(row);
			} else if (column == colorIndex) {
				return builder.colorCounts == null ? colors.get(row) : colorLists.get(row);
			} else if (column == shapeIndex) {
				return builder.colorCounts == null ? shapes.get(row) : shapeLists.get(row);
			} else if (stringByIndex.containsKey(column)) {
				return builder.stringValues.get(stringByIndex.get(column)).get(row);
			} else if (qualityByIndex.containsKey(column)) {
				return builder.qualityValues.get(qualityByIndex.get(column)).get(row);
			} else if (conditionByIndex.containsKey(column)) {
				return builder.conditionValues.get(conditionByIndex.get(column)).get(row).getValue();
			} else if (conditionMinByIndex.containsKey(column)) {
				return builder.conditionValues.get(conditionMinByIndex.get(column)).get(row).getMin();
			} else if (conditionMaxByIndex.containsKey(column)) {
				return builder.conditionValues.get(conditionMaxByIndex.get(column)).get(row).getMax();
			} else if (parameterByIndex.containsKey(column)) {
				return builder.parameterValues.get(parameterByIndex.get(column)).get(row).getValue();
			} else if (parameterErrorByIndex.containsKey(column)) {
				return builder.parameterValues.get(parameterErrorByIndex.get(column)).get(row).getError();
			} else if (parameterTByIndex.containsKey(column)) {
				return builder.parameterValues.get(parameterTByIndex.get(column)).get(row).getT();
			} else if (parameterPByIndex.containsKey(column)) {
				return builder.parameterValues.get(parameterPByIndex.get(column)).get(row).getP();
			} else if (column == dataIndex) {
				return builder.data != null ? builder.data.get(row) : null;
			} else if (column == formulaIndex) {
				return builder.formulas != null ? builder.formulas.get(row) : null;
			} else if (column == qualityUnitIndex) {
				return builder.qualityValueUnits != null ? builder.qualityValueUnits.get(row) : null;
			} else if (column == parametersIndex) {
				return builder.parameters != null ? builder.parameters.get(row) : null;
			} else if (column == covariancesIndex) {
				Map<String, Map<String, Double>> covMatrix = new LinkedHashMap<>();

				if (builder.parameters != null) {
					for (String param : builder.parameters.get(row).keySet()) {
						covMatrix.put(param, builder.parameters.get(row).get(param).getCorrelations().map());
					}
				} else {
					for (String param : builder.parameterValues.keySet()) {
						covMatrix.put(param, builder.parameterValues.get(param).get(row).getCorrelations().map());
					}
				}

				return covMatrix;
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
				return builder.colorCounts == null ? Color.class : List.class;
			} else if (column == shapeIndex) {
				return builder.colorCounts == null ? Color.class : List.class;
			} else if (stringByIndex.containsKey(column)) {
				return String.class;
			} else if (qualityByIndex.containsKey(column)) {
				return Double.class;
			} else if (conditionByIndex.containsKey(column)) {
				return Double.class;
			} else if (conditionMinByIndex.containsKey(column)) {
				return Double.class;
			} else if (conditionMaxByIndex.containsKey(column)) {
				return Double.class;
			} else if (parameterByIndex.containsKey(column)) {
				return Double.class;
			} else if (parameterErrorByIndex.containsKey(column)) {
				return Double.class;
			} else if (parameterTByIndex.containsKey(column)) {
				return Double.class;
			} else if (parameterPByIndex.containsKey(column)) {
				return Double.class;
			} else if (column == dataIndex) {
				return List.class;
			} else if (column == formulaIndex) {
				return String.class;
			} else if (column == qualityUnitIndex) {
				return String.class;
			} else if (column == parametersIndex) {
				return Map.class;
			} else if (column == covariancesIndex) {
				return Map.class;
			}

			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			setValueToSingleCell(value, row, column);

			if (builder.selectionsExclusive && column == selectedIndex && value.equals(Boolean.TRUE)) {
				for (int i = 0; i < getRowCount(); i++) {
					if (i != row) {
						setValueToSingleCell(false, i, selectedIndex);
						fireTableCellUpdated(i, selectedIndex);
					}
				}
			}

			fireTableCellUpdated(row, column);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == selectedIndex || column == colorIndex || column == shapeIndex || column == dataIndex
					|| column == formulaIndex || column == parametersIndex || column == covariancesIndex;
		}

		@SuppressWarnings("unchecked")
		private void setValueToSingleCell(Object value, int row, int column) {
			if (column == selectedIndex) {
				selections.set(row, (Boolean) value);
			} else if (column == colorIndex) {
				if (builder.colorCounts == null) {
					colors.set(row, (Color) value);
				} else {
					colorLists.set(row, (List<Color>) value);
				}
			} else if (column == shapeIndex) {
				if (builder.colorCounts == null) {
					shapes.set(row, (NamedShape) value);
				} else {
					shapeLists.set(row, (List<NamedShape>) value);
				}
			}
		}
	}
}
