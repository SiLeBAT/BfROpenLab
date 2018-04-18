/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.primitives.Doubles;

public class VectorFunction implements ValueAndJacobianFunction {

	private String formula;
	private List<String> parameters;
	private Map<String, List<Double>> variableValues;

	private Parser parser;
	private ASTNode function;

	public VectorFunction(String formula, List<String> parameters, Map<String, List<Double>> variableValues)
			throws ParseException {
		this.formula = formula;
		this.parameters = parameters;
		this.variableValues = variableValues;

		parser = new Parser();
		function = parser.parse(formula);
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		List<Double> result = new ArrayList<>();

		for (int i = 0; i < parameters.size(); i++) {
			parser.setVarValue(parameters.get(i), point[i]);
		}

		int n = variableValues.values().stream().findAny().get().size();

		for (int i = 0; i < n; i++) {
			for (Map.Entry<String, List<Double>> entry : variableValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue().get(i));
			}

			try {
				double value = parser.evaluate(function);

				result.add(Double.isFinite(value) ? value : Double.NaN);
			} catch (ParseException e) {
				e.printStackTrace();
				result.add(Double.NaN);
			}
		}

		return Doubles.toArray(result);
	}

	@Override
	public MultivariateMatrixFunction createJacobian() {
		List<VectorFunction> diffFunctions = new ArrayList<>();

		for (int i = 0; i < parameters.size(); i++) {
			try {
				diffFunctions.add(new VectorFunction(formula, parameters, variableValues));
			} catch (ParseException e) {
			}
		}

		return point -> MathUtils.aproxJacobianParallel(diffFunctions, point,
				variableValues.values().stream().findAny().get().size());
	}
}
