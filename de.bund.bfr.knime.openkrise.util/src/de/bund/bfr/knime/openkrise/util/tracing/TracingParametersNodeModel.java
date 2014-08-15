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
package de.bund.bfr.knime.openkrise.util.tracing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.BooleanCell;
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
import org.knime.core.node.NotConfigurableException;

import com.thoughtworks.xstream.XStream;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.TracingConstants;
import de.bund.bfr.knime.openkrise.TracingUtils;

/**
 * This is the model implementation of TracingVisualizer.
 * 
 * 
 * @author Christian Thoens
 */
public class TracingParametersNodeModel extends NodeModel {

	private TracingParametersSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected TracingParametersNodeModel() {
		super(3, 2);
		set = new TracingParametersSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = inData[0];
		BufferedDataTable edgeTable = inData[1];
		BufferedDataTable dataTable = inData[2];
		Map<String, Class<?>> nodeProperties = TracingUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = TracingUtils
				.getTableColumns(edgeTable.getSpec());
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(
				nodeTable, nodeProperties);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable,
				edgeProperties, nodes);
		MyNewTracing tracing = new MyNewTracing(getDeliveries(dataTable),
				new LinkedHashMap<Integer, Double>(),
				new LinkedHashMap<Integer, Double>(),
				new LinkedHashSet<Integer>(), new LinkedHashSet<Integer>(), 0);

		Map<String, Double> nodeWeights = new LinkedHashMap<>();
		Map<String, Double> edgeWeights = new LinkedHashMap<>();
		Set<String> crossNodes = new LinkedHashSet<>();
		Set<String> crossEdges = new LinkedHashSet<>();
		Set<String> filterNodes = new LinkedHashSet<>();
		Set<String> filterEdges = new LinkedHashSet<>();
		Set<String> backwardNodes = new LinkedHashSet<>();
		Set<String> forwardNodes = new LinkedHashSet<>();
		Set<String> backwardEdges = new LinkedHashSet<>();
		Set<String> forwardEdges = new LinkedHashSet<>();

		for (GraphNode node : nodes.values()) {
			String id = node.getId();
			Double weight = null;

			if (set.getNodeWeightConditionValue() != null) {
				if (isInCondition(node, set.getNodeWeightCondition())) {
					weight = set.getNodeWeightConditionValue();
				}
			} else {
				weight = set.getNodeWeights().get(id);
			}

			if (weight != null && weight != 0.0) {
				nodeWeights.put(id, weight);
				tracing.setCase(Integer.parseInt(id), weight);
			}

			Boolean cross = null;

			if (set.getNodeContaminationConditionValue() != null) {
				if (isInCondition(node, set.getNodeContaminationCondition())) {
					cross = set.getNodeContaminationConditionValue();
				}
			} else {
				cross = set.getNodeCrossContaminations().get(id);
			}

			if (cross != null && cross) {
				crossNodes.add(id);
				tracing.setCrossContamination(Integer.parseInt(id), cross);
			}
		}

		for (Edge<GraphNode> edge : edges) {
			String id = edge.getId();
			Double weight = null;

			if (set.getEdgeWeightConditionValue() != null) {
				if (isInCondition(edge, set.getEdgeWeightCondition())) {
					weight = set.getEdgeWeightConditionValue();
				}
			} else {
				weight = set.getEdgeWeights().get(id);
			}

			if (weight != null && weight != 0.0) {
				edgeWeights.put(id, weight);
				tracing.setCaseDelivery(Integer.parseInt(id), weight);
			}

			Boolean cross = null;

			if (set.getEdgeContaminationConditionValue() != null) {
				if (isInCondition(edge, set.getEdgeContaminationCondition())) {
					cross = set.getEdgeContaminationConditionValue();
				}
			} else {
				cross = set.getEdgeCrossContaminations().get(id);
			}

			if (cross != null && cross) {
				crossEdges.add(id);
				tracing.setCrossContaminationDelivery(Integer.parseInt(id),
						cross);
			}
		}

		tracing.fillDeliveries(set.isEnforeTemporalOrder());

		for (GraphNode node : nodes.values()) {
			String id = node.getId();
			Boolean filter = null;

			if (set.getNodeFilterConditionValue() != null) {
				if (isInCondition(node, set.getNodeFilterCondition())) {
					filter = set.getNodeFilterConditionValue();
				}
			} else {
				filter = set.getNodeFilter().get(id);
			}

			if (filter != null && filter) {
				filterNodes.add(id);
				backwardNodes.addAll(TracingUtils.toString(tracing
						.getBackwardStations(Integer.parseInt(id))));
				forwardNodes.addAll(TracingUtils.toString(tracing
						.getForwardStations(Integer.parseInt(id))));
				backwardEdges.addAll(TracingUtils.toString(tracing
						.getBackwardDeliveries(Integer.parseInt(id))));
				forwardEdges.addAll(TracingUtils.toString(tracing
						.getForwardDeliveries(Integer.parseInt(id))));
			}
		}

