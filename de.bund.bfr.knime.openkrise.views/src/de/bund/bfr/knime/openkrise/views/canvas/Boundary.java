package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Polygon;
import de.bund.bfr.jung.MoveController;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;

public class Boundary implements MoveController<GraphNode> {
  
//  private static final double BOUNDARY_AREA_RELATIVE_MARGIN = LocationCanvasUtils.INVALID_AREA_RELATIVE_MARGIN; //    0.2;
//  private static final double BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH = LocationCanvasUtils.INVALID_AREA_RELATIVE_BORDERWIDTH; // 0.02;
//  private static final double BOUNDARY_WIDTH = 5; // 10
//  private static final double STATION_DISTANCE = 10;
  
  private Polygon boundaryArea;
  //private Shape boundaryArea;
  private Rectangle2D boundaryRect;
  private Rectangle2D[] sideAreas; 
  private double boundaryWidth;
    
//  private Map<GraphNode, Set<GraphNode>> bNodeToNonBNodeSetMap;
//  private Map<Set<GraphNode>, GraphNode> nonBNodeSetToBNodeSetMap;
//  private Map<Set<GraphNode>, List<GraphNode>> nonBNodeSetToBNodeListMap;
//  private Map<String, Point2D> nodePositionMap;
  private Set<String> boundaryNodeIds;
  
  private Point2D lastDragPosition;
  private double rotationAngle;
  
  
//  Boundary(Set<V> nonBoundaryNodes, Set<V> boundaryNodes, Set<E> edges, Dimension availableAreaSize) {
//    resetBoundaryBasedOnTotalArea(availableAreaSize);
//    //this.initMaps();
//  }
  
  Boundary() {
    //resetBoundaryBasedOnTotalArea(availableAreaSize);
    //this.initMaps();
  }
  
//  Boundary(Set<V> nonBoundaryNodes, Set<V> boundaryNodes, Set<E> edges, Rectangle2D layoutArea, Map<String, Point2D> innerNodePositions) {
//      this.initMaps();
//      this.initNodePositions(boundaryNodes);;
//  }
  
//  private void initNodePositions(Map<String, Point2D> nodePositions) {
//    Random random = new Random();
//    double u = 2*(boundaryRect.getHeight() + boundaryRect.getWidth());
//    this.nodePositionMap = new HashMap<>();
//    for(String nodeId: boundaryNodeIds) this.nodePositionMap.put(nodeId, convertToTwoDimensionalPosition(random.nextDouble()*u, boundaryRect));
//  }
  
  Boundary(Set<GraphNode> nonBoundaryNodes, Set<GraphNode> boundaryNodes, Set<Edge<GraphNode>> edges, Map<GraphNode, Point2D> boundaryNodePositions, Rectangle2D boundaryPosition) {
    this.initMaps();
     
  }
  
