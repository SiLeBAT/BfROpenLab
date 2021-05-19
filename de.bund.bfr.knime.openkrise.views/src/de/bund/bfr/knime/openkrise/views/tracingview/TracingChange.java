/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import de.bund.bfr.jung.LabelPosition;
import de.bund.bfr.knime.Pair;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.util.ArrowHeadType;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionTracingGraphCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;

public class TracingChange implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public enum ExplosionViewAction {
		None, Opened, Closed
	}

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
		private Pair<ArrowHeadType, ArrowHeadType> arrowHeadTypeDiff;
		private boolean showLegendChanged;

		private boolean enforceTempChanged;
		private boolean showForwardChanged;
		private boolean showWithoutDateChanged;
		private Pair<GregorianCalendar, GregorianCalendar> showToDateDiff;

		private Pair<Integer, Integer> nodeSizeDiff;
		private Pair<Integer, Integer> nodeMaxSizeDiff;
		private Pair<Integer, Integer> edgeThicknessDiff;
		private Pair<Integer, Integer> edgeMaxThicknessDiff;

		private Pair<LabelPosition, LabelPosition> nodeLabelPositionDiff;
		private Pair<Integer, Integer> fontSizeDiff;
		private boolean fontBoldChanged;
		private Pair<String, String> labelDiff;

		private Pair<Integer, Integer> borderAlphaDiff;
		private boolean avoidOverlayChanged;
		private Pair<double[],double[]> boundaryParamsDiff;
		

		public static TracingChange createViewChange(boolean showGisBefore, boolean showGisAfter, GisType gisTypeBefore,
				GisType gisTypeAfter, ExplosionSettings objExplosionSettingsBefore, ExplosionSettings objExplosionSettingsAfter, ExplosionViewAction enmExplosionViewAction) {
			Builder builder = new Builder();

			builder.viewDiff = new ViewDiff(showGisBefore, showGisAfter, gisTypeBefore, gisTypeAfter, objExplosionSettingsBefore, objExplosionSettingsAfter, enmExplosionViewAction);

			return builder.build();
		}
		
		public static TracingChange createViewChange(boolean showGisBefore, boolean showGisAfter, GisType gisTypeBefore,
				GisType gisTypeAfter) {
			return createViewChange(showGisBefore,showGisAfter, gisTypeBefore, gisTypeAfter, null, null, ExplosionViewAction.None);
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
			arrowHeadTypeDiff = null;
			showLegendChanged = false;
			enforceTempChanged = false;
			showForwardChanged = false;
			showWithoutDateChanged = false;
			showToDateDiff = null;

			nodeSizeDiff = null;
			nodeMaxSizeDiff = null;
			edgeThicknessDiff = null;
			edgeMaxThicknessDiff = null;

			nodeLabelPositionDiff = null;
			fontSizeDiff = null;
			fontBoldChanged = false;
			labelDiff = null;

			borderAlphaDiff = null;
			avoidOverlayChanged = false;
			
			boundaryParamsDiff = null;
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

		public Builder arrowHeadType(ArrowHeadType arrowHeadTypeBefore, ArrowHeadType arrowHeadTypeAfter) {
			arrowHeadTypeDiff = createDiff(arrowHeadTypeBefore, arrowHeadTypeAfter);
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

		public Builder showWithoutDateChanged(boolean showWithoutDateBefore, boolean showWithoutDateAfter) {
			showWithoutDateChanged = showWithoutDateBefore != showWithoutDateAfter;
			return this;
		}

		public Builder showToDateChanged(GregorianCalendar showToDateBefore, GregorianCalendar showToDateAfter) {
			showToDateDiff = createDiff(showToDateBefore, showToDateAfter);
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

		public Builder nodeLabelPosition(LabelPosition nodeLabelPositionBefore, LabelPosition nodeLabelPositionAfter) {
			nodeLabelPositionDiff = createDiff(nodeLabelPositionBefore, nodeLabelPositionAfter);
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
		
		public Builder boundaryParams(double[] boundaryParamsBefore, double boundaryParamsAfter[]) {
		  boundaryParamsDiff = createDiff(boundaryParamsBefore, boundaryParamsAfter);
		  
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
			canvas.setTransform((undo ? builder.transformDiff.inverse() : builder.transformDiff)
					.concatenate(canvas.getTransform()));
		}

		if (builder.edgeJoinChanged) {
			canvas.getOptionsPanel().setJoinEdges(!canvas.getOptionsPanel().isJoinEdges());
		}

		if (builder.skipEdgelessChanged) {
			canvas.getOptionsPanel().setSkipEdgelessNodes(!canvas.getOptionsPanel().isSkipEdgelessNodes());
		}

		if (builder.showEdgesInMetaChanged) {
			canvas.getOptionsPanel().setShowEdgesInMetaNode(!canvas.getOptionsPanel().isShowEdgesInMetaNode());
		}

		if (builder.arrowHeadTypeDiff != null) {
			canvas.getOptionsPanel().setArrowHeadType(applyDiff(builder.arrowHeadTypeDiff, undo));
		}

		if (builder.showLegendChanged) {
			canvas.getOptionsPanel().setShowLegend(!canvas.getOptionsPanel().isShowLegend());
		}

		if (builder.enforceTempChanged) {
			canvas.setEnforceTemporalOrder(!canvas.isEnforceTemporalOrder());
		}

		if (builder.showForwardChanged) {
			canvas.setShowForward(!canvas.isShowForward());
		}

		if (builder.showWithoutDateChanged) {
			canvas.setShowDeliveriesWithoutDate(!canvas.isShowDeliveriesWithoutDate());
		}

		if (builder.showToDateDiff != null) {
			canvas.setShowToDate(applyDiff(builder.showToDateDiff, undo));
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
			((GraphCanvas) canvas).setNodePositions(
					toMap(symDiff(toSet(((GraphCanvas) canvas).getNodePositions()), builder.changedNodePositions)));
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
			canvas.getOptionsPanel().setNodeSize(applyDiff(builder.nodeSizeDiff, undo));
		}

		if (builder.nodeMaxSizeDiff != null) {
			canvas.getOptionsPanel().setNodeMaxSize(applyDiff(builder.nodeMaxSizeDiff, undo));
		}

		if (builder.edgeThicknessDiff != null) {
			canvas.getOptionsPanel().setEdgeThickness(applyDiff(builder.edgeThicknessDiff, undo));
		}

		if (builder.edgeMaxThicknessDiff != null) {
			canvas.getOptionsPanel().setEdgeMaxThickness(applyDiff(builder.edgeMaxThicknessDiff, undo));
		}

		if (builder.nodeLabelPositionDiff != null) {
			canvas.getOptionsPanel().setNodeLabelPosition(applyDiff(builder.nodeLabelPositionDiff, undo));
		}

		if (builder.fontSizeDiff != null) {
			canvas.getOptionsPanel().setFontSize(applyDiff(builder.fontSizeDiff, undo));
		}

		if (builder.fontBoldChanged) {
			canvas.getOptionsPanel().setFontBold(!canvas.getOptionsPanel().isFontBold());
		}

		if (builder.labelDiff != null) {
			canvas.getOptionsPanel().setLabel(applyDiff(builder.labelDiff, undo));
		}

		if (builder.borderAlphaDiff != null) {
			canvas.getOptionsPanel().setBorderAlpha(applyDiff(builder.borderAlphaDiff, undo));
		}

		if (builder.avoidOverlayChanged) {
			canvas.getOptionsPanel().setAvoidOverlay(!canvas.getOptionsPanel().isAvoidOverlay());
		}
		
		if (builder.boundaryParamsDiff!=null) {
		  ((ExplosionTracingGraphCanvas) canvas).setBoundaryParams(applyDiff(builder.boundaryParamsDiff, undo));
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
				&& builder.arrowHeadTypeDiff == null && !builder.showLegendChanged && !builder.enforceTempChanged
				&& !builder.showForwardChanged && !builder.showWithoutDateChanged && builder.showToDateDiff == null
				&& builder.nodeSizeDiff == null && builder.nodeMaxSizeDiff == null && builder.edgeThicknessDiff == null
				&& builder.edgeMaxThicknessDiff == null && builder.nodeLabelPositionDiff == null
				&& builder.fontSizeDiff == null && !builder.fontBoldChanged && builder.labelDiff == null
				&& builder.borderAlphaDiff == null && !builder.avoidOverlayChanged && builder.boundaryParamsDiff==null;
	}

	private static <T> Set<T> symDiff(Set<T> before, Set<T> after) {
		return new LinkedHashSet<>(Sets.symmetricDifference(before, after));
	}

	private static <K, V> Set<Pair<K, V>> toSet(Map<K, V> map) {
		Set<Pair<K, V>> set = new LinkedHashSet<>();

		map.forEach((key, value) -> set.add(new Pair<>(key, value)));

		return set;
	}

	private static <K, V> Map<K, V> toMap(Set<Pair<K, V>> set) {
		Map<K, V> map = new LinkedHashMap<>();

		set.forEach(pair -> map.put(pair.getFirst(), pair.getSecond()));

		return map;
	}

	private static <T> Pair<T, T> createDiff(T before, T after) {
		return !Objects.equals(before, after) ? new Pair<>(before, after) : null;
	}

	private static <T> T applyDiff(Pair<T, T> diff, boolean undo) {
		return undo ? diff.getFirst() : diff.getSecond();
	}

	private static class ViewDiff implements Serializable {

		private static final long serialVersionUID = 1L;

		private boolean showGisChanged;

		private GisType gisTypeBefore;
		private GisType gisTypeAfter;
		private ExplosionSettings gobjExplosionSettingsBefore;
		private ExplosionSettings gobjExplosionSettingsAfter;
		private ExplosionViewAction genmExplosionViewAction;
		
		public ViewDiff(boolean showGisBefore, boolean showGisAfter, GisType gisTypeBefore, GisType gisTypeAfter, ExplosionSettings objExplosionSettingsBefore, ExplosionSettings objExplosionSettingsAfter, ExplosionViewAction enmExplosionViewAction) {
			this.showGisChanged = showGisBefore != showGisAfter;	
			this.gisTypeBefore = gisTypeBefore;
			this.gisTypeAfter = gisTypeAfter;
			this.gobjExplosionSettingsBefore = objExplosionSettingsBefore;
			this.gobjExplosionSettingsAfter = objExplosionSettingsAfter;
			this.genmExplosionViewAction = enmExplosionViewAction;
		}

		public void undoRedo(TracingViewSettings set, boolean undo) {
			if (showGisChanged) {
				set.setShowGis(!set.isShowGis());
			}

			set.setGisType(undo ? gisTypeBefore : gisTypeAfter);
			
			if(this.genmExplosionViewAction!=ExplosionViewAction.None) {
				if(undo) {
					if(this.genmExplosionViewAction==ExplosionViewAction.Closed) {
						set.getExplosionSettingsList().setActiveExplosionSettings(this.gobjExplosionSettingsBefore, true);
					} else {
						set.getExplosionSettingsList().setActiveExplosionSettings(this.gobjExplosionSettingsBefore, this.gobjExplosionSettingsAfter);
					}
				} else {
					if(this.genmExplosionViewAction==ExplosionViewAction.Closed) {
						set.getExplosionSettingsList().setActiveExplosionSettings(this.gobjExplosionSettingsAfter, this.gobjExplosionSettingsBefore);
					} else {
						set.getExplosionSettingsList().setActiveExplosionSettings(this.gobjExplosionSettingsAfter, true);
					}
				}
			}
		}

		public boolean isIdentity() {
			return !showGisChanged && gisTypeBefore == gisTypeAfter && this.gobjExplosionSettingsBefore==this.gobjExplosionSettingsAfter;
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

			(undo ? highlightingOrderChanges : highlightingOrderChanges.inverse())
					.forEach((newIndex, oldIndex) -> conditions.set(newIndex, oldConditions.get(oldIndex)));
			(undo ? removedConditions : addedConditions).forEach(c -> conditions.set(conditions.indexOf(null), c));

			return new HighlightConditionList(conditions, highlighting.isPrioritizeColors() != prioritizeColorsChanged);
		}

		public boolean isIdentity() {
			return removedConditions.isEmpty() && addedConditions.isEmpty() && !prioritizeColorsChanged
					&& highlightingOrderChanges.entrySet().stream().allMatch(e -> e.getKey().equals(e.getValue()));
		}
	}
}
