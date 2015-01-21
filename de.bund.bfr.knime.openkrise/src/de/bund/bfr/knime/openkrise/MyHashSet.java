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

import java.util.HashMap;
import java.util.HashSet;

public class MyHashSet<T> extends HashSet<T> {

	final static int FD = 4;
	final static int BD = 5;
	
	private HashSet<Integer> furtherIds = new HashSet<>();
	/**
	 * 
	 */
	private static final long serialVersionUID = -2199772191561806909L;

	public boolean containsId(Integer id) {
		return furtherIds.contains(id);
	}
	public void addId(Integer id) {
		furtherIds.add(id);
	}
	
	@SuppressWarnings("unchecked")
	public void merge(HashMap<Integer, MyDelivery> allDeliveries, int type) {
			for (Integer i : furtherIds) {
				MyDelivery dd = allDeliveries.get(i);
					if (type == FD && dd.getForwardDeliveries() != null) this.addAll((HashSet<T>) dd.getForwardDeliveries());
					else if (type == BD && dd.getBackwardDeliveries() != null) this.addAll((HashSet<T>) dd.getBackwardDeliveries());
			}
		furtherIds.clear();
	}
}
