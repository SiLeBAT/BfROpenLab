/*******************************************************************************
 * Copyright (c) 2014-2026 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.itest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Iterables;

public class LanguageDiffUtils {
	
	private static final String LANGUAGE_TAG_PATTERN = "(?<=_)(hu|de|en|es)(?=\\.txt$)";
	
	private static String getFileNameWithoutLanguage(String fileName) {
		return fileName.replaceAll(LANGUAGE_TAG_PATTERN, ""); 
	}
	
	
	private static List<List<String>> getLanguageVariants(List<String> filenames) {
		Map<String, List<String>> refToVariants = new HashMap<>();
		
		for (String name : filenames) {
			String refName = getFileNameWithoutLanguage(name);
			List<String> variants = refToVariants.get(refName);
			if (variants == null) {
				variants = new ArrayList<>();
				refToVariants.put(refName, variants);
			}
			variants.add(name);
		}
		return refToVariants.values().stream().collect(Collectors.toList());
		
	}
	
	private static List<String> extractExpectedLanguageInvariantLines(List<String> lines) {
		int index = Iterables.indexOf(lines, (line) -> line.equals(DBUtils.DB_CONTENT_END_TAG));
		if (index >= 0) {
			List<String> linesUntilDBEnd = lines.subList(0, index + 1);
			final Pattern sourceLinePattern = Pattern.compile(
				"^" + String.join("\\s*\\|\\s*", new String[]{"\\d+", "\"[a-z]+\"", "-?\\d+", "\"Source\"", "\"[^\\|\"]+\\.xlsx[^\\\\|\\\"]*\""}) + "$", 
				Pattern.CASE_INSENSITIVE
			);
			return linesUntilDBEnd.stream().filter(line ->  !sourceLinePattern.matcher(line).matches()).collect(Collectors.toList());
		}
		return lines;
	}
	
	private static List<String> removeTemplateFileNameReferences(List<String> lines, String txtoutFileName) {
		final String replacement = "####";
		final String templateName = txtoutFileName.replaceAll("\\.txt$", ".xlsx");
		return lines.stream().map(
				line -> line.replaceAll("(?<=\\|/)" + Pattern.quote(templateName) + "(?=\\.xlsx)", replacement)
		).collect(Collectors.toList());
	}
	
	static boolean compareLanguageVariants(String dir, String diffDir) {
		boolean result = true;
		try {
			File[] files = new File(dir).listFiles((f) -> f.isFile() && f.getName().endsWith(".txt"));
			Arrays.sort(files);
			
			List<List<String>> variantsList = getLanguageVariants(Stream.of(files).map(f -> f.getName()).collect(Collectors.toList()));
			
			for (List<String> variants : variantsList) {
				if (variants.size() > 1) {
					// more than one language variants
					String refName = variants.get(0);
					File refFile = new File(dir, refName);
					List<String> refLines = Files.readAllLines(refFile.toPath());    
					refLines = extractExpectedLanguageInvariantLines(refLines);
					refLines = removeTemplateFileNameReferences(refLines, refName);
				
					for (int i = 1; i < variants.size(); i++) {
						String fileName = variants.get(i);
						File file = new File(dir, fileName);
						List<String> lines = Files.readAllLines(file.toPath());    
						lines = extractExpectedLanguageInvariantLines(lines);
						lines = removeTemplateFileNameReferences(lines, fileName);
						
						boolean linesAreEqual = DiffUtils.areLinesEqual(refLines, lines);
						if (!linesAreEqual) {
							String mergeName = DiffUtils.getDiffFileName(refFile, file) + ".html";
							DiffUtils.writeDiffToHtml(refLines, lines, Paths.get(diffDir, mergeName).toString());
						}
						
						result = result && linesAreEqual;
					}
				}
			}
			
			return result;
		} catch (Exception ex) {
			System.err.println("Comparing and merging language specific out files failed.");
			ex.printStackTrace();
		}
		return false;
	}

}
