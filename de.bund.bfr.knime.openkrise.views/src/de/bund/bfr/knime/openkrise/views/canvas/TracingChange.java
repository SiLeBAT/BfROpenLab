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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;

public class TracingChange {

	public static class Builder {

		private Set<String> nodesWithChangedSelection;

		private Set<String> edgesWithChangedSelection;

		private Set<Map.Entry<String, Point2D>> changedNodePositions;

		public Builder() {
			nodesWithChangedSelection = null;
			edgesWithChangedSelection = null;
			changedNodePositions = null;
		}

		public Builder selectedNodes(Set<String> selectedNodesBefore, Set<String> selectedNodesAfter) {
			nodesWithChangedSelection = symDiff(selectedNodesBefore, selectedNodesAfter);
			return this;
		}

		public Builder selectedEdges(Set<String> selectedEdgesBefore, Set<String> selectedEdgesAfter) {
			edgesWithChangedSelection = symDiff(selectedEdgesBefore, selectedEdgesAfter);
			return this;
		}

		public Builder nodePositions(Map<String, Point2D> nodePositionsBefore,
				Map<String, Point2D> nodePositionsAfter) {
			changedNodePositions = symDiff(nodePositionsBefore.entrySet(), nodePositionsAfter.entrySet());
			return this;
		}

		public TracingChange build() {
			return new TracingChange(this);
		}
	}

	private Builder builder;

	private TracingChange(Builder builder) {
		this.builder = builder;
	}

	public void undo(ITracingCanvas<?> canvas) {
		if (builder.nodesWithChangedSelection != null) {
			canvas.setSelectedNodeIds(symDiff(canvas.getSelectedNodeIds(), builder.nodesWithChangedSelection));
		}

		if (builder.edgesWithChangedSelection != null) {
			canvas.setSelectedEdgeIds(symDiff(canvas.getSelectedEdgeIds(), builder.edgesWithChangedSelection));
		}

		if (canvas instanceof GraphCanvas && builder.changedNodePositions != null) {
			GraphCanvas c = (GraphCanvas) canvas;

			c.setNodePositions(undoNodePositions(c.getNodePositions()));
		}
	}

	public Map<String, Point2D> undoNodePositions(Map<String, Point2D> nodePositions) {
		if (builder.changedNodePositions == null) {
			return nodePositions;
		}

		Map<String, Point2D> undonePositions = new LinkedHashMap<>();

		for (Map.Entry<String, Point2D> entry : symDiff(nodePositions.entrySet(), builder.changedNodePositions)) {
			undonePositions.put(entry.getKey(), entry.getValue());
		}

		return undonePositions;
	}

	private static <T> Set<T> symDiff(Set<T> before, Set<T> after) {
		return new LinkedHashSet<>(Sets.symmetricDifference(before, after));
	}
}
