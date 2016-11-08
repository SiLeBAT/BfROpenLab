/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views.fittedparameterview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.Plotables;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.views.ViewReader;
import de.bund.bfr.knime.pmmlite.views.ViewUtils;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel.ConditionValue;
import de.bund.bfr.math.MathUtils;

class FittedParameterViewReader implements ViewReader {

	private List<String> ids;
	private List<Integer> colorCounts;
	private Map<String, List<String>> stringValues;
	private Map<String, List<ConditionValue>> conditionValues;
	private Set<String> visibleColumns;
	private Map<String, Plotable> plotables;
	private Map<String, String> legend;

	public FittedParameterViewReader(PmmPortObject input) throws UnitException {
		ListMultimap<String, PrimaryModel> modelsById = ArrayListMultimap.create();

		for (PrimaryModel model : input.getData(PrimaryModel.class)) {
			modelsById.put(model.getFormula().getId(), model);
		}

		ids = new ArrayList<>();
		colorCounts = new ArrayList<>();
		plotables = new LinkedHashMap<>();
		legend = new LinkedHashMap<>();
		stringValues = new LinkedHashMap<>();
		stringValues.put(PmmUtils.PARAMETER, new ArrayList<>());
		stringValues.put(PmmUtils.MODEL, new ArrayList<>());
		visibleColumns = new LinkedHashSet<>(Arrays.asList(PmmUtils.PARAMETER, PmmUtils.MODEL));
		conditionValues = ViewUtils.initConditionsValues(PmmUtils.getData(modelsById.values()));

		for (List<PrimaryModel> currentModels : Multimaps.asMap(modelsById).values()) {
			PrimaryModelFormula formula = currentModels.get(0).getFormula();
			Map<String, Plotable> currentPlotables = Plotables.readFittedParameters(currentModels);

			for (Parameter param : formula.getParams()) {
				plotables.put(createId(formula, param), currentPlotables.get(param.getName()));
			}
		}

		Map<String, PmmUnit> units = ViewUtils.getMostCommonUnits(plotables.values());

		for (List<PrimaryModel> currentModels : Multimaps.asMap(modelsById).values()) {
			PrimaryModelFormula formula = currentModels.get(0).getFormula();
			Map<String, ConditionValue> condRanges = new LinkedHashMap<>();
			List<Map<String, Condition>> conditionsByName = new ArrayList<>();

			for (PrimaryModel model : currentModels) {
				conditionsByName.add(PmmUtils.getByName(model.getData().getConditions()));
			}

			for (String cond : conditionValues.keySet()) {
				double min = Double.POSITIVE_INFINITY;
				double max = Double.NEGATIVE_INFINITY;

				for (Map<String, Condition> byName : conditionsByName) {
					Condition condition = byName.get(cond);

					if (condition != null) {
						double value = PmmUtils.convertTo(MathUtils.nullToNan(condition.getValue()),
								condition.getUnit(), units.get(cond));

						if (Double.isFinite(value)) {
							min = Math.min(min, value);
							max = Math.max(max, value);
						}
					}
				}

				condRanges.put(cond,
						new ConditionValue(!Double.isInfinite(min) ? min : null, !Double.isInfinite(max) ? max : null));
			}

			for (Parameter param : formula.getParams()) {
				String id = createId(formula, param);

				ids.add(id);
				legend.put(id, param.getName() + " (" + formula.getName() + ")");
				colorCounts.add(plotables.get(id).getNumberOfCombinations());
				stringValues.get(PmmUtils.PARAMETER).add(param.getName());
				stringValues.get(PmmUtils.MODEL).add(formula.getName());

				for (Map.Entry<String, List<ConditionValue>> entry : conditionValues.entrySet()) {
					entry.getValue().add(condRanges.get(entry.getKey()));
				}
			}
		}

		conditionValues = PmmUtils.addUnitToKey(conditionValues, units);
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
		return new ChartSelectionPanel.Builder(ids, true, true).stringValues(stringValues)
				.conditionValues(conditionValues).colorCounts(colorCounts).visibleColumns(visibleColumns);
	}

	private static String createId(PrimaryModelFormula formula, Parameter param) {
		return formula.getId() + param.getName();
	}
}
