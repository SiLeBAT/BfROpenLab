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
package de.bund.bfr.knime.nls.fitting;

import java.awt.BorderLayout;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObject;
import org.nfunk.jep.ParseException;

import de.bund.bfr.knime.nls.chart.ChartAllPanel;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.Plotable;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.knime.nls.view.FunctionReader;
import de.bund.bfr.knime.nls.view.ViewUtils;

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

	private FunctionReader reader;
	private InteractiveFittingSettings set;

	private ChartCreator chartCreator;
	private ChartConfigPanel configPanel;

	private FunctionPortObject functionObject;
	private BufferedDataTable varTable;

	/**
	 * New pane for configuring the DiffFunctionFitting node.
	 */
	protected InteractiveFittingNodeDialog() {
		set = new InteractiveFittingSettings();

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		set.loadSettings(settings);
		functionObject = (FunctionPortObject) input[0];
		varTable = (BufferedDataTable) input[1];
		reader = new FunctionReader(functionObject, varTable, set
				.getViewSettings().getCurrentParamX());
		((JPanel) getTab("Options")).removeAll();
		((JPanel) getTab("Options")).add(createMainComponent());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		set.setMinStartValues(configPanel.getParamsX());
		set.saveSettings(settings);
	}

	private JComponent createMainComponent() {
		Map<String, Double> paramsX = ViewUtils.createZeroMap(functionObject
				.getFunction().getIndependentVariables());
		Set<String> changeableParameters = new LinkedHashSet<>(functionObject
				.getFunction().getParameters());

		paramsX.putAll(ViewUtils.createZeroMap(functionObject.getFunction()
				.getParameters()));

		configPanel = new ChartConfigPanel(changeableParameters);
		configPanel.setParameters(reader.getDepVar(), paramsX, null, null);
		configPanel.setParamXValues(set.getMinStartValues());
		chartCreator = new ChartCreator(reader.getPlotables(),
				reader.getLegend());
		chartCreator.setParamY(reader.getDepVar());

		set.getViewSettings().setToConfigPanel(configPanel);
		configPanel.addConfigListener(this);
		chartCreator.addZoomListener(this);
		createChart();

		return new ChartAllPanel(chartCreator, configPanel);
	}

	private void createChart() {
		set.getViewSettings().setFromConfigPanel(configPanel);
		set.getViewSettings().setToChartCreator(chartCreator);

		for (Plotable plotable : reader.getPlotables().values()) {
			plotable.setParameters(configPanel.getParamsX());
		}

		try {
			chartCreator.setChart(chartCreator.createChart());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void configChanged() {
		if (!configPanel.getParamX().equals(
				set.getViewSettings().getCurrentParamX())) {
			set.getViewSettings().setFromConfigPanel(configPanel);
			reader = new FunctionReader(functionObject, varTable, set
					.getViewSettings().getCurrentParamX());
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
