/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightConditionChecker;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightSelectionDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.ImageFileChooser;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.jung.EdgeLabelRenderer;
import de.bund.bfr.knime.gis.views.canvas.jung.GraphMouse;
import de.bund.bfr.knime.gis.views.canvas.jung.MiddleEdgeArrowRenderingSupport;
import de.bund.bfr.knime.gis.views.canvas.jung.ShapePickSupport;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeDrawTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.FontTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeFillTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeStrokeTransformer;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer;

public abstract class Canvas<V extends Node> extends JPanel implements ChangeListener,
		ItemListener, MouseListener, CanvasPopupMenu.ClickListener,
		CanvasOptionsPanel.ChangeListener, ICanvas<V> {

	private static final long serialVersionUID = 1L;
	private static final String IS_META_NODE = "IsMeta";

	protected VisualizationViewer<V, Edge<V>> viewer;
	protected Transform transform;
	protected Naming naming;

	protected List<V> allNodes;
	protected List<Edge<V>> allEdges;
	protected Set<V> nodes;
	protected Set<Edge<V>> edges;
	protected Map<String, V> nodeSaveMap;
	protected Map<String, Edge<V>> edgeSaveMap;
	protected Map<Edge<V>, Set<Edge<V>>> joinMap;
	protected Map<String, Set<String>> collapsedNodes;

	protected NodePropertySchema nodeSchema;
	protected EdgePropertySchema edgeSchema;
	protected String metaNodeProperty;

	protected HighlightConditionList nodeHighlightConditions;
	protected HighlightConditionList edgeHighlightConditions;

	private CanvasOptionsPanel optionsPanel;
	private CanvasPopupMenu popup;

	private List<CanvasListener> canvasListeners;

	public Canvas(List<V> nodes, List<Edge<V>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming) {
		this.nodeSchema = nodeSchema;
		this.edgeSchema = edgeSchema;
		this.naming = naming;
		transform = Transform.IDENTITY_TRANSFORM;
		canvasListeners = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();

		this.nodes = new LinkedHashSet<>();
		this.edges = new LinkedHashSet<>();
		CanvasUtils.copyNodesAndEdges(nodes, edges, this.nodes, this.edges);
		allNodes = nodes;
		allEdges = edges;
		nodeSaveMap = CanvasUtils.getElementsById(this.nodes);
		edgeSaveMap = CanvasUtils.getElementsById(this.edges);
		joinMap = new LinkedHashMap<>();
		collapsedNodes = new LinkedHashMap<>();
		metaNodeProperty = KnimeUtils.createNewValue(IS_META_NODE, nodeSchema.getMap().keySet());
		nodeSchema.getMap().put(metaNodeProperty, Boolean.class);

		viewer = new VisualizationViewer<>(new StaticLayout<>(
				new DirectedSparseMultigraph<V, Edge<V>>()));
		viewer.setBackground(Color.WHITE);
		viewer.addMouseListener(this);
		viewer.getPickedVertexState().addItemListener(this);
		viewer.getPickedEdgeState().addItemListener(this);
		viewer.getRenderContext().setVertexFillPaintTransformer(
				new NodeFillTransformer<>(viewer.getRenderContext()));
		viewer.getRenderContext().setVertexStrokeTransformer(
				new NodeStrokeTransformer<V>(metaNodeProperty));
		viewer.getRenderContext().setEdgeDrawPaintTransformer(
				new EdgeDrawTransformer<>(viewer.getRenderContext()));
		viewer.getRenderer().setEdgeLabelRenderer(new EdgeLabelRenderer<V, Edge<V>>());
		((MutableAffineTransformer) viewer.getRenderContext().getMultiLayerTransformer()
				.getTransformer(Layer.LAYOUT)).addChangeListener(this);
		viewer.addPostRenderPaintable(new PostPaintable(false));
		viewer.addPostRenderPaintable(createZoomingPaintable());
		viewer.getGraphLayout().setGraph(CanvasUtils.createGraph(this.nodes, this.edges));
		viewer.setPickSupport(new ShapePickSupport<>(viewer));

		GraphMouse<V, Edge<V>> graphMouse = createGraphMouse();

		graphMouse.setMode(CanvasOptionsPanel.DEFAULT_MODE);

		viewer.setGraphMouse(graphMouse);

		setLayout(new BorderLayout());
		add(viewer, BorderLayout.CENTER);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void addCanvasListener(CanvasListener listener) {
		canvasListeners.add(listener);
	}

	@Override
	public void removeCanvasListener(CanvasListener listener) {
		canvasListeners.remove(listener);
	}

	@Override
	public Set<V> getNodes() {
		return nodes;
	}

	@Override
	public Set<Edge<V>> getEdges() {
		return edges;
	}

	@Override
	public Dimension getCanvasSize() {
		return viewer.getSize();
	}

	@Override
	public void setCanvasSize(Dimension canvasSize) {
		viewer.setPreferredSize(canvasSize);
		resetLayoutItemClicked();
	}

	@Override
	public Mode getEditingMode() {
		return optionsPanel.getEditingMode();
	}

	@Override
	public void setEditingMode(Mode editingMode) {
		optionsPanel.setEditingMode(editingMode);
	}

	@Override
	public boolean isShowLegend() {
		return optionsPanel.isShowLegend();
	}

	@Override
	public void setShowLegend(boolean showLegend) {
		optionsPanel.setShowLegend(showLegend);
	}

	@Override
	public boolean isJoinEdges() {
		return optionsPanel.isJoinEdges();
	}

	@Override
	public void setJoinEdges(boolean joinEdges) {
		optionsPanel.setJoinEdges(joinEdges);
	}

	@Override
	public boolean isSkipEdgelessNodes() {
		return optionsPanel.isSkipEdgelessNodes();
	}

	@Override
	public void setSkipEdgelessNodes(boolean skipEdgelessNodes) {
		optionsPanel.setSkipEdgelessNodes(skipEdgelessNodes);
	}

	@Override
	public boolean isShowEdgesInMetaNode() {
		return optionsPanel.isShowEdgesInMetaNode();
	}

	@Override
	public void setShowEdgesInMetaNode(boolean showEdgesInMetaNode) {
		optionsPanel.setShowEdgesInMetaNode(showEdgesInMetaNode);
	}

	@Override
	public int getFontSize() {
		return optionsPanel.getFontSize();
	}

	@Override
	public void setFontSize(int fontSize) {
		optionsPanel.setFontSize(fontSize);
	}

	@Override
	public boolean isFontBold() {
		return optionsPanel.isFontBold();
	}

	@Override
	public void setFontBold(boolean fontBold) {
		optionsPanel.setFontBold(fontBold);
	}

	@Override
	public int getNodeSize() {
		return optionsPanel.getNodeSize();
	}

	@Override
	public void setNodeSize(int nodeSize) {
		optionsPanel.setNodeSize(nodeSize);
	}

	@Override
	public boolean isArrowInMiddle() {
		return optionsPanel.isArrowInMiddle();
	}

	@Override
	public void setArrowInMiddle(boolean arrowInMiddle) {
		optionsPanel.setArrowInMiddle(arrowInMiddle);
	}

	@Override
	public String getLabel() {
		return optionsPanel.getLabel();
	}

	@Override
	public void setLabel(String label) {
		optionsPanel.setLabel(label);
	}

	@Override
	public int getBorderAlpha() {
		return optionsPanel.getBorderAlpha();
	}

	@Override
	public void setBorderAlpha(int borderAlpha) {
		optionsPanel.setBorderAlpha(borderAlpha);
	}

	@Override
	public NodePropertySchema getNodeSchema() {
		return nodeSchema;
	}

	@Override
	public EdgePropertySchema getEdgeSchema() {
		return edgeSchema;
	}

	@Override
	public Naming getNaming() {
		return naming;
	}

	@Override
	public Set<V> getSelectedNodes() {
		Set<V> selected = new LinkedHashSet<>(viewer.getPickedVertexState().getPicked());

		selected.retainAll(nodes);

		return selected;
	}

	@Override
	public void setSelectedNodes(Set<V> selectedNodes) {
		viewer.getPickedVertexState().clear();

		for (V node : selectedNodes) {
			if (nodes.contains(node)) {
				viewer.getPickedVertexState().pick(node, true);
			}
		}
	}

	@Override
	public Set<Edge<V>> getSelectedEdges() {
		Set<Edge<V>> selected = new LinkedHashSet<>(viewer.getPickedEdgeState().getPicked());

		selected.retainAll(edges);

		return selected;
	}

	@Override
	public void setSelectedEdges(Set<Edge<V>> selectedEdges) {
		viewer.getPickedEdgeState().clear();

		for (Edge<V> edge : selectedEdges) {
			if (edges.contains(edge)) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public Set<String> getSelectedNodeIds() {
		return CanvasUtils.getElementIds(getSelectedNodes());
	}

	@Override
	public void setSelectedNodeIds(Set<String> selectedNodeIds) {
		setSelectedNodes(CanvasUtils.getElementsById(nodes, selectedNodeIds));
	}

	@Override
	public Set<String> getSelectedEdgeIds() {
		return CanvasUtils.getElementIds(getSelectedEdges());
	}

	@Override
	public void setSelectedEdgeIds(Set<String> selectedEdgeIds) {
		setSelectedEdges(CanvasUtils.getElementsById(edges, selectedEdgeIds));
	}

	@Override
	public HighlightConditionList getNodeHighlightConditions() {
		return nodeHighlightConditions;
	}

	@Override
	public void setNodeHighlightConditions(HighlightConditionList nodeHighlightConditions) {
		this.nodeHighlightConditions = nodeHighlightConditions;
		applyChanges();
		fireNodeHighlightingChanged();
	}

	@Override
	public HighlightConditionList getEdgeHighlightConditions() {
		return edgeHighlightConditions;
	}

	@Override
	public void setEdgeHighlightConditions(HighlightConditionList edgeHighlightConditions) {
		this.edgeHighlightConditions = edgeHighlightConditions;
		applyChanges();
		fireEdgeHighlightingChanged();
	}

	@Override
	public Map<String, Set<String>> getCollapsedNodes() {
		return collapsedNodes;
	}

	@Override
	public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
		applyChanges();
		fireCollapsedNodesChanged();
	}

	@Override
	public Transform getTransform() {
		return transform;
	}

	@Override
	public void setTransform(Transform transform) {
		this.transform = transform;

		((MutableAffineTransformer) viewer.getRenderContext().getMultiLayerTransformer()
				.getTransformer(Layer.LAYOUT)).setTransform(transform.toAffineTransform());
		applyTransform();
		viewer.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		AffineTransform transform = ((MutableAffineTransformer) viewer.getRenderContext()
				.getMultiLayerTransformer().getTransformer(Layer.LAYOUT)).getTransform();

		if (transform.getScaleX() != 0.0 && transform.getScaleY() != 0.0) {
			this.transform = new Transform(transform);
		} else {
			this.transform = Transform.IDENTITY_TRANSFORM;
		}

		applyTransform();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getItem() instanceof Node) {
			if (viewer.getPickedVertexState().getPicked().isEmpty()) {
				popup.setNodeSelectionEnabled(false);
			} else {
				popup.setNodeSelectionEnabled(true);
			}

			fireNodeSelectionChanged();
		} else if (e.getItem() instanceof Edge) {
			if (viewer.getPickedEdgeState().getPicked().isEmpty()) {
				popup.setEdgeSelectionEnabled(false);
			} else {
				popup.setEdgeSelectionEnabled(true);
			}

			fireEdgeSelectionChanged();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			switch (getEditingMode()) {
			case TRANSFORMING:
				setEditingMode(Mode.PICKING);
				viewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				break;
			case PICKING:
				setEditingMode(Mode.TRANSFORMING);
				viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void resetLayoutItemClicked() {
		setTransform(Transform.IDENTITY_TRANSFORM);
	}

	@Override
	public void saveAsItemClicked() {
		ImageFileChooser chooser = new ImageFileChooser();

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			if (chooser.getFileFilter() == ImageFileChooser.PNG_FILTER) {
				try {
					VisualizationImageServer<V, Edge<V>> server = getVisualizationServer(false);
					BufferedImage img = new BufferedImage(viewer.getWidth(), viewer.getHeight(),
							BufferedImage.TYPE_INT_RGB);

					server.paint(img.getGraphics());
					ImageIO.write(img, "png", chooser.getImageFile());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this, "Error saving png file", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else if (chooser.getFileFilter() == ImageFileChooser.SVG_FILTER) {
				try {
					VisualizationImageServer<V, Edge<V>> server = getVisualizationServer(true);
					DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
					Document document = domImpl.createDocument(null, "svg", null);
					SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
					Writer outsvg = new OutputStreamWriter(new FileOutputStream(
							chooser.getImageFile()), StandardCharsets.UTF_8);

					svgGenerator.setSVGCanvasSize(new Dimension(viewer.getWidth(), viewer
							.getHeight()));
					server.paint(svgGenerator);
					svgGenerator.stream(outsvg, true);
					outsvg.close();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this, "Error saving svg file", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public void selectConnectionsItemClicked() {
		Set<V> selected = getSelectedNodes();

		for (Edge<V> edge : edges) {
			if (selected.contains(edge.getFrom()) && selected.contains(edge.getTo())) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public void selectIncomingItemClicked() {
		Set<V> selected = getSelectedNodes();

		for (Edge<V> edge : edges) {
			if (selected.contains(edge.getTo())) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public void selectOutgoingItemClicked() {
		Set<V> selected = getSelectedNodes();

		for (Edge<V> edge : edges) {
			if (selected.contains(edge.getFrom())) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public void clearSelectedNodesItemClicked() {
		viewer.getPickedVertexState().clear();
	}

	@Override
	public void clearSelectedEdgesItemClicked() {
		viewer.getPickedEdgeState().clear();
	}

	@Override
	public void highlightSelectedNodesItemClicked() {
		HighlightListDialog dialog = openNodeHighlightDialog();
		List<List<LogicalHighlightCondition>> conditions = new ArrayList<>();

		for (String id : getSelectedNodeIds()) {
			LogicalHighlightCondition c = new LogicalHighlightCondition(nodeSchema.getId(),
					LogicalHighlightCondition.EQUAL_TYPE, id);

			conditions.add(Arrays.asList(c));
		}

		AndOrHighlightCondition condition = new AndOrHighlightCondition(conditions, null, false,
				Color.RED, false, false, null);

		dialog.setAutoAddCondition(condition);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setNodeHighlightConditions(dialog.getHighlightConditions());
		}
	}

	@Override
	public void highlightSelectedEdgesItemClicked() {
		HighlightListDialog dialog = openEdgeHighlightDialog();
		List<List<LogicalHighlightCondition>> conditions = new ArrayList<>();

		for (String id : getSelectedEdgeIds()) {
			LogicalHighlightCondition c = new LogicalHighlightCondition(edgeSchema.getId(),
					LogicalHighlightCondition.EQUAL_TYPE, id);

			conditions.add(Arrays.asList(c));
		}

		AndOrHighlightCondition condition = new AndOrHighlightCondition(conditions, null, false,
				Color.RED, false, false, null);

		dialog.setAutoAddCondition(condition);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setEdgeHighlightConditions(dialog.getHighlightConditions());
		}
	}

	@Override
	public void highlightNodesItemClicked() {
		HighlightListDialog dialog = openNodeHighlightDialog();

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setNodeHighlightConditions(dialog.getHighlightConditions());
		}
	}

	@Override
	public void highlightEdgesItemClicked() {
		HighlightListDialog dialog = openEdgeHighlightDialog();

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setEdgeHighlightConditions(dialog.getHighlightConditions());
		}
	}

	@Override
	public void clearHighlightedNodesItemClicked() {
		if (JOptionPane.showConfirmDialog(this, "Do you really want to remove all " + naming.node()
				+ " highlight conditions?", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			setNodeHighlightConditions(new HighlightConditionList());
		}
	}

	@Override
	public void clearHighlightedEdgesItemClicked() {
		if (JOptionPane.showConfirmDialog(this, "Do you really want to remove all " + naming.edge()
				+ " highlight conditions?", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			setEdgeHighlightConditions(new HighlightConditionList());
		}
	}

	@Override
	public void selectHighlightedNodesItemClicked() {
		HighlightSelectionDialog dialog = new HighlightSelectionDialog(this,
				nodeHighlightConditions.getConditions());

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedNodes(CanvasUtils.getHighlightedElements(nodes,
					dialog.getHighlightConditions()));
		}
	}

	@Override
	public void selectHighlightedEdgesItemClicked() {
		HighlightSelectionDialog dialog = new HighlightSelectionDialog(this,
				edgeHighlightConditions.getConditions());

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedEdges(CanvasUtils.getHighlightedElements(edges,
					dialog.getHighlightConditions()));
		}

	}

	@Override
	public void highlightNodeCategoriesItemClicked() {
		String[] properties = nodeSchema.getMap().keySet().toArray(new String[0]);
		String result = (String) JOptionPane.showInputDialog(this,
				"Select Property with Categories?", "Highlight Categories",
				JOptionPane.QUESTION_MESSAGE, null, properties, properties[0]);

		if (result != null) {
			HighlightConditionList newHighlighting = new HighlightConditionList(
					nodeHighlightConditions);

			newHighlighting.getConditions().addAll(
					CanvasUtils.createCategorialHighlighting(nodes, result));
			setNodeHighlightConditions(newHighlighting);
		}
	}

	@Override
	public void selectNodesItemClicked() {
		nodeSchema.getPossibleValues().clear();
		nodeSchema.getPossibleValues().putAll(CanvasUtils.getPossibleValues(nodeSaveMap.values()));

		HighlightDialog dialog = HighlightDialog.createFilterDialog(this, nodeSchema, null);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedNodes(CanvasUtils.getHighlightedElements(nodes,
					Arrays.asList(dialog.getHighlightCondition())));
		}
	}

	@Override
	public void selectEdgesItemClicked() {
		edgeSchema.getPossibleValues().clear();
		edgeSchema.getPossibleValues().putAll(CanvasUtils.getPossibleValues(edgeSaveMap.values()));

		HighlightDialog dialog = HighlightDialog.createFilterDialog(this, edgeSchema, null);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedEdges(CanvasUtils.getHighlightedElements(edges,
					Arrays.asList(dialog.getHighlightCondition())));
		}
	}

	@Override
	public void nodePropertiesItemClicked() {
		PropertiesDialog<V> dialog = PropertiesDialog.createNodeDialog(this, getSelectedNodes(),
				nodeSchema, true);

		dialog.setVisible(true);
	}

	@Override
	public void edgePropertiesItemClicked() {
		PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(this, getSelectedEdges(),
				edgeSchema, true);

		dialog.setVisible(true);
	}

	@Override
	public void nodeAllPropertiesItemClicked() {
		Set<V> pickedAll = new LinkedHashSet<>();

		for (V node : getSelectedNodes()) {
			if (collapsedNodes.containsKey(node.getId())) {
				for (String id : collapsedNodes.get(node.getId())) {
					pickedAll.add(nodeSaveMap.get(id));
				}
			} else {
				pickedAll.add(node);
			}
		}

		PropertiesDialog<V> dialog = PropertiesDialog.createNodeDialog(this, pickedAll, nodeSchema,
				false);

		dialog.setVisible(true);
	}

	@Override
	public void edgeAllPropertiesItemClicked() {
		Set<Edge<V>> allPicked = new LinkedHashSet<>();

		for (Edge<V> p : getSelectedEdges()) {
			if (joinMap.containsKey(p)) {
				allPicked.addAll(joinMap.get(p));
			} else {
				allPicked.add(p);
			}
		}

		PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(this, allPicked, edgeSchema,
				false);

		dialog.setVisible(true);
	}

	@Override
	public void collapseToNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : selectedIds) {
			if (collapsedNodes.keySet().contains(id)) {
				JOptionPane.showMessageDialog(this, "Some of the selected " + naming.nodes()
						+ " are already collapsed", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		String newId = CanvasUtils.openNewIdDialog(this, nodeSaveMap.keySet(), naming.Node());

		if (newId == null) {
			return;
		}

		collapsedNodes.put(newId, selectedIds);
		applyChanges();
		fireCollapsedNodesChanged();
		setSelectedNodeIds(new LinkedHashSet<>(Arrays.asList(newId)));
	}

	@Override
	public void expandFromNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : selectedIds) {
			if (!collapsedNodes.keySet().contains(id)) {
				JOptionPane.showMessageDialog(this, "Some of the selected " + naming.nodes()
						+ " are not collapsed", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Set<String> newIds = new LinkedHashSet<>();

		for (String id : selectedIds) {
			newIds.addAll(collapsedNodes.remove(id));
			nodeSaveMap.remove(id);
		}

		applyChanges();
		fireCollapsedNodesChanged();
		setSelectedNodeIds(newIds);
	}

	@Override
	public void collapseByPropertyItemClicked() {
		Set<String> selectedIds = CanvasUtils.getElementIds(getSelectedNodes());
		Set<String> idsToCollapse;

		if (!selectedIds.isEmpty()
				&& JOptionPane.showConfirmDialog(this, "Use only the selected " + naming.nodes()
						+ " for collapsing?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			idsToCollapse = selectedIds;
		} else {
			idsToCollapse = CanvasUtils.getElementIds(getNodes());
		}

		for (String id : idsToCollapse) {
			if (collapsedNodes.keySet().contains(id)) {
				String message;

				if (idsToCollapse == selectedIds) {
					message = "Some of the selected " + naming.nodes() + " are already collapsed.";
				} else {
					message = "Some of the " + naming.nodes()
							+ " are already collapsed. You have to clear all collapsed "
							+ naming.nodes() + " before.";
				}

				JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Map<Object, Set<V>> nodesByProperty = CanvasUtils.openCollapseByPropertyDialog(this,
				nodeSchema.getMap().keySet(), idsToCollapse, nodeSaveMap);
		Set<String> newCollapsedIds = new LinkedHashSet<>();

		if (nodesByProperty.isEmpty()) {
			return;
		}

		for (Map.Entry<Object, Set<V>> entry : nodesByProperty.entrySet()) {
			String newId = KnimeUtils.createNewValue(entry.getKey().toString(),
					nodeSaveMap.keySet());

			collapsedNodes.put(newId, CanvasUtils.getElementIds(entry.getValue()));
			newCollapsedIds.add(newId);
		}

		applyChanges();
		fireCollapsedNodesChanged();
		setSelectedNodeIds(newCollapsedIds);
	}

	@Override
	public void clearCollapsedNodesItemClicked() {
		if (JOptionPane.showConfirmDialog(this, "Do you really want to uncollapse all collapsed "
				+ naming.nodes() + "?", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			for (String id : collapsedNodes.keySet()) {
				nodeSaveMap.remove(id);
			}

			collapsedNodes.clear();
			applyChanges();
			fireCollapsedNodesChanged();
			viewer.getPickedVertexState().clear();
		}
	}

	@Override
	public void editingModeChanged() {
		GraphMouse<V, Edge<V>> graphMouse = createGraphMouse();

		graphMouse.setMode(optionsPanel.getEditingMode());

		viewer.setGraphMouse(graphMouse);
	}

	@Override
	public void showLegendChanged() {
		viewer.repaint();
	}

	@Override
	public void joinEdgesChanged() {
		applyChanges();
		fireEdgeJoinChanged();
	}

	@Override
	public void skipEdgelessNodesChanged() {
		applyChanges();
		fireSkipEdgelessChanged();
	}

	@Override
	public void showEdgesInMetaNodeChanged() {
		applyChanges();
		fireShowEdgesInMetaNodeChanged();
	}

	@Override
	public void fontChanged() {
		viewer.getRenderContext().setVertexFontTransformer(
				new FontTransformer<V>(optionsPanel.getFontSize(), optionsPanel.isFontBold()));
		viewer.getRenderContext()
				.setEdgeFontTransformer(
						new FontTransformer<Edge<V>>(optionsPanel.getFontSize(), optionsPanel
								.isFontBold()));
		viewer.repaint();
	}

	@Override
	public void nodeSizeChanged() {
		applyChanges();
	}

	@Override
	public void arrowInMiddleChanged() {
		viewer.getRenderer()
				.getEdgeRenderer()
				.setEdgeArrowRenderingSupport(
						optionsPanel.isArrowInMiddle() ? new MiddleEdgeArrowRenderingSupport<>()
								: new BasicEdgeArrowRenderingSupport<>());
		viewer.repaint();
	}

	@Override
	public void labelChanged() {
		viewer.repaint();
	}

	@Override
	public VisualizationViewer<V, Edge<V>> getViewer() {
		return viewer;
	}

	@Override
	public VisualizationImageServer<V, Edge<V>> getVisualizationServer(boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = new VisualizationImageServer<>(
				viewer.getGraphLayout(), viewer.getSize());

		server.setBackground(Color.WHITE);
		server.setRenderContext(viewer.getRenderContext());
		server.setRenderer(viewer.getRenderer());
		server.addPostRenderPaintable(new PostPaintable(true));

		return server;
	}

	@Override
	public CanvasOptionsPanel getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public void setOptionsPanel(CanvasOptionsPanel optionsPanel) {
		if (this.optionsPanel != null) {
			remove(this.optionsPanel);
		}

		this.optionsPanel = optionsPanel;
		optionsPanel.addChangeListener(this);
		add(optionsPanel, BorderLayout.SOUTH);
		revalidate();
	}

	@Override
	public CanvasPopupMenu getPopupMenu() {
		return popup;
	}

	@Override
	public void setPopupMenu(CanvasPopupMenu popup) {
		this.popup = popup;
		popup.addClickListener(this);
		viewer.setComponentPopupMenu(popup);
	}

	@Override
	public void applyChanges() {
		Set<String> selectedNodeIds = getSelectedNodeIds();
		Set<String> selectedEdgeIds = getSelectedEdgeIds();

		applyNodeCollapse();
		applyInvisibility();
		applyJoinEdgesAndSkipEdgeless();
		applyHighlights();
		applyShowEdgesInMetaNode();
		viewer.getGraphLayout().setGraph(CanvasUtils.createGraph(nodes, edges));

		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
		viewer.repaint();
	}

	@Override
	public void applyNodeCollapse() {
		nodes.clear();
		edges.clear();

		Map<String, String> collapseTo = new LinkedHashMap<>();

		for (String to : collapsedNodes.keySet()) {
			for (String from : collapsedNodes.get(to)) {
				collapseTo.put(from, to);
			}
		}

		Map<String, V> nodesById = new LinkedHashMap<>();

		for (String id : CanvasUtils.getElementIds(allNodes)) {
			if (!collapseTo.keySet().contains(id)) {
				V newNode = nodeSaveMap.get(id);

				nodes.add(newNode);
				nodesById.put(id, newNode);
			}
		}

		Set<V> metaNodes = new LinkedHashSet<>();

		for (String newId : collapsedNodes.keySet()) {
			V newNode = nodeSaveMap.get(newId);

			if (newNode == null) {
				Set<V> nodes = CanvasUtils.getElementsById(nodeSaveMap, collapsedNodes.get(newId));

				newNode = createMetaNode(newId, nodes);
				nodeSaveMap.put(newId, newNode);
			}

			nodes.add(newNode);
			nodesById.put(newNode.getId(), newNode);
			metaNodes.add(newNode);
		}

		for (Edge<V> edge : allEdges) {
			V from = nodesById.get(edge.getFrom().getId());
			V to = nodesById.get(edge.getTo().getId());

			if (from == null) {
				from = nodesById.get(collapseTo.get(edge.getFrom().getId()));
			}

			if (to == null) {
				to = nodesById.get(collapseTo.get(edge.getTo().getId()));
			}

			Edge<V> newEdge = edgeSaveMap.get(edge.getId());

			if (!newEdge.getFrom().equals(from) || !newEdge.getTo().equals(to)) {
				newEdge = new Edge<>(newEdge.getId(), newEdge.getProperties(), from, to);
				newEdge.getProperties().put(edgeSchema.getFrom(), from.getId());
				newEdge.getProperties().put(edgeSchema.getTo(), to.getId());
				edgeSaveMap.put(newEdge.getId(), newEdge);
			}

			edges.add(newEdge);
		}
	}

	@Override
	public void applyInvisibility() {
		CanvasUtils.removeInvisibleElements(nodes, nodeHighlightConditions);
		CanvasUtils.removeInvisibleElements(edges, edgeHighlightConditions);
		CanvasUtils.removeNodelessEdges(edges, nodes);
	}

	@Override
	public void applyJoinEdgesAndSkipEdgeless() {
		joinMap.clear();

		if (isJoinEdges()) {
			joinMap.putAll(CanvasUtils.joinEdges(edges, edgeSchema,
					CanvasUtils.getElementIds(allEdges)));
			edges.clear();
			edges.addAll(joinMap.keySet());
		}

		if (isSkipEdgelessNodes()) {
			CanvasUtils.removeEdgelessNodes(nodes, edges);
		}
	}

	@Override
	public void applyHighlights() {
		CanvasUtils.applyNodeHighlights(viewer.getRenderContext(), nodes, nodeHighlightConditions,
				getNodeSize());
		CanvasUtils.applyEdgeHighlights(viewer.getRenderContext(), edges, edgeHighlightConditions);
	}

	@Override
	public void applyShowEdgesInMetaNode() {
		if (!isShowEdgesInMetaNode()) {
			for (Iterator<Edge<V>> iterator = edges.iterator(); iterator.hasNext();) {
				Edge<V> edge = iterator.next();

				if (edge.getFrom() == edge.getTo()
						&& collapsedNodes.containsKey(edge.getFrom().getId())) {
					iterator.remove();
				}
			}
		}
	}

	protected HighlightListDialog openNodeHighlightDialog() {
		nodeSchema.getPossibleValues().clear();
		nodeSchema.getPossibleValues().putAll(CanvasUtils.getPossibleValues(nodeSaveMap.values()));

		return new HighlightListDialog(this, nodeSchema, nodeHighlightConditions);
	}

	protected HighlightListDialog openEdgeHighlightDialog() {
		edgeSchema.getPossibleValues().clear();
		edgeSchema.getPossibleValues().putAll(CanvasUtils.getPossibleValues(edgeSaveMap.values()));

		HighlightListDialog dialog = new HighlightListDialog(this, edgeSchema,
				edgeHighlightConditions);

		dialog.addChecker(new EdgeHighlightChecker());

		return dialog;
	}

	protected GraphMouse<V, Edge<V>> createGraphMouse() {
		return new GraphMouse<>(new PickingPlugin(), 1.1);
	}

	protected ZoomingPaintable createZoomingPaintable() {
		return new ZoomingPaintable(this, 1.2);
	}

	protected abstract void applyTransform();

	protected abstract V createMetaNode(String id, Collection<V> nodes);

	private void fireNodeSelectionChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.nodeSelectionChanged(this);
		}
	}

	private void fireEdgeSelectionChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.edgeSelectionChanged(this);
		}
	}

	private void fireNodeHighlightingChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.nodeHighlightingChanged(this);
		}
	}

	private void fireEdgeHighlightingChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.edgeHighlightingChanged(this);
		}
	}

	private void fireEdgeJoinChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.edgeJoinChanged(this);
		}
	}

	private void fireSkipEdgelessChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.skipEdgelessChanged(this);
		}
	}

	private void fireShowEdgesInMetaNodeChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.showEdgesInMetaNodeChanged(this);
		}
	}

	private void fireCollapsedNodesChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.collapsedNodesChanged(this);
		}
	}

	protected class PickingPlugin extends PickingGraphMousePlugin<V, Edge<V>> {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				V node = viewer.getPickSupport().getVertex(viewer.getGraphLayout(), e.getX(),
						e.getY());
				Edge<V> edge = viewer.getPickSupport().getEdge(viewer.getGraphLayout(), e.getX(),
						e.getY());

				if (node != null) {
					SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(),
							node, nodeSchema);

					dialog.setVisible(true);
				} else if (edge != null) {
					SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(),
							edge, edgeSchema);

					dialog.setVisible(true);
				}
			}
		}
	}

	private class EdgeHighlightChecker implements HighlightConditionChecker {

		@Override
		public String findError(HighlightCondition condition) {
			String error = "The column \"" + edgeSchema.getId()
					+ "\" cannot be used with \"Invisible\" option as it is used as "
					+ naming.edge() + " ID";

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
					for (List<LogicalHighlightCondition> cc : logicalCondition.getConditions()) {
						for (LogicalHighlightCondition c : cc) {
							if (edgeSchema.getId().equals(c.getProperty())) {
								return error;
							}
						}
					}
				}

				if (valueCondition != null) {
					if (edgeSchema.getId().equals(valueCondition.getProperty())) {
						return error;
					}
				}
			}

			return null;
		}
	}

	private class PostPaintable implements Paintable {

		private boolean toImage;

		public PostPaintable(boolean toImage) {
			this.toImage = toImage;
		}

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics g) {
			if (getLabel() != null && !getLabel().isEmpty()) {
				paintLabel(g);
			}

			if (optionsPanel.isShowLegend()) {
				new CanvasLegend<>(Canvas.this, nodeHighlightConditions, nodes,
						edgeHighlightConditions, edges).paint(g, getCanvasSize().width,
						getCanvasSize().height, optionsPanel.getFontSize(),
						optionsPanel.isFontBold());
			}

			if (toImage) {
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, getCanvasSize().width - 1, getCanvasSize().height - 1);
			}
		}

		private void paintLabel(Graphics g) {
			int w = getCanvasSize().width;
			Font font = new Font("Default", Font.BOLD, 10);
			int fontHeight = g.getFontMetrics(font).getHeight();
			int fontAscent = g.getFontMetrics(font).getAscent();
			int dy = 2;

			int dx = 5;
			int sw = (int) font
					.getStringBounds(getLabel(), ((Graphics2D) g).getFontRenderContext())
					.getWidth();

			g.setColor(CanvasUtils.LEGEND_BACKGROUND);
			g.fillRect(w - sw - 2 * dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
			g.setColor(Color.BLACK);
			g.drawRect(w - sw - 2 * dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
			g.setFont(font);
			g.drawString(getLabel(), w - sw - dx, dy + fontAscent);
		}
	}
}
