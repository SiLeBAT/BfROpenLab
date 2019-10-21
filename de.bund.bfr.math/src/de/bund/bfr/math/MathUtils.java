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
package de.bund.bfr.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleConsumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.math.DoubleMath;

public class MathUtils {

	public static final double DERIV_EPSILON = 1e-6;

	private MathUtils() {
	}

	public static double nullToNan(Double d) {
		return d != null ? d : Double.NaN;
	}

	public static String replaceVariable(String formula, String var, String newVar) {
		if (var.equals(newVar)) {
			return formula;
		}

		String newFormular = " " + formula + " ";
		boolean foundReplacement = true;

		while (foundReplacement) {
			foundReplacement = false;

			for (int i = 1; i < newFormular.length() - var.length(); i++) {
				boolean matches = newFormular.substring(i, i + var.length()).equals(var);
				boolean start = !isVariableCharacter(newFormular.charAt(i - 1));
				boolean end = !isVariableCharacter(newFormular.charAt(i + var.length()));

				if (matches && start && end) {
					String orginal = newFormular.substring(i - 1, i + var.length() + 1);
					String replacement = newFormular.charAt(i - 1) + newVar + newFormular.charAt(i + var.length());

					newFormular = newFormular.replace(orginal, replacement);
					foundReplacement = true;
					break;
				}
			}
		}

		return newFormular.replace(" ", "");
	}

	public static boolean isVariableCharacter(char ch) {
		return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$';
	}

