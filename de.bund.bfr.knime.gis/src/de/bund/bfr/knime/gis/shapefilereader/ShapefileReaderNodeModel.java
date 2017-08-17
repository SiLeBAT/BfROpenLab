/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.shapefilereader;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;

/**
 * This is the model implementation of ShapefileReader.
 * 
 * 
 * @author Christian Thoens
 */
public class ShapefileReaderNodeModel extends NoInternalsNodeModel {

	protected static final String SHP_FILE = "FileName";

	private SettingsModelString shpFile;

	private static final String LATITUDE_COLUMN = "PolygonCenterLatitude";
	private static final String LONGITUDE_COLUMN = "PolygonCenterLongitude";
	private static final String AREA_COLUMN = "PolygonArea";

	private String latitudeColumn;
	private String longitudeColumn;
	private String areaColumn;

	/**
	 * Constructor for the node model.
	 */
	public ShapefileReaderNodeModel() {
		super(0, 1);
		shpFile = new SettingsModelString(SHP_FILE, null);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		ShapefileDataStore dataStore = GisUtils.getDataStore(shpFile.getStringValue());
		ContentFeatureCollection collection = dataStore.getFeatureSource().getFeatures();
		DataTableSpec spec = createSpec(collection.getSchema());
		BufferedDataContainer container = exec.createDataContainer(spec);
		MathTransform transform = null;
		int index = 0;

		try {
			transform = CRS.findMathTransform(GisUtils.getCoordinateSystem(shpFile.getStringValue()),
					CRS.decode("EPSG:4326"), true);
		} catch (FileNotFoundException | NoSuchFileException e) {
			transform = new AffineTransform2D(0, 1, 1, 0, 0, 0);
		}

		try (SimpleFeatureIterator iterator = collection.features()) {
			loop: while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				DataCell[] cells = new DataCell[spec.getNumColumns()];
				MultiPolygon shape = null;

				for (Property p : feature.getProperties()) {
					String name = p.getName().toString().trim();
					int i = spec.findColumnIndex(name);
					Object value = p.getValue();

					if (value == null) {
						cells[i] = DataType.getMissingCell();
					} else if (value instanceof Geometry) {
						if (!(value instanceof MultiPolygon)) {
							continue loop;
						}

						shape = (MultiPolygon) JTS.transform((MultiPolygon) value, transform);
						cells[i] = new ShapeBlobCell(shape);
					} else if (value instanceof Integer) {
						cells[i] = new IntCell((Integer) p.getValue());
					} else if (value instanceof Double) {
						cells[i] = new DoubleCell((Double) p.getValue());
					} else if (value instanceof Boolean) {
						cells[i] = BooleanCellFactory.create((Boolean) p.getValue());
					} else {
						cells[i] = new StringCell(p.getValue().toString());
					}
				}

				if (shape == null) {
					continue loop;
				}

				Point2D p = GisUtils.getCenterOfLargestPolygon(shape);

				cells[spec.findColumnIndex(latitudeColumn)] = IO.createCell(p.getX());
				cells[spec.findColumnIndex(longitudeColumn)] = IO.createCell(p.getY());
				cells[spec.findColumnIndex(areaColumn)] = IO.createCell(shape.getArea());

				exec.checkCanceled();
				exec.setProgress((double) index / (double) collection.size());
				container.addRowToTable(new DefaultRow(index + "", cells));
				index++;
			}
		}

		dataStore.dispose();
		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		if (shpFile.getStringValue() == null) {
			throw new InvalidSettingsException("No file name specified");
		}

		DataTableSpec[] result = null;

		try {
			ShapefileDataStore dataStore = GisUtils.getDataStore(shpFile.getStringValue());

			result = new DataTableSpec[] { createSpec(dataStore.getFeatureSource().getSchema()) };
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		shpFile.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		shpFile.validateSettings(settings);
	}

	private DataTableSpec createSpec(SimpleFeatureType type) {
		List<DataColumnSpec> columns = new ArrayList<>();
		Set<String> columnNames = new LinkedHashSet<>();

		for (AttributeType t : type.getTypes()) {
			String name;

			if (t == type.getGeometryDescriptor().getType()) {
				name = type.getGeometryDescriptor().getName().toString();
			} else {
				name = t.getName().toString();
			}

			if (t == type.getGeometryDescriptor().getType()) {
				columns.add(new DataColumnSpecCreator(name, ShapeBlobCell.TYPE).createSpec());
			} else if (t.getBinding() == Integer.class) {
				columns.add(new DataColumnSpecCreator(name, IntCell.TYPE).createSpec());
			} else if (t.getBinding() == Double.class) {
				columns.add(new DataColumnSpecCreator(name, DoubleCell.TYPE).createSpec());
			} else if (t.getBinding() == Boolean.class) {
				columns.add(new DataColumnSpecCreator(name, BooleanCell.TYPE).createSpec());
			} else {
				columns.add(new DataColumnSpecCreator(name, StringCell.TYPE).createSpec());
			}

			columnNames.add(name);
		}

		latitudeColumn = KnimeUtils.createNewValue(LATITUDE_COLUMN, columnNames);
		longitudeColumn = KnimeUtils.createNewValue(LONGITUDE_COLUMN, columnNames);
		areaColumn = KnimeUtils.createNewValue(AREA_COLUMN, columnNames);

		columns.add(new DataColumnSpecCreator(latitudeColumn, DoubleCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(longitudeColumn, DoubleCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(areaColumn, DoubleCell.TYPE).createSpec());

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

}
