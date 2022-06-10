/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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

import java.util.List;
import java.util.Map;

import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.views.chart.ChartConfigPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartCreator;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSamplePanel;
import de.bund.bfr.knime.ui.Dialogs;

public abstract class MultiSelectionViewDialog extends ViewDialog {

	@Override
	protected ViewSettings createSettings() {
		return new MultiSelectionViewSettings();
	}

	@Override
	protected void initChartComponents() {
		Map<String, List<Double>> variablesX = ViewUtils.computeStartValues(reader.getPlotables().values());
		Map<String, Double> minValues = ViewUtils.computeMinValues(reader.getPlotables().values());
		Map<String, Double> maxValues = ViewUtils.computeMaxValues(reader.getPlotables().values());
		boolean hasOtherVariables = variablesX.keySet().stream().anyMatch(v -> !v.equals(PmmUtils.TIME));

		configPanel = new ChartConfigPanel(hasOtherVariables ? ChartConfigPanel.InputType.VARIABLE_FIELDS
				: ChartConfigPanel.InputType.NO_VARIABLE_INPUT, allowInterval());
		configPanel.init(PmmUtils.CONCENTRATION, variablesX, minValues, maxValues, PmmUtils.TIME);
		selectionPanel = reader.getSelectionPanelBuilder().build();
		chartCreator = new ChartCreator(reader.getPlotables(), reader.getLegend());

		set.setToConfigPanel(configPanel);
		set.setToSelectionPanel(selectionPanel);

		if (set.isFirstStart()) {
			configPanel.setVarX(PmmUtils.TIME);
			configPanel.setVarY(PmmUtils.CONCENTRATION);
			configPanel.setUnitX(PmmUtils.nullToEmpty(reader.getUnits().get(PmmUtils.TIME)));
			configPanel.setUnitY(PmmUtils.nullToEmpty(reader.getUnits().get(PmmUtils.CONCENTRATION)));
		}

		configPanel.addConfigListener(this);
		selectionPanel.addSelectionListener(this);
		chartCreator.addZoomListener(this);

		if (showSamplePanel()) {
			samplePanel = new ChartSamplePanel();
			samplePanel.setNameX(PmmUtils.TIME);
			set.setToSamplePanel(samplePanel);
			samplePanel.addEditListener(this);
		}
	}

	@Override
	protected void updateChart() {
		List<String> ids = configPanel.isDisplayHighlighted() ? selectionPanel.getFocusedIDs()
				: selectionPanel.getSelectedIDs();

		if (showSamplePanel()) {
			set.setFromSamplePanel(samplePanel);
		}

		set.setFromConfigPanel(configPanel);
		set.setFromSelectionPanel(selectionPanel);
		set.setToChartCreator(chartCreator);

		for (String id : ids) {
			Plotable plotable = reader.getPlotables().get(id);

			if (plotable != null) {
				set.setToPlotable(plotable);
			}
		}

		try {
			chartCreator.createChart(ids);
		} catch (ParseException | UnitException e) {
			chartCreator.createEmptyChart();
			Dialogs.showErrorMessage(chartCreator, e.getMessage());
		}
	}
}
