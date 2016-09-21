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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

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
	private List<Node> functions;

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
		parser = new Parser(Stream
				.concat(Stream.concat(dependentVariables.stream(), parameters.stream()),
						variableValues.stream().findAny().get().keySet().stream())
				.collect(Collectors.toCollection(LinkedHashSet::new)));
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
			DiffFunction f = new DiffFunction(parser, functions, dependentVariables, variableValues.get(i),
					timeVariable, interpolator);
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
