/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views.primarymodelselection;

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
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.views.ViewReader;
import de.bund.bfr.knime.pmmlite.views.ViewUtils;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel.ConditionValue;

class PrimaryModelSelectionReader implements ViewReader {

	private List<PrimaryModel> models;
	private List<String> ids;
	private List<TimeSeries> data;
	private List<String> formulas;
	private Map<String, List<String>> stringValues;
	private Map<String, List<Double>> qualityValues;
	private List<String> qualityValueUnits;
	private Map<String, List<ConditionValue>> conditionValues;
	private Map<String, List<ParameterValue>> parameterValues;
	private Set<String> visibleColumns;
	private Set<String> filterableColumns;
	private Map<String, Plotable> plotables;
	private Map<String, String> legend;
	private Map<String, PmmUnit> units;

	public PrimaryModelSelectionReader(PmmPortObject input) throws UnitException, ParseException {
		models = input.getData(PrimaryModel.class);
		ids = new ArrayList<>();
		plotables = new LinkedHashMap<>();
		legend = new LinkedHashMap<>();
		data = new ArrayList<>();
		formulas = new ArrayList<>();
		qualityValues = new LinkedHashMap<>();
		qualityValues.put(PmmUtils.SSE, new ArrayList<>());
		qualityValues.put(PmmUtils.MSE, new ArrayList<>());
		qualityValues.put(PmmUtils.RMSE, new ArrayList<>());
		qualityValues.put(PmmUtils.R2, new ArrayList<>());
		qualityValues.put(PmmUtils.AIC, new ArrayList<>());
		qualityValues.put(PmmUtils.DOF, new ArrayList<>());
		qualityValueUnits = new ArrayList<>();
		stringValues = new LinkedHashMap<>();
		stringValues.put(PmmUtils.MODEL, new ArrayList<>());
		stringValues.put(PmmUtils.DATA, new ArrayList<>());
		stringValues.put(ChartSelectionPanel.STATUS, new ArrayList<>());
		stringValues.put(PmmUtils.ORGANISM, new ArrayList<>());
		stringValues.put(PmmUtils.MATRIX_TYPE, new ArrayList<>());
		visibleColumns = new LinkedHashSet<>(Arrays.asList(PmmUtils.MODEL, ChartSelectionPanel.STATUS, PmmUtils.DATA));
		filterableColumns = new LinkedHashSet<>(
				Arrays.asList(PmmUtils.MODEL, ChartSelectionPanel.STATUS, PmmUtils.DATA));
		conditionValues = ViewUtils.initConditionsValues(PmmUtils.getData(models));
		parameterValues = new LinkedHashMap<>();

		for (String param : PmmUtils.getParameters(models)) {
			parameterValues.put(param, new ArrayList<>());
		}

		for (PrimaryModel model : models) {
			Plotable p = Plotables.read(model, true, false);
			String initParam = model.getFormula().getInitialParam();

			if (initParam != null && p.getParameters().get(initParam) == null) {
				p.getParameters().remove(initParam);
				p.getVariableParameters().put(initParam, 0.0);
			}

			plotables.put(model.getId(), p);
		}

		units = ViewUtils.getMostCommonUnits(plotables.values());

		for (PrimaryModel model : models) {
			TimeSeries series = model.getData();
			Plotable plotable = plotables.get(model.getId());

			ids.add(model.getId());
			data.add(series);
			formulas.add(model.getFormula().getExpression());
			stringValues.get(PmmUtils.MODEL).add(model.getFormula().getName());
			stringValues.get(PmmUtils.DATA).add(series.getName());
			stringValues.get(PmmUtils.ORGANISM).add(series.getOrganism());
			stringValues.get(PmmUtils.MATRIX_TYPE).add(series.getMatrix());
			stringValues.get(ChartSelectionPanel.STATUS).add(plotable.getStatus().toString());
			qualityValues.get(PmmUtils.SSE).add(plotable.getSse());
			qualityValues.get(PmmUtils.MSE).add(plotable.getMse());
			qualityValues.get(PmmUtils.RMSE).add(plotable.getRmse());
			qualityValues.get(PmmUtils.R2).add(plotable.getR2());
			qualityValues.get(PmmUtils.AIC).add(plotable.getAic());
			qualityValues.get(PmmUtils.DOF).add(ViewUtils.toDouble(plotable.getDegreesOfFreedom()));
			qualityValueUnits.add(model.getFormula().getDepVar().getUnit().toString());
			legend.put(model.getId(), model.getFormula().getName() + " (" + series.getName() + ")");

			ViewUtils.addConditionsValuesFromData(conditionValues, units, series);

			for (Map.Entry<String, List<ParameterValue>> entry : parameterValues.entrySet()) {
				ParameterValue value = model.getParamValues().get(entry.getKey());

				if (value == null) {
					value = ModelsFactory.eINSTANCE.createParameterValue();
				}

				entry.getValue().add(value);
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
				.parameterValues(parameterValues).filterableColumns(filterableColumns).data(data).formulas(formulas)
				.visibleColumns(visibleColumns);
	}
}
