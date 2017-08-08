/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.BackwardUtils;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.openkrise.util.Activator;

public class TracingParametersSettings extends NodeSettings {

	private static final XmlConverter SERIALIZER = new XmlConverter(Activator.class.getClassLoader());

	private static final String CFG_NODE_WEIGHTS = "CaseWeights";
	private static final String CFG_EDGE_WEIGHTS = "EdgeWeights";
	private static final String CFG_NODE_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_EDGE_CROSS_CONTAMINATIONS = "EdgeCrossContaminations";
	private static final String CFG_NODE_KILL_CONTAMINATIONS = "NodeKillContaminations";
	private static final String CFG_EDGE_KILL_CONTAMINATIONS = "EdgeKillContaminations";
	private static final String CFG_OBSERVED_NODES = "Filter";
	private static final String CFG_OBSERVED_EDGES = "EdgeFilter";
	private static final String CFG_NODE_WEIGHT_CONDITION = "WeightCondition";
	private static final String CFG_EDGE_WEIGHT_CONDITION = "EdgeWeightCondition";
	private static final String CFG_NODE_CONTAMINATION_CONDITION = "ContaminationCondition";
	private static final String CFG_EDGE_CONTAMINATION_CONDITION = "EdgeContaminationCondition";
	private static final String CFG_NODE_KILL_CONDITION = "NodeKillCondition";
	private static final String CFG_EDGE_KILL_CONDITION = "EdgeKillCondition";
	private static final String CFG_OBSERVED_NODES_CONDITION = "FilterCondition";
	private static final String CFG_OBSERVED_EDGES_CONDITION = "EdgeFilterCondition";
	private static final String CFG_NODE_WEIGHT_CONDITION_VALUE = "WeightConditionValue";
	private static final String CFG_EDGE_WEIGHT_CONDITION_VALUE = "EdgeWeightConditionValue";
	private static final String CFG_NODE_CONTAMINATION_CONDITION_VALUE = "ContaminationConditionValue";
	private static final String CFG_EDGE_CONTAMINATION_CONDITION_VALUE = "EdgeContaminationConditionValue";
	private static final String CFG_NODE_KILL_CONDITION_VALUE = "NodeKillConditionValue";
	private static final String CFG_EDGE_KILL_CONDITION_VALUE = "EdgeKillConditionValue";
	private static final String CFG_OBSERVED_NODES_CONDITION_VALUE = "FilterConditionValue";
	private static final String CFG_OBSERVED_EDGES_CONDITION_VALUE = "EdgeFilterConditionValue";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";

	private Map<String, Double> nodeWeights;
	private Map<String, Double> edgeWeights;
	private Map<String, Boolean> nodeCrossContaminations;
	private Map<String, Boolean> edgeCrossContaminations;
	private Map<String, Boolean> nodeKillContaminations;
	private Map<String, Boolean> edgeKillContaminations;
	private Map<String, Boolean> observedNodes;
	private Map<String, Boolean> observedEdges;
	private AndOrHighlightCondition nodeWeightCondition;
	private AndOrHighlightCondition edgeWeightCondition;
	private AndOrHighlightCondition nodeContaminationCondition;
	private AndOrHighlightCondition edgeContaminationCondition;
	private AndOrHighlightCondition nodeKillCondition;
	private AndOrHighlightCondition edgeKillCondition;
	private AndOrHighlightCondition observedNodesCondition;
	private AndOrHighlightCondition observedEdgesCondition;
	private Double nodeWeightConditionValue;
	private Double edgeWeightConditionValue;
	private Boolean nodeContaminationConditionValue;
	private Boolean edgeContaminationConditionValue;
	private Boolean nodeKillConditionValue;
	private Boolean edgeKillConditionValue;
	private Boolean observedNodesConditionValue;
	private Boolean observedEdgesConditionValue;
	private boolean enforeTemporalOrder;

