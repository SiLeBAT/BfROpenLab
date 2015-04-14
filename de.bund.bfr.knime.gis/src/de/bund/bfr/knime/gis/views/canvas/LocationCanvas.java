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
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;

public class LocationCanvas extends ShapefileCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	private List<RegionNode> regions;
	private com.vividsolutions.jts.geom.Polygon invalidArea;

	public LocationCanvas(boolean allowEdges, Naming naming) {
		this(new ArrayList<LocationNode>(),
				new ArrayList<Edge<LocationNode>>(), new NodePropertySchema(),
				new EdgePropertySchema(), naming, new ArrayList<RegionNode>(),
				allowEdges);
	}

	public LocationCanvas(List<LocationNode> nodes,
			NodePropertySchema nodeSchema, Naming naming,
			List<RegionNode> regions) {
		this(nodes, new ArrayList<Edge<LocationNode>>(), nodeSchema,
				new EdgePropertySchema(), naming, regions, false);
	}

	public LocationCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming,
			List<RegionNode> regions) {
		this(nodes, edges, nodeSchema, edgeSchema, naming, regions, true);
	}

	private LocationCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming,
			List<RegionNode> regions, boolean allowEdges) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		this.regions = regions;
		invalidArea = null;

		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, true));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, true, true));
		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(getNodeSize(),
						new LinkedHashMap<LocationNode, Double>()));

		Set<LocationNode> validNodes = new LinkedHashSet<>();
		Set<LocationNode> invalidNodes = new LinkedHashSet<>();
		Map<LocationNode, Set<LocationNode>> invalidToValid = new LinkedHashMap<>();
		Map<LocationNode, Set<LocationNode>> invalidToInvalid = new LinkedHashMap<>();

		for (LocationNode node : this.nodes) {
			if (node.getCenter() != null) {
				viewer.getGraphLayout().setLocation(node, node.getCenter());
				validNodes.add(node);
			} else {
				invalidNodes.add(node);
				invalidToValid.put(node, new LinkedHashSet<LocationNode>());
				invalidToInvalid.put(node, new LinkedHashSet<LocationNode>());
			}
		}

		for (Edge<LocationNode> edge : this.edges) {
			if (edge.getFrom() == edge.getTo()) {
				continue;
			}

			if (invalidNodes.contains(edge.getFrom())) {
				if (invalidNodes.contains(edge.getTo())) {
					invalidToInvalid.get(edge.getFrom()).add(edge.getTo());
				} else {
					invalidToValid.get(edge.getFrom()).add(edge.getTo());
				}
			}

			if (invalidNodes.contains(edge.getTo())) {
				if (invalidNodes.contains(edge.getFrom())) {
					invalidToInvalid.get(edge.getTo()).add(edge.getFrom());
				} else {
					invalidToValid.get(edge.getTo()).add(edge.getFrom());
				}
			}
		}

		if (!invalidNodes.isEmpty()) {
			Rectangle2D bounds = CanvasUtils.getLocationBounds(validNodes);
			double d = Math.max(bounds.getWidth(), bounds.getHeight()) * 0.02;

			if (d == 0.0) {
				d = 1.0;
			}

			invalidArea = GisUtils.createBorderPolygon(new Rectangle2D.Double(
					bounds.getX() - d, bounds.getY() - d, bounds.getWidth() + 2
							* d, bounds.getHeight() + 2 * d), 2 * d);

			Rectangle2D rect = new Rectangle2D.Double(bounds.getX() - 2 * d,
					bounds.getY() - 2 * d, bounds.getWidth() + 4 * d,
					bounds.getHeight() + 4 * d);
			Set<LocationNode> nodesToDo = new LinkedHashSet<>(invalidNodes);

			for (Iterator<LocationNode> iterator = nodesToDo.iterator(); iterator
					.hasNext();) {
				LocationNode node = iterator.next();
				Set<LocationNode> validConnections = invalidToValid.get(node);

				if (!validConnections.isEmpty()) {
					List<Point2D> points = new ArrayList<>();

					for (LocationNode n : validConnections) {
						points.add(n.getCenter());
					}

					Point2D p = CanvasUtils.getClosestPointOnRect(
							CanvasUtils.getCenter(points), rect);

					node.updateCenter(p);
					viewer.getGraphLayout().setLocation(node, p);
					iterator.remove();
				}
			}

			while (true) {
				boolean nothingChanged = true;

				for (Iterator<LocationNode> iterator = nodesToDo.iterator(); iterator
						.hasNext();) {
					LocationNode node = iterator.next();
					Set<LocationNode> inValidConnections = invalidToInvalid
							.get(node);
					List<Point2D> points = new ArrayList<>();

					for (LocationNode n : inValidConnections) {
						if (n.getCenter() != null) {
							points.add(n.getCenter());
						}
					}

					if (!points.isEmpty()) {
						Point2D p = CanvasUtils.getClosestPointOnRect(
								CanvasUtils.getCenter(points), rect);

						node.updateCenter(p);
						viewer.getGraphLayout().setLocation(node, p);
						iterator.remove();
						nothingChanged = false;
					}
				}

				if (nothingChanged) {
					break;
				}
			}

			for (Iterator<LocationNode> iterator = nodesToDo.iterator(); iterator
					.hasNext();) {
				LocationNode node = iterator.next();
				Point2D p = new Point2D.Double(bounds.getMinX() - 2 * d,
						bounds.getMaxY() - 2 * d);

				node.updateCenter(p);
				viewer.getGraphLayout().setLocation(node, p);
				iterator.remove();
			}
		}
	}

	@Override
	public Collection<RegionNode> getRegions() {
		return regions;
	}

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = CanvasUtils.getLocationBounds(nodes);

		if (bounds != null) {
			zoomTo(bounds);
		} else {
			super.resetLayoutItemClicked();
		}
	}

	@Override
	protected void paintGis(Graphics g, boolean toSvg) {
		super.paintGis(g, toSvg);

		if (invalidArea != null) {
			Polygon transformed = transform.apply(invalidArea, true);

			BufferedImage invalidAreaImage = new BufferedImage(
					getCanvasSize().width, getCanvasSize().height,
					BufferedImage.TYPE_INT_ARGB);
			Graphics imgGraphics = invalidAreaImage.getGraphics();

			((Graphics2D) imgGraphics).setPaint(CanvasUtils.mixColors(
					Color.WHITE, Arrays.asList(Color.RED, Color.WHITE),
					Arrays.asList(1.0, 1.0)));
			imgGraphics.fillPolygon(transformed);
			imgGraphics.setColor(Color.BLACK);
			imgGraphics.drawPolygon(transformed);

			float[] edgeScales = { 1f, 1f, 1f, 0.3f };
			float[] edgeOffsets = new float[4];

			((Graphics2D) g).drawImage(invalidAreaImage, new RescaleOp(
					edgeScales, edgeOffsets, null), 0, 0);
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
		properties.put(nodeSchema.getLatitude(), null);
		properties.put(nodeSchema.getLongitude(), null);

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

		viewer.getGraphLayout().setLocation(newNode, newNode.getCenter());

		return newNode;
	}
}
