/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.jung.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;

import edu.uci.ics.jung.graph.Graph;

public class GridLayout<V, E> extends Layout<V, E> {

	private enum Direction {
		UP, DOWN, RIGHT, LEFT
	}

	public GridLayout(Graph<V, E> graph, Dimension size) {
		super(graph, size);
	}

	@Override
	public Map<V, Point2D> getNodePositions(Map<V, Point2D> initialPositions, DoubleConsumer progressListener) {
		Map<V, Point2D> newPositions = new LinkedHashMap<>(initialPositions);
		List<V> nodes = new ArrayList<>(graph.getVertices());

		nodes.removeAll(locked);

		List<Direction> directions = new ArrayList<>();
		int steps = 1;

		while (directions.size() < nodes.size()) {
			directions.addAll(Collections.nCopies(steps, Direction.UP));
			directions.addAll(Collections.nCopies(steps, Direction.RIGHT));
			steps++;
			directions.addAll(Collections.nCopies(steps, Direction.DOWN));
			directions.addAll(Collections.nCopies(steps, Direction.LEFT));
			steps++;
		}

		double width = getSize().getWidth();
		double height = getSize().getHeight();
		double d = Math.min(width, height) / (Math.ceil(Math.sqrt(nodes.size())) + 1);
		double x = width / 2;
		double y = height / 2;

		for (int i = 0; i < nodes.size(); i++) {
			newPositions.put(nodes.get(i), new Point2D.Double(x, y));

			switch (directions.get(i)) {
			case DOWN:
				y -= d;
				break;
			case LEFT:
				x -= d;
				break;
			case RIGHT:
				x += d;
				break;
			case UP:
				y += d;
				break;
			default:
				throw new RuntimeException("Unknown Direction");
			}
		}

		return newPositions;
	}
}
