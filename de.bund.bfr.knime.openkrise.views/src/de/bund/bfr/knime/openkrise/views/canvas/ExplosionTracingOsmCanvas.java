package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;

import org.apache.commons.collections15.Transformer;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.jung.BetterGraphMouse;
import de.bund.bfr.jung.JungListener;
import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.jung.layout.LayoutType;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
//import de.bund.bfr.knime.gis.views.canvas.ExplosionCanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.common.Delivery;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public class ExplosionTracingOsmCanvas extends TracingOsmCanvas implements IExplosionCanvas<LocationNode> {

	private static Logger logger =  Logger.getLogger("de.bund.bfr");
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String metaNodeId;
	private Set<LocationNode> boundaryNodes; 
	private Set<LocationNode> nonBoundaryNodes;
	private Set<LocationNode> hiddenNodes;
	private Set<Edge<LocationNode>> hiddenEdges;
	private SetMultimap<String, String> boundaryNodesToInnerNodesMap;
	// private Set<LocationNode> allBoundaryNodes;
	
//	private BufferedImage image;
	private Polygon boundaryArea;
		
	public static final double BOUNDARY_MARGIN = 0.2;
	
//	private Map<String, Set<String>> allCollapsedNodes;

	public ExplosionTracingOsmCanvas(List<LocationNode> nodes, List<Edge<LocationNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties, Map<String, Delivery> deliveries, boolean lotBased, String metaNodeId, Set<String> containedNodesIds) {
		super(nodes, edges, nodeProperties, edgeProperties, deliveries, lotBased);
		
		logger.finest("entered");
//		this.image = null;
		this.metaNodeId = metaNodeId;
		this.nonBoundaryNodes = this.nodes.stream().filter(n -> containedNodesIds.contains(n.getId())).collect(Collectors.toSet());
		this.initHiddenObjects();
		
		this.boundaryNodes = Sets.difference(Sets.difference(this.nodes, this.nonBoundaryNodes),this.hiddenNodes);
		
		//this.boundaryNodes = this.nodes.stream().filter(n -> !containedNodes.contains(n.getId())).collect(Collectors.toSet());
		//this.allBoundaryNodes = this.boundaryNodes.stream().collect(Collectors.toSet());
		this.boundaryNodesToInnerNodesMap = ExplosionCanvasUtils.collectBoundaryNodesInnerNeighbours(this.nonBoundaryNodes, this.boundaryNodes, Sets.difference(this.edges, this.hiddenEdges));
		
//		this.getViewer().addPreRenderPaintable(new PrePaintable(false));
		//this.getViewer().addPostRenderPaintable(new ExplosionCanvasUtils.LabelPaintable(this.getViewer(),strKey,()->call(l->l.closeExplosionViewRequested())));
		this.getViewer().addPostRenderPaintable(
				new ExplosionCanvasUtils.LabelPaintable(
						this.getViewer(),
						metaNodeId,
						()-> Stream.of(getListeners(ExplosionListener.class)).forEach(l->l.closeExplosionViewRequested(this))));
		
		//this.boundaryNodes.forEach(n -> this.getViewer().getGraphLayout().lock(n, true));
		
		this.placeNodes(this.nonBoundaryNodes, this.edges);
		
		
		logger.finest("leaving");
	}
	
	private void initHiddenObjects() {
		
		this.hiddenNodes = new HashSet<>();
		this.hiddenEdges = new HashSet<>();
		this.boundaryNodes = new HashSet<>();
		
		for(Edge<LocationNode> edge : this.edges) {
			if(this.nonBoundaryNodes.contains(edge.getFrom())) {
				
				if(!this.nonBoundaryNodes.contains(edge.getTo())) this.boundaryNodes.add(edge.getTo());
				
			} else if(this.nonBoundaryNodes.contains(edge.getTo())) {
				
				this.boundaryNodes.add(edge.getFrom());
				
			} else {
				
				this.hiddenEdges.add(edge);
			}
		}
		
		this.hiddenNodes = Sets.difference(Sets.difference(this.nodes, this.nonBoundaryNodes), this.boundaryNodes);

	}
	
