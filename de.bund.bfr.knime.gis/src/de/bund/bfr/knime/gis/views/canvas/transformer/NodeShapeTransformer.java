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
package de.bund.bfr.knime.gis.views.canvas.transformer;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

public class NodeShapeTransformer<V> implements Transformer<V, Shape> {

	private int size;
	private Map<V, Double> thicknessValues;

	public NodeShapeTransformer(int size, Map<V, Double> thicknessValues) {
		this.size = size;
		this.thicknessValues = thicknessValues;

		if (!thicknessValues.isEmpty()) {
			double max = Collections.max(thicknessValues.values());

			if (max != 0.0) {
				for (V node : thicknessValues.keySet()) {
					thicknessValues.put(node, thicknessValues.get(node) / max);
				}
			}
		}
	}

	@Override
	public Shape transform(V n) {
		Double factor = thicknessValues.get(n);

		if (factor == null) {
			factor = 0.0;
		}

		int size = (int) (this.size * (1 + factor));

		return new Ellipse2D.Double(-size / 2, -size / 2, size, size);
	}
}
