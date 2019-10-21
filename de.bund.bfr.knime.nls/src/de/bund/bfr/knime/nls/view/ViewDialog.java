/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.nls.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.chart.ChartAllPanel;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;
import de.bund.bfr.knime.nls.chart.Plotable;

public abstract class ViewDialog extends DataAwareNodeDialogPane
		implements ChartSelectionPanel.SelectionListener, ChartConfigPanel.ConfigListener, ChartCreator.ZoomListener {

	protected PortObject[] input;

	protected ViewReader reader;
	protected ViewSettings set;

	protected JPanel mainPanel;

	protected ChartCreator chartCreator;
	protected ChartSelectionPanel selectionPanel;
	protected ChartConfigPanel configPanel;

	protected ViewDialog() {
		set = new ViewSettings();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		addTab("Options", mainPanel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		this.input = input;

		set.loadSettings(settings);
		reader = createReader();
		updateChartPanel();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.saveSettings(settings);
	}

	protected abstract ViewReader createReader();

	protected abstract ChartConfigPanel createConfigPanel();

	protected void updateChartPanel() {
		configPanel = createConfigPanel();
		configPanel.init(reader.getDepVar(), NlsUtils.getSortedVariables(reader.getPlotables().values()), null);
		selectionPanel = new ChartSelectionPanel(reader.getIds(), reader.getStringColumns(), reader.getDoubleColumns());
		chartCreator = new ChartCreator(reader.getPlotables(), reader.getLegend());
		chartCreator.setVarY(reader.getDepVar());

		set.setToConfigPanel(configPanel);
		set.setToSelectionPanel(selectionPanel);
		configPanel.addConfigListener(this);
		selectionPanel.addSelectionListener(this);
		chartCreator.addZoomListener(this);
		updateChart();

		mainPanel.removeAll();
		mainPanel.add(new ChartAllPanel(chartCreator, selectionPanel, configPanel));
		mainPanel.revalidate();
	}

	protected void updateChart() {
		set.setFromConfigPanel(configPanel);
		set.setFromSelectionPanel(selectionPanel);
		set.setToChartCreator(chartCreator);

		for (Plotable plotable : reader.getPlotables().values()) {
			set.setToPlotable(plotable);
		}

		try {
			chartCreator.setChart(chartCreator.createChart());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(ChartSelectionPanel source) {
		updateChart();
	}

	@Override
	public void configChanged(ChartConfigPanel source) {
		updateChart();
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
