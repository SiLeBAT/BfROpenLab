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

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		addTab("Options", UI.createNorthPanel(panel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		addressBox.removeAllColumns();
		countryCodeBox.removeAllColumns();

		for (DataColumnSpec column : KnimeUtils.getColumns(specs[0], StringCell.TYPE)) {
			addressBox.addColumn(column);
			countryCodeBox.addColumn(column);
		}

		set.loadSettings(settings);

		providerBox.setSelectedItem(set.getServiceProvider());
		delayField.setText(set.getRequestDelay() + "");
		multipleBox.setSelectedItem(set.getMultipleResults());

		addressBox.setSelectedColumnName(set.getAddressColumn());
		countryCodeBox.setSelectedColumnName(set.getCountryCodeColumn());
		serverField.setText(set.getGisgraphyServer() != null ? set.getGisgraphyServer() : "");

		updatePanel();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (addressBox.getSelectedColumnName() == null) {
			throw new InvalidSettingsException("No Address Data specified");
		}

		if (delayField.getText().trim().isEmpty()) {
			throw new InvalidSettingsException("No Request Delay specified");
		}

		try {
			Integer.parseInt(delayField.getText());
		} catch (NumberFormatException e) {
			throw new InvalidSettingsException("Request Delay invalid");
		}

		set.setAddressColumn(addressBox.getSelectedColumnName());
		set.setServiceProvider((String) providerBox.getSelectedItem());
		set.setRequestDelay(Integer.parseInt(delayField.getText()));
		set.setMultipleResults((String) multipleBox.getSelectedItem());

		if (set.getServiceProvider().equals(GeocodingSettings.PROVIDER_GISGRAPHY)) {
			if (countryCodeBox.getSelectedColumnName() == null) {
				throw new InvalidSettingsException("No Country Code specified");
			}

			if (set.getServiceProvider().equals(GeocodingSettings.PROVIDER_GISGRAPHY)
					&& serverField.getText().trim().isEmpty()) {
				throw new InvalidSettingsException("No Server specified");
			}

			set.setCountryCodeColumn(countryCodeBox.getSelectedColumnName());
			set.setGisgraphyServer(serverField.getText().trim());
		}

		set.saveSettings(settings);
	}

	private void updatePanel() {
		String provider = (String) providerBox.getSelectedItem();

		panel.removeAll();
		panel.add(UI.createOptionsPanel("Provider", Arrays.asList(new JLabel("Service Provider")),
				Arrays.asList(providerBox)));

		if (provider.equals(GeocodingSettings.PROVIDER_MAPQUEST)) {
			panel.add(UI.createOptionsPanel("Addresses", Arrays.asList(new JLabel("Address:")),
					Arrays.asList(addressBox)));
			panel.add(UI.createOptionsPanel("Other Options",
					Arrays.asList(new JLabel("Delay between Request (ms):"), new JLabel("When multiple Results:")),
					Arrays.asList(delayField, multipleBox)));
		} else if (provider.equals(GeocodingSettings.PROVIDER_GISGRAPHY)) {
			panel.add(UI.createOptionsPanel("Addresses",
					Arrays.asList(new JLabel("Address:"), new JLabel("Country Code:")),
					Arrays.asList(addressBox, countryCodeBox)));
			panel.add(UI.createOptionsPanel("Other Options",
					Arrays.asList(new JLabel("Server Address:"), new JLabel("Delay between Request (ms):"),
							new JLabel("When multiple Results:")),
					Arrays.asList(serverField, delayField, multipleBox)));
		} else if (provider.equals(GeocodingSettings.PROVIDER_BKG)) {
			panel.add(UI.createOptionsPanel("Addresses", Arrays.asList(new JLabel("Address:")),
					Arrays.asList(addressBox)));
			panel.add(UI.createOptionsPanel("Other Options",
					Arrays.asList(new JLabel("Delay between Request (ms):"), new JLabel("When multiple Results:")),
					Arrays.asList(delayField, multipleBox)));
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
