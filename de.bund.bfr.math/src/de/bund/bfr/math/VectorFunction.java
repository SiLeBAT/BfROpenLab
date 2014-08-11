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

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import com.google.common.collect.Sets;

public class VectorFunction implements MultivariateVectorFunction {

	private DJep parser;
	private Node function;
	private List<String> parameters;
	private Map<String, double[]> variableValues;
	private int dimension;

	public VectorFunction(String formula, List<String> parameters,
			Map<String, double[]> variableValues) throws ParseException {
		this.parameters = parameters;
		this.variableValues = variableValues;

		parser = MathUtilities.createParser(Sets.union(new LinkedHashSet<>(
				parameters), variableValues.keySet()));
		function = parser.parse(formula);

		for (double[] values : variableValues.values()) {
			dimension = values.length;
			break;
		}
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		double[] retValue = new double[dimension];

		for (int i = 0; i < parameters.size(); i++) {
			parser.setVarValue(parameters.get(i), point[i]);
		}

		try {
			for (int i = 0; i < dimension; i++) {
				for (Map.Entry<String, double[]> entry : variableValues
						.entrySet()) {
					parser.setVarValue(entry.getKey(), entry.getValue()[i]);
				}

				Object number = parser.evaluate(function);

				if (MathUtilities.isValidDouble(number)) {
					retValue[i] = (Double) number;
				} else {
					retValue[i] = Double.NaN;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return retValue;
	}
}
