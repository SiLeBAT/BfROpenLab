/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

import java.util.ArrayList;
import java.util.Collection;
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
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.common.Delivery;
import de.bund.bfr.knime.openkrise.common.Tracing;

/**
 * This is the model implementation of TracingVisualizer.
 * 
 * 
 * @author Christian Thoens
 */
public class TracingParametersNodeModel extends NoInternalsNodeModel {

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
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		BufferedDataTable nodeTable = inData[0];
		BufferedDataTable edgeTable = inData[1];
		BufferedDataTable tracingTable = inData[2];
		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(nodeTable.getSpec()),
				TracingColumns.ID);
		EdgePropertySchema edgeSchema = new EdgePropertySchema(TracingUtils.getTableColumns(edgeTable.getSpec()),
				TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO);
		Map<RowKey, String> skippedDeliveryRows = new LinkedHashMap<>();
		Map<RowKey, String> skippedDeliveryRelationsRows = new LinkedHashMap<>();

		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedDeliveryRows);
		Map<String, Delivery> deliveries = TracingUtils.readDeliveries(tracingTable, edges,
				skippedDeliveryRelationsRows);
		Tracing tracing = new Tracing(deliveries.values());

		skippedDeliveryRows.forEach((key,
				value) -> setWarningMessage("Deliveries Table: Row " + key.getString() + " skipped (" + value + ")"));
		skippedDeliveryRelationsRows.forEach((key, value) -> setWarningMessage(
				"Deliveries Relations Table: Row " + key.getString() + " skipped (" + value + ")"));

		Map<String, Double> nodeWeights = createValueMap(nodes.values(), set.getNodeWeightCondition(),
				set.getNodeWeightConditionValue(), 0.0, set.getNodeWeights());
		Map<String, Double> edgeWeights = createValueMap(edges, set.getEdgeWeightCondition(),
				set.getEdgeWeightConditionValue(), 0.0, set.getEdgeWeights());
		Map<String, Boolean> crossNodes = createValueMap(nodes.values(), set.getNodeContaminationCondition(),
				set.getNodeContaminationConditionValue(), false, set.getNodeCrossContaminations());
		Map<String, Boolean> crossEdges = createValueMap(edges, set.getEdgeContaminationCondition(),
				set.getEdgeContaminationConditionValue(), false, set.getEdgeCrossContaminations());
		Map<String, Boolean> killNodes = createValueMap(nodes.values(), set.getNodeKillCondition(),
				set.getNodeKillConditionValue(), false, set.getNodeKillContaminations());
		Map<String, Boolean> killEdges = createValueMap(edges, set.getEdgeKillCondition(),
				set.getEdgeKillConditionValue(), false, set.getEdgeKillContaminations());

		nodeWeights.forEach((stationId, weight) -> tracing.setStationWeight(stationId, weight));
		edgeWeights.forEach((deliveryId, weight) -> tracing.setDeliveryWeight(deliveryId, weight));
		crossNodes.forEach((stationId, isCross) -> tracing.setCrossContaminationOfStation(stationId, isCross));
		crossEdges.forEach((deliveryId, isCross) -> tracing.setCrossContaminationOfDelivery(deliveryId, isCross));
		killNodes.forEach((stationId, isKill) -> tracing.setKillContaminationOfStation(stationId, isKill));
		killEdges.forEach((deliveryId, isKill) -> tracing.setKillContaminationOfDelivery(deliveryId, isKill));

		Tracing.Result result = tracing.getResult(set.isEnforeTemporalOrder());
		Map<String, Boolean> observedNodes = createValueMap(nodes.values(), set.getObservedNodesCondition(),
				set.getObservedNodesConditionValue(), false, set.getObservedNodes());
		Map<String, Boolean> observedEdges = createValueMap(edges, set.getObservedEdgesCondition(),
				set.getObservedEdgesConditionValue(), false, set.getObservedEdges());
		Set<String> backwardNodes = new LinkedHashSet<>();
		Set<String> forwardNodes = new LinkedHashSet<>();
		Set<String> backwardEdges = new LinkedHashSet<>();
		Set<String> forwardEdges = new LinkedHashSet<>();

		observedNodes.forEach((stationId, observed) -> {
			if (observed) {
				backwardNodes.addAll(result.getBackwardStationsByStation().get(stationId));
				forwardNodes.addAll(result.getForwardStationsByStation().get(stationId));
				backwardEdges.addAll(result.getBackwardDeliveriesByStation().get(stationId));
				forwardEdges.addAll(result.getForwardDeliveriesByStation().get(stationId));
			}
		});

		observedEdges.forEach((deliveryId, observed) -> {
			if (observed) {
				backwardNodes.addAll(result.getBackwardStationsByDelivery().get(deliveryId));
				forwardNodes.addAll(result.getForwardStationsByDelivery().get(deliveryId));
				backwardEdges.addAll(result.getBackwardDeliveriesByDelivery().get(deliveryId));
				forwardEdges.addAll(result.getForwardDeliveriesByDelivery().get(deliveryId));
			}
		});

		int index = 0;
		DataTableSpec nodeOutSpec = createOutSpec(nodeTable.getSpec(), TracingColumns.STATION_IN_OUT_COLUMNS);
		BufferedDataContainer nodeContainer = exec.createDataContainer(nodeOutSpec);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(nodeTable.getSpec().findColumnIndex(TracingColumns.ID)));
			DataCell[] cells = new DataCell[nodeOutSpec.getNumColumns()];

			for (String column : nodeTable.getSpec().getColumnNames()) {
				cells[nodeOutSpec.findColumnIndex(column)] = row.getCell(nodeTable.getSpec().findColumnIndex(column));
			}

			cells[nodeOutSpec.findColumnIndex(TracingColumns.WEIGHT)] = IO
					.createCell(nodeWeights.containsKey(id) ? nodeWeights.get(id) : 0.0);
			cells[nodeOutSpec.findColumnIndex(TracingColumns.CROSS_CONTAMINATION)] = IO
					.createCell(crossNodes.containsKey(id) ? crossNodes.get(id) : false);
			cells[nodeOutSpec.findColumnIndex(TracingColumns.KILL_CONTAMINATION)] = IO
					.createCell(killNodes.containsKey(id) ? killNodes.get(id) : false);
			cells[nodeOutSpec.findColumnIndex(TracingColumns.OBSERVED)] = IO
					.createCell(observedNodes.containsKey(id) ? observedNodes.get(id) : false);
			cells[nodeOutSpec.findColumnIndex(TracingColumns.SCORE)] = IO.createCell(result.getStationScore(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.MAX_LOT_SCORE)] = IO.createCell(result.getMaxLotScore(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.NORMALIZED_SCORE)] = IO
					.createCell(result.getStationNormalizedScore(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.POSITIVE_SCORE)] = IO
					.createCell(result.getStationPositiveScore(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.NEGATIVE_SCORE)] = IO
					.createCell(result.getStationNegativeScore(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.BACKWARD)] = IO.createCell(backwardNodes.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingColumns.FORWARD)] = IO.createCell(forwardNodes.contains(id));

			nodeContainer.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index / (double) (nodeTable.size() + edgeTable.size()));
			index++;
		}

		nodeContainer.close();

		DataTableSpec edgeOutSpec = createOutSpec(edgeTable.getSpec(), TracingColumns.DELIVERY_IN_OUT_COLUMNS);
		BufferedDataContainer edgeContainer = exec.createDataContainer(edgeOutSpec);

		for (DataRow row : edgeTable) {
			String id = IO.getToCleanString(row.getCell(edgeTable.getSpec().findColumnIndex(TracingColumns.ID)));
			DataCell[] cells = new DataCell[edgeOutSpec.getNumColumns()];

			for (String column : edgeTable.getSpec().getColumnNames()) {
				cells[edgeOutSpec.findColumnIndex(column)] = row.getCell(edgeTable.getSpec().findColumnIndex(column));
			}

			cells[edgeOutSpec.findColumnIndex(TracingColumns.WEIGHT)] = IO
					.createCell(edgeWeights.containsKey(id) ? edgeWeights.get(id) : 0.0);
			cells[edgeOutSpec.findColumnIndex(TracingColumns.CROSS_CONTAMINATION)] = IO
					.createCell(crossEdges.containsKey(id) ? crossEdges.get(id) : false);
			cells[edgeOutSpec.findColumnIndex(TracingColumns.KILL_CONTAMINATION)] = IO
					.createCell(killEdges.containsKey(id) ? killEdges.get(id) : false);
			cells[edgeOutSpec.findColumnIndex(TracingColumns.OBSERVED)] = IO
					.createCell(observedEdges.containsKey(id) ? observedEdges.get(id) : false);
			cells[edgeOutSpec.findColumnIndex(TracingColumns.SCORE)] = IO.createCell(result.getDeliveryScore(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.LOT_SCORE)] = IO.createCell(result.getLotScore(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.NORMALIZED_SCORE)] = IO
					.createCell(result.getDeliveryNormalizedScore(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.POSITIVE_SCORE)] = IO
					.createCell(result.getDeliveryPositiveScore(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.NEGATIVE_SCORE)] = IO
					.createCell(result.getDeliveryNegativeScore(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.BACKWARD)] = IO.createCell(backwardEdges.contains(id));
			cells[edgeOutSpec.findColumnIndex(TracingColumns.FORWARD)] = IO.createCell(forwardEdges.contains(id));

			edgeContainer.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index / (double) (nodeTable.size() + edgeTable.size()));
			index++;
		}

		edgeContainer.close();

		return new BufferedDataTable[] { nodeContainer.getTable(), edgeContainer.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return new DataTableSpec[] { createOutSpec(inSpecs[0], TracingColumns.STATION_IN_OUT_COLUMNS),
				createOutSpec(inSpecs[1], TracingColumns.DELIVERY_IN_OUT_COLUMNS) };
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

	private static DataTableSpec createOutSpec(DataTableSpec spec, List<String> columns)
			throws InvalidSettingsException {
		List<DataColumnSpec> outSpec = new ArrayList<>();

		for (DataColumnSpec column : spec) {
			if (columns.contains(column.getName())) {
				throw new InvalidSettingsException(
						"Column name \"" + column.getName() + "\" is not allowed in input table");
			}

			outSpec.add(column);
		}

		for (String column : columns) {
			outSpec.add(new DataColumnSpecCreator(column, TracingColumns.IN_OUT_COLUMN_TYPES.get(column)).createSpec());
		}

		return new DataTableSpec(outSpec.toArray(new DataColumnSpec[0]));
	}

	private static <T> Map<String, T> createValueMap(Collection<? extends Element> elements,
			AndOrHighlightCondition condition, T inValue, T outValue, Map<String, T> defaultValues) {
		Map<String, T> result = new LinkedHashMap<>();

		if (inValue != null) {
			if (condition != null) {
				condition.getValues(elements)
						.forEach((element, value) -> result.put(element.getId(), value != 0.0 ? inValue : outValue));
			} else {
				elements.forEach(e -> result.put(e.getId(), inValue));
			}
		} else {
			for (Element element : elements) {
				T value = defaultValues.get(element.getId());

				result.put(element.getId(), value != null ? value : outValue);
			}
		}

		return result;
	}
}
