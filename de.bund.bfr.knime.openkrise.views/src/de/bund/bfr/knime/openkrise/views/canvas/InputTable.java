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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.ui.DoubleCellRenderer;

public class InputTable extends JTable {

	public static final String INPUT = "Input";

	private static final long serialVersionUID = 1L;

	private static final Color GRID_COLOR = new JTable().getGridColor();

	public InputTable(List<? extends Element> elements) {
		List<Input> inputs = new ArrayList<>();

		for (Element e : elements) {
			double weight = e.getProperties().get(TracingColumns.WEIGHT) != null
					? (Double) e.getProperties().get(TracingColumns.WEIGHT) : 0.0;
			boolean crossContamination = e.getProperties().get(TracingColumns.CROSS_CONTAMINATION) != null
					? (Boolean) e.getProperties().get(TracingColumns.CROSS_CONTAMINATION) : false;
			boolean killContamination = e.getProperties().get(TracingColumns.KILL_CONTAMINATION) != null
					? (Boolean) e.getProperties().get(TracingColumns.KILL_CONTAMINATION) : false;
			boolean observed = e.getProperties().get(TracingColumns.OBSERVED) != null
					? (Boolean) e.getProperties().get(TracingColumns.OBSERVED) : false;

			inputs.add(new Input(weight, crossContamination, killContamination, observed));
		}

		setModel(new InputTableModel(inputs));
		setRowHeight(new JCheckBox().getPreferredSize().height);
		getColumn(INPUT).setCellRenderer(new InputRenderer());
		getColumn(INPUT).setCellEditor(new InputEditor());
	}

	public static class Input {

		private double weight;
		private boolean crossContamination;
		private boolean killContamination;
		private boolean observed;

		public Input(double weight, boolean crossContamination, boolean killContamination, boolean observed) {
			this.weight = weight;
			this.crossContamination = crossContamination;
			this.killContamination = killContamination;
			this.observed = observed;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}

		public boolean isCrossContamination() {
			return crossContamination;
		}

		public void setCrossContamination(boolean crossContamination) {
			this.crossContamination = crossContamination;
		}

		public boolean isKillContamination() {
			return killContamination;
		}

		public void setKillContamination(boolean killContamination) {
			this.killContamination = killContamination;
		}

		public boolean isObserved() {
			return observed;
		}

		public void setObserved(boolean observed) {
			this.observed = observed;
		}

		@Override
		public String toString() {
			return "Input [weight=" + weight + ", crossContamination=" + crossContamination + ", observed=" + observed
					+ "]";
		}
	}

	private static class InputTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<Input> inputs;

		public InputTableModel(List<Input> inputs) {
			this.inputs = inputs;
		}

		@Override
		public int getRowCount() {
			return inputs.size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int column) {
			return INPUT;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return Input.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return inputs.get(rowIndex);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			inputs.set(rowIndex, (Input) aValue);
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
	}

	private static class InputRenderer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Input input = (Input) value;
			JPanel panel = new JPanel();

			panel.setBackground(GRID_COLOR);
			panel.setLayout(new GridLayout(1, 4, 1, 0));
			panel.add(getTableRendererComponent(input.getWeight(), Double.class, isSelected, hasFocus));
			panel.add(getTableRendererComponent(input.isCrossContamination(), Boolean.class, isSelected, hasFocus));
			panel.add(getTableRendererComponent(input.isKillContamination(), Boolean.class, isSelected, hasFocus));
			panel.add(getTableRendererComponent(input.isObserved(), Boolean.class, isSelected, hasFocus));

