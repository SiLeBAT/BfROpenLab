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
package de.bund.bfr.knime.openkrise;

import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.geocode.GeocodingSettings;

public interface TracingColumns {

	public static final String ID = "ID";
	public static final String NAME = "Name";
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
	public static final String MAX_LOT_SCORE = "Max Lot Score";
	public static final String LOT_SCORE = "Lot Score";

	public static final String OBSERVED = "Observed";
	public static final String BACKWARD = "Backward";
	public static final String FORWARD = "Forward";
	
	public static final String IS_SELECTED = "Is selected";

	public static final String CLUSTER_ID = "ClusterID";
	public static final String ADDRESS = GeocodingSettings.DEFAULT_ADDRESS_COLUMN;
	public static final String DELIVERY_DEPARTURE = "Date Delivery";
	public static final String DELIVERY_ARRIVAL = "Date Delivery Arrival";

	public static final String STATION_ID = "Station ID";
	public static final String STATION_NAME = "Station Name";
	public static final String STATION_STREET = "Street";
	public static final String STATION_HOUSENO = "HouseNumber";
	public static final String STATION_ZIP = "ZIP";
	public static final String STATION_CITY = "City";
	public static final String STATION_DISTRICT = "District";
	public static final String STATION_STATE = "State";
	public static final String STATION_COUNTRY = GeocodingSettings.DEFAULT_COUNTRY_COLUMN;
	public static final String STATION_TOB = "type of business";
	public static final String STATION_SIMPLESUPPLIER = "SimpleSupplier";
	public static final String STATION_DEADSTART = "DeadStart";
	public static final String STATION_DEADEND = "DeadEnd";

	public static final String LOT_ID = "Lot ID";

	public static final String DELIVERY_ID = "Delivery ID";
	public static final String DELIVERY_AMOUNT = "Amount [kg]";
	public static final String DELIVERY_NUM_PU = "Amount";
	public static final String DELIVERY_TYPE_PU = "Amount Unit";

	public static final String PRODUCT_NUMBER = "Item Number";
	public static final String LOT_NUMBER = "Lot Number";

	public static final ImmutableList<String> STATION_COLUMNS = ImmutableList.of(ID, BackwardUtils.STATION_SERIAL, NAME,
			BackwardUtils.STATION_NODE, STATION_TOB, STATION_SIMPLESUPPLIER, STATION_DEADSTART, STATION_DEADEND,
			FILESOURCES);
	public static final ImmutableList<String> DELIVERY_COLUMNS = ImmutableList.of(ID, BackwardUtils.DELIVERY_SERIAL,
			FROM, TO, PRODUCT_NUMBER, NAME, LOT_NUMBER, DELIVERY_AMOUNT, DELIVERY_NUM_PU, DELIVERY_TYPE_PU,
			DELIVERY_DEPARTURE, DELIVERY_ARRIVAL, FILESOURCES);
	public static final ImmutableList<String> ADDRESS_COLUMNS = ImmutableList.of(ADDRESS, STATION_STREET,
			STATION_HOUSENO, STATION_ZIP, STATION_CITY, STATION_DISTRICT, STATION_STATE, STATION_COUNTRY,
			BackwardUtils.STATION_COUNTY, GeocodingNodeModel.LATITUDE_COLUMN, GeocodingNodeModel.LONGITUDE_COLUMN);

	public static final ImmutableList<String> IN_COLUMNS = ImmutableList.of(WEIGHT, CROSS_CONTAMINATION,
			KILL_CONTAMINATION, OBSERVED);
	public static final ImmutableList<String> STATION_OUT_COLUMNS = ImmutableList.of(SCORE, MAX_LOT_SCORE,
			NORMALIZED_SCORE, POSITIVE_SCORE, NEGATIVE_SCORE, BACKWARD, FORWARD);
	
	public static final ImmutableList<String> STATION_OUTPORTONLY_COLUMNS = ImmutableList.of(IS_SELECTED);
	
	public static final ImmutableList<String> DELIVERY_OUT_COLUMNS = ImmutableList.of(SCORE, LOT_SCORE,
			NORMALIZED_SCORE, POSITIVE_SCORE, NEGATIVE_SCORE, BACKWARD, FORWARD);
	public static final ImmutableMap<String, DataType> IN_OUT_COLUMN_TYPES = new ImmutableMap.Builder<String, DataType>()
			.put(WEIGHT, DoubleCell.TYPE).put(CROSS_CONTAMINATION, BooleanCell.TYPE)
			.put(KILL_CONTAMINATION, BooleanCell.TYPE).put(SCORE, DoubleCell.TYPE).put(MAX_LOT_SCORE, DoubleCell.TYPE)
			.put(LOT_SCORE, DoubleCell.TYPE).put(NORMALIZED_SCORE, DoubleCell.TYPE).put(POSITIVE_SCORE, DoubleCell.TYPE)
			.put(NEGATIVE_SCORE, DoubleCell.TYPE).put(OBSERVED, BooleanCell.TYPE).put(BACKWARD, BooleanCell.TYPE)
			.put(FORWARD, BooleanCell.TYPE).put(IS_SELECTED, BooleanCell.TYPE).build();
	public static final ImmutableMap<String, Class<?>> IN_OUT_COLUMN_CLASSES = new ImmutableMap.Builder<String, Class<?>>()
			.put(WEIGHT, Double.class).put(CROSS_CONTAMINATION, Boolean.class).put(KILL_CONTAMINATION, Boolean.class)
			.put(SCORE, Double.class).put(MAX_LOT_SCORE, Double.class).put(LOT_SCORE, Double.class)
			.put(NORMALIZED_SCORE, Double.class).put(POSITIVE_SCORE, Double.class).put(NEGATIVE_SCORE, Double.class)
			.put(OBSERVED, Boolean.class).put(BACKWARD, Boolean.class).put(FORWARD, Boolean.class).build();

	public static final ImmutableList<String> STATION_IN_OUT_COLUMNS = new ImmutableList.Builder<String>()
			.addAll(IN_COLUMNS).addAll(STATION_OUT_COLUMNS).build();
	public static final ImmutableList<String> DELIVERY_IN_OUT_COLUMNS = new ImmutableList.Builder<String>()
			.addAll(IN_COLUMNS).addAll(DELIVERY_OUT_COLUMNS).build();
	public static final ImmutableSet<String> OUT_COLUMNS = new ImmutableSet.Builder<String>()
			.addAll(STATION_OUT_COLUMNS).addAll(DELIVERY_OUT_COLUMNS).build();
}
