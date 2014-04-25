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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.nfunk.jep.ParseException;

public class Test {

	public static void main(String[] args) {
		try {
			diff();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void nls() throws ParseException {
		String formula = "a*x^2+b*x+c";
		List<String> parameters = Arrays.asList("a", "b", "c");
		Map<String, Double> minParams = new LinkedHashMap<String, Double>();
		Map<String, Double> maxParams = new LinkedHashMap<String, Double>();
		List<Double> targetValues = Arrays.asList(1.0, 5.0, 8.0);
		Map<String, List<Double>> argumentValues = new LinkedHashMap<String, List<Double>>();

		argumentValues.put("x", Arrays.asList(0.0, 2.0, 3.0));

		ParameterOptimizer optimizer = new ParameterOptimizer(formula,
				parameters, minParams, maxParams, minParams, maxParams,
				targetValues, argumentValues, false);

		optimizer.optimize(1, 1, true);

		System.out.println(optimizer.getParameterValues());
	}

	private static void diff() throws ParseException {
		String formula = "-1/Dref*exp(ln(10)/z*(T-Tref))";
		List<String> parameters = Arrays.asList("Dref", "Tref", "z");
		List<Double> timeValues = new ArrayList<Double>();
		Map<String, List<Double>> variableValues = new LinkedHashMap<String, List<Double>>();

		variableValues.put("T", new ArrayList<Double>());

		for (int i = 0; i < 100; i += 10) {
			timeValues.add((double) i);
			variableValues.get("T").add(20.0 + 0.1 * i);
		}

		VectorDiffFunction f = new VectorDiffFunction(formula, parameters, "y",
				"time", timeValues, variableValues, 10.0);
		VectorDiffFunctionJacobian f2 = new VectorDiffFunctionJacobian(formula,
				parameters, "y", "time", timeValues, variableValues, 10.0);

		double[] guess = new double[] { 5.0, 55.0, 17.0 };
		double[] target = f.value(new double[] { 10.0, 30.0, 10.0 });
		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
		PointVectorValuePair p = optimizer.optimize(new ModelFunction(f),
				new ModelFunctionJacobian(f2), new MaxEval(100), new Target(
						target), new Weight(new double[] { 1.0, 1.0, 1.0, 1.0,
						1.0, 1.0, 1.0, 1.0, 1.0, 1.0 }),
				new InitialGuess(guess));

		System.out.println(rmse(target, f.value(guess), 3));
		System.out.println(Arrays.asList(ArrayUtils.toObject(f.value(p
				.getPoint()))));
		System.out.println(rmse(target, f.value(p.getPoint()), 3));
	}

	private static double rmse(double[] v1, double[] v2, int n) {
		double error = 0.0;

		for (int i = 0; i < n; i++) {
			error += (v1[i] - v2[i]) * (v1[i] - v2[i]);
		}

		return Math.sqrt(error);
	}
}
