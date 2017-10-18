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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;

import org.apache.commons.collections15.Transformer;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.jung.BetterGraphMouse;
import de.bund.bfr.jung.JungListener;
import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Formatter;

public class ExplosionCanvasUtils {

	private static final GeometryFactory FACTORY = new GeometryFactory();
	private static Logger logger =  Logger.getLogger("de.bund.bfr");

	public static final double BOUNDARY_AREA_RELATIVE_MARGIN = LocationCanvasUtils.INVALID_AREA_RELATIVE_MARGIN; //    0.2;
	public static final double BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH = LocationCanvasUtils.INVALID_AREA_RELATIVE_BORDERWIDTH; // 0.02;
	public static final double BOUNDARY_WIDTH = 5; // 10
	public static final double STATION_DISTANCE = 10;
	
	public static Polygon createBorderPolygon(Rectangle2D rect, double d) {
		Coordinate[] outerRing = new Coordinate[] { new Coordinate(rect.getMinX() - d, rect.getMinY() - d),
				new Coordinate(rect.getMaxX() + d, rect.getMinY() - d),
				new Coordinate(rect.getMaxX() + d, rect.getMaxY() + d),
				new Coordinate(rect.getMinX() - d, rect.getMaxY() + d),
				new Coordinate(rect.getMinX() - d, rect.getMinY() - d) };
		Coordinate[] innerRing = new Coordinate[] { new Coordinate(rect.getMinX(), rect.getMinY()),
				new Coordinate(rect.getMaxX(), rect.getMinY()), new Coordinate(rect.getMaxX(), rect.getMaxY()),
				new Coordinate(rect.getMinX(), rect.getMaxY()), new Coordinate(rect.getMinX(), rect.getMinY()) };

		return FACTORY.createPolygon(FACTORY.createLinearRing(outerRing),
				new LinearRing[] { FACTORY.createLinearRing(innerRing) });
	}
	
	public static void paintBoundaryArea(Graphics2D g, int w, int h, Shape boundaryArea) {
		BufferedImage boundaryAreaImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D imgGraphics = boundaryAreaImage.createGraphics();

		imgGraphics.setPaint(CanvasUtils.mixColors(Color.WHITE, Arrays.asList(Color.GREEN, Color.WHITE),
				Arrays.asList(1.0, 1.0), false));
		imgGraphics.fill(boundaryArea);
		imgGraphics.setColor(Color.BLACK);
		imgGraphics.draw(boundaryArea);
	
		CanvasUtils.drawImageWithAlpha(g, boundaryAreaImage, 75);
		boundaryAreaImage.flush();
	}
	
	public static Map<String, Set<String>> filterCollapsedNodeAccordingToExplosion(Map<String, Set<String>> collapsedNodes, String explodedNodeKey, Set<String> retainNodes) {
		return collapsedNodes.entrySet().stream()
				.filter(e->e.getKey()!=explodedNodeKey && !Sets.intersection(e.getValue(), retainNodes).isEmpty())
				.collect(Collectors.toMap(e->e.getKey(),e-> new HashSet<>(Sets.intersection(retainNodes,e.getValue()))));
	}
	
