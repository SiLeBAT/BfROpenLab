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
package de.bund.bfr.knime.pmmlite.util.fitting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.knime.core.node.CanceledExecutionException;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.AssignUtils;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.models.VariableRange;
import de.bund.bfr.math.LeastSquaresOptimization;
import de.bund.bfr.math.Transform;

public class SecondaryEstimationThread extends EstimationThread<SecondaryModel> {

	private List<String> warnings;

	public SecondaryEstimationThread(SecondaryModel dataModel, Map<String, Double> minStartValues,
			Map<String, Double> maxStartValues, boolean enforceLimits, int nParameterSpace, int nLevenberg,
			boolean stopWhenSuccessful, AtomicInteger finishedThreads) {
		super(dataModel, minStartValues, maxStartValues, enforceLimits, nParameterSpace, nLevenberg, stopWhenSuccessful,
				finishedThreads);
		warnings = new ArrayList<>();
	}

	@Override
	protected void estimate() throws ParseException, UnitException {
		SecondaryModel newDataModel = EcoreUtil.copy(dataModel);

		AssignUtils.applyAssignmentsAndConversion(newDataModel);

		List<Double> targetValues = new ArrayList<>();
		Map<String, List<Double>> argumentValues = new LinkedHashMap<>();

		for (String param : PmmUtils.getNames(newDataModel.getFormula().getIndepVars())) {
			argumentValues.put(param, new ArrayList<>());
		}

		loop: for (PrimaryModel data : newDataModel.getData()) {
			Map<String, Double> values = new LinkedHashMap<>();
			Parameter depVar = PmmUtils.getByName(data.getFormula().getParams())
					.get(newDataModel.getFormula().getDepVar().getName());
			Double value = data.getParamValues().get(depVar.getName()).getValue();

			if (value == null) {
				continue;
			}

			Map<String, Condition> conditionsByName = PmmUtils.getByName(data.getData().getConditions());

			for (String arg : argumentValues.keySet()) {
				Condition cond = conditionsByName.get(arg);

				if (cond == null || cond.getValue() == null) {
					continue loop;
				}

				values.put(arg, cond.getValue());
			}

			if ((depVar.getMin() != null && value < depVar.getMin())
					|| (depVar.getMax() != null && value > depVar.getMax())) {
				warnings.add("Some primary parameters are out of their range of values");
			}

			Transform trans = newDataModel.getFormula().getTransformation();

			targetValues.add(trans.to(value));

			for (Map.Entry<String, List<Double>> entry : argumentValues.entrySet()) {
				entry.getValue().add(values.get(entry.getKey()));
			}
		}

		List<Parameter> parameters = newDataModel.getFormula().getParams();
		String formula = newDataModel.getFormula().getExpression();
		LeastSquaresOptimization optimizer = LeastSquaresOptimization.createVectorOptimizer(formula,
				PmmUtils.getNames(parameters), targetValues, argumentValues);

		if (enforceLimits) {
			optimizer.getMinValues().putAll(PmmUtils.getMinValues(parameters));
			optimizer.getMaxValues().putAll(PmmUtils.getMaxValues(parameters));
		}

		try {
			LeastSquaresOptimization.Result result = optimizer.optimize(nParameterSpace, nLevenberg, stopWhenSuccessful,
					minStartValues, maxStartValues, FittingConstants.MAX_LEVENBERG_ITERATIONS, progress -> {
					}, null);

			for (Map.Entry<String, List<Double>> entry : argumentValues.entrySet()) {
				VariableRange indepRange = ModelsFactory.eINSTANCE.createVariableRange();

				if (!entry.getValue().isEmpty()) {
					indepRange.setMin(Collections.min(entry.getValue()));
					indepRange.setMax(Collections.max(entry.getValue()));
				}

				dataModel.getVariableRanges().put(entry.getKey(), indepRange);
			}

			for (Parameter param : dataModel.getFormula().getParams()) {
				dataModel.getParamValues().put(param.getName(), PmmUtils.createParameterValue(param.getName(), result));
			}

			dataModel.setSse(result.getSse());
			dataModel.setMse(result.getMse());
			dataModel.setRmse(result.getRmse());
			dataModel.setR2(result.getR2());
			dataModel.setAic(result.getAic());
			dataModel.setDegreesOfFreedom(result.getDegreesOfFreedom());
			PmmUtils.setId(dataModel);
		} catch (CanceledExecutionException e) {
		}
	}

	public List<String> getWarnings() {
		return warnings;
	}
}
