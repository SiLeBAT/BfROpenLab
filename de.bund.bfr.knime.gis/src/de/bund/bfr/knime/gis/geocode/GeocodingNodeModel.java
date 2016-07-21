/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

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
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.Activator;
import de.bund.bfr.knime.gis.GisUtils;
import net.minidev.json.JSONArray;

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
	public static final String DISTRICT_COLUMN = "GeocodingDistrict";
	public static final String STATE_COLUMN = "GeocodingState";
	public static final String COUNTRY_COLUMN = "GeocodingCountry";
	public static final String POSTAL_CODE_COLUMN = "GeocodingPostalCode";
	public static final String LATITUDE_COLUMN = "GeocodingLatitude";
	public static final String LONGITUDE_COLUMN = "GeocodingLongitude";

	private ImmutableSet<String> GEOCODING_COLUMNS = ImmutableSet.of(URL_COLUMN, STREET_COLUMN, CITY_COLUMN,
			DISTRICT_COLUMN, STATE_COLUMN, COUNTRY_COLUMN, POSTAL_CODE_COLUMN, LATITUDE_COLUMN, LONGITUDE_COLUMN);

	private static final String DE = "DE";

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
			int countryCodeIndex = spec.findColumnIndex(set.getCountryCodeColumn());
			String address = null;
			String countryCode = null;

			if (addressIndex != -1) {
				address = IO.getCleanString(row.getCell(addressIndex));
			}

			if (countryCodeIndex != -1) {
				countryCode = IO.getCleanString(row.getCell(countryCodeIndex));
			}

			GeocodingResult result = performGeocoding(address, countryCode);

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
	protected void reset() {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private GeocodingResult performGeocoding(String address, String countryCode)
			throws XPathExpressionException, IOException, ParserConfigurationException, URISyntaxException,
			SAXException, CanceledExecutionException, InvalidSettingsException {
		switch (set.getServiceProvider()) {
		case MAPQUEST:
			return performMapQuestGeocoding(address);
		case GISGRAPHY:
			return performGisgraphyGeocoding(address, countryCode);
		case BKG:
			return performBkgGeocoding(address);
		}

		throw new RuntimeException("Should not happen");
	}

	private GeocodingResult performMapQuestGeocoding(String address)
			throws IOException, ParserConfigurationException, XPathExpressionException, URISyntaxException,
			SAXException, InvalidSettingsException, CanceledExecutionException {
		if (address == null) {
			return new GeocodingResult();
		}

		String mapQuestKey = Activator.getDefault().getPreferenceStore()
				.getString(GeocodingPreferencePage.MAPQUEST_KEY);

		if (Strings.isNullOrEmpty(mapQuestKey)) {
			throw new InvalidSettingsException(
					"MapQuest key in preferences missing. Please enter it under KNIME->Geocoding.");
		}

		String url = "https://open.mapquestapi.com/geocoding/v1/address?key="
				+ URLEncoder.encode(mapQuestKey, StandardCharsets.UTF_8.name()) + "&location="
				+ URLEncoder.encode(address, StandardCharsets.UTF_8.name());
		String urlWithoutKey = url.replace(mapQuestKey, "XXXXXX");

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

				results.add(new GeocodingResult(url, read(r, "$.street"), read(r, "$.adminArea5"),
						read(r, "$.adminArea4"), read(r, "$.adminArea3"), read(r, "$.adminArea1"),
						read(r, "$.postalCode"), readDouble(r, "$.latLng.lat"), readDouble(r, "$.latLng.lng")));
			}

			return getIndex(address, results, new GeocodingResult(urlWithoutKey));
		}
	}

	private GeocodingResult performGisgraphyGeocoding(String address, String countryCode)
			throws IOException, ParserConfigurationException, XPathExpressionException, URISyntaxException,
			SAXException, CanceledExecutionException {
		if (address == null || countryCode == null) {
			return new GeocodingResult();
		}

		String url = set.getGisgraphyServer() + "?address=" + URLEncoder.encode(address, StandardCharsets.UTF_8.name())
				+ "&country=" + URLEncoder.encode(countryCode, StandardCharsets.UTF_8.name()) + "&format=json";

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
			throws MalformedURLException, IOException, InvalidSettingsException, CanceledExecutionException {
		if (address == null) {
			return new GeocodingResult();
		}

		String uuid = Activator.getDefault().getPreferenceStore().getString(GeocodingPreferencePage.BKG_UUID);

		if (Strings.isNullOrEmpty(uuid)) {
			throw new InvalidSettingsException("UUID in preferences missing. Please enter it under KNIME->Geocoding.");
		}

		String url = "https://sg.geodatenzentrum.de/gdz_geokodierung__"
				+ URLEncoder.encode(uuid, StandardCharsets.UTF_8.name()) + "/geosearch?query="
				+ URLEncoder.encode(address, StandardCharsets.UTF_8.name());
		String urlWithoutUuid = url.replace(uuid, "XXXXXX");

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
			this.url = url;
			this.street = street;
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
			return GisUtils.getAddress(street, null, city, district, state, country, postalCode, false);
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
			add(UI.createHorizontalPanel(new JLabel(searchTerm)), BorderLayout.NORTH);
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
