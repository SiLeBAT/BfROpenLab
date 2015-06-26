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
package de.bund.bfr.knime.gis.views.locationvisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import de.bund.bfr.knime.gis.views.LocationSettings.GisType;
import de.bund.bfr.knime.gis.views.ViewUtils;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationOsmCanvas;
import de.bund.bfr.knime.gis.views.canvas.Naming;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class LocationVisualizerCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private LocationVisualizerSettings set;

	public LocationVisualizerCanvasCreator(BufferedDataTable shapeTable, BufferedDataTable table,
			LocationVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = table;
		this.set = set;
	}

	public Canvas<LocationNode> createCanvas() throws InvalidSettingsException {
		Map<String, Class<?>> nodeProperties = ViewUtils.getTableColumns(nodeTable.getSpec());
		List<LocationNode> nodes = new ArrayList<>(ViewUtils.readLocationNodes(nodeTable, nodeProperties, null,
				set.getGisSettings().getNodeLatitudeColumn(), set.getGisSettings().getNodeLongitudeColumn()).values());
		String nodeIdProperty = ViewUtils.createNewIdProperty(nodes, nodeProperties);
		NodePropertySchema nodeSchema = new NodePropertySchema(nodeProperties, nodeIdProperty);

		nodeSchema.setLatitude(set.getGisSettings().getNodeLatitudeColumn());
		nodeSchema.setLongitude(set.getGisSettings().getNodeLongitudeColumn());

		Canvas<LocationNode> canvas;

		if (set.getGisSettings().getGisType() == GisType.SHAPEFILE) {
			List<RegionNode> regions = ViewUtils.readRegionNodes(shapeTable, set.getGisSettings().getShapeColumn());

			canvas = new LocationCanvas(nodes, nodeSchema, Naming.DEFAULT_NAMING, regions);
		} else {
			TileSource tileSource;

			switch (set.getGisSettings().getGisType()) {
			case MAPNIK:
				tileSource = new OsmTileSource.Mapnik();
				break;
			case CYCLE_MAP:
				tileSource = new OsmTileSource.CycleMap();
				break;
			case BING_AERIAL:
				tileSource = new BingAerialTileSource();
				break;
			case MAPQUEST:
				tileSource = new MapQuestOsmTileSource();
				break;
			case MAPQUEST_AERIAL:
				tileSource = new MapQuestOpenAerialTileSource();
				break;
			default:
				throw new IllegalArgumentException();
			}

			canvas = new LocationOsmCanvas(nodes, nodeSchema, Naming.DEFAULT_NAMING);
			((LocationOsmCanvas) canvas).setTileSource(tileSource);
		}

		set.getGisSettings().setToCanvas(canvas);

		return canvas;
	}

}
