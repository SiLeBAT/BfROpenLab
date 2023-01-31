/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.ListMultimap;

import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.common.Nameable;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.Model;
import de.bund.bfr.knime.pmmlite.core.models.ModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.math.LeastSquaresOptimization;

public class PmmUtils {

	public static final String TIME = "Time";
	public static final String CONCENTRATION = "Concentration";

	public static final String DATA = "Data Name";
	public static final String MODEL = "Model Name";
	public static final String PARAMETER = "Parameter Name";
	public static final String COMMENT = "User Comment";
	public static final String ORGANISM = "Organism Type";
	public static final String MATRIX_TYPE = "Matrix Type";

	public static final String SSE = "SSE ";
	public static final String MSE = "MSE ";
	public static final String RMSE = "RMSE ";
	public static final String R2 = "R2 ";
	public static final String AIC = "AIC ";
	public static final String DOF = "Degrees of Freedom";
	public static final String LOCAL_SSE = "SSE (Local)";
	public static final String LOCAL_MSE = "MSE (Local)";
	public static final String LOCAL_RMSE = "RMSE (Local)";
	public static final String UNIT = "Unit ";

	private PmmUtils() {
	}

	public static String createId(String s) {
		return s.replaceAll("[^\\w_]", "");
	}

	public static String createMathSymbol(String s) {
		return s.trim().replaceAll(" +", " ").replaceAll(" ", "_").replaceAll("[^\\w_]", "");
	}

	public static void setId(ModelFormula obj) {
		obj.setId(createId(obj.eClass().getName() + "_" + obj.getName()));
	}

	public static void setId(TimeSeries obj) {
		obj.setId(createId(obj.eClass().getName() + "_" + obj.getName()));
	}

	public static void setId(PrimaryModel obj) {
		List<String> idParts = new ArrayList<>();

		idParts.add(obj.eClass().getName());
		idParts.add(obj.getFormula().getId());

		if (obj.getSse() != null) {
			idParts.add(String.valueOf(obj.getSse()));
		}

		if (obj.getData() != null) {
			idParts.add(obj.getData().getId());
		}

		obj.setId(createId(Joiner.on("_").join(idParts)));
	}

	public static void setId(SecondaryModel obj) {
		List<String> idParts = new ArrayList<>();

		idParts.add(obj.eClass().getName());
		idParts.add(obj.getFormula().getId());
		idParts.add(String.valueOf(getIds(obj.getData()).hashCode()));
		idParts.add(String.valueOf(obj.getAssignments().stream().map(e -> new Pair<>(e.getKey(), e.getValue()))
				.collect(Collectors.toCollection(LinkedHashSet::new)).hashCode()));

		if (obj.getSse() != null) {
			idParts.add(String.valueOf(obj.getSse()));
		}

		obj.setId(createId(Joiner.on("_").join(idParts)));
	}

	public static void setId(TertiaryModel obj) {
		List<String> idParts = new ArrayList<>();

		idParts.add(obj.eClass().getName());
		idParts.add(obj.getFormula().getId());
		idParts.add(String.valueOf(getIds(obj.getData()).hashCode()));
		idParts.add(String.valueOf(obj.getAssignments().stream().map(e -> new Pair<>(e.getKey(), e.getValue()))
				.collect(Collectors.toCollection(LinkedHashSet::new)).hashCode()));

		if (obj.getSse() != null) {
			idParts.add(String.valueOf(obj.getSse()));
		}

		obj.setId(createId(Joiner.on("_").join(idParts)));
	}

	public static List<ModelFormula> getFormulas(List<? extends Model> models) {
		return models.stream().map(m -> m.getFormula()).collect(Collectors.toList());
	}

	public static Set<TimeSeries> getData(Collection<? extends Model> models) {
		Set<TimeSeries> data = new LinkedHashSet<>();

		for (Model model : models) {
			if (model instanceof PrimaryModel) {
				data.add(((PrimaryModel) model).getData());
			} else if (model instanceof SecondaryModel) {
				((SecondaryModel) model).getData().forEach(m -> data.add(m.getData()));
			} else if (model instanceof TertiaryModel) {
				data.addAll(((TertiaryModel) model).getData());
			}
		}

		return data;
	}

