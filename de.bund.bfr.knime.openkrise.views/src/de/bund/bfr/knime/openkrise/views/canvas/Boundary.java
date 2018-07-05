package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.vividsolutions.jts.geom.Polygon;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Node;

public class Boundary<V extends Node,E> {
  
  private static final double BOUNDARY_AREA_RELATIVE_MARGIN = LocationCanvasUtils.INVALID_AREA_RELATIVE_MARGIN; //    0.2;
  private static final double BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH = LocationCanvasUtils.INVALID_AREA_RELATIVE_BORDERWIDTH; // 0.02;
  private static final double BOUNDARY_WIDTH = 5; // 10
  private static final double STATION_DISTANCE = 10;
  
  private Polygon boundaryArea;
  private Rectangle2D rect;
    
  private Map<V, Set<V>> bNodeToNonBNodeSetMap;
  private Map<Set<V>, V> nonBNodeSetToBNodeSetMap;
  private Map<Set<V>, List<V>> nonBNodeSetToBNodeListMap;
  private Map<String, Point2D> nodePositionMap;
  private Set<String> boundaryNodeIds;
  
  
  Boundary(Set<V> nonBoundaryNodes, Set<V> boundaryNodes, Set<E> edges, Dimension availableAreaSize) {
    resetBoundaryBasedOnTotalArea(availableAreaSize);
    //this.initMaps();
  }
  
//  Boundary(Set<V> nonBoundaryNodes, Set<V> boundaryNodes, Set<E> edges, Rectangle2D layoutArea, Map<String, Point2D> innerNodePositions) {
//      this.initMaps();
//      this.initNodePositions(boundaryNodes);;
//  }
  
  private void initNodePositions(Map<String, Point2D> nodePositions) {
    Random random = new Random();
    double u = 2*(rect.getHeight() + rect.getWidth());
    this.nodePositionMap = new HashMap<>();
    for(String nodeId: boundaryNodeIds) this.nodePositionMap.put(nodeId, convertToTwoDimensionalPosition(random.nextDouble()*u, rect));
  }
  
  Boundary(Set<V> nonBoundaryNodes, Set<V> boundaryNodes, Set<E> edges, Map<V, Point2D> boundaryNodePositions, Rectangle2D boundaryPosition) {
    this.initMaps();
     
  }
  
  private void initMaps() {
    
  }
  
  protected void resetBoundaryBasedOnInternalArea(Polygon internalArea) {
    
  }
  
  protected void resetBoundaryBasedOnTotalArea(Polygon totalArea) {
    
  }
  
  protected void resetBoundary(Dimension availableSize, Rectangle2D innerBoundary) {
    //boundaryArea = ExplosionCanvasUtils.getAreaRect(area)
    //Dimension size = canvas.getViewer().getSize();
    
    
    double maxSize = Math.max(size.getWidth(), size.getHeight());
    
    
    double innerSize = maxSize / (1 + 2 * BOUNDARY_AREA_RELATIVE_MARGIN + BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH);
    double margin = innerSize * BOUNDARY_AREA_RELATIVE_MARGIN;
    double w = innerSize * BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH;
    
    this.rect =  new Rectangle2D.Double(
            0 + margin + w, 
            0 + margin + w, 
            size.getWidth() - 2 * margin - 2 * w, 
            size.getHeight() - 2 * margin - 2 * w);
  }
  
  
  
  protected void resetBoundaryBasedOnTotalArea(Dimension size) {
    //boundaryArea = ExplosionCanvasUtils.getAreaRect(area)
    //Dimension size = canvas.getViewer().getSize();
    double maxSize = Math.max(size.getWidth(), size.getHeight());
    
    
    double innerSize = maxSize / (1 + 2 * BOUNDARY_AREA_RELATIVE_MARGIN + BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH);
    double margin = innerSize * BOUNDARY_AREA_RELATIVE_MARGIN;
    double w = innerSize * BOUNDARY_AREA_RELATIVE_BOUNDARYWIDTH;
    
    this.rect =  new Rectangle2D.Double(
            0 + margin + w, 
            0 + margin + w, 
            size.getWidth() - 2 * margin - 2 * w, 
            size.getHeight() - 2 * margin - 2 * w);
  }
  
  
  
  
  protected void placeNodes(Set<V> boundaryNodes, Map<String, Point2D> innerNodePositions) {
    List<Set<V>> groupList = new ArrayList<>();
    for(V node : boundaryNodes) groupList.add(bNodeToNonBNodeSetMap.get(node));
    
  }
  
  protected  Map<String, Point2D> getNodePositions() {
    return nodePositionMap;
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
