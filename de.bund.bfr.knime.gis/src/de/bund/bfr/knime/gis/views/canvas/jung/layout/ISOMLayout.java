/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.canvas.jung.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import edu.uci.ics.jung.graph.Graph;

public class ISOMLayout<V, E> extends AbstractLayout<V, E> {

	private static final int MAX_EPOCH = 2000;
	private static final int RADIUS_CONSTANT_TIME = 100;
	private static final int MIN_RADIUS = 1;
	private static final double INITIAL_ADAPTION = 0.9;
	private static final double COOLING_FACTOR = 2.0;
	private static final double MIN_ADAPTION = 0.0;

	private int epoch;
	private int radius;
	private double adaption;

	private Map<V, Point2D> newPositions;
	private Map<V, VertexState> vertexStates;

	private Random random;

	public ISOMLayout(Graph<V, E> graph, Dimension size) {
		super(graph, size);
	}

	@Override
	public Map<V, Point2D> getNodePositions(Map<V, Point2D> initialPositions) {
		random = new Random();
		newPositions = new LinkedHashMap<>();

		for (V v : getGraph().getVertices()) {
			newPositions.put(v,
					new Point2D.Double(random.nextDouble() * size.width, random.nextDouble() * size.height));
		}

		epoch = 1;
		radius = 5;
		adaption = INITIAL_ADAPTION;
		vertexStates = new LinkedHashMap<>();

		for (V v : getGraph().getVertices()) {
			vertexStates.put(v, new VertexState());
		}

		while (!done()) {
			step();
		}

		return newPositions;
	}

	public boolean done() {
		return epoch >= MAX_EPOCH;
	}

	public void step() {
		adjust();
		updateParameters();
	}

	private void adjust() {
		for (V v : getGraph().getVertices()) {
			VertexState state = vertexStates.get(v);

			state.distance = 0;
			state.visited = false;
		}

		Point2D tempPos = new Point2D.Double(10.0 + random.nextDouble() * getSize().getWidth(),
				10.0 + random.nextDouble() * getSize().getHeight());
		V winner = getClosest(tempPos.getX(), tempPos.getY());

		adjustVertex(winner, tempPos);
	}

	private void updateParameters() {
		double factor = Math.exp(-COOLING_FACTOR * ++epoch / MAX_EPOCH);

		adaption = Math.max(MIN_ADAPTION, factor * INITIAL_ADAPTION);

		if (radius > MIN_RADIUS && epoch % RADIUS_CONSTANT_TIME == 0) {
			radius--;
		}
	}

	private void adjustVertex(V v, Point2D tempPos) {
		VertexState state = vertexStates.get(v);
		Deque<V> queue = new LinkedList<>();

		state.distance = 0;
		state.visited = true;
		queue.addLast(v);

		while (!queue.isEmpty()) {
			V current = queue.removeFirst();
			VertexState currState = vertexStates.get(current);
			Point2D currPos = newPositions.get(current);

			double dx = tempPos.getX() - currPos.getX();
			double dy = tempPos.getY() - currPos.getY();
			double factor = adaption / Math.pow(2, currState.distance);

			currPos.setLocation(currPos.getX() + factor * dx, currPos.getY() + factor * dy);

			if (currState.distance < radius) {
				for (V child : getGraph().getNeighbors(current)) {
					VertexState childState = vertexStates.get(child);

					if (!childState.visited) {
						childState.visited = true;
						childState.distance = currState.distance + 1;
						queue.addLast(child);
					}
				}
			}
		}
	}

	private V getClosest(double x, double y) {
		double minDistance = Double.POSITIVE_INFINITY;
		V closest = null;

		for (Map.Entry<V, Point2D> pos : newPositions.entrySet()) {
			double dx = pos.getValue().getX() - x;
			double dy = pos.getValue().getY() - y;
			double dist = dx * dx + dy * dy;

			if (dist < minDistance) {
				minDistance = dist;
				closest = pos.getKey();
			}
		}

		return closest;
	}

	private static class VertexState {

		private int distance;
		private boolean visited;

		public VertexState() {
			distance = 0;
			visited = false;
		}
	}
}