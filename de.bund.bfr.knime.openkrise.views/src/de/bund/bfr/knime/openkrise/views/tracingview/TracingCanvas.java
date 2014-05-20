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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.GraphMouse;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightConditionChecker;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.views.TracingConstants;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class TracingCanvas extends GraphCanvas implements
		HighlightConditionChecker {

	private static final long serialVersionUID = 1L;

	private static boolean DEFAULT_PERFORM_TRACING = true;

	private HashMap<Integer, MyDelivery> deliveries;
	private boolean enforceTemporalOrder;
	private boolean performTracing;

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
		performTracing = DEFAULT_PERFORM_TRACING;
	}

	public Map<String, Double> getCaseWeights() {
		Map<String, Double> caseWeights = new LinkedHashMap<String, Double>();

		for (GraphNode node : getNodeSaveMap().values()) {
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

		for (GraphNode node : getNodeSaveMap().values()) {
			node.getProperties().put(TracingConstants.CASE_WEIGHT_COLUMN,
					caseWeights.get(node.getId()));
		}

		applyChanges();
	}

	public Map<String, Boolean> getCrossContaminations() {
		Map<String, Boolean> contaminations = new LinkedHashMap<String, Boolean>();

		for (GraphNode node : getNodeSaveMap().values()) {
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

		for (GraphNode node : getNodeSaveMap().values()) {
			node.getProperties().put(
					TracingConstants.CROSS_CONTAMINATION_COLUMN,
					crossContaminations.get(node.getId()));
		}

		applyChanges();
	}

	public Map<String, Boolean> getFilter() {
		Map<String, Boolean> filter = new LinkedHashMap<String, Boolean>();

		for (GraphNode node : getNodeSaveMap().values()) {
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

		for (GraphNode node : getNodeSaveMap().values()) {
			node.getProperties().put(TracingConstants.FILTER_COLUMN,
					filter.get(node.getId()));
		}

		applyChanges();
	}

	public Map<String, Boolean> getEdgeFilter() {
		Map<String, Boolean> edgeFilter = new LinkedHashMap<String, Boolean>();

		for (Edge<GraphNode> edge : getEdgeSaveMap().values()) {
			Boolean value = (Boolean) edge.getProperties().get(
					TracingConstants.FILTER_COLUMN);

			if (value == null) {
				value = false;
			}

			edgeFilter.put(edge.getId(), value);
		}

		return edgeFilter;
	}

	public void setEdgeFilter(Map<String, Boolean> edgeFilter) {
		if (edgeFilter.isEmpty()) {
			return;
		}

		for (Edge<GraphNode> edge : getEdgeSaveMap().values()) {
			edge.getProperties().put(TracingConstants.FILTER_COLUMN,
					edgeFilter.get(edge.getId()));
		}

		applyChanges();
	}

	public boolean isPerformTracing() {
		return performTracing;
	}

	public void setPerformTracing(boolean performTracing) {
		this.performTracing = performTracing;

		if (performTracing) {
			applyChanges();
		}
	}

	@Override
	public String findError(HighlightCondition condition) {
		List<String> tracingColumns = Arrays.asList(
				TracingConstants.SCORE_COLUMN,
				TracingConstants.BACKWARD_COLUMN,
				TracingConstants.FORWARD_COLUMN);
		String error = "The following columns cannot be used with \"Invisible\" option:\n";

		for (String column : tracingColumns) {
			error += column + ", ";
		}

		error = error.substring(0, error.length() - 2);

		if (condition != null && condition.isInvisible()) {
			AndOrHighlightCondition logicalCondition = null;
			ValueHighlightCondition valueCondition = null;

			if (condition instanceof AndOrHighlightCondition) {
				logicalCondition = (AndOrHighlightCondition) condition;
			} else if (condition instanceof ValueHighlightCondition) {
				valueCondition = (ValueHighlightCondition) condition;
			} else if (condition instanceof LogicalValueHighlightCondition) {
				logicalCondition = ((LogicalValueHighlightCondition) condition)
						.getLogicalCondition();
				valueCondition = ((LogicalValueHighlightCondition) condition)
						.getValueCondition();
			}

			if (logicalCondition != null) {
				for (List<LogicalHighlightCondition> cc : logicalCondition
						.getConditions()) {
					for (LogicalHighlightCondition c : cc) {
						if (tracingColumns.contains(c.getProperty())) {
							return error;
						}
					}
				}
			}

			if (valueCondition != null) {
				if (tracingColumns.contains(valueCondition.getProperty())) {
					return error;
				}
			}
		}

		return null;
	}

	@Override
	protected void showNodeProperties() {
		Set<GraphNode> picked = new LinkedHashSet<GraphNode>(getSelectedNodes());

		picked.retainAll(getVisibleNodes());

		EditablePropertiesDialog dialog = new EditablePropertiesDialog(this,
				picked, getNodeProperties());

		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			applyTracing();
		}
	}

	@Override
	protected void showEdgeProperties() {
		Set<Edge<GraphNode>> picked = new LinkedHashSet<Edge<GraphNode>>(
				getSelectedEdges());

		picked.retainAll(getVisibleEdges());

		EditablePropertiesDialog dialog = new EditablePropertiesDialog(this,
				picked, getEdgeProperties());

		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			applyTracing();
		}
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, getNodeProperties(), true, true,
				true, getNodeHighlightConditions(), this);
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		return new HighlightListDialog(this, getEdgeProperties(), true, true,
				true, getEdgeHighlightConditions(), this);
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
								EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

								dialog.setVisible(true);

								if (dialog.isApproved()) {
									applyChanges();
								}
							} else if (edge != null) {
								if (!isJoinEdges()) {
									EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(
											e.getComponent(), edge,
											getEdgeProperties());

									dialog.setVisible(true);

									if (dialog.isApproved()) {
										applyChanges();
									}
								} else {
									SinglePropertiesDialog dialog = new SinglePropertiesDialog(
											e.getComponent(), edge,
											getEdgeProperties());

									dialog.setVisible(true);
								}
							}
						}
					}
				}, getEditingMode());
	}

	@Override
	protected void applyChanges() {
		Set<String> selectedNodeIds = getSelectedNodeIds();
		Set<String> selectedEdgeIds = getSelectedEdgeIds();

		applyNodeCollapse();
		applyHighlights();
		applyTracing();
		applyHighlights();

		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
		getViewer().repaint();
	}

	private void applyTracing() {
		if (!performTracing) {
			return;
		}

		Set<Integer> edgeIds = new LinkedHashSet<Integer>();

		for (Edge<GraphNode> edge : getVisibleEdges()) {
			if (!isJoinEdges()) {
				edgeIds.add(getIntegerId(edge));
			} else {
				for (Edge<GraphNode> e : getJoinMap().get(edge)) {
					edgeIds.add(getIntegerId(e));
				}
			}
		}

		HashMap<Integer, MyDelivery> activeDeliveries = new HashMap<Integer, MyDelivery>();

		for (int id : edgeIds) {
			activeDeliveries.put(id, deliveries.get(id));
		}

		MyNewTracing tracing = new MyNewTracing(activeDeliveries,
				new LinkedHashMap<Integer, Double>(),
				new LinkedHashSet<Integer>(), 0.0);

		for (String id : getCollapsedNodes().keySet()) {
			Set<String> nodeIdStrings = getCollapsedNodes().get(id).keySet();
			HashSet<Integer> nodeIds = new HashSet<Integer>();

			for (String idString : nodeIdStrings) {
				nodeIds.add(Integer.parseInt(idString));
			}

			tracing.mergeStations(nodeIds, createId(nodeIdStrings));
		}

		for (GraphNode node : getVisibleNodes()) {
			int id = getIntegerId(node);
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

		Set<Integer> backwardNodes = new LinkedHashSet<Integer>();
		Set<Integer> forwardNodes = new LinkedHashSet<Integer>();
		Set<Integer> backwardEdges = new LinkedHashSet<Integer>();
		Set<Integer> forwardEdges = new LinkedHashSet<Integer>();

		for (GraphNode node : getVisibleNodes()) {
			int id = getIntegerId(node);
			Boolean value = (Boolean) node.getProperties().get(
					TracingConstants.FILTER_COLUMN);

			if (value != null && value == true) {
				backwardNodes.addAll(tracing.getBackwardStations(id));
				forwardNodes.addAll(tracing.getForwardStations(id));
				backwardEdges.addAll(tracing.getBackwardDeliveries(id));
				forwardEdges.addAll(tracing.getForwardDeliveries(id));
			}
		}

		if (!isJoinEdges()) {
			for (Edge<GraphNode> edge : getVisibleEdges()) {
				int id = getIntegerId(edge);
				Boolean value = (Boolean) edge.getProperties().get(
						TracingConstants.FILTER_COLUMN);

				if (value != null && value == true) {
					backwardNodes.addAll(tracing.getBackwardStations2(id));
					forwardNodes.addAll(tracing.getForwardStations2(id));
					backwardEdges.addAll(tracing.getBackwardDeliveries2(id));
					forwardEdges.addAll(tracing.getForwardDeliveries2(id));
				}
			}
		}

		for (GraphNode node : getNodes()) {
			int id = getIntegerId(node);

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
				edge.getProperties().put(TracingConstants.FILTER_COLUMN, null);
				edge.getProperties().put(TracingConstants.SCORE_COLUMN, null);
				edge.getProperties()
						.put(TracingConstants.BACKWARD_COLUMN, null);
				edge.getProperties().put(TracingConstants.FORWARD_COLUMN, null);
			}
		}
	}

	private int getIntegerId(GraphNode node) {
		if (getCollapsedNodes().containsKey(node.getId())) {
			return createId(getCollapsedNodes().get(node.getId()).keySet());
		} else {
			return Integer.parseInt(node.getId());
		}
	}

	private int getIntegerId(Edge<GraphNode> edge) {
		return Integer.parseInt(edge.getId());
	}

	private static int createId(Collection<String> c) {
		return KnimeUtilities.listToString(new ArrayList<String>(c)).hashCode();
	}
}
