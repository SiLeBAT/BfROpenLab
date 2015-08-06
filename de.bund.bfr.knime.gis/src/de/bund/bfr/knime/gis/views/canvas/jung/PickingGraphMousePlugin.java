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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;

public class PickingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
		implements MouseListener, MouseMotionListener {

	protected V vertex;
	protected E edge;

	private Rectangle2D rect = new Rectangle2D.Float();
	private Paintable lensPaintable;

	private List<PickingChangeListener> listeners;

	public PickingGraphMousePlugin() {
		super(InputEvent.BUTTON1_MASK);

		listeners = new ArrayList<>();
		cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		lensPaintable = new Paintable() {

			@Override
			public void paint(Graphics g) {
				Color oldColor = g.getColor();

				g.setColor(Color.CYAN);
				((Graphics2D) g).draw(rect);
				g.setColor(oldColor);
			}

			@Override
			public boolean useTransform() {
				return false;
			}
		};
	}

	public void addPickingChangeListener(PickingChangeListener listener) {
		listeners.add(listener);
	}

	public void removePickingChangeListener(PickingChangeListener listener) {
		listeners.remove(listener);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mousePressed(MouseEvent e) {
		down = e.getPoint();
		VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
		GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
		PickedState<V> pickedVertexState = vv.getPickedVertexState();
		PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
		if (e.getButton() == MouseEvent.BUTTON1 && pickSupport != null && pickedVertexState != null) {
			Layout<V, E> layout = vv.getGraphLayout();
			if (!e.isShiftDown()) {
				rect.setFrameFromDiagonal(down, down);
				Point2D ip = e.getPoint();

				vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
				if (vertex != null) {
					if (pickedVertexState.isPicked(vertex) == false) {
						pickedVertexState.clear();
						pickedVertexState.pick(vertex, true);
						fireNodePickingChanged();
					}
				} else if ((edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
					pickedEdgeState.clear();
					pickedEdgeState.pick(edge, true);
					fireEdgePickingChanged();
				} else {
					vv.addPostRenderPaintable(lensPaintable);
					pickedEdgeState.clear();
					pickedVertexState.clear();
					fireNodePickingChanged();
					fireEdgePickingChanged();
				}
			} else {
				vv.addPostRenderPaintable(lensPaintable);
				rect.setFrameFromDiagonal(down, down);
				Point2D ip = e.getPoint();
				vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
				if (vertex != null) {
					boolean wasThere = pickedVertexState.pick(vertex, !pickedVertexState.isPicked(vertex));
					fireNodePickingChanged();
					if (wasThere) {
						vertex = null;
					}
				} else if ((edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
					pickedEdgeState.pick(edge, !pickedEdgeState.isPicked(edge));
					fireEdgePickingChanged();
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mouseReleased(MouseEvent e) {
		VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
		Layout<V, E> layout = vv.getGraphLayout();
		PickedState<V> pickedVertexState = vv.getPickedVertexState();
		GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
		Point2D out = e.getPoint();

		rect.setFrameFromDiagonal(down, e.getPoint());

		if (down != null && down.distance(out) > 5 && e.getButton() == MouseEvent.BUTTON1) {
			if (!e.isShiftDown()) {
				pickedVertexState.clear();
			}

			for (V v : pickSupport.getVertices(layout, rect)) {
				pickedVertexState.pick(v, true);
			}

			fireNodePickingChanged();
		}

		down = null;
		vertex = null;
		edge = null;
		rect.setFrame(0, 0, 0, 0);
		vv.removePostRenderPaintable(lensPaintable);
		vv.repaint();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mouseDragged(MouseEvent e) {
		VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
		if (vertex != null) {
			Point p = e.getPoint();
			Point2D graphPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
			Point2D graphDown = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
			Layout<V, E> layout = vv.getGraphLayout();
			double dx = graphPoint.getX() - graphDown.getX();
			double dy = graphPoint.getY() - graphDown.getY();
			PickedState<V> ps = vv.getPickedVertexState();

			for (V v : ps.getPicked()) {
				Point2D vp = layout.transform(v);
				vp.setLocation(vp.getX() + dx, vp.getY() + dy);
				layout.setLocation(v, vp);
			}
			down = p;
		} else {
			rect.setFrameFromDiagonal(down, e.getPoint());
		}

		vv.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		((Component) e.getSource()).setCursor(cursor);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	private void fireNodePickingChanged() {
		for (PickingChangeListener listener : listeners) {
			listener.nodePickingChanged();
		}
	}

	private void fireEdgePickingChanged() {
		for (PickingChangeListener listener : listeners) {
			listener.edgePickingChanged();
		}
	}
}
