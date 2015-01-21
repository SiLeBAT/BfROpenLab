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

import java.awt.Font;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class FontTransformer<T extends Element> implements Transformer<T, Font> {

	private Font font;

	public FontTransformer(int size, boolean bold) {
		font = new Font("default", bold ? Font.BOLD : Font.PLAIN, size);
	}

	@Override
	public Font transform(T element) {
		return font;
	}
}
