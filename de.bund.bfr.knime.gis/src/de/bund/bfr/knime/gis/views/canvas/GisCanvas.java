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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import de.bund.bfr.jung.BetterScalingGraphMousePlugin;
import de.bund.bfr.jung.GisScalingGraphMousePlugin;
import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.jung.layout.LayoutType;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public abstract class GisCanvas<V extends Node> extends Canvas<V>implements IGisCanvas<V> {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;

	public GisCanvas(List<V> nodes, List<Edge<V>> edges, NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema,
			Naming naming) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		image = null;

		viewer.addPreRenderPaintable(new PrePaintable(false));
	}

	@Override
	public void layoutItemClicked(LayoutType layoutType) {
	}

	@Override
	public VisualizationImageServer<V, Edge<V>> getVisualizationServer(boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = super.getVisualizationServer(toSvg);

		server.addPreRenderPaintable(new PrePaintable(toSvg));

		return server;
	}

	protected void flushImage() {
		if (image != null) {
			image.flush();
			image = null;
		}
	}

	@Override
	protected BetterScalingGraphMousePlugin createScalingPlugin() {
		return new GisScalingGraphMousePlugin(1 / 2f, 2f);
	}

	@Override
	protected ZoomingPaintable createZoomingPaintable() {
		ZoomingPaintable zoom = new ZoomingPaintable(viewer, 2.0);

		zoom.addChangeListener(this);

		return zoom;
	}

	protected abstract void paintGis(Graphics2D g, boolean toSvg, boolean onWhiteBackground);

	private void paintGisImage(Graphics2D g) {
		int width = getCanvasSize().width;
		int height = getCanvasSize().height;

		if (image == null || image.getWidth() != width || image.getHeight() != height) {
			flushImage();
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			paintGis(image.createGraphics(), false, true);
		}

		g.drawImage(image, 0, 0, null);
	}

	private class PrePaintable implements Paintable {

		private boolean toSvg;

		public PrePaintable(boolean toSvg) {
			this.toSvg = toSvg;
		}

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics g) {
			if (toSvg) {
				paintGis((Graphics2D) g, true, true);
			} else {
				paintGisImage((Graphics2D) g);
			}
		}
	}
}
