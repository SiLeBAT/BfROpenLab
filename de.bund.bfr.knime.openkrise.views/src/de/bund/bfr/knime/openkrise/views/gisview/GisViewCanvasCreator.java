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
package de.bund.bfr.knime.openkrise.views.gisview;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;

public class GisViewCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private GisViewSettings set;

	public GisViewCanvasCreator(BufferedDataTable shapeTable,
			BufferedDataTable table, GisViewSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = table;
		this.set = set;
	}

	public LocationCanvas createCanvas() throws NotConfigurableException {
		NodePropertySchema nodeSchema = new NodePropertySchema(
				TracingUtils.getTableColumns(nodeTable.getSpec()),
				TracingColumns.ID);

		nodeSchema.setLatitude(GeocodingNodeModel.LATITUDE_COLUMN);
		nodeSchema.setLongitude(GeocodingNodeModel.LONGITUDE_COLUMN);

		List<LocationNode> nodes = new ArrayList<>(TracingUtils
				.readLocationNodes(nodeTable, nodeSchema,
						new LinkedHashSet<RowKey>()).values());
		List<RegionNode> regions = TracingUtils.readRegions(shapeTable,
				new LinkedHashSet<RowKey>());
		LocationCanvas canvas = new LocationCanvas(nodes, nodeSchema,
				TracingUtils.NAMING, regions);

		set.getGisSettings().setToCanvas(canvas, true);

		return canvas;
	}

}
