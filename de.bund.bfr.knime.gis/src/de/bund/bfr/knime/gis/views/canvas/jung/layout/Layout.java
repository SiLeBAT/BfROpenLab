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
package de.bund.bfr.knime.gis.views.canvas.jung.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleConsumer;

import edu.uci.ics.jung.graph.Graph;

public abstract class Layout<V, E> {

	protected Graph<V, E> graph;
	protected Dimension size;

	protected Set<V> locked;

	public Layout(Graph<V, E> graph, Dimension size) {
		this.graph = graph;
		this.size = size;
		locked = new LinkedHashSet<>();
	}

	public Graph<V, E> getGraph() {
		return graph;
	}

	public Dimension getSize() {
		return size;
	}

	public void setLocked(V v, boolean locked) {
		if (locked) {
			this.locked.add(v);
		} else {
			this.locked.remove(v);
		}
	}

	public boolean isLocked(V v) {
		return locked.contains(v);
	}

	public abstract Map<V, Point2D> getNodePositions(Map<V, Point2D> initialPositions, DoubleConsumer progressListener);
}
