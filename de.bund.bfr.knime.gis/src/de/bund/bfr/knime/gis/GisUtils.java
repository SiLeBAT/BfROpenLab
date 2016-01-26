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
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.referencing.CRS;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.gui.jmapviewer.OsmMercator;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
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

	private static final GeometryFactory FACTORY = new GeometryFactory();

	private GisUtils() {
	}

	public static Polygon createBorderPolygon(Rectangle2D rect, double d) {
		Coordinate[] outerRing = new Coordinate[] { new Coordinate(rect.getMinX() - d, rect.getMinY() - d),
				new Coordinate(rect.getMaxX() + d, rect.getMinY() - d),
				new Coordinate(rect.getMaxX() + d, rect.getMaxY() + d),
				new Coordinate(rect.getMinX() - d, rect.getMaxY() + d),
				new Coordinate(rect.getMinX() - d, rect.getMinY() - d) };
		Coordinate[] innerRing = new Coordinate[] { new Coordinate(rect.getMinX(), rect.getMinY()),
				new Coordinate(rect.getMaxX(), rect.getMinY()), new Coordinate(rect.getMaxX(), rect.getMaxY()),
				new Coordinate(rect.getMinX(), rect.getMaxY()), new Coordinate(rect.getMinX(), rect.getMinY()) };

		return FACTORY.createPolygon(FACTORY.createLinearRing(outerRing),
				new LinearRing[] { FACTORY.createLinearRing(innerRing) });
	}

	public static Point2D latLonToViz(Point2D latLon) {
		return new Point2D.Double(OsmMercator.LonToX(latLon.getY(), 0), OsmMercator.LatToY(latLon.getX(), 0));
	}

	public static MultiPolygon latLonToViz(MultiPolygon polygon) {
		Polygon[] transformed = new Polygon[polygon.getNumGeometries()];

		for (int i = 0; i < polygon.getNumGeometries(); i++) {
			Polygon poly = (Polygon) polygon.getGeometryN(i);
			LinearRing exterior = FACTORY.createLinearRing(latLonToViz(poly.getExteriorRing().getCoordinates()));
			LinearRing[] interior = new LinearRing[poly.getNumInteriorRing()];

			for (int j = 0; j < poly.getNumInteriorRing(); j++) {
				interior[j] = FACTORY.createLinearRing(latLonToViz(poly.getInteriorRingN(j).getCoordinates()));
			}

			transformed[i] = FACTORY.createPolygon(exterior, interior);
		}
		return FACTORY.createMultiPolygon(transformed);
	}

	private static Coordinate[] latLonToViz(Coordinate[] coordinates) {
		Coordinate[] transformed = new Coordinate[coordinates.length];

		for (int i = 0; i < coordinates.length; i++) {
			transformed[i] = new Coordinate(OsmMercator.LonToX(coordinates[i].y, 0),
					OsmMercator.LatToY(coordinates[i].x, 0));
		}

		return transformed;
	}

	public static ShapefileDataStore getDataStore(String shpFile) throws IOException {
		return new ShapefileDataStore(KnimeUtils.getFile(shpFile).toURI().toURL());
	}

	public static CoordinateReferenceSystem getCoordinateSystem(String shpFile)
			throws InvalidPathException, MalformedURLException, IOException, FactoryException {
		try (Stream<String> stream = Files
				.lines(KnimeUtils.getFile(FilenameUtils.removeExtension(shpFile) + ".prj").toPath())) {
			return CRS.parseWKT(stream.collect(Collectors.joining()));
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

	public static Point2D getCenterOfLargestPolygon(MultiPolygon poly) {
		double largestArea = 0.0;
		Point center = null;

		for (int i = 0; i < poly.getNumGeometries(); i++) {
			Polygon p = (Polygon) poly.getGeometryN(i);
			double area = p.getArea();

			if (area > largestArea) {
				largestArea = area;
				center = p.getCentroid();
			}
		}

		return center != null ? new Point2D.Double(center.getX(), center.getY()) : null;
	}

	public static String getAddress(String street, String houseNumber, String city, String district, String state,
			String country, String postalCode, boolean houseNumberAfterStreet) {
		List<String> parts = new ArrayList<>();

		if (street != null && houseNumber != null) {
			if (houseNumberAfterStreet) {
				parts.add(street + " " + houseNumber);
			} else {
				parts.add(houseNumber + " " + street);
			}
		} else if (street != null) {
			parts.add(street);
		}

		if (city != null) {
			parts.add(city);
		}

		if (postalCode != null) {
			parts.add(postalCode);
		}

		if (district != null) {
			parts.add(district);
		}

		if (state != null) {
			parts.add(state);
		}

		if (country != null) {
			parts.add(country);
		}

		if (parts.isEmpty()) {
			return null;
		}

		return Joiner.on(", ").join(parts);
	}
}
