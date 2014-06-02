/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;

public class CanvasLegend<V extends Node> {

	private static final int LEGEND_COLOR_BOX_WIDTH = 30;
	private static final int LEGEND_DX = 10;
	private static final int LEGEND_DY = 3;

	private Map<String, Color> nodeLegend;
	private Map<String, Color> edgeLegend;
	private Map<String, String> nodeUseGradient;
	private Map<String, String> edgeUseGradient;

	public CanvasLegend(HighlightConditionList nodeHighlightConditions,
			Collection<V> nodes,
			HighlightConditionList edgeHighlightConditions,
			Collection<Edge<V>> edges) {
		nodeLegend = new LinkedHashMap<String, Color>();
		edgeLegend = new LinkedHashMap<String, Color>();
		nodeUseGradient = new LinkedHashMap<String, String>();
		edgeUseGradient = new LinkedHashMap<String, String>();

		for (HighlightCondition condition : nodeHighlightConditions
				.getConditions()) {
			String name = condition.getName();
			Color color = condition.getColor();

			if (condition.isShowInLegend() && name != null && !name.isEmpty()
					&& color != null) {
				nodeLegend.put(name, color);

				if (condition instanceof ValueHighlightCondition
						|| condition instanceof LogicalValueHighlightCondition) {
					nodeUseGradient.put(name, CanvasUtilities
							.toRangeString(condition.getValueRange(nodes)));
				}
			}
		}

		for (HighlightCondition condition : edgeHighlightConditions
				.getConditions()) {
			String name = condition.getName();
			Color color = condition.getColor();

			if (condition.isShowInLegend() && name != null && !name.isEmpty()
					&& color != null) {
				edgeLegend.put(name, color);

				if (condition instanceof ValueHighlightCondition
						|| condition instanceof LogicalValueHighlightCondition) {
					edgeUseGradient.put(name, CanvasUtilities
							.toRangeString(condition.getValueRange(edges)));
				}
			}
		}
	}

	public void paint(Graphics g, int width, int height, int fontSize,
			boolean fontBold) {
		if (nodeLegend.isEmpty() && edgeLegend.isEmpty()) {
			return;
		}

		FontRenderContext fontRenderContext = ((Graphics2D) g)
				.getFontRenderContext();
		int maxNodeWidth = 0;
		int maxEdgeWidth = 0;
		int maxNodeRangeWidth = 0;
		int maxEdgeRangeWidth = 0;
		Font legendFont = new Font("Default",
				fontBold ? Font.BOLD : Font.PLAIN, fontSize);
		Font legendHeadFont = new Font("Default", fontBold ? Font.BOLD
				| Font.ITALIC : Font.BOLD, fontSize);

		for (String name : nodeLegend.keySet()) {
			maxNodeWidth = Math.max(maxNodeWidth, (int) legendFont
					.getStringBounds(name, fontRenderContext).getWidth());
		}

		for (String name : edgeLegend.keySet()) {
			maxEdgeWidth = Math.max(maxEdgeWidth, (int) legendFont
					.getStringBounds(name, fontRenderContext).getWidth());
		}

		for (String range : nodeUseGradient.values()) {
			maxNodeRangeWidth = Math.max(maxNodeRangeWidth, (int) legendFont
					.getStringBounds(range, fontRenderContext).getWidth());
		}

		for (String range : edgeUseGradient.values()) {
			maxEdgeRangeWidth = Math.max(maxEdgeRangeWidth, (int) legendFont
					.getStringBounds(range, fontRenderContext).getWidth());
		}

		int legendHeight = g.getFontMetrics(legendFont).getHeight();
		int legendHeadHeight = g.getFontMetrics(legendHeadFont).getHeight();
		int fontAscent = g.getFontMetrics(legendFont).getAscent();
		int headFontAcent = g.getFontMetrics(legendHeadFont).getAscent();

		int xNodeColor = LEGEND_DX;
		int xNodeName = xNodeColor + LEGEND_COLOR_BOX_WIDTH + LEGEND_DX;
		int xNodeRange = xNodeName + maxNodeWidth + LEGEND_DX;
		int xNodeEnd = maxNodeRangeWidth == 0 ? xNodeRange : xNodeRange
				+ maxNodeRangeWidth + LEGEND_DX;

		int xEdgeColor = nodeLegend.isEmpty() ? LEGEND_DX : xNodeEnd
				+ LEGEND_DX;
		int xEdgeName = xEdgeColor + LEGEND_COLOR_BOX_WIDTH + LEGEND_DX;
		int xEdgeRange = xEdgeName + maxEdgeWidth + LEGEND_DX;
		int xEdgeEnd = maxEdgeRangeWidth == 0 ? xEdgeRange : xEdgeRange
				+ maxEdgeRangeWidth + LEGEND_DX;

		int xEnd = edgeLegend.isEmpty() ? xNodeEnd : xEdgeEnd;
		int yStart = height - Math.max(nodeLegend.size(), edgeLegend.size())
				* (legendHeight + LEGEND_DY) - legendHeadHeight - 3 * LEGEND_DY;
		int yNode = yStart + LEGEND_DY;
		int yEdge = yStart + LEGEND_DY;

		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, yStart, xEnd, height - yStart - 1);
		g.setColor(Color.BLACK);
		g.drawRect(0, yStart, xEnd, height - yStart - 1);
		g.setFont(legendHeadFont);

