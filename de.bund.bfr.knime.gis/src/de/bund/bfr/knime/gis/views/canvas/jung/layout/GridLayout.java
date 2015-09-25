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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;

public class GridLayout<V, E> extends AbstractLayout<V, E> {

	public GridLayout(Graph<V, E> graph, Dimension size) {
		super(graph, size);
	}

	@Override
	public Map<V, Point2D> getNodePositions(Map<V, Point2D> initialPositions) {
		Map<V, Point2D> newPositions = new LinkedHashMap<>(initialPositions);
		List<V> nodes = new ArrayList<>(graph.getVertices());

		nodes.removeAll(locked);

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

				newPositions.put(nodes.get(index), new Point2D.Double(i * d + sx, j * d + sy));
				index++;
			}
		}

		return newPositions;
	}
}
