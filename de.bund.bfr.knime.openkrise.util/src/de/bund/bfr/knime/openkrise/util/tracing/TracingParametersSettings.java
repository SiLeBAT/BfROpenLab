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

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.openkrise.util.Activator;

public class TracingParametersSettings extends NodeSettings {

	private static final XmlConverter SERIALIZER = new XmlConverter(
			Activator.class.getClassLoader());

	private static final String CFG_NODE_WEIGHTS = "CaseWeights";
	private static final String CFG_EDGE_WEIGHTS = "EdgeWeights";
	private static final String CFG_NODE_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_EDGE_CROSS_CONTAMINATIONS = "EdgeCrossContaminations";
	private static final String CFG_NODE_FILTER = "Filter";
	private static final String CFG_EDGE_FILTER = "EdgeFilter";
	private static final String CFG_NODE_WEIGHT_CONDITION = "WeightCondition";
	private static final String CFG_EDGE_WEIGHT_CONDITION = "EdgeWeightCondition";
	private static final String CFG_NODE_CONTAMINATION_CONDITION = "ContaminationCondition";
	private static final String CFG_EDGE_CONTAMINATION_CONDITION = "EdgeContaminationCondition";
	private static final String CFG_NODE_FILTER_CONDITION = "FilterCondition";
	private static final String CFG_EDGE_FILTER_CONDITION = "EdgeFilterCondition";
	private static final String CFG_NODE_WEIGHT_CONDITION_VALUE = "WeightConditionValue";
	private static final String CFG_EDGE_WEIGHT_CONDITION_VALUE = "EdgeWeightConditionValue";
	private static final String CFG_NODE_CONTAMINATION_CONDITION_VALUE = "ContaminationConditionValue";
	private static final String CFG_EDGE_CONTAMINATION_CONDITION_VALUE = "EdgeContaminationConditionValue";
	private static final String CFG_NODE_FILTER_CONDITION_VALUE = "FilterConditionValue";
	private static final String CFG_EDGE_FILTER_CONDITION_VALUE = "EdgeFilterConditionValue";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";

	private static final boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;

	private Map<String, Double> nodeWeights;
	private Map<String, Double> edgeWeights;
	private Map<String, Boolean> nodeCrossContaminations;
	private Map<String, Boolean> edgeCrossContaminations;
	private Map<String, Boolean> nodeFilter;
	private Map<String, Boolean> edgeFilter;
	private AndOrHighlightCondition nodeWeightCondition;
	private AndOrHighlightCondition edgeWeightCondition;
	private AndOrHighlightCondition nodeContaminationCondition;
	private AndOrHighlightCondition edgeContaminationCondition;
	private AndOrHighlightCondition nodeFilterCondition;
	private AndOrHighlightCondition edgeFilterCondition;
	private Double nodeWeightConditionValue;
	private Double edgeWeightConditionValue;
	private Boolean nodeContaminationConditionValue;
	private Boolean edgeContaminationConditionValue;
	private Boolean nodeFilterConditionValue;
	private Boolean edgeFilterConditionValue;
	private boolean enforeTemporalOrder;

