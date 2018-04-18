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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import de.bund.bfr.knime.gis.views.canvas.LocationOsmCanvas;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertySelectorCreator;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingPropertySelectorCreator;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.common.Delivery;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public class TracingOsmCanvas extends LocationOsmCanvas implements ITracingGisCanvas<LocationNode> {

	//private static Logger logger =  Logger.getLogger("de.bund.bfr");
	
	private static final long serialVersionUID = 1L;

	private TracingDelegate<LocationNode> tracing;

	public TracingOsmCanvas() {
		this(new ArrayList<>(0), new ArrayList<>(0), new NodePropertySchema(), new EdgePropertySchema(),
				new LinkedHashMap<>(0), false, true);
	}

	public TracingOsmCanvas(List<LocationNode> nodes, List<Edge<LocationNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties, Map<String, Delivery> deliveries, boolean lotBased) {
		this(nodes, edges, nodeProperties, edgeProperties, deliveries, lotBased, true);
	}
	
	public TracingOsmCanvas(List<LocationNode> nodes, List<Edge<LocationNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties, Map<String, Delivery> deliveries, boolean lotBased, boolean allowCollapse) {
		super(nodes, edges, nodeProperties, edgeProperties, !lotBased ? TracingUtils.NAMING : TracingUtils.LOT_NAMING, allowCollapse);
		tracing = new TracingDelegate<>(this, nodeSaveMap, edgeSaveMap, joinMap, deliveries);
	}

	@Override
	public void addTracingListener(TracingListener listener) {
		listenerList.add(TracingListener.class, listener);
	}

	@Override
	public void removeTracingListener(TracingListener listener) {
		listenerList.remove(TracingListener.class, listener);
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
	public void setNodeCrossContaminations(Map<String, Boolean> nodeCrossContaminations) {
		tracing.setNodeCrossContaminations(nodeCrossContaminations);
	}

	@Override
	public Map<String, Boolean> getEdgeCrossContaminations() {
		return tracing.getEdgeCrossContaminations();
	}

	@Override
	public void setEdgeCrossContaminations(Map<String, Boolean> edgeCrossContaminations) {
		tracing.setEdgeCrossContaminations(edgeCrossContaminations);
	}

	@Override
	public Map<String, Boolean> getNodeKillContaminations() {
		return tracing.getNodeKillContaminations();
	}

	@Override
	public void setNodeKillContaminations(Map<String, Boolean> nodeKillContaminations) {
		tracing.setNodeKillContaminations(nodeKillContaminations);
	}

	@Override
	public Map<String, Boolean> getEdgeKillContaminations() {
		return tracing.getEdgeKillContaminations();
	}

	@Override
	public void setEdgeKillContaminations(Map<String, Boolean> edgeKillContaminations) {
		tracing.setEdgeKillContaminations(edgeKillContaminations);
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
	public GregorianCalendar getShowToDate() {
		return tracing.getShowToDate();
	}

	@Override
	public void setShowToDate(GregorianCalendar showToDate) {
		tracing.setShowToDate(showToDate);
	}

	@Override
	public boolean isShowDeliveriesWithoutDate() {
		return tracing.isShowDeliveriesWithoutDate();
	}

	@Override
	public void setShowDeliveriesWithoutDate(boolean showDeliveriesWithoutDate) {
		tracing.setShowDeliveriesWithoutDate(showDeliveriesWithoutDate);
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
	public VisualizationImageServer<LocationNode, Edge<LocationNode>> getVisualizationServer(boolean toSvg) {
		VisualizationImageServer<LocationNode, Edge<LocationNode>> server = super.getVisualizationServer(toSvg);

		server.addPostRenderPaintable(new TracingDelegate.PostPaintable(this));

		return server;
	}

	@Override
	public void applyChanges() {
		tracing.applyChanges();
	}
	
	@Override
	protected boolean isExplosionViewSupported() { return true; }

	@Override
	public void doubleClickedOn(Object obj, MouseEvent e) {
			
		if(e.isControlDown() && (obj instanceof Node) && this.collapsedNodes.containsKey(((Node) obj).getId())) {
			// Strg + DoubleClick on meta node
				
			this.openExplosionViewItemClicked();
				
		} else {
			
			tracing.doubleClickedOn(obj);
				
		}		
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		HighlightListDialog dialog = super.openNodeHighlightDialog();

		dialog.addChecker(new TracingDelegate.HighlightChecker());

		return dialog;
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		HighlightListDialog dialog = super.openEdgeHighlightDialog();

		dialog.addChecker(new TracingDelegate.HighlightChecker());

		return dialog;
	}

	@Override
	protected PropertySelectorCreator createPropertySelectorCreator() {
		return new TracingPropertySelectorCreator(metaNodeProperty);
	}
}
