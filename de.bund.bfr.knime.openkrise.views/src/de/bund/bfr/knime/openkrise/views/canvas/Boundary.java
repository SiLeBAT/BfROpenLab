package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Polygon;
import de.bund.bfr.jung.MoveController;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import edu.uci.ics.jung.graph.Graph;

public class Boundary implements MoveController<GraphNode> {
  
//  private static final double BOUNDARY_AREA_RELATIVE_MARGIN = LocationCanvasUtils.INVALID_AREA_RELATIVE_MARGIN; //    0.2;
//  private static final double BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH = LocationCanvasUtils.INVALID_AREA_RELATIVE_BORDERWIDTH; // 0.02;
//  private static final double BOUNDARY_WIDTH = 5; // 10
//  private static final double STATION_DISTANCE = 10;
//  private static class MyLogger {
//    private void finest(String msg) {
//      SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
//      String strDate = sdfDate.format(new Date());
//      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
////      System.out.println("Finest" + "\t" + strDate + "\t" + stackTrace[3].getClassName() + "." + stackTrace[3].getMethodName() + "\t" + msg);
//    }
//  }
  
//  private static MyLogger logger = new MyLogger(); // =  Logger.getLogger("de.bund.bfr");
  
//  {
//    logger = Logger.getLogger("de.bund.bfr.boundary");
//    Formatter fmt = new Formatter() {
//
//    @Override
//    public String format(LogRecord arg0) {
//      SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
//      String strDate = sdfDate.format(arg0.getMillis());
//      return arg0.getLevel() + "\t" + strDate + "\t" + arg0.getSourceClassName() + "." + arg0.getSourceMethodName() + "\t" + arg0.getMessage() + "\n";
//    }
//      
//    };
//    StreamHandler sh = new StreamHandler(System.out, fmt);
//    sh.setLevel(Level.FINEST);
//    logger.addHandler(sh);
//    logger.setLevel(Level.FINEST);
//  }
  
  private Polygon boundaryArea;
  //private Shape boundaryArea;
  private Rectangle2D boundaryRect;
  private Rectangle2D innerRect;
  private Rectangle2D outerRect;
  private Rectangle2D[] sideAreas; 
  private Double boundaryWidth;
    
//  private Map<GraphNode, Set<GraphNode>> bNodeToNonBNodeSetMap;
//  private Map<Set<GraphNode>, GraphNode> nonBNodeSetToBNodeSetMap;
//  private Map<Set<GraphNode>, List<GraphNode>> nonBNodeSetToBNodeListMap;
//  private Map<String, Point2D> nodePositionMap;
  private Set<String> boundaryNodeIds;
  
  //private Point2D lastDragPosition;
  
  
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
    //this.initMaps();
     
  }
  
  protected void paint(Graphics2D g, Canvas<GraphNode> canvas) {
    if(boundaryArea!=null) {
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
}
  
  
  protected double[] getParams() {
    if(boundaryRect==null) return null; 
    else return new double[] { boundaryRect.getX(), boundaryRect.getY(), boundaryRect.getWidth(), boundaryRect.getHeight(), boundaryWidth };
  }
  
  protected void setParams(double[] params) {
//    logger.finest("entered params: " + Arrays.toString(params) );
    if(params==null) removeBoundary();
    else setBoundary(params[0], params[1],params[2], params[3], params[4]);
//    logger.finest("leaving");
  }
  
//  private static Polygon getBorderPolygon(double x, double y, double width, double height, double borderWidth) {
//    return GisUtils.createBorderPolygon(
//          new Rectangle2D.Double(
//                  x - borderWidth/2, 
//                  y - borderWidth/2,
//                  width + borderWidth,
//                  height + borderWidth), borderWidth);
//  }
  
//  protected void resetBoundaryBasedOnInternalArea(Polygon internalArea) {
//    
//  }
//  
//  protected void resetBoundaryBasedOnTotalArea(Polygon totalArea) {
//    
//  }
  
  protected void update(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions) {
//    logger.finest("entered");
    //Rectangle rect =
    
    //Rectangle2D defaultBounds = ExplosionCanvasUtils.getInnerBoundaryRect(canvas);
    
    Rectangle2D oldBoundaryRect = this.boundaryRect;
    Double oldBoundaryWidth = this.boundaryWidth;
    
    
    this.resetBoundary(canvas, nodePositions);
    
//    logger.finest("RectUpdate: " + rectToString(oldBoundaryRect) +  " -> " + rectToString(boundaryRect));
//    logger.finest("BoundaryWithUpdate: " + oldBoundaryWidth +  " -> " + boundaryWidth);
    //boundaryArea = ExplosionCanvasUtils.createBoundaryArea(innerHull, canvas);
    //this.resetBoundary(innerHull, canvas);
    //this.setBoundaryArea(innerHull==null?null:createBoundaryArea(innerHull, canvas));
    //boundaryRect = ExplosionCanvasUtils.getAreaRect(boundaryArea);
    
    //double newBoundaryWidth = ExplosionCanvasUtils.getAreaBorderWidth(this.boundaryArea);
    
    if(oldBoundaryRect!=null && boundaryRect!=null) moveNodesToUpdatedBoundary(canvas, nodePositions, oldBoundaryRect, oldBoundaryWidth);
    else if(boundaryRect!=null) putNodesOnBoundary(canvas, nodePositions);
    
//    logger.finest("leaving");
  }
  
  private void resetBoundary(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions) {
    if(nodePositions.isEmpty()) {
      
      removeBoundary();
      
    } else {
      
      Rectangle2D innerBounds = getBounds(canvas, nodePositions);


      double size = Math.max(innerBounds.getWidth(), innerBounds.getHeight());
      //if(size == 0) size = Math.max(canvas.getCanvasSize().getWidth(), canvas.getCanvasSize().getHeight())/2;
      //if(size == 0) size = 1;
      double scale = canvas.getTransform().getScaleX();
      double nodeSize  = canvas.getOptionsPanel().getNodeSize()/scale;
      double fontSize = canvas.getOptionsPanel().getFontSize()/scale;
      
      double margin = Math.max(size/4,3*nodeSize+4*fontSize);
      double w = nodeSize + fontSize; //size * BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH;

      setBoundary(innerBounds.getX()-margin-w/2,innerBounds.getY()-margin-w/2,innerBounds.getWidth()+2*margin+w,innerBounds.getHeight()+2*margin+w,w);
//      logger.finest("nodeSize: " + nodeSize + ", fontSize: " + fontSize);
    }
  }
//  /*
//   * returns a rectangular border frame based on the bounds which are supposed to contain the inner nodes
//   * ToDO:
//   * 
//   * @param innerBounds - rectangular border area
//   */
//  public static Polygon createBoundaryArea(Rectangle2D innerBounds, Canvas<GraphNode> canvas) {
//      double size = Math.max(innerBounds.getWidth(), innerBounds.getHeight());
//      //if(size == 0) size = Math.max(canvas.getCanvasSize().getWidth(), canvas.getCanvasSize().getHeight())/2;
//      //if(size == 0) size = 1;
//      double nodeSize  = canvas.getTransform().applyInverse(canvas.getOptionsPanel().getNodeSize(),0).getX();
//      double fontSize = canvas.getTransform().applyInverse(canvas.getOptionsPanel().getFontSize(),0).getX();
//     
//      double margin = Math.max(size/4,3*nodeSize+4*fontSize);
//      double w = nodeSize + fontSize; //size * BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH;
//      
//      return GisUtils.createBorderPolygon(
//              new Rectangle2D.Double(
//                      innerBounds.getX() - margin - w/2, 
//                      innerBounds.getY() - margin - w/2,
//                      innerBounds.getWidth() + 2 * margin + w,
//                      innerBounds.getHeight() + 2 * margin + w), w);
//  }
  
  
  private void setBoundary(double x, double y, double width, double height, double boundaryWidth) {
    boundaryRect = new Rectangle2D.Double(x,y,width,height);
    this.boundaryWidth = boundaryWidth;
    boundaryArea = GisUtils.createBorderPolygon(
        new Rectangle2D.Double(
            x+boundaryWidth/2, //
            y+boundaryWidth/2,
            width-boundaryWidth,
            height-boundaryWidth), boundaryWidth);
    //boundaryWidth = ExplosionCanvasUtils.getAreaBorderWidth(boundaryArea);
    innerRect = new Rectangle2D.Double(boundaryRect.getX()+boundaryWidth/2, boundaryRect.getY()+boundaryWidth/2,boundaryRect.getWidth()-boundaryWidth,boundaryRect.getHeight()-boundaryWidth);
    outerRect = new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2, boundaryRect.getY()-boundaryWidth/2,boundaryRect.getWidth()+boundaryWidth,boundaryRect.getHeight()+boundaryWidth);
    this.sideAreas = new Rectangle2D[] {
        new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryRect.getWidth() + boundaryWidth, boundaryWidth), //top
        new Rectangle2D.Double(boundaryRect.getMaxX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryWidth, boundaryRect.getHeight() + boundaryWidth), //right
        new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getMaxY()-boundaryWidth/2, boundaryRect.getWidth() + boundaryWidth, boundaryWidth), //bottom
        new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryWidth, boundaryRect.getHeight() + boundaryWidth) //left
    };
  }
  
  private void removeBoundary() {
    boundaryArea = null;
    boundaryRect = null;
    boundaryWidth = null; 
    sideAreas = null;
    innerRect = null;
    outerRect = null;
  }
  
  
//  private void setBoundaryArea(Polygon newBoundaryArea) {
//    if(newBoundaryArea==null) {
//      boundaryArea = null;
//      boundaryRect = null;
//      boundaryWidth = null; 
//      sideAreas = null;
//      innerRect = null;
//      outerRect = null;
//    } else {
//      boundaryArea = newBoundaryArea;
//      boundaryRect = ExplosionCanvasUtils.getAreaRect(boundaryArea);
//      boundaryWidth = ExplosionCanvasUtils.getAreaBorderWidth(boundaryArea);
//      innerRect = new Rectangle2D.Double(boundaryRect.getX()+boundaryWidth/2, boundaryRect.getY()+boundaryWidth/2,boundaryRect.getWidth()-boundaryWidth,boundaryRect.getHeight()-boundaryWidth);
//      outerRect = new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2, boundaryRect.getY()-boundaryWidth/2,boundaryRect.getWidth()+boundaryWidth,boundaryRect.getHeight()+boundaryWidth);
//      this.sideAreas = new Rectangle2D[] {
//          new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryRect.getWidth() + boundaryWidth, boundaryWidth), //top
//          new Rectangle2D.Double(boundaryRect.getMaxX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryWidth, boundaryRect.getHeight() + boundaryWidth), //right
//          new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getMaxY()-boundaryWidth/2, boundaryRect.getWidth() + boundaryWidth, boundaryWidth), //bottom
//          new Rectangle2D.Double(boundaryRect.getX()-boundaryWidth/2,boundaryRect.getY()-boundaryWidth/2, boundaryWidth, boundaryRect.getHeight() + boundaryWidth) //left
//      };
//    }
//  }
  
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
  
  protected void moveNodesToUpdatedBoundary(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions, Rectangle2D oldBoundaryRect, Double oldBoundaryWidth) {
//    logger.finest("entered");
//    AffineTransform at = new AffineTransform();
//    Point2D fromCenter = new Point2D.Double(oldBoundaryRect.getCenterX(), oldBoundaryRect.getCenterY());
//    Point2D toCenter = new Point2D.Double(boundaryRect.getCenterX(), boundaryRect.getCenterY());
//    Point2D trans = PointUtils.substractPoints(toCenter, fromCenter);
    
    //Point2D trans = new Point2D.Double(oldBoundaryRect.getCenterX() - oldBoundaryRect.getCenterY(), oldBoundaryRect.getCenterY() - oldBoundaryRect.getCenterY())
    //Point2D scale = new Point2D.Double(boundaryRect.getWidth()/oldBoundaryRect.getWidth(), boundaryRect.getHeight()/oldBoundaryRect.getHeight());
//    Point2D trans = new Point2D.Double(boundaryRect.getCenterX()-oldBoundaryRect.getCenterX(), boundaryRect.getCenterY()-oldBoundaryRect.getCenterY());
//    Point2D scale = 
    
//    at.translate(boundaryRect.getCenterX()-oldBoundaryRect.getCenterX(), boundaryRect.getCenterY()-oldBoundaryRect.getCenterY());
//    at.scale(boundaryRect.getWidth()/oldBoundaryRect.getWidth(), boundaryRect.getHeight()/oldBoundaryRect.getHeight() );
    
    Rectangle2D[] oldProjectableRects = getProjectableRects(oldBoundaryRect, oldBoundaryWidth);
    Rectangle2D[] newProjectableRects = getProjectableRects(boundaryRect, boundaryWidth);
    
    Map<Integer, AffineTransform> rectIndexToTransformMap = new HashMap<>();
    
    for(GraphNode node : canvas.getBoundaryNodes()) {
      Point2D oldPosition = nodePositions.get(node.getId());
      
      if(oldPosition!=null) {
        // for numeric reasons the a point might not be contained in any rectangle
        // move the point back to the best position
        int rectIndex = getContainerRect(oldPosition, oldProjectableRects);
        if(rectIndex<0) {
          Point2D newPoint = ExplosionCanvasUtils.getClosestPointOnRect(oldPosition, oldProjectableRects[0]);
          double minDistance = distanceFromZero(PointUtils.substractPoints(newPoint, oldPosition));
          rectIndex = 0;
          
          for(int i=1; i<8; ++i) {
            Point2D testPoint = ExplosionCanvasUtils.getClosestPointOnRect(oldPosition, oldProjectableRects[i]);
            double testDistance = distanceFromZero(PointUtils.substractPoints(testPoint, oldPosition));
                        
            if(testDistance<minDistance) {
              newPoint = testPoint;
              minDistance = testDistance;
              rectIndex = i;
            }
          }
//          logger.finest("correct " + oldPosition.toString() + " -> " + newPoint.toString());
          oldPosition = newPoint;
        }
        
//        logger.finest("rectIndex: " + rectIndex);
        
        AffineTransform at = rectIndexToTransformMap.get(rectIndex);
        if(at==null) {
          
         at = new AffineTransform(); 
        
         Rectangle2D fromRect = oldProjectableRects[rectIndex];
         Rectangle2D toRect = newProjectableRects[rectIndex];
         //at.setToIdentity();
         at.translate(toRect.getX(), toRect.getY());
         at.scale(toRect.getWidth()/fromRect.getWidth(), toRect.getHeight()/fromRect.getHeight());
         at.translate(-fromRect.getX(), -fromRect.getY());
         
         rectIndexToTransformMap.put(rectIndex, at);
        
        }
//        logger.finest("transform " + oldPosition.toString() + " -> " + at.transform(oldPosition, null).toString());
        nodePositions.put(node.getId(), at.transform(oldPosition, null));
      }
    }
//    logger.finest("leaving");
  }
  
  private double distanceFromZero(Point2D point) {
    return Math.max(Math.abs(point.getX()), Math.abs(point.getY()));
  }
  private int getContainerRect(Point2D point, Rectangle2D[] rects) {
    for(int i=0; i<rects.length; ++i) if(rects[i].contains(point)) return i;
    return -1;
  }
  
  private Rectangle2D[] getProjectableRects(Rectangle2D rect, Double boundaryWidth) {
    return new Rectangle2D[] {
        new Rectangle2D.Double(rect.getX()-boundaryWidth/2, rect.getY()-boundaryWidth/2, boundaryWidth, boundaryWidth),
        new Rectangle2D.Double(rect.getX()+boundaryWidth/2, rect.getY()-boundaryWidth/2, rect.getWidth() - boundaryWidth, boundaryWidth),
        new Rectangle2D.Double(rect.getMaxX()-boundaryWidth/2, rect.getY()-boundaryWidth/2, boundaryWidth, boundaryWidth),
        new Rectangle2D.Double(rect.getMaxX()-boundaryWidth/2, rect.getY()+boundaryWidth/2, boundaryWidth, rect.getHeight() - boundaryWidth),
        new Rectangle2D.Double(rect.getMaxX()-boundaryWidth/2, rect.getMaxY()-boundaryWidth/2, boundaryWidth, boundaryWidth),
        new Rectangle2D.Double(rect.getX()+boundaryWidth/2, rect.getMaxY()-boundaryWidth/2, rect.getWidth() - boundaryWidth, boundaryWidth),
        new Rectangle2D.Double(rect.getX()-boundaryWidth/2, rect.getMaxY()-boundaryWidth/2, boundaryWidth, boundaryWidth),
        new Rectangle2D.Double(rect.getX()-boundaryWidth/2, rect.getY()+boundaryWidth/2, boundaryWidth, rect.getHeight() - boundaryWidth)
    };
  }
  
  protected void layout(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions, Set<GraphNode> nodesToLayout) {
//    logger.finest("entered");
//    for(GraphNode node: nodesToLayout) {
//      nodePositions.put(node.getId(), getRandomPointOnBoundary());
//    }
    Map<GraphNode, Set<GraphNode>> boundaryNodeToInnerNodesMap = getBoundaryToInnerNodesMap(canvas, nodesToLayout);
    Map<Set<GraphNode>, List<GraphNode>> innerNodesToBoundaryNodesMap = getInnerNodesToBoundaryNodesMap(boundaryNodeToInnerNodesMap);
    Map<Set<GraphNode>, Point2D> innerNodesToPointMap = getInnerNodesToPointMap(canvas, innerNodesToBoundaryNodesMap, nodePositions);
    
    Map<GraphNode, Point2D> newPositions = new HashMap<>();
    for(Map.Entry<Set<GraphNode>, Point2D> entry : innerNodesToPointMap.entrySet()) {
      Point2D point = ExplosionCanvasUtils.getClosestPointOnRect(entry.getValue(), boundaryRect);
      for(GraphNode node : innerNodesToBoundaryNodesMap.get(entry.getKey())) {
        newPositions.put(node, point);
      }
    }
    
//    Map<GraphNode, Point2D> newPositionsBackup = new HashMap<>(newPositions);
    
    //for(Point2D p : newPositions.values()) System.out.println(p.toString());
 // if an inner node has connections to several boundary nodes
    // then their might be positioned on the same spot (depending on their other connections)
    // this method distributes the boundary nodes with identical positions
    updateBoundaryNodePositionsByRemovingVisualConflicts(
                newPositions, boundaryRect, 
                boundaryNodeToInnerNodesMap, boundaryWidth);
   
    for(GraphNode node : nodesToLayout) nodePositions.put(node.getId(), newPositions.get(node));

//    for(GraphNode node : newPositions.keySet()) logger.finest(newPositionsBackup.get(node).toString() + " -> " + newPositions.get(node).toString());
    
    // the boundary positions were only set for the visual nodes so far
    // but the position of the boundary meta nodes will be overwritten by 
    // the position of the center of their contained nodes ->
    // the position of the contained nodes is set to the position of their meta node
    Map<String, Set<String>> collapsedNodes = canvas.getCollapsedNodes();
    Sets.intersection(collapsedNodes.keySet(), nodesToLayout.stream().map(Node::getId).collect(Collectors.toSet())).forEach(metaKey -> {
                  Point2D p = nodePositions.get(metaKey);
                  collapsedNodes.get(metaKey).forEach(k -> nodePositions.put(k, p));
              });
//    logger.finest("leaving");
  }
  
/*
* updates the positions such that all boundary nodes have a minimum distance from each other (if possible)
*/
private <N extends Node> void updateBoundaryNodePositionsByRemovingVisualConflicts(Map<N, Point2D> positions, Rectangle2D rect, Map<N, Set<N>> boundaryNodeToInnerNodesMap, double distance) { 
       
   if(!boundaryNodeToInnerNodesMap.isEmpty()) {
       // idea: map the nodes on the rectangle to a line, sort them on the line, move them on the 
       // line to satisfy the minimum distance
       
       List<BoundaryNode<N>> sortableBoundaryNodeList = new ArrayList<>();
       
       // map them to the line
       boundaryNodeToInnerNodesMap.entrySet().forEach(e -> {
           sortableBoundaryNodeList.add(new BoundaryNode<>(convertToOneDimensionalPosition(positions.get(e.getKey()), rect),e.getKey(), e.getValue()));
       });
       
       // sort them on the line
       Collections.sort(sortableBoundaryNodeList);
       
       //List<Double> test = sortableBoundaryNodeList.stream().mapToDouble(e -> e.getPoint()).boxed().collect(Collectors.toList());
       
       // move them on the line
       List<Double> res = ExplosionCanvasUtils.getPositionsWithoutConflict(
           sortableBoundaryNodeList.stream().mapToDouble(e -> e.getPoint()).boxed().collect(Collectors.toList()),
           distance, rect);
//               test, 
//               distance, rect).stream()
//               .map(e -> convertToTwoDimensionalPosition(e, rect)).collect(Collectors.toList());
       
       //System.out.println("outerRect:" + rectToString(outerRect));
       //System.out.println("innerRect:" + rectToString(innerRect));
       for(int i = 0; i < sortableBoundaryNodeList.size(); i++) {
         positions.put(sortableBoundaryNodeList.get(i).getNode(), convertToTwoDimensionalPosition(res.get(i),((i&1)!=0 ? -distance/2 : distance/2)*(1-distance/1000), rect));
         //logger.finest("New position of " + sortableBoundaryNodeList.get(i).getNode().getId() + ": " + positions.get(sortableBoundaryNodeList.get(i).getNode()).toString() + " (On B: " + isPointOnBoundary(positions.get(sortableBoundaryNodeList.get(i).getNode())) + ")");
       }
   }
}

/*
 * The class is needed to sort positions of boundary nodes attached with additional information
 */
private static class BoundaryNode<N extends Node> implements Comparable<BoundaryNode<N>> {
    private double point;
    private N node;
    private Set<N> innerNeighbourNodes;
    
    BoundaryNode(double point, N node, Set<N> innerNeighbourNodes) {
        this.point = point;
        this.node = node;
        this.innerNeighbourNodes = innerNeighbourNodes;
    }
    
    @Override
    public int compareTo(BoundaryNode<N> arg0) {
        int res = Double.compare(this.point,arg0.getPoint());
        if(res!=0) return res;
        
        res = Integer.compare(this.innerNeighbourNodes.hashCode(),arg0.getInnerNeighbourNodes().hashCode());
        if(res != 0) return res;
        
        return this.node.getId().compareTo(arg0.getNode().getId());
    }
    
    public double getPoint() { return this.point; }
    public N getNode() { return this.node; }
    public Set<N> getInnerNeighbourNodes() { return this.innerNeighbourNodes; }
}
  
  private Map<Set<GraphNode>,Point2D> getInnerNodesToPointMap(ExplosionTracingGraphCanvas canvas, Map<Set<GraphNode>, List<GraphNode>> innerNodesToBoundaryNodesMap,  Map<String, Point2D> nodePositions) {
    Map<Set<GraphNode>, Point2D> innerNodesToPointMap = new HashMap<>();
//    Layout<GraphNode, Edge<GraphNode>> layout = canvas.getViewer().getGraphLayout();
    for(Set<GraphNode> nodes : innerNodesToBoundaryNodesMap.keySet()) {
      if(nodes.isEmpty()) innerNodesToPointMap.put(nodes, getRandomPointOnBoundary());
      else {
//        Set<GraphNode> nodesForCenter = new HashSet<>(entry.getValue());
//        
//        List<Point2D> pointsForCenter = new ArrayList<>();
//        for(GraphNode node : nodesForCenter) pointsForCenter.add(layout.transform(node));
//        
//        pointsForCenter = new ArrayList<>();
//        for(GraphNode node : nodesForCenter) pointsForCenter.add(nodePositions.get(node.getId()));
//        
//            //nodesForCenter.stream().map(n -> layout.transform(n)).collect(Collectors.toList());
//        Point2D center = PointUtils.getCenter(pointsForCenter);
        //innerNodesToPointMap.put(nodes, PointUtils.getCenter(nodes.stream().map(n -> layout.transform(n)).collect(Collectors.toList())));
        innerNodesToPointMap.put(nodes, PointUtils.getCenter(nodes.stream().map(n -> nodePositions.get(n.getId())).collect(Collectors.toList())));
      }
    }
    return innerNodesToPointMap;
  }
  
  private Map<Set<GraphNode>, List<GraphNode>> getInnerNodesToBoundaryNodesMap(Map<GraphNode, Set<GraphNode>> boundaryNodesToInnerNodesMap) {
    Map<Set<GraphNode>, List<GraphNode>> innerNodesToBoundaryNodesMap= new HashMap<>();
    for(Map.Entry<GraphNode, Set<GraphNode>> entry : boundaryNodesToInnerNodesMap.entrySet()) {
      List<GraphNode> list = innerNodesToBoundaryNodesMap.get(entry.getValue());
      if(list==null) innerNodesToBoundaryNodesMap.put(entry.getValue(), new ArrayList<>(Arrays.asList(entry.getKey())));
      else list.add(entry.getKey());
    }
    return innerNodesToBoundaryNodesMap;
  }
  
  private Map<GraphNode, Set<GraphNode>> getBoundaryToInnerNodesMap(ExplosionTracingGraphCanvas canvas, Set<GraphNode> nodesToLayout) {
    Map<GraphNode, Set<GraphNode>> connectionMap = new HashMap<>();
    //VisualizationViewer<GraphNode, Edge<GraphNode>> viewer = null;
    Graph<GraphNode,Edge<GraphNode>> graph = canvas.getViewer().getGraphLayout().getGraph(); //    viewer.getModel().getGraphLayout().getGraph();
    for(GraphNode boundaryNode: nodesToLayout) connectionMap.put(boundaryNode, new HashSet<>(graph.getNeighbors(boundaryNode)));
    return connectionMap;
  }
  
  protected void putNodesOnBoundary(ExplosionTracingGraphCanvas canvas, Map<String, Point2D> nodePositions) {
//    logger.finest("entered");
    //ArrayList<GraphNode> nodesWithoutPosition = new ArrayList<>();
    if(boundaryNodeIds!=null && boundaryRect!=null) {
    for(GraphNode node : canvas.getNodes()) {
      // this node should be visible 
      if(boundaryNodeIds.contains(node.getId())) {
        // this is a boundary node
        Point2D oldPosition = nodePositions.get(node.getId());
        
        if(!isPointOnBoundary(oldPosition)) {
//          logger.finest("P(" + node.getId() + ") not on B, innerRect.contains(" + oldPosition.toString() + "):" + innerRect.contains(oldPosition));
          Point2D pointOnRect = ExplosionCanvasUtils.getClosestPointOnRect(oldPosition, innerRect.contains(oldPosition)?innerRect:outerRect);
          nodePositions.put(node.getId(), pointOnRect);
        } else {
//          logger.finest("P(" + node.getId() + ") on B");
        }
      }
    }
    }
//    logger.finest("leaving");
    //if(!nodesWithoutPosition.isEmpty()) this.layout(canvas,  nodePositions, nodesWithoutPosition);
  }
  
  protected Point2D getRandomPointOnBoundary() {
//    logger.finest("entered");
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
//    System.out.println("move entered");
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
//      System.out.println("move CP1: all points on boundary");
      newPositions.putAll(newBoundaryPositions);
      
    } else {
      
      if(newBoundaryPositions.size()!=nodes.size()) {
        // not only Boundary nodes
        // use Projection
//        System.out.println("move CP2: not only boundary nodes are moving");
        for(GraphNode node : newBoundaryPositions.keySet()) newPositions.put(node, move(node.getId(), oldPositions.get(node), movement));
      } else {
        // only boundary Nodes
//        System.out.println("move CP3: only Boundary nodes are moving");
        Point2D dragPoint = PointUtils.addPoints(oldPositions.get(dragNode), movement);
        if(isPointOnBoundary(dragPoint)) {
          // dragPoint is on boundary
          // perform compact move
//          System.out.println("move CP4: drag Point is on boundary use compact move");
          newPositions.putAll(compactMove(oldPositions, dragNode, dragPoint));
        } else {
          // dragPoint is not on Boundary
          // use Projection
//          System.out.println("move CP4: drag Point is not on boundary use projection");
          for(GraphNode node : newBoundaryPositions.keySet()) newPositions.put(node, move(node.getId(), oldPositions.get(node), movement));
        }
      }
    }
//    System.out.println("move leaving");
    return newPositions;
  }
  
  private boolean isPointOnBoundary(Point2D p) {
    for(Rectangle2D rect : sideAreas) if(rect.contains(p)) {
      //i++;
      //System.out.println("Point " + p.toString() + " is on Boundary(" + i + ") " + rectToString(boundaryRect) + ".");
      return true;
    }
    //System.out.println("Point " + p.toString() + " is not on Boundary " + rectToString(boundaryRect) + ".");
    return false;
  }
  
//  private static String rectToString(Rectangle2D rect) {
//    if(rect==null) return "null";
//    else return "[" + rect.getX() + "," + rect.getY() + "," + rect.getMaxX() + "," + rect.getMaxY() + "]";
//  }
  
  private HashMap<GraphNode, Point2D> compactMove(Map<GraphNode, Point2D> oldBoundaryPositions, GraphNode dragNode, Point2D dragPoint) {
//    System.out.println("compactMove entered");
    //HashMap<GraphNode, Point2D> oldBoundaryPositions = new HashMap<>(oldPositions.entrySet().stream().filter(e -> boundaryNodeIds.contains(e.getKey().getId())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    HashMap<GraphNode, Point2D> newPositions = new HashMap<>();
    
    double dragPoint1D = convertToOneDimensionalPosition(ExplosionCanvasUtils.getClosestPointOnRect(dragPoint, boundaryRect), boundaryRect);
    double movement1D = dragPoint1D - convertToOneDimensionalPosition(ExplosionCanvasUtils.getClosestPointOnRect(oldBoundaryPositions.get(dragNode), boundaryRect), boundaryRect);
    System.out.println("dragPoint1D: " + dragPoint1D);
    //Set<String> nodeIds = Sets.intersection(boundaryNodeIds, oldPositions.keySet());
    for(GraphNode node: oldBoundaryPositions.keySet()) {
      Point2D oldPointOnRect2D = ExplosionCanvasUtils.getClosestPointOnRect(oldBoundaryPositions.get(node), boundaryRect);
      Double oldPointOnRect1D = convertToOneDimensionalPosition(oldPointOnRect2D,  boundaryRect );
      Double newPointOnRect1D = oldPointOnRect1D + movement1D;
//      System.out.println(oldBoundaryPositions.get(node).toString() + " -> " + oldPointOnRect2D.toString() + " -> " + oldPointOnRect1D);
      Point2D oldPointDistanceToRect2D = PointUtils.substractPoints(oldBoundaryPositions.get(node), oldPointOnRect2D);
      double oldPointDistanceToRect1D = (boundaryRect.contains(oldBoundaryPositions.get(node))?-1:1) * Math.max(Math.abs(oldPointDistanceToRect2D.getX()), Math.abs(oldPointDistanceToRect2D.getY()));
      
      Point2D newPoint2D = convertToTwoDimensionalPosition(newPointOnRect1D, oldPointDistanceToRect1D, boundaryRect);
      newPositions.put(node, newPoint2D);
    }
//    System.out.println("compactMove leaving");
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
      return p.getY()-rect.getMinY()+rect.getWidth();
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
