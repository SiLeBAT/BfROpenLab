/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.canvas.element;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.TopologyException;

import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;

public class RegionNode extends Node {

	private MultiPolygon polygon;
	private Point2D center;

	private Shape transformedPolygon;

	public RegionNode(String id, Map<String, Object> properties, MultiPolygon polygon) {
		super(id, properties);
		updatePolygon(polygon);
	}

	public MultiPolygon getPolygon() {
		return polygon;
	}

	public void updatePolygon(MultiPolygon polygon) {
		this.polygon = polygon;
		center = GisUtils.getCenterOfLargestPolygon(polygon);
		transformedPolygon = null;
	}

	public Point2D getCenter() {
		return center;
	}

	public Rectangle2D getBoundingBox() {
		Envelope bounds = polygon.getEnvelopeInternal();

		return new Rectangle2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
	}

	public Shape getTransformedPolygon() {
		return transformedPolygon;
	}

	public void createTransformedPolygons(Transform transform) {
		transformedPolygon = transform.apply(polygon);
	}

	public boolean containsPoint(Point2D point) {
		try {
			return polygon.contains(polygon.getFactory().createPoint(new Coordinate(point.getX(), point.getY())));
		} catch (TopologyException e) {
			return false;
		}
	}

	@Override
	public RegionNode copy() {
		return new RegionNode(getId(), new LinkedHashMap<>(getProperties()), polygon);
	}
}
