/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.HashMap;

public class XlsOther {
	private HashMap<Integer, String> extraVals = new HashMap<>();	
	public HashMap<Integer, String> getExtraVals() {
		return extraVals;
	}

	public static String BLOCK(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Sonstiges";
		else if (lang.equals("en")) return "Other";
		else if (lang.equals("es")) return "Otro";
		else return null;
	}
	public static String COMMENT(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Kommentar";
		else if (lang.equals("en")) return "Comment";
		else if (lang.equals("es")) return "Comentarios";
		else return null;
	}

	public void addField(String fieldname, int index, String lang) {
		if (fieldname != null) {
			String s = fieldname.replaceAll("\\s+","");
			if (s.equalsIgnoreCase(COMMENT(lang))) commentCol = index;
			else extraVals.put(index, s);
		}
	}

	private int startCol = -1;
	private int endCol = -1;
	private int commentCol = -1;
	
	public int getStartCol() {
		return startCol;
	}
	public void setStartCol(int startCol) {
		this.startCol = startCol;
	}
	public int getEndCol() {
		return endCol;
	}
	public void setEndCol(int endCol) {
		this.endCol = endCol;
	}
	public int getCommentCol() {
		return commentCol;
	}
	public void setCommentCol(int commentCol) {
		this.commentCol = commentCol;
	}
}
