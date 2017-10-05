package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.commons.collections15.Transformer;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Formatter;

public class ExplosionCanvasUtils {

	private static final GeometryFactory FACTORY = new GeometryFactory();
	private static Logger logger =  Logger.getLogger("de.bund.bfr");

	public static final double BOUNDARY_MARGIN = 0.2;
	public static final double BOUNDARY_WIDTH = 10;
	
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
	
	public static <N extends Node> Set<N> removeOuterNodes(Set<N> nodes, Set<N> explodedNodes, Set<N> boundaryNodes) {
		Set<N> toRemove  = nodes.stream().filter(n -> !(explodedNodes.contains(n) || boundaryNodes.contains(n))).collect(Collectors.toSet());

		nodes.removeAll(toRemove);
		return toRemove;
	}
	
	public static Map<String, Set<String>> filterCollapsedNodeAccordingToExplosion(Map<String, Set<String>> collapsedNodes, String explodedNodeKey, Set<String> retainNodes) {
//		List<Entry<String,Set<String>>> myMap = collapsedNodes.entrySet().stream().filter(e->e.getKey()!=explodedNodeKey && !Sets.intersection(e.getValue(), retainNodes).isEmpty()).collect(Collectors.toList());
//		Map<String, Set<String>> test = collapsedNodes.entrySet().stream()
//				.filter(e->e.getKey()!=explodedNodeKey && !Sets.intersection(e.getValue(), retainNodes).isEmpty())
//				.collect(Collectors.toMap(e->e.getKey(),e->Sets.intersection(retainNodes,e.getValue())));
		return collapsedNodes.entrySet().stream()
				.filter(e->e.getKey()!=explodedNodeKey && !Sets.intersection(e.getValue(), retainNodes).isEmpty())
				.collect(Collectors.toMap(e->e.getKey(),e->Sets.intersection(retainNodes,e.getValue())));
	}
	
	public static boolean isPointOnRect(Point2D p, Rectangle2D rect) {
		return getClosestPointOnRect(p,rect).distance(p) < Double.max(rect.getWidth(),rect.getHeight())/1000;
	}
	
	public static boolean arePointsEquallyDistant(Point2D pFrom, Point2D pTo1, Point2D pTo2) {
		double d1 = pFrom.distance(pTo1);
		double d2 = pFrom.distance(pTo2);
		return d1==d2 || Math.abs(d1-d2)/Math.max(d1, d2) < 0.0001;
	}
	
	public static Rectangle2D getNonBoundaryRect(Canvas canvas) {
		//Rectangle outerBounds = canvas.getViewer().getBounds();
		Dimension size = canvas.getViewer().getSize();
		//double size = Math.max(size.getWidth(), size.getHeight());
		
//		if (size == 0.0) {
//			size = 1.0;
//		}
		
		double margin = (Math.max(size.getWidth(), size.getHeight()) - BOUNDARY_WIDTH) / (1 + 2 * BOUNDARY_MARGIN);
		
		return new Rectangle2D.Double(
				0 + margin + BOUNDARY_WIDTH / 2, 
				0 + margin + BOUNDARY_WIDTH / 2, 
				size.getWidth() - 2 * margin - BOUNDARY_WIDTH, 
				size.getHeight() - 2 * margin - BOUNDARY_WIDTH);
				
//		Transformer<GraphNode, Shape> vertexShapeTransformer = this.getViewer().getRenderContext().getVertexShapeTransformer();
//		
//		
//		
//		double refNodeSize = this.boundaryNodes.stream().map(n -> vertexShapeTransformer.transform(n).getBounds().getSize().getWidth()).max(Double::compare).orElse(0.0);    // this.getEdgeWeights().entrySet().stream().filter(e -> boundaryNodesIds.contains(e.getKey())).map(e -> e.getValue()).max(Double::compare).orElse(0.0);
//				
//		double d = Double.max(BOUNDARY_MARGIN * size, refNodeSize*5);
	}
//	
//	public static getBoundaryRect(Rectangle innerBounds, double relativeMargin, double ) {
//	
//	}

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
	
	
	public static void updateBoundaryNodePositionsByRemovingVisualConflicts(Map<String,Point2D> positions, Rectangle2D rect, Set<? extends Edge<? extends Node>> edges, double distance, Set<? extends Node> boundaryNodes) {
		//Map<Point2D, SetMultimap<Set<String>, String>> myMap = new LinkedHashMap<>();
				
		SetMultimap<String, String> boundaryNodeToInnerNodesMap = LinkedHashMultimap.create();
		
		edges.forEach(e -> {
			if(boundaryNodes.contains(e.getFrom())) {
				boundaryNodeToInnerNodesMap.put(e.getFrom().getId(), e.getTo().getId());
			} else if(boundaryNodes.contains(e.getTo())) {
				boundaryNodeToInnerNodesMap.put(e.getTo().getId(), e.getFrom().getId());
			}
		});
		
		List<BoundaryNode> sortableBoundaryNodeList = new ArrayList<>();
		
		//positions.values().forEach( p -> myMap.put(p, LinkedHashMultimap.create()));
		
		boundaryNodeToInnerNodesMap.asMap().entrySet().forEach(e -> {
			sortableBoundaryNodeList.add(new BoundaryNode(convertToOneDimensionalPosition(positions.get(e.getKey()), rect),e.getKey(), new HashSet<String>(e.getValue())));
		});
		
		Collections.sort(sortableBoundaryNodeList);
		
		DoubleStream tmp = sortableBoundaryNodeList.stream().mapToDouble(e -> e.getPoint());
		double[] tmpA = tmp.toArray();
		Stream<Double> tmp2 = sortableBoundaryNodeList.stream().mapToDouble(e -> e.getPoint()).boxed();
		Object[] tmp2A = tmp2.toArray();
		List<Double> tmp3 = sortableBoundaryNodeList.stream().mapToDouble(e -> e.getPoint()).boxed().collect(Collectors.toList());
		
		List<Point2D> res = getPositionsWithoutConflict(
				sortableBoundaryNodeList.stream().mapToDouble(e -> e.getPoint()).boxed().collect(Collectors.toList()), 
				distance, rect).stream()
				.map(e -> convertToTwoDimensionalPosition(e, rect)).collect(Collectors.toList());
		
		for(int i = 0; i < sortableBoundaryNodeList.size(); i++) positions.put(sortableBoundaryNodeList.get(i).getId(), res.get(i));
	}
	
	private static class BoundaryNode implements Comparable<BoundaryNode> {
		private double point;
		private String id;
		private Set<String> innerNeighbourNodesIds;
		
		BoundaryNode(double point, String id, Set<String> innerNeighbourNodesIds) {
			this.point = point;
			this.id = id;
			this.innerNeighbourNodesIds = innerNeighbourNodesIds;
			//Collections.sort(this.innerNeighbourNodesIds);
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
	
	
	
}
