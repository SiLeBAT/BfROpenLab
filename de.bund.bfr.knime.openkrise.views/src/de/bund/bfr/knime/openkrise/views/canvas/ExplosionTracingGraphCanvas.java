package de.bund.bfr.knime.openkrise.views.canvas;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

import org.apache.commons.collections15.Transformer;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.jung.BetterGraphMouse;
import de.bund.bfr.jung.BetterPickingGraphMousePlugin;
import de.bund.bfr.jung.JungListener;
import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.jung.layout.FRLayout;
import de.bund.bfr.jung.layout.LayoutType;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
//import de.bund.bfr.knime.gis.views.canvas.ExplosionCanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.openkrise.common.Delivery;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import java.util.logging.Logger;

public class ExplosionTracingGraphCanvas extends TracingGraphCanvas implements IExplosionCanvas<GraphNode> {

	private static Logger logger =  Logger.getLogger("de.bund.bfr");
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String metaNodeId;
	private Set<GraphNode> boundaryNodes; 
	private Set<GraphNode> nonBoundaryNodes;
	private Set<GraphNode> hiddenNodes; 
	private Set<Edge<GraphNode>> hiddenEdges; 
	
	private SetMultimap<String, String> boundaryNodesToInnerNodesMap;
	
	private Map<String,Set<String>> allCollapsedNodes;
	
	private BufferedImage image;
	private Polygon boundaryArea;
	
	public static final double BOUNDARY_MARGIN = 0.2;
	
	//private Map<String, Set<String>> allCollapsedNodes;

	public ExplosionTracingGraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties, Map<String, Delivery> deliveries, boolean lotBased, String metaNodeId, Set<String> containedNodes) {
		super(nodes, edges, nodeProperties, edgeProperties, deliveries, lotBased, false);
		
		logger.finest("entered");
		//this.image = null;
		this.metaNodeId = metaNodeId;
		//this.boundaryNodes = this.nodes.stream().filter(n -> !containedNodes.contains(n.getId())).collect(Collectors.toSet());
		this.nonBoundaryNodes = this.nodes.stream().filter(n -> containedNodes.contains(n.getId())).collect(Collectors.toSet());
        
		this.hiddenNodes = new HashSet<>();
		this.hiddenEdges = new HashSet<>();
		this.boundaryNodes = new HashSet<>();
		
		ExplosionCanvasUtils.initBoundaryAndHiddenNodes(
				this.nodes, this.edges, this.nonBoundaryNodes, this.boundaryNodes, this.hiddenNodes, this.hiddenEdges);
		
		this.edges = Sets.difference(this.edges, this.hiddenEdges).stream().collect(Collectors.toSet());
		this.boundaryNodesToInnerNodesMap = ExplosionCanvasUtils.createBoundaryNodesToInnerNodesMap(this.nonBoundaryNodes, this.boundaryNodes, this.edges);
		
		this.getViewer().addPreRenderPaintable(new PrePaintable(false));
		this.getViewer().addPostRenderPaintable(
				new ExplosionCanvasUtils.LabelPaintable(
						this.getViewer(),
						metaNodeId,
						()-> Stream.of(getListeners(ExplosionListener.class)).forEach(l->l.closeExplosionViewRequested(this))));
		
//		this.getViewer().addPreRenderPaintable(new PrePaintable(false));
//		this.getViewer().addPostRenderPaintable(
//				new ExplosionCanvasUtils.LabelPaintable(
//						this.getViewer(),
//						strKey,
//						()-> Stream.of(getListeners(ExplosionListener.class)).forEach(l->l.closeExplosionViewRequested(this))));
//		
//		this.boundaryNodes.forEach(n -> this.getViewer().getGraphLayout().lock(n, true));
		
//		this.addTracingListener(new TracingListener() {
//
//			@Override
//			public void nodePropertiesChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void edgePropertiesChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void nodeWeightsChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void edgeWeightsChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void nodeCrossContaminationsChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void edgeCrossContaminationsChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void nodeKillContaminationsChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void edgeKillContaminationsChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void observedNodesChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void observedEdgesChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void enforceTemporalOrderChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void showForwardChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void dateSettingsChanged(ITracingCanvas<?> source) {
//				// TODO Auto-generated method stub
//				ExplosionTracingGraphCanvas.this.repositionBoundaryNodes();
//			}
//			
//		});
		logger.finest("leaving");
	}
	
