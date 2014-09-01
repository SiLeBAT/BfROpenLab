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
package de.bund.bfr.knime.esri.pointreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.bund.bfr.knime.esri.EsriUtils;

/**
 * This is the model implementation of PointReader.
 * 
 * 
 * @author Christian Thoens
 */
public class PointReaderNodeModel extends NodeModel {

	protected static final String SHP_FILE = "ShpFile";

	private static final String LATITUDE_COLUMN = "Latitude";
	private static final String LONGITUDE_COLUMN = "Longitude";

	private SettingsModelString shpFile;

	/**
	 * Constructor for the node model.
	 */
	protected PointReaderNodeModel() {
		super(0, 1);
		shpFile = new SettingsModelString(SHP_FILE, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		ShapefileDataStore dataStore = EsriUtils.getDataStore(shpFile
				.getStringValue());
		ContentFeatureCollection collection = dataStore.getFeatureSource()
				.getFeatures();
		CoordinateReferenceSystem system = EsriUtils
				.getCoordinateSystem(shpFile.getStringValue());

		Map<String, String> renaming = getRenaming(collection.getSchema());
		DataTableSpec spec = createSpec(collection.getSchema(), renaming)[0];
		BufferedDataContainer container = exec.createDataContainer(spec);
		SimpleFeatureIterator iterator = collection.features();
		MathTransform transform = null;
		int index = 0;
		int count = 0;

		if (system != null) {
			transform = CRS.findMathTransform(system, CRS.decode("EPSG:4326"),
					true);
		}

		DataCell[] cells = new DataCell[spec.getNumColumns()];

		while (iterator.hasNext()) {
			SimpleFeature feature = iterator.next();
			Property geoProperty = null;

			for (Property p : feature.getProperties()) {
				int column = spec.findColumnIndex(renaming.get(p.getName()
						.toString()));
				Object value = p.getValue();

				if (value == null) {
					cells[column] = DataType.getMissingCell();
				} else if (value instanceof Geometry) {
					geoProperty = p;
				} else if (value instanceof Integer) {
					cells[column] = new IntCell((Integer) p.getValue());
				} else if (value instanceof Double) {
					cells[column] = new DoubleCell((Double) p.getValue());
				} else if (value instanceof Boolean) {
					cells[column] = BooleanCell.get((Boolean) p.getValue());
				} else {
					cells[column] = new StringCell(p.getValue().toString());
				}
			}

			if (geoProperty == null) {
				continue;
			}

			Geometry geo = (Geometry) geoProperty.getValue();

			if (transform != null) {
				geo = JTS.transform(geo, transform);
			}

			List<Geometry> geos = EsriUtils.getSimpleGeometries(geo);

			for (Geometry g : geos) {
				if (!(g instanceof Point)) {
					continue;
				}

				Coordinate c = g.getCoordinate();

				if (c != null) {
					double lat = system != null ? c.x : c.y;
					double lon = system != null ? c.y : c.x;

					cells[spec.findColumnIndex(LATITUDE_COLUMN)] = new DoubleCell(
							lat);
					cells[spec.findColumnIndex(LONGITUDE_COLUMN)] = new DoubleCell(
							lon);
				} else {
					cells[spec.findColumnIndex(LATITUDE_COLUMN)] = DataType
							.getMissingCell();
					cells[spec.findColumnIndex(LONGITUDE_COLUMN)] = DataType
							.getMissingCell();
				}

				container.addRowToTable(new DefaultRow(String.valueOf(index),
						cells));
				index++;
			}

			exec.checkCanceled();
			exec.setProgress((double) count / (double) collection.size());
			count++;
		}

		iterator.close();
		dataStore.dispose();
		container.close();

		return new BufferedDataTable[] { container.getTable() };
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
		if (shpFile.getStringValue() == null) {
			throw new InvalidSettingsException("No file name specified");
		}

		DataTableSpec[] result = null;

		try {
			ShapefileDataStore dataStore = EsriUtils.getDataStore(shpFile
					.getStringValue());
			SimpleFeatureType type = dataStore.getSchema();

			result = createSpec(type, getRenaming(type));
			dataStore.dispose();
		} catch (IOException e) {
			throw new InvalidSettingsException(e.getMessage());
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		shpFile.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		shpFile.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		shpFile.validateSettings(settings);
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

	private static DataTableSpec[] createSpec(SimpleFeatureType type,
			Map<String, String> renaming) {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (AttributeType t : type.getTypes()) {
			if (t == type.getGeometryDescriptor().getType()) {
				continue;
			}

			String name = renaming.get(t.getName().toString());

			if (t.getBinding() == Integer.class) {
				columns.add(new DataColumnSpecCreator(name, IntCell.TYPE)
						.createSpec());
			} else if (t.getBinding() == Double.class) {
				columns.add(new DataColumnSpecCreator(name, DoubleCell.TYPE)
						.createSpec());
			} else if (t.getBinding() == Boolean.class) {
				columns.add(new DataColumnSpecCreator(name, BooleanCell.TYPE)
						.createSpec());
			} else {
				columns.add(new DataColumnSpecCreator(name, StringCell.TYPE)
						.createSpec());
			}
		}

		columns.add(new DataColumnSpecCreator(LATITUDE_COLUMN, DoubleCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(LONGITUDE_COLUMN, DoubleCell.TYPE)
				.createSpec());

		return new DataTableSpec[] { new DataTableSpec(
				columns.toArray(new DataColumnSpec[0])) };
	}

	private static Map<String, String> getRenaming(SimpleFeatureType type) {
		Map<String, String> renaming = new LinkedHashMap<>();
		Set<String> columnNames = new LinkedHashSet<>();

		columnNames.add(LATITUDE_COLUMN);
		columnNames.add(LONGITUDE_COLUMN);

		for (AttributeType t : type.getTypes()) {
			if (t == type.getGeometryDescriptor().getType()) {
				continue;
			}

			String name = t.getName().toString();
			String newName = createNewName(name, columnNames);

			renaming.put(name, newName);
			columnNames.add(newName);
		}

		return renaming;
	}

	private static String createNewName(String name, Collection<String> names) {
		if (!names.contains(name)) {
			return name;
		}

		for (int i = 2;; i++) {
			String newValue = name + "_" + i;

			if (!names.contains(newValue)) {
				return newValue;
			}
		}
	}
}
