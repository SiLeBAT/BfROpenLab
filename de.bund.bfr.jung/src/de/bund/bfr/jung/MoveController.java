package de.bund.bfr.jung;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;

public interface MoveController<V> {
  Point2D move(V node, Point2D fromPosition, Point2D movement);
//  Point2D move(V node, Point2D fromPosition, Point2D movement, Point2D dragPosition);
  Map<V, Point2D>  move(Set<V> nodes, Map<V, Point2D> oldPositions, Point2D movement, V dragNode);
}
