/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.chart.ChartUtils;
import de.bund.bfr.knime.chart.NamedShape;
import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.math.MathUtils;

public class ChartCreator extends JPanel {

	private static final long serialVersionUID = 1L;

	private ChartPanel chartPanel;

	private Map<String, Plotable> plotables;
	private Map<String, String> legend;
	private Map<String, Color> colors;
	private Map<String, NamedShape> shapes;
	private Map<String, List<Color>> colorLists;
	private Map<String, List<NamedShape>> shapeLists;

	private Plotable.Variable varX;
	private Plotable.Variable varY;
	private boolean useManualRange;
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	private boolean drawLines;
	private boolean showLegend;
	private boolean showConfidence;
	private boolean showPrediction;
	private int resolution;

	public ChartCreator(Map<String, Plotable> plotables, Map<String, String> legend) {
		this.plotables = plotables;
		this.legend = legend;
		colors = new LinkedHashMap<>();
		shapes = new LinkedHashMap<>();
		colorLists = new LinkedHashMap<>();
		shapeLists = new LinkedHashMap<>();

		chartPanel = new ChartPanel(new JFreeChart(new XYPlot())) {

			private static final long serialVersionUID = 1L;

			@Override
			public void mouseReleased(MouseEvent e) {
				ValueAxis domainAxis = ((XYPlot) getChart().getPlot()).getDomainAxis();
				ValueAxis rangeAxis = ((XYPlot) getChart().getPlot()).getRangeAxis();

				Range xRange1 = domainAxis.getRange();
				Range yRange1 = rangeAxis.getRange();
				super.mouseReleased(e);
				Range xRange2 = domainAxis.getRange();
				Range yRange2 = rangeAxis.getRange();

				if (!xRange1.equals(xRange2) || !yRange1.equals(yRange2)) {
					minX = xRange2.getLowerBound();
					maxX = xRange2.getUpperBound();
					minY = yRange2.getLowerBound();
					maxY = yRange2.getUpperBound();
					fireZoomChanged();
				}
			}
		};
		chartPanel.getPopupMenu().removeAll();

		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
	}

	public void addZoomListener(ZoomListener listener) {
		listenerList.add(ZoomListener.class, listener);
	}

	public void removeZoomListener(ZoomListener listener) {
		listenerList.remove(ZoomListener.class, listener);
	}

	public void setChart(JFreeChart chart) {
		chartPanel.setChart(chart);
	}

	public void createEmptyChart() {
		setChart(new JFreeChart(new XYPlot()));
	}

	public void createChart(String idToPaint) throws ParseException, UnitException {
		setChart(getChart(idToPaint));
	}

	public void createChart(List<String> idsToPaint) throws ParseException, UnitException {
		setChart(getChart(idsToPaint));
	}

	public JFreeChart getChart(String idToPaint) throws ParseException, UnitException {
		if (idToPaint != null) {
			return getChart(Arrays.asList(idToPaint));
		} else {
			return getChart(new ArrayList<>(0));
		}
	}

	public JFreeChart getChart(List<String> idsToPaint) throws ParseException, UnitException {
		if (varX == null || varY == null || varX.getName() == null || varY.getName() == null) {
			return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, new XYPlot(), showLegend);
		}

		NumberAxis xAxis = new NumberAxis(varX.getDisplayString());
		NumberAxis yAxis = new NumberAxis(varY.getDisplayString());
		XYPlot plot = new XYPlot(null, xAxis, yAxis, null);
		double usedMinX = Double.POSITIVE_INFINITY;
		double usedMaxX = Double.NEGATIVE_INFINITY;
		int index = 0;
		List<Color> defaultColors = ChartUtils.createColorList(idsToPaint.size());
		List<NamedShape> defaultShapes = ChartUtils.createShapeList(idsToPaint.size());

