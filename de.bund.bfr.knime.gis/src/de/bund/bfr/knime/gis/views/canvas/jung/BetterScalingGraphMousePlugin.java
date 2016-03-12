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
package de.bund.bfr.knime.gis.views.canvas.jung;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;

import de.bund.bfr.knime.gis.views.canvas.jung.BetterGraphMouse.ChangeListener;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;

public class BetterScalingGraphMousePlugin extends AbstractGraphMousePlugin implements MouseWheelListener {

	private EventListenerList listeners;

	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> lastTask;

	protected float in;
	protected float out;

	public BetterScalingGraphMousePlugin(float in, float out) {
		super(0);
		this.in = in;
		this.out = out;
		listeners = new EventListenerList();
		scheduler = Executors.newScheduledThreadPool(1);
		lastTask = null;
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(ChangeListener.class, listener);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() == 0) {
			return;
		}

		VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();

		new LayoutScalingControl().scale(vv, e.getWheelRotation() > 0 ? in : out, e.getPoint());
		vv.repaint();

		if (lastTask != null) {
			lastTask.cancel(false);
		}

		lastTask = scheduler.schedule(
				() -> Stream.of(listeners.getListeners(ChangeListener.class)).forEach(l -> l.transformFinished()), 200,
				TimeUnit.MILLISECONDS);
	}
}
