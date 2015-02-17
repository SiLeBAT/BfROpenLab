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
package de.bund.bfr.knime.nls.chart;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.TextListener;
import de.bund.bfr.knime.ui.VariablePanel;
import de.bund.bfr.math.Transform;

public class ChartConfigPanel extends JPanel implements ItemListener,
		TextListener, MouseListener, VariablePanel.ValueListener {

	private static final long serialVersionUID = 1L;

	private static final double DEFAULT_MINX = 0.0;
	private static final double DEFAULT_MAXX = 1.0;
	private static final double DEFAULT_MINY = 0.0;
	private static final double DEFAULT_MAXY = 1.0;

	private List<ConfigListener> configListeners;

	private JCheckBox drawLinesBox;
	private JCheckBox showLegendBox;
	private JCheckBox exportAsSvgBox;
	private JCheckBox showConfidenceBox;
	private JSlider resolutionSlider;

	private JCheckBox minToZeroBox;
	private JCheckBox manualRangeBox;
	private DoubleTextField minXField;
	private DoubleTextField minYField;
	private DoubleTextField maxXField;
	private DoubleTextField maxYField;

	private JComboBox<String> xBox;
	private JComboBox<String> yBox;
	private JComboBox<Transform> xTransBox;
	private JComboBox<Transform> yTransBox;
	private String lastVarX;
	private List<String> variablesX;

	private List<String> parameters;
	private JPanel outerParameterPanel;
	private VariablePanel parameterPanel;

	public ChartConfigPanel(boolean allowConfidence, boolean allowExport,
			boolean allowParameters) {
		configListeners = new ArrayList<>();
		lastVarX = null;

		drawLinesBox = new JCheckBox("Draw Lines");
		drawLinesBox.setSelected(false);
		drawLinesBox.addItemListener(this);
		showLegendBox = new JCheckBox("Show Legend");
		showLegendBox.setSelected(true);
		showLegendBox.addItemListener(this);
		exportAsSvgBox = new JCheckBox("Export as SVG");
		exportAsSvgBox.setSelected(false);
		showConfidenceBox = new JCheckBox("Show Confidence");
		showConfidenceBox.setSelected(false);
		showConfidenceBox.addItemListener(this);
		resolutionSlider = new JSlider(0, 1000, 1000);
		resolutionSlider.setMinorTickSpacing(100);
		resolutionSlider.setMajorTickSpacing(200);
		resolutionSlider.setPaintTicks(true);
		resolutionSlider.setPaintLabels(true);
		resolutionSlider.addMouseListener(this);

		JPanel resolutionPanel = new JPanel();

		resolutionPanel.setLayout(new BoxLayout(resolutionPanel,
				BoxLayout.X_AXIS));
		resolutionPanel.add(new JLabel("Resolution:"));
		resolutionPanel.add(Box.createHorizontalStrut(5));
		resolutionPanel.add(resolutionSlider);
		resolutionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel displayOptionsPanel = new JPanel();
		int row = 0;

		displayOptionsPanel.setLayout(new BoxLayout(displayOptionsPanel,
				BoxLayout.Y_AXIS));
		displayOptionsPanel.setLayout(new GridBagLayout());
		displayOptionsPanel.add(showLegendBox, UI.westConstraints(0, row));
		displayOptionsPanel.add(drawLinesBox, UI.westConstraints(1, row));
		row++;

		if (allowConfidence && allowExport) {
			displayOptionsPanel.add(showConfidenceBox,
					UI.westConstraints(0, row));
			displayOptionsPanel.add(exportAsSvgBox, UI.westConstraints(1, row));
			row++;
		} else if (allowConfidence) {
			displayOptionsPanel.add(showConfidenceBox,
					UI.westConstraints(0, row));
			row++;
		} else if (allowExport) {
			displayOptionsPanel.add(exportAsSvgBox, UI.westConstraints(0, row));
			row++;
		}

		displayOptionsPanel.add(resolutionPanel,
				UI.westConstraints(0, row, 2, 1));

		JPanel outerDisplayOptionsPanel = new JPanel();

		outerDisplayOptionsPanel.setBorder(BorderFactory
				.createTitledBorder("Display Options"));
		outerDisplayOptionsPanel.setLayout(new BorderLayout());
		outerDisplayOptionsPanel.add(displayOptionsPanel, BorderLayout.WEST);

		JPanel rangePanel = new JPanel();

		minToZeroBox = new JCheckBox("Set Minimum to Zero");
		minToZeroBox.setSelected(false);
		minToZeroBox.addItemListener(this);
		manualRangeBox = new JCheckBox("Set Manual Range");
		manualRangeBox.setSelected(false);
		manualRangeBox.addItemListener(this);
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
		rangePanel.add(minToZeroBox, UI.westConstraints(0, 0, 4, 1));
		rangePanel.add(manualRangeBox, UI.westConstraints(0, 1, 4, 1));
		rangePanel.add(new JLabel("Min X:"), UI.westConstraints(0, 2, 1, 1));
		rangePanel.add(minXField, UI.westConstraints(1, 2, 1, 1));
		rangePanel.add(new JLabel("Max X:"), UI.westConstraints(2, 2, 1, 1));
		rangePanel.add(maxXField, UI.westConstraints(3, 2, 1, 1));
		rangePanel.add(new JLabel("Min Y:"), UI.westConstraints(0, 3, 1, 1));
		rangePanel.add(minYField, UI.westConstraints(1, 3, 1, 1));
		rangePanel.add(new JLabel("Max Y:"), UI.westConstraints(2, 3, 1, 1));
		rangePanel.add(maxYField, UI.westConstraints(3, 3, 1, 1));

		JPanel outerRangePanel = new JPanel();

		outerRangePanel.setBorder(BorderFactory.createTitledBorder("Range"));
		outerRangePanel.setLayout(new BorderLayout());
		outerRangePanel.add(rangePanel, BorderLayout.WEST);

		xBox = new JComboBox<>();
		xBox.addItemListener(this);
		yBox = new JComboBox<>();
		xTransBox = new JComboBox<>(Transform.values());
		xTransBox.addItemListener(this);
		yTransBox = new JComboBox<>(Transform.values());
		yTransBox.addItemListener(this);

		JPanel variablesPanel = new JPanel();

		variablesPanel.setLayout(new GridBagLayout());
		variablesPanel.add(new JLabel("X:"), UI.westConstraints(0, 0, 1, 1));
		variablesPanel.add(xBox, UI.westConstraints(1, 0, 1, 1));
		variablesPanel.add(new JLabel("Y:"), UI.westConstraints(2, 0, 1, 1));
		variablesPanel.add(yBox, UI.westConstraints(3, 0, 1, 1));
		variablesPanel.add(new JLabel("X Transform:"),
				UI.westConstraints(0, 1, 1, 1));
		variablesPanel.add(xTransBox, UI.westConstraints(1, 1, 1, 1));
		variablesPanel.add(new JLabel("Y Transform:"),
				UI.westConstraints(2, 1, 1, 1));
		variablesPanel.add(yTransBox, UI.westConstraints(3, 1, 1, 1));

		JPanel outerVariablesPanel = new JPanel();

		outerVariablesPanel.setBorder(BorderFactory
				.createTitledBorder("Variables"));
		outerVariablesPanel.setLayout(new BorderLayout());
		outerVariablesPanel.add(variablesPanel, BorderLayout.WEST);

		parameters = new ArrayList<>();
		parameterPanel = new VariablePanel(
				new LinkedHashMap<String, List<Double>>(),
				new LinkedHashMap<String, Double>(),
				new LinkedHashMap<String, Double>(), false, true);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(outerDisplayOptionsPanel);
		mainPanel.add(outerRangePanel);
		mainPanel.add(outerVariablesPanel);

		if (allowParameters) {
			outerParameterPanel = new JPanel();
			outerParameterPanel.setBorder(BorderFactory
					.createTitledBorder("Parameters"));
			outerParameterPanel.setLayout(new BorderLayout());
			outerParameterPanel.add(parameterPanel, BorderLayout.WEST);
			mainPanel.add(outerParameterPanel);
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

	public boolean isMinToZero() {
		return minToZeroBox.isSelected();
	}

	public void setMinToZero(boolean minToZero) {
		minToZeroBox.setSelected(minToZero);
	}

	public boolean isManualRange() {
		return manualRangeBox.isSelected();
	}

	public void setManualRange(boolean manualRange) {
		manualRangeBox.setSelected(manualRange);
	}

	public double getMinX() {
		if (minXField.isValueValid()) {
			return minXField.getValue();
		}

		return DEFAULT_MINX;
	}

	public void setMinX(double minX) {
		minXField.setValue(minX);
	}

	public double getMinY() {
		if (minYField.isValueValid()) {
			return minYField.getValue();
		}

		return DEFAULT_MINY;
	}

	public void setMinY(double minY) {
		minYField.setValue(minY);
	}

	public double getMaxX() {
		if (maxXField.isValueValid()) {
			return maxXField.getValue();
		}

		return DEFAULT_MAXX;
	}

	public void setMaxX(double maxX) {
		maxXField.setValue(maxX);
	}

	public double getMaxY() {
		if (maxYField.isValueValid()) {
			return maxYField.getValue();
		}

		return DEFAULT_MAXY;
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

	public int getResolution() {
		return resolutionSlider.getValue();
	}

	public void setResolution(int resolution) {
		resolutionSlider.setValue(resolution);
	}

	public String getVarX() {
		return (String) xBox.getSelectedItem();
	}

	public void setVarX(String varX) {
		if (varX != null) {
			xBox.setSelectedItem(varX);
		} else if (xBox.getItemCount() != 0) {
			xBox.setSelectedIndex(0);
		}
	}

	public String getVarY() {
		return (String) yBox.getSelectedItem();
	}

	public Transform getTransformX() {
		return (Transform) xTransBox.getSelectedItem();
	}

	public void setTransformX(Transform transformX) {
		xTransBox.setSelectedItem(transformX);
	}

	public Transform getTransformY() {
		return (Transform) yTransBox.getSelectedItem();
	}

	public void setTransformY(Transform transformY) {
		yTransBox.setSelectedItem(transformY);
	}

	public void init(String varY, List<String> variablesX,
			List<String> parameters) {
		if (variablesX == null) {
			variablesX = new ArrayList<>();
		}

		if (parameters == null) {
			parameters = new ArrayList<>();
		}

		if (varY == null) {
			yBox.removeAllItems();
		} else if (!varY.equals(yBox.getSelectedItem())) {
			yBox.removeAllItems();
			yBox.addItem(varY);
			yBox.setSelectedIndex(0);
		}

		if (!variablesX.equals(this.variablesX)) {
			this.variablesX = variablesX;

			xBox.removeItemListener(this);
			xBox.removeAllItems();

			for (String var : variablesX) {
				xBox.addItem(var);
			}

			if (!variablesX.isEmpty()) {
				if (variablesX.contains(lastVarX)) {
					xBox.setSelectedItem(lastVarX);
				} else {
					xBox.setSelectedIndex(0);
				}

				lastVarX = (String) xBox.getSelectedItem();
			} else {
				lastVarX = null;
			}

			xBox.addItemListener(this);
		}

		if (!parameters.equals(this.parameters)) {
			this.parameters = parameters;

			Map<String, List<Double>> paramMap = new LinkedHashMap<>();

			for (String param : parameters) {
				paramMap.put(param, new ArrayList<Double>());
			}

			parameterPanel = new VariablePanel(paramMap,
					new LinkedHashMap<String, Double>(),
					new LinkedHashMap<String, Double>(), false, true);
			parameterPanel.addValueListener(this);

			outerParameterPanel.removeAll();
			outerParameterPanel.add(parameterPanel, BorderLayout.WEST);

			Container container = getParent();

			while (container != null) {
				if (container instanceof JPanel) {
					((JPanel) container).revalidate();
					break;
				}

				container = container.getParent();
			}
		}
	}

	public Map<String, Double> getParamValues() {
		return parameterPanel.getValues();
	}

	public void setParamValues(Map<String, Double> paramValues) {
		parameterPanel.setValues(paramValues);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == manualRangeBox) {
			if (manualRangeBox.isSelected()) {
				minToZeroBox.setEnabled(false);
				minXField.setEnabled(true);
				minYField.setEnabled(true);
				maxXField.setEnabled(true);
				maxYField.setEnabled(true);
			} else {
				minToZeroBox.setEnabled(true);
				minXField.setEnabled(false);
				minYField.setEnabled(false);
				maxXField.setEnabled(false);
				maxYField.setEnabled(false);
			}
		} else if (e.getSource() == xBox) {
			lastVarX = (String) xBox.getSelectedItem();
		}

		if (e.getSource() instanceof JCheckBox
				|| (e.getSource() instanceof JComboBox && e.getStateChange() == ItemEvent.SELECTED)) {
			fireConfigChanged();
		}
	}

	@Override
	public void textChanged(Object source) {
		fireConfigChanged();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == resolutionSlider) {
			fireConfigChanged();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void valuesChanged() {
		fireConfigChanged();
	}

	private void fireConfigChanged() {
		for (ConfigListener listener : configListeners) {
			listener.configChanged();
		}
	}

	public static interface ConfigListener {

		public void configChanged();
	}
}
