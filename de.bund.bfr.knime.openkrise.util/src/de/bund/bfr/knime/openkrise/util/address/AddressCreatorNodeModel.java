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
package de.bund.bfr.knime.openkrise.util.address;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.openkrise.TracingColumns;

/**
 * This is the model implementation of AddressCreator.
 * 
 *
 * @author Christian Thoens
 */
public class AddressCreatorNodeModel extends NodeModel {

	protected static final String CFG_STREET = "Street";
	protected static final String CFG_HOUSE_NUMBER = "HouseNumber";
	protected static final String CFG_CITY = "City";
	protected static final String CFG_DISTRICT = "District";
	protected static final String CFG_STATE = "State";
	protected static final String CFG_COUNTRY = "Country";
	protected static final String CFG_POSTAL_CODE = "PostalCode";

	private SettingsModelString street;
	private SettingsModelString houseNumber;
	private SettingsModelString city;
	private SettingsModelString district;
	private SettingsModelString state;
	private SettingsModelString country;
	private SettingsModelString postalCode;

	/**
	 * Constructor for the node model.
	 */
	protected AddressCreatorNodeModel() {
		super(1, 1);
		street = new SettingsModelString(CFG_STREET, TracingColumns.STATION_STREET);
		houseNumber = new SettingsModelString(CFG_HOUSE_NUMBER, TracingColumns.STATION_HOUSENO);
		city = new SettingsModelString(CFG_CITY, TracingColumns.STATION_CITY);
		district = new SettingsModelString(CFG_DISTRICT, TracingColumns.STATION_DISTRICT);
		state = new SettingsModelString(CFG_STATE, TracingColumns.STATION_STATE);
		country = new SettingsModelString(CFG_COUNTRY, TracingColumns.STATION_COUNTRY);
		postalCode = new SettingsModelString(CFG_POSTAL_CODE, TracingColumns.STATION_ZIP);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable table = inData[0];
		DataTableSpec spec = table.getSpec();
		DataTableSpec outSpec = createOutSpec(spec);
		BufferedDataContainer container = exec.createDataContainer(outSpec);

		int streetColumn = spec.findColumnIndex(street.getStringValue());
		int houseNumberColumn = spec.findColumnIndex(houseNumber.getStringValue());
		int cityColumn = spec.findColumnIndex(city.getStringValue());
		int districtColumn = spec.findColumnIndex(district.getStringValue());
		int stateColumn = spec.findColumnIndex(state.getStringValue());
		int countryColumn = spec.findColumnIndex(country.getStringValue());
		int postalCodeColumn = spec.findColumnIndex(postalCode.getStringValue());

		int index = 0;

		for (DataRow row : table) {
			DataCell[] cells = new DataCell[outSpec.getNumColumns()];

			for (int i = 0; i < spec.getNumColumns(); i++) {
				cells[outSpec.findColumnIndex(spec.getColumnNames()[i])] = row.getCell(i);
			}

			String street = null;
			String houseNumber = null;
			String city = null;
			String district = null;
			String state = null;
			String country = null;
			String postalCode = null;

			if (streetColumn != -1) {
				street = IO.getCleanString(row.getCell(streetColumn));
			}

			if (houseNumberColumn != -1) {
				houseNumber = IO.getCleanString(row.getCell(houseNumberColumn));
			}

			if (cityColumn != -1) {
				city = IO.getCleanString(row.getCell(cityColumn));
			}

			if (districtColumn != -1) {
				district = IO.getCleanString(row.getCell(districtColumn));
			}

			if (stateColumn != -1) {
				state = IO.getCleanString(row.getCell(stateColumn));
			}

			if (countryColumn != -1) {
				country = IO.getCleanString(row.getCell(countryColumn));
			}

			if (postalCodeColumn != -1) {
				postalCode = IO.getCleanString(row.getCell(postalCodeColumn));
			}

			cells[outSpec.findColumnIndex(TracingColumns.ADDRESS)] = IO.createCell(GisUtils
					.getAddress(street, houseNumber, city, district, state, country, postalCode));
			container.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index / (double) table.getRowCount());
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
		return new DataTableSpec[] { createOutSpec(inSpecs[0]) };
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
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

	private static DataTableSpec createOutSpec(DataTableSpec spec) throws InvalidSettingsException {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : spec) {
			if (column.getName().equals(TracingColumns.ADDRESS)) {
				throw new InvalidSettingsException("Column \"" + TracingColumns.ADDRESS
						+ "\" already exists");
			}

			columns.add(column);
		}

		columns.add(new DataColumnSpecCreator(TracingColumns.ADDRESS, StringCell.TYPE).createSpec());

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}
}
