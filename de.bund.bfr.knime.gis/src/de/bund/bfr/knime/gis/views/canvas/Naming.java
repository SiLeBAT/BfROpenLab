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
package de.bund.bfr.knime.gis.views.canvas;

public class Naming {

	public static final Naming DEFAULT_NAMING = new Naming("Node", "Nodes", "Edge", "Edges");

	private String node;
	private String nodes;
	private String edge;
	private String edges;

	public Naming(String node, String nodes, String edge, String edges) {
		this.node = node;
		this.nodes = nodes;
		this.edge = edge;
		this.edges = edges;
	}

	public String Node() {
		return node;
	}

	public String Nodes() {
		return nodes;
	}

	public String Edge() {
		return edge;
	}

	public String Edges() {
		return edges;
	}

	public String node() {
		return node.toLowerCase();
	}

	public String nodes() {
		return nodes.toLowerCase();
	}

	public String edge() {
		return edge.toLowerCase();
	}

	public String edges() {
		return edges.toLowerCase();
	}
}
