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
package de.bund.bfr.knime.gis;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;
import de.bund.bfr.knime.gis.shapecell.ShapeValue;

/**
 * @author Christian Thoens
 */
public class GisUtilities {

	private GisUtilities() {
	}

	public static Geometry getShape(DataCell cell) {
		if (cell instanceof ShapeValue) {
			return ((ShapeValue) cell).getShape();
		}

		return null;
	}

	public static List<DataColumnSpec> getShapeColumns(DataTableSpec spec) {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : spec) {
			if (column.getType() == ShapeBlobCell.TYPE) {
				columns.add(column);
			}
		}

		return columns;
	}

	public static Point2D.Double getCenter(MultiPolygon poly) {
		double largestArea = 0.0;
		Point2D.Double center = null;

		for (int index = 0; index < poly.getNumGeometries(); index++) {
			Polygon part = (Polygon) poly.getGeometryN(index);
			Coordinate[] points = part.getExteriorRing().getCoordinates();
			int n = points.length;

			if (!points[0].equals2D(points[n - 1])) {
				n = n + 1;
				points = addFirstElement(points);
			}

			double area = 0.0;
			double cx = 0.0;
			double cy = 0.0;

			for (int i = 0; i < n - 1; i++) {
				double mem = points[i].x * points[i + 1].y - points[i + 1].x
						* points[i].y;

				area += mem;
				cx += (points[i].x + points[i + 1].x) * mem;
				cy += (points[i].y + points[i + 1].y) * mem;
			}

			area /= 2.0;
			cx /= 6 * area;
			cy /= 6 * area;
			area = Math.abs(area);

			if (area > largestArea) {
				largestArea = area;
				center = new Point2D.Double(cx, cy);
			}
		}

		return center;
	}

	public static double getArea(MultiPolygon poly) {
		double wholeArea = 0.0;

		for (int index = 0; index < poly.getNumGeometries(); index++) {
			Polygon part = (Polygon) poly.getGeometryN(index);
			Coordinate[] points = part.getExteriorRing().getCoordinates();
			int n = points.length;

			if (!points[0].equals2D(points[n - 1])) {
				n = n + 1;
				points = addFirstElement(points);
			}

			double area = 0.0;

			for (int i = 0; i < n - 1; i++) {
				area += points[i].x * points[i + 1].y - points[i + 1].x
						* points[i].y;
			}

			wholeArea += Math.abs(area / 2.0);
		}

		return wholeArea;
	}

	public static Rectangle2D.Double getBoundingBox(MultiPolygon poly) {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (int index = 0; index < poly.getNumGeometries(); index++) {
			Polygon part = (Polygon) poly.getGeometryN(index);

			for (Coordinate c : part.getExteriorRing().getCoordinates()) {
				minX = Math.min(minX, c.x);
				maxX = Math.max(maxX, c.x);
				minY = Math.min(minY, c.y);
				maxY = Math.max(maxY, c.y);
			}
		}

		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	public static boolean containsPoint(MultiPolygon poly, Point2D point) {
		double x = point.getX();
		double y = point.getY();

		for (int index = 0; index < poly.getNumGeometries(); index++) {
			Polygon part = (Polygon) poly.getGeometryN(index);
			Coordinate[] points = part.getExteriorRing().getCoordinates();
			int n = points.length;

			if (!points[0].equals2D(points[n - 1])) {
				n = n + 1;
				points = addFirstElement(points);
			}

			int hits = 0;
			double x1 = points[0].x;
			double y1 = points[0].y;

			for (int i = 1; i < n; i++) {
				double x2 = points[i].x;
				double y2 = points[i].y;

				if (y == y2) {
					if (x < x2) {
						double y3 = points[(i + 1) % n].y;

						if (y > Math.min(y1, y3) && y < Math.max(y1, y3)) {
							hits++;
						}
					}
				} else {
					if (y > Math.min(y1, y2) && y < Math.max(y1, y2)) {
						double xProjection = (x2 - x1) / (y2 - y1) * (y - y1)
								+ x1;

						if (x < xProjection) {
							hits++;
						}
					}
				}

				x1 = x2;
				y1 = y2;
			}

			if (hits % 2 != 0) {
				return true;
			}
		}

		return false;
	}

	private static Coordinate[] addFirstElement(Coordinate[] v) {
		int n = v.length;
		Coordinate[] result = new Coordinate[n + 1];

		System.arraycopy(v, 0, result, 0, n);
		result[n] = v[0];

		return result;
	}

}
