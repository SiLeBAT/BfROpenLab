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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorFunction implements ValueAndJacobianFunction {

	private List<String> parameters;
	private Map<String, List<Double>> variableValues;

	private int nParams;
	private int nValues;
	private Parser parser;
	private Node function;
	private Map<String, Node> derivatives;

	public VectorFunction(String formula, List<String> parameters, Map<String, List<Double>> variableValues)
			throws ParseException {
		this.parameters = parameters;
		this.variableValues = variableValues;

		nParams = parameters.size();
		nValues = variableValues.values().stream().findAny().get().size();
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
		double[] retValue = new double[nValues];

		for (int ip = 0; ip < nParams; ip++) {
			parser.setVarValue(parameters.get(ip), point[ip]);
		}

		for (int iv = 0; iv < nValues; iv++) {
			for (Map.Entry<String, List<Double>> entry : variableValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue().get(iv));
			}

			try {
				double value = parser.evaluate(function);

				retValue[iv] = Double.isFinite(value) ? value : Double.NaN;
			} catch (ParseException e) {
				e.printStackTrace();
				retValue[iv] = Double.NaN;
			}
		}

		return retValue;
	}

	@Override
	public MultivariateMatrixFunction createJacobian() {
		return point -> {
			double[][] retValue = new double[nValues][nParams];

			for (int ip = 0; ip < nParams; ip++) {
				parser.setVarValue(parameters.get(ip), point[ip]);
			}

			for (int iv = 0; iv < nValues; iv++) {
				for (Map.Entry<String, List<Double>> entry : variableValues.entrySet()) {
					parser.setVarValue(entry.getKey(), entry.getValue().get(iv));
				}

				for (int ip = 0; ip < nParams; ip++) {
					try {
						double value = parser.evaluate(derivatives.get(parameters.get(ip)));

						if (!Double.isFinite(value)) {
							parser.setVarValue(parameters.get(ip), point[ip] - MathUtils.DERIV_EPSILON);

							double value1 = parser.evaluate(function);

							parser.setVarValue(parameters.get(ip), point[ip] + MathUtils.DERIV_EPSILON);

							double value2 = parser.evaluate(function);

							parser.setVarValue(parameters.get(ip), point[ip]);

							value = (value2 - value1) / (2 * MathUtils.DERIV_EPSILON);
						}

						retValue[iv][ip] = Double.isFinite(value) ? value : Double.NaN;
					} catch (ParseException e) {
						e.printStackTrace();
						retValue[iv][ip] = Double.NaN;
					}
				}
			}

			return retValue;
		};
	}
}
