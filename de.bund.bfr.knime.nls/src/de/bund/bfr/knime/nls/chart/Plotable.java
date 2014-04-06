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
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import de.bund.bfr.math.MathUtilities;

public class Plotable {

	private static final int FUNCTION_STEPS = 1000;

	public static final int DATASET = 0;
	public static final int FUNCTION = 1;
	public static final int BOTH = 2;

	private int type;
	private Map<String, List<Double>> valueLists;
	private String function;
	private String functionValue;
	private Map<String, Double> functionArguments;
	private Map<String, Double> functionConstants;
	private Map<String, Double> functionParameters;
	private Map<String, Map<String, Double>> covariances;
	private Map<String, Double> minArguments;
	private Map<String, Double> maxArguments;
	private Integer degreesOfFreedom;

	public Plotable(int type) {
		this.type = type;
		valueLists = new LinkedHashMap<String, List<Double>>();
		functionConstants = new LinkedHashMap<String, Double>();
		functionArguments = new LinkedHashMap<String, Double>();
		minArguments = new LinkedHashMap<String, Double>();
		maxArguments = new LinkedHashMap<String, Double>();
		functionParameters = new LinkedHashMap<String, Double>();
		covariances = new LinkedHashMap<String, Map<String, Double>>();
		degreesOfFreedom = null;
	}

	public int getType() {
		return type;
	}

	public List<Double> getValueList(String parameter) {
		return valueLists.get(parameter);
	}

