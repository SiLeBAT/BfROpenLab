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
import org.apache.commons.lang.StringUtils;

class SetVersionNumber {

	static ROOT = ".."

	static main(args) {
		println "version:"
		def version = new Scanner(System.in).nextLine()

		String SEMANTIC_VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+";
		String VERSION_EXTENTION_PATTERN = "[a-zA-Z0-9-]+"; //"k\\d+-\\d+";
		String EXTENDED_VERSION_PATTERN = "${SEMANTIC_VERSION_PATTERN}\\.${VERSION_EXTENTION_PATTERN}";
		
		String fullVersion = "";
			
		if (version.matches(SEMANTIC_VERSION_PATTERN)) {
			fullVersion = "${version}.qualifier";
		} else if (version.matches(EXTENDED_VERSION_PATTERN)) {
			fullVersion = "${version}qualifier"; //"${version}_qualifier";
		} else {
			throw new Exception("Invalid version. Please use either pattern <Major>.<Minor>.<Patch> or <Major>.<Minor>.<Patch>.<Qualifier prefix ([a-zA-Z0-9-]+)> e.g. 1.1.39 or 1.1.39.K4-1-");
		}
		
		String[] VERSION_PATTERNS = [
			"${SEMANTIC_VERSION_PATTERN}\\.${VERSION_EXTENTION_PATTERN}",  //"${SEMANTIC_VERSION_PATTERN}\\.${VERSION_EXTENTION_PATTERN}_",
			"${SEMANTIC_VERSION_PATTERN}\\."
		];
		
		for (String VERSION_PATTERN: VERSION_PATTERNS) {
			String VERSION_PATTERN_W_QT = "${VERSION_PATTERN}qualifier";
			String VERSION_PATTERN_W_QN = "${VERSION_PATTERN}\\d+";
		
		
			for (def d : new File(ROOT).listFiles())
				if (d.isDirectory() && (d.name.startsWith("de.bund.bfr") || d.name.startsWith("de.nrw"))) {
					def manifest = new File("${d.absolutePath}/META-INF/MANIFEST.MF")
					def feature = new File("${d.absolutePath}/feature.xml")
					def site = new File("${d.absolutePath}/site.xml")
					def category = new File("${d.absolutePath}/category.xml")
					
					if (manifest.exists()) {
						manifest.text = manifest.text
								//.replaceFirst(/Bundle-Version: [\d\.]+\.qualifier/, "Bundle-Version: ${fullVersion}")
							.replaceFirst("Bundle-Version: ${VERSION_PATTERN_W_QT}", "Bundle-Version: ${fullVersion}")
						// manifest.text = replaceFirst(manifest.text, "Bundle-Version: ${VERSION_PATTERN_W_QT}", "Bundle-Version: ${fullVersion}");
					}
	
					if (feature.exists())
						feature.text = feature.text
							//.replaceFirst(/version="[\d\.]+\.qualifier"/, "version=\"${fullVersion}\"")
							.replaceFirst("version=\"${VERSION_PATTERN_W_QT}\"", "version=\"${fullVersion}\"")
	
					if (site.exists())
						site.text = site.text
							//.replaceAll(/version="[\d\.]+\.qualifier"/, "version=\"${fullVersion}\"")
							.replaceAll("version=\"${VERSION_PATTERN_W_QT}\"", "version=\"${fullVersion}\"")
							//.replaceAll(/_[\d\.]+\.qualifier\.jar/, "_${fullVersion}.jar")
							.replaceAll("_${VERSION_PATTERN_W_QT}\\.jar", "_${fullVersion}.jar")
							// .replaceAll(/version="[\d\.]+\.\d\d+"/, "version=\"${fullVersion}\"")
							.replaceAll("version=\"${VERSION_PATTERN_W_QN}\"", "version=\"${fullVersion}\"")
							// .replaceAll(/_[\d\.]+\.\d\d+\.jar/, "_${fullVersion}.jar")
							.replaceAll("_${VERSION_PATTERN_W_QN}\\.jar", "_${fullVersion}.jar")
								
					if (category.exists())
						category.text = category.text
							// .replaceAll(/version="[\d\.]+\.qualifier"/, "version=\"${fullVersion}\"")
							.replaceAll("version=\"${VERSION_PATTERN_W_QT}\"", "version=\"${fullVersion}\"")
							// .replaceAll(/_[\d\.]+\.qualifier\.jar/, "_${fullVersion}.jar")
							.replaceAll("_${VERSION_PATTERN_W_QT}\\.jar", "_${fullVersion}.jar")
				}
		}
	}
}
