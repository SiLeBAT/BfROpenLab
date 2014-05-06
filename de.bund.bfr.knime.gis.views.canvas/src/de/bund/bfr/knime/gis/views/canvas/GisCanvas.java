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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JSlider;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public abstract class GisCanvas<V extends Node> extends Canvas<V> {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_BORDER_ALPHA = 255;

	private List<RegionNode> regions;

	private BufferedImage image;

	private int borderAlpha;

	private JSlider borderAlphaSlider;
	private JButton borderAlphaButton;

	public GisCanvas(List<RegionNode> regions,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty) {
		super(nodeProperties, edgeProperties, nodeIdProperty, edgeIdProperty,
				edgeFromProperty, edgeToProperty);
		setAllowCollapse(false);
		this.regions = regions;
		borderAlpha = DEFAULT_BORDER_ALPHA;
		image = null;

		getViewer().addPreRenderPaintable(new Paintable() {

			@Override
			public boolean useTransform() {
				return false;
			}

			@Override
			public void paint(Graphics g) {
				paintGisImage(g);
			}
		});

		borderAlphaSlider = new JSlider(0, 255, borderAlpha);
		borderAlphaSlider.setPreferredSize(new Dimension(100, borderAlphaSlider
				.getPreferredSize().height));
		borderAlphaButton = new JButton("Apply");
		borderAlphaButton.addActionListener(this);
		addOptionsItem("Border Alpha", borderAlphaSlider, borderAlphaButton);
	}

	public List<RegionNode> getRegions() {
		return regions;
	}

	public int getBorderAlpha() {
		return borderAlpha;
	}

	public void setBorderAlpha(int borderAlpha) {
		this.borderAlpha = borderAlpha;
		borderAlphaSlider.setValue(borderAlpha);
		flushImage();
		getViewer().repaint();
	}

	@Override
	public void setCanvasSize(Dimension canvasSize) {
		super.setCanvasSize(canvasSize);
		computeTransform(canvasSize);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == borderAlphaButton) {
			this.borderAlpha = borderAlphaSlider.getValue();
			flushImage();
			getViewer().repaint();
		}
	}

	@Override
	protected VisualizationImageServer<V, Edge<V>> createVisualizationServer(
			final boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = super
				.createVisualizationServer(toSvg);

		server.addPreRenderPaintable(new Paintable() {

			@Override
			public boolean useTransform() {
				return false;
			}

			@Override
			public void paint(Graphics g) {
				paintGis(g, getCanvasSize().width, getCanvasSize().height,
						toSvg);
			}
		});

		return server;
	}

	@Override
	protected void resetLayout() {
		computeTransform(getViewer().getSize());
	}

	@Override
	protected void applyTransform() {
		flushImage();
		computeTransformedShapes();
		getViewer().repaint();
	}

	@Override
	protected void collapseToNode() {
	}

	@Override
	protected void expandFromNode() {
	}

	protected void flushImage() {
		if (image != null) {
			image.flush();
			image = null;
		}
	}

	protected void paintGis(Graphics g, int width, int height, boolean toSvg) {
		paintBackground(g, width, height);
		paintRegionBorders(g, width, height, toSvg);
	}

	protected void paintBackground(Graphics g, int width, int height) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
	}

	protected void paintRegionBorders(Graphics g, int width, int height,
			boolean toSvg) {
		if (!toSvg) {
			BufferedImage borderImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			Graphics borderGraphics = borderImage.getGraphics();

			borderGraphics.setColor(Color.BLACK);

			for (RegionNode node : regions) {
				for (Polygon part : node.getTransformedPolygon()) {
					borderGraphics.drawPolygon(part);
				}
			}

			CanvasUtilities.drawImageWithAlpha(g, borderImage, borderAlpha);
		} else {
			g.setColor(new Color(0, 0, 0, borderAlpha));

			for (RegionNode node : regions) {
				for (Polygon part : node.getTransformedPolygon()) {
					g.drawPolygon(part);
				}
			}
		}
	}

	private void computeTransform(Dimension canvasSize) {
		Rectangle2D.Double polygonsBounds = getPolygonsBounds();

		if (polygonsBounds != null) {
			double widthRatio = canvasSize.width / polygonsBounds.width;
			double heightRatio = canvasSize.height / polygonsBounds.height;
			double canvasCenterX = canvasSize.width / 2.0;
			double canvasCenterY = canvasSize.height / 2.0;
			double polygonCenterX = polygonsBounds.getCenterX();
			double polygonCenterY = polygonsBounds.getCenterY();

			double scaleX = Math.min(widthRatio, heightRatio);
			double scaleY = -scaleX;
			double translationX = canvasCenterX - polygonCenterX * scaleX;
			double translationY = canvasCenterY - polygonCenterY * scaleY;

			setTransform(scaleX, scaleY, translationX, translationY);
		} else {
			setTransform(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
		}
	}

	private void paintGisImage(Graphics g) {
		int width = getCanvasSize().width;
		int height = getCanvasSize().height;

		if (image == null || image.getWidth() != width
				|| image.getHeight() != height) {
			flushImage();
			image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			paintGis(image.getGraphics(), width, height, false);
		}

		g.drawImage(image, 0, 0, null);
	}

	private void computeTransformedShapes() {
		for (RegionNode node : regions) {
			node.setTransform(getTranslationX(), getTranslationY(),
					getScaleX(), getScaleY());
		}
	}

	private Rectangle2D.Double getPolygonsBounds() {
		Rectangle2D.Double bounds = null;

		for (RegionNode node : regions) {
			if (bounds == null) {
				bounds = node.getBoundingBox();
			} else {
				bounds = (Rectangle2D.Double) bounds.createUnion(node
						.getBoundingBox());
			}
		}

		return bounds;
	}
}