		for (String id : idsToPaint) {
			Plotable plotable = plotables.get(id);

			if (plotable == null) {
				continue;
			}

			if (plotable.getType() == Plotable.Type.BOTH || plotable.getType() == Plotable.Type.BOTH_MANY
					|| plotable.getType() == Plotable.Type.FUNCTION
					|| plotable.getType() == Plotable.Type.FUNCTION_SAMPLE) {
				double minArg = varX.to(MathUtils.nullToNan(plotable.getMinValues().get(varX.getName())),
						plotable.getUnits().get(varX.getName()));
				double maxArg = varX.to(MathUtils.nullToNan(plotable.getMaxValues().get(varX.getName())),
						plotable.getUnits().get(varX.getName()));

				if (Double.isFinite(minArg)) {
					usedMinX = Math.min(usedMinX, minArg);
				}

				if (Double.isFinite(maxArg)) {
					usedMaxX = Math.max(usedMaxX, maxArg);
				}
			}

			if (plotable.getType() == Plotable.Type.BOTH || plotable.getType() == Plotable.Type.BOTH_MANY) {
				for (Map<String, Integer> choice : plotable.getAllChoices(varX.getName())) {
					double[][] points = plotable.getPoints(varX, varY, choice);

					if (points != null) {
						for (int i = 0; i < points[0].length; i++) {
							usedMinX = Math.min(usedMinX, points[0][i]);
							usedMaxX = Math.max(usedMaxX, points[0][i]);
						}
					}
				}
			}

			if (plotable.getType() == Plotable.Type.DATASET || plotable.getType() == Plotable.Type.DATASET_MANY) {
				double[][] points = plotable.getPoints(varX, varY);

				if (points != null) {
					for (int i = 0; i < points[0].length; i++) {
						usedMinX = Math.min(usedMinX, points[0][i]);
						usedMaxX = Math.max(usedMaxX, points[0][i]);
					}
				}
			}

			if (plotable.getType() == Plotable.Type.FUNCTION_SAMPLE) {
				for (Double x : plotable.getSamples()) {
					if (x != null && Double.isFinite(x)) {
						usedMinX = Math.min(usedMinX, x);
						usedMaxX = Math.max(usedMaxX, x);
					}
				}
			}
		}

		if (Double.isInfinite(usedMinX)) {
			usedMinX = 0.0;
		}

		if (Double.isInfinite(usedMaxX)) {
			usedMaxX = 100.0;
		}

		if (varX.getName().equals(PmmUtils.TIME) || varX.getName().equals(PmmUtils.CONCENTRATION)) {
			usedMinX = Math.min(usedMinX, 0.0);
			xAxis.setAutoRangeIncludesZero(true);
		} else {
			xAxis.setAutoRangeIncludesZero(false);
		}

		if (varY.getName().equals(PmmUtils.TIME) || varY.getName().equals(PmmUtils.CONCENTRATION)) {
			yAxis.setAutoRangeIncludesZero(true);
		} else {
			yAxis.setAutoRangeIncludesZero(false);
		}

		if (usedMinX == usedMaxX) {
			usedMinX -= 1.0;
			usedMaxX += 1.0;
		}

		if (useManualRange && minX < maxX && minY < maxY) {
			usedMinX = minX;
			usedMaxX = maxX;
			xAxis.setRange(new Range(minX, maxX));
			yAxis.setRange(new Range(minY, maxY));
		}

		for (String id : idsToPaint) {
			Plotable plotable = plotables.get(id);

			if (plotable == null) {
				continue;
			}

			plotable.setFunctionSteps(resolution);

			switch (plotable.getType()) {
			case DATASET:
				plotDataSet(plot, plotable, id, defaultColors.get(index), defaultShapes.get(index));
				break;
			case DATASET_MANY:
				plotDataSetStrict(plot, plotable, id);
				break;
			case FUNCTION:
				plotFunction(plot, plotable, id, defaultColors.get(index), defaultShapes.get(index), usedMinX,
						usedMaxX);
				break;
			case FUNCTION_SAMPLE:
				plotFunctionSample(plot, plotable, id, defaultColors.get(index), defaultShapes.get(index), usedMinX,
						usedMaxX);
				break;
			case BOTH:
				plotBoth(plot, plotable, id, defaultColors.get(index), defaultShapes.get(index), usedMinX, usedMaxX);
				break;
			case BOTH_MANY:
				plotBothStrict(plot, plotable, id, usedMinX, usedMaxX);
				break;
			default:
				throw new RuntimeException("Unknown type of plotable: " + plotable.getType());
			}

			index++;
		}

