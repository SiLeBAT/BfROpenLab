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

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorDiffFunction implements MultivariateVectorFunction {

	private DJep parser;
	private Node function;
	private List<String> parameters;
	private String dependentVariable;
	private String timeVariable;
	private List<Double> timeValues;
	private Map<String, List<Double>> variableValues;
	private double initialValue;

	public VectorDiffFunction(String formula, List<String> parameters,
			String valueVariable, String timeVariable, List<Double> timeValues,
			Map<String, List<Double>> variableValues, double initialValue)
			throws ParseException {
		this.parameters = parameters;
		this.dependentVariable = valueVariable;
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
	public double[] value(double[] point) throws IllegalArgumentException {
		double[] result = new double[timeValues.size()];

		for (int i = 0; i < parameters.size(); i++) {
			parser.setVarValue(parameters.get(i), point[i]);
		}

		DiffFunction f = new DiffFunction(parser, function, dependentVariable,
				timeVariable, timeValues, variableValues);
		ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(
				0.01);
		double time = 0.0;
		double[] value = { initialValue };

		result[0] = initialValue;

		for (int i = 1; i < timeValues.size(); i++) {
			integrator.integrate(f, time, value, timeValues.get(i), value);
			time = timeValues.get(i);
			result[i] = value[0];
		}

		return result;
	}

}
