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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.openkrise.views.tracingview.TracingViewSettings;

public class TracingChange {

	public static class Builder {

		private ViewDiff viewDiff;

		private Set<String> nodesWithChangedSelection;
		private Set<String> edgesWithChangedSelection;

		private HighlightingDiff nodeHighlightingDiff;
		private HighlightingDiff edgeHighlightingDiff;

		private Set<Map.Entry<String, Point2D>> changedNodePositions;
		private Set<Map.Entry<String, Set<String>>> changedCollapsedNodes;

		private Set<Map.Entry<String, Double>> changedNodeWeights;
		private Set<Map.Entry<String, Double>> changedEdgeWeights;

		private Set<Map.Entry<String, Boolean>> changedNodeCrossContams;
		private Set<Map.Entry<String, Boolean>> changedEdgeCrossContams;

		private Set<Map.Entry<String, Boolean>> changedNodeKillContams;
		private Set<Map.Entry<String, Boolean>> changedEdgeKillContams;

		private Set<Map.Entry<String, Boolean>> changedObservedNodes;
		private Set<Map.Entry<String, Boolean>> changedObservedEdges;

		private boolean edgeJoinChanged;
		private boolean skipEdgelessChanged;
		private boolean showEdgesInMetaChanged;

		private boolean enforceTempChanged;
		private boolean showForwardChanged;

		public static TracingChange createViewChange(boolean showGisBefore, boolean showGisAfter, GisType gisTypeBefore,
				GisType gisTypeAfter) {
			Builder builder = new Builder();

			builder.viewDiff = new ViewDiff(showGisBefore, showGisAfter, gisTypeBefore, gisTypeAfter);

			return builder.build();
		}

		public Builder() {
			viewDiff = null;
			nodesWithChangedSelection = new LinkedHashSet<>();
			edgesWithChangedSelection = new LinkedHashSet<>();
			nodeHighlightingDiff = null;
			edgeHighlightingDiff = null;
			changedNodePositions = new LinkedHashSet<>();
			changedCollapsedNodes = new LinkedHashSet<>();
			changedNodeWeights = new LinkedHashSet<>();
			changedEdgeWeights = new LinkedHashSet<>();
			changedNodeCrossContams = new LinkedHashSet<>();
			changedEdgeCrossContams = new LinkedHashSet<>();
			changedNodeKillContams = new LinkedHashSet<>();
			changedEdgeKillContams = new LinkedHashSet<>();
			changedObservedNodes = new LinkedHashSet<>();
			changedObservedEdges = new LinkedHashSet<>();

			edgeJoinChanged = false;
			skipEdgelessChanged = false;
			showEdgesInMetaChanged = false;
			enforceTempChanged = false;
			showForwardChanged = false;
		}

		public Builder selectedNodes(Set<String> selectedNodesBefore, Set<String> selectedNodesAfter) {
			nodesWithChangedSelection = symDiff(selectedNodesBefore, selectedNodesAfter);
			return this;
		}

		public Builder selectedEdges(Set<String> selectedEdgesBefore, Set<String> selectedEdgesAfter) {
			edgesWithChangedSelection = symDiff(selectedEdgesBefore, selectedEdgesAfter);
			return this;
		}

		public Builder nodeHighlighting(HighlightConditionList nodeHighlightingBefore,
				HighlightConditionList nodeHighlightingAfter) {
			nodeHighlightingDiff = new HighlightingDiff(nodeHighlightingBefore, nodeHighlightingAfter);
			return this;
		}

		public Builder edgeHighlighting(HighlightConditionList edgeHighlightingBefore,
				HighlightConditionList edgeHighlightingAfter) {
			edgeHighlightingDiff = new HighlightingDiff(edgeHighlightingBefore, edgeHighlightingAfter);
			return this;
		}

		public Builder nodePositions(Map<String, Point2D> nodePositionsBefore,
				Map<String, Point2D> nodePositionsAfter) {
			changedNodePositions = symDiff(nodePositionsBefore.entrySet(), nodePositionsAfter.entrySet());
			return this;
		}

		public Builder collapsedNodes(Map<String, Set<String>> collapsedNodesBefore,
				Map<String, Set<String>> collapsedNodesAfter) {
			changedCollapsedNodes = symDiff(collapsedNodesBefore.entrySet(), collapsedNodesAfter.entrySet());
			return this;
		}

		public Builder nodeWeights(Map<String, Double> nodeWeightsBefore, Map<String, Double> nodeWeightsAfter) {
			changedNodeWeights = symDiff(nodeWeightsBefore.entrySet(), nodeWeightsAfter.entrySet());
			return this;
		}

