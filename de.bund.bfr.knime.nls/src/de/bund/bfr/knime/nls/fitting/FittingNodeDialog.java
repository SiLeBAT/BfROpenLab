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
package de.bund.bfr.knime.nls.fitting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.functionport.FunctionPortObjectSpec;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.IntTextField;
import de.bund.bfr.math.InterpolationFactory;

/**
 * <code>NodeDialog</code> for the "DiffFunctionFitting" Node.
 * 
 * @author Christian Thoens
 */
public class FittingNodeDialog extends NodeDialogPane {

	private FittingSettings set;
	private boolean isDiff;

	private JPanel mainPanel;
	private JPanel expertPanel;

	private DoubleTextField lodField;
	private JCheckBox fitAllAtOnceBox;
	private Map<String, JCheckBox> useDifferentInitValuesBoxes;
	private JCheckBox expertBox;

	private DoubleTextField stepSizeField;
	private JComboBox<InterpolationFactory.Type> interpolatorBox;
	private IntTextField nParamSpaceField;
	private IntTextField nLevenbergField;
	private JCheckBox stopWhenSuccessBox;
	private IntTextField maxIterationsField;
	private JButton clearButton;
	private JCheckBox limitsBox;
	private Map<String, DoubleTextField> minimumFields;
	private Map<String, DoubleTextField> maximumFields;

