/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class LocationCanvas extends GisCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	private List<Edge<LocationNode>> allEdges;
	private Set<LocationNode> nodes;
	private Set<Edge<LocationNode>> edges;
	private Map<Edge<LocationNode>, Set<Edge<LocationNode>>> joinMap;

	private boolean allowEdges;

	public LocationCanvas(boolean allowEdges) {
		this(new ArrayList<LocationNode>(),
				new ArrayList<Edge<LocationNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(), null, null, null, null,
				new ArrayList<RegionNode>(), allowEdges);
	}

	public LocationCanvas(List<LocationNode> nodes,
			Map<String, Class<?>> nodeProperties, String nodeIdProperty,
			List<RegionNode> regions) {
		this(nodes, new ArrayList<Edge<LocationNode>>(), nodeProperties,
				new LinkedHashMap<String, Class<?>>(), nodeIdProperty, null,
				null, null, regions, false);
	}

	public LocationCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty, List<RegionNode> regions) {
		this(nodes, edges, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, edgeFromProperty, edgeToProperty, regions, true);
	}

	private LocationCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty, List<RegionNode> regionNodes,
			boolean allowEdges) {
		super(regionNodes, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, edgeFromProperty, edgeToProperty);
		this.allowEdges = allowEdges;
		this.nodes = new LinkedHashSet<LocationNode>(nodes);
		this.edges = new LinkedHashSet<Edge<LocationNode>>(edges);
		allEdges = edges;
		joinMap = new LinkedHashMap<Edge<LocationNode>, Set<Edge<LocationNode>>>();

		setPopupMenu(new CanvasPopupMenu(allowEdges, false, false));
		setOptionsPanel(new CanvasOptionsPanel(allowEdges, true, true));
		getViewer().getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<LocationNode>(getNodeSize(),
						new LinkedHashMap<LocationNode, Double>()));
		getViewer().getGraphLayout().setGraph(
				CanvasUtilities.createGraph(this.nodes, this.edges));

		for (LocationNode node : this.nodes) {
			getViewer().getGraphLayout().setLocation(node, node.getCenter());
		}
	}

	public Set<LocationNode> getNodes() {
		return nodes;
	}

	public Set<Edge<LocationNode>> getEdges() {
		return edges;
	}

	@Override
	protected void applyChanges() {
		Set<String> selectedEdgeIds = getSelectedEdgeIds();

		if (isJoinEdges()) {
			edges = CanvasUtilities.removeInvisibleElements(allEdges,
					getEdgeHighlightConditions());
			joinMap = CanvasUtilities.joinEdges(edges, getEdgeProperties(),
					getEdgeIdProperty(), getEdgeFromProperty(),
					getEdgeToProperty(),
					CanvasUtilities.getElementIds(allEdges));
			edges = joinMap.keySet();
		} else {
			edges = new LinkedHashSet<Edge<LocationNode>>(allEdges);
			joinMap = new LinkedHashMap<Edge<LocationNode>, Set<Edge<LocationNode>>>();
		}

		getViewer().getGraphLayout().setGraph(
				CanvasUtilities.createGraph(nodes, edges));

		CanvasUtilities.applyNodeHighlights(getViewer(), nodes,
				getNodeHighlightConditions(), getNodeSize(), !allowEdges);

		if (!isJoinEdges()) {
			CanvasUtilities.applyEdgeHighlights(getViewer(), edges,
					getEdgeHighlightConditions());
		} else {
			HighlightConditionList conditions = CanvasUtilities
					.removeInvisibleConditions(getEdgeHighlightConditions());

			CanvasUtilities.applyEdgeHighlights(getViewer(), edges, conditions);
		}

		CanvasUtilities.applyEdgelessNodes(getViewer(), isSkipEdgelessNodes());
		setSelectedEdgeIds(selectedEdgeIds);
		getViewer().repaint();
	}

	@Override
	protected GraphMouse<LocationNode, Edge<LocationNode>> createMouseModel(
			Mode editingMode) {
		return new GraphMouse<LocationNode, Edge<LocationNode>>(
				new PickingGraphMousePlugin<LocationNode, Edge<LocationNode>>() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1
								&& e.getClickCount() == 2) {
							LocationNode node = getViewer().getPickSupport()
									.getVertex(getViewer().getGraphLayout(),
											e.getX(), e.getY());
							Edge<LocationNode> edge = getViewer()
									.getPickSupport().getEdge(
											getViewer().getGraphLayout(),
											e.getX(), e.getY());

							if (node != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

								dialog.setVisible(true);
							} else if (edge != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), edge,
										getEdgeProperties());

								dialog.setVisible(true);
							}
						}
					}

					@Override
					public void mouseDragged(MouseEvent e) {
						if (vertex == null) {
							super.mouseDragged(e);
						}
					}
				}, editingMode);
	}

	@Override
	protected Map<Edge<LocationNode>, Set<Edge<LocationNode>>> getJoinMap() {
		return joinMap;
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, getNodeProperties(), allowEdges,
				true, true, getNodeHighlightConditions(), null);
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		return new HighlightListDialog(this, getEdgeProperties(), true, true,
				true, getEdgeHighlightConditions(), null);
	}

}
