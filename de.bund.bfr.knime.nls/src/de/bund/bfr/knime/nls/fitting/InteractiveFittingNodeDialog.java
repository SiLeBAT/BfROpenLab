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
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
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
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObject;
import org.nfunk.jep.ParseException;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.ChartUtils;
import de.bund.bfr.knime.nls.chart.Plotable;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.knime.nls.view.DiffFunctionReader;
import de.bund.bfr.knime.nls.view.FunctionReader;
import de.bund.bfr.knime.nls.view.Reader;
import de.bund.bfr.knime.ui.DoubleTextField;

/**
 * <code>NodeDialog</code> for the "DiffFunctionFitting" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class InteractiveFittingNodeDialog extends DataAwareNodeDialogPane
		implements ChartConfigPanel.ConfigListener, ChartCreator.ZoomListener {

	private boolean isDiff;
	private Reader reader;
	private InteractiveFittingSettings set;

	private ChartCreator chartCreator;
	private ChartConfigPanel configPanel;
	private DoubleTextField stepSizeField;
	private JCheckBox enforceLimitsBox;

	private FunctionPortObject functionObject;
	private BufferedDataTable varTable;
	private BufferedDataTable conditionTable;

	/**
	 * New pane for configuring the DiffFunctionFitting node.
	 */
	protected InteractiveFittingNodeDialog(boolean isDiff) {
		this.isDiff = isDiff;
		set = new InteractiveFittingSettings();

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		set.loadSettings(settings);
		functionObject = (FunctionPortObject) input[0];
		varTable = (BufferedDataTable) input[1];

		if (isDiff) {
			conditionTable = (BufferedDataTable) input[2];
			reader = new DiffFunctionReader(functionObject, varTable, conditionTable);
		} else {
			reader = new FunctionReader(functionObject, varTable, set.getViewSettings().getVarX());
		}

		((JPanel) getTab("Options")).removeAll();
		((JPanel) getTab("Options")).add(createMainComponent());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.setStartValues(configPanel.getParamValues());
		set.setMinStartValues(configPanel.getMinValues());
		set.setMaxStartValues(configPanel.getMaxValues());
		set.setEnforceLimits(enforceLimitsBox.isSelected());
		set.setStepSize(isDiff ? stepSizeField.getValue() : Double.NaN);
		set.saveSettings(settings);
	}

	private JComponent createMainComponent() {
		enforceLimitsBox = new JCheckBox("Enforce Limits");
		enforceLimitsBox.setSelected(set.isEnforceLimits());
		configPanel = new ChartConfigPanel(false, false, true);
		configPanel.init(reader.getDepVar(), new ArrayList<>(ChartUtils.getVariables(reader.getPlotables().values())),
				new ArrayList<>(ChartUtils.getParameters(reader.getPlotables().values())), set.getMinStartValues(),
				set.getMaxStartValues());
		set.getViewSettings().setToConfigPanel(configPanel);
		configPanel.setParamValues(set.getStartValues());
		chartCreator = new ChartCreator(reader.getPlotables(), reader.getLegend());
		chartCreator.setVarY(reader.getDepVar());

		configPanel.addConfigListener(this);
		chartCreator.addZoomListener(this);
		createChart();

		JPanel regressionPanel;

		if (isDiff) {
			stepSizeField = new DoubleTextField(false, 8);
			stepSizeField.setMinValue(Double.MIN_NORMAL);
			stepSizeField.setValue(set.getStepSize());
			regressionPanel = UI.createOptionsPanel("Regression",
					Arrays.asList(new JLabel("Integration Step Size"), enforceLimitsBox),
					Arrays.asList(stepSizeField, new JLabel()));
		} else {
			regressionPanel = UI.createOptionsPanel("Regression", Arrays.asList(enforceLimitsBox),
					Arrays.asList(new JLabel()));
		}

		JPanel rightPanel = new JPanel();

		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(new JScrollPane(configPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		rightPanel.add(regressionPanel, BorderLayout.SOUTH);

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(chartCreator, BorderLayout.CENTER);
		panel.add(rightPanel, BorderLayout.EAST);

		return panel;
	}

	private void createChart() {
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
	public void configChanged() {
		if (!configPanel.getVarX().equals(set.getViewSettings().getVarX())) {
			set.getViewSettings().setFromConfigPanel(configPanel);

			if (isDiff) {
				reader = new DiffFunctionReader(functionObject, varTable, conditionTable);
			} else {
				reader = new FunctionReader(functionObject, varTable, set.getViewSettings().getVarX());
			}

			((JPanel) getTab("Options")).removeAll();
			((JPanel) getTab("Options")).add(createMainComponent());
		} else {
			createChart();
		}
	}

	@Override
	public void zoomChanged() {
		configPanel.removeConfigListener(this);
		configPanel.setManualRange(true);
		configPanel.setMinX(chartCreator.getMinX());
		configPanel.setMaxX(chartCreator.getMaxX());
		configPanel.setMinY(chartCreator.getMinY());
		configPanel.setMaxY(chartCreator.getMaxY());
		configPanel.addConfigListener(this);
		createChart();
	}
}
