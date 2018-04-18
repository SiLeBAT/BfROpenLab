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
package de.bund.bfr.knime.pmmlite.util.fitting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.knime.core.node.CanceledExecutionException;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.AssignUtils;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.core.models.VariableRange;
import de.bund.bfr.math.LeastSquaresOptimization;

public class TertiaryEstimationThread extends EstimationThread<TertiaryModel> {

	public TertiaryEstimationThread(TertiaryModel dataModel, Map<String, Double> minStartValues,
			Map<String, Double> maxStartValues, boolean enforceLimits, int nParameterSpace, int nLevenberg,
			boolean stopWhenSuccessful, AtomicInteger finishedThreads) {
		super(dataModel, minStartValues, maxStartValues, enforceLimits, nParameterSpace, nLevenberg, stopWhenSuccessful,
				finishedThreads);
	}

	@Override
	protected void estimate() throws ParseException, UnitException {
		TertiaryModel newDataModel = EcoreUtil.copy(dataModel);

		AssignUtils.applyAssignmentsAndConversion(newDataModel);

		List<Double> targetValues = new ArrayList<>();
		Map<String, List<Double>> argumentValues = new LinkedHashMap<>();

		for (Variable indep : newDataModel.getFormula().getIndepVars()) {
			argumentValues.put(indep.getName(), new ArrayList<>());
		}

		loop: for (TimeSeries series : newDataModel.getData()) {
			Map<String, Double> values = new LinkedHashMap<>();

			for (String indepName : argumentValues.keySet()) {
				if (!indepName.equals(PmmUtils.TIME)) {
					Optional<Condition> cond = series.getConditions().stream()
							.filter(c -> c.getName().equals(indepName)).findAny();
					Double value = cond.isPresent() ? cond.get().getValue() : null;

					if (value == null || !Double.isFinite(value)) {
						continue loop;
					}

					values.put(indepName, value);
				}
			}

			int n = 0;

			for (TimeSeriesPoint p : series.getPoints()) {
				if (Double.isFinite(p.getTime()) && Double.isFinite(p.getConcentration())) {
					argumentValues.get(PmmUtils.TIME).add(p.getTime());
					targetValues.add(p.getConcentration());
					n++;
				}
			}

			for (Map.Entry<String, List<Double>> entry : argumentValues.entrySet()) {
				if (!entry.getKey().equals(PmmUtils.TIME)) {
					entry.getValue().addAll(Collections.nCopies(n, values.get(entry.getKey())));
				}
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
}
