/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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

import java.util.GregorianCalendar;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Node;

public interface ITracingCanvas<V extends Node> extends ICanvas<V> {

	void addTracingListener(TracingListener listener);

	void removeTracingListener(TracingListener listener);

	Map<String, Double> getNodeWeights();

	void setNodeWeights(Map<String, Double> nodeWeights);

	Map<String, Double> getEdgeWeights();

	void setEdgeWeights(Map<String, Double> edgeWeights);

	Map<String, Boolean> getNodeCrossContaminations();

	void setNodeCrossContaminations(Map<String, Boolean> nodeCrossContaminations);

	Map<String, Boolean> getEdgeCrossContaminations();

	void setEdgeCrossContaminations(Map<String, Boolean> edgeCrossContaminations);

	Map<String, Boolean> getNodeKillContaminations();

	void setNodeKillContaminations(Map<String, Boolean> nodeKillContaminations);

	Map<String, Boolean> getEdgeKillContaminations();

	void setEdgeKillContaminations(Map<String, Boolean> edgeKillContaminations);

	Map<String, Boolean> getObservedNodes();

	void setObservedNodes(Map<String, Boolean> observedNodes);

	Map<String, Boolean> getObservedEdges();

	void setObservedEdges(Map<String, Boolean> observedEdges);

	boolean isEnforceTemporalOrder();

	void setEnforceTemporalOrder(boolean enforceTemporalOrder);

	boolean isShowForward();

	void setShowForward(boolean showForward);

	GregorianCalendar getShowToDate();

	void setShowToDate(GregorianCalendar showToDate);

	boolean isShowDeliveriesWithoutDate();

	void setShowDeliveriesWithoutDate(boolean showDeliveriesWithoutDate);

	boolean isPerformTracing();

	void setPerformTracing(boolean performTracing);
}