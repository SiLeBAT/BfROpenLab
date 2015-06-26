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
package de.bund.bfr.knime.openkrise.views.gisgraphview;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;

public class GisGraphViewCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private GisGraphViewSettings set;

	public GisGraphViewCanvasCreator(BufferedDataTable shapeTable, BufferedDataTable nodeTable,
			BufferedDataTable edgeTable, GisGraphViewSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.set = set;
	}

	public GraphCanvas createGraphCanvas() throws NotConfigurableException {
		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(nodeTable.getSpec()),
				TracingColumns.ID);
		EdgePropertySchema edgeSchema = new EdgePropertySchema(TracingUtils.getTableColumns(edgeTable.getSpec()),
				TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO);

		nodeSchema.setLatitude(GeocodingNodeModel.LATITUDE_COLUMN);
		nodeSchema.setLongitude(GeocodingNodeModel.LONGITUDE_COLUMN);

		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes, new LinkedHashSet<RowKey>());
		GraphCanvas canvas = new GraphCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema,
				TracingUtils.NAMING, false);

		set.getGraphSettings().setToCanvas(canvas);

		return canvas;
	}

	public LocationCanvas createGisCanvas() throws NotConfigurableException {
		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(nodeTable.getSpec()),
				TracingColumns.ID);
		EdgePropertySchema edgeSchema = new EdgePropertySchema(TracingUtils.getTableColumns(edgeTable.getSpec()),
				TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO);

		nodeSchema.setLatitude(GeocodingNodeModel.LATITUDE_COLUMN);
		nodeSchema.setLongitude(GeocodingNodeModel.LONGITUDE_COLUMN);

		Map<String, LocationNode> nodes = TracingUtils.readLocationNodes(nodeTable, nodeSchema,
				new LinkedHashSet<RowKey>(), true);
		List<Edge<LocationNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes,
				new LinkedHashSet<RowKey>());
		List<RegionNode> regions = TracingUtils.readRegions(shapeTable, new LinkedHashSet<RowKey>());
		LocationCanvas canvas = new LocationCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema,
				TracingUtils.NAMING, regions);

		set.getGraphSettings().setToCanvas(canvas);
		set.getGisSettings().setToCanvas(canvas, false);

		return canvas;
	}
}