	public static Map<String, Set<String>> filterCollapsedNodeAccordingToExplosion(Map<String, Set<String>> collapsedNodes, String explodedNodeKey) {
		return collapsedNodes.entrySet().stream()
				.filter(e -> e.getKey() != explodedNodeKey)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); //    e->e.getKey(),e->Sets.intersection(retainNodes,e.getValue())));
	}
	
	public static Rectangle2D getInnerBoundaryRect(Canvas canvas) {
		
		Dimension size = canvas.getViewer().getSize();
		double maxSize = Math.max(size.getWidth(), size.getHeight());
		
		
		double innerSize = maxSize / (1 + 2 * BOUNDARY_AREA_RELATIVE_MARGIN + BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH);
		double margin = innerSize * BOUNDARY_AREA_RELATIVE_MARGIN;
		double w = innerSize * BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH;
		
		return new Rectangle2D.Double(
				0 + margin + w / 2, 
				0 + margin + w / 2, 
				size.getWidth() - 2 * margin - w, 
				size.getHeight() - 2 * margin - w);
				
	}
	
	public static Rectangle2D getInnerBoundaryRect(Set<LocationNode> innerNodes) {
		List<Point2D> positions = new ArrayList<>();

		for (LocationNode node : innerNodes) {
			if (node.getCenter() != null) {
				positions.add(node.getCenter());
			} else {
				// this should not happen since the node center was already set
				return null;
			}
		}
		
		return PointUtils.getBounds(positions); 
	}
	
	public static Rectangle2D getAreaRect(Polygon area) {
		Rectangle2D bounds = null;
		
		if(area!=null) {
			
			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			double minY = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;
			
			for(Coordinate coord : area.getCoordinates()) {
				if(minX > coord.x) minX = coord.x;
				if(maxX < coord.x) maxX = coord.x;
				if(minY > coord.y) minY = coord.y;
				if(maxY < coord.y) maxY = coord.y;
			}
			
			double w = getAreaBorderWidth(area);
			
			bounds = new Rectangle2D.Double(minX + w / 2, minY + w / 2, maxX - minX - w, maxY - minY - w);
		}
		
		return bounds;
	}
			
	public static double getAreaBorderWidth(Polygon area) {
		
		if(area != null) {
			
			double minX = Double.MAX_VALUE;
			double minX2 = Double.MAX_VALUE;
			
			for(Coordinate coord : area.getCoordinates()) if(minX > coord.x) minX = coord.x;
				
			for(Coordinate coord : area.getCoordinates()) if((minX2 > coord.x) && (minX < coord.x)) minX2 = coord.x;
			
			return minX2 - minX;
			
		} else {
			
			return 0;
			
		}
	}
	
	public static Polygon createBoundaryArea(Polygon invalidArea) {
		Rectangle2D invalidAreaRect = getAreaRect(invalidArea);
		double w = getAreaBorderWidth(invalidArea);
		
		double size = Math.max(invalidAreaRect.getWidth(), invalidAreaRect.getHeight());
		double margin = BOUNDARY_AREA_RELATIVE_MARGIN/2 * size;
		
		return GisUtils.createBorderPolygon(new Rectangle2D.Double(
				invalidAreaRect.getX() - margin/2 - w/2, 
				invalidAreaRect.getY() - margin/2 - w/2,
				invalidAreaRect.getWidth() + margin + w,
				invalidAreaRect.getHeight() + margin + w), w);
	}
	
	public static Polygon createBoundaryArea(Rectangle2D innerBounds) {
		double size = Math.max(innerBounds.getWidth(), innerBounds.getHeight());
		double margin = size * BOUNDARY_AREA_RELATIVE_MARGIN;
		double w = size * BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH;
		
		return GisUtils.createBorderPolygon(
				new Rectangle2D.Double(
						innerBounds.getX() - margin - w/2, 
						innerBounds.getY() - margin - w/2,
						innerBounds.getWidth() + 2 * margin + w,
						innerBounds.getHeight() + 2 * margin + w), w);
	}
	 
    public static <T extends Node> void initBoundaryAndHiddenNodes(
    		Set<T> nodes, Set<Edge<T>> edges, Set<T> nonBoundaryNodes, Set<T> boundaryNodes, 
    		Set<T> hiddenNodes, Set<Edge<T>> hiddenEdges) {
		
		for(Edge<T> edge : edges) {
			
			if(nonBoundaryNodes.contains(edge.getFrom())) {
				
				if(!nonBoundaryNodes.contains(edge.getTo())) boundaryNodes.add(edge.getTo());
				
			} else if(nonBoundaryNodes.contains(edge.getTo())) {
				
				boundaryNodes.add(edge.getFrom());
				
			} else {
				
				hiddenEdges.add(edge);
			}
		}
		
		hiddenNodes.addAll(Sets.difference(Sets.difference(nodes, nonBoundaryNodes), boundaryNodes));

	}
