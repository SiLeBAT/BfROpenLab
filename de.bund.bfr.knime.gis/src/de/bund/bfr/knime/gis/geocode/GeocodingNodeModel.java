/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.gis.geocode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.bund.bfr.knime.IO;

/**
 * This is the model implementation of Geocoding.
 * 
 * 
 * @author Christian Thoens
 */
public class GeocodingNodeModel extends NodeModel {

	public static final String URL_COLUMN = "GeocodingURL";
	public static final String STREET_COLUMN = "GeocodingStreet";
	public static final String CITY_COLUMN = "GeocodingCity";
	public static final String COUNTY_COLUMN = "GeocodingCounty";
	public static final String STATE_COLUMN = "GeocodingState";
	public static final String COUNTRY_COLUMN = "GeocodingCountry";
	public static final String POSTAL_CODE_COLUMN = "GeocodingPostalCode";
	public static final String LATITUDE_COLUMN = "GeocodingLatitude";
	public static final String LONGITUDE_COLUMN = "GeocodingLongitude";

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
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable table = inData[0];
		DataTableSpec spec = table.getSpec();
		DataTableSpec outSpec = configure(new DataTableSpec[] { spec })[0];
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		int index = 0;

		for (DataRow row : table) {
			DataCell[] cells = new DataCell[outSpec.getNumColumns()];

			for (int i = 0; i < spec.getNumColumns(); i++) {
				cells[i] = row.getCell(i);
			}

			int addressIndex = spec.findColumnIndex(set.getAddressColumn());
			int streetIndex = spec.findColumnIndex(set.getStreetColumn());
			int cityIndex = spec.findColumnIndex(set.getCityColumn());
			int countyIndex = spec.findColumnIndex(set.getCountyColumn());
			int stateIndex = spec.findColumnIndex(set.getStateColumn());
			int countryIndex = spec.findColumnIndex(set.getCountryColumn());
			int countryCodeIndex = spec.findColumnIndex(set
					.getCountryCodeColumn());
			int postalCodeIndex = spec.findColumnIndex(set
					.getPostalCodeColumn());
			String address = null;
			String street = null;
			String city = null;
			String county = null;
			String state = null;
			String country = null;
			String countryCode = null;
			String postalCode = null;

			if (addressIndex != -1) {
				address = IO.getCleanString(row.getCell(addressIndex));
			}

			if (streetIndex != -1) {
				street = IO.getCleanString(row.getCell(streetIndex));
			}

			if (cityIndex != -1) {
				city = IO.getCleanString(row.getCell(cityIndex));
			}

			if (countyIndex != -1) {
				county = IO.getCleanString(row.getCell(countyIndex));
			}

			if (stateIndex != -1) {
				state = IO.getCleanString(row.getCell(stateIndex));
			}

			if (countryIndex != -1) {
				country = IO.getCleanString(row.getCell(countryIndex));
			}

			if (countryCodeIndex != -1) {
				countryCode = IO.getCleanString(row.getCell(countryCodeIndex));
			}

			if (postalCodeIndex != -1) {
				postalCode = IO.getCleanString(row.getCell(postalCodeIndex));
			}

			GeocodingResult result = null;

			if (set.getServiceProvider().equals(
					GeocodingSettings.PROVIDER_MAPQUEST)) {
				try {
					result = performMapQuestGeocoding(street, city, county,
							state, country, postalCode);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (set.getServiceProvider().equals(
					GeocodingSettings.PROVIDER_GISGRAPHY)) {
				try {
					result = performGisgraphyGeocoding(address, countryCode);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			switch (result.getStatus()) {
			case GeocodingResult.STATUS_OK:
			case GeocodingResult.STATUS_FAILED:
				cells[outSpec.findColumnIndex(URL_COLUMN)] = IO
						.createCell(result.getUrl());
				cells[outSpec.findColumnIndex(STREET_COLUMN)] = IO
						.createCell(result.getStreet());
				cells[outSpec.findColumnIndex(CITY_COLUMN)] = IO
						.createCell(result.getCity());
				cells[outSpec.findColumnIndex(COUNTY_COLUMN)] = IO
						.createCell(result.getCounty());
				cells[outSpec.findColumnIndex(STATE_COLUMN)] = IO
						.createCell(result.getState());
				cells[outSpec.findColumnIndex(COUNTRY_COLUMN)] = IO
						.createCell(result.getCountry());
				cells[outSpec.findColumnIndex(POSTAL_CODE_COLUMN)] = IO
						.createCell(result.getPostalCode());
				cells[outSpec.findColumnIndex(LATITUDE_COLUMN)] = IO
						.createCell(result.getLatitude());
				cells[outSpec.findColumnIndex(LONGITUDE_COLUMN)] = IO
						.createCell(result.getLongitude());
				break;
			case GeocodingResult.STATUS_OVER_LIMIT:
				throw new Exception("OVER_QUERY_LIMIT");
			}

			container.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.setProgress((double) (index++) / (double) table.getRowCount());
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
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (int i = 0; i < inSpecs[0].getNumColumns(); i++) {
			String name = inSpecs[0].getColumnSpec(i).getName();

			if (name.equals(URL_COLUMN) || name.equals(STREET_COLUMN)
					|| name.equals(CITY_COLUMN) || name.equals(COUNTY_COLUMN)
					|| name.equals(STATE_COLUMN) || name.equals(COUNTRY_COLUMN)
					|| name.equals(POSTAL_CODE_COLUMN)
					|| name.equals(LATITUDE_COLUMN)
					|| name.equals(LONGITUDE_COLUMN)) {
				throw new InvalidSettingsException("Column name \"" + name
						+ "\" not allowed in input table");
			}

			columns.add(inSpecs[0].getColumnSpec(i));
		}

		columns.add(new DataColumnSpecCreator(URL_COLUMN, StringCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(STREET_COLUMN, StringCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(CITY_COLUMN, StringCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(COUNTY_COLUMN, StringCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(STATE_COLUMN, StringCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(COUNTRY_COLUMN, StringCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(POSTAL_CODE_COLUMN,
				StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(LATITUDE_COLUMN, DoubleCell.TYPE)
				.createSpec());
		columns.add(new DataColumnSpecCreator(LONGITUDE_COLUMN, DoubleCell.TYPE)
				.createSpec());

		return new DataTableSpec[] { new DataTableSpec(
				columns.toArray(new DataColumnSpec[0])) };
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	private GeocodingResult performMapQuestGeocoding(String street,
			String city, String county, String state, String country,
			String postalCode) throws IOException,
			ParserConfigurationException, XPathExpressionException {
		if (street == null && city == null && county == null && state == null
				&& country == null && postalCode == null) {
			return new GeocodingResult("", GeocodingResult.STATUS_FAILED);
		}

		StringBuilder json = new StringBuilder("{\"location\":{");

		if (street != null) {
			json.append("\"street\":\"" + street + "\",");
		}

		if (city != null) {
			json.append("\"city\":\"" + city + "\",");
		}

		if (county != null) {
			json.append("\"county\":\"" + county + "\",");
		}

		if (state != null) {
			json.append("\"state\":\"" + state + "\",");
		}

		if (country != null) {
			json.append("\"country\":\"" + country + "\",");
		}

		if (postalCode != null) {
			json.append("\"postalCode\":\"" + postalCode + "\",");
		}

		json.deleteCharAt(json.length() - 1);
		json.append("},}");

		URI uri;

		try {
			uri = new URI("http", "open.mapquestapi.com",
					"/geocoding/v1/address", "json=" + json.toString(), null);
		} catch (URISyntaxException e) {
			uri = null;
			e.printStackTrace();
		}

		String url = uri.toASCIIString() + "&key=" + set.getMapQuestKey()
				+ "&outFormat=xml";
		URLConnection yc = new URL(url).openConnection();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = null;

		try {
			doc = dBuilder.parse(yc.getInputStream());
		} catch (Exception e) {
			return new GeocodingResult(url, GeocodingResult.STATUS_FAILED);
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		int n = ((NodeList) xPath.compile(
				"/response/results/result/locations/location").evaluate(doc,
				XPathConstants.NODESET)).getLength();
		List<String> streets = new ArrayList<>();
		List<String> cities = new ArrayList<>();
		List<String> counties = new ArrayList<>();
		List<String> states = new ArrayList<>();
		List<String> countries = new ArrayList<>();
		List<String> postalCodes = new ArrayList<>();
		List<Double> latitudes = new ArrayList<>();
		List<Double> longitudes = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			streets.add(xPath.compile(
					"/response/results/result/locations/location[" + (i + 1)
							+ "]/street").evaluate(doc));
			cities.add(xPath.compile(
					"/response/results/result/locations/location[" + (i + 1)
							+ "]/adminArea5").evaluate(doc));
			counties.add(xPath.compile(
					"/response/results/result/locations/location[" + (i + 1)
							+ "]/adminArea4").evaluate(doc));
			states.add(xPath.compile(
					"/response/results/result/locations/location[" + (i + 1)
							+ "]/adminArea3").evaluate(doc));
			countries.add(xPath.compile(
					"/response/results/result/locations/location[" + (i + 1)
							+ "]/adminArea1").evaluate(doc));
			postalCodes.add(xPath.compile(
					"/response/results/result/locations/location[" + (i + 1)
							+ "]/postalCode").evaluate(doc));
			latitudes.add(Double.parseDouble(xPath.compile(
					"/response/results/result/locations/location[" + (i + 1)
							+ "]/latLng/lat").evaluate(doc)));
			longitudes.add(Double.parseDouble(xPath.compile(
					"/response/results/result/locations/location[" + (i + 1)
							+ "]/latLng/lng").evaluate(doc)));

		}

		String address = getAddress(street, city, county, state, country,
				postalCode);
		List<String> addresses = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			addresses.add(getAddress(streets.get(i), cities.get(i),
					counties.get(i), states.get(i), countries.get(i),
					postalCodes.get(i)));
		}

		int index = -1;

		if (n == 0) {
			return new GeocodingResult(url, GeocodingResult.STATUS_FAILED);
		} else if (n == 1) {
			index = 0;
		} else if (n > 1) {
			if (set.getMultipleResults().equals(
					GeocodingSettings.MULTIPLE_DO_NOT_USE)) {
				return new GeocodingResult(url, GeocodingResult.STATUS_FAILED);
			} else if (set.getMultipleResults().equals(
					GeocodingSettings.MULTIPLE_USE_FIRST)) {
				index = 0;
			} else if (set.getMultipleResults().equals(
					GeocodingSettings.MULTIPLE_ASK_USER)) {
				Object selection = JOptionPane.showInputDialog(null, address,
						"Select Best Fit", JOptionPane.QUESTION_MESSAGE, null,
						addresses.toArray(), addresses.get(0));

				if (selection != null) {
					index = addresses.indexOf(selection);
				} else {
					return new GeocodingResult(url,
							GeocodingResult.STATUS_FAILED);
				}
			}
		}

		return new GeocodingResult(url, streets.get(index), cities.get(index),
				counties.get(index), states.get(index), countries.get(index),
				postalCodes.get(index), latitudes.get(index),
				longitudes.get(index), GeocodingResult.STATUS_OK);
	}

	private GeocodingResult performGisgraphyGeocoding(String address,
			String countryCode) throws IOException,
			ParserConfigurationException, XPathExpressionException {
		if (address == null || countryCode == null) {
			return new GeocodingResult(null, GeocodingResult.STATUS_FAILED);
		}

		String server = set.getGisgraphyServer().replace("http://", "");
		String authority = server.substring(0, server.indexOf("/"));
		String path = server.substring(server.indexOf("/"));
		URI uri;

		try {
			uri = new URI("http", authority, path, "address=" + address
					+ "&country=" + countryCode + "&postal=true", null);
		} catch (URISyntaxException e) {
			uri = null;
			e.printStackTrace();
		}

		String url = uri.toASCIIString();
		URLConnection yc = new URL(url).openConnection();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = null;

		try {
			doc = dBuilder.parse(yc.getInputStream());
		} catch (Exception e) {
			return new GeocodingResult(url, GeocodingResult.STATUS_FAILED);
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		int n = ((NodeList) xPath.compile("/results/result").evaluate(doc,
				XPathConstants.NODESET)).getLength();
		List<String> cities = new ArrayList<>();
		List<String> states = new ArrayList<>();
		List<String> countryCodes = new ArrayList<>();
		List<Double> latitudes = new ArrayList<>();
		List<Double> longitudes = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			cities.add(xPath.compile("/results/result[" + (i + 1) + "]/city")
					.evaluate(doc));
			states.add(xPath.compile("/results/result[" + (i + 1) + "]/state")
					.evaluate(doc));
			countryCodes.add(xPath.compile(
					"/results/result[" + (i + 1) + "]/countryCode").evaluate(
					doc));
			latitudes.add(Double.parseDouble(xPath.compile(
					"/results/result[" + (i + 1) + "]/lat").evaluate(doc)));
			longitudes.add(Double.parseDouble(xPath.compile(
					"/results/result[" + (i + 1) + "]/lng").evaluate(doc)));

		}

		String fullAddress = getAddress(address, null, null, null, countryCode,
				null);
		List<String> addresses = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			addresses.add(getAddress(null, cities.get(i), null, states.get(i),
					countryCodes.get(i), null));
		}

		int index = -1;

		if (n == 0) {
			return new GeocodingResult(url, GeocodingResult.STATUS_FAILED);
		} else if (n == 1) {
			index = 0;
		} else if (n > 1) {
			if (set.getMultipleResults().equals(
					GeocodingSettings.MULTIPLE_DO_NOT_USE)) {
				return new GeocodingResult(url, GeocodingResult.STATUS_FAILED);
			} else if (set.getMultipleResults().equals(
					GeocodingSettings.MULTIPLE_USE_FIRST)) {
				index = 0;
			} else if (set.getMultipleResults().equals(
					GeocodingSettings.MULTIPLE_ASK_USER)) {
				Object selection = JOptionPane.showInputDialog(null,
						fullAddress, "Select Best Fit",
						JOptionPane.QUESTION_MESSAGE, null,
						addresses.toArray(), addresses.get(0));

				if (selection != null) {
					index = addresses.indexOf(selection);
				} else {
					return new GeocodingResult(url,
							GeocodingResult.STATUS_FAILED);
				}
			}
		}

		return new GeocodingResult(url, null, cities.get(index), null,
				states.get(index), countryCodes.get(index), null,
				latitudes.get(index), longitudes.get(index),
				GeocodingResult.STATUS_OK);
	}

	private static String getAddress(String street, String city, String county,
			String state, String country, String postalCode) {
		StringBuilder s = new StringBuilder();

		if (!isEmpty(street)) {
			s.append(street + ", ");
		}

		if (!isEmpty(city) && !isEmpty(postalCode)) {
			s.append(postalCode + " " + city + ", ");
		} else if (!isEmpty(city)) {
			s.append(city + ", ");
		} else if (!isEmpty(postalCode)) {
			s.append(postalCode + ", ");
		}

		if (!isEmpty(county)) {
			s.append(county + ", ");
		}

		if (!isEmpty(state)) {
			s.append(state + ", ");
		}

		if (!isEmpty(country)) {
			s.append(country + ", ");
		}

		if (s.length() > 2) {
			s.delete(s.length() - 2, s.length());
		}

		return s.toString();
	}

	private static boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static class GeocodingResult {

		public static final int STATUS_OK = 1;
		public static final int STATUS_FAILED = 2;
		public static final int STATUS_OVER_LIMIT = 3;

		private String url;
		private String street;
		private String city;
		private String county;
		private String state;
		private String country;
		private String postalCode;
		private Double latitude;
		private Double longitude;
		private int status;

		public GeocodingResult(String url, int status) {
			this(url, null, null, null, null, null, null, null, null, status);
		}

		public GeocodingResult(String url, String street, String city,
				String county, String state, String country, String postalCode,
				Double latitude, Double longitude, int status) {
			this.url = url;
			this.street = street;
			this.city = city;
			this.county = county;
			this.state = state;
			this.country = country;
			this.postalCode = postalCode;
			this.latitude = latitude;
			this.longitude = longitude;
			this.status = status;
		}

		public String getUrl() {
			return url;
		}

		public String getStreet() {
			return street;
		}

		public String getCity() {
			return city;
		}

		public String getCounty() {
			return county;
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

		public int getStatus() {
			return status;
		}
	}

}
