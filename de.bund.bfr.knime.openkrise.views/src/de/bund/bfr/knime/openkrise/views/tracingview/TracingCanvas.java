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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bund.bfr.knime.gis.views.canvas.CanvasUtilities;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.GraphMouse;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SingleElementPropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.views.TracingConstants;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class TracingCanvas extends GraphCanvas {

	private static final long serialVersionUID = 1L;

	private HashMap<Integer, MyDelivery> deliveries;
	private boolean enforceTemporalOrder;

	public TracingCanvas() {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(),
				new HashMap<Integer, MyDelivery>(), false);
	}

	public TracingCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties,
			HashMap<Integer, MyDelivery> deliveries,
			boolean enforceTemporalOrder) {
		super(nodes, edges, nodeProperties, edgeProperties,
				TracingConstants.ID_COLUMN, TracingConstants.ID_COLUMN,
				TracingConstants.FROM_COLUMN, TracingConstants.TO_COLUMN);
		this.deliveries = deliveries;
		this.enforceTemporalOrder = enforceTemporalOrder;
	}

	public Map<String, Double> getCaseWeights() {
		Map<String, Double> caseWeights = new LinkedHashMap<String, Double>();

		for (GraphNode node : getNodes()) {
			Double value = (Double) node.getProperties().get(
					TracingConstants.CASE_WEIGHT_COLUMN);

			if (value == null) {
				value = 0.0;
			}

			caseWeights.put(node.getId(), value);
		}

		return caseWeights;
	}

	public void setCaseWeights(Map<String, Double> caseWeights) {
		if (caseWeights.isEmpty()) {
			return;
		}

		for (GraphNode node : getNodes()) {
			node.getProperties().put(TracingConstants.CASE_WEIGHT_COLUMN,
					caseWeights.get(node.getId()));
		}

		applyTracing();
	}

	public Map<String, Boolean> getCrossContaminations() {
		Map<String, Boolean> contaminations = new LinkedHashMap<String, Boolean>();

		for (GraphNode node : getNodes()) {
			Boolean value = (Boolean) node.getProperties().get(
					TracingConstants.CROSS_CONTAMINATION_COLUMN);

			if (value == null) {
				value = false;
			}

			contaminations.put(node.getId(), value);
		}

		return contaminations;
	}

	public void setCrossContaminations(Map<String, Boolean> crossContaminations) {
		if (crossContaminations.isEmpty()) {
			return;
		}

		for (GraphNode node : getNodes()) {
			node.getProperties().put(
					TracingConstants.CROSS_CONTAMINATION_COLUMN,
					crossContaminations.get(node.getId()));
		}

		applyTracing();
	}

	public Map<String, Boolean> getFilter() {
		Map<String, Boolean> filter = new LinkedHashMap<String, Boolean>();

		for (GraphNode node : getNodes()) {
			Boolean value = (Boolean) node.getProperties().get(
					TracingConstants.FILTER_COLUMN);

			if (value == null) {
				value = false;
			}

			filter.put(node.getId(), value);
		}

		return filter;
	}

	public void setFilter(Map<String, Boolean> filter) {
		if (filter.isEmpty()) {
			return;
		}

		for (GraphNode node : getNodes()) {
			node.getProperties().put(TracingConstants.FILTER_COLUMN,
					filter.get(node.getId()));
		}

		applyTracing();
	}

	@Override
	protected void showNodeProperties() {
		Set<GraphNode> picked = new LinkedHashSet<GraphNode>(getViewer()
				.getPickedVertexState().getPicked());
		Set<String> pickedIds = CanvasUtilities.getElementIds(picked);

		picked.retainAll(getViewer().getGraphLayout().getGraph().getVertices());

		EditablePropertiesDialog dialog = new EditablePropertiesDialog(this,
				picked, getNodeProperties());

		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			applyTracing();
			setSelectedNodeIds(pickedIds);
		}
	}

	@Override
	protected GraphMouse<GraphNode, Edge<GraphNode>> createMouseModel() {
		return new GraphMouse<GraphNode, Edge<GraphNode>>(
				new PickingGraphMousePlugin<GraphNode, Edge<GraphNode>>() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1
								&& e.getClickCount() == 2) {
							GraphNode node = getViewer().getPickSupport()
									.getVertex(getViewer().getGraphLayout(),
											e.getX(), e.getY());
							Edge<GraphNode> edge = getViewer().getPickSupport()
									.getEdge(getViewer().getGraphLayout(),
											e.getX(), e.getY());

							if (node != null) {
								EditableSingleElementPropertiesDialog dialog = new EditableSingleElementPropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

								CanvasUtilities.placeDialogAt(dialog,
										e.getLocationOnScreen());
								dialog.setVisible(true);

								if (dialog.isApproved()) {
									applyTracing();
								}
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
				});
	}

	@Override
	protected void applyNodeCollapse() {
		super.applyNodeCollapse();
		applyTracing();
	}

	@Override
	protected boolean applyHighlights() {
		if (super.applyHighlights()) {
			applyTracing();

			return true;
		}

		return false;
	}

	private void applyTracing() {
		Set<Integer> edgeIds = new LinkedHashSet<Integer>();

		for (Edge<GraphNode> edge : getVisibleEdges()) {
			if (!isJoinEdges()) {
				edgeIds.add(Integer.parseInt(edge.getId()));
			} else {
				for (Edge<GraphNode> e : getJoinMap().get(edge)) {
					edgeIds.add(Integer.parseInt(e.getId()));
				}
			}
		}

		HashMap<Integer, MyDelivery> activeDeliveries = new HashMap<Integer, MyDelivery>();

		for (int id : edgeIds) {
			activeDeliveries.put(id, deliveries.get(id));
		}

		MyNewTracing tracing = new MyNewTracing(activeDeliveries, null, null,
				0.0);

		for (String metaNodeIdString : getCollapsedNodes().keySet()) {
			int metaNodeId = Integer.parseInt(metaNodeIdString);
			Set<String> nodeIdStrings = getCollapsedNodes().get(
					metaNodeIdString).keySet();
			HashSet<Integer> nodeIds = new HashSet<Integer>();

			for (String idString : nodeIdStrings) {
				nodeIds.add(Integer.parseInt(idString));
			}

			tracing.mergeStations(nodeIds, metaNodeId);
		}

		for (GraphNode node : getNodes()) {
			int id = Integer.parseInt(node.getId());
			Double caseValue = (Double) node.getProperties().get(
					TracingConstants.CASE_WEIGHT_COLUMN);
			Boolean contaminationValue = (Boolean) node.getProperties().get(
					TracingConstants.CROSS_CONTAMINATION_COLUMN);

			if (caseValue != null) {
				tracing.setCase(id, caseValue);
			} else {
				tracing.setCase(id, 0.0);
			}

			if (contaminationValue != null) {
				tracing.setCrossContamination(id, contaminationValue);
			} else {
				tracing.setCrossContamination(id, false);
			}
		}

		tracing.fillDeliveries(enforceTemporalOrder);

		Set<Integer> filterNodes = new LinkedHashSet<Integer>();
		Set<Integer> backwardNodes = new LinkedHashSet<Integer>();
		Set<Integer> forwardNodes = new LinkedHashSet<Integer>();
		Set<Integer> backwardEdges = new LinkedHashSet<Integer>();
		Set<Integer> forwardEdges = new LinkedHashSet<Integer>();

		for (GraphNode node : getNodes()) {
			int id = Integer.parseInt(node.getId());
			Boolean value = (Boolean) node.getProperties().get(
					TracingConstants.FILTER_COLUMN);

			if (value != null && value == true) {
				filterNodes.add(id);
				backwardNodes.addAll(tracing.getBackwardStations(id));
				forwardNodes.addAll(tracing.getForwardStations(id));
				backwardEdges.addAll(tracing.getBackwardDeliveries(id));
				forwardEdges.addAll(tracing.getForwardDeliveries(id));
			}
		}

		for (GraphNode node : getNodes()) {
			int id = Integer.parseInt(node.getId());

			node.getProperties().put(TracingConstants.SCORE_COLUMN,
					tracing.getStationScore(id));
			node.getProperties().put(TracingConstants.BACKWARD_COLUMN,
					backwardNodes.contains(id));
			node.getProperties().put(TracingConstants.FORWARD_COLUMN,
					forwardNodes.contains(id));
		}

		if (!isJoinEdges()) {
			for (Edge<GraphNode> edge : getEdges()) {
				int id = Integer.parseInt(edge.getId());

				edge.getProperties().put(TracingConstants.SCORE_COLUMN,
						tracing.getDeliveryScore(id));
				edge.getProperties().put(TracingConstants.BACKWARD_COLUMN,
						backwardEdges.contains(id));
				edge.getProperties().put(TracingConstants.FORWARD_COLUMN,
						forwardEdges.contains(id));
			}
		} else {
			for (Edge<GraphNode> edge : getEdges()) {
				edge.getProperties().put(TracingConstants.SCORE_COLUMN, null);
				edge.getProperties()
						.put(TracingConstants.BACKWARD_COLUMN, null);
				edge.getProperties().put(TracingConstants.FORWARD_COLUMN, null);
			}
		}

		applyHighlights();
	}
}
