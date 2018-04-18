/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.network.analyzer;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.network.core.analyzer.Analyzer;
import org.knime.network.core.analyzer.AnalyzerType;
import org.knime.network.core.analyzer.NumericAnalyzer;
import org.knime.network.core.api.GraphObjectIterator;
import org.knime.network.core.api.GraphObjectType;
import org.knime.network.core.api.KPartiteGraphView;
import org.knime.network.core.api.Partition;
import org.knime.network.core.api.PersistentObject;
import org.knime.network.core.core.exception.PersistenceException;

public class ClosenessAnalyzerType extends NumericAnalyzer<PersistentObject> implements AnalyzerType<PersistentObject> {

	private static final String ID = "Closeness";
	private static final String NAME = "Closeness Centrality (BfR)";

	private int numberOfNodes;
	private int numberOfEdges;
	private Map<String, Double> edgeWeights;
	private Map<String, Collection<String>> incidentNodes;
	private Map<String, Collection<String>> outgoingEdges;

	public ClosenessAnalyzerType() {
		super(new String[] { ID });
		numberOfNodes = 0;
		numberOfEdges = 0;
		edgeWeights = new HashMap<>();
		incidentNodes = new HashMap<>();
		outgoingEdges = new HashMap<>();
	}

	@Override
	protected void initializeInternal(KPartiteGraphView<PersistentObject, Partition> view, ExecutionMonitor exec)
			throws PersistenceException, CanceledExecutionException {
		super.initializeInternal(view, exec);
		numberOfNodes = (int) view.getNoOfNodes();
		numberOfEdges = (int) view.getNoOfEdges();
		edgeWeights.clear();
		incidentNodes.clear();
		outgoingEdges.clear();

		for (PersistentObject edge : view.getEdges()) {
			edgeWeights.put(edge.getId(), view.getEdgeWeight(edge));
			incidentNodes.put(edge.getId(),
					view.getIncidentNodes(edge).stream().map(o -> o.getId()).collect(Collectors.toList()));
		}

		if (edgeWeights.values().stream().allMatch(v -> v == 1.0)) {
			edgeWeights.clear();
		}

		for (PersistentObject node : view.getNodes()) {
			outgoingEdges.put(node.getId(),
					view.getOutgoingEdges(node).stream().map(o -> o.getId()).collect(Collectors.toList()));
		}
	}

	@Override
	protected double[] numericAnalyzeInternal(ExecutionMonitor exec,
			KPartiteGraphView<PersistentObject, Partition> view, PersistentObject object)
			throws PersistenceException, CanceledExecutionException {
		if (edgeWeights.isEmpty()) {
			return new double[] { computeWithoutEdgeWeights(object.getId()) };
		} else {
			return new double[] { computeWithEdgeWeights(object.getId()) };
		}
	}

	@Override
	protected GraphObjectIterator<PersistentObject> getGraphObjectIterator(
			KPartiteGraphView<PersistentObject, Partition> view) throws PersistenceException {
		return view.getNodes();
	}

	private double computeWithoutEdgeWeights(String nodeId) {
		Deque<String> nodeQueue = new LinkedList<>();
		Map<String, Integer> visitedNodes = new HashMap<>(numberOfNodes, 1.0f);
		Set<String> visitedEdges = new HashSet<>(numberOfEdges, 1.0f);
		int distanceSum = 0;

		visitedNodes.put(nodeId, 0);
		nodeQueue.addLast(nodeId);

		while (!nodeQueue.isEmpty()) {
			String currentNodeId = nodeQueue.removeFirst();
			int targetNodeDistance = visitedNodes.get(currentNodeId) + 1;

			for (String edgeId : outgoingEdges.get(currentNodeId)) {
				if (visitedEdges.add(edgeId)) {
					for (String targetNodeId : incidentNodes.get(edgeId)) {
						if (!currentNodeId.equals(targetNodeId) && !visitedNodes.containsKey(targetNodeId)) {
							visitedNodes.put(targetNodeId, targetNodeDistance);
							nodeQueue.addLast(targetNodeId);
							distanceSum += targetNodeDistance;
						}
					}
				}
			}
		}

		return 1.0 / (distanceSum + (numberOfNodes - visitedNodes.size()) * numberOfNodes);
	}

	private double computeWithEdgeWeights(String nodeId) {
		Map<String, Double> nextNodes = new HashMap<>();
		Map<String, Double> nodeDistances = new HashMap<>(numberOfNodes, 1.0f);
		Set<String> visitedEdges = new HashSet<>(numberOfEdges, 1.0f);

		nextNodes.put(nodeId, 0.0);

		while (!nextNodes.isEmpty()) {
			Map.Entry<String, Double> currentNode = Collections.min(nextNodes.entrySet(),
					(e1, e2) -> e1.getValue().compareTo(e2.getValue()));
			String currentNodeId = currentNode.getKey();
			double currentNodeDistance = currentNode.getValue();

			nextNodes.remove(currentNodeId);
			nodeDistances.put(currentNodeId, currentNodeDistance);

			for (String edgeId : outgoingEdges.get(currentNodeId)) {
				if (visitedEdges.add(edgeId)) {
					for (String targetNodeId : incidentNodes.get(edgeId)) {
						if (!currentNodeId.equals(targetNodeId) && !nodeDistances.containsKey(targetNodeId)) {
							double newDistance = currentNodeDistance + edgeWeights.get(edgeId);
							Double oldDistance = nextNodes.get(targetNodeId);

							if (oldDistance == null || newDistance < oldDistance) {
								nextNodes.put(targetNodeId, newDistance);
							}
						}
					}
				}
			}
		}

		return 1.0 / (nodeDistances.values().stream().collect(Collectors.summingDouble(Double::doubleValue))
				+ (numberOfNodes - nodeDistances.size()) * numberOfNodes);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getToolTip() {
		return NAME;
	}

	@Override
	public boolean getDefaultSelected() {
		return false;
	}

	@Override
	public Analyzer<PersistentObject> getAnalyzer() {
		return this;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public GraphObjectType getTargetType() {
		return GraphObjectType.NODE;
	}

	@Override
	public Analyzer<PersistentObject> createInstance() {
		return this;
	}

	@Override
	public String[] getColumnNames() {
		return new String[] { ID };
	}

	@Override
	public DataType[] getDataTypes() {
		return new DataType[] { DoubleCell.TYPE };
	}

	@Override
	public AnalyzerType<PersistentObject> getType() {
		return this;
	}
}
