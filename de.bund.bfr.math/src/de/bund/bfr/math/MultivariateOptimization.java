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
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.primitives.Doubles;

import de.bund.bfr.math.MathUtils.ParamRange;
import de.bund.bfr.math.MathUtils.StartValues;

public class MultivariateOptimization implements Optimization {

	private MultivariateFunction optimizerFunction;
	private List<String> parameters;
	private String sdParam;

	private MultivariateOptimization(List<String> parameters, String sdParam, MultivariateFunction optimizerFunction) {
		this.parameters = parameters;
		this.sdParam = sdParam;
		this.optimizerFunction = optimizerFunction;
	}

	public static MultivariateOptimization createLodOptimizer(String formula, List<String> parameters,
			List<Double> targetValues, Map<String, List<Double>> variableValues, double levelOfDetection)
			throws ParseException {
		String sdParam = parameters.stream().collect(Collectors.joining());
		List<String> params = Stream.concat(parameters.stream(), Stream.of(sdParam)).collect(Collectors.toList());

		return new MultivariateOptimization(params, sdParam,
				new LodFunction(formula, params, variableValues, targetValues, levelOfDetection, sdParam));
	}

	@Override
	public Result optimize(int nParameterSpace, int nOptimizations, boolean stopWhenSuccessful,
			Map<String, Double> minStartValues, Map<String, Double> maxStartValues, int maxIterations,
			DoubleConsumer progessListener, ExecutionContext exec) throws CanceledExecutionException {
		if (exec != null) {
			exec.checkCanceled();
		}

		progessListener.accept(0.0);

		List<ParamRange> ranges = MathUtils.getParamRanges(parameters, minStartValues, maxStartValues, nParameterSpace);

		ranges.set(parameters.indexOf(sdParam), new ParamRange(1.0, 1, 1.0));

		List<StartValues> startValuesList = MathUtils.createStartValuesList(ranges, nOptimizations,
				values -> optimizerFunction.value(Doubles.toArray(values)),
				progress -> progessListener.accept(0.5 * progress), exec);
		Result result = new Result();
		AtomicInteger currentIteration = new AtomicInteger();
		SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-10, 1e-10) {

			@Override
			public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
				if (super.converged(iteration, previous, current)) {
					return true;
				}

				return currentIteration.incrementAndGet() >= maxIterations;
			}
		});
		int count = 0;

		for (StartValues startValues : startValuesList) {
			if (exec != null) {
				exec.checkCanceled();
			}

			progessListener.accept(0.5 * count++ / startValuesList.size() + 0.5);

			try {
				PointValuePair optimizerResults = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
						new MaxIter(maxIterations), new InitialGuess(Doubles.toArray(startValues.getValues())),
						new ObjectiveFunction(optimizerFunction), GoalType.MAXIMIZE,
						new NelderMeadSimplex(parameters.size()));
				double logLikelihood = optimizerResults.getValue() != null ? optimizerResults.getValue() : Double.NaN;

				if (result.logLikelihood == null || logLikelihood > result.logLikelihood) {
					result = getResults(optimizerResults);

					if (result.logLikelihood == 0.0 || stopWhenSuccessful) {
						break;
					}
				}
			} catch (TooManyEvaluationsException | TooManyIterationsException | ConvergenceException e) {
			}
		}

		return result;
	}

	private Result getResults(PointValuePair optimizerResults) {
		Result r = new Result();

		r.logLikelihood = optimizerResults.getValue();

		for (int i = 0; i < parameters.size(); i++) {
			if (parameters.get(i).equals(sdParam)) {
				r.sdValue = optimizerResults.getPoint()[i];
			} else {
				r.parameterValues.put(parameters.get(i), optimizerResults.getPoint()[i]);
			}
		}

		return r;
	}

	public static class Result implements OptimizationResult {

		private Map<String, Double> parameterValues;
		private Double sdValue;
		private Double logLikelihood;

		public Result() {
			parameterValues = new LinkedHashMap<>();
			sdValue = null;
			logLikelihood = null;
		}

		@Override
		public Map<String, Double> getParameterValues() {
			return parameterValues;
		}

		public Double getSdValue() {
			return sdValue;
		}

		public Double getLogLikelihood() {
			return logLikelihood;
		}

		@Override
		public Result copy() {
			Result r = new Result();

			r.parameterValues = new LinkedHashMap<>(parameterValues);
			r.sdValue = sdValue;
			r.logLikelihood = logLikelihood;

			return r;
		}
	}
}
