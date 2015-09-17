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

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;

public class BetterScalingGraphMousePlugin extends AbstractGraphMousePlugin implements MouseWheelListener, Runnable {

	private static final long TIME_OUT = (long) 2e8;

	private List<ChangeListener> changeListeners;

	private Thread changeThread;
	private long lastEvent;

	protected float in;
	protected float out;

	public BetterScalingGraphMousePlugin(float in, float out) {
		super(0);
		this.in = in;
		this.out = out;
		changeListeners = new ArrayList<>();
		changeThread = null;
		lastEvent = 0;
	}

	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		changeListeners.remove(listener);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() == 0) {
			return;
		}

		VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();

		new LayoutScalingControl().scale(vv, e.getWheelRotation() > 0 ? in : out, e.getPoint());
		vv.repaint();
		lastEvent = System.nanoTime();

		if (changeThread == null || !changeThread.isAlive()) {
			changeThread = new Thread(this);
			changeThread.start();
		}
	}

	@Override
	public void run() {
		while (true) {
			long wait = lastEvent + TIME_OUT - System.nanoTime();

			if (wait <= 0) {
				break;
			}

			try {
				Thread.sleep(wait / (long) 1e6);
			} catch (InterruptedException e) {
			}
		}

		for (ChangeListener listener : changeListeners) {
			listener.scalingChanged();
		}
	}

	public interface ChangeListener {

		void scalingChanged();
	}
}
