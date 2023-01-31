/*******************************************************************************
 * Copyright (c) 2014-2023 Federal Institute for Risk Assessment (BfR), Germany
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

class RemoveFeatureFromContentJar {

	static CATEGORY = "bfropenlab"
	static UPDATE_SITE = "../de.bund.bfr.knime.update.p2"
	static FEATURE = "de.bund.bfr.knime.sbml.feature.feature.group"

	static CONTENT_JAR = "content.jar"
	static CONTENT_XML = "content.xml"

	static main(args) {
		def contentJar = new File("${UPDATE_SITE}/${CONTENT_JAR}")
		def zip = new ZipFile(contentJar)
		def declarations = []
		
		zip.getInputStream(zip.getEntry(CONTENT_XML)).eachLine {
			if (it.startsWith("<?")) {
				declarations.add(it)
			}
		}
		
		def root = new XmlParser().parse(zip.getInputStream(zip.getEntry(CONTENT_XML)))
		def features = root.units.unit.findAll{ it.@id == FEATURE }
		
		features.each { it.parent().remove(it) }
		root.units.@size = root.units.unit.size()
		
		def category = root.units.unit.find{ it.@id == CATEGORY }
		def featureInCategory = category.requires.required.find{ it.@name == FEATURE }
		
		featureInCategory.parent().remove(featureInCategory)
		category.requires.@size = category.requires.required.size()

		def out = new ZipOutputStream(new FileOutputStream(contentJar))

		out.putNextEntry(new ZipEntry(CONTENT_XML))
		declarations.each { out << it + "\n" }
		new XmlNodePrinter(new PrintWriter(out)).print(root)
		out.closeEntry()
		out.close()
	}
}
