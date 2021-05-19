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
package de.bund.bfr.math;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleConsumer;
import java.util.stream.Collectors;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Pair;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.primitives.Doubles;

import de.bund.bfr.math.MathUtils.ParamRange;
import de.bund.bfr.math.MathUtils.StartValues;

public class LeastSquaresOptimization implements Optimization {

	private static final double COV_THRESHOLD = 1e-14;

	private ValueAndJacobianFunction optimizerFunction;

	private List<String> parameters;
	private List<Double> targetValues;

	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private LeastSquaresOptimization(List<String> parameters, List<Double> targetValues,
			ValueAndJacobianFunction optimizerFunction) {
		this.parameters = parameters;
		this.targetValues = targetValues;
		this.optimizerFunction = optimizerFunction;

		minValues = new LinkedHashMap<>();
		maxValues = new LinkedHashMap<>();
	}

	public static LeastSquaresOptimization createVectorOptimizer(String formula, List<String> parameters,
			List<Double> targetValues, Map<String, List<Double>> variableValues) throws ParseException {
		return new LeastSquaresOptimization(parameters, targetValues,
				new VectorFunction(formula, parameters, variableValues));
	}

	public static LeastSquaresOptimization createVectorDiffOptimizer(List<String> formulas,
			List<String> dependentVariables, List<Double> initValues, List<String> initParameters,
			List<String> parameters, List<Double> timeValues, List<Double> targetValues, String dependentVariable,
			String timeVariable, Map<String, List<Double>> variableValues, IntegratorFactory integrator,
			InterpolationFactory interpolator) throws ParseException {
		return new LeastSquaresOptimization(parameters, targetValues,
				new VectorDiffFunction(formulas, dependentVariables, initValues, initParameters, parameters,
						variableValues, timeValues, dependentVariable, timeVariable, integrator, interpolator));
	}

	public static LeastSquaresOptimization createMultiVectorDiffOptimizer(List<String> formulas,
			List<String> dependentVariables, List<Double> initValues, List<List<String>> initParameters,
			List<String> parameters, List<List<Double>> timeValues, List<List<Double>> targetValues,
			String dependentVariable, String timeVariable, List<Map<String, List<Double>>> variableValues,
			IntegratorFactory integrator, InterpolationFactory interpolator) throws ParseException {
		return new LeastSquaresOptimization(parameters,
				targetValues.stream().flatMap(List::stream).collect(Collectors.toList()),
				new MultiVectorDiffFunction(formulas, dependentVariables, initValues, initParameters, parameters,
						variableValues, timeValues, dependentVariable, timeVariable, integrator, interpolator));
	}

	public Map<String, Double> getMinValues() {
		return minValues;
	}

	public Map<String, Double> getMaxValues() {
		return maxValues;
	}

	@Override
	public Result optimize(int nParameterSpace, int nOptimizations, boolean stopWhenSuccessful,
			Map<String, Double> minStartValues, Map<String, Double> maxStartValues, int maxIterations,
			DoubleConsumer progressListener, ExecutionContext exec) throws CanceledExecutionException {
		if (exec != null) {
			exec.checkCanceled();
		}

		progressListener.accept(0.0);

		List<ParamRange> ranges = MathUtils.getParamRanges(parameters, minStartValues, maxStartValues, nParameterSpace);
		RealVector targetVector = new ArrayRealVector(Doubles.toArray(targetValues));
		List<StartValues> startValuesList = MathUtils.createStartValuesList(ranges, nOptimizations,
				values -> targetVector
						.getDistance(new ArrayRealVector(optimizerFunction.value(Doubles.toArray(values)))),
				progress -> progressListener.accept(0.5 * progress), exec);
		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
		Result result = new Result();
		AtomicInteger count = new AtomicInteger(0);

		for (StartValues startValues : startValuesList) {
			if (exec != null) {
				exec.checkCanceled();
			}

			progressListener.accept(0.5 * count.get() / startValuesList.size() + 0.5);

			try {
				LeastSquaresBuilder builder = createLeastSquaresBuilder(startValues.getValues(), maxIterations);

				builder.checker((iteration, previous, current) -> {
					double currentProgress = (double) iteration / (double) maxIterations;

					if (exec != null) {
						try {
							exec.checkCanceled();
						} catch (CanceledExecutionException e) {
							return true;
						}
					}

					progressListener.accept(0.5 * (count.get() + currentProgress) / startValuesList.size() + 0.5);
					return iteration == maxIterations;
				});

				LeastSquaresOptimizer.Optimum optimizerResults = optimizer.optimize(builder.build());

				if (exec != null) {
					exec.checkCanceled();
				}

				double cost = optimizerResults.getCost();

				if (result.sse == null || cost * cost < result.sse) {
					result = getResults(optimizerResults);

					if (result.sse == 0.0) {
						break;
					}

					if (result.r2 != null && result.r2 > 0.0 && stopWhenSuccessful) {
						break;
					}
				}
			} catch (TooManyEvaluationsException | TooManyIterationsException | ConvergenceException e) {
			}

			count.incrementAndGet();
		}

		return result;
	}

	private LeastSquaresBuilder createLeastSquaresBuilder(List<Double> startValues, int maxIterations) {
		LeastSquaresBuilder builder = new LeastSquaresBuilder()
				.model(optimizerFunction, optimizerFunction.createJacobian()).maxEvaluations(Integer.MAX_VALUE)
				.maxIterations(maxIterations).target(Doubles.toArray(targetValues)).start(Doubles.toArray(startValues));

		if (!minValues.isEmpty() || !maxValues.isEmpty()) {
			builder.parameterValidator(params -> {

				for (int i = 0; i < parameters.size(); i++) {
					double value = params.getEntry(i);
					Double min = minValues.get(parameters.get(i));
					Double max = maxValues.get(parameters.get(i));

					if (min != null && value < min) {
						value = min;
					}

					if (max != null && value > max) {
						value = max;
					}

					params.setEntry(i, value);
				}

				return params;
			});
		}

		return builder;
	}

	private Result getResults(LeastSquaresOptimizer.Optimum optimizerResults) {
		Result r = new Result();
		double cost = optimizerResults.getCost();

		r.sse = cost * cost;
		r.degreesOfFreedom = targetValues.size() - parameters.size();
		r.r2 = MathUtils.getR2(r.sse, targetValues);
		r.aic = MathUtils.getAic(parameters.size(), targetValues.size(), r.sse);

		for (int i = 0; i < parameters.size(); i++) {
			r.parameterValues.put(parameters.get(i), optimizerResults.getPoint().getEntry(i));
		}

		if (r.degreesOfFreedom <= 0) {
			return r;
		}

		r.mse = r.sse / r.degreesOfFreedom;
		r.rmse = Math.sqrt(r.mse);

		double[][] covMatrix;

		try {
			covMatrix = optimizerResults.getCovariances(COV_THRESHOLD).getData();
		} catch (SingularMatrixException e) {
			return r;
		}

		r.parameterStandardErrors = new LinkedHashMap<>();
		r.parameterTValues = new LinkedHashMap<>();
		r.parameterPValues = new LinkedHashMap<>();
		r.covariances = new LinkedHashMap<>();

		for (int i = 0; i < parameters.size(); i++) {
			double error = Math.sqrt(r.mse * covMatrix[i][i]);

			r.parameterStandardErrors.put(parameters.get(i), error);

			double tValue = optimizerResults.getPoint().getEntry(i) / error;

			r.parameterTValues.put(parameters.get(i), tValue);
			r.parameterPValues.put(parameters.get(i), MathUtils.getPValue(tValue, r.degreesOfFreedom));
		}

		for (int i = 0; i < parameters.size(); i++) {
			for (int j = 0; j < parameters.size(); j++) {
				r.covariances.put(new Pair<>(parameters.get(i), parameters.get(j)), r.mse * covMatrix[i][j]);
			}
		}

		return r;
	}

	public static class Result implements OptimizationResult {

		private Map<String, Double> parameterValues;
		private Map<String, Double> parameterStandardErrors;
		private Map<String, Double> parameterTValues;
		private Map<String, Double> parameterPValues;
		private Map<Pair<String, String>, Double> covariances;
		private Double sse;
		private Double mse;
		private Double rmse;
		private Double r2;
		private Double aic;
		private Integer degreesOfFreedom;

		public Result() {
			parameterValues = new LinkedHashMap<>();
			parameterStandardErrors = new LinkedHashMap<>();
			parameterTValues = new LinkedHashMap<>();
			parameterPValues = new LinkedHashMap<>();
			covariances = new LinkedHashMap<>();
			sse = null;
			mse = null;
			rmse = null;
			r2 = null;
			aic = null;
			degreesOfFreedom = null;
		}

		@Override
		public Map<String, Double> getParameterValues() {
			return parameterValues;
		}

		public Map<String, Double> getParameterStandardErrors() {
			return parameterStandardErrors;
		}

		public Map<String, Double> getParameterTValues() {
			return parameterTValues;
		}

		public Map<String, Double> getParameterPValues() {
			return parameterPValues;
		}

		public Map<Pair<String, String>, Double> getCovariances() {
			return covariances;
		}

		public Double getSse() {
			return sse;
		}

		public Double getMse() {
			return mse;
		}

		public Double getRmse() {
			return rmse;
		}

		public Double getR2() {
			return r2;
		}

		public Double getAic() {
			return aic;
		}

		public Integer getDegreesOfFreedom() {
			return degreesOfFreedom;
		}

		@Override
		public Result copy() {
			Result r = new Result();

			r.parameterValues = new LinkedHashMap<>(parameterValues);
			r.parameterStandardErrors = new LinkedHashMap<>(parameterStandardErrors);
			r.parameterTValues = new LinkedHashMap<>(parameterTValues);
			r.parameterPValues = new LinkedHashMap<>(parameterPValues);
			r.covariances = new LinkedHashMap<>();
			covariances.forEach((key, value) -> r.covariances.put(new Pair<>(key), value));

			r.sse = sse;
			r.mse = mse;
			r.rmse = rmse;
			r.r2 = r2;
			r.aic = aic;
			r.degreesOfFreedom = degreesOfFreedom;

			return r;
		}
	}
}
