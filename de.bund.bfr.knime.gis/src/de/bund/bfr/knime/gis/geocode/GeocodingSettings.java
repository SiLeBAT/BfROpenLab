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
package de.bund.bfr.knime.gis.geocode;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.gis.views.canvas.backward.BackwardUtils;

public class GeocodingSettings extends NodeSettings {

	public static enum Provider {
		MAPQUEST("MapQuest"), GISGRAPHY("Gisgraphy"), BKG("Bundesamt für Kartographie und Geodäsie");

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

	private static final String CFG_SERVICE_PROVIDER = "ServiceProvider";
	private static final String CFG_ADDRESS_COLUMN = "AddressColumn";
	private static final String CFG_COUNTRY_CODE_COLUMN = "CountryCodeColumn";
	private static final String CFG_GISGRAPHY_SERVER = "GisgraphyServer";
	private static final String CFG_REQUEST_DELAY = "RequestDelay";
	private static final String CFG_MULTIPLE_RESULTS = "UseMultiple";

	private Provider serviceProvider;
	private String addressColumn;
	private String countryCodeColumn;
	private String gisgraphyServer;
	private int requestDelay;
	private Multiple multipleResults;

	public GeocodingSettings() {
		serviceProvider = Provider.MAPQUEST;
		addressColumn = null;
		countryCodeColumn = null;
		gisgraphyServer = null;
		requestDelay = 500;
		multipleResults = Multiple.DO_NOT_USE;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			try {
				serviceProvider = Provider.valueOf(settings.getString(CFG_SERVICE_PROVIDER));
			} catch (IllegalArgumentException e) {
				serviceProvider = BackwardUtils.toNewProviderFormat(settings.getString(CFG_SERVICE_PROVIDER));
			}
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
		settings.addString(CFG_COUNTRY_CODE_COLUMN, countryCodeColumn);
		settings.addString(CFG_GISGRAPHY_SERVER, gisgraphyServer);
		settings.addInt(CFG_REQUEST_DELAY, requestDelay);
		settings.addString(CFG_MULTIPLE_RESULTS, multipleResults.name());
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

	public Multiple getMultipleResults() {
		return multipleResults;
	}

	public void setMultipleResults(Multiple multipleResults) {
		this.multipleResults = multipleResults;
	}
}
