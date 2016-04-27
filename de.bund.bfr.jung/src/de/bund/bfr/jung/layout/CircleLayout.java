/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleConsumer;

import edu.uci.ics.jung.graph.Graph;

public class CircleLayout<V, E> extends Layout<V, E> {

	public CircleLayout(Graph<V, E> graph, Dimension size) {
		super(graph, size);
	}

	@Override
	public Map<V, Point2D> getNodePositions(Map<V, Point2D> initialPositions, DoubleConsumer progressListener) {
		Map<V, Point2D> newPositions = new LinkedHashMap<>(initialPositions);
		Set<V> nodes = new LinkedHashSet<>(graph.getVertices());

		nodes.removeAll(locked);

		double width = getSize().getWidth();
		double height = getSize().getHeight();
		double radius = 0.45 * Math.min(width, height);
		int index = 0;

		for (V node : nodes) {
			double angle = 2 * Math.PI * index++ / nodes.size();

			newPositions.put(node,
					new Point2D.Double(Math.cos(angle) * radius + width / 2, Math.sin(angle) * radius + height / 2));
		}

		return newPositions;
	}
}
