/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Dimension;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import de.bund.bfr.jung.BetterVisualizationViewer;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasOptionsPanel;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasPopupMenu;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public interface ICanvas<V extends Node> {

	void addCanvasListener(CanvasListener listener);

	void removeCanvasListener(CanvasListener listener);

	Set<V> getNodes();

	Set<Edge<V>> getEdges();

	Dimension getCanvasSize();

	void setCanvasSize(Dimension canvasSize);

	NodePropertySchema getNodeSchema();

	EdgePropertySchema getEdgeSchema();

	Naming getNaming();

	Set<V> getSelectedNodes();

	void setSelectedNodes(Set<V> selectedNodes);

	Set<Edge<V>> getSelectedEdges();

	void setSelectedEdges(Set<Edge<V>> selectedEdges);

	Set<String> getSelectedNodeIds();

	void setSelectedNodeIds(Set<String> selectedNodeIds);

	void setSelectedNodeIdsWithoutListener(Set<String> selectedNodeIds);

	Set<String> getSelectedEdgeIds();

	void setSelectedEdgeIds(Set<String> selectedEdgeIds);

	void setSelectedEdgeIdsWithoutListener(Set<String> selectedEdgeIds);

	HighlightConditionList getNodeHighlightConditions();

	void setNodeHighlightConditions(HighlightConditionList nodeHighlightConditions);

	HighlightConditionList getEdgeHighlightConditions();

	void setEdgeHighlightConditions(HighlightConditionList edgeHighlightConditions);

	void setHighlightConditions(HighlightConditionList nodeHighlightConditions,
			HighlightConditionList edgeHighlightConditions);

	Map<String, Set<String>> getCollapsedNodes();

	void setCollapsedNodes(Map<String, Set<String>> collapsedNodes);

	Transform getTransform();

	void setTransform(Transform transform);

	BetterVisualizationViewer<V, Edge<V>> getViewer();

	VisualizationImageServer<V, Edge<V>> getVisualizationServer(boolean toSvg);

	CanvasOptionsPanel getOptionsPanel();

	void setOptionsPanel(CanvasOptionsPanel optionsPanel);

	CanvasPopupMenu getPopupMenu();

	void setPopupMenu(CanvasPopupMenu popup);

	void applyChanges();

	void resetNodesAndEdges();

	void applyNodeCollapse();

	void applyInvisibility();

	void applyJoinEdgesAndSkipEdgeless();

	void applyHighlights();

	void applyShowEdgesInMetaNode();

	JPanel getComponent();
}