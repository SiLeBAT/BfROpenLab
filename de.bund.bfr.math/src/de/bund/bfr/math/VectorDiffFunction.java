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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorDiffFunction implements MultivariateVectorFunction {

	private DJep parser;
	private Node[] functions;
	private String[] dependentVariables;
	private double[] initValues;
	private String[] initParameters;
	private String[] parameters;
	private Map<String, double[]> variableValues;
	private double[] timeValues;
	private int dependentIndex;
	private String timeVariable;
	private IntegratorFactory integrator;

	public VectorDiffFunction(String[] formulas, String[] dependentVariables, double[] initValues,
			String[] initParameters, String[] parameters, Map<String, double[]> variableValues, double[] timeValues,
			String dependentVariable, String timeVariable, IntegratorFactory integrator) throws ParseException {
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

	public VectorDiffFunction(DJep parser, Node[] functions, String[] dependentVariables, double[] initValues,
			String[] initParameters, String[] parameters, Map<String, double[]> variableValues, double[] timeValues,
			int dependentIndex, String timeVariable, IntegratorFactory integrator) {
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

		double[] values = new double[dependentVariables.length];

		for (int i = 0; i < dependentVariables.length; i++) {
			if (!Double.isNaN(initValues[i])) {
				values[i] = initValues[i];
			} else {
				values[i] = point[Arrays.asList(parameters).indexOf(initParameters[i])];
			}
		}

		DiffFunction f = new DiffFunction(parser, functions, dependentVariables, variableValues, timeVariable);
		FirstOrderIntegrator integratorInstance = integrator.createIntegrator();
		double[] result = new double[timeValues.length];

		result[0] = values[dependentIndex];

		for (int i = 1; i < timeValues.length; i++) {
			integratorInstance.integrate(f, timeValues[i - 1], values, timeValues[i], values);
			result[i] = values[dependentIndex];
		}

		return result;
	}
}