		public Builder edgeWeights(Map<String, Double> edgeWeightsBefore, Map<String, Double> edgeWeightsAfter) {
			changedEdgeWeights = symDiff(edgeWeightsBefore.entrySet(), edgeWeightsAfter.entrySet());
			return this;
		}

		public Builder nodeCrossContaminations(Map<String, Boolean> nodeCrossContamsBefore,
				Map<String, Boolean> nodeCrossContamsAfter) {
			changedNodeCrossContams = symDiff(nodeCrossContamsBefore.entrySet(), nodeCrossContamsAfter.entrySet());
			return this;
		}

		public Builder edgeCrossContaminations(Map<String, Boolean> edgeCrossContamsBefore,
				Map<String, Boolean> edgeCrossContamsAfter) {
			changedEdgeCrossContams = symDiff(edgeCrossContamsBefore.entrySet(), edgeCrossContamsAfter.entrySet());
			return this;
		}

		public Builder nodeKillContaminations(Map<String, Boolean> nodeKillContamsBefore,
				Map<String, Boolean> nodeKillContamsAfter) {
			changedNodeKillContams = symDiff(nodeKillContamsBefore.entrySet(), nodeKillContamsAfter.entrySet());
			return this;
		}

		public Builder edgeKillContaminations(Map<String, Boolean> edgeKillContamsBefore,
				Map<String, Boolean> edgeKillContamsAfter) {
			changedEdgeKillContams = symDiff(edgeKillContamsBefore.entrySet(), edgeKillContamsAfter.entrySet());
			return this;
		}

		public Builder observedNodes(Map<String, Boolean> observedNodesBefore,
				Map<String, Boolean> observedNodesAfter) {
			changedObservedNodes = symDiff(observedNodesBefore.entrySet(), observedNodesAfter.entrySet());
			return this;
		}

		public Builder observedEdges(Map<String, Boolean> observedEdgesBefore,
				Map<String, Boolean> observedEdgesAfter) {
			changedObservedEdges = symDiff(observedEdgesBefore.entrySet(), observedEdgesAfter.entrySet());
			return this;
		}

		public Builder joinEdges(boolean joinEdgesBefore, boolean joinEdgesAfter) {
			edgeJoinChanged = joinEdgesBefore != joinEdgesAfter;
			return this;
		}

		public Builder skipEdgelessNodes(boolean skipEdgelessBefore, boolean skipEdgelessAfter) {
			skipEdgelessChanged = skipEdgelessBefore != skipEdgelessAfter;
			return this;
		}

		public Builder showEdgesInMetaNode(boolean showEdgesInMetaBefore, boolean showEdgesInMetaAfter) {
			showEdgesInMetaChanged = showEdgesInMetaBefore != showEdgesInMetaAfter;
			return this;
		}

		public Builder enforceTemporalOrder(boolean enforceTempBefore, boolean enforceTempAfter) {
			enforceTempChanged = enforceTempBefore != enforceTempAfter;
			return this;
		}