  protected void paint(Graphics2D g, Canvas<GraphNode> canvas) {
    BufferedImage bufferedImage = new BufferedImage(canvas.getCanvasSize().width, canvas.getCanvasSize().height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D imgGraphics = bufferedImage.createGraphics();
    
    Shape boundaryArea = canvas.getTransform().apply(this.boundaryArea);
       
    imgGraphics.setPaint(CanvasUtils.mixColors(Color.WHITE, Arrays.asList(Color.GREEN, Color.WHITE),
            Arrays.asList(1.0, 1.0), false));
    imgGraphics.fill(boundaryArea);
    imgGraphics.setColor(Color.BLACK);
    imgGraphics.draw(boundaryArea);

    //CanvasUtils.drawImageWithAlpha(g, boundaryAreaImage, 75);
    CanvasUtils.drawImageWithAlpha(g, bufferedImage, 75);
    //boundaryAreaImage.flush();
    bufferedImage.flush();
}
  
  private void initMaps() {
    
  }
  
//  protected void resetBoundaryBasedOnInternalArea(Polygon internalArea) {
//    
//  }
//  
//  protected void resetBoundaryBasedOnTotalArea(Polygon totalArea) {
//    
//  }
  
  protected void update(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions) {
    //Rectangle rect =
    Rectangle2D innerHull = getBounds(canvas, nodePositions);
    //Rectangle2D defaultBounds = ExplosionCanvasUtils.getInnerBoundaryRect(canvas);
    
    Rectangle2D oldBoundaryRect = this.boundaryRect;
    //boundaryArea = ExplosionCanvasUtils.createBoundaryArea(innerHull, canvas);
    this.setBoundaryArea(ExplosionCanvasUtils.createBoundaryArea(innerHull, canvas));
    //boundaryRect = ExplosionCanvasUtils.getAreaRect(boundaryArea);
    
    //double newBoundaryWidth = ExplosionCanvasUtils.getAreaBorderWidth(this.boundaryArea);
    
    if(oldBoundaryRect!=null) moveNodesToUpdatedBoundary(canvas, nodePositions, oldBoundaryRect);
  }
  
  private void setBoundaryArea(Polygon newBoundaryArea) {
    boundaryArea = newBoundaryArea;
    boundaryRect = ExplosionCanvasUtils.getAreaRect(boundaryArea);
    boundaryWidth = ExplosionCanvasUtils.getAreaBorderWidth(boundaryArea);
    this.sideAreas = new Rectangle2D[] {
      new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryRect.getWidth() + boundaryWidth, boundaryWidth), //top
      new Rectangle2D.Double(boundaryRect.getMaxX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryWidth, boundaryRect.getHeight() + boundaryWidth), //right
      new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getMaxY()-boundaryWidth/2, boundaryRect.getWidth() + boundaryWidth, boundaryWidth), //bottom
      new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryWidth, boundaryRect.getHeight() + boundaryWidth) //left
    };
  }
  
  public static Rectangle2D getBounds(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions) {
    Rectangle2D bounds = null;
    
    for(GraphNode node : canvas.getLayoutableNodes()) {
      Point2D point = nodePositions.get(node.getId());
      if(point!=null) {
        if(bounds==null) bounds = new Rectangle2D.Double(point.getX(), point.getY(), 0, 0);
        else bounds.add(point);
      }
    }

    return bounds;
}
  
