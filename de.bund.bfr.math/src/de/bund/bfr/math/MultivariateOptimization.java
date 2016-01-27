/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.nfunk.jep.ParseException;

public class MultivariateOptimization {

	private static final double EPSILON = 0.00001;

	private MultivariateFunction optimizerFunction;

	private String[] parameters;

	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private List<ProgressListener> progressListeners;

	private String sdParam;

	public MultivariateOptimization(String formula, String[] parameters, double[] targetValues,
			Map<String, double[]> variableValues, double levelOfDetection) throws ParseException {
		sdParam = Stream.of(parameters).collect(Collectors.joining());
		this.parameters = Stream.concat(Stream.of(parameters), Stream.of(sdParam)).toArray(String[]::new);
		optimizerFunction = new LodFunction(formula, this.parameters, variableValues, targetValues, levelOfDetection,
				sdParam);
		minValues = new LinkedHashMap<>();
		maxValues = new LinkedHashMap<>();
		progressListeners = new ArrayList<>();
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

	public Result optimize(int nParameterSpace, int nLevenberg, boolean stopWhenSuccessful,
			Map<String, Double> minStartValues, Map<String, Double> maxStartValues, int maxIterations) {
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
			maxStepCount = (int) Math.pow(nParameterSpace, 1.0 / paramsWithRange);
		}

		for (int i = 0; i < parameters.length; i++) {
			Double min = minStartValues.get(parameters[i]);
			Double max = maxStartValues.get(parameters[i]);

			if (parameters[i].equals(sdParam)) {
				paramMin[i] = 1.0;
				paramStepCount[i] = 1;
				paramStepSize[i] = 1.0;
			} else if (min != null && max != null) {
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

		List<StartValues> startValuesList = createStartValuesList(paramMin, paramStepCount, paramStepSize, nLevenberg);

		return optimize(startValuesList, stopWhenSuccessful, maxIterations);
	}

	private Result optimize(final List<StartValues> startValuesList, final boolean stopWhenSuccessful,
			final int maxIterations) {
		Result result = null;
		SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
		final AtomicInteger count = new AtomicInteger(0);

		for (StartValues startValues : startValuesList) {
			fireProgressChanged(0.5 * count.get() / startValuesList.size() + 0.5);

			try {
				PointValuePair optimizerResults = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
						new MaxIter(maxIterations), new InitialGuess(startValues.getValues()),
						new ObjectiveFunction(optimizerFunction), GoalType.MAXIMIZE,
						new NelderMeadSimplex(parameters.length));
				double error = optimizerResults.getValue() != null ? optimizerResults.getValue() : Double.NaN;

				if (result == null || error < result.error) {
					result = getResults(optimizerResults);

					if (result.error == 0.0 || stopWhenSuccessful) {
						break;
					}
				}
			} catch (TooManyEvaluationsException | TooManyIterationsException | ConvergenceException e) {
				System.out.println(e.getMessage());
			}

			count.incrementAndGet();
		}

		return result != null ? result : getResults();
	}

	private List<StartValues> createStartValuesList(double[] paramMin, int[] paramStepCount, double[] paramStepSize,
			int n) {
		List<StartValues> valuesList = new ArrayList<>();
		int[] paramStepIndex = new int[parameters.length];
		boolean done = false;
		int allStepSize = 1;
		int count = 0;

		for (int s : paramStepCount) {
			allStepSize *= s;
		}

		Arrays.fill(paramStepIndex, 0);

		while (!done) {
			fireProgressChanged(0.5 * count / allStepSize);

			double[] values = new double[parameters.length];

			for (int i = 0; i < parameters.length; i++) {
				values[i] = paramMin[i] + paramStepIndex[i] * paramStepSize[i];
			}

			double error = optimizerFunction.value(values);

			if (Double.isFinite(error)) {
				valuesList.add(new StartValues(values, error));
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

			fireProgressChanged(0.5 * count / allStepSize);
			count++;
		}

		Collections.sort(valuesList, (o1, o2) -> Double.compare(o1.getError(), o2.getError()));

		return valuesList.size() > n ? valuesList.subList(0, n) : valuesList;
	}

	private Result getResults() {
		Result r = new Result();

		r.parameterValues = new LinkedHashMap<>();
		r.error = null;

		return r;
	}

	private Result getResults(PointValuePair optimizerResults) {
		Result r = getResults();

		r.error = optimizerResults.getValue() != null ? optimizerResults.getValue() : Double.NaN;

		for (int i = 0; i < parameters.length; i++) {
			if (!parameters[i].equals(sdParam)) {
				r.parameterValues.put(parameters[i], optimizerResults.getPoint()[i]);
			}
		}

		return r;
	}

	private void fireProgressChanged(double progress) {
		progressListeners.forEach(l -> l.progressChanged(progress));
	}

	private static class StartValues {

		private double[] values;
		private double error;

		public StartValues(double[] values, double error) {
			this.values = values;
			this.error = error;
		}

		public double[] getValues() {
			return values;
		}

		public double getError() {
			return error;
		}
	}

	public static class Result implements OptimizationResult {

		private Map<String, Double> parameterValues;
		private Double error;

		@Override
		public Map<String, Double> getParameterValues() {
			return parameterValues;
		}

		public Double getError() {
			return error;
		}

		public Result copy() {
			Result r = new Result();

			r.parameterValues = new LinkedHashMap<>(parameterValues);
			r.error = error;

			return r;
		}
	}
}
