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
package de.bund.bfr.knime.gis.views.canvas.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JComponent;
import com.google.common.base.Strings;
import de.bund.bfr.jung.BetterEdgeRenderer;
import de.bund.bfr.jung.BetterVisualizationViewer;
import de.bund.bfr.jung.NamedShape;
import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.decorators.DirectionalEdgeArrowTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

public class CanvasLegend<V extends Node> {

	private static final int LEGEND_DX = 10;
	private static final int LEGEND_DY = 3;

	private Canvas<V> owner;
	private Map<String, LegendColor> nodeLegend;
	private Map<String, LegendColor> edgeLegend;

	public CanvasLegend(Canvas<V> owner, HighlightConditionList nodeHighlightConditions, Collection<V> nodes,
			HighlightConditionList edgeHighlightConditions, Collection<Edge<V>> edges) {
		this.owner = owner;
		nodeLegend = new LinkedHashMap<>();
		edgeLegend = new LinkedHashMap<>();

		for (HighlightCondition condition : nodeHighlightConditions.getConditions()) {
			String name = condition.getName();
			Color color = condition.getColor();
			NamedShape shape = condition.getShape();

			if (condition.isShowInLegend() && !Strings.isNullOrEmpty(name) && (color != null || shape != null)) {
				if (color != null && (condition instanceof ValueHighlightCondition
						|| condition instanceof LogicalValueHighlightCondition)) {
					nodeLegend.put(name + " [" + toRangeString(condition.getValueRange(nodes)) + "]",
							new LegendColor(Color.WHITE, color, shape != null ? shape : NamedShape.CIRCLE));
				} else {
					nodeLegend.put(name, new LegendColor(color != null ? color : Color.WHITE,
							shape != null ? shape : NamedShape.CIRCLE));
				}
			}
		}

		for (HighlightCondition condition : edgeHighlightConditions.getConditions()) {
			String name = condition.getName();
			Color color = condition.getColor();

			if (condition.isShowInLegend() && !Strings.isNullOrEmpty(name) && color != null) {
				if (condition instanceof ValueHighlightCondition
						|| condition instanceof LogicalValueHighlightCondition) {
					edgeLegend.put(name + " [" + toRangeString(condition.getValueRange(edges)) + "]",
							new LegendColor(Color.BLACK, color, null));
				} else {
					edgeLegend.put(name, new LegendColor(color, null));
				}
			}
		}
	}

	public void paint(Graphics2D g) {
		if (nodeLegend.isEmpty() && edgeLegend.isEmpty()) {
			return;
		}

		int height = owner.getCanvasSize().height;
		FontRenderContext fontRenderContext = g.getFontRenderContext();
		int maxNodeWidth = 0;
		int maxEdgeWidth = 0;
		Font legendFont = new Font("Default", owner.getOptionsPanel().isFontBold() ? Font.BOLD : Font.PLAIN,
				owner.getOptionsPanel().getFontSize());
		Font legendHeadFont = new Font("Default",
				owner.getOptionsPanel().isFontBold() ? Font.BOLD | Font.ITALIC : Font.BOLD,
				owner.getOptionsPanel().getFontSize());

		for (String name : nodeLegend.keySet()) {
			maxNodeWidth = Math.max(maxNodeWidth, (int) legendFont.getStringBounds(name, fontRenderContext).getWidth());
		}

		for (String name : edgeLegend.keySet()) {
			maxEdgeWidth = Math.max(maxEdgeWidth, (int) legendFont.getStringBounds(name, fontRenderContext).getWidth());
		}

		int legendHeight = g.getFontMetrics(legendFont).getHeight();
		int legendHeadHeight = g.getFontMetrics(legendHeadFont).getHeight();
		int fontAscent = g.getFontMetrics(legendFont).getAscent();
		int headFontAcent = g.getFontMetrics(legendHeadFont).getAscent();

		int xNodeColor = LEGEND_DX;
		int xNodeName = xNodeColor + legendHeight + LEGEND_DX;
		int xNodeEnd = xNodeName + maxNodeWidth + LEGEND_DX;

		int xEdgeColor = nodeLegend.isEmpty() ? LEGEND_DX : xNodeEnd + LEGEND_DX;
		int xEdgeName = xEdgeColor + legendHeight + LEGEND_DX;
		int xEdgeEnd = xEdgeName + maxEdgeWidth + LEGEND_DX;

		int xEnd = edgeLegend.isEmpty() ? xNodeEnd : xEdgeEnd;
		int yStart = owner.getCanvasSize().height
				- Math.max(nodeLegend.size(), edgeLegend.size()) * (legendHeight + LEGEND_DY) - legendHeadHeight
				- 3 * LEGEND_DY;
		int yNode = yStart + LEGEND_DY;
		int yEdge = yStart + LEGEND_DY;
		Color currentColor = g.getColor();
		Font currentFont = g.getFont();

		g.setColor(ZoomingPaintable.BACKGROUND);
		g.fillRect(-1, yStart, xEnd, height - yStart);
		g.setColor(Color.BLACK);
		g.drawRect(-1, yStart, xEnd, height - yStart);
		g.setFont(legendHeadFont);

		if (!nodeLegend.isEmpty()) {
			g.drawString(owner.getNaming().Nodes(), xNodeColor, yNode + headFontAcent);
			yNode += legendHeadHeight + LEGEND_DY;
		}

		if (!edgeLegend.isEmpty()) {
			g.drawString(owner.getNaming().Edges(), xEdgeColor, yEdge + headFontAcent);
			yEdge += legendHeadHeight + LEGEND_DY;
		}

		g.setFont(legendFont);

		for (Map.Entry<String, LegendColor> entry : nodeLegend.entrySet()) {
			entry.getValue().paintNode(g, xNodeColor, yNode, legendHeight);
			g.drawString(entry.getKey(), xNodeName, yNode + fontAscent);
			yNode += legendHeight + LEGEND_DY;
		}

		for (Map.Entry<String, LegendColor> entry : edgeLegend.entrySet()) {
			entry.getValue().paintEdge(g, xEdgeColor, yEdge, legendHeight);
			g.drawString(entry.getKey(), xEdgeName, yEdge + fontAscent);
			yEdge += legendHeight + LEGEND_DY;
		}

		g.setColor(currentColor);
		g.setFont(currentFont);
	}
	
