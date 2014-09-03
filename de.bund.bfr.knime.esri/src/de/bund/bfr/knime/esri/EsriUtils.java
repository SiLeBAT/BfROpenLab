/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
package de.bund.bfr.knime.esri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class EsriUtils {

	private static final GeometryFactory GEO_FACTORY = new GeometryFactory();

	private EsriUtils() {
	}

	public static List<Geometry> getSimpleGeometries(Geometry g) {
		List<Geometry> list = new ArrayList<>();

		if (g instanceof GeometryCollection) {
			GeometryCollection collection = (GeometryCollection) g;

			for (int i = 0; i < collection.getNumGeometries(); i++) {
				list.add(collection.getGeometryN(i));
			}
		} else {
			list.add(g);
		}

		return list;
	}

	public static Polygon createPolygon(List<Coordinate> coordinates) {
		List<LinearRing> rings = new ArrayList<>();
		int index = 0;

		while (index < coordinates.size()) {
			List<Coordinate> ring = new ArrayList<>();
			Coordinate firstCoordinate = coordinates.get(index);

			ring.add(firstCoordinate);
			index++;

			for (; index < coordinates.size(); index++) {
				ring.add(coordinates.get(index));

				if (coordinates.get(index).equals2D(firstCoordinate)) {
					index++;
					break;
				}
			}

			rings.add(new LinearRing(new CoordinateArraySequence(ring
					.toArray(new Coordinate[0])), GEO_FACTORY));
		}

		LinearRing shell = rings.remove(0);
		LinearRing[] holes = rings.toArray(new LinearRing[0]);

		return new Polygon(shell, holes, GEO_FACTORY);
	}

	public static ShapefileDataStore getDataStore(String shpFile)
			throws IOException {
		return new ShapefileDataStore(EsriUtils.getFile(shpFile).toURI()
				.toURL());
	}

	public static CoordinateReferenceSystem getCoordinateSystem(String shpFile)
			throws IOException, FactoryException {
		try (BufferedReader reader = new BufferedReader(new FileReader(
				EsriUtils.getFile(FilenameUtils.removeExtension(shpFile)
						+ ".prj")))) {
			StringBuilder wkt = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				wkt.append(line);
			}

			return CRS.parseWKT(wkt.toString());
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private static File getFile(String fileName) throws FileNotFoundException {
		File file = new File(fileName);

		if (!file.exists()) {
			try {
				file = new File(new URI(fileName).getPath());
			} catch (URISyntaxException e1) {
			}
		}

		if (!file.exists()) {
			throw new FileNotFoundException("File not found: " + fileName);
		}

		return file;
	}
}