	public void addValueList(String parameter, List<Double> valueList) {
		valueLists.put(parameter, valueList);
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getFunctionValue() {
		return functionValue;
	}

	public void setFunctionValue(String functionValue) {
		this.functionValue = functionValue;
	}

	public Map<String, Double> getFunctionConstants() {
		return functionConstants;
	}

	public void setFunctionConstants(Map<String, Double> functionConstants) {
		this.functionConstants = functionConstants;
	}

	public Map<String, Double> getFunctionArguments() {
		return functionArguments;
	}

	public void setFunctionArguments(Map<String, Double> functionArguments) {
		this.functionArguments = functionArguments;
	}

	public Map<String, Double> getFunctionParameters() {
		return functionParameters;
	}

	public void setFunctionParameters(Map<String, Double> functionParameters) {
		this.functionParameters = functionParameters;
	}

	public Map<String, Map<String, Double>> getCovariances() {
		return covariances;
	}

	public void setCovariances(Map<String, Map<String, Double>> covariances) {
		this.covariances = covariances;
	}

	public Map<String, Double> getMinArguments() {
		return minArguments;
	}

	public void setMinArguments(Map<String, Double> minArguments) {
		this.minArguments = minArguments;
	}

	public Map<String, Double> getMaxArguments() {
		return maxArguments;
	}

	public void setMaxArguments(Map<String, Double> maxArguments) {
		this.maxArguments = maxArguments;
	}

	public Integer getDegreesOfFreedom() {
		return degreesOfFreedom;
	}

	public void setDegreesOfFreedom(Integer degreesOfFreedom) {
		this.degreesOfFreedom = degreesOfFreedom;
	}

	public double[][] getPoints(String paramX, String paramY,
			String transformX, String transformY) {
		List<Double> xList = valueLists.get(paramX);
		List<Double> yList = valueLists.get(paramY);

		if (xList == null || yList == null) {
			return null;
		}

		List<Point2D.Double> points = new ArrayList<Point2D.Double>(
				xList.size());

		for (int i = 0; i < xList.size(); i++) {
			Double x = xList.get(i);
			Double y = yList.get(i);

			if (x != null) {
				x = transform(x, transformX);
			}

			if (y != null) {
				y = transform(y, transformY);
			}

			if (isValidValue(x) && isValidValue(y)) {
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

	public double[][] getFunctionPoints(String paramX, String paramY,
			String transformX, String transformY, double minX, double maxX,
			double minY, double maxY) {
		if (function == null) {
			return null;
		}

		double[][] points = new double[2][FUNCTION_STEPS];
		DJep parser = MathUtilities.createParser();
		Node f = null;

		for (String constant : functionConstants.keySet()) {
			if (functionConstants.get(constant) == null) {
				return null;
			}

			parser.addConstant(constant, functionConstants.get(constant));
		}

		for (String param : functionParameters.keySet()) {
			if (functionParameters.get(param) == null) {
				return null;
			}

			parser.addConstant(param, functionParameters.get(param));
		}

		for (String param : functionArguments.keySet()) {
			if (!param.equals(paramX)) {
				parser.addConstant(param, functionArguments.get(param));
			}
		}

		parser.addVariable(paramX, 0.0);

		try {
			f = parser.parse(function);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		for (int j = 0; j < FUNCTION_STEPS; j++) {
			double x = minX + (double) j / (double) (FUNCTION_STEPS - 1)
					* (maxX - minX);

			parser.setVarValue(paramX, inverseTransform(x, transformX));

			try {
				Object number = parser.evaluate(f);
				Double y;

				if (number instanceof Double) {
					y = (Double) number;
					y = transform(y, transformY);

					if (y == null || y < minY || y > maxY || y.isInfinite()) {
						y = Double.NaN;
					}
				} else {
					y = Double.NaN;
				}

				points[0][j] = x;
				points[1][j] = y;
			} catch (ParseException e) {
				points[0][j] = x;
				points[1][j] = Double.NaN;
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
		}

		return points;
	}

	public double[][] getFunctionErrors(String paramX, String paramY,
			String transformX, String transformY, double minX, double maxX,
			double minY, double maxY) {
		if (function == null) {
			return null;
		}

		double[][] points = new double[2][FUNCTION_STEPS];
		DJep parser = MathUtilities.createParser();
		Node f = null;

		for (String constant : functionConstants.keySet()) {
			if (functionConstants.get(constant) == null) {
				return null;
			}

			parser.addConstant(constant, functionConstants.get(constant));
		}

		for (String param : functionParameters.keySet()) {
			if (functionParameters.get(param) == null
					|| covariances.get(param) == null) {
				return null;
			}

			for (String param2 : functionParameters.keySet()) {
				if (covariances.get(param).get(param2) == null) {
					return null;
				}
			}

			parser.addConstant(param, functionParameters.get(param));
		}

		for (String param : functionArguments.keySet()) {
			if (!param.equals(paramX)) {
				parser.addConstant(param, functionArguments.get(param));
			}
		}

		Map<String, Node> derivatives = new LinkedHashMap<String, Node>();

		parser.addVariable(paramX, 0.0);

		try {
			f = parser.parse(function);

			for (String param : functionParameters.keySet()) {
				derivatives.put(param, parser.differentiate(f, param));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		for (int n = 0; n < FUNCTION_STEPS; n++) {
			double x = minX + (double) n / (double) (FUNCTION_STEPS - 1)
					* (maxX - minX);

			parser.setVarValue(paramX, inverseTransform(x, transformX));

			try {
				Double y = 0.0;
				boolean failed = false;
				List<String> paramList = new ArrayList<String>(
						functionParameters.keySet());

				for (String param : paramList) {
					Object obj = parser.evaluate(derivatives.get(param));

					if (!(obj instanceof Double)) {
						failed = true;
						break;
					}

					y += (Double) obj * (Double) obj
							* covariances.get(param).get(param);
				}

				for (int i = 0; i < paramList.size() - 1; i++) {
					for (int j = i + 1; j < paramList.size(); j++) {
						Object obj1 = parser.evaluate(derivatives.get(paramList
								.get(i)));
						Object obj2 = parser.evaluate(derivatives.get(paramList
								.get(j)));

						if (!(obj1 instanceof Double)
								|| !(obj2 instanceof Double)) {
							failed = true;
							break;
						}

						double cov = covariances.get(paramList.get(i)).get(
								paramList.get(j));

						y += 2.0 * (Double) obj1 * (Double) obj2 * cov;
					}
				}

				points[0][n] = x;

				if (!failed) {
					// 95% interval
					TDistribution dist = new TDistribution(degreesOfFreedom);

					y = Math.sqrt(y)
							* dist.inverseCumulativeProbability(1.0 - 0.05 / 2.0);
					y = transform(y, transformY);

					if (y != null) {
						points[1][n] = y;
					} else {
						points[1][n] = Double.NaN;
					}
				} else {
					points[1][n] = Double.NaN;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
		}

		return points;
	}

	public boolean isPlotable() {
		if (type == FUNCTION) {
			for (String param : functionParameters.keySet()) {
				if (functionParameters.get(param) == null) {
					return false;
				}
			}

			return true;
		} else {
			List<String> paramsX = new ArrayList<String>(valueLists.keySet());
			List<String> paramsY = new ArrayList<String>();

			if (type == DATASET) {
				paramsY = paramsX;
			} else if (type == BOTH) {
				if (functionValue != null) {
					paramsY = Arrays.asList(functionValue);
				}
			}

			for (String paramX : paramsX) {
				for (String paramY : paramsY) {
					if (isPlotable(paramX, paramY)) {
						return true;
					}
				}
			}

			return false;
		}
	}

	private boolean isPlotable(String paramX, String paramY) {
		boolean dataSetPlotable = false;
		boolean functionPlotable = false;
		List<Double> xs = valueLists.get(paramX);
		List<Double> ys = valueLists.get(paramY);

		if (xs != null && ys != null) {
			for (int i = 0; i < xs.size(); i++) {
				if (isValidValue(xs.get(i)) && isValidValue(ys.get(i))) {
					dataSetPlotable = true;
					break;
				}
			}
		}

		if (function != null && functionValue.equals(paramY)
				&& functionArguments.containsKey(paramX)) {
			boolean notValid = false;

			for (Double value : functionParameters.values()) {
				if (!isValidValue(value)) {
					notValid = true;
					break;
				}
			}

			if (!notValid) {
				functionPlotable = true;
			}
		}

		if (type == DATASET) {
			return dataSetPlotable;
		} else if (type == FUNCTION) {
			return functionPlotable;
		} else if (type == BOTH) {
			return dataSetPlotable && functionPlotable;
		}

		return false;
	}

	private boolean isValidValue(Double value) {
		return value != null && !value.isNaN() && !value.isInfinite();
	}

	public static Double transform(Double value, String transform) {
		if (value == null) {
			return null;
		} else if (transform.equals(ChartUtilities.NO_TRANSFORM)) {
			return value;
		} else if (transform.equals(ChartUtilities.SQRT_TRANSFORM)) {
			return Math.sqrt(value);
		} else if (transform.equals(ChartUtilities.LN_TRANSFORM)) {
			return Math.log(value);
		} else if (transform.equals(ChartUtilities.LOG_TRANSFORM)) {
			return Math.log10(value);
		} else if (transform.equals(ChartUtilities.EXP_TRANSFORM)) {
			return Math.exp(value);
		} else if (transform.equals(ChartUtilities.EXP10_TRANSFORM)) {
			return Math.pow(10.0, value);
		} else if (transform.equals(ChartUtilities.DIVX_TRANSFORM)) {
			return 1 / value;
		} else if (transform.equals(ChartUtilities.DIVX2_TRANSFORM)) {
			return 1 / value / value;
		}

		return null;
	}

	public static Double inverseTransform(Double value, String transform) {
		if (value == null) {
			return null;
		} else if (transform.equals(ChartUtilities.NO_TRANSFORM)) {
			return value;
		} else if (transform.equals(ChartUtilities.SQRT_TRANSFORM)) {
			return value * value;
		} else if (transform.equals(ChartUtilities.LN_TRANSFORM)) {
			return Math.exp(value);
		} else if (transform.equals(ChartUtilities.LOG_TRANSFORM)) {
			return Math.pow(10.0, value);
		} else if (transform.equals(ChartUtilities.EXP_TRANSFORM)) {
			return Math.log(value);
		} else if (transform.equals(ChartUtilities.EXP10_TRANSFORM)) {
			return Math.log10(value);
		} else if (transform.equals(ChartUtilities.DIVX_TRANSFORM)) {
			return 1 / value;
		} else if (transform.equals(ChartUtilities.DIVX2_TRANSFORM)) {
			return 1 / Math.sqrt(value);
		}

		return null;
	}
}