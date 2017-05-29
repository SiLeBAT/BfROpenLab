package de.bund.bfr.knime.openkrise.util.json;

import com.google.common.collect.ImmutableSet;

public class JsonConstants {

	public static final String JSON_COLUMN = "json";

	public static final String STATION_ID_PREFIX = "S";
	public static final String DELIVERY_ID_PREFIX = "D";

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
