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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nfunk.jep.ParseException;

public class Test {

	public static void main(String[] args) {
		try {
			nls();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		try {
			diff();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private static void nls() throws ParseException {
		String formula = "a*x^2+b*x+c";
		List<String> parameters = Arrays.asList("a", "b", "c");
		Map<String, Double> minParams = new LinkedHashMap<String, Double>();
		Map<String, Double> maxParams = new LinkedHashMap<String, Double>();
		List<Double> targetValues = Arrays.asList(1.0, 5.0, 8.0, 17.0, 30.0);
		Map<String, List<Double>> argumentValues = new LinkedHashMap<String, List<Double>>();

		argumentValues.put("x", Arrays.asList(0.0, 2.0, 3.0, 4.0, 5.0));

		ParameterOptimizer optimizer = new ParameterOptimizer(formula,
				parameters, minParams, maxParams, minParams, maxParams,
				targetValues, argumentValues, false);

		optimizer.optimize(1, 1, true);

		System.out.println(optimizer.getParameterValues());
		System.out.println(optimizer.getRMSE());
	}

	private static void diff() throws ParseException {
		String formula = "-1/Dref*exp(ln(10)/z*(T-Tref))";
		List<String> parameters = Arrays.asList("Dref", "Tref", "z");
		Map<String, Double> minParams = new LinkedHashMap<String, Double>();
		Map<String, Double> maxParams = new LinkedHashMap<String, Double>();

		minParams.put("Dref", 0.0);
		minParams.put("Tref", 0.0);
		minParams.put("z", 0.0);
		maxParams.put("Dref", 50.0);
		maxParams.put("Tref", 50.0);
		maxParams.put("z", 50.0);

		Map<String, List<Double>> variableValues = new LinkedHashMap<String, List<Double>>();

		variableValues.put("time", new ArrayList<Double>());
		variableValues.put("T", new ArrayList<Double>());

		for (int i = 0; i < 100; i += 10) {
			variableValues.get("time").add((double) i);
			variableValues.get("T").add(20.0 + 0.1 * i);
		}

		List<Double> targetValues = new ArrayList<Double>();

		for (double v : new VectorDiffFunction(formula, parameters, "y",
				"time", variableValues, 10.0).value(new double[] { 9.43,
				23.4355, 10.43534 })) {
			targetValues.add(v);
		}

		ParameterOptimizer optimizer = new ParameterOptimizer(formula,
				parameters, minParams, maxParams, targetValues, "y", "time",
				variableValues);

		optimizer.optimize(100, 1, true);

		System.out.println(optimizer.getParameterValues());
		System.out.println(optimizer.getRMSE());
	}
}
