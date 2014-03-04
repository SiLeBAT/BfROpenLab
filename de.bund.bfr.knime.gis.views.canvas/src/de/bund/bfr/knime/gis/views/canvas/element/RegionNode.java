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
package de.bund.bfr.knime.gis.views.canvas.element;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.gis.GisUtilities;

public class RegionNode extends Node {

	private MultiPolygon polygon;
	private Point2D.Double center;
	private Rectangle2D.Double boundingBox;

	private List<Polygon> transformedPolygon;
	private Point transformedCenter;

	public RegionNode(String id, Map<String, Object> properties,
			MultiPolygon polygon) {
		super(id, properties);
		this.polygon = polygon;
		center = GisUtilities.getCenter(polygon);
		boundingBox = GisUtilities.getBoundingBox(polygon);
	}

	public MultiPolygon getPolygon() {
		return polygon;
	}

	public Point2D.Double getCenter() {
		return center;
	}

	public Rectangle2D.Double getBoundingBox() {
		return boundingBox;
	}

	public List<Polygon> getTransformedPolygon() {
		return transformedPolygon;
	}

	public Point getTransformedCenter() {
		return transformedCenter;
	}

	public void setTransform(double translationX, double translationY,
			double scaleX, double scaleY) {
		transformedPolygon = new ArrayList<Polygon>();
		transformedCenter = new Point();

		for (int index = 0; index < polygon.getNumGeometries(); index++) {
			com.vividsolutions.jts.geom.Polygon part = (com.vividsolutions.jts.geom.Polygon) polygon
					.getGeometryN(index);
			Coordinate[] points = part.getExteriorRing().getCoordinates();
			int n = points.length;
			int[] xs = new int[n];
			int[] ys = new int[n];

			for (int i = 0; i < n; i++) {
				xs[i] = (int) (points[i].x * scaleX + translationX);
				ys[i] = (int) (points[i].y * scaleY + translationY);
			}

			transformedPolygon.add(new Polygon(xs, ys, n));
		}

		if (center != null) {
			transformedCenter.x = (int) (center.x * scaleX + translationX);
			transformedCenter.y = (int) (center.y * scaleY + translationY);
		}
	}

	public boolean containsPoint(Point2D.Double point) {
		return boundingBox.contains(point)
				&& GisUtilities.containsPoint(polygon, point);
	}
}
