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

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorFunction implements MultivariateVectorFunction {

	private DJep parser;
	private Node function;
	private String[] parameters;
	private String[] arguments;
	private double[][] argumentValues;
	private double[] targetValues;

	public VectorFunction(DJep parser, Node function, List<String> parameters,
			Map<String, List<Double>> argumentValues, List<Double> targetValues) {
		this.parser = parser;
		this.function = function;
		this.parameters = parameters.toArray(new String[0]);
		this.arguments = argumentValues.keySet().toArray(new String[0]);
		this.argumentValues = new double[targetValues.size()][argumentValues
				.size()];
		this.targetValues = new double[targetValues.size()];

		for (int i = 0; i < targetValues.size(); i++) {
			this.targetValues[i] = targetValues.get(i);
			int j = 0;

			for (List<Double> value : argumentValues.values()) {
				this.argumentValues[i][j] = value.get(i);
				j++;
			}
		}
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		double[] retValue = new double[targetValues.length];

		for (int i = 0; i < parameters.length; i++) {
			parser.setVarValue(parameters[i], point[i]);
		}

		try {
			for (int i = 0; i < targetValues.length; i++) {
				for (int j = 0; j < arguments.length; j++) {
					parser.setVarValue(arguments[j], argumentValues[i][j]);
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
