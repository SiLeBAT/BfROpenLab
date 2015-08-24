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

import java.awt.geom.Rectangle2D;
import java.util.Collection;

import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class RegionCanvasUtils {

	private RegionCanvasUtils() {
	}

	public static Rectangle2D getBounds(Collection<RegionNode> nodes) {
		Rectangle2D bounds = null;

		for (RegionNode node : nodes) {
			bounds = bounds != null ? bounds.createUnion(node.getBoundingBox()) : node.getBoundingBox();
		}

		return bounds;
	}
}
