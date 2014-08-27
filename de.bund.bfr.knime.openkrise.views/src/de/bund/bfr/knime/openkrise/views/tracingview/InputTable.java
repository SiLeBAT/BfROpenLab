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

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.openkrise.TracingConstants;

public class InputTable extends JTable {

	public static final String INPUT = "Input";

	private static final long serialVersionUID = 1L;

	public InputTable(List<? extends Element> elements) {
		List<Input> inputs = new ArrayList<>();

		for (Element e : elements) {
			double weight = 0.0;
			boolean crossContamination = false;
			boolean observed = false;

			if (e.getProperties().get(TracingConstants.WEIGHT_COLUMN) != null) {
				weight = (Double) e.getProperties().get(
						TracingConstants.WEIGHT_COLUMN);
			}

			if (e.getProperties().get(
					TracingConstants.CROSS_CONTAMINATION_COLUMN) != null) {
				crossContamination = (Boolean) e.getProperties().get(
						TracingConstants.CROSS_CONTAMINATION_COLUMN);
			}

			if (e.getProperties().get(TracingConstants.OBSERVED_COLUMN) != null) {
				observed = (Boolean) e.getProperties().get(
						TracingConstants.OBSERVED_COLUMN);
			}

			inputs.add(new Input(weight, crossContamination, observed));
		}

		setModel(new InputTableModel(inputs));
		setRowHeight(new JCheckBox().getPreferredSize().height);
		getColumn(INPUT).setCellRenderer(new InputRenderer());
		getColumn(INPUT).setCellEditor(new InputEditor());
	}

	public static class Input {

		private double weight;
		private boolean crossContamination;
		private boolean observed;

		public Input(double weight, boolean crossContamination, boolean observed) {
			this.weight = weight;
			this.crossContamination = crossContamination;
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

		public boolean isObserved() {
			return observed;
		}

		public void setObserved(boolean observed) {
			this.observed = observed;
		}

		@Override
		public String toString() {
			return "Input [weight=" + weight + ", crossContamination="
					+ crossContamination + ", observed=" + observed + "]";
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
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Input input = (Input) value;			
			JPanel panel = new JPanel();

			panel.setLayout(new GridLayout(1, 3));
			panel.add(getTableRendererComponent(input.getWeight(),
					Double.class, isSelected, hasFocus));
			panel.add(getTableRendererComponent(input.isCrossContamination(),
					Boolean.class, isSelected, hasFocus));
			panel.add(getTableRendererComponent(input.isObserved(),
					Boolean.class, isSelected, hasFocus));

			return panel;
		}

		private static Component getTableRendererComponent(Object value,
				Class<?> columnClass, boolean isSelected, boolean hasFocus) {
			JTable table = new JTable(new Object[][] { { value } },
					new Object[] { "" });

			return table.getDefaultRenderer(columnClass)
					.getTableCellRendererComponent(table, value, isSelected,
							hasFocus, 0, 0);
		}
	}

	private static class InputEditor extends AbstractCellEditor implements
			TableCellEditor, CellEditorListener, FocusListener {

		private static final long serialVersionUID = 1L;

		private TableCellEditor weightEditor;
		private TableCellEditor ccEditor;
		private TableCellEditor observedEditor;

		public InputEditor() {
			weightEditor = null;
			ccEditor = null;
			observedEditor = null;
		}

		@Override
		public Object getCellEditorValue() {
			double weight = 0.0;
			boolean cc = false;
			boolean observed = false;

			try {
				weight = Double.parseDouble(weightEditor.getCellEditorValue()
						.toString());
			} catch (NumberFormatException e) {
			} catch (NullPointerException e) {
			}

			try {
				cc = (Boolean) ccEditor.getCellEditorValue();
			} catch (ClassCastException e) {
			} catch (NullPointerException e) {
			}

			try {
				observed = (Boolean) observedEditor.getCellEditorValue();
			} catch (ClassCastException e) {
			} catch (NullPointerException e) {
			}

			return new Input(weight, cc, observed);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			Input input = (Input) value;
			JTable weightTable = new JTable(
					new Object[][] { { input.getWeight() } },
					new Object[] { "" });
			JTable ccTable = new JTable(
					new Object[][] { { input.isCrossContamination() } },
					new Object[] { "" });
			JTable observedTable = new JTable(
					new Object[][] { { input.isObserved() } },
					new Object[] { "" });

			weightEditor = weightTable.getDefaultEditor(Double.class);
			weightEditor.addCellEditorListener(this);
			ccEditor = ccTable.getDefaultEditor(Boolean.class);
			ccEditor.addCellEditorListener(this);
			observedEditor = observedTable.getDefaultEditor(Boolean.class);
			observedEditor.addCellEditorListener(this);

			JPanel panel = new JPanel();

			panel.setLayout(new GridLayout(1, 3));
			panel.add(weightEditor.getTableCellEditorComponent(weightTable,
					input.getWeight(), isSelected, 0, 0));
			panel.add(ccEditor.getTableCellEditorComponent(ccTable,
					input.isCrossContamination(), isSelected, 0, 0));
			panel.add(observedEditor.getTableCellEditorComponent(observedTable,
					input.isObserved(), isSelected, 0, 0));

			return panel;
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			stopCellEditing();
		}

		@Override
		public void editingCanceled(ChangeEvent e) {
			cancelCellEditing();
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void focusLost(FocusEvent e) {
			// weightField.selectAll();
		}
	}
}
