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
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.primitives.Doubles;

public class MultiVectorDiffFunction implements ValueAndJacobianFunction {

	private List<String> formulas;
	private List<String> dependentVariables;
	private List<Double> initValues;
	private List<List<String>> initParameters;
	private List<String> parameters;
	private List<Map<String, List<Double>>> variableValues;
	private List<List<Double>> timeValues;
	private String dependentVariable;
	private String timeVariable;
	private IntegratorFactory integrator;
	private InterpolationFactory interpolator;

	private int dependentIndex;
	private Parser parser;
	private List<ASTNode> functions;

	public MultiVectorDiffFunction(List<String> formulas, List<String> dependentVariables, List<Double> initValues,
			List<List<String>> initParameters, List<String> parameters, List<Map<String, List<Double>>> variableValues,
			List<List<Double>> timeValues, String dependentVariable, String timeVariable, IntegratorFactory integrator,
			InterpolationFactory interpolator) throws ParseException {
		this.formulas = formulas;
		this.dependentVariables = dependentVariables;
		this.initValues = initValues;
		this.initParameters = initParameters;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.timeValues = timeValues;
		this.dependentVariable = dependentVariable;
		this.timeVariable = timeVariable;
		this.integrator = integrator;
		this.interpolator = interpolator;

		dependentIndex = dependentVariables.indexOf(dependentVariable);
		parser = new Parser();
		functions = new ArrayList<>();

		for (String f : formulas) {
			functions.add(parser.parse(f));
		}
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		for (int i = 0; i < parameters.size(); i++) {
			if (!initParameters.contains(parameters.get(i))) {
				parser.setVarValue(parameters.get(i), point[i]);
			}
		}

		FirstOrderIntegrator integratorInstance = integrator.createIntegrator();
		List<Double> result = new ArrayList<>();

		for (int i = 0; i < timeValues.size(); i++) {
			Map<String, UnivariateFunction> variableFunctions = MathUtils
					.createInterpolationFunctions(variableValues.get(i), timeVariable, interpolator);
			FirstOrderDifferentialEquations f = new FirstOrderDifferentialEquations() {

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

			double[] values = new double[formulas.size()];

			for (int j = 0; j < formulas.size(); j++) {
				values[j] = initValues.get(j) != null ? initValues.get(j)
						: point[parameters.indexOf(initParameters.get(i).get(j))];
			}

			result.add(values[dependentIndex]);

			for (int j = 1; j < timeValues.get(i).size(); j++) {
				integratorInstance.integrate(f, timeValues.get(i).get(j - 1), values, timeValues.get(i).get(j), values);
				result.add(values[dependentIndex]);
			}
		}

		return Doubles.toArray(result);
	}

	@Override
	public MultivariateMatrixFunction createJacobian() {
		List<MultiVectorDiffFunction> diffFunctions = new ArrayList<>();

		for (int i = 0; i < parameters.size(); i++) {
			try {
				diffFunctions.add(new MultiVectorDiffFunction(formulas, dependentVariables, initValues, initParameters,
						parameters, variableValues, timeValues, dependentVariable, timeVariable, integrator,
						interpolator));
			} catch (ParseException e) {
			}
		}

		return point -> MathUtils.aproxJacobianParallel(diffFunctions, point,
				timeValues.stream().mapToInt(t -> t.size()).sum());
	}
}
