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
	
	private Polygon boundaryArea;
		
	public static final double BOUNDARY_MARGIN = 0.2;

	public ExplosionTracingOsmCanvas(List<LocationNode> nodes, List<Edge<LocationNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties, Map<String, Delivery> deliveries, boolean lotBased, String metaNodeId, Set<String> containedNodesIds) {
		super(nodes, edges, nodeProperties, edgeProperties, deliveries, lotBased);
		
		logger.finest("entered");

		this.metaNodeId = metaNodeId;
		this.nonBoundaryNodes = this.nodes.stream().filter(n -> containedNodesIds.contains(n.getId())).collect(Collectors.toSet());
		this.hiddenNodes = new HashSet<>();
		this.hiddenEdges = new HashSet<>();
		this.boundaryNodes = new HashSet<>();
		
		ExplosionCanvasUtils.initBoundaryAndHiddenNodes(this.nodes, this.edges, this.nonBoundaryNodes, this.boundaryNodes, this.hiddenNodes, this.hiddenEdges);
		
		this.edges = Sets.difference(this.edges, this.hiddenEdges).stream().collect(Collectors.toSet());
		this.boundaryNodesToInnerNodesMap = ExplosionCanvasUtils.createBoundaryNodesToInnerNodesMap(this.nonBoundaryNodes, this.boundaryNodes, this.edges);
		//this.hiddenNodes.forEach(n -> n.updateCenter(new Point2D.Double(Double.NaN, Double.NaN)));

		this.getViewer().addPostRenderPaintable(
				new ExplosionCanvasUtils.LabelPaintable(
						this.getViewer(),
						metaNodeId,
						()-> Stream.of(getListeners(ExplosionListener.class)).forEach(l->l.closeExplosionViewRequested(this))));
		
		
		this.placeNodes(this.nonBoundaryNodes, this.edges);
		
		
		logger.finest("leaving");
	}
	
	@Override
	public void resetNodesAndEdges() {
		logger.finest("entered");
		super.resetNodesAndEdges();
		
		if(this.hiddenNodes != null) this.nodes.removeAll(this.hiddenNodes); //  Sets.difference(this.nodes, this.hiddenNodes);
		if(this.hiddenEdges != null) this.edges.removeAll(this.hiddenEdges);   //this.edges = Sets.difference(this.edges, this.hiddenEdges);
		
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
		
		// remove old meta nodes which are obsolete
		Sets.difference(this.collapsedNodes.keySet(), collapsedNodes.keySet()).forEach(id -> nodeSaveMap.remove(id));
		
		Map<String, Set<String>> newFilteredCollapsedNodes = ExplosionCanvasUtils.filterCollapsedNodeAccordingToExplosion(
				collapsedNodes, this.metaNodeId,
				CanvasUtils.getElementIds(Sets.union(this.nonBoundaryNodes, this.boundaryNodes)));
		
		// create meta nodes which do not belong to the explosion view
		// this is required because the properties of the meta nodes have to be maintained
		Sets.difference(collapsedNodes.keySet(), newFilteredCollapsedNodes.keySet()).forEach(metaId -> {
			
			if(!nodeSaveMap.containsKey(metaId)) nodeSaveMap.put(metaId, createMetaNode(metaId, CanvasUtils.getElementsById(nodeSaveMap, collapsedNodes.get(metaId))));
		
		});
		
		
		for(String metaId : newFilteredCollapsedNodes.keySet()) {
			Set<String> nodeIds = newFilteredCollapsedNodes.get(metaId);
			
			if(Sets.intersection(nodeIds, this.boundaryNodesToInnerNodesMap.keySet()).isEmpty()) {
				nodeIds.forEach(id -> this.boundaryNodesToInnerNodesMap.putAll(metaId, this.boundaryNodesToInnerNodesMap.get(id)));
			}
		}

//		newFilteredCollapsedNodes.forEach((metaId, nodeIds) -> {
//			if(Sets.intersection(nodeIds, new HashSet<>(this.boundaryNodesToInnerNodesMap.keySet())).isEmpty()) {
//				nodeIds.forEach(id -> this.boundaryNodesToInnerNodesMap.putAll(metaId, this.boundaryNodesToInnerNodesMap.get(id)));
//			}
//		});
//		newCollapsedNodes.keySet().forEach(metaKey -> {
//			newCollapsedNodes.get(metaKey).forEach(key -> {
//				this.boundaryNodesToInnerNodesMap.putAll(metaKey, this.boundaryNodesToInnerNodesMap.get(key));
//			});
//		});
		
		this.collapsedNodes = newFilteredCollapsedNodes;
		
		this.applyChanges();
		
        if(this.boundaryNodes != null) {
		
//			Layout<LocationNode, Edge<LocationNode>> layout = this.getViewer().getGraphLayout();
//		
//			this.boundaryNodes.forEach(n -> layout.lock(n, true));
		
		
			if(!this.boundaryNodes.isEmpty()) {
				
				this.boundaryArea = ExplosionCanvasUtils.placeBoundaryNodes(
						this.boundaryNodes, 
						this.nonBoundaryNodes, 
						this.nodeSaveMap,
						this.boundaryNodesToInnerNodesMap,
						this.collapsedNodes, 
						this.getViewer().getGraphLayout(), 
						this.getInvalidArea());
			}
		}  
        
		call(l -> l.collapsedNodesChanged(this));
		//this.flushImage();
		logger.finest("leaving");
	}
	
//	@Override
//	public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
//		logger.finest("entered");
//		
//		
//		Sets.difference(this.collapsedNodes.keySet(), this.collapsedNodes.keySet()).forEach(id -> nodeSaveMap.remove(id));
//		
//		Map<String, Set<String>> newCollapsedNodes = ExplosionCanvasUtils.filterCollapsedNodeAccordingToExplosion(
//				collapsedNodes, this.metaNodeId,
//				CanvasUtils.getElementIds(Sets.union(this.nonBoundaryNodes, this.boundaryNodes)));
//		
//		Sets.difference(collapsedNodes.keySet(), newCollapsedNodes.keySet()).forEach(metaId -> {
//			
//			if(!nodeSaveMap.containsKey(metaId)) nodeSaveMap.put(metaId, createMetaNode(metaId, CanvasUtils.getElementsById(nodeSaveMap, collapsedNodes.get(metaId))));
//			
//		});
//
//		newCollapsedNodes.keySet().forEach(metaKey -> {
//			newCollapsedNodes.get(metaKey).forEach(key -> {
//				this.boundaryNodesToInnerNodesMap.putAll(metaKey, this.boundaryNodesToInnerNodesMap.get(key));
//			});
//		});
//		
//		this.applyChanges();
//		
//        if(this.boundaryNodes != null) {
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
//		call(l -> l.collapsedNodesChanged(this));
//		this.flushImage();
//		logger.finest("leaving");
//	}
	
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
