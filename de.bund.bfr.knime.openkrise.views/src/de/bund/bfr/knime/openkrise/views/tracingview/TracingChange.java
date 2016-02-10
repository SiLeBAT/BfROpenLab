/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import de.bund.bfr.knime.Pair;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;

public class TracingChange implements Serializable {

	private static final long serialVersionUID = 1L;

	public static class Builder implements Serializable {

		private static final long serialVersionUID = 1L;

		private ViewDiff viewDiff;

		private Transform transformDiff;

		private Set<String> nodesWithChangedSelection;
		private Set<String> edgesWithChangedSelection;

		private HighlightingDiff nodeHighlightingDiff;
		private HighlightingDiff edgeHighlightingDiff;

		private Set<Pair<String, Point2D>> changedNodePositions;
		private Set<Pair<String, Set<String>>> changedCollapsedNodes;

		private Set<Pair<String, Double>> changedNodeWeights;
		private Set<Pair<String, Double>> changedEdgeWeights;

		private Set<Pair<String, Boolean>> changedNodeCrossContams;
		private Set<Pair<String, Boolean>> changedEdgeCrossContams;

		private Set<Pair<String, Boolean>> changedNodeKillContams;
		private Set<Pair<String, Boolean>> changedEdgeKillContams;

		private Set<Pair<String, Boolean>> changedObservedNodes;
		private Set<Pair<String, Boolean>> changedObservedEdges;

		private boolean edgeJoinChanged;
		private boolean skipEdgelessChanged;
		private boolean showEdgesInMetaChanged;
		private boolean arrowInMiddleChanged;
		private boolean showLegendChanged;

		private boolean enforceTempChanged;
		private boolean showForwardChanged;

		private Pair<Integer, Integer> nodeSizeDiff;
		private Pair<Integer, Integer> nodeMaxSizeDiff;
		private Pair<Integer, Integer> edgeThicknessDiff;
		private Pair<Integer, Integer> edgeMaxThicknessDiff;

		private Pair<Integer, Integer> fontSizeDiff;
		private boolean fontBoldChanged;
		private Pair<String, String> labelDiff;

		private Pair<Integer, Integer> borderAlphaDiff;
		private boolean avoidOverlayChanged;

		public static TracingChange createViewChange(boolean showGisBefore, boolean showGisAfter, GisType gisTypeBefore,
				GisType gisTypeAfter) {
			Builder builder = new Builder();

			builder.viewDiff = new ViewDiff(showGisBefore, showGisAfter, gisTypeBefore, gisTypeAfter);

			return builder.build();
		}

		public Builder() {
			viewDiff = null;
			transformDiff = null;
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
			arrowInMiddleChanged = false;
			showLegendChanged = false;
			enforceTempChanged = false;
			showForwardChanged = false;

			nodeSizeDiff = null;
			nodeMaxSizeDiff = null;
			edgeThicknessDiff = null;
			edgeMaxThicknessDiff = null;

			fontSizeDiff = null;
			fontBoldChanged = false;
			labelDiff = null;

			borderAlphaDiff = null;
			avoidOverlayChanged = false;
		}

		public Builder transform(Transform transformBefore, Transform transformAfter) {
			transformDiff = transformAfter.concatenate(transformBefore.inverse());
			return this;
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
			changedNodePositions = symDiff(toSet(nodePositionsBefore), toSet(nodePositionsAfter));
			return this;
		}

		public Builder collapsedNodes(Map<String, Set<String>> collapsedNodesBefore,
				Map<String, Set<String>> collapsedNodesAfter) {
			changedCollapsedNodes = symDiff(toSet(collapsedNodesBefore), toSet(collapsedNodesAfter));
			return this;
		}

		public Builder nodeWeights(Map<String, Double> nodeWeightsBefore, Map<String, Double> nodeWeightsAfter) {
			changedNodeWeights = symDiff(toSet(nodeWeightsBefore), toSet(nodeWeightsAfter));
			return this;
		}

		public Builder edgeWeights(Map<String, Double> edgeWeightsBefore, Map<String, Double> edgeWeightsAfter) {
			changedEdgeWeights = symDiff(toSet(edgeWeightsBefore), toSet(edgeWeightsAfter));
			return this;
		}

		public Builder nodeCrossContaminations(Map<String, Boolean> nodeCrossContamsBefore,
				Map<String, Boolean> nodeCrossContamsAfter) {
			changedNodeCrossContams = symDiff(toSet(nodeCrossContamsBefore), toSet(nodeCrossContamsAfter));
			return this;
		}

