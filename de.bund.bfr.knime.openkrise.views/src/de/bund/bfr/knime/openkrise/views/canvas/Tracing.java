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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.util.LinkedHashMap;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.openkrise.TracingColumns;

public class Tracing<V extends Node> {

	private Map<String, V> nodeSaveMap;
	private Map<String, Edge<V>> edgeSaveMap;

	public Tracing(Map<String, V> nodeSaveMap, Map<String, Edge<V>> edgeSaveMap) {
		this.nodeSaveMap = nodeSaveMap;
		this.edgeSaveMap = edgeSaveMap;
	}

	public Map<String, Double> getNodeWeights() {
		Map<String, Double> nodeWeights = new LinkedHashMap<>();

		for (V node : nodeSaveMap.values()) {
			nodeWeights.put(node.getId(),
					(Double) node.getProperties().get(TracingColumns.WEIGHT));
		}

		return nodeWeights;
	}

	public void setNodeWeights(Map<String, Double> nodeWeights) {
		for (V node : nodeSaveMap.values()) {
			if (nodeWeights.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.WEIGHT,
						nullToZero(nodeWeights.get(node.getId())));
			}
		}
	}

	public Map<String, Double> getEdgeWeights() {
		Map<String, Double> edgeWeights = new LinkedHashMap<>();

		for (Edge<V> edge : edgeSaveMap.values()) {
			edgeWeights.put(edge.getId(),
					(Double) edge.getProperties().get(TracingColumns.WEIGHT));
		}

		return edgeWeights;
	}

	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		for (Edge<V> edge : edgeSaveMap.values()) {
			if (edgeWeights.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.WEIGHT,
						nullToZero(edgeWeights.get(edge.getId())));
			}
		}
	}

	public Map<String, Boolean> getNodeCrossContaminations() {
		Map<String, Boolean> nodeCrossContaminations = new LinkedHashMap<>();

		for (V node : nodeSaveMap.values()) {
			nodeCrossContaminations.put(node.getId(), (Boolean) node
					.getProperties().get(TracingColumns.CROSS_CONTAMINATION));
		}

		return nodeCrossContaminations;
	}

	public void setNodeCrossContaminations(
			Map<String, Boolean> nodeCrossContaminations) {
		for (V node : nodeSaveMap.values()) {
			if (nodeCrossContaminations.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.CROSS_CONTAMINATION,
						nullToFalse(nodeCrossContaminations.get(node.getId())));
			}
		}
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		Map<String, Boolean> edgeCrossContaminations = new LinkedHashMap<>();

		for (Edge<V> edge : edgeSaveMap.values()) {
			edgeCrossContaminations.put(edge.getId(), (Boolean) edge
					.getProperties().get(TracingColumns.CROSS_CONTAMINATION));
		}

		return edgeCrossContaminations;
	}

	public void setEdgeCrossContaminations(
			Map<String, Boolean> edgeCrossContaminations) {
		for (Edge<V> edge : edgeSaveMap.values()) {
			if (edgeCrossContaminations.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.CROSS_CONTAMINATION,
						nullToFalse(edgeCrossContaminations.get(edge.getId())));
			}
		}
	}

	public Map<String, Boolean> getObservedNodes() {
		Map<String, Boolean> observedNodes = new LinkedHashMap<>();

		for (V node : nodeSaveMap.values()) {
			observedNodes
					.put(node.getId(),
							(Boolean) node.getProperties().get(
									TracingColumns.OBSERVED));
		}

		return observedNodes;
	}

	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		for (V node : nodeSaveMap.values()) {
			if (observedNodes.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.OBSERVED,
						nullToFalse(observedNodes.get(node.getId())));
			}
		}
	}

	public Map<String, Boolean> getObservedEdges() {
		Map<String, Boolean> observedEdges = new LinkedHashMap<>();

		for (Edge<V> edge : edgeSaveMap.values()) {
			observedEdges
					.put(edge.getId(),
							(Boolean) edge.getProperties().get(
									TracingColumns.OBSERVED));
		}

		return observedEdges;
	}

	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		for (Edge<V> edge : edgeSaveMap.values()) {
			if (observedEdges.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.OBSERVED,
						nullToFalse(observedEdges.get(edge.getId())));
			}
		}
	}

	private static double nullToZero(Double value) {
		return value == null ? 0.0 : value;
	}

	private static boolean nullToFalse(Boolean value) {
		return value == null ? false : value;
	}
}
