/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views.dataselection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.Plotables;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.views.ViewReader;
import de.bund.bfr.knime.pmmlite.views.ViewUtils;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel.ConditionValue;

class DataSelectionReader implements ViewReader {

	private List<TimeSeries> timeSeries;
	private List<String> ids;
	private Map<String, List<String>> stringValues;
	private Map<String, List<ConditionValue>> conditionValues;
	private Set<String> visibleColumns;
	private Set<String> filterableColumns;
	private Map<String, Plotable> plotables;
	private Map<String, String> legend;
	private Map<String, PmmUnit> units;

	public DataSelectionReader(PmmPortObject input) throws UnitException {
		timeSeries = input.getData(TimeSeries.class);
		units = PmmUtils.getMostCommonUnits(timeSeries);
		ids = new ArrayList<>();
		plotables = new LinkedHashMap<>();
		stringValues = new LinkedHashMap<>();
		stringValues.put(PmmUtils.DATA, new ArrayList<>());
		stringValues.put(PmmUtils.ORGANISM, new ArrayList<>());
		stringValues.put(PmmUtils.MATRIX_TYPE, new ArrayList<>());
		legend = new LinkedHashMap<>();
		visibleColumns = new LinkedHashSet<>(Arrays.asList(PmmUtils.DATA));
		filterableColumns = new LinkedHashSet<>(Arrays.asList(PmmUtils.ORGANISM, PmmUtils.MATRIX_TYPE));
		conditionValues = ViewUtils.initConditionsValues(timeSeries);

		for (TimeSeries series : timeSeries) {
			String id = series.getId() + "";
			Plotable plotable = Plotables.read(series);

			ids.add(id);
			stringValues.get(PmmUtils.DATA).add(series.getName());
			stringValues.get(PmmUtils.ORGANISM).add(series.getOrganism());
			stringValues.get(PmmUtils.MATRIX_TYPE).add(series.getMatrix());
			plotables.put(id, plotable);
			legend.put(id, series.getName());

			ViewUtils.addConditionsValuesFromData(conditionValues, units, series);
		}

		conditionValues = PmmUtils.addUnitToKey(conditionValues, units);
		visibleColumns.addAll(conditionValues.keySet());
	}

	@Override
	public Map<String, Plotable> getPlotables() {
		return plotables;
	}

	@Override
	public Map<String, String> getLegend() {
		return legend;
	}

	@Override
	public Map<String, PmmUnit> getUnits() {
		return units;
	}

	@Override
	public ChartSelectionPanel.Builder getSelectionPanelBuilder() {
		return new ChartSelectionPanel.Builder(ids, false, false).stringValues(stringValues)
				.conditionValues(conditionValues).filterableColumns(filterableColumns).data(timeSeries)
				.visibleColumns(visibleColumns);
	}
}
