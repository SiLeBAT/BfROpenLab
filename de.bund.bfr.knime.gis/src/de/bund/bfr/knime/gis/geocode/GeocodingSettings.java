/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
import de.bund.bfr.knime.gis.BackwardUtils;

public class GeocodingSettings extends NodeSettings {

	public static enum Provider {
		MAPQUEST("MapQuest"), GISGRAPHY("Gisgraphy"), BKG("Bundesamt für Kartographie und Geodäsie"), PHOTON("Photon"), BING("Bing");

		private String name;

		private Provider(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static enum Multiple {
		DO_NOT_USE("Do not use"), USE_FIRST("Use first"), ASK_USER("Ask User");

		private String name;

		private Multiple(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static final String DEFAULT_ADDRESS_COLUMN = "Address";
	public static final String DEFAULT_STREET_COLUMN = "Street";
	public static final String DEFAULT_CITY_COLUMN = "City";
	public static final String DEFAULT_ZIP_COLUMN = "Zip";
	public static final String DEFAULT_COUNTRY_COLUMN = "Country";

	private static final String CFG_SERVICE_PROVIDER = "ServiceProvider";
	private static final String CFG_ADDRESS_COLUMN = "AddressColumn";
	private static final String CFG_STREET_COLUMN = "StreetColumn";
	private static final String CFG_CITY_COLUMN = "CityColumn";
	private static final String CFG_ZIP_COLUMN = "ZipColumn";
	private static final String CFG_COUNTRY_CODE_COLUMN = "CountryCodeColumn";
	private static final String CFG_GISGRAPHY_SERVER = "GisgraphyServer";
	private static final String CFG_PHOTON_SERVER = "PhotonServer";
	private static final String CFG_REQUEST_DELAY = "RequestDelay";
	private static final String CFG_MULTIPLE_RESULTS = "UseMultiple";
	private static final String CFG_USE_SINGLE_LINE_ADDRESS = "UseSingleLineAddress";

	private Provider serviceProvider;
	private String addressColumn;
	private String streetColumn;
	private String cityColumn;
	private String zipColumn;
	private String countryCodeColumn;
	private String gisgraphyServer;
	private String photonServer;
	private int requestDelay;
	private Multiple multipleResults;
	private boolean useSingleLineAddress;

	public GeocodingSettings() {
		serviceProvider = Provider.MAPQUEST;
		addressColumn = DEFAULT_ADDRESS_COLUMN;
		streetColumn = DEFAULT_STREET_COLUMN;
		cityColumn = DEFAULT_CITY_COLUMN;
		zipColumn = DEFAULT_ZIP_COLUMN;
		countryCodeColumn = DEFAULT_COUNTRY_COLUMN;
		gisgraphyServer = null;
		photonServer = null;
		requestDelay = 500;
		multipleResults = Multiple.DO_NOT_USE;
		useSingleLineAddress = true;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
	    boolean useSingleLineAddressSettingWasFound = false;
		try {
			try {
				serviceProvider = Provider.valueOf(settings.getString(CFG_SERVICE_PROVIDER));
			} catch (IllegalArgumentException e) {
				serviceProvider = BackwardUtils.toNewProviderFormat(settings.getString(CFG_SERVICE_PROVIDER));
			}
		} catch (InvalidSettingsException e) {
		}
		
		try {
          useSingleLineAddress = settings.getBoolean(CFG_USE_SINGLE_LINE_ADDRESS);
          useSingleLineAddressSettingWasFound = true;
        } catch (InvalidSettingsException e) {
        }
		
		try {
			addressColumn = settings.getString(CFG_ADDRESS_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			streetColumn = settings.getString(CFG_STREET_COLUMN);
			if(!useSingleLineAddressSettingWasFound) useSingleLineAddress = false;
		} catch (InvalidSettingsException e) {
		}

		try {
			cityColumn = settings.getString(CFG_CITY_COLUMN);
			if(!useSingleLineAddressSettingWasFound) useSingleLineAddress = false;
		} catch (InvalidSettingsException e) {
		}

		try {
			zipColumn = settings.getString(CFG_ZIP_COLUMN);
			if(!useSingleLineAddressSettingWasFound) useSingleLineAddress = false;
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
			photonServer = settings.getString(CFG_PHOTON_SERVER);
		} catch (InvalidSettingsException e) {
		}

		try {
			requestDelay = settings.getInt(CFG_REQUEST_DELAY);
		} catch (InvalidSettingsException e) {
		}

		try {
			try {
				multipleResults = Multiple.valueOf(settings.getString(CFG_MULTIPLE_RESULTS));
			} catch (IllegalArgumentException e) {
				multipleResults = BackwardUtils.toNewMultipleFormat(settings.getString(CFG_MULTIPLE_RESULTS));
			}
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_SERVICE_PROVIDER, serviceProvider.name());
		settings.addString(CFG_ADDRESS_COLUMN, addressColumn);
		settings.addString(CFG_STREET_COLUMN, streetColumn);
		settings.addString(CFG_CITY_COLUMN, cityColumn);
		settings.addString(CFG_ZIP_COLUMN, zipColumn);
		settings.addString(CFG_COUNTRY_CODE_COLUMN, countryCodeColumn);
		settings.addString(CFG_GISGRAPHY_SERVER, gisgraphyServer);
		settings.addString(CFG_PHOTON_SERVER, photonServer);
		settings.addInt(CFG_REQUEST_DELAY, requestDelay);
		settings.addString(CFG_MULTIPLE_RESULTS, multipleResults.name());
		settings.addBoolean(CFG_USE_SINGLE_LINE_ADDRESS, useSingleLineAddress);
	}

	public Provider getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(Provider serviceProvider) {
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

	public String getZipColumn() {
		return zipColumn;
	}

	public void setZipColumn(String zipColumn) {
		this.zipColumn = zipColumn;
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
	
	public String getPhotonServer() {
		return photonServer;
	}

	public void setPhotonServer(String photonServer) {
		this.photonServer = photonServer;
	}

	public int getRequestDelay() {
		return requestDelay;
	}

	public void setRequestDelay(int requestDelay) {
		this.requestDelay = requestDelay;
	}

	public Multiple getMultipleResults() {
		return multipleResults;
	}

	public void setMultipleResults(Multiple multipleResults) {
		this.multipleResults = multipleResults;
	}
	
	public boolean getUseSingleLineAddress() {
      return useSingleLineAddress;
    }
	
	public void setUseSingleLineAddress(boolean useSingleLineAddress) {
      this.useSingleLineAddress = useSingleLineAddress;
    }
}
