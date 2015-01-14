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

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.openkrise.views.Activator;

public class TracingViewSettings extends NodeSettings {

	protected static final XmlConverter SERIALIZER = new XmlConverter(
			Activator.class.getClassLoader());

	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";

	private static final String CFG_NODE_WEIGHTS = "CaseWeights";
	private static final String CFG_EDGE_WEIGHTS = "EdgeWeights";
	private static final String CFG_NODE_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_EDGE_CROSS_CONTAMINATIONS = "EdgeCrossContaminations";
	private static final String CFG_OBSERVED_NODES = "Filter";
	private static final String CFG_OBSERVED_EDGES = "EdgeFilter";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";
	private static final String CFG_SHOW_FORWARD = "ShowConnected";

	private static final boolean DEFAULT_EXPORT_AS_SVG = false;
	private static final boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;
	private static final boolean DEFAULT_SHOW_FORWARD = false;

	private boolean exportAsSvg;

	private Map<String, Double> nodeWeights;
	private Map<String, Double> edgeWeights;
	private Map<String, Boolean> nodeCrossContaminations;
	private Map<String, Boolean> edgeCrossContaminations;
	private Map<String, Boolean> observedNodes;
	private Map<String, Boolean> observedEdges;
	private boolean enforeTemporalOrder;
	private boolean showForward;

	private GraphSettings graphSettings;
	private GisSettings gisSettings;

	public TracingViewSettings() {
		exportAsSvg = DEFAULT_EXPORT_AS_SVG;

		nodeWeights = new LinkedHashMap<>();
		edgeWeights = new LinkedHashMap<>();
		nodeCrossContaminations = new LinkedHashMap<>();
		edgeCrossContaminations = new LinkedHashMap<>();
		observedNodes = new LinkedHashMap<>();
		observedEdges = new LinkedHashMap<>();
		enforeTemporalOrder = DEFAULT_ENFORCE_TEMPORAL_ORDER;
		showForward = DEFAULT_SHOW_FORWARD;

		graphSettings = new GraphSettings();
		gisSettings = new GisSettings();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void loadSettings(NodeSettingsRO settings) {
		try {
			exportAsSvg = settings.getBoolean(CFG_EXPORT_AS_SVG);
		} catch (InvalidSettingsException e) {
		}

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
			observedNodes = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_OBSERVED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedEdges = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_OBSERVED_EDGES));
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

		graphSettings.loadSettings(settings);
		gisSettings.loadSettings(settings);
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);

		settings.addString(CFG_NODE_WEIGHTS, SERIALIZER.toXml(nodeWeights));
		settings.addString(CFG_EDGE_WEIGHTS, SERIALIZER.toXml(edgeWeights));
		settings.addString(CFG_NODE_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(nodeCrossContaminations));
		settings.addString(CFG_EDGE_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(edgeCrossContaminations));
		settings.addString(CFG_OBSERVED_NODES, SERIALIZER.toXml(observedNodes));
		settings.addString(CFG_OBSERVED_EDGES, SERIALIZER.toXml(observedEdges));
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);
		settings.addBoolean(CFG_SHOW_FORWARD, showForward);

		graphSettings.saveSettings(settings);
		gisSettings.saveSettings(settings);
	}

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
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

	public GraphSettings getGraphSettings() {
		return graphSettings;
	}

	public void setGraphSettings(GraphSettings graphSettings) {
		this.graphSettings = graphSettings;
	}

	public GisSettings getGisSettings() {
		return gisSettings;
	}

	public void setGisSettings(GisSettings gisSettings) {
		this.gisSettings = gisSettings;
	}
}
