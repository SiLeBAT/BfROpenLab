/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.math;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorDiffFunction implements MultivariateVectorFunction {

	private DJep[] parsers;
	private Node[] functions;
	private String[] valueVariables;
	private double[] initialValues;
	private String[] parameters;
	private Map<String, double[]> variableValues;
	private String dependentVariable;
	private String diffVariable;

	public VectorDiffFunction(String[] formulas, String[] valueVariables,
			double[] initialValues, String[] parameters,
			Map<String, double[]> variableValues, String dependentVariable,
			String diffVariable) throws ParseException {
		this.valueVariables = valueVariables;
		this.initialValues = initialValues;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.dependentVariable = dependentVariable;
		this.diffVariable = diffVariable;

		Set<String> variables = new LinkedHashSet<>();

		variables.addAll(Arrays.asList(valueVariables));
		variables.addAll(variableValues.keySet());
		variables.addAll(Arrays.asList(parameters));

		parsers = new DJep[formulas.length];
		functions = new Node[formulas.length];

		for (int i = 0; i < formulas.length; i++) {
			parsers[i] = MathUtilities.createParser(variables);
			functions[i] = parsers[i].parse(formulas[i]);
		}
	}

	public VectorDiffFunction(DJep[] parsers, Node[] functions,
			String[] valueVariables, double[] initialValues,
			String[] parameters, Map<String, double[]> variableValues,
			String dependentVariable, String diffVariable) {
		this.parsers = parsers;
		this.functions = functions;
		this.valueVariables = valueVariables;
		this.initialValues = initialValues;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.dependentVariable = dependentVariable;
		this.diffVariable = diffVariable;
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		int depIndex = Arrays.asList(valueVariables).indexOf(dependentVariable);
		double[] diffValues = variableValues.get(diffVariable);

		for (int i = 0; i < parsers.length; i++) {
			for (int j = 0; j < parameters.length; j++) {
				parsers[i].setVarValue(parameters[j], point[j]);
			}
		}

		DiffFunction f = new DiffFunction(parsers, functions, valueVariables,
				variableValues, diffVariable);
		ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(
				0.01);
		double[] values = initialValues.clone();
		double[] result = new double[diffValues.length];

		result[0] = values[depIndex];

		for (int i = 1; i < diffValues.length; i++) {
			integrator.integrate(f, diffValues[i - 1], values, diffValues[i],
					values);
			result[i] = values[depIndex];
		}

		return result;
	}
}
