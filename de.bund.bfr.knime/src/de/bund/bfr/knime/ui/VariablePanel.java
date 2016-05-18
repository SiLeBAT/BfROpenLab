/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;

public class VariablePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int SLIDER_MAX = 100;

	private Map<String, List<Double>> variables;
	private Map<String, List<Boolean>> selectedValues;
	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private Map<String, DoubleTextField> valueFields;
	private Map<String, JButton> rangeButtons;
	private Map<String, JSlider> valueSliders;

	public VariablePanel(Map<String, List<Double>> variables, Map<String, Double> minValues,
			Map<String, Double> maxValues, boolean multiSelection, boolean allowSetRanges, boolean instantSliders) {
		this.variables = KnimeUtils.nullToEmpty(variables);
		this.minValues = new LinkedHashMap<>(KnimeUtils.nullToEmpty(minValues));
		this.maxValues = new LinkedHashMap<>(KnimeUtils.nullToEmpty(maxValues));
		selectedValues = new LinkedHashMap<>();
		valueFields = new LinkedHashMap<>();
		rangeButtons = new LinkedHashMap<>();
		valueSliders = new LinkedHashMap<>();
		setLayout(new GridBagLayout());

		int row = 0;

		for (Map.Entry<String, List<Double>> entry : this.variables.entrySet()) {
			String var = entry.getKey();

			selectedValues.put(var, new ArrayList<>(Collections.nCopies(entry.getValue().size(), true)));

			if (multiSelection) {
				JButton selectButton = new JButton(var + " Values");

				selectButton.addActionListener(e -> valueButtonPressed(var));
				add(selectButton, UI.westConstraints(0, row, 3, 1));
			} else {
				JLabel label = new JLabel(var + ":");
				DoubleTextField input = new DoubleTextField(false, 8);
				JSlider slider = new JSlider(0, SLIDER_MAX);
				JButton rangeButton = new JButton("Set Limits");
				Double value = !entry.getValue().isEmpty() ? entry.getValue().get(0) : null;
				Double min = this.minValues.get(var);
				Double max = this.maxValues.get(var);

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
				input.addTextListener(e -> textChanged(var));

				if (min != null && max != null) {
					slider.setValue(doubleToInt(value, min, max));
				} else {
					slider.setValue(0);
					slider.setEnabled(false);
				}

				slider.setPreferredSize(new Dimension(80, slider.getPreferredSize().height));
				slider.addChangeListener(e -> sliderChanged(var, instantSliders));
				slider.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseReleased(MouseEvent e) {
						if (slider.isEnabled()) {
							sliderChanged(var, true);
						}
					}
				});
				rangeButton.addActionListener(e -> rangeButtonPressed(var));

				valueFields.put(var, input);
				rangeButtons.put(var, rangeButton);
				valueSliders.put(var, slider);

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
		listenerList.add(ValueListener.class, listener);
	}

	public void removeValueListener(ValueListener listener) {
		listenerList.remove(ValueListener.class, listener);
	}

	public void setEnabled(String var, boolean enabled) {
		DoubleTextField field = valueFields.get(var);
		JButton rangeButton = rangeButtons.get(var);
		JSlider slider = valueSliders.get(var);

		if (field != null) {
			field.setEnabled(enabled);
		}

		if (rangeButton != null) {
			rangeButton.setEnabled(false);
		}

		if (slider != null && minValues.get(var) != null && maxValues.get(var) != null) {
			slider.setEnabled(enabled);
		}
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
		valueFields.forEach((var, field) -> {
			if (values.containsKey(var)) {
				field.setValue(values.get(var));
			}
		});
	}

	public Map<String, Double> getMinValues() {
		return minValues;
	}

	public Map<String, Double> getMaxValues() {
		return maxValues;
	}

	private void valueButtonPressed(String var) {
		SelectDialog dialog = new SelectDialog(var, variables.get(var), selectedValues.get(var));

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			selectedValues.put(var, dialog.getSelected());
			fireValuesChanged();
		}
	}

	private void rangeButtonPressed(String var) {
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

			List<ChangeListener> changeListeners = Arrays.asList(slider.getListeners(ChangeListener.class));

			changeListeners.forEach(l -> slider.removeChangeListener(l));
			slider.setValue(field.getValue() != null
					? doubleToInt(field.getValue(), minValues.get(var), maxValues.get(var)) : 0);
			changeListeners.forEach(l -> slider.addChangeListener(l));
		}
	}

	private void textChanged(String var) {
		DoubleTextField field = valueFields.get(var);
		JSlider slider = valueSliders.get(var);

		if (field.getValue() == null) {
			return;
		}

		if (slider != null && slider.isEnabled()) {
			List<ChangeListener> changeListeners = Arrays.asList(slider.getListeners(ChangeListener.class));

			changeListeners.forEach(l -> slider.removeChangeListener(l));
			slider.setValue(doubleToInt(field.getValue(), minValues.get(var), maxValues.get(var)));
			changeListeners.forEach(l -> slider.addChangeListener(l));
		}

		fireValuesChanged();
	}

	private void sliderChanged(String var, boolean applyChange) {
		JSlider slider = valueSliders.get(var);
		DoubleTextField field = valueFields.get(var);
		List<TextListener> textListeners = Arrays.asList(field.getListeners(TextListener.class));

		if (!applyChange) {
			textListeners.forEach(l -> field.removeTextListener(l));
		}

		field.setValue(intToDouble(slider.getValue(), minValues.get(var), maxValues.get(var)));

		if (!applyChange) {
			textListeners.forEach(l -> field.addTextListener(l));
		}
	}

	private int doubleToInt(double d, double min, double max) {
		int value = (int) ((d - min) / (max - min) * SLIDER_MAX);

		return Math.min(Math.max(value, 0), SLIDER_MAX);
	}

	private double intToDouble(int i, double min, double max) {
		return (double) i / (double) SLIDER_MAX * (max - min) + min;
	}

	private void fireValuesChanged() {
		Stream.of(getListeners(ValueListener.class)).forEach(l -> l.valuesChanged(this));
	}

	private class SelectDialog extends KnimeDialog {

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
			okButton.addActionListener(e -> okButtonPressed());
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(e -> dispose());

			JPanel centerPanel = new JPanel();

			centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			centerPanel.setLayout(new GridLayout(values.size(), 1, 5, 5));

			for (int i = 0; i < values.size(); i++) {
				JCheckBox box = new JCheckBox(NumberFormat.getInstance(Locale.US).format(values.get(i)));

				box.setSelected(initialSelected.get(i));
				box.addActionListener(e -> okButton.setEnabled(selectBoxes.stream().anyMatch(b -> b.isSelected())));
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

		private void okButtonPressed() {
			approved = true;
			selected = selectBoxes.stream().map(b -> b.isSelected()).collect(Collectors.toList());
			dispose();
		}
	}

	private class RangeDialog extends KnimeDialog {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private Double min;
		private Double max;

		private DoubleTextField minField;
		private DoubleTextField maxField;

		public RangeDialog(String title, Double initialMin, Double initialMax) {
			super(VariablePanel.this, title, DEFAULT_MODALITY_TYPE);

			approved = false;
			min = null;
			max = null;

			minField = new DoubleTextField(true, 10);
			minField.setValue(initialMin);
			maxField = new DoubleTextField(true, 10);
			maxField.setValue(initialMax);

			JButton okButton = new JButton("OK");
			JButton cancelButton = new JButton("Cancel");

			okButton.addActionListener(e -> okButtonPressed());
			cancelButton.addActionListener(e -> dispose());

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

		private void okButtonPressed() {
			if (minField.getValue() != null && maxField.getValue() != null
					&& minField.getValue() >= maxField.getValue()) {
				Dialogs.showErrorMessage(this, "Min must be smaller than Max", "Error");
			}

			approved = true;
			min = minField.getValue();
			max = maxField.getValue();
			dispose();
		}
	}

	public static interface ValueListener extends EventListener {

		void valuesChanged(VariablePanel source);
	}
}
