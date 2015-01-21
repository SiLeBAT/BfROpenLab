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

	private DJep[] parsers;
	private Node[] functions;
	private String[] dependentVariables;
	private Double[] initValues;
	private String[] initParameters;
	private String[] parameters;
	private Map<String, double[]> variableValues;
	private double[] timeValues;
	private int dependentIndex;
	private String timeVariable;
	private Integrator integrator;

	public VectorDiffFunction(String[] formulas, String[] dependentVariables,
			Double[] initValues, String[] initParameters, String[] parameters,
			Map<String, double[]> variableValues, double[] timeValues,
			String dependentVariable, String timeVariable, Integrator integrator)
			throws ParseException {
		this.dependentVariables = dependentVariables;
		this.initValues = initValues;
		this.initParameters = initParameters;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.timeValues = timeValues;
		this.dependentIndex = Arrays.asList(dependentVariables).indexOf(
				dependentVariable);
		this.timeVariable = timeVariable;
		this.integrator = integrator;

		Set<String> variables = new LinkedHashSet<>();

		variables.addAll(Arrays.asList(dependentVariables));
		variables.addAll(variableValues.keySet());
		variables.addAll(Arrays.asList(parameters));

		parsers = new DJep[formulas.length];
		functions = new Node[formulas.length];

		for (int i = 0; i < formulas.length; i++) {
			parsers[i] = MathUtils.createParser(variables);
			functions[i] = parsers[i].parse(formulas[i]);
		}
	}

	public VectorDiffFunction(DJep[] parsers, Node[] functions,
			String[] dependentVariables, Double[] initValues,
			String[] initParameters, String[] parameters,
			Map<String, double[]> variableValues, double[] timeValues,
			int dependentIndex, String timeVariable, Integrator integrator) {
		this.parsers = parsers;
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
		for (int i = 0; i < parsers.length; i++) {
			for (int j = 0; j < parameters.length; j++) {
				if (!Arrays.asList(initParameters).contains(parameters[j])) {
					parsers[i].setVarValue(parameters[j], point[j]);
				}
			}
		}

		double[] values = new double[dependentVariables.length];

		for (int i = 0; i < dependentVariables.length; i++) {
			if (initValues[i] != null) {
				values[i] = initValues[i];
			} else {
				values[i] = point[Arrays.asList(parameters).indexOf(
						initParameters[i])];
			}
		}

		DiffFunction f = new DiffFunction(parsers, functions,
				dependentVariables, variableValues, timeVariable);
		FirstOrderIntegrator integratorInstance = integrator.createIntegrator();
		double[] result = new double[timeValues.length];

		result[0] = values[dependentIndex];

		for (int i = 1; i < timeValues.length; i++) {
			integratorInstance.integrate(f, timeValues[i - 1], values,
					timeValues[i], values);
			result[i] = values[dependentIndex];
		}

		return result;
	}
}
