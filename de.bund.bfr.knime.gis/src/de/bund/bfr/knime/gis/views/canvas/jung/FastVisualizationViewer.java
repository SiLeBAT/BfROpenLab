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
package de.bund.bfr.knime.gis.views.canvas.jung;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class FastVisualizationViewer<V, E> extends VisualizationViewer<V, E> {

	private static final long serialVersionUID = 1L;

	public FastVisualizationViewer() {
		super(new StaticLayout<>(new DirectedSparseMultigraph<V, E>()));
		setDoubleBuffered(true);
	}

	public void drawRect(Rectangle2D rect) {
		Graphics g = this.getGraphics();

		g.drawImage(offscreen, 0, 0, null);

		if (rect != null) {
			Color oldColor = g.getColor();

			g.setColor(Color.CYAN);
			((Graphics2D) g).draw(rect);
			g.setColor(oldColor);
		}
	}
}
