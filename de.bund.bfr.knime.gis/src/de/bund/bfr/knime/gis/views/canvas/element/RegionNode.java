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
package de.bund.bfr.knime.gis.views.canvas.element;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.Transform;

public class RegionNode extends Node {

	private MultiPolygon polygon;
	private Point2D center;
	private Rectangle2D boundingBox;

	private List<Polygon> transformedPolygon;
	private List<Polygon> transformedPolygonWithHoles;

	public RegionNode(String id, Map<String, Object> properties,
			MultiPolygon polygon) {
		super(id, properties);
		this.polygon = polygon;
		center = GisUtils.getCenter(polygon);
		boundingBox = GisUtils.getBoundingBox(polygon);
		transformedPolygon = null;
		transformedPolygonWithHoles = null;
	}

	public MultiPolygon getPolygon() {
		return polygon;
	}

	public Point2D getCenter() {
		return center;
	}

	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}

	public List<Polygon> getTransformedPolygon() {
		return transformedPolygon;
	}

	public List<Polygon> getTransformedPolygonWithHoles() {
		return transformedPolygonWithHoles;
	}

	public void createTransformedPolygons(Transform transform) {
		transformedPolygon = transform.apply(polygon, false);
	}

	public void createTransformedPolygonsWithHoles(Transform transform) {
		transformedPolygonWithHoles = transform.apply(polygon, true);
	}

	public boolean containsPoint(Point2D point) {
		return boundingBox.contains(point)
				&& GisUtils.containsPoint(polygon, point);
	}

	@Override
	public RegionNode copy() {
		return new RegionNode(getId(), new LinkedHashMap<>(getProperties()),
				polygon);
	}
}
