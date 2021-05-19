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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtree;


/**
 * @author Armin
 *
 */
class MyDBTreeNode {

	private int id = -1;
	private String code = "";
	private String description = "";
	private int codeSystemNum = -1;
	private boolean isLeaf = false;
	private boolean isVisible = true;
	
	MyDBTreeNode(int id, String code, String description, boolean isLeaf, int codeSystemNum) {
		this.id = id;
		this.code = code;
		this.codeSystemNum = codeSystemNum;
		this.description = description;
		this.isLeaf = isLeaf;
	}

	public int getID() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
	
	public int getCodeSystemNum() {
		return codeSystemNum;
	}
	
	public String toString() {
		if (code == null || code.trim().length() == 0) return description;
		else return code + ": " + description;
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}
	public boolean isVisible() {
		return isVisible;
	}
	public void setVisible(boolean visible) {
		isVisible = visible;
	}
}
