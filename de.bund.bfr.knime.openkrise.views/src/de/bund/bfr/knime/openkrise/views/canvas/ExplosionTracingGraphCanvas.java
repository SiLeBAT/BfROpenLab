/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.views.canvas;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.jung.layout.LayoutType;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.common.Delivery;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

/**
 * A <code>TracingGraphCanvas</code> for the explosion of meta nodes
 */
public class ExplosionTracingGraphCanvas extends TracingGraphCanvas implements IExplosionCanvas<GraphNode> {

	//private static Logger logger =  Logger.getLogger("de.bund.bfr");
	

	private static final long serialVersionUID = 1L;
	
	
	private String metaNodeId; // the id of the exploded meta node
	
	private Set<GraphNode> nonBoundaryNodes; // nodes which are collapse into the exploded node
	/*
	 *  Nodes which are connected to at least one of the collapsed nodes (<code>nonBoundaryNodes</code>)
	 *  This set does not contain meta nodes.
	 */
	private Set<GraphNode> boundaryNodes; 
	
	private Set<GraphNode> hiddenNodes; // Nodes which are not connected to the collapsed nodes (<code>nonBoundaryNodes</code>)
	private Set<Edge<GraphNode>> hiddenEdges; // Edges which are not connected to the collapsed nodes (<code>nonBoundaryNodes</code>)
	
	private SetMultimap<String, String> boundaryNodesToInnerNodesMap; // Map of the boundary node ids to their connected inner nodes ids 
	
	//private Map<String,Set<String>> allCollapsedNodes;
	
	private BufferedImage image;
	// private Polygon boundaryArea;
	private Boundary boundary;
	

	public ExplosionTracingGraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties, Map<String, Delivery> deliveries, boolean lotBased, String metaNodeId, Set<String> containedNodes) {
		super(nodes, edges, nodeProperties, edgeProperties, deliveries, lotBased, false);
		
		this.metaNodeId = metaNodeId;
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
		
	}
	
	/*
	 * All boundary nodes have to be locked, so that they cannot be moved with the mouse
	 */
	@Override
	public void applyNodeCollapse() {
		
		super.applyNodeCollapse();
		
		Layout<GraphNode, Edge<GraphNode>> layout = this.getViewer().getGraphLayout();
		
		Set<GraphNode> allBoundaryNodes = CanvasUtils.getElementsById(this.nodeSaveMap,this.boundaryNodesToInnerNodesMap.keySet());
		
		allBoundaryNodes.forEach(n -> layout.lock(n, true));
	}
	
	@Override
	protected Rectangle2D getLayoutBounds() {
		return ExplosionCanvasUtils.getInnerBoundaryRect(this);
	}
	
	@Override
	public void resetNodesAndEdges() {
		
		super.resetNodesAndEdges();
		
		// nodes and edges which do not belong to the explosion are removed
		if(this.hiddenNodes != null) this.nodes.removeAll(this.hiddenNodes); 
		if(this.hiddenEdges != null) this.edges.removeAll(this.hiddenEdges); 
		
	}
	
	@Override
	public VisualizationImageServer<GraphNode, Edge<GraphNode>> getVisualizationServer(boolean toSvg) {
		VisualizationImageServer<GraphNode, Edge<GraphNode>> server = super.getVisualizationServer(toSvg);
		
		server.addPreRenderPaintable(new PrePaintable(toSvg));
        server.addPostRenderPaintable(new ExplosionCanvasUtils.LabelPaintable(this.getViewer(),this.metaNodeId));
		return server;
	}
	
