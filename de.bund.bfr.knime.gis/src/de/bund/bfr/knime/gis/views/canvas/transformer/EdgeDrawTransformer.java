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
package de.bund.bfr.knime.gis.views.canvas.transformer;

import java.awt.Color;
import java.awt.Paint;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import edu.uci.ics.jung.visualization.RenderContext;

public class EdgeDrawTransformer<V extends Node> implements
		Transformer<Edge<V>, Paint> {

	private RenderContext<V, Edge<V>> renderContext;
	private Map<Edge<V>, Paint> edgeColors;

	public EdgeDrawTransformer(RenderContext<V, Edge<V>> viewer,
			Map<Edge<V>, List<Double>> alphaValues, List<Color> colors) {
		this.renderContext = viewer;
		edgeColors = new LinkedHashMap<>();

		for (Map.Entry<Edge<V>, List<Double>> entry : alphaValues.entrySet()) {
			edgeColors.put(entry.getKey(), CanvasUtils.mixColors(Color.BLACK,
					colors, entry.getValue()));
		}
	}

	@Override
	public Paint transform(Edge<V> edge) {
		if (renderContext.getPickedEdgeState().getPicked().contains(edge)) {
			return Color.GREEN;
		} else if (edgeColors.containsKey(edge)) {
			return edgeColors.get(edge);
		} else {
			return Color.BLACK;
		}
	}

}
