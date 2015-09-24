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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.RadiusGraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.Graph;

/**
 * Implements a self-organizing map layout algorithm, based on Meyer's
 * self-organizing graph methods.
 *
 * @author Yan Biao Boey
 */
public class ISOMLayout<V, E> extends AbstractLayout<V, E> {

	private Map<V, ISOMVertexData> isomVertexData;

	private int maxEpoch;
	private int epoch;

	private int radiusConstantTime;
	private int radius;
	private int minRadius;

	private double adaption;
	private double initialAdaption;
	private double minAdaption;

	private GraphElementAccessor<V, E> elementAccessor;

	private double coolingFactor;

	private List<V> queue;

	public ISOMLayout(Graph<V, E> g) {
		super(g);

		elementAccessor = new RadiusGraphElementAccessor<V, E>();
		queue = new ArrayList<V>();
		isomVertexData = LazyMap.decorate(new HashMap<V, ISOMVertexData>(), new Factory<ISOMVertexData>() {

			@Override
			public ISOMVertexData create() {
				return new ISOMVertexData();
			}
		});
	}

	@Override
	public void initialize() {
		setInitializer(new RandomLocationTransformer<V>(getSize()));

		maxEpoch = 2000;
		epoch = 1;
		radiusConstantTime = 100;
		radius = 5;
		minRadius = 1;
		initialAdaption = 90.0D / 100.0D;
		adaption = initialAdaption;
		minAdaption = 0;
		coolingFactor = 2;

		while (!done()) {
			step();
		}
	}

	@Override
	public void reset() {
		initialize();
	}

	public boolean done() {
		return epoch >= maxEpoch;
	}

	public void step() {
		if (epoch < maxEpoch) {
			adjust();
			updateParameters();
		}
	}

	private void adjust() {
		Point2D tempXYD = new Point2D.Double();

		tempXYD.setLocation(10 + Math.random() * getSize().getWidth(), 10 + Math.random() * getSize().getHeight());

		V winner = elementAccessor.getVertex(this, tempXYD.getX(), tempXYD.getY());

		for (V v : getGraph().getVertices()) {
			ISOMVertexData ivd = getISOMVertexData(v);
			ivd.distance = 0;
			ivd.visited = false;
		}

		adjustVertex(winner, tempXYD);
	}

	private void updateParameters() {
		double factor = Math.exp(-1 * coolingFactor * (1.0 * ++epoch / maxEpoch));

		adaption = Math.max(minAdaption, factor * initialAdaption);

		if ((radius > minRadius) && (epoch % radiusConstantTime == 0)) {
			radius--;
		}
	}

	private void adjustVertex(V v, Point2D tempXYD) {
		ISOMVertexData ivd = getISOMVertexData(v);

		queue.clear();
		ivd.distance = 0;
		ivd.visited = true;
		queue.add(v);

		V current;

		while (!queue.isEmpty()) {
			current = queue.remove(0);
			ISOMVertexData currData = getISOMVertexData(current);
			Point2D currXYData = transform(current);

			double dx = tempXYD.getX() - currXYData.getX();
			double dy = tempXYD.getY() - currXYData.getY();
			double factor = adaption / Math.pow(2, currData.distance);

			currXYData.setLocation(currXYData.getX() + (factor * dx), currXYData.getY() + (factor * dy));

			if (currData.distance < radius) {
				Collection<V> s = getGraph().getNeighbors(current);

				for (V child : s) {
					ISOMVertexData childData = getISOMVertexData(child);
					if (childData != null && !childData.visited) {
						childData.visited = true;
						childData.distance = currData.distance + 1;
						queue.add(child);
					}
				}
			}
		}
	}

	private ISOMVertexData getISOMVertexData(V v) {
		return isomVertexData.get(v);
	}

	private static class ISOMVertexData {

		int distance;
		boolean visited;

		public ISOMVertexData() {
			distance = 0;
			visited = false;
		}
	}
}