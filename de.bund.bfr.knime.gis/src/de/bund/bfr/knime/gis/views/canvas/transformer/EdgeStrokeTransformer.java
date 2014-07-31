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
package de.bund.bfr.knime.gis.views.canvas.transformer;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

public class EdgeStrokeTransformer<E> implements Transformer<E, Stroke> {

	private Map<E, Double> thicknessValues;

	public EdgeStrokeTransformer(Map<E, Double> thicknessValues) {
		this.thicknessValues = thicknessValues;

		if (!thicknessValues.isEmpty()) {
			double max = Collections.max(thicknessValues.values());

			if (max != 0.0) {
				for (E edge : thicknessValues.keySet()) {
					thicknessValues.put(edge, thicknessValues.get(edge) / max);
				}
			}
		}
	}

	@Override
	public Stroke transform(E edge) {
		int thickness = (int) Math.round(thicknessValues.get(edge) * 10.0) + 1;

		return new BasicStroke(thickness);
	}

}
