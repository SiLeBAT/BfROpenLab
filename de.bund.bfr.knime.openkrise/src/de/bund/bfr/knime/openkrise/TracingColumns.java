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
package de.bund.bfr.knime.openkrise;

public interface TracingColumns {

	public static final String ID = "ID";
	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String NEXT = "Next";
	public static final String FILESOURCES = "ImportSources";

	public static final String OLD_WEIGHT = "CaseWeight";
	public static final String WEIGHT = "Weight";
	public static final String CROSS_CONTAMINATION = "CrossContamination";
	public static final String SCORE = "Score";

	public static final String OLD_OBSERVED = "Filter";
	public static final String OBSERVED = "Observed";
	public static final String BACKWARD = "Backward";
	public static final String FORWARD = "Forward";

	public static final String CLUSTER_ID = "ClusterID";
	public static final String ADDRESS = "Address";
	public static final String DELIVERY_DATE = "Date Delivery";

	public static final String STATION_NODE = "node"; // deprecated
	public static final String STATION_NAME = "Name";
	public static final String STATION_STREET = "Street";
	public static final String STATION_HOUSENO = "HouseNumber";
	public static final String STATION_ZIP = "ZIP";
	public static final String STATION_CITY = "City";
	public static final String STATION_DISTRICT = "District";
	public static final String STATION_STATE = "State";
	public static final String STATION_COUNTRY = "Country";
	public static final String STATION_VAT = "VAT"; // deprecated
	public static final String STATION_TOB = "type of business";
	public static final String STATION_NUMCASES = "Number Cases"; // deprecated
	public static final String STATION_DATESTART = "Date start"; // deprecated
	public static final String STATION_DATEPEAK = "Date peak"; // deprecated
	public static final String STATION_DATEEND = "Date end"; // deprecated
	public static final String STATION_SERIAL = "Serial";
	public static final String STATION_SIMPLESUPPLIER = "SimpleSupplier";
	public static final String STATION_DEADSTART = "DeadStart";
	public static final String STATION_DEADEND = "DeadEnd";
	public static final String STATION_COUNTY = "County"; // deprecated

	public static final String DELIVERY_ITEMNUM = "Item Number";
	public static final String DELIVERY_ITEMNAME = "Name";
	public static final String DELIVERY_LOTNUM = "Lot Number"; // Lot number
	public static final String DELIVERY_AMOUNT = "Amount [kg]";
	public static final String DELIVERY_NUM_PU = "Amount";
	public static final String DELIVERY_TYPE_PU = "Amount Unit";
	public static final String DELIVERY_SERIAL = "Serial";
	public static final String DELIVERY_CHARGENUM = "Charge Number"; // deprecated

	// now: extra fields:
	public static final String DELIVERY_ORIGIN = "OriginCountry"; // deprecated
	public static final String DELIVERY_ENDCHAIN = "EndChain"; // deprecated
	public static final String DELIVERY_ENDCHAINWHY = "ExplanationEndChain"; // deprecated
	public static final String DELIVERY_REMARKS = "Contact_Questions_Remarks"; // deprecated
	public static final String DELIVERY_FURTHERTB = "FurtherTB"; // deprecated
	public static final String DELIVERY_MICROSAMPLE = "MicroSample"; // deprecated
	public static final String DELIVERY_PROCESSING = "Processing"; // deprecated
	public static final String DELIVERY_USAGE = "IntendedUse"; // deprecated
	public static final String DELIVERY_DATEEXP = "Date Expiration"; // deprecated
	public static final String DELIVERY_DATEMANU = "Date Manufactoring"; // deprecated
}
