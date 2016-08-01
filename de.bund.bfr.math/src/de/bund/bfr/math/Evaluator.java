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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Doubles;

public class Evaluator {

	private static Cache<FunctionConf, double[]> results = createCache();
	private static Cache<ErrorFunctionConf, double[]> errorResults = createCache();
	private static Cache<DiffFunctionConf, double[]> diffResults = createCache();
	private static Cache<ErrorDiffFunctionConf, double[]> errorDiffResults = createCache();

	private static <K> Cache<K, double[]> createCache() {
		return CacheBuilder.newBuilder().weigher((K key, double[] value) -> value.length).maximumWeight(1000000)
				.expireAfterAccess(1, TimeUnit.MINUTES).build();
	}

	public static double[] getFunctionPoints(Map<String, Double> parserConstants, String formula, String varX,
			double[] valuesX) throws ParseException {
		FunctionConf function = new FunctionConf(parserConstants, formula, varX, valuesX);
		double[] result = results.getIfPresent(function);

		if (result != null) {
			return result;
		}

		Parser parser = new Parser();

		parserConstants.forEach((constant, value) -> parser.addConstant(constant, value));
		parser.addVariable(varX);

		Node f = parser.parse(formula);
		double[] valuesY = new double[valuesX.length];

		Arrays.fill(valuesY, Double.NaN);

		for (int i = 0; i < valuesX.length; i++) {
			parser.setVarValue(varX, valuesX[i]);
			valuesY[i] = parser.evaluate(f);
		}

		results.put(function, valuesY);

		return valuesY;
	}

	public static double[] getFunctionErrors(Map<String, Double> parserConstants, String formula, String varX,
			double[] valuesX, Map<String, Map<String, Double>> covariances, double extraVariance, int degreesOfFreedom)
			throws ParseException {
		ErrorFunctionConf function = new ErrorFunctionConf(parserConstants, formula, varX, valuesX, covariances,
				extraVariance, degreesOfFreedom);
		double[] result = errorResults.getIfPresent(function);

		if (result != null) {
			return result;
		}

		Parser parser = new Parser();

		parserConstants.forEach((constant, value) -> parser.addConstant(constant, value));
		parser.addVariable(varX);

		List<String> paramList = new ArrayList<>(covariances.keySet());
		Node f = parser.parse(formula);
		Map<String, Node> derivatives = new LinkedHashMap<>();

		for (String param : paramList) {
			derivatives.put(param, parser.differentiate(f, param));
		}

		double[] valuesY = new double[valuesX.length];
		double conf95 = MathUtils.get95PercentConfidence(degreesOfFreedom);

		Arrays.fill(valuesY, Double.NaN);

		loop: for (int index = 0; index < valuesX.length; index++) {
			parser.setVarValue(varX, valuesX[index]);

			double variance = 0.0;
			int n = paramList.size();
			double[] derivValues = new double[n];

			for (int i = 0; i < n; i++) {
				String param = paramList.get(i);

				derivValues[i] = parser.evaluate(derivatives.get(param));
				variance += derivValues[i] * derivValues[i] * covariances.get(param).get(param);

				if (!Double.isFinite(variance)) {
					continue loop;
				}
			}

			for (int i = 0; i < n - 1; i++) {
				for (int j = i + 1; j < n; j++) {
					variance += 2.0 * derivValues[i] * derivValues[j]
							* covariances.get(paramList.get(i)).get(paramList.get(j));

					if (!Double.isFinite(variance)) {
						continue loop;
					}
				}
			}

			valuesY[index] = Math.sqrt(variance + extraVariance) * conf95;
		}

		errorResults.put(function, valuesY);

		return valuesY;
	}

	public static double[] getDiffPoints(Map<String, Double> parserConstants, Map<String, String> functions,
			Map<String, Double> initValues, Map<String, String> initParameters, Map<String, double[]> conditionLists,
			String dependentVariable, Map<String, Double> independentVariables, String varX, double[] valuesX,
			IntegratorFactory integrator, InterpolationFactory interpolator) throws ParseException {
		DiffFunctionConf function = new DiffFunctionConf(parserConstants, functions, initValues, initParameters,
				conditionLists, dependentVariable, independentVariables, varX, valuesX, integrator, interpolator);
		double[] result = diffResults.getIfPresent(function);

		if (result != null) {
			return result;
		}

		Node[] fs = new Node[functions.size()];
		String[] valueVariables = new String[functions.size()];
		double[] values = new double[functions.size()];
		int depIndex = -1;
		int index = 0;
		Parser parser = new Parser();

		parserConstants.forEach((constant, value) -> parser.addConstant(constant, value));
		parser.addVariable(dependentVariable);
		independentVariables.keySet().forEach(var -> parser.addVariable(var));

		for (Map.Entry<String, String> entry : functions.entrySet()) {
			String var = entry.getKey();

			fs[index] = parser.parse(entry.getValue());
			valueVariables[index] = var;

			if (initValues.containsKey(var)) {
				values[index] = initValues.get(var);
			} else {
				values[index] = parserConstants.get(initParameters.get(var));
			}

			if (var.equals(dependentVariable)) {
				depIndex = index;
			}

			index++;
		}

		double[] valuesY = new double[valuesX.length];
		DiffFunction f = new DiffFunction(parser, fs, valueVariables, conditionLists, varX, interpolator);
		FirstOrderIntegrator instance = integrator.createIntegrator();
		double diffValue = conditionLists.get(varX)[0];

		for (int i = 0; i < valuesX.length; i++) {
			if (valuesX[i] == diffValue) {
				valuesY[i] = values[depIndex];
			} else if (valuesX[i] > diffValue) {
				instance.integrate(f, diffValue, values, valuesX[i], values);
				diffValue = valuesX[i];
				valuesY[i] = values[depIndex];
			} else {
				valuesY[i] = Double.NaN;
			}
		}

		diffResults.put(function, valuesY);

		return valuesY;
	}

	public static double[] getDiffErrors(Map<String, Double> parserConstants, Map<String, String> functions,
			Map<String, Double> initValues, Map<String, String> initParameters, Map<String, double[]> conditionLists,
			String dependentVariable, Map<String, Double> independentVariables, String varX, double[] valuesX,
			IntegratorFactory integrator, InterpolationFactory interpolator,
			Map<String, Map<String, Double>> covariances, double extraVariance, int degreesOfFreedom)
			throws ParseException {
		ErrorDiffFunctionConf function = new ErrorDiffFunctionConf(parserConstants, functions, initValues,
				initParameters, conditionLists, dependentVariable, independentVariables, varX, valuesX, integrator,
				interpolator, covariances, extraVariance, degreesOfFreedom);
		double[] result = errorDiffResults.getIfPresent(function);

		if (result != null) {
			return result;
		}

		List<String> paramList = new ArrayList<>(covariances.keySet());
		Map<String, double[]> derivValues = new LinkedHashMap<>();

		paramList.parallelStream().forEach(param -> {
			Map<String, Double> constantsMinus = new LinkedHashMap<>(parserConstants);
			Map<String, Double> constantsPlus = new LinkedHashMap<>(parserConstants);
			double value = parserConstants.get(param);

			constantsMinus.put(param, value - MathUtils.DERIV_EPSILON);
			constantsPlus.put(param, value + MathUtils.DERIV_EPSILON);

			double[] deriv = new double[valuesX.length];

			try {
				double[] valuesMinus = getDiffPoints(constantsMinus, functions, initValues, initParameters,
						conditionLists, dependentVariable, independentVariables, varX, valuesX, integrator,
						interpolator);
				double[] valuesPlus = getDiffPoints(constantsPlus, functions, initValues, initParameters,
						conditionLists, dependentVariable, independentVariables, varX, valuesX, integrator,
						interpolator);

				for (int i = 0; i < valuesX.length; i++) {
					deriv[i] = (valuesPlus[i] - valuesMinus[i]) / (2 * MathUtils.DERIV_EPSILON);
				}
			} catch (ParseException e) {
				Arrays.fill(deriv, Double.NaN);
			}

			derivValues.put(param, deriv);
		});

		double[] valuesY = new double[valuesX.length];
		double conf95 = MathUtils.get95PercentConfidence(degreesOfFreedom);

		Arrays.fill(valuesY, Double.NaN);

		loop: for (int index = 0; index < valuesX.length; index++) {
			double variance = 0.0;
			int n = paramList.size();

			for (int i = 0; i < n; i++) {
				String param = paramList.get(i);
				double value = derivValues.get(param)[index];

				variance += value * value * covariances.get(param).get(param);

				if (!Double.isFinite(variance)) {
					continue loop;
				}
			}

			for (int i = 0; i < n - 1; i++) {
				for (int j = i + 1; j < n; j++) {
					String param1 = paramList.get(i);
					String param2 = paramList.get(j);

					variance += 2.0 * derivValues.get(param1)[index] * derivValues.get(param2)[index]
							* covariances.get(param1).get(param2);

					if (!Double.isFinite(variance)) {
						continue loop;
					}
				}
			}

			valuesY[index] = Math.sqrt(variance + extraVariance) * conf95;
		}

		errorDiffResults.put(function, valuesY);

		return valuesY;
	}

	private static class FunctionConf {

		private Map<String, Double> parserConstants;
		private String formula;
		private String varX;
		private double[] valuesX;

		public FunctionConf(Map<String, Double> parserConstants, String formula, String varX, double[] valuesX) {
			this.parserConstants = parserConstants;
			this.formula = formula;
			this.varX = varX;
			this.valuesX = valuesX;
		}

		@Override
		public int hashCode() {
			return Objects.hash(parserConstants, formula, varX, Arrays.hashCode(valuesX));
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			FunctionConf other = (FunctionConf) obj;

			return Objects.equals(parserConstants, other.parserConstants) && Objects.equals(formula, other.formula)
					&& Objects.equals(varX, other.varX) && Arrays.equals(valuesX, other.valuesX);
		}
	}

	private static class ErrorFunctionConf extends FunctionConf {

		private Map<String, Map<String, Double>> covariances;
		private double extraVariance;
		private int degreesOfFreedom;

		public ErrorFunctionConf(Map<String, Double> parserConstants, String formula, String varX, double[] valuesX,
				Map<String, Map<String, Double>> covariances, double extraVariance, int degreesOfFreedom) {
			super(parserConstants, formula, varX, valuesX);
			this.covariances = covariances;
			this.extraVariance = extraVariance;
			this.degreesOfFreedom = degreesOfFreedom;
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), covariances, extraVariance, degreesOfFreedom);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			ErrorFunctionConf other = (ErrorFunctionConf) obj;

			return super.equals(other) && Objects.equals(covariances, other.covariances)
					&& extraVariance == other.extraVariance && degreesOfFreedom == other.degreesOfFreedom;
		}
	}

	private static class DiffFunctionConf {

		private Map<String, Double> parserConstants;
		private Map<String, String> functions;
		private Map<String, Double> initValues;
		private Map<String, String> initParameters;
		private Map<String, double[]> conditionLists;
		private String dependentVariable;
		private Map<String, Double> independentVariables;
		private String varX;
		private double[] valuesX;
		private IntegratorFactory integrator;
		private InterpolationFactory interpolator;

		public DiffFunctionConf(Map<String, Double> parserConstants, Map<String, String> functions,
				Map<String, Double> initValues, Map<String, String> initParameters,
				Map<String, double[]> conditionLists, String dependentVariable,
				Map<String, Double> independentVariables, String varX, double[] valuesX, IntegratorFactory integrator,
				InterpolationFactory interpolator) {
			this.parserConstants = parserConstants;
			this.functions = functions;
			this.initValues = initValues;
			this.initParameters = initParameters;
			this.conditionLists = conditionLists;
			this.dependentVariable = dependentVariable;
			this.independentVariables = independentVariables;
			this.varX = varX;
			this.valuesX = valuesX;
			this.integrator = integrator;
			this.interpolator = interpolator;
		}

		@Override
		public int hashCode() {
			return Objects.hash(parserConstants, functions, initValues, initParameters, convert(conditionLists),
					dependentVariable, independentVariables, varX, Arrays.hashCode(valuesX), integrator, interpolator);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			DiffFunctionConf other = (DiffFunctionConf) obj;

			return Objects.equals(parserConstants, other.parserConstants) && Objects.equals(functions, other.functions)
					&& Objects.equals(initValues, other.initValues)
					&& Objects.equals(initParameters, other.initParameters)
					&& Objects.equals(convert(conditionLists), convert(other.conditionLists))
					&& Objects.equals(dependentVariable, other.dependentVariable)
					&& Objects.equals(independentVariables, other.independentVariables)
					&& Objects.equals(varX, other.varX) && Arrays.equals(valuesX, other.valuesX)
					&& Objects.equals(integrator, other.integrator) && Objects.equals(interpolator, other.interpolator);
		}

		private static Map<String, List<Double>> convert(Map<String, double[]> map) {
			Map<String, List<Double>> converted = new LinkedHashMap<>();

			map.forEach((key, value) -> converted.put(key, Doubles.asList(value)));

			return converted;
		}
	}

	private static class ErrorDiffFunctionConf extends DiffFunctionConf {

		private Map<String, Map<String, Double>> covariances;
		private double extraVariance;
		private int degreesOfFreedom;

		public ErrorDiffFunctionConf(Map<String, Double> parserConstants, Map<String, String> functions,
				Map<String, Double> initValues, Map<String, String> initParameters,
				Map<String, double[]> conditionLists, String dependentVariable,
				Map<String, Double> independentVariables, String varX, double[] valuesX, IntegratorFactory integrator,
				InterpolationFactory interpolator, Map<String, Map<String, Double>> covariances, double extraVariance,
				int degreesOfFreedom) {
			super(parserConstants, functions, initValues, initParameters, conditionLists, dependentVariable,
					independentVariables, varX, valuesX, integrator, interpolator);
			this.covariances = covariances;
			this.extraVariance = extraVariance;
			this.degreesOfFreedom = degreesOfFreedom;
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), covariances, extraVariance, degreesOfFreedom);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			ErrorDiffFunctionConf other = (ErrorDiffFunctionConf) obj;

			return super.equals(other) && Objects.equals(covariances, other.covariances)
					&& extraVariance == other.extraVariance && degreesOfFreedom == other.degreesOfFreedom;
		}
	}
}
