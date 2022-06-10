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
package de.bund.bfr.knime.openkrise.util.address;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.openkrise.TracingColumns;

/**
 * This is the model implementation of AddressCreator.
 * 
 *
 * @author Christian Thoens
 */
public class AddressCreatorNodeModel extends SimpleStreamableFunctionNodeModel {

	protected static final String CFG_STREET = "Street";
	protected static final String CFG_HOUSE_NUMBER = "HouseNumber";
	protected static final String CFG_CITY = "City";
	protected static final String CFG_DISTRICT = "District";
	protected static final String CFG_STATE = "State";
	protected static final String CFG_COUNTRY = "Country";
	protected static final String CFG_POSTAL_CODE = "PostalCode";
	protected static final String CFG_HOUSE_NUMBER_AFTER_STREET = "HouseNumberAfterStreet";

	private SettingsModelString street;
	private SettingsModelString houseNumber;
	private SettingsModelString city;
	private SettingsModelString district;
	private SettingsModelString state;
	private SettingsModelString country;
	private SettingsModelString postalCode;
	private SettingsModelBoolean houseNumberAfterStreet;

	/**
	 * Constructor for the node model.
	 */
	protected AddressCreatorNodeModel() {
		street = new SettingsModelString(CFG_STREET, TracingColumns.STATION_STREET);
		houseNumber = new SettingsModelString(CFG_HOUSE_NUMBER, TracingColumns.STATION_HOUSENO);
		city = new SettingsModelString(CFG_CITY, TracingColumns.STATION_CITY);
		district = new SettingsModelString(CFG_DISTRICT, TracingColumns.STATION_DISTRICT);
		state = new SettingsModelString(CFG_STATE, TracingColumns.STATION_STATE);
		country = new SettingsModelString(CFG_COUNTRY, TracingColumns.STATION_COUNTRY);
		postalCode = new SettingsModelString(CFG_POSTAL_CODE, TracingColumns.STATION_ZIP);
		houseNumberAfterStreet = new SettingsModelBoolean(CFG_HOUSE_NUMBER_AFTER_STREET, true);
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec) throws InvalidSettingsException {
		if (spec.containsName(TracingColumns.ADDRESS)) {
			throw new InvalidSettingsException("Column \"" + TracingColumns.ADDRESS + "\" already exists");
		}

		ColumnRearranger rearranger = new ColumnRearranger(spec);
		int streetColumn = spec.findColumnIndex(street.getStringValue());
		int houseNumberColumn = spec.findColumnIndex(houseNumber.getStringValue());
		int cityColumn = spec.findColumnIndex(city.getStringValue());
		int districtColumn = spec.findColumnIndex(district.getStringValue());
		int stateColumn = spec.findColumnIndex(state.getStringValue());
		int countryColumn = spec.findColumnIndex(country.getStringValue());
		int postalCodeColumn = spec.findColumnIndex(postalCode.getStringValue());

		rearranger.append(new AbstractCellFactory(
				new DataColumnSpecCreator(TracingColumns.ADDRESS, StringCell.TYPE).createSpec()) {

			@Override
			public DataCell[] getCells(DataRow row) {
				String street = streetColumn != -1 ? street = IO.getCleanString(row.getCell(streetColumn)) : null;
				String houseNumber = houseNumberColumn != -1
						? houseNumber = IO.getCleanString(row.getCell(houseNumberColumn)) : null;
				String city = cityColumn != -1 ? city = IO.getCleanString(row.getCell(cityColumn)) : null;
				String district = districtColumn != -1 ? district = IO.getCleanString(row.getCell(districtColumn))
						: null;
				String state = stateColumn != -1 ? state = IO.getCleanString(row.getCell(stateColumn)) : null;
				String country = countryColumn != -1 ? country = IO.getCleanString(row.getCell(countryColumn)) : null;
				String postalCode = postalCodeColumn != -1
						? postalCode = IO.getCleanString(row.getCell(postalCodeColumn)) : null;

				return new DataCell[] { IO.createCell(GisUtils.getAddress(street, houseNumber, city, district, state,
						country, postalCode, houseNumberAfterStreet.getBooleanValue())) };
			}
		});

		return rearranger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		street.saveSettingsTo(settings);
		houseNumber.saveSettingsTo(settings);
		city.saveSettingsTo(settings);
		district.saveSettingsTo(settings);
		state.saveSettingsTo(settings);
		country.saveSettingsTo(settings);
		postalCode.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		street.loadSettingsFrom(settings);
		houseNumber.loadSettingsFrom(settings);
		city.loadSettingsFrom(settings);
		district.loadSettingsFrom(settings);
		state.loadSettingsFrom(settings);
		country.loadSettingsFrom(settings);
		postalCode.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		street.validateSettings(settings);
		houseNumber.validateSettings(settings);
		city.validateSettings(settings);
		district.validateSettings(settings);
		state.validateSettings(settings);
		country.validateSettings(settings);
		postalCode.validateSettings(settings);
	}
}
