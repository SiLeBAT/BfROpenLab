/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingparameters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesTable;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.openkrise.views.TracingConstants;

public class TableInputPanel<T> extends JPanel implements ActionListener,
		RowSorterListener, CellEditorListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private Class<?> type;

	private Map<Integer, T> values;
	private AndOrHighlightCondition condition;

	private Map<String, Class<?>> nodeProperties;
	private Collection<GraphNode> nodes;

	private JPanel tablePanel;

	private JButton filterButton;
	private JButton setAllButton;
	private JTable table;
	private JTable inputTable;
	private JScrollPane scrollPane;

	public TableInputPanel(Class<?> type) {
		this.type = type;
		tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());

		setLayout(new BorderLayout());
		add(createOptionsPanel(), BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
	}

	public void update(Collection<GraphNode> nodes,
			Map<String, Class<?>> nodeProperties, Map<Integer, T> values,
			AndOrHighlightCondition condition) {
		this.nodes = nodes;
		this.nodeProperties = nodeProperties;
		this.values = values;
		this.condition = condition;

		tablePanel.removeAll();
		tablePanel
				.add(createInputPanel(filterNodes(nodes, condition),
						nodeProperties), BorderLayout.CENTER);
	}

	public Map<Integer, T> getValues() {
		if (inputTable.isEditing()) {
			inputTable.getDefaultEditor(type).stopCellEditing();
		}

		Set<Integer> filteredIds = new LinkedHashSet<Integer>();
		int idColumn = UI.findColumn(table, TracingConstants.ID_COLUMN);

		for (int row = 0; row < table.getRowCount(); row++) {
			filteredIds.add((Integer) table.getValueAt(row, idColumn));
		}

		Map<Integer, T> newValues = new LinkedHashMap<Integer, T>();

		for (int id : filteredIds) {
			newValues.put(id, values.get(id));
		}

		return newValues;
	}

	public AndOrHighlightCondition getCondition() {
		return condition;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == filterButton) {
			HighlightDialog dialog = new HighlightDialog(filterButton,
					nodeProperties, false, false, false, false, false,
					condition);

			dialog.setLocationRelativeTo(filterButton);
			dialog.setVisible(true);

			if (dialog.isApproved()) {
				condition = (AndOrHighlightCondition) dialog
						.getHighlightCondition();
				tablePanel.removeAll();
				tablePanel.add(
						createInputPanel(filterNodes(nodes, condition),
								nodeProperties), BorderLayout.CENTER);
				tablePanel.revalidate();
			}
		} else if (e.getSource() == setAllButton) {
			if (type == Boolean.class) {
				Object result = JOptionPane.showInputDialog(this,
						"Set All Values to?", "Input",
						JOptionPane.QUESTION_MESSAGE, null, new Boolean[] {
								Boolean.TRUE, Boolean.FALSE }, Boolean.TRUE);

				if (result != null) {
					setAllValuesTo(result);
				}
			} else if (type == Double.class) {
				Object result = JOptionPane.showInputDialog(this,
						"Set All Values to?", "Input",
						JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
				Double value = null;

				try {
					value = Double.parseDouble(result.toString());
				} catch (NumberFormatException ex) {
				} catch (NullPointerException ex) {
				}

				if (value != null) {
					setAllValuesTo(value);
				}
			}
		}
	}

	@Override
	public void sorterChanged(RowSorterEvent e) {
		if (e.getSource() == table.getRowSorter()) {
			int idColumn = UI.findColumn(table, TracingConstants.ID_COLUMN);

			for (int row = 0; row < table.getRowCount(); row++) {
				int id = (Integer) table.getValueAt(row, idColumn);

				inputTable.setValueAt(values.get(id), row, 0);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void editingStopped(ChangeEvent e) {
		int idColumn = UI.findColumn(table, TracingConstants.ID_COLUMN);

		for (int row = 0; row < table.getRowCount(); row++) {
			int id = (Integer) table.getValueAt(row, idColumn);

			values.put(id, (T) inputTable.getValueAt(row, 0));
		}
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int i = inputTable.getSelectionModel().getAnchorSelectionIndex();
		int hScroll = scrollPane.getHorizontalScrollBar().getValue();

		table.getSelectionModel().setSelectionInterval(i, i);
		table.setVisible(false);
		table.scrollRectToVisible(new Rectangle(table.getCellRect(i, 0, true)));
		scrollPane.getHorizontalScrollBar().setValue(hScroll);
		table.setVisible(true);
	}

	private JComponent createOptionsPanel() {
		filterButton = new JButton("Filter");
		filterButton.addActionListener(this);
		setAllButton = new JButton("Set All To");
		setAllButton.addActionListener(this);

		return UI.createWestPanel(UI.createButtonPanel(filterButton,
				setAllButton));
	}

	private JComponent createInputPanel(Collection<GraphNode> nodes,
			Map<String, Class<?>> nodeProperties) {
		table = new PropertiesTable(nodes, nodeProperties);
		inputTable = new InputTable(type, table.getRowCount());

		table.getRowSorter().addRowSorterListener(this);
		table.getRowSorter().toggleSortOrder(0);
		inputTable.getDefaultEditor(type).addCellEditorListener(this);
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

	private void setAllValuesTo(Object value) {
		for (int row = 0; row < inputTable.getRowCount(); row++) {
			inputTable.setValueAt(value, row, 0);
		}

		editingStopped(null);
	}

	private static Collection<GraphNode> filterNodes(
			Collection<GraphNode> nodes, AndOrHighlightCondition condition) {
		if (condition == null) {
			return nodes;
		}

		Collection<GraphNode> filteredNodes = new ArrayList<GraphNode>();
		Map<GraphNode, Double> values = condition.getValues(nodes);

		for (GraphNode node : values.keySet()) {
			if (values.get(node) != 0.0) {
				filteredNodes.add(node);
			}
		}

		return filteredNodes;
	}
}
