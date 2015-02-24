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
package de.bund.bfr.knime.gis;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.referencing.CRS;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;
import de.bund.bfr.knime.gis.shapecell.ShapeValue;

/**
 * @author Christian Thoens
 */
public class GisUtils {

	public static final MathTransform LATLON_TO_VIZ = readTransformFromFile();

	private GisUtils() {
	}

	public static ShapefileDataStore getDataStore(String shpFile)
			throws IOException {
		return new ShapefileDataStore(KnimeUtils.getFile(shpFile).toURI()
				.toURL());
	}

	public static CoordinateReferenceSystem getCoordinateSystem(String shpFile)
			throws IOException, FactoryException {
		try (BufferedReader reader = new BufferedReader(new FileReader(
				KnimeUtils.getFile(FilenameUtils.removeExtension(shpFile)
						+ ".prj")))) {
			StringBuilder wkt = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				wkt.append(line);
			}

			return CRS.parseWKT(wkt.toString());
		}
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

	public static Point2D getCenter(MultiPolygon poly) {
		double largestArea = 0.0;
		Point center = null;

		for (int index = 0; index < poly.getNumGeometries(); index++) {
			Polygon part = (Polygon) poly.getGeometryN(index);
			double area = part.getArea();

			if (area > largestArea) {
				largestArea = area;
				center = part.getCentroid();
			}
		}

		return center != null ? new Point2D.Double(center.getX(), center.getY())
				: null;
	}

	public static double getArea(MultiPolygon poly) {
		return poly.getArea();
	}

	public static Rectangle2D getBoundingBox(MultiPolygon poly) {
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

	/*
	 * Is used instead of createTransform, since using createTransform during
	 * load of node settings resulted in an error on OS X.
	 */
	private static MathTransform readTransformFromFile() {
		try (ObjectInputStream in = new ObjectInputStream(
				Activator.class
						.getResourceAsStream("/de/bund/bfr/knime/gis/transform.bin"))) {
			return (MathTransform) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static MathTransform createTransform() {
		try {
			return CRS.findMathTransform(CRS.decode("EPSG:4326"),
					CRS.decode("EPSG:3857"), true);
		} catch (FactoryException e) {
			e.printStackTrace();
			return null;
		}
	}
}
