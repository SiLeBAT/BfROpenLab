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
package de.bund.bfr.knime.gis.views.canvas.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class EdgePropertySchema extends PropertySchema {

	private String id;
	private String from;
	private String to;

	public EdgePropertySchema() {
		this(new LinkedHashMap<>(0), null, null, null);
	}

	public EdgePropertySchema(Map<String, Class<?>> map, String id, String from, String to) {
		super(map);
		this.id = id;
		this.from = from;
		this.to = to;
	}

	public String getId() {
		return id;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	@Override
	public Type getType() {
		return Type.EDGE;
	}
}
