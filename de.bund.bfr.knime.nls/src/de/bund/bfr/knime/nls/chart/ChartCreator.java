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

import java.awt.Color;
import java.awt.Shape;
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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import de.bund.bfr.math.Transform;

public class ChartCreator extends ChartPanel {

	private static final long serialVersionUID = 1L;

	private List<ZoomListener> zoomListeners;

	private Map<String, Plotable> plotables;
	private Map<String, String> legend;
	private Map<String, Color> colors;
	private Map<String, Shape> shapes;
	private boolean selectAll;
	private List<String> selectedIds;

	private String paramX;
	private String paramY;
	private Transform transformX;
	private Transform transformY;
	private boolean useManualRange;
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	private boolean drawLines;
	private boolean showLegend;
	private boolean showConfidenceInterval;

	public ChartCreator(Map<String, Plotable> plotables,
			Map<String, String> legend) {
		super(new JFreeChart(new XYPlot()));
		this.plotables = plotables;
		this.legend = legend;
		colors = new LinkedHashMap<String, Color>();
		shapes = new LinkedHashMap<String, Shape>();
		zoomListeners = new ArrayList<ZoomListener>();
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

	public JFreeChart createChart() {
		if (paramX == null || paramY == null) {
			return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT,
					new XYPlot(), showLegend);
		}

		List<String> idsToPaint;

		if (selectAll) {
			idsToPaint = new ArrayList<String>(plotables.keySet());
		} else {
			idsToPaint = selectedIds;
		}

		NumberAxis xAxis = new NumberAxis(transformX.getName(paramX));
		NumberAxis yAxis = new NumberAxis(transformY.getName(paramY));
		XYPlot plot = new XYPlot(null, xAxis, yAxis, null);
		double usedMinX = Double.POSITIVE_INFINITY;
		double usedMaxX = Double.NEGATIVE_INFINITY;
		int index = 0;
		ColorAndShapeCreator colorAndShapeCreator = new ColorAndShapeCreator(
				idsToPaint.size());

		for (String id : idsToPaint) {
			Plotable plotable = plotables.get(id);

			if (plotable != null) {
				if (plotable.getType() == Plotable.Type.BOTH) {
					Double minArg = Transform.transform(plotable
							.getMinArguments().get(paramX), transformX);
					Double maxArg = Transform.transform(plotable
							.getMaxArguments().get(paramX), transformX);

					if (minArg != null) {
						usedMinX = Math.min(usedMinX, minArg);
					}

					if (maxArg != null) {
						usedMaxX = Math.max(usedMaxX, maxArg);
					}

					double[][] points = plotable.getPoints(paramX, paramY,
							transformX, transformY);

					for (int i = 0; i < points[0].length; i++) {
						usedMinX = Math.min(usedMinX, points[0][i]);
						usedMaxX = Math.max(usedMaxX, points[0][i]);
					}
				} else if (plotable.getType() == Plotable.Type.DATASET) {
					double[][] points = plotable.getPoints(paramX, paramY,
							transformX, transformY);

					if (points != null) {
						for (int i = 0; i < points[0].length; i++) {
							usedMinX = Math.min(usedMinX, points[0][i]);
							usedMaxX = Math.max(usedMaxX, points[0][i]);
						}
					}
				} else if (plotable.getType() == Plotable.Type.FUNCTION) {
					Double minArg = Transform.transform(plotable
							.getMinArguments().get(paramX), transformX);
					Double maxArg = Transform.transform(plotable
							.getMaxArguments().get(paramX), transformX);

					if (minArg != null) {
						usedMinX = Math.min(usedMinX, minArg);
					}

					if (maxArg != null) {
						usedMaxX = Math.max(usedMaxX, maxArg);
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

			if (plotable != null && plotable.getType() == Plotable.Type.DATASET) {
				plotDataSet(plot, plotable, id, colorAndShapeCreator
						.getColorList().get(index), colorAndShapeCreator
						.getShapeList().get(index));
				index++;
			}
		}

		for (String id : idsToPaint) {
			Plotable plotable = plotables.get(id);

			if (plotable != null
					&& plotable.getType() == Plotable.Type.FUNCTION) {
				plotFunction(plot, plotable, id, colorAndShapeCreator
						.getColorList().get(index), colorAndShapeCreator
						.getShapeList().get(index), usedMinX, usedMaxX);
				index++;
			}
		}

		for (String id : idsToPaint) {
			Plotable plotable = plotables.get(id);

			if (plotable != null && plotable.getType() == Plotable.Type.BOTH) {
				plotBoth(plot, plotable, id, colorAndShapeCreator
						.getColorList().get(index), colorAndShapeCreator
						.getShapeList().get(index), usedMinX, usedMaxX);
				index++;
			}
		}

		return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot,
				showLegend);
	}

	public void setParamX(String paramX) {
		this.paramX = paramX;
	}

	public void setParamY(String paramY) {
		this.paramY = paramY;
	}

	public void setTransformX(Transform transformX) {
		this.transformX = transformX;
	}

	public void setTransformY(Transform transformY) {
		this.transformY = transformY;
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

	public void setColors(Map<String, Color> colors) {
		this.colors = colors;
	}

	public void setShapes(Map<String, Shape> shapes) {
		this.shapes = shapes;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}

	public void setSelectedIds(List<String> selectedIds) {
		this.selectedIds = selectedIds;
	}

	private void fireZoomChanged() {
		for (ZoomListener listener : zoomListeners) {
			listener.zoomChanged();
		}
	}

	private void plotDataSet(XYPlot plot, Plotable plotable, String id,
			Color defaultColor, Shape defaultShape) {
		double[][] points = plotable.getPoints(paramX, paramY, transformX,
				transformY);
		String leg = legend.get(id);
		Color color = colors.get(id);
		Shape shape = shapes.get(id);

		if (color == null) {
			color = defaultColor;
		}

		if (shape == null) {
			shape = defaultShape;
		}

		if (points != null) {
			DefaultXYDataset dataset = new DefaultXYDataset();
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(
					drawLines, true);

			dataset.addSeries(leg, points);
			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
			renderer.setSeriesPaint(0, color);
			renderer.setSeriesShape(0, shape);

			int i;

			if (plot.getDataset(0) == null) {
				i = 0;
			} else {
				i = plot.getDatasetCount();
			}

			plot.setDataset(i, dataset);
			plot.setRenderer(i, renderer);
		}
	}

	private void plotFunction(XYPlot plot, Plotable plotable, String id,
			Color defaultColor, Shape defaultShape, double minX, double maxX) {
		double[][] points = plotable.getFunctionPoints(paramX, paramY,
				transformX, transformY, minX, maxX, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		double[][] functionErrors = null;
		String leg = legend.get(id);
		Color color = colors.get(id);
		Shape shape = shapes.get(id);

		if (showConfidenceInterval) {
			functionErrors = plotable.getFunctionErrors(paramX, paramY,
					transformX, transformY, minX, maxX,
					Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		}

		if (color == null) {
			color = defaultColor;
		}

		if (shape == null) {
			shape = defaultShape;
		}

		if (points != null) {
			int i;

			if (plot.getDataset(0) == null) {
				i = 0;
			} else {
				i = plot.getDatasetCount();
			}

			if (functionErrors != null) {
				YIntervalSeriesCollection functionDataset = new YIntervalSeriesCollection();
				DeviationRenderer functionRenderer = new DeviationRenderer(
						true, false);
				YIntervalSeries series = new YIntervalSeries(leg);

				for (int j = 0; j < points[0].length; j++) {
					double error = Double.isNaN(functionErrors[1][j]) ? 0.0
							: functionErrors[1][j];

					series.add(points[0][j], points[1][j],
							points[1][j] - error, points[1][j] + error);
				}

				functionDataset.addSeries(series);
				functionRenderer
						.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
				functionRenderer.setSeriesPaint(0, color);
				functionRenderer.setSeriesFillPaint(0, color);
				functionRenderer.setSeriesShape(0, shape);

				plot.setDataset(i, functionDataset);
				plot.setRenderer(i, functionRenderer);
			} else {
				DefaultXYDataset functionDataset = new DefaultXYDataset();
				XYLineAndShapeRenderer functionRenderer = new XYLineAndShapeRenderer(
						true, false);

				functionDataset.addSeries(leg, points);
				functionRenderer
						.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
				functionRenderer.setSeriesPaint(0, color);
				functionRenderer.setSeriesShape(0, shape);

				plot.setDataset(i, functionDataset);
				plot.setRenderer(i, functionRenderer);
			}
		}
	}

	private void plotBoth(XYPlot plot, Plotable plotable, String id,
			Color defaultColor, Shape defaultShape, double minX, double maxX) {
		double[][] modelPoints = plotable.getFunctionPoints(paramX, paramY,
				transformX, transformY, minX, maxX, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		double[][] dataPoints = plotable.getPoints(paramX, paramY, transformX,
				transformY);
		double[][] functionErrors = null;
		String leg = legend.get(id);
		Color color = colors.get(id);
		Shape shape = shapes.get(id);

		if (showConfidenceInterval) {
			functionErrors = plotable.getFunctionErrors(paramX, paramY,
					transformX, transformY, minX, maxX,
					Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		}

		if (color == null) {
			color = defaultColor;
		}

		if (shape == null) {
			shape = defaultShape;
		}

		if (modelPoints != null) {
			int i;

			if (plot.getDataset(0) == null) {
				i = 0;
			} else {
				i = plot.getDatasetCount();
			}

			if (functionErrors != null) {
				YIntervalSeriesCollection functionDataset = new YIntervalSeriesCollection();
				DeviationRenderer functionRenderer = new DeviationRenderer(
						true, false);
				YIntervalSeries series = new YIntervalSeries(leg);

				for (int j = 0; j < modelPoints[0].length; j++) {
					double error = Double.isNaN(functionErrors[1][j]) ? 0.0
							: functionErrors[1][j];

					series.add(modelPoints[0][j], modelPoints[1][j],
							modelPoints[1][j] - error, modelPoints[1][j]
									+ error);
				}

				functionDataset.addSeries(series);
				functionRenderer
						.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
				functionRenderer.setSeriesPaint(0, color);
				functionRenderer.setSeriesFillPaint(0, color);
				functionRenderer.setSeriesShape(0, shape);

				if (dataPoints != null) {
					functionRenderer.setBaseSeriesVisibleInLegend(false);
				}

				plot.setDataset(i, functionDataset);
				plot.setRenderer(i, functionRenderer);
			} else {
				DefaultXYDataset functionDataset = new DefaultXYDataset();
				XYLineAndShapeRenderer functionRenderer = new XYLineAndShapeRenderer(
						true, false);

				functionDataset.addSeries(leg, modelPoints);
				functionRenderer
						.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
				functionRenderer.setSeriesPaint(0, color);
				functionRenderer.setSeriesShape(0, shape);

				if (dataPoints != null) {
					functionRenderer.setBaseSeriesVisibleInLegend(false);
				}

				plot.setDataset(i, functionDataset);
				plot.setRenderer(i, functionRenderer);
			}
		}

		if (dataPoints != null) {
			DefaultXYDataset dataSet = new DefaultXYDataset();
			XYLineAndShapeRenderer dataRenderer = new XYLineAndShapeRenderer(
					drawLines, true);

			dataSet.addSeries(leg, dataPoints);
			dataRenderer
					.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
			dataRenderer.setSeriesPaint(0, color);
			dataRenderer.setSeriesShape(0, shape);

			int i;

			if (plot.getDataset(0) == null) {
				i = 0;
			} else {
				i = plot.getDatasetCount();
			}

			plot.setDataset(i, dataSet);
			plot.setRenderer(i, dataRenderer);
		}
	}

	public static interface ZoomListener {

		public void zoomChanged();
	}

}
