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
package de.bund.bfr.knime.pmmlite.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.views.chart.ChartAllPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartConfigPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartCreator;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSamplePanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;

public abstract class ViewDialog extends DataAwareNodeDialogPane implements ChartSelectionPanel.SelectionListener,
		ChartConfigPanel.ConfigListener, ChartSamplePanel.EditListener, ChartCreator.ZoomListener {

	private JPanel tabPanel;

	protected ChartCreator chartCreator;
	protected ChartSelectionPanel selectionPanel;
	protected ChartConfigPanel configPanel;
	protected ChartSamplePanel samplePanel;

	protected ViewSettings set;
	protected ViewReader reader;

	protected ViewDialog() {
		tabPanel = new JPanel();
		tabPanel.setLayout(new BorderLayout());
		addTab("Options", tabPanel, false);
		set = createSettings();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		set.load(settings);

		try {
			reader = createReader((PmmPortObject) input[0]);
		} catch (UnitException | ParseException e) {
			throw new NotConfigurableException(e.getMessage());
		}

		initChartComponents();
		updateChart();
		tabPanel.removeAll();
		tabPanel.add(showSamplePanel() ? new ChartAllPanel(chartCreator, selectionPanel, configPanel, samplePanel)
				: new ChartAllPanel(chartCreator, selectionPanel, configPanel));
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (showSamplePanel()) {
			set.setFromSamplePanel(samplePanel);
		}

		set.setFromConfigPanel(configPanel);
		set.setFromSelectionPanel(selectionPanel);
		set.save(settings);
	}

	protected abstract ViewSettings createSettings();

	protected abstract ViewReader createReader(PmmPortObject input) throws UnitException, ParseException;

	protected abstract boolean showSamplePanel();

	protected abstract boolean allowInterval();

	protected abstract void initChartComponents();

	protected abstract void updateChart();

	@Override
	public void selectionChanged(ChartSelectionPanel source) {
		updateChart();
	}

	@Override
	public void focusChanged(ChartSelectionPanel source) {
		if (configPanel.isDisplayHighlighted()) {
			updateChart();
		}
	}

	@Override
	public void configChanged(ChartConfigPanel source) {
		updateChart();
	}

	@Override
	public void valuesChanged(ChartSamplePanel source) {
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