//  protected void moveNodesToUpdatedBoundary(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions, Rectangle2D oldBoundaryRect) {
//    AffineTransform at = new AffineTransform();
//    Point2D fromCenter = new Point2D.Double(oldBoundaryRect.getCenterX(), oldBoundaryRect.getCenterY());
//    Point2D toCenter = new Point2D.Double(boundaryRect.getCenterX(), boundaryRect.getCenterY());
//    Point2D.
//    at.translate(boundaryRect.getX()-oldBoundaryRect.getX(), boundaryRect.getY()-oldBoundaryRect.getY());
//    at.scale(boundaryRect.getWidth()-oldBoundaryRect.getWidth(), boundaryRect.getHeight()-oldBoundaryRect.getHeight() );
//    for(GraphNode node : canvas.getBoundaryNodes()) {
//      Point2D oldPosition = nodePositions.get(node.getId());
//      if(oldPosition!=null) nodePositions.put(node.getId(), at.transform(oldPosition, null));
//    }
//  }
  
  protected void moveNodesToUpdatedBoundary(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions, Rectangle2D oldBoundaryRect) {
    AffineTransform at = new AffineTransform();
//    Point2D fromCenter = new Point2D.Double(oldBoundaryRect.getCenterX(), oldBoundaryRect.getCenterY());
//    Point2D toCenter = new Point2D.Double(boundaryRect.getCenterX(), boundaryRect.getCenterY());
//    Point2D trans = PointUtils.substractPoints(toCenter, fromCenter);
    
    //Point2D trans = new Point2D.Double(oldBoundaryRect.getCenterX() - oldBoundaryRect.getCenterY(), oldBoundaryRect.getCenterY() - oldBoundaryRect.getCenterY())
    //Point2D scale = new Point2D.Double(boundaryRect.getWidth()/oldBoundaryRect.getWidth(), boundaryRect.getHeight()/oldBoundaryRect.getHeight());
//    Point2D trans = new Point2D.Double(boundaryRect.getCenterX()-oldBoundaryRect.getCenterX(), boundaryRect.getCenterY()-oldBoundaryRect.getCenterY());
//    Point2D scale = 
    
    at.translate(boundaryRect.getCenterX()-oldBoundaryRect.getCenterX(), boundaryRect.getCenterY()-oldBoundaryRect.getCenterY());
    at.scale(boundaryRect.getWidth()/oldBoundaryRect.getWidth(), boundaryRect.getHeight()/oldBoundaryRect.getHeight() );
    for(GraphNode node : canvas.getBoundaryNodes()) {
      Point2D oldPosition = nodePositions.get(node.getId());
      if(oldPosition!=null) nodePositions.put(node.getId(), at.transform(oldPosition, null));
    }
  }
  
  protected void layout(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions, Set<GraphNode> nodesToLayout) {
    for(GraphNode node: nodesToLayout) {
      nodePositions.put(node.getId(), getRandomPointOnBoundary());
    }
  }
  
  protected void putNodesOnBoundary(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions) {
    //ArrayList<GraphNode> nodesWithoutPosition = new ArrayList<>();
    
    for(GraphNode node : canvas.getNodes()) {
      // this node should be visible 
      if(boundaryNodeIds.contains(node.getId())) {
        // this is a boundary node
        Point2D oldPosition = nodePositions.get(node.getId());
        
          Point2D pointOnRect = ExplosionCanvasUtils.getClosestPointOnRect(oldPosition, boundaryRect);
        
          Point2D diff = PointUtils.substractPoints(oldPosition, pointOnRect);
          double maxDiff = Math.max(Math.abs(diff.getX()), Math.abs(diff.getY()));
          if(maxDiff>boundaryWidth/2) {
            // node is not on the boundary
            // this node
            nodePositions.put(node.getId(), pointOnRect);
          }
        
      }
    }
    //if(!nodesWithoutPosition.isEmpty()) this.layout(canvas,  nodePositions, nodesWithoutPosition);
  }
  
  protected Point2D getRandomPointOnBoundary() {
    Random rnd = new Random();
    switch(rnd.nextInt(4)) {
      case 0: 
        // random point on left side
        return new Point2D.Double(boundaryRect.getX() - boundaryWidth/2 + boundaryWidth * rnd.nextDouble(), 
            boundaryRect.getY() + boundaryRect.getHeight()*rnd.nextDouble());
      case 1:
        // random point on top side 
        return new Point2D.Double(boundaryRect.getX() + boundaryRect.getWidth() * rnd.nextDouble(), 
            boundaryRect.getY() - boundaryWidth/2 + boundaryWidth * rnd.nextDouble() );
      case 2:
        // random point on right side
        return new Point2D.Double(boundaryRect.getX() + boundaryRect.getWidth() - boundaryWidth/2 + boundaryWidth * rnd.nextDouble(), 
            boundaryRect.getY() + boundaryRect.getHeight()*rnd.nextDouble());
      case 3:
        // random point on bottom side 
        return new Point2D.Double(boundaryRect.getX() + boundaryRect.getWidth() * rnd.nextDouble(), 
            boundaryRect.getY() + boundaryRect.getHeight() - boundaryWidth/2 + boundaryWidth * rnd.nextDouble() );
      default:
        // this is supposed to happen
        return null;
    }
  }

  @Override
  public Point2D move(GraphNode node, Point2D fromPosition, Point2D movement) {
    return move(node.getId(), fromPosition, movement);
//    Point2D newPosition = PointUtils.addPoints(fromPosition, movement);
//    if(boundaryNodeIds!=null && boundaryNodeIds.contains(node.getId())) {
//      Point2D pointOnRect = ExplosionCanvasUtils.getClosestPointOnRect(newPosition, boundaryRect);
//      Point2D diff = PointUtils.substractPoints(newPosition, pointOnRect);
//      double maxDiff = Math.max(Math.abs(diff.getX()), Math.abs(diff.getY()));
//      if(maxDiff<=boundaryWidth/2) {
//        // newPosition is on boundary
//        return newPosition;
//      } return PointUtils.addPoints(pointOnRect, scaleMove(diff, boundaryWidth/2/maxDiff));
//    } else return newPosition;
  }
  
  public Point2D move(String nodeId, Point2D fromPosition, Point2D movement) {
    Point2D newPosition = PointUtils.addPoints(fromPosition, movement);
    if(boundaryNodeIds!=null && boundaryNodeIds.contains(nodeId)) {
      Point2D pointOnRect = ExplosionCanvasUtils.getClosestPointOnRect(newPosition, boundaryRect);
      Point2D diff = PointUtils.substractPoints(newPosition, pointOnRect);
      double maxDiff = Math.max(Math.abs(diff.getX()), Math.abs(diff.getY()));
      if(maxDiff<=boundaryWidth/2) {
        // newPosition is on boundary
        return newPosition;
      } return PointUtils.addPoints(pointOnRect, scaleMove(diff, boundaryWidth/2/maxDiff));
    } else return newPosition;
  }
  
  
  private static Point2D scaleMove(Point2D p, double scale) {
    return new Point2D.Double(p.getX()*scale, p.getY()*scale);
  }
  
  protected void setNodes(Set<GraphNode> nodes) {
    this.boundaryNodeIds = nodes.stream().map(GraphNode::getId).collect(Collectors.toSet());
  }

  @Override
  public Map<GraphNode, Point2D>  move(Set<GraphNode> nodes, Map<GraphNode, Point2D> oldPositions, Point2D movement, GraphNode dragNode) {
    Map<GraphNode, Point2D> newPositions = new HashMap<>(oldPositions);
    
//    //ArrayList<GraphNode> boundaryNodes = nodes.stream().filter(n -> this.boundaryNodeIds.contains(n.getId())).collect(Collectors.toList());
    Map<GraphNode, Point2D> newBoundaryPositions = new HashMap<>();
    
    for(GraphNode node : nodes) {
      if(boundaryNodeIds.contains(node.getId())) newBoundaryPositions.put(node, PointUtils.addPoints(oldPositions.get(node), movement));
      else newPositions.put(node, PointUtils.addPoints(oldPositions.get(node), movement));
    }
    
    boolean allPointsOnBoundary = true;
    for(Point2D p : newBoundaryPositions.values()) if(!(allPointsOnBoundary=isPointOnBoundary(p))) break;
    
    if(allPointsOnBoundary) {
      //
      newPositions.putAll(newBoundaryPositions);
      
    } else {
      
      if(newBoundaryPositions.size()!=nodes.size()) {
        // not only Boundary nodes
        // use Projection
        for(GraphNode node : newBoundaryPositions.keySet()) newPositions.put(node, move(node.getId(), oldPositions.get(node), movement));
      } else {
        // only boundary Nodes
        Point2D dragPoint = PointUtils.addPoints(oldPositions.get(dragNode), movement);
        if(isPointOnBoundary(dragPoint)) {
          // dragPoint is on boundary
          // perform compact move
          newPositions.putAll(compactMove(oldPositions, dragNode, dragPoint));
        } else {
          // dragPoint is not on Boundary
          // use Projection
          for(GraphNode node : newBoundaryPositions.keySet()) newPositions.put(node, move(node.getId(), oldPositions.get(node), movement));
        }
      }
    }
    return newPositions;
  }
  
  private boolean isPointOnBoundary(Point2D p) {
    int i=-1;
    for(Rectangle2D rect : sideAreas) if(rect.contains(p)) {
      //i++;
      //System.out.println("Point " + p.toString() + " is on Boundary(" + i + ") " + rectToString(boundaryRect) + ".");
      return true;
    }
    //System.out.println("Point " + p.toString() + " is not on Boundary " + rectToString(boundaryRect) + ".");
    return false;
  }
  
  private static String rectToString(Rectangle2D rect) {
    return "[" + rect.getX() + "," + rect.getY() + "," + rect.getMaxX() + "," + rect.getMaxY() + "]";
  }
  
  private HashMap<GraphNode, Point2D> compactMove(Map<GraphNode, Point2D> oldBoundaryPositions, GraphNode dragNode, Point2D dragPoint) {
    //HashMap<GraphNode, Point2D> oldBoundaryPositions = new HashMap<>(oldPositions.entrySet().stream().filter(e -> boundaryNodeIds.contains(e.getKey().getId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    HashMap<GraphNode, Point2D> newPositions = new HashMap<>();
    
    double dragPoint1D = convertToOneDimensionalPosition(ExplosionCanvasUtils.getClosestPointOnRect(dragPoint, boundaryRect), boundaryRect);
    double movement1D = dragPoint1D - convertToOneDimensionalPosition(ExplosionCanvasUtils.getClosestPointOnRect(oldBoundaryPositions.get(dragNode), boundaryRect), boundaryRect);
    
    //Set<String> nodeIds = Sets.intersection(boundaryNodeIds, oldPositions.keySet());
    for(GraphNode node: oldBoundaryPositions.keySet()) {
      Point2D oldPointOnRect2D = ExplosionCanvasUtils.getClosestPointOnRect(oldBoundaryPositions.get(node), boundaryRect);
      Double oldPointOnRect1D = convertToOneDimensionalPosition(oldPointOnRect2D,  boundaryRect );
      Double newPointOnRect1D = oldPointOnRect1D + movement1D;
      
      Point2D oldPointDistanceToRect2D = PointUtils.substractPoints(oldBoundaryPositions.get(node), oldPointOnRect2D);
      double oldPointDistanceToRect1D = (boundaryRect.contains(oldBoundaryPositions.get(node))?-1:1) * Math.max(Math.abs(oldPointDistanceToRect2D.getX()), Math.abs(oldPointDistanceToRect2D.getY()));
      
      Point2D newPoint2D = convertToTwoDimensionalPosition(newPointOnRect1D, oldPointDistanceToRect1D, boundaryRect);
      newPositions.put(node, newPoint2D);
    }
    return newPositions;
  }
  
  private static double convertToOneDimensionalPosition(Point2D p, Rectangle2D rect) {
    
    if(p.getY()==rect.getY()) {
      // p is on the top side
      return p.getX()-rect.getX();
    } else if(p.getY()==rect.getMaxY()) {
      // p is on the bottom side
      return rect.getMaxX()-p.getX()+rect.getWidth()+rect.getHeight();
    } else if(p.getX()==rect.getX()) {
      // p is on the left side
      return rect.getMaxY()-p.getY()+2*rect.getWidth()+rect.getHeight();
    } else {
      // p is on the right side
      return rect.getMaxY()-p.getY()+rect.getWidth();
    }
  }
  
  private static Point2D convertToTwoDimensionalPosition(Double p, Double dist, Rectangle2D rect) {
    // normalize p if required
    double circumference = 2*rect.getWidth() + 2*rect.getHeight();
    if(p>=circumference) p = p%circumference;
    if(p<0) p += circumference;
    
    double pathLength = 0.0;
    if(p <= (pathLength += rect.getWidth())) {
      // p is on the top side
      return new Point2D.Double(rect.getX() + p, rect.getMinY()-dist);
    } else if(p <= (pathLength += rect.getHeight())) {
      // p is on the right side
      return new Point2D.Double(rect.getMaxX()  + dist, rect.getY() + p - pathLength + rect.getHeight());
    } else if(p <= (pathLength += rect.getWidth())) {
      // p is on the bottom side
      return new Point2D.Double(rect.getMinX() - (p - pathLength), rect.getMaxY() + dist);
    } else {
      // p is on the left side
      return new Point2D.Double(rect.getX() - dist, rect.getMaxY() - (p - pathLength));
    }
  }

