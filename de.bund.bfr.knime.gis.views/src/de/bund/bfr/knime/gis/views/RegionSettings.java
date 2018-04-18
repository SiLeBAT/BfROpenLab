/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class RegionSettings extends GisSettings {

	private static final String CFG_SHAPE_REGION_COLUMN = "ShapeRegionColumn";
	private static final String CFG_NODE_REGION_COLUMN = "NodeRegionColumn";

	private String shapeRegionColumn;
	private String nodeRegionColumn;

	public RegionSettings() {
		shapeRegionColumn = null;
		nodeRegionColumn = null;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			shapeRegionColumn = settings.getString(CFG_SHAPE_REGION_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeRegionColumn = settings.getString(CFG_NODE_REGION_COLUMN);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		settings.addString(CFG_SHAPE_REGION_COLUMN, shapeRegionColumn);
		settings.addString(CFG_NODE_REGION_COLUMN, nodeRegionColumn);
	}

	public String getShapeRegionColumn() {
		return shapeRegionColumn;
	}

	public void setShapeRegionColumn(String shapeRegionColumn) {
		this.shapeRegionColumn = shapeRegionColumn;
	}

	public String getNodeRegionColumn() {
		return nodeRegionColumn;
	}

	public void setNodeRegionColumn(String nodeRegionColumn) {
		this.nodeRegionColumn = nodeRegionColumn;
	}
}
