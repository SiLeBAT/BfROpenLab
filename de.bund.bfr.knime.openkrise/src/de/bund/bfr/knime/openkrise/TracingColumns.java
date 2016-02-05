/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.openkrise;

import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;

public interface TracingColumns {

	public static final String ID = "ID";
	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String NEXT = "Next";
	public static final String FILESOURCES = "ImportSources";

	public static final String WEIGHT = "Weight";
	public static final String CROSS_CONTAMINATION = "CrossContamination";
	public static final String KILL_CONTAMINATION = "Kill Contamination";
	public static final String SCORE = "Score";
	public static final String NORMALIZED_SCORE = "Normalized Score";
	public static final String POSITIVE_SCORE = "Positive Score";
	public static final String NEGATIVE_SCORE = "Negative Score";

	public static final String OBSERVED = "Observed";
	public static final String BACKWARD = "Backward";
	public static final String FORWARD = "Forward";

	public static final String CLUSTER_ID = "ClusterID";
	public static final String ADDRESS = "Address";
	public static final String DELIVERY_DEPARTURE = "Date Delivery";
	public static final String DELIVERY_ARRIVAL = "Date Delivery Arrival";

	public static final String STATION_NAME = "Name";
	public static final String STATION_STREET = "Street";
	public static final String STATION_HOUSENO = "HouseNumber";
	public static final String STATION_ZIP = "ZIP";
	public static final String STATION_CITY = "City";
	public static final String STATION_DISTRICT = "District";
	public static final String STATION_STATE = "State";
	public static final String STATION_COUNTRY = "Country";
	public static final String STATION_TOB = "type of business";
	public static final String STATION_SIMPLESUPPLIER = "SimpleSupplier";
	public static final String STATION_DEADSTART = "DeadStart";
	public static final String STATION_DEADEND = "DeadEnd";

	public static final String DELIVERY_ITEMNUM = "Item Number";
	public static final String DELIVERY_ITEMNAME = "Name";
	public static final String DELIVERY_AMOUNT = "Amount [kg]";
	public static final String DELIVERY_NUM_PU = "Amount";
	public static final String DELIVERY_TYPE_PU = "Amount Unit";
	public static final String DELIVERY_SERIAL = "Serial";
	public static final String DELIVERY_LOTNUM = "Lot Number";

	public static final ImmutableList<String> STATION_COLUMNS = ImmutableList.of(ID,
			TracingBackwardUtils.STATION_SERIAL, STATION_NAME, TracingBackwardUtils.STATION_NODE, STATION_TOB,
			STATION_SIMPLESUPPLIER, STATION_DEADSTART, STATION_DEADEND, FILESOURCES);
	public static final ImmutableList<String> DELIVERY_COLUMNS = ImmutableList.of(ID, DELIVERY_SERIAL, FROM, TO,
			DELIVERY_ITEMNUM, DELIVERY_ITEMNAME, DELIVERY_LOTNUM, DELIVERY_AMOUNT, DELIVERY_NUM_PU, DELIVERY_TYPE_PU,
			DELIVERY_DEPARTURE, DELIVERY_ARRIVAL, FILESOURCES);
	public static final ImmutableList<String> ADDRESS_COLUMNS = ImmutableList.of(ADDRESS, STATION_STREET,
			STATION_HOUSENO, STATION_ZIP, STATION_CITY, STATION_DISTRICT, STATION_STATE, STATION_COUNTRY,
			TracingBackwardUtils.STATION_COUNTY, GeocodingNodeModel.LATITUDE_COLUMN,
			GeocodingNodeModel.LONGITUDE_COLUMN);
	public static final ImmutableList<String> INPUT_COLUMNS = ImmutableList.of(WEIGHT, CROSS_CONTAMINATION,
			KILL_CONTAMINATION, OBSERVED);
	public static final ImmutableList<String> OUTPUT_COLUMNS = ImmutableList.of(SCORE, NORMALIZED_SCORE, POSITIVE_SCORE,
			NEGATIVE_SCORE, BACKWARD, FORWARD);
	public static final ImmutableMap<String, DataType> COLUMN_TYPES = new ImmutableMap.Builder<String, DataType>()
			.put(TracingColumns.WEIGHT, DoubleCell.TYPE).put(TracingColumns.CROSS_CONTAMINATION, BooleanCell.TYPE)
			.put(TracingColumns.KILL_CONTAMINATION, BooleanCell.TYPE).put(TracingColumns.SCORE, DoubleCell.TYPE)
			.put(TracingColumns.NORMALIZED_SCORE, DoubleCell.TYPE).put(TracingColumns.POSITIVE_SCORE, DoubleCell.TYPE)
			.put(TracingColumns.NEGATIVE_SCORE, DoubleCell.TYPE).put(TracingColumns.OBSERVED, BooleanCell.TYPE)
			.put(TracingColumns.BACKWARD, BooleanCell.TYPE).put(TracingColumns.FORWARD, BooleanCell.TYPE).build();
	public static final ImmutableMap<String, Class<?>> COLUMN_CLASSES = new ImmutableMap.Builder<String, Class<?>>()
			.put(TracingColumns.WEIGHT, Double.class).put(TracingColumns.CROSS_CONTAMINATION, Boolean.class)
			.put(TracingColumns.KILL_CONTAMINATION, Boolean.class).put(TracingColumns.SCORE, Double.class)
			.put(TracingColumns.NORMALIZED_SCORE, Double.class).put(TracingColumns.POSITIVE_SCORE, Double.class)
			.put(TracingColumns.NEGATIVE_SCORE, Double.class).put(TracingColumns.OBSERVED, Boolean.class)
			.put(TracingColumns.BACKWARD, Boolean.class).put(TracingColumns.FORWARD, Boolean.class).build();
}
