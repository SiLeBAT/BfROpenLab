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
package de.bund.bfr.knime.openkrise.views.gisview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;

import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.openkrise.TracingConstants;
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

	public LocationCanvas createCanvas() {
		List<RegionNode> regions = TracingUtils.readRegionNodes(shapeTable);
		Map<String, Class<?>> nodeProperties = TracingUtils
				.getTableColumns(nodeTable.getSpec());
		List<LocationNode> nodes = new ArrayList<>(TracingUtils
				.readLocationNodes(nodeTable, nodeProperties).values());

		if (nodes.isEmpty()) {
			return null;
		}

		LocationCanvas canvas = new LocationCanvas(nodes, nodeProperties,
				TracingConstants.ID_COLUMN, regions);

		canvas.setNodeName(TracingConstants.NODE_NAME);
		canvas.setEdgeName(TracingConstants.EDGE_NAME);
		canvas.setNodesName(TracingConstants.NODES_NAME);
		canvas.setEdgesName(TracingConstants.EDGES_NAME);
		canvas.setShowLegend(set.getGisSettings().isShowLegend());
		canvas.setCanvasSize(set.getGisSettings().getCanvasSize());
		canvas.setFontSize(set.getGisSettings().getFontSize());
		canvas.setFontBold(set.getGisSettings().isFontBold());
		canvas.setBorderAlpha(set.getGisSettings().getBorderAlpha());
		canvas.setEditingMode(set.getGisSettings().getEditingMode());
		canvas.setNodeSize(set.getGisSettings().getNodeSize());
		canvas.setNodeHighlightConditions(TracingUtils.renameColumns(set
				.getGisSettings().getNodeHighlightConditions(), nodeProperties));

		if (!Double.isNaN(set.getGisSettings().getScaleX())
				&& !Double.isNaN(set.getGisSettings().getScaleY())
				&& !Double.isNaN(set.getGisSettings().getTranslationX())
				&& !Double.isNaN(set.getGisSettings().getTranslationY())) {
			canvas.setTransform(set.getGisSettings().getScaleX(), set
					.getGisSettings().getScaleY(), set.getGisSettings()
					.getTranslationX(), set.getGisSettings().getTranslationY());
		}

		return canvas;
	}

}
