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

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.openkrise.views.GraphSettings;
import de.bund.bfr.knime.openkrise.views.ViewSettings;

public class TracingViewSettings extends ViewSettings {

	private static final String CFG_NODE_WEIGHTS = "CaseWeights";
	private static final String CFG_EDGE_WEIGHTS = "EdgeWeights";
	private static final String CFG_NODE_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_EDGE_CROSS_CONTAMINATIONS = "EdgeCrossContaminations";
	private static final String CFG_NODE_FILTER = "Filter";
	private static final String CFG_EDGE_FILTER = "EdgeFilter";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";
	private static final String CFG_SHOW_FORWARD = "ShowConnected";
	private static final String CFG_LABEL = "Label";

	private static final boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;
	private static final boolean DEFAULT_SHOW_FORWARD = false;

	private Map<String, Double> nodeWeights;
	private Map<String, Double> edgeWeights;
	private Map<String, Boolean> nodeCrossContaminations;
	private Map<String, Boolean> edgeCrossContaminations;
	private Map<String, Boolean> nodeFilter;
	private Map<String, Boolean> edgeFilter;
	private boolean enforeTemporalOrder;
	private boolean showForward;
	private String label;

	private GraphSettings graphSettings;

	public TracingViewSettings() {
		nodeWeights = new LinkedHashMap<>();
		edgeWeights = new LinkedHashMap<>();
		nodeCrossContaminations = new LinkedHashMap<>();
		edgeCrossContaminations = new LinkedHashMap<>();
		nodeFilter = new LinkedHashMap<>();
		edgeFilter = new LinkedHashMap<>();
		enforeTemporalOrder = DEFAULT_ENFORCE_TEMPORAL_ORDER;
		showForward = DEFAULT_SHOW_FORWARD;
		label = null;

		graphSettings = new GraphSettings();
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
			enforeTemporalOrder = settings
					.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}

		try {
			showForward = settings.getBoolean(CFG_SHOW_FORWARD);
		} catch (InvalidSettingsException e) {
		}

		try {
			label = settings.getString(CFG_LABEL);
		} catch (InvalidSettingsException e) {
		}

		graphSettings.loadSettings(settings);
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
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);
		settings.addBoolean(CFG_SHOW_FORWARD, showForward);
		settings.addString(CFG_LABEL, label);

		graphSettings.saveSettings(settings);
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

	public boolean isEnforeTemporalOrder() {
		return enforeTemporalOrder;
	}

	public void setEnforeTemporalOrder(boolean enforeTemporalOrder) {
		this.enforeTemporalOrder = enforeTemporalOrder;
	}

	public boolean isShowForward() {
		return showForward;
	}

	public void setShowForward(boolean showForward) {
		this.showForward = showForward;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public GraphSettings getGraphSettings() {
		return graphSettings;
	}

	public void setGraphSettings(GraphSettings graphSettings) {
		this.graphSettings = graphSettings;
	}
}
