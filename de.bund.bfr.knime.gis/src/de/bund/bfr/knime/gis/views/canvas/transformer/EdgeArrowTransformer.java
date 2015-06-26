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

import java.awt.BasicStroke;
import java.awt.Shape;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

public class EdgeArrowTransformer<V extends Node> implements Transformer<Context<Graph<V, Edge<V>>, Edge<V>>, Shape> {

	private EdgeStrokeTransformer<Edge<V>> strokeTransformer;

	public EdgeArrowTransformer(EdgeStrokeTransformer<Edge<V>> strokeTransformer) {
		this.strokeTransformer = strokeTransformer;
	}

	@Override
	public Shape transform(Context<Graph<V, Edge<V>>, Edge<V>> edge) {
		BasicStroke stroke = (BasicStroke) strokeTransformer.transform(edge.element);
		float thickness = stroke.getLineWidth();

		return ArrowFactory.getNotchedArrow(thickness + 8, 2 * thickness + 10, 4);
	}
}
