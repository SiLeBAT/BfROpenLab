/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.shapeproperties;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;

public class ShapePropertiesSettings extends NodeSettings {

	private static final String CFG_SHAPE_COLUMN = "ShapeColumn";

	private String shapeColumn;

	public ShapePropertiesSettings() {
		shapeColumn = null;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			shapeColumn = settings.getString(CFG_SHAPE_COLUMN);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_SHAPE_COLUMN, shapeColumn);
	}

	public String getShapeColumn() {
		return shapeColumn;
	}

	public void setShapeColumn(String shapeColumn) {
		this.shapeColumn = shapeColumn;
	}
}
