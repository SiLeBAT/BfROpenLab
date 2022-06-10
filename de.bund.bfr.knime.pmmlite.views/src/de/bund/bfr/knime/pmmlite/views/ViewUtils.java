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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel.ConditionValue;
import de.bund.bfr.math.MathUtils;

public class ViewUtils {

	private ViewUtils() {
	}

	public static Map<String, List<ConditionValue>> initConditionsValues(Collection<TimeSeries> data) {
		Map<String, List<ConditionValue>> values = new LinkedHashMap<>();

		for (String cond : PmmUtils.getConditions(data)) {
			values.put(cond, new ArrayList<>());
		}

		return values;
	}

	public static void addConditionsValuesFromData(Map<String, List<ConditionValue>> values, Map<String, PmmUnit> units,
			TimeSeries data) throws UnitException {
		Map<String, Condition> conditionsByName = PmmUtils.getByName(data.getConditions());

		for (Map.Entry<String, List<ConditionValue>> entry : values.entrySet()) {
			Condition condition = conditionsByName.get(entry.getKey());
			double value = Double.NaN;

			if (condition != null) {
				value = PmmUtils.convertTo(MathUtils.nullToNan(condition.getValue()), condition.getUnit(),
						units.get(entry.getKey()));
			}

			entry.getValue().add(new ConditionValue(Double.isFinite(value) ? value : null));
		}
	}

	public static void addConditionsRangesFromData(Map<String, List<ConditionValue>> ranges, Map<String, PmmUnit> units,
			Collection<TimeSeries> data) throws UnitException {
		List<Map<String, Condition>> conditionsByName = new ArrayList<>();

		for (TimeSeries series : data) {
			conditionsByName.add(PmmUtils.getByName(series.getConditions()));
		}

		for (Map.Entry<String, List<ConditionValue>> entry : ranges.entrySet()) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;

			for (Map<String, Condition> byName : conditionsByName) {
				Condition condition = byName.get(entry.getKey());

				if (condition != null) {
					double value = PmmUtils.convertTo(MathUtils.nullToNan(condition.getValue()), condition.getUnit(),
							units.get(entry.getKey()));

					if (Double.isFinite(value)) {
						min = Math.min(min, value);
						max = Math.max(max, value);
					}
				}
			}

			entry.getValue()
					.add(new ConditionValue(Double.isFinite(min) ? min : null, Double.isFinite(max) ? max : null));
		}
	}

	public static Map<String, PmmUnit> getMostCommonUnits(Collection<Plotable> plotables) {
		ListMultimap<String, PmmUnit> units = ArrayListMultimap.create();

		for (Plotable plotable : plotables) {
			for (Map.Entry<String, PmmUnit> entry : plotable.getUnits().entrySet()) {
				units.put(entry.getKey(), entry.getValue());
			}
		}

		Map<String, PmmUnit> mostCommon = new LinkedHashMap<>();

		for (Map.Entry<String, Collection<PmmUnit>> entry : units.asMap().entrySet()) {
			mostCommon.put(entry.getKey(), PmmUtils.getMaxCounted(entry.getValue()));
		}

		return mostCommon;
	}

	public static Map<String, Double> computeMinValues(Collection<Plotable> plotables) {
		Map<String, Double> minValues = new LinkedHashMap<>();

		for (Plotable plotable : plotables) {
			for (String var : plotable.getAllVariables()) {
				Double oldMin = minValues.get(var);
				Double min = plotable.getMinValues().get(var);

				if (oldMin == null) {
					minValues.put(var, min);
				} else if (min != null) {
					minValues.put(var, Math.min(min, oldMin));
				}
			}
		}

		return minValues;
	}

	public static Map<String, Double> computeMaxValues(Collection<Plotable> plotables) {
		Map<String, Double> maxValues = new LinkedHashMap<>();

		for (Plotable plotable : plotables) {
			for (String var : plotable.getAllVariables()) {
				Double oldMax = maxValues.get(var);
				Double max = plotable.getMaxValues().get(var);

				if (oldMax == null) {
					maxValues.put(var, max);
				} else if (max != null) {
					maxValues.put(var, Math.max(max, oldMax));
				}
			}
		}

		return maxValues;
	}

	public static Map<String, List<Double>> computeStartValues(Collection<Plotable> plotables) {
		Map<String, Double> minValues = ViewUtils.computeMinValues(plotables);
		Map<String, Double> maxValues = ViewUtils.computeMaxValues(plotables);
		Map<String, List<Double>> values = new LinkedHashMap<>();

		for (Plotable plotable : plotables) {
			for (String var : plotable.getAllVariables()) {
				if (minValues.get(var) != null) {
					values.put(var, Arrays.asList(minValues.get(var)));
				} else if (maxValues.get(var) != null) {
					values.put(var, Arrays.asList(maxValues.get(var)));
				} else {
					values.put(var, Arrays.asList(0.0));
				}
			}
		}

		return values;
	}

	public static Double toDouble(Integer value) {
		return value != null ? value.doubleValue() : null;
	}
}
