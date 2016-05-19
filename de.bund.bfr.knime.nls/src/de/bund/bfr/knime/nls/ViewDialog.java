/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.nls;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;

public abstract class ViewDialog extends DataAwareNodeDialogPane
		implements ChartSelectionPanel.SelectionListener, ChartConfigPanel.ConfigListener, ChartCreator.ZoomListener {

	protected ViewSettings set;

	protected ChartCreator chartCreator;
	protected ChartSelectionPanel selectionPanel;
	protected ChartConfigPanel configPanel;

	protected ViewDialog() {
		set = createSettings();

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		addTab("Options", panel, false);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.saveSettings(settings);
	}

	protected ViewSettings createSettings() {
		return new ViewSettings();
	}

	protected abstract void createChart();

	@Override
	public void selectionChanged(ChartSelectionPanel source) {
		createChart();
	}

	@Override
	public void configChanged(ChartConfigPanel source) {
		createChart();
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
		createChart();
	}
}
