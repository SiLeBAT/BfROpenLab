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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;
import java.util.Set;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public interface ICanvas<V extends Node> {

	public abstract void addCanvasListener(CanvasListener listener);

	public abstract void removeCanvasListener(CanvasListener listener);

	public abstract Set<V> getNodes();

	public abstract Set<Edge<V>> getEdges();

	public abstract Dimension getCanvasSize();

	public abstract void setCanvasSize(Dimension canvasSize);

	public abstract Mode getEditingMode();

	public abstract void setEditingMode(Mode editingMode);

	public abstract boolean isShowLegend();

	public abstract void setShowLegend(boolean showLegend);

	public abstract boolean isJoinEdges();

	public abstract void setJoinEdges(boolean joinEdges);

	public abstract boolean isSkipEdgelessNodes();

	public abstract void setSkipEdgelessNodes(boolean skipEdgelessNodes);

	public abstract int getFontSize();

	public abstract void setFontSize(int fontSize);

	public abstract boolean isFontBold();

	public abstract void setFontBold(boolean fontBold);

	public abstract int getNodeSize();

	public abstract void setNodeSize(int nodeSize);

	public abstract boolean isArrowInMiddle();

	public abstract void setArrowInMiddle(boolean arrowInMiddle);

	public abstract String getLabel();

	public abstract void setLabel(String label);

	public abstract int getBorderAlpha();

	public abstract void setBorderAlpha(int borderAlpha);

	public abstract NodePropertySchema getNodeSchema();

	public abstract EdgePropertySchema getEdgeSchema();

	public abstract Naming getNaming();

	public abstract Set<V> getSelectedNodes();

	public abstract void setSelectedNodes(Set<V> selectedNodes);

	public abstract Set<Edge<V>> getSelectedEdges();

	public abstract void setSelectedEdges(Set<Edge<V>> selectedEdges);

	public abstract Set<String> getSelectedNodeIds();

	public abstract void setSelectedNodeIds(Set<String> selectedNodeIds);

	public abstract Set<String> getSelectedEdgeIds();

	public abstract void setSelectedEdgeIds(Set<String> selectedEdgeIds);

	public abstract HighlightConditionList getNodeHighlightConditions();

	public abstract void setNodeHighlightConditions(
			HighlightConditionList nodeHighlightConditions);

	public abstract HighlightConditionList getEdgeHighlightConditions();

	public abstract void setEdgeHighlightConditions(
			HighlightConditionList edgeHighlightConditions);

	public abstract Map<String, Set<String>> getCollapsedNodes();

	public abstract void setCollapsedNodes(
			Map<String, Set<String>> collapsedNodes);

	public abstract Transform getTransform();

	public abstract void setTransform(Transform transform);

	public abstract VisualizationViewer<V, Edge<V>> getViewer();

	public abstract VisualizationImageServer<V, Edge<V>> getVisualizationServer(
			boolean toSvg);

	public abstract CanvasOptionsPanel getOptionsPanel();

	public abstract void setOptionsPanel(CanvasOptionsPanel optionsPanel);

	public abstract CanvasPopupMenu getPopupMenu();

	public abstract void setPopupMenu(CanvasPopupMenu popup);

	public abstract void applyChanges();

	public abstract void applyNodeCollapse();

	public abstract void applyInvisibility();

	public abstract void applyJoinEdgesAndSkipEdgeless();

	public abstract void applyHighlights();

	public abstract Component getComponent();

}