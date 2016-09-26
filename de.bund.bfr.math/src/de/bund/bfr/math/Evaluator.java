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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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

		parserConstants.forEach((constant, value) -> parser.setVarValue(constant, value));

		ASTNode f = parser.parse(formula);
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

		if (result == null) {
			result = getErrors(valuesX, parserConstants, covariances, extraVariance, degreesOfFreedom,
					new ParameterFunction() {

						@Override
						public double[] getValuesY(Map<String, Double> parameterValues) throws ParseException {
							return getFunctionPoints(parameterValues, formula, varX, valuesX);
						}
					});
			errorResults.put(function, result);
		}

		return result;
	}

	public static double[] getDiffPoints(Map<String, Double> parserConstants, Map<String, String> functions,
			Map<String, Double> initValues, Map<String, String> initParameters,
			Map<String, List<Double>> conditionLists, String dependentVariable,
			Map<String, Double> independentVariables, String varX, double[] valuesX, IntegratorFactory integrator,
			InterpolationFactory interpolator) throws ParseException {
		DiffFunctionConf function = new DiffFunctionConf(parserConstants, functions, initValues, initParameters,
				conditionLists, dependentVariable, independentVariables, varX, valuesX, integrator, interpolator);
		double[] result = diffResults.getIfPresent(function);

		if (result != null) {
			return result;
		}

		List<ASTNode> fs = new ArrayList<>();
		List<String> valueVariables = new ArrayList<>();
		double[] values = new double[functions.size()];
		Parser parser = new Parser();

		parserConstants.forEach((constant, value) -> parser.setVarValue(constant, value));

		int index = 0;

		for (Map.Entry<String, String> entry : functions.entrySet()) {
			String var = entry.getKey();

			fs.add(parser.parse(entry.getValue()));
			valueVariables.add(var);
			values[index++] = initValues.containsKey(var) ? initValues.get(var)
					: parserConstants.get(initParameters.get(var));
		}

		Map<String, UnivariateFunction> variableFunctions = MathUtils.createInterpolationFunctions(conditionLists, varX,
				interpolator);
		FirstOrderDifferentialEquations f = new FirstOrderDifferentialEquations() {

			@Override
			public int getDimension() {
				return functions.size();
			}

			@Override
			public void computeDerivatives(double t, double[] y, double[] yDot)
					throws MaxCountExceededException, DimensionMismatchException {
				parser.setVarValue(varX, t);
				variableFunctions.forEach((var, function) -> parser.setVarValue(var, function.value(t)));

				for (int i = 0; i < functions.size(); i++) {
					parser.setVarValue(valueVariables.get(i), y[i]);
				}

				for (int i = 0; i < functions.size(); i++) {
					try {
						double value = parser.evaluate(fs.get(i));

						yDot[i] = Double.isFinite(value) ? value : Double.NaN;
					} catch (ParseException e) {
						e.printStackTrace();
						yDot[i] = Double.NaN;
					}
				}
			}
		};

		FirstOrderIntegrator instance = integrator.createIntegrator();
		double diffValue = conditionLists.get(varX).get(0);
		int depIndex = valueVariables.indexOf(dependentVariable);
		double[] valuesY = new double[valuesX.length];

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
			Map<String, Double> initValues, Map<String, String> initParameters,
			Map<String, List<Double>> conditionLists, String dependentVariable,
			Map<String, Double> independentVariables, String varX, double[] valuesX, IntegratorFactory integrator,
			InterpolationFactory interpolator, Map<String, Map<String, Double>> covariances, double extraVariance,
			int degreesOfFreedom) throws ParseException {
		ErrorDiffFunctionConf function = new ErrorDiffFunctionConf(parserConstants, functions, initValues,
				initParameters, conditionLists, dependentVariable, independentVariables, varX, valuesX, integrator,
				interpolator, covariances, extraVariance, degreesOfFreedom);
		double[] result = errorDiffResults.getIfPresent(function);

		if (result == null) {
			result = getErrors(valuesX, parserConstants, covariances, extraVariance, degreesOfFreedom,
					new ParameterFunction() {

						@Override
						public double[] getValuesY(Map<String, Double> parameterValues) throws ParseException {
							return getDiffPoints(parameterValues, functions, initValues, initParameters, conditionLists,
									dependentVariable, independentVariables, varX, valuesX, integrator, interpolator);
						}
					});
			errorDiffResults.put(function, result);
		}

		return result;
	}

	private static double[] getErrors(double[] valuesX, Map<String, Double> parserConstants,
			Map<String, Map<String, Double>> covariances, double extraVariance, int degreesOfFreedom,
			ParameterFunction f) throws ParseException {
		List<String> paramList = new ArrayList<>(covariances.keySet());
		Map<String, double[]> derivValues = new LinkedHashMap<>();
		Map<String, ParseException> exceptions = new LinkedHashMap<>();

		paramList.parallelStream().forEach(param -> {
			Map<String, Double> constantsMinus = new LinkedHashMap<>(parserConstants);
			Map<String, Double> constantsPlus = new LinkedHashMap<>(parserConstants);
			double value = parserConstants.get(param);

			constantsMinus.put(param, value - MathUtils.DERIV_EPSILON);
			constantsPlus.put(param, value + MathUtils.DERIV_EPSILON);

			double[] deriv = new double[valuesX.length];

			try {
				double[] valuesMinus = f.getValuesY(constantsMinus);
				double[] valuesPlus = f.getValuesY(constantsPlus);

				for (int i = 0; i < valuesX.length; i++) {
					deriv[i] = (valuesPlus[i] - valuesMinus[i]) / (2 * MathUtils.DERIV_EPSILON);
				}

				derivValues.put(param, deriv);
			} catch (ParseException e) {
				exceptions.put(param, e);
			}
		});

		if (!exceptions.isEmpty()) {
			throw exceptions.values().stream().findAny().get();
		}

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
		private Map<String, List<Double>> conditionLists;
		private String dependentVariable;
		private Map<String, Double> independentVariables;
		private String varX;
		private double[] valuesX;
		private IntegratorFactory integrator;
		private InterpolationFactory interpolator;

		public DiffFunctionConf(Map<String, Double> parserConstants, Map<String, String> functions,
				Map<String, Double> initValues, Map<String, String> initParameters,
				Map<String, List<Double>> conditionLists, String dependentVariable,
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
			return Objects.hash(parserConstants, functions, initValues, initParameters, conditionLists,
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
					&& Objects.equals(conditionLists, other.conditionLists)
					&& Objects.equals(dependentVariable, other.dependentVariable)
					&& Objects.equals(independentVariables, other.independentVariables)
					&& Objects.equals(varX, other.varX) && Arrays.equals(valuesX, other.valuesX)
					&& Objects.equals(integrator, other.integrator) && Objects.equals(interpolator, other.interpolator);
		}
	}

	private static class ErrorDiffFunctionConf extends DiffFunctionConf {

		private Map<String, Map<String, Double>> covariances;
		private double extraVariance;
		private int degreesOfFreedom;

		public ErrorDiffFunctionConf(Map<String, Double> parserConstants, Map<String, String> functions,
				Map<String, Double> initValues, Map<String, String> initParameters,
				Map<String, List<Double>> conditionLists, String dependentVariable,
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

	private interface ParameterFunction {

		double[] getValuesY(Map<String, Double> parameterValues) throws ParseException;
	}
}
