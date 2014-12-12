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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;

/**
 * This is the model implementation of TracingVisualizer.
 * 
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeModel extends NodeModel {

	private TracingViewSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected TracingViewNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE,
				BufferedDataTable.TYPE, BufferedDataTable.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE,
						BufferedDataTable.TYPE, ImagePortObject.TYPE,
						BufferedDataTable.TYPE });
		set = new TracingViewSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		BufferedDataTable nodeTable = (BufferedDataTable) inObjects[0];
		BufferedDataTable edgeTable = (BufferedDataTable) inObjects[1];
		HashMap<Integer, MyDelivery> tracing = TracingUtils.getDeliveries(
				(BufferedDataTable) inObjects[2], edgeTable);
		TracingCanvas canvas = new TracingViewCanvasCreator(nodeTable,
				edgeTable, tracing, set).createGraphCanvas();
		TracingCanvas allEdgesCanvas = createAllEdgesCanvas(nodeTable,
				edgeTable, tracing, set);

		int index = 0;
		DataTableSpec nodeOutSpec = createNodeOutSpec(nodeTable.getSpec());
		BufferedDataContainer nodeContainer = exec
				.createDataContainer(nodeOutSpec);

		for (GraphNode node : allEdgesCanvas.getNodes()) {
			DataCell[] cells = new DataCell[nodeOutSpec.getNumColumns()];

			for (int i = 0; i < cells.length; i++) {
				cells[i] = DataType.getMissingCell();
			}

			for (String property : allEdgesCanvas.getNodeProperties().keySet()) {
				int column = nodeOutSpec.findColumnIndex(property);

				if (column != -1) {
					Class<?> type = allEdgesCanvas.getNodeProperties().get(
							property);

					if (type == String.class) {
						cells[column] = IO.createCell((String) node
								.getProperties().get(property));
					} else if (type == Integer.class) {
						cells[column] = IO.createCell((Integer) node
								.getProperties().get(property));
					} else if (type == Double.class) {
						cells[column] = IO.createCell((Double) node
								.getProperties().get(property));
					} else if (type == Boolean.class) {
						cells[column] = IO.createCell((Boolean) node
								.getProperties().get(property));
					}
				}
			}

			nodeContainer.addRowToTable(new DefaultRow(index + "", cells));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) (allEdgesCanvas.getNodes().size() + allEdgesCanvas
							.getEdges().size()));
			index++;
		}

		nodeContainer.close();

		DataTableSpec edgeOutSpec = createEdgeOutSpec(edgeTable.getSpec());
		BufferedDataContainer edgeContainer = exec
				.createDataContainer(edgeOutSpec);

		for (Edge<GraphNode> edge : allEdgesCanvas.getEdges()) {
			DataCell[] cells = new DataCell[edgeOutSpec.getNumColumns()];

			for (int i = 0; i < cells.length; i++) {
				cells[i] = DataType.getMissingCell();
			}

			for (String property : allEdgesCanvas.getEdgeProperties().keySet()) {
				int column = edgeOutSpec.findColumnIndex(property);

				if (column != -1) {
					Class<?> type = allEdgesCanvas.getEdgeProperties().get(
							property);

					if (type == String.class) {
						cells[column] = IO.createCell((String) edge
								.getProperties().get(property));
					} else if (type == Integer.class) {
						cells[column] = IO.createCell((Integer) edge
								.getProperties().get(property));
					} else if (type == Double.class) {
						cells[column] = IO.createCell((Double) edge
								.getProperties().get(property));
					} else if (type == Boolean.class) {
						cells[column] = IO.createCell((Boolean) edge
								.getProperties().get(property));
					}
				}
			}

			edgeContainer.addRowToTable(new DefaultRow((index - allEdgesCanvas
					.getNodes().size()) + "", cells));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) (allEdgesCanvas.getNodes().size() + allEdgesCanvas
							.getEdges().size()));
			index++;
		}

		edgeContainer.close();

		return new PortObject[] { nodeContainer.getTable(),
				edgeContainer.getTable(),
				CanvasUtils.getImage(set.isExportAsSvg(), canvas),
				KnimeUtils.xmlToTable(set.toXml(), exec) };
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		DataTableSpec nodeSpec = (DataTableSpec) inSpecs[0];
		DataTableSpec edgeSpec = (DataTableSpec) inSpecs[1];

		return new PortObjectSpec[] { createNodeOutSpec(nodeSpec),
				createEdgeOutSpec(edgeSpec),
				CanvasUtils.getImageSpec(set.isExportAsSvg()),
				KnimeUtils.getXmlSpec() };
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

	private static DataTableSpec createNodeOutSpec(DataTableSpec nodeSpec)
			throws InvalidSettingsException {
		List<DataColumnSpec> newNodeSpec = new ArrayList<>();
		Map<String, DataType> columns = new LinkedHashMap<>();

		for (DataColumnSpec column : nodeSpec) {
			if (column.getName().equals(TracingColumns.ID)) {
				column = new DataColumnSpecCreator(column.getName(),
						StringCell.TYPE).createSpec();
			}

			newNodeSpec.add(column);
			columns.put(column.getName(), column.getType());
		}

		Map<String, DataType> newColumns = new LinkedHashMap<>();

		newColumns.put(TracingColumns.WEIGHT, DoubleCell.TYPE);
		newColumns.put(TracingColumns.CROSS_CONTAMINATION, BooleanCell.TYPE);
		newColumns.put(TracingColumns.SCORE, DoubleCell.TYPE);
		newColumns.put(TracingColumns.OBSERVED, BooleanCell.TYPE);
		newColumns.put(TracingColumns.BACKWARD, BooleanCell.TYPE);
		newColumns.put(TracingColumns.FORWARD, BooleanCell.TYPE);

		for (String column : newColumns.keySet()) {
			if (!columns.containsKey(column)) {
				newNodeSpec.add(new DataColumnSpecCreator(column, newColumns
						.get(column)).createSpec());
			} else if (!columns.get(column).equals(newColumns.get(column))) {
				throw new InvalidSettingsException("Type of column \"" + column
						+ "\" must be \"" + newColumns.get(column) + "\"");
			}
		}

		return new DataTableSpec(newNodeSpec.toArray(new DataColumnSpec[0]));
	}

	private static DataTableSpec createEdgeOutSpec(DataTableSpec edgeSpec)
			throws InvalidSettingsException {
		List<DataColumnSpec> newEdgeSpec = new ArrayList<>();
		Map<String, DataType> columns = new LinkedHashMap<>();

		for (DataColumnSpec column : edgeSpec) {
			if (column.getName().equals(TracingColumns.ID)
					|| column.getName().equals(TracingColumns.FROM)
					|| column.getName().equals(TracingColumns.TO)) {
				column = new DataColumnSpecCreator(column.getName(),
						StringCell.TYPE).createSpec();
			}

			newEdgeSpec.add(column);
			columns.put(column.getName(), column.getType());
		}

		Map<String, DataType> newColumns = new LinkedHashMap<>();

		newColumns.put(TracingColumns.WEIGHT, DoubleCell.TYPE);
		newColumns.put(TracingColumns.CROSS_CONTAMINATION, BooleanCell.TYPE);
		newColumns.put(TracingColumns.OBSERVED, BooleanCell.TYPE);
		newColumns.put(TracingColumns.SCORE, DoubleCell.TYPE);
		newColumns.put(TracingColumns.BACKWARD, BooleanCell.TYPE);
		newColumns.put(TracingColumns.FORWARD, BooleanCell.TYPE);

		for (String column : newColumns.keySet()) {
			if (!columns.containsKey(column)) {
				newEdgeSpec.add(new DataColumnSpecCreator(column, newColumns
						.get(column)).createSpec());
			} else if (!columns.get(column).equals(newColumns.get(column))) {
				throw new InvalidSettingsException("Type of column \"" + column
						+ "\" must be \"" + newColumns.get(column) + "\"");
			}
		}

		return new DataTableSpec(newEdgeSpec.toArray(new DataColumnSpec[0]));
	}

	private static TracingCanvas createAllEdgesCanvas(
			BufferedDataTable nodeTable, BufferedDataTable edgeTable,
			HashMap<Integer, MyDelivery> deliveries, TracingViewSettings set)
			throws InvalidSettingsException {
		boolean joinEdges = set.getGraphSettings().isJoinEdges();

		set.getGraphSettings().setJoinEdges(false);

		TracingCanvas canvas = new TracingViewCanvasCreator(nodeTable,
				edgeTable, deliveries, set).createGraphCanvas();

		set.getGraphSettings().setJoinEdges(joinEdges);

		return canvas;
	}

}
