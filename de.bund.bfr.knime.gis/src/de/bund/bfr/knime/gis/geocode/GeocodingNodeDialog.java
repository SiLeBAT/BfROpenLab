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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.ColumnComboBox;

/**
 * <code>NodeDialog</code> for the "Geocoding" Node.
 * 
 * @author Christian Thoens
 */
public class GeocodingNodeDialog extends NodeDialogPane implements ItemListener {

	private GeocodingSettings set;

	private JComboBox<String> providerBox;
	private JTextField delayField;
	private JComboBox<String> multipleBox;

	private ColumnComboBox addressBox;
	private ColumnComboBox countryCodeBox;
	private JTextField serverField;

	private ColumnComboBox streetBox;
	private ColumnComboBox cityBox;
	private ColumnComboBox countyBox;
	private ColumnComboBox stateBox;
	private ColumnComboBox countryBox;
	private ColumnComboBox postalCodeBox;
	private JTextField keyField;

	private JPanel panel;

	/**
	 * New pane for configuring the Geocoding node.
	 */
	public GeocodingNodeDialog() {
		set = new GeocodingSettings();
		providerBox = new JComboBox<>(GeocodingSettings.PROVIDER_CHOICES);
		providerBox.addItemListener(this);
		delayField = new JTextField();
		multipleBox = new JComboBox<>(GeocodingSettings.MULTIPLE_CHOICES);

		addressBox = new ColumnComboBox(false);
		countryCodeBox = new ColumnComboBox(false);
		serverField = new JTextField();

		streetBox = new ColumnComboBox(true);
		cityBox = new ColumnComboBox(true);
		countyBox = new ColumnComboBox(true);
		stateBox = new ColumnComboBox(true);
		countryBox = new ColumnComboBox(true);
		postalCodeBox = new ColumnComboBox(true);
		keyField = new JTextField();

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		addTab("Options", UI.createNorthPanel(panel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		addressBox.removeAllColumns();
		countryCodeBox.removeAllColumns();

		streetBox.removeAllColumns();
		cityBox.removeAllColumns();
		countyBox.removeAllColumns();
		stateBox.removeAllColumns();
		countryBox.removeAllColumns();
		postalCodeBox.removeAllColumns();

		for (DataColumnSpec column : KnimeUtils.getColumns(specs[0],
				StringCell.TYPE)) {
			addressBox.addColumn(column);
			countryCodeBox.addColumn(column);

			streetBox.addColumn(column);
			cityBox.addColumn(column);
			countyBox.addColumn(column);
			stateBox.addColumn(column);
			countryBox.addColumn(column);
			postalCodeBox.addColumn(column);
		}

		set.loadSettings(settings);

		providerBox.setSelectedItem(set.getServiceProvider());
		delayField.setText(set.getRequestDelay() + "");
		multipleBox.setSelectedItem(set.getMultipleResults());

		addressBox.setSelectedColumnName(set.getAddressColumn());
		countryCodeBox.setSelectedColumnName(set.getCountryCodeColumn());
		serverField.setText(set.getGisgraphyServer() != null ? set
				.getGisgraphyServer() : "");

		streetBox.setSelectedColumnName(set.getStreetColumn());
		cityBox.setSelectedColumnName(set.getCityColumn());
		countyBox.setSelectedColumnName(set.getCountyColumn());
		stateBox.setSelectedColumnName(set.getStateColumn());
		countryBox.setSelectedColumnName(set.getCountryColumn());
		postalCodeBox.setSelectedColumnName(set.getPostalCodeColumn());
		keyField.setText(set.getMapQuestKey() != null ? set.getMapQuestKey()
				: "");

		updatePanel();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (delayField.getText().trim().isEmpty()) {
			throw new InvalidSettingsException("No Request Delay specified");
		}

		try {
			Integer.parseInt(delayField.getText());
		} catch (NumberFormatException e) {
			throw new InvalidSettingsException("Request Delay invalid");
		}

		set.setServiceProvider((String) providerBox.getSelectedItem());
		set.setRequestDelay(Integer.parseInt(delayField.getText()));
		set.setMultipleResults((String) multipleBox.getSelectedItem());

		if (set.getServiceProvider()
				.equals(GeocodingSettings.PROVIDER_MAPQUEST)) {
			if (streetBox.getSelectedColumnName() == null
					&& cityBox.getSelectedColumnName() == null
					&& countyBox.getSelectedColumnName() == null
					&& stateBox.getSelectedColumnName() == null
					&& countryBox.getSelectedColumnName() == null
					&& postalCodeBox.getSelectedColumnName() == null) {
				throw new InvalidSettingsException("No Address Data specified");
			}

			if (keyField.getText().trim().isEmpty()) {
				throw new InvalidSettingsException("No MapQuest Key specified");
			}

			set.setStreetColumn(streetBox.getSelectedColumnName());
			set.setCityColumn(cityBox.getSelectedColumnName());
			set.setCountyColumn(countyBox.getSelectedColumnName());
			set.setStateColumn(stateBox.getSelectedColumnName());
			set.setCountryColumn(countryBox.getSelectedColumnName());
			set.setPostalCodeColumn(postalCodeBox.getSelectedColumnName());
			set.setMapQuestKey(keyField.getText().trim());
		} else if (set.getServiceProvider().equals(
				GeocodingSettings.PROVIDER_GISGRAPHY_PUBLIC)
				|| set.getServiceProvider().equals(
						GeocodingSettings.PROVIDER_GISGRAPHY)) {
			if (addressBox.getSelectedColumnName() == null) {
				throw new InvalidSettingsException("No Address specified");
			}

			if (countryCodeBox.getSelectedColumnName() == null) {
				throw new InvalidSettingsException("No Country Code specified");
			}

			if (set.getServiceProvider().equals(
					GeocodingSettings.PROVIDER_GISGRAPHY)
					&& serverField.getText().trim().isEmpty()) {
				throw new InvalidSettingsException("No Server specified");
			}

			set.setAddressColumn(addressBox.getSelectedColumnName());
			set.setCountryCodeColumn(countryCodeBox.getSelectedColumnName());
			set.setGisgraphyServer(serverField.getText().trim());
		}

		set.saveSettings(settings);
	}

	private void updatePanel() {
		String provider = (String) providerBox.getSelectedItem();

		panel.removeAll();
		panel.add(UI.createOptionsPanel("Provider",
				Arrays.asList(new JLabel("Service Provider")),
				Arrays.asList(providerBox)));

		if (provider.equals(GeocodingSettings.PROVIDER_MAPQUEST)) {
			panel.add(UI.createOptionsPanel("Address", Arrays.asList(
					new JLabel("Street:"), new JLabel("City:"), new JLabel(
							"County:"), new JLabel("State:"), new JLabel(
							"Country:"), new JLabel("Postal Code:")), Arrays
					.asList(streetBox, cityBox, countyBox, stateBox,
							countryBox, postalCodeBox)));
			panel.add(UI.createOptionsPanel("Other Options", Arrays.asList(
					new JLabel("MapQuest Key:"), new JLabel(
							"Delay between Request:"), new JLabel(
							"When multiple Results:")), Arrays.asList(keyField,
					delayField, multipleBox)));
		} else if (provider.equals(GeocodingSettings.PROVIDER_GISGRAPHY_PUBLIC)) {
			panel.add(UI.createOptionsPanel("Addresses", Arrays.asList(
					new JLabel("Address:"), new JLabel("Country Code:")),
					Arrays.asList(addressBox, countryCodeBox)));
			panel.add(UI.createOptionsPanel("Other Options", Arrays.asList(
					new JLabel("Delay between Request:"), new JLabel(
							"When multiple Results:")), Arrays.asList(
					delayField, multipleBox)));
		} else if (provider.equals(GeocodingSettings.PROVIDER_GISGRAPHY)) {
			panel.add(UI.createOptionsPanel("Addresses", Arrays.asList(
					new JLabel("Address:"), new JLabel("Country Code:")),
					Arrays.asList(addressBox, countryCodeBox)));
			panel.add(UI.createOptionsPanel("Other Options", Arrays.asList(
					new JLabel("Server Address:"), new JLabel(
							"Delay between Request:"), new JLabel(
							"When multiple Results:")), Arrays.asList(
					serverField, delayField, multipleBox)));
		}

		panel.revalidate();

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			updatePanel();
		}
	}

}
