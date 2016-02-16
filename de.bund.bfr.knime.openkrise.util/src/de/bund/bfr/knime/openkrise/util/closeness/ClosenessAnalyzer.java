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
package de.bund.bfr.knime.openkrise.util.closeness;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.network.core.analyzer.Analyzer;
import org.knime.network.core.analyzer.AnalyzerType;
import org.knime.network.core.analyzer.NumericAnalyzer;
import org.knime.network.core.api.GraphObjectIterator;
import org.knime.network.core.api.KPartiteGraphView;
import org.knime.network.core.api.Partition;
import org.knime.network.core.api.PersistentObject;
import org.knime.network.core.core.exception.PersistenceException;

public class ClosenessAnalyzer extends NumericAnalyzer<PersistentObject> {

	private static final ClosenessAnalyzerType TYPE = new ClosenessAnalyzerType();
	private static final String[] COLUMN_NAMES = new String[] { "Closeness" };
	private static final DataType[] COLUMN_TYPES = new DataType[] { DoubleCell.TYPE };

	private int numberOfNodes;
	private int numberOfEdges;
	private Map<PersistentObject, Collection<PersistentObject>> incidentNodes;
	private Map<PersistentObject, Collection<PersistentObject>> outgoingEdges;

	protected ClosenessAnalyzer() {
		super(new String[] { "Closeness" });
		numberOfNodes = 0;
		numberOfEdges = 0;
		incidentNodes = new HashMap<>();
		outgoingEdges = new HashMap<>();
	}

	@Override
	protected void initializeInternal(KPartiteGraphView<PersistentObject, Partition> view, ExecutionMonitor exec)
			throws PersistenceException, CanceledExecutionException {
		super.initializeInternal(view, exec);
		numberOfNodes = (int) view.getNoOfNodes();
		numberOfEdges = (int) view.getNoOfEdges();
		incidentNodes.clear();
		outgoingEdges.clear();

		for (PersistentObject edge : view.getEdges()) {
			incidentNodes.put(edge, view.getIncidentNodes(edge));
		}

		for (PersistentObject node : view.getNodes()) {
			outgoingEdges.put(node, view.getOutgoingEdges(node));
		}
	}

	@Override
	protected double[] numericAnalyzeInternal(ExecutionMonitor exec,
			KPartiteGraphView<PersistentObject, Partition> view, PersistentObject object)
					throws PersistenceException, CanceledExecutionException {
		Deque<PersistentObject> nodeQueue = new LinkedList<>();
		Map<String, Integer> visitedNodes = new HashMap<>(numberOfNodes, 1.0f);
		Set<String> visitedEdges = new HashSet<>(numberOfEdges, 1.0f);
		int distanceSum = 0;

		visitedNodes.put(object.getId(), 0);
		nodeQueue.addFirst(object);

		while (!nodeQueue.isEmpty()) {
			PersistentObject currentNode = nodeQueue.removeLast();
			String currentNodeId = currentNode.getId();
			int distance = visitedNodes.get(currentNodeId) + 1;

			for (PersistentObject edge : outgoingEdges.get(currentNode)) {
				if (visitedEdges.add(edge.getId())) {
					for (PersistentObject targetNode : incidentNodes.get(edge)) {
						String targetNodeId = targetNode.getId();

						if (!currentNodeId.equals(targetNodeId) && !visitedNodes.containsKey(targetNodeId)) {
							visitedNodes.put(targetNodeId, distance);
							nodeQueue.addFirst(targetNode);
							distanceSum += distance;
						}
					}
				}
			}
		}

		int denom = distanceSum + (numberOfNodes - visitedNodes.size()) * numberOfNodes;

		return new double[] { 1.0 / denom };
	}

	@Override
	public Analyzer<PersistentObject> createInstance() {
		return this;
	}

	@Override
	public String[] getColumnNames() {
		return COLUMN_NAMES;
	}

	@Override
	public DataType[] getDataTypes() {
		return COLUMN_TYPES;
	}

	@Override
	public AnalyzerType<PersistentObject> getType() {
		return TYPE;
	}

	@Override
	protected GraphObjectIterator<PersistentObject> getGraphObjectIterator(
			KPartiteGraphView<PersistentObject, Partition> view) throws PersistenceException {
		return view.getNodes();
	}
}
