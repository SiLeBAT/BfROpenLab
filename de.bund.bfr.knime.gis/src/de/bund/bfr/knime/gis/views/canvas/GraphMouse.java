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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.event.InputEvent;

import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;

public class GraphMouse<V, E> extends AbstractModalGraphMouse {

	public GraphMouse() {
		this(new PickingGraphMousePlugin<V, E>());
	}

	public GraphMouse(PickingGraphMousePlugin<V, E> pickingPlugin) {
		super(1.1f, 1 / 1.1f);

		scalingPlugin = new ScalingGraphMousePlugin(new LayoutScalingControl(),
				0, in, out);
		translatingPlugin = new TranslatingGraphMousePlugin(
				InputEvent.BUTTON1_MASK);
		this.pickingPlugin = pickingPlugin;
	}

	@Override
	protected void loadPlugins() {
	}

	@Override
	protected void setPickingMode() {
		remove(scalingPlugin);
		remove(translatingPlugin);
		add(pickingPlugin);
	}

	@Override
	protected void setTransformingMode() {
		remove(pickingPlugin);
		add(scalingPlugin);
		add(translatingPlugin);
	}
}
