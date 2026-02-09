/*******************************************************************************
 * Copyright (c) 2014-2025 German Federal Institute for Risk Assessment (BfR)
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.StringEscapeUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;

public class DiffUtils {
	
	private final static String CSS_CONTENT = """
			table {
			  font-family: monospace, monospace;
			  font-size: 10pt;
			  margin: 10px auto;
			  border-collapse: collapse;
			  border-radius: 3pt;
			}

			th,
			td {
			  padding: 0.6em;
			  vertical-align: top;
			}
			
			tr :nth-child(1),
			tr :nth-child(2) {
			  text-align: center;
			}
			
			tr.changed {
				:nth-child(1),
				:nth-child(2) {
					background-color: #F0E68C;
				}
				
				:nth-child(3),
				:nth-child(4) {
					background-color: #F7F1C0;
				}
			}

			tr.insert {
				:nth-child(1),
				:nth-child(2) {
					background-color: #90EE90;
				}
				
				:nth-child(3),
				:nth-child(4) {
					background-color: #C1F5C1;
					
					span {
						background-color: #90EE90;
					}
				}
			}

			tr.delete {
				:nth-child(1),
				:nth-child(2) {
					background-color: #F88379;
				}
				
				:nth-child(3),
				:nth-child(4) {
					background-color: #FBC1BB;
					
					span {
						background-color: #F88379;
					}
				}
			}
		  """;
	
	private static String getLineChangeHtml(String line, List<Chunk<String>> chunks) {
		int lastLinePos = 0;
		StringBuilder result = new StringBuilder();
		
		for (Chunk<String> chunk : chunks) {
			int changePos = chunk.getPosition();
			result.append(StringEscapeUtils.escapeHtml4(line.substring(lastLinePos, changePos)));
			lastLinePos = changePos;
			
			if (!chunk.getLines().isEmpty()) {
				result.append("<span>" + StringEscapeUtils.escapeHtml4(chunk.getLines().get(0)) + "</span>");
				lastLinePos += chunk.getLines().get(0).length();
			}
		}
		
		result.append(StringEscapeUtils.escapeHtml4(line.substring(lastLinePos)));
		
		return result.toString();
	}
	
	private static void writeLineToHtml(BufferedWriter writer, String line, Integer sourceLineIndex, Integer targetLineIndex) throws IOException {
		String rowClass = sourceLineIndex == null ? "insert" : targetLineIndex == null ? "delete" : "unchange";
		String sign = sourceLineIndex == null ? "+" : targetLineIndex == null ? "-" : "&nbsp;";
		writer.write("<tr class=\"diff-line-row " + rowClass + "\">");
    	writer.write("<td>" + (sourceLineIndex == null ? "" : sourceLineIndex + 1) + "</td>");
    	writer.write("<td>" + (targetLineIndex == null ? "" : targetLineIndex + 1) + "</td>");
    	writer.write("<td>" + sign + "</td>");
    	writer.write("<td>" + line + "</td>");
    	writer.write("</tr>\n");
	}
	
	static void writeDiffToHtml(List<String> lines1, List<String> lines2, String path) throws Exception {
		File file = new File(path);
		File dir = new File(file.getParent());
		if (!dir.exists()) if (!dir.mkdirs()) throw new Exception("Could not create path \"" + path + "\"");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		Patch<String> patch = com.github.difflib.DiffUtils.diff(lines1, lines2);
		
		writer.write("<html><head><style>\n" + CSS_CONTENT + "</style></head><body>");
		writer.write("<table><tbody>"); 
		int iLastProcSourceLineIndex = -1;
		int iLastProcTargetLineIndex = -1;
		
        for (AbstractDelta<String> delta : patch.getDeltas()) {
        	Chunk<String> sourceChunk = delta.getSource();
        	Chunk<String> targetChunk = delta.getTarget();
        	int sourceLineIndex = sourceChunk.getPosition();
        	int targetLineIndex = targetChunk.getPosition();
        	
        	if (sourceChunk.getPosition() > iLastProcSourceLineIndex + 1) {
        		// print unchanged lines
            	int nUnchangedRows = sourceLineIndex - iLastProcSourceLineIndex - 1;
            	for (int i = 1; i <= nUnchangedRows; i++) {
            		iLastProcSourceLineIndex++;
            		iLastProcTargetLineIndex++;
            		writeLineToHtml(writer, StringEscapeUtils.escapeHtml4(lines1.get(iLastProcSourceLineIndex)), iLastProcSourceLineIndex, iLastProcTargetLineIndex);
            	}
            }
        	
        	if (
        			delta.getType() == DeltaType.CHANGE && 
        			sourceChunk.getLines().size() == 1 && 
        			targetChunk.getLines().size() == 1
        		) {
        		String sourceLine = sourceChunk.getLines().get(0);
        		String targetLine = targetChunk.getLines().get(0);
        		Patch<String> linePatch = com.github.difflib.DiffUtils.diffInline(sourceLine, targetLine);
        		iLastProcSourceLineIndex++;
        		writeLineToHtml(
        				writer, 
        				getLineChangeHtml(sourceLine, linePatch.getDeltas().stream().map(d -> d.getSource()).collect(Collectors.toList())), 
        				sourceLineIndex,
        				null
        		);
        		iLastProcTargetLineIndex++;
        		writeLineToHtml(
        				writer, 
        				getLineChangeHtml(targetLine, linePatch.getDeltas().stream().map(d -> d.getTarget()).collect(Collectors.toList())), 
        				null,
        				targetLineIndex
        		);
        		
        	} else {
        	
        		if (iLastProcSourceLineIndex + 1 != sourceLineIndex) throw new Exception("NaDaS");
	            
	            for (String line : delta.getSource().getLines()) {
	            	iLastProcSourceLineIndex++;
	            	writeLineToHtml(writer, StringEscapeUtils.escapeHtml4(line), iLastProcSourceLineIndex, null);
	            }
	 
	            if (iLastProcTargetLineIndex + 1 != targetChunk.getPosition()) throw new Exception("NaDaT");
	            
	            for (String line : delta.getTarget().getLines()) {
	            	iLastProcTargetLineIndex++;
	            	writeLineToHtml(writer, StringEscapeUtils.escapeHtml4(line), null, iLastProcTargetLineIndex);
	            }
        	}
        }
        if (iLastProcSourceLineIndex < lines1.size() - 1) {
        	int nUnchangedRows = lines1.size() - iLastProcSourceLineIndex - 1;
        	for (int i = 1; i <= nUnchangedRows; i++) {
        		writeLineToHtml(
        				writer, 
        				StringEscapeUtils.escapeHtml4(lines1.get(iLastProcSourceLineIndex + i)), 
        				iLastProcSourceLineIndex + i, 
        				iLastProcTargetLineIndex + i
        		);
        	}
        }
        writer.write("</tbody></table>"); 
        writer.write("</body></html>");
        writer.close();
	}
	
	
	static String getDiffFileName(File file1, File file2) {
		String name1 = file1.getName();
		String name2 = file2.getName();
		if (name1.equals(name2)) {
			return name1;
		}
		int l1 = name1.length();
		int l2 = name2.length();
		int l = Math.min(l1,  l2);
		String prefix = "";
		for (int i = 0; i < l; i++) {
			if (name1.charAt(i) != name2.charAt(i)) {
				prefix = name1.substring(0, i);
				break;
			}
		}
		l -= prefix.length();
		String suffix = "";
		for (int i = 0; i < l1 && i < l2; i++) {
			if (name1.charAt(l1 - i - 1) != name2.charAt(l2 - i - 1)) {
				suffix = name1.substring(l1 - i);
				break;
			}
		}
		return prefix + 
			name1.substring(prefix.length(), l1 - suffix.length()) + "-vs-" +
			name2.substring(prefix.length(), l2 - suffix.length()) + 
			suffix;
	}
	
	static boolean areLinesEqual(List<String> lines1, List<String> lines2) {
		String content1 = String.join("\n", lines1);
	    String content2 = String.join("\n", lines2);
	    
		return content1.compareTo(content2) == 0;
	}
	
	static boolean compareLines(List<String> lines1, List<String> lines2, String diffFilePath) throws Exception {
		if (areLinesEqual(lines1, lines2)) return true;
		writeDiffToHtml(lines1, lines2, diffFilePath);
		return false;
	}
	
	static boolean compareFiles(File oldFile, File newFile, String diffDir) throws Exception {
	    Path oldPath = Paths.get(oldFile.getPath());
		Path newPath = Paths.get(newFile.getPath());
		List<String> oldLines = Files.readAllLines(oldPath);
		List<String> newLines = Files.readAllLines(newPath);

	    String oldContent = String.join("\n", oldLines);
	    String newContent = String.join("\n", newLines);
	    
		if (oldContent.compareTo(newContent) == 0) return true;
		
		System.err.println("File \"" + oldFile.getName() + "\" is different" + 
			(oldFile.getName().equals(newFile.getName()) ? "" : " from file \"" + newFile.getName() + "\"") + 
				".");
		
		writeDiffToHtml(
				oldLines,
				newLines,
				Paths.get(diffDir, getDiffFileName(oldFile, newFile) + ".html").toString()
		);

		return false;
	}
	
	public static boolean compareOrCopyFilesInDir(String compDir, String refDir, String diffDir) {
		try {
			File[] compFiles = new File(compDir).listFiles((fn) -> fn.isFile() && fn.getName().endsWith(".txt"));
			File[] refFiles = new File(refDir).listFiles((fn) -> fn.isFile() && fn.getName().endsWith(".txt"));
			Set<String> refFileNames = Stream.of(refFiles).map(f -> f.getName()).collect(Collectors.toSet());
			
			Arrays.sort(compFiles, (f1,f2) -> f1.getName().compareTo(f2.getName()));
			boolean result = true;
			for (File compFile : compFiles) {
				String fileName = compFile.getName();
				File refFile = new File(refDir, fileName);
		
				if (!refFileNames.contains(fileName)) {
					java.nio.file.Files.copy(Paths.get(compFile.getPath()), Paths.get(refFile.getPath()));
					result = true;
				} else {
					result = compareFiles(refFile, compFile, diffDir) && result;
				}
			}
			
			return result;
		} catch(Exception ex) {
			System.err.println("compareDirs of \"" + compDir + "\" and \"" + refDir + "\" failed.");
			ex.printStackTrace();
		}
		return false;
	}
}
