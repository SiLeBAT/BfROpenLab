/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.jung;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;

public class BetterEdgeShapeTransformer<V, E>
		implements Transformer<Context<Graph<V, E>, E>, Shape>, IndexedRendering<V, E> {

	private int fontSize;

	private EdgeIndexFunction<V, E> edgeIndexFunction;

	public BetterEdgeShapeTransformer(int fontSize) {
		this.fontSize = fontSize;
		edgeIndexFunction = null;
	}

	@Override
	public void setEdgeIndexFunction(EdgeIndexFunction<V, E> edgeIndexFunction) {
		this.edgeIndexFunction = edgeIndexFunction;
	}

	@Override
	public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
		return edgeIndexFunction;
	}

	@Override
	public Shape transform(Context<Graph<V, E>, E> context) {
		Pair<V> endpoints = context.graph.getEndpoints(context.element);
		int index = edgeIndexFunction != null ? edgeIndexFunction.getIndex(context.graph, context.element) : 1;

		if (endpoints != null && endpoints.getFirst().equals(endpoints.getSecond())) {
			float diam = 1.0f + index * fontSize / 40.0f;

			return new Ellipse2D.Float(-diam / 2.0f, -diam / 2.0f, diam, diam);
		} else {
			float controlY = 10.0f + 1.7f * fontSize * index;

			return new QuadCurve2D.Float(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f);
		}
	}
}