//  @Override
//  public Point2D move(GraphNode node, Point2D fromPosition, Point2D movement,
//      Point2D dragPosition) {
//    if(boundaryNodeIds==null || !boundaryNodeIds.contains(node.getId())) {
//      return PointUtils.addPoints(fromPosition, movement);
//    } else {
//     if(lastDragPosition==null || !dragPosition.equals(lastDragPosition)) {
//       lastDragPosition = dragPosition;
//       rotationAngle = getRotationAngle(fromPoint, toPoint);
//       
//     }
//    }
//  }
//  
//  private double getRotationAngle(Point2D fromPoint, Point2D toPoint) {
//    
//    return Math.acos( 
//  }
//  
//  private Point2D rotatePoint(Point2D point) {
//    
//  }
 
//  private Rectangle2D getEnclosingHullFromLayoutableNodes(ExplosionTracingGraphCanvas canvas) {
//    Map<String, Point2D> positions = canvas.getNodePositions(canvas.getLayoutableNodes());
//  
//  // safety test
//  if(positions == null) return;
//  
//  /* the positions might not be available for all nodes because e.g. the date filter specified by the user 
//   * might remove some inner nodes from the graph so the layout methods do not set their position
//   * --> these empty position are set to the left upper corner of default bounds
//   */
//  
//  
//  Rectangle2D defaultBounds = ExplosionCanvasUtils.getInnerBoundaryRect(this);
//  
//  Set<String> nodesWithoutPosition = positions.entrySet().stream().filter(e -> Double.isNaN(e.getValue().getX()) || Double.isNaN(e.getValue().getY())).map(e -> e.getKey()).collect(Collectors.toSet());
//  
//  nodesWithoutPosition.forEach(nodeId -> positions.put(nodeId, new Point2D.Double(defaultBounds.getX(), defaultBounds.getY())));
//  
//  
//  Rectangle2D bounds = PointUtils.getBounds(positions.values());
//  }
  
  
//  protected void resetBoundary(Dimension availableSize, Rectangle2D innerBoundary) {
//    //boundaryArea = ExplosionCanvasUtils.getAreaRect(area)
//    //Dimension size = canvas.getViewer().getSize();
//    
//    
//    double maxSize = Math.max(size.getWidth(), size.getHeight());
//    
//    
//    double innerSize = maxSize / (1 + 2 * BOUNDARY_AREA_RELATIVE_MARGIN + BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH);
//    double margin = innerSize * BOUNDARY_AREA_RELATIVE_MARGIN;
//    double w = innerSize * BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH;
//    
//    this.rect =  new Rectangle2D.Double(
//            0 + margin + w, 
//            0 + margin + w, 
//            size.getWidth() - 2 * margin - 2 * w, 
//            size.getHeight() - 2 * margin - 2 * w);
//  }
  
  
  