		public Builder showForwardChanged(boolean showForwardBefore, boolean showForwardAfter) {
			showForwardChanged = showForwardBefore != showForwardAfter;
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

	public boolean isViewChange() {
		return builder.viewDiff != null;
	}

	public void undo(ITracingCanvas<?> canvas) {
		undoRedo(canvas, true);
	}

	public void redo(ITracingCanvas<?> canvas) {
		undoRedo(canvas, false);
	}

	public void undo(TracingViewSettings set) {
		if (builder.viewDiff != null) {
			builder.viewDiff.undoRedo(set, true);
		}
	}

	public void redo(TracingViewSettings set) {
		if (builder.viewDiff != null) {
			builder.viewDiff.undoRedo(set, false);
		}
	}

	private void undoRedo(ITracingCanvas<?> canvas, boolean undo) {
		if (builder.edgeJoinChanged) {
			canvas.setJoinEdges(!canvas.isJoinEdges());
		}

		if (builder.skipEdgelessChanged) {
			canvas.setSkipEdgelessNodes(!canvas.isSkipEdgelessNodes());
		}

		if (builder.showEdgesInMetaChanged) {
			canvas.setShowEdgesInMetaNode(!canvas.isShowEdgesInMetaNode());
		}

		if (builder.enforceTempChanged) {
			canvas.setEnforceTemporalOrder(!canvas.isEnforceTemporalOrder());
		}

		if (builder.showForwardChanged) {
			canvas.setShowForward(!canvas.isShowForward());
		}

		if (!builder.changedCollapsedNodes.isEmpty()) {
			canvas.setCollapsedNodes(
					createMap(symDiff(canvas.getCollapsedNodes().entrySet(), builder.changedCollapsedNodes)));
		}

		if (builder.nodeHighlightingDiff != null && !builder.nodeHighlightingDiff.isIdentity()) {
			if (undo) {
				canvas.setNodeHighlightConditions(
						builder.nodeHighlightingDiff.undo(canvas.getNodeHighlightConditions()));
			} else {
				canvas.setNodeHighlightConditions(
						builder.nodeHighlightingDiff.redo(canvas.getNodeHighlightConditions()));
			}

		}

		if (builder.edgeHighlightingDiff != null && !builder.edgeHighlightingDiff.isIdentity()) {
			if (undo) {
				canvas.setEdgeHighlightConditions(
						builder.edgeHighlightingDiff.undo(canvas.getEdgeHighlightConditions()));
			} else {
				canvas.setEdgeHighlightConditions(
						builder.edgeHighlightingDiff.redo(canvas.getEdgeHighlightConditions()));
			}
		}

		if (!builder.nodesWithChangedSelection.isEmpty()) {
			canvas.setSelectedNodeIds(symDiff(canvas.getSelectedNodeIds(), builder.nodesWithChangedSelection));
		}

		if (!builder.edgesWithChangedSelection.isEmpty()) {
			canvas.setSelectedEdgeIds(symDiff(canvas.getSelectedEdgeIds(), builder.edgesWithChangedSelection));
		}

		if (canvas instanceof GraphCanvas && !builder.changedNodePositions.isEmpty()) {
			GraphCanvas c = (GraphCanvas) canvas;

			c.setNodePositions(createMap(symDiff(c.getNodePositions().entrySet(), builder.changedNodePositions)));
		}

		if (!builder.changedNodeWeights.isEmpty()) {
			canvas.setNodeWeights(createMap(symDiff(canvas.getNodeWeights().entrySet(), builder.changedNodeWeights)));
		}

		if (!builder.changedEdgeWeights.isEmpty()) {
			canvas.setEdgeWeights(createMap(symDiff(canvas.getEdgeWeights().entrySet(), builder.changedEdgeWeights)));
		}

		if (!builder.changedNodeCrossContams.isEmpty()) {
			canvas.setNodeCrossContaminations(createMap(
					symDiff(canvas.getNodeCrossContaminations().entrySet(), builder.changedNodeCrossContams)));
		}

		if (!builder.changedEdgeCrossContams.isEmpty()) {
			canvas.setEdgeCrossContaminations(createMap(
					symDiff(canvas.getEdgeCrossContaminations().entrySet(), builder.changedEdgeCrossContams)));
		}

		if (!builder.changedNodeKillContams.isEmpty()) {
			canvas.setNodeKillContaminations(
					createMap(symDiff(canvas.getNodeKillContaminations().entrySet(), builder.changedNodeKillContams)));
		}

		if (!builder.changedEdgeKillContams.isEmpty()) {
			canvas.setEdgeKillContaminations(
					createMap(symDiff(canvas.getEdgeKillContaminations().entrySet(), builder.changedEdgeKillContams)));
		}

		if (!builder.changedObservedNodes.isEmpty()) {
			canvas.setObservedNodes(
					createMap(symDiff(canvas.getObservedNodes().entrySet(), builder.changedObservedNodes)));
		}

		if (!builder.changedObservedEdges.isEmpty()) {
			canvas.setObservedEdges(
					createMap(symDiff(canvas.getObservedEdges().entrySet(), builder.changedObservedEdges)));
		}
	}

	public boolean isIdentity() {
		return (builder.viewDiff == null || builder.viewDiff.isIdentity())
				&& builder.nodesWithChangedSelection.isEmpty() && builder.edgesWithChangedSelection.isEmpty()
				&& (builder.nodeHighlightingDiff == null || builder.nodeHighlightingDiff.isIdentity())
				&& (builder.edgeHighlightingDiff == null || builder.edgeHighlightingDiff.isIdentity())
				&& builder.changedNodePositions.isEmpty() && builder.changedCollapsedNodes.isEmpty()
				&& builder.changedNodeWeights.isEmpty() && builder.changedEdgeWeights.isEmpty()
				&& builder.changedNodeCrossContams.isEmpty() && builder.changedEdgeCrossContams.isEmpty()
				&& builder.changedNodeKillContams.isEmpty() && builder.changedEdgeKillContams.isEmpty()
				&& builder.changedObservedNodes.isEmpty() && builder.changedObservedEdges.isEmpty()
				&& !builder.edgeJoinChanged && !builder.skipEdgelessChanged && !builder.showEdgesInMetaChanged
				&& !builder.enforceTempChanged && !builder.showForwardChanged;
	}

	private static <T> Set<T> symDiff(Set<T> before, Set<T> after) {
		return new LinkedHashSet<>(Sets.symmetricDifference(before, after));
	}

	private static <K, V> Map<K, V> createMap(Set<Map.Entry<K, V>> entries) {
		Map<K, V> map = new LinkedHashMap<>();

		for (Map.Entry<K, V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}

		return map;
	}

	private static class ViewDiff {

		private boolean showGisChanged;

		private GisType gisTypeBefore;
		private GisType gisTypeAfter;

		public ViewDiff(boolean showGisBefore, boolean showGisAfter, GisType gisTypeBefore, GisType gisTypeAfter) {
			showGisChanged = showGisBefore != showGisAfter;
			this.gisTypeBefore = gisTypeBefore;
			this.gisTypeAfter = gisTypeAfter;
		}

		public void undoRedo(TracingViewSettings set, boolean undo) {
			if (showGisChanged) {
				set.setShowGis(!set.isShowGis());
			}

			set.setGisType(undo ? gisTypeBefore : gisTypeAfter);
		}

		public boolean isIdentity() {
			return !showGisChanged && gisTypeBefore == gisTypeAfter;
		}
	}

	private static class HighlightingDiff {

		private BiMap<Integer, Integer> highlightingOrderChanges;
		private List<HighlightCondition> removedConditions;
		private List<HighlightCondition> addedConditions;
		private boolean prioritizeColorsChanged;

		public HighlightingDiff(HighlightConditionList highlightingBefore, HighlightConditionList highlightingAfter) {
			highlightingOrderChanges = HashBiMap.create();
			removedConditions = new ArrayList<>();
			addedConditions = new ArrayList<>();
			prioritizeColorsChanged = highlightingBefore.isPrioritizeColors() != highlightingAfter.isPrioritizeColors();

			List<HighlightCondition> before = highlightingBefore.getConditions();
			List<HighlightCondition> after = highlightingAfter.getConditions();
			Set<HighlightCondition> intersect = Sets.intersection(new LinkedHashSet<>(before),
					new LinkedHashSet<>(after));

			for (HighlightCondition c : intersect) {
				highlightingOrderChanges.put(before.indexOf(c), after.indexOf(c));
			}

			for (HighlightCondition c : before) {
				if (!intersect.contains(c)) {
					removedConditions.add(c);
				}
			}

			for (HighlightCondition c : after) {
				if (!intersect.contains(c)) {
					addedConditions.add(c);
				}
			}
		}

		public HighlightConditionList undo(HighlightConditionList highlighting) {
			int n = highlightingOrderChanges.size() + removedConditions.size();
			List<HighlightCondition> oldConditions = highlighting.getConditions();
			List<HighlightCondition> conditions = new ArrayList<>(Collections.nCopies(n, (HighlightCondition) null));

			for (Map.Entry<Integer, Integer> entry : highlightingOrderChanges.entrySet()) {
				conditions.set(entry.getKey(), oldConditions.get(entry.getValue()));
			}

			for (HighlightCondition c : removedConditions) {
				conditions.set(conditions.indexOf(null), c);
			}

			return new HighlightConditionList(conditions, highlighting.isPrioritizeColors() != prioritizeColorsChanged);
		}

		public HighlightConditionList redo(HighlightConditionList highlighting) {
			int n = highlightingOrderChanges.size() + addedConditions.size();
			List<HighlightCondition> oldConditions = highlighting.getConditions();
			List<HighlightCondition> conditions = new ArrayList<>(Collections.nCopies(n, (HighlightCondition) null));

			for (Map.Entry<Integer, Integer> entry : highlightingOrderChanges.entrySet()) {
				conditions.set(entry.getValue(), oldConditions.get(entry.getKey()));
			}

			for (HighlightCondition c : addedConditions) {
				conditions.set(conditions.indexOf(null), c);
			}

			return new HighlightConditionList(conditions, highlighting.isPrioritizeColors() != prioritizeColorsChanged);
		}

		public boolean isIdentity() {
			if (!removedConditions.isEmpty() || !addedConditions.isEmpty()) {
				return false;
			}

			for (Map.Entry<Integer, Integer> entry : highlightingOrderChanges.entrySet()) {
				if (!entry.getKey().equals(entry.getValue())) {
					return false;
				}
			}

			return true;
		}
	}
}