	public TracingParametersSettings() {
		nodeWeights = new LinkedHashMap<>();
		edgeWeights = new LinkedHashMap<>();
		nodeCrossContaminations = new LinkedHashMap<>();
		edgeCrossContaminations = new LinkedHashMap<>();
		nodeFilter = new LinkedHashMap<>();
		edgeFilter = new LinkedHashMap<>();
		nodeWeightCondition = null;
		edgeWeightCondition = null;
		nodeContaminationCondition = null;
		edgeContaminationCondition = null;
		nodeFilterCondition = null;
		edgeFilterCondition = null;
		nodeWeightConditionValue = Double.NaN;
		edgeWeightConditionValue = Double.NaN;
		nodeContaminationConditionValue = false;
		edgeContaminationConditionValue = false;
		nodeFilterConditionValue = false;
		edgeFilterConditionValue = false;
		enforeTemporalOrder = DEFAULT_ENFORCE_TEMPORAL_ORDER;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			nodeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_NODE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_EDGE_WEIGHTS));
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
			nodeFilter = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_NODE_FILTER));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFilter = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_EDGE_FILTER));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeWeightCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_WEIGHT_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeightCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_WEIGHT_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeContaminationCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings
							.getString(CFG_NODE_CONTAMINATION_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeContaminationCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings
							.getString(CFG_EDGE_CONTAMINATION_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeFilterCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_FILTER_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFilterCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_FILTER_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeWeightConditionValue = nanToNull(settings
					.getDouble(CFG_NODE_WEIGHT_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeightConditionValue = nanToNull(settings
					.getDouble(CFG_EDGE_WEIGHT_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeContaminationConditionValue = falseToNull(settings
					.getBoolean(CFG_NODE_CONTAMINATION_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeContaminationConditionValue = falseToNull(settings
					.getBoolean(CFG_EDGE_CONTAMINATION_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeFilterConditionValue = falseToNull(settings
					.getBoolean(CFG_NODE_FILTER_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFilterConditionValue = falseToNull(settings
					.getBoolean(CFG_EDGE_FILTER_CONDITION_VALUE));
		} catch (InvalidSettingsException e) {
		}

		try {
			enforeTemporalOrder = settings
					.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_NODE_WEIGHTS, SERIALIZER.toXml(nodeWeights));
		settings.addString(CFG_EDGE_WEIGHTS, SERIALIZER.toXml(edgeWeights));
		settings.addString(CFG_NODE_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(nodeCrossContaminations));
		settings.addString(CFG_EDGE_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(edgeCrossContaminations));
		settings.addString(CFG_NODE_FILTER, SERIALIZER.toXml(nodeFilter));
		settings.addString(CFG_EDGE_FILTER, SERIALIZER.toXml(edgeFilter));
		settings.addString(CFG_NODE_WEIGHT_CONDITION,
				SERIALIZER.toXml(nodeWeightCondition));
		settings.addString(CFG_EDGE_WEIGHT_CONDITION,
				SERIALIZER.toXml(edgeWeightCondition));
		settings.addString(CFG_NODE_CONTAMINATION_CONDITION,
				SERIALIZER.toXml(nodeContaminationCondition));
		settings.addString(CFG_EDGE_CONTAMINATION_CONDITION,
				SERIALIZER.toXml(edgeContaminationCondition));
		settings.addString(CFG_NODE_FILTER_CONDITION,
				SERIALIZER.toXml(nodeFilterCondition));
		settings.addString(CFG_EDGE_FILTER_CONDITION,
				SERIALIZER.toXml(edgeFilterCondition));
		settings.addDouble(CFG_NODE_WEIGHT_CONDITION_VALUE,
				nullToNan(nodeWeightConditionValue));
		settings.addDouble(CFG_EDGE_WEIGHT_CONDITION_VALUE,
				nullToNan(edgeWeightConditionValue));
		settings.addBoolean(CFG_NODE_CONTAMINATION_CONDITION_VALUE,
				nullToFalse(nodeContaminationConditionValue));
		settings.addBoolean(CFG_EDGE_CONTAMINATION_CONDITION_VALUE,
				nullToFalse(edgeContaminationConditionValue));
		settings.addBoolean(CFG_NODE_FILTER_CONDITION_VALUE,
				nullToFalse(nodeFilterConditionValue));
		settings.addBoolean(CFG_EDGE_FILTER_CONDITION_VALUE,
				nullToFalse(edgeFilterConditionValue));
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

	public void setNodeCrossContaminations(
			Map<String, Boolean> nodeCrossContaminations) {
		this.nodeCrossContaminations = nodeCrossContaminations;
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		return edgeCrossContaminations;
	}

	public void setEdgeCrossContaminations(
			Map<String, Boolean> edgeCrossContaminations) {
		this.edgeCrossContaminations = edgeCrossContaminations;
	}

	public Map<String, Boolean> getNodeFilter() {
		return nodeFilter;
	}

	public void setNodeFilter(Map<String, Boolean> nodeFilter) {
		this.nodeFilter = nodeFilter;
	}

	public Map<String, Boolean> getEdgeFilter() {
		return edgeFilter;
	}

	public void setEdgeFilter(Map<String, Boolean> edgeFilter) {
		this.edgeFilter = edgeFilter;
	}

	public AndOrHighlightCondition getNodeWeightCondition() {
		return nodeWeightCondition;
	}

	public void setNodeWeightCondition(
			AndOrHighlightCondition nodeWeightCondition) {
		this.nodeWeightCondition = nodeWeightCondition;
	}

	public AndOrHighlightCondition getEdgeWeightCondition() {
		return edgeWeightCondition;
	}

	public void setEdgeWeightCondition(
			AndOrHighlightCondition edgeWeightCondition) {
		this.edgeWeightCondition = edgeWeightCondition;
	}

	public AndOrHighlightCondition getNodeContaminationCondition() {
		return nodeContaminationCondition;
	}

	public void setNodeContaminationCondition(
			AndOrHighlightCondition nodeContaminationCondition) {
		this.nodeContaminationCondition = nodeContaminationCondition;
	}

	public AndOrHighlightCondition getEdgeContaminationCondition() {
		return edgeContaminationCondition;
	}

	public void setEdgeContaminationCondition(
			AndOrHighlightCondition edgeContaminationCondition) {
		this.edgeContaminationCondition = edgeContaminationCondition;
	}

	public AndOrHighlightCondition getNodeFilterCondition() {
		return nodeFilterCondition;
	}

	public void setNodeFilterCondition(
			AndOrHighlightCondition nodeFilterCondition) {
		this.nodeFilterCondition = nodeFilterCondition;
	}

	public AndOrHighlightCondition getEdgeFilterCondition() {
		return edgeFilterCondition;
	}

	public void setEdgeFilterCondition(
			AndOrHighlightCondition edgeFilterCondition) {
		this.edgeFilterCondition = edgeFilterCondition;
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

	public void setNodeContaminationConditionValue(
			Boolean nodeContaminationConditionValue) {
		this.nodeContaminationConditionValue = nodeContaminationConditionValue;
	}

	public Boolean getEdgeContaminationConditionValue() {
		return edgeContaminationConditionValue;
	}

	public void setEdgeContaminationConditionValue(
			Boolean edgeContaminationConditionValue) {
		this.edgeContaminationConditionValue = edgeContaminationConditionValue;
	}

	public Boolean getNodeFilterConditionValue() {
		return nodeFilterConditionValue;
	}

	public void setNodeFilterConditionValue(Boolean nodeFilterConditionValue) {
		this.nodeFilterConditionValue = nodeFilterConditionValue;
	}

	public Boolean getEdgeFilterConditionValue() {
		return edgeFilterConditionValue;
	}

	public void setEdgeFilterConditionValue(Boolean edgeFilterConditionValue) {
		this.edgeFilterConditionValue = edgeFilterConditionValue;
	}

	public boolean isEnforeTemporalOrder() {
		return enforeTemporalOrder;
	}

	public void setEnforeTemporalOrder(boolean enforeTemporalOrder) {
		this.enforeTemporalOrder = enforeTemporalOrder;
	}

	private static boolean nullToFalse(Boolean b) {
		return b != null ? b : false;
	}

	private static Boolean falseToNull(boolean b) {
		return b ? b : null;
	}

	private static double nullToNan(Double d) {
		return d != null ? d : Double.NaN;
	}

	private static Double nanToNull(double d) {
		return !Double.isNaN(d) ? d : null;
	}
}
