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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.ui.DoubleCellRenderer;

public class InputTable extends JTable {

	public static final String INPUT = "Input";

	private static final long serialVersionUID = 1L;

	private static final Color GRID_COLOR = new JTable().getGridColor();

	private InputTableHeader header;

	public InputTable(InputTableHeader header, List<? extends Element> elements) {
		this.header = header;

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

		DefaultTableModel model = new DefaultTableModel(inputs.size(), 0);

		model.addColumn(INPUT, new Vector<>(inputs));

		setModel(model);
		setRowHeight(new JCheckBox().getPreferredSize().height);
		getColumn(INPUT).setCellRenderer(new InputRenderer());
		getColumn(INPUT).setCellEditor(new InputEditor());
	}

	private class InputRenderer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Input input = (Input) value;
			JPanel panel = new JPanel();

			panel.setBackground(GRID_COLOR);
			panel.setLayout(new RowLayout(4, 1));
			panel.add(getTableRendererComponent(input.getWeight(), Double.class, isSelected, hasFocus,
					header.getComponent(0).getPreferredSize().width - 1));
			panel.add(getTableRendererComponent(input.isCrossContamination(), Boolean.class, isSelected, hasFocus,
					header.getComponent(1).getPreferredSize().width - 1));
			panel.add(getTableRendererComponent(input.isKillContamination(), Boolean.class, isSelected, hasFocus,
					header.getComponent(2).getPreferredSize().width - 1));
			panel.add(getTableRendererComponent(input.isObserved(), Boolean.class, isSelected, hasFocus,
					header.getComponent(3).getPreferredSize().width - 1));

			return panel;
		}

		private Component getTableRendererComponent(Object value, Class<?> columnClass, boolean isSelected,
				boolean hasFocus, int width) {
			JTable table = new JTable(new Object[][] { { value } }, new Object[] { "" });

			table.setDefaultRenderer(Double.class, new DoubleCellRenderer());

			Component c = table.getDefaultRenderer(columnClass).getTableCellRendererComponent(table, value, isSelected,
					hasFocus, 0, 0);

			c.setPreferredSize(new Dimension(width, c.getPreferredSize().height));

			return c;
		}
	}

	private class InputEditor extends AbstractCellEditor implements TableCellEditor, CellEditorListener {

		private static final long serialVersionUID = 1L;

		private JTable weightTable;
		private JTable ccTable;
		private JTable killTable;
		private JTable observedTable;

		private JTextField weightField;

		public InputEditor() {
			weightTable = new JTable(new DefaultTableModel(1, 1));
			ccTable = new JTable(new DefaultTableModel(1, 1));
			killTable = new JTable(new DefaultTableModel(1, 1));
			observedTable = new JTable(new DefaultTableModel(1, 1));
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
			Component ccField = ccEditor.getTableCellEditorComponent(ccTable, input.isCrossContamination(), isSelected,
					0, 0);
			Component killField = killEditor.getTableCellEditorComponent(killTable, input.isKillContamination(),
					isSelected, 0, 0);
			Component observedField = observedEditor.getTableCellEditorComponent(observedTable, input.isObserved(),
					isSelected, 0, 0);

			weightField.setPreferredSize(new Dimension(header.getComponent(0).getPreferredSize().width - 1,
					weightField.getPreferredSize().height));
			ccField.setPreferredSize(new Dimension(header.getComponent(1).getPreferredSize().width - 1,
					ccField.getPreferredSize().height));
			killField.setPreferredSize(new Dimension(header.getComponent(2).getPreferredSize().width - 1,
					killField.getPreferredSize().height));
			observedField.setPreferredSize(new Dimension(header.getComponent(3).getPreferredSize().width - 1,
					observedField.getPreferredSize().height));

			JPanel panel = new JPanel();

			panel.setBackground(GRID_COLOR);
			panel.setLayout(new RowLayout(4, 1));
			panel.add(weightField);
			panel.add(ccField);
			panel.add(killField);
			panel.add(observedField);

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

	private static class RowLayout implements LayoutManager, Serializable {

		private static final long serialVersionUID = 1L;

		private int columns;
		private int gap;

		public RowLayout(int columns, int gap) {
			this.columns = columns;
			this.gap = gap;
		}

		@Override
		public void layoutContainer(Container c) {
			Insets insets = c.getInsets();
			int height = c.getHeight() - (insets.top + insets.bottom);
			int[] widths = new int[columns];

			for (int column = 0; column < columns; column++) {
				widths[column] = c.getComponent(column).getPreferredSize().width;
			}

			int x = insets.left;

			for (int column = 0; column < columns; column++) {
				c.getComponent(column).setBounds(x, insets.top, widths[column], height);
				x += (widths[column] + gap);
			}
		}

		@Override
		public Dimension minimumLayoutSize(Container c) {
			Insets insets = c.getInsets();
			int height = 0;
			int width = 0;

			for (int column = 0; column < columns; column++) {
				height = Math.max(height, c.getComponent(column).getPreferredSize().height);
				width += c.getComponent(column).getPreferredSize().width;
			}

			height += insets.top + insets.bottom;
			width += (gap * (columns - 1)) + insets.right + insets.left;

			return new Dimension(width, height);

		}

		@Override
		public Dimension preferredLayoutSize(Container c) {
			return minimumLayoutSize(c);
		}

		@Override
		public void addLayoutComponent(String s, Component c) {
		}

		@Override
		public void removeLayoutComponent(Component c) {
		}
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
}
