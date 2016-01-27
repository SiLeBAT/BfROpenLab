/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.regionvisualizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.ViewUtils;
import de.bund.bfr.knime.gis.views.canvas.GisCanvas;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.RegionOsmCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;

public class RegionVisualizerCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private RegionVisualizerSettings set;

	private Set<String> nonExistingRegions;

	public RegionVisualizerCanvasCreator(BufferedDataTable shapeTable, BufferedDataTable nodeTable,
			RegionVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = nodeTable;
		this.set = set;

		nonExistingRegions = new LinkedHashSet<>();
	}

	public GisCanvas<RegionNode> createCanvas() throws NotConfigurableException {
		Map<String, MultiPolygon> polygonMap = ViewUtils.readPolygons(shapeTable, set.getGisSettings().getShapeColumn(),
				set.getGisSettings().getShapeRegionColumn());
		Map<String, Class<?>> nodeProperties = ViewUtils.getTableColumns(nodeTable.getSpec());
		List<RegionNode> nodes = new ArrayList<>(ViewUtils.readRegionNodes(nodeTable, nodeProperties, polygonMap, null,
				set.getGisSettings().getNodeRegionColumn(), nonExistingRegions).values());
		NodePropertySchema nodeSchema = new NodePropertySchema(nodeProperties,
				set.getGisSettings().getNodeRegionColumn());
		GisCanvas<RegionNode> canvas;

		if (set.getGisSettings().getGisType() == GisType.SHAPEFILE) {
			canvas = new RegionCanvas(nodes, nodeSchema, Naming.DEFAULT_NAMING);
		} else {
			canvas = new RegionOsmCanvas(nodes, nodeSchema, Naming.DEFAULT_NAMING);
			((RegionOsmCanvas) canvas).setTileSource(set.getGisSettings().getGisType().getTileSource());
		}

		set.getGisSettings().setToCanvas(canvas);

		return canvas;
	}

	public Set<String> getNonExistingRegions() {
		return nonExistingRegions;
	}
}
