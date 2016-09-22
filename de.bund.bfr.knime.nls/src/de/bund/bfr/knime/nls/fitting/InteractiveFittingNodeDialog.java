/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.collect.Lists;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.Plotable;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.knime.nls.view.DiffFunctionViewReader;
import de.bund.bfr.knime.nls.view.FunctionViewReader;
import de.bund.bfr.knime.nls.view.ViewReader;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.IntTextField;

/**
 * <code>NodeDialog</code> for the "DiffFunctionFitting" Node.
 * 
 * @author Christian Thoens
 */
public class InteractiveFittingNodeDialog extends DataAwareNodeDialogPane
		implements ChartConfigPanel.ConfigListener, ChartCreator.ZoomListener {

	private boolean isDiff;
	private ViewReader reader;
	private InteractiveFittingSettings set;

	private JPanel mainPanel;

	private ChartCreator chartCreator;
	private ChartConfigPanel configPanel;
	private JCheckBox enforceLimitsBox;
	private IntTextField maxIterationsField;
	private JCheckBox lodBox;
	private DoubleTextField lodField;
	private JCheckBox fitAllAtOnceBox;
	private Map<String, JCheckBox> useDifferentInitValuesBoxes;
	private DoubleTextField stepSizeField;

	private FunctionPortObject functionObject;
	private BufferedDataTable varTable;
	private BufferedDataTable conditionTable;

	/**
	 * New pane for configuring the DiffFunctionFitting node.
	 */
	protected InteractiveFittingNodeDialog(boolean isDiff) {
		this.isDiff = isDiff;
		set = new InteractiveFittingSettings();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		addTab("Options", mainPanel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		set.loadSettings(settings);
		functionObject = (FunctionPortObject) input[0];
		varTable = (BufferedDataTable) input[1];

		if (isDiff) {
			conditionTable = (BufferedDataTable) input[2];
			reader = new DiffFunctionViewReader(functionObject, varTable, conditionTable);
		} else {
			reader = new FunctionViewReader(functionObject, null, varTable, null, set.getViewSettings().getVarX());
		}

		updateChartPanel();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (!isDiff && lodBox.isSelected() && !lodField.isValueValid()) {
			throw new InvalidSettingsException("");
		}

		Set<String> initValuesWithDifferentStart = new LinkedHashSet<>();

		useDifferentInitValuesBoxes.forEach((depVar, box) -> {
			if (box.isSelected()) {
				initValuesWithDifferentStart.add(depVar);
			}
		});

		if (!maxIterationsField.isValueValid()) {
			throw new InvalidSettingsException("");
		}

		set.setLevelOfDetection(lodBox.isSelected() ? lodField.getValue() : null);
		set.setFitAllAtOnce(fitAllAtOnceBox.isSelected());
		set.setInitValuesWithDifferentStart(initValuesWithDifferentStart);
		set.setEnforceLimits(enforceLimitsBox.isSelected());
		set.setMaxLevenbergIterations(maxIterationsField.getValue());
		set.setStartValues(configPanel.getParamValues());
		set.setMinStartValues(configPanel.getMinParamValues());
		set.setMaxStartValues(configPanel.getMaxParamValues());
		set.setStepSize(stepSizeField.getValue());
		set.setInterpolator(configPanel.getInterpolator());
		set.saveSettings(settings);
	}

	private void updateChartPanel() {
		lodBox = new JCheckBox("Enable");
		lodBox.setSelected(set.getLevelOfDetection() != null);
		lodBox.addActionListener(e -> {
			lodField.setEnabled(lodBox.isSelected());
			enforceLimitsBox.setEnabled(!lodBox.isSelected());
		});
		lodField = new DoubleTextField(true, 8);
		lodField.setEnabled(lodBox.isSelected());
		lodField.setValue(set.getLevelOfDetection());
		enforceLimitsBox = new JCheckBox("Enforce Limits");
		enforceLimitsBox.setEnabled(!lodBox.isSelected());
		enforceLimitsBox.setSelected(set.isEnforceLimits());
		maxIterationsField = new IntTextField(false, 8);
		maxIterationsField.setMinValue(1);
		maxIterationsField.setValue(set.getMaxLevenbergIterations());
		fitAllAtOnceBox = new JCheckBox("Fit All At Once");
		fitAllAtOnceBox.setSelected(set.isFitAllAtOnce());
		useDifferentInitValuesBoxes = new LinkedHashMap<>();
		fitAllAtOnceBox.addActionListener(
				e -> useDifferentInitValuesBoxes.values().forEach(b -> b.setEnabled(fitAllAtOnceBox.isSelected())));

		stepSizeField = new DoubleTextField(false, 8);
		stepSizeField.setMinValue(Double.MIN_NORMAL);
		stepSizeField.setValue(set.getStepSize());

		configPanel = new ChartConfigPanel(false, false, true, false, isDiff);
		configPanel.init(reader.getDepVar(), new ArrayList<>(NlsUtils.getVariables(reader.getPlotables().values())),
				new ArrayList<>(NlsUtils.getParameters(reader.getPlotables().values())));
		set.getViewSettings().setToConfigPanel(configPanel);
		configPanel.setParamValues(set.getStartValues(), set.getMinStartValues(), set.getMaxStartValues());
		configPanel.setInterpolator(set.getInterpolator());
		chartCreator = new ChartCreator(reader.getPlotables(), reader.getLegend());
		chartCreator.setVarY(reader.getDepVar());

		configPanel.addConfigListener(this);
		chartCreator.addZoomListener(this);
		updateChart();

		List<Component> leftComponents = Lists.newArrayList(enforceLimitsBox,
				new JLabel("Maximal Iterations in each run of Optimization Algorithm"));
		List<Component> rightComponents = Lists.newArrayList(new JLabel(), maxIterationsField);

		if (isDiff) {
			leftComponents.add(fitAllAtOnceBox);
			rightComponents.add(new JLabel());

			functionObject.getFunction().getInitParameters().forEach((depVar, param) -> {
				if (functionObject.getFunction().getInitValues().get(depVar) == null) {
					JCheckBox box = new JCheckBox("Use Different Values for " + param);

					box.setSelected(set.getInitValuesWithDifferentStart().contains(depVar));
					box.setEnabled(fitAllAtOnceBox.isSelected());
					leftComponents.add(box);
					rightComponents.add(new JLabel());
					useDifferentInitValuesBoxes.put(depVar, box);
				}
			});

			leftComponents.add(new JLabel("Integration Step Size"));
			rightComponents.add(stepSizeField);
		} else {
			leftComponents.add(new JLabel("Level of Detection"));
			rightComponents.add(UI.createHorizontalPanel(false, lodBox, lodField));
		}

		JPanel fittingConfigPanel = new JPanel();

		fittingConfigPanel.setLayout(new BorderLayout());
		fittingConfigPanel.add(new JScrollPane(configPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		fittingConfigPanel.add(UI.createOptionsPanel("Fitting", leftComponents, rightComponents), BorderLayout.SOUTH);

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(chartCreator, BorderLayout.CENTER);
		panel.add(fittingConfigPanel, BorderLayout.EAST);

		mainPanel.removeAll();
		mainPanel.add(panel);
		mainPanel.revalidate();
	}

	private void updateChart() {
		set.getViewSettings().setFromConfigPanel(configPanel);
		set.getViewSettings().setToChartCreator(chartCreator);

		for (Plotable plotable : reader.getPlotables().values()) {
			plotable.getParameters().clear();
			plotable.getParameters().putAll(configPanel.getParamValues());
		}

		try {
			chartCreator.setChart(chartCreator.createChart());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void configChanged(ChartConfigPanel source) {
		if (!configPanel.getVarX().equals(set.getViewSettings().getVarX())) {
			set.getViewSettings().setFromConfigPanel(configPanel);

			if (isDiff) {
				reader = new DiffFunctionViewReader(functionObject, varTable, conditionTable);
			} else {
				reader = new FunctionViewReader(functionObject, null, varTable, null, set.getViewSettings().getVarX());
			}

			updateChartPanel();
		} else {
			updateChart();
		}
	}

	@Override
	public void zoomChanged(ChartCreator source) {
		configPanel.removeConfigListener(this);
		configPanel.setManualRange(true);
		configPanel.setMinX(chartCreator.getMinX());
		configPanel.setMaxX(chartCreator.getMaxX());
		configPanel.setMinY(chartCreator.getMinY());
		configPanel.setMaxY(chartCreator.getMaxY());
		configPanel.addConfigListener(this);
		updateChart();
	}
}
