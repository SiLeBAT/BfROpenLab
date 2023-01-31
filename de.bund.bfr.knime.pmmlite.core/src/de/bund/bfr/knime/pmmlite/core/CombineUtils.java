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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.Model;
import de.bund.bfr.knime.pmmlite.core.models.ModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Renamings;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.core.models.VariableRange;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.math.Transform;

public class CombineUtils {

	private CombineUtils() {
	}

	public static List<TertiaryModel> combine(List<SecondaryModel> dataModels) throws UnitException {
		// 1. find secondary models and data for each primary model
		Map<String, PrimaryModel> primModelMap = new LinkedHashMap<>();
		Map<String, Map<String, Double>> paramMeanMap = new LinkedHashMap<>();
		ListMultimap<String, SecondaryModel> secModelMap = ArrayListMultimap.create();
		ListMultimap<String, TimeSeries> dataMap = ArrayListMultimap.create();

		for (SecondaryModel dataModel : dataModels) {
			PrimaryModel primModel = dataModel.getData().get(0);
			String id = primModel.getFormula().getId();

			if (!primModelMap.containsKey(id)) {
				primModelMap.put(id, primModel);

				ListMultimap<String, Double> paramValues = ArrayListMultimap.create();

				for (PrimaryModel primData : dataModel.getData()) {
					dataMap.put(id, primData.getData());

					for (Parameter param : primModel.getFormula().getParams()) {
						ParameterValue value = primData.getParamValues().get(param.getName());

						if (value != null && value.getValue() != null) {
							paramValues.put(param.getName(), value.getValue());
						}
					}
				}

				Map<String, Double> paramMeans = new LinkedHashMap<>();

				for (Map.Entry<String, Collection<Double>> entry : paramValues.asMap().entrySet()) {
					paramMeans.put(entry.getKey(), DoubleMath.mean(Doubles.toArray(entry.getValue())));
				}

				paramMeanMap.put(id, paramMeans);
			}

			secModelMap.put(id, dataModel);
			paramMeanMap.get(id).remove(dataModel.getAssignments().get(dataModel.getFormula().getDepVar().getName()));
		}

		// 2. if secondary does not exist for a parameter create constant model
		// 3. call combine for each primary models with its secondary models
		List<TertiaryModel> tertiaryModels = new ArrayList<>();

		for (Map.Entry<String, PrimaryModel> entry : primModelMap.entrySet()) {
			String id = entry.getKey();
			PrimaryModel primModel = entry.getValue();
			List<SecondaryModel> secModels = secModelMap.get(id);
			List<Model> allModels = new ArrayList<>();

			allModels.add(primModel);
			allModels.addAll(secModels);

			TertiaryModel tertModel = ModelsFactory.eINSTANCE.createTertiaryModel();

			tertModel.setId(Joiner.on("").join(PmmUtils.getIds(allModels)));
			tertModel.setName(Joiner.on("_").join(PmmUtils.getNames(secModels)));

			for (Map.Entry<String, VariableRange> range : primModel.getVariableRanges().entrySet()) {
				tertModel.getVariableRanges().put(range.getKey(), EcoreUtil.copy(range.getValue()));
			}

			for (Map.Entry<String, ParameterValue> value : primModel.getParamValues().entrySet()) {
				tertModel.getParamValues().put(value.getKey(), EcoreUtil.copy(value.getValue()));
			}

			for (SecondaryModel secModel : secModels) {
				String depVarAssignment = secModel.getAssignments().get(secModel.getFormula().getDepVar().getName());

				tertModel.getParamValues().remove(depVarAssignment);
			}

			List<SecondaryModelFormula> secFormulas = new ArrayList<>();
			Map<String, String> assignments = new LinkedHashMap<>();
			Map<String, Renamings> secondaryRenamings = new LinkedHashMap<>();

			for (SecondaryModel secModel : secModels) {
				SecondaryModelFormula secFormula = secModel.getFormula();
				String depVarAssignment = secModel.getAssignments().get(secFormula.getDepVar().getName());
				Renamings renamings = ModelsFactory.eINSTANCE.createRenamings();

				for (Map.Entry<String, String> assignment : secModel.getAssignments().entrySet()) {
					if (!assignment.getKey().equals(secFormula.getDepVar().getName())) {
						renamings.getMap().put(assignment.getKey(), assignment.getValue());
						tertModel.getAssignments().put(assignment.getValue(), assignment.getValue());
					}
				}

				secFormulas.add(secFormula);
				assignments.put(depVarAssignment, secFormula.getId());
				secondaryRenamings.put(depVarAssignment, renamings);

				for (Variable var : secFormula.getIndepVars()) {
					VariableRange range = secModel.getVariableRanges()
							.get(secModel.getAssignments().get(var.getName()));

					addIndep(tertModel, renamings.getMap().get(var.getName()), EcoreUtil.copy(range));
				}

				for (Map.Entry<String, ParameterValue> value : secModel.getParamValues().entrySet()) {
					addParam(tertModel, value.getKey(), EcoreUtil.copy(value.getValue()));
				}
			}

			for (Parameter param : primModel.getFormula().getParams()) {
				if (!assignments.containsKey(param.getName())) {
					ParameterValue value = ModelsFactory.eINSTANCE.createParameterValue();

					value.setValue(paramMeanMap.get(primModel.getFormula().getId()).get(param.getName()));
					tertModel.getParamValues().put(param.getName(), value);
				}
			}

			for (ParameterValue value : tertModel.getParamValues().values()) {
				value.setError(null);
				value.setP(null);
				value.setT(null);
				value.getCorrelations().clear();
			}

			tertModel.setFormula(combine(primModel.getFormula(), secFormulas, assignments, secondaryRenamings));
			tertModel.getAssignments().putAll(primModel.getAssignments());
			tertModel.getData().addAll(dataMap.get(id));
			tertiaryModels.add(tertModel);
		}

		return tertiaryModels;
	}