	public TracingParametersSettings() {
		nodeWeights = new LinkedHashMap<>();
		edgeWeights = new LinkedHashMap<>();
		nodeCrossContaminations = new LinkedHashMap<>();
		edgeCrossContaminations = new LinkedHashMap<>();
		nodeKillContaminations = new LinkedHashMap<>();
		edgeKillContaminations = new LinkedHashMap<>();
		observedNodes = new LinkedHashMap<>();
		observedEdges = new LinkedHashMap<>();
		nodeWeightCondition = null;
		edgeWeightCondition = null;
		nodeContaminationCondition = null;
		edgeContaminationCondition = null;
		nodeKillCondition = null;
		edgeKillCondition = null;
		observedNodesCondition = null;
		observedEdgesCondition = null;
		nodeWeightConditionValue = null;
		edgeWeightConditionValue = null;
		nodeContaminationConditionValue = null;
		edgeContaminationConditionValue = null;
		nodeKillConditionValue = null;
		edgeKillConditionValue = null;
		observedNodesConditionValue = null;
		observedEdgesConditionValue = null;
		enforeTemporalOrder = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			nodeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_NODE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_EDGE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeCrossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeCrossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeKillContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_KILL_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeKillContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_KILL_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedNodes = (Map<String, Boolean>) SERIALIZER.fromXml(settings.getString(CFG_OBSERVED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedEdges = (Map<String, Boolean>) SERIALIZER.fromXml(settings.getString(CFG_OBSERVED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeWeightCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_NODE_WEIGHT_CONDITION)));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeightCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_EDGE_WEIGHT_CONDITION)));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeContaminationCondition = (AndOrHighlightCondition) SERIALIZER.fromXml(
					BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_NODE_CONTAMINATION_CONDITION)));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeContaminationCondition = (AndOrHighlightCondition) SERIALIZER.fromXml(
					BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_EDGE_CONTAMINATION_CONDITION)));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeKillCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_NODE_KILL_CONDITION)));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeKillCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_EDGE_KILL_CONDITION)));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedNodesCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_OBSERVED_NODES_CONDITION)));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedEdgesCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_OBSERVED_EDGES_CONDITION)));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeWeightConditionValue = nanToNull(settings.getDouble(CFG_NODE_WEIGHT_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeightConditionValue = nanToNull(settings.getDouble(CFG_EDGE_WEIGHT_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeContaminationConditionValue = falseToNull(settings.getBoolean(CFG_NODE_CONTAMINATION_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeContaminationConditionValue = falseToNull(settings.getBoolean(CFG_EDGE_CONTAMINATION_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeKillConditionValue = falseToNull(settings.getBoolean(CFG_NODE_KILL_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeKillConditionValue = falseToNull(settings.getBoolean(CFG_EDGE_KILL_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedNodesConditionValue = falseToNull(settings.getBoolean(CFG_OBSERVED_NODES_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedEdgesConditionValue = falseToNull(settings.getBoolean(CFG_OBSERVED_EDGES_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			enforeTemporalOrder = settings.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_NODE_WEIGHTS, SERIALIZER.toXml(nodeWeights));
		settings.addString(CFG_EDGE_WEIGHTS, SERIALIZER.toXml(edgeWeights));
		settings.addString(CFG_NODE_CROSS_CONTAMINATIONS, SERIALIZER.toXml(nodeCrossContaminations));
		settings.addString(CFG_EDGE_CROSS_CONTAMINATIONS, SERIALIZER.toXml(edgeCrossContaminations));
		settings.addString(CFG_NODE_KILL_CONTAMINATIONS, SERIALIZER.toXml(nodeKillContaminations));
		settings.addString(CFG_EDGE_KILL_CONTAMINATIONS, SERIALIZER.toXml(edgeKillContaminations));
		settings.addString(CFG_OBSERVED_NODES, SERIALIZER.toXml(observedNodes));
		settings.addString(CFG_OBSERVED_EDGES, SERIALIZER.toXml(observedEdges));
		settings.addString(CFG_NODE_WEIGHT_CONDITION, SERIALIZER.toXml(nodeWeightCondition));
		settings.addString(CFG_EDGE_WEIGHT_CONDITION, SERIALIZER.toXml(edgeWeightCondition));
		settings.addString(CFG_NODE_CONTAMINATION_CONDITION, SERIALIZER.toXml(nodeContaminationCondition));
		settings.addString(CFG_EDGE_CONTAMINATION_CONDITION, SERIALIZER.toXml(edgeContaminationCondition));
		settings.addString(CFG_NODE_KILL_CONDITION, SERIALIZER.toXml(nodeKillCondition));
		settings.addString(CFG_EDGE_KILL_CONDITION, SERIALIZER.toXml(edgeKillCondition));
		settings.addString(CFG_OBSERVED_NODES_CONDITION, SERIALIZER.toXml(observedNodesCondition));
		settings.addString(CFG_OBSERVED_EDGES_CONDITION, SERIALIZER.toXml(observedEdgesCondition));
		settings.addDouble(CFG_NODE_WEIGHT_CONDITION_VALUE, nullToNan(nodeWeightConditionValue));
		settings.addDouble(CFG_EDGE_WEIGHT_CONDITION_VALUE, nullToNan(edgeWeightConditionValue));
		settings.addBoolean(CFG_NODE_CONTAMINATION_CONDITION_VALUE, nullToFalse(nodeContaminationConditionValue));
		settings.addBoolean(CFG_EDGE_CONTAMINATION_CONDITION_VALUE, nullToFalse(edgeContaminationConditionValue));
		settings.addBoolean(CFG_NODE_KILL_CONDITION_VALUE, nullToFalse(nodeKillConditionValue));
		settings.addBoolean(CFG_EDGE_KILL_CONDITION_VALUE, nullToFalse(edgeKillConditionValue));
		settings.addBoolean(CFG_OBSERVED_NODES_CONDITION_VALUE, nullToFalse(observedNodesConditionValue));
		settings.addBoolean(CFG_OBSERVED_EDGES_CONDITION_VALUE, nullToFalse(observedEdgesConditionValue));
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);
	}

	public Map<String, Double> getNodeWeights() {
		return nodeWeights;
	}

	public void setNodeWeights(Map<String, Double> nodeWeights) {
		this.nodeWeights = nodeWeights;
	}

	public Map<String, Double> getEdgeWeights() {
		return edgeWeights;
	}

	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		this.edgeWeights = edgeWeights;
	}

	public Map<String, Boolean> getNodeCrossContaminations() {
		return nodeCrossContaminations;
	}

	public void setNodeCrossContaminations(Map<String, Boolean> nodeCrossContaminations) {
		this.nodeCrossContaminations = nodeCrossContaminations;
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		return edgeCrossContaminations;
	}

	public void setEdgeCrossContaminations(Map<String, Boolean> edgeCrossContaminations) {
		this.edgeCrossContaminations = edgeCrossContaminations;
	}

	public Map<String, Boolean> getNodeKillContaminations() {
		return nodeKillContaminations;
	}

	public void setNodeKillContaminations(Map<String, Boolean> nodeKillContaminations) {
		this.nodeKillContaminations = nodeKillContaminations;
	}

	public Map<String, Boolean> getEdgeKillContaminations() {
		return edgeKillContaminations;
	}

	public void setEdgeKillContaminations(Map<String, Boolean> edgeKillContaminations) {
		this.edgeKillContaminations = edgeKillContaminations;
	}

	public Map<String, Boolean> getObservedNodes() {
		return observedNodes;
	}

	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		this.observedNodes = observedNodes;
	}

	public Map<String, Boolean> getObservedEdges() {
		return observedEdges;
	}

	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		this.observedEdges = observedEdges;
	}

	public AndOrHighlightCondition getNodeWeightCondition() {
		return nodeWeightCondition;
	}

	public void setNodeWeightCondition(AndOrHighlightCondition nodeWeightCondition) {
		this.nodeWeightCondition = nodeWeightCondition;
	}

	public AndOrHighlightCondition getEdgeWeightCondition() {
		return edgeWeightCondition;
	}

	public void setEdgeWeightCondition(AndOrHighlightCondition edgeWeightCondition) {
		this.edgeWeightCondition = edgeWeightCondition;
	}

	public AndOrHighlightCondition getNodeContaminationCondition() {
		return nodeContaminationCondition;
	}

	public void setNodeContaminationCondition(AndOrHighlightCondition nodeContaminationCondition) {
		this.nodeContaminationCondition = nodeContaminationCondition;
	}

	public AndOrHighlightCondition getEdgeContaminationCondition() {
		return edgeContaminationCondition;
	}

	public void setEdgeContaminationCondition(AndOrHighlightCondition edgeContaminationCondition) {
		this.edgeContaminationCondition = edgeContaminationCondition;
	}

	public AndOrHighlightCondition getNodeKillCondition() {
		return nodeKillCondition;
	}

	public void setNodeKillCondition(AndOrHighlightCondition nodeKillCondition) {
		this.nodeKillCondition = nodeKillCondition;
	}

	public AndOrHighlightCondition getEdgeKillCondition() {
		return edgeKillCondition;
	}

	public void setEdgeKillCondition(AndOrHighlightCondition edgeKillCondition) {
		this.edgeKillCondition = edgeKillCondition;
	}

	public AndOrHighlightCondition getObservedNodesCondition() {
		return observedNodesCondition;
	}

	public void setObservedNodesCondition(AndOrHighlightCondition observedNodesCondition) {
		this.observedNodesCondition = observedNodesCondition;
	}

	public AndOrHighlightCondition getObservedEdgesCondition() {
		return observedEdgesCondition;
	}

	public void setObservedEdgesCondition(AndOrHighlightCondition observedEdgesCondition) {
		this.observedEdgesCondition = observedEdgesCondition;
	}

	public Double getNodeWeightConditionValue() {
		return nodeWeightConditionValue;
	}

	public void setNodeWeightConditionValue(Double nodeWeightConditionValue) {
		this.nodeWeightConditionValue = nodeWeightConditionValue;
	}

	public Double getEdgeWeightConditionValue() {
		return edgeWeightConditionValue;
	}

	public void setEdgeWeightConditionValue(Double edgeWeightConditionValue) {
		this.edgeWeightConditionValue = edgeWeightConditionValue;
	}

	public Boolean getNodeContaminationConditionValue() {
		return nodeContaminationConditionValue;
	}

	public void setNodeContaminationConditionValue(Boolean nodeContaminationConditionValue) {
		this.nodeContaminationConditionValue = nodeContaminationConditionValue;
	}

	public Boolean getEdgeContaminationConditionValue() {
		return edgeContaminationConditionValue;
	}

	public void setEdgeContaminationConditionValue(Boolean edgeContaminationConditionValue) {
		this.edgeContaminationConditionValue = edgeContaminationConditionValue;
	}

	public Boolean getNodeKillConditionValue() {
		return nodeKillConditionValue;
	}

	public void setNodeKillConditionValue(Boolean nodeKillConditionValue) {
		this.nodeKillConditionValue = nodeKillConditionValue;
	}

	public Boolean getEdgeKillConditionValue() {
		return edgeKillConditionValue;
	}

	public void setEdgeKillConditionValue(Boolean edgeKillConditionValue) {
		this.edgeKillConditionValue = edgeKillConditionValue;
	}

	public Boolean getObservedNodesConditionValue() {
		return observedNodesConditionValue;
	}

	public void setObservedNodesConditionValue(Boolean observedNodesConditionValue) {
		this.observedNodesConditionValue = observedNodesConditionValue;
	}

	public Boolean getObservedEdgesConditionValue() {
		return observedEdgesConditionValue;
	}

	public void setObservedEdgesConditionValue(Boolean observedEdgesConditionValue) {
		this.observedEdgesConditionValue = observedEdgesConditionValue;
	}

	public boolean isEnforeTemporalOrder() {
		return enforeTemporalOrder;
	}

	public void setEnforeTemporalOrder(boolean enforeTemporalOrder) {
		this.enforeTemporalOrder = enforeTemporalOrder;
	}
}
