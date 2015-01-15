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
package de.bund.bfr.knime.update.p2.deploy

class FixContentJar {

	static String CATEGORY = "test";
	static String UPDATE_SITE = "../de.bund.bfr.knime.update.p2"

	static main(args) {
		def root = new XmlParser().parse("${UPDATE_SITE}/content.xml");
		def unit = root.units.unit.findAll{ it.@id == CATEGORY }.get(0)

		unit.requires.required.each { s ->
			s.@range = "0.0.0"
		}

		def writer = new StringWriter()
		new XmlNodePrinter(new PrintWriter(writer)).print(unit)
		def result = writer.toString()

		println result
	}
}
