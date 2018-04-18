/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import com.google.common.collect.Sets;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class BetterDirectedSparseMultigraph<V, E> extends DirectedSparseMultigraph<V, E> {

	private static final long serialVersionUID = 1L;

	private BetterVisualizationViewer<V, E> owner;

	public BetterDirectedSparseMultigraph(BetterVisualizationViewer<V, E> owner) {
		this.owner = owner;
		vertices = new LinkedHashMap<>();
		edges = new LinkedHashMap<>();
	}

	@Override
	public Collection<V> getVertices() {
		Set<V> picked = owner.getPickedVertexState().getPicked();
		Set<V> unPicked = Sets.difference(vertices.keySet(), picked);

		return Sets.union(unPicked, picked);
	}

	@Override
	public Collection<E> getEdges() {
		Set<E> picked = owner.getPickedEdgeState().getPicked();
		Set<E> unPicked = Sets.difference(edges.keySet(), picked);

		return Sets.union(unPicked, picked);
	}
}
