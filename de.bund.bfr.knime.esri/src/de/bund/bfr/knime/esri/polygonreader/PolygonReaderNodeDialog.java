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
package de.bund.bfr.knime.esri.polygonreader;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "PolygonReader" Node.
 * 
 * @author Christian Thoens
 */
public class PolygonReaderNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the PolygonReader node.
	 */
	protected PolygonReaderNodeDialog() {
		DialogComponentFileChooser shpDialog = new DialogComponentFileChooser(
				new SettingsModelString(PolygonReaderNodeModel.SHP_FILE, null),
				"ShpFileHistory", ".shp");
		DialogComponentOptionalString rowIdDialog = new DialogComponentOptionalString(
				new SettingsModelOptionalString(
						PolygonReaderNodeModel.ROW_ID_PREFIX, null, false),
				"Row ID Prefix");
		DialogComponentBoolean exteriorDialog = new DialogComponentBoolean(
				new SettingsModelBoolean(
						PolygonReaderNodeModel.GET_EXTERIOR_POLYGON, false),
				"Get Exterior Ring of Polygons");

		shpDialog.setBorderTitle("SHP File");
		addDialogComponent(shpDialog);
		addDialogComponent(rowIdDialog);
		addDialogComponent(exteriorDialog);
	}
}
