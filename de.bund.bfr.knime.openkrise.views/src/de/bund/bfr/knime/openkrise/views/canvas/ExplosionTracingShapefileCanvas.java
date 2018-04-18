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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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

import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.common.Delivery;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public class ExplosionTracingShapefileCanvas extends TracingShapefileCanvas implements IExplosionCanvas<LocationNode> {
	//private static Logger logger =  Logger.getLogger("de.bund.bfr");
	
		private static final long serialVersionUID = 1L;
		
		private String metaNodeId; // the id of the exploded meta node
		
		private Set<LocationNode> nonBoundaryNodes; // nodes which are collapse into the exploded node
		/*
		 *  Nodes which are connected to at least one of the collapsed nodes (<code>nonBoundaryNodes</code>)
		 *  This set does not contain meta nodes.
		 */
		private Set<LocationNode> boundaryNodes; 
		
		private Set<LocationNode> hiddenNodes; // Nodes which are not connected to the collapsed nodes (<code>nonBoundaryNodes</code>)
		private Set<Edge<LocationNode>> hiddenEdges; // Edges which are not connected to the collapsed nodes (<code>nonBoundaryNodes</code>)
		
		private SetMultimap<String, String> boundaryNodesToInnerNodesMap; // Map of the boundary node ids to their connected inner nodes ids
		
		//private Map<String,Set<String>> allCollapsedNodes;
		
		private Polygon boundaryArea;
			

		public ExplosionTracingShapefileCanvas(List<LocationNode> nodes, List<Edge<LocationNode>> edges, NodePropertySchema nodeProperties,
				EdgePropertySchema edgeProperties, List<RegionNode> regions, Map<String, Delivery> deliveries, boolean lotBased, String metaNodeId, Set<String> containedNodesIds) {
			super(nodes, edges, nodeProperties, edgeProperties, regions, deliveries, lotBased);
			
			this.metaNodeId = metaNodeId;
			this.nonBoundaryNodes = this.nodes.stream().filter(n -> containedNodesIds.contains(n.getId())).collect(Collectors.toSet());
			this.hiddenNodes = new HashSet<>();
			this.hiddenEdges = new HashSet<>();
			this.boundaryNodes = new HashSet<>();
			
			ExplosionCanvasUtils.initBoundaryAndHiddenNodes(this.nodes, this.edges, this.nonBoundaryNodes, this.boundaryNodes, this.hiddenNodes, this.hiddenEdges);
			
			this.edges = Sets.difference(this.edges, this.hiddenEdges).stream().collect(Collectors.toSet());
			this.boundaryNodesToInnerNodesMap = ExplosionCanvasUtils.createBoundaryNodesToInnerNodesMap(this.nonBoundaryNodes, this.boundaryNodes, this.edges);
			
			this.getViewer().addPostRenderPaintable(
					new ExplosionCanvasUtils.LabelPaintable(
							this.getViewer(),
							metaNodeId,
							()-> Stream.of(getListeners(ExplosionListener.class)).forEach(l->l.closeExplosionViewRequested(this))));
			
			this.placeNodes(this.nonBoundaryNodes, this.edges);
		}
		
//		@Override
//		public void nodeAllPropertiesItemClicked() {
//			ExplosionCanvasUtils.showAllPropertiesDialog(this, this.allCollapsedNodes, this.nodeSaveMap);
//		}
		
		@Override
		public void resetNodesAndEdges() {
			super.resetNodesAndEdges();
			
			// nodes and edges which do not belong to the explosion are removed
			if(this.hiddenNodes != null) this.nodes.removeAll(this.hiddenNodes); 
			if(this.hiddenEdges != null) this.edges.removeAll(this.hiddenEdges);  
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
		
		/*
		 * places the inner nodes
		 */
		@Override
		protected void placeNodes(Set<LocationNode> nodes, Set<Edge<LocationNode>> edges) {
			
			if(this.nonBoundaryNodes != null) {
				
				super.placeNodes(
						this.nonBoundaryNodes, 
						this.edges.stream().filter(e -> this.nonBoundaryNodes.contains(e.getTo()) && this.nonBoundaryNodes.contains(e.getFrom())).collect(Collectors.toSet()));
				
				// missing positional information of hidden or boundary nodes is set to NaN
				// because createMetaNode cannot deal with missing information
				if(this.hiddenNodes != null) this.hiddenNodes.stream().filter(n -> n.getCenter()==null).collect(Collectors.toSet()).forEach(n -> n.updateCenter(new Point2D.Double(Double.NaN, Double.NaN)));
				if(this.boundaryNodes != null) this.boundaryNodes.stream().filter(n -> n.getCenter()==null).collect(Collectors.toSet()).forEach(n -> n.updateCenter(new Point2D.Double(Double.NaN, Double.NaN)));
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
		
		/*
		 * sets boundary area and places boundary nodes
		 */
		private void placeBoundaryNodes() {
			if(this.boundaryNodes != null) {
			
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
		}
		
		@Override
		public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
					
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
			
	        this.placeBoundaryNodes();
	        
			call(l -> l.collapsedNodesChanged(this));
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
		public Set<LocationNode> getBoundaryNodes() {
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
