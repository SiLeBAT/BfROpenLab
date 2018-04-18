/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.canvas.element;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocationNode extends Node {

	private Point2D center;

	public LocationNode(String id, Map<String, Object> properties, Point2D center) {
		super(id, properties);
		updateCenter(center);
	}

	public Point2D getCenter() {
		return center;
	}

	public void updateCenter(Point2D center) {
		this.center = center;
	}

	@Override
	public LocationNode copy() {
		return new LocationNode(getId(), new LinkedHashMap<>(getProperties()), center);
	}
}
