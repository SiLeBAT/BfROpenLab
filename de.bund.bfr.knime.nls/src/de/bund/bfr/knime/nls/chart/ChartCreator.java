/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.nls.chart;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.nfunk.jep.ParseException;

import de.bund.bfr.knime.chart.ChartUtils;
import de.bund.bfr.knime.chart.NamedShape;
import de.bund.bfr.math.InterpolationFactory;
import de.bund.bfr.math.Transform;

public class ChartCreator extends ChartPanel {

	private static final long serialVersionUID = 1L;

	private List<ZoomListener> zoomListeners;

	private Map<String, Plotable> plotables;
	private Map<String, String> legend;
	private Map<String, Color> colors;
	private Map<String, NamedShape> shapes;
	private boolean selectAll;
	private List<String> selectedIds;

	private String varX;
	private String varY;
	private Transform transformX;
	private Transform transformY;
	private boolean minToZero;
	private boolean useManualRange;
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	private boolean drawLines;
	private boolean showLegend;
	private boolean showConfidenceInterval;
	private int resolution;
	private InterpolationFactory.Type interpolator;

	public ChartCreator(Map<String, Plotable> plotables, Map<String, String> legend) {
		super(new JFreeChart(new XYPlot()));
		this.plotables = plotables;
		this.legend = legend;
		colors = new LinkedHashMap<>();
		shapes = new LinkedHashMap<>();
		zoomListeners = new ArrayList<>();
		getPopupMenu().removeAll();
	}

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

	public void addZoomListener(ZoomListener listener) {
		zoomListeners.add(listener);
	}

	public void removeZoomListener(ZoomListener listener) {
		zoomListeners.remove(listener);
	}

	public JFreeChart createChart() throws ParseException {
		if (varX == null || varY == null) {
			return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, new XYPlot(), showLegend);
		}

		List<String> idsToPaint;

		if (selectAll) {
			idsToPaint = new ArrayList<>(plotables.keySet());
		} else {
			idsToPaint = selectedIds;
		}

		NumberAxis xAxis = new NumberAxis(transformX.getName(varX));
		NumberAxis yAxis = new NumberAxis(transformY.getName(varY));
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

			if (plotable.getType() == Plotable.Type.DATA || plotable.getType() == Plotable.Type.DATA_FUNCTION
					|| plotable.getType() == Plotable.Type.DATA_DIFF) {
				double[][] points = plotable.getDataPoints(varX, varY, transformX, transformY);

				if (points != null) {
					for (int i = 0; i < points[0].length; i++) {
						usedMinX = Math.min(usedMinX, points[0][i]);
						usedMaxX = Math.max(usedMaxX, points[0][i]);
					}
				}
			}

