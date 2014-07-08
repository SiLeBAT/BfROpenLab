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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import de.bund.bfr.knime.openkrise.views.BfrLogo;
import de.bund.bfr.knime.openkrise.views.FoodChainLabLogo;
import de.bund.bfr.knime.openkrise.views.TracingConstants;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class TracingCanvas extends GraphCanvas {

	private static final long serialVersionUID = 1L;

	private static boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;
	private static boolean DEFAULT_PERFORM_TRACING = true;

	private HashMap<Integer, MyDelivery> deliveries;
	private boolean enforceTemporalOrder;
	private boolean performTracing;

	public TracingCanvas() {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(),
				new HashMap<Integer, MyDelivery>());
	}

	public TracingCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties,
			HashMap<Integer, MyDelivery> deliveries) {
		super(nodes, edges, nodeProperties, edgeProperties,
				TracingConstants.ID_COLUMN, TracingConstants.ID_COLUMN,
				TracingConstants.FROM_COLUMN, TracingConstants.TO_COLUMN, true);
		this.deliveries = deliveries;
		enforceTemporalOrder = DEFAULT_ENFORCE_TEMPORAL_ORDER;
		performTracing = DEFAULT_PERFORM_TRACING;
		getViewer().prependPostRenderPaintable(new PostPaintable());
	}

	public Map<String, Double> getCaseWeights() {
		Map<String, Double> caseWeights = new LinkedHashMap<>();

		for (GraphNode node : getNodeSaveMap().values()) {
			Double value = (Double) node.getProperties().get(
					TracingConstants.CASE_WEIGHT_COLUMN);

			caseWeights.put(node.getId(), value);
		}

		return caseWeights;
	}

	public void setCaseWeights(Map<String, Double> caseWeights) {
		for (GraphNode node : getNodeSaveMap().values()) {
			if (caseWeights.containsKey(node.getId())) {
				node.getProperties().put(TracingConstants.CASE_WEIGHT_COLUMN,
						caseWeights.get(node.getId()));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public Map<String, Boolean> getCrossContaminations() {
		Map<String, Boolean> contaminations = new LinkedHashMap<>();

		for (GraphNode node : getNodeSaveMap().values()) {
			Boolean value = (Boolean) node.getProperties().get(
					TracingConstants.CROSS_CONTAMINATION_COLUMN);

			contaminations.put(node.getId(), value);
		}

		return contaminations;
	}

	public void setCrossContaminations(Map<String, Boolean> crossContaminations) {
		for (GraphNode node : getNodeSaveMap().values()) {
			if (crossContaminations.containsKey(node.getId())) {
				node.getProperties().put(
						TracingConstants.CROSS_CONTAMINATION_COLUMN,
						crossContaminations.get(node.getId()));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public Map<String, Boolean> getNodeFilter() {
		Map<String, Boolean> filter = new LinkedHashMap<>();

		for (GraphNode node : getNodeSaveMap().values()) {
			Boolean value = (Boolean) node.getProperties().get(
					TracingConstants.FILTER_COLUMN);

			filter.put(node.getId(), value);
		}

		return filter;
	}

	public void setNodeFilter(Map<String, Boolean> nodeFilter) {
		for (GraphNode node : getNodeSaveMap().values()) {
			if (nodeFilter.containsKey(node.getId())) {
				node.getProperties().put(TracingConstants.FILTER_COLUMN,
						nodeFilter.get(node.getId()));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public Map<String, Boolean> getEdgeFilter() {
		Map<String, Boolean> edgeFilter = new LinkedHashMap<>();

		for (Edge<GraphNode> edge : getEdgeSaveMap().values()) {
			Boolean value = (Boolean) edge.getProperties().get(
					TracingConstants.FILTER_COLUMN);

			edgeFilter.put(edge.getId(), value);
		}

		return edgeFilter;
	}

	public void setEdgeFilter(Map<String, Boolean> edgeFilter) {
		for (Edge<GraphNode> edge : getEdgeSaveMap().values()) {
			if (edgeFilter.containsKey(edge.getId())) {
				edge.getProperties().put(TracingConstants.FILTER_COLUMN,
						edgeFilter.get(edge.getId()));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public boolean isEnforceTemporalOrder() {
		return enforceTemporalOrder;
	}

	public void setEnforceTemporalOrder(boolean enforceTemporalOrder) {
		this.enforceTemporalOrder = enforceTemporalOrder;
		
		if (performTracing) {
			applyChanges();
		}
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
	public void nodePropertiesItemClicked() {
		Set<GraphNode> picked = new LinkedHashSet<>(getSelectedNodes());

		picked.retainAll(getVisibleNodes());

		EditablePropertiesDialog dialog = EditablePropertiesDialog
				.createNodeDialog(this, picked, getNodeProperties(), true);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			applyChanges();
		}
	}

	@Override
	public void edgePropertiesItemClicked() {
		if (isJoinEdges()) {
			super.edgePropertiesItemClicked();
		} else {
			Set<Edge<GraphNode>> picked = new LinkedHashSet<>(
					getSelectedEdges());

			picked.retainAll(getVisibleEdges());

			EditablePropertiesDialog dialog = EditablePropertiesDialog
					.createEdgeDialog(this, picked, getEdgeProperties(), true);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				applyChanges();
			}
		}
	}

	@Override
	public void edgeAllPropertiesItemClicked() {
		Set<Edge<GraphNode>> picked = new LinkedHashSet<>(getSelectedEdges());

		picked.retainAll(getVisibleEdges());

		Set<Edge<GraphNode>> allPicked = new LinkedHashSet<>();

		if (!getJoinMap().isEmpty()) {
			for (Edge<GraphNode> p : picked) {
				if (getJoinMap().containsKey(p)) {
					allPicked.addAll(getJoinMap().get(p));
				}
			}
		} else {
			allPicked.addAll(picked);
		}

		EditablePropertiesDialog dialog = EditablePropertiesDialog
				.createEdgeDialog(this, allPicked, getEdgeProperties(), false);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			applyChanges();
		}
	}

	@Override
	protected VisualizationImageServer<GraphNode, Edge<GraphNode>> createVisualizationServer(
			boolean toSvg) {
		VisualizationImageServer<GraphNode, Edge<GraphNode>> server = super
				.createVisualizationServer(toSvg);

		server.prependPostRenderPaintable(new PostPaintable());

		return server;
	}

	@Override
	protected GraphMouse<GraphNode, Edge<GraphNode>> createMouseModel(
			Mode editingMode) {
		return new GraphMouse<>(
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
				}, editingMode);
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

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		HighlightListDialog dialog = super.openNodeHighlightDialog();

		dialog.addChecker(new HighlightChecker());

		return dialog;
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		HighlightListDialog dialog = super.openEdgeHighlightDialog();

		dialog.addChecker(new HighlightChecker());

		return dialog;
	}

	private void applyTracing() {
		if (!performTracing) {
			return;
		}

		Set<Edge<GraphNode>> edges = new LinkedHashSet<>();

		if (!isJoinEdges()) {
			edges.addAll(getVisibleEdges());
		} else {
			for (Edge<GraphNode> edge : getVisibleEdges()) {
				edges.addAll(getJoinMap().get(edge));
			}
		}

		HashMap<Integer, MyDelivery> activeDeliveries = new HashMap<>();

		for (Edge<GraphNode> id : edges) {
			activeDeliveries.put(getIntegerId(id),
					deliveries.get(getIntegerId(id)));
		}

		MyNewTracing tracing = new MyNewTracing(activeDeliveries,
				new LinkedHashMap<Integer, Double>(),
				new LinkedHashSet<Integer>(), 0.0);

		for (String id : getCollapsedNodes().keySet()) {
			Set<String> nodeIdStrings = getCollapsedNodes().get(id).keySet();
			Set<Integer> nodeIds = new LinkedHashSet<>();

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

		Set<Integer> backwardNodes = new LinkedHashSet<>();
		Set<Integer> forwardNodes = new LinkedHashSet<>();
		Set<Integer> backwardEdges = new LinkedHashSet<>();
		Set<Integer> forwardEdges = new LinkedHashSet<>();

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

		for (Edge<GraphNode> edge : edges) {
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

		for (GraphNode node : getNodes()) {
			int id = getIntegerId(node);

			node.getProperties().put(TracingConstants.SCORE_COLUMN,
					tracing.getStationScore(id));
			node.getProperties().put(TracingConstants.BACKWARD_COLUMN,
					backwardNodes.contains(id));
			node.getProperties().put(TracingConstants.FORWARD_COLUMN,
					forwardNodes.contains(id));
		}

		for (Edge<GraphNode> edge : edges) {
			int id = Integer.parseInt(edge.getId());

			edge.getProperties().put(TracingConstants.SCORE_COLUMN,
					tracing.getDeliveryScore(id));
			edge.getProperties().put(TracingConstants.BACKWARD_COLUMN,
					backwardEdges.contains(id));
			edge.getProperties().put(TracingConstants.FORWARD_COLUMN,
					forwardEdges.contains(id));
		}

		if (isJoinEdges()) {
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

	private static int getIntegerId(Edge<GraphNode> edge) {
		return Integer.parseInt(edge.getId());
	}

	private static int createId(Collection<String> c) {
		return KnimeUtilities.listToString(new ArrayList<>(c)).hashCode();
	}

	private class HighlightChecker implements HighlightConditionChecker {

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
	}

	private class PostPaintable implements Paintable {

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics g) {
			int w = getCanvasSize().width;
			int h = getCanvasSize().height;

			Font font = new Font("Default", Font.BOLD, 20);

			int height = 28;
			int fontHeight = g.getFontMetrics(font).getHeight();
			int fontAscent = g.getFontMetrics(font).getAscent();
			int dFont = (height - fontHeight) / 2;
			int logoHeight = 18;
			int dLogo = (height - logoHeight) / 2;

			int dx = 10;
			String s1 = "Created with";
			int sw1 = (int) font.getStringBounds(s1,
					((Graphics2D) g).getFontRenderContext()).getWidth();
			String s2 = "by";
			int sw2 = (int) font.getStringBounds(s2,
					((Graphics2D) g).getFontRenderContext()).getWidth();
			FoodChainLabLogo logo1 = new FoodChainLabLogo();
			int iw1 = logo1.getOrigWidth() * logoHeight / logo1.getOrigHeight();
			BfrLogo logo2 = new BfrLogo();
			int iw2 = logo2.getOrigWidth() * logoHeight / logo2.getOrigHeight();

			g.setColor(new Color(230, 230, 230));
			g.fillRect(w - sw1 - iw1 - sw2 - iw2 - 5 * dx, h - height, sw1
					+ iw1 + sw2 + iw2 + 5 * dx, height);
			g.setColor(Color.BLACK);
			g.drawRect(w - sw1 - iw1 - sw2 - iw2 - 5 * dx, h - height, sw1
					+ iw1 + sw2 + iw2 + 5 * dx, height);
			g.setFont(font);
			g.drawString(s1, w - sw1 - iw1 - sw2 - iw2 - 4 * dx, h - fontHeight
					- dFont + fontAscent);
			logo1.setDimension(new Dimension(iw1, logoHeight));
			logo1.paintIcon(null, g, w - iw1 - sw2 - iw2 - 3 * dx, h
					- logoHeight - dLogo);
			g.drawString(s2, w - sw2 - iw2 - 2 * dx, h - fontHeight - dFont
					+ fontAscent);
			logo2.setDimension(new Dimension(iw2, logoHeight));
			logo2.paintIcon(null, g, w - iw2 - dx, h - logoHeight - dLogo);
		}
	}
}
