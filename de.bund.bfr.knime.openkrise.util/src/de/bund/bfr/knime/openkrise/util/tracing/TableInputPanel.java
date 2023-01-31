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
package de.bund.bfr.knime.openkrise.util.tracing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesTable;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingPropertySelectorCreator;

public class TableInputPanel<T> extends JPanel implements RowSorterListener, CellEditorListener, ListSelectionListener {

	public static enum Type {
		NODE, EDGE
	}

	private static final long serialVersionUID = 1L;

	private Class<?> classType;
	private Type type;

	private Map<String, T> values;
	private AndOrHighlightCondition condition;

	private PropertySchema schema;
	private Collection<? extends Element> elements;

	private JPanel tablePanel;

	private JButton filterButton;
	private JButton removeFilterButton;
	private JButton clearButton;
	private JCheckBox setAllBox;
	private JTextField setAllField;
	private JTable table;
	private JTable inputTable;
	private JScrollPane scrollPane;

	public TableInputPanel(Class<?> classType, Type type) {
		this.classType = classType;
		this.type = type;
		tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());

		setLayout(new BorderLayout());
		add(createOptionsPanel(), BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
	}

	public void update(Collection<? extends Element> elements, PropertySchema schema, Map<String, T> values,
			AndOrHighlightCondition condition, T valueForAll) {
		this.elements = elements;
		this.schema = schema;
		this.values = values;
		this.condition = condition;

		removeFilterButton.setEnabled(condition != null);
		tablePanel.removeAll();
		tablePanel.add(createInputPanel(filterElements(elements, condition)), BorderLayout.CENTER);
		updateSetAll(valueForAll != null);

		if (valueForAll != null && classType == Double.class) {
			setAllField.setText(valueForAll.toString());
		}
	}

	public Map<String, T> getValues() {
		if (inputTable.isEditing()) {
			inputTable.getCellEditor().stopCellEditing();
		}

		Set<String> filteredIds = new LinkedHashSet<>();
		int idColumn = UI.findColumn(table, TracingColumns.ID);

		for (int row = 0; row < table.getRowCount(); row++) {
			filteredIds.add((String) table.getValueAt(row, idColumn));
		}

		Map<String, T> newValues = new LinkedHashMap<>();

		for (String id : filteredIds) {
			newValues.put(id, values.get(id));
		}

		return newValues;
	}

	@SuppressWarnings("unchecked")
	public T getValueForAll() throws InvalidSettingsException {
		if (classType == Boolean.class && setAllBox.isSelected()) {
			return (T) Boolean.TRUE;
		} else if (classType == Double.class && setAllBox.isSelected()) {
			try {
				return (T) Double.valueOf(Double.parseDouble(setAllField.getText()));
			} catch (NumberFormatException e) {
				throw new InvalidSettingsException("\"" + setAllField.getText() + "\" is not a valid number");
			}
		}

		return null;
	}

	public AndOrHighlightCondition getCondition() {
		return condition;
	}

