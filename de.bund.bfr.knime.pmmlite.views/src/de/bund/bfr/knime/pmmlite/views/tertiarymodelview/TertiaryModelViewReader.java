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
package de.bund.bfr.knime.pmmlite.views.tertiarymodelview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.Plotables;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.views.ViewReader;
import de.bund.bfr.knime.pmmlite.views.ViewUtils;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel.ConditionValue;

class TertiaryModelViewReader implements ViewReader {

	private List<String> ids;
	private List<TimeSeries> data;
	private List<String> formulas;
	private List<Map<String, ParameterValue>> parameters;
	private Map<String, List<String>> stringValues;
	private Map<String, List<Double>> qualityValues;
	private List<String> qualityValueUnits;
	private Map<String, List<ConditionValue>> conditionValues;
	private Set<String> visibleColumns;
	private Set<String> filterableColumns;
	private Map<String, Plotable> plotables;
	private Map<String, String> legend;
	private Map<String, PmmUnit> units;

	public TertiaryModelViewReader(PmmPortObject input) throws UnitException, ParseException {
		List<TertiaryModel> models = input.getData(TertiaryModel.class);

		ids = new ArrayList<>();
		plotables = new LinkedHashMap<>();
		legend = new LinkedHashMap<>();
		formulas = new ArrayList<>();
		parameters = new ArrayList<>();
		data = null;
		qualityValues = new LinkedHashMap<>();
		qualityValues.put(PmmUtils.LOCAL_SSE, new ArrayList<>());
		qualityValues.put(PmmUtils.LOCAL_MSE, new ArrayList<>());
		qualityValues.put(PmmUtils.LOCAL_RMSE, new ArrayList<>());
		qualityValueUnits = new ArrayList<>();
		conditionValues = null;
		data = new ArrayList<>();
		stringValues = new LinkedHashMap<>();
		stringValues.put(PmmUtils.MODEL, new ArrayList<>());
		stringValues.put(PmmUtils.DATA, new ArrayList<>());
		stringValues.put(ChartSelectionPanel.STATUS, new ArrayList<>());
		visibleColumns = new LinkedHashSet<>(Arrays.asList(PmmUtils.MODEL, ChartSelectionPanel.STATUS, PmmUtils.DATA));
		filterableColumns = new LinkedHashSet<>(
				Arrays.asList(PmmUtils.MODEL, ChartSelectionPanel.STATUS, PmmUtils.DATA));
		conditionValues = ViewUtils.initConditionsValues(PmmUtils.getData(models));

		for (TertiaryModel model : models) {
			for (int i = 0; i < model.getData().size(); i++) {
				plotables.put(createId(model, model.getData().get(i)), Plotables.read(model, i, false));
			}
		}

		units = ViewUtils.getMostCommonUnits(plotables.values());

		for (TertiaryModel model : models) {
			for (TimeSeries series : model.getData()) {
				String id = createId(model, series);
				Plotable plotable = plotables.get(id);

				ids.add(id);
				formulas.add(model.getFormula().getExpression());
				parameters.add(model.getParamValues().map());
				data.add(series);
				stringValues.get(PmmUtils.MODEL).add(model.getFormula().getName());
				stringValues.get(PmmUtils.DATA).add(series.getName());
				stringValues.get(ChartSelectionPanel.STATUS).add(plotable.getStatus().toString());
				qualityValues.get(PmmUtils.LOCAL_SSE).add(plotable.getSse());
				qualityValues.get(PmmUtils.LOCAL_MSE).add(plotable.getMse());
				qualityValues.get(PmmUtils.LOCAL_RMSE).add(plotable.getRmse());
				qualityValueUnits.add(model.getFormula().getDepVar().getUnit().toString());
				legend.put(id, model.getFormula().getName() + " (" + series.getName() + ")");

				ViewUtils.addConditionsValuesFromData(conditionValues, units, series);
			}
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
				.qualityValues(qualityValues).qualityValueUnits(qualityValueUnits).conditionValues(conditionValues)
				.filterableColumns(filterableColumns).data(data).parameters(parameters).formulas(formulas)
				.visibleColumns(visibleColumns);
	}

	private static String createId(TertiaryModel model, TimeSeries series) {
		return model.getId() + series.getId();
	}
}
