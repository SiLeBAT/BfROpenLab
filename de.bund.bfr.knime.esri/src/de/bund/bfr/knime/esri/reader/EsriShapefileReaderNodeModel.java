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
package de.bund.bfr.knime.esri.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.knime.KnimeUtilities;

/**
 * This is the model implementation of EsriShapefileReader.
 * 
 * 
 * @author Christian Thoens
 */
public class EsriShapefileReaderNodeModel extends NodeModel {

	protected static final String SHP_FILE = "ShpFile";
	protected static final String GET_EXTERIOR_POLYGON = "GetExteriorPolygon";

	private static final String LATITUDE_COLUMN = "Latitude";
	private static final String LONGITUDE_COLUMN = "Longitude";

	private SettingsModelString shpFile;
	private SettingsModelBoolean getExteriorPolygon;

	private FeatureCollection<?, ?> collection;
	private CoordinateReferenceSystem system;

	/**
	 * Constructor for the node model.
	 */
	protected EsriShapefileReaderNodeModel() {
		super(0, 2);
		shpFile = new SettingsModelString(SHP_FILE, null);
		getExteriorPolygon = new SettingsModelBoolean(GET_EXTERIOR_POLYGON,
				false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		DataTableSpec[] spec = createSpec(collection);
		DataTableSpec spec1 = spec[0];
		DataTableSpec spec2 = spec[1];
		BufferedDataContainer container1 = exec.createDataContainer(spec1);
		BufferedDataContainer container2 = exec.createDataContainer(spec2);
		FeatureIterator<?> iterator = collection.features();
		MathTransform transform = null;
		int index1 = 0;
		int index2 = 0;

		if (system != null) {
			transform = CRS.findMathTransform(system, CRS.decode("EPSG:4326"),
					true);
		}

		while (iterator.hasNext()) {
			Feature feature = iterator.next();
			DataCell[] cells1 = new DataCell[spec1.getNumColumns()];
			Property geoProperty = null;

			for (Property p : feature.getProperties()) {
				int column = spec1.findColumnIndex(p.getName().toString());
				Object value = p.getValue();

				if (value instanceof Geometry) {
					geoProperty = p;
				} else if (value == null) {
					cells1[column] = DataType.getMissingCell();
				} else {
					cells1[column] = new StringCell(p.getValue().toString());
				}
			}

			Geometry geo = (Geometry) geoProperty.getValue();

			if (transform != null) {
				geo = JTS.transform(geo, transform);
			}

			List<Geometry> geos = getSimpleGeometries(geo);

			for (Geometry g : geos) {
				Coordinate[] coordinates;

				if (g instanceof Polygon
						&& getExteriorPolygon.getBooleanValue()) {
					coordinates = ((Polygon) g).getExteriorRing()
							.getCoordinates();
				} else {
					coordinates = g.getCoordinates();
				}

				List<StringCell> rowIdCells = new ArrayList<>();

				for (Coordinate c : coordinates) {
					DataCell[] cells2 = new DataCell[spec2.getNumColumns()];
					double lat = system != null ? c.x : c.y;
					double lon = system != null ? c.y : c.x;

					cells2[spec2.findColumnIndex(LATITUDE_COLUMN)] = new DoubleCell(
							lat);
					cells2[spec2.findColumnIndex(LONGITUDE_COLUMN)] = new DoubleCell(
							lon);

					container2
							.addRowToTable(new DefaultRow(index2 + "", cells2));
					rowIdCells.add(new StringCell(index2 + ""));
					index2++;
				}

				cells1[spec1.findColumnIndex(geoProperty.getName().toString())] = CollectionCellFactory
						.createListCell(rowIdCells);
				exec.checkCanceled();
				exec.setProgress((double) index1 / (double) collection.size());
				container1.addRowToTable(new DefaultRow(index1 + "", cells1));
				index1++;
			}
		}

		container1.close();
		container2.close();

		return new BufferedDataTable[] { container1.getTable(),
				container2.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		String fileName = shpFile.getStringValue();

		if (fileName == null) {
			throw new InvalidSettingsException("No file name specified");
		}

		try {
			File shpFile = KnimeUtilities.getFile(fileName);
			Map<String, URL> map = new HashMap<>();

			map.put("url", shpFile.toURI().toURL());

			DataStore dataStore = DataStoreFinder.getDataStore(map);
			String typeName = dataStore.getTypeNames()[0];
			FeatureSource<?, ?> source = dataStore.getFeatureSource(typeName);

			collection = source.getFeatures();

			try {
				File prjFile = KnimeUtilities.getFile(FilenameUtils
						.removeExtension(shpFile.getAbsolutePath()) + ".prj");
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

		return createSpec(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		shpFile.saveSettingsTo(settings);
		getExteriorPolygon.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		shpFile.loadSettingsFrom(settings);
		getExteriorPolygon.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		shpFile.validateSettings(settings);
		getExteriorPolygon.validateSettings(settings);
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

	private static DataTableSpec[] createSpec(FeatureCollection<?, ?> collection) {
		SimpleFeatureType type = (SimpleFeatureType) collection.getSchema();
		List<DataColumnSpec> columns1 = new ArrayList<>();

		for (AttributeType t : type.getTypes()) {
			if (t == type.getGeometryDescriptor().getType()) {
				columns1.add(new DataColumnSpecCreator(type
						.getGeometryDescriptor().getName().toString(), ListCell
						.getCollectionType(StringCell.TYPE)).createSpec());
			} else {				
				columns1.add(new DataColumnSpecCreator(t.getName().toString(),
						StringCell.TYPE).createSpec());
			}
		}

		List<DataColumnSpec> columns2 = new ArrayList<>();

		columns2.add(new DataColumnSpecCreator(LATITUDE_COLUMN, DoubleCell.TYPE)
				.createSpec());
		columns2.add(new DataColumnSpecCreator(LONGITUDE_COLUMN,
				DoubleCell.TYPE).createSpec());

		return new DataTableSpec[] {
				new DataTableSpec(columns1.toArray(new DataColumnSpec[0])),
				new DataTableSpec(columns2.toArray(new DataColumnSpec[0])) };
	}

	private static List<Geometry> getSimpleGeometries(Geometry g) {
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
}
