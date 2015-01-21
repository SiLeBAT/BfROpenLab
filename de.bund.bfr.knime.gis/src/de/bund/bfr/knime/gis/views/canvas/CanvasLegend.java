/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
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

	private Canvas<V> owner;
	private Map<String, Image> nodeLegend;
	private Map<String, Image> edgeLegend;

	public CanvasLegend(Canvas<V> owner,
			HighlightConditionList nodeHighlightConditions,
			Collection<V> nodes,
			HighlightConditionList edgeHighlightConditions,
			Collection<Edge<V>> edges) {
		this.owner = owner;
		nodeLegend = new LinkedHashMap<>();
		edgeLegend = new LinkedHashMap<>();

		for (HighlightCondition condition : nodeHighlightConditions
				.getConditions()) {
			String name = condition.getName();
			Color color = condition.getColor();

			if (condition.isShowInLegend() && name != null && !name.isEmpty()
					&& color != null) {
				BufferedImage image = new BufferedImage(LEGEND_COLOR_BOX_WIDTH,
						LEGEND_COLOR_BOX_WIDTH, BufferedImage.TYPE_INT_RGB);
				Graphics g = image.getGraphics();

				if (condition instanceof ValueHighlightCondition
						|| condition instanceof LogicalValueHighlightCondition) {
					name += " ["
							+ CanvasUtils.toRangeString(condition
									.getValueRange(nodes)) + "]";
					((Graphics2D) g).setPaint(new GradientPaint(0, 0,
							Color.WHITE, LEGEND_COLOR_BOX_WIDTH, 0, color));
				} else {
					g.setColor(color);
				}

				g.fillRect(0, 0, LEGEND_COLOR_BOX_WIDTH, LEGEND_COLOR_BOX_WIDTH);
				nodeLegend.put(name, image);
			}
		}

		for (HighlightCondition condition : edgeHighlightConditions
				.getConditions()) {
			String name = condition.getName();
			Color color = condition.getColor();

			if (condition.isShowInLegend() && name != null && !name.isEmpty()
					&& color != null) {
				BufferedImage image = new BufferedImage(LEGEND_COLOR_BOX_WIDTH,
						LEGEND_COLOR_BOX_WIDTH, BufferedImage.TYPE_INT_RGB);
				Graphics g = image.getGraphics();

				if (condition instanceof ValueHighlightCondition
						|| condition instanceof LogicalValueHighlightCondition) {
					name += " ["
							+ CanvasUtils.toRangeString(condition
									.getValueRange(edges)) + "]";
					((Graphics2D) g).setPaint(new GradientPaint(0, 0,
							Color.WHITE, LEGEND_COLOR_BOX_WIDTH, 0, color));
				} else {
					g.setColor(color);
				}

				g.fillRect(0, 0, LEGEND_COLOR_BOX_WIDTH, LEGEND_COLOR_BOX_WIDTH);
				edgeLegend.put(name, image);
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

		int legendHeight = g.getFontMetrics(legendFont).getHeight();
		int legendHeadHeight = g.getFontMetrics(legendHeadFont).getHeight();
		int fontAscent = g.getFontMetrics(legendFont).getAscent();
		int headFontAcent = g.getFontMetrics(legendHeadFont).getAscent();

		int xNodeColor = LEGEND_DX;
		int xNodeName = xNodeColor + LEGEND_COLOR_BOX_WIDTH + LEGEND_DX;
		int xNodeEnd = xNodeName + maxNodeWidth + LEGEND_DX;

		int xEdgeColor = nodeLegend.isEmpty() ? LEGEND_DX : xNodeEnd
				+ LEGEND_DX;
		int xEdgeName = xEdgeColor + LEGEND_COLOR_BOX_WIDTH + LEGEND_DX;
		int xEdgeEnd = xEdgeName + maxEdgeWidth + LEGEND_DX;

		int xEnd = edgeLegend.isEmpty() ? xNodeEnd : xEdgeEnd;
		int yStart = height - Math.max(nodeLegend.size(), edgeLegend.size())
				* (legendHeight + LEGEND_DY) - legendHeadHeight - 3 * LEGEND_DY;
		int yNode = yStart + LEGEND_DY;
		int yEdge = yStart + LEGEND_DY;

		g.setColor(new Color(230, 230, 230));
		g.fillRect(-1, yStart, xEnd, height - yStart);
		g.setColor(Color.BLACK);
		g.drawRect(-1, yStart, xEnd, height - yStart);
		g.setFont(legendHeadFont);

		if (!nodeLegend.isEmpty()) {
			g.drawString(owner.getNaming().Nodes(), xNodeColor, yNode
					+ headFontAcent);
			yNode += legendHeadHeight + LEGEND_DY;
		}

		if (!edgeLegend.isEmpty()) {
			g.drawString(owner.getNaming().Edges(), xEdgeColor, yEdge
					+ headFontAcent);
			yEdge += legendHeadHeight + LEGEND_DY;
		}

		g.setFont(legendFont);

		for (String name : nodeLegend.keySet()) {
			g.drawImage(nodeLegend.get(name), xNodeColor, yNode,
					LEGEND_COLOR_BOX_WIDTH, legendHeight, null);
			g.setColor(Color.BLACK);
			g.drawRect(xNodeColor, yNode, LEGEND_COLOR_BOX_WIDTH, legendHeight);
			g.drawString(name, xNodeName, yNode + fontAscent);
			yNode += legendHeight + LEGEND_DY;
		}

		for (String name : edgeLegend.keySet()) {
			g.drawImage(edgeLegend.get(name), xEdgeColor, yEdge,
					LEGEND_COLOR_BOX_WIDTH, legendHeight, null);
			g.setColor(Color.BLACK);
			g.drawRect(xEdgeColor, yEdge, LEGEND_COLOR_BOX_WIDTH, legendHeight);
			g.drawString(name, xEdgeName, yEdge + fontAscent);
			yEdge += legendHeight + LEGEND_DY;
		}
	}
}
