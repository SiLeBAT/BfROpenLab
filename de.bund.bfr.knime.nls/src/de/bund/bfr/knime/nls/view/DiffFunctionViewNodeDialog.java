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
package de.bund.bfr.knime.nls.view;

import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.nfunk.jep.ParseException;

import de.bund.bfr.knime.nls.chart.ChartAllPanel;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;
import de.bund.bfr.knime.nls.chart.ChartUtils;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;

/**
 * <code>NodeDialog</code> for the "DiffFunctionView" Node.
 * 
 * @author Christian Thoens
 */
public class DiffFunctionViewNodeDialog extends DataAwareNodeDialogPane
		implements ChartSelectionPanel.SelectionListener,
		ChartConfigPanel.ConfigListener, ChartCreator.ZoomListener {

	private DiffFunctionViewReader reader;
	private ViewSettings set;

	private ChartCreator chartCreator;
	private ChartSelectionPanel selectionPanel;
	private ChartConfigPanel configPanel;

	private FunctionPortObject functionObject;
	private BufferedDataTable paramTable;
	private BufferedDataTable varTable;
	private BufferedDataTable conditionTable;
	private BufferedDataTable covarianceTable;

	/**
	 * New pane for configuring the DiffFunctionView node.
	 */
	protected DiffFunctionViewNodeDialog() {
		set = new ViewSettings();

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		set.loadSettings(settings);
		functionObject = (FunctionPortObject) input[0];
		paramTable = (BufferedDataTable) input[1];
		varTable = (BufferedDataTable) input[2];
		conditionTable = (BufferedDataTable) input[3];
		covarianceTable = (BufferedDataTable) input[4];
		reader = new DiffFunctionViewReader(functionObject, paramTable,
				varTable, conditionTable, covarianceTable);
		((JPanel) getTab("Options")).removeAll();
		((JPanel) getTab("Options")).add(createMainComponent());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		set.setFromConfigPanel(configPanel);
		set.setFromSelectionPanel(selectionPanel);
		set.saveSettings(settings);
	}

	private JComponent createMainComponent() {
		Map<String, Double> paramsX = new LinkedHashMap<>();
		Map<String, Double> minValues = ChartUtils.getMinValues(reader
				.getPlotables().values());
		Map<String, Double> maxValues = ChartUtils.getMaxValues(reader
				.getPlotables().values());

		for (String var : ChartUtils.getVariables(reader.getPlotables()
				.values())) {
			if (minValues.get(var) != null) {
				paramsX.put(var, minValues.get(var));
			} else if (maxValues.get(var) != null) {
				paramsX.put(var, maxValues.get(var));
			} else {
				paramsX.put(var, 0.0);
			}
		}

		configPanel = new ChartConfigPanel(false);
		configPanel.setParameters(reader.getDepVar(), paramsX, minValues,
				maxValues);
		selectionPanel = new ChartSelectionPanel(reader.getIds(),
				reader.getStringColumns(), reader.getDoubleColumns());
		chartCreator = new ChartCreator(reader.getPlotables(),
				reader.getLegend());
		chartCreator.setParamY(reader.getDepVar());

		set.setToConfigPanel(configPanel);
		set.setToSelectionPanel(selectionPanel);
		configPanel.addConfigListener(this);
		selectionPanel.addSelectionListener(this);
		chartCreator.addZoomListener(this);
		createChart();

		return new ChartAllPanel(chartCreator, selectionPanel, configPanel);
	}

	private void createChart() {
		set.setFromConfigPanel(configPanel);
		set.setFromSelectionPanel(selectionPanel);
		set.setToChartCreator(chartCreator);

		try {
			chartCreator.setChart(chartCreator.createChart());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged() {
		createChart();
	}

	@Override
	public void configChanged() {
		if (!configPanel.getParamX().equals(set.getCurrentParamX())) {
			set.setFromConfigPanel(configPanel);
			set.setFromSelectionPanel(selectionPanel);
			reader = new DiffFunctionViewReader(functionObject, paramTable,
					varTable, conditionTable, covarianceTable);
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
