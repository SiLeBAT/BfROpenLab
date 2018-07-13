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
package de.bund.bfr.knime.gis.geocode;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.Activator;
import de.bund.bfr.knime.gis.GisUtils;
import net.minidev.json.JSONArray;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * This is the model implementation of Geocoding.
 * 
 * 
 * @author Christian Thoens
 */
public class GeocodingNodeModel extends NoInternalsNodeModel {

	public static final String URL_COLUMN = "GeocodingURL";
	public static final String STREET_COLUMN = "GeocodingStreet";
	public static final String CITY_COLUMN = "GeocodingCity";
	public static final String DISTRICT_COLUMN = "GeocodingDistrict";
	public static final String STATE_COLUMN = "GeocodingState";
	public static final String COUNTRY_COLUMN = "GeocodingCountry";
	public static final String POSTAL_CODE_COLUMN = "GeocodingPostalCode";
	public static final String LATITUDE_COLUMN = "Latitude";
	public static final String LONGITUDE_COLUMN = "Longitude";

	private ImmutableSet<String> GEOCODING_COLUMNS = ImmutableSet.of(URL_COLUMN, STREET_COLUMN, CITY_COLUMN,
			DISTRICT_COLUMN, STATE_COLUMN, COUNTRY_COLUMN, POSTAL_CODE_COLUMN, LATITUDE_COLUMN, LONGITUDE_COLUMN);

	private static final String DE = "DE";
	private static final String NO_KEY = "XXXXXX";
	
	private static final String PATTERN_CODE_SERVER = "<SERVER>";
	private static final String PATTERN_CODE_KEY = "<KEY>";
	private static final String PATTERN_CODE_ADDRESS = "<ADDRESS>";
	private static final String PATTERN_CODE_STREET = "<STREET>";
	private static final String PATTERN_CODE_CITY = "<CITY>";
	private static final String PATTERN_CODE_ZIP = "<ZIP>";
	private static final String PATTERN_CODE_COUNTRY = "<COUNTRY>";
	
	private static final String URL_PATTERN_MAPQUEST = "https://open.mapquestapi.com/geocoding/v1/address?key=" + PATTERN_CODE_KEY + "&location=" + PATTERN_CODE_ADDRESS;
	//private static final String URL_PATTERN_MAPQUEST_WITH_COUNTRY = "https://open.mapquestapi.com/geocoding/v1/address?key=" + PATTERN_CODE_KEY + "&location=" + PATTERN_CODE_ADDRESS + "&country=" + PATTERN_CODE_COUNTRY;
	private static final String URL_PATTERN_MAPQUEST5BOX = "https://open.mapquestapi.com/geocoding/v1/address?key=" + PATTERN_CODE_KEY + "&street=" + PATTERN_CODE_STREET + "&city=" + PATTERN_CODE_CITY + "&postalCode=" + PATTERN_CODE_ZIP + "&country=" + PATTERN_CODE_COUNTRY;
	private static final String URL_PATTERN_BKG = "https://sg.geodatenzentrum.de/gdz_geokodierung__" + PATTERN_CODE_KEY + "/geosearch?query=" + PATTERN_CODE_ADDRESS;
	private static final String URL_PATTERN_GISGRAPHY = PATTERN_CODE_SERVER + "?address=" + PATTERN_CODE_ADDRESS + "&country=" + PATTERN_CODE_COUNTRY + "&format=json";
	//private static final String URL_PATTERN_PHOTON = PATTERN_CODE_SERVER + "api?q=" + PATTERN_CODE_ADDRESS + "&osm_tag=highway:residential&limit=2";
	private static final String URL_PATTERN_PHOTON = PATTERN_CODE_SERVER + "api?q=" + PATTERN_CODE_ADDRESS; // + "&limit=2";
	private static final String URL_PATTERN_BING = "http://dev.virtualearth.net/REST/v1/Locations?query=" + PATTERN_CODE_ADDRESS + "&key=" + PATTERN_CODE_KEY;
	
	//private static final String OSM_KEY_HIGHWAY = "highway";
	
	private GeocodingSettings set;