	public static Set<String> getSymbols(Collection<String> terms) {
		return terms.stream().map(t -> MathUtils.getSymbols(t)).flatMap(Set::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static Set<String> getSymbols(String formula) {
		Set<String> symbols = new LinkedHashSet<>();

		try {
			getSymbols(ASTNode.parseFormula(formula), symbols);
		} catch (ParseException | NullPointerException e) {
		}

		return symbols;
	}

	private static void getSymbols(ASTNode math, Set<String> symbols) {
		if (math.isName()) {
			symbols.add(math.getName());
		}

		math.getChildren().forEach(c -> getSymbols(c, symbols));
	}

	public static Double getR2(double sse, List<Double> targetValues) {
		if (targetValues.size() < 2) {
			return null;
		}

		double targetMean = DoubleMath.mean(targetValues);
		double targetTotalSumOfSquares = targetValues.stream().mapToDouble(v -> Math.pow(v - targetMean, 2.0)).sum();
		double rSquared = 1 - sse / targetTotalSumOfSquares;

		return Math.max(rSquared, 0.0);
	}

	public static Double getAic(int numParam, int numSample, double sse) {
		if (numSample <= numParam + 2) {
			return null;
		}

		return numSample * Math.log(sse / numSample) + 2.0 * (numParam + 1.0)
				+ 2.0 * (numParam + 1.0) * (numParam + 2.0) / (numSample - numParam - 2.0);
	}

	public static double getPValue(double tValue, int degreesOfFreedom) {
		TDistribution dist = new TDistribution(degreesOfFreedom);

		return 1.0 - dist.probability(-Math.abs(tValue), Math.abs(tValue));
	}

	public static double get95PercentConfidence(int degreesOfFreedom) {
		TDistribution dist = new TDistribution(degreesOfFreedom);

		return dist.inverseCumulativeProbability(1.0 - 0.05 / 2.0);
	}

	public static double[][] aproxJacobianParallel(List<? extends MultivariateVectorFunction> functions, double[] point,
			int nResult) {
		double[][] result = new double[nResult][functions.size()];

		IntStream.range(0, functions.size()).parallel().forEach(i -> {
			double[] p = point.clone();

			p[i] = point[i] - DERIV_EPSILON;

			double[] result1 = functions.get(i).value(p);

			p[i] = point[i] + DERIV_EPSILON;

			double[] result2 = functions.get(i).value(p);

			for (int j = 0; j < nResult; j++) {
				result[j][i] = (result2[j] - result1[j]) / (2 * DERIV_EPSILON);
			}
		});

		return result;
	}

	public static List<ParamRange> getParamRanges(List<String> parameters, Map<String, Double> minStartValues,
			Map<String, Double> maxStartValues, int n) {
		final double EPSILON = DERIV_EPSILON * 10.0;
		List<ParamRange> paramRanges = new ArrayList<>();
		int paramsWithRange = 0;
		int maxStepCount = n;

		for (String param : parameters) {
			if (minStartValues.get(param) != null && maxStartValues.get(param) != null) {
				paramsWithRange++;
			}
		}

		if (paramsWithRange != 0) {
			maxStepCount = (int) Math.pow(n, 1.0 / paramsWithRange);
		}

		for (String param : parameters) {
			Double min = minStartValues.get(param);
			Double max = maxStartValues.get(param);

			if (min != null && max != null) {
				paramRanges.add(new ParamRange(min, maxStepCount, (max - min) / (maxStepCount - 1)));
			} else if (min != null) {
				paramRanges.add(new ParamRange(min != 0.0 ? min : EPSILON, 1, 1.0));
			} else if (max != null) {
				paramRanges.add(new ParamRange(max != 0.0 ? max : -EPSILON, 1, 1.0));
			} else {
				paramRanges.add(new ParamRange(EPSILON, 1, 1.0));
			}
		}

		return paramRanges;
	}

	public static List<StartValues> createStartValuesList(List<ParamRange> ranges, int n,
			ToDoubleFunction<List<Double>> errorFunction, DoubleConsumer progessListener, ExecutionContext exec)
			throws CanceledExecutionException {
		List<StartValues> valuesList = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			valuesList.add(new StartValues(Collections.nCopies(ranges.size(), i + 1.0), Double.POSITIVE_INFINITY));
		}

		List<Integer> paramStepIndex = new ArrayList<>(Collections.nCopies(ranges.size(), 0));
		boolean done = false;
		int allStepSize = 1;
		int count = 0;

		for (ParamRange range : ranges) {
			allStepSize *= range.getStepCount();
		}

		while (!done) {
			List<Double> values = new ArrayList<>();

			for (int i = 0; i < ranges.size(); i++) {
				values.add(ranges.get(i).getMin() + paramStepIndex.get(i) * ranges.get(i).getStepSize());
			}

			double error = errorFunction.applyAsDouble(values);

			if (Double.isFinite(error) || error < valuesList.get(n - 1).getError()) {
				for (int i = 0; i < n; i++) {
					if (error < valuesList.get(i).getError()) {
						valuesList.add(i, new StartValues(values, error));
						valuesList.remove(n);
						break;
					}
				}
			}

			for (int i = 0;; i++) {
				if (i >= ranges.size()) {
					done = true;
					break;
				}

				paramStepIndex.set(i, paramStepIndex.get(i) + 1);

				if (paramStepIndex.get(i) >= ranges.get(i).getStepCount()) {
					paramStepIndex.set(i, 0);
				} else {
					break;
				}
			}

			if (exec != null) {
				exec.checkCanceled();
			}

			progessListener.accept((double) ++count / (double) allStepSize);
		}

		return valuesList;
	}

	public static FirstOrderDifferentialEquations createDiffEquations(Parser parser, List<ASTNode> functions,
			List<String> dependentVariables, String timeVariable, Map<String, UnivariateFunction> variableFunctions) {
		return new FirstOrderDifferentialEquations() {

			@Override
			public int getDimension() {
				return functions.size();
			}

			@Override
			public void computeDerivatives(double t, double[] y, double[] yDot)
					throws MaxCountExceededException, DimensionMismatchException {
				parser.setVarValue(timeVariable, t);
				variableFunctions.forEach((var, function) -> parser.setVarValue(var, function.value(t)));

				for (int i = 0; i < functions.size(); i++) {
					parser.setVarValue(dependentVariables.get(i), y[i]);
				}

				for (int i = 0; i < functions.size(); i++) {
					try {
						double value = parser.evaluate(functions.get(i));

						yDot[i] = Double.isFinite(value) ? value : Double.NaN;
					} catch (ParseException e) {
						e.printStackTrace();
						yDot[i] = Double.NaN;
					}
				}
			}
		};
	}

	public static Map<String, UnivariateFunction> createInterpolationFunctions(Map<String, List<Double>> variableValues,
			String timeVariable, InterpolationFactory interpolator) {
		Map<String, UnivariateFunction> variableFunctions = new LinkedHashMap<>();

		variableValues.forEach((var, values) -> {
			if (!var.equals(timeVariable)) {
				variableFunctions.put(var,
						interpolator.createInterpolationFunction(variableValues.get(timeVariable), values));
			}
		});

		return variableFunctions;
	}

	public static class ParamRange {

		private double min;
		private int stepCount;
		private double stepSize;

		public ParamRange(double min, int stepCount, double stepSize) {
			this.min = min;
			this.stepCount = stepCount;
			this.stepSize = stepSize;
		}

		public double getMin() {
			return min;
		}

		public int getStepCount() {
			return stepCount;
		}

		public double getStepSize() {
			return stepSize;
		}
	}

	public static class StartValues {

		private List<Double> values;
		private double error;

		public StartValues(List<Double> values, double error) {
			this.values = values;
			this.error = error;
		}

		public List<Double> getValues() {
			return values;
		}

		public double getError() {
			return error;
		}
	}
}
