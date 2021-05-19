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
package de.bund.bfr.knime.pmmlite.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.collect.Lists;

import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint;
import de.bund.bfr.knime.pmmlite.core.models.Model;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.core.models.VariableRange;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.math.Transform;

public class Plotables {

	private Plotables() {
	}

	public static Plotable read(TimeSeries series) {
		Plotable plotable = new Plotable(Plotable.Type.DATASET);

		addTimeSeriesToPlotable(plotable, series);

		return plotable;
	}

	public static Plotable read(PrimaryModel model, boolean useData, boolean initParamAsVariable)
			throws ParseException, UnitException {
		PrimaryModel modelCopy = EcoreUtil.copy(model);
		AssignUtils.applyAssignmentsAndConversion(modelCopy);

		Plotable plotable = new Plotable(useData ? Plotable.Type.BOTH : Plotable.Type.FUNCTION);
		Variable dep = modelCopy.getFormula().getDepVar();
		Variable indep = modelCopy.getFormula().getIndepVar();
		VariableRange indepRange = modelCopy.getVariableRanges().get(indep.getName());
		String initParam = modelCopy.getFormula().getInitialParam();

		addTimeSeriesToPlotable(plotable, modelCopy.getData());
		plotable.setFunction(modelCopy.getFormula().getExpression());
		plotable.setFunctionValue(dep.getName());
		plotable.getUnits().put(dep.getName(), dep.getUnit());
		plotable.getUnits().put(indep.getName(), indep.getUnit());
		plotable.getVariables().put(indep.getName(), Lists.newArrayList(0.0));

		if (indepRange != null) {
			plotable.getMinValues().put(indep.getName(), indepRange.getMin());
			plotable.getMaxValues().put(indep.getName(), indepRange.getMax());
		}

		for (Parameter param : modelCopy.getFormula().getParams()) {
			String name = param.getName();
			ParameterValue paramValue = modelCopy.getParamValues().get(name);
			Double value = paramValue != null ? paramValue.getValue() : null;
			Map<String, Double> cov = new LinkedHashMap<>();

			if (paramValue != null) {
				for (Parameter param2 : modelCopy.getFormula().getParams()) {
					cov.put(param2.getName(), paramValue.getCorrelations().get(param2.getName()));
				}
			}

			plotable.getCovariances().put(name, cov);
			plotable.getMinValues().put(name, param.getMin());
			plotable.getMaxValues().put(name, param.getMax());

			if (initParamAsVariable && name.equals(initParam)) {
				plotable.getVariableParameters().put(name, value != null ? value : 0.0);
			} else {
				plotable.getParameters().put(name, value);
			}
		}

		if (useData) {
			Plotable.Variable varX = new Plotable.Variable(PmmUtils.TIME, plotable.getUnits().get(PmmUtils.TIME),
					Transform.NO_TRANSFORM);
			Plotable.Variable varY = new Plotable.Variable(PmmUtils.CONCENTRATION,
					plotable.getUnits().get(PmmUtils.CONCENTRATION), Transform.NO_TRANSFORM);

			plotable.computeQualityMeasure(varX, varY, false);
		} else {
			setModelMeasuresToPlotable(modelCopy, plotable);
		}

		return plotable;
	}

