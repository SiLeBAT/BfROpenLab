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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorFunctionJacobian implements MultivariateMatrixFunction {

	private static final double EPSILON = 0.00001;

	private DJep parser;
	private Node function;
	private List<String> parameters;
	private Map<String, Node> derivatives;
	private List<Map<String, List<Double>>> argumentValues;
	private int dimension;

	public VectorFunctionJacobian(String formula, List<String> parameters,
			Map<String, List<Double>> argumentValues) throws ParseException {
		this.parameters = parameters;
		this.argumentValues = createChangeLists(argumentValues);

		parser = MathUtilities.createParser(CollectionUtils.union(parameters,
				argumentValues.keySet()));
		function = parser.parse(formula);
		derivatives = new LinkedHashMap<String, Node>();

		for (String param : parameters) {
			derivatives.put(param, parser.differentiate(function, param));
		}

		for (List<Double> values : argumentValues.values()) {
			dimension = values.size();
			break;
		}
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		double[][] retValue = new double[dimension][parameters.size()];

		try {
			Map<String, Double> paramValues = new LinkedHashMap<String, Double>();

			for (int i = 0; i < parameters.size(); i++) {
				paramValues.put(parameters.get(i), point[i]);
			}

			for (int i = 0; i < dimension; i++) {
				for (int j = 0; j < parameters.size(); j++) {
					retValue[i][j] = evalWithSingularityCheck(
							parameters.get(j), paramValues, i);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return retValue;
	}

	private double evalWithSingularityCheck(String param,
			Map<String, Double> paramValues, int index) throws ParseException {
		for (Map.Entry<String, Double> entry : paramValues.entrySet()) {
			parser.setVarValue(entry.getKey(), entry.getValue());
		}

		for (Map<String, List<Double>> argValues : argumentValues) {
			for (Map.Entry<String, List<Double>> entry : argValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue().get(index));
			}

			Object number = parser.evaluate(derivatives.get(param));

			if (MathUtilities.isValidDouble(number)) {
				return (Double) number;
			}
		}

		for (Map<String, List<Double>> argValues : argumentValues) {
			for (Map.Entry<String, List<Double>> entry : argValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue().get(index));
			}

			parser.setVarValue(param, paramValues.get(param) - EPSILON);

			Object number1 = parser.evaluate(function);

			parser.setVarValue(param, paramValues.get(param) + EPSILON);

			Object number2 = parser.evaluate(function);

			if (MathUtilities.isValidDouble(number1)
					&& MathUtilities.isValidDouble(number2)) {
				return ((Double) number2 - (Double) number1) / (2 * EPSILON);
			}
		}

		return Double.NaN;
	}

	private static List<Map<String, List<Double>>> createChangeLists(
			Map<String, List<Double>> argumentValues) {
		int n = argumentValues.size();
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

		List<Map<String, List<Double>>> result = new ArrayList<Map<String, List<Double>>>();

		for (List<Integer> changeList : changeLists) {
			Map<String, List<Double>> newArgumentValues = new LinkedHashMap<String, List<Double>>();
			int i = 0;

			for (String arg : argumentValues.keySet()) {
				List<Double> newValues = new ArrayList<Double>();
				double d = changeList.get(i) * EPSILON;

				for (double v : argumentValues.get(arg)) {
					newValues.add(v + d);
				}

				newArgumentValues.put(arg, newValues);
				i++;
			}

			result.add(newArgumentValues);
		}

		return result;
	}
}
