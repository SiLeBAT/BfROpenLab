/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.nfunk.jep.ParseException;

public class ParameterOptimizer {

	private static final double EPSILON = 0.00001;
	private static final int MAX_EVAL = 10000;
	private static final double COV_THRESHOLD = 1e-14;

	private MultivariateVectorFunction optimizerFunction;
	private MultivariateMatrixFunction optimizerFunctionJacobian;

	private String[] parameters;
	private double[] targetValues;

	private boolean successful;
	private Map<String, Double> parameterValues;
	private Map<String, Double> parameterStandardErrors;
	private Map<String, Double> parameterTValues;
	private Map<String, Double> parameterPValues;
	private Map<String, Map<String, Double>> covariances;
	private Double sse;
	private Double mse;
	private Double rmse;
	private Double r2;
	private Double aic;
	private Integer degreesOfFreedom;

	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private List<ProgressListener> progressListeners;

	private ParameterOptimizer(String[] parameters, double[] targetValues) {
		this.parameters = parameters;
		this.targetValues = targetValues;

		successful = false;
		minValues = new LinkedHashMap<>();
		maxValues = new LinkedHashMap<>();
		progressListeners = new ArrayList<>();

		resetResults();
	}

	public ParameterOptimizer(String formula, String[] parameters,
			double[] targetValues, Map<String, double[]> variableValues)
			throws ParseException {
		this(parameters, targetValues);
		optimizerFunction = new VectorFunction(formula, parameters,
				variableValues);
		optimizerFunctionJacobian = new VectorFunctionJacobian(formula,
				parameters, variableValues);

	}

	public ParameterOptimizer(String[] formulas, String[] dependentVariables,
			Double[] initValues, String[] initParameters, String[] parameters,
			double[] timeValues, double[] targetValues,
			String dependentVariable, String timeVariable,
			Map<String, double[]> variableValues, IntegratorFactory integrator)
			throws ParseException {
		this(parameters, targetValues);
		optimizerFunction = new VectorDiffFunction(formulas,
				dependentVariables, initValues, initParameters, parameters,
				variableValues, timeValues, dependentVariable, timeVariable,
				integrator);
		optimizerFunctionJacobian = new VectorDiffFunctionJacobian(formulas,
				dependentVariables, initValues, initParameters, parameters,
				variableValues, timeValues, dependentVariable, timeVariable,
				integrator);
	}

	public Map<String, Double> getMinValues() {
		return minValues;
	}

	public Map<String, Double> getMaxValues() {
		return maxValues;
	}

	public void addProgressListener(ProgressListener listener) {
		progressListeners.add(listener);
	}

	public void removeProgressListener(ProgressListener listener) {
		progressListeners.remove(listener);
	}

	public void optimize(int nParameterSpace, int nLevenberg,
			boolean stopWhenSuccessful, Map<String, Double> minStartValues,
			Map<String, Double> maxStartValues) {
		double[] paramMin = new double[parameters.length];
		int[] paramStepCount = new int[parameters.length];
		double[] paramStepSize = new double[parameters.length];
		int paramsWithRange = 0;
		int maxStepCount = nParameterSpace;

		for (String param : parameters) {
			Double min = minStartValues.get(param);
			Double max = maxStartValues.get(param);

			if (min != null && max != null) {
				paramsWithRange++;
			}
		}

		if (paramsWithRange != 0) {
			maxStepCount = (int) Math.pow(nParameterSpace,
					1.0 / paramsWithRange);
		}

		for (int i = 0; i < parameters.length; i++) {
			Double min = minStartValues.get(parameters[i]);
			Double max = maxStartValues.get(parameters[i]);

			if (min != null && max != null) {
				paramMin[i] = min;
				paramStepCount[i] = maxStepCount;
				paramStepSize[i] = (max - min) / (maxStepCount - 1);
			} else if (min != null) {
				paramMin[i] = min != 0.0 ? min : EPSILON;
				paramStepCount[i] = 1;
				paramStepSize[i] = 1.0;
			} else if (max != null) {
				paramMin[i] = max != 0.0 ? max : -EPSILON;
				paramStepCount[i] = 1;
				paramStepSize[i] = 1.0;
			} else {
				paramMin[i] = EPSILON;
				paramStepCount[i] = 1;
				paramStepSize[i] = 1.0;
			}
		}

		List<double[]> startValueChoices = createStartValueChoices(paramMin,
				paramStepCount, paramStepSize, nLevenberg);

		optimize(startValueChoices, stopWhenSuccessful);
	}

	public boolean isSuccessful() {
		return successful;
	}

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

