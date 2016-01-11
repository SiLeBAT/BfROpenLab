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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class MultiVectorDiffFunction implements MultivariateVectorFunction {

	private Parser parser;
	private Node[] functions;
	private String[] dependentVariables;
	private double[] initValues;
	private List<String[]> initParameters;
	private String[] parameters;
	private Map<String, List<double[]>> variableValues;
	private List<double[]> timeValues;
	private int dependentIndex;
	private String timeVariable;
	private IntegratorFactory integrator;
	private InterpolationFactory interpolator;

	public MultiVectorDiffFunction(String[] formulas, String[] dependentVariables, double[] initValues,
			List<String[]> initParameters, String[] parameters, Map<String, List<double[]>> variableValues,
			List<double[]> timeValues, String dependentVariable, String timeVariable, IntegratorFactory integrator,
			InterpolationFactory interpolator) throws ParseException {
		this.dependentVariables = dependentVariables;
		this.initValues = initValues;
		this.initParameters = initParameters;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.timeValues = timeValues;
		this.dependentIndex = Arrays.asList(dependentVariables).indexOf(dependentVariable);
		this.timeVariable = timeVariable;
		this.integrator = integrator;
		this.interpolator = interpolator;

		parser = new Parser(Stream.concat(Stream.concat(Stream.of(dependentVariables), Stream.of(parameters)),
				variableValues.keySet().stream()).collect(Collectors.toSet()));
		functions = new Node[formulas.length];

		for (int i = 0; i < formulas.length; i++) {
			functions[i] = parser.parse(formulas[i]);
		}
	}

	public MultiVectorDiffFunction(Parser parser, Node[] functions, String[] dependentVariables, double[] initValues,
			List<String[]> initParameters, String[] parameters, Map<String, List<double[]>> variableValues,
			List<double[]> timeValues, int dependentIndex, String timeVariable, IntegratorFactory integrator,
			InterpolationFactory interpolator) {
		this.parser = parser;
		this.functions = functions;
		this.dependentVariables = dependentVariables;
		this.initValues = initValues;
		this.initParameters = initParameters;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.timeValues = timeValues;
		this.dependentIndex = dependentIndex;
		this.timeVariable = timeVariable;
		this.integrator = integrator;
		this.interpolator = interpolator;
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		for (int i = 0; i < parameters.length; i++) {
			if (!Arrays.asList(initParameters).contains(parameters[i])) {
				parser.setVarValue(parameters[i], point[i]);
			}
		}

		FirstOrderIntegrator integratorInstance = integrator.createIntegrator();
		int n = timeValues.stream().mapToInt(t -> t.length).sum();
		double[] result = new double[n];
		int index = 0;

		for (int j = 0; j < timeValues.size(); j++) {
			double[] t = timeValues.get(j);
			Map<String, double[]> vv = new LinkedHashMap<>();

			for (Map.Entry<String, List<double[]>> entry : variableValues.entrySet()) {
				vv.put(entry.getKey(), entry.getValue().get(j));
			}

			DiffFunction f = new DiffFunction(parser, functions, dependentVariables, vv, timeVariable, interpolator);
			double[] values = new double[dependentVariables.length];

			for (int i = 0; i < dependentVariables.length; i++) {
				if (Double.isFinite(initValues[i])) {
					values[i] = initValues[i];
				} else {
					values[i] = point[Arrays.asList(parameters).indexOf(initParameters.get(j)[i])];
				}
			}

			result[index++] = values[dependentIndex];

			for (int i = 1; i < t.length; i++) {
				integratorInstance.integrate(f, t[i - 1], values, t[i], values);
				result[index++] = values[dependentIndex];
			}
		}

		return result;
	}
}
