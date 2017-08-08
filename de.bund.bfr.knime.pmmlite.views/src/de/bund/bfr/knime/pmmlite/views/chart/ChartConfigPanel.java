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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitDialog;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.IntTextField;
import de.bund.bfr.knime.ui.TextInput;
import de.bund.bfr.knime.ui.TextListener;
import de.bund.bfr.knime.ui.VariablePanel;
import de.bund.bfr.math.Transform;

public class ChartConfigPanel extends JPanel
		implements ActionListener, MouseListener, ItemListener, TextListener, VariablePanel.ValueListener {

	public static enum InputType {
		NO_VARIABLE_INPUT, VARIABLE_FIELDS, VARIABLE_BOXES
	}

	private static final long serialVersionUID = 1L;

	private static final double DEFAULT_MINX = 0.0;
	private static final double DEFAULT_MAXX = 1.0;
	private static final double DEFAULT_MINY = 0.0;
	private static final double DEFAULT_MAXY = 1.0;
	private static final int DEFAULT_EXPORT_WIDTH = 640;
	private static final int DEFAULT_EXPORT_HEIGHT = 480;

	private ChartAllPanel owner;

	private JCheckBox drawLinesBox;
	private JCheckBox showLegendBox;
	private JCheckBox displayHighlightedBox;
	private JCheckBox showConfidenceBox;
	private JCheckBox showPredictionBox;
	private JCheckBox exportAsSvgBox;
	private IntTextField exportWidthField;
	private IntTextField exportHeightField;
	private JSlider resolutionSlider;

	private JCheckBox manualRangeBox;
	private DoubleTextField minXField;
	private DoubleTextField minYField;
	private DoubleTextField maxXField;
	private DoubleTextField maxYField;

	private JComboBox<String> xBox;
	private JComboBox<String> yBox;
	private JButton xUnitButton;
	private JButton yUnitButton;
	private PmmUnit xUnit;
	private PmmUnit yUnit;
	private JComboBox<Transform> xTransBox;
	private JComboBox<Transform> yTransBox;
	private String lastVarX;
	private Map<String, List<Double>> variablesX;
	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private JPanel outerValuesPanel;
	private VariablePanel valuesPanel;

	private InputType type;

	public ChartConfigPanel(InputType type, boolean allowInterval) {
		owner = null;
		this.type = type;
		lastVarX = null;

		drawLinesBox = new JCheckBox("Draw Lines");
		drawLinesBox.setSelected(false);
		drawLinesBox.addItemListener(this);
		displayHighlightedBox = new JCheckBox("Display Highlighted Row");
		displayHighlightedBox.setSelected(false);
		displayHighlightedBox.addItemListener(this);
		showLegendBox = new JCheckBox("Show Legend");
		showLegendBox.setSelected(true);
		showLegendBox.addItemListener(this);
		exportAsSvgBox = new JCheckBox("Export as SVG");
		exportAsSvgBox.setSelected(false);
		showConfidenceBox = new JCheckBox("Show Conf. Interval");
		showConfidenceBox.setSelected(false);
		showConfidenceBox.addItemListener(this);
		showPredictionBox = new JCheckBox("Show Pred. Interval");
		showPredictionBox.setSelected(false);
		showPredictionBox.addItemListener(this);
		exportWidthField = new IntTextField(false, 4);
		exportHeightField = new IntTextField(false, 4);
		resolutionSlider = new JSlider(0, 1000, 1000);
		resolutionSlider.setMinorTickSpacing(100);
		resolutionSlider.setMajorTickSpacing(200);
		resolutionSlider.setPaintTicks(true);
		resolutionSlider.setPaintLabels(true);
		resolutionSlider.addMouseListener(this);
		valuesPanel = new VariablePanel(null, null, null, type == InputType.VARIABLE_BOXES, false, false);

		JPanel sizePanel = new JPanel();

		sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.X_AXIS));
		sizePanel.add(new JLabel("Export Size:"));
		sizePanel.add(Box.createHorizontalStrut(5));
		sizePanel.add(exportWidthField);
		sizePanel.add(Box.createHorizontalStrut(5));
		sizePanel.add(new JLabel("x"));
		sizePanel.add(Box.createHorizontalStrut(5));
		sizePanel.add(exportHeightField);
		sizePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel resolutionPanel = new JPanel();

		resolutionPanel.setLayout(new BoxLayout(resolutionPanel, BoxLayout.X_AXIS));
		resolutionPanel.add(new JLabel("Resolution:"));
		resolutionPanel.add(Box.createHorizontalStrut(5));
		resolutionPanel.add(resolutionSlider);
		resolutionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel displayOptionsPanel = new JPanel();
		int row = 0;

		displayOptionsPanel.setLayout(new BoxLayout(displayOptionsPanel, BoxLayout.Y_AXIS));
		displayOptionsPanel.setLayout(new GridBagLayout());
		displayOptionsPanel.add(drawLinesBox, UI.westConstraints(0, row));
		displayOptionsPanel.add(displayHighlightedBox, UI.westConstraints(1, row));
		row++;
		displayOptionsPanel.add(showLegendBox, UI.westConstraints(0, row));
		displayOptionsPanel.add(exportAsSvgBox, UI.westConstraints(1, row));
		row++;

		if (allowInterval) {
			displayOptionsPanel.add(showConfidenceBox, UI.westConstraints(0, row));
			displayOptionsPanel.add(showPredictionBox, UI.westConstraints(1, row));
			row++;
		}

		displayOptionsPanel.add(sizePanel, UI.westConstraints(0, row, 2, 1));
		row++;
		displayOptionsPanel.add(resolutionPanel, UI.westConstraints(0, row, 2, 1));

		JPanel outerDisplayOptionsPanel = new JPanel();

		outerDisplayOptionsPanel.setBorder(BorderFactory.createTitledBorder("Display Options"));
		outerDisplayOptionsPanel.setLayout(new BorderLayout());
		outerDisplayOptionsPanel.add(displayOptionsPanel, BorderLayout.WEST);

		JPanel rangePanel = new JPanel();

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
		rangePanel.add(manualRangeBox, UI.westConstraints(0, 0, 4, 1));
		rangePanel.add(new JLabel("Min X:"), UI.westConstraints(0, 1));
		rangePanel.add(minXField, UI.westConstraints(1, 1));
		rangePanel.add(new JLabel("Max X:"), UI.westConstraints(2, 1));
		rangePanel.add(maxXField, UI.westConstraints(3, 1));
		rangePanel.add(new JLabel("Min Y:"), UI.westConstraints(0, 2));
		rangePanel.add(minYField, UI.westConstraints(1, 2));
		rangePanel.add(new JLabel("Max Y:"), UI.westConstraints(2, 2));
		rangePanel.add(maxYField, UI.westConstraints(3, 2));

		JPanel outerRangePanel = new JPanel();

		outerRangePanel.setBorder(BorderFactory.createTitledBorder("Range"));
		outerRangePanel.setLayout(new BorderLayout());
		outerRangePanel.add(rangePanel, BorderLayout.WEST);

		xBox = new JComboBox<>();
		xBox.addItemListener(this);
		yBox = new JComboBox<>();
		xUnit = new PmmUnit.Builder().build();
		yUnit = new PmmUnit.Builder().build();
		xUnitButton = new JButton(xUnit.toString());
		xUnitButton.addActionListener(this);
		yUnitButton = new JButton(yUnit.toString());
		yUnitButton.addActionListener(this);
		xTransBox = new JComboBox<>(Transform.values());
		xTransBox.addItemListener(this);
		yTransBox = new JComboBox<>(Transform.values());
		yTransBox.addItemListener(this);

		JPanel variablesPanel = new JPanel();

		variablesPanel.setLayout(new GridBagLayout());
		variablesPanel.add(new JLabel("X:"), UI.westConstraints(0, 0));
		variablesPanel.add(xBox, UI.westConstraints(1, 0));
		variablesPanel.add(new JLabel("Y:"), UI.westConstraints(2, 0));
		variablesPanel.add(yBox, UI.westConstraints(3, 0));

		variablesPanel.add(new JLabel("X Unit:"), UI.westConstraints(0, 1));
		variablesPanel.add(xUnitButton, UI.fillConstraints(1, 1));
		variablesPanel.add(new JLabel("Y Unit:"), UI.westConstraints(2, 1));
		variablesPanel.add(yUnitButton, UI.fillConstraints(3, 1));

		variablesPanel.add(new JLabel("X Transform:"), UI.westConstraints(0, 2));
		variablesPanel.add(xTransBox, UI.westConstraints(1, 2));
		variablesPanel.add(new JLabel("Y Transform:"), UI.westConstraints(2, 2));
		variablesPanel.add(yTransBox, UI.westConstraints(3, 2));

		JPanel outerVariablesPanel = new JPanel();

		outerVariablesPanel.setBorder(BorderFactory.createTitledBorder("Variables on Display"));
		outerVariablesPanel.setLayout(new BorderLayout());
		outerVariablesPanel.add(variablesPanel, BorderLayout.WEST);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(outerDisplayOptionsPanel);
		mainPanel.add(outerRangePanel);
		mainPanel.add(outerVariablesPanel);

		if (type != InputType.NO_VARIABLE_INPUT) {
			outerValuesPanel = new JPanel();
			outerValuesPanel.setBorder(BorderFactory.createTitledBorder("Other Variables"));
			outerValuesPanel.setLayout(new BorderLayout());
			outerValuesPanel.add(valuesPanel, BorderLayout.WEST);
			mainPanel.add(outerValuesPanel);
		}

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.NORTH);
	}

	public ChartAllPanel getOwner() {
		return owner;
	}

	public void setOwner(ChartAllPanel owner) {
		this.owner = owner;
	}

	public void addConfigListener(ConfigListener listener) {
		listenerList.add(ConfigListener.class, listener);
	}

	public void removeConfigListener(ConfigListener listener) {
		listenerList.remove(ConfigListener.class, listener);
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

	public boolean isDisplayHighlighted() {
		return displayHighlightedBox.isSelected();
	}

	public void setDisplayHighlighted(boolean displayHighlighted) {
		displayHighlightedBox.setSelected(displayHighlighted);
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

	public void setShowConfidence(boolean showConfidence) {
		showConfidenceBox.setSelected(showConfidence);
	}

	public boolean isShowPrediction() {
		return showPredictionBox.isSelected();
	}

	public void setShowPrediction(boolean showPrediction) {
		showPredictionBox.setSelected(showPrediction);
	}

	public int getExportWidth() {
		if (exportWidthField.isValueValid()) {
			return exportWidthField.getValue();
		} else {
			return DEFAULT_EXPORT_WIDTH;
		}
	}

	public void setExportWidth(int exportWidth) {
		exportWidthField.setValue(exportWidth);
	}

	public int getExportHeight() {
		if (exportHeightField.isValueValid()) {
			return exportHeightField.getValue();
		} else {
			return DEFAULT_EXPORT_HEIGHT;
		}
	}

	public void setExportHeight(int exportHeight) {
		exportHeightField.setValue(exportHeight);
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
		xBox.setSelectedItem(varX);
	}

	public String getVarY() {
		return (String) yBox.getSelectedItem();
	}

	public void setVarY(String varY) {
		yBox.setSelectedItem(varY);
	}

	public PmmUnit getUnitX() {
		return xUnit;
	}

	public void setUnitX(PmmUnit xUnit) {
		this.xUnit = xUnit;
		xUnitButton.setText(this.xUnit.toString());
	}

	public PmmUnit getUnitY() {
		return yUnit;
	}

	public void setUnitY(PmmUnit yUnit) {
		this.yUnit = yUnit;
		yUnitButton.setText(this.yUnit.toString());
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

	public void clear() {
		init(null, null, null, null, null);
	}

	public void init(String varY, String varX) {
		Map<String, List<Double>> variablesX = new LinkedHashMap<>();

		variablesX.put(varX, new ArrayList<>(0));
		init(varY, variablesX, null, null, null);
	}

	public void init(String varY, Map<String, List<Double>> variablesX, Map<String, Double> minValues,
			Map<String, Double> maxValues) {
		init(varY, variablesX, minValues, maxValues, null);
	}

	public void init(String varY, Map<String, List<Double>> variablesX, Map<String, Double> minValues,
			Map<String, Double> maxValues, String lockedVarX) {
		boolean variablesChanged = !Objects.equals(this.variablesX, variablesX);
		this.variablesX = variablesX != null ? variablesX : Collections.emptyMap();
		this.minValues = minValues != null ? minValues : Collections.emptyMap();
		this.maxValues = maxValues != null ? maxValues : Collections.emptyMap();

		if (variablesChanged) {
			xBox.removeItemListener(this);
			xBox.removeAllItems();

			if (lockedVarX != null) {
				xBox.addItem(lockedVarX);
			} else {
				for (String var : this.variablesX.keySet()) {
					xBox.addItem(var);
				}
			}

			if (!this.variablesX.isEmpty()) {
				if (this.variablesX.containsKey(lastVarX)) {
					xBox.setSelectedItem(lastVarX);
				} else if (this.variablesX.containsKey(PmmUtils.TIME)) {
					xBox.setSelectedItem(PmmUtils.TIME);
				} else {
					xBox.setSelectedIndex(0);
				}

				lastVarX = (String) xBox.getSelectedItem();
			} else {
				lastVarX = null;
			}

			xBox.addItemListener(this);
			updateValuesPanel();
		}

		if (varY == null) {
			yBox.removeAllItems();
		} else if (!varY.equals(yBox.getSelectedItem())) {
			yBox.removeAllItems();
			yBox.addItem(varY);
			yBox.setSelectedIndex(0);
		}
	}

	public Map<String, List<Boolean>> getSelectedValues() {
		return valuesPanel.getSelectedValues();
	}

	public void setSelectedValues(Map<String, List<Boolean>> selectedValues) {
		valuesPanel.setSelectedValues(selectedValues);
	}

	public Map<String, Double> getVariableValues() {
		Map<String, Double> variableValues = valuesPanel.getValues();

		variableValues.put((String) xBox.getSelectedItem(), 0.0);

		return variableValues;
	}

	public void setVariableValues(Map<String, Double> variableValues) {
		valuesPanel.setValues(variableValues);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == xUnitButton) {
			UnitDialog dialog = new UnitDialog(xUnitButton, xUnit);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setUnitX(dialog.getUnit());

				if (owner != null) {
					owner.revalidate();
				}

				fireConfigChanged();
			}
		} else if (e.getSource() == yUnitButton) {
			UnitDialog dialog = new UnitDialog(yUnitButton, yUnit);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setUnitY(dialog.getUnit());

				if (owner != null) {
					owner.revalidate();
				}

				fireConfigChanged();
			}
		}
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
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == manualRangeBox) {
			minXField.setEnabled(manualRangeBox.isSelected());
			minYField.setEnabled(manualRangeBox.isSelected());
			maxXField.setEnabled(manualRangeBox.isSelected());
			maxYField.setEnabled(manualRangeBox.isSelected());
		} else if (e.getSource() == xBox) {
			lastVarX = (String) xBox.getSelectedItem();
			updateValuesPanel();
		} else if (e.getSource() == showConfidenceBox && showConfidenceBox.isSelected()) {
			showPredictionBox.removeItemListener(this);
			showPredictionBox.setSelected(false);
			showPredictionBox.addItemListener(this);
		} else if (e.getSource() == showPredictionBox && showPredictionBox.isSelected()) {
			showConfidenceBox.removeItemListener(this);
			showConfidenceBox.setSelected(false);
			showConfidenceBox.addItemListener(this);
		}

		if (e.getSource() instanceof JCheckBox
				|| (e.getSource() instanceof JComboBox && e.getStateChange() == ItemEvent.SELECTED)) {
			fireConfigChanged();
		}
	}

	@Override
	public void textChanged(TextInput source) {
		fireConfigChanged();
	}

	@Override
	public void valuesChanged(VariablePanel source) {
		fireConfigChanged();
	}

	private void updateValuesPanel() {
		if (type == InputType.NO_VARIABLE_INPUT) {
			return;
		}

		Map<String, List<Double>> variables = new LinkedHashMap<>(variablesX);

		variables.remove(xBox.getSelectedItem());
		valuesPanel = new VariablePanel(variables, minValues, maxValues, type == InputType.VARIABLE_BOXES, false,
				false);
		valuesPanel.addValueListener(this);

		outerValuesPanel.removeAll();
		outerValuesPanel.add(valuesPanel, BorderLayout.WEST);

		if (owner != null) {
			owner.revalidate();
		}
	}

	private void fireConfigChanged() {
		Stream.of(getListeners(ConfigListener.class)).forEach(l -> l.configChanged(this));
	}

	public static interface ConfigListener extends EventListener {

		void configChanged(ChartConfigPanel source);
	}
}
