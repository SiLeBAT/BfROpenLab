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

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorMultiDiffFunctionJacobian implements
		MultivariateMatrixFunction {

	private static final double EPSILON = 0.00001;

	private DJep[] parsers;
	private Node[] functions;
	private String[] valueVariables;
	private double[] initialValues;
	private String[] parameters;
	private Map<String, double[]> variableValues;
	private String diffVariable;

	public VectorMultiDiffFunctionJacobian(String[] formulas,
			String[] valueVariables, double[] initialValues,
			String[] parameters, Map<String, double[]> variableValues,
			String diffVariable) throws ParseException {
		this.valueVariables = valueVariables;
		this.initialValues = initialValues;
		this.parameters = parameters;
		this.variableValues = variableValues;
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

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		int n = variableValues.get(diffVariable).length * functions.length;
		double[][] result = new double[n][parameters.length];

		for (int j = 0; j < parameters.length; j++) {
			double paramValue = point[j];

			point[j] = paramValue - EPSILON;

			double[] result1 = new VectorMultiDiffFunction(parsers, functions,
					valueVariables, initialValues, parameters, variableValues,
					diffVariable).value(point);

			point[j] = paramValue + EPSILON;

			double[] result2 = new VectorMultiDiffFunction(parsers, functions,
					valueVariables, initialValues, parameters, variableValues,
					diffVariable).value(point);

			point[j] = paramValue;

			for (int i = 0; i < n; i++) {
				result[i][j] = (result2[i] - result1[i]) / (2 * EPSILON);
			}
		}

		return result;
	}
}