//	@Override
//	public void nodeMovementFinished() {
//		
//		this.placeBoundaryNodes(false);
//		super.nodeMovementFinished();
//		
//	}
	
	private void call(Consumer<CanvasListener> action) {
		Stream.of(getListeners(CanvasListener.class)).forEach(action);
	}
	
	
	@Override
	public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
		
		// remove old meta nodes which are obsolete
		Sets.difference(this.collapsedNodes.keySet(), collapsedNodes.keySet()).forEach(id -> nodeSaveMap.remove(id));
		
		// filters the collapsedNodes map according to the explosion
		Map<String, Set<String>> newFilteredCollapsedNodes = ExplosionCanvasUtils.filterCollapsedNodeAccordingToExplosion(
				collapsedNodes, this.metaNodeId,
				CanvasUtils.getElementIds(Sets.union(this.nonBoundaryNodes, this.boundaryNodes)));
				
		// create meta nodes which do not belong to the explosion view
		// this is required because the properties of the meta nodes have to be maintained
		Sets.difference(collapsedNodes.keySet(), newFilteredCollapsedNodes.keySet()).forEach(metaId -> {
			if(!nodeSaveMap.containsKey(metaId)) {
				nodeSaveMap.put(metaId, createMetaNode(metaId, CanvasUtils.getElementsById(nodeSaveMap, collapsedNodes.get(metaId))));
			}
		});
				
		// add the ids of boundary meta nodes to the node map		
		for(String metaId : newFilteredCollapsedNodes.keySet()) {
			Set<String> nodeIds = newFilteredCollapsedNodes.get(metaId);
			if(!Sets.intersection(nodeIds, this.boundaryNodesToInnerNodesMap.keySet()).isEmpty()) {
				nodeIds.forEach(id -> this.boundaryNodesToInnerNodesMap.putAll(metaId, this.boundaryNodesToInnerNodesMap.get(id)));
			}
		}

		//this.allCollapsedNodes = collapsedNodes;
		
		this.collapsedNodes = newFilteredCollapsedNodes;
				
		this.applyChanges();
				
		//if(this.boundaryNodes != null) this.placeBoundaryNodes(false);
		        
		call(l -> l.collapsedNodesChanged(this));
	}
	
	@Override
	protected Set<GraphNode> getLayoutableNodes() { 
		return this.nonBoundaryNodes; 
	}
	
	@Override
	protected void applyLayout(LayoutType layoutType, Set<GraphNode> nodesForLayout, boolean showProgressDialog) {
	    Set<GraphNode> nonBoundaryNodesForLayout = new HashSet<>(Sets.intersection(nodesForLayout, this.nonBoundaryNodes));
	    Set<GraphNode> boundaryNodesForLayout = new HashSet<>(Sets.difference(nodesForLayout, this.nonBoundaryNodes));
	    
	    //if(resetBoundary) boundary.resetBounds(super.getLayoutBounds()); // todo: transfrom layoutbounds to boundary area
	    //if(resetBoundary) boundary.resetBounds(this.getViewer().getSize(),  getInnerBounds()); // todo: transfrom layoutbounds to boundary area
	    
		//super.applyLayout(layoutType, nodesForLayout, showProgressDialog, false);
	    if(!nonBoundaryNodesForLayout.isEmpty()) super.applyLayout(layoutType, nonBoundaryNodesForLayout, showProgressDialog, false);
	    
	    boolean resetBoundary = !nonBoundaryNodesForLayout.isEmpty() || !Sets.difference(this.boundaryNodes, boundaryNodesForLayout).isEmpty();
	    
	    if(resetBoundary) this.resetBoundary();
		
	    //  this.placeBoundaryNodes(false);
	    if(!boundaryNodesForLayout.isEmpty()) this.placeBoundaryNodes(boundaryNodesForLayout);
			
		Stream.of(getListeners(CanvasListener.class)).forEach(l -> l.layoutProcessFinished(this));
	}
	
	private void resetBoundary() {
	  
	}
	
	private Rectangle2D getInnerBounds() {
	  return PointUtils.getBounds(this.getNodePositions(this.nonBoundaryNodes).values());
	}
	
	/*
	 * The boundary area might need an update.
	 */
	@Override
	public void transformFinished() {
		//this.placeBoundaryNodes(true);
		this.flushImage();
		super.transformFinished();
	}
	