//	@Override
//	public void applyNodeCollapse() {
//		logger.finest("entered");
//		super.applyNodeCollapse();
//		
//		if(this.nonBoundaryNodes != null) {
//			
//			this.boundaryNodes = Sets.difference(this.nodes, this.nonBoundaryNodes);
//		
//			Layout<LocationNode, Edge<LocationNode>> layout = this.getViewer().getGraphLayout();
//		
//			this.boundaryNodes.forEach(n -> layout.lock(n, true));
//		
//		
//			if(!this.boundaryNodes.isEmpty()) {
//				
//				this.boundaryArea = ExplosionCanvasUtils.placeBoundaryNodes(
//						this.boundaryNodes, 
//						this.nonBoundaryNodes, 
//						this.nodeSaveMap,
//						this.boundaryNodesToInnerNodesMap,
//						this.collapsedNodes, 
//						layout, 
//						this.getInvalidArea());
//			}
//		}
//		
//		logger.finest("leaving");
//	}
	
	@Override
	public void resetNodesAndEdges() {
		logger.finest("entered");
		super.resetNodesAndEdges();
		
		if(this.hiddenNodes != null) this.nodes = Sets.difference(this.nodes, this.hiddenNodes);
		if(this.hiddenEdges != null) this.edges = Sets.difference(this.edges, this.hiddenEdges);
		
		logger.finest("leaving");
	}
	
	@Override
	public VisualizationImageServer<LocationNode, Edge<LocationNode>> getVisualizationServer(boolean toSvg) {
		VisualizationImageServer<LocationNode, Edge<LocationNode>> server = super.getVisualizationServer(toSvg);
		
        server.addPostRenderPaintable(new ExplosionCanvasUtils.LabelPaintable(this.getViewer(),this.metaNodeId));
		return server;
	}
	
	private void call(Consumer<CanvasListener> action) {
		Stream.of(getListeners(CanvasListener.class)).forEach(action);
	}
	
	@Override
	protected void placeNodes(Set<LocationNode> nodes, Set<Edge<LocationNode>> edges) {
		
		if(this.nonBoundaryNodes != null) {
			
			//Set<LocationNode> nonBoundaryNodes = Sets.intersection(this.nodes, this.nonBoundaryNodes);
			
			super.placeNodes(
					this.nonBoundaryNodes, 
					this.edges.stream().filter(e -> this.nonBoundaryNodes.contains(e.getTo()) && this.nonBoundaryNodes.contains(e.getFrom())).collect(Collectors.toSet()));
			
		} else {
			// do nothing
		}
		
	}

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = PointUtils.getBounds(getNodePositions(this.nonBoundaryNodes).values());
		

		if (bounds != null) {
			if(this.boundaryArea!=null) bounds = ExplosionCanvasUtils.getAreaRect(this.boundaryArea);
			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, 2.0));
			transformFinished();
		} else {
			super.resetLayoutItemClicked();
		}
	}
	
	@Override
	public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
		logger.finest("entered");
		
		
		Sets.difference(this.collapsedNodes.keySet(), this.collapsedNodes.keySet()).forEach(id -> nodeSaveMap.remove(id));
		
		Map<String, Set<String>> newCollapsedNodes = ExplosionCanvasUtils.filterCollapsedNodeAccordingToExplosion(
				collapsedNodes, this.metaNodeId,
				CanvasUtils.getElementIds(Sets.union(this.nonBoundaryNodes, this.boundaryNodes)));
		
		Sets.difference(collapsedNodes.keySet(), newCollapsedNodes.keySet()).forEach(metaId -> {
			
			if(!nodeSaveMap.containsKey(metaId)) nodeSaveMap.put(metaId, createMetaNode(metaId, CanvasUtils.getElementsById(nodeSaveMap, collapsedNodes.get(metaId))));
			
		});

		newCollapsedNodes.keySet().forEach(metaKey -> {
			newCollapsedNodes.get(metaKey).forEach(key -> {
				this.boundaryNodesToInnerNodesMap.putAll(metaKey, this.boundaryNodesToInnerNodesMap.get(key));
			});
		});
		
		this.applyChanges();
		
        if(this.boundaryNodes != null) {
		
			Layout<LocationNode, Edge<LocationNode>> layout = this.getViewer().getGraphLayout();
		
			this.boundaryNodes.forEach(n -> layout.lock(n, true));
		
		
			if(!this.boundaryNodes.isEmpty()) {
				
				this.boundaryArea = ExplosionCanvasUtils.placeBoundaryNodes(
						this.boundaryNodes, 
						this.nonBoundaryNodes, 
						this.nodeSaveMap,
						this.boundaryNodesToInnerNodesMap,
						this.collapsedNodes, 
						layout, 
						this.getInvalidArea());
			}
		}  
        
		call(l -> l.collapsedNodesChanged(this));
		this.flushImage();
		logger.finest("leaving");
	}
	
	@Override
	protected void paintGis(Graphics2D g, boolean toSvg, boolean onWhiteBackground) {
		super.paintGis(g, toSvg, onWhiteBackground);

		if (this.boundaryArea != null) {
			ExplosionCanvasUtils.paintBoundaryArea(g, getCanvasSize().width, getCanvasSize().height,
					transform.apply(this.boundaryArea));
		}
	}

	@Override
	public Set getBoundaryNodes() {
		// TODO Auto-generated method stub
		return this.boundaryNodes;
	}

	@Override
	public void addExplosionListener(ExplosionListener listener) {
		// TODO Auto-generated method stub
		this.listenerList.add(ExplosionListener.class, listener);
	}

	@Override
	public void removeExplosionListener(ExplosionListener listener) {
		// TODO Auto-generated method stub
		this.listenerList.remove(ExplosionListener.class, listener);
	}


}
