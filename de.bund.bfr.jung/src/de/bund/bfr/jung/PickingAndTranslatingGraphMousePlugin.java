/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;

public class PickingAndTranslatingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin 
implements MouseListener, MouseMotionListener, JungListener, KeyListener {

	private EventListenerList listeners;

	private boolean pickingStarted;
	private boolean pickingIsDeactivated;
	private boolean translatingStarted;
	private BetterPickingGraphMousePlugin<V,E> pickingPlugin;
	private BetterTranslatingGraphMousePlugin translatingPlugin;
	private boolean keyListenerAdded;
	private boolean mouseHoversOverNodeOrEdge; // this global status is needed because the information is otherwise not available on the keyrelease event

	public PickingAndTranslatingGraphMousePlugin(BetterPickingGraphMousePlugin<V,E> pickingPlugin, BetterTranslatingGraphMousePlugin translatingPlugin) {
		super(0);
		this.pickingPlugin = pickingPlugin;
		this.pickingIsDeactivated = false;
		if (this.pickingPlugin != null) this.pickingPlugin.addChangeListener(this);
		this.translatingPlugin = translatingPlugin;
		if (this.translatingPlugin != null) this.translatingPlugin.addChangeListener(this);
		this.listeners = new EventListenerList();
		this.keyListenerAdded = false;
	}

	public void addChangeListener(JungListener listener) {
		listeners.add(JungListener.class, listener);
	}

	public void removeChangeListener(JungListener listener) {
		listeners.remove(JungListener.class, listener);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (this.pickingPlugin != null && !pickingIsDeactivated) this.pickingPlugin.mouseClicked(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		boolean isPickingEvent = (e.getButton() == MouseEvent.BUTTON1) && (e.isShiftDown() || this.hoversMouseOverEdgeOrNode(e)) && !pickingIsDeactivated;
		boolean isTranslatingEvent = (e.getButton() == MouseEvent.BUTTON1) && !isPickingEvent;

		if (isPickingEvent && (this.pickingPlugin != null)) {

			this.pickingStarted = true;
			this.pickingPlugin.mousePressed(e);

		} else if (isTranslatingEvent && (this.translatingPlugin != null)) {

			this.translatingStarted = true;
			this.translatingPlugin.mousePressed(e);

		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		if (this.pickingStarted) {

			this.pickingStarted = false;
			this.pickingPlugin.mouseReleased(e);

		} else if (this.translatingStarted) {

			this.translatingStarted = false;
			this.translatingPlugin.mouseReleased(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		if (this.pickingStarted) {
			this.pickingPlugin.mouseDragged(e);	
		} else if (this.translatingStarted) {
			this.translatingPlugin.mouseDragged(e);	
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mouseEntered(MouseEvent e) {

		this.mouseHoversOverNodeOrEdge = this.hoversMouseOverEdgeOrNode(e);
		// the focus is requested to receive key events
		((VisualizationViewer<V, E>) e.getSource()).requestFocus();
		if(!this.keyListenerAdded) {
			this.keyListenerAdded = true;
			((VisualizationViewer<V, E>) e.getSource()).addKeyListener(this);
		}
		if (!this.translatingStarted && this.mouseHoversOverNodeOrEdge) {
			if(!pickingIsDeactivated) ((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	private boolean hoversMouseOverEdgeOrNode(MouseEvent e) {
		@SuppressWarnings("unchecked")
		VisualizationViewer<V, E> viewer = (VisualizationViewer<V, E>) e.getSource();

		return (viewer.getPickSupport().getVertex(viewer.getGraphLayout(), e.getX(), e.getY()) != null) ||
				(viewer.getPickSupport().getEdge(viewer.getGraphLayout(), e.getX(), e.getY()) != null);
	}
	
	public void setPickingDeactivated(boolean deactivated) {
	  this.pickingIsDeactivated = deactivated;
	  if(!deactivated) pickingStarted = false;
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		this.mouseHoversOverNodeOrEdge = false;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
		this.mouseHoversOverNodeOrEdge = this.hoversMouseOverEdgeOrNode(e);
		
		if (!this.translatingStarted && (this.hoversMouseOverEdgeOrNode(e) || e.isShiftDown())) {
			if(!pickingIsDeactivated) ((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			else ((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else {
			((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void setCursor(InputEvent e, int type) {
		((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(type));
	}
	
	private void call(Consumer<JungListener> action) {
		Stream.of(listeners.getListeners(JungListener.class)).forEach(action);
	}

	@Override
	public void pickingFinished() {
		this.call(l -> l.pickingFinished());
	}

	@Override
	public void nodePickingFinished() {
		this.call(l -> l.nodePickingFinished());
	}

	@Override
	public void edgePickingFinished() {
		this.call(l -> l.edgePickingFinished());
	}

	@Override
	public void nodeMovementFinished() {
		this.call(l -> l.nodeMovementFinished());
	}

	@Override
	public void transformFinished() {
		this.call(l -> l.transformFinished());
	}

	@Override
	public void modeChangeFinished() {}

	@Override
	public void doubleClickedOn(Object obj, MouseEvent e) {
		this.call(l -> l.doubleClickedOn(obj, e));
	}

	@Override
	public void keyTyped(KeyEvent e) {}
		

	@Override
	public void keyPressed(KeyEvent e) {

		if (!(this.translatingStarted || this.pickingStarted)) {
			
			if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				// show activ picking mode
				this.setCursor(e, Cursor.HAND_CURSOR);
			} else {
				if(!this.mouseHoversOverNodeOrEdge)	this.setCursor(e, Cursor.DEFAULT_CURSOR);
			}
			
		} else {
			// translation mode does not care about this event
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!(this.translatingStarted || this.pickingStarted)) {
			if (!this.mouseHoversOverNodeOrEdge && (e.getKeyCode() == KeyEvent.VK_SHIFT)) this.setCursor(e, Cursor.DEFAULT_CURSOR);
		}
	}
}
