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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View;

/*
 * Setting object for an explosion view
 */
public class ExplosionSettings {
	
	private final static String CFG_KEY = "ExplosionKey";
	private final static String CFG_NODE_SUBSET = "ExplosionNodeSubset";
	
	private String metaNodeId;
	private Set<String> containedNodesIds;
	private List<String> selectedNodes;
	private List<String> selectedEdges;
	private GisSettings gisSettings;
	private GraphSettings graphSettings;
	
	public ExplosionSettings() {
		this.gisSettings = new GisSettings();
		this.graphSettings = new GraphSettings();
		this.selectedNodes = new ArrayList<>();
		this.selectedEdges = new ArrayList<>();
	}
	
	public ExplosionSettings(String metaNodeId, Set<String> containedNodesIds) {
		this();
		this.metaNodeId = metaNodeId;
		this.containedNodesIds = containedNodesIds;
	}

	public GraphSettings getGraphSettings() { return this.graphSettings; }
	public GisSettings getGisSettings() { return this.gisSettings; }
	public String getKey() { return this.metaNodeId; }
	public Set<String> getContainedNodesIds() {	return this.containedNodesIds; }
	
	@SuppressWarnings("unchecked")
	public void loadSettings(NodeSettingsRO settings, String prefix) {
	  try {
	    this.metaNodeId = settings.getString(prefix + CFG_KEY);
	  } catch (InvalidSettingsException e) {
	  }
	  try {
	    this.containedNodesIds = (Set<String>) GraphSettings.SERIALIZER.fromXml(settings.getString(prefix + CFG_NODE_SUBSET));
	  } catch (InvalidSettingsException e) {
	  }
	  this.gisSettings.loadSettings(settings, prefix);
	  this.graphSettings.loadSettings(settings, prefix);
	}

	public void saveSettings(NodeSettingsWO settings, String prefix) {
	  settings.addString(prefix + CFG_KEY, this.metaNodeId);
	  settings.addString(prefix + CFG_NODE_SUBSET, GraphSettings.SERIALIZER.toXml(this.containedNodesIds));
	  this.gisSettings.saveSettings(settings, prefix);
	  this.graphSettings.saveSettings(settings, prefix);
	}
	
//	public void saveSettings(SettingsJson.View.ExplosionSettings settings) {
//	  settings.id = metaNodeId;
//	  this.graphSettings.saveSettings(settings.graphSettings);
//	  this.gisSettings.saveSettings(settings.gisSettings);
//    }
	
	public void saveSettings(JsonConverter.JsonBuilder jsonBuilder, int index) {
      jsonBuilder.setExplosionId(index, metaNodeId);
      this.graphSettings.saveSettings(jsonBuilder, index);
      this.gisSettings.saveSettings(jsonBuilder, index);
    }
	
	public void loadSettings(View.ExplosionSettings settings) {
      metaNodeId = settings.id;
      this.graphSettings.loadSettings(settings.graphSettings);
      this.gisSettings.loadSettings(settings.gisSettings);
    }
	
	protected List<String> getSelectedNodes() { return this.selectedNodes; }
	
	protected List<String> getSelectedEdges() { return this.selectedEdges; }
	
	protected void setSelectedNodes(List<String> selectedNodes) { this.selectedNodes = selectedNodes; }
	
	protected void setSelectedEdges(List<String> selectedEdges) { this.selectedEdges = selectedEdges; }

}