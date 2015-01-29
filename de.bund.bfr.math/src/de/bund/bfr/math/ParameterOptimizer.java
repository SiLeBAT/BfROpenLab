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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.nfunk.jep.ParseException;

public class ParameterOptimizer {

	private static final double EPSILON = 0.00001;
	private static final int MAX_EVAL = 10000;
	private static final double COV_THRESHOLD = 1e-14;

	private MultivariateVectorFunction optimizerFunction;
	private MultivariateMatrixFunction optimizerFunctionJacobian;

	private String[] parameters;
	private double[] targetValues;

	private LevenbergMarquardtOptimizer optimizer;
	private PointVectorValuePair optimizerValues;

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
	private Integer dof;

	private List<ProgressListener> progressListeners;

	public ParameterOptimizer(String formula, String[] parameters,
			double[] targetValues, Map<String, double[]> variableValues)
			throws ParseException {
		this.parameters = parameters;
		this.targetValues = targetValues;

		optimizerFunction = new VectorFunction(formula, parameters,
				variableValues);
		optimizerFunctionJacobian = new VectorFunctionJacobian(formula,
				parameters, variableValues);
		successful = false;
		resetResults();

		progressListeners = new ArrayList<>();
	}

	public ParameterOptimizer(String[] formulas, String[] dependentVariables,
			Double[] initValues, String[] initParameters, String[] parameters,
			double[] timeValues, double[] targetValues,
			String dependentVariable, String timeVariable,
			Map<String, double[]> variableValues, Integrator integrator)
			throws ParseException {
		this.parameters = parameters;
		this.targetValues = targetValues;

		optimizerFunction = new VectorDiffFunction(formulas,
				dependentVariables, initValues, initParameters, parameters,
				variableValues, timeValues, dependentVariable, timeVariable,
				integrator);
		optimizerFunctionJacobian = new VectorDiffFunctionJacobian(formulas,
				dependentVariables, initValues, initParameters, parameters,
				variableValues, timeValues, dependentVariable, timeVariable,
				integrator);
		successful = false;
		resetResults();

		progressListeners = new ArrayList<>();
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
		List<Double> paramMin = new ArrayList<>();
		List<Integer> paramStepCount = new ArrayList<>();
		List<Double> paramStepSize = new ArrayList<>();
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

		for (String param : parameters) {
			Double min = minStartValues.get(param);
			Double max = maxStartValues.get(param);

			if (min != null && max != null) {
				paramMin.add(min);
				paramStepCount.add(maxStepCount);

				if (max > min) {
					paramStepSize.add((max - min) / (maxStepCount - 1));
				} else {
					paramStepSize.add(1.0);
				}
			} else if (min != null) {
				if (min != 0.0) {
					paramMin.add(min);
				} else {
					paramMin.add(EPSILON);
				}

				paramStepCount.add(1);
				paramStepSize.add(1.0);
			} else if (max != null) {
				if (max != 0.0) {
					paramMin.add(max);
				} else {
					paramMin.add(-EPSILON);
				}

				paramStepCount.add(1);
				paramStepSize.add(1.0);
			} else {
				paramMin.add(EPSILON);
				paramStepCount.add(1);
				paramStepSize.add(1.0);
			}
		}

		List<double[]> bestValues = new ArrayList<>();
		List<Double> bestError = new ArrayList<>();

		for (int i = 0; i < nLevenberg; i++) {
			double[] v = new double[parameters.length];

			Arrays.fill(v, i + 1.0);
			bestValues.add(v);
			bestError.add(Double.POSITIVE_INFINITY);
		}

		List<Integer> paramStepIndex = new ArrayList<>(Collections.nCopies(
				parameters.length, 0));
		boolean done = false;
		int allStepSize = 1;
		int count = 0;

		for (int s : paramStepCount) {
			allStepSize *= s;
		}

		while (!done) {
			for (ProgressListener listener : progressListeners) {
				listener.progressChanged((double) count / (double) allStepSize);
			}

			count++;

			double[] values = new double[parameters.length];

			for (int i = 0; i < parameters.length; i++) {
				values[i] = paramMin.get(i) + paramStepIndex.get(i)
						* paramStepSize.get(i);
			}

			double[] p = optimizerFunction.value(values);
			double error = 0.0;

			for (int i = 0; i < targetValues.length; i++) {
				if (Double.isNaN(p[i])) {
					error = Double.POSITIVE_INFINITY;
					break;
				}

				double diff = targetValues[i] - p[i];

				error += diff * diff;
			}

			for (int i = nLevenberg; i >= 0; i--) {
				if (i == 0 || !(error < bestError.get(i - 1))) {
					if (i != nLevenberg) {
						bestError.add(i, error);
						bestValues.add(i, values);
						bestError.remove(nLevenberg);
						bestValues.remove(nLevenberg);
					}

					break;
				}
			}

			for (int i = 0;; i++) {
				if (i >= parameters.length) {
					done = true;
					break;
				}

				paramStepIndex.set(i, paramStepIndex.get(i) + 1);

				if (paramStepIndex.get(i) >= paramStepCount.get(i)) {
					paramStepIndex.set(i, 0);
				} else {
					break;
				}
			}
		}

		successful = false;

		for (double[] startValues : bestValues) {
			try {
				optimize(startValues);

				if (!successful || optimizer.getChiSquare() < sse) {
					useCurrentResults();

					if (sse == 0.0) {
						successful = true;
						break;
					}

					if (r2 != null && r2 != 0.0) {
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

	public Double getSSE() {
		return sse;
	}

	public Double getMSE() {
		return mse;
	}

	public Double getRMSE() {
		return rmse;
	}

	public Double getR2() {
		return r2;
	}

	public Double getAIC() {
		return aic;
	}

	public Integer getDOF() {
		return dof;
	}

	private void optimize(double[] startValues) {
		double[] weights = new double[targetValues.length];

		Arrays.fill(weights, 1.0);
		optimizer = new LevenbergMarquardtOptimizer();
		optimizerValues = optimizer.optimize(new ModelFunction(
				optimizerFunction), new ModelFunctionJacobian(
				optimizerFunctionJacobian), new MaxEval(MAX_EVAL), new Target(
				targetValues), new Weight(weights), new InitialGuess(
				startValues));
	}

	private void useCurrentResults() {
		resetResults();
		sse = optimizer.getChiSquare();
		dof = targetValues.length - parameters.length;
		r2 = MathUtils.getR2(sse, targetValues);
		aic = MathUtils.getAic(parameters.length, targetValues.length, sse);

		for (int i = 0; i < parameters.length; i++) {
			parameterValues.put(parameters[i], optimizerValues.getPoint()[i]);
		}

		if (dof <= 0) {
			return;
		}

		mse = sse / dof;
		rmse = Math.sqrt(mse);

		double[][] covMatrix;

		try {
			covMatrix = optimizer.computeCovariances(
					optimizerValues.getPoint(), COV_THRESHOLD);
		} catch (SingularMatrixException e) {
			return;
		}

		double factor = optimizer.getChiSquare() / dof;

		parameterStandardErrors = new LinkedHashMap<>();
		parameterTValues = new LinkedHashMap<>();
		parameterPValues = new LinkedHashMap<>();
		covariances = new LinkedHashMap<>();

		for (int i = 0; i < parameters.length; i++) {
			double error = Math.sqrt(factor * covMatrix[i][i]);

			parameterStandardErrors.put(parameters[i], error);

			double tValue = optimizerValues.getPoint()[i] / error;

			parameterTValues.put(parameters[i], tValue);
			parameterPValues.put(parameters[i],
					MathUtils.getPValue(tValue, dof));
		}

		for (int i = 0; i < parameters.length; i++) {
			Map<String, Double> cov = new LinkedHashMap<>();

			for (int j = 0; j < parameters.length; j++) {
				cov.put(parameters[j], factor * covMatrix[i][j]);
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
		dof = null;

		for (String param : parameters) {
			covariances.put(param, new LinkedHashMap<String, Double>());
		}
	}

	public static interface ProgressListener {

		public void progressChanged(double progress);
	}
}
