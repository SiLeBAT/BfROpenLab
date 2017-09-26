package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
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
	
	public static Map<String, Set<String>> filterExplosedNode(Map<String, Set<String>> nodes, String explodedNodeKey) {
		return nodes.entrySet().stream().filter(e->e.getKey()!=explodedNodeKey).collect(Collectors.toMap(e->e.getKey(),e->e.getValue()));
	}
	
	public static boolean isPointOnRect(Point2D p, Rectangle2D rect) {
		return getClosestPointOnRect(p,rect).distance(p) < Double.max(rect.getWidth(),rect.getHeight())/1000;
	}
	
	public static boolean arePointsEquallyDistant(Point2D pFrom, Point2D pTo1, Point2D pTo2) {
		double d1 = pFrom.distance(pTo1);
		double d2 = pFrom.distance(pTo2);
		return d1==d2 || Math.abs(d1-d2)/Math.max(d1, d2) < 0.0001;
	}

	public static Point2D getClosestPointOnRect(Point2D pointInRect, Rectangle2D rect) {
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
	
	public static void updateBoundaryPositionsByRemovingVisualConflicts(Map<String,Point2D> positions, Rectangle2D rect, Set<? extends Edge<? extends Node>> edges, double distance, List<? extends Node> boundaryNodes) {
		Map<Point2D, SetMultimap<Set<String>, String>> myMap = new LinkedHashMap<>();
				
		SetMultimap<String, String> boundaryNodeToInnerNodes = LinkedHashMultimap.create();
		//SetMultimap<Set<String>, String> innerNodeSetToBoundaryNodes = LinkedHashMultimap.create();
		edges.forEach(e -> {
			if(boundaryNodes.contains(e.getFrom())) {
				boundaryNodeToInnerNodes.put(e.getFrom().getId(), e.getTo().getId());
			} else if(boundaryNodes.contains(e.getTo())) {
				boundaryNodeToInnerNodes.put(e.getTo().getId(), e.getFrom().getId());
			}
		});
		
		positions.values().forEach( p -> myMap.put(p, LinkedHashMultimap.create()));
		
		boundaryNodes.forEach(n -> {
			myMap.get(positions.get(n.getId())).put(new LinkedHashSet<>(boundaryNodeToInnerNodes.get(n.getId())), n.getId());
		});
		
		
		
		boundaryNodeToInnerNodes.asMap().entrySet().forEach(e -> {
			innerNodeSetToBoundaryNodes.put(new LinkedHashSet<>(e.getValue()), e.getKey());
		});
		
		
		
		Map.Entry<K, V>
		SetMultimap<Point2D, String> nodeRefPoints = LinkedHashMultimap.create();
		List<Point2D> p2DList;
		List<Double> p1DList = p2DList.stream().map(p2D -> convertToOneDimensionalPosition(p2D, rect)).collect(Collectors.toList());
	}
	
	private static List<Double> fitDistances(List<Double> pointList, double distance, Rectangle2D rect) {
		List<Bucket> bucketList = new ArrayList<>();
		for(int i=0; i<pointList.size(); i++) {
			bucketList.add(new Bucket(pointList.get(i),1));
		}
		double rectLength = 2 * rect.getWidth() + 2 * rect.getHeight();
		for(int i=0; i<pointList.size(); i++) {
			bucketList.add(new Bucket(pointList.get(i)+rectLength,1));
		}
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
			}
			i++;
		}
		
		List<Double> newPointList = new ArrayList<>();
		bucketList.forEach(b -> {
			for(int w=0; w<b.getWeight(); w++) {
				newPointList.add(b.getPoint()-(b.getWeight()-1)/2*distance+w*distance);
			}
		});
		
		return Stream.concat(
				newPointList.subList(pointList.size(), pointList.size()-1+bucketList.get(0).getWeight()).stream(),
				newPointList.subList(bucketList.get(0).getWeight(), pointList.size()-1).stream()
				).collect(Collectors.toList());
	}
	
	private static LinkedHashMap<String, Point2D> getSortedPositionList() {
		
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
