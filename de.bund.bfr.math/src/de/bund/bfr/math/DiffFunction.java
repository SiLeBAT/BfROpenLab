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

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class DiffFunction implements FirstOrderDifferentialEquations {

	private DJep parser;
	private Node function;
	private String valueVariable;
	private String timeVariable;
	private Map<String, List<Double>> variableValues;

	public DiffFunction(String formula, Map<String, Double> paramValues,
			String valueVariable, String timeVariable,
			Map<String, List<Double>> variableValues) throws ParseException {
		this.valueVariable = valueVariable;
		this.timeVariable = timeVariable;
		this.variableValues = variableValues;

		Set<String> variables = new LinkedHashSet<String>();

		variables.add(valueVariable);
		variables.addAll(variableValues.keySet());
		variables.addAll(paramValues.keySet());

		parser = MathUtilities.createParser(variables);
		function = parser.parse(formula);

		for (Map.Entry<String, Double> entry : paramValues.entrySet()) {
			parser.setVarValue(entry.getKey(), entry.getValue());
		}
	}

	public DiffFunction(DJep parser, Node function, String dependentVariable,
			String timeVariable, Map<String, List<Double>> variableValues) {
		this.parser = parser;
		this.function = function;
		this.valueVariable = dependentVariable;
		this.timeVariable = timeVariable;
		this.variableValues = variableValues;
	}

	@Override
	public void computeDerivatives(double t, double[] y, double[] yDot)
			throws MaxCountExceededException, DimensionMismatchException {
		List<Double> timeValues = variableValues.get(timeVariable);
		int index;

		for (index = 0; index < timeValues.size() - 1; index++) {
			if (t < timeValues.get(index + 1)) {
				break;
			}
		}

		parser.setVarValue(valueVariable, y[0]);
		parser.setVarValue(timeVariable, t);

		for (Map.Entry<String, List<Double>> entry : variableValues.entrySet()) {
			parser.setVarValue(entry.getKey(), entry.getValue().get(index));
		}

		try {
			Object number = parser.evaluate(function);

			if (MathUtilities.isValidDouble(number)) {
				yDot[0] = (Double) number;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getDimension() {
		return 1;
	}
}
