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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class MultiVectorDiffFunction implements MultivariateVectorFunction {

	private DJep parser;
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

	public MultiVectorDiffFunction(String[] formulas, String[] dependentVariables, double[] initValues,
			List<String[]> initParameters, String[] parameters, Map<String, List<double[]>> variableValues,
			List<double[]> timeValues, String dependentVariable, String timeVariable, IntegratorFactory integrator)
					throws ParseException {
		this.dependentVariables = dependentVariables;
		this.initValues = initValues;
		this.initParameters = initParameters;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.timeValues = timeValues;
		this.dependentIndex = Arrays.asList(dependentVariables).indexOf(dependentVariable);
		this.timeVariable = timeVariable;
		this.integrator = integrator;

		Set<String> variables = new LinkedHashSet<>();

		variables.addAll(Arrays.asList(dependentVariables));
		variables.addAll(variableValues.keySet());
		variables.addAll(Arrays.asList(parameters));

		parser = MathUtils.createParser(variables);
		functions = new Node[formulas.length];

		for (int i = 0; i < formulas.length; i++) {
			functions[i] = parser.parse(formulas[i]);
		}
	}

	public MultiVectorDiffFunction(DJep parser, Node[] functions, String[] dependentVariables, double[] initValues,
			List<String[]> initParameters, String[] parameters, Map<String, List<double[]>> variableValues,
			List<double[]> timeValues, int dependentIndex, String timeVariable, IntegratorFactory integrator) {
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
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		for (int i = 0; i < parameters.length; i++) {
			if (!Arrays.asList(initParameters).contains(parameters[i])) {
				parser.setVarValue(parameters[i], point[i]);
			}
		}

		FirstOrderIntegrator integratorInstance = integrator.createIntegrator();
		int n = 0;

		for (double[] t : timeValues) {
			n += t.length;
		}

		double[] result = new double[n];
		int index = 0;

		for (int j = 0; j < timeValues.size(); j++) {
			double[] t = timeValues.get(j);
			Map<String, double[]> vv = new LinkedHashMap<>();

			for (Map.Entry<String, List<double[]>> entry : variableValues.entrySet()) {
				vv.put(entry.getKey(), entry.getValue().get(j));
			}

			DiffFunction f = new DiffFunction(parser, functions, dependentVariables, vv, timeVariable);
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
