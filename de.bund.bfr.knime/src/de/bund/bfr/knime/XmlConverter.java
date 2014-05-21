/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.input.ReaderInputStream;

public class XmlConverter {

	private ClassLoader loader;

	public XmlConverter(ClassLoader loader) {
		this.loader = loader;
	}

	public String toXml(Object obj) {
		if (obj == null) {
			return null;
		}
		
		Thread currentThread = Thread.currentThread();
		ClassLoader currentLoader = Thread.currentThread()
				.getContextClassLoader();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(out);

		currentThread.setContextClassLoader(loader);
		encoder.writeObject(obj);
		encoder.close();
		currentThread.setContextClassLoader(currentLoader);

		try {
			return out.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object fromXml(String s) {
		if (s == null) {
			return null;
		}
		
		Thread currentThread = Thread.currentThread();
		ClassLoader currentLoader = Thread.currentThread()
				.getContextClassLoader();
		StringReader in = new StringReader(s);
		XMLDecoder decoder = new XMLDecoder(new ReaderInputStream(in));
		Object obj;

		currentThread.setContextClassLoader(loader);
		obj = decoder.readObject();
		decoder.close();
		currentThread.setContextClassLoader(currentLoader);

		return obj;
	}
}
