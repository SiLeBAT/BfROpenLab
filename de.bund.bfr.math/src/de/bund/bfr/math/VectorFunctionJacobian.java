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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorFunctionJacobian implements MultivariateMatrixFunction {

	private static final double EPSILON = 0.00001;

	private DJep parser;
	private Node function;
	private String[] parameters;
	private Node[] derivatives;
	private String[] arguments;
	private double[][] argumentValues;
	private double[] targetValues;

	private List<List<Integer>> changeLists;

	public VectorFunctionJacobian(DJep parser, Node function,
			List<String> parameters, List<Node> derivatives,
			Map<String, List<Double>> argumentValues, List<Double> targetValues) {
		this.parser = parser;
		this.function = function;
		this.parameters = parameters.toArray(new String[0]);
		this.derivatives = derivatives.toArray(new Node[0]);
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

		changeLists = createChangeLists();
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		double[][] retValue = new double[targetValues.length][parameters.length];

		try {
			for (int i = 0; i < targetValues.length; i++) {
				for (int j = 0; j < derivatives.length; j++) {
					retValue[i][j] = evalWithSingularityCheck(j,
							argumentValues[i], point);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return retValue;
	}

	private double evalWithSingularityCheck(int index, double[] argValues,
			double[] paramValues) throws ParseException {
		for (int i = 0; i < parameters.length; i++) {
			parser.setVarValue(parameters[i], paramValues[i]);
		}

		for (List<Integer> list : changeLists) {
			for (int i = 0; i < arguments.length; i++) {
				double d = list.get(i) * EPSILON;

				parser.setVarValue(arguments[i], argValues[i] + d);
			}

			Object number = parser.evaluate(derivatives[index]);

			if (MathUtilities.isValidDouble(number)) {
				return (Double) number;
			}
		}

		for (List<Integer> list : changeLists) {
			for (int i = 0; i < arguments.length; i++) {
				double d = list.get(i) * EPSILON;

				parser.setVarValue(arguments[i], argValues[i] + d);
			}

			parser.setVarValue(parameters[index], paramValues[index] - EPSILON);

			Object number1 = parser.evaluate(function);

			parser.setVarValue(parameters[index], paramValues[index] + EPSILON);

			Object number2 = parser.evaluate(function);

			if (MathUtilities.isValidDouble(number1)
					&& MathUtilities.isValidDouble(number2)) {
				return ((Double) number2 - (Double) number1) / (2 * EPSILON);
			}
		}

		return Double.NaN;
	}

	private List<List<Integer>> createChangeLists() {
		int n = arguments.length;
		boolean done = false;
		List<List<Integer>> changeLists = new ArrayList<List<Integer>>();
		List<Integer> list = new ArrayList<Integer>(Collections.nCopies(n, -1));

		while (!done) {
			changeLists.add(new ArrayList<Integer>(list));

			for (int i = 0;; i++) {
				if (i >= n) {
					done = true;
					break;
				}

				list.set(i, list.get(i) + 1);

				if (list.get(i) > 1) {
					list.set(i, -1);
				} else {
					break;
				}
			}
		}

		Collections.sort(changeLists, new Comparator<List<Integer>>() {

			@Override
			public int compare(List<Integer> l1, List<Integer> l2) {
				int n1 = 0;
				int n2 = 0;

				for (int i : l1) {
					if (i == 0) {
						n1++;
					}
				}

				for (int i : l2) {
					if (i == 0) {
						n2++;
					}
				}

				if (n1 < n2) {
					return 1;
				} else if (n1 > n2) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		return changeLists;
	}
}
