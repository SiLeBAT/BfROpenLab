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

public class VectorDiffFunction implements ValueAndJacobianFunction {

	private List<String> formulas;
	private List<String> dependentVariables;
	private List<Double> initValues;
	private List<String> initParameters;
	private List<String> parameters;
	private Map<String, List<Double>> variableValues;
	private String dependentVariable;
	private List<Double> timeValues;
	private String timeVariable;
	private IntegratorFactory integrator;
	private InterpolationFactory interpolator;

	private Map<String, UnivariateFunction> variableFunctions;
	private int dependentIndex;
	private Parser parser;
	private List<ASTNode> functions;

	public VectorDiffFunction(List<String> formulas, List<String> dependentVariables, List<Double> initValues,
			List<String> initParameters, List<String> parameters, Map<String, List<Double>> variableValues,
			List<Double> timeValues, String dependentVariable, String timeVariable, IntegratorFactory integrator,
			InterpolationFactory interpolator) throws ParseException {
		this.formulas = formulas;
		this.dependentVariables = dependentVariables;
		this.initValues = initValues;
		this.initParameters = initParameters;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.dependentVariable = dependentVariable;
		this.timeValues = timeValues;
		this.timeVariable = timeVariable;
		this.integrator = integrator;
		this.interpolator = interpolator;

		variableFunctions = MathUtils.createInterpolationFunctions(variableValues, timeVariable, interpolator);
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

		double[] values = new double[formulas.size()];

		for (int i = 0; i < formulas.size(); i++) {
			values[i] = initValues.get(i) != null ? initValues.get(i)
					: point[parameters.indexOf(initParameters.get(i))];
		}

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

		FirstOrderIntegrator integratorInstance = integrator.createIntegrator();
		List<Double> result = new ArrayList<>();

		result.add(values[dependentIndex]);

		for (int i = 1; i < timeValues.size(); i++) {
			integratorInstance.integrate(f, timeValues.get(i - 1), values, timeValues.get(i), values);
			result.add(values[dependentIndex]);
		}

		return Doubles.toArray(result);
	}

	@Override
	public MultivariateMatrixFunction createJacobian() {
		List<VectorDiffFunction> diffFunctions = new ArrayList<>();

		for (int i = 0; i < parameters.size(); i++) {
			try {
				diffFunctions.add(
						new VectorDiffFunction(formulas, dependentVariables, initValues, initParameters, parameters,
								variableValues, timeValues, dependentVariable, timeVariable, integrator, interpolator));
			} catch (ParseException e) {
			}
		}

		return point -> MathUtils.aproxJacobianParallel(diffFunctions, point, timeValues.size());
	}
}
