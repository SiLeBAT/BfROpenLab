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

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingControl;

public class GisScalingGraphMousePlugin extends AbstractGraphMousePlugin implements MouseWheelListener {

	private static final long TIME_OUT = (long) 2e8;

	private ScalingControl scaler;
	private float in;
	private float out;

	private long lastScrollTime;

	public GisScalingGraphMousePlugin(ScalingControl scaler, float in, float out) {
		super(0);
		this.scaler = scaler;
		this.in = in;
		this.out = out;
		lastScrollTime = 0;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() == 0) {
			return;
		}

		long time = System.nanoTime();

		if (time - lastScrollTime < TIME_OUT) {
			return;
		}

		lastScrollTime = time;

		VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();

		if (e.getWheelRotation() > 0) {
			scaler.scale(vv, in, e.getPoint());
		} else {
			scaler.scale(vv, out, e.getPoint());
		}

		vv.repaint();
	}
}