//    private void initBoundaryAndHiddenNodes() {
//		
//		this.hiddenNodes = new HashSet<>();
//		this.hiddenEdges = new HashSet<>();
//		this.boundaryNodes = new HashSet<>();
//		
//		for(Edge<GraphNode> edge : this.edges) {
//			if(this.nonBoundaryNodes.contains(edge.getFrom())) {
//				
//				if(!this.nonBoundaryNodes.contains(edge.getTo())) this.boundaryNodes.add(edge.getTo());
//				
//			} else if(this.nonBoundaryNodes.contains(edge.getTo())) {
//				
//				this.boundaryNodes.add(edge.getFrom());
//				
//			} else {
//				
//				this.hiddenEdges.add(edge);
//			}
//		}
//		
//		this.hiddenNodes = Sets.difference(Sets.difference(this.nodes, this.nonBoundaryNodes), this.boundaryNodes);
//
//	}
	
//	@Override
//	public void applyChanges() {
//		super.applyChanges();
//		this.repositionBoundaryNodes();
//	}
	
	@Override
	public void applyNodeCollapse() {
		logger.finest("entered");
		super.applyNodeCollapse();
		
		// check whether this is ok
		//this.boundaryNodes = Sets.difference(this.nodes, this.nonBoundaryNodes);
		
		Layout<GraphNode, Edge<GraphNode>> layout = this.getViewer().getGraphLayout();
		
		Set<GraphNode> boundaryNodes = CanvasUtils.getElementsById(this.nodeSaveMap,this.boundaryNodesToInnerNodesMap.keySet());
		
		boundaryNodes.forEach(n -> layout.lock(n, true));
		logger.finest("leaving");
	}
	
	@Override
	protected Rectangle2D getLayoutBounds() {
		return ExplosionCanvasUtils.getInnerBoundaryRect(this);
	}
	
	@Override
	public void resetNodesAndEdges() {
		logger.finest("entered");
		super.resetNodesAndEdges();
		
		if(this.hiddenNodes != null) this.nodes.removeAll(this.hiddenNodes); //= new HashSet<>(Sets.difference(this.nodes, this.hiddenNodes));
		if(this.hiddenEdges != null) this.edges.removeAll(this.hiddenEdges); //= new HashSet<>(Sets.difference(this.edges, this.hiddenEdges));
		
		logger.finest("leaving");
	}
	
	@Override
	public VisualizationImageServer<GraphNode, Edge<GraphNode>> getVisualizationServer(boolean toSvg) {
		VisualizationImageServer<GraphNode, Edge<GraphNode>> server = super.getVisualizationServer(toSvg);
		
		server.addPreRenderPaintable(new PrePaintable(toSvg));
        server.addPostRenderPaintable(new ExplosionCanvasUtils.LabelPaintable(this.getViewer(),this.metaNodeId));
		return server;
	}
	
	@Override
	public void nodeMovementFinished() {
		this.placeBoundaryNodes(false);
		//this.getViewer().addPreRenderPaintable(paintable);
		//this.getViewer().repaint();
		super.nodeMovementFinished();
		//call(l -> l.nodePositionsChanged(this));
	}
	
	private void call(Consumer<CanvasListener> action) {
		Stream.of(getListeners(CanvasListener.class)).forEach(action);
	}
	
	@Override
	public void nodeAllPropertiesItemClicked() {
		ExplosionCanvasUtils.showAllPropertiesDialog(this, this.allCollapsedNodes, this.nodeSaveMap);
	}
	
	@Override
	public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
		logger.finest("entered");
		
		// remove old meta nodes which are obsolete
		Sets.difference(this.collapsedNodes.keySet(), collapsedNodes.keySet()).forEach(id -> nodeSaveMap.remove(id));
				
		Map<String, Set<String>> newFilteredCollapsedNodes = ExplosionCanvasUtils.filterCollapsedNodeAccordingToExplosion(
				collapsedNodes, this.metaNodeId,
				CanvasUtils.getElementIds(Sets.union(this.nonBoundaryNodes, this.boundaryNodes)));
				
		// create meta nodes which do not belong to the explosion view
		// this is required because the properties of the meta nodes have to be maintained
		Sets.difference(collapsedNodes.keySet(), newFilteredCollapsedNodes.keySet()).forEach(metaId -> {
			
			if(!nodeSaveMap.containsKey(metaId)) {
				
//				Set<String> nodeIds = collapsedNodes.get(metaId);
//				Set<GraphNode> nodes = CanvasUtils.getElementsById(this.nodeSaveMap, nodeIds); //this.getNodePositions(CanvasUtils.getElementsById(this.nodeSaveMap, nodeIds))
//				Map<String, Point2D> positions = this.getNodePositions(nodes);
//				positions = positions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new Point2D.Double(Double.NaN, Double.NaN)));
//				this.setNodePositions(positions);
				
				nodeSaveMap.put(metaId, createMetaNode(metaId, CanvasUtils.getElementsById(nodeSaveMap, collapsedNodes.get(metaId))));
			}
			
		});
				
				
		for(String metaId : newFilteredCollapsedNodes.keySet()) {
			Set<String> nodeIds = newFilteredCollapsedNodes.get(metaId);
			
			if(Sets.intersection(nodeIds, this.boundaryNodesToInnerNodesMap.keySet()).isEmpty()) {
				nodeIds.forEach(id -> this.boundaryNodesToInnerNodesMap.putAll(metaId, this.boundaryNodesToInnerNodesMap.get(id)));
			}
		}

		this.allCollapsedNodes = collapsedNodes;
		
		this.collapsedNodes = newFilteredCollapsedNodes;
				
		this.applyChanges();
				
		if(this.boundaryNodes != null) this.placeBoundaryNodes(false);
		        
		call(l -> l.collapsedNodesChanged(this));
		logger.finest("leaving");
	}
	
	@Override
	protected Set<GraphNode> getLayoutableNodes() { 
		return this.nonBoundaryNodes; 
	}
	
	@Override
	protected void applyLayout(LayoutType layoutType, Set<GraphNode> nodesForLayout, boolean showProgressDialog) {
		super.applyLayout(layoutType, nodesForLayout, showProgressDialog, false);
		//Sets.difference(this.boundaryNodes, this.nodes).forEach(n -> viewer.getGraphLayout().setLocation(n,  new Point2D.Double(Double.NaN, Double.NaN)));
		// viewer.getGraphLayout().setLocation(node, transform.apply(pos.getX(), pos.getY()));
		this.placeBoundaryNodes(false);
		//Rectangle viewBounds = this.getViewer().getBounds();
		//if this.getViewer().getBounds() 
		Stream.of(getListeners(CanvasListener.class)).forEach(l -> l.layoutProcessFinished(this));
	}
	
