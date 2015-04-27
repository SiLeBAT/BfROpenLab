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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;

public class NodeShapeTransformer<V> implements Transformer<V, Shape> {

	private int size;
	private Map<V, Double> thicknessValues;
	private double denom;

	public NodeShapeTransformer(int size) {
		this(size, new LinkedHashMap<V, Double>());
	}

	public NodeShapeTransformer(int size, Map<V, Double> thicknessValues) {
		this.size = size;
		this.thicknessValues = thicknessValues;
		denom = CanvasUtils.getDenominator(thicknessValues.values());
	}

	@Override
	public Shape transform(V n) {
		Double factor = thicknessValues.get(n);

		if (factor == null) {
			factor = 0.0;
		}

		int size = (int) (this.size * (1 + factor / denom));

		return new Ellipse2D.Double(-size / 2, -size / 2, size, size);
	}
}
