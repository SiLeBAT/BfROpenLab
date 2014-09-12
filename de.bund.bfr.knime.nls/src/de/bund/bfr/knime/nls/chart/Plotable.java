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
package de.bund.bfr.knime.nls.chart;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import de.bund.bfr.math.DiffFunction;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.math.Transform;

public class Plotable {

	private static final int FUNCTION_STEPS = 1000;

	public static enum Type {
		DATA, FUNCTION, DATA_FUNCTION, DATA_DIFF
	}

	public static enum Status {
		OK, FAILED, NO_COVARIANCE;

		@Override
		public String toString() {
			switch (this) {
			case OK:
				return "Ok";
			case FAILED:
				return "Failed";
			case NO_COVARIANCE:
				return "No Cov. Matrix";
			}

			return super.toString();
		}
	}

	private Type type;
	private Map<String, double[]> valueLists;
	private Map<String, double[]> conditionLists;
	private String function;
	private String dependentVariable;
	private Map<String, String> functions;
	private Map<String, Double> initValues;
	private Map<String, String> initParameters;
	private String diffVariable;
	private Map<String, Double> independentVariables;
	private Map<String, Double> constants;
	private Map<String, Double> parameters;
	private Map<String, Map<String, Double>> covariances;
	private Map<String, Double> minVariables;
	private Map<String, Double> maxVariables;
	private Integer degreesOfFreedom;

	public Plotable(Type type) {
		this.type = type;
		valueLists = new LinkedHashMap<>();
		conditionLists = new LinkedHashMap<>();
		function = null;
		dependentVariable = null;
		functions = new LinkedHashMap<>();
		initValues = new LinkedHashMap<>();
		initParameters = new LinkedHashMap<>();
		diffVariable = null;
		constants = new LinkedHashMap<>();
		independentVariables = new LinkedHashMap<>();
		minVariables = new LinkedHashMap<>();
		maxVariables = new LinkedHashMap<>();
		parameters = new LinkedHashMap<>();
		covariances = new LinkedHashMap<>();
		degreesOfFreedom = null;
	}

	public Type getType() {
		return type;
	}

	public Map<String, double[]> getValueLists() {
		return valueLists;
	}

	public void setValueLists(Map<String, double[]> valueLists) {
		this.valueLists = valueLists;
	}

	public Map<String, double[]> getConditionLists() {
		return conditionLists;
	}

	public void setConditionLists(Map<String, double[]> conditionLists) {
		this.conditionLists = conditionLists;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getDependentVariable() {
		return dependentVariable;
	}

	public void setDependentVariable(String dependentVariable) {
		this.dependentVariable = dependentVariable;
	}

	public Map<String, String> getFunctions() {
		return functions;
	}

	public void setFunctions(Map<String, String> functions) {
		this.functions = functions;
	}

	public Map<String, String> getInitParameters() {
		return initParameters;
	}

	public void setInitParameters(Map<String, String> initParameters) {
		this.initParameters = initParameters;
	}

	public Map<String, Double> getInitValues() {
		return initValues;
	}

	public void setInitValues(Map<String, Double> initValues) {
		this.initValues = initValues;
	}

	public String getDiffVariable() {
		return diffVariable;
	}

	public void setDiffVariable(String diffVariable) {
		this.diffVariable = diffVariable;
	}

	public Map<String, Double> getConstants() {
		return constants;
	}

	public void setConstants(Map<String, Double> constants) {
		this.constants = constants;
	}

	public Map<String, Double> getIndependentVariables() {
		return independentVariables;
	}

	public void setIndependentVariables(Map<String, Double> independentVariables) {
		this.independentVariables = independentVariables;
	}

	public Map<String, Double> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Double> parameters) {
		this.parameters = parameters;
	}

	public Map<String, Map<String, Double>> getCovariances() {
		return covariances;
	}

	public void setCovariances(Map<String, Map<String, Double>> covariances) {
		this.covariances = covariances;
	}

	public Map<String, Double> getMinVariables() {
		return minVariables;
	}

	public void setMinVariables(Map<String, Double> minVariables) {
		this.minVariables = minVariables;
	}

	public Map<String, Double> getMaxVariables() {
		return maxVariables;
	}

	public void setMaxVariables(Map<String, Double> maxVariables) {
		this.maxVariables = maxVariables;
	}

	public Integer getDegreesOfFreedom() {
		return degreesOfFreedom;
	}

