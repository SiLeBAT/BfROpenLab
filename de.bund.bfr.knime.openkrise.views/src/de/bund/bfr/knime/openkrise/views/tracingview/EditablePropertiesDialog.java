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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesTable;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.openkrise.TracingConstants;

public class EditablePropertiesDialog extends JDialog implements
		ActionListener, CellEditorListener, RowSorterListener,
		ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private static enum Type {
		NODE, EDGE
	}

	private TracingCanvas parent;
	private Type type;

	private List<Element> elementList;

	private JScrollPane scrollPane;
	private PropertiesTable table;
	private InputTable inputTable;
	private JButton okButton;
	private JButton cancelButton;

	private JButton selectButton;
	private JButton weightButton;
	private JButton contaminationButton;
	private JButton filterButton;

	private boolean approved;

	private Map<String, InputTable.Input> values;

	private EditablePropertiesDialog(TracingCanvas parent,
			Collection<? extends Element> elements,
			Map<String, Class<?>> properties, Type type,
			boolean allowViewSelection) {
		super(SwingUtilities.getWindowAncestor(parent), "Properties",
				DEFAULT_MODALITY_TYPE);
		this.parent = parent;
		this.type = type;

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

		Map<String, Class<?>> uneditableProperties = new LinkedHashMap<>(
				properties);

		uneditableProperties.remove(TracingConstants.WEIGHT_COLUMN);
		uneditableProperties
				.remove(TracingConstants.CROSS_CONTAMINATION_COLUMN);
		uneditableProperties.remove(TracingConstants.OBSERVED_COLUMN);

		elementList = new ArrayList<>(elements);
		table = new PropertiesTable(elementList, uneditableProperties,
				idColumns);
		table.getRowSorter().addRowSorterListener(this);
		inputTable = new InputTable(elementList);
		inputTable.getColumn(InputTable.INPUT).getCellEditor()
				.addCellEditorListener(this);
		inputTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inputTable.getSelectionModel().addListSelectionListener(this);
		values = new LinkedHashMap<>();
		updateValues();

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		selectButton = new JButton("Select in View");
		selectButton.addActionListener(this);
		weightButton = new JButton("Set All " + TracingConstants.WEIGHT_COLUMN);
		weightButton.addActionListener(this);
		contaminationButton = new JButton("Set All "
				+ TracingConstants.CROSS_CONTAMINATION_COLUMN);
		contaminationButton.addActionListener(this);
		filterButton = new JButton("Set All "
				+ TracingConstants.OBSERVED_COLUMN);
		filterButton.addActionListener(this);

		JPanel cornerPanel = new JPanel();

		cornerPanel.setLayout(new GridLayout(1, 3));
		cornerPanel
				.add(getTableHeaderComponent(TracingConstants.WEIGHT_COLUMN));
		cornerPanel
				.add(getTableHeaderComponent(TracingConstants.CROSS_CONTAMINATION_COLUMN));
		cornerPanel
				.add(getTableHeaderComponent(TracingConstants.OBSERVED_COLUMN));

		scrollPane = new JScrollPane();
		scrollPane
				.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, cornerPanel);
		scrollPane.setRowHeaderView(inputTable);
		scrollPane.setViewportView(table);
		scrollPane.setPreferredSize(UI.getMaxDimension(
				scrollPane.getPreferredSize(), table.getPreferredSize()));

		JViewport rowHeader = scrollPane.getRowHeader();

		rowHeader.setPreferredSize(new Dimension(
				cornerPanel.getMinimumSize().width, rowHeader
						.getPreferredSize().height));

		JPanel southPanel = new JPanel();

		southPanel.setLayout(new BorderLayout());
		southPanel.add(
				UI.createEmptyBorderPanel(new JLabel("Number of Elements: "
						+ elements.size())), BorderLayout.WEST);
		southPanel.add(UI.createHorizontalPanel(okButton, cancelButton),
				BorderLayout.EAST);

		List<JButton> buttons = new ArrayList<>();

		if (allowViewSelection) {
			buttons.add(selectButton);
		}

		if (properties.containsKey(TracingConstants.WEIGHT_COLUMN)) {
			buttons.add(weightButton);
		}

		if (properties.containsKey(TracingConstants.CROSS_CONTAMINATION_COLUMN)) {
			buttons.add(contaminationButton);
		}

		if (properties.containsKey(TracingConstants.OBSERVED_COLUMN)) {
			buttons.add(filterButton);
		}

		setLayout(new BorderLayout());
		add(UI.createWestPanel(UI.createHorizontalPanel(buttons
				.toArray(new JButton[0]))), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this);
	}

	public static EditablePropertiesDialog createNodeDialog(
			TracingCanvas parent, Collection<GraphNode> nodes,
			Map<String, Class<?>> properties, boolean allowViewSelection) {
		return new EditablePropertiesDialog(parent, nodes, properties,
				Type.NODE, allowViewSelection);
	}

	public static <V extends Node> EditablePropertiesDialog createEdgeDialog(
			TracingCanvas parent, Collection<Edge<GraphNode>> edges,
			Map<String, Class<?>> properties, boolean allowViewSelection) {
		return new EditablePropertiesDialog(parent, edges, properties,
				Type.EDGE, allowViewSelection);
	}

	public boolean isApproved() {
		return approved;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			approved = true;

			if (inputTable.isEditing()) {
				inputTable.getColumn(InputTable.INPUT).getCellEditor()
						.stopCellEditing();
			}

			for (Element element : elementList) {
				InputTable.Input input = values.get(element.getId());

				element.getProperties().put(TracingConstants.WEIGHT_COLUMN,
						input.getWeight());
				element.getProperties().put(
						TracingConstants.CROSS_CONTAMINATION_COLUMN,
						input.isCrossContamination());
				element.getProperties().put(TracingConstants.OBSERVED_COLUMN,
						input.isObserved());
			}

			dispose();
		} else if (e.getSource() == cancelButton) {
			approved = false;
			dispose();
		} else if (e.getSource() == selectButton) {
			switch (type) {
			case NODE:
				Set<GraphNode> nodes = new LinkedHashSet<>();

				for (Element element : table.getSelectedElements()) {
					nodes.add((GraphNode) element);
				}

				parent.setSelectedNodes(nodes);
				break;
			case EDGE:
				Set<Edge<GraphNode>> edges = new LinkedHashSet<>();

				for (Element element : table.getSelectedElements()) {
					edges.add((Edge<GraphNode>) element);
				}

				parent.setSelectedEdges(edges);
				break;
			}
		} else if (e.getSource() == weightButton) {
			Object result = JOptionPane.showInputDialog(this,
					"Set All Values to?", TracingConstants.WEIGHT_COLUMN,
					JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
			Double value = null;

			try {
				value = Double.parseDouble(result.toString());
			} catch (NumberFormatException ex) {
			} catch (NullPointerException ex) {
			}

			if (value != null) {
				setAllValuesTo(TracingConstants.WEIGHT_COLUMN, value);
			}
		} else if (e.getSource() == contaminationButton) {
			Object result = JOptionPane.showInputDialog(this,
					"Set All Values to?",
					TracingConstants.CROSS_CONTAMINATION_COLUMN,
					JOptionPane.QUESTION_MESSAGE, null, new Boolean[] {
							Boolean.TRUE, Boolean.FALSE }, Boolean.TRUE);

			if (result != null) {
				setAllValuesTo(TracingConstants.CROSS_CONTAMINATION_COLUMN,
						result);
			}
		} else if (e.getSource() == filterButton) {
			Object result = JOptionPane.showInputDialog(this,
					"Set All Values to?", TracingConstants.OBSERVED_COLUMN,
					JOptionPane.QUESTION_MESSAGE, null, new Boolean[] {
							Boolean.TRUE, Boolean.FALSE }, Boolean.TRUE);

			if (result != null) {
				setAllValuesTo(TracingConstants.OBSERVED_COLUMN, result);
			}
		}
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		updateValues();
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	@Override
	public void sorterChanged(RowSorterEvent e) {
		if (inputTable.isEditing()) {
			inputTable.getColumn(InputTable.INPUT).getCellEditor()
					.stopCellEditing();
		}
		
		applyValues();
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

	private void updateValues() {
		int idColumn = UI.findColumn(table, TracingConstants.ID_COLUMN);

		for (int row = 0; row < table.getRowCount(); row++) {
			String id = (String) table.getValueAt(row, idColumn);

			values.put(id, (InputTable.Input) inputTable.getValueAt(row, 0));
		}
	}

	private void applyValues() {
		int idColumn = UI.findColumn(table, TracingConstants.ID_COLUMN);

		for (int row = 0; row < table.getRowCount(); row++) {
			String id = (String) table.getValueAt(row, idColumn);

			inputTable.setValueAt(values.get(id), row, 0);
		}
	}

	private void setAllValuesTo(String column, Object value) {
		for (InputTable.Input input : values.values()) {
			if (column.equals(TracingConstants.WEIGHT_COLUMN)) {
				input.setWeight((Double) value);
			} else if (column
					.equals(TracingConstants.CROSS_CONTAMINATION_COLUMN)) {
				input.setCrossContamination((Boolean) value);
			} else if (column.equals(TracingConstants.OBSERVED_COLUMN)) {
				input.setObserved((Boolean) value);
			}
		}

		applyValues();
	}

	private static Component getTableHeaderComponent(String name) {
		JTable table = new JTable(new Object[1][1], new Object[] { name });

		return table.getTableHeader().getDefaultRenderer()
				.getTableCellRendererComponent(table, name, false, false, 0, 0);
	}
}
