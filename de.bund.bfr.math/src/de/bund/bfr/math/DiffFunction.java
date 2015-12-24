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

import java.util.Map;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class DiffFunction implements FirstOrderDifferentialEquations {

	private Parser parser;
	private Node[] functions;
	private String[] dependentVariables;
	private String timeVariable;
	private Map<String, double[]> variableValues;

	private int lastIndex;

	public DiffFunction(Parser parser, Node[] functions, String[] dependentVariables,
			Map<String, double[]> variableValues, String timeVariable) {
		this.parser = parser;
		this.functions = functions;
		this.dependentVariables = dependentVariables;
		this.timeVariable = timeVariable;
		this.variableValues = variableValues;
	}

	@Override
	public void computeDerivatives(double t, double[] y, double[] yDot)
			throws MaxCountExceededException, DimensionMismatchException {
		double[] diffValues = variableValues.get(timeVariable);
		int index = 0;

		if (diffValues[lastIndex] <= t) {
			index = lastIndex;
		}

		for (; index < diffValues.length; index++) {
			if (index == diffValues.length - 1 || diffValues[index + 1] > t) {
				break;
			}
		}

		lastIndex = index;

		for (Map.Entry<String, double[]> entry : variableValues.entrySet()) {
			parser.setVarValue(entry.getKey(), entry.getValue()[index]);
		}

		for (int i = 0; i < y.length; i++) {
			parser.setVarValue(dependentVariables[i], y[i]);
		}

		parser.setVarValue(timeVariable, t);

		try {
			for (int i = 0; i < y.length; i++) {
				Object number = parser.evaluate(functions[i]);

				if (!(number instanceof Double)) {
					number = Double.NaN;
				}

				yDot[i] = (Double) number;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getDimension() {
		return functions.length;
	}
}
