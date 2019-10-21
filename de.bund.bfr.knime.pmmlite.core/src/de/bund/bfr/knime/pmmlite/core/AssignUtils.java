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
package de.bund.bfr.knime.pmmlite.core;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil;

import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint;
import de.bund.bfr.knime.pmmlite.core.models.Model;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.math.MathUtils;

public class AssignUtils {

	private AssignUtils() {
	}

	public static void applyAssignmentsAndConversion(Model model) throws UnitException {
		if (model instanceof PrimaryModel) {
			apply((PrimaryModel) model);
		} else if (model instanceof SecondaryModel) {
			apply((SecondaryModel) model);
		} else if (model instanceof TertiaryModel) {
			apply((TertiaryModel) model);
		}
	}

	private static void apply(PrimaryModel model) throws UnitException {
		model.setFormula(EcoreUtil.copy(model.getFormula()));
		model.setData(EcoreUtil.copy(model.getData()));

		Variable depVar = model.getFormula().getDepVar();
		Variable indepVar = model.getFormula().getIndepVar();

		depVar.setName(model.getAssignments().get(depVar.getName()));

		String newName = model.getAssignments().get(indepVar.getName());

		if (!indepVar.getName().equals(newName)) {
			model.getFormula().setExpression(
					MathUtils.replaceVariable(model.getFormula().getExpression(), indepVar.getName(), newName));
			indepVar.setName(newName);
		}

		for (TimeSeriesPoint p : model.getData().getPoints()) {
			p.setConcentration(
					PmmUtils.convertTo(p.getConcentration(), model.getData().getConcentrationUnit(), depVar.getUnit()));
		}

		for (TimeSeriesPoint p : model.getData().getPoints()) {
			p.setTime(PmmUtils.convertTo(p.getTime(), model.getData().getTimeUnit(), indepVar.getUnit()));
		}

		model.getData().setConcentrationUnit(depVar.getUnit());
		model.getData().setTimeUnit(indepVar.getUnit());
	}

	private static void apply(SecondaryModel model) throws UnitException {
		Collection<PrimaryModel> mem = EcoreUtil.copyAll(model.getData());

		model.setFormula(EcoreUtil.copy(model.getFormula()));
		model.getData().clear();
		model.getData().addAll(mem);

		for (PrimaryModel data : model.getData()) {
			apply(data);
		}

		Variable depVar = model.getFormula().getDepVar();

		depVar.setName(model.getAssignments().get(depVar.getName()));

		for (Variable indep : model.getFormula().getIndepVars()) {
			String newName = model.getAssignments().get(indep.getName());

			if (!newName.equals(indep.getName())) {
				model.getFormula().setExpression(
						MathUtils.replaceVariable(model.getFormula().getExpression(), indep.getName(), newName));
				indep.setName(newName);
			}
		}

		Map<String, PmmUnit> unassignedConditionUnits = PmmUtils.getMostCommonUnits(PmmUtils.getData(model.getData()));

		for (PrimaryModel m : model.getData()) {
			Map<String, Condition> conditionsByName = PmmUtils.getByName(m.getData().getConditions());

			for (Variable indep : model.getFormula().getIndepVars()) {
				Condition cond = conditionsByName.remove(indep.getName());

				cond.setValue(
						PmmUtils.convertTo(MathUtils.nullToNan(cond.getValue()), cond.getUnit(), indep.getUnit()));
				cond.setUnit(indep.getUnit());
			}

			for (Condition cond : conditionsByName.values()) {
				PmmUnit unit = unassignedConditionUnits.get(cond.getName());

				cond.setValue(PmmUtils.convertTo(MathUtils.nullToNan(cond.getValue()), cond.getUnit(), unit));
				cond.setUnit(unit);
			}
		}
	}

	private static void apply(TertiaryModel model) throws UnitException {
		Collection<TimeSeries> mem = EcoreUtil.copyAll(model.getData());

		model.setFormula(EcoreUtil.copy(model.getFormula()));
		model.getData().clear();
		model.getData().addAll(mem);

		Variable depVar = model.getFormula().getDepVar();

		depVar.setName(model.getAssignments().get(depVar.getName()));

		for (Variable indep : model.getFormula().getIndepVars()) {
			String newName = model.getAssignments().get(indep.getName());

			if (!newName.equals(indep.getName())) {
				model.getFormula().setExpression(
						MathUtils.replaceVariable(model.getFormula().getExpression(), indep.getName(), newName));
				indep.setName(newName);
			}
		}

		Variable timeVar = PmmUtils.getByName(model.getFormula().getIndepVars()).get(PmmUtils.TIME);
		Map<String, PmmUnit> unassignedConditionUnits = PmmUtils.getMostCommonUnits(model.getData());

		for (TimeSeries data : model.getData()) {
			for (TimeSeriesPoint p : data.getPoints()) {
				p.setConcentration(
						PmmUtils.convertTo(p.getConcentration(), data.getConcentrationUnit(), depVar.getUnit()));
			}

			for (TimeSeriesPoint p : data.getPoints()) {
				p.setTime(PmmUtils.convertTo(p.getTime(), data.getTimeUnit(), timeVar.getUnit()));
			}

			data.setConcentrationUnit(depVar.getUnit());
			data.setTimeUnit(timeVar.getUnit());

			Map<String, Condition> conditionsByName = PmmUtils.getByName(data.getConditions());

			for (Variable indep : model.getFormula().getIndepVars()) {
				Condition cond = conditionsByName.remove(indep.getName());

				if (cond != null) {
					cond.setValue(
							PmmUtils.convertTo(MathUtils.nullToNan(cond.getValue()), cond.getUnit(), indep.getUnit()));
					cond.setUnit(indep.getUnit());
				}
			}

			for (Condition cond : conditionsByName.values()) {
				PmmUnit unit = unassignedConditionUnits.get(cond.getName());

				cond.setValue(PmmUtils.convertTo(MathUtils.nullToNan(cond.getValue()), cond.getUnit(), unit));
				cond.setUnit(unit);
			}
		}
	}
}
