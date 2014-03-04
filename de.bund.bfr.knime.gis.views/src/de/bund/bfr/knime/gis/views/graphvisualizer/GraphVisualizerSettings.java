/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.views.graphvisualizer;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.SimpleGraphVisualizerSettings;

public class GraphVisualizerSettings extends SimpleGraphVisualizerSettings {

	private static final String CFG_COLLAPSED_NODES = "CollapsedNodes";

	private Map<String, Map<String, Point2D>> collapsedNodes;

	public GraphVisualizerSettings() {
		collapsedNodes = new LinkedHashMap<String, Map<String, Point2D>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			collapsedNodes = (Map<String, Map<String, Point2D>>) SERIALIZER
					.fromXml(settings.getString(CFG_COLLAPSED_NODES));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		settings.addString(CFG_COLLAPSED_NODES,
				SERIALIZER.toXml(collapsedNodes));
	}

	public Map<String, Map<String, Point2D>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(
			Map<String, Map<String, Point2D>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
	}

}
