/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import com.google.common.primitives.Doubles;

public class VectorFunction implements ValueAndJacobianFunction {

	private List<String> parameters;
	private Map<String, List<Double>> variableValues;

	private Parser parser;
	private Node function;
	private Map<String, Node> derivatives;

	public VectorFunction(String formula, List<String> parameters, Map<String, List<Double>> variableValues)
			throws ParseException {
		this.parameters = parameters;
		this.variableValues = variableValues;

		parser = new Parser(Stream.concat(parameters.stream(), variableValues.keySet().stream())
				.collect(Collectors.toCollection(LinkedHashSet::new)));
		function = parser.parse(formula);
		derivatives = new LinkedHashMap<>();

		for (String param : parameters) {
			derivatives.put(param, parser.differentiate(function, param));
		}
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
		return point -> {
			double[][] result = new double[variableValues.values().stream().findAny().get().size()][parameters.size()];

			for (int ip = 0; ip < parameters.size(); ip++) {
				parser.setVarValue(parameters.get(ip), point[ip]);
			}

			for (int i = 0; i < result.length; i++) {
				for (Map.Entry<String, List<Double>> entry : variableValues.entrySet()) {
					parser.setVarValue(entry.getKey(), entry.getValue().get(i));
				}

				for (int j = 0; j < parameters.size(); j++) {
					try {
						double value = parser.evaluate(derivatives.get(parameters.get(j)));

						if (!Double.isFinite(value)) {
							parser.setVarValue(parameters.get(j), point[j] - MathUtils.DERIV_EPSILON);

							double value1 = parser.evaluate(function);

							parser.setVarValue(parameters.get(j), point[j] + MathUtils.DERIV_EPSILON);

							double value2 = parser.evaluate(function);

							parser.setVarValue(parameters.get(j), point[j]);

							value = (value2 - value1) / (2 * MathUtils.DERIV_EPSILON);
						}

						result[i][j] = Double.isFinite(value) ? value : Double.NaN;
					} catch (ParseException e) {
						e.printStackTrace();
						result[i][j] = Double.NaN;
					}
				}
			}

			return result;
		};
	}
}