			if (plotable.getType() == Plotable.Type.FUNCTION || plotable.getType() == Plotable.Type.DATA_FUNCTION
					|| plotable.getType() == Plotable.Type.DATA_DIFF) {
				Double minArg = transformX.to(plotable.getMinValues().get(varX));
				Double maxArg = transformX.to(plotable.getMaxValues().get(varX));

				if (minArg != null) {
					usedMinX = Math.min(usedMinX, minArg);
				}

				if (maxArg != null) {
					usedMaxX = Math.max(usedMaxX, maxArg);
				}
			}
		}

		if (Double.isInfinite(usedMinX)) {
			usedMinX = 0.0;
		}

		if (Double.isInfinite(usedMaxX)) {
			usedMaxX = 100.0;
		}

		xAxis.setAutoRangeIncludesZero(false);
		yAxis.setAutoRangeIncludesZero(false);

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
			plotable.setInterpolator(interpolator);

			switch (plotable.getType()) {
			case DATA:
				plotDataSet(plot, plotable, id, defaultColors.get(index), defaultShapes.get(index));
				break;
			case FUNCTION:
				plotFunction(plot, plotable, id, defaultColors.get(index), defaultShapes.get(index), usedMinX,
						usedMaxX);
				break;
			case DATA_FUNCTION:
				plotDataFunction(plot, plotable, id, defaultColors.get(index), defaultShapes.get(index), usedMinX,
						usedMaxX);
				break;
			case DATA_DIFF:
				plotDataDiff(plot, plotable, id, defaultColors.get(index), defaultShapes.get(index), usedMinX,
						usedMaxX);
				break;
			}

			index++;
		}

		if (minToZero && !useManualRange) {
			Range xRange = xAxis.getRange();
			Range yRange = yAxis.getRange();

			if (xRange.getUpperBound() <= 0.0 || yRange.getUpperBound() <= 0.0) {
				return null;
			}

			xAxis.setRange(new Range(0.0, xRange.getUpperBound()));
			yAxis.setRange(new Range(0.0, yRange.getUpperBound()));
		}

		return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
	}

	public void setVarX(String varX) {
		this.varX = varX;
	}

	public void setVarY(String varY) {
		this.varY = varY;
	}

	public void setTransformX(Transform transformX) {
		this.transformX = transformX;
	}

	public void setTransformY(Transform transformY) {
		this.transformY = transformY;
	}

	public void setMinToZero(boolean minToZero) {
		this.minToZero = minToZero;
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

	public void setShowConfidence(boolean showConfidenceInterval) {
		this.showConfidenceInterval = showConfidenceInterval;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public void setInterpolator(InterpolationFactory.Type interpolator) {
		this.interpolator = interpolator;
	}

	public void setColors(Map<String, Color> colors) {
		this.colors = colors;
	}

	public void setShapes(Map<String, NamedShape> shapes) {
		this.shapes = shapes;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}

	public void setSelectedIds(List<String> selectedIds) {
		this.selectedIds = selectedIds;
	}

	private void fireZoomChanged() {
		zoomListeners.forEach(l -> l.zoomChanged());
	}

	private void plotDataSet(XYPlot plot, Plotable plotable, String id, Color defaultColor, NamedShape defaultShape) {
		XYDataset dataSet = createDataSet(plotable, id);
		XYItemRenderer renderer = createRenderer(plotable, id, defaultColor, defaultShape);

		if (dataSet != null && renderer != null) {
			ChartUtils.addDataSetToPlot(plot, dataSet, renderer);
		}
	}

	private void plotFunction(XYPlot plot, Plotable plotable, String id, Color defaultColor, NamedShape defaultShape,
			double minX, double maxX) throws ParseException {
		XYDataset dataSet = createFunctionDataSet(plotable, id, minX, maxX);
		XYItemRenderer renderer = createFunctionRenderer(plotable, id, defaultColor, defaultShape, dataSet);

		if (dataSet != null && renderer != null) {
			ChartUtils.addDataSetToPlot(plot, dataSet, renderer);
		}
	}

	private void plotDataFunction(XYPlot plot, Plotable plotable, String id, Color defaultColor,
			NamedShape defaultShape, double minX, double maxX) throws ParseException {
		XYDataset functionDataSet = createFunctionDataSet(plotable, id, minX, maxX);
		XYItemRenderer functionRenderer = createFunctionRenderer(plotable, id, defaultColor, defaultShape,
				functionDataSet);
		XYDataset dataSet = createDataSet(plotable, id);
		XYItemRenderer renderer = createRenderer(plotable, id, defaultColor, defaultShape);

		if (functionDataSet != null && functionRenderer != null) {
			if (dataSet != null && renderer != null) {
				functionRenderer.setBaseSeriesVisibleInLegend(false);
			}

			ChartUtils.addDataSetToPlot(plot, functionDataSet, functionRenderer);
		}

		if (dataSet != null && renderer != null) {
			ChartUtils.addDataSetToPlot(plot, dataSet, renderer);
		}
	}

	private void plotDataDiff(XYPlot plot, Plotable plotable, String id, Color defaultColor, NamedShape defaultShape,
			double minX, double maxX) throws ParseException {
		XYDataset diffDataSet = createDiffDataSet(plotable, id, minX, maxX);
		XYItemRenderer diffRenderer = createFunctionRenderer(plotable, id, defaultColor, defaultShape, diffDataSet);
		XYDataset dataSet = createDataSet(plotable, id);
		XYItemRenderer renderer = createRenderer(plotable, id, defaultColor, defaultShape);

		if (diffDataSet != null && diffRenderer != null) {
			if (dataSet != null && renderer != null) {
				diffRenderer.setBaseSeriesVisibleInLegend(false);
			}

			ChartUtils.addDataSetToPlot(plot, diffDataSet, diffRenderer);
		}

		if (dataSet != null && renderer != null) {
			ChartUtils.addDataSetToPlot(plot, dataSet, renderer);
		}
	}

	private XYDataset createDataSet(Plotable plotable, String id) {
		double[][] points = plotable.getDataPoints(varX, varY, transformX, transformY);
		String leg = legend.get(id);

		if (points != null) {
			DefaultXYDataset dataSet = new DefaultXYDataset();

			dataSet.addSeries(leg, points);

			return dataSet;
		}

		return null;
	}

	private XYItemRenderer createRenderer(Plotable plotable, String id, Color defaultColor, NamedShape defaultShape) {
		Color color = colors.containsKey(id) ? colors.get(id) : defaultColor;
		NamedShape shape = shapes.containsKey(id) ? shapes.get(id) : defaultShape;
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(drawLines, true);

		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesPaint(0, color);
		renderer.setSeriesShape(0, shape.getShape());

		return renderer;
	}

	private XYDataset createFunctionDataSet(Plotable plotable, String id, double minX, double maxX)
			throws ParseException {
		double[][] points = plotable.getFunctionPoints(varX, transformX, transformY, minX, maxX);
		double[][] errors = null;
		String leg = legend.get(id);

		if (showConfidenceInterval) {
			errors = plotable.getFunctionErrors(varX, transformX, transformY, minX, maxX, false);
		}

		if (points != null) {
			if (errors != null) {
				YIntervalSeriesCollection functionDataset = new YIntervalSeriesCollection();
				YIntervalSeries series = new YIntervalSeries(leg);

				for (int j = 0; j < points[0].length; j++) {
					double error = Double.isFinite(errors[1][j]) ? errors[1][j] : 0.0;

					series.add(points[0][j], points[1][j], points[1][j] - error, points[1][j] + error);
				}

				functionDataset.addSeries(series);

				return functionDataset;
			} else {
				DefaultXYDataset functionDataset = new DefaultXYDataset();

				functionDataset.addSeries(leg, points);

				return functionDataset;
			}
		}

		return null;
	}

	private XYItemRenderer createFunctionRenderer(Plotable plotable, String id, Color defaultColor,
			NamedShape defaultShape, XYDataset dataSet) {
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

	private XYDataset createDiffDataSet(Plotable plotable, String id, double minX, double maxX) throws ParseException {
		double[][] points = plotable.getDiffPoints(varX, transformX, transformY, minX, maxX);
		double[][] errors = null;
		String leg = legend.get(id);

		if (showConfidenceInterval) {
			errors = plotable.getDiffErrors(varX, transformX, transformY, minX, maxX, false);
		}

		if (points != null) {
			if (errors != null) {
				YIntervalSeriesCollection functionDataset = new YIntervalSeriesCollection();
				YIntervalSeries series = new YIntervalSeries(leg);

				for (int j = 0; j < points[0].length; j++) {
					double error = Double.isFinite(errors[1][j]) ? errors[1][j] : 0.0;

					series.add(points[0][j], points[1][j], points[1][j] - error, points[1][j] + error);
				}

				functionDataset.addSeries(series);

				return functionDataset;
			} else {
				DefaultXYDataset functionDataset = new DefaultXYDataset();

				functionDataset.addSeries(leg, points);

				return functionDataset;
			}
		}

		return null;
	}

	public static interface ZoomListener {

		void zoomChanged();
	}

}
