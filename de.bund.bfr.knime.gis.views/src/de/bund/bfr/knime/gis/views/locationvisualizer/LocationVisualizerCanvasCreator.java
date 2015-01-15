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
package de.bund.bfr.knime.gis.views.locationvisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.gis.views.ViewUtils;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.Naming;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class LocationVisualizerCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private LocationVisualizerSettings set;

	public LocationVisualizerCanvasCreator(BufferedDataTable shapeTable,
			BufferedDataTable table, LocationVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = table;
		this.set = set;
	}

	public LocationCanvas createCanvas() throws InvalidSettingsException {
		List<RegionNode> regions = ViewUtils.readRegionNodes(shapeTable, set
				.getGisSettings().getShapeColumn());
		Map<String, Class<?>> nodeProperties = ViewUtils
				.getTableColumns(nodeTable.getSpec());
		List<LocationNode> nodes = new ArrayList<>(ViewUtils.readLocationNodes(
				nodeTable, nodeProperties, null,
				set.getGisSettings().getNodeLatitudeColumn(),
				set.getGisSettings().getNodeLongitudeColumn()).values());
		String nodeIdProperty = ViewUtils.createNewIdProperty(nodes,
				nodeProperties);
		NodePropertySchema nodeSchema = new NodePropertySchema(nodeProperties,
				nodeIdProperty);

		nodeSchema.setLatitude(set.getGisSettings().getNodeLatitudeColumn());
		nodeSchema.setLongitude(set.getGisSettings().getNodeLongitudeColumn());

		LocationCanvas canvas = new LocationCanvas(nodes, nodeSchema,
				Naming.DEFAULT_NAMING, regions);

		set.getGisSettings().setToCanvas(canvas);

		return canvas;
	}

}
