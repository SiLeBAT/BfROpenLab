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
package de.bund.bfr.knime.nls.chart;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.TextListener;

public class ChartConfigPanel extends JPanel implements ActionListener,
		TextListener, ChangeListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private static final double DEFAULT_MINX = 0.0;
	private static final double DEFAULT_MAXX = 1.0;
	private static final double DEFAULT_MINY = 0.0;
	private static final double DEFAULT_MAXY = 1.0;

	private static final int SLIDER_MAX = 100;

	private List<ConfigListener> configListeners;

	private JCheckBox drawLinesBox;
	private JCheckBox showLegendBox;
	private JCheckBox exportAsSvgBox;
	private JCheckBox showConfidenceBox;

	private JCheckBox manualRangeBox;
	private DoubleTextField minXField;
	private DoubleTextField minYField;
	private DoubleTextField maxXField;
	private DoubleTextField maxYField;

	private JComboBox<String> xBox;
	private JComboBox<String> yBox;
	private JComboBox<String> xTransBox;
	private JComboBox<String> yTransBox;
	private String lastParamX;
	private Map<String, Double> parametersX;
	private Map<String, Double> minParamValuesX;
	private Map<String, Double> maxParamValuesX;

	private JPanel parameterValuesPanel;
	private List<String> parameters;
	private List<JLabel> parameterLabels;
	private List<DoubleTextField> parameterFields;
	private List<JSlider> parameterSliders;

	public ChartConfigPanel(boolean showParamFields) {
		configListeners = new ArrayList<ConfigListener>();
		lastParamX = null;

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new GridBagLayout());

		drawLinesBox = new JCheckBox("Draw Lines");
		drawLinesBox.setSelected(false);
		drawLinesBox.addActionListener(this);
		showLegendBox = new JCheckBox("Show Legend");
		showLegendBox.setSelected(true);
		showLegendBox.addActionListener(this);
		exportAsSvgBox = new JCheckBox("Export as SVG");
		exportAsSvgBox.setSelected(false);
		showConfidenceBox = new JCheckBox("Show Confidence Interval");
		showConfidenceBox.setSelected(false);
		showConfidenceBox.addActionListener(this);

		JPanel displayOptionsPanel = new JPanel();

		displayOptionsPanel.setLayout(new BoxLayout(displayOptionsPanel,
				BoxLayout.Y_AXIS));
		displayOptionsPanel.setLayout(new GridBagLayout());
		displayOptionsPanel.add(showLegendBox, createConstraints(0, 0, 1, 1));
		displayOptionsPanel.add(showConfidenceBox,
				createConstraints(1, 0, 1, 1));
		displayOptionsPanel.add(drawLinesBox, createConstraints(0, 1, 1, 1));
		displayOptionsPanel.add(exportAsSvgBox, createConstraints(1, 1, 1, 1));

		JPanel outerDisplayOptionsPanel = new JPanel();

		outerDisplayOptionsPanel.setBorder(BorderFactory
				.createTitledBorder("Display Options"));
		outerDisplayOptionsPanel.setLayout(new BorderLayout());
		outerDisplayOptionsPanel.add(displayOptionsPanel, BorderLayout.WEST);
		mainPanel.add(outerDisplayOptionsPanel, createConstraints(0));

		JPanel rangePanel = new JPanel();

		manualRangeBox = new JCheckBox("Set Manual Range");
		manualRangeBox.setSelected(false);
		manualRangeBox.addActionListener(this);
		minXField = new DoubleTextField(false, 8);
		minXField.setValue(DEFAULT_MINX);
		minXField.setEnabled(false);
		minXField.addTextListener(this);
		minYField = new DoubleTextField(false, 8);
		minYField.setValue(DEFAULT_MINY);
		minYField.setEnabled(false);
		minYField.addTextListener(this);
		maxXField = new DoubleTextField(false, 8);
		maxXField.setValue(DEFAULT_MAXX);
		maxXField.setEnabled(false);
		maxXField.addTextListener(this);
		maxYField = new DoubleTextField(false, 8);
		maxYField.setValue(DEFAULT_MAXY);
		maxYField.setEnabled(false);
		maxYField.addTextListener(this);

		rangePanel.setLayout(new GridBagLayout());
		rangePanel.add(manualRangeBox, createConstraints(0, 0, 4, 1));
		rangePanel.add(new JLabel("Min X:"), createConstraints(0, 1, 1, 1));
		rangePanel.add(minXField, createConstraints(1, 1, 1, 1));
		rangePanel.add(new JLabel("Max X:"), createConstraints(2, 1, 1, 1));
		rangePanel.add(maxXField, createConstraints(3, 1, 1, 1));
		rangePanel.add(new JLabel("Min Y:"), createConstraints(0, 2, 1, 1));
		rangePanel.add(minYField, createConstraints(1, 2, 1, 1));
		rangePanel.add(new JLabel("Max Y:"), createConstraints(2, 2, 1, 1));
		rangePanel.add(maxYField, createConstraints(3, 2, 1, 1));

		JPanel outerRangePanel = new JPanel();

		outerRangePanel.setBorder(BorderFactory.createTitledBorder("Range"));
		outerRangePanel.setLayout(new BorderLayout());
		outerRangePanel.add(rangePanel, BorderLayout.WEST);
		mainPanel.add(outerRangePanel, createConstraints(1));

		xBox = new JComboBox<String>();
		xBox.addActionListener(this);
		yBox = new JComboBox<String>();
		xTransBox = new JComboBox<String>(ChartUtilities.TRANSFORMS);
		xTransBox.addActionListener(this);
		yTransBox = new JComboBox<String>(ChartUtilities.TRANSFORMS);
		yTransBox.addActionListener(this);

		JPanel parametersPanel = new JPanel();

		parametersPanel.setLayout(new GridBagLayout());
		parametersPanel.add(new JLabel("X:"), createConstraints(0, 0, 1, 1));
		parametersPanel.add(xBox, createConstraints(1, 0, 1, 1));
		parametersPanel.add(new JLabel("Y:"), createConstraints(2, 0, 1, 1));
		parametersPanel.add(yBox, createConstraints(3, 0, 1, 1));
		parametersPanel.add(new JLabel("X Transform:"),
				createConstraints(0, 1, 1, 1));
		parametersPanel.add(xTransBox, createConstraints(1, 1, 1, 1));
		parametersPanel.add(new JLabel("Y Transform:"),
				createConstraints(2, 1, 1, 1));
		parametersPanel.add(yTransBox, createConstraints(3, 1, 1, 1));

		JPanel outerParametersPanel = new JPanel();

		outerParametersPanel.setBorder(BorderFactory
				.createTitledBorder("Variables on Display"));
		outerParametersPanel.setLayout(new BorderLayout());
		outerParametersPanel.add(parametersPanel, BorderLayout.WEST);
		mainPanel.add(outerParametersPanel, createConstraints(2));

		parameterValuesPanel = new JPanel();
		parameterValuesPanel.setLayout(new GridBagLayout());
		parameters = new ArrayList<String>();
		parameterFields = new ArrayList<DoubleTextField>();
		parameterLabels = new ArrayList<JLabel>();
		parameterSliders = new ArrayList<JSlider>();

		JPanel outerParameterValuesPanel = new JPanel();

		outerParameterValuesPanel.setBorder(BorderFactory
				.createTitledBorder("Other Variables"));
		outerParameterValuesPanel.setLayout(new BorderLayout());
		outerParameterValuesPanel.add(parameterValuesPanel, BorderLayout.WEST);

		if (showParamFields) {
			mainPanel.add(outerParameterValuesPanel, createConstraints(3));
		}

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.NORTH);
	}

	public void addConfigListener(ConfigListener listener) {
		configListeners.add(listener);
	}

	public void removeConfigListener(ConfigListener listener) {
		configListeners.remove(listener);
	}

	public boolean isManualRange() {
		return manualRangeBox.isSelected();
	}

	public void setManualRange(boolean manualRange) {
		manualRangeBox.setSelected(manualRange);

		if (manualRangeBox.isSelected()) {
			minXField.setEnabled(true);
			minYField.setEnabled(true);
			maxXField.setEnabled(true);
			maxYField.setEnabled(true);
		} else {
			minXField.setEnabled(false);
			minYField.setEnabled(false);
			maxXField.setEnabled(false);
			maxYField.setEnabled(false);
		}
	}

	public double getMinX() {
		if (minXField.isValueValid()) {
			return minXField.getValue();
		} else {
			return DEFAULT_MINX;
		}
	}

	public void setMinX(double minX) {
		minXField.setValue(minX);
	}

	public double getMinY() {
		if (minYField.isValueValid()) {
			return minYField.getValue();
		} else {
			return DEFAULT_MINY;
		}
	}

	public void setMinY(double minY) {
		minYField.setValue(minY);
	}

	public double getMaxX() {
		if (maxXField.isValueValid()) {
			return maxXField.getValue();
		} else {
			return DEFAULT_MAXX;
		}
	}

	public void setMaxX(double maxX) {
		maxXField.setValue(maxX);
	}

	public double getMaxY() {
		if (maxYField.isValueValid()) {
			return maxYField.getValue();
		} else {
			return DEFAULT_MAXY;
		}
	}

	public void setMaxY(double maxY) {
		maxYField.setValue(maxY);
	}

	public boolean isDrawLines() {
		return drawLinesBox.isSelected();
	}

	public void setDrawLines(boolean drawLines) {
		drawLinesBox.setSelected(drawLines);
	}

	public boolean isShowLegend() {
		return showLegendBox.isSelected();
	}

	public void setShowLegend(boolean showLegend) {
		showLegendBox.setSelected(showLegend);
	}

	public boolean isExportAsSvg() {
		return exportAsSvgBox.isSelected();
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		exportAsSvgBox.setSelected(exportAsSvg);
	}

	public boolean isShowConfidence() {
		return showConfidenceBox.isSelected();
	}

	public void setShowConfidence(boolean showConfidenceInterval) {
		showConfidenceBox.setSelected(showConfidenceInterval);
	}

	public String getParamX() {
		return (String) xBox.getSelectedItem();
	}

	public void setParamX(String paramX) {
		if (paramX != null) {
			xBox.setSelectedItem(paramX);
		} else if (xBox.getItemCount() != 0) {
			xBox.setSelectedIndex(0);
		}
	}

	public String getParamY() {
		return (String) yBox.getSelectedItem();
	}

	public String getTransformX() {
		return (String) xTransBox.getSelectedItem();
	}

	public void setTransformX(String transformX) {
		xTransBox.setSelectedItem(transformX);
	}

	public String getTransformY() {
		return (String) yTransBox.getSelectedItem();
	}

	public void setTransformY(String transformY) {
		yTransBox.setSelectedItem(transformY);
	}

	public void setParameters(String paramY, Map<String, Double> parametersX,
			Map<String, Double> minParamValuesX,
			Map<String, Double> maxParamValuesX) {
		if (parametersX == null) {
			parametersX = new LinkedHashMap<String, Double>();
		}

		if (minParamValuesX == null) {
			minParamValuesX = new LinkedHashMap<String, Double>();
		}

		if (maxParamValuesX == null) {
			maxParamValuesX = new LinkedHashMap<String, Double>();
		}

		if (!parametersX.equals(this.parametersX)
				|| !minParamValuesX.equals(this.minParamValuesX)
				|| !maxParamValuesX.equals(this.maxParamValuesX)) {
			this.parametersX = parametersX;
			this.minParamValuesX = minParamValuesX;
			this.maxParamValuesX = maxParamValuesX;

			xBox.removeActionListener(this);
			xBox.removeAllItems();

			for (String param : parametersX.keySet()) {
				xBox.addItem(param);
			}

			if (!parametersX.isEmpty()) {
				if (parametersX.containsKey(lastParamX)) {
					xBox.setSelectedItem(lastParamX);
				} else {
					xBox.setSelectedIndex(0);
				}

				lastParamX = (String) xBox.getSelectedItem();
			} else {
				lastParamX = null;
			}

			xBox.addActionListener(this);
			updateParametersPanel();
		}

		if (paramY == null) {
			yBox.removeAllItems();
		} else if (!paramY.equals(yBox.getSelectedItem())) {
			yBox.removeAllItems();
			yBox.addItem(paramY);
			yBox.setSelectedIndex(0);
		}
	}

	public Map<String, Double> getParamsX() {
		Map<String, Double> valueLists = new LinkedHashMap<String, Double>();

		for (int i = 0; i < parameterFields.size(); i++) {
			DoubleTextField field = parameterFields.get(i);

			if (field.getValue() != null) {
				valueLists.put(parameters.get(i), field.getValue());
			} else {
				valueLists.put(parameters.get(i), 0.0);
			}
		}

		valueLists.put((String) xBox.getSelectedItem(), 0.0);

		return valueLists;
	}

	public void setParamXValues(Map<String, Double> paramXValues) {
		for (int i = 0; i < parameterFields.size(); i++) {
			DoubleTextField field = parameterFields.get(i);

			field.setValue(paramXValues.get(parameters.get(i)));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == manualRangeBox) {
			if (manualRangeBox.isSelected()) {
				minXField.setEnabled(true);
				minYField.setEnabled(true);
				maxXField.setEnabled(true);
				maxYField.setEnabled(true);
			} else {
				minXField.setEnabled(false);
				minYField.setEnabled(false);
				maxXField.setEnabled(false);
				maxYField.setEnabled(false);
			}

			fireConfigChanged();
		} else if (e.getSource() == xBox) {
			lastParamX = (String) xBox.getSelectedItem();
			updateParametersPanel();
			fireConfigChanged();
		} else {
			fireConfigChanged();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		int i = parameterSliders.indexOf(e.getSource());
		JSlider slider = parameterSliders.get(i);
		DoubleTextField field = parameterFields.get(i);

		field.removeTextListener(this);
		field.setValue(intToDouble(slider.getValue(),
				minParamValuesX.get(parameters.get(i)),
				maxParamValuesX.get(parameters.get(i))));
		field.addTextListener(this);
	}

	@Override
	public void textChanged(Object source) {
		if (parameterFields.contains(source)) {
			int i = parameterFields.indexOf(source);
			DoubleTextField field = parameterFields.get(i);
			JSlider slider = parameterSliders.get(i);

			if (field.getValue() != null && slider != null) {
				int value = doubleToInt(field.getValue(),
						minParamValuesX.get(parameters.get(i)),
						maxParamValuesX.get(parameters.get(i)));

				slider.removeChangeListener(this);
				slider.setValue(Math.min(Math.max(value, 0), SLIDER_MAX));
				slider.addChangeListener(this);
			}
		}

		fireConfigChanged();
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
		int i = parameterSliders.indexOf(e.getSource());
		JSlider slider = parameterSliders.get(i);
		DoubleTextField field = parameterFields.get(i);

		field.setValue(intToDouble(slider.getValue(),
				minParamValuesX.get(parameters.get(i)),
				maxParamValuesX.get(parameters.get(i))));
	}

	private void updateParametersPanel() {
		parameterValuesPanel.removeAll();
		parameters.clear();
		parameterFields.clear();
		parameterLabels.clear();
		parameterSliders.clear();

		int row = 0;

		if (parametersX == null) {
			return;
		}

		for (String param : parametersX.keySet()) {
			if (param.equals(xBox.getSelectedItem())) {
				continue;
			}

			JLabel label = new JLabel(param + ":");
			DoubleTextField input = new DoubleTextField(false, 8);
			JSlider slider = null;
			Double value = parametersX.get(param);
			Double min = minParamValuesX.get(param);
			Double max = maxParamValuesX.get(param);

			if (min != null && max != null) {
				if (value == null) {
					value = min;
				}

				slider = new JSlider(0, SLIDER_MAX, doubleToInt(
						Math.min(Math.max(value, min), max), min, max));
				slider.setPreferredSize(new Dimension(50, slider
						.getPreferredSize().height));
				slider.addChangeListener(this);
				slider.addMouseListener(this);
			}

			if (value == null) {
				value = 0.0;
			}

			input.setValue(value);
			input.addTextListener(this);

			parameters.add(param);
			parameterFields.add(input);
			parameterLabels.add(label);
			parameterSliders.add(slider);
			parameterValuesPanel.add(label, createConstraints(0, row, 1, 1));
			parameterValuesPanel.add(input, createConstraints(2, row, 1, 1));

			if (slider != null) {
				parameterValuesPanel.add(slider,
						createConstraints(1, row, 1, 1));
			}

			row++;
		}

		Container container = getParent();

		while (container != null) {
			if (container instanceof JPanel) {
				((JPanel) container).revalidate();
				break;
			}

			container = container.getParent();
		}
	}

	private void fireConfigChanged() {
		for (ConfigListener listener : configListeners) {
			listener.configChanged();
		}
	}

	private static int doubleToInt(double d, double min, double max) {
		return (int) ((d - min) / (max - min) * SLIDER_MAX);
	}

	private static double intToDouble(int i, double min, double max) {
		return (double) i / (double) SLIDER_MAX * (max - min) + min;
	}

	private static GridBagConstraints createConstraints(int x, int y, int w,
			int h) {
		return new GridBagConstraints(x, y, w, h, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2,
						2, 2, 2), 0, 0);
	}

	private static GridBagConstraints createConstraints(int y) {
		return new GridBagConstraints(0, y, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0);
	}

	public static interface ConfigListener {

		public void configChanged();
	}

}
