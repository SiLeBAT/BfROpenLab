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
import java.util.LinkedHashMap;
import java.util.Map;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class FRLayout<V, E> extends AbstractLayout<V, E> {

	private static final double EPSILON = 0.000001;
	private static final double ATTRACTION_MULTIPLIER = 0.75;
	private static final double REPULSION_MULTIPLIER = 0.75;
	private static final int MAX_ITERATIONS = 700;

	private double forceConstant;
	private double temperature;
	private int currentIteration;
	private double attraction_constant;
	private double repulsion_constant;
	private double max_dimension;

	private Map<V, Point2D> positionChanges;

	public FRLayout(Graph<V, E> graph) {
		super(graph);
	}

	@Override
	public void initialize() {
		Dimension d = getSize();

		setInitializer(new RandomLocationTransformer<V>(d));

		max_dimension = Math.max(d.height, d.width);
		currentIteration = 0;
		temperature = d.getWidth() / 10;
		forceConstant = Math.sqrt(d.getHeight() * d.getWidth() / getGraph().getVertexCount());
		attraction_constant = ATTRACTION_MULTIPLIER * forceConstant;
		repulsion_constant = REPULSION_MULTIPLIER * forceConstant;
		positionChanges = new LinkedHashMap<>();

		for (V v : getGraph().getVertices()) {
			positionChanges.put(v, new Point2D.Double());
		}

		while (!done()) {
			step();
		}
	}

	@Override
	public void reset() {
		initialize();
	}

	public boolean done() {
		return currentIteration > MAX_ITERATIONS || temperature < 1.0 / max_dimension;
	}

	public void step() {
		currentIteration++;

		for (V v : getGraph().getVertices()) {
			calcRepulsion(v);
		}

		for (E e : getGraph().getEdges()) {
			calcAttraction(e);
		}

		for (V v : getGraph().getVertices()) {
			if (!isLocked(v)) {
				calcPositions(v);
			}
		}

		cool();
	}

	private void calcRepulsion(V v1) {
		Point2D fvd = positionChanges.get(v1);

		fvd.setLocation(0, 0);

		for (V v2 : getGraph().getVertices()) {
			if (v1 == v2) {
				continue;
			}

			Point2D p1 = transform(v1);
			Point2D p2 = transform(v2);

			if (p1 == null || p2 == null) {
				continue;
			}

			double xDelta = p1.getX() - p2.getX();
			double yDelta = p1.getY() - p2.getY();
			double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));
			double factor = repulsion_constant * repulsion_constant / deltaLength / deltaLength;

			fvd.setLocation(fvd.getX() + xDelta * factor, fvd.getY() + yDelta * factor);
		}
	}

	private void calcAttraction(E e) {
		Pair<V> endpoints = getGraph().getEndpoints(e);
		V v1 = endpoints.getFirst();
		V v2 = endpoints.getSecond();
		boolean v1_locked = isLocked(v1);
		boolean v2_locked = isLocked(v2);

		if (v1_locked && v2_locked) {
			return;
		}

		Point2D p1 = transform(v1);
		Point2D p2 = transform(v2);

		if (p1 == null || p2 == null) {
			return;
		}

		double xDelta = p1.getX() - p2.getX();
		double yDelta = p1.getY() - p2.getY();
		double deltaLength = Math.max(EPSILON, Math.sqrt(xDelta * xDelta + yDelta * yDelta));
		double factor = deltaLength / attraction_constant;

		xDelta *= factor;
		yDelta *= factor;

		if (!v1_locked) {
			Point2D p = positionChanges.get(v1);

			p.setLocation(p.getX() - xDelta, p.getY() - yDelta);
		}

		if (!v2_locked) {
			Point2D p = positionChanges.get(v2);

			p.setLocation(p.getX() + xDelta, p.getY() + yDelta);
		}
	}

	private void calcPositions(V v) {
		Point2D delta = positionChanges.get(v);
		double xDelta = delta.getX();
		double yDelta = delta.getY();
		double deltaLength = Math.max(EPSILON, Math.sqrt(xDelta * xDelta + yDelta * yDelta));
		double factor = Math.min(deltaLength, temperature) / deltaLength;

		Point2D pos = transform(v);

		pos.setLocation(pos.getX() + xDelta * factor, pos.getY() + yDelta * factor);
	}

	private void cool() {
		temperature *= 1.0 - (double) currentIteration / (double) MAX_ITERATIONS;
	}
}
