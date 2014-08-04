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

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;

public class CircleLayout<V, E> extends AbstractLayout<V, E> {

	public CircleLayout(Graph<V, E> g) {
		super(g);
	}

	@Override
	public void initialize() {
		List<V> nodes = new ArrayList<>();

		for (V node : getGraph().getVertices()) {
			if (!isLocked(node)) {
				nodes.add(node);
			}
		}

		double width = getSize().getWidth();
		double height = getSize().getHeight();
		double radius = 0.45 * Math.min(width, height);

		for (int i = 0; i < nodes.size(); i++) {
			double angle = (2 * Math.PI * i) / nodes.size();

			transform(nodes.get(i)).setLocation(
					Math.cos(angle) * radius + width / 2,
					Math.sin(angle) * radius + height / 2);
		}
	}

	@Override
	public void reset() {
		initialize();
	}
}
