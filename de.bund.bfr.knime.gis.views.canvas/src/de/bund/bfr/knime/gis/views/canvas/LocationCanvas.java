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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JTextField;

import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SingleElementPropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class LocationCanvas extends GisCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_LOCATIONSIZE = 4;

	private Set<LocationNode> nodes;
	private Set<Edge<LocationNode>> edges;
	private Set<LocationNode> invisibleNodes;
	private Set<Edge<LocationNode>> invisibleEdges;

	private int locationSize;
	private JTextField locationSizeField;
	private JButton locationSizeButton;

	public LocationCanvas(boolean allowEdges) {
		this(new ArrayList<LocationNode>(),
				new ArrayList<Edge<LocationNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(), null, null,
				new ArrayList<RegionNode>(), allowEdges);
	}

	public LocationCanvas(List<LocationNode> nodes,
			Map<String, Class<?>> nodeProperties, String nodeIdProperty,
			List<RegionNode> regions) {
		this(nodes, new ArrayList<Edge<LocationNode>>(), nodeProperties,
				new LinkedHashMap<String, Class<?>>(), nodeIdProperty, null,
				regions, false);
	}

	public LocationCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, List<RegionNode> regions) {
		this(nodes, edges, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, regions, true);
	}

	private LocationCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, List<RegionNode> regionNodes,
			boolean allowEdges) {
		super(regionNodes, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty);
		this.nodes = new LinkedHashSet<LocationNode>(nodes);
		this.edges = new LinkedHashSet<Edge<LocationNode>>(edges);
		setAllowEdges(allowEdges);
		locationSize = DEFAULT_LOCATIONSIZE;
		invisibleNodes = new LinkedHashSet<LocationNode>();
		invisibleEdges = new LinkedHashSet<Edge<LocationNode>>();

		if (!allowEdges) {
			invisibleNodes.addAll(this.nodes);
		}

		Layout<LocationNode, Edge<LocationNode>> layout = getViewer()
				.getGraphLayout();
		Graph<LocationNode, Edge<LocationNode>> graph = layout.getGraph();

		for (LocationNode node : this.nodes) {
			if (!allowEdges) {
				graph.addVertex(node);
			}

			layout.setLocation(node, node.getCenter());
		}

		for (Edge<LocationNode> edge : this.edges) {
			graph.addEdge(edge, edge.getFrom(), edge.getTo());
		}

		locationSizeField = new JTextField("" + locationSize, 5);
		locationSizeButton = new JButton("Apply");
		locationSizeButton.addActionListener(this);
		addOptionsItem("Location Size", locationSizeField,
				locationSizeButton);
	}

	public Set<LocationNode> getNodes() {
		return nodes;
	}

	public Set<Edge<LocationNode>> getEdges() {
		return edges;
	}

	public int getLocationSize() {
		return locationSize;
	}

	public void setLocationSize(int locationSize) {
		this.locationSize = locationSize;
		locationSizeField.setText(locationSize + "");
		applyHighlights();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == locationSizeButton) {
			try {
				locationSize = Integer.parseInt(locationSizeField.getText());
				applyHighlights();
			} catch (NumberFormatException ex) {
			}
		}
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, getNodeProperties(),
				isAllowEdges(), true, true, getNodeHighlightConditions());
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		return new HighlightListDialog(this, getEdgeProperties(), true, true,
				true, getEdgeHighlightConditions());
	}

	@Override
	protected boolean applyHighlights() {
		boolean changed1 = CanvasUtilities.applyNodeHighlights(getViewer(),
				nodes, edges, invisibleNodes, invisibleEdges,
				getNodeHighlightConditions(), locationSize, !isAllowEdges());
		boolean changed2 = CanvasUtilities.applyEdgeHighlights(getViewer(),
				edges, invisibleEdges, getEdgeHighlightConditions());

		return changed1 || changed2;
	}

	@Override
	protected GraphMouse<LocationNode, Edge<LocationNode>> createMouseModel() {
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
								SingleElementPropertiesDialog dialog = new SingleElementPropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

								CanvasUtilities.placeDialogAt(dialog,
										e.getLocationOnScreen());
								dialog.setVisible(true);
							} else if (edge != null) {
								SingleElementPropertiesDialog dialog = new SingleElementPropertiesDialog(
										e.getComponent(), edge,
										getEdgeProperties());

								CanvasUtilities.placeDialogAt(dialog,
										e.getLocationOnScreen());
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
				});
	}

	@Override
	protected void applyEdgeJoin() {
		// TODO Auto-generated method stub	
	}

}
