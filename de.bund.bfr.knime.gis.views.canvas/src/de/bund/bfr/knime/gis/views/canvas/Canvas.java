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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import de.bund.bfr.knime.UI;
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
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeDrawTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeFillTransformer;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

public abstract class Canvas<V extends Node> extends JPanel implements
		ChangeListener, ActionListener, ItemListener, KeyListener {

	public static final String TRANSFORMING_MODE = "Transforming";
	public static final String PICKING_MODE = "Picking";

	private static final long serialVersionUID = 1L;

	private static final String EDITING_MODE = "Editing Mode";
	private static final String SHOW_LEGEND = "Show Legend";
	private static final String JOIN_EDGES = "Join Edges";
	private static final String SKIP_EDGELESS_NODES = "Skip Edgeless Nodes";

	private static final boolean DEFAULT_ALLOW_EDGES = true;
	private static final boolean DEFAULT_ALLOW_HIGHLIGHTING = true;
	private static final boolean DEFAULT_ALLOW_COLLAPSE = false;
	private static final String DEFAULT_MODE = TRANSFORMING_MODE;
	private static final boolean DEFAULT_SHOW_LEGEND = false;
	private static final boolean DEFAULT_JOIN_EDGES = false;
	private static final boolean DEFAULT_SKIP_EDGELESS_NODES = false;

	private static final int LEGEND_WIDTH = 30;
	private static final int LEGEND_HEIGHT = 12;
	private static final int LEGEND_DX = 10;
	private static final int LEGEND_DY = 3;
	private static final Font LEGEND_FONT = new Font("Default", 0, 12);
	private static final Font LEGEND_HEAD_FONT = new Font("Default", Font.BOLD,
			12);

	private VisualizationViewer<V, Edge<V>> viewer;
	private JPanel optionsPanel;

	private boolean allowEdges;
	private boolean allowHighlighting;
	private boolean allowCollapse;
	private double scaleX;
	private double scaleY;
	private double translationX;
	private double translationY;

	private JMenu nodeSelectionMenu;
	private JMenu edgeSelectionMenu;
	private JMenu nodeHighlightMenu;
	private JMenu edgeHighlightMenu;

	private JMenuItem resetLayoutItem;
	private JMenuItem saveAsItem;
	private JMenuItem selectConnectingItem;
	private JMenuItem clearSelectNodesItem;
	private JMenuItem clearSelectEdgesItem;
	private JMenuItem highlightNodesItem;
	private JMenuItem clearHighlightNodesItem;
	private JMenuItem selectHighlightedNodesItem;
	private JMenuItem highlightSelectedNodesItem;
	private JMenuItem highlightEdgesItem;
	private JMenuItem clearHighlightEdgesItem;
	private JMenuItem selectHighlightedEdgesItem;
	private JMenuItem highlightSelectedEdgesItem;
	private JMenuItem nodePropertiesItem;
	private JMenuItem edgePropertiesItem;
	private JMenuItem collapseToNodeItem;
	private JMenuItem expandFromNodeItem;

	private String editingMode;
	private JComboBox<String> modeBox;
	private boolean showLegend;
	private JCheckBox legendBox;
	private boolean joinEdges;
	private JCheckBox joinBox;
	private boolean skipEdgelessNodes;
	private JCheckBox skipBox;

	private List<CanvasListener> canvasListeners;

	private HighlightConditionList nodeHighlightConditions;
	private HighlightConditionList edgeHighlightConditions;

	private Map<String, Class<?>> nodeProperties;
	private Map<String, Class<?>> edgeProperties;
	private String nodeIdProperty;
	private String edgeIdProperty;
	private String edgeFromProperty;
	private String edgeToProperty;

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
		this.allowEdges = DEFAULT_ALLOW_EDGES;
		this.allowHighlighting = DEFAULT_ALLOW_HIGHLIGHTING;
		this.allowCollapse = DEFAULT_ALLOW_COLLAPSE;
		scaleX = Double.NaN;
		scaleY = Double.NaN;
		translationX = Double.NaN;
		translationY = Double.NaN;
		editingMode = DEFAULT_MODE;
		showLegend = DEFAULT_SHOW_LEGEND;
		joinEdges = DEFAULT_JOIN_EDGES;
		skipEdgelessNodes = DEFAULT_SKIP_EDGELESS_NODES;

		viewer = new VisualizationViewer<V, Edge<V>>(
				new StaticLayout<V, Edge<V>>(
						new DirectedSparseMultigraph<V, Edge<V>>()));
		viewer.setBackground(Color.WHITE);
		viewer.addKeyListener(this);
		viewer.getPickedVertexState().addItemListener(this);
		viewer.getPickedEdgeState().addItemListener(this);
		viewer.getRenderContext().setVertexFillPaintTransformer(
				new NodeFillTransformer<V>(viewer,
						new LinkedHashMap<V, List<Double>>(),
						new ArrayList<Color>()));
		viewer.getRenderContext().setEdgeDrawPaintTransformer(
				new EdgeDrawTransformer<Edge<V>>(viewer,
						new LinkedHashMap<Edge<V>, List<Double>>(),
						new ArrayList<Color>()));
		((MutableAffineTransformer) viewer.getRenderContext()
				.getMultiLayerTransformer().getTransformer(Layer.LAYOUT))
				.addChangeListener(this);
		viewer.addPostRenderPaintable(new Paintable() {

			@Override
			public boolean useTransform() {
				return false;
			}

			@Override
			public void paint(Graphics g) {
				if (showLegend) {
					paintLegend(g);
				}
			}
		});

		canvasListeners = new ArrayList<CanvasListener>();

		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();

		optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		modeBox = new JComboBox<String>(new String[] { TRANSFORMING_MODE,
				PICKING_MODE });
		modeBox.setSelectedItem(editingMode);
		modeBox.addActionListener(this);
		legendBox = new JCheckBox("Activate");
		legendBox.setSelected(showLegend);
		legendBox.addActionListener(this);
		joinBox = new JCheckBox("Activate");
		joinBox.setSelected(joinEdges);
		joinBox.addActionListener(this);
		skipBox = new JCheckBox("Activate");
		skipBox.setSelected(skipEdgelessNodes);
		skipBox.addActionListener(this);

		setLayout(new BorderLayout());
		add(viewer, BorderLayout.CENTER);
		add(UI.createWestPanel(optionsPanel), BorderLayout.SOUTH);
		addOptionsItem(EDITING_MODE, modeBox);
		addOptionsItem(SHOW_LEGEND, legendBox);
		addOptionsItem(JOIN_EDGES, joinBox);
		addOptionsItem(SKIP_EDGELESS_NODES, skipBox);
		createPopupMenuItems();
		applyPopupMenu();
		applyMouseModel();
	}

	public void addCanvasListener(CanvasListener listener) {
		canvasListeners.add(listener);
	}

	public void removeCanvasListener(CanvasListener listener) {
		canvasListeners.remove(listener);
	}

	public Dimension getCanvasSize() {
		return viewer.getSize();
	}

	public void setCanvasSize(Dimension canvasSize) {
		viewer.setPreferredSize(canvasSize);
	}

	public boolean isAllowEdges() {
		return allowEdges;
	}

	public void setAllowEdges(boolean allowEdges) {
		this.allowEdges = allowEdges;
		applyPopupMenu();
		setOptionsItemVisible(JOIN_EDGES, allowEdges);
		setOptionsItemVisible(SKIP_EDGELESS_NODES, allowEdges);
	}

	public boolean isAllowHighlighting() {
		return allowHighlighting;
	}

	public void setAllowHighlighting(boolean allowHighlighting) {
		this.allowHighlighting = allowHighlighting;
		applyPopupMenu();
	}

	public boolean isAllowCollapse() {
		return allowCollapse;
	}

	public void setAllowCollapse(boolean allowCollapse) {
		this.allowCollapse = allowCollapse;
		applyPopupMenu();
	}

	public String getEditingMode() {
		return editingMode;
	}

	public void setEditingMode(String editingMode) {
		this.editingMode = editingMode;
		modeBox.setSelectedItem(editingMode);
		applyMouseModel();
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
		legendBox.setSelected(showLegend);
		viewer.repaint();
	}

	public boolean isJoinEdges() {
		return joinEdges;
	}

	public void setJoinEdges(boolean joinEdges) {
		this.joinEdges = joinEdges;
		joinBox.setSelected(joinEdges);
		applyChanges();
		fireEdgeJoinChanged();
	}

	public boolean isSkipEdgelessNodes() {
		return skipEdgelessNodes;
	}

	public void setSkipEdgelessNodes(boolean skipEdgelessNodes) {
		this.skipEdgelessNodes = skipEdgelessNodes;
		skipBox.setSelected(skipEdgelessNodes);
		applyChanges();
		fireSkipEdgelessChanged();
	}

	public Collection<V> getVisibleNodes() {
		return getViewer().getGraphLayout().getGraph().getVertices();
	}

	public Collection<Edge<V>> getVisibleEdges() {
		return getViewer().getGraphLayout().getGraph().getEdges();
	}

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

	public void setSelectedNodes(Set<V> selectedNodes) {
		for (V node : viewer.getGraphLayout().getGraph().getVertices()) {
			if (selectedNodes.contains(node)) {
				viewer.getPickedVertexState().pick(node, true);
			} else {
				viewer.getPickedVertexState().pick(node, false);
			}
		}
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

	public Set<V> getSelectedNodes() {
		return viewer.getPickedVertexState().getPicked();
	}

	public Set<Edge<V>> getSelectedEdges() {
		return viewer.getPickedEdgeState().getPicked();
	}

	public Set<String> getSelectedNodeIds() {
		return CanvasUtilities.getElementIds(viewer.getPickedVertexState()
				.getPicked());
	}

	public void setSelectedNodeIds(Set<String> selectedNodeIds) {
		setSelectedNodes(CanvasUtilities.getElementsById(viewer
				.getGraphLayout().getGraph().getVertices(), selectedNodeIds));
	}

	public Set<String> getSelectedEdgeIds() {
		return CanvasUtilities.getElementIds(viewer.getPickedEdgeState()
				.getPicked());
	}

	public void setSelectedEdgeIds(Set<String> selectedEdgeIds) {
		setSelectedEdges(CanvasUtilities.getElementsById(viewer
				.getGraphLayout().getGraph().getEdges(), selectedEdgeIds));
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

	public BufferedImage getImage() {
		VisualizationImageServer<V, Edge<V>> server = createVisualizationServer(false);
		BufferedImage img = new BufferedImage(viewer.getSize().width,
				viewer.getSize().height, BufferedImage.TYPE_INT_RGB);

		server.paint(img.createGraphics());

		return img;
	}

	public SVGDocument getSvgDocument() {
		VisualizationImageServer<V, Edge<V>> server = createVisualizationServer(true);
		SVGDOMImplementation domImpl = new SVGDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		svgGenerator.setSVGCanvasSize(viewer.getSize());
		server.paint(svgGenerator);
		svgGenerator.finalize();
		document.replaceChild(svgGenerator.getRoot(),
				document.getDocumentElement());

		return (SVGDocument) document;
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
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == modeBox) {
			editingMode = (String) modeBox.getSelectedItem();
			applyMouseModel();
		} else if (e.getSource() == legendBox) {
			showLegend = legendBox.isSelected();
			viewer.repaint();
		} else if (e.getSource() == joinBox) {
			joinEdges = joinBox.isSelected();
			applyChanges();
			fireEdgeJoinChanged();
		} else if (e.getSource() == skipBox) {
			skipEdgelessNodes = skipBox.isSelected();
			applyChanges();
			fireSkipEdgelessChanged();
		} else if (e.getSource() == saveAsItem) {
			ImageFileChooser chooser = new ImageFileChooser();

			if (chooser.showSaveDialog(saveAsItem) == JFileChooser.APPROVE_OPTION) {
				if (chooser.getFileFormat() == ImageFileChooser.PNG_FORMAT) {
					try {
						saveAsPng(chooser.getImageFile());
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(saveAsItem,
								"Error saving png file", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} else if (chooser.getFileFormat() == ImageFileChooser.SVG_FORMAT) {
					try {
						saveAsSvg(chooser.getImageFile());
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(saveAsItem,
								"Error saving svg file", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} else if (e.getSource() == selectConnectingItem) {
			Map<V, List<Edge<V>>> connectingEdges = new LinkedHashMap<V, List<Edge<V>>>();

			for (V node : viewer.getGraphLayout().getGraph().getVertices()) {
				connectingEdges.put(node, new ArrayList<Edge<V>>());
			}

			for (Edge<V> edge : viewer.getGraphLayout().getGraph().getEdges()) {
				connectingEdges.get(edge.getFrom()).add(edge);
				connectingEdges.get(edge.getTo()).add(edge);
			}

			for (V node : getViewer().getPickedVertexState().getPicked()) {
				for (Edge<V> edge : connectingEdges.get(node)) {
					if (!getViewer().getGraphLayout().getGraph()
							.containsEdge(edge)) {
						continue;
					}

					V otherNode = null;

					if (edge.getFrom() == node) {
						otherNode = edge.getTo();
					} else if (edge.getTo() == node) {
						otherNode = edge.getFrom();
					}

					if (getViewer().getPickedVertexState().isPicked(otherNode)) {
						getViewer().getPickedEdgeState().pick(edge, true);
					}
				}
			}
		} else if (e.getSource() == selectHighlightedNodesItem) {
			HighlightSelectionDialog dialog = new HighlightSelectionDialog(
					this, nodeHighlightConditions.getConditions());

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setSelectedNodes(CanvasUtilities.getHighlightedElements(viewer
						.getGraphLayout().getGraph().getVertices(),
						dialog.getHighlightConditions()));
			}
		} else if (e.getSource() == selectHighlightedEdgesItem) {
			HighlightSelectionDialog dialog = new HighlightSelectionDialog(
					this, edgeHighlightConditions.getConditions());

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setSelectedEdges(CanvasUtilities.getHighlightedElements(viewer
						.getGraphLayout().getGraph().getEdges(),
						dialog.getHighlightConditions()));
			}
		} else if (e.getSource() == resetLayoutItem) {
			resetLayout();
		} else if (e.getSource() == clearSelectNodesItem) {
			viewer.getPickedVertexState().clear();
		} else if (e.getSource() == clearSelectEdgesItem) {
			viewer.getPickedEdgeState().clear();
		} else if (e.getSource() == highlightNodesItem) {
			HighlightListDialog dialog = openNodeHighlightDialog();

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setNodeHighlightConditions(dialog.getHighlightConditions());
			}
		} else if (e.getSource() == clearHighlightNodesItem) {
			if (JOptionPane
					.showConfirmDialog(
							clearHighlightNodesItem,
							"Do you really want to remove all node highlight conditions?",
							"Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				setNodeHighlightConditions(new HighlightConditionList());
			}
		} else if (e.getSource() == highlightEdgesItem) {
			HighlightListDialog dialog = openEdgeHighlightDialog();

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setEdgeHighlightConditions(dialog.getHighlightConditions());
			}
		} else if (e.getSource() == clearHighlightEdgesItem) {
			if (JOptionPane
					.showConfirmDialog(
							clearHighlightNodesItem,
							"Do you really want to remove all edge highlight conditions?",
							"Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				setEdgeHighlightConditions(new HighlightConditionList());
			}
		} else if (e.getSource() == nodePropertiesItem) {
			showNodeProperties();
		} else if (e.getSource() == edgePropertiesItem) {
			showEdgeProperties();
		} else if (e.getSource() == highlightSelectedNodesItem) {
			HighlightListDialog dialog = openNodeHighlightDialog();
			List<List<LogicalHighlightCondition>> conditions = new ArrayList<List<LogicalHighlightCondition>>();

			for (String id : getSelectedNodeIds()) {
				LogicalHighlightCondition c = new LogicalHighlightCondition(
						nodeIdProperty, LogicalHighlightCondition.EQUAL_TYPE,
						id);

				conditions.add(Arrays.asList(c));
			}

			AndOrHighlightCondition condition = new AndOrHighlightCondition(
					conditions, null, Color.RED, false, false, null);

			dialog.setAutoAddCondition(condition);
			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setNodeHighlightConditions(dialog.getHighlightConditions());
			}
		} else if (e.getSource() == highlightSelectedEdgesItem) {
			HighlightListDialog dialog = openEdgeHighlightDialog();
			List<List<LogicalHighlightCondition>> conditions = new ArrayList<List<LogicalHighlightCondition>>();

			for (String id : getSelectedEdgeIds()) {
				LogicalHighlightCondition c = new LogicalHighlightCondition(
						edgeIdProperty, LogicalHighlightCondition.EQUAL_TYPE,
						id);

				conditions.add(Arrays.asList(c));
			}

			AndOrHighlightCondition condition = new AndOrHighlightCondition(
					conditions, null, Color.RED, false, false, null);

			dialog.setAutoAddCondition(condition);
			dialog.setVisible(true);

			if (dialog.isApproved()) {
				setEdgeHighlightConditions(dialog.getHighlightConditions());
			}
		} else if (e.getSource() == collapseToNodeItem) {
			collapseToNode();
		} else if (e.getSource() == expandFromNodeItem) {
			expandFromNode();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getItem() instanceof Node) {
			if (viewer.getPickedVertexState().getPicked().isEmpty()) {
				nodeSelectionMenu.setEnabled(false);
			} else {
				nodeSelectionMenu.setEnabled(true);
			}

			fireNodeSelectionChanged();
		} else if (e.getItem() instanceof Edge) {
			if (viewer.getPickedEdgeState().getPicked().isEmpty()) {
				edgeSelectionMenu.setEnabled(false);
			} else {
				edgeSelectionMenu.setEnabled(true);
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

	protected VisualizationViewer<V, Edge<V>> getViewer() {
		return viewer;
	}

	protected Point2D toGraphCoordinates(int x, int y) {
		return new Point2D.Double((x - translationX) / scaleX,
				(y - translationY) / scaleY);
	}

	protected Point toWindowsCoordinates(double x, double y) {
		return new Point((int) (x * scaleX + translationX),
				(int) (y * scaleY + translationY));
	}

	protected void addOptionsItem(String name, JComponent... components) {
		JPanel panel = new JPanel();
		TitledBorder border = BorderFactory.createTitledBorder(name);

		panel.setBorder(border);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		for (JComponent c : components) {
			panel.add(UI.createCenterPanel(c));
		}

		if (components.length == 1) {
			int titleWidth = border.getMinimumSize(components[0]).width;

			components[0].setPreferredSize(new Dimension(Math.max(
					components[0].getPreferredSize().width, titleWidth),
					components[0].getPreferredSize().height));
		}
		optionsPanel.add(panel);
		optionsPanel.add(Box.createHorizontalStrut(5));
	}

	protected void setOptionsItemVisible(String name, boolean visible) {
		int index = -1;

		for (int i = 0; i < optionsPanel.getComponentCount(); i++) {
			if (optionsPanel.getComponent(i) instanceof JPanel) {
				JPanel c = (JPanel) optionsPanel.getComponent(i);

				if (c.getBorder() instanceof TitledBorder) {
					TitledBorder b = (TitledBorder) c.getBorder();

					if (b.getTitle().equals(name)) {
						index = i;
						break;
					}
				}
			}
		}

		if (index == -1) {
			return;
		}

		JPanel panel = (JPanel) optionsPanel.getComponent(index);

		if (!visible && panel.getPreferredSize().width != 0) {
			panel.setPreferredSize(new Dimension(0, 0));
			optionsPanel.remove(index + 1);
			optionsPanel.revalidate();
		} else if (visible && panel.getPreferredSize().width == 0) {
			panel.setPreferredSize(panel.getMinimumSize());
			optionsPanel.add(Box.createHorizontalStrut(5), index + 1);
			optionsPanel.revalidate();
		}
	}

	protected VisualizationImageServer<V, Edge<V>> createVisualizationServer(
			final boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = new VisualizationImageServer<V, Edge<V>>(
				viewer.getGraphLayout(), viewer.getSize());

		server.setBackground(Color.WHITE);
		server.setRenderContext(viewer.getRenderContext());

		return server;
	}

	protected void showNodeProperties() {
		Set<V> picked = new LinkedHashSet<V>(viewer.getPickedVertexState()
				.getPicked());

		picked.retainAll(getVisibleNodes());

		PropertiesDialog dialog = new PropertiesDialog(this, picked,
				nodeProperties);

		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	protected void showEdgeProperties() {
		Set<Edge<V>> picked = new LinkedHashSet<Edge<V>>(viewer
				.getPickedEdgeState().getPicked());

		picked.retainAll(getVisibleEdges());

		PropertiesDialog dialog = new PropertiesDialog(this, picked,
				edgeProperties);

		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	protected abstract void resetLayout();

	protected abstract HighlightListDialog openNodeHighlightDialog();

	protected abstract HighlightListDialog openEdgeHighlightDialog();

	protected abstract void applyChanges();

	protected abstract void applyTransform();

	protected abstract void collapseToNode();

	protected abstract void expandFromNode();

	protected abstract GraphMouse<V, Edge<V>> createMouseModel();

	private void paintLegend(Graphics g) {
		FontRenderContext fontRenderContext = ((Graphics2D) g)
				.getFontRenderContext();
		Map<String, Color> nodeLegend = new LinkedHashMap<String, Color>();
		Map<String, Color> edgeLegend = new LinkedHashMap<String, Color>();
		int maxNodeWidth = 0;
		int maxEdgeWidth = 0;

		for (HighlightCondition condition : nodeHighlightConditions
				.getConditions()) {
			String name = condition.getName();
			Color color = condition.getColor();

			if (name != null && !name.isEmpty() && color != null) {
				nodeLegend.put(name, color);
				maxNodeWidth = Math.max(maxNodeWidth, (int) LEGEND_FONT
						.getStringBounds(name, fontRenderContext).getWidth());
			}
		}

		for (HighlightCondition condition : edgeHighlightConditions
				.getConditions()) {
			String name = condition.getName();
			Color color = condition.getColor();

			if (name != null && !name.isEmpty() && color != null) {
				edgeLegend.put(name, color);
				maxEdgeWidth = Math.max(maxEdgeWidth, (int) LEGEND_FONT
						.getStringBounds(name, fontRenderContext).getWidth());
			}
		}

		int y0 = getCanvasSize().height
				- (Math.max(nodeLegend.size(), edgeLegend.size()) + 1)
				* (LEGEND_HEIGHT + LEGEND_DY) - LEGEND_DY;

		g.setColor(Color.BLACK);
		g.setFont(LEGEND_HEAD_FONT);
		g.drawString("Nodes", LEGEND_DX, y0 + LEGEND_FONT.getSize());

		if (allowEdges) {
			g.drawString("Edges", 3 * LEGEND_DX + maxNodeWidth + LEGEND_WIDTH,
					y0 + LEGEND_FONT.getSize());
		}

		int yNode = y0 + LEGEND_HEIGHT + LEGEND_DY;
		int yEdge = y0 + LEGEND_HEIGHT + LEGEND_DY;

		g.setFont(LEGEND_FONT);

		for (String name : nodeLegend.keySet()) {
			g.setColor(Color.BLACK);
			g.drawString(name, LEGEND_DX, yNode + LEGEND_FONT.getSize());
			g.setColor(nodeLegend.get(name));
			g.fillRect(2 * LEGEND_DX + maxNodeWidth, yNode, LEGEND_WIDTH,
					LEGEND_HEIGHT);

			yNode += LEGEND_HEIGHT + LEGEND_DY;
		}

		for (String name : edgeLegend.keySet()) {
			g.setColor(Color.BLACK);
			g.drawString(name, 3 * LEGEND_DX + maxNodeWidth + LEGEND_WIDTH,
					yEdge + LEGEND_FONT.getSize());
			g.setColor(edgeLegend.get(name));
			g.fillRect(5 * LEGEND_DX + maxNodeWidth + maxEdgeWidth
					+ LEGEND_WIDTH, yEdge, LEGEND_WIDTH, LEGEND_HEIGHT);

			yEdge += LEGEND_HEIGHT + LEGEND_DY;
		}

		// TODO finish legend
	}

	private void applyPopupMenu() {
		JPopupMenu popup = new JPopupMenu();

		popup.add(resetLayoutItem);
		popup.add(saveAsItem);

		if (allowEdges) {
			nodeSelectionMenu.add(nodePropertiesItem);
			nodeSelectionMenu.add(clearSelectNodesItem);
			nodeSelectionMenu.add(selectConnectingItem);
			nodeSelectionMenu.add(highlightSelectedNodesItem);

			if (allowCollapse) {
				nodeSelectionMenu.add(new JSeparator());
				nodeSelectionMenu.add(collapseToNodeItem);
				nodeSelectionMenu.add(expandFromNodeItem);
			}

			edgeSelectionMenu.add(edgePropertiesItem);
			edgeSelectionMenu.add(clearSelectEdgesItem);
			edgeSelectionMenu.add(highlightSelectedEdgesItem);

			popup.add(new JSeparator());
			popup.add(nodeSelectionMenu);
			popup.add(edgeSelectionMenu);

			if (allowHighlighting) {
				nodeHighlightMenu.add(highlightNodesItem);
				nodeHighlightMenu.add(clearHighlightNodesItem);
				nodeHighlightMenu.add(selectHighlightedNodesItem);

				edgeHighlightMenu.add(highlightEdgesItem);
				edgeHighlightMenu.add(clearHighlightEdgesItem);
				edgeHighlightMenu.add(selectHighlightedEdgesItem);

				popup.add(nodeHighlightMenu);
				popup.add(edgeHighlightMenu);
			}
		} else {
			nodeSelectionMenu.add(nodePropertiesItem);
			nodeSelectionMenu.add(clearSelectNodesItem);
			nodeSelectionMenu.add(highlightSelectedNodesItem);

			popup.add(new JSeparator());
			popup.add(nodeSelectionMenu);

			if (allowHighlighting) {
				nodeHighlightMenu.add(highlightNodesItem);
				nodeHighlightMenu.add(clearHighlightNodesItem);
				nodeHighlightMenu.add(selectHighlightedNodesItem);

				popup.add(nodeHighlightMenu);
			}
		}

		viewer.setComponentPopupMenu(popup);
	}

	private void applyMouseModel() {
		GraphMouse<V, Edge<V>> mouseModel = createMouseModel();

		if (editingMode.equals(TRANSFORMING_MODE)) {
			mouseModel.setMode(Mode.TRANSFORMING);
		} else {
			mouseModel.setMode(Mode.PICKING);
		}

		viewer.setGraphMouse(mouseModel);
	}

	private void saveAsPng(File file) throws IOException {
		VisualizationImageServer<V, Edge<V>> server = createVisualizationServer(false);
		BufferedImage img = new BufferedImage(viewer.getWidth(),
				viewer.getHeight(), BufferedImage.TYPE_INT_RGB);

		server.paint(img.getGraphics());
		ImageIO.write(img, "png", file);
	}

	private void saveAsSvg(File file) throws IOException {
		VisualizationImageServer<V, Edge<V>> server = createVisualizationServer(true);
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		Writer outsvg = new OutputStreamWriter(new FileOutputStream(file),
				"UTF-8");

		svgGenerator.setSVGCanvasSize(new Dimension(viewer.getWidth(), viewer
				.getHeight()));
		server.paint(svgGenerator);
		svgGenerator.stream(outsvg, true);
		outsvg.close();
	}

	private void createPopupMenuItems() {
		nodeSelectionMenu = new JMenu("Node Selection");
		nodeSelectionMenu.setEnabled(false);
		edgeSelectionMenu = new JMenu("Edge Selection");
		edgeSelectionMenu.setEnabled(false);
		nodeHighlightMenu = new JMenu("Node Highlighting");
		edgeHighlightMenu = new JMenu("Edge Highlighting");

		resetLayoutItem = new JMenuItem("Reset Layout");
		resetLayoutItem.addActionListener(this);
		saveAsItem = new JMenuItem("Save As ...");
		saveAsItem.addActionListener(this);

		clearSelectNodesItem = new JMenuItem("Clear");
		clearSelectNodesItem.addActionListener(this);
		nodePropertiesItem = new JMenuItem("Show Properties");
		nodePropertiesItem.addActionListener(this);

		edgePropertiesItem = new JMenuItem("Show Properties");
		edgePropertiesItem.addActionListener(this);
		clearSelectEdgesItem = new JMenuItem("Clear");
		clearSelectEdgesItem.addActionListener(this);
		selectConnectingItem = new JMenuItem("Select Connections");
		selectConnectingItem.addActionListener(this);

		highlightNodesItem = new JMenuItem("Edit");
		highlightNodesItem.addActionListener(this);
		clearHighlightNodesItem = new JMenuItem("Clear");
		clearHighlightNodesItem.addActionListener(this);
		selectHighlightedNodesItem = new JMenuItem("Select Highlighted");
		selectHighlightedNodesItem.addActionListener(this);
		highlightSelectedNodesItem = new JMenuItem("Highlight Selected");
		highlightSelectedNodesItem.addActionListener(this);

		highlightEdgesItem = new JMenuItem("Edit");
		highlightEdgesItem.addActionListener(this);
		clearHighlightEdgesItem = new JMenuItem("Clear");
		clearHighlightEdgesItem.addActionListener(this);
		selectHighlightedEdgesItem = new JMenuItem("Select Highlighted");
		selectHighlightedEdgesItem.addActionListener(this);
		highlightSelectedEdgesItem = new JMenuItem("Highlight Selected");
		highlightSelectedEdgesItem.addActionListener(this);

		collapseToNodeItem = new JMenuItem("Collapse to Meta Node");
		collapseToNodeItem.addActionListener(this);
		expandFromNodeItem = new JMenuItem("Expand from Meta Node");
		expandFromNodeItem.addActionListener(this);
	}

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
}