	/**
	 * Constructor for the node model.
	 */
	public GeocodingNodeModel() {
		super(1, 1);
		set = new GeocodingSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		BufferedDataTable table = inData[0];
		DataTableSpec spec = table.getSpec();
		DataTableSpec outSpec = configure(new DataTableSpec[] { spec })[0];
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		int index = 0;

		for (DataRow row : table) {
			DataCell[] cells = new DataCell[outSpec.getNumColumns()];

			for (String column : spec.getColumnNames()) {
				cells[outSpec.findColumnIndex(column)] = row.getCell(spec.findColumnIndex(column));
			}

			int addressIndex = spec.findColumnIndex(set.getAddressColumn());
			int streetIndex = spec.findColumnIndex(set.getStreetColumn());
			int cityIndex = spec.findColumnIndex(set.getCityColumn());
			int zipIndex = spec.findColumnIndex(set.getZipColumn());
			int countryCodeIndex = spec.findColumnIndex(set.getCountryCodeColumn());
			String address = null;
			String street = null;
			String city = null;
			String zip = null;
			String countryCode = null;

			if (addressIndex != -1) {
			  // either use addresstext
			  address = IO.getCleanString(row.getCell(addressIndex));
			  
			} else {

			  // or use detailed address information
			  if (streetIndex != -1) {
			    street = IO.getCleanString(row.getCell(streetIndex));
			  }

			  if (cityIndex != -1) {
			    city = IO.getCleanString(row.getCell(cityIndex));
			  }

			  if (zipIndex != -1) {
			    zip = IO.getCleanString(row.getCell(zipIndex));
			  }
			}

			if (countryCodeIndex != -1) {
				countryCode = IO.getCleanString(row.getCell(countryCodeIndex));
			}

			GeocodingResult result;

			switch (set.getServiceProvider()) {
			case MAPQUEST:
				result = performMapQuestGeocoding(address, street, city, zip, countryCode);
				break;
			case GISGRAPHY:
				result = performGisgraphyGeocoding(address, countryCode);
				break;
			case BKG:
				result = performBkgGeocoding(address);
				break;
			case PHOTON:
				result = performPhotonGeocoding(address);
				break;
			case BING:
			    result = performBingGeocoding(address);
			    break;
			default:
				throw new RuntimeException("Unknown service provider");
			}

			if (result.getLatitude() == null || result.getLongitude() == null) {
				setWarningMessage("Geocoding failed for row " + row.getKey().getString());
			}

			cells[outSpec.findColumnIndex(URL_COLUMN)] = IO.createCell(result.getUrl());
			cells[outSpec.findColumnIndex(STREET_COLUMN)] = IO.createCell(result.getStreet());
			cells[outSpec.findColumnIndex(CITY_COLUMN)] = IO.createCell(result.getCity());
			cells[outSpec.findColumnIndex(DISTRICT_COLUMN)] = IO.createCell(result.getDistrict());
			cells[outSpec.findColumnIndex(STATE_COLUMN)] = IO.createCell(result.getState());
			cells[outSpec.findColumnIndex(COUNTRY_COLUMN)] = IO.createCell(result.getCountry());
			cells[outSpec.findColumnIndex(POSTAL_CODE_COLUMN)] = IO.createCell(result.getPostalCode());
			cells[outSpec.findColumnIndex(LATITUDE_COLUMN)] = IO.createCell(result.getLatitude());
			cells[outSpec.findColumnIndex(LONGITUDE_COLUMN)] = IO.createCell(result.getLongitude());
			container.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.setProgress((double) (index++) / (double) table.size());
			exec.checkCanceled();

			try {
				Thread.sleep(set.getRequestDelay());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec c : inSpecs[0]) {
			if (GEOCODING_COLUMNS.contains(c.getName())) {
				throw new InvalidSettingsException("Column name \"" + c.getName() + "\" not allowed in input table");
			}

			columns.add(c);
		}

		columns.add(new DataColumnSpecCreator(URL_COLUMN, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(STREET_COLUMN, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(CITY_COLUMN, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(DISTRICT_COLUMN, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(STATE_COLUMN, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(COUNTRY_COLUMN, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(POSTAL_CODE_COLUMN, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(LATITUDE_COLUMN, DoubleCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(LONGITUDE_COLUMN, DoubleCell.TYPE).createSpec());

		return new DataTableSpec[] { new DataTableSpec(columns.toArray(new DataColumnSpec[0])) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		set.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	private GeocodingResult performMapQuestGeocoding(String address, String street, String city, String zip, String country)
			throws IOException, InvalidSettingsException, CanceledExecutionException {
		if (address == null && street == null && city == null && zip == null && country == null) {
			return new GeocodingResult();
		}

		String mapQuestKey = Activator.getDefault().getPreferenceStore()
				.getString(GeocodingPreferencePage.MAPQUEST_KEY);

		if (Strings.isNullOrEmpty(mapQuestKey)) {
			throw new InvalidSettingsException(
					"MapQuest key in preferences missing. Please enter it under KNIME->Geocoding.");
		}

		String url, urlWithoutKey = null;
		
		if (address != null) { 
		  // use single address field
		  url = createMapQuestUrl(address, mapQuestKey);
          urlWithoutKey = createMapQuestUrl(address, NO_KEY);
          
		} else {
		  
			url = createMapQuest5BoxUrl(street, city, zip, country, mapQuestKey);
			urlWithoutKey = createMapQuest5BoxUrl(street, city, zip, country, NO_KEY);
		}

		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(new URL(url).openConnection().getInputStream(), StandardCharsets.UTF_8.name()))) {
			String json = buffer.lines().collect(Collectors.joining("\n"));

			if (Strings.isNullOrEmpty(json)) {
				return new GeocodingResult(urlWithoutKey);
			}

			JSONArray jsonResults = JsonPath.parse(json).read("$.results[*].locations[*]");
			List<GeocodingResult> results = new ArrayList<>();

			for (Object jsonResult : jsonResults) {
				DocumentContext r = JsonPath.parse(jsonResult);

				results.add(new GeocodingResult(urlWithoutKey, read(r, "$.street"), read(r, "$.adminArea5"),
						read(r, "$.adminArea4"), read(r, "$.adminArea3"), read(r, "$.adminArea1"),
						read(r, "$.postalCode"), readDouble(r, "$.latLng.lat"), readDouble(r, "$.latLng.lng")));
			}

			return getIndex(address, results, new GeocodingResult(urlWithoutKey));
		}
	}

	private GeocodingResult performGisgraphyGeocoding(String address, String countryCode)
			throws IOException, CanceledExecutionException {
		if (address == null || countryCode == null) {
			return new GeocodingResult();
		}

		String url = createGisgraphyUrl(set.getGisgraphyServer(), address, countryCode);

		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(new URL(url).openConnection().getInputStream(), StandardCharsets.UTF_8.name()))) {
			String json = buffer.lines().collect(Collectors.joining("\n"));

			if (Strings.isNullOrEmpty(json)) {
				return new GeocodingResult(url);
			}

			JSONArray jsonResults = JsonPath.parse(json).read("$.result[*]");
			List<GeocodingResult> results = new ArrayList<>();

			for (Object jsonResult : jsonResults) {
				DocumentContext r = JsonPath.parse(jsonResult);

				results.add(new GeocodingResult(url, read(r, "$.streetName"), read(r, "$.city"), read(r, "$.district"),
						read(r, "$.state"), read(r, "$.countryCode"), read(r, "$.zipCode"), readDouble(r, "$.lat"),
						readDouble(r, "$.lng")));
			}

			return getIndex(address + ", " + countryCode, results, new GeocodingResult(url));
		} catch (IOException e) {
			JOptionPane options = new JOptionPane(e.getMessage() + "\nDo you want to continue?",
					JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION);
			JDialog dialog = options.createDialog("Error");

			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);

			if (options.getValue() instanceof Integer && (Integer) options.getValue() == JOptionPane.YES_OPTION) {
				return new GeocodingResult(url);
			} else {
				throw e;
			}
		}
	}

	private GeocodingResult performBkgGeocoding(String address)
			throws IOException, InvalidSettingsException, CanceledExecutionException {
		if (address == null) {
			return new GeocodingResult();
		}

		String uuid = Activator.getDefault().getPreferenceStore().getString(GeocodingPreferencePage.BKG_UUID);

		if (Strings.isNullOrEmpty(uuid)) {
			throw new InvalidSettingsException("UUID in preferences missing. Please enter it under KNIME->Geocoding.");
		}

		String url = createBkgUrl(address, uuid);
		String urlWithoutUuid = createBkgUrl(address, NO_KEY);

		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(new URL(url).openConnection().getInputStream(), StandardCharsets.UTF_8.name()))) {
			String json = buffer.lines().collect(Collectors.joining("\n"));

			if (Strings.isNullOrEmpty(json)) {
				return new GeocodingResult(urlWithoutUuid);
			}

			JSONArray jsonResults = JsonPath.parse(json).read("$.features[*]");
			List<GeocodingResult> results = new ArrayList<>();

			for (Object jsonResult : jsonResults) {
				DocumentContext r = JsonPath.parse(jsonResult);

				results.add(new GeocodingResult(urlWithoutUuid, read(r, "$.properties.strasse"),
						read(r, "$.properties.ort"), read(r, "$.properties.kreis"), read(r, "$.properties.bundesland"),
						DE, read(r, "$.properties.plz"), readDouble(r, "$.geometry.coordinates[1]"),
						readDouble(r, "$.geometry.coordinates[0]")));
			}

			return getIndex(address, results, new GeocodingResult(urlWithoutUuid));
		}
	}

	private GeocodingResult performPhotonGeocoding(String address) 
		throws IOException, InvalidSettingsException, CanceledExecutionException {
		
		if (address == null) {
			return new GeocodingResult();
		}

		String url = createPhotonUrl(set.getPhotonServer(), address);

		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(new URL(url).openConnection().getInputStream(), StandardCharsets.UTF_8.name()))) {
			String json = buffer.lines().collect(Collectors.joining("\n"));

			if (Strings.isNullOrEmpty(json)) {
				return new GeocodingResult(url);
			}

			JSONArray jsonResults = JsonPath.parse(json).read("$.features[*]");
			List<GeocodingResult> results = new ArrayList<>();

			for (Object jsonResult : jsonResults) {
				DocumentContext r = JsonPath.parse(jsonResult);
				//String osm_key = read(r, "$.properties.osm_key");
				//results.add(new GeocodingResult(url, read(r, "$.properties.name"),   // the osm_tag highway seems to cause that the name is set as the street
				//results.add(new GeocodingResult(url, read(r, "$.properties.street"),   // without osm_tag=highway street is set as street
				results.add(new GeocodingResult(url, read(r, "$.properties.street"),   // if key==highway the street is in the name attribute otherwise street
				        read(r, "$.properties.housenumber"),
						read(r, "$.properties.city"), null, read(r, "$.properties.state"),
						read(r, "$.properties.country"), read(r, "$.properties.postcode"), readDouble(r, "$.geometry.coordinates[1]"),
						readDouble(r, "$.geometry.coordinates[0]")));
			}

			return getIndex(address, results, new GeocodingResult(url));
		} catch (IOException e) {
			JOptionPane options = new JOptionPane(e.getMessage() + "\nDo you want to continue?",
					JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION);
			JDialog dialog = options.createDialog("Error");

			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);

			if (options.getValue() instanceof Integer && (Integer) options.getValue() == JOptionPane.YES_OPTION) {
				return new GeocodingResult(url);
			} else {
				throw e;
			}
		}
	}
	
	private GeocodingResult performBingGeocoding(String address)
        throws IOException, InvalidSettingsException, CanceledExecutionException {
    if (address == null) return new GeocodingResult();
    
    String bingMapsKey = Activator.getDefault().getPreferenceStore()
            .getString(GeocodingPreferencePage.BING_KEY);

    if (Strings.isNullOrEmpty(bingMapsKey)) {
        throw new InvalidSettingsException(
                "Bing Maps Key in preferences is missing. Please enter it under KNIME->Geocoding.");
    }

    String url = createBingUrl(address, bingMapsKey);
    String urlWithoutKey = createBingUrl(address, NO_KEY);
    

    try (BufferedReader buffer = new BufferedReader(
            new InputStreamReader(new URL(url).openConnection().getInputStream(), StandardCharsets.UTF_8.name()))) {
      
        String json = buffer.lines().collect(Collectors.joining("\n"));

        if (Strings.isNullOrEmpty(json)) {
            return new GeocodingResult(urlWithoutKey);
        }

        JSONArray jsonResults = JsonPath.parse(json).read("$.resourceSets[*]"); //.resources[*]");
        List<GeocodingResult> results = new ArrayList<>();

        for (Object jsonResult : jsonResults) {
          JSONArray jsonSubResults = JsonPath.parse(jsonResult).read("$.resources[*]"); //.resources[*]");
          for (Object jsonSubResult : jsonSubResults) {
            DocumentContext r = JsonPath.parse(jsonSubResult);

            results.add(new GeocodingResult(urlWithoutKey, read(r, "$.address.addressLine"), read(r, "$.address.locality"),
                    read(r, "$.address.adminDistrict2"), read(r, "$.address.adminDistrict"), read(r, "$.address.countryRegion"),
                    read(r, "$.address.postalCode"), readDouble(r, "$.point.coordinates[0]"), readDouble(r, "$.point.coordinates[1]")));
          }
        }

        return getIndex(address, results, new GeocodingResult(urlWithoutKey));
    }
}
	
	private GeocodingResult getIndex(String address, List<GeocodingResult> choices, GeocodingResult defaultValue)
			throws CanceledExecutionException {
		if (choices.size() == 0) {
			return defaultValue;
		} else if (choices.size() == 1) {
			return choices.get(0);
		} else if (set.getMultipleResults() == GeocodingSettings.Multiple.DO_NOT_USE) {
			return defaultValue;
		} else if (set.getMultipleResults() == GeocodingSettings.Multiple.USE_FIRST) {
			return choices.get(0);
		} else if (set.getMultipleResults() == GeocodingSettings.Multiple.ASK_USER) {
			ChooseDialog dialog = new ChooseDialog(address, choices, defaultValue);

			dialog.setVisible(true);

			if (dialog.isCanceled()) {
				throw new CanceledExecutionException();
			}

			return dialog.getResult();
		}

		return defaultValue;
	}

	private static String createMapQuestUrl(String address, String key) throws UnsupportedEncodingException {
		return URL_PATTERN_MAPQUEST.replace(
				PATTERN_CODE_KEY,URLEncoder.encode(key, StandardCharsets.UTF_8.name())).replace(
			    PATTERN_CODE_ADDRESS, URLEncoder.encode(address, StandardCharsets.UTF_8.name()));
	}
	
//	private static String createMapQuestUrl(String address, String country, String key) throws UnsupportedEncodingException {
//      return URL_PATTERN_MAPQUEST_WITH_COUNTRY.replace(
//              PATTERN_CODE_KEY,URLEncoder.encode(key, StandardCharsets.UTF_8.name())).replace(
//              PATTERN_CODE_ADDRESS, URLEncoder.encode(address, StandardCharsets.UTF_8.name())).replace(
//              PATTERN_CODE_COUNTRY, URLEncoder.encode(country, StandardCharsets.UTF_8.name()));
//    }
	
	private static String createBingUrl(String address, String key) throws UnsupportedEncodingException {
      return URL_PATTERN_BING.replace(
              PATTERN_CODE_KEY,URLEncoder.encode(key, StandardCharsets.UTF_8.name())).replace(
              PATTERN_CODE_ADDRESS, URLEncoder.encode(address, StandardCharsets.UTF_8.name()));
    }
	
	private static String createMapQuest5BoxUrl(String street, String city, String zip, String country, String key) throws UnsupportedEncodingException {
		return URL_PATTERN_MAPQUEST5BOX.replace(
				PATTERN_CODE_KEY,URLEncoder.encode(key, StandardCharsets.UTF_8.name())).replace(
				PATTERN_CODE_STREET, street == null ? "" : URLEncoder.encode(street, StandardCharsets.UTF_8.name())).replace(
				PATTERN_CODE_CITY, city == null ? "" : URLEncoder.encode(city, StandardCharsets.UTF_8.name())).replace(
				PATTERN_CODE_ZIP, zip == null ? "" : URLEncoder.encode(zip, StandardCharsets.UTF_8.name())).replace(
				PATTERN_CODE_COUNTRY, country == null ? "" : URLEncoder.encode(country, StandardCharsets.UTF_8.name()));
	}

	private static String createGisgraphyUrl(String server, String address, String countryCode)
			throws UnsupportedEncodingException {
		return URL_PATTERN_GISGRAPHY.replace(
				PATTERN_CODE_SERVER, postprocessServer(server)).replace(
			    PATTERN_CODE_ADDRESS, URLEncoder.encode(address, StandardCharsets.UTF_8.name())).replace(
			    PATTERN_CODE_COUNTRY, URLEncoder.encode(countryCode, StandardCharsets.UTF_8.name()));
						
	}

	private static String createBkgUrl(String address, String key) throws UnsupportedEncodingException {
		return URL_PATTERN_BKG.replace(
				PATTERN_CODE_KEY,URLEncoder.encode(key, StandardCharsets.UTF_8.name())).replace(
				PATTERN_CODE_ADDRESS, URLEncoder.encode(address, StandardCharsets.UTF_8.name()));		
	}
	
	private static String createPhotonUrl(String server, String address) throws UnsupportedEncodingException {
		return URL_PATTERN_PHOTON.replace(
				PATTERN_CODE_SERVER, postprocessServer(server)).replace(
				PATTERN_CODE_ADDRESS, URLEncoder.encode(address, StandardCharsets.UTF_8.name()));
	}
	
	private static String postprocessServer(String server) {
		return (server.endsWith("/") ? server : server + "/");
	}

	private static String read(DocumentContext doc, String path) {
		try {
			return doc.read(path);
		} catch (PathNotFoundException | ClassCastException e) {
			return null;
		}
	}

	private static Double readDouble(DocumentContext doc, String path) {
		try {
			return doc.read(path);
		} catch (PathNotFoundException | ClassCastException e) {
			return null;
		}
	}

	private static class GeocodingResult {

		private String url;
		private String street;
		private String houseNumber;
		private String city;
		private String district;
		private String state;
		private String country;
		private String postalCode;
		private Double latitude;
		private Double longitude;

		public GeocodingResult() {
			this(null);
		}

		public GeocodingResult(String url) {
			this(url, null, null, null, null, null, null, null, null);
		}

		public GeocodingResult(String url, String street, String city, String district, String state, String country,
				String postalCode, Double latitude, Double longitude) {
		  this(url, street, null, city, district, state, country, postalCode, latitude, longitude);
//			this.url = url;
//			this.street = street;
//			this.city = city;
//			this.district = district;
//			this.state = state;
//			this.country = country;
//			this.postalCode = postalCode;
//			this.latitude = latitude;
//			this.longitude = longitude;
		}
		
		public GeocodingResult(String url, String street, String houseNumber, String city, String district, String state, String country,
            String postalCode, Double latitude, Double longitude) {
          this.url = url;
          this.street = street;
          this.houseNumber = houseNumber;
          this.city = city;
          this.district = district;
          this.state = state;
          this.country = country;
          this.postalCode = postalCode;
          this.latitude = latitude;
          this.longitude = longitude;
      }

		public String getUrl() {
			return url;
		}

		public String getStreet() {
			return street;
		}
		
		public String getHouseNumber() {
          return houseNumber;
      }

		public String getCity() {
			return city;
		}

		public String getDistrict() {
			return district;
		}

		public String getState() {
			return state;
		}

		public String getCountry() {
			return country;
		}

		public String getPostalCode() {
			return postalCode;
		}

		public Double getLatitude() {
			return latitude;
		}

		public Double getLongitude() {
			return longitude;
		}

		@Override
		public String toString() {
			return GisUtils.getAddress(street, houseNumber, city, district, state, country, postalCode, false);
		}
	}

	private static class ChooseDialog extends JDialog {

		private static final long serialVersionUID = 1L;

		private boolean isCanceled;
		private GeocodingResult result;

		public ChooseDialog(String searchTerm, List<GeocodingResult> choices, GeocodingResult defaultValue) {
			super(JOptionPane.getRootFrame(), "Select Best Fit", DEFAULT_MODALITY_TYPE);

			JList<GeocodingResult> choicesList = new JList<>(new Vector<>(choices));

			choicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			choicesList.setSelectedIndex(0);
			choicesList.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
						result = choicesList.getSelectedValue();
						dispose();
					}
				}
			});

			JButton selectButton = new JButton("Select");
			JButton skipButton = new JButton("Skip");
			JButton cancelButton = new JButton("Cancel");

			selectButton.addActionListener(e -> {
				result = choicesList.getSelectedValue();
				dispose();
			});
			skipButton.addActionListener(e -> dispose());
			cancelButton.addActionListener(e -> {
				isCanceled = true;
				dispose();
			});

			setLayout(new BorderLayout());
			add(UI.createBorderPanel(new JLabel(searchTerm)), BorderLayout.NORTH);
			add(new JScrollPane(choicesList), BorderLayout.CENTER);
			add(UI.createEastPanel(UI.createHorizontalPanel(selectButton, skipButton, cancelButton)),
					BorderLayout.SOUTH);
			pack();
			setResizable(false);
			setLocationRelativeTo(getOwner());
			setAlwaysOnTop(true);
			getRootPane().setDefaultButton(selectButton);

			isCanceled = false;
			result = defaultValue;
		}

		public boolean isCanceled() {
			return isCanceled;
		}

		public GeocodingResult getResult() {
			return result;
		}
	}
}
