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
package de.bund.bfr.knime.gis.views.canvas.highlighting;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public interface HighlightCondition {

	public abstract String getName();

	public abstract boolean isShowInLegend();

	public abstract Color getColor();

	public abstract boolean isInvisible();

	public abstract boolean isUseThickness();

	public abstract String getLabelProperty();

	public abstract <T extends Element> Map<T, Double> getValues(
			Collection<T> elements);

	public abstract Point2D getValueRange(Collection<? extends Element> elements);
}
