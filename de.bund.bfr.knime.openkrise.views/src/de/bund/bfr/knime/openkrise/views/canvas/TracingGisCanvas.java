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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.ZoomingPaintable;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingUtils;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class TracingGisCanvas extends LocationCanvas implements
		ITracingCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	private Tracing<LocationNode> tracing;

	public TracingGisCanvas() {
		this(new ArrayList<LocationNode>(),
				new ArrayList<Edge<LocationNode>>(), new NodePropertySchema(),
				new EdgePropertySchema(), new ArrayList<RegionNode>(),
				new LinkedHashMap<Integer, MyDelivery>());
	}

	public TracingGisCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties, List<RegionNode> regions,
			Map<Integer, MyDelivery> deliveries) {
		super(nodes, edges, nodeProperties, edgeProperties,
				TracingUtils.NAMING, regions);
		tracing = new Tracing<>(this, nodeSaveMap, edgeSaveMap, joinMap,
				deliveries);
	}

	@Override
	public Map<String, Double> getNodeWeights() {
		return tracing.getNodeWeights();
	}

	@Override
	public void setNodeWeights(Map<String, Double> nodeWeights) {
		tracing.setNodeWeights(nodeWeights);
	}

	@Override
	public Map<String, Double> getEdgeWeights() {
		return tracing.getEdgeWeights();
	}

	@Override
	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		tracing.setEdgeWeights(edgeWeights);
	}

	@Override
	public Map<String, Boolean> getNodeCrossContaminations() {
		return tracing.getNodeCrossContaminations();
	}

	@Override
	public void setNodeCrossContaminations(
			Map<String, Boolean> nodeCrossContaminations) {
		tracing.setNodeCrossContaminations(nodeCrossContaminations);
	}

	@Override
	public Map<String, Boolean> getEdgeCrossContaminations() {
		return tracing.getEdgeCrossContaminations();
	}

	@Override
	public void setEdgeCrossContaminations(
			Map<String, Boolean> edgeCrossContaminations) {
		tracing.setEdgeCrossContaminations(edgeCrossContaminations);
	}

	@Override
	public Map<String, Boolean> getObservedNodes() {
		return tracing.getObservedNodes();
	}

	@Override
	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		tracing.setObservedNodes(observedNodes);
	}

	@Override
	public Map<String, Boolean> getObservedEdges() {
		return tracing.getObservedEdges();
	}

	@Override
	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		tracing.setObservedEdges(observedEdges);
	}

	@Override
	public boolean isEnforceTemporalOrder() {
		return tracing.isEnforceTemporalOrder();
	}

	@Override
	public void setEnforceTemporalOrder(boolean enforceTemporalOrder) {
		tracing.setEnforceTemporalOrder(enforceTemporalOrder);
	}

	@Override
	public boolean isShowForward() {
		return tracing.isShowForward();
	}

	@Override
	public void setShowForward(boolean showForward) {
		tracing.setShowForward(showForward);
	}

	@Override
	public boolean isPerformTracing() {
		return tracing.isPerformTracing();
	}

	@Override
	public void setPerformTracing(boolean performTracing) {
		tracing.setPerformTracing(performTracing);
	}

	@Override
	public void nodePropertiesItemClicked() {
		tracing.nodePropertiesItemClicked();
	}

	@Override
	public void edgePropertiesItemClicked() {
		tracing.edgePropertiesItemClicked();
	}

	@Override
	public void edgeAllPropertiesItemClicked() {
		tracing.edgeAllPropertiesItemClicked();
	}

	@Override
	public VisualizationImageServer<LocationNode, Edge<LocationNode>> getVisualizationServer(
			boolean toSvg) {
		VisualizationImageServer<LocationNode, Edge<LocationNode>> server = super
				.getVisualizationServer(toSvg);

		server.prependPostRenderPaintable(new Tracing.PostPaintable(this));

		return server;
	}

	@Override
	protected ZoomingPaintable createZoomingPaintable() {
		return new ZoomingPaintable(this, Tracing.PostPaintable.HEIGHT);
	}

	@Override
	public void applyChanges() {
		tracing.applyChanges();
	}

	@Override
	public void applyInvisibility() {
		tracing.applyInvisibility();
	}

	@Override
	protected PickingGraphMousePlugin<LocationNode, Edge<LocationNode>> createPickingPlugin() {
		return new Tracing.PickingPlugin<>(this);
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		HighlightListDialog dialog = super.openNodeHighlightDialog();

		dialog.addChecker(new Tracing.HighlightChecker());

		return dialog;
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		HighlightListDialog dialog = super.openEdgeHighlightDialog();

		dialog.addChecker(new Tracing.HighlightChecker());

		return dialog;
	}
}
