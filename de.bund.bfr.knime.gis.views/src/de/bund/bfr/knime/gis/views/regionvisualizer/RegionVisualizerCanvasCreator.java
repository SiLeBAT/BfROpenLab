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
package de.bund.bfr.knime.gis.views.regionvisualizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;

import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.ViewUtilities;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class RegionVisualizerCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private RegionVisualizerSettings set;

	private Set<String> nonExistingRegions;

	public RegionVisualizerCanvasCreator(BufferedDataTable shapeTable,
			BufferedDataTable nodeTable, RegionVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = nodeTable;
		this.set = set;

		nonExistingRegions = new LinkedHashSet<String>();
	}

	public RegionCanvas createGISCanvas() {
		Map<String, MultiPolygon> polygonMap = ViewUtilities.readPolygons(
				shapeTable, set.getShapeColumn(), set.getShapeRegionColumn());
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		List<RegionNode> nodes = new ArrayList<RegionNode>(ViewUtilities
				.readRegionNodes(nodeTable, nodeProperties, polygonMap, null,
						null, set.getNodeRegionColumn(), false,
						nonExistingRegions).values());

		if (nodes.isEmpty()) {
			return null;
		}

		RegionCanvas canvas = new RegionCanvas(nodes, nodeProperties,
				set.getNodeRegionColumn());

		canvas.setCanvasSize(set.getCanvasSize());
		canvas.setBorderAlpha(set.getBorderAlpha());
		canvas.setNodeHighlightConditions(set.getNodeHighlightConditions());

		if (!Double.isNaN(set.getScaleX()) && !Double.isNaN(set.getScaleY())
				&& !Double.isNaN(set.getTranslationX())
				&& !Double.isNaN(set.getTranslationY())) {
			canvas.setTransform(set.getScaleX(), set.getScaleY(),
					set.getTranslationX(), set.getTranslationY());
		}

		return canvas;
	}

	public Set<String> getNonExistingRegions() {
		return nonExistingRegions;
	}
}
