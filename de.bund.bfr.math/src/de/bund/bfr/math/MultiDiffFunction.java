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

import java.util.Map;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class MultiDiffFunction implements FirstOrderDifferentialEquations {

	private DJep[] parsers;
	private Node[] functions;
	private String[] valueVariables;
	private String diffVariable;
	private Map<String, double[]> variableValues;

	private int lastIndex;

	public MultiDiffFunction(DJep[] parsers, Node[] functions,
			String[] valueVariables, String diffVariable,
			Map<String, double[]> variableValues) {
		this.parsers = parsers;
		this.functions = functions;
		this.valueVariables = valueVariables;
		this.diffVariable = diffVariable;
		this.variableValues = variableValues;
	}

	@Override
	public void computeDerivatives(double t, double[] y, double[] yDot)
			throws MaxCountExceededException, DimensionMismatchException {
		double[] diffValues = variableValues.get(diffVariable);
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

		for (int i = 0; i < parsers.length; i++) {
			for (Map.Entry<String, double[]> entry : variableValues.entrySet()) {
				parsers[i].setVarValue(entry.getKey(), entry.getValue()[index]);
			}

			for (int j = 0; j < y.length; j++) {
				parsers[i].setVarValue(valueVariables[j], y[j]);
			}

			parsers[i].setVarValue(diffVariable, t);
		}

		try {
			for (int i = 0; i < y.length; i++) {
				Object number = parsers[i].evaluate(functions[i]);

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
