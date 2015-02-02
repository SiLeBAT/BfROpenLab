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

import de.bund.bfr.knime.NodeSettings;

public class GeocodingSettings extends NodeSettings {

	public static final String PROVIDER_MAPQUEST = "MapQuest";
	public static final String PROVIDER_GISGRAPHY_PUBLIC = "Gisgraphy Public Server";
	public static final String PROVIDER_GISGRAPHY = "Gisgraphy";
	public static final String PROVIDER_BKG = "Bundesamt für Kartographie und Geodäsie";
	public static final String[] PROVIDER_CHOICES = { PROVIDER_MAPQUEST,
			PROVIDER_GISGRAPHY_PUBLIC, PROVIDER_GISGRAPHY, PROVIDER_BKG };

	public static final String MULTIPLE_DO_NOT_USE = "Do not use";
	public static final String MULTIPLE_USE_FIRST = "Use first";
	public static final String MULTIPLE_ASK_USER = "Ask User";
	public static final String[] MULTIPLE_CHOICES = { MULTIPLE_DO_NOT_USE,
			MULTIPLE_USE_FIRST, MULTIPLE_ASK_USER };

	private static final String CFG_SERVICE_PROVIDER = "ServiceProvider";
	private static final String CFG_ADDRESS_COLUMN = "AddressColumn";
	private static final String CFG_STREET_COLUMN = "StreetColumn";
	private static final String CFG_CITY_COLUMN = "CityColumn";
	private static final String CFG_COUNTY_COLUMN = "CountyColumn";
	private static final String CFG_STATE_COLUMN = "StateColumn";
	private static final String CFG_COUNTRY_COLUMN = "CountryColumn";
	private static final String CFG_COUNTRY_CODE_COLUMN = "CountryCodeColumn";
	private static final String CFG_POSTAL_CODE_COLUMN = "PostalCodeColumn";
	private static final String CFG_MAP_QUEST_KEY = "MapQuestKey";
	private static final String CFG_GISGRAPHY_SERVER = "GisgraphyServer";
	private static final String CFG_BKG_UUID = "BKG UUID";
	private static final String CFG_REQUEST_DELAY = "RequestDelay";
	private static final String CFG_MULTIPLE_RESULTS = "UseMultiple";

	private String serviceProvider;
	private String addressColumn;
	private String streetColumn;
	private String cityColumn;
	private String countyColumn;
	private String stateColumn;
	private String countryColumn;
	private String countryCodeColumn;
	private String postalCodeColumn;
	private String mapQuestKey;
	private String gisgraphyServer;
	private String bkgUuid;
	private int requestDelay;
	private String multipleResults;

	public GeocodingSettings() {
		serviceProvider = PROVIDER_MAPQUEST;
		addressColumn = null;
		streetColumn = null;
		cityColumn = null;
		countyColumn = null;
		stateColumn = null;
		countryColumn = null;
		countryCodeColumn = null;
		postalCodeColumn = null;
		mapQuestKey = null;
		gisgraphyServer = null;
		bkgUuid = null;
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
			streetColumn = settings.getString(CFG_STREET_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			cityColumn = settings.getString(CFG_CITY_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			countyColumn = settings.getString(CFG_COUNTY_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			stateColumn = settings.getString(CFG_STATE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			countryColumn = settings.getString(CFG_COUNTRY_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			countryCodeColumn = settings.getString(CFG_COUNTRY_CODE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			postalCodeColumn = settings.getString(CFG_POSTAL_CODE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			mapQuestKey = settings.getString(CFG_MAP_QUEST_KEY);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisgraphyServer = settings.getString(CFG_GISGRAPHY_SERVER);
		} catch (InvalidSettingsException e) {
		}

		try {
			bkgUuid = settings.getString(CFG_BKG_UUID);
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
		settings.addString(CFG_STREET_COLUMN, streetColumn);
		settings.addString(CFG_CITY_COLUMN, cityColumn);
		settings.addString(CFG_COUNTY_COLUMN, countyColumn);
		settings.addString(CFG_STATE_COLUMN, stateColumn);
		settings.addString(CFG_COUNTRY_COLUMN, countryColumn);
		settings.addString(CFG_COUNTRY_CODE_COLUMN, countryCodeColumn);
		settings.addString(CFG_POSTAL_CODE_COLUMN, postalCodeColumn);
		settings.addString(CFG_MAP_QUEST_KEY, mapQuestKey);
		settings.addString(CFG_GISGRAPHY_SERVER, gisgraphyServer);
		settings.addString(CFG_BKG_UUID, bkgUuid);
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

	public String getStreetColumn() {
		return streetColumn;
	}

	public void setStreetColumn(String streetColumn) {
		this.streetColumn = streetColumn;
	}

	public String getCityColumn() {
		return cityColumn;
	}

	public void setCityColumn(String cityColumn) {
		this.cityColumn = cityColumn;
	}

	public String getCountyColumn() {
		return countyColumn;
	}

	public void setCountyColumn(String countyColumn) {
		this.countyColumn = countyColumn;
	}

	public String getStateColumn() {
		return stateColumn;
	}

	public void setStateColumn(String stateColumn) {
		this.stateColumn = stateColumn;
	}

	public String getCountryColumn() {
		return countryColumn;
	}

	public void setCountryColumn(String countryColumn) {
		this.countryColumn = countryColumn;
	}

	public String getCountryCodeColumn() {
		return countryCodeColumn;
	}

	public void setCountryCodeColumn(String countryCodeColumn) {
		this.countryCodeColumn = countryCodeColumn;
	}

	public String getPostalCodeColumn() {
		return postalCodeColumn;
	}

	public void setPostalCodeColumn(String postalCodeColumn) {
		this.postalCodeColumn = postalCodeColumn;
	}

	public String getMapQuestKey() {
		return mapQuestKey;
	}

	public void setMapQuestKey(String mapQuestKey) {
		this.mapQuestKey = mapQuestKey;
	}

	public String getGisgraphyServer() {
		return gisgraphyServer;
	}

	public void setGisgraphyServer(String gisgraphyServer) {
		this.gisgraphyServer = gisgraphyServer;
	}

	public String getBkgUuid() {
		return bkgUuid;
	}

	public void setBkgUuid(String bkgUuid) {
		this.bkgUuid = bkgUuid;
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
