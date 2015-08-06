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

import java.awt.event.InputEvent;

import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;

public class GraphMouse<V, E> extends AbstractModalGraphMouse {

	private boolean pickingDeactivated;

	public GraphMouse(PickingGraphMousePlugin<V, E> pickingPlugin, double zoomingFactor, boolean gisScaling) {
		super((float) (1 / zoomingFactor), (float) zoomingFactor);

		if (gisScaling) {
			scalingPlugin = new GisScalingGraphMousePlugin(new LayoutScalingControl(), in, out);
		} else {
			scalingPlugin = new ScalingGraphMousePlugin(new LayoutScalingControl(), 0, in, out);
		}

		translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
		this.pickingPlugin = pickingPlugin;
		pickingDeactivated = false;
		add(scalingPlugin);
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
	public void addPickingChangeListener(PickingChangeListener listener) {
		((PickingGraphMousePlugin<V, E>) pickingPlugin).addPickingChangeListener(listener);
	}

	@SuppressWarnings("unchecked")
	public void removePickingChangeListener(PickingChangeListener listener) {
		((PickingGraphMousePlugin<V, E>) pickingPlugin).removePickingChangeListener(listener);
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

			this.pickingDeactivated = pickingDeactivated;
		}
	}
}
