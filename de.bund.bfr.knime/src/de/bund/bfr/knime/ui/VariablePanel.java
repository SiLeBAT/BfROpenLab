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
package de.bund.bfr.knime.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;

public class VariablePanel extends JPanel implements ActionListener, TextListener, ChangeListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private static final int SLIDER_MAX = 100;

	private Map<String, List<Double>> variables;
	private Map<String, List<Boolean>> selectedValues;
	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private BiMap<String, JButton> valueButtons;
	private BiMap<String, JLabel> valueLabels;
	private BiMap<String, DoubleTextField> valueFields;
	private BiMap<String, JSlider> valueSliders;
	private BiMap<String, JButton> rangeButtons;

	private List<ValueListener> valueListeners;

	private boolean instantSliders;

	public VariablePanel(Map<String, List<Double>> variables, Map<String, Double> minValues,
			Map<String, Double> maxValues, boolean multiSelection, boolean allowSetRanges, boolean instantSliders) {
		this.variables = KnimeUtils.nullToEmpty(variables);
		this.minValues = new LinkedHashMap<>(KnimeUtils.nullToEmpty(minValues));
		this.maxValues = new LinkedHashMap<>(KnimeUtils.nullToEmpty(maxValues));
		this.instantSliders = instantSliders;
		selectedValues = new LinkedHashMap<>();
		valueListeners = new ArrayList<>();
		valueFields = HashBiMap.create();
		valueButtons = HashBiMap.create();
		valueLabels = HashBiMap.create();
		valueSliders = HashBiMap.create();
		rangeButtons = HashBiMap.create();
		setLayout(new GridBagLayout());

		int row = 0;

		for (Map.Entry<String, List<Double>> entry : this.variables.entrySet()) {
			String var = entry.getKey();

			selectedValues.put(var, new ArrayList<>(Collections.nCopies(entry.getValue().size(), true)));

			if (multiSelection) {
				JButton selectButton = new JButton(var + " Values");

				selectButton.addActionListener(this);
				valueButtons.put(var, selectButton);
				add(selectButton, UI.westConstraints(0, row, 3, 1));
			} else {
				JLabel label = new JLabel(var + ":");
				DoubleTextField input = new DoubleTextField(false, 8);
				JSlider slider = new JSlider(0, SLIDER_MAX);
				JButton rangeButton = new JButton("Set Limits");
				Double value = null;
				Double min = this.minValues.get(var);
				Double max = this.maxValues.get(var);

				if (!entry.getValue().isEmpty()) {
					value = entry.getValue().get(0);
				}

				if (value == null) {
					if (min != null) {
						value = min;
					} else if (max != null) {
						value = max;
					} else {
						value = 0.0;
					}
				}

				input.setValue(value);
				input.addTextListener(this);

				if (min != null && max != null) {
					slider.setValue(doubleToInt(value, min, max));
				} else {
					slider.setValue(0);
					slider.setEnabled(false);
				}

				slider.setPreferredSize(new Dimension(80, slider.getPreferredSize().height));
				slider.addChangeListener(this);
				slider.addMouseListener(this);
				rangeButton.addActionListener(this);

				valueLabels.put(var, label);
				valueFields.put(var, input);
				valueSliders.put(var, slider);
				rangeButtons.put(var, rangeButton);

				add(label, UI.westConstraints(0, row));
				add(input, UI.westConstraints(2, row));

				if ((min != null && max != null) || allowSetRanges) {
					add(slider, UI.westConstraints(1, row));
				}

				if (allowSetRanges) {
					add(rangeButton, UI.westConstraints(3, row));
				}
			}

			row++;
		}
	}

	public void addValueListener(ValueListener listener) {
		valueListeners.add(listener);
	}

	public void removeValueListener(ValueListener listener) {
		valueListeners.remove(listener);
	}

	public Map<String, List<Boolean>> getSelectedValues() {
		return selectedValues;
	}

	public void setSelectedValues(Map<String, List<Boolean>> selectedValues) {
		this.selectedValues = selectedValues;
	}

	public Map<String, Double> getValues() {
		Map<String, Double> variableValues = new LinkedHashMap<>();

		for (String var : variables.keySet()) {
			DoubleTextField field = valueFields.get(var);

			variableValues.put(var, field.getValue());
		}

		return variableValues;
	}

	public void setValues(Map<String, Double> values) {
		for (Map.Entry<String, DoubleTextField> entry : valueFields.entrySet()) {
			if (values.containsKey(entry.getKey())) {
				entry.getValue().setValue(values.get(entry.getKey()));
			}
		}
	}

	public Map<String, Double> getMinValues() {
		return minValues;
	}

	public Map<String, Double> getMaxValues() {
		return maxValues;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (valueButtons.values().contains(e.getSource())) {
			String var = valueButtons.inverse().get(e.getSource());
			SelectDialog dialog = new SelectDialog(var, variables.get(var), selectedValues.get(var));

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				selectedValues.put(var, dialog.getSelected());
				valueListeners.forEach(l -> l.valuesChanged(this));
			}
		} else if (rangeButtons.values().contains(e.getSource())) {
			String var = rangeButtons.inverse().get(e.getSource());
			RangeDialog dialog = new RangeDialog(var, minValues.get(var), maxValues.get(var));

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				minValues.put(var, dialog.getMin());
				maxValues.put(var, dialog.getMax());

				JSlider slider = valueSliders.get(var);
				DoubleTextField field = valueFields.get(var);

				if (minValues.get(var) == null || maxValues.get(var) == null) {
					slider.setEnabled(false);
					return;
				}

				slider.setEnabled(true);
				slider.removeChangeListener(this);
				slider.setValue(field.getValue() != null
						? doubleToInt(field.getValue(), minValues.get(var), maxValues.get(var)) : 0);
				slider.addChangeListener(this);
			}
		}
	}

	@Override
	public void textChanged(TextInput source) {
		if (valueFields.values().contains(source)) {
			DoubleTextField field = (DoubleTextField) source;
			String var = valueFields.inverse().get(field);
			JSlider slider = valueSliders.get(var);

			if (field.getValue() == null) {
				return;
			}

			if (slider != null && slider.isEnabled()) {
				slider.removeChangeListener(this);
				slider.setValue(doubleToInt(field.getValue(), minValues.get(var), maxValues.get(var)));
				slider.addChangeListener(this);
			}

			valueListeners.forEach(l -> l.valuesChanged(this));
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (valueSliders.values().contains(e.getSource())) {
			sliderChanged((JSlider) e.getSource(), instantSliders);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (valueSliders.values().contains(e.getSource()) && ((JSlider) e.getSource()).isEnabled()) {
			sliderChanged((JSlider) e.getSource(), true);
		}
	}

	private void sliderChanged(JSlider slider, boolean applyChange) {
		String var = valueSliders.inverse().get(slider);
		DoubleTextField field = valueFields.get(var);

		if (!applyChange) {
			field.removeTextListener(this);
		}

		field.setValue(intToDouble(slider.getValue(), minValues.get(var), maxValues.get(var)));

		if (!applyChange) {
			field.addTextListener(this);
		}
	}

	private int doubleToInt(double d, double min, double max) {
		int value = (int) ((d - min) / (max - min) * SLIDER_MAX);

		return Math.min(Math.max(value, 0), SLIDER_MAX);
	}

	private double intToDouble(int i, double min, double max) {
		return (double) i / (double) SLIDER_MAX * (max - min) + min;
	}

	private class SelectDialog extends KnimeDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private List<Boolean> selected;

		private List<JCheckBox> selectBoxes;

		private JButton okButton;
		private JButton cancelButton;

		public SelectDialog(String title, List<Double> values, List<Boolean> initialSelected) {
			super(VariablePanel.this, title, DEFAULT_MODALITY_TYPE);

			approved = false;
			selected = null;

			selectBoxes = new ArrayList<>();
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			JPanel centerPanel = new JPanel();

			centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			centerPanel.setLayout(new GridLayout(values.size(), 1, 5, 5));

			for (int i = 0; i < values.size(); i++) {
				JCheckBox box = new JCheckBox(NumberFormat.getInstance(Locale.US).format(values.get(i)));

				box.setSelected(initialSelected.get(i));
				box.addActionListener(this);
				selectBoxes.add(box);
				centerPanel.add(box);
			}

			setLayout(new BorderLayout());
			add(centerPanel, BorderLayout.CENTER);
			add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

			pack();
			UI.adjustDialog(this);
			setLocationRelativeTo(VariablePanel.this);
			getRootPane().setDefaultButton(okButton);
			setResizable(false);
		}

		public boolean isApproved() {
			return approved;
		}

		public List<Boolean> getSelected() {
			return selected;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				approved = true;
				selected = new ArrayList<>();

				for (JCheckBox box : selectBoxes) {
					selected.add(box.isSelected());
				}

				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			} else if (selectBoxes.contains(e.getSource())) {
				boolean noSelection = true;

				for (JCheckBox box : selectBoxes) {
					if (box.isSelected()) {
						noSelection = false;
						break;
					}
				}

				okButton.setEnabled(!noSelection);
			}
		}
	}

	private class RangeDialog extends KnimeDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private Double min;
		private Double max;

		private DoubleTextField minField;
		private DoubleTextField maxField;

		private JButton okButton;
		private JButton cancelButton;

		public RangeDialog(String title, Double initialMin, Double initialMax) {
			super(VariablePanel.this, title, DEFAULT_MODALITY_TYPE);

			approved = false;
			min = null;
			max = null;

			minField = new DoubleTextField(true, 10);
			minField.setValue(initialMin);
			maxField = new DoubleTextField(true, 10);
			maxField.setValue(initialMax);
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			setLayout(new BorderLayout());
			add(UI.createHorizontalPanel(UI.createOptionsPanel(null,
					Arrays.asList(new JLabel("Min:"), new JLabel("Max:")), Arrays.asList(minField, maxField))),
					BorderLayout.CENTER);
			add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

			pack();
			UI.adjustDialog(this);
			setLocationRelativeTo(VariablePanel.this);
			getRootPane().setDefaultButton(okButton);
			setResizable(false);
		}

		public boolean isApproved() {
			return approved;
		}

		public Double getMin() {
			return min;
		}

		public Double getMax() {
			return max;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				if (minField.getValue() != null && maxField.getValue() != null
						&& minField.getValue() >= maxField.getValue()) {
					Dialogs.showErrorMessage(okButton, "Min must be smaller than Max", "Error");
				}

				approved = true;
				min = minField.getValue();
				max = maxField.getValue();
				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			}
		}
	}

	public static interface ValueListener {

		void valuesChanged(VariablePanel source);
	}
}
