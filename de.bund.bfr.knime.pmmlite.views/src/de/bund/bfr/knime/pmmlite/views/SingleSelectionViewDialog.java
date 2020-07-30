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
package de.bund.bfr.knime.pmmlite.views;

import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.views.chart.ChartConfigPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartCreator;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSamplePanel;
import de.bund.bfr.knime.ui.Dialogs;

public abstract class SingleSelectionViewDialog extends ViewDialog {

	protected String lastSelectedID;
	protected String lastVarX;

	protected SingleSelectionViewDialog() {
		lastSelectedID = null;
		lastVarX = null;
	}

	@Override
	protected ViewSettings createSettings() {
		return new SingleSelectionViewSettings();
	}

	@Override
	protected void initChartComponents() {
		configPanel = new ChartConfigPanel(ChartConfigPanel.InputType.VARIABLE_FIELDS, allowInterval());
		selectionPanel = reader.getSelectionPanelBuilder().build();
		chartCreator = new ChartCreator(reader.getPlotables(), reader.getLegend());

		Plotable plotable = reader.getPlotables().get(((SingleSelectionViewSettings) set).getSelectedID());

		if (plotable != null) {
			configPanel.init(plotable.getFunctionValue(), plotable.getPossibleVariableValues(), plotable.getMinValues(),
					plotable.getMaxValues());
		}

		set.setToConfigPanel(configPanel);
		set.setToSelectionPanel(selectionPanel);

		selectionPanel.addSelectionListener(this);
		configPanel.addConfigListener(this);
		chartCreator.addZoomListener(this);
		lastSelectedID = configPanel.isDisplayHighlighted() ? selectionPanel.getFocusedID()
				: selectionPanel.getSelectedID();
		lastVarX = configPanel.getVarX();

		if (showSamplePanel()) {
			samplePanel = new ChartSamplePanel();
			set.setToSamplePanel(samplePanel);
			samplePanel.addEditListener(this);
		}
	}

	@Override
	protected void updateChart() {
		String selectedID = configPanel.isDisplayHighlighted() ? selectionPanel.getFocusedID()
				: selectionPanel.getSelectedID();
		Plotable plotable = reader.getPlotables().get(selectedID);

		if (plotable != null) {
			configPanel.init(plotable.getFunctionValue(), plotable.getPossibleVariableValues(), plotable.getMinValues(),
					plotable.getMaxValues());

			PmmUnit unitX = plotable.getUnits().get(configPanel.getVarX());
			PmmUnit unitY = plotable.getUnits().get(configPanel.getVarY());

			if (!selectedID.equals(lastSelectedID) || !configPanel.getVarX().equals(lastVarX)) {
				configPanel.setUnitX(PmmUtils.nullToEmpty(unitX));
				configPanel.setUnitY(PmmUtils.nullToEmpty(unitY));
			}

			lastSelectedID = selectedID;
			lastVarX = configPanel.getVarX();
		} else {
			configPanel.clear();
		}

		if (showSamplePanel()) {
			set.setFromSamplePanel(samplePanel);
		}

		set.setFromConfigPanel(configPanel);
		set.setFromSelectionPanel(selectionPanel);
		set.setToChartCreator(chartCreator);

		if (plotable != null) {
			set.setToPlotable(plotable);
		}

		try {
			chartCreator.createChart(selectedID);
		} catch (ParseException | UnitException e) {
			chartCreator.createEmptyChart();
			Dialogs.showErrorMessage(chartCreator, e.getMessage());
		}
	}
}
