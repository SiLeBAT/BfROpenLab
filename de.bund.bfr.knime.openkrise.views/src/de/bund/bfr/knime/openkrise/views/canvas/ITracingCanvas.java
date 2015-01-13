/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Node;

public interface ITracingCanvas<V extends Node> extends ICanvas<V> {

	public abstract Map<String, Double> getNodeWeights();

	public abstract void setNodeWeights(Map<String, Double> nodeWeights);

	public abstract Map<String, Double> getEdgeWeights();

	public abstract void setEdgeWeights(Map<String, Double> edgeWeights);

	public abstract Map<String, Boolean> getNodeCrossContaminations();

	public abstract void setNodeCrossContaminations(
			Map<String, Boolean> nodeCrossContaminations);

	public abstract Map<String, Boolean> getEdgeCrossContaminations();

	public abstract void setEdgeCrossContaminations(
			Map<String, Boolean> edgeCrossContaminations);

	public abstract Map<String, Boolean> getObservedNodes();

	public abstract void setObservedNodes(Map<String, Boolean> observedNodes);

	public abstract Map<String, Boolean> getObservedEdges();

	public abstract void setObservedEdges(Map<String, Boolean> observedEdges);

	public abstract boolean isEnforceTemporalOrder();

	public abstract void setEnforceTemporalOrder(boolean enforceTemporalOrder);

	public abstract boolean isShowForward();

	public abstract void setShowForward(boolean showForward);

	public abstract boolean isPerformTracing();

	public abstract void setPerformTracing(boolean performTracing);

}