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
package de.bund.bfr.knime.esri.polygonproperties;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "PolygonProperties" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class PolygonPropertiesNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the PolygonProperties node.
	 */
	@SuppressWarnings("unchecked")
	protected PolygonPropertiesNodeDialog() {
		DialogComponentColumnNameSelection polygonComponent = new DialogComponentColumnNameSelection(
				new SettingsModelString(
						PolygonPropertiesNodeModel.CFG_POLYGON_COLUMN, null),
				"Polygon Column", 0, ListDataValue.class);
		DialogComponentColumnNameSelection latitudeComponent = new DialogComponentColumnNameSelection(
				new SettingsModelString(
						PolygonPropertiesNodeModel.CFG_LATITUDE_COLUMN, null),
				"Latitude Column", 1, DoubleValue.class);
		DialogComponentColumnNameSelection longitudeComponent = new DialogComponentColumnNameSelection(
				new SettingsModelString(
						PolygonPropertiesNodeModel.CFG_LONGITUDE_COLUMN, null),
				"Longitude Column", 1, DoubleValue.class);

		addDialogComponent(polygonComponent);
		addDialogComponent(latitudeComponent);
		addDialogComponent(longitudeComponent);
	}
}