//  protected void resetBoundaryBasedOnTotalArea(Dimension size) {
//    //boundaryArea = ExplosionCanvasUtils.getAreaRect(area)
//    //Dimension size = canvas.getViewer().getSize();
//    double maxSize = Math.max(size.getWidth(), size.getHeight());
//    
//    
//    double innerSize = maxSize / (1 + 2 * BOUNDARY_AREA_RELATIVE_MARGIN + BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH);
//    double margin = innerSize * BOUNDARY_AREA_RELATIVE_MARGIN;
//    double w = innerSize * BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH;
//    
//    this.rect =  new Rectangle2D.Double(
//            0 + margin + w, 
//            0 + margin + w, 
//            size.getWidth() - 2 * margin - 2 * w, 
//            size.getHeight() - 2 * margin - 2 * w);
//  }
  
  
  
  
//  protected void placeNodes(Set<V> boundaryNodes, Map<String, Point2D> innerNodePositions) {
//    List<Set<V>> groupList = new ArrayList<>();
//    for(V node : boundaryNodes) groupList.add(bNodeToNonBNodeSetMap.get(node));
//    
//  }
  
//  protected  Map<String, Point2D> getNodePositions() {
//    return nodePositionMap;
//  }
//  
//  private static Point2D convertToTwoDimensionalPosition(double p, Rectangle2D rect) {
//    double d = rect.getWidth() + rect.getHeight();
//    double u = 2 * d;
//
//    while (p < 0) p+= u;
//    while (p > u) p-= u;
//
//    if (p <= d) {
//      if (p <= rect.getWidth()) {
//        return new Point2D.Double(rect.getX() + p, rect.getY());
//      } else {
//        return new Point2D.Double(rect.getX() + rect.getWidth(), rect.getY() + p - rect.getWidth());
//      }
//    } else {
//      if (p <= d + rect.getWidth()) {
//        return new Point2D.Double(rect.getX() + rect.getWidth() - (p - d), rect.getY() + rect.getHeight());
//      } else {
//        return new Point2D.Double(rect.getX(), rect.getY() + rect.getHeight() - (p - d - rect.getWidth()));
//      }
//    }
//  }
}