//	/*
//	 * sets the boundary area and place the boundary nodes
//	 */
//	public void placeBoundaryNodes(boolean onlyUpdateBoundaryAreaPosition) {
//		
//		if(this.isPerformTracing()) {
//		
//			if (!((this.boundaryNodes == null) || this.boundaryNodes.isEmpty())) {
//				
//				// Step 1: get the bounds of the inner nodes
//				
//				
//				Map<String, Point2D> positions = this.getNodePositions(this.nonBoundaryNodes);
//				
//				// safety test
//				if(positions == null) return;
//				
//				/* the positions might not be available for all nodes because e.g. the date filter specified by the user 
//				 * might remove some inner nodes from the graph so the layout methods do not set their position
//				 * --> these empty position are set to the left upper corner of default bounds
//				 */
//				
//				
//				Rectangle2D defaultBounds = ExplosionCanvasUtils.getInnerBoundaryRect(this);
//				
//				Set<String> nodesWithoutPosition = positions.entrySet().stream().filter(e -> Double.isNaN(e.getValue().getX()) || Double.isNaN(e.getValue().getY())).map(e -> e.getKey()).collect(Collectors.toSet());
//				
//				nodesWithoutPosition.forEach(nodeId -> positions.put(nodeId, new Point2D.Double(defaultBounds.getX(), defaultBounds.getY())));
//				
//				
//				Rectangle2D bounds = PointUtils.getBounds(positions.values());
//				
//				if(bounds.isEmpty()) {
//					
//					boundaryArea = null;
//					this.flushImage();
//					return;
//				}
//			
//				Polygon newBoundaryArea = ExplosionCanvasUtils.createBoundaryArea(bounds);
//
//				boolean boundaryAreaChanged = boundaryArea==null || !newBoundaryArea.equals(boundaryArea);
//			
//				boundaryArea = newBoundaryArea;
//			
//				Rectangle2D rect = ExplosionCanvasUtils.getAreaRect(this.boundaryArea);
//				double w = ExplosionCanvasUtils.getAreaBorderWidth(this.boundaryArea);
//			
//				if(!onlyUpdateBoundaryAreaPosition) {
//					
//					// <code>updateSet</code> is supposed to be the set of ids of visible boundary nodes if no date filter is applied
//					Set<String> updateSet = boundaryNodesToInnerNodesMap.keySet().stream().collect(Collectors.toSet());
//					
//					collapsedNodes.entrySet().forEach(e -> updateSet.removeAll(e.getValue()));
//					
//					// sets the positions of the boundary nodes to nearby positions on the boundary area
//					for(String boundaryNodeKey: updateSet) {
//						
//						Point2D pCenter = PointUtils.getCenter(
//								this.boundaryNodesToInnerNodesMap.get(boundaryNodeKey).stream().map(innerNodeKey -> positions.get(innerNodeKey))
//								.collect(Collectors.toList()));
//						
//						Point2D pBR = ExplosionCanvasUtils.getClosestPointOnRect(pCenter, rect);
//						 
//						positions.put(boundaryNodeKey, pBR);
//					}
//					
//					// if an inner node has connections to several boundary nodes
//					// then their might be positioned on the same spot (depending on their other connections)
//					// this method distributes the boundary nodes with identical positions
//		            ExplosionCanvasUtils.updateBoundaryNodePositionsByRemovingVisualConflicts(
//		            		positions, rect, 
//		            		boundaryNodesToInnerNodesMap.asMap().entrySet().stream().filter(e -> updateSet.contains(e.getKey()))
//		            		.collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<String>(e.getValue()))),
//		            		w);
//					
//					// the boundary positions were only set for the visual nodes so far
//					// but the position of the boundary meta nodes will be overwritten by 
//					// the position of the center of their contained nodes ->
//					// the position of the contained nodes is set to the position of their meta node
//		            Sets.intersection(collapsedNodes.keySet(), updateSet).forEach(metaKey -> {
//					        	  Point2D p = positions.get(metaKey);
//					        	  collapsedNodes.get(metaKey).forEach(k -> positions.put(k, p));
//					          });
//					
//					this.setNodePositions(positions);
//				}
//				
//				if(boundaryAreaChanged) this.flushImage();
//			}
//		}
//
//	}
	
	@Override
	public void initLayout() {
	  //if(this.boundaryNodes.isEmpty()) this.initBoundary();
	  super.initLayout();  
	  initBoundaryLayout();
//      if (!this.getLayoutableNodes().isEmpty()) {
//          applyLayout(LayoutType.ISOM_LAYOUT, this.getLayoutableNodes(), false);
//      }
	  //this.initBoundaryNodePositions();
  }
	