		for (Edge<GraphNode> edge : edges) {
			String id = edge.getId();
			Boolean filter = null;

			if (set.getEdgeFilterConditionValue() != null) {
				if (isInCondition(edge, set.getEdgeFilterCondition())) {
					filter = set.getEdgeFilterConditionValue();
				}
			} else {
				filter = set.getEdgeFilter().get(id);
			}

			if (filter != null && filter) {
				filterEdges.add(id);
				backwardNodes.addAll(TracingUtils.toString(tracing
						.getBackwardStations2(Integer.parseInt(id))));
				forwardNodes.addAll(TracingUtils.toString(tracing
						.getForwardStations2(Integer.parseInt(id))));
				backwardEdges.addAll(TracingUtils.toString(tracing
						.getBackwardDeliveries2(Integer.parseInt(id))));
				forwardEdges.addAll(TracingUtils.toString(tracing
						.getForwardDeliveries2(Integer.parseInt(id))));
			}
		}

		int index = 0;
		DataTableSpec nodeInSpec = nodeTable.getSpec();
		DataTableSpec nodeOutSpec = createNodeOutSpec(nodeInSpec);
		BufferedDataContainer nodeContainer = exec
				.createDataContainer(nodeOutSpec);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(nodeInSpec
					.findColumnIndex(TracingConstants.ID_COLUMN)));
			DataCell[] cells = new DataCell[nodeOutSpec.getNumColumns()];

			for (DataColumnSpec column : nodeInSpec) {
				cells[nodeOutSpec.findColumnIndex(column.getName())] = row
						.getCell(nodeInSpec.findColumnIndex(column.getName()));
			}

			cells[nodeOutSpec
					.findColumnIndex(TracingConstants.CASE_WEIGHT_COLUMN)] = IO
					.createCell(nodeWeights.get(id));
			cells[nodeOutSpec
					.findColumnIndex(TracingConstants.CROSS_CONTAMINATION_COLUMN)] = IO
					.createCell(crossNodes.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingConstants.SCORE_COLUMN)] = IO
					.createCell(tracing.getStationScore(Integer.parseInt(id)));
			cells[nodeOutSpec.findColumnIndex(TracingConstants.FILTER_COLUMN)] = IO
					.createCell(filterNodes.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingConstants.BACKWARD_COLUMN)] = IO
					.createCell(backwardNodes.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingConstants.FORWARD_COLUMN)] = IO
					.createCell(forwardNodes.contains(id));

			nodeContainer.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) (nodeTable.getRowCount() + edgeTable
							.getRowCount()));
			index++;
		}

		nodeContainer.close();

		DataTableSpec edgeInSpec = edgeTable.getSpec();
		DataTableSpec edgeOutSpec = createEdgeOutSpec(edgeInSpec);
		BufferedDataContainer edgeContainer = exec
				.createDataContainer(edgeOutSpec);

		for (DataRow row : edgeTable) {
			String id = IO.getToCleanString(row.getCell(edgeInSpec
					.findColumnIndex(TracingConstants.ID_COLUMN)));
			DataCell[] cells = new DataCell[edgeOutSpec.getNumColumns()];

			for (DataColumnSpec column : edgeInSpec) {
				cells[edgeOutSpec.findColumnIndex(column.getName())] = row
						.getCell(edgeInSpec.findColumnIndex(column.getName()));
			}

			cells[edgeOutSpec
					.findColumnIndex(TracingConstants.CASE_WEIGHT_COLUMN)] = IO
					.createCell(edgeWeights.get(id));
			cells[edgeOutSpec
					.findColumnIndex(TracingConstants.CROSS_CONTAMINATION_COLUMN)] = IO
					.createCell(crossEdges.contains(id));
			cells[edgeOutSpec.findColumnIndex(TracingConstants.FILTER_COLUMN)] = IO
					.createCell(filterEdges.contains(id));
			cells[edgeOutSpec.findColumnIndex(TracingConstants.SCORE_COLUMN)] = IO
					.createCell(tracing.getDeliveryScore(Integer.parseInt(id)));
			cells[edgeOutSpec.findColumnIndex(TracingConstants.BACKWARD_COLUMN)] = IO
					.createCell(backwardEdges.contains(id));
			cells[edgeOutSpec.findColumnIndex(TracingConstants.FORWARD_COLUMN)] = IO
					.createCell(forwardEdges.contains(id));

			edgeContainer.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) (nodeTable.getRowCount() + edgeTable
							.getRowCount()));
			index++;
		}

		edgeContainer.close();

		return new BufferedDataTable[] { nodeContainer.getTable(),
				edgeContainer.getTable() };
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
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		DataTableSpec nodeSpec = inSpecs[0];
		DataTableSpec edgeSpec = inSpecs[1];

		return new DataTableSpec[] { createNodeOutSpec(nodeSpec),
				createEdgeOutSpec(edgeSpec) };
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

	protected static HashMap<Integer, MyDelivery> getDeliveries(
			BufferedDataTable dataTable) throws NotConfigurableException {
		if (dataTable.getRowCount() == 0) {
			throw new NotConfigurableException("Tracing Table is empty");
		}

		DataRow row = null;

		for (DataRow r : dataTable) {
			row = r;
			break;
		}

		DataCell cell = row.getCell(0);
		String xml = ((StringValue) cell).getStringValue();
		XStream xstream = MyNewTracing.getXStream();

		return ((MyNewTracing) xstream.fromXML(xml)).getAllDeliveries();
	}

	private static DataTableSpec createNodeOutSpec(DataTableSpec nodeSpec)
			throws InvalidSettingsException {
		List<DataColumnSpec> newNodeSpec = new ArrayList<>();
		Map<String, DataType> newColumns = new LinkedHashMap<>();

		newColumns.put(TracingConstants.CASE_WEIGHT_COLUMN, DoubleCell.TYPE);
		newColumns.put(TracingConstants.CROSS_CONTAMINATION_COLUMN,
				BooleanCell.TYPE);
		newColumns.put(TracingConstants.SCORE_COLUMN, DoubleCell.TYPE);
		newColumns.put(TracingConstants.FILTER_COLUMN, BooleanCell.TYPE);
		newColumns.put(TracingConstants.BACKWARD_COLUMN, BooleanCell.TYPE);
		newColumns.put(TracingConstants.FORWARD_COLUMN, BooleanCell.TYPE);

		for (DataColumnSpec column : nodeSpec) {
			if (newColumns.containsKey(column.getName())) {
				throw new InvalidSettingsException("Column name \""
						+ column.getName() + "\" is not allowed in input table");
			}

			newNodeSpec.add(column);
		}

		for (String column : newColumns.keySet()) {
			newNodeSpec.add(new DataColumnSpecCreator(column, newColumns
					.get(column)).createSpec());
		}

		return new DataTableSpec(newNodeSpec.toArray(new DataColumnSpec[0]));
	}

	private static DataTableSpec createEdgeOutSpec(DataTableSpec edgeSpec)
			throws InvalidSettingsException {
		List<DataColumnSpec> newEdgeSpec = new ArrayList<>();
		Map<String, DataType> newColumns = new LinkedHashMap<>();

		newColumns.put(TracingConstants.CASE_WEIGHT_COLUMN, DoubleCell.TYPE);
		newColumns.put(TracingConstants.CROSS_CONTAMINATION_COLUMN,
				BooleanCell.TYPE);
		newColumns.put(TracingConstants.FILTER_COLUMN, BooleanCell.TYPE);
		newColumns.put(TracingConstants.SCORE_COLUMN, DoubleCell.TYPE);
		newColumns.put(TracingConstants.BACKWARD_COLUMN, BooleanCell.TYPE);
		newColumns.put(TracingConstants.FORWARD_COLUMN, BooleanCell.TYPE);

		for (DataColumnSpec column : edgeSpec) {
			if (newColumns.containsKey(column.getName())) {
				throw new InvalidSettingsException("Column name \""
						+ column.getName() + "\" is not allowed in input table");
			}

			newEdgeSpec.add(column);
		}

		for (String column : newColumns.keySet()) {
			newEdgeSpec.add(new DataColumnSpecCreator(column, newColumns
					.get(column)).createSpec());
		}

		return new DataTableSpec(newEdgeSpec.toArray(new DataColumnSpec[0]));
	}

	private static boolean isInCondition(Element element,
			AndOrHighlightCondition condition) {
		if (condition == null) {
			return true;
		}

		return condition.getValues(Arrays.asList(element)).get(element) != 0.0;
	}

}
