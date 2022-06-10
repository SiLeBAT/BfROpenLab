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

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "AddressCreator" Node.
 * 
 * @author Christian Thoens
 */
public class AddressCreatorNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the AddressCreator node.
	 */
	@SuppressWarnings("unchecked")
	protected AddressCreatorNodeDialog() {
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(AddressCreatorNodeModel.CFG_STREET, null), "Street Column", 0, false, true,
				StringValue.class));
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(AddressCreatorNodeModel.CFG_HOUSE_NUMBER, null), "House Number Column", 0,
				false, true, StringValue.class));
		addDialogComponent(
				new DialogComponentColumnNameSelection(new SettingsModelString(AddressCreatorNodeModel.CFG_CITY, null),
						"City Column", 0, false, true, StringValue.class));
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(AddressCreatorNodeModel.CFG_DISTRICT, null), "District Column", 0, false, true,
				StringValue.class));
		addDialogComponent(
				new DialogComponentColumnNameSelection(new SettingsModelString(AddressCreatorNodeModel.CFG_STATE, null),
						"State Column", 0, false, true, StringValue.class));
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(AddressCreatorNodeModel.CFG_COUNTRY, null), "Country Column", 0, false, true,
				StringValue.class));
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(AddressCreatorNodeModel.CFG_POSTAL_CODE, null), "Postal Code Column", 0, false,
				true, StringValue.class));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(AddressCreatorNodeModel.CFG_HOUSE_NUMBER_AFTER_STREET, true),
				"House Number after Street Name"));
	}
}