//	@Override
//	public void initLayout() {
//		logger.finest("entered");
//		//this.getTracing().applyChanges(ignoreTimeWindow);
//		logger.finest("leaving");
//	}
	
//	@Override
//	public void applyInvisibility() {
//		ExplosionCanvasUtils.removeOuterNodes(nodes, this.nonBoundaryNodes, this.boundaryNodes);
//		super.applyInvisibility();
//	}
	
//	@Override 
//	protected Map<String, Point2D> getNodePositions(Collection<GraphNode> nodes) {
//		logger.finest("entered");
//		Map<String, Point2D> nodePos = super.getNodePositions(nodes.stream().filter(n-> this.nonBoundaryNodes.contains(n) || this.boundaryNodes.contains(n)).collect(Collectors.toList()));
//		logger.finest("leaving");
//		return nodePos;
//	}
	
	@Override
	public void transformFinished() {
		this.placeBoundaryNodes(true);
		this.flushImage();
		super.transformFinished();
	}
	
//	@Override
//	public void setNodePositions(Map<String, Point2D> nodePositions) {
//		logger.finest("entered");
//		List<GraphNode> nodesWithoutPos = new ArrayList<>();
//
//		
//			
//		logger.finest("leaving");
//	}
	
	public void placeBoundaryNodes(boolean onlyUpdateBoundaryAreaPosition) {
		logger.finest("entered");
		if(this.isPerformTracing()) {
		
			if (!((this.boundaryNodes == null) || this.boundaryNodes.isEmpty())) {
				
				//Map<String, Point2D> positions = this.getNodePositions(boundaryNodes)
				
				Map<String, Point2D> positions = this.getNodePositions(this.nonBoundaryNodes);
				
				if(positions == null) return;
				
				Rectangle2D defaultBounds = ExplosionCanvasUtils.getInnerBoundaryRect(this);
				
				Set<String> nodesWithoutPosition = positions.entrySet().stream().filter(e -> Double.isNaN(e.getValue().getX()) || Double.isNaN(e.getValue().getY())).map(e -> e.getKey()).collect(Collectors.toSet());
				
				nodesWithoutPosition.forEach(nodeId -> positions.put(nodeId, new Point2D.Double(defaultBounds.getX(), defaultBounds.getY())));
				
				
				//Set<String> nonBoundaryNodeIds = CanvasUtils.getElementIds(this.nonBoundaryNodes);
			
				Rectangle2D bounds = PointUtils.getBounds(positions.values());
				
//				Rectangle2D bounds = PointUtils.getBounds(
//						positions.entrySet().stream()
//						.filter(e -> nonBoundaryNodeIds.contains(e.getKey())).map(e -> e.getValue()).collect(Collectors.toList()));
			
				if(bounds.isEmpty()) {
					boundaryArea = null;
					logger.finest("leaving bounds.isEmpty()=TRUE");
					return;
				}
			
				Polygon newBoundaryArea = ExplosionCanvasUtils.createBoundaryArea(bounds);

				boolean boundaryAreaChanged = boundaryArea==null || !newBoundaryArea.equals(boundaryArea);
			
				boundaryArea = newBoundaryArea;
			
				Rectangle2D rect = ExplosionCanvasUtils.getAreaRect(this.boundaryArea);
				double w = ExplosionCanvasUtils.getAreaBorderWidth(this.boundaryArea);
			
				if(!onlyUpdateBoundaryAreaPosition) {
					
					Set<String> updateSet = boundaryNodesToInnerNodesMap.keySet().stream().collect(Collectors.toSet());
					
					collapsedNodes.entrySet().forEach(e -> updateSet.removeAll(e.getValue()));
					
					for(String boundaryNodeKey: updateSet) {
//						List<Point2D> pointList = new ArrayList<>();
//						
//						for(String innerNodeKey: this.boundaryNodesToInnerNodesMap.get(boundaryNodeKey)) {
//							pointList.add(positions.get(innerNodeKey));
//						}
//						
//						Point2D pCenter = PointUtils.getCenter(pointList);
						Point2D pCenter = PointUtils.getCenter(
								this.boundaryNodesToInnerNodesMap.get(boundaryNodeKey).stream().map(innerNodeKey -> positions.get(innerNodeKey))
								.collect(Collectors.toList()));
						
						Point2D pBR = getClosestPointOnRect(pCenter, rect);
						 
						positions.put(boundaryNodeKey, pBR);
					}
					
				
				// ExplosionCanvasUtils.updateBoundaryNodePositionsByRemovingVisualConflicts(positions, rect, this.edges, 2 * refNodeSize, this.boundaryNodes);
	//			ExplosionCanvasUtils.updateBoundaryNodePositionsByRemovingVisualConflicts(positions, rect, this.edges, w, this.boundaryNodes);
	//			
	//			// the boundary positions were only set for the visual nodes so far
	//			// but the position of the boundary meta nodes will be overwritten by 
	//			// the position of the center of their contained nodes ->
	//			// the position of the contained nodes is set to the position of their meta node
	//			Sets.intersection(this.collapsedNodes.keySet(), 
	//					          nodeRefPoints.keySet().stream().map(n -> n.getId()).collect(Collectors.toSet())).forEach(metaKey -> {
	//					        	  Point2D p = positions.get(metaKey);
	//					        	  this.collapsedNodes.get(metaKey).forEach(k -> positions.put(k, p));
	//					          });
					//boundaryNodesToInnerNodesMap.asMap().entrySet().stream().filter(e -> updateSet.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<String>(e.getValue())));
					
		            ExplosionCanvasUtils.updateBoundaryNodePositionsByRemovingVisualConflicts(
		            		positions, rect, 
		            		boundaryNodesToInnerNodesMap.asMap().entrySet().stream().filter(e -> updateSet.contains(e.getKey()))
		            		.collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<String>(e.getValue()))),
		            		w);
					
					// the boundary positions were only set for the visual nodes so far
					// but the position of the boundary meta nodes will be overwritten by 
					// the position of the center of their contained nodes ->
					// the position of the contained nodes is set to the position of their meta node
		            Sets.intersection(collapsedNodes.keySet(), updateSet).forEach(metaKey -> {
					        	  Point2D p = positions.get(metaKey);
					        	  collapsedNodes.get(metaKey).forEach(k -> positions.put(k, p));
					          });
		
//		            Sets.intersection(collapsedNodes.keySet(), updateSet).forEach(metaKey -> {
//			        	  Point2D p = positions.get(metaKey);
//			        	  collapsedNodes.get(metaKey).forEach(k -> positions.put(k, p));
//			          });
					
					this.setNodePositions(positions);
				}
				
				if(boundaryAreaChanged) this.flushImage();
			}
		}
		logger.finest("leaving");
	}
		
	private static Point2D getClosestPointOnRect(Point2D pointInRect, Rectangle2D rect) {
		double dx1 = Math.abs(pointInRect.getX() - rect.getMinX());
		double dx2 = Math.abs(pointInRect.getX() - rect.getMaxX());
		double dy1 = Math.abs(pointInRect.getY() - rect.getMinY());
		double dy2 = Math.abs(pointInRect.getY() - rect.getMaxY());
		double min = Collections.min(Arrays.asList(dx1, dx2, dy1, dy2));

		if (dx1 == min) {
			return new Point2D.Double(rect.getMinX(), pointInRect.getY());
		} else if (dx2 == min) {
			return new Point2D.Double(rect.getMaxX(), pointInRect.getY());
		} else if (dy1 == min) {
			return new Point2D.Double(pointInRect.getX(), rect.getMinY());
		} else if (dy2 == min) {
			return new Point2D.Double(pointInRect.getX(), rect.getMaxY());
		}

		throw new RuntimeException("This should not happen");
	}
	
	protected void flushImage() {
		if (image != null) {
			image.flush();
			image = null;
		}
	}
	
