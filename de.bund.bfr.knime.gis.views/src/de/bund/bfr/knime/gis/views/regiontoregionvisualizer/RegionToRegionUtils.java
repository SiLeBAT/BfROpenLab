/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class RegionToRegionUtils {

	private RegionToRegionUtils() {
	}

	public static Set<String> getSelectedGisNodeIds(Set<RegionNode> gisNodes, Set<GraphNode> selectedGraphNodes) {
		Map<String, RegionNode> gisNodesByRegion = CanvasUtils.getElementsById(gisNodes);
		Set<String> selectedGisNodeIds = new LinkedHashSet<>();

		for (GraphNode graphNode : selectedGraphNodes) {
			RegionNode gisNode = gisNodesByRegion.get(graphNode.getRegion());

			if (gisNode != null) {
				selectedGisNodeIds.add(gisNode.getId());
			}
		}

		return selectedGisNodeIds;
	}

	public static Set<String> getSelectedGraphNodeIds(Set<GraphNode> graphNodes, Set<RegionNode> selectedGisNodes) {
		Set<String> selectedGraphNodeIds = new LinkedHashSet<>();
		SetMultimap<String, String> graphNodesByRegion = LinkedHashMultimap.create();

		for (GraphNode graphNode : graphNodes) {
			graphNodesByRegion.put(graphNode.getRegion(), graphNode.getId());
		}

		for (RegionNode gisNode : selectedGisNodes) {
			selectedGraphNodeIds.addAll(graphNodesByRegion.get(gisNode.getId()));
		}

		return selectedGraphNodeIds;
	}

	public static Set<String> getSelectedGisEdgeIds(Set<Edge<RegionNode>> gisEdges,
			Set<Edge<GraphNode>> graphSelectedEdges, boolean joinEdges) {
		if (!joinEdges) {
			return CanvasUtils.getElementIds(graphSelectedEdges);
		}

		SetMultimap<List<String>, String> gisEdgesByRegion = LinkedHashMultimap.create();

		for (Edge<RegionNode> gisEdge : gisEdges) {
			String fromRegion = gisEdge.getFrom().getId();
			String toRegion = gisEdge.getTo().getId();

			gisEdgesByRegion.put(Arrays.asList(fromRegion, toRegion), gisEdge.getId());
		}

		Set<String> selectedGisEdgeIds = new LinkedHashSet<>();

		for (Edge<GraphNode> graphEdge : graphSelectedEdges) {
			String fromRegion = graphEdge.getFrom().getRegion();
			String toRegion = graphEdge.getTo().getRegion();

			selectedGisEdgeIds.addAll(gisEdgesByRegion.get(Arrays.asList(fromRegion, toRegion)));
		}

		return selectedGisEdgeIds;
	}

	public static Set<String> getSelectedGraphEdgeIds(Set<Edge<GraphNode>> graphEdges,
			Set<Edge<RegionNode>> gisSelectedEdges, boolean joinEdges) {
		if (!joinEdges) {
			return CanvasUtils.getElementIds(gisSelectedEdges);
		}

		SetMultimap<List<String>, String> graphEdgesByRegion = LinkedHashMultimap.create();

		for (Edge<GraphNode> graphEdge : graphEdges) {
			String fromRegion = graphEdge.getFrom().getRegion();
			String toRegion = graphEdge.getTo().getRegion();

			graphEdgesByRegion.put(Arrays.asList(fromRegion, toRegion), graphEdge.getId());
		}

		Set<String> selectedGraphEdgeIds = new LinkedHashSet<>();

		for (Edge<RegionNode> gisEdge : gisSelectedEdges) {
			String fromRegion = gisEdge.getFrom().getId();
			String toRegion = gisEdge.getTo().getId();

			selectedGraphEdgeIds.addAll(graphEdgesByRegion.get(Arrays.asList(fromRegion, toRegion)));
		}

		return selectedGraphEdgeIds;
	}
}
