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

public class NodeShapeTransformer<V> implements Transformer<V, Shape> {

	private int size;
	private Integer maxSize;
	private Map<V, Double> thicknessValues;
	private double denom;

	public NodeShapeTransformer(int size, Integer maxSize) {
		this(size, maxSize, new LinkedHashMap<>(0));
	}

	public NodeShapeTransformer(int size, Integer maxSize, Map<V, Double> thicknessValues) {
		this.size = size;
		this.maxSize = maxSize;
		this.thicknessValues = thicknessValues;
		denom = TransformerUtils.getDenominator(thicknessValues.values());
	}

	@Override
	public Shape transform(V n) {
		int max = maxSize != null ? maxSize : size * 2;
		Double factor = thicknessValues.get(n);

		if (factor == null) {
			factor = 0.0;
		}

		double s = size + (max - size) * factor / denom;

		return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
	}
}
