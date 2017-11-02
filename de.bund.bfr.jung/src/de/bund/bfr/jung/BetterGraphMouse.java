/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;

import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;

public class BetterGraphMouse<V, E> extends AbstractModalGraphMouse {

	private EventListenerList listeners;

	private boolean pickingDeactivated;

	public BetterGraphMouse(BetterPickingGraphMousePlugin<V, E> pickingPlugin,
			BetterScalingGraphMousePlugin scalingPlugin) {
		super(1, 1);

		listeners = new EventListenerList();
		translatingPlugin = new BetterTranslatingGraphMousePlugin();
		this.pickingPlugin = pickingPlugin;
		this.scalingPlugin = scalingPlugin;
		pickingDeactivated = false;
		add(scalingPlugin);
	}

	public Mode getMode() {
		return mode;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);

		if (e.getButton() == MouseEvent.BUTTON2) {
			BetterVisualizationViewer<V, E> vv = (BetterVisualizationViewer<V, E>) e.getSource();

			if (mode == Mode.TRANSFORMING) {
				setMode(Mode.PICKING);
				vv.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				setMode(Mode.TRANSFORMING);
				vv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		Stream.of(listeners.getListeners(JungListener.class)).forEach(l -> l.modeChangeFinished());
	}

	@Override
	protected void loadPlugins() {
	}

	@Override
	protected void setPickingMode() {
		remove(translatingPlugin);

		if (!pickingDeactivated) {
			add(pickingPlugin);
		}
	}

	@Override
	protected void setTransformingMode() {
		remove(pickingPlugin);
		add(translatingPlugin);
	}

	@SuppressWarnings("unchecked")
	public void addChangeListener(JungListener listener) {
		listeners.add(JungListener.class, listener);
		((BetterTranslatingGraphMousePlugin) translatingPlugin).addChangeListener(listener);
		((BetterScalingGraphMousePlugin) scalingPlugin).addChangeListener(listener);
		((BetterPickingGraphMousePlugin<V, E>) pickingPlugin).addChangeListener(listener);
	}

	@SuppressWarnings("unchecked")
	public void removeChangeListener(JungListener listener) {
		listeners.remove(JungListener.class, listener);
		((BetterTranslatingGraphMousePlugin) translatingPlugin).removeChangeListener(listener);
		((BetterScalingGraphMousePlugin) scalingPlugin).removeChangeListener(listener);
		((BetterPickingGraphMousePlugin<V, E>) pickingPlugin).removeChangeListener(listener);
	}

	public boolean isPickingDeactivated() {
		return pickingDeactivated;
	}

	public void setPickingDeactivated(boolean pickingDeactivated) {
		if (pickingDeactivated != this.pickingDeactivated && mode == Mode.PICKING) {
			if (pickingDeactivated) {
				remove(pickingPlugin);
			} else {
				add(pickingPlugin);
			}
		}

		this.pickingDeactivated = pickingDeactivated;
	}
	
	// public BetterPickingGraphMousePlugin<V,E> getPickingPlugin() { return (BetterPickingGraphMousePlugin<V,E>) this.pickingPlugin; }
}
