/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.chart.NamedShape;
import de.bund.bfr.knime.pmmlite.core.DataTable;
import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.KnimeDialog;

class SelectTableUtilities {

	private SelectTableUtilities() {
	}

	public static TableCellRenderer createViewRenderer() {
		return new ViewRenderer();
	}

	public static TableCellRenderer createColorRenderer() {
		return new ColorRenderer();
	}

	public static TableCellRenderer createColorListRenderer() {
		return new ColorListRenderer();
	}

	public static TableCellRenderer createShapeListRenderer() {
		return new ShapeListRenderer();
	}

	public static TableCellRenderer createCheckBoxRenderer() {
		return new CheckBoxRenderer();
	}

	public static TableCellEditor createColorEditor() {
		return new ColorEditor();
	}

	public static TableCellEditor createColorListEditor() {
		return new ColorListEditor();
	}

	public static TableCellEditor createShapeListEditor() {
		return new ShapeListEditor();
	}

	public static TableCellEditor createTimeSeriesEditor() {
		return new TimeSeriesEditor();
	}

	public static TableCellEditor createFormulaEditor() {
		return new FormulaEditor();
	}

	public static TableCellEditor createParameterEditor() {
		return new ParameterEditor();
	}

	public static TableCellEditor createCovarianceEditor() {
		return new CovarianceEditor();
	}

	public static TableCellEditor createCheckBoxEditor() {
		return new CheckBoxEditor();
	}

	public static <T extends TableModel> TableRowSorter<T> createSelectTableRowSorter(T model,
			Map<String, String> filters) {
		return new SelectTableRowSorter<>(model, filters);
	}

