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
package de.bund.bfr.knime.nls.functionview;

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
import de.bund.bfr.knime.nls.chart.Plotable;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;

/**
 * <code>NodeDialog</code> for the "FunctionView" Node.
 * 
 * @author Christian Thoens
 */
public class FunctionViewNodeDialog extends DataAwareNodeDialogPane implements
		ChartSelectionPanel.SelectionListener, ChartConfigPanel.ConfigListener,
		ChartCreator.ZoomListener {

	private FunctionViewReader reader;
	private FunctionViewSettings set;

	private ChartCreator chartCreator;
	private ChartSelectionPanel selectionPanel;
	private ChartConfigPanel configPanel;

	private FunctionPortObject functionObject;
	private BufferedDataTable paramTable;
	private BufferedDataTable covarianceTable;
	private BufferedDataTable varTable;

	/**
	 * New pane for configuring the FunctionView node.
	 */
	protected FunctionViewNodeDialog() {
		set = new FunctionViewSettings();

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
		covarianceTable = (BufferedDataTable) input[2];
		varTable = (BufferedDataTable) input[3];
		reader = new FunctionViewReader(functionObject, paramTable,
				covarianceTable, varTable, set.getCurrentParamX());
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
		Map<String, Double> minValues = new LinkedHashMap<>();
		Map<String, Double> maxValues = new LinkedHashMap<>();

		for (Plotable plotable : reader.getPlotables().values()) {
			paramsX.putAll(plotable.getIndependentVariables());

			for (Map.Entry<String, Double> min : plotable.getMinVariables()
					.entrySet()) {
				Double oldMin = minValues.get(min.getKey());

				if (oldMin == null) {
					minValues.put(min.getKey(), min.getValue());
				} else if (min.getValue() != null) {
					minValues.put(min.getKey(),
							Math.min(min.getValue(), oldMin));
				}
			}

			for (Map.Entry<String, Double> max : plotable.getMaxVariables()
					.entrySet()) {
				Double oldMax = minValues.get(max.getKey());

				if (oldMax == null) {
					maxValues.put(max.getKey(), max.getValue());
				} else if (max.getValue() != null) {
					maxValues.put(max.getKey(),
							Math.max(max.getValue(), oldMax));
				}
			}
		}

		for (String var : paramsX.keySet()) {
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

		set.setToConfigPanel(configPanel);
		set.setToSelectionPanel(selectionPanel);
		configPanel.addConfigListener(this);
		selectionPanel.addSelectionListener(this);
		chartCreator.addZoomListener(this);
		createChart();

		return new ChartAllPanel(chartCreator, selectionPanel, configPanel);
	}

	private void createChart() {
		chartCreator.setParamX(configPanel.getParamX());
		chartCreator.setParamY(configPanel.getParamY());
		chartCreator.setTransformX(configPanel.getTransformX());
		chartCreator.setTransformY(configPanel.getTransformY());
		chartCreator.setMinToZero(configPanel.isMinToZero());
		chartCreator.setManualRange(configPanel.isManualRange());
		chartCreator.setMinX(configPanel.getMinX());
		chartCreator.setMinY(configPanel.getMinY());
		chartCreator.setMaxX(configPanel.getMaxX());
		chartCreator.setMaxY(configPanel.getMaxY());
		chartCreator.setDrawLines(configPanel.isDrawLines());
		chartCreator.setShowLegend(configPanel.isShowLegend());
		chartCreator.setShowConfidence(configPanel.isShowConfidence());
		chartCreator.setSelectAll(selectionPanel.isSelectAll());
		chartCreator.setSelectedIds(selectionPanel.getSelectedIds());
		chartCreator.setColors(selectionPanel.getColors());
		chartCreator.setShapes(selectionPanel.getShapes());

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
			reader = new FunctionViewReader(functionObject, paramTable,
					covarianceTable, varTable, set.getCurrentParamX());
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