	/**
	 * New pane for configuring the DiffFunctionFitting node.
	 */
	protected FittingNodeDialog(boolean isDiff) {
		this.isDiff = isDiff;
		set = new FittingSettings();

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		addTab("Options", UI.createNorthPanel(mainPanel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		Function f = ((FunctionPortObjectSpec) specs[0]).getFunction();

		set.loadSettings(settings);

		lodField = new DoubleTextField(true, 8);
		lodField.setValue(set.getLevelOfDetection());
		fitAllAtOnceBox = new JCheckBox("Fit All At Once");
		fitAllAtOnceBox.setSelected(set.isFitAllAtOnce());
		fitAllAtOnceBox.addActionListener(
				e -> useDifferentInitValuesBoxes.values().forEach(b -> b.setEnabled(fitAllAtOnceBox.isSelected())));
		useDifferentInitValuesBoxes = new LinkedHashMap<>();
		expertBox = new JCheckBox("Expert Settings");
		expertBox.setSelected(set.isExpertSettings());
		expertBox.addActionListener(e -> expertPanel.setVisible(expertBox.isSelected()));

		JPanel p;

		if (isDiff) {
			List<JCheckBox> boxes = new ArrayList<>();

			boxes.add(fitAllAtOnceBox);

			for (Map.Entry<String, String> entry : f.getInitParameters().entrySet()) {
				if (f.getInitValues().get(entry.getKey()) == null) {
					JCheckBox box = new JCheckBox("Use Different Values for " + entry.getValue());

					box.setSelected(set.getInitValuesWithDifferentStart().contains(entry.getKey()));
					box.setEnabled(fitAllAtOnceBox.isSelected());
					boxes.add(box);
					useDifferentInitValuesBoxes.put(entry.getKey(), box);
				}
			}

			boxes.add(expertBox);
			p = UI.createOptionsPanel(null, boxes, Collections.nCopies(boxes.size(), new JLabel()));
		} else {
			p = UI.createOptionsPanel(null, Arrays.asList(new JLabel("Level of Detection"), expertBox),
					Arrays.asList(lodField, new JLabel()));
		}

		mainPanel.add(p, BorderLayout.NORTH);

		updateExpertPanel(f);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (!isDiff && !lodField.isValueValid()) {
			throw new InvalidSettingsException("");
		}

		if (isDiff && !stepSizeField.isValueValid()) {
			throw new InvalidSettingsException("");
		}

		if (!nParamSpaceField.isValueValid() || !nLevenbergField.isValueValid() || !maxIterationsField.isValueValid()
				|| minimumFields == null || maximumFields == null) {
			throw new InvalidSettingsException("");
		}

		Set<String> initValuesWithDifferentStart = new LinkedHashSet<>();
		Map<String, Double> minStartValues = new LinkedHashMap<>();
		Map<String, Double> maxStartValues = new LinkedHashMap<>();

		for (Map.Entry<String, JCheckBox> entry : useDifferentInitValuesBoxes.entrySet()) {
			if (entry.getValue().isSelected()) {
				initValuesWithDifferentStart.add(entry.getKey());
			}
		}

		for (Map.Entry<String, DoubleTextField> entry : minimumFields.entrySet()) {
			minStartValues.put(entry.getKey(), entry.getValue().getValue());
		}

		for (Map.Entry<String, DoubleTextField> entry : maximumFields.entrySet()) {
			maxStartValues.put(entry.getKey(), entry.getValue().getValue());
		}

		set.setLevelOfDetection(lodField.getValue());
		set.setFitAllAtOnce(fitAllAtOnceBox.isSelected());
		set.setInitValuesWithDifferentStart(initValuesWithDifferentStart);
		set.setExpertSettings(expertBox.isSelected());
		set.setnParameterSpace(nParamSpaceField.getValue());
		set.setnLevenberg(nLevenbergField.getValue());
		set.setStopWhenSuccessful(stopWhenSuccessBox.isSelected());
		set.setMaxLevenbergIterations(maxIterationsField.getValue());
		set.setEnforceLimits(limitsBox.isSelected());
		set.setMinStartValues(minStartValues);
		set.setMaxStartValues(maxStartValues);
		set.setStepSize(stepSizeField.getValue());
		set.setInterpolator((InterpolationFactory.Type) interpolatorBox.getSelectedItem());

		set.saveSettings(settings);
	}

	private void updateExpertPanel(Function f) {
		if (expertPanel != null) {
			mainPanel.remove(expertPanel);
		}

		expertPanel = new JPanel();
		expertPanel.setLayout(new BoxLayout(expertPanel, BoxLayout.Y_AXIS));
		expertPanel.add(createRegressionPanel());
		expertPanel.add(createRangePanel(f));

		mainPanel.add(expertPanel, BorderLayout.CENTER);
		mainPanel.revalidate();
		mainPanel.repaint();

		Dimension preferredSize = mainPanel.getPreferredSize();

		if (expertBox.isSelected()) {
			expertPanel.setVisible(true);
		} else {
			expertPanel.setVisible(false);
		}

		mainPanel.setPreferredSize(preferredSize);
	}

	private Component createRegressionPanel() {
		nParamSpaceField = new IntTextField(false, 8);
		nParamSpaceField.setMinValue(1);
		nParamSpaceField.setValue(set.getnParameterSpace());
		nLevenbergField = new IntTextField(false, 8);
		nLevenbergField.setMinValue(1);
		nLevenbergField.setValue(set.getnLevenberg());
		stopWhenSuccessBox = new JCheckBox("Stop When Regression Successful");
		stopWhenSuccessBox.setSelected(set.isStopWhenSuccessful());
		maxIterationsField = new IntTextField(false, 8);
		maxIterationsField.setMinValue(1);
		maxIterationsField.setValue(set.getMaxLevenbergIterations());
		stepSizeField = new DoubleTextField(false, 8);
		stepSizeField.setMinValue(Double.MIN_NORMAL);
		stepSizeField.setValue(set.getStepSize());
		interpolatorBox = new JComboBox<>(InterpolationFactory.Type.values());
		interpolatorBox.setSelectedItem(set.getInterpolator());

		List<Component> leftComps = new ArrayList<>(
				Arrays.asList(new JLabel("Maximal Evaluations to Find Start Values"),
						new JLabel("Maximal Executions of the Levenberg Algorithm"), stopWhenSuccessBox,
						new JLabel("Maximal Iterations in each run of Levenberg Algorithm")));
		List<Component> rightComps = new ArrayList<>(
				Arrays.asList(nParamSpaceField, nLevenbergField, new JLabel(), maxIterationsField));

		if (isDiff) {
			leftComps.add(0, new JLabel("Integration Step Size"));
			rightComps.add(0, stepSizeField);
			leftComps.add(1, new JLabel("Interpolation Function"));
			rightComps.add(1, interpolatorBox);
		}

		return UI.createOptionsPanel("Nonlinear Regression Parameters", leftComps, rightComps);
	}

	private Component createRangePanel(Function function) {
		limitsBox = new JCheckBox("Enforce start values as limits");
		limitsBox.setSelected(set.isEnforceLimits());
		clearButton = new JButton("Clear");
		clearButton
				.addActionListener(e -> Stream.concat(minimumFields.values().stream(), maximumFields.values().stream())
						.forEach(f -> f.setValue(null)));
		minimumFields = new LinkedHashMap<>();
		maximumFields = new LinkedHashMap<>();

		JPanel northRangePanel = new JPanel();

		northRangePanel.setLayout(new BoxLayout(northRangePanel, BoxLayout.Y_AXIS));

		JPanel modelPanel = new JPanel();
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		List<String> params = function.getParameters();

		leftPanel.setLayout(new GridLayout(params.size(), 1));
		rightPanel.setLayout(new GridLayout(params.size(), 1));

		for (String param : params) {
			DoubleTextField minField = new DoubleTextField(true, 8);
			DoubleTextField maxField = new DoubleTextField(true, 8);

			if (set.getMinStartValues().get(param) != null) {
				minField.setValue(set.getMinStartValues().get(param));
			}

			if (set.getMaxStartValues().get(param) != null) {
				maxField.setValue(set.getMaxStartValues().get(param));
			}

			JPanel minMaxPanel = new JPanel();

			minMaxPanel.setLayout(new BoxLayout(minMaxPanel, BoxLayout.X_AXIS));
			minMaxPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			minMaxPanel.add(minField);
			minMaxPanel.add(Box.createHorizontalStrut(5));
			minMaxPanel.add(new JLabel("to"));
			minMaxPanel.add(Box.createHorizontalStrut(5));
			minMaxPanel.add(maxField);

			minimumFields.put(param, minField);
			maximumFields.put(param, maxField);
			leftPanel.add(UI.createHorizontalPanel(new JLabel(param)));
			rightPanel.add(minMaxPanel);
		}

		modelPanel.setLayout(new BorderLayout());
		modelPanel.add(leftPanel, BorderLayout.WEST);
		modelPanel.add(rightPanel, BorderLayout.EAST);

		JPanel rangePanel = new JPanel();

		rangePanel.setLayout(new BorderLayout());
		rangePanel.add(modelPanel, BorderLayout.NORTH);

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder("Specific Start Values for Fitting Procedure - Optional"));
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(clearButton, limitsBox)), BorderLayout.NORTH);
		panel.add(new JScrollPane(rangePanel), BorderLayout.CENTER);

		return panel;
	}
}
