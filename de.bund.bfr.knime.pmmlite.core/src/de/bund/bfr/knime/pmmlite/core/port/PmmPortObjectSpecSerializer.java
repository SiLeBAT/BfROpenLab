/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.core.port;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.knime.core.node.port.PortObjectSpec.PortObjectSpecSerializer;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

public class PmmPortObjectSpecSerializer extends PortObjectSpecSerializer<PmmPortObjectSpec> {

	@Override
	public void savePortObjectSpec(PmmPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
			throws IOException {
		try (ObjectOutputStream objectOut = new ObjectOutputStream(out)) {
			objectOut.writeObject(portObjectSpec);
		}
	}

	@Override
	public PmmPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
		try (ObjectInputStream objectIn = new ObjectInputStream(in)) {
			return (PmmPortObjectSpec) objectIn.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		}
	}
}