	public void setDegreesOfFreedom(Integer degreesOfFreedom) {
		this.degreesOfFreedom = degreesOfFreedom;
	}

	public double[][] getDataPoints(String paramX, String paramY,
			Transform transformX, Transform transformY) {
		double[] xList = valueLists.get(paramX);
		double[] yList = valueLists.get(paramY);

		if (xList == null || yList == null) {
			return null;
		}

		List<Point2D.Double> points = new ArrayList<>(xList.length);

		for (int i = 0; i < xList.length; i++) {
			Double x = Transform.transform(xList[i], transformX);
			Double y = Transform.transform(yList[i], transformY);

			if (MathUtils.isValidDouble(x) && MathUtils.isValidDouble(y)) {
				points.add(new Point2D.Double(x, y));
			}
		}

		Collections.sort(points, new Comparator<Point2D.Double>() {

			@Override
			public int compare(Point2D.Double p1, Point2D.Double p2) {
				return Double.compare(p1.x, p2.x);
			}
		});

		double[][] pointsArray = new double[2][points.size()];

		for (int i = 0; i < points.size(); i++) {
			pointsArray[0][i] = points.get(i).x;
			pointsArray[1][i] = points.get(i).y;
		}

		return pointsArray;
	}

	public double[][] getFunctionPoints(String paramX, Transform transformX,
			Transform transformY, double minX, double maxX, double minY,
			double maxY) throws ParseException {
		DJep parser = createParser(paramX);

		if (function == null || parser == null) {
			return null;
		}

		double[][] points = new double[2][FUNCTION_STEPS];
		Node f = parser.parse(function);

		for (int j = 0; j < FUNCTION_STEPS; j++) {
			double x = minX + (double) j / (double) (FUNCTION_STEPS - 1)
					* (maxX - minX);

			parser.setVarValue(paramX,
					Transform.inverseTransform(x, transformX));

			Object number = parser.evaluate(f);
			Double y;

			if (number instanceof Double) {
				y = Transform.transform((Double) number, transformY);

				if (!MathUtils.isValidDouble(y) || y < minY || y > maxY) {
					y = Double.NaN;
				}
			} else {
				y = Double.NaN;
			}

			points[0][j] = x;
			points[1][j] = y;
		}

		return points;
	}

	public double[][] getFunctionErrors(String paramX, Transform transformX,
			Transform transformY, double minX, double maxX, double minY,
			double maxY) throws ParseException {
		DJep parser = createParser(paramX);

		if (function == null || parser == null || covarianceMatrixMissing()) {
			return null;
		}

		double[][] points = new double[2][FUNCTION_STEPS];
		Node f = parser.parse(function);
		Map<String, Node> derivatives = new LinkedHashMap<>();
		TDistribution tDist = new TDistribution(degreesOfFreedom);

		for (String param : parameters.keySet()) {
			derivatives.put(param, parser.differentiate(f, param));
		}

		for (int n = 0; n < FUNCTION_STEPS; n++) {
			double x = minX + (double) n / (double) (FUNCTION_STEPS - 1)
					* (maxX - minX);

			parser.setVarValue(paramX,
					Transform.inverseTransform(x, transformX));

			Double y = Transform.transform(
					getError(parser, derivatives, tDist), transformY);

			if (!MathUtils.isValidDouble(y)) {
				y = Double.NaN;
			}

			points[0][n] = x;
			points[1][n] = y;
		}

		return points;
	}

	public double[][] getDiffPoints(String paramX, Transform transformX,
			Transform transformY, double minX, double maxX, double minY,
			double maxY) throws ParseException {
		if (functions.isEmpty() || !paramX.equals(diffVariable)) {
			return null;
		}

		DJep[] parsers = new DJep[functions.size()];
		Node[] fs = new Node[functions.size()];
		String[] valueVariables = new String[functions.size()];
		double[] value = new double[functions.size()];
		int depIndex = -1;
		int index = 0;

		for (String var : functions.keySet()) {
			parsers[index] = createParser(diffVariable);
			fs[index] = parsers[index].parse(functions.get(var));
			valueVariables[index] = var;

			if (initValues.containsKey(var)) {
				value[index] = initValues.get(var);
			} else {
				value[index] = parameters.get(initParameters.get(var));
			}

			if (var.equals(dependentVariable)) {
				depIndex = index;
			}

			index++;
		}

		double[][] points = new double[2][FUNCTION_STEPS];
		DiffFunction f = new DiffFunction(parsers, fs, valueVariables,
				conditionLists, diffVariable);
		ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(
				0.01);
		double diffValue = conditionLists.get(diffVariable)[0];

		for (int j = 0; j < FUNCTION_STEPS; j++) {
			double x = minX + (double) j / (double) (FUNCTION_STEPS - 1)
					* (maxX - minX);
			double transX = Transform.inverseTransform(x, transformX);
			Double y;

			if (transX == diffValue) {
				y = Transform.transform(value[depIndex], transformY);
			} else if (transX > diffValue) {
				integrator.integrate(f, diffValue, value, transX, value);
				y = Transform.transform(value[depIndex], transformY);
				diffValue = transX;
			} else {
				y = Double.NaN;
			}

			if (!MathUtils.isValidDouble(y)) {
				y = Double.NaN;
			}

			points[0][j] = x;
			points[1][j] = y;
		}

		return points;
	}

