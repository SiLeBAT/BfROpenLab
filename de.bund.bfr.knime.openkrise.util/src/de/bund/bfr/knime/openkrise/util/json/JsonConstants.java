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
package de.bund.bfr.knime.openkrise.util.json;

import com.google.common.collect.ImmutableSet;

public class JsonConstants {

	public static final String JSON_COLUMN = "json";

	public static final String STATION_ID_PREFIX = "S";
	public static final String DELIVERY_ID_PREFIX = "D";
	
	public static final String SETTINGS = "settings";

	public static final String ELEMENTS = "elements";
	public static final String STATIONS = "stations";
	public static final String DELIVERIES = "deliveries";
	public static final String DELIVERY_RELATIONS = "deliveriesRelations";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String SOURCE = "source";
	public static final String TARGET = "target";
	public static final String LOT = "lot";
	public static final String DATE = "date";
	public static final String CONNECTIONS = "connections";
	public static final String PROPERTIES = "properties";

	public static final ImmutableSet<String> STATION_PROPERTIES = ImmutableSet.of("id", "name", "incoming", "outgoing",
			"connections", "invisible", "contained", "contains", "selected", "observed", "forward", "backward",
			"outbreak", "crossContamination", "score", "commonLink", "position", "positionRelativeTo", "properties");
	public static final ImmutableSet<String> DELIVERY_PROPERTIES = ImmutableSet.of("id", "name", "lot", "date",
			"source", "target", "originalSource", "originalTarget", "invisible", "selected", "observed", "forward",
			"backward", "score", "properties");

	private JsonConstants() {
	}
}
