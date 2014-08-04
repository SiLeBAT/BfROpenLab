/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.gis.views.canvas.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class FRLayout<V, E> extends AbstractLayout<V, E> implements
		IterativeContext {

	private static final double EPSILON = 0.000001;
	private static final double ATTRACTION_MULTIPLIER = 0.75;
	private static final double REPULSION_MULTIPLIER = 0.75;
	private static final int MAX_ITERATIONS = 700;

	private double forceConstant;
	private double temperature;
	private int currentIteration;
	private Map<V, FRVertexData> frVertexData;
	private double attraction_constant;
	private double repulsion_constant;
	private double max_dimension;

	public FRLayout(Graph<V, E> g) {
		super(g);

		frVertexData = LazyMap.decorate(new HashMap<V, FRVertexData>(),
				new Factory<FRVertexData>() {
					@Override
					public FRVertexData create() {
						return new FRVertexData();
					}
				});
	}

	@Override
	public void setSize(Dimension size) {
		if (initialized == false) {
			setInitializer(new RandomLocationTransformer<V>(size));
		}

		super.setSize(size);
		max_dimension = Math.max(size.height, size.width);
	}

	@Override
	public void reset() {
		initialize();
	}

	@Override
	public void initialize() {
		Graph<V, E> graph = getGraph();
		Dimension d = getSize();

		if (graph == null || d == null) {
			return;
		}

		currentIteration = 0;
		temperature = d.getWidth() / 10;

		forceConstant = Math.sqrt(d.getHeight() * d.getWidth()
				/ graph.getVertexCount());

		attraction_constant = ATTRACTION_MULTIPLIER * forceConstant;
		repulsion_constant = REPULSION_MULTIPLIER * forceConstant;
	}

	/**
	 * Returns true once the current iteration has passed the maximum count,
	 * <tt>MAX_ITERATIONS</tt>.
	 */
	@Override
	public boolean done() {
		if (currentIteration > MAX_ITERATIONS
				|| temperature < 1.0 / max_dimension) {
			return true;
		}
		return false;
	}

	/**
	 * Moves the iteration forward one notch, calculation attraction and
	 * repulsion between vertices and edges and cooling the temperature.
	 */
	@Override
	public synchronized void step() {
		currentIteration++;

		/**
		 * Calculate repulsion
		 */
		while (true) {

			try {
				for (V v1 : getGraph().getVertices()) {
					calcRepulsion(v1);
				}

				break;
			} catch (ConcurrentModificationException cme) {
			}
		}

		/**
		 * Calculate attraction
		 */
		while (true) {
			try {
				for (E e : getGraph().getEdges()) {
					calcAttraction(e);
				}

				break;
			} catch (ConcurrentModificationException cme) {
			}
		}

		while (true) {
			try {
				for (V v : getGraph().getVertices()) {
					if (!isLocked(v)) {
						calcPositions(v);
					}
				}
				break;
			} catch (ConcurrentModificationException cme) {
			}
		}

		cool();
	}

	private synchronized void calcPositions(V v) {
		FRVertexData fvd = frVertexData.get(v);

		if (fvd == null) {
			return;
		}

		Point2D xyd = transform(v);
		double deltaLength = Math.max(EPSILON, fvd.norm());
		double newXDisp = fvd.getX() / deltaLength
				* Math.min(deltaLength, temperature);

		if (Double.isNaN(newXDisp)) {
			throw new IllegalArgumentException(
					"Unexpected mathematical result in FRLayout:calcPositions [xdisp]");
		}

		double newYDisp = fvd.getY() / deltaLength
				* Math.min(deltaLength, temperature);

		xyd.setLocation(xyd.getX() + newXDisp, xyd.getY() + newYDisp);
	}

	private void calcAttraction(E e) {
		Pair<V> endpoints = getGraph().getEndpoints(e);
		V v1 = endpoints.getFirst();
		V v2 = endpoints.getSecond();
		boolean v1_locked = isLocked(v1);
		boolean v2_locked = isLocked(v2);

		if (v1_locked && v2_locked) {
			// both locked, do nothing
			return;
		}

		Point2D p1 = transform(v1);
		Point2D p2 = transform(v2);

		if (p1 == null || p2 == null) {
			return;
		}

		double xDelta = p1.getX() - p2.getX();
		double yDelta = p1.getY() - p2.getY();
		double deltaLength = Math.max(EPSILON,
				Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));
		double force = (deltaLength * deltaLength) / attraction_constant;

		if (Double.isNaN(force)) {
			throw new IllegalArgumentException(
					"Unexpected mathematical result in FRLayout:calcPositions [force]");
		}

		double dx = (xDelta / deltaLength) * force;
		double dy = (yDelta / deltaLength) * force;

		if (v1_locked == false) {
			FRVertexData fvd1 = frVertexData.get(v1);
			fvd1.offset(-dx, -dy);
		}

		if (v2_locked == false) {
			FRVertexData fvd2 = frVertexData.get(v2);
			fvd2.offset(dx, dy);
		}
	}

	private void calcRepulsion(V v1) {
		FRVertexData fvd1 = frVertexData.get(v1);

		if (fvd1 == null) {
			return;
		}

		fvd1.setLocation(0, 0);

		try {
			for (V v2 : getGraph().getVertices()) {
				if (v1 == v2) {
					continue;
				}

				Point2D p1 = transform(v1);
				Point2D p2 = transform(v2);
				if (p1 == null || p2 == null)
					continue;
				double xDelta = p1.getX() - p2.getX();
				double yDelta = p1.getY() - p2.getY();

				double deltaLength = Math.max(EPSILON,
						Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

				double force = (repulsion_constant * repulsion_constant)
						/ deltaLength;

				if (Double.isNaN(force)) {
					throw new RuntimeException(
							"Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
				}

				fvd1.offset((xDelta / deltaLength) * force,
						(yDelta / deltaLength) * force);
			}
		} catch (ConcurrentModificationException cme) {
			calcRepulsion(v1);
		}
	}

	private void cool() {
		temperature *= (1.0 - currentIteration / (double) MAX_ITERATIONS);
	}

	private static class FRVertexData extends Point2D.Double {

		private static final long serialVersionUID = 1L;

		protected void offset(double x, double y) {
			this.x += x;
			this.y += y;
		}

		protected double norm() {
			return Math.sqrt(x * x + y * y);
		}
	}
}
