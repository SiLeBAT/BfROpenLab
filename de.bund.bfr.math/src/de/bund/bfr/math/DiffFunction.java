/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.analysis.UnivariateFunction;
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

	private Map<String, UnivariateFunction> variableFunctions;

	public DiffFunction(Parser parser, Node[] functions, String[] dependentVariables,
			Map<String, double[]> variableValues, String timeVariable, InterpolationFactory interpolator) {
		this.parser = parser;
		this.functions = functions;
		this.dependentVariables = dependentVariables;
		this.timeVariable = timeVariable;

		variableFunctions = new LinkedHashMap<>();
		variableValues.forEach((var, values) -> {
			if (!var.equals(timeVariable)) {
				variableFunctions.put(var,
						interpolator.createInterpolationFunction(variableValues.get(timeVariable), values));
			}
		});
	}

	@Override
	public void computeDerivatives(double t, double[] y, double[] yDot)
			throws MaxCountExceededException, DimensionMismatchException {
		parser.setVarValue(timeVariable, t);
		variableFunctions.forEach((var, function) -> parser.setVarValue(var, function.value(t)));

		for (int i = 0; i < y.length; i++) {
			parser.setVarValue(dependentVariables[i], y[i]);
		}

		for (int i = 0; i < y.length; i++) {
			try {
				double value = parser.evaluate(functions[i]);

				yDot[i] = Double.isFinite(value) ? value : Double.NaN;
			} catch (ParseException e) {
				e.printStackTrace();
				yDot[i] = Double.NaN;
			}
		}
	}

	@Override
	public int getDimension() {
		return functions.length;
	}
}
