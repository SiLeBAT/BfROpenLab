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
package de.bund.bfr.jung;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

public class BetterTranslatingGraphMousePlugin extends AbstractGraphMousePlugin
		implements MouseListener, MouseMotionListener {

	private EventListenerList listeners;

	private boolean changed;

	public BetterTranslatingGraphMousePlugin() {
		super(0);
		listeners = new EventListenerList();
	}

	public void addChangeListener(JungListener listener) {
		listeners.add(JungListener.class, listener);
	}

	public void removeChangeListener(JungListener listener) {
		listeners.remove(JungListener.class, listener);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();

		if (e.getButton() == MouseEvent.BUTTON1) {
			changed = false;

			down = e.getPoint();
			vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();

		if (e.getButton() == MouseEvent.BUTTON1) {
			down = null;
			vv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			if (changed) {
				Stream.of(listeners.getListeners(JungListener.class)).forEach(l -> l.transformFinished());
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();

		if (down != null) {
			MutableTransformer modelTransformer = vv.getRenderContext().getMultiLayerTransformer()
					.getTransformer(Layer.LAYOUT);
			Point2D p1 = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
			Point2D p2 = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());

			modelTransformer.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
			down = e.getPoint();
			vv.repaint();

			changed = true;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
