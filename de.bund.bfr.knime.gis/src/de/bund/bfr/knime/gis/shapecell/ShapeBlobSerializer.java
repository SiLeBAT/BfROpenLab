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

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

public class ShapeBlobSerializer implements DataCellSerializer<ShapeBlobCell> {

	@Override
	public void serialize(ShapeBlobCell cell, DataCellDataOutput output) throws IOException {
		byte[] bytes = new WKBWriter().write(cell.getShape());

		output.writeInt(bytes.length);
		output.write(bytes);
	}

	@Override
	public ShapeBlobCell deserialize(DataCellDataInput input) throws IOException {
		byte[] bytes = new byte[input.readInt()];

		input.readFully(bytes);

		try {
			return new ShapeBlobCell(new WKBReader().read(bytes));
		} catch (ParseException e) {
			throw new IOException(e.getMessage());
		}
	}

}
