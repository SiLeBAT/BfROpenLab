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
package de.bund.bfr.knime.update.p2.deploy

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class FixContentJar {

	static String CATEGORY = "bfropenlab"
	static String UPDATE_SITE = "../de.bund.bfr.knime.update.p2"

	static String CONTENT_JAR = "content.jar"
	static String CONTENT_XML = "content.xml"

	static main(args) {
		def contentJar = new File("${UPDATE_SITE}/${CONTENT_JAR}")
		def zip = new ZipFile(contentJar)
		def root = new XmlParser().parse(zip.getInputStream(zip.getEntry(CONTENT_XML)))
		def unit = root.units.unit.findAll{ it.@id == CATEGORY }.get(0)

		unit.requires.required.each { s ->
			s.@range = "0.0.0"
		}

		def out = new ZipOutputStream(new FileOutputStream(contentJar))

		out.putNextEntry(new ZipEntry(CONTENT_XML))
		new XmlNodePrinter(new PrintWriter(out)).print(root)
		out.closeEntry()
		out.close()
	}
}
