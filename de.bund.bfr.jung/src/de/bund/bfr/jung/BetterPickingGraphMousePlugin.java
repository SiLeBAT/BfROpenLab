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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;

import de.bund.bfr.knime.PointUtils;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;

public class BetterPickingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
		implements MouseListener, MouseMotionListener {

	protected V vertex;
	protected E edge;

	private boolean allowMovingNodes;

	private Rectangle2D rect = new Rectangle2D.Float();

	private EventListenerList listeners;

	private boolean nodesMoved;

	public BetterPickingGraphMousePlugin(boolean allowMovingNodes) {
		super(0);
		this.allowMovingNodes = allowMovingNodes;
		listeners = new EventListenerList();
	}

	public void addChangeListener(JungListener listener) {
		listeners.add(JungListener.class, listener);
	}

	public void removeChangeListener(JungListener listener) {
		listeners.remove(JungListener.class, listener);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			V node = getPickedNode(e);
			E edge = getPickedEdge(e);

			if (node != null) {
				call(l -> l.doubleClickedOn(node));
			} else if (edge != null) {
				call(l -> l.doubleClickedOn(edge));
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mousePressed(MouseEvent e) {
		down = e.getPoint();
		nodesMoved = false;

		PickedState<V> pickedVertexState = ((VisualizationViewer<V, E>) e.getSource()).getPickedVertexState();
		PickedState<E> pickedEdgeState = ((VisualizationViewer<V, E>) e.getSource()).getPickedEdgeState();

		if (e.getButton() == MouseEvent.BUTTON1) {
			rect.setFrameFromDiagonal(down, down);

			if (!e.isShiftDown()) {
				if ((vertex = getPickedNode(e)) != null) {
					if (!pickedVertexState.isPicked(vertex)) {
						pickedVertexState.clear();
						pickedVertexState.pick(vertex, true);
						call(l -> l.nodePickingFinished());
					}
				} else if ((edge = getPickedEdge(e)) != null) {
					if (!pickedEdgeState.isPicked(edge)) {
						pickedEdgeState.clear();
						pickedEdgeState.pick(edge, true);
						call(l -> l.edgePickingFinished());
					}
				} else {
					boolean nodesPicked = !pickedVertexState.getPicked().isEmpty();
					boolean edgesPicked = !pickedEdgeState.getPicked().isEmpty();

					if (nodesPicked && edgesPicked) {
						pickedVertexState.clear();
						pickedEdgeState.clear();
						call(l -> l.pickingFinished());
					} else if (nodesPicked) {
						pickedVertexState.clear();
						call(l -> l.nodePickingFinished());
					} else if (edgesPicked) {
						pickedEdgeState.clear();
						call(l -> l.edgePickingFinished());
					}
				}
			} else {
				if ((vertex = getPickedNode(e)) != null) {
					if (pickedVertexState.pick(vertex, !pickedVertexState.isPicked(vertex))) {
						vertex = null;
					}

					call(l -> l.nodePickingFinished());
				} else if ((edge = getPickedEdge(e)) != null) {
					if (pickedEdgeState.pick(edge, !pickedEdgeState.isPicked(edge))) {
						edge = null;
					}

					call(l -> l.edgePickingFinished());
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mouseReleased(MouseEvent e) {
		BetterVisualizationViewer<V, E> vv = (BetterVisualizationViewer<V, E>) e.getSource();
		PickedState<V> pickedVertexState = vv.getPickedVertexState();
		GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();

		if (down != null && down.distance(e.getPoint()) > 5 && e.getButton() == MouseEvent.BUTTON1) {
			rect.setFrameFromDiagonal(down, e.getPoint());

			if (!e.isShiftDown()) {
				pickedVertexState.clear();
			}

			for (V v : pickSupport.getVertices(vv.getGraphLayout(), rect)) {
				pickedVertexState.pick(v, true);
			}

			call(l -> l.nodePickingFinished());
		}

		if (nodesMoved) {
			call(l -> l.nodeMovementFinished());
		}

		down = null;
		nodesMoved = false;
		vertex = null;
		edge = null;
		rect.setFrame(0, 0, 0, 0);
		vv.drawRect(null);
		vv.repaint();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mouseDragged(MouseEvent e) {
		BetterVisualizationViewer<V, E> vv = (BetterVisualizationViewer<V, E>) e.getSource();

		if (vertex != null) {
			if (allowMovingNodes) {
				Point2D graphPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());
				Point2D graphDown = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
				Point2D move = PointUtils.substractPoints(graphPoint, graphDown);
				Layout<V, E> layout = vv.getGraphLayout();
				PickedState<V> ps = vv.getPickedVertexState();

				for (V v : ps.getPicked()) {
					layout.setLocation(v, PointUtils.addPoints(layout.transform(v), move));
				}

				nodesMoved = true;
				vv.repaint();
			}

			down = e.getPoint();
		} else if (edge != null) {
			down = e.getPoint();
		} else {
			rect.setFrameFromDiagonal(down, e.getPoint());
			vv.drawRect(rect);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@SuppressWarnings("unchecked")
	protected V getPickedNode(MouseEvent e) {
		VisualizationViewer<V, E> viewer = (VisualizationViewer<V, E>) e.getSource();

		return viewer.getPickSupport().getVertex(viewer.getGraphLayout(), e.getX(), e.getY());
	}

	@SuppressWarnings("unchecked")
	protected E getPickedEdge(MouseEvent e) {
		VisualizationViewer<V, E> viewer = (VisualizationViewer<V, E>) e.getSource();

		return viewer.getPickSupport().getEdge(viewer.getGraphLayout(), e.getX(), e.getY());
	}

	private void call(Consumer<JungListener> action) {
		Stream.of(listeners.getListeners(JungListener.class)).forEach(action);
	}
}