//	@Override
//	public void openExplosionViewItemClicked() {
//		Set<String> selectedNodeIds = getSelectedNodeIds();
//		
//		// exactly one node must be selected
//		if(selectedNodeIds==null || selectedNodeIds.isEmpty() || selectedNodeIds.size()!=1) return;
//		// this node has to be a metanode
//		String selectedNodeId = (String) selectedNodeIds.toArray()[0]; //.iterator().next();
//		if(!this.allCollapsedNodes.keySet().contains(selectedNodeId)) return;
//		
//		call(l -> l.openExplosionViewRequested(this, selectedNodeId));
//	}
	
	
	
	
	private void paintGraph(Graphics2D g, boolean toSvg) {
		//super.paintGis(g, toSvg, onWhiteBackground);
		//logger.finest("entered toSvg=" + (toSvg?"true":"false"));
		if (this.boundaryArea != null) {
			ExplosionCanvasUtils.paintBoundaryArea(g, getCanvasSize().width, getCanvasSize().height,
					transform.apply(this.boundaryArea));
		}
		//logger.finest("leaving");
	}
	
	private void paintGraphImage(Graphics2D g) {
		//logger.finest("entered");
		int width = getCanvasSize().width;
		int height = getCanvasSize().height;

		if (image == null || image.getWidth() != width || image.getHeight() != height) {
			flushImage();
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			this.paintGraph(image.createGraphics(), false);
		}

		g.drawImage(image, 0, 0, null);
		//logger.finest("leaving");
	}
	
	@Override
	public Map<String, Point2D> getNodePositions() {
//		updatePositionsOfCollapsedNodes();
//		Collection<GraphNode> col =  nodeSaveMap.values();
//		col = nodeSaveMap.values().stream().filter(n -> !collapsedNodes.containsKey(n.getId()))
//				.collect(Collectors.toList());
//		Map<String, Point2D> res = getNodePositions(col);
		return getNodePositions(this.nonBoundaryNodes);
//		return nodeSaveMap.values().stream().filter(n -> !collapsedNodes.containsKey(n.getId()))
//				.collect(Collectors.toList()));
	}
	