	public static TertiaryModelFormula combine(PrimaryModelFormula primFormula, List<SecondaryModelFormula> secFormulas,
			Map<String, String> assignments, Map<String, Renamings> secondaryRenamings) throws UnitException {
		TertiaryModelFormula formula = ModelsFactory.eINSTANCE.createTertiaryModelFormula();
		List<ModelFormula> allModels = new ArrayList<>();

		allModels.add(primFormula);
		allModels.addAll(secFormulas);

		formula.setName(primFormula.getName() + " (Tertiary)");
		formula.setId(Joiner.on("").join(PmmUtils.getIds(allModels)));
		formula.setPrimaryFormula(primFormula);
		formula.getSecondaryFormulas().addAll(secFormulas);
		formula.getAssignments().putAll(assignments);
		formula.setExpression(primFormula.getExpression());
		formula.setDepVar(EcoreUtil.copy(primFormula.getDepVar()));
		formula.getIndepVars().add(EcoreUtil.copy(primFormula.getIndepVar()));
		formula.getParams().addAll(EcoreUtil.copyAll(primFormula.getParams()));
		formula.setTimeVar(primFormula.getIndepVar().getName());
		formula.setInitialParam(primFormula.getInitialParam());

		Map<String, SecondaryModelFormula> secFormulasById = PmmUtils.getById(secFormulas);
		Map<String, Parameter> paramsByName = PmmUtils.getByName(formula.getParams());

		for (Map.Entry<String, String> entry : assignments.entrySet()) {
			String varName = entry.getKey();
			SecondaryModelFormula secFormula = secFormulasById.get(entry.getValue());
			String f = secFormula.getExpression();
			Renamings renamings = secondaryRenamings != null ? secondaryRenamings.get(varName) : null;

			if (renamings == null) {
				renamings = ModelsFactory.eINSTANCE.createRenamings();
			} else {
				formula.getSecondaryRenamings().put(varName, renamings);
			}

			if (varName.equals(formula.getInitialParam())) {
				formula.setInitialParam(null);
			}

			formula.getParams().remove(paramsByName.get(varName));

			for (Variable indep : secFormula.getIndepVars()) {
				Variable renamedIndep = EcoreUtil.copy(indep);

				if (renamings.getMap().containsKey(indep.getName())) {
					renamedIndep.setName(renamings.getMap().get(indep.getName()));
					f = MathUtils.replaceVariable(f, indep.getName(), renamedIndep.getName());
				}

				if (!addIndep(formula, renamedIndep)) {
					throw new UnitException(renamedIndep.getName() + " has different units in secondary models");
				}
			}

			for (Parameter param : secFormula.getParams()) {
				String newName = addParam(formula, EcoreUtil.copy(param));

				f = MathUtils.replaceVariable(f, param.getName(), newName);
			}

			Transform transform = secFormula.getTransformation();

			if (!f.equals(varName) || transform != Transform.NO_TRANSFORM) {
				formula.setExpression(MathUtils.replaceVariable(formula.getExpression(), varName, transform.from(f)));
			}
		}

		return formula;
	}

	private static boolean addIndep(TertiaryModelFormula formula, Variable indep) {
		Map<String, Variable> variablesByName = PmmUtils.getByName(formula.getIndepVars());

		if (variablesByName.containsKey(indep.getName())) {
			Variable oldIndep = variablesByName.get(indep.getName());

			if (!oldIndep.getUnit().equals(indep.getUnit())) {
				return false;
			}
		} else {
			formula.getIndepVars().add(indep);
		}

		return true;
	}

	private static void addIndep(TertiaryModel model, String indepName, VariableRange range) {
		VariableRange oldRange = model.getVariableRanges().get(indepName);

		if (oldRange != null) {
			if (oldRange.getMin() == null) {
				oldRange.setMin(range.getMin());
			} else if (range.getMin() != null) {
				oldRange.setMin(Math.max(oldRange.getMin(), range.getMin()));
			}

			if (oldRange.getMax() == null) {
				oldRange.setMax(range.getMax());
			} else if (range.getMax() != null) {
				oldRange.setMax(Math.min(oldRange.getMax(), range.getMax()));
			}
		} else {
			model.getVariableRanges().put(indepName, range);
		}
	}

	private static String addParam(TertiaryModelFormula formula, Parameter param) {
		Set<String> paramNames = new LinkedHashSet<>(PmmUtils.getNames(formula.getParams()));
		String newName = getNewParamName(param.getName(), paramNames);

		param.setName(newName);
		formula.getParams().add(param);

		return newName;
	}

	private static String addParam(TertiaryModel model, String paramName, ParameterValue value) {
		Set<String> paramNames = model.getParamValues().keySet();
		String newName = getNewParamName(paramName, paramNames);

		if (value != null) {
			model.getParamValues().put(newName, value);
		}

		return paramName;
	}

	private static String getNewParamName(String name, Set<String> usedNames) {
		String newName = name;
		int i = 2;

		while (usedNames.contains(newName)) {
			newName = name + i;
			i++;
		}

		return newName;
	}
}