//	public static Polygon placeBoundaryNodes(Set<LocationNode> allBoundaryNodes, Set<LocationNode> nodes, Set<Edge<LocationNode>> edges, Map<String,Set<String>> collapsedNodes, Layout<LocationNode, Edge<LocationNode>> layout, Polygon invalidArea) {
//		logger.finest("entered");
//
//		Polygon boundaryArea = null;
//		
//		if (!allBoundaryNodes.isEmpty()) {
//			
//			Set<LocationNode> boundaryNodes = Sets.intersection(nodes, allBoundaryNodes);
//			
//			boundaryArea = (invalidArea != null ? createBoundaryArea(invalidArea) : createBoundaryArea(getInnerBoundaryRect(boundaryNodes)));
//			
//			Rectangle2D rect = getAreaRect(boundaryArea);
//			double w = getAreaBorderWidth(boundaryArea);
//
//			SetMultimap<LocationNode, Point2D> nodeRefPoints = LinkedHashMultimap.create();
//			Map<String, Point2D> positions = new LinkedHashMap<>();
//			
//			for(Edge<LocationNode> e : edges) {
//				if(boundaryNodes.contains(e.getFrom())) {
//					if(!boundaryNodes.contains(e.getTo())) {
//						nodeRefPoints.put(e.getFrom(), e.getTo().getCenter());
//					}
//				} else if(boundaryNodes.contains(e.getTo())) {
//					nodeRefPoints.put(e.getTo(), e.getFrom().getCenter());
//				}
//			}
//			
//			
//			nodeRefPoints.asMap().entrySet().forEach(e -> {
//				Point2D pCenter = PointUtils.getCenter(e.getValue());
//				if(pCenter == null) {
//					 pCenter = null;
//				}
//				Point2D pBR = getClosestPointOnRect(pCenter, rect);
//				 
//				positions.put(e.getKey().getId(), pBR);
//			});
//			
//			
//            ExplosionCanvasUtils.updateBoundaryNodePositionsByRemovingVisualConflicts(positions, rect, edges, w, boundaryNodes);
//			
//			// the boundary positions were only set for the visual nodes so far
//			// but the position of the boundary meta nodes will be overwritten by 
//			// the position of the center of their contained nodes ->
//			// the position of the contained nodes is set to the position of their meta node
//			Sets.intersection(collapsedNodes.keySet(), 
//					          nodeRefPoints.keySet().stream().map(n -> n.getId()).collect(Collectors.toSet())).forEach(metaKey -> {
//					        	  Point2D p = positions.get(metaKey);
//					        	  collapsedNodes.get(metaKey).forEach(k -> positions.put(k, p));
//					          });
//			
//			//Layout<LocationNode, Edge<LocationNode>> layout = this.getViewer().getGraphLayout();
//			
//			Map<String, LocationNode> nodeMap = allBoundaryNodes.stream().collect(Collectors.toMap((n) -> n.getId(), n -> n));
//			
//			for( LocationNode node: boundaryNodes ) {
//				Point2D p = positions.get(node.getId());
//				node.updateCenter(p);
//				layout.setLocation(node, p);
//				if(collapsedNodes.containsKey(node.getId())) {
//					collapsedNodes.get(node.getId()).forEach(nodeKey -> {
//						LocationNode collapsedNode = nodeMap.get(nodeKey);
//						collapsedNode.updateCenter(p);
//						layout.setLocation(collapsedNode, p);
//					});
//				}
//			}
//		}
//
//		logger.finest("leaving");
//		return boundaryArea;
//	}
	