	public static Plotable read(SecondaryModel model) throws ParseException, UnitException {
		SecondaryModel modelCopy = EcoreUtil.copy(model);
		AssignUtils.applyAssignmentsAndConversion(modelCopy);

		Plotable plotable = new Plotable(Plotable.Type.BOTH_MANY);
		Variable depVar = modelCopy.getFormula().getDepVar();
		Transform transform = modelCopy.getFormula().getTransformation();
		String depVarName = transform.getName(depVar.getName());

		plotable.setFunction(modelCopy.getFormula().getExpression());
		plotable.setFunctionValue(transform.getName(depVar.getName()));
		plotable.getUnits().put(depVarName, depVar.getUnit());

		for (Variable indep : modelCopy.getFormula().getIndepVars()) {
			VariableRange indepRange = modelCopy.getVariableRanges().get(indep.getName());

			plotable.getVariables().put(indep.getName(), Lists.newArrayList(0.0));
			plotable.getUnits().put(indep.getName(), indep.getUnit());

			if (indepRange != null) {
				plotable.getMinValues().put(indep.getName(), indepRange.getMin());
				plotable.getMaxValues().put(indep.getName(), indepRange.getMax());
			}
		}

		for (Parameter param : modelCopy.getFormula().getParams()) {
			String name = param.getName();
			ParameterValue paramValue = modelCopy.getParamValues().get(name);

			plotable.getMinValues().put(name, param.getMin());
			plotable.getMaxValues().put(name, param.getMax());

			if (paramValue != null) {
				Map<String, Double> cov = new LinkedHashMap<>();

				for (Parameter param2 : modelCopy.getFormula().getParams()) {
					cov.put(param2.getName(), paramValue.getCorrelations().get(param2.getName()));
				}

				plotable.getParameters().put(param.getName(), paramValue.getValue());
				plotable.getCovariances().put(param.getName(), cov);
			} else {
				plotable.getParameters().put(param.getName(), null);
				plotable.getCovariances().put(param.getName(), new LinkedHashMap<>(0));
			}
		}

		List<Double> depVarData = new ArrayList<>();
		Map<String, List<Double>> condData = new LinkedHashMap<>();

		for (String cond : PmmUtils.getConditions(PmmUtils.getData(modelCopy.getData()))) {
			condData.put(cond, new ArrayList<>());
		}

		for (PrimaryModel data : modelCopy.getData()) {
			ParameterValue depVarValue = data.getParamValues().get(depVar.getName());

			if (depVarValue == null) {
				continue;
			}

			double value = modelCopy.getFormula().getTransformation().to(MathUtils.nullToNan(depVarValue.getValue()));

			if (!Double.isFinite(value)) {
				continue;
			}

			depVarData.add(value);

			Map<String, Condition> conditionsByName = PmmUtils.getByName(data.getData().getConditions());

			for (Map.Entry<String, List<Double>> entry : condData.entrySet()) {
				Condition cond = conditionsByName.get(entry.getKey());

				if (cond != null) {
					entry.getValue().add(cond.getValue());

					if (!plotable.getUnits().containsKey(entry.getKey())) {
						plotable.getUnits().put(entry.getKey(), cond.getUnit());
					}
				} else {
					entry.getValue().add(null);
				}
			}
		}

		plotable.getValueLists().put(depVarName, depVarData);
		plotable.getValueLists().putAll(condData);

		Variable indepVar = modelCopy.getFormula().getIndepVars().get(0);
		Plotable.Variable varX = new Plotable.Variable(indepVar.getName(), plotable.getUnits().get(indepVar.getName()),
				Transform.NO_TRANSFORM);
		Plotable.Variable varY = new Plotable.Variable(depVarName, plotable.getUnits().get(depVarName),
				Transform.NO_TRANSFORM);

		plotable.getVariables().putAll(plotable.getPossibleVariableValues());
		plotable.computeQualityMeasure(varX, varY, false);

		return plotable;
	}

