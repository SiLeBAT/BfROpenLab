package de.bund.bfr.jung;

import java.awt.geom.Point2D;

public interface MoveController<V> {
  Point2D move(V node, Point2D fromPosition, Point2D movement);
}
