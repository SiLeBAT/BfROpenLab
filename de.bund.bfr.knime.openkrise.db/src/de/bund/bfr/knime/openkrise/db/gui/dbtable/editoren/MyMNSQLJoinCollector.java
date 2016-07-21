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
package de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren;

import java.util.HashSet;

import de.bund.bfr.knime.openkrise.db.MyTable;

public class MyMNSQLJoinCollector {

	private String toSelect = "";
	private String toJoin = "";
	private boolean hasUnknownFields = false;
	private int addCounter = 0;
	private HashSet<MyTable> alreadyJoined = new HashSet<>(); 

	public MyMNSQLJoinCollector(String toSelect, String toJoin) {
		this.toSelect = toSelect;
		this.toJoin = toJoin;
		addCounter = 0;
	}
	
	public void addToSelect(String addSelect) {
		toSelect += addSelect;
		addCounter++;
	}
	public void addToJoin(String addJoin) {
		toJoin += addJoin;
	}
	
	public String getToSelect() {
		return toSelect;
	}
	public String getToJoin() {
		return toJoin;
	}

	public HashSet<MyTable> getAlreadyJoined() {
		return alreadyJoined;
	}

	public boolean hasUnknownFields() {
		return hasUnknownFields;
	}
	public void setHasUnknownFields(boolean hasUnknownFields) {
		this.hasUnknownFields = hasUnknownFields;
	}

	public int getAddCounter() {
		return addCounter;
	}
}
