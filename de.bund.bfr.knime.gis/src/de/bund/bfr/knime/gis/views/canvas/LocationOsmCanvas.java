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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;

public class LocationOsmCanvas extends OsmCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	private Rectangle2D invalidArea;

	public LocationOsmCanvas(boolean allowEdges, Naming naming) {
		this(new ArrayList<LocationNode>(),
				new ArrayList<Edge<LocationNode>>(), new NodePropertySchema(),
				new EdgePropertySchema(), naming, allowEdges);
	}

	public LocationOsmCanvas(List<LocationNode> nodes,
			NodePropertySchema nodeSchema, Naming naming) {
		this(nodes, new ArrayList<Edge<LocationNode>>(), nodeSchema,
				new EdgePropertySchema(), naming, false);
	}

	public LocationOsmCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming) {
		this(nodes, edges, nodeSchema, edgeSchema, naming, true);
	}

	private LocationOsmCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming, boolean allowEdges) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		invalidArea = null;

		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, true));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, true, false));
		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(getNodeSize(),
						new LinkedHashMap<LocationNode, Double>()));

		List<Point2D> validPoints = new ArrayList<>();

		for (LocationNode node : this.nodes) {
			if (node.getCenter() != null) {
				Point2D p = convertLatLonToPos(node.getCenter());

				validPoints.add(p);
				viewer.getGraphLayout().setLocation(node, p);
			}
		}

		if (validPoints.size() != this.nodes.size()) {
			Rectangle2D bounds = CanvasUtils.getBounds(validPoints);
			double d = Math.max(bounds.getWidth(), bounds.getHeight()) * 0.1;

			if (d == 0.0) {
				d = 1.0;
			}

			Point2D p = new Point2D.Double(bounds.getMinX() - 2 * d,
					bounds.getMinY() - 2 * d);

			for (LocationNode node : this.nodes) {
				if (node.getCenter() == null) {
					viewer.getGraphLayout().setLocation(node, p);
				}
			}

			invalidArea = new Rectangle2D.Double(bounds.getMinX() - 3 * d,
					bounds.getMinY() - 3 * d, 2 * d, 2 * d);
		}
	}

	@Override
	protected void paintGis(Graphics g, boolean toSvg) {
		super.paintGis(g, toSvg);

		if (invalidArea != null) {
			Rectangle transformed = transform.apply(invalidArea);

			((Graphics2D) g).setPaint(CanvasUtils.mixColors(Color.WHITE,
					Arrays.asList(Color.RED, Color.WHITE),
					Arrays.asList(1.0, 1.0)));
			g.fillRect(transformed.x, transformed.y, transformed.width,
					transformed.height);
			g.setColor(Color.BLACK);
			g.drawRect(transformed.x, transformed.y, transformed.width,
					transformed.height);
		}
	}

	@Override
	protected LocationNode createMetaNode(String id,
			Collection<LocationNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (LocationNode node : nodes) {
			CanvasUtils.addMapToMap(properties, nodeSchema,
					node.getProperties());
		}

		properties.put(nodeSchema.getId(), id);
		properties.put(metaNodeProperty, true);

		if (nodeSchema.getLatitude() != null) {
			properties.put(nodeSchema.getLatitude(),
					CanvasUtils.getMeanValue(nodes, nodeSchema.getLatitude()));
		}

		if (nodeSchema.getLongitude() != null) {
			properties.put(nodeSchema.getLongitude(),
					CanvasUtils.getMeanValue(nodes, nodeSchema.getLongitude()));
		}

		List<Double> xList = new ArrayList<Double>();
		List<Double> yList = new ArrayList<Double>();

		for (LocationNode node : nodes) {
			xList.add(node.getCenter().getX());
			yList.add(node.getCenter().getY());
		}

		double x = DoubleMath.mean(Doubles.toArray(xList));
		double y = DoubleMath.mean(Doubles.toArray(yList));
		LocationNode newNode = new LocationNode(id, properties,
				new Point2D.Double(x, y));

		viewer.getGraphLayout().setLocation(newNode,
				convertLatLonToPos(newNode.getCenter()));

		return newNode;
	}

	private static Point2D convertLatLonToPos(Point2D latLon) {
		return new Point2D.Double(OsmMercator.LonToX(latLon.getX(), 0),
				OsmMercator.LatToY(latLon.getY(), 0));
	}
}