		if (!nodeLegend.isEmpty()) {
			g.drawString("Nodes", xNodeColor, yNode + headFontAcent);
			yNode += legendHeadHeight + LEGEND_DY;
		}

		if (!edgeLegend.isEmpty()) {
			g.drawString("Edges", xEdgeColor, yEdge + headFontAcent);
			yEdge += legendHeadHeight + LEGEND_DY;
		}

		g.setFont(legendFont);

		for (String name : nodeLegend.keySet()) {
			g.setColor(Color.BLACK);
			g.drawString(name, xNodeName, yNode + fontAscent);

			if (nodeUseGradient.containsKey(name)) {
				g.drawString(nodeUseGradient.get(name), xNodeRange, yNode
						+ fontAscent);
				((Graphics2D) g).setPaint(new GradientPaint(xNodeColor, 0,
						Color.WHITE, xNodeColor + LEGEND_COLOR_BOX_WIDTH, 0,
						nodeLegend.get(name)));
			} else {
				g.setColor(nodeLegend.get(name));
			}

			g.fillRect(xNodeColor, yNode, LEGEND_COLOR_BOX_WIDTH, legendHeight);
			g.setColor(Color.BLACK);
			g.drawRect(xNodeColor, yNode, LEGEND_COLOR_BOX_WIDTH, legendHeight);
			yNode += legendHeight + LEGEND_DY;
		}

		for (String name : edgeLegend.keySet()) {
			g.setColor(Color.BLACK);
			g.drawString(name, xEdgeName, yEdge + fontAscent);

			if (edgeUseGradient.containsKey(name)) {
				g.drawString(edgeUseGradient.get(name), xEdgeRange, yEdge
						+ fontAscent);
				((Graphics2D) g).setPaint(new GradientPaint(xEdgeColor, 0,
						Color.BLACK, xEdgeColor + LEGEND_COLOR_BOX_WIDTH, 0,
						edgeLegend.get(name)));
			} else {
				g.setColor(edgeLegend.get(name));
			}

			g.fillRect(xEdgeColor, yEdge, LEGEND_COLOR_BOX_WIDTH, legendHeight);
			g.setColor(Color.BLACK);
			g.drawRect(xEdgeColor, yEdge, LEGEND_COLOR_BOX_WIDTH, legendHeight);
			yEdge += legendHeight + LEGEND_DY;
		}
	}
}
