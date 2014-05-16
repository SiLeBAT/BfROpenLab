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
package de.bund.bfr.knime.gis.views.locationvisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.ViewUtilities;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
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

	public LocationCanvas createCanvas() {
		List<RegionNode> regions = ViewUtilities.readRegionNodes(shapeTable,
				set.getShapeColumn());
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		List<LocationNode> nodes = new ArrayList<LocationNode>(ViewUtilities
				.readLocationNodes(nodeTable, nodeProperties, null,
						set.getLatitudeColumn(), set.getLongitudeColumn())
				.values());

		if (nodes.isEmpty()) {
			return null;
		}

		String nodeIdProperty = ViewUtilities.createNewIdProperty(nodes,
				nodeProperties);
		LocationCanvas canvas = new LocationCanvas(nodes, nodeProperties,
				nodeIdProperty, regions);

		canvas.setShowLegend(set.isShowLegend());
		canvas.setCanvasSize(set.getCanvasSize());
		canvas.setTextSize(set.getTextSize());
		canvas.setBorderAlpha(set.getBorderAlpha());
		canvas.setNodeSize(set.getNodeSize());
		canvas.setNodeHighlightConditions(set.getNodeHighlightConditions());

		if (!Double.isNaN(set.getScaleX()) && !Double.isNaN(set.getScaleY())
				&& !Double.isNaN(set.getTranslationX())
				&& !Double.isNaN(set.getTranslationY())) {
			canvas.setTransform(set.getScaleX(), set.getScaleY(),
					set.getTranslationX(), set.getTranslationY());
		}

		return canvas;
	}

}
