/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
import org.knime.core.data.DataType;
import org.knime.core.data.container.BlobDataCell;

import org.locationtech.jts.geom.Geometry;

public class ShapeBlobCell extends BlobDataCell implements ShapeValue {

	public static final DataType TYPE = DataType.getType(ShapeBlobCell.class);

	private static final long serialVersionUID = 1L;

	private Geometry shape;

	public ShapeBlobCell(Geometry shape) {
		this.shape = shape;
	}

	@Override
	public String toString() {
		return shape.getClass().getSimpleName();
	}

	@Override
	protected boolean equalsDataCell(DataCell dc) {
		if (dc instanceof ShapeBlobCell) {
			return shape.equals(((ShapeBlobCell) dc).getShape());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return shape.hashCode();
	}

	@Override
	public Geometry getShape() {
		return shape;
	}
}
