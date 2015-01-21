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
package de.bund.bfr.knime.gis.views.canvas.layout;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;

public class GridLayout<V, E> extends AbstractLayout<V, E> {

	public GridLayout(Graph<V, E> graph) {
		super(graph);
	}

	@Override
	public void initialize() {
		List<V> nodes = new ArrayList<>();

		for (V node : getGraph().getVertices()) {
			if (!isLocked(node)) {
				nodes.add(node);
			}
		}

		int n = (int) Math.ceil(Math.sqrt(nodes.size()));
		int index = 0;
		double width = getSize().getWidth();
		double height = getSize().getHeight();
		double d = Math.min(width, height) / (n + 1);
		double sx = width / 2 - (n - 1) * d / 2;
		double sy = height / 2 - (n - 1) * d / 2;

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (index >= nodes.size()) {
					break;
				}

				transform(nodes.get(index)).setLocation(i * d + sx, j * d + sy);
				index++;
			}
		}
	}

	@Override
	public void reset() {
		initialize();
	}

}