	@Override
	public void sorterChanged(RowSorterEvent e) {
		if (inputTable.isEditing()) {
			inputTable.getCellEditor().stopCellEditing();
		}

		if (e.getSource() == table.getRowSorter()) {
			int idColumn = UI.findColumn(table, TracingColumns.ID);

			for (int row = 0; row < table.getRowCount(); row++) {
				String id = (String) table.getValueAt(row, idColumn);

				inputTable.setValueAt(values.get(id), row, 0);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void editingStopped(ChangeEvent e) {
		int idColumn = UI.findColumn(table, TracingColumns.ID);

		for (int row = 0; row < table.getRowCount(); row++) {
			String id = (String) table.getValueAt(row, idColumn);

			values.put(id, (T) inputTable.getValueAt(row, 0));
		}
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == inputTable.getSelectionModel()) {
			int hScroll = scrollPane.getHorizontalScrollBar().getValue();

			table.getSelectionModel().removeListSelectionListener(this);
			table.getSelectionModel().setSelectionInterval(inputTable.getSelectionModel().getMinSelectionIndex(),
					inputTable.getSelectionModel().getMaxSelectionIndex());
			table.getSelectionModel().addListSelectionListener(this);

			table.setVisible(false);
			table.scrollRectToVisible(
					new Rectangle(table.getCellRect(inputTable.getSelectionModel().getLeadSelectionIndex(), 0, true)));
			scrollPane.getHorizontalScrollBar().setValue(hScroll);
			table.setVisible(true);
		} else if (e.getSource() == table.getSelectionModel()) {
			inputTable.getSelectionModel().removeListSelectionListener(this);
			inputTable.getSelectionModel().setSelectionInterval(table.getSelectionModel().getMinSelectionIndex(),
					table.getSelectionModel().getMaxSelectionIndex());
			inputTable.getSelectionModel().addListSelectionListener(this);
		}
	}

	private JComponent createOptionsPanel() {
		filterButton = new JButton("Set Filter");
		filterButton.addActionListener(e -> filterPressed());
		removeFilterButton = new JButton("Remove Filter");
		removeFilterButton.setEnabled(false);
		removeFilterButton.addActionListener(e -> removeFilterPressed());
		clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> clear());
		setAllBox = new JCheckBox("Set All");
		setAllBox.addActionListener(e -> updateSetAll(setAllBox.isSelected()));
		setAllField = new JTextField(10);
		setAllField.setEnabled(false);

		if (classType == Boolean.class) {
			return UI.createWestPanel(
					UI.createHorizontalPanel(filterButton, removeFilterButton, clearButton, setAllBox));
		} else if (classType == Double.class) {
			return UI.createWestPanel(
					UI.createHorizontalPanel(filterButton, removeFilterButton, clearButton, setAllBox, setAllField));
		}

		return null;
	}

	private JComponent createInputPanel(Collection<? extends Element> elements) {
		Set<String> idColumns = new LinkedHashSet<>();

		if (type == Type.NODE) {
			idColumns.add(TracingColumns.ID);
		} else if (type == Type.EDGE) {
			idColumns.addAll(Arrays.asList(TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO));
		}

		table = new PropertiesTable(new ArrayList<>(elements), schema, idColumns);
		inputTable = new InputTable(classType, table.getRowCount());

		table.getRowSorter().addRowSorterListener(this);
		table.getRowSorter().toggleSortOrder(0);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		table.setTransferHandler(null);
		inputTable.getDefaultEditor(classType).addCellEditorListener(this);
		inputTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		inputTable.getSelectionModel().addListSelectionListener(this);
		scrollPane = new JScrollPane();
		scrollPane.setRowHeaderView(inputTable);
		scrollPane.setViewportView(table);

		JViewport rowHeader = scrollPane.getRowHeader();

		rowHeader.setPreferredSize(new Dimension(100, rowHeader.getPreferredSize().height));

		return scrollPane;
	}

	private void filterPressed() {
		HighlightDialog dialog = HighlightDialog.createFilterDialog(filterButton, schema, condition,
				new TracingPropertySelectorCreator());

		dialog.setLocationRelativeTo(filterButton);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			condition = (AndOrHighlightCondition) dialog.getHighlightCondition();
			tablePanel.removeAll();
			tablePanel.add(createInputPanel(filterElements(elements, condition)), BorderLayout.CENTER);
			tablePanel.revalidate();
			updateSetAll(setAllBox.isSelected());
			removeFilterButton.setEnabled(true);
		}
	}

	private void removeFilterPressed() {
		condition = null;
		tablePanel.removeAll();
		tablePanel.add(createInputPanel(elements), BorderLayout.CENTER);
		tablePanel.revalidate();
		updateSetAll(setAllBox.isSelected());
		removeFilterButton.setEnabled(false);
	}

	private void clear() {
		if (setAllBox.isSelected()) {
			setAllBox.setSelected(false);
			updateSetAll(false);
		}

		for (int i = 0; i < inputTable.getRowCount(); i++) {
			inputTable.setValueAt(null, i, 0);
		}
	}

	private void updateSetAll(boolean setAll) {
		setAllBox.setSelected(setAll);
		setAllField.setEnabled(setAll);

		if (setAll) {
			scrollPane.setRowHeader(null);
		} else {
			int value = scrollPane.getVerticalScrollBar().getValue();

			scrollPane.setRowHeaderView(inputTable);
			scrollPane.getVerticalScrollBar().setValue(0);
			scrollPane.getVerticalScrollBar().setValue(value);

			JViewport rowHeader = scrollPane.getRowHeader();

			rowHeader.setPreferredSize(new Dimension(100, rowHeader.getPreferredSize().height));
		}
	}

	private static <T extends Element> Collection<T> filterElements(Collection<T> nodes,
			AndOrHighlightCondition condition) {
		if (condition == null) {
			return nodes;
		}

		return condition.getValues(nodes).entrySet().stream().filter(e -> e.getValue() != 0.0).map(e -> e.getKey())
				.collect(Collectors.toList());
	}
}
