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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorDiffFunctionJacobian implements MultivariateMatrixFunction {

	private static final double EPSILON = 0.00001;

	private DJep parser;
	private Node function;
	private List<String> parameters;
	private String valueVariable;
	private String timeVariable;
	private List<Double> timeValues;
	private Map<String, List<Double>> variableValues;
	private double initialValue;

	public VectorDiffFunctionJacobian(String formula, List<String> parameters,
			String valueVariable, String timeVariable, List<Double> timeValues,
			Map<String, List<Double>> variableValues, double initialValue)
			throws ParseException {
		this.parameters = parameters;
		this.valueVariable = valueVariable;
		this.timeVariable = timeVariable;
		this.timeValues = timeValues;
		this.variableValues = variableValues;
		this.initialValue = initialValue;

		Set<String> variables = new LinkedHashSet<String>();

		variables.add(valueVariable);
		variables.add(timeVariable);
		variables.addAll(variableValues.keySet());
		variables.addAll(parameters);

		parser = MathUtilities.createParser(variables);
		function = parser.parse(formula);
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		double[][] result = new double[timeValues.size()][parameters.size()];

		for (int j = 0; j < parameters.size(); j++) {
			double paramValue = point[j];

			point[j] = paramValue - EPSILON;

			double[] result1 = new VectorDiffFunction(parser, function,
					parameters, valueVariable, timeVariable, timeValues,
					variableValues, initialValue).value(point);

			point[j] = paramValue + EPSILON;

			double[] result2 = new VectorDiffFunction(parser, function,
					parameters, valueVariable, timeVariable, timeValues,
					variableValues, initialValue).value(point);

			for (int i = 0; i < timeValues.size(); i++) {
				result[i][j] = (result2[i] - result1[i]) / (2 * EPSILON);
			}
		}

		return result;
	}
}
