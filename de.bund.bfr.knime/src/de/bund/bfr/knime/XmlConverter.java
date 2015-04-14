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
package de.bund.bfr.knime;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.xml.sax.InputSource;

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
		ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(out, StandardCharsets.UTF_8.name(), true, 0);

		currentThread.setContextClassLoader(loader);
		encoder.writeObject(obj);
		encoder.close();
		currentThread.setContextClassLoader(currentLoader);

		try {
			return out.toString(StandardCharsets.UTF_8.name());
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
		ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
		XMLDecoder decoder = new XMLDecoder(new InputSource(new StringReader(s)));
		Object obj;

		currentThread.setContextClassLoader(loader);
		obj = decoder.readObject();
		decoder.close();
		currentThread.setContextClassLoader(currentLoader);

		return obj;
	}
}
