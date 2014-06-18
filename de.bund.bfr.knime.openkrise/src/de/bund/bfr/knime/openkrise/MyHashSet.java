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
