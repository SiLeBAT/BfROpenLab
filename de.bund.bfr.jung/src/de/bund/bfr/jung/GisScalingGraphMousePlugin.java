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

import java.awt.event.MouseWheelEvent;

public class GisScalingGraphMousePlugin extends BetterScalingGraphMousePlugin {

	private long lastScrollTime;

	public GisScalingGraphMousePlugin(float in, float out) {
		super(in, out);
		lastScrollTime = 0;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() == 0) {
			return;
		}

		long time = System.nanoTime();

		if (time - lastScrollTime < 2e8) {
			return;
		}

		lastScrollTime = time;
		super.mouseWheelMoved(e);
	}
}