	public Status getStatus() {
		if (!isPlotable()) {
			return Status.FAILED;
		} else if (covarianceMatrixMissing()) {
			return Status.NO_COVARIANCE;
		}

		return Status.OK;
	}

	private boolean isPlotable() {
		List<Type> typesWithParams = Arrays.asList(Type.FUNCTION,
				Type.DATA_FUNCTION, Type.DATA_DIFF);
		List<Type> typesWithData = Arrays.asList(Type.DATA, Type.DATA_FUNCTION,
				Type.DATA_DIFF);

		if (typesWithParams.contains(type)
				&& parameters.values().contains(null)) {
			return false;
		}

		if (typesWithData.contains(type)) {
			if (valueLists.isEmpty()) {
				return false;
			}

			int n = valueLists.get(new ArrayList<>(valueLists.keySet()).get(0)).length;
			boolean containsData = false;

			for (int i = 0; i < n; i++) {
				boolean containsNull = false;

				for (String var : valueLists.keySet()) {
					if (!MathUtils.isValidDouble(valueLists.get(var)[i])) {
						containsNull = true;
						break;
					}
				}

				if (!containsNull) {
					containsData = true;
					break;
				}
			}

			if (!containsData) {
				return false;
			}
		}

		return true;
	}

	private boolean covarianceMatrixMissing() {
		for (String param : parameters.keySet()) {
			if (covariances.get(param) == null) {
				return true;
			}

			for (String param2 : parameters.keySet()) {
				if (covariances.get(param).get(param2) == null) {
					return true;
				}
			}
		}

		return false;
	}

	private DJep createParser(String varX) {
		DJep parser = MathUtils.createParser();

		for (String constant : constants.keySet()) {
			if (constants.get(constant) == null) {
				return null;
			}

			parser.addConstant(constant, constants.get(constant));
		}

		for (String param : parameters.keySet()) {
			if (parameters.get(param) == null) {
				return null;
			}

			parser.addConstant(param, parameters.get(param));
		}

		if (type == Type.DATA_DIFF) {
			for (String var : independentVariables.keySet()) {
				parser.addVariable(var, 0.0);
			}

			parser.addVariable(dependentVariable, 0.0);
		} else {
			for (String param : independentVariables.keySet()) {
				if (!param.equals(varX)) {
					parser.addConstant(param, independentVariables.get(param));
				}
			}

			parser.addVariable(varX, 0.0);
		}

		return parser;
	}

	private Double getError(DJep parser, Map<String, Node> derivatives,
			TDistribution tDist) throws ParseException {
		Double y = 0.0;
		List<String> paramList = new ArrayList<>(parameters.keySet());

		for (String param : paramList) {
			Object obj = parser.evaluate(derivatives.get(param));

			if (!MathUtils.isValidDouble(obj)) {
				return null;
			}

			y += (Double) obj * (Double) obj
					* covariances.get(param).get(param);
		}

		for (int i = 0; i < paramList.size() - 1; i++) {
			for (int j = i + 1; j < paramList.size(); j++) {
				Object obj1 = parser
						.evaluate(derivatives.get(paramList.get(i)));
				Object obj2 = parser
						.evaluate(derivatives.get(paramList.get(j)));

				if (!MathUtils.isValidDouble(obj1)
						|| !MathUtils.isValidDouble(obj2)) {
					return null;
				}

				double cov = covariances.get(paramList.get(i)).get(
						paramList.get(j));

				y += 2.0 * (Double) obj1 * (Double) obj2 * cov;
			}
		}

		return Math.sqrt(y)
				* tDist.inverseCumulativeProbability(1.0 - 0.05 / 2.0);
	}
}
