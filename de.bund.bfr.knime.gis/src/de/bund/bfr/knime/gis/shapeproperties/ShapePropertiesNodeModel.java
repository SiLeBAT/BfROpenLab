package de.bund.bfr.knime.gis.shapeproperties;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.GisUtilities;

/**
 * This is the model implementation of GetShapeCenter.
 * 
 * 
 * @author Christian Thoens
 */
public class ShapePropertiesNodeModel extends NodeModel {

	private static final String LATITUDE_COLUMN = "CenterLatitude";
	private static final String LONGITUDE_COLUMN = "CenterLongitude";
	private static final String AREA_COLUMN = "Area";

	private ShapePropertiesSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected ShapePropertiesNodeModel() {
		super(1, 1);
		set = new ShapePropertiesSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable inTable = inData[0];
		DataTableSpec inSpec = inTable.getSpec();
		DataTableSpec outSpec = configure(new DataTableSpec[] { inSpec })[0];
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		int n = 0;

		for (DataRow row : inTable) {
			DataCell[] cells = new DataCell[outSpec.getNumColumns()];

			for (int i = 0; i < inSpec.getNumColumns(); i++) {
				cells[outSpec.findColumnIndex(inSpec.getColumnNames()[i])] = row
						.getCell(i);
			}

			Double lat = null;
			Double lon = null;
			Double area = null;
			Geometry shape = GisUtilities.getShape(row.getCell(inSpec
					.findColumnIndex(set.getShapeColumn())));

			if (shape instanceof MultiPolygon) {
				Point2D p = GisUtilities.getCenter((MultiPolygon) shape);

				lat = p.getX();
				lon = p.getY();
				area = GisUtilities.getArea((MultiPolygon) shape);
			}

			cells[outSpec.findColumnIndex(LATITUDE_COLUMN)] = IO
					.createCell(lat);
			cells[outSpec.findColumnIndex(LONGITUDE_COLUMN)] = IO
					.createCell(lon);
			cells[outSpec.findColumnIndex(AREA_COLUMN)] = IO.createCell(area);
			container.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.setProgress((double) n / (double) inTable.getRowCount());
			exec.checkCanceled();
			n++;
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
		List<DataColumnSpec> columns = new ArrayList<DataColumnSpec>();

		for (int i = 0; i < inSpecs[0].getNumColumns(); i++) {
			String name = inSpecs[0].getColumnSpec(i).getName();

			if (name.equals(LATITUDE_COLUMN) || name.equals(LONGITUDE_COLUMN)
					|| name.equals(AREA_COLUMN)) {
				throw new InvalidSettingsException("Column name \"" + name
						+ "\" not allowed in input table");
			}

			columns.add(inSpecs[0].getColumnSpec(i));
		}

		columns.add(new DataColumnSpecCreator(LATITUDE_COLUMN, DoubleCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(LONGITUDE_COLUMN, DoubleCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(AREA_COLUMN, DoubleCell.TYPE)
				.createSpec());

		return new DataTableSpec[] { new DataTableSpec(
				columns.toArray(new DataColumnSpec[0])) };
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

}
