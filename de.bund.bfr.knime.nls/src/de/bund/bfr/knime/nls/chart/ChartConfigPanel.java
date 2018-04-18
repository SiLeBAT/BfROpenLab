/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.VariablePanel;
import de.bund.bfr.math.InterpolationFactory;
import de.bund.bfr.math.Transform;

public class ChartConfigPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final double DEFAULT_MINX = 0.0;
	private static final double DEFAULT_MAXX = 1.0;
	private static final double DEFAULT_MINY = 0.0;
	private static final double DEFAULT_MAXY = 1.0;

	private JCheckBox drawLinesBox;
	private JCheckBox showLegendBox;
	private JCheckBox exportAsSvgBox;
	private JCheckBox showConfidenceBox;
	private JSlider resolutionSlider;
	private JComboBox<InterpolationFactory.Type> interpolatorBox;

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

	private JPanel outerParameterPanel;
	private VariablePanel parameterPanel;

	private JPanel outerVariablePanel;
	private VariablePanel variablePanel;

	public ChartConfigPanel(boolean allowConfidence, boolean allowExport, boolean allowParameters,
			boolean allowVariables, boolean isDiff) {
		drawLinesBox = new JCheckBox("Draw Lines");
		drawLinesBox.setSelected(false);
		drawLinesBox.addItemListener(e -> fireConfigChanged());
		showLegendBox = new JCheckBox("Show Legend");
		showLegendBox.setSelected(true);
		showLegendBox.addItemListener(e -> fireConfigChanged());
		exportAsSvgBox = new JCheckBox("Export as SVG");
		exportAsSvgBox.setSelected(false);
		exportAsSvgBox.addItemListener(e -> fireConfigChanged());
		showConfidenceBox = new JCheckBox("Show Confidence");
		showConfidenceBox.setSelected(false);
		showConfidenceBox.addItemListener(e -> fireConfigChanged());
		resolutionSlider = new JSlider(0, 1000, 1000);
		resolutionSlider.setMinorTickSpacing(100);
		resolutionSlider.setMajorTickSpacing(200);
		resolutionSlider.setPaintTicks(true);
		resolutionSlider.setPaintLabels(true);
		resolutionSlider.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				fireConfigChanged();
			}
		});
		interpolatorBox = new JComboBox<>(InterpolationFactory.Type.values());
		interpolatorBox.setSelectedIndex(0);
		interpolatorBox.addItemListener(UI.newItemSelectListener(e -> fireConfigChanged()));

		JPanel resolutionPanel = new JPanel();

		resolutionPanel.setLayout(new BoxLayout(resolutionPanel, BoxLayout.X_AXIS));
		resolutionPanel.add(new JLabel("Resolution:"));
		resolutionPanel.add(Box.createHorizontalStrut(5));
		resolutionPanel.add(resolutionSlider);
		resolutionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel interpolatorPanel = new JPanel();

		interpolatorPanel.setLayout(new BoxLayout(interpolatorPanel, BoxLayout.X_AXIS));
		interpolatorPanel.add(new JLabel("Interpolation Function:"));
		interpolatorPanel.add(Box.createHorizontalStrut(5));
		interpolatorPanel.add(interpolatorBox);
		interpolatorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel displayOptionsPanel = new JPanel();
		int row = 0;

		displayOptionsPanel.setLayout(new BoxLayout(displayOptionsPanel, BoxLayout.Y_AXIS));
		displayOptionsPanel.setLayout(new GridBagLayout());
		displayOptionsPanel.add(showLegendBox, UI.westConstraints(0, row));
		displayOptionsPanel.add(drawLinesBox, UI.westConstraints(1, row));
		row++;

		if (allowConfidence && allowExport) {
			displayOptionsPanel.add(showConfidenceBox, UI.westConstraints(0, row));
			displayOptionsPanel.add(exportAsSvgBox, UI.westConstraints(1, row));
			row++;
		} else if (allowConfidence) {
			displayOptionsPanel.add(showConfidenceBox, UI.westConstraints(0, row));
			row++;
		} else if (allowExport) {
			displayOptionsPanel.add(exportAsSvgBox, UI.westConstraints(0, row));
			row++;
		}

		displayOptionsPanel.add(resolutionPanel, UI.westConstraints(0, row, 2, 1));
		row++;

		if (isDiff) {
			displayOptionsPanel.add(interpolatorPanel, UI.westConstraints(0, row, 2, 1));
		}

		JPanel outerDisplayOptionsPanel = new JPanel();

		outerDisplayOptionsPanel.setBorder(BorderFactory.createTitledBorder("Display Options"));
		outerDisplayOptionsPanel.setLayout(new BorderLayout());
		outerDisplayOptionsPanel.add(displayOptionsPanel, BorderLayout.WEST);

		JPanel rangePanel = new JPanel();

		minToZeroBox = new JCheckBox("Set Minimum to Zero");
		minToZeroBox.setSelected(false);
		minToZeroBox.addItemListener(e -> fireConfigChanged());
		manualRangeBox = new JCheckBox("Set Manual Range");
		manualRangeBox.setSelected(false);
		manualRangeBox.addItemListener(e -> {
			minToZeroBox.setEnabled(!manualRangeBox.isSelected());
			minXField.setEnabled(manualRangeBox.isSelected());
			minYField.setEnabled(manualRangeBox.isSelected());
			maxXField.setEnabled(manualRangeBox.isSelected());
			maxYField.setEnabled(manualRangeBox.isSelected());
			fireConfigChanged();
		});
		minXField = new DoubleTextField(false, 8);
		minXField.setValue(DEFAULT_MINX);
		minXField.setEnabled(false);
		minXField.addTextListener(e -> fireConfigChanged());
		minYField = new DoubleTextField(false, 8);
		minYField.setValue(DEFAULT_MINY);
		minYField.setEnabled(false);
		minYField.addTextListener(e -> fireConfigChanged());
		maxXField = new DoubleTextField(false, 8);
		maxXField.setValue(DEFAULT_MAXX);
		maxXField.setEnabled(false);
		maxXField.addTextListener(e -> fireConfigChanged());
		maxYField = new DoubleTextField(false, 8);
		maxYField.setValue(DEFAULT_MAXY);
		maxYField.setEnabled(false);
		maxYField.addTextListener(e -> fireConfigChanged());

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
		xBox.addItemListener(UI.newItemSelectListener(e -> {
			if (variablePanel != null) {
				for (String var : variablePanel.getValues().keySet()) {
					variablePanel.setEnabled(var, !var.equals(getVarX()));
				}
			}

			fireConfigChanged();
		}));
		yBox = new JComboBox<>();
		xTransBox = new JComboBox<>(Transform.values());
		xTransBox.addItemListener(UI.newItemSelectListener(e -> fireConfigChanged()));
		yTransBox = new JComboBox<>(Transform.values());
		yTransBox.addItemListener(UI.newItemSelectListener(e -> fireConfigChanged()));

		JPanel variablesPanel = new JPanel();

		variablesPanel.setLayout(new GridBagLayout());
		variablesPanel.add(new JLabel("X:"), UI.westConstraints(0, 0, 1, 1));
		variablesPanel.add(xBox, UI.westConstraints(1, 0, 1, 1));
		variablesPanel.add(new JLabel("Y:"), UI.westConstraints(2, 0, 1, 1));
		variablesPanel.add(yBox, UI.westConstraints(3, 0, 1, 1));
		variablesPanel.add(new JLabel("X Transform:"), UI.westConstraints(0, 1, 1, 1));
		variablesPanel.add(xTransBox, UI.westConstraints(1, 1, 1, 1));
		variablesPanel.add(new JLabel("Y Transform:"), UI.westConstraints(2, 1, 1, 1));
		variablesPanel.add(yTransBox, UI.westConstraints(3, 1, 1, 1));

		JPanel outerVariablesPanel = new JPanel();

		outerVariablesPanel.setBorder(BorderFactory.createTitledBorder("Variables"));
		outerVariablesPanel.setLayout(new BorderLayout());
		outerVariablesPanel.add(variablesPanel, BorderLayout.WEST);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(outerDisplayOptionsPanel);
		mainPanel.add(outerRangePanel);
		mainPanel.add(outerVariablesPanel);

		if (allowParameters) {
			outerParameterPanel = new JPanel();
			outerParameterPanel.setBorder(BorderFactory.createTitledBorder("Parameter Values"));
			outerParameterPanel.setLayout(new BorderLayout());
			mainPanel.add(outerParameterPanel);
		}

		if (allowVariables) {
			outerVariablePanel = new JPanel();
			outerVariablePanel.setBorder(BorderFactory.createTitledBorder("Variable Values"));
			outerVariablePanel.setLayout(new BorderLayout());
			mainPanel.add(outerVariablePanel);
		}

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.NORTH);
	}

	public void addConfigListener(ConfigListener listener) {
		listenerList.add(ConfigListener.class, listener);
	}

	public void removeConfigListener(ConfigListener listener) {
		listenerList.remove(ConfigListener.class, listener);
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
		return minXField.isValueValid() ? minXField.getValue() : DEFAULT_MINX;
	}

	public void setMinX(double minX) {
		minXField.setValue(minX);
	}

	public double getMinY() {
		return minYField.isValueValid() ? minYField.getValue() : DEFAULT_MINY;
	}

	public void setMinY(double minY) {
		minYField.setValue(minY);
	}

	public double getMaxX() {
		return maxXField.isValueValid() ? maxXField.getValue() : DEFAULT_MAXX;
	}

	public void setMaxX(double maxX) {
		maxXField.setValue(maxX);
	}

	public double getMaxY() {
		return maxYField.isValueValid() ? maxYField.getValue() : DEFAULT_MAXY;
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

	public InterpolationFactory.Type getInterpolator() {
		return (InterpolationFactory.Type) interpolatorBox.getSelectedItem();
	}

	public void setInterpolator(InterpolationFactory.Type interpolator) {
		interpolatorBox.setSelectedItem(interpolator);
	}

	public String getVarX() {
		return (String) xBox.getSelectedItem();
	}

	public void setVarX(String varX) {
		if (UI.hasItem(xBox, varX)) {
			xBox.setSelectedItem(varX);
		} else if (xBox.getItemCount() != 0) {
			xBox.setSelectedIndex(0);
		} else {
			xBox.setSelectedItem(null);
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

	public void init(String varY, List<String> variablesX, List<String> parameters) {
		String currentVarX = getVarX();

		yBox.removeAllItems();
		yBox.addItem(varY);
		yBox.setSelectedIndex(0);
		xBox.removeAllItems();

		if (variablesX != null && !variablesX.isEmpty()) {
			variablesX.forEach(x -> xBox.addItem(x));

			if (variablesX.contains(currentVarX)) {
				xBox.setSelectedItem(currentVarX);
			} else {
				xBox.setSelectedIndex(0);
			}
		}

		if (outerParameterPanel != null) {
			outerParameterPanel.removeAll();
			parameterPanel = new VariablePanel(
					Maps.asMap(new LinkedHashSet<>(parameters != null ? parameters : Collections.emptySet()),
							Functions.constant(new ArrayList<>())),
					null, null, false, true, true);
			parameterPanel.addValueListener(e -> fireConfigChanged());
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

		if (outerVariablePanel != null) {
			outerVariablePanel.removeAll();
			variablePanel = new VariablePanel(
					Maps.asMap(new LinkedHashSet<>(variablesX != null ? variablesX : Collections.emptySet()),
							Functions.constant(new ArrayList<>())),
					null, null, false, true, true);
			variablePanel.setEnabled(getVarX(), false);
			variablePanel.addValueListener(e -> fireConfigChanged());
			outerVariablePanel.add(variablePanel, BorderLayout.WEST);

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
		return parameterPanel != null ? parameterPanel.getValues() : null;
	}

	public Map<String, Double> getMinParamValues() {
		return parameterPanel != null ? parameterPanel.getMinValues() : null;
	}

	public Map<String, Double> getMaxParamValues() {
		return parameterPanel != null ? parameterPanel.getMaxValues() : null;
	}

	public void setParamValues(Map<String, Double> values, Map<String, Double> minValues,
			Map<String, Double> maxValues) {
		if (outerParameterPanel != null) {
			outerParameterPanel.removeAll();
			parameterPanel = new VariablePanel(
					Maps.asMap(parameterPanel.getValues().keySet(), Functions.constant(new ArrayList<>())), minValues,
					maxValues, false, true, true);
			parameterPanel.setValues(values);
			parameterPanel.addValueListener(e -> fireConfigChanged());
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

	public Map<String, Double> getVariableValues() {
		return variablePanel != null ? variablePanel.getValues() : new LinkedHashMap<>();
	}

	public Map<String, Double> getMinVariableValues() {
		return variablePanel != null ? variablePanel.getMinValues() : new LinkedHashMap<>();
	}

	public Map<String, Double> getMaxVariableValues() {
		return variablePanel != null ? variablePanel.getMaxValues() : new LinkedHashMap<>();
	}

	public void setVariableValues(Map<String, Double> values, Map<String, Double> minValues,
			Map<String, Double> maxValues) {
		if (outerVariablePanel != null) {
			outerVariablePanel.removeAll();
			variablePanel = new VariablePanel(
					Maps.asMap(variablePanel.getValues().keySet(), Functions.constant(new ArrayList<>())), minValues,
					maxValues, false, true, true);
			variablePanel.setValues(values);
			variablePanel.setEnabled(getVarX(), false);
			variablePanel.addValueListener(e -> fireConfigChanged());
			outerVariablePanel.add(variablePanel, BorderLayout.WEST);

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

	private void fireConfigChanged() {
		Stream.of(getListeners(ConfigListener.class)).forEach(l -> l.configChanged(this));
	}

	public static interface ConfigListener extends EventListener {

		void configChanged(ChartConfigPanel source);
	}
}
