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
package de.bund.bfr.knime.esri.polygonproperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.knime.esri.EsriUtils;

/**
 * This is the model implementation of PolygonProperties.
 * 
 * 
 * @author Christian Thoens
 */
public class PolygonPropertiesNodeModel extends NodeModel {

	protected static final String CFG_POLYGON_COLUMN = "PolygonColumn";
	protected static final String CFG_LATITUDE_COLUMN = "LatitudeColumn";
	protected static final String CFG_LONGITUDE_COLUMN = "LongitudeColumn";

	private static final String CENTER_LATITUDE_COLUMN = "PolygonCenterLatitude";
	private static final String CENTER_LONGITUDE_COLUMN = "PolygonCenterLongitude";
	private static final String AREA_COLUMN = "PolygonArea";

	private SettingsModelString polygonColumm;
	private SettingsModelString latitudeColumm;
	private SettingsModelString longitudeColumm;

	/**
	 * Constructor for the node model.
	 */
	protected PolygonPropertiesNodeModel() {
		super(2, 1);
		polygonColumm = new SettingsModelString(CFG_POLYGON_COLUMN, null);
		latitudeColumm = new SettingsModelString(CFG_LATITUDE_COLUMN, null);
		longitudeColumm = new SettingsModelString(CFG_LONGITUDE_COLUMN, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable coordinateTable = inData[1];
		Map<String, Coordinate> coordinateMap = new LinkedHashMap<>();

		for (DataRow row : coordinateTable) {
			DataCell latitudeCell = row.getCell(coordinateTable.getSpec()
					.findColumnIndex(latitudeColumm.getStringValue()));
			DataCell longitudeCell = row.getCell(coordinateTable.getSpec()
					.findColumnIndex(longitudeColumm.getStringValue()));

			if (latitudeCell instanceof DoubleValue
					&& longitudeCell instanceof DoubleValue) {
				coordinateMap.put(row.getKey().getString(), new Coordinate(
						((DoubleValue) latitudeCell).getDoubleValue(),
						((DoubleValue) longitudeCell).getDoubleValue()));
			}
		}

		BufferedDataTable polygonTable = inData[0];
		DataTableSpec outSpec = createSpec(polygonTable.getSpec());
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		int index = 0;

		for (DataRow row : polygonTable) {
			DataCell[] cells = new DataCell[outSpec.getNumColumns()];
			DataCell polygonCell = row.getCell(polygonTable.getSpec()
					.findColumnIndex(polygonColumm.getStringValue()));
			List<Coordinate> coordinates = new ArrayList<>();

			if (polygonCell instanceof ListDataValue) {
				for (DataCell cell : (ListDataValue) polygonCell) {
					if (cell instanceof StringValue) {
						String id = ((StringValue) cell).getStringValue();

						if (coordinateMap.containsKey(id)) {
							coordinates.add(coordinateMap.get(id));
						}
					}
				}
			}

			if (!coordinates.isEmpty()) {
				Polygon poly = EsriUtils.createPolygon(coordinates);
				Point center = poly.getCentroid();

				cells[outSpec.findColumnIndex(CENTER_LATITUDE_COLUMN)] = new DoubleCell(
						center.getX());
				cells[outSpec.findColumnIndex(CENTER_LONGITUDE_COLUMN)] = new DoubleCell(
						center.getY());
				cells[outSpec.findColumnIndex(AREA_COLUMN)] = new DoubleCell(
						poly.getArea());
			} else {
				cells[outSpec.findColumnIndex(CENTER_LATITUDE_COLUMN)] = DataType
						.getMissingCell();
				cells[outSpec.findColumnIndex(CENTER_LONGITUDE_COLUMN)] = DataType
						.getMissingCell();
				cells[outSpec.findColumnIndex(AREA_COLUMN)] = DataType
						.getMissingCell();
			}

			for (String column : polygonTable.getSpec().getColumnNames()) {
				cells[outSpec.findColumnIndex(column)] = row
						.getCell(polygonTable.getSpec().findColumnIndex(column));
			}

			exec.checkCanceled();
			container.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.setProgress((double) index
					/ (double) polygonTable.getRowCount());
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return new DataTableSpec[] { createSpec(inSpecs[0]) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		polygonColumm.saveSettingsTo(settings);
		latitudeColumm.saveSettingsTo(settings);
		longitudeColumm.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		polygonColumm.loadSettingsFrom(settings);
		latitudeColumm.loadSettingsFrom(settings);
		longitudeColumm.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		polygonColumm.validateSettings(settings);
		latitudeColumm.validateSettings(settings);
		longitudeColumm.validateSettings(settings);
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

	private static DataTableSpec createSpec(DataTableSpec inSpec)
			throws InvalidSettingsException {
		List<String> newColumnNames = Arrays.asList(CENTER_LATITUDE_COLUMN,
				CENTER_LONGITUDE_COLUMN, AREA_COLUMN);
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : inSpec) {
			if (newColumnNames.contains(column.getName())) {
				throw new InvalidSettingsException("Column name \""
						+ column.getName()
						+ "\" not allowed in first input table");
			}

			columns.add(column);
		}

		columns.add(new DataColumnSpecCreator(CENTER_LATITUDE_COLUMN,
				DoubleCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(CENTER_LONGITUDE_COLUMN,
				DoubleCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(AREA_COLUMN, DoubleCell.TYPE)
				.createSpec());

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}
}
