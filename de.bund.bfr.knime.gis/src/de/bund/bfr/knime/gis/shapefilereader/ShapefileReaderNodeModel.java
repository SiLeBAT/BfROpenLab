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
package de.bund.bfr.knime.gis.shapefilereader;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.GisUtilities;
import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;
import de.bund.bfr.knime.gis.shapecell.ShapeCellFactory;

/**
 * This is the model implementation of ShapefileReader.
 * 
 * 
 * @author Christian Thoens
 */
public class ShapefileReaderNodeModel extends NodeModel {

	private static final String LATITUDE_COLUMN = "PolygonCenterLatitude";
	private static final String LONGITUDE_COLUMN = "PolygonCenterLongitude";
	private static final String AREA_COLUMN = "PolygonArea";

	private ShapefileReaderSettings set;
	private FeatureCollection<?, ?> collection;
	private CoordinateReferenceSystem system;

	private String latitudeColumn;
	private String longitudeColumn;
	private String areaColumn;

	/**
	 * Constructor for the node model.
	 */
	protected ShapefileReaderNodeModel() {
		super(0, 1);
		set = new ShapefileReaderSettings();
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		DataTableSpec spec = createSpec(collection);
		BufferedDataContainer container = exec.createDataContainer(spec);
		int n = collection.size();
		FeatureIterator<?> iterator = collection.features();
		MathTransform transform = null;
		int index = 0;

		if (system != null) {
			transform = CRS.findMathTransform(system, CRS.decode("EPSG:4326"),
					true);
		} else {
			transform = new AffineTransform2D(0, 1, 1, 0, 0, 0);
		}

		while (iterator.hasNext()) {
			Feature feature = iterator.next();
			DataCell[] cells = new DataCell[spec.getNumColumns()];
			Geometry shape = null;

			for (Property p : feature.getProperties()) {
				String name = p.getName().toString().trim();
				int i = spec.findColumnIndex(name);

				if (p.getValue() instanceof Geometry) {
					shape = JTS.transform((Geometry) p.getValue(), transform);
					cells[i] = ShapeCellFactory.create(shape);
				} else {
					cells[i] = new StringCell(p.getValue().toString());
				}
			}

			Double lat = null;
			Double lon = null;
			Double area = null;

			if (shape instanceof MultiPolygon) {
				Point2D p = GisUtilities.getCenter((MultiPolygon) shape);

				lat = p.getX();
				lon = p.getY();
				area = GisUtilities.getArea((MultiPolygon) shape);
			}

			cells[spec.findColumnIndex(latitudeColumn)] = IO.createCell(lat);
			cells[spec.findColumnIndex(longitudeColumn)] = IO.createCell(lon);
			cells[spec.findColumnIndex(areaColumn)] = IO.createCell(area);

			exec.checkCanceled();
			exec.setProgress((double) index / (double) n);
			container.addRowToTable(new DefaultRow(index + "", cells));
			index++;
		}

		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		if (set.getFileName() != null) {
			try {
				File shpFile = KnimeUtilities.getFile(set.getFileName());
				Map<String, URL> map = new HashMap<>();

				map.put("url", shpFile.toURI().toURL());

				DataStore dataStore = DataStoreFinder.getDataStore(map);
				String typeName = dataStore.getTypeNames()[0];
				FeatureSource<?, ?> source = dataStore
						.getFeatureSource(typeName);

				collection = source.getFeatures();

				try {
					File prjFile = KnimeUtilities.getFile(FilenameUtils
							.removeExtension(shpFile.getAbsolutePath())
							+ ".prj");
					BufferedReader reader = new BufferedReader(new FileReader(
							prjFile));
					String wkt = "";
					String line;

					while ((line = reader.readLine()) != null) {
						wkt += line;
					}

					system = CRS.parseWKT(wkt);
				} catch (FileNotFoundException e) {
					system = null;
				}
			} catch (IOException e) {
				throw new InvalidSettingsException(e.getMessage());
			} catch (FactoryException e) {
				throw new InvalidSettingsException(e.getMessage());
			}
		} else {
			throw new InvalidSettingsException("No file name specified");
		}

		return new DataTableSpec[] { createSpec(collection) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		set.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	private DataTableSpec createSpec(FeatureCollection<?, ?> collection) {
		FeatureIterator<?> iterator = collection.features();
		Feature feature = iterator.next();
		List<DataColumnSpec> columns = new ArrayList<>();
		Set<String> columnNames = new LinkedHashSet<>();

		for (Property p : feature.getProperties()) {
			String name = p.getName().toString().trim();

			columnNames.add(name);

			if (p.getValue() instanceof Geometry) {
				columns.add(new DataColumnSpecCreator(name, ShapeBlobCell.TYPE)
						.createSpec());
			} else {
				columns.add(new DataColumnSpecCreator(name, StringCell.TYPE)
						.createSpec());
			}
		}

		latitudeColumn = KnimeUtilities.createNewValue(LATITUDE_COLUMN,
				columnNames);
		longitudeColumn = KnimeUtilities.createNewValue(LONGITUDE_COLUMN,
				columnNames);
		areaColumn = KnimeUtilities.createNewValue(AREA_COLUMN, columnNames);

		columns.add(new DataColumnSpecCreator(latitudeColumn, DoubleCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(longitudeColumn, DoubleCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(areaColumn, DoubleCell.TYPE)
				.createSpec());

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

}