	public static Set<String> getConditions(Collection<TimeSeries> data) {
		return data.stream().map(s -> s.getConditions()).flatMap(List::stream).map(c -> c.getName())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static Set<String> getParameters(Collection<? extends Model> models) {
		return models.stream().map(m -> m.getParamValues().keySet()).flatMap(Set::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static List<String> getIds(List<? extends Identifiable> identifiables) {
		return identifiables.stream().map(i -> i.getId()).collect(Collectors.toList());
	}

	public static <V extends Identifiable> Map<String, V> getById(Collection<V> identifiables) {
		Map<String, V> byId = new LinkedHashMap<>();

		identifiables.forEach(i -> byId.put(i.getId(), i));

		return byId;
	}

	public static <V extends Identifiable> List<V> getById(List<V> identifiables, Set<String> ids) {
		return identifiables.stream().filter(i -> ids.contains(i.getId())).collect(Collectors.toList());
	}

	public static void sortByName(List<? extends Nameable> nameables) {
		Collections.sort(nameables, (o1, o2) -> o1.getName().compareTo(o2.getName()));
	}

	public static List<String> getNames(List<? extends Nameable> nameables) {
		return nameables.stream().map(n -> n.getName()).collect(Collectors.toList());
	}

	public static <V extends Nameable> Map<String, V> getByName(Collection<V> nameables) {
		Map<String, V> byName = new LinkedHashMap<>();

		nameables.forEach(n -> byName.put(n.getName(), n));

		return byName;
	}

	public static Map<String, Double> getMinValues(Collection<Parameter> parameters) {
		Map<String, Double> minValues = new LinkedHashMap<>();

		parameters.forEach(p -> minValues.put(p.getName(), p.getMin()));

		return minValues;
	}

	public static Map<String, Double> getMaxValues(Collection<Parameter> parameters) {
		Map<String, Double> maxValues = new LinkedHashMap<>();

		parameters.forEach(p -> maxValues.put(p.getName(), p.getMax()));

		return maxValues;
	}

	public static String getInitialParam(ModelFormula formula) {
		if (formula instanceof PrimaryModelFormula) {
			return ((PrimaryModelFormula) formula).getInitialParam();
		} else if (formula instanceof TertiaryModelFormula) {
			return ((TertiaryModelFormula) formula).getInitialParam();
		}

		return null;
	}

	public static List<Variable> getIndependentVariables(ModelFormula formula) {
		Set<Variable> variables = new LinkedHashSet<>();

		if (formula instanceof PrimaryModelFormula && ((PrimaryModelFormula) formula).getIndepVar() != null) {
			variables.add(((PrimaryModelFormula) formula).getIndepVar());
		} else if (formula instanceof SecondaryModelFormula) {
			variables.addAll(((SecondaryModelFormula) formula).getIndepVars());
		} else if (formula instanceof TertiaryModelFormula) {
			variables.addAll(((TertiaryModelFormula) formula).getIndepVars());
		}

		return new ArrayList<>(variables);
	}

	public static List<Variable> getVariables(ModelFormula formula) {
		Set<Variable> variables = new LinkedHashSet<>();

		if (formula.getDepVar() != null) {
			variables.add(formula.getDepVar());
		}

		variables.addAll(getIndependentVariables(formula));

		return new ArrayList<>(variables);
	}

	public static double convertTo(double value, PmmUnit fromUnit, PmmUnit toUnit) throws UnitException {
		return !toUnit.isEmpty() ? fromUnit.convertTo(value, toUnit) : value;
	}

	public static double convertFrom(double value, PmmUnit fromUnit, PmmUnit toUnit) throws UnitException {
		return !fromUnit.isEmpty() ? fromUnit.convertTo(value, toUnit) : value;
	}

	public static Map<String, PmmUnit> getMostCommonUnits(Collection<TimeSeries> data) {
		ListMultimap<String, PmmUnit> units = ArrayListMultimap.create();

		for (TimeSeries series : data) {
			units.put(PmmUtils.TIME, series.getTimeUnit());
			units.put(PmmUtils.CONCENTRATION, series.getConcentrationUnit());

			for (Condition cond : series.getConditions()) {
				units.put(cond.getName(), cond.getUnit());
			}
		}

		Map<String, PmmUnit> mostCommon = new LinkedHashMap<>();

		for (Map.Entry<String, Collection<PmmUnit>> entry : units.asMap().entrySet()) {
			mostCommon.put(entry.getKey(), PmmUtils.getMaxCounted(entry.getValue()));
		}

		return mostCommon;
	}

	public static <T> T getMaxCounted(Collection<T> values) {
		return Collections.max(LinkedHashMultiset.create(values).entrySet(),
				(o1, o2) -> Integer.compare(o1.getCount(), o2.getCount())).getElement();
	}

	public static <T> Map<String, T> addUnitToKey(Map<String, T> map, Map<String, PmmUnit> units) {
		Map<String, T> newMap = new LinkedHashMap<>();

		for (Map.Entry<String, T> entry : map.entrySet()) {
			PmmUnit unit = units.get(entry.getKey());
			String unitString = unit != null ? " (" + unit.toString() + ")" : "";

			newMap.put(entry.getKey() + unitString, entry.getValue());
		}

		return newMap;
	}

	public static ParameterValue createParameterValue(String paramName, LeastSquaresOptimization.Result result) {
		ParameterValue paramValue = ModelsFactory.eINSTANCE.createParameterValue();

		paramValue.setValue(result.getParameterValues().get(paramName));
		paramValue.setError(result.getParameterStandardErrors().get(paramName));
		paramValue.setT(result.getParameterTValues().get(paramName));
		paramValue.setP(result.getParameterPValues().get(paramName));

		for (Map.Entry<Pair<String, String>, Double> entry : result.getCovariances().entrySet()) {
			if (entry.getKey().getFirst().equals(paramName)) {
				paramValue.getCorrelations().put(entry.getKey().getSecond(), entry.getValue());
			}
		}

		return paramValue;
	}

	public static PmmUnit nullToEmpty(PmmUnit unit) {
		return unit != null ? unit : new PmmUnit.Builder().build();
	}
}
