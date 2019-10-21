/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.canvas.highlighting;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;

import de.bund.bfr.jung.NamedShape;
import de.bund.bfr.knime.gis.views.canvas.element.Element;

public interface HighlightCondition {

	String getName();

	boolean isShowInLegend();

	Color getColor();

	boolean isInvisible();

	boolean isUseThickness();

	String getLabelProperty();

	NamedShape getShape();

	<T extends Element> Map<T, Double> getValues(Collection<? extends T> elements);

	Point2D getValueRange(Collection<? extends Element> elements);

	HighlightCondition copy();
}
