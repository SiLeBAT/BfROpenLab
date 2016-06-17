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
package de.bund.bfr.jung;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;

public class BetterVisualizationViewer<V, E> extends VisualizationViewer<V, E> {

	private static final long serialVersionUID = 1L;

	public BetterVisualizationViewer() {
		super(new ChangeSupportLayout<>(new StaticLayout<>(new DirectedSparseMultigraph<>())));
		getRenderer().setEdgeRenderer(new BetterEdgeRenderer<>());
		getRenderer().setEdgeLabelRenderer(new BetterEdgeLabelRenderer<>());
		getRenderer().setVertexLabelRenderer(new BetterVertexLabelRenderer<>());
		setPickSupport(new BetterShapePickSupport<>(this));
		getGraphLayout().setGraph(new BetterDirectedSparseMultigraph<>(this));
		setDoubleBuffered(true);
	}

	public void drawRect(Rectangle2D rect) {
		Graphics2D g = (Graphics2D) getGraphics();

		g.drawImage(offscreen, 0, 0, null);

		if (rect != null) {
			Color currentColor = g.getColor();

			g.setColor(Color.CYAN);
			g.draw(rect);
			g.setColor(currentColor);
		}
	}

	@Override
	public void setGraphLayout(Layout<V, E> layout) {
		super.setGraphLayout(layout instanceof ChangeEventSupport ? layout : new ChangeSupportLayout<>(layout));
	}

	private static class ChangeSupportLayout<V, E> extends LayoutDecorator<V, E> implements ChangeEventSupport {

		private ChangeEventSupport changeSupport;

		public ChangeSupportLayout(Layout<V, E> delegate) {
			super(delegate);
			changeSupport = new DefaultChangeEventSupport(this);
		}

		@Override
		public void step() {
			super.step();
			fireStateChanged();
		}

		@Override
		public void initialize() {
			super.initialize();
			fireStateChanged();
		}

		@Override
		public boolean done() {
			if (delegate instanceof IterativeContext) {
				return ((IterativeContext) delegate).done();
			}

			return true;
		}

		@Override
		public void setLocation(V v, Point2D location) {
			super.setLocation(v, location);
			fireStateChanged();
		}

		@Override
		public void addChangeListener(ChangeListener l) {
			changeSupport.addChangeListener(l);
		}

		@Override
		public void removeChangeListener(ChangeListener l) {
			changeSupport.removeChangeListener(l);
		}

		@Override
		public ChangeListener[] getChangeListeners() {
			return changeSupport.getChangeListeners();
		}

		@Override
		public void fireStateChanged() {
			changeSupport.fireStateChanged();
		}
	}
}
