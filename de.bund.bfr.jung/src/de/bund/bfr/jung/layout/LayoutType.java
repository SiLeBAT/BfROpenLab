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
package de.bund.bfr.jung.layout;

import java.awt.Dimension;

import edu.uci.ics.jung.graph.Graph;

public enum LayoutType {
	GRID_LAYOUT("Grid Layout") {
		@Override
		public <V, E> Layout<V, E> create(Graph<V, E> graph, Dimension size) {
			return new GridLayout<>(graph, size);
		}
	},
	CIRCLE_LAYOUT("Circle Layout") {
		@Override
		public <V, E> Layout<V, E> create(Graph<V, E> graph, Dimension size) {
			return new CircleLayout<>(graph, size);
		}
	},
	FR_LAYOUT("Fruchterman-Reingold") {
		@Override
		public <V, E> Layout<V, E> create(Graph<V, E> graph, Dimension size) {
			return new FRLayout<>(graph, size, false);
		}
	},
	ISOM_LAYOUT("Self-Organizing Map") {
		@Override
		public <V, E> Layout<V, E> create(Graph<V, E> graph, Dimension size) {
			return new ISOMLayout<>(graph, size);
		}
	};

	private String name;

	private LayoutType(String name) {
		this.name = name;
	}

	public abstract <V, E> Layout<V, E> create(Graph<V, E> graph, Dimension size);

	@Override
	public String toString() {
		return name;
	}
}