	public static Plotable read(TertiaryModel model, Integer dataIndex, boolean initParamAsVariable)
			throws ParseException, UnitException {
		TertiaryModel modelCopy = EcoreUtil.copy(model);
		AssignUtils.applyAssignmentsAndConversion(modelCopy);

		Plotable plotable = new Plotable(dataIndex != null ? Plotable.Type.BOTH : Plotable.Type.FUNCTION);
		Variable depVar = modelCopy.getFormula().getDepVar();
		String initParam = modelCopy.getFormula().getInitialParam();

		if (dataIndex != null) {
			addTimeSeriesToPlotable(plotable, modelCopy.getData().get(dataIndex));
		}

		plotable.setFunction(modelCopy.getFormula().getExpression());
		plotable.setFunctionValue(depVar.getName());
		plotable.getUnits().put(depVar.getName(), depVar.getUnit());

		for (Variable indep : modelCopy.getFormula().getIndepVars()) {
			if (dataIndex != null && !indep.getName().equals(PmmUtils.TIME)) {
				for (Condition condition : modelCopy.getData().get(dataIndex).getConditions()) {
					if (indep.getName().equals(condition.getName())) {
						plotable.getConstants().put(indep.getName(), condition.getValue());
					}
				}
			} else {
				VariableRange indepRange = modelCopy.getVariableRanges().get(indep.getName());

				plotable.getVariables().put(indep.getName(), Lists.newArrayList(0.0));
				plotable.getUnits().put(indep.getName(), indep.getUnit());

				if (indepRange != null) {
					plotable.getMinValues().put(indep.getName(), indepRange.getMin());
					plotable.getMaxValues().put(indep.getName(), indepRange.getMax());
				}
			}
		}

		for (Parameter param : modelCopy.getFormula().getParams()) {
			String name = param.getName();
			ParameterValue paramValue = modelCopy.getParamValues().get(name);
			Double value = paramValue != null ? paramValue.getValue() : null;
			Map<String, Double> cov = new LinkedHashMap<>();

			if (paramValue != null) {
				for (Parameter param2 : modelCopy.getFormula().getParams()) {
					cov.put(param2.getName(), paramValue.getCorrelations().get(param2.getName()));
				}
			}

			plotable.getCovariances().put(name, cov);
			plotable.getMinValues().put(name, param.getMin());
			plotable.getMaxValues().put(name, param.getMax());

			if (initParamAsVariable && name.equals(initParam)) {
				plotable.getVariableParameters().put(name, value != null ? value : 0.0);
			} else {
				plotable.getParameters().put(name, value);
			}
		}

		Plotable.Variable varX = new Plotable.Variable(PmmUtils.TIME, plotable.getUnits().get(PmmUtils.TIME),
				Transform.NO_TRANSFORM);
		Plotable.Variable varY = new Plotable.Variable(PmmUtils.CONCENTRATION,
				plotable.getUnits().get(PmmUtils.CONCENTRATION), Transform.NO_TRANSFORM);

		if (dataIndex == null) {
			List<Variable> variables = PmmUtils.getVariables(modelCopy.getFormula());

			for (Variable indep : variables) {
				plotable.getValueLists().put(indep.getName(), new ArrayList<>());
			}

			for (TimeSeries s : modelCopy.getData()) {
				int n = s.getPoints().size();

				for (TimeSeriesPoint p : s.getPoints()) {
					plotable.getValueLists().get(PmmUtils.TIME).add(p.getTime());
					plotable.getValueLists().get(PmmUtils.CONCENTRATION).add(p.getConcentration());
				}

				Map<String, Condition> conditionsByName = PmmUtils.getByName(s.getConditions());

				for (Variable indep : variables) {
					String name = indep.getName();

					if (name.equals(PmmUtils.TIME) || name.equals(PmmUtils.CONCENTRATION)) {
						continue;
					}

					Condition cond = conditionsByName.get(name);

					if (cond != null) {
						plotable.getValueLists().get(name).addAll(Collections.nCopies(n, cond.getValue()));
						plotable.getUnits().put(name, cond.getUnit());
					}
				}
			}

			plotable.getVariables().putAll(plotable.getPossibleVariableValues());
			plotable.computeQualityMeasure(varX, varY, false);
		} else {
			plotable.computeQualityMeasure(varX, varY, true);
			plotable.setGlobalMse(modelCopy.getMse());
			plotable.setGlobalDegreesOfFreedom(modelCopy.getDegreesOfFreedom());
		}

		return plotable;
	}

	public static Map<String, Plotable> readFittedParameters(List<PrimaryModel> models) throws UnitException {
		List<PrimaryModel> modelsCopy = new ArrayList<>(EcoreUtil.copyAll(models));

		for (Model model : modelsCopy) {
			AssignUtils.applyAssignmentsAndConversion(model);
		}

		Map<String, Plotable> plotables = new LinkedHashMap<>();
		List<TimeSeries> data = modelsCopy.stream().map(m -> m.getData()).collect(Collectors.toList());
		PrimaryModelFormula modelFormula = modelsCopy.get(0).getFormula();
		Map<String, List<Double>> arguments = new LinkedHashMap<>();
		Map<String, List<Double>> condValueLists = new LinkedHashMap<>();
		Map<String, PmmUnit> condUnits = PmmUtils.getMostCommonUnits(data);
		Map<String, List<Double>> paramValueLists = new LinkedHashMap<>();

		for (String cond : PmmUtils.getConditions(PmmUtils.getData(modelsCopy))) {
			arguments.put(cond, Lists.newArrayList(0.0));
			condValueLists.put(cond, new ArrayList<>());
		}

		for (Parameter param : modelFormula.getParams()) {
			paramValueLists.put(param.getName(), new ArrayList<>());
		}

		for (PrimaryModel model : modelsCopy) {
			for (Condition condition : model.getData().getConditions()) {
				double value = PmmUtils.convertTo(MathUtils.nullToNan(condition.getValue()), condition.getUnit(),
						condUnits.get(condition.getName()));

				condValueLists.get(condition.getName()).add(Double.isFinite(value) ? value : null);
			}
		}

		for (PrimaryModel model : modelsCopy) {
			for (Parameter param : modelFormula.getParams()) {
				String paramName = param.getName();
				ParameterValue paramValue = model.getParamValues().get(paramName);

				if (paramValue != null) {
					paramValueLists.get(paramName).add(paramValue.getValue());
				}
			}
		}

		for (Parameter param : modelFormula.getParams()) {
			Plotable plotable = new Plotable(Plotable.Type.DATASET_MANY);

			plotable.setFunctionValue(param.getName());
			plotable.getVariables().putAll(arguments);
			plotable.getValueLists().put(param.getName(), paramValueLists.get(param.getName()));
			plotable.getValueLists().putAll(condValueLists);
			plotable.getUnits().putAll(condUnits);

			plotables.put(param.getName(), plotable);
		}

		return plotables;
	}

	private static void addTimeSeriesToPlotable(Plotable plotable, TimeSeries series) {
		plotable.getUnits().put(PmmUtils.TIME, series.getTimeUnit());
		plotable.getUnits().put(PmmUtils.CONCENTRATION, series.getConcentrationUnit());
		plotable.getValueLists().put(PmmUtils.TIME, new ArrayList<>());
		plotable.getValueLists().put(PmmUtils.CONCENTRATION, new ArrayList<>());

		for (TimeSeriesPoint p : series.getPoints()) {
			plotable.getValueLists().get(PmmUtils.TIME).add(p.getTime());
			plotable.getValueLists().get(PmmUtils.CONCENTRATION).add(p.getConcentration());
		}

		for (Condition cond : series.getConditions()) {
			plotable.getUnits().put(cond.getName(), cond.getUnit());
			plotable.getValueLists().put(cond.getName(),
					new ArrayList<>(Collections.nCopies(series.getPoints().size(), cond.getValue())));
		}
	}

	private static void setModelMeasuresToPlotable(Model model, Plotable plotable) {
		plotable.setSse(model.getSse());
		plotable.setMse(model.getMse());
		plotable.setRmse(model.getRmse());
		plotable.setR2(model.getR2());
		plotable.setAic(model.getAic());
		plotable.setDegreesOfFreedom(model.getDegreesOfFreedom());
		plotable.setGlobalMse(model.getMse());
		plotable.setGlobalDegreesOfFreedom(model.getDegreesOfFreedom());
	}
}
