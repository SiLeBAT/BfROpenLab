/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.openkrise.util.tracing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.knime.core.data.RowKey;
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

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.openkrise.Delivery;
import de.bund.bfr.knime.openkrise.Tracing;
import de.bund.bfr.knime.openkrise.TracingColumns;
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
		BufferedDataTable tracingTable = inData[2];
		NodePropertySchema nodeSchema = new NodePropertySchema(
				TracingUtils.getTableColumns(nodeTable.getSpec()), TracingColumns.ID);
		EdgePropertySchema edgeSchema = new EdgePropertySchema(
				TracingUtils.getTableColumns(edgeTable.getSpec()), TracingColumns.ID,
				TracingColumns.FROM, TracingColumns.TO);
		Set<RowKey> skippedEdgeRows = new LinkedHashSet<>();
		Set<RowKey> skippedTracingRows = new LinkedHashSet<>();

		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes,
				skippedEdgeRows);
		Map<String, Delivery> deliveries = TracingUtils.readDeliveries(tracingTable, edges,
				skippedTracingRows);
		Tracing tracing = new Tracing(deliveries);

		for (RowKey key : skippedEdgeRows) {
			setWarningMessage("Delivery Table: Row " + key.getString() + " skipped");
		}

		for (RowKey key : skippedTracingRows) {
			setWarningMessage("Tracing Table: Row " + key.getString() + " skipped");
		}

		Map<String, Double> nodeWeights = new LinkedHashMap<>();
		Map<String, Double> edgeWeights = new LinkedHashMap<>();
		Set<String> crossNodes = new LinkedHashSet<>();
		Set<String> crossEdges = new LinkedHashSet<>();

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
				tracing.setCase(id, weight);
			} else {
				nodeWeights.put(id, 0.0);
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
				tracing.setCrossContamination(id, cross);
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
				tracing.setCaseDelivery(id, weight);
			} else {
				edgeWeights.put(id, 0.0);
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
				tracing.setCrossContaminationDelivery(id, cross);
			}
		}

		tracing.init(set.isEnforeTemporalOrder());

		Set<String> filterNodes = new LinkedHashSet<>();
		Set<String> filterEdges = new LinkedHashSet<>();
		Set<String> backwardNodes = new LinkedHashSet<>();
		Set<String> forwardNodes = new LinkedHashSet<>();
		Set<String> backwardEdges = new LinkedHashSet<>();
		Set<String> forwardEdges = new LinkedHashSet<>();

		for (GraphNode node : nodes.values()) {
			String id = node.getId();
			Boolean filter = null;

			if (set.getObservedNodesConditionValue() != null) {
				if (isInCondition(node, set.getObservedNodesCondition())) {
					filter = set.getObservedNodesConditionValue();
				}
			} else {
				filter = set.getObservedNodes().get(id);
			}

			if (filter != null && filter) {
				filterNodes.add(id);
				backwardNodes.addAll(tracing.getBackwardStations(id));
				forwardNodes.addAll(tracing.getForwardStations(id));
				backwardEdges.addAll(tracing.getBackwardDeliveries(id));
				forwardEdges.addAll(tracing.getForwardDeliveries(id));
			}
		}

		for (Edge<GraphNode> edge : edges) {
			String id = edge.getId();
			Boolean filter = null;

			if (set.getObservedEdgesConditionValue() != null) {
				if (isInCondition(edge, set.getObservedEdgesCondition())) {
					filter = set.getObservedEdgesConditionValue();
				}
			} else {
				filter = set.getObservedEdges().get(id);
			}

			if (filter != null && filter) {
				filterEdges.add(id);
				backwardNodes.addAll(tracing.getBackwardStations2(id));
				forwardNodes.addAll(tracing.getForwardStations2(id));
				backwardEdges.addAll(tracing.getBackwardDeliveries2(id));
				forwardEdges.addAll(tracing.getForwardDeliveries2(id));
			}
		}

		Map<String, Double> nodeScores = new LinkedHashMap<>();
		Map<String, Double> edgeScores = new LinkedHashMap<>();

		for (GraphNode node : nodes.values()) {
			nodeScores.put(node.getId(), tracing.getStationScore(node.getId()));
		}

		for (Edge<GraphNode> edge : edges) {
			edgeScores.put(edge.getId(), tracing.getDeliveryScore(edge.getId()));
		}

		double maxScore = Math.max(Collections.max(nodeScores.values()),
				Collections.max(edgeScores.values()));
		double normFactor = maxScore > 0.0 ? 1.0 / maxScore : 1.0;

		int index = 0;
		DataTableSpec nodeOutSpec = createNodeOutSpec(nodeTable.getSpec());
		BufferedDataContainer nodeContainer = exec.createDataContainer(nodeOutSpec);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(nodeTable.getSpec().findColumnIndex(
					TracingColumns.ID)));
			DataCell[] cells = new DataCell[nodeOutSpec.getNumColumns()];

			for (DataColumnSpec column : nodeTable.getSpec()) {
				cells[nodeOutSpec.findColumnIndex(column.getName())] = row.getCell(nodeTable
						.getSpec().findColumnIndex(column.getName()));
			}

			cells[nodeOutSpec.findColumnIndex(TracingColumns.WEIGHT)] = IO.createCell(nodeWeights
					.get(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.CROSS_CONTAMINATION)] = IO
					.createCell(crossNodes.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.SCORE)] = IO.createCell(nodeScores
					.get(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.NORMALIZED_SCORE)] = IO
					.createCell(normFactor * nodeScores.get(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.OBSERVED)] = IO.createCell(filterNodes
					.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.BACKWARD)] = IO
					.createCell(backwardNodes.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.FORWARD)] = IO.createCell(forwardNodes
					.contains(id));

			nodeContainer.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) (nodeTable.getRowCount() + edgeTable.getRowCount()));
			index++;
		}

		nodeContainer.close();

		DataTableSpec edgeOutSpec = createEdgeOutSpec(edgeTable.getSpec());
		BufferedDataContainer edgeContainer = exec.createDataContainer(edgeOutSpec);

		for (DataRow row : edgeTable) {
			String id = IO.getToCleanString(row.getCell(edgeTable.getSpec().findColumnIndex(
					TracingColumns.ID)));
			DataCell[] cells = new DataCell[edgeOutSpec.getNumColumns()];

			for (DataColumnSpec column : edgeTable.getSpec()) {
				cells[edgeOutSpec.findColumnIndex(column.getName())] = row.getCell(edgeTable
						.getSpec().findColumnIndex(column.getName()));
			}

			cells[edgeOutSpec.findColumnIndex(TracingColumns.WEIGHT)] = IO.createCell(edgeWeights
					.get(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.CROSS_CONTAMINATION)] = IO
					.createCell(crossEdges.contains(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.OBSERVED)] = IO.createCell(filterEdges
					.contains(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.SCORE)] = IO.createCell(edgeScores
					.get(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.NORMALIZED_SCORE)] = IO
					.createCell(normFactor * edgeScores.get(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.BACKWARD)] = IO
					.createCell(backwardEdges.contains(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.FORWARD)] = IO.createCell(forwardEdges
					.contains(id));

			edgeContainer.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) (nodeTable.getRowCount() + edgeTable.getRowCount()));
			index++;
		}

		edgeContainer.close();

		return new BufferedDataTable[] { nodeContainer.getTable(), edgeContainer.getTable() };
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
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec nodeSpec = inSpecs[0];
		DataTableSpec edgeSpec = inSpecs[1];

		return new DataTableSpec[] { createNodeOutSpec(nodeSpec), createEdgeOutSpec(edgeSpec) };
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

	private static DataTableSpec createNodeOutSpec(DataTableSpec nodeSpec)
			throws InvalidSettingsException {
		List<DataColumnSpec> newNodeSpec = new ArrayList<>();
		Map<String, DataType> newColumns = new LinkedHashMap<>();

		newColumns.put(TracingColumns.WEIGHT, DoubleCell.TYPE);
		newColumns.put(TracingColumns.CROSS_CONTAMINATION, BooleanCell.TYPE);
		newColumns.put(TracingColumns.SCORE, DoubleCell.TYPE);
		newColumns.put(TracingColumns.NORMALIZED_SCORE, DoubleCell.TYPE);
		newColumns.put(TracingColumns.OBSERVED, BooleanCell.TYPE);
		newColumns.put(TracingColumns.BACKWARD, BooleanCell.TYPE);
		newColumns.put(TracingColumns.FORWARD, BooleanCell.TYPE);

		for (DataColumnSpec column : nodeSpec) {
			if (newColumns.containsKey(column.getName())) {
				throw new InvalidSettingsException("Column name \"" + column.getName()
						+ "\" is not allowed in input table");
			}

			newNodeSpec.add(column);
		}

		for (Map.Entry<String, DataType> entry : newColumns.entrySet()) {
			newNodeSpec.add(new DataColumnSpecCreator(entry.getKey(), entry.getValue())
					.createSpec());
		}

		return new DataTableSpec(newNodeSpec.toArray(new DataColumnSpec[0]));
	}

	private static DataTableSpec createEdgeOutSpec(DataTableSpec edgeSpec)
			throws InvalidSettingsException {
		List<DataColumnSpec> newEdgeSpec = new ArrayList<>();
		Map<String, DataType> newColumns = new LinkedHashMap<>();

		newColumns.put(TracingColumns.WEIGHT, DoubleCell.TYPE);
		newColumns.put(TracingColumns.CROSS_CONTAMINATION, BooleanCell.TYPE);
		newColumns.put(TracingColumns.OBSERVED, BooleanCell.TYPE);
		newColumns.put(TracingColumns.SCORE, DoubleCell.TYPE);
		newColumns.put(TracingColumns.NORMALIZED_SCORE, DoubleCell.TYPE);
		newColumns.put(TracingColumns.BACKWARD, BooleanCell.TYPE);
		newColumns.put(TracingColumns.FORWARD, BooleanCell.TYPE);

		for (DataColumnSpec column : edgeSpec) {
			if (newColumns.containsKey(column.getName())) {
				throw new InvalidSettingsException("Column name \"" + column.getName()
						+ "\" is not allowed in input table");
			}

			newEdgeSpec.add(column);
		}

		for (Map.Entry<String, DataType> entry : newColumns.entrySet()) {
			newEdgeSpec.add(new DataColumnSpecCreator(entry.getKey(), entry.getValue())
					.createSpec());
		}

		return new DataTableSpec(newEdgeSpec.toArray(new DataColumnSpec[0]));
	}

	private static boolean isInCondition(Element element, AndOrHighlightCondition condition) {
		if (condition == null) {
			return true;
		}

		return condition.getValues(Arrays.asList(element)).get(element) != 0.0;
	}

}
