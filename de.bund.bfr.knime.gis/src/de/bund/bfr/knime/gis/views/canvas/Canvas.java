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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
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
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeDrawTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.FontTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.MiddleEdgeArrowRenderingSupport;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeFillTransformer;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

public abstract class Canvas<V extends Node> extends JPanel implements
		ActionListener, ChangeListener, ItemListener, KeyListener,
		MouseListener, CanvasPopupMenu.ClickListener,
		CanvasOptionsPanel.ChangeListener {

	private static final long serialVersionUID = 1L;
	private static final String COPY = "Copy";
	private static final String PASTE = "Paste";

	private static final String DEFAULT_NODE_NAME = "Node";
	private static final String DEFAULT_EDGE_NAME = "Edge";
	private static final String DEFAULT_NODES_NAME = "Nodes";
	private static final String DEFAULT_EDGES_NAME = "Edges";

	private VisualizationViewer<V, Edge<V>> viewer;
	private CanvasOptionsPanel optionsPanel;
	private CanvasPopupMenu popup;

	private double scaleX;
	private double scaleY;
	private double translationX;
	private double translationY;

	private List<CanvasListener> canvasListeners;

	private HighlightConditionList nodeHighlightConditions;
	private HighlightConditionList edgeHighlightConditions;

	private Map<String, Class<?>> nodeProperties;
	private Map<String, Class<?>> edgeProperties;
	private String nodeIdProperty;
	private String edgeIdProperty;
	private String edgeFromProperty;
	private String edgeToProperty;

	private String nodeName;
	private String edgeName;
	private String nodesName;
	private String edgesName;

	public Canvas(Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty) {
		this.nodeProperties = nodeProperties;
		this.edgeProperties = edgeProperties;
		this.nodeIdProperty = nodeIdProperty;
		this.edgeIdProperty = edgeIdProperty;
		this.edgeFromProperty = edgeFromProperty;
		this.edgeToProperty = edgeToProperty;
		nodeName = DEFAULT_NODE_NAME;
		edgeName = DEFAULT_EDGE_NAME;
		nodesName = DEFAULT_NODES_NAME;
		edgesName = DEFAULT_EDGES_NAME;
		scaleX = Double.NaN;
		scaleY = Double.NaN;
		translationX = Double.NaN;
		translationY = Double.NaN;
		canvasListeners = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();

		viewer = new VisualizationViewer<>(new StaticLayout<>(
				new DirectedSparseMultigraph<V, Edge<V>>()));
		viewer.setBackground(Color.WHITE);
		viewer.addKeyListener(this);
		viewer.addMouseListener(this);
		viewer.getPickedVertexState().addItemListener(this);
		viewer.getPickedEdgeState().addItemListener(this);
		viewer.getRenderContext().setVertexFillPaintTransformer(
				new NodeFillTransformer<>(viewer,
						new LinkedHashMap<V, List<Double>>(),
						new ArrayList<Color>()));
		viewer.getRenderContext().setEdgeDrawPaintTransformer(
				new EdgeDrawTransformer<>(viewer,
						new LinkedHashMap<Edge<V>, List<Double>>(),
						new ArrayList<Color>()));
		((MutableAffineTransformer) viewer.getRenderContext()
				.getMultiLayerTransformer().getTransformer(Layer.LAYOUT))
				.addChangeListener(this);
		viewer.addPostRenderPaintable(new PostPaintable(false));
		viewer.setGraphMouse(createMouseModel(Mode.TRANSFORMING));
		viewer.registerKeyboardAction(this, COPY, KeyStroke.getKeyStroke(
				KeyEvent.VK_C, ActionEvent.CTRL_MASK, false),
				JComponent.WHEN_FOCUSED);
		viewer.registerKeyboardAction(this, PASTE, KeyStroke.getKeyStroke(
				KeyEvent.VK_V, ActionEvent.CTRL_MASK, false),
				JComponent.WHEN_FOCUSED);

		setLayout(new BorderLayout());
		add(viewer, BorderLayout.CENTER);
	}

	public void addCanvasListener(CanvasListener listener) {
		canvasListeners.add(listener);
	}

	public void removeCanvasListener(CanvasListener listener) {
		canvasListeners.remove(listener);
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
		applyNameChanges();
	}

	public String getEdgeName() {
		return edgeName;
	}

	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
		applyNameChanges();
	}

	public String getNodesName() {
		return nodesName;
	}

	public void setNodesName(String nodesName) {
		this.nodesName = nodesName;
		applyNameChanges();
	}

	public String getEdgesName() {
		return edgesName;
	}

	public void setEdgesName(String edgesName) {
		this.edgesName = edgesName;
		applyNameChanges();
	}

	public Dimension getCanvasSize() {
		return viewer.getSize();
	}

	public void setCanvasSize(Dimension canvasSize) {
		viewer.setPreferredSize(canvasSize);
	}

	public Mode getEditingMode() {
		return optionsPanel.getEditingMode();
	}

	public void setEditingMode(Mode editingMode) {
		optionsPanel.setEditingMode(editingMode);
	}

	public boolean isShowLegend() {
		return optionsPanel.isShowLegend();
	}

	public void setShowLegend(boolean showLegend) {
		optionsPanel.setShowLegend(showLegend);
	}

	public boolean isJoinEdges() {
		return optionsPanel.isJoinEdges();
	}

	public void setJoinEdges(boolean joinEdges) {
		optionsPanel.setJoinEdges(joinEdges);
	}

	public boolean isSkipEdgelessNodes() {
		return optionsPanel.isSkipEdgelessNodes();
	}

	public void setSkipEdgelessNodes(boolean skipEdgelessNodes) {
		optionsPanel.setSkipEdgelessNodes(skipEdgelessNodes);
	}

	public int getFontSize() {
		return optionsPanel.getFontSize();
	}

	public void setFontSize(int fontSize) {
		optionsPanel.setFontSize(fontSize);
	}

	public boolean isFontBold() {
		return optionsPanel.isFontBold();
	}

	public void setFontBold(boolean fontBold) {
		optionsPanel.setFontBold(fontBold);
	}

	public int getNodeSize() {
		return optionsPanel.getNodeSize();
	}

	public void setNodeSize(int nodeSize) {
		optionsPanel.setNodeSize(nodeSize);
	}

	public boolean isArrowInMiddle() {
		return optionsPanel.isArrowInMiddle();
	}

	public void setArrowInMiddle(boolean arrowInMiddle) {
		optionsPanel.setArrowInMiddle(arrowInMiddle);
	}

	public int getBorderAlpha() {
		return optionsPanel.getBorderAlpha();
	}

	public void setBorderAlpha(int borderAlpha) {
		optionsPanel.setBorderAlpha(borderAlpha);
	}

	public abstract Set<V> getNodes();

	public abstract Set<Edge<V>> getEdges();

	public Map<String, Class<?>> getNodeProperties() {
		return nodeProperties;
	}

	public Map<String, Class<?>> getEdgeProperties() {
		return edgeProperties;
	}

	public String getNodeIdProperty() {
		return nodeIdProperty;
	}

	public String getEdgeIdProperty() {
		return edgeIdProperty;
	}

	public String getEdgeFromProperty() {
		return edgeFromProperty;
	}

	public String getEdgeToProperty() {
		return edgeToProperty;
	}

	public Set<V> getSelectedNodes() {
		return viewer.getPickedVertexState().getPicked();
	}

	public void setSelectedNodes(Set<V> selectedNodes) {
		for (V node : viewer.getGraphLayout().getGraph().getVertices()) {
			if (selectedNodes.contains(node)) {
				viewer.getPickedVertexState().pick(node, true);
			} else {
				viewer.getPickedVertexState().pick(node, false);
			}
		}
	}

	public Set<Edge<V>> getSelectedEdges() {
		return viewer.getPickedEdgeState().getPicked();
	}

	public void setSelectedEdges(Set<Edge<V>> selectedEdges) {
		for (Edge<V> edge : viewer.getGraphLayout().getGraph().getEdges()) {
			if (selectedEdges.contains(edge)) {
				viewer.getPickedEdgeState().pick(edge, true);
			} else {
				viewer.getPickedEdgeState().pick(edge, false);
			}
		}
	}

	public Set<String> getSelectedNodeIds() {
		return CanvasUtils.getElementIds(viewer.getPickedVertexState()
				.getPicked());
	}

	public void setSelectedNodeIds(Set<String> selectedNodeIds) {
		setSelectedNodes(CanvasUtils.getElementsById(viewer.getGraphLayout()
				.getGraph().getVertices(), selectedNodeIds));
	}

	public Set<String> getSelectedEdgeIds() {
		return CanvasUtils.getElementIds(viewer.getPickedEdgeState()
				.getPicked());
	}

	public void setSelectedEdgeIds(Set<String> selectedEdgeIds) {
		setSelectedEdges(CanvasUtils.getElementsById(viewer.getGraphLayout()
				.getGraph().getEdges(), selectedEdgeIds));
	}

	public HighlightConditionList getNodeHighlightConditions() {
		return nodeHighlightConditions;
	}

	public void setNodeHighlightConditions(
			HighlightConditionList nodeHighlightConditions) {
		this.nodeHighlightConditions = nodeHighlightConditions;
		applyChanges();
		fireNodeHighlightingChanged();
	}

	public HighlightConditionList getEdgeHighlightConditions() {
		return edgeHighlightConditions;
	}

	public void setEdgeHighlightConditions(
			HighlightConditionList edgeHighlightConditions) {
		this.edgeHighlightConditions = edgeHighlightConditions;
		applyChanges();
		fireEdgeHighlightingChanged();
	}

	public double getScaleX() {
		return scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public double getTranslationX() {
		return translationX;
	}

	public double getTranslationY() {
		return translationY;
	}

	public void setTransform(double scaleX, double scaleY, double translationX,
			double translationY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.translationX = translationX;
		this.translationY = translationY;

		((MutableAffineTransformer) viewer.getRenderContext()
				.getMultiLayerTransformer().getTransformer(Layer.LAYOUT))
				.setTransform(new AffineTransform(scaleX, 0, 0, scaleY,
						translationX, translationY));
		applyTransform();
		viewer.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(COPY)) {
			List<String> selected = new ArrayList<>(getSelectedNodeIds());
			StringSelection stsel = new StringSelection(
					KnimeUtils.listToString(selected));

			Toolkit.getDefaultToolkit().getSystemClipboard()
					.setContents(stsel, stsel);
			JOptionPane.showMessageDialog(this, nodeName
					+ " selection has been copied to clipboard", "Clipboard",
					JOptionPane.INFORMATION_MESSAGE);
		} else if (e.getActionCommand().equals(PASTE)) {
			Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
			String selected = null;

			try {
				selected = (String) system.getContents(this).getTransferData(
						DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			setSelectedNodeIds(new LinkedHashSet<>(
					KnimeUtils.stringToList(selected)));
			JOptionPane.showMessageDialog(this, nodeName
					+ " selection has been pasted from clipboard", "Clipboard",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		AffineTransform transform = ((MutableAffineTransformer) viewer
				.getRenderContext().getMultiLayerTransformer()
				.getTransformer(Layer.LAYOUT)).getTransform();

		if (transform.getScaleX() != 0.0 && transform.getScaleY() != 0.0) {
			scaleX = transform.getScaleX();
			scaleY = transform.getScaleY();
			translationX = transform.getTranslateX();
			translationY = transform.getTranslateY();
		} else {
			scaleX = Double.NaN;
			scaleY = Double.NaN;
			translationX = Double.NaN;
			translationY = Double.NaN;
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
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Point2D center = viewer.getRenderContext().getMultiLayerTransformer()
				.inverseTransform(Layer.VIEW, viewer.getCenter());
		MutableTransformer transformer = viewer.getRenderContext()
				.getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

		if (e.getKeyCode() == KeyEvent.VK_UP) {
			transformer.scale(1 / 1.1f, 1 / 1.1f, center);
			viewer.repaint();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			transformer.scale(1.1f, 1.1f, center);
			viewer.repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
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
				viewer.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				break;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		viewer.requestFocus();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void saveAsItemClicked() {
		ImageFileChooser chooser = new ImageFileChooser();

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			if (chooser.getFileFormat() == ImageFileChooser.Format.PNG_FORMAT) {
				try {
					VisualizationImageServer<V, Edge<V>> server = getVisualizationServer(false);
					BufferedImage img = new BufferedImage(viewer.getWidth(),
							viewer.getHeight(), BufferedImage.TYPE_INT_RGB);

					server.paint(img.getGraphics());
					ImageIO.write(img, "png", chooser.getImageFile());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this,
							"Error saving png file", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else if (chooser.getFileFormat() == ImageFileChooser.Format.SVG_FORMAT) {
				try {
					VisualizationImageServer<V, Edge<V>> server = getVisualizationServer(true);
					DOMImplementation domImpl = GenericDOMImplementation
							.getDOMImplementation();
					Document document = domImpl.createDocument(null, "svg",
							null);
					SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
					Writer outsvg = new OutputStreamWriter(
							new FileOutputStream(chooser.getImageFile()),
							StandardCharsets.UTF_8);

					svgGenerator.setSVGCanvasSize(new Dimension(viewer
							.getWidth(), viewer.getHeight()));
					server.paint(svgGenerator);
					svgGenerator.stream(outsvg, true);
					outsvg.close();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this,
							"Error saving svg file", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public void selectConnectionsItemClicked() {
		for (Edge<V> edge : getEdges()) {
			if (getSelectedNodes().contains(edge.getFrom())
					&& getSelectedNodes().contains(edge.getTo())) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public void selectIncomingItemClicked() {
		for (Edge<V> edge : getEdges()) {
			if (getSelectedNodes().contains(edge.getTo())) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public void selectOutgoingItemClicked() {
		for (Edge<V> edge : getEdges()) {
			if (getSelectedNodes().contains(edge.getFrom())) {
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
			LogicalHighlightCondition c = new LogicalHighlightCondition(
					nodeIdProperty, LogicalHighlightCondition.EQUAL_TYPE, id);

			conditions.add(Arrays.asList(c));
		}

		AndOrHighlightCondition condition = new AndOrHighlightCondition(
				conditions, null, false, Color.RED, false, false, null);

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
			LogicalHighlightCondition c = new LogicalHighlightCondition(
					edgeIdProperty, LogicalHighlightCondition.EQUAL_TYPE, id);

			conditions.add(Arrays.asList(c));
		}

		AndOrHighlightCondition condition = new AndOrHighlightCondition(
				conditions, null, false, Color.RED, false, false, null);

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
		if (JOptionPane.showConfirmDialog(this,
				"Do you really want to remove all " + nodeName.toLowerCase()
						+ " highlight conditions?", "Please Confirm",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			setNodeHighlightConditions(new HighlightConditionList());
		}
	}

	@Override
	public void clearHighlightedEdgesItemClicked() {
		if (JOptionPane.showConfirmDialog(this,
				"Do you really want to remove all " + edgeName.toLowerCase()
						+ " highlight conditions?", "Please Confirm",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			setEdgeHighlightConditions(new HighlightConditionList());
		}
	}

	@Override
	public void selectHighlightedNodesItemClicked() {
		HighlightSelectionDialog dialog = new HighlightSelectionDialog(this,
				nodeHighlightConditions.getConditions());

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedNodes(CanvasUtils.getHighlightedElements(getNodes(),
					dialog.getHighlightConditions()));
		}
	}

	@Override
	public void selectHighlightedEdgesItemClicked() {
		HighlightSelectionDialog dialog = new HighlightSelectionDialog(this,
				edgeHighlightConditions.getConditions());

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedEdges(CanvasUtils.getHighlightedElements(getEdges(),
					dialog.getHighlightConditions()));
		}

	}

	@Override
	public void selectNodesItemClicked() {
		HighlightDialog dialog = new HighlightDialog(this, nodeProperties,
				false, false, false, false, false, false, null, null);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedNodes(CanvasUtils.getHighlightedElements(getNodes(),
					Arrays.asList(dialog.getHighlightCondition())));
		}
	}

	@Override
	public void selectEdgesItemClicked() {
		HighlightDialog dialog = new HighlightDialog(this, edgeProperties,
				false, false, false, false, false, false, null, null);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedEdges(CanvasUtils.getHighlightedElements(getEdges(),
					Arrays.asList(dialog.getHighlightCondition())));
		}
	}

	@Override
	public void nodePropertiesItemClicked() {
		Set<V> picked = new LinkedHashSet<>(getSelectedNodes());

		picked.retainAll(getNodes());

		PropertiesDialog<V> dialog = PropertiesDialog.createNodeDialog(this,
				picked, nodeProperties, true,
				new LinkedHashSet<>(Arrays.asList(nodeIdProperty)));

		dialog.setVisible(true);
	}

	@Override
	public void edgePropertiesItemClicked() {
		Set<Edge<V>> picked = new LinkedHashSet<>(getSelectedEdges());

		picked.retainAll(getEdges());

		PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(
				this,
				picked,
				edgeProperties,
				true,
				new LinkedHashSet<>(Arrays.asList(edgeIdProperty,
						edgeFromProperty, edgeToProperty)));

		dialog.setVisible(true);
	}

	@Override
	public void edgeAllPropertiesItemClicked() {
		Set<Edge<V>> picked = new LinkedHashSet<>(getSelectedEdges());

		picked.retainAll(getEdges());

		Set<Edge<V>> allPicked = new LinkedHashSet<>();

		if (!getJoinMap().isEmpty()) {
			for (Edge<V> p : picked) {
				if (getJoinMap().containsKey(p)) {
					allPicked.addAll(getJoinMap().get(p));
				}
			}
		} else {
			allPicked.addAll(picked);
		}

		PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(
				this,
				allPicked,
				edgeProperties,
				false,
				new LinkedHashSet<>(Arrays.asList(edgeIdProperty,
						edgeFromProperty, edgeToProperty)));

		dialog.setVisible(true);
	}

	@Override
	public void editingModeChanged() {
		viewer.setGraphMouse(createMouseModel(optionsPanel.getEditingMode()));
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
	public void fontChanged() {
		viewer.getRenderContext().setVertexFontTransformer(
				new FontTransformer<V>(optionsPanel.getFontSize(), optionsPanel
						.isFontBold()));
		viewer.getRenderContext().setEdgeFontTransformer(
				new FontTransformer<Edge<V>>(optionsPanel.getFontSize(),
						optionsPanel.isFontBold()));
		viewer.repaint();
	}

	@Override
	public void nodeSizeChanged() {
		applyChanges();
	}

	@Override
	public void arrowInMiddleChanged() {
		if (optionsPanel.isArrowInMiddle()) {
			viewer.getRenderer()
					.getEdgeRenderer()
					.setEdgeArrowRenderingSupport(
							new MiddleEdgeArrowRenderingSupport<>());
		} else {
			viewer.getRenderer()
					.getEdgeRenderer()
					.setEdgeArrowRenderingSupport(
							new BasicEdgeArrowRenderingSupport<>());
		}

		viewer.repaint();
	}

	public VisualizationViewer<V, Edge<V>> getViewer() {
		return viewer;
	}

	public VisualizationImageServer<V, Edge<V>> getVisualizationServer(
			final boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = new VisualizationImageServer<>(
				viewer.getGraphLayout(), viewer.getSize());

		server.setBackground(Color.WHITE);
		server.setRenderContext(viewer.getRenderContext());
		server.setRenderer(viewer.getRenderer());
		server.addPostRenderPaintable(new PostPaintable(true));

		return server;
	}

	protected CanvasOptionsPanel getOptionsPanel() {
		return optionsPanel;
	}

	protected void setOptionsPanel(CanvasOptionsPanel optionsPanel) {
		if (this.optionsPanel != null) {
			remove(this.optionsPanel);
		}

		this.optionsPanel = optionsPanel;
		optionsPanel.addChangeListener(this);
		add(optionsPanel, BorderLayout.SOUTH);
		revalidate();
	}

	protected CanvasPopupMenu getPopupMenu() {
		return popup;
	}

	protected void setPopupMenu(CanvasPopupMenu popup) {
		this.popup = popup;
		popup.addClickListener(this);
		viewer.setComponentPopupMenu(popup);
	}

	protected Point2D toGraphCoordinates(int x, int y) {
		return new Point2D.Double((x - translationX) / scaleX,
				(y - translationY) / scaleY);
	}

	protected Point toWindowsCoordinates(double x, double y) {
		return new Point((int) (x * scaleX + translationX),
				(int) (y * scaleY + translationY));
	}

	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, nodeProperties,
				nodeHighlightConditions);
	}

	protected HighlightListDialog openEdgeHighlightDialog() {
		HighlightListDialog dialog = new HighlightListDialog(this,
				edgeProperties, edgeHighlightConditions);

		dialog.addChecker(new EdgeHighlightChecker());

		return dialog;
	}

	protected abstract void applyNameChanges();

	protected abstract void applyChanges();

	protected abstract void applyTransform();

	protected abstract GraphMouse<V, Edge<V>> createMouseModel(Mode editingMode);

	protected abstract Map<Edge<V>, Set<Edge<V>>> getJoinMap();

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

	private class EdgeHighlightChecker implements HighlightConditionChecker {

		@Override
		public String findError(HighlightCondition condition) {
			String error = "The column \""
					+ edgeIdProperty
					+ "\" cannot be used with \"Invisible\" option as it is used as "
					+ edgeName.toLowerCase() + " ID";

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
							if (edgeIdProperty.equals(c.getProperty())) {
								return error;
							}
						}
					}
				}

				if (valueCondition != null) {
					if (edgeIdProperty.equals(valueCondition.getProperty())) {
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
			if (optionsPanel.isShowLegend()) {
				new CanvasLegend<>(Canvas.this, nodeHighlightConditions,
						getNodes(), edgeHighlightConditions, getEdges()).paint(
						g, getCanvasSize().width, getCanvasSize().height,
						optionsPanel.getFontSize(), optionsPanel.isFontBold());
			}

			if (toImage) {
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, getCanvasSize().width - 1,
						getCanvasSize().height - 1);
			}
		}
	}
}