		return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
	}

	public void setVarX(Plotable.Variable varX) {
		this.varX = varX;
	}

	public void setVarY(Plotable.Variable varY) {
		this.varY = varY;
	}

	public void setManualRange(boolean useManualRange) {
		this.useManualRange = useManualRange;
	}

	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	public void setDrawLines(boolean drawLines) {
		this.drawLines = drawLines;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	public void setShowConfidence(boolean showConfidence) {
		this.showConfidence = showConfidence;
	}

	public void setShowPrediction(boolean showPrediction) {
		this.showPrediction = showPrediction;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public void setColors(Map<String, Color> colors) {
		this.colors = colors;
	}

	public void setShapes(Map<String, NamedShape> shapes) {
		this.shapes = shapes;
	}

	public void setColorLists(Map<String, List<Color>> colorLists) {
		this.colorLists = colorLists;
	}

	public void setShapeLists(Map<String, List<NamedShape>> shapeLists) {
		this.shapeLists = shapeLists;
	}

	private void plotDataSet(XYPlot plot, Plotable plotable, String id, Color defaultColor, NamedShape defaultShape)
			throws UnitException {
		XYDataset dataSet = createDataSet(plotable, id);
		XYItemRenderer renderer = createRenderer(id, defaultColor, defaultShape, dataSet);

		if (dataSet != null) {
			ChartUtils.addDataSetToPlot(plot, dataSet, renderer);
		}
	}

	private void plotDataSetStrict(XYPlot plot, Plotable plotable, String id) throws UnitException {
		List<XYDataset> dataSets = createStrictDataSets(plotable, id);
		List<XYItemRenderer> renderers = createStrictRenderers(id, dataSets.size());

		for (int i = 0; i < dataSets.size(); i++) {
			if (dataSets.get(i) != null) {
				ChartUtils.addDataSetToPlot(plot, dataSets.get(i), renderers.get(i));
			}
		}
	}

	private void plotFunction(XYPlot plot, Plotable plotable, String id, Color defaultColor, NamedShape defaultShape,
			double minX, double maxX) throws ParseException, UnitException {
		XYDataset dataSet = createFunctionDataSet(plotable, id, minX, maxX);
		XYItemRenderer renderer = createFunctionRenderer(id, defaultColor, defaultShape, dataSet);

		if (dataSet != null) {
			ChartUtils.addDataSetToPlot(plot, dataSet, renderer);
		}
	}

	private void plotFunctionSample(XYPlot plot, Plotable plotable, String id, Color defaultColor,
			NamedShape defaultShape, double minX, double maxX) throws ParseException, UnitException {
		XYDataset functionDataSet = createFunctionDataSet(plotable, id, minX, maxX);
		XYItemRenderer functionRenderer = createFunctionRenderer(id, defaultColor, defaultShape, functionDataSet);
		XYDataset sampleDataSet = createSampleDataSet(plotable, id, minX, maxX);
		XYItemRenderer renderer = createRenderer(id, defaultColor, defaultShape, sampleDataSet);

		if (functionDataSet != null) {
			if (sampleDataSet != null) {
				functionRenderer.setBaseSeriesVisibleInLegend(false);
			}

			ChartUtils.addDataSetToPlot(plot, functionDataSet, functionRenderer);
		}

		if (sampleDataSet != null) {
			ChartUtils.addDataSetToPlot(plot, sampleDataSet, renderer);
		}
	}

	private void plotBoth(XYPlot plot, Plotable plotable, String id, Color defaultColor, NamedShape defaultShape,
			double minX, double maxX) throws ParseException, UnitException {
		XYDataset functionDataSet = createFunctionDataSet(plotable, id, minX, maxX);
		XYItemRenderer functionRenderer = createFunctionRenderer(id, defaultColor, defaultShape, functionDataSet);
		XYDataset dataSet = createDataSet(plotable, id);
		XYItemRenderer renderer = createRenderer(id, defaultColor, defaultShape, dataSet);

		if (functionDataSet != null) {
			if (dataSet != null) {
				functionRenderer.setBaseSeriesVisibleInLegend(false);
			}

			ChartUtils.addDataSetToPlot(plot, functionDataSet, functionRenderer);
		}

		if (dataSet != null) {
			ChartUtils.addDataSetToPlot(plot, dataSet, renderer);
		}
	}

	private void plotBothStrict(XYPlot plot, Plotable plotable, String id, double minX, double maxX)
			throws ParseException, UnitException {
		List<XYDataset> functionDataSets = createStrictFunctionDataSets(plotable, id, minX, maxX);
		List<XYItemRenderer> functionRenderers = createStrictFunctionRenderers(id, functionDataSets);
		List<XYDataset> dataSets = createStrictDataSets(plotable, id);
		List<XYItemRenderer> renderers = createStrictRenderers(id, dataSets.size());

		for (int i = 0; i < functionDataSets.size(); i++) {
			if (functionDataSets.get(i) != null) {
				if (dataSets.get(i) != null) {
					functionRenderers.get(i).setBaseSeriesVisibleInLegend(false);
				}

				ChartUtils.addDataSetToPlot(plot, functionDataSets.get(i), functionRenderers.get(i));
			}

			if (dataSets.get(i) != null) {
				ChartUtils.addDataSetToPlot(plot, dataSets.get(i), renderers.get(i));
			}
		}
	}

	private XYDataset createDataSet(Plotable plotable, String id) throws UnitException {
		double[][] points = plotable.getPoints(varX, varY);

		if (points != null) {
			DefaultXYDataset dataSet = new DefaultXYDataset();

			dataSet.addSeries(legend.get(id), points);

			return dataSet;
		}

		return null;
	}

	private XYItemRenderer createRenderer(String id, Color defaultColor, NamedShape defaultShape, XYDataset dataSet) {
		Color color = colors.containsKey(id) ? colors.get(id) : defaultColor;
		NamedShape shape = shapes.containsKey(id) ? shapes.get(id) : defaultShape;

		if (dataSet instanceof YIntervalSeriesCollection) {
			DeviationRenderer renderer = new DeviationRenderer(drawLines, !drawLines);

			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
			renderer.setSeriesPaint(0, color);
			renderer.setSeriesFillPaint(0, color);
			renderer.setSeriesShape(0, shape.getShape());

			return renderer;
		} else {
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(drawLines, !drawLines);

			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
			renderer.setSeriesPaint(0, color);
			renderer.setSeriesShape(0, shape.getShape());

			return renderer;
		}
	}

	private XYDataset createFunctionDataSet(Plotable plotable, String id, double minX, double maxX)
			throws ParseException, UnitException {
		double[][] points = plotable.getFunctionPoints(varX, varY, minX, maxX);

		if (points == null) {
			return null;
		}

		double[][] functionErrors = null;

		if (showConfidence || showPrediction) {
			functionErrors = plotable.getFunctionErrors(varX, varY, minX, maxX, showPrediction);
		}

		if (functionErrors != null) {
			YIntervalSeriesCollection functionDataset = new YIntervalSeriesCollection();
			YIntervalSeries series = new YIntervalSeries(legend.get(id));

			for (int j = 0; j < points[0].length; j++) {
				double error = Double.isNaN(functionErrors[1][j]) ? 0.0 : functionErrors[1][j];

				series.add(points[0][j], points[1][j], points[1][j] - error, points[1][j] + error);
			}

			functionDataset.addSeries(series);

			return functionDataset;
		} else {
			DefaultXYDataset dataSet = new DefaultXYDataset();

			dataSet.addSeries(legend.get(id), points);

			return dataSet;
		}
	}

	private XYItemRenderer createFunctionRenderer(String id, Color defaultColor, NamedShape defaultShape,
			XYDataset dataSet) {
		Color color = colors.containsKey(id) ? colors.get(id) : defaultColor;
		NamedShape shape = shapes.containsKey(id) ? shapes.get(id) : defaultShape;

		if (dataSet instanceof YIntervalSeriesCollection) {
			DeviationRenderer functionRenderer = new DeviationRenderer(true, false);

			functionRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
			functionRenderer.setSeriesPaint(0, color);
			functionRenderer.setSeriesFillPaint(0, color);
			functionRenderer.setSeriesShape(0, shape.getShape());

			return functionRenderer;
		} else {
			XYLineAndShapeRenderer functionRenderer = new XYLineAndShapeRenderer(true, false);

			functionRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
			functionRenderer.setSeriesPaint(0, color);
			functionRenderer.setSeriesShape(0, shape.getShape());

			return functionRenderer;
		}
	}

	private XYDataset createSampleDataSet(Plotable plotable, String id, double minX, double maxX)
			throws ParseException, UnitException {
		double[][] samplePoints = plotable.getFunctionSamplePoints(varX, varY, minX, maxX);

		if (samplePoints != null) {
			DefaultXYDataset sampleDataset = new DefaultXYDataset();

			sampleDataset.addSeries(legend.get(id), samplePoints);

			return sampleDataset;
		}

		return null;
	}

	private List<XYDataset> createStrictDataSets(Plotable plotable, String id) throws UnitException {
		List<XYDataset> dataSets = new ArrayList<>();

		for (Map<String, Integer> choiceMap : plotable.getAllChoices(varX.getName())) {
			double[][] dataPoints = plotable.getPoints(varX, varY, choiceMap);

			if (dataPoints == null) {
				dataSets.add(null);
				continue;
			}

			DefaultXYDataset dataSet = new DefaultXYDataset();
			StringBuilder addLegend = new StringBuilder();

			for (Map.Entry<String, Integer> entry : choiceMap.entrySet()) {
				if (!entry.getKey().equals(varX.getName())) {
					Double value = plotable.getVariables().get(entry.getKey()).get(entry.getValue());

					if (value != null) {
						String s = NumberFormat.getInstance(Locale.US).format(value);

						addLegend.append(" (" + entry.getKey() + "=" + s + ")");
					}
				}
			}

			dataSet.addSeries(legend.get(id) + addLegend, dataPoints);
			dataSets.add(dataSet);
		}

		return dataSets;
	}

	private List<XYItemRenderer> createStrictRenderers(String id, int n) {
		List<XYItemRenderer> renderers = new ArrayList<>();
		List<Color> colorList = colorLists.containsKey(id) ? colorLists.get(id) : ChartUtils.createColorList(n);
		List<NamedShape> shapeList = shapeLists.containsKey(id) ? shapeLists.get(id) : ChartUtils.createShapeList(n);

		for (int i = 0; i < n; i++) {
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(drawLines, true);

			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
			renderer.setSeriesPaint(0, colorList.get(i));
			renderer.setSeriesShape(0, shapeList.get(i).getShape());
			renderers.add(renderer);
		}

		return renderers;
	}

	private List<XYDataset> createStrictFunctionDataSets(Plotable plotable, String id, double minX, double maxX)
			throws ParseException, UnitException {
		List<XYDataset> dataSets = new ArrayList<>();

		for (Map<String, Integer> choiceMap : plotable.getAllChoices(varX.getName())) {
			double[][] modelPoints = plotable.getFunctionPoints(varX, varY, minX, maxX, choiceMap);

			if (modelPoints == null) {
				dataSets.add(null);
				continue;
			}

			double[][] modelErrors = null;

			if (showConfidence || showPrediction) {
				modelErrors = plotable.getFunctionErrors(varX, varY, minX, maxX, showPrediction, choiceMap);
			}

			StringBuilder addLegend = new StringBuilder();

			for (Map.Entry<String, Integer> entry : choiceMap.entrySet()) {
				if (!entry.getKey().equals(varX.getName())) {
					Double value = plotable.getVariables().get(entry.getKey()).get(entry.getValue());

					if (value != null) {
						String s = NumberFormat.getInstance(Locale.US).format(value);

						addLegend.append(" (" + entry.getKey() + "=" + s + ")");
					}
				}
			}

			if (modelErrors != null) {
				YIntervalSeriesCollection dataSet = new YIntervalSeriesCollection();
				YIntervalSeries series = new YIntervalSeries(legend.get(id));

				for (int j = 0; j < modelPoints[0].length; j++) {
					double error = Double.isNaN(modelErrors[1][j]) ? 0.0 : modelErrors[1][j];

					series.add(modelPoints[0][j], modelPoints[1][j], modelPoints[1][j] - error,
							modelPoints[1][j] + error);
				}

				dataSet.addSeries(series);
				dataSets.add(dataSet);
			} else {
				DefaultXYDataset dataSet = new DefaultXYDataset();

				dataSet.addSeries(legend.get(id) + addLegend, modelPoints);

				dataSets.add(dataSet);
			}
		}

		return dataSets;
	}

	private List<XYItemRenderer> createStrictFunctionRenderers(String id, List<XYDataset> dataSets) {
		List<XYItemRenderer> renderers = new ArrayList<>();
		List<Color> colorList = colorLists.get(id);
		List<NamedShape> shapeList = shapeLists.get(id);

		if (colorList == null || colorList.isEmpty()) {
			colorList = ChartUtils.createColorList(dataSets.size());
		}

		if (shapeList == null || shapeList.isEmpty()) {
			shapeList = ChartUtils.createShapeList(dataSets.size());
		}

		for (int i = 0; i < dataSets.size(); i++) {
			if (dataSets.get(i) instanceof YIntervalSeriesCollection) {
				DeviationRenderer renderer = new DeviationRenderer(true, false);

				renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
				renderer.setSeriesPaint(0, colorList.get(i));
				renderer.setSeriesFillPaint(0, colorList.get(i));
				renderer.setSeriesShape(0, shapeList.get(i).getShape());

				renderers.add(renderer);
			} else {
				XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);

				renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
				renderer.setSeriesPaint(0, colorList.get(i));
				renderer.setSeriesShape(0, shapeList.get(i).getShape());

				renderers.add(renderer);
			}
		}

		return renderers;
	}

	private void fireZoomChanged() {
		Stream.of(getListeners(ZoomListener.class)).forEach(l -> l.zoomChanged(this));
	}

	public static interface ZoomListener extends EventListener {

		void zoomChanged(ChartCreator source);
	}
}
