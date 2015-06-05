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
import java.awt.Stroke;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

public class EdgeStrokeTransformer<E> implements Transformer<E, Stroke> {

	private int thickness;
	private Integer maxThickness;
	private Map<E, Double> thicknessValues;
	private double denom;

	public EdgeStrokeTransformer(int thickness, Integer maxThickness, Map<E, Double> thicknessValues) {
		this.thickness = thickness;
		this.maxThickness = maxThickness;
		this.thicknessValues = thicknessValues;
		denom = TransformerUtils.getDenominator(thicknessValues.values());
	}

	@Override
	public Stroke transform(E edge) {
		int max = maxThickness != null ? maxThickness : thickness + 10;
		Double factor = thicknessValues.get(edge);

		if (factor == null) {
			factor = 0.0;
		}

		return new BasicStroke((float) (thickness + (max - thickness) * factor / denom));
	}
}
