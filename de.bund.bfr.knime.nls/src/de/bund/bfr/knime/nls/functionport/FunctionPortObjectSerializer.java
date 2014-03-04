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
package de.bund.bfr.knime.nls.functionport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

public class FunctionPortObjectSerializer extends
		PortObjectSerializer<FunctionPortObject> {

	@Override
	public void savePortObject(FunctionPortObject portObject,
			PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		ObjectOutputStream objectOut = new ObjectOutputStream(out);

		objectOut.writeObject(portObject);
		objectOut.close();
	}

	@Override
	public FunctionPortObject loadPortObject(PortObjectZipInputStream in,
			PortObjectSpec spec, ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		ObjectInputStream objectIn = new ObjectInputStream(in);
		FunctionPortObject portObject;

		try {
			portObject = (FunctionPortObject) objectIn.readObject();
		} catch (ClassNotFoundException e) {
			portObject = null;
			e.printStackTrace();
		}

		objectIn.close();

		return portObject;
	}
}
