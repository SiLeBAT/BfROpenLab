/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

class SetVersionNumber {

	static String ROOT = ".."
	static String VERSION = "1.1.2"

	static main(args) {
		for (File d : new File(ROOT).listFiles())
			if (d.isDirectory() && d.name.startsWith("de.bund.bfr")) {
				def manifest = new File("${d.absolutePath}/META-INF/MANIFEST.MF")
				def feature = new File("${d.absolutePath}/feature.xml")
				def site = new File("${d.absolutePath}/site.xml")

				if (manifest.exists()) {
					manifest.text = manifest.text
						.replaceFirst(/Bundle-Version: [\d\.]+\.qualifier/, "Bundle-Version: ${VERSION}.qualifier")
				}

				if (feature.exists())
					feature.text = feature.text
						.replaceFirst(/version="[\d\.]+\.qualifier"/, "version=\"${VERSION}.qualifier\"")
					
				if (site.exists()) {
					site.text = site.text
						.replaceAll(/version="[\d\.]+\.qualifier"/, "version=\"${VERSION}.qualifier\"")
						.replaceAll(/_[\d\.]+\.qualifier\.jar/, "_${VERSION}.qualifier.jar")
						.replaceAll(/version="[\d\.]+\.\d\d+"/, "version=\"${VERSION}.qualifier\"")
						.replaceAll(/_[\d\.]+\.\d\d+\.jar/, "_${VERSION}.qualifier.jar")
				}
			}
	}
}