	private static String toRangeString(Point2D p) {
		NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

		return format.format(p.getX()) + " -> " + format.format(p.getY());
	}

	private static class LegendColor {

		private Color color;

		private Color fromColor;
		private Color toColor;

		private NamedShape shape;

		public LegendColor(Color color, NamedShape shape) {
			this.color = color;
			this.shape = shape;

			fromColor = null;
			toColor = null;
		}

		public LegendColor(Color fromColor, Color toColor, NamedShape shape) {
			this.fromColor = fromColor;
			this.toColor = toColor;
			this.shape = shape;

			color = null;
		}

		public void paintNode(Graphics2D g, int x, int y, int size) {
			Paint currentPaint = g.getPaint();
			AffineTransform currentTransform = g.getTransform();
			Shape s = shape.getShape(size);

			g.setPaint(color != null ? color
					: new GradientPaint(-size / 2.0f, 0.0f, fromColor, size / 2.0f, 0.0f, toColor));
			g.translate(x + size / 2, y + size / 2);
			g.fill(s);
			g.setColor(Color.BLACK);
			g.draw(s);
			g.setTransform(currentTransform);
			g.setPaint(currentPaint);
		}
		
		private void paintEdge(Graphics2D g, int x, int y, int size) {
		  Paint currentPaint = g.getPaint();
		  Stroke currentStroke = g.getStroke();
          AffineTransform currentTransform = g.getTransform();
          Color currentColor = g.getColor();
          
          int arrowTipX = 0 + size/6; 
	      //ArrowFactory.getNotchedArrow(width, length, notch_depth)
	      Shape arrow = ArrowFactory.getNotchedArrow(size/5, size/4, size/9);
	      
	      Line2D line2D = new Line2D.Float(-size/2.0f - arrowTipX, 0.0f, 0.0f + size/2.0f - arrowTipX, 0.0f);
	      
	      g.setPaint(color != null ? color
              : new GradientPaint(-size, 0.0f, fromColor, 0.0f, 0.0f, toColor));
	      g.translate(x + size/2 + arrowTipX, y + size / 2);
	      
	      g.setColor(color==null?Color.BLACK:color);
	      
	      g.setStroke(new BasicStroke(size/8.0f));
	      g.draw(line2D);
	      g.setColor(Color.BLACK);
	      g.draw(arrow);
	      
	      g.setColor(currentColor);
	      g.setTransform(currentTransform);
	      g.setPaint(currentPaint);
	      g.setStroke(currentStroke);
      
	    }
	}
}
