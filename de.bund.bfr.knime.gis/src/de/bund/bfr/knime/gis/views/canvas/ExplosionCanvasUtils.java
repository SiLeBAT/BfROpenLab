package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

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
	
//	public static void logInfo(String text) {
//		jlog.log(Level.INFO, text);
//	}
	
	
}