//	@Override
//	public void initLayout() {
//		logger.finest("entered");
//		if (!this.nonBoundaryNodes.isEmpty()) {
//			this.applyLayout(LayoutType.ISOM_LAYOUT, this.nonBoundaryNodes, false);
//		}
//		logger.finest("leaving");
//	}
	
	private class PrePaintable implements Paintable {
		
		private boolean toSvg;

		public PrePaintable(boolean toSvg) {
			this.toSvg = toSvg;
		}

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics g) {
			//logger.finest("entered toSvg=" + (toSvg?"true":"false"));
			if (toSvg) {
				ExplosionTracingGraphCanvas.this.paintGraph((Graphics2D) g, true);
			} else {
				ExplosionTracingGraphCanvas.this.paintGraphImage((Graphics2D) g);
			}
			//logger.finest("leaving");
		}
	}
	
//	public class LabelPaintable implements Paintable, MouseMotionListener, MouseListener {
//
//		private VisualizationViewer<?, ?> viewer;
//
//		private EventListenerList listeners;
//
//		private Rectangle closeRect;
//
//		private boolean closeFocused;
//		
//		//private boolean gbolLabelOnly;
//		private String gstrLabel;
//		private Runnable function;
//
//		public LabelPaintable(VisualizationViewer<?, ?> viewer, String strLabel, Runnable function) {
//			this.viewer = viewer;
//			this.gstrLabel = strLabel + " Explosion View";
//			this.function = function;
//			listeners = new EventListenerList();
//			closeRect = null;
//			closeFocused = false;
//			
//			if(!this.labelOnly()) {
//			  viewer.addMouseMotionListener(this);
//			  viewer.addMouseListener(this);
//			}
//		}
//		
//		public LabelPaintable(VisualizationViewer<?, ?> viewer, String strLabel) {
//			this(viewer, strLabel, null);
////			this.viewer = viewer;
////			this.gstrLabel = strLabel;
////			this.function = function;
////			//this.gbolLabelOnly = labelOnly;
////			listeners = new EventListenerList();
////			closeRect = null;
////			closeFocused = false;
////			
////			if(!this.labelOnly()) {
////			  viewer.addMouseMotionListener(this);
////			  viewer.addMouseListener(this);
////			}
//		}
//
//		public void addChangeListener(JungListener listener) {
//			listeners.add(JungListener.class, listener);
//		}
//
//		public void removeChangeListener(JungListener listener) {
//			listeners.remove(JungListener.class, listener);
//		}
//
//		@Override
//		public void paint(Graphics graphics) {
//			int w = viewer.getSize().width;
//			int h = viewer.getSize().height;
//			int size = 30;
//			int d = 10;
//			int lineD = 8;
//			int lineWidth = 2;
//
//			
//			//int xPlus = w - d - size;
//			//int yPlus = h - size - d - 2 * size;
//			//int xMinus = xPlus;
//			//int yMinus = h - size - d - size;
//						
//			Graphics2D g = (Graphics2D) graphics;
//			
//			Font font = new Font("Default", Font.BOLD, 10);
//			int fontHeight = g.getFontMetrics(font).getHeight();
//			int fontAscent = g.getFontMetrics(font).getAscent();
//			
//			int dy = 2;
//
//			int dx = 5;
//			
//			
//			int sw = (int) font.getStringBounds(this.gstrLabel, g.getFontRenderContext()).getWidth();
//			int sh = (int) font.getStringBounds(this.gstrLabel, g.getFontRenderContext()).getHeight();
//
//			Color currentColor = g.getColor();
//			Stroke currentStroke = g.getStroke();
//
//			if(!this.labelOnly()) this.closeRect = new Rectangle(w/2 + sw/2 + dx - fontAscent/2 - dx/2, dy ,fontAscent,fontAscent);
//			
//			g.setColor(ZoomingPaintable.BACKGROUND);
//			g.fillRect(w/2 - sw/2 - dx - (this.labelOnly()?0:this.closeRect.width/2+dx/2) , 0 , sw + 2*dx + (this.labelOnly()?0:this.closeRect.width+dx), fontHeight + 2*dy);
////			g.setColor(minusFocused ? Color.BLUE : BACKGROUND);
////			g.fillRect(xMinus, yMinus, size, size);
//			g.setColor(Color.BLACK);
//			g.drawRect(w/2 - sw/2 - dx - (this.labelOnly()?0:this.closeRect.width/2+dx/2), 0 , sw + 2*dx + (this.labelOnly()?0:this.closeRect.width+dx), fontHeight + 2*dy);
////			g.drawRect(xMinus, yMinus, size, size);
//			g.setFont(font);
//			g.drawString(this.gstrLabel, w/2 - sw/2 - (this.labelOnly()?0:this.closeRect.width/2+dx/2), dy+fontAscent);
//			if(!this.labelOnly()) {
//				//this.closeRect = new Rectangle(w/2 + sw/2 + dx,dy+fontAscent,fontAscent,fontAscent);
//				g.setStroke(new BasicStroke(lineWidth));
//				if(this.closeFocused) g.setColor(Color.BLUE);
//				g.drawLine(this.closeRect.x,this.closeRect.y,this.closeRect.x+this.closeRect.width,this.closeRect.y+this.closeRect.height);
//				g.drawLine(this.closeRect.x,this.closeRect.y+this.closeRect.height,this.closeRect.x+this.closeRect.width,this.closeRect.y);
//			}
//			g.setColor(currentColor);
//			g.setStroke(currentStroke);
//
////			closeRect = new Rectangle(xPlus, yPlus, size, size);
////			minusRect = new Rectangle(xMinus, yMinus, size, size);
//		}
//
//		private boolean labelOnly() { return this.function==null; }
//		
//		@Override
//		public boolean useTransform() {
//			return false;
//		}
//
//		@Override
//		public void mouseDragged(MouseEvent e) {
//		}
//
//		@Override
//		public void mouseMoved(MouseEvent e) {
//			boolean newCloseFocused = closeRect != null && closeRect.contains(e.getPoint());
//			boolean changed = newCloseFocused != closeFocused;
//
//			closeFocused = newCloseFocused;
////			minusFocused = newMinusFocused;
//
//			if (changed) {
//				BetterGraphMouse<?, ?> graphMouse = (BetterGraphMouse<?, ?>) viewer.getGraphMouse();
//
//				graphMouse.setPickingDeactivated(closeFocused);
//				paint(viewer.getGraphics());
//			}
//		}
//
//		@Override
//		public void mouseClicked(MouseEvent e) {
//			if (e.getButton() == MouseEvent.BUTTON1 && (closeFocused)) {
//				try {
//					//this.function.call();
//					this.function.run();
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
//		}
//
//		@Override
//		public void mousePressed(MouseEvent e) {
//		}
//
//		@Override
//		public void mouseReleased(MouseEvent e) {
//		}
//
//		@Override
//		public void mouseEntered(MouseEvent e) {
//		}
//
//		@Override
//		public void mouseExited(MouseEvent e) {
//		}
//	}
	
	@Override
	public void setPerformTracing(boolean performTracing) {
		super.setPerformTracing(performTracing);
		this.placeBoundaryNodes(false);
	}

	@Override
	public Set getBoundaryNodes() {
		// TODO Auto-generated method stub
		return this.boundaryNodes;
	}

	@Override
	public void addExplosionListener(ExplosionListener listener) {
		// TODO Auto-generated method st
		this.listenerList.add(ExplosionListener.class, listener);
	}

	@Override
	public void removeExplosionListener(ExplosionListener listener) {
		// TODO Auto-generated method stub
		this.listenerList.remove(ExplosionListener.class, listener);
	}

}