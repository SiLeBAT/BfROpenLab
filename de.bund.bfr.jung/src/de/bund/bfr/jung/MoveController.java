/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.jung;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;

public interface MoveController<V> {
  Point2D move(V node, Point2D fromPosition, Point2D movement);
//  Point2D move(V node, Point2D fromPosition, Point2D movement, Point2D dragPosition);
  Map<V, Point2D>  move(Set<V> nodes, Map<V, Point2D> oldPositions, Point2D movement, V dragNode);
}
