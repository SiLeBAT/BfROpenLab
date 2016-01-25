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
package de.bund.bfr.knime.gis.geocode;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.collect.ImmutableList;

import de.bund.bfr.knime.NodeSettings;

public class GeocodingSettings extends NodeSettings {

	public static final String PROVIDER_MAPQUEST = "MapQuest";
	public static final String PROVIDER_GISGRAPHY = "Gisgraphy";
	public static final String PROVIDER_BKG = "Bundesamt für Kartographie und Geodäsie";
	public static final ImmutableList<String> PROVIDER_CHOICES = ImmutableList.of(PROVIDER_MAPQUEST, PROVIDER_GISGRAPHY,
			PROVIDER_BKG);

	public static final String MULTIPLE_DO_NOT_USE = "Do not use";
	public static final String MULTIPLE_USE_FIRST = "Use first";
	public static final String MULTIPLE_ASK_USER = "Ask User";
	public static final ImmutableList<String> MULTIPLE_CHOICES = ImmutableList.of(MULTIPLE_DO_NOT_USE,
			MULTIPLE_USE_FIRST, MULTIPLE_ASK_USER);

	private static final String CFG_SERVICE_PROVIDER = "ServiceProvider";
	private static final String CFG_ADDRESS_COLUMN = "AddressColumn";
	private static final String CFG_COUNTRY_CODE_COLUMN = "CountryCodeColumn";
	private static final String CFG_GISGRAPHY_SERVER = "GisgraphyServer";
	private static final String CFG_REQUEST_DELAY = "RequestDelay";
	private static final String CFG_MULTIPLE_RESULTS = "UseMultiple";

	private String serviceProvider;
	private String addressColumn;
	private String countryCodeColumn;
	private String gisgraphyServer;
	private int requestDelay;
	private String multipleResults;

	public GeocodingSettings() {
		serviceProvider = PROVIDER_MAPQUEST;
		addressColumn = null;
		countryCodeColumn = null;
		gisgraphyServer = null;
		requestDelay = 500;
		multipleResults = MULTIPLE_DO_NOT_USE;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			serviceProvider = settings.getString(CFG_SERVICE_PROVIDER);
		} catch (InvalidSettingsException e) {
		}

		try {
			addressColumn = settings.getString(CFG_ADDRESS_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			countryCodeColumn = settings.getString(CFG_COUNTRY_CODE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisgraphyServer = settings.getString(CFG_GISGRAPHY_SERVER);
		} catch (InvalidSettingsException e) {
		}

		try {
			requestDelay = settings.getInt(CFG_REQUEST_DELAY);
		} catch (InvalidSettingsException e) {
		}

		try {
			multipleResults = settings.getString(CFG_MULTIPLE_RESULTS);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_SERVICE_PROVIDER, serviceProvider);
		settings.addString(CFG_ADDRESS_COLUMN, addressColumn);
		settings.addString(CFG_COUNTRY_CODE_COLUMN, countryCodeColumn);
		settings.addString(CFG_GISGRAPHY_SERVER, gisgraphyServer);
		settings.addInt(CFG_REQUEST_DELAY, requestDelay);
		settings.addString(CFG_MULTIPLE_RESULTS, multipleResults);
	}

	public String getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(String serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public String getAddressColumn() {
		return addressColumn;
	}

	public void setAddressColumn(String addressColumn) {
		this.addressColumn = addressColumn;
	}

	public String getCountryCodeColumn() {
		return countryCodeColumn;
	}

	public void setCountryCodeColumn(String countryCodeColumn) {
		this.countryCodeColumn = countryCodeColumn;
	}

	public String getGisgraphyServer() {
		return gisgraphyServer;
	}

	public void setGisgraphyServer(String gisgraphyServer) {
		this.gisgraphyServer = gisgraphyServer;
	}

	public int getRequestDelay() {
		return requestDelay;
	}

	public void setRequestDelay(int requestDelay) {
		this.requestDelay = requestDelay;
	}

	public String getMultipleResults() {
		return multipleResults;
	}

	public void setMultipleResults(String multipleResults) {
		this.multipleResults = multipleResults;
	}
}