	private static class ViewRenderer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			return new JButton("View");
		}
	}

	private static class ColorRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ColorRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setBackground((Color) color);

			return this;
		}
	}

	private static class ColorListRenderer extends JComponent implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		private List<Color> colorList;

		public ColorListRenderer() {
			colorList = new ArrayList<>();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
				int row, int column) {
			colorList = (List<Color>) color;

			return this;
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (colorList.isEmpty()) {
				super.paintComponent(g);
			} else {
				double w = (double) getWidth() / (double) colorList.size();
				Color currentColor = g.getColor();

				for (int i = 0; i < colorList.size(); i++) {
					g.setColor(colorList.get(i));
					g.fillRect((int) (i * w), 0, (int) Math.max(w, 1), getHeight());
				}

				g.setColor(currentColor);
			}
		}
	}

	private static class ShapeListRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ShapeListRenderer() {
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
				int row, int column) {
			return this;
		}
	}

	private static class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

		private static final long serialVersionUID = -8337460338388283099L;

		public CheckBoxRenderer() {
			super();
			setHorizontalAlignment(SwingConstants.CENTER);
			setBorderPainted(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			int statusColumn = -1;

			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumnName(i).equals(ChartSelectionPanel.STATUS)) {
					statusColumn = i;
					break;
				}
			}

			if (statusColumn != -1) {
				String statusValue = (String) table.getValueAt(row, statusColumn);

				if (statusValue.equals(Plotable.Status.OK.toString())) {
					setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
					setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
				} else if (statusValue.equals(Plotable.Status.FAILED.toString())) {
					setForeground(Color.RED);
					setBackground(Color.RED);
				} else if (statusValue.equals(Plotable.Status.OUT_OF_LIMITS.toString())) {
					setForeground(Color.YELLOW);
					setBackground(Color.YELLOW);
				} else if (statusValue.equals(Plotable.Status.NO_COVARIANCE.toString())) {
					setForeground(Color.YELLOW);
					setBackground(Color.YELLOW);
				} else if (statusValue.equals(Plotable.Status.OUT_OF_LIMITS_AND_NO_COVARIANCE.toString())) {
					setForeground(Color.YELLOW);
					setBackground(Color.YELLOW);
				}
			} else {
				setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
				setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			}

			setSelected((value != null && ((Boolean) value).booleanValue()));
			setBorder(hasFocus ? UI.TABLE_CELL_FOCUS_BORDER : UI.TABLE_CELL_BORDER);

			return this;
		}
	}

	private static class ColorEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton colorButton;

		public ColorEditor() {
			colorButton = new JButton();
			colorButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Color newColor = Dialogs.showColorChooser(colorButton, "Choose Color", colorButton.getBackground());

					if (newColor != null) {
						colorButton.setBackground(newColor);
						stopCellEditing();
					} else {
						cancelCellEditing();
					}
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			colorButton.setBackground((Color) value);

			return colorButton;
		}

		@Override
		public Object getCellEditorValue() {
			return colorButton.getBackground();
		}

	}

	private static class ColorListEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton button;
		private List<Color> colorList;

		public ColorListEditor() {
			button = new JButton();
			colorList = new ArrayList<>();
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ColorListDialog dialog = new ColorListDialog(button, colorList);

					dialog.setVisible(true);

					if (dialog.isApproved()) {
						colorList = dialog.getColorList();
						stopCellEditing();
					} else {
						cancelCellEditing();
					}
				}
			});
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			colorList = (List<Color>) value;

			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return colorList;
		}

		private static class ColorListDialog extends KnimeDialog implements ActionListener {

			private static final long serialVersionUID = 1L;

			private boolean approved;
			private List<Color> colorList;

			private List<JButton> colorButtons;

			private JButton okButton;
			private JButton cancelButton;

			public ColorListDialog(Component owner, List<Color> initialColors) {
				super(owner, "Color Palette", DEFAULT_MODALITY_TYPE);

				approved = false;
				colorList = null;

				colorButtons = new ArrayList<>();
				okButton = new JButton("OK");
				okButton.addActionListener(this);
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);

				JPanel centerPanel = new JPanel();

				centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				centerPanel.setLayout(new GridLayout(initialColors.size(), 1, 5, 5));

				for (Color color : initialColors) {
					JButton button = new JButton();

					button.setBackground(color);
					button.setPreferredSize(new Dimension(button.getPreferredSize().width, 20));
					button.addActionListener(this);
					colorButtons.add(button);
					centerPanel.add(button);
				}

				JPanel scrollPanel = new JPanel();

				scrollPanel.setLayout(new BorderLayout());
				scrollPanel.add(centerPanel, BorderLayout.NORTH);

				setLayout(new BorderLayout());
				add(new JScrollPane(scrollPanel), BorderLayout.CENTER);
				add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

				pack();
				UI.adjustDialog(this);
				setLocationRelativeTo(owner);
				getRootPane().setDefaultButton(okButton);
			}

			public boolean isApproved() {
				return approved;
			}

			public List<Color> getColorList() {
				return colorList;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == okButton) {
					approved = true;
					colorList = new ArrayList<>();

					for (JButton button : colorButtons) {
						colorList.add(button.getBackground());
					}

					dispose();
				} else if (e.getSource() == cancelButton) {
					dispose();
				} else {
					JButton button = (JButton) e.getSource();
					Color newColor = Dialogs.showColorChooser(button, "Choose Color", button.getBackground());

					if (newColor != null) {
						button.setBackground(newColor);
					}
				}
			}
		}
	}

	private static class ShapeListEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton button;
		private List<NamedShape> shapeList;

		public ShapeListEditor() {
			button = new JButton();
			shapeList = new ArrayList<>();
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ShapeListDialog dialog = new ShapeListDialog(button, shapeList);

					dialog.setVisible(true);

					if (dialog.isApproved()) {
						shapeList = dialog.getShapeList();
						stopCellEditing();
					} else {
						cancelCellEditing();
					}
				}
			});
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			shapeList = (List<NamedShape>) value;

			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return shapeList;
		}

		private static class ShapeListDialog extends KnimeDialog implements ActionListener {

			private static final long serialVersionUID = 1L;

			private boolean approved;
			private List<NamedShape> shapeList;

			private List<JComboBox<NamedShape>> shapeBoxes;

			private JButton okButton;
			private JButton cancelButton;

			public ShapeListDialog(Component owner, List<NamedShape> initialShapes) {
				super(owner, "Shape Palette", DEFAULT_MODALITY_TYPE);

				approved = false;
				shapeList = null;

				shapeBoxes = new ArrayList<>();
				okButton = new JButton("OK");
				okButton.addActionListener(this);
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);

				JPanel centerPanel = new JPanel();

				centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				centerPanel.setLayout(new GridLayout(initialShapes.size(), 1, 5, 5));

				for (NamedShape shape : initialShapes) {
					JComboBox<NamedShape> box = new JComboBox<>(NamedShape.values());

					box.setSelectedItem(shape);
					shapeBoxes.add(box);
					centerPanel.add(box);
				}

				JPanel scrollPanel = new JPanel();

				scrollPanel.setLayout(new BorderLayout());
				scrollPanel.add(centerPanel, BorderLayout.NORTH);

				setLayout(new BorderLayout());
				add(new JScrollPane(scrollPanel), BorderLayout.CENTER);
				add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

				pack();
				UI.adjustDialog(this);
				setLocationRelativeTo(owner);
				getRootPane().setDefaultButton(okButton);
			}

			public boolean isApproved() {
				return approved;
			}

			public List<NamedShape> getShapeList() {
				return shapeList;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == okButton) {
					approved = true;
					shapeList = new ArrayList<>();

					for (JComboBox<NamedShape> box : shapeBoxes) {
						shapeList.add((NamedShape) box.getSelectedItem());
					}

					dispose();
				} else if (e.getSource() == cancelButton) {
					dispose();
				}
			}
		}
	}

	private static class TimeSeriesEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		private static final long serialVersionUID = 1L;

		private JButton button;
		private TimeSeries timeSeries;

		public TimeSeriesEditor() {
			button = new JButton("View");
			button.addActionListener(this);
			timeSeries = null;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			timeSeries = (TimeSeries) value;

			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return timeSeries;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			TimeSeriesDialog dialog = new TimeSeriesDialog(button, timeSeries);

			dialog.setVisible(true);
			cancelCellEditing();
		}

		private static class TimeSeriesDialog extends KnimeDialog implements ActionListener {

			private static final long serialVersionUID = 1L;

			public TimeSeriesDialog(JComponent owner, TimeSeries timeSeries) {
				super(SwingUtilities.getWindowAncestor(owner), "Data Points", DEFAULT_MODALITY_TYPE);

				JButton okButton = new JButton("OK");

				okButton.addActionListener(this);

				setLayout(new BorderLayout());
				add(new DataTable(timeSeries.getPoints()), BorderLayout.CENTER);
				add(UI.createEastPanel(UI.createBorderPanel(okButton)), BorderLayout.SOUTH);

				pack();
				UI.adjustDialog(this);
				setLocationRelativeTo(owner);
				getRootPane().setDefaultButton(okButton);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		}
	}

	private static class FormulaEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		private static final long serialVersionUID = 1L;

		private JButton button;
		private String formula;

		public FormulaEditor() {
			button = new JButton("View");
			button.addActionListener(this);
			formula = "";
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			formula = (String) value;

			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return formula;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			FormulaDialog dialog = new FormulaDialog(button, formula);

			dialog.setVisible(true);
			cancelCellEditing();
		}

		private static class FormulaDialog extends KnimeDialog implements ActionListener {

			private static final long serialVersionUID = 1L;

			private JButton okButton;

			public FormulaDialog(Component owner, String formula) {
				super(owner, "Formula", DEFAULT_MODALITY_TYPE);

				okButton = new JButton("OK");
				okButton.addActionListener(this);

				JTextField field = new JTextField();

				field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				field.setText(formula);
				field.setEditable(false);
				field.setPreferredSize(
						new Dimension(field.getPreferredSize().width + 10, field.getPreferredSize().height));

				setLayout(new BorderLayout());
				add(new JScrollPane(UI.createNorthPanel(field)), BorderLayout.CENTER);
				add(UI.createEastPanel(UI.createBorderPanel(okButton)), BorderLayout.SOUTH);

				pack();
				UI.adjustDialog(this);
				setLocationRelativeTo(owner);
				getRootPane().setDefaultButton(okButton);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		}
	}

	private static class ParameterEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		private static final long serialVersionUID = 1L;

		private JButton button;
		private Map<String, ParameterValue> parameters;

		public ParameterEditor() {
			button = new JButton("View");
			button.addActionListener(this);
			parameters = new LinkedHashMap<>();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			parameters = (Map<String, ParameterValue>) value;

			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return parameters;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ParameterDialog dialog = new ParameterDialog(button, parameters);

			dialog.setVisible(true);
			cancelCellEditing();
		}

		private static class ParameterDialog extends KnimeDialog implements ActionListener {

			private static final long serialVersionUID = 1L;

			public ParameterDialog(Component owner, Map<String, ParameterValue> parameters) {
				super(owner, "Parameters", DEFAULT_MODALITY_TYPE);
				String[] params = parameters.keySet().toArray(new String[0]);
				ParameterValue[] values = parameters.values().toArray(new ParameterValue[0]);

				JTable table = new JTable(new ParameterTableModel(params, values));
				JButton okButton = new JButton("OK");

				okButton.addActionListener(this);

				setLayout(new BorderLayout());
				add(UI.createTablePanel(table), BorderLayout.CENTER);
				add(UI.createEastPanel(UI.createBorderPanel(okButton)), BorderLayout.SOUTH);

				pack();
				UI.adjustDialog(this);
				setLocationRelativeTo(owner);
				getRootPane().setDefaultButton(okButton);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

			private static class ParameterTableModel extends AbstractTableModel {

				private static final long serialVersionUID = 1L;

				private String[] params;
				private ParameterValue[] values;

				public ParameterTableModel(String[] params, ParameterValue[] values) {
					this.params = params;
					this.values = values;
				}

				@Override
				public int getRowCount() {
					return params.length;
				}

				@Override
				public int getColumnCount() {
					return 5;
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					switch (columnIndex) {
					case 0:
						return params[rowIndex];
					case 1:
						return values[rowIndex].getValue();
					case 2:
						return values[rowIndex].getError();
					case 3:
						return values[rowIndex].getT();
					case 4:
						return values[rowIndex].getP();
					default:
						throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
					}
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					switch (columnIndex) {
					case 0:
						return String.class;
					case 1:
						return Double.class;
					case 2:
						return Double.class;
					case 3:
						return Double.class;
					case 4:
						return Double.class;
					default:
						throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
					}
				}

				@Override
				public String getColumnName(int column) {
					switch (column) {
					case 0:
						return "Parameter";
					case 1:
						return "Value";
					case 2:
						return ChartSelectionPanel.STD_ERROR;
					case 3:
						return ChartSelectionPanel.T_VALUE;
					case 4:
						return ChartSelectionPanel.P_VALUE;
					default:
						throw new IndexOutOfBoundsException("Column index out of bounds: " + column);
					}
				}
			}
		}
	}

	private static class CovarianceEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		private static final long serialVersionUID = 1L;

		private JButton button;
		private Map<String, Map<String, Double>> covMatrix;

		public CovarianceEditor() {
			button = new JButton("View");
			button.addActionListener(this);
			covMatrix = new LinkedHashMap<>();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			covMatrix = (Map<String, Map<String, Double>>) value;

			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return covMatrix;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			CovarianceDialog dialog = new CovarianceDialog(button, covMatrix);

			dialog.setVisible(true);
			cancelCellEditing();
		}

		private static class CovarianceDialog extends KnimeDialog implements ActionListener {

			private static final long serialVersionUID = 1L;

			public CovarianceDialog(Component owner, Map<String, Map<String, Double>> covMatrix) {
				super(owner, "Covariance Matrix", DEFAULT_MODALITY_TYPE);
				int n = covMatrix.keySet().size();
				String[] params = covMatrix.keySet().toArray(new String[0]);
				Double[][] values = new Double[n][n];

				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						values[i][j] = covMatrix.get(params[i]).get(params[j]);
					}
				}

				JTable table = new JTable(new CovarianceTableModel(params, values));
				JButton okButton = new JButton("OK");

				okButton.addActionListener(this);

				setLayout(new BorderLayout());
				add(UI.createTablePanel(table), BorderLayout.CENTER);
				add(UI.createEastPanel(UI.createBorderPanel(okButton)), BorderLayout.SOUTH);

				pack();
				UI.adjustDialog(this);
				setLocationRelativeTo(owner);
				getRootPane().setDefaultButton(okButton);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

			private static class CovarianceTableModel extends AbstractTableModel {

				private static final long serialVersionUID = 1L;

				private String[] params;
				private Double[][] values;

				public CovarianceTableModel(String[] params, Double[][] values) {
					this.params = params;
					this.values = values;
				}

				@Override
				public int getRowCount() {
					return params.length;
				}

				@Override
				public int getColumnCount() {
					return params.length + 1;
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					if (columnIndex == 0) {
						return params[rowIndex];
					} else {
						return values[rowIndex][columnIndex - 1];
					}
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 0) {
						return String.class;
					} else {
						return Double.class;
					}
				}

				@Override
				public String getColumnName(int column) {
					if (column == 0) {
						return "Parameter";
					} else {
						return params[column - 1];
					}
				}

			}
		}
	}

	private static class CheckBoxEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 1L;

		public CheckBoxEditor() {
			super(new JCheckBox());
			((JCheckBox) getComponent()).setHorizontalAlignment(SwingConstants.CENTER);
		}
	}

	private static class SelectTableRowSorter<T extends TableModel> extends TableRowSorter<T> {

		public SelectTableRowSorter(final T model, final Map<String, String> filters) {
			super(model);

			if (filters != null && !filters.isEmpty()) {
				setRowFilter(new RowFilter<T, Object>() {

					@Override
					public boolean include(RowFilter.Entry<? extends T, ? extends Object> entry) {
						for (int i = 0; i < model.getColumnCount(); i++) {
							String filter = filters.get(model.getColumnName(i));

							if (filter != null && !entry.getStringValue(i).equals(filter)) {
								return false;
							}
						}

						return true;
					}
				});
			}
		}

		@Override
		public void toggleSortOrder(int column) {
			List<? extends SortKey> sortKeys = getSortKeys();

			if (sortKeys.size() > 0) {
				if (sortKeys.get(0).getColumn() == column && sortKeys.get(0).getSortOrder() == SortOrder.DESCENDING) {
					setSortKeys(null);
					return;
				}
			}

			super.toggleSortOrder(column);
		}
	}
}
