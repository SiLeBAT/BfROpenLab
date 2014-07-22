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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

public class EsriUtils {

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

	public static FeatureCollection<?, ?> getFeatures(String shpFile)
			throws IOException {
		Map<String, URL> map = new HashMap<>();

		map.put("url", EsriUtils.getFile(shpFile).toURI().toURL());

		DataStore dataStore = DataStoreFinder.getDataStore(map);
		String typeName = dataStore.getTypeNames()[0];
		FeatureSource<?, ?> source = dataStore.getFeatureSource(typeName);

		return source.getFeatures();
	}

	public static CoordinateReferenceSystem getCoordinateSystem(String shpFile)
			throws IOException, FactoryException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					EsriUtils.getFile(FilenameUtils.removeExtension(shpFile)
							+ ".prj")));
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