//	public static Polygon placeBoundaryNodes(
//			Set<LocationNode> boundaryNodes, Set<LocationNode> nonBoundaryNodes, Map<String, LocationNode> nodeSaveMap, 
//			SetMultimap<String, String> boundaryNodesToInnerNodesMap, 
//			Map<String,Set<String>> collapsedNodes, Layout<LocationNode, Edge<LocationNode>> layout, Polygon invalidArea) {
//		
//		logger.finest("entered");
//
//		Polygon boundaryArea = null;
//		
//		if (!((boundaryNodes == null) || boundaryNodes.isEmpty())) {
//			
//			
//			boundaryArea = (invalidArea != null ? createBoundaryArea(invalidArea) : createBoundaryArea(getInnerBoundaryRect(nonBoundaryNodes)));
//			
//			Rectangle2D rect = getAreaRect(boundaryArea);
//			double w = getAreaBorderWidth(boundaryArea);
//
//			Map<String, Point2D> positions = new LinkedHashMap<>();
//			
//			//Set<String> updateSet = boundaryNodesToInnerNodesMap.keySet().stream().collect(Collectors.toSet());
//			
//			
//			//collapsedNodes.entrySet().forEach(e -> updateSet.removeAll(e.getValue()));
//			
//			// for(String nodeKey: updateSet) {
//			for(String nodeKey: boundaryNodesToInnerNodesMap.keySet()) {
//	
//				Point2D pCenter = PointUtils.getCenter(
//						boundaryNodesToInnerNodesMap.get(nodeKey).stream().map(key -> nodeSaveMap.get(key).getCenter())
//						.collect(Collectors.toList()));
//				
//				Point2D pBR = getClosestPointOnRect(pCenter, rect);
//				 
//				positions.put(nodeKey, pBR);
//			}
//			
//			
//           ExplosionCanvasUtils.updateBoundaryNodePositionsByRemovingVisualConflicts(positions, rect, boundaryNodesToInnerNodesMap, w, boundaryNodes);
//			
//			// the boundary positions were only set for the visual nodes so far
//			// but the position of the boundary meta nodes will be overwritten by 
//			// the position of the center of their contained nodes ->
//			// the position of the contained nodes is set to the position of their meta node
//            //Sets.intersection(collapsedNodes.keySet(), updateSet).forEach(metaKey -> {
////           Sets.intersection(collapsedNodes.keySet(), boundaryNodesToInnerNodesMap.keySet()).forEach(metaKey -> {
////			        	  Point2D p = positions.get(metaKey);
////			        	  collapsedNodes.get(metaKey).forEach(k -> positions.put(k, p));
////			          });
//
//			for( LocationNode node: boundaryNodes ) {
//				Point2D p = positions.get(node.getId());
//				node.updateCenter(p);
//				layout.setLocation(node, p);
//				if(collapsedNodes.containsKey(node.getId())) {
//					collapsedNodes.get(node.getId()).forEach(nodeId -> {
//						LocationNode collapsedNode = nodeSaveMap.get(nodeId);
//						collapsedNode.updateCenter(p);
//						layout.setLocation(collapsedNode, p);
//					});
//				}
//			}
//		}
//
//		logger.finest("leaving");
//		return boundaryArea;
//	}

    public static Polygon placeBoundaryNodes(
			Set<LocationNode> boundaryNodes, Set<LocationNode> nonBoundaryNodes, Map<String, LocationNode> nodeSaveMap, 
			SetMultimap<String, String> boundaryNodesToInnerNodesMap, 
			Map<String,Set<String>> collapsedNodes, Layout<LocationNode, Edge<LocationNode>> layout, Polygon invalidArea) {
		
		logger.finest("entered");

		Polygon boundaryArea = null;
		
		if (!((boundaryNodes == null) || boundaryNodes.isEmpty())) {
			
			
			boundaryArea = (invalidArea != null ? createBoundaryArea(invalidArea) : createBoundaryArea(getInnerBoundaryRect(nonBoundaryNodes)));
			
			Rectangle2D rect = getAreaRect(boundaryArea);
			double w = getAreaBorderWidth(boundaryArea);

//			SetMultimap<String, Point2D> nodeRefPoints = LinkedHashMultimap.create();
			Map<String, Point2D> positions = new LinkedHashMap<>();
			
			Set<String> updateSet = boundaryNodesToInnerNodesMap.keySet().stream().collect(Collectors.toSet());
			
			collapsedNodes.entrySet().forEach(e -> updateSet.removeAll(e.getValue()));
			
			for(String nodeKey: updateSet) {
	
				Point2D pCenter = PointUtils.getCenter(
						boundaryNodesToInnerNodesMap.get(nodeKey).stream().map(key -> nodeSaveMap.get(key).getCenter())
						.collect(Collectors.toList()));
				
				Point2D pBR = getClosestPointOnRect(pCenter, rect);
				 
				positions.put(nodeKey, pBR);
			}
			
			
           ExplosionCanvasUtils.updateBoundaryNodePositionsByRemovingVisualConflicts(positions, rect, boundaryNodesToInnerNodesMap, w, boundaryNodes);
			
			// the boundary positions were only set for the visual nodes so far
			// but the position of the boundary meta nodes will be overwritten by 
			// the position of the center of their contained nodes ->
			// the position of the contained nodes is set to the position of their meta node
            Sets.intersection(collapsedNodes.keySet(), updateSet).forEach(metaKey -> {
			        	  Point2D p = positions.get(metaKey);
			        	  collapsedNodes.get(metaKey).forEach(k -> positions.put(k, p));
			          });

			for( LocationNode node: boundaryNodes ) {
				Point2D p = positions.get(node.getId());
				node.updateCenter(p);
				layout.setLocation(node, p);
				if(collapsedNodes.containsKey(node.getId())) {
					collapsedNodes.get(node.getId()).forEach(nodeId -> {
						LocationNode collapsedNode = nodeSaveMap.get(nodeId);
						collapsedNode.updateCenter(p);
						layout.setLocation(collapsedNode, p);
					});
				}
			}
		}

		logger.finest("leaving");
		return boundaryArea;
	}
    
	public static SetMultimap<String, String> createBoundaryNodesToInnerNodesMap(Set<? extends Node> nonBoundaryNodes, Set<? extends Node> boundaryNodes, Set<? extends Edge<? extends Node>> edges) {
		SetMultimap<String, String> boundaryNodesToInnerNodesMap = LinkedHashMultimap.create();
		
		for(Edge<? extends Node> edge: edges) {
			if(boundaryNodes.contains(edge.getTo())) {
				if(nonBoundaryNodes.contains(edge.getFrom())) boundaryNodesToInnerNodesMap.put(edge.getTo().getId(), edge.getFrom().getId());
			} else if(boundaryNodes.contains(edge.getFrom())) {
				if(nonBoundaryNodes.contains(edge.getTo())) boundaryNodesToInnerNodesMap.put(edge.getFrom().getId(), edge.getTo().getId());
			}
		}
		
		return boundaryNodesToInnerNodesMap;
	}
	
	public static Point2D getClosestPointOnRect(Point2D pointInRect, Rectangle2D rect) {
		
		if(!(pointInRect!=null && Double.isFinite(pointInRect.getX()) && Double.isFinite(pointInRect.getY()))) return new Point2D.Double(Double.NaN,Double.NaN);
		if(!(rect!=null && Double.isFinite(rect.getX()) && Double.isFinite(rect.getY()) && Double.isFinite(rect.getWidth()) && Double.isFinite(rect.getHeight()))) return new Point2D.Double(Double.NaN,Double.NaN);
		
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
	
	
	public static void updateBoundaryNodePositionsByRemovingVisualConflicts(Map<String,Point2D> positions, Rectangle2D rect, Collection<? extends Edge<? extends Node>> edges, double distance, Set<? extends Node> boundaryNodes) {
				
		SetMultimap<String, String> boundaryNodeToInnerNodesMap = LinkedHashMultimap.create();
		
		edges.forEach(e -> {
			if(boundaryNodes.contains(e.getFrom())) {
				boundaryNodeToInnerNodesMap.put(e.getFrom().getId(), e.getTo().getId());
			} else if(boundaryNodes.contains(e.getTo())) {
				boundaryNodeToInnerNodesMap.put(e.getTo().getId(), e.getFrom().getId());
			}
		});
		
		if(!boundaryNodeToInnerNodesMap.isEmpty()) {
			
			List<BoundaryNode> sortableBoundaryNodeList = new ArrayList<>();
			
			boundaryNodeToInnerNodesMap.asMap().entrySet().forEach(e -> {
				sortableBoundaryNodeList.add(new BoundaryNode(convertToOneDimensionalPosition(positions.get(e.getKey()), rect),e.getKey(), new HashSet<String>(e.getValue())));
			});
			
			Collections.sort(sortableBoundaryNodeList);
			
			List<Point2D> res = getPositionsWithoutConflict(
					sortableBoundaryNodeList.stream().mapToDouble(e -> e.getPoint()).boxed().collect(Collectors.toList()), 
					distance, rect).stream()
					.map(e -> convertToTwoDimensionalPosition(e, rect)).collect(Collectors.toList());
			
			for(int i = 0; i < sortableBoundaryNodeList.size(); i++) positions.put(sortableBoundaryNodeList.get(i).getId(), res.get(i));
		}
	}
	
	public static void updateBoundaryNodePositionsByRemovingVisualConflicts(Map<String,Point2D> positions, Rectangle2D rect, SetMultimap<String, String> boundaryNodeToInnerNodesMap, double distance, Set<? extends Node> boundaryNodes) {
			
		if(!boundaryNodeToInnerNodesMap.isEmpty()) {
			
			List<BoundaryNode> sortableBoundaryNodeList = new ArrayList<>();
			
			boundaryNodeToInnerNodesMap.asMap().entrySet().forEach(e -> {
				sortableBoundaryNodeList.add(new BoundaryNode(convertToOneDimensionalPosition(positions.get(e.getKey()), rect),e.getKey(), new HashSet<String>(e.getValue())));
			});
			
			Collections.sort(sortableBoundaryNodeList);
			
			List<Point2D> res = getPositionsWithoutConflict(
					sortableBoundaryNodeList.stream().mapToDouble(e -> e.getPoint()).boxed().collect(Collectors.toList()), 
					distance, rect).stream()
					.map(e -> convertToTwoDimensionalPosition(e, rect)).collect(Collectors.toList());
			
			for(int i = 0; i < sortableBoundaryNodeList.size(); i++) positions.put(sortableBoundaryNodeList.get(i).getId(), res.get(i));
		}
	}
	
	private static class BoundaryNode implements Comparable<BoundaryNode> {
		private double point;
		private String id;
		private Set<String> innerNeighbourNodesIds;
		
		BoundaryNode(double point, String id, Set<String> innerNeighbourNodesIds) {
			this.point = point;
			this.id = id;
			this.innerNeighbourNodesIds = innerNeighbourNodesIds;
		}
		
		@Override
		public int compareTo(BoundaryNode arg0) {
			// TODO Auto-generated method stub
			int res = Double.compare(this.point,arg0.getPoint());
			if(res!=0) return res;
			
			res = Integer.compare(this.innerNeighbourNodesIds.hashCode(),arg0.getInnerNeighbourNodesIds().hashCode());
			if(res != 0) return res;
			
			return this.id.compareTo(arg0.getId());
		}
		
		public double getPoint() { return this.point; }
		public String getId() { return this.id; }
		public Set<String> getInnerNeighbourNodesIds() { return this.innerNeighbourNodesIds; }
	}
	
	private static List<Double> getPositionsWithoutConflict(List<Double> pointList, double nodeDistance, Rectangle2D rect) {
		List<Bucket> bucketList = new ArrayList<>();
		for(int i=0; i<pointList.size(); i++) {
			bucketList.add(new Bucket(pointList.get(i),1));
		}
		double rectLength = 2 * rect.getWidth() + 2 * rect.getHeight();
		for(int i=0; i<pointList.size(); i++) {
			bucketList.add(new Bucket(pointList.get(i)+rectLength,1));
		}
		
		double distance = Math.min(nodeDistance, rectLength/2/pointList.size());
		
		int i = 0; 
		
		while(i<bucketList.size()-1) {
			Bucket b = bucketList.get(i);
			while(i>0 && areBucketsInConflict(b,bucketList.get(i-1),distance)) {
				b.join(bucketList.get(i-1));
				bucketList.remove(i-1);
				i--;
			}
			if(areBucketsInConflict(b,bucketList.get(i+1),distance)) {
				b.join(bucketList.get(i+1));
				bucketList.remove(i+1);
			} else {
			  i++;
			}
		}
		
		List<Double> newPointList = new ArrayList<>();
		bucketList.forEach(b -> {
			for(int w=0; w<b.getWeight(); w++) {
				newPointList.add(b.getPoint()-(b.getWeight()-1)/2*distance+w*distance);
			}
		});
		
		Stream<Double> result = newPointList.subList(pointList.size(), pointList.size() + bucketList.get(0).getWeight()).stream();
		if(bucketList.get(0).getWeight() < pointList.size()) {
			result = Stream.concat(result, newPointList.subList(bucketList.get(0).getWeight(), pointList.size()).stream());
		}
		List<Double> tmp = result.collect(Collectors.toList());
		return tmp;
	}
	
	private static class Bucket {
		double point;
		int weight;
		
		public Bucket(double point, int weight) {
			this.point = point;
			this.weight = weight;
		}
		
		public double getPoint() { return this.point; }
		public int getWeight() { return this.weight; }
		
		public void join(Bucket b) {
		  this.point = (this.point * this.weight + b.getPoint() * b.getWeight())/(this.weight + b.getWeight());	
		  this.weight+= b.getWeight();
		}
	}
	
	private static boolean areBucketsInConflict(Bucket b1, Bucket b2, double distance) {
		if(b1.getPoint()<=b2.getPoint()) {
			return b2.getPoint()-(b2.getWeight()+1)/2*distance < b1.getPoint()+(b1.getWeight()+1)/2*distance;
		} else {
			return b1.getPoint()-(b1.getWeight()+1)/2*distance < b2.getPoint()+(b2.getWeight()+1)/2*distance;
		}
	}
	
	private static double convertToOneDimensionalPosition(Point2D p, Rectangle2D rect) {
		double dT = p.getY() - rect.getY();
		double dR = rect.getX() + rect.getWidth() - p.getX();
		double dB = rect.getY() + rect.getHeight() - p.getY();
		double dL = p.getX() - rect.getX();
		double min = Collections.min(Arrays.asList(dT, dR, dB, dL));
		
		if (dT == min) {
			return Math.max(0, dL);
		} else if (dR == min) {
			return rect.getWidth() + Math.max(0, dT);
		} else if (dB == min) {
			return rect.getWidth() + rect.getHeight() + Math.max(0, dR);
		} else if (dL == min) {
			return 2 * rect.getWidth() + rect.getHeight() + Math.max(0, dB);
		} 
		
		throw new RuntimeException("This should not happen");
	}
	
	private static Point2D convertToTwoDimensionalPosition(double p, Rectangle2D rect) {
		double d = rect.getWidth() + rect.getHeight();
		double u = 2 * d;
		
		while (p < 0) p+= u;
		while (p > u) p-= u;
		
		if (p <= d) {
			if (p <= rect.getWidth()) {
				return new Point2D.Double(rect.getX() + p, rect.getY());
			} else {
				return new Point2D.Double(rect.getX() + rect.getWidth(), rect.getY() + p - rect.getWidth());
			}
		} else {
			if (p <= d + rect.getWidth()) {
				return new Point2D.Double(rect.getX() + rect.getWidth() - (p - d), rect.getY() + rect.getHeight());
			} else {
				return new Point2D.Double(rect.getX(), rect.getY() + rect.getHeight() - (p - d - rect.getWidth()));
			}
		}
	}
	
	public static class LabelPaintable implements Paintable, MouseMotionListener, MouseListener {

		private VisualizationViewer<?, ?> viewer;

		private Rectangle closeRect;

		private boolean closeFocused;
		
		private String label;
		private Runnable closeMethod;
		
		protected LabelPaintable(VisualizationViewer<?, ?> viewer, String label) {
			this(viewer, label, null);
		}
		
		protected LabelPaintable(VisualizationViewer<?, ?> viewer, String label, Runnable closeMethod) {
			this.viewer = viewer;
			this.label = label + " Explosion View";
			this.closeMethod = closeMethod;
			
			closeRect = null;
			closeFocused = false;
			
			if(!this.labelOnly()) {
			  viewer.addMouseMotionListener(this);
			  viewer.addMouseListener(this);
			}
		}
		
		private boolean labelOnly() { return this.closeMethod == null; }

		@Override
		public void paint(Graphics graphics) {
			int w = this.viewer.getSize().width;
			int lineWidth = 2;

						
			Graphics2D g = (Graphics2D) graphics;
			
			Font font = new Font("Default", Font.BOLD, 10);
			int fontHeight = g.getFontMetrics(font).getHeight();
			int fontAscent = g.getFontMetrics(font).getAscent();
			
			int dy = 2;

			int dx = 5;
			
			
			int sw = (int) font.getStringBounds(this.label, g.getFontRenderContext()).getWidth();
			
			Color currentColor = g.getColor();
			Stroke currentStroke = g.getStroke();

			if(!this.labelOnly()) this.closeRect = new Rectangle(w/2 + sw/2 + dx - fontAscent/2 - dx/2, dy ,fontAscent,fontAscent);
			
			g.setColor(ZoomingPaintable.BACKGROUND);
			g.fillRect(w/2 - sw/2 - dx - (this.labelOnly()?0:this.closeRect.width/2+dx/2) , 0 , sw + 2*dx + (this.labelOnly()?0:this.closeRect.width+dx), fontHeight + 2*dy);
			g.setColor(Color.BLACK);
			g.drawRect(w/2 - sw/2 - dx - (this.labelOnly()?0:this.closeRect.width/2+dx/2), 0 , sw + 2*dx + (this.labelOnly()?0:this.closeRect.width+dx), fontHeight + 2*dy);
			g.setFont(font);
			g.drawString(this.label, w/2 - sw/2 - (this.labelOnly()?0:this.closeRect.width/2+dx/2), dy+fontAscent);
			if(!this.labelOnly()) {
				g.setStroke(new BasicStroke(lineWidth));
				if(this.closeFocused) g.setColor(Color.BLUE);
				g.drawLine(this.closeRect.x,this.closeRect.y,this.closeRect.x+this.closeRect.width,this.closeRect.y+this.closeRect.height);
				g.drawLine(this.closeRect.x,this.closeRect.y+this.closeRect.height,this.closeRect.x+this.closeRect.width,this.closeRect.y);
			}
			g.setColor(currentColor);
			g.setStroke(currentStroke);

		}
		
		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			boolean newCloseFocused = closeRect != null && closeRect.contains(e.getPoint());
			boolean changed = newCloseFocused != closeFocused;

			closeFocused = newCloseFocused;

			if (changed) {
				BetterGraphMouse<?, ?> graphMouse = (BetterGraphMouse<?, ?>) viewer.getGraphMouse();

				graphMouse.setPickingDeactivated(closeFocused);
				paint(viewer.getGraphics());
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && (closeFocused)) {
				try {

					this.closeMethod.run();
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
	}
	
}
