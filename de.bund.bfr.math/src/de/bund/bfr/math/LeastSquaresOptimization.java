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
import org.nfunk.jep.ParseException;

import com.google.common.primitives.Doubles;

import de.bund.bfr.math.MathUtils.ParamRange;
import de.bund.bfr.math.MathUtils.StartValues;

public class LeastSquaresOptimization implements Optimization {

	private static final double COV_THRESHOLD = 1e-14;

	private ValueAndJacobianFunction optimizerFunction;

	private String[] parameters;
	private double[] targetValues;

	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private LeastSquaresOptimization(String[] parameters, double[] targetValues) {
		this.parameters = parameters;
		this.targetValues = targetValues;

		minValues = new LinkedHashMap<>();
		maxValues = new LinkedHashMap<>();
	}

	public LeastSquaresOptimization(String formula, String[] parameters, double[] targetValues,
			Map<String, double[]> variableValues) throws ParseException {
		this(parameters, targetValues);
		optimizerFunction = new VectorFunction(formula, parameters, variableValues);
	}

	public LeastSquaresOptimization(String[] formulas, String[] dependentVariables, double[] initValues,
			String[] initParameters, String[] parameters, double[] timeValues, double[] targetValues,
			String dependentVariable, String timeVariable, Map<String, double[]> variableValues,
			IntegratorFactory integrator, InterpolationFactory interpolator) throws ParseException {
		this(parameters, targetValues);
		optimizerFunction = new VectorDiffFunction(formulas, dependentVariables, initValues, initParameters, parameters,
				variableValues, timeValues, dependentVariable, timeVariable, integrator, interpolator);
	}

	public LeastSquaresOptimization(String[] formulas, String[] dependentVariables, double[] initValues,
			List<String[]> initParameters, String[] parameters, List<double[]> timeValues, List<double[]> targetValues,
			String dependentVariable, String timeVariable, Map<String, List<double[]>> variableValues,
			IntegratorFactory integrator, InterpolationFactory interpolator) throws ParseException {
		this(parameters, Doubles.toArray(
				targetValues.stream().map(v -> Doubles.asList(v)).flatMap(List::stream).collect(Collectors.toList())));
		optimizerFunction = new MultiVectorDiffFunction(formulas, dependentVariables, initValues, initParameters,
				parameters, variableValues, timeValues, dependentVariable, timeVariable, integrator, interpolator);
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
			DoubleConsumer progressListener) {
		progressListener.accept(0.0);

		ParamRange[] ranges = MathUtils.getParamRanges(parameters, minStartValues, maxStartValues, nParameterSpace);
		RealVector targetVector = new ArrayRealVector(targetValues);
		List<StartValues> startValuesList = MathUtils.createStartValuesList(ranges, nOptimizations,
				values -> targetVector.getDistance(new ArrayRealVector(optimizerFunction.value(values))),
				progress -> progressListener.accept(0.5 * progress));
		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
		Result result = getResults();
		AtomicInteger count = new AtomicInteger(0);

		for (StartValues startValues : startValuesList) {
			progressListener.accept(0.5 * count.get() / startValuesList.size() + 0.5);

			try {
				LeastSquaresBuilder builder = createLeastSquaresBuilder(startValues.getValues(), maxIterations);

				builder.checker((iteration, previous, current) -> {
					double currentProgress = (double) iteration / (double) maxIterations;

					progressListener.accept(0.5 * (count.get() + currentProgress) / startValuesList.size() + 0.5);
					return iteration == maxIterations;
				});

				LeastSquaresOptimizer.Optimum optimizerResults = optimizer.optimize(builder.build());
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

	private LeastSquaresBuilder createLeastSquaresBuilder(double[] startValues, int maxIterations) {
		LeastSquaresBuilder builder = new LeastSquaresBuilder()
				.model(optimizerFunction, optimizerFunction.createJacobian()).maxEvaluations(Integer.MAX_VALUE)
				.maxIterations(maxIterations).target(targetValues).start(startValues);

		if (!minValues.isEmpty() || !maxValues.isEmpty()) {
			builder.parameterValidator(params -> {

				for (int i = 0; i < parameters.length; i++) {
					double value = params.getEntry(i);
					Double min = minValues.get(parameters[i]);
					Double max = maxValues.get(parameters[i]);

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

	private Result getResults() {
		Result r = new Result();

		r.parameterValues = new LinkedHashMap<>();
		r.parameterStandardErrors = new LinkedHashMap<>();
		r.parameterTValues = new LinkedHashMap<>();
		r.parameterPValues = new LinkedHashMap<>();
		r.covariances = new LinkedHashMap<>();
		r.sse = null;
		r.mse = null;
		r.rmse = null;
		r.r2 = null;
		r.aic = null;
		r.degreesOfFreedom = null;

		return r;
	}

	private Result getResults(LeastSquaresOptimizer.Optimum optimizerResults) {
		Result r = getResults();
		double cost = optimizerResults.getCost();

		r.sse = cost * cost;
		r.degreesOfFreedom = targetValues.length - parameters.length;
		r.r2 = MathUtils.getR2(r.sse, targetValues);
		r.aic = MathUtils.getAic(parameters.length, targetValues.length, r.sse);

		for (int i = 0; i < parameters.length; i++) {
			r.parameterValues.put(parameters[i], optimizerResults.getPoint().getEntry(i));
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

		for (int i = 0; i < parameters.length; i++) {
			double error = Math.sqrt(r.mse * covMatrix[i][i]);

			r.parameterStandardErrors.put(parameters[i], error);

			double tValue = optimizerResults.getPoint().getEntry(i) / error;

			r.parameterTValues.put(parameters[i], tValue);
			r.parameterPValues.put(parameters[i], MathUtils.getPValue(tValue, r.degreesOfFreedom));
		}

		for (int i = 0; i < parameters.length; i++) {
			for (int j = 0; j < parameters.length; j++) {
				r.covariances.put(new Pair<>(parameters[i], parameters[j]), r.mse * covMatrix[i][j]);
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