//	private void initBoundary() {
//	  boundary = new Boundary(this.nonBoundaryNodes, this.boundaryNodes, this.edges, this.getViewer().getSize());
//	}
//	
//	private void initBoundaryNodePositions() {
//	  //if(this.boundary!=null) boundary.initNodePositions(this.getNodePositions());
//	}
	
	private void initBoundaryLayout() {
	  
	}
	
	/*
     * sets the boundary area and place the boundary nodes
     */
    public void placeBoundaryNodes(Set<GraphNode> nonBoundaryNodesToPlace) {
        
        if(this.isPerformTracing()) {
        
            if (!((nonBoundaryNodesToPlace == null) || nonBoundaryNodesToPlace.isEmpty())) {
                
                if(boundary!=null) {
                  boundary.placeNodes(nonBoundaryNodesToPlace, this.getNodePositions(this.nonBoundaryNodes));
                  this.setNodePositions(boundary.getNodePositions());
                }
                // Step 1: get the bounds of the inner nodes
                
                
//                Map<String, Point2D> innerNodePositions = this.getNodePositions(this.nonBoundaryNodes);
//                
//                // safety test
//                if(innerNodePositions == null) return;
//                
//                /* the positions might not be available for all nodes because e.g. the date filter specified by the user 
//                 * might remove some inner nodes from the graph so the layout methods do not set their position
//                 * --> these empty position are set to the left upper corner of default bounds
//                 */
//                
//                
//                
//                
//                
//                
//                
//                
//                
//                
//                Rectangle2D defaultBounds = ExplosionCanvasUtils.getInnerBoundaryRect(this);
//                
//                Set<String> nodesWithoutPosition = positions.entrySet().stream().filter(e -> Double.isNaN(e.getValue().getX()) || Double.isNaN(e.getValue().getY())).map(e -> e.getKey()).collect(Collectors.toSet());
//                
//                nodesWithoutPosition.forEach(nodeId -> positions.put(nodeId, new Point2D.Double(defaultBounds.getX(), defaultBounds.getY())));
//                
//                
//                Rectangle2D bounds = PointUtils.getBounds(positions.values());
//                
//                if(bounds.isEmpty()) {
//                    
//                    boundaryArea = null;
//                    this.flushImage();
//                    return;
//                }
//            
//                Polygon newBoundaryArea = ExplosionCanvasUtils.createBoundaryArea(bounds);
//
//                boolean boundaryAreaChanged = boundaryArea==null || !newBoundaryArea.equals(boundaryArea);
//            
//                boundaryArea = newBoundaryArea;
//            
//                Rectangle2D rect = ExplosionCanvasUtils.getAreaRect(this.boundaryArea);
//                double w = ExplosionCanvasUtils.getAreaBorderWidth(this.boundaryArea);
//            
//                if(!onlyUpdateBoundaryAreaPosition) {
//                    
//                    // <code>updateSet</code> is supposed to be the set of ids of visible boundary nodes if no date filter is applied
//                    Set<String> updateSet = boundaryNodesToInnerNodesMap.keySet().stream().collect(Collectors.toSet());
//                    
//                    collapsedNodes.entrySet().forEach(e -> updateSet.removeAll(e.getValue()));
//                    
//                    // sets the positions of the boundary nodes to nearby positions on the boundary area
//                    for(String boundaryNodeKey: updateSet) {
//                        
//                        Point2D pCenter = PointUtils.getCenter(
//                                this.boundaryNodesToInnerNodesMap.get(boundaryNodeKey).stream().map(innerNodeKey -> positions.get(innerNodeKey))
//                                .collect(Collectors.toList()));
//                        
//                        Point2D pBR = ExplosionCanvasUtils.getClosestPointOnRect(pCenter, rect);
//                         
//                        positions.put(boundaryNodeKey, pBR);
//                    }
//                    
//                    // if an inner node has connections to several boundary nodes
//                    // then their might be positioned on the same spot (depending on their other connections)
//                    // this method distributes the boundary nodes with identical positions
//                    ExplosionCanvasUtils.updateBoundaryNodePositionsByRemovingVisualConflicts(
//                            positions, rect, 
//                            boundaryNodesToInnerNodesMap.asMap().entrySet().stream().filter(e -> updateSet.contains(e.getKey()))
//                            .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<String>(e.getValue()))),
//                            w);
//                    
//                    // the boundary positions were only set for the visual nodes so far
//                    // but the position of the boundary meta nodes will be overwritten by 
//                    // the position of the center of their contained nodes ->
//                    // the position of the contained nodes is set to the position of their meta node
//                    Sets.intersection(collapsedNodes.keySet(), updateSet).forEach(metaKey -> {
//                                  Point2D p = positions.get(metaKey);
//                                  collapsedNodes.get(metaKey).forEach(k -> positions.put(k, p));
//                              });
//                    
//                    this.setNodePositions(positions);
//                }
//                
//                if(boundaryAreaChanged) this.flushImage();
            }
        }

    }
	
	/*
	 * deletes the buffered image
	 */
	protected void flushImage() {
		if (image != null) {
			image.flush();
			image = null;
		}
	}
	
	private void paintGraph(Graphics2D g, boolean toSvg) {
		if(boundary!=null) boundary.paint(g,  this);

//		if (this.boundaryArea != null) {
//			ExplosionCanvasUtils.paintBoundaryArea(g, getCanvasSize().width, getCanvasSize().height,
//					transform.apply(this.boundaryArea));
//		}

	}
	
	private void paintGraphImage(Graphics2D g) {
		
		int width = getCanvasSize().width;
		int height = getCanvasSize().height;

		if (image == null || image.getWidth() != width || image.getHeight() != height) {
			flushImage();
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			this.paintGraph(image.createGraphics(), false);
		}

		g.drawImage(image, 0, 0, null);
	
	}
	
//	@Override
//	public Map<String, Point2D> getNodePositions() {
//        // only return the information for the inner nodes
//		return getNodePositions(this.nonBoundaryNodes);
//
//	}
	
	/*
	 * PrePaintable needed to integrate the boundary area
	 */
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
			
			if (toSvg) {
				ExplosionTracingGraphCanvas.this.paintGraph((Graphics2D) g, true);
			} else {
				ExplosionTracingGraphCanvas.this.paintGraphImage((Graphics2D) g);
			}
			
		}
	}
	
	@Override
	public void setPerformTracing(boolean performTracing) {
		super.setPerformTracing(performTracing);
		this.placeBoundaryNodes(false);
	}

	@Override
	public Set<GraphNode> getBoundaryNodes() {
		return this.boundaryNodes;
	}

	@Override
	public void addExplosionListener(ExplosionListener listener) {
		this.listenerList.add(ExplosionListener.class, listener);
	}

	@Override
	public void removeExplosionListener(ExplosionListener listener) {
		this.listenerList.remove(ExplosionListener.class, listener);
	}

}