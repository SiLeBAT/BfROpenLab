/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.openkrise.util.tracing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import de.bund.bfr.knime.openkrise.TracingConstants;

public class TableInputPanel<T> extends JPanel implements ActionListener,
		RowSorterListener, CellEditorListener, ListSelectionListener {

	public static enum Type {
		NODE, EDGE
	}

	private static final long serialVersionUID = 1L;

	private Class<?> classType;
	private Type type;

	private Map<String, T> values;
	private AndOrHighlightCondition condition;

	private Map<String, Class<?>> properties;
	private Collection<? extends Element> elements;

	private JPanel tablePanel;

	private JButton filterButton;
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

	public void update(Collection<? extends Element> elements,
			Map<String, Class<?>> properties, Map<String, T> values,
			AndOrHighlightCondition condition, T valueForAll) {
		this.elements = elements;
		this.properties = properties;
		this.values = values;
		this.condition = condition;

		tablePanel.removeAll();
		tablePanel.add(
				createInputPanel(filterElements(elements, condition),
						properties), BorderLayout.CENTER);
		updateSetAll(valueForAll != null);

		if (valueForAll != null && classType == Double.class) {
			setAllField.setText(valueForAll.toString());
		}
	}

	public Map<String, T> getValues() {
		if (inputTable.isEditing()) {
			inputTable.getDefaultEditor(classType).stopCellEditing();
		}

		Set<String> filteredIds = new LinkedHashSet<>();
		int idColumn = UI.findColumn(table, TracingConstants.ID_COLUMN);

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
				return (T) new Double(Double.parseDouble(setAllField.getText()));
			} catch (NumberFormatException e) {
				throw new InvalidSettingsException("\"" + setAllField.getText()
						+ "\" is not a valid number");
			}
		}

		return null;
	}

	public AndOrHighlightCondition getCondition() {
		return condition;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == filterButton) {
			HighlightDialog dialog = new HighlightDialog(filterButton,
					properties, false, false, false, false, false, false,
					condition, null);

			dialog.setLocationRelativeTo(filterButton);
			dialog.setVisible(true);

			if (dialog.isApproved()) {
				condition = (AndOrHighlightCondition) dialog
						.getHighlightCondition();
				tablePanel.removeAll();
				tablePanel.add(
						createInputPanel(filterElements(elements, condition),
								properties), BorderLayout.CENTER);
				tablePanel.revalidate();
				updateSetAll(setAllBox.isSelected());
			}
		} else if (e.getSource() == clearButton) {
			if (setAllBox.isSelected()) {
				setAllBox.setSelected(false);
				updateSetAll(false);
			}

			for (int i = 0; i < inputTable.getRowCount(); i++) {
				inputTable.setValueAt(null, i, 0);
			}
		} else if (e.getSource() == setAllBox) {
			updateSetAll(setAllBox.isSelected());
		}
	}

	@Override
	public void sorterChanged(RowSorterEvent e) {
		if (inputTable.isEditing()) {
			inputTable.getDefaultEditor(classType).stopCellEditing();
		}

		if (e.getSource() == table.getRowSorter()) {
			int idColumn = UI.findColumn(table, TracingConstants.ID_COLUMN);

			for (int row = 0; row < table.getRowCount(); row++) {
				String id = (String) table.getValueAt(row, idColumn);

				inputTable.setValueAt(values.get(id), row, 0);
			}
		}

		// int i = table.getSelectionModel().getAnchorSelectionIndex();
		//
		// System.out.println(i);
		// inputTable.getSelectionModel().removeListSelectionListener(this);
		// inputTable.getSelectionModel().setSelectionInterval(i, i);
		// inputTable.getSelectionModel().addListSelectionListener(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void editingStopped(ChangeEvent e) {
		int idColumn = UI.findColumn(table, TracingConstants.ID_COLUMN);

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
			int i = inputTable.getSelectionModel().getAnchorSelectionIndex();
			int hScroll = scrollPane.getHorizontalScrollBar().getValue();

			table.getSelectionModel().setSelectionInterval(i, i);
			table.setVisible(false);
			table.scrollRectToVisible(new Rectangle(table.getCellRect(i, 0,
					true)));
			scrollPane.getHorizontalScrollBar().setValue(hScroll);
			table.setVisible(true);
		} else if (e.getSource() == table.getSelectionModel()) {
			int i = table.getSelectionModel().getAnchorSelectionIndex();

			inputTable.getSelectionModel().removeListSelectionListener(this);
			inputTable.getSelectionModel().setSelectionInterval(i, i);
			inputTable.getSelectionModel().addListSelectionListener(this);
		}
	}

	private JComponent createOptionsPanel() {
		filterButton = new JButton("Filter");
		filterButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		setAllBox = new JCheckBox("Set All");
		setAllBox.addActionListener(this);
		setAllField = new JTextField(10);
		setAllField.setEnabled(false);

		if (classType == Boolean.class) {
			return UI.createWestPanel(UI.createHorizontalPanel(filterButton,
					clearButton, setAllBox));
		} else if (classType == Double.class) {
			return UI.createWestPanel(UI.createHorizontalPanel(filterButton,
					clearButton, setAllBox, setAllField));
		}

		return null;
	}

	private JComponent createInputPanel(Collection<? extends Element> nodes,
			Map<String, Class<?>> nodeProperties) {
		Set<String> idColumns = new LinkedHashSet<>();

		switch (type) {
		case NODE:
			idColumns.add(TracingConstants.ID_COLUMN);
			break;
		case EDGE:
			idColumns.addAll(Arrays.asList(TracingConstants.ID_COLUMN,
					TracingConstants.FROM_COLUMN, TracingConstants.TO_COLUMN));
			break;
		}

		table = new PropertiesTable(new ArrayList<>(nodes), nodeProperties,
				idColumns);
		inputTable = new InputTable(classType, table.getRowCount());

		table.getRowSorter().addRowSorterListener(this);
		table.getRowSorter().toggleSortOrder(0);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		inputTable.getDefaultEditor(classType).addCellEditorListener(this);
		inputTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inputTable.getSelectionModel().addListSelectionListener(this);
		scrollPane = new JScrollPane();
		scrollPane.setRowHeaderView(inputTable);
		scrollPane.setViewportView(table);

		JViewport rowHeader = scrollPane.getRowHeader();

		rowHeader.setPreferredSize(new Dimension(100, rowHeader
				.getPreferredSize().height));

		return scrollPane;
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

			rowHeader.setPreferredSize(new Dimension(100, rowHeader
					.getPreferredSize().height));
		}
	}

	private static <T extends Element> Collection<T> filterElements(
			Collection<T> nodes, AndOrHighlightCondition condition) {
		if (condition == null) {
			return nodes;
		}

		Collection<T> filteredNodes = new ArrayList<>();
		Map<T, Double> values = condition.getValues(nodes);

		for (T node : values.keySet()) {
			if (values.get(node) != 0.0) {
				filteredNodes.add(node);
			}
		}

		return filteredNodes;
	}
}