		public Builder edgeCrossContaminations(Map<String, Boolean> edgeCrossContamsBefore,
				Map<String, Boolean> edgeCrossContamsAfter) {
			changedEdgeCrossContams = symDiff(toSet(edgeCrossContamsBefore), toSet(edgeCrossContamsAfter));
			return this;
		}

		public Builder nodeKillContaminations(Map<String, Boolean> nodeKillContamsBefore,
				Map<String, Boolean> nodeKillContamsAfter) {
			changedNodeKillContams = symDiff(toSet(nodeKillContamsBefore), toSet(nodeKillContamsAfter));
			return this;
		}

		public Builder edgeKillContaminations(Map<String, Boolean> edgeKillContamsBefore,
				Map<String, Boolean> edgeKillContamsAfter) {
			changedEdgeKillContams = symDiff(toSet(edgeKillContamsBefore), toSet(edgeKillContamsAfter));
			return this;
		}

		public Builder observedNodes(Map<String, Boolean> observedNodesBefore,
				Map<String, Boolean> observedNodesAfter) {
			changedObservedNodes = symDiff(toSet(observedNodesBefore), toSet(observedNodesAfter));
			return this;
		}

		public Builder observedEdges(Map<String, Boolean> observedEdgesBefore,
				Map<String, Boolean> observedEdgesAfter) {
			changedObservedEdges = symDiff(toSet(observedEdgesBefore), toSet(observedEdgesAfter));
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

		public Builder arrowInMiddle(boolean arrowInMiddleBefore, boolean arrowInMiddleAfter) {
			arrowInMiddleChanged = arrowInMiddleBefore != arrowInMiddleAfter;
			return this;
		}

		public Builder showLegend(boolean showLegendBefore, boolean showLegendAfter) {
			showLegendChanged = showLegendBefore != showLegendAfter;
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

		public Builder nodeSize(int nodeSizeBefore, int nodeSizeAfter, Integer nodeMaxSizeBefore,
				Integer nodeMaxSizeAfter) {
			nodeSizeDiff = createDiff(nodeSizeBefore, nodeSizeAfter);
			nodeMaxSizeDiff = createDiff(nodeMaxSizeBefore, nodeMaxSizeAfter);
			return this;
		}

		public Builder edgeThickness(int edgeThicknessBefore, int edgeThicknessAfter, Integer edgeMaxThicknessBefore,
				Integer edgeMaxThicknessAfter) {
			edgeThicknessDiff = createDiff(edgeThicknessBefore, edgeThicknessAfter);
			edgeMaxThicknessDiff = createDiff(edgeMaxThicknessBefore, edgeMaxThicknessAfter);
			return this;
		}

		public Builder font(int fontSizeBefore, int fontSizeAfter, boolean fontBoldBefore, boolean fontBoldAfter) {
			fontSizeDiff = createDiff(fontSizeBefore, fontSizeAfter);
			fontBoldChanged = fontBoldBefore != fontBoldAfter;
			return this;
		}

		public Builder label(String labelBefore, String labelAfter) {
			labelDiff = createDiff(labelBefore, labelAfter);
			return this;
		}

		public Builder borderAlpha(int borderAlphaBefore, int borderAlphaAfter) {
			borderAlphaDiff = createDiff(borderAlphaBefore, borderAlphaAfter);
			return this;
		}

		public Builder avoidOverlay(boolean avoidOverlayBefore, boolean avoidOverlayAfter) {
			avoidOverlayChanged = avoidOverlayBefore != avoidOverlayAfter;
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
		if (builder.transformDiff != null && !builder.transformDiff.equals(Transform.IDENTITY_TRANSFORM)) {
			if (undo) {
				canvas.setTransform(builder.transformDiff.inverse().concatenate(canvas.getTransform()));
			} else {
				canvas.setTransform(builder.transformDiff.concatenate(canvas.getTransform()));
			}
		}

		if (builder.edgeJoinChanged) {
			canvas.setJoinEdges(!canvas.isJoinEdges());
		}

		if (builder.skipEdgelessChanged) {
			canvas.setSkipEdgelessNodes(!canvas.isSkipEdgelessNodes());
		}

		if (builder.showEdgesInMetaChanged) {
			canvas.setShowEdgesInMetaNode(!canvas.isShowEdgesInMetaNode());
		}

		if (builder.arrowInMiddleChanged) {
			canvas.setArrowInMiddle(!canvas.isArrowInMiddle());
		}

		if (builder.showLegendChanged) {
			canvas.setShowLegend(!canvas.isShowLegend());
		}

		if (builder.enforceTempChanged) {
			canvas.setEnforceTemporalOrder(!canvas.isEnforceTemporalOrder());
		}

		if (builder.showForwardChanged) {
			canvas.setShowForward(!canvas.isShowForward());
		}

		if (!builder.changedCollapsedNodes.isEmpty()) {
			canvas.setCollapsedNodes(toMap(symDiff(toSet(canvas.getCollapsedNodes()), builder.changedCollapsedNodes)));
		}

		if (builder.nodeHighlightingDiff != null && !builder.nodeHighlightingDiff.isIdentity()) {
			canvas.setNodeHighlightConditions(
					builder.nodeHighlightingDiff.undoRedo(canvas.getNodeHighlightConditions(), undo));
		}

		if (builder.edgeHighlightingDiff != null && !builder.edgeHighlightingDiff.isIdentity()) {
			canvas.setEdgeHighlightConditions(
					builder.edgeHighlightingDiff.undoRedo(canvas.getEdgeHighlightConditions(), undo));
		}

		if (!builder.nodesWithChangedSelection.isEmpty()) {
			canvas.setSelectedNodeIds(symDiff(canvas.getSelectedNodeIds(), builder.nodesWithChangedSelection));
		}

		if (!builder.edgesWithChangedSelection.isEmpty()) {
			canvas.setSelectedEdgeIds(symDiff(canvas.getSelectedEdgeIds(), builder.edgesWithChangedSelection));
		}

		if (canvas instanceof GraphCanvas && !builder.changedNodePositions.isEmpty()) {
			GraphCanvas c = (GraphCanvas) canvas;

			c.setNodePositions(toMap(symDiff(toSet(c.getNodePositions()), builder.changedNodePositions)));
		}

		if (!builder.changedNodeWeights.isEmpty()) {
			canvas.setNodeWeights(toMap(symDiff(toSet(canvas.getNodeWeights()), builder.changedNodeWeights)));
		}

		if (!builder.changedEdgeWeights.isEmpty()) {
			canvas.setEdgeWeights(toMap(symDiff(toSet(canvas.getEdgeWeights()), builder.changedEdgeWeights)));
		}

		if (!builder.changedNodeCrossContams.isEmpty()) {
			canvas.setNodeCrossContaminations(
					toMap(symDiff(toSet(canvas.getNodeCrossContaminations()), builder.changedNodeCrossContams)));
		}

		if (!builder.changedEdgeCrossContams.isEmpty()) {
			canvas.setEdgeCrossContaminations(
					toMap(symDiff(toSet(canvas.getEdgeCrossContaminations()), builder.changedEdgeCrossContams)));
		}

		if (!builder.changedNodeKillContams.isEmpty()) {
			canvas.setNodeKillContaminations(
					toMap(symDiff(toSet(canvas.getNodeKillContaminations()), builder.changedNodeKillContams)));
		}

		if (!builder.changedEdgeKillContams.isEmpty()) {
			canvas.setEdgeKillContaminations(
					toMap(symDiff(toSet(canvas.getEdgeKillContaminations()), builder.changedEdgeKillContams)));
		}

		if (!builder.changedObservedNodes.isEmpty()) {
			canvas.setObservedNodes(toMap(symDiff(toSet(canvas.getObservedNodes()), builder.changedObservedNodes)));
		}

		if (!builder.changedObservedEdges.isEmpty()) {
			canvas.setObservedEdges(toMap(symDiff(toSet(canvas.getObservedEdges()), builder.changedObservedEdges)));
		}

		if (builder.nodeSizeDiff != null) {
			canvas.setNodeSize(undo ? builder.nodeSizeDiff.getFirst() : builder.nodeSizeDiff.getSecond());
		}

		if (builder.nodeMaxSizeDiff != null) {
			canvas.setNodeMaxSize(undo ? builder.nodeMaxSizeDiff.getFirst() : builder.nodeMaxSizeDiff.getSecond());
		}

		if (builder.edgeThicknessDiff != null) {
			canvas.setEdgeThickness(
					undo ? builder.edgeThicknessDiff.getFirst() : builder.edgeThicknessDiff.getSecond());
		}

		if (builder.edgeMaxThicknessDiff != null) {
			canvas.setEdgeMaxThickness(
					undo ? builder.edgeMaxThicknessDiff.getFirst() : builder.edgeMaxThicknessDiff.getSecond());
		}

		if (builder.fontSizeDiff != null) {
			canvas.setFontSize(undo ? builder.fontSizeDiff.getFirst() : builder.fontSizeDiff.getSecond());
		}

		if (builder.fontBoldChanged) {
			canvas.setFontBold(!canvas.isFontBold());
		}

		if (builder.labelDiff != null) {
			canvas.setLabel(undo ? builder.labelDiff.getFirst() : builder.labelDiff.getSecond());
		}

		if (builder.borderAlphaDiff != null) {
			canvas.setBorderAlpha(undo ? builder.borderAlphaDiff.getFirst() : builder.borderAlphaDiff.getSecond());
		}

		if (builder.avoidOverlayChanged) {
			canvas.setAvoidOverlay(!canvas.isAvoidOverlay());
		}
	}

	public boolean isIdentity() {
		return (builder.viewDiff == null || builder.viewDiff.isIdentity())
				&& (builder.transformDiff == null || builder.transformDiff.equals(Transform.IDENTITY_TRANSFORM))
				&& builder.nodesWithChangedSelection.isEmpty() && builder.edgesWithChangedSelection.isEmpty()
				&& (builder.nodeHighlightingDiff == null || builder.nodeHighlightingDiff.isIdentity())
				&& (builder.edgeHighlightingDiff == null || builder.edgeHighlightingDiff.isIdentity())
				&& builder.changedNodePositions.isEmpty() && builder.changedCollapsedNodes.isEmpty()
				&& builder.changedNodeWeights.isEmpty() && builder.changedEdgeWeights.isEmpty()
				&& builder.changedNodeCrossContams.isEmpty() && builder.changedEdgeCrossContams.isEmpty()
				&& builder.changedNodeKillContams.isEmpty() && builder.changedEdgeKillContams.isEmpty()
				&& builder.changedObservedNodes.isEmpty() && builder.changedObservedEdges.isEmpty()
				&& !builder.edgeJoinChanged && !builder.skipEdgelessChanged && !builder.showEdgesInMetaChanged
				&& !builder.arrowInMiddleChanged && !builder.showLegendChanged && !builder.enforceTempChanged
				&& !builder.showForwardChanged && builder.nodeSizeDiff == null && builder.nodeMaxSizeDiff == null
				&& builder.edgeThicknessDiff == null && builder.edgeMaxThicknessDiff == null
				&& builder.fontSizeDiff == null && !builder.fontBoldChanged && builder.labelDiff == null
				&& builder.borderAlphaDiff == null && !builder.avoidOverlayChanged;
	}

	private static <T> Set<T> symDiff(Set<T> before, Set<T> after) {
		return new LinkedHashSet<>(Sets.symmetricDifference(before, after));
	}

	private static <K, V> Set<Pair<K, V>> toSet(Map<K, V> map) {
		Set<Pair<K, V>> set = new LinkedHashSet<>();

		for (Map.Entry<K, V> entry : map.entrySet()) {
			set.add(new Pair<>(entry.getKey(), entry.getValue()));
		}

		return set;
	}

	private static <K, V> Map<K, V> toMap(Set<Pair<K, V>> set) {
		return set.stream()
				.collect(Collectors.toMap(e -> e.getFirst(), e -> e.getSecond(), (u, v) -> null, LinkedHashMap::new));
	}

	private static <T> Pair<T, T> createDiff(T before, T after) {
		return !Objects.equals(before, after) ? new Pair<>(before, after) : null;
	}

	private static class ViewDiff implements Serializable {

		private static final long serialVersionUID = 1L;

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

	private static class HighlightingDiff implements Serializable {

		private static final long serialVersionUID = 1L;

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

		public HighlightConditionList undoRedo(HighlightConditionList highlighting, boolean undo) {
			int n = highlightingOrderChanges.size() + (undo ? removedConditions.size() : addedConditions.size());
			List<HighlightCondition> oldConditions = highlighting.getConditions();
			List<HighlightCondition> conditions = new ArrayList<>(Collections.nCopies(n, (HighlightCondition) null));

			for (Map.Entry<Integer, Integer> entry : undo ? highlightingOrderChanges.entrySet()
					: highlightingOrderChanges.inverse().entrySet()) {
				conditions.set(entry.getKey(), oldConditions.get(entry.getValue()));
			}

			for (HighlightCondition c : undo ? removedConditions : addedConditions) {
				conditions.set(conditions.indexOf(null), c);
			}

			return new HighlightConditionList(conditions, highlighting.isPrioritizeColors() != prioritizeColorsChanged);
		}

		public boolean isIdentity() {
			if (!removedConditions.isEmpty() || !addedConditions.isEmpty() || prioritizeColorsChanged) {
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
