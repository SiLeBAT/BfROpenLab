/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingGisCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGraphCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingOsmCanvas;

/**
 * This is the model implementation of TracingVisualizer.
 * 
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeModel extends NodeModel {

	private TracingViewSettings set;

	private int count;
	private int maxCount;

	/**
	 * Constructor for the node model.
	 */
	protected TracingViewNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE,
				BufferedDataTable.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, ImagePortObject.TYPE,
						ImagePortObject.TYPE, ImagePortObject.TYPE });
		set = new TracingViewSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = (BufferedDataTable) inObjects[0];
		BufferedDataTable edgeTable = (BufferedDataTable) inObjects[1];
		BufferedDataTable tracingTable = (BufferedDataTable) inObjects[2];
		BufferedDataTable shapeTable = (BufferedDataTable) inObjects[3];
		GisType originalGisType = set.getGisType();

		if (set.getGisType() == GisType.SHAPEFILE && shapeTable == null) {
			set.setGisType(GisType.MAPNIK);
		}

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(nodeTable, edgeTable, tracingTable, shapeTable,
				set);
		TracingGraphCanvas graphCanvas = creator.createGraphCanvas();
		ImagePortObject graphImage = CanvasUtils.getImage(set.isExportAsSvg(), graphCanvas);
		ImagePortObject gisImage;
		ImagePortObject combinedImage;

		if (creator.hasGisCoordinates()) {
			ITracingGisCanvas<?> gisCanvas = creator.createGisCanvas();

			if (gisCanvas instanceof TracingOsmCanvas) {
				((TracingOsmCanvas) gisCanvas).loadAllTiles();
			}

			gisImage = CanvasUtils.getImage(set.isExportAsSvg(), gisCanvas);
			combinedImage = CanvasUtils.getImage(set.isExportAsSvg(), graphCanvas, gisCanvas);
		} else {
			gisImage = CanvasUtils.getImage(set.isExportAsSvg());
			combinedImage = CanvasUtils.getImage(set.isExportAsSvg());
		}

		set.setGisType(originalGisType);

		for (RowKey key : creator.getSkippedEdgeRows()) {
			setWarningMessage("Delivery Table: Row " + key.getString() + " skipped");
		}

		for (RowKey key : creator.getSkippedTracingRows()) {
			setWarningMessage("Tracing Table: Row " + key.getString() + " skipped");
		}

		for (RowKey key : creator.getSkippedShapeRows()) {
			setWarningMessage("Shape Table: Row " + key.getString() + " skipped");
		}

		graphCanvas.setJoinEdges(false);

		count = 0;
		maxCount = graphCanvas.getNodes().size() + graphCanvas.getEdges().size();

		BufferedDataTable nodeOutTable = createTable(graphCanvas.getNodes(), graphCanvas.getNodeSchema().getMap(),
				createNodeOutSpec(nodeTable.getSpec()), exec);
		BufferedDataTable edgeOutTable = createTable(graphCanvas.getEdges(), graphCanvas.getEdgeSchema().getMap(),
				createEdgeOutSpec(edgeTable.getSpec()), exec);

		return new PortObject[] { nodeOutTable, edgeOutTable, graphImage, gisImage, combinedImage };
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec nodeSpec = (DataTableSpec) inSpecs[0];
		DataTableSpec edgeSpec = (DataTableSpec) inSpecs[1];

		return new PortObjectSpec[] { createNodeOutSpec(nodeSpec), createEdgeOutSpec(edgeSpec),
				CanvasUtils.getImageSpec(set.isExportAsSvg()), CanvasUtils.getImageSpec(set.isExportAsSvg()),
				CanvasUtils.getImageSpec(set.isExportAsSvg()) };
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private BufferedDataTable createTable(Collection<? extends Element> elements, Map<String, Class<?>> propertyTypes,
			DataTableSpec spec, ExecutionContext exec) throws CanceledExecutionException {
		BufferedDataContainer nodeContainer = exec.createDataContainer(spec);
		int index = 0;

		for (Element node : elements) {
			DataCell[] cells = new DataCell[spec.getNumColumns()];

			for (int i = 0; i < cells.length; i++) {
				cells[i] = DataType.getMissingCell();
			}

			propertyTypes.forEach((property, type) -> {
				int column = spec.findColumnIndex(property);

				if (column != -1) {
					if (type == String.class) {
						cells[column] = IO.createCell((String) node.getProperties().get(property));
					} else if (type == Integer.class) {
						cells[column] = IO.createCell((Integer) node.getProperties().get(property));
					} else if (type == Double.class) {
						cells[column] = IO.createCell((Double) node.getProperties().get(property));
					} else if (type == Boolean.class) {
						cells[column] = IO.createCell((Boolean) node.getProperties().get(property));
					}
				}
			});

			nodeContainer.addRowToTable(new DefaultRow(index++ + "", cells));
			exec.checkCanceled();
			exec.setProgress((double) count / (double) maxCount);
			count++;
		}

		nodeContainer.close();

		return nodeContainer.getTable();
	}

	private static DataTableSpec createNodeOutSpec(DataTableSpec nodeSpec) throws InvalidSettingsException {
		List<DataColumnSpec> newNodeSpec = new ArrayList<>();
		Map<String, DataType> columns = new LinkedHashMap<>();

		for (DataColumnSpec column : nodeSpec) {
			if (column.getName().equals(TracingColumns.ID)) {
				column = new DataColumnSpecCreator(column.getName(), StringCell.TYPE).createSpec();
			}

			column = TracingUtils.toCompatibleColumn(column);
			newNodeSpec.add(column);
			columns.put(column.getName(), column.getType());
		}

		for (Map.Entry<String, DataType> entry : TracingColumns.COLUMN_TYPES.entrySet()) {
			DataType oldType = columns.get(entry.getKey());

			if (oldType == null) {
				newNodeSpec.add(new DataColumnSpecCreator(entry.getKey(), entry.getValue()).createSpec());
			} else if (!oldType.equals(entry.getValue())) {
				throw new InvalidSettingsException(
						"Type of column \"" + entry.getKey() + "\" must be \"" + entry.getValue() + "\"");
			}
		}

		return new DataTableSpec(newNodeSpec.toArray(new DataColumnSpec[0]));
	}

	private static DataTableSpec createEdgeOutSpec(DataTableSpec edgeSpec) throws InvalidSettingsException {
		List<DataColumnSpec> newEdgeSpec = new ArrayList<>();
		Map<String, DataType> columns = new LinkedHashMap<>();

		for (DataColumnSpec column : edgeSpec) {
			if (column.getName().equals(TracingColumns.ID) || column.getName().equals(TracingColumns.FROM)
					|| column.getName().equals(TracingColumns.TO)) {
				column = new DataColumnSpecCreator(column.getName(), StringCell.TYPE).createSpec();
			}

			column = TracingUtils.toCompatibleColumn(column);
			newEdgeSpec.add(column);
			columns.put(column.getName(), column.getType());
		}

		for (Map.Entry<String, DataType> entry : TracingColumns.COLUMN_TYPES.entrySet()) {
			DataType oldType = columns.get(entry.getKey());

			if (oldType == null) {
				newEdgeSpec.add(new DataColumnSpecCreator(entry.getKey(), entry.getValue()).createSpec());
			} else if (!oldType.equals(entry.getValue())) {
				throw new InvalidSettingsException(
						"Type of column \"" + entry.getKey() + "\" must be \"" + entry.getValue() + "\"");
			}
		}

		return new DataTableSpec(newEdgeSpec.toArray(new DataColumnSpec[0]));
	}
}
