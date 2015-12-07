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
package de.bund.bfr.knime.gis.shapecell;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellFactory;
import org.knime.core.data.DataType;

import com.vividsolutions.jts.geom.Geometry;

public class ShapeCellFactory implements DataCellFactory {

	private ShapeCellFactory() {
	}

	public static DataCell create(Geometry shape) {
		if (shape == null) {
			throw new NullPointerException("Shape must not be null");
		}

		return new ShapeBlobCell(shape);
	}

	@Override
	public DataType getDataType() {
		return ShapeBlobCell.TYPE;
	}
}
