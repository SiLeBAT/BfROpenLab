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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorFunction implements MultivariateVectorFunction {

	private Parser parser;
	private Node function;
	private String[] parameters;
	private Map<String, double[]> variableValues;

	private int nParams;
	private int nValues;

	public VectorFunction(String formula, String[] parameters, Map<String, double[]> variableValues)
			throws ParseException {
		this.parameters = parameters;
		this.variableValues = variableValues;

		nParams = parameters.length;
		nValues = variableValues.values().stream().findAny().get().length;

		parser = new Parser(
				Stream.concat(Stream.of(parameters), variableValues.keySet().stream()).collect(Collectors.toSet()));
		function = parser.parse(formula);
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		double[] retValue = new double[nValues];

		for (int ip = 0; ip < nParams; ip++) {
			parser.setVarValue(parameters[ip], point[ip]);
		}

		for (int iv = 0; iv < nValues; iv++) {
			for (Map.Entry<String, double[]> entry : variableValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue()[iv]);
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
}
