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
package de.bund.bfr.knime.openkrise;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MyHashSet extends HashSet<String> {

	final static int FD = 4;
	final static int BD = 5;

	private Set<String> furtherIds = new HashSet<>();
	/**
	 * 
	 */
	private static final long serialVersionUID = -2199772191561806909L;

	public boolean containsId(String id) {
		return furtherIds.contains(id);
	}

	public void addId(String id) {
		furtherIds.add(id);
	}

	public void merge(Map<String, MyHashSet> backwardDeliveries,
			Map<String, MyHashSet> forwardDeliveries, int type) {
		for (String i : furtherIds) {
			MyHashSet backward = backwardDeliveries.get(i);
			MyHashSet forward = forwardDeliveries.get(i);

			if (type == FD && forward != null)
				this.addAll(forward);
			else if (type == BD && backward != null)
				this.addAll(backward);
		}
		furtherIds.clear();
	}
}