			return panel;
		}

		private static Component getTableRendererComponent(Object value, Class<?> columnClass, boolean isSelected,
				boolean hasFocus) {
			JTable table = new JTable(new Object[][] { { value } }, new Object[] { "" });

			table.setDefaultRenderer(Double.class, new DoubleCellRenderer());

			return table.getDefaultRenderer(columnClass).getTableCellRendererComponent(table, value, isSelected,
					hasFocus, 0, 0);
		}
	}

	private static class InputEditor extends AbstractCellEditor implements TableCellEditor, CellEditorListener {

		private static final long serialVersionUID = 1L;

		private JTable weightTable;
		private JTable ccTable;
		private JTable killTable;
		private JTable observedTable;

		private JTextField weightField;

		public InputEditor() {
			weightTable = new JTable(new SimpleModel(Double.class));
			ccTable = new JTable(new SimpleModel(Boolean.class));
			killTable = new JTable(new SimpleModel(Boolean.class));
			observedTable = new JTable(new SimpleModel(Boolean.class));
		}

		@Override
		public Object getCellEditorValue() {
			try {
				weightTable.setValueAt(Double.parseDouble(weightField.getText()), 0, 0);
			} catch (NumberFormatException ex) {
			}

			double weight = 0.0;
			boolean cc = false;
			boolean kill = false;
			boolean observed = false;

			try {
				weight = (Double) weightTable.getValueAt(0, 0);
			} catch (ClassCastException | NullPointerException e) {
			}

			try {
				cc = (Boolean) ccTable.getValueAt(0, 0);
			} catch (ClassCastException | NullPointerException e) {
			}

			try {
				kill = (Boolean) killTable.getValueAt(0, 0);
			} catch (ClassCastException | NullPointerException e) {
			}

			try {
				observed = (Boolean) observedTable.getValueAt(0, 0);
			} catch (ClassCastException | NullPointerException e) {
			}

			return new Input(weight, cc, kill, observed);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			Input input = (Input) value;
			TableCellEditor weightEditor = weightTable.getDefaultEditor(Double.class);
			TableCellEditor ccEditor = ccTable.getDefaultEditor(Boolean.class);
			TableCellEditor killEditor = killTable.getDefaultEditor(Boolean.class);
			TableCellEditor observedEditor = observedTable.getDefaultEditor(Boolean.class);

			weightEditor.addCellEditorListener(this);
			ccEditor.addCellEditorListener(this);
			killEditor.addCellEditorListener(this);
			observedEditor.addCellEditorListener(this);

			weightTable.setValueAt(input.getWeight(), 0, 0);
			ccTable.setValueAt(input.isCrossContamination(), 0, 0);
			killTable.setValueAt(input.isKillContamination(), 0, 0);
			observedTable.setValueAt(input.isObserved(), 0, 0);
			weightField = (JTextField) weightEditor.getTableCellEditorComponent(weightTable, input.getWeight(),
					isSelected, 0, 0);

			JPanel panel = new JPanel();

			panel.setBackground(GRID_COLOR);
			panel.setLayout(new GridLayout(1, 4, 1, 0));
			panel.add(weightField);
			panel.add(ccEditor.getTableCellEditorComponent(ccTable, input.isCrossContamination(), isSelected, 0, 0));
			panel.add(killEditor.getTableCellEditorComponent(killTable, input.isKillContamination(), isSelected, 0, 0));
			panel.add(observedEditor.getTableCellEditorComponent(observedTable, input.isObserved(), isSelected, 0, 0));

			return panel;
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			TableCellEditor weightEditor = weightTable.getDefaultEditor(Double.class);
			TableCellEditor ccEditor = ccTable.getDefaultEditor(Boolean.class);
			TableCellEditor killEditor = killTable.getDefaultEditor(Boolean.class);
			TableCellEditor observedEditor = observedTable.getDefaultEditor(Boolean.class);

			if (e.getSource() == weightEditor) {
				weightTable.setValueAt(weightEditor.getCellEditorValue(), 0, 0);
			}

			if (e.getSource() == ccEditor) {
				ccTable.setValueAt(ccEditor.getCellEditorValue(), 0, 0);
			}

			if (e.getSource() == killEditor) {
				killTable.setValueAt(killEditor.getCellEditorValue(), 0, 0);
			}

			if (e.getSource() == observedEditor) {
				observedTable.setValueAt(observedEditor.getCellEditorValue(), 0, 0);
			}

			stopCellEditing();
		}

		@Override
		public void editingCanceled(ChangeEvent e) {
		}
	}

	private static class SimpleModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private Class<?> valueClass;
		private Object value;

		public SimpleModel(Class<?> valueClass) {
			this.valueClass = valueClass;
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return valueClass;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return value;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			value = aValue;
		}
	}
}
