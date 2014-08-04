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
package de.bund.bfr.knime.gis.shapecell;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

public class ShapeBlobSerializer implements DataCellSerializer<ShapeBlobCell> {

	@Override
	public void serialize(ShapeBlobCell cell, DataCellDataOutput output)
			throws IOException {
		try {
			WKBWriter writer = new WKBWriter();
			byte[] bytes = writer.write(cell.getShape());

			output.writeInt(bytes.length);
			output.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ShapeBlobCell deserialize(DataCellDataInput input)
			throws IOException {
		try {
			byte[] bytes = new byte[input.readInt()];

			input.readFully(bytes);

			WKBReader reader = new WKBReader();

			return new ShapeBlobCell(reader.read(bytes));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
