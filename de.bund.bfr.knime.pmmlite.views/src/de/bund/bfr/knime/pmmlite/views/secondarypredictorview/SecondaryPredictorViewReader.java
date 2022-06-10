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
package de.bund.bfr.knime.pmmlite.views.secondarypredictorview;

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
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.views.ViewReader;
import de.bund.bfr.knime.pmmlite.views.ViewUtils;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel.ConditionValue;

class SecondaryPredictorViewReader implements ViewReader {

	private List<String> ids;
	private List<String> formulas;
	private List<Map<String, ParameterValue>> parameters;
	private Map<String, List<String>> stringValues;
	private Map<String, List<Double>> qualityValues;
	private Map<String, List<ConditionValue>> conditionValues;
	private Set<String> visibleColumns;
	private Set<String> filterableColumns;
	private Map<String, Plotable> plotables;
	private Map<String, String> legend;

	public SecondaryPredictorViewReader(PmmPortObject input) throws UnitException, ParseException {
		List<SecondaryModel> models = input.getData(SecondaryModel.class);

		ids = new ArrayList<>();
		plotables = new LinkedHashMap<>();
		legend = new LinkedHashMap<>();
		formulas = new ArrayList<>();
		parameters = new ArrayList<>();
		stringValues = new LinkedHashMap<>();
		stringValues.put(PmmUtils.PARAMETER, new ArrayList<>());
		stringValues.put(PmmUtils.MODEL, new ArrayList<>());
		stringValues.put(ChartSelectionPanel.STATUS, new ArrayList<>());
		qualityValues = new LinkedHashMap<>();
		qualityValues.put(PmmUtils.SSE, new ArrayList<>());
		qualityValues.put(PmmUtils.MSE, new ArrayList<>());
		qualityValues.put(PmmUtils.RMSE, new ArrayList<>());
		qualityValues.put(PmmUtils.R2, new ArrayList<>());
		qualityValues.put(PmmUtils.AIC, new ArrayList<>());
		qualityValues.put(PmmUtils.DOF, new ArrayList<>());
		filterableColumns = new LinkedHashSet<>(Arrays.asList(ChartSelectionPanel.STATUS));
		visibleColumns = new LinkedHashSet<>(
				Arrays.asList(PmmUtils.PARAMETER, PmmUtils.MODEL, ChartSelectionPanel.STATUS));
		conditionValues = ViewUtils.initConditionsValues(PmmUtils.getData(models));

		for (SecondaryModel model : models) {
			Plotable plotable = Plotables.read(model);

			plotable.setType(Plotable.Type.FUNCTION_SAMPLE);
			plotables.put(model.getId(), plotable);
		}

		Map<String, PmmUnit> units = ViewUtils.getMostCommonUnits(plotables.values());

		for (SecondaryModel model : models) {
			String depVar = model.getAssignments().get(model.getFormula().getDepVar().getName());
			PrimaryModel primModel = model.getData().get(0);
			Plotable plotable = plotables.get(model.getId());

			ids.add(model.getId());
			formulas.add(model.getFormula().getExpression());
			parameters.add(model.getParamValues().map());
			stringValues.get(PmmUtils.PARAMETER).add(depVar + " (" + primModel.getFormula().getName() + ")");
			stringValues.get(PmmUtils.MODEL).add(model.getFormula().getName());
			stringValues.get(ChartSelectionPanel.STATUS).add(plotable.getStatus().toString());
			qualityValues.get(PmmUtils.SSE).add(plotable.getSse());
			qualityValues.get(PmmUtils.MSE).add(plotable.getMse());
			qualityValues.get(PmmUtils.RMSE).add(plotable.getRmse());
			qualityValues.get(PmmUtils.R2).add(plotable.getR2());
			qualityValues.get(PmmUtils.AIC).add(plotable.getAic());
			qualityValues.get(PmmUtils.DOF).add(ViewUtils.toDouble(plotable.getDegreesOfFreedom()));
			legend.put(model.getId(), depVar + " (" + primModel.getFormula().getName() + ")");

			ViewUtils.addConditionsRangesFromData(conditionValues, units, PmmUtils.getData(model.getData()));
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
		throw new UnsupportedOperationException();
	}

	@Override
	public ChartSelectionPanel.Builder getSelectionPanelBuilder() {
		return new ChartSelectionPanel.Builder(ids, true, true).stringValues(stringValues).qualityValues(qualityValues)
				.conditionValues(conditionValues).filterableColumns(filterableColumns).parameters(parameters)
				.formulas(formulas).visibleColumns(visibleColumns);
	}
}
