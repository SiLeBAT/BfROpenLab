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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
			JTextField weightField = new JTextField(String.valueOf(input
					.getWeight()));
			JCheckBox ccBox = new JCheckBox();
			JCheckBox observedBox = new JCheckBox();

			weightField.setBorder(null);
			weightField.setHorizontalAlignment(JTextField.RIGHT);
			ccBox.setSelected(input.isCrossContamination());
			ccBox.setHorizontalAlignment(SwingConstants.CENTER);
			observedBox.setSelected(input.isObserved());
			observedBox.setHorizontalAlignment(SwingConstants.CENTER);

			panel.setLayout(new GridLayout(1, 3, 5, 5));
			panel.add(weightField);
			panel.add(ccBox);
			panel.add(observedBox);

			return panel;
		}
	}

	private static class InputEditor extends AbstractCellEditor implements
			ActionListener, TableCellEditor, FocusListener {

		private static final long serialVersionUID = 1L;

		private JTextField weightField;
		private JCheckBox ccBox;
		private JCheckBox observedBox;

		public InputEditor() {
			weightField = new JTextField();
			weightField.setBorder(null);
			weightField.setHorizontalAlignment(JTextField.RIGHT);
			weightField.addFocusListener(this);
			ccBox = new JCheckBox();
			ccBox.addActionListener(this);
			ccBox.setHorizontalAlignment(SwingConstants.CENTER);
			observedBox = new JCheckBox();
			observedBox.setHorizontalAlignment(SwingConstants.CENTER);
			observedBox.addActionListener(this);
		}

		@Override
		public Object getCellEditorValue() {
			double weight = 0.0;

			try {
				weight = Double.parseDouble(weightField.getText());
			} catch (NumberFormatException e) {
			}

			return new Input(weight, ccBox.isSelected(),
					observedBox.isSelected());
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			Input input = (Input) value;

			weightField.setText(String.valueOf(input.getWeight()));
			ccBox.setSelected(input.isCrossContamination());
			observedBox.setSelected(input.isObserved());

			JPanel panel = new JPanel();

			panel.setLayout(new GridLayout(1, 3, 5, 5));
			panel.add(weightField);
			panel.add(ccBox);
			panel.add(observedBox);

			return panel;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			stopCellEditing();
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void focusLost(FocusEvent e) {
			weightField.selectAll();
		}
	}
}