	public Map<String, Map<String, Double>> getCovariances() {
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

	private void optimize(List<double[]> startValueChoices,
			boolean stopWhenSuccessful) {
		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

		successful = false;

		for (double[] startValues : startValueChoices) {
			try {
				LeastSquaresOptimizer.Optimum optimizerResults = optimizer
						.optimize(createLeastSquaresProblem(startValues));
				double cost = optimizerResults.getCost();

				if (!successful || cost * cost < sse) {
					setResults(optimizerResults);

					if (sse == 0.0) {
						successful = true;
						break;
					}

					if (r2 != null && r2 > 0.0) {
						successful = true;

						if (stopWhenSuccessful) {
							break;
						}
					}
				}
			} catch (TooManyEvaluationsException e) {
				break;
			} catch (ConvergenceException e) {
			}
		}

		if (!successful) {
			resetResults();
		}
	}

	private List<double[]> createStartValueChoices(double[] paramMin,
			int[] paramStepCount, double[] paramStepSize, int n) {
		List<double[]> bestChoices = new ArrayList<>();
		List<Double> bestError = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			double[] v = new double[parameters.length];

			Arrays.fill(v, i + 1.0);
			bestChoices.add(v);
			bestError.add(Double.POSITIVE_INFINITY);
		}

		RealVector targetVector = new ArrayRealVector(targetValues);
		int[] paramStepIndex = new int[parameters.length];
		boolean done = false;
		int allStepSize = 1;
		int count = 0;

		for (int s : paramStepCount) {
			allStepSize *= s;
		}

		Arrays.fill(paramStepIndex, 0);

		while (!done) {
			for (ProgressListener listener : progressListeners) {
				listener.progressChanged((double) count / (double) allStepSize);
			}

			count++;

			double[] values = new double[parameters.length];

			for (int i = 0; i < parameters.length; i++) {
				values[i] = paramMin[i] + paramStepIndex[i] * paramStepSize[i];
			}

			double error = targetVector.getDistance(new ArrayRealVector(
					optimizerFunction.value(values)));

			for (int i = n; i >= 0; i--) {
				if (i == 0 || error >= bestError.get(i - 1)) {
					if (i != n) {
						bestError.add(i, error);
						bestChoices.add(i, values);
						bestError.remove(n);
						bestChoices.remove(n);
					}

					break;
				}
			}

			for (int i = 0;; i++) {
				if (i >= parameters.length) {
					done = true;
					break;
				}

				paramStepIndex[i]++;

				if (paramStepIndex[i] >= paramStepCount[i]) {
					paramStepIndex[i] = 0;
				} else {
					break;
				}
			}
		}

		return bestChoices;
	}

	private LeastSquaresProblem createLeastSquaresProblem(double[] startValues) {
		LeastSquaresBuilder builder = new LeastSquaresBuilder()
				.model(optimizerFunction, optimizerFunctionJacobian)
				.maxEvaluations(MAX_EVAL).maxIterations(MAX_EVAL)
				.target(targetValues).start(startValues);

		if (!minValues.isEmpty() || !maxValues.isEmpty()) {
			builder = builder.parameterValidator(new ParameterValidator() {

				@Override
				public RealVector validate(RealVector params) {
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
				}
			});
		}

		return builder.build();
	}

	private void setResults(LeastSquaresOptimizer.Optimum optimizerResults) {
		resetResults();

		double cost = optimizerResults.getCost();

		sse = cost * cost;
		degreesOfFreedom = targetValues.length - parameters.length;
		r2 = MathUtils.getR2(sse, targetValues);
		aic = MathUtils.getAic(parameters.length, targetValues.length, sse);

		for (int i = 0; i < parameters.length; i++) {
			parameterValues.put(parameters[i], optimizerResults.getPoint()
					.getEntry(i));
		}

		if (degreesOfFreedom <= 0) {
			return;
		}

		mse = sse / degreesOfFreedom;
		rmse = Math.sqrt(mse);

		double[][] covMatrix;

		try {
			covMatrix = optimizerResults.getCovariances(COV_THRESHOLD)
					.getData();
		} catch (SingularMatrixException e) {
			return;
		}

		parameterStandardErrors = new LinkedHashMap<>();
		parameterTValues = new LinkedHashMap<>();
		parameterPValues = new LinkedHashMap<>();
		covariances = new LinkedHashMap<>();

		for (int i = 0; i < parameters.length; i++) {
			double error = Math.sqrt(mse * covMatrix[i][i]);

			parameterStandardErrors.put(parameters[i], error);

			double tValue = optimizerResults.getPoint().getEntry(i) / error;

			parameterTValues.put(parameters[i], tValue);
			parameterPValues.put(parameters[i],
					MathUtils.getPValue(tValue, degreesOfFreedom));
		}

		for (int i = 0; i < parameters.length; i++) {
			Map<String, Double> cov = new LinkedHashMap<>();

			for (int j = 0; j < parameters.length; j++) {
				cov.put(parameters[j], mse * covMatrix[i][j]);
			}

			covariances.put(parameters[i], cov);
		}
	}

	private void resetResults() {
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

		for (String param : parameters) {
			covariances.put(param, new LinkedHashMap<String, Double>());
		}
	}

	public static interface ProgressListener {

		public void progressChanged(double progress);
	}
}
