/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesTable;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesTableTransferHandler;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.KnimeDialog;

public class EditablePropertiesDialog<V extends Node> extends KnimeDialog
		implements CellEditorListener, RowSorterListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private static enum Type {
		NODE, EDGE
	}

	private ICanvas<V> parent;
	private Type type;

	private List<Element> elementList;

	private JScrollPane scrollPane;
	private PropertiesTable table;
	private InputTable inputTable;

	private boolean approved;

	private Map<String, InputTable.Input> values;

	private EditablePropertiesDialog(ICanvas<V> parent, Collection<? extends Element> elements, PropertySchema schema,
			Type type, boolean allowViewSelection) {
		super(parent.getComponent(), "Properties", DEFAULT_MODALITY_TYPE);
		this.parent = parent;
		this.type = type;

		Set<String> idColumns = new LinkedHashSet<>();

		if (type == Type.NODE) {
			idColumns.add(TracingColumns.ID);
		} else if (type == Type.EDGE) {
			idColumns.addAll(Arrays.asList(TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO));
		}

		elementList = new ArrayList<>(elements);
		table = new PropertiesTable(elementList, schema, idColumns);
		table.getRowSorter().addRowSorterListener(this);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		TableColumnModel columnModel = table.getColumnModel();

		columnModel.moveColumn(columnModel.getColumnIndex(TracingColumns.WEIGHT), 0);
		columnModel.moveColumn(columnModel.getColumnIndex(TracingColumns.CROSS_CONTAMINATION), 1);
		columnModel.moveColumn(columnModel.getColumnIndex(TracingColumns.KILL_CONTAMINATION), 2);
		columnModel.moveColumn(columnModel.getColumnIndex(TracingColumns.OBSERVED), 3);
		setInvisible(table.getColumn(TracingColumns.WEIGHT));
		setInvisible(table.getColumn(TracingColumns.CROSS_CONTAMINATION));
		setInvisible(table.getColumn(TracingColumns.KILL_CONTAMINATION));
		setInvisible(table.getColumn(TracingColumns.OBSERVED));

		TableCellRenderer boldHeaderRenderer = new BoldHeaderRenderer(table.getTableHeader().getDefaultRenderer());

		for (String column : TracingColumns.OUTPUT_COLUMNS) {
			table.getColumn(column).setHeaderRenderer(boldHeaderRenderer);
		}

		InputTable.Header inputTableHeader = new InputTable.Header();

		inputTable = new InputTable(inputTableHeader, elementList);
		inputTable.getColumn(InputTable.INPUT).getCellEditor().addCellEditorListener(this);
		inputTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		inputTable.getSelectionModel().addListSelectionListener(this);
		inputTable.setTransferHandler(new PropertiesTableTransferHandler(table));
		values = new LinkedHashMap<>();
		updateValues();

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		JButton selectButton = new JButton("Select in View");

		okButton.addActionListener(e -> okButtonPressed());
		cancelButton.addActionListener(e -> dispose());
		selectButton.addActionListener(e -> selectButtonPressed());

		JButton weightButton = new JButton("Set All " + TracingColumns.WEIGHT);
		JButton crossButton = new JButton("Set All " + TracingColumns.CROSS_CONTAMINATION);
		JButton killButton = new JButton("Set All " + TracingColumns.KILL_CONTAMINATION);
		JButton observedButton = new JButton("Set All " + TracingColumns.OBSERVED);

		weightButton.addActionListener(e -> weightSetAllButtonClicked());
		crossButton.addActionListener(e -> booleanSetAllButtonClicked(TracingColumns.CROSS_CONTAMINATION));
		killButton.addActionListener(e -> booleanSetAllButtonClicked(TracingColumns.KILL_CONTAMINATION));
		observedButton.addActionListener(e -> booleanSetAllButtonClicked(TracingColumns.OBSERVED));

		scrollPane = new JScrollPane();
		scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, inputTableHeader);
		scrollPane.setRowHeaderView(inputTable);
		scrollPane.setViewportView(table);
		scrollPane.setPreferredSize(UI.getMaxDimension(scrollPane.getPreferredSize(), table.getPreferredSize()));

		JViewport rowHeader = scrollPane.getRowHeader();

		rowHeader.setPreferredSize(
				new Dimension(inputTableHeader.getPreferredSize().width, rowHeader.getPreferredSize().height));

		JPanel southPanel = new JPanel();

		southPanel.setLayout(new BorderLayout());
		southPanel.add(UI.createBorderPanel(new JLabel("Number of Elements: " + elements.size())), BorderLayout.WEST);
		southPanel.add(UI.createHorizontalPanel(okButton, cancelButton), BorderLayout.EAST);

		List<JButton> buttons = new ArrayList<>();

		if (allowViewSelection) {
			buttons.add(selectButton);
		}

		buttons.add(weightButton);
		buttons.add(crossButton);
		buttons.add(killButton);
		buttons.add(observedButton);
		setLayout(new BorderLayout());
		add(UI.createWestPanel(UI.createHorizontalPanel(buttons.toArray(new JButton[0]))), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(parent.getComponent());
		getRootPane().setDefaultButton(okButton);

		approved = false;
	}

	public static <V extends Node> EditablePropertiesDialog<V> createNodeDialog(ICanvas<V> parent, Collection<V> nodes,
			NodePropertySchema schema, boolean allowViewSelection) {
		return new EditablePropertiesDialog<>(parent, nodes, schema, Type.NODE, allowViewSelection);
	}

	public static <V extends Node> EditablePropertiesDialog<V> createEdgeDialog(ICanvas<V> parent,
			Collection<Edge<V>> edges, EdgePropertySchema schema, boolean allowViewSelection) {
		return new EditablePropertiesDialog<>(parent, edges, schema, Type.EDGE, allowViewSelection);
	}

	public boolean isApproved() {
		return approved;
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
			inputTable.getCellEditor().stopCellEditing();
		}

		applyValues();
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

	private void updateValues() {
		int idColumn = UI.findColumn(table, TracingColumns.ID);

		for (int row = 0; row < table.getRowCount(); row++) {
			String id = (String) table.getValueAt(row, idColumn);
			InputTable.Input input = (InputTable.Input) inputTable.getValueAt(row, 0);

			values.put(id, input);
			table.setValueAt(input.getWeight(), row, table.getColumnModel().getColumnIndex(TracingColumns.WEIGHT));
			table.setValueAt(input.isCrossContamination(), row,
					table.getColumnModel().getColumnIndex(TracingColumns.CROSS_CONTAMINATION));
			table.setValueAt(input.isKillContamination(), row,
					table.getColumnModel().getColumnIndex(TracingColumns.KILL_CONTAMINATION));
			table.setValueAt(input.isObserved(), row, table.getColumnModel().getColumnIndex(TracingColumns.OBSERVED));
		}
	}

	private void applyValues() {
		int idColumn = UI.findColumn(table, TracingColumns.ID);

		for (int row = 0; row < table.getRowCount(); row++) {
			String id = (String) table.getValueAt(row, idColumn);

			inputTable.setValueAt(values.get(id), row, 0);
		}
	}

	private void okButtonPressed() {
		approved = true;

		if (inputTable.isEditing()) {
			inputTable.getCellEditor().stopCellEditing();
		}

		for (Element element : elementList) {
			InputTable.Input input = values.get(element.getId());

			element.getProperties().put(TracingColumns.WEIGHT, input.getWeight());
			element.getProperties().put(TracingColumns.CROSS_CONTAMINATION, input.isCrossContamination());
			element.getProperties().put(TracingColumns.KILL_CONTAMINATION, input.isKillContamination());
			element.getProperties().put(TracingColumns.OBSERVED, input.isObserved());
		}

		dispose();
	}

	@SuppressWarnings("unchecked")
	private void selectButtonPressed() {
		if (type == Type.NODE) {
			Set<V> nodes = new LinkedHashSet<>();

			for (Element element : table.getSelectedElements()) {
				nodes.add((V) element);
			}

			parent.setSelectedNodes(nodes);
		} else if (type == Type.EDGE) {
			Set<Edge<V>> edges = new LinkedHashSet<>();

			for (Element element : table.getSelectedElements()) {
				edges.add((Edge<V>) element);
			}

			parent.setSelectedEdges(edges);
		}
	}

	private void weightSetAllButtonClicked() {
		String result = Dialogs.showInputDialog(this, "Set All Values to?", TracingColumns.WEIGHT, "1.0");
		Double value = null;

		if (result != null) {
			try {
				value = Double.parseDouble(result.toString());
			} catch (NumberFormatException ex) {
				Dialogs.showErrorMessage(this, result.toString() + " is not a valid number");
			}
		}

		if (value != null) {
			if (inputTable.isEditing()) {
				inputTable.getCellEditor().stopCellEditing();
			}

			setAllValuesTo(TracingColumns.WEIGHT, value);
		}
	}

	private void booleanSetAllButtonClicked(String column) {
		String result = Dialogs.showInputDialog(this, "Set All Values to?", column,
				Arrays.asList(Boolean.TRUE.toString(), Boolean.FALSE.toString()));

		if (result != null) {
			if (inputTable.isEditing()) {
				inputTable.getCellEditor().stopCellEditing();
			}

			setAllValuesTo(column, Boolean.parseBoolean(result));
		}
	}

	private void setAllValuesTo(String column, Object value) {
		for (InputTable.Input input : values.values()) {
			if (column.equals(TracingColumns.WEIGHT)) {
				input.setWeight((Double) value);
			} else if (column.equals(TracingColumns.CROSS_CONTAMINATION)) {
				input.setCrossContamination((Boolean) value);
			} else if (column.equals(TracingColumns.KILL_CONTAMINATION)) {
				input.setKillContamination((Boolean) value);
			} else if (column.equals(TracingColumns.OBSERVED)) {
				input.setObserved((Boolean) value);
			}
		}

		applyValues();
	}

	private static void setInvisible(TableColumn column) {
		column.setMinWidth(0);
		column.setMaxWidth(0);
		column.setPreferredWidth(0);
	}

	private static class BoldHeaderRenderer implements UIResource, TableCellRenderer {

		private TableCellRenderer original;

		public BoldHeaderRenderer(TableCellRenderer original) {
			this.original = original;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component c = original.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			c.setFont(c.getFont().deriveFont(Font.BOLD));

			return c;
		}
	}
}
