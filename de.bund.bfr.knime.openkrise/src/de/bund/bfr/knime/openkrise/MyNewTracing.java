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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MyNewTracing {

	private Map<Integer, MyDelivery> allDeliveries;
	private Map<Integer, Set<Integer>> allIncoming;
	private Map<Integer, Set<Integer>> allOutgoing;
	private Map<Integer, Double> caseStations = null;
	private Map<Integer, Double> caseDeliveries = null;
	private Set<Integer> ccStations = null;
	private Set<Integer> ccDeliveries = null;
	private LinkedHashMap<Integer, Set<Integer>> sortedStations = null;
	private LinkedHashMap<Integer, Set<Integer>> sortedDeliveries = null;
	private double caseSum = 0;
	private boolean enforceTemporalOrder = false;

	public MyNewTracing(Map<Integer, MyDelivery> allDeliveries) {
		this.allDeliveries = new HashMap<>();

		for (MyDelivery d : allDeliveries.values()) {
			this.allDeliveries.put(d.getId(), d.clone());
		}

		this.caseStations = new HashMap<>();
		this.ccStations = new HashSet<>();
		this.caseDeliveries = new HashMap<>();
		this.ccDeliveries = new HashSet<>();
		this.caseSum = 0;
		removeEmptyIds(this.allDeliveries);
	}

	public Map<Integer, MyDelivery> getAllDeliveries() {
		return allDeliveries;
	}

	private Map<Integer, Set<Integer>> getAllIncoming() {
		if (allIncoming == null) {
			allIncoming = new HashMap<>();
			for (MyDelivery d : allDeliveries.values()) {
				int rid = d.getRecipientID();
				if (!allIncoming.containsKey(rid))
					allIncoming.put(rid, new HashSet<Integer>());
				allIncoming.get(rid).add(d.getId());
			}
		}
		return allIncoming;
	}

	private Map<Integer, Set<Integer>> getAllOutgoing() {
		if (allOutgoing == null) {
			allOutgoing = new HashMap<>();
			for (MyDelivery d : allDeliveries.values()) {
				int sid = d.getSupplierID();
				if (!allOutgoing.containsKey(sid))
					allOutgoing.put(sid, new HashSet<Integer>());
				allOutgoing.get(sid).add(d.getId());
			}
		}
		return allOutgoing;
	}

	public boolean isStationStart(int id) {
		boolean result = true;
		for (Integer key : allDeliveries.keySet()) {
			if (allDeliveries.get(key).getRecipientID() == id) {
				result = false;
				break;
			}
		}
		return result;
	}

	public boolean isSimpleSupplier(int id) {
		if (isStationStart(id)) {
			int recId = 0;
			for (Integer key : allDeliveries.keySet()) {
				if (allDeliveries.get(key).getSupplierID() == id) {
					if (recId == 0)
						recId = allDeliveries.get(key).getRecipientID();
					else if (recId != allDeliveries.get(key).getRecipientID())
						return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean isStationEnd(int id) {
		boolean result = true;
		for (Integer key : allDeliveries.keySet()) {
			if (allDeliveries.get(key).getSupplierID() == id) {
				result = false;
				break;
			}
		}
		return result;
	}

	public Double getStationScore(int id) {
		if (sortedStations == null)
			getScores();
		if (caseSum > 0 && sortedStations.get(id) != null) {
			double sum = 0;
			for (Integer key : sortedStations.get(id)) {
				if (key < 0) {
					if (caseDeliveries.get(-key) != null) {
						sum += caseDeliveries.get(-key);
					}
				} else {
					if (caseStations.get(key) != null) {
						sum += caseStations.get(key);
					}
				}
			}
			if (caseStations.containsKey(id))
				sum += caseStations.get(id);
			return sum / caseSum;
		}
		if (caseSum > 0 && caseStations.containsKey(id))
			return caseStations.get(id) / caseSum;
		return -1.0;
	}

	public Double getDeliveryScore(int id) {
		if (sortedDeliveries == null)
			getScores();
		if (caseSum > 0 && sortedDeliveries.get(id) != null) {
			double sum = 0;
			for (Integer key : sortedDeliveries.get(id)) {
				if (key < 0)
					sum += caseDeliveries.get(-key);
				else
					sum += caseStations.get(key);
			}
			return sum / caseSum;
			// return ((double) sortedDeliveries.get(id).size()) /
			// caseStations.size();
		}
		return -1.0;
	}

	private void getScores() {
		// getForwardStationsWithCases counts for each delivery. But: it might
		// be the case that a station delivers into "different" directions
		// (deliveries), and all of them have cases!!!
		// Therefore, we sum here based on the suppliers (supplierSum), not on
		// the deliveries!!!
		HashMap<Integer, Set<Integer>> supplierSum = new HashMap<>();
		HashMap<Integer, Set<Integer>> deliverySum = new HashMap<>();
		for (MyDelivery md : allDeliveries.values()) {
			Set<Integer> fwc = new HashSet<>();

			fwc.addAll(getForwardStationsWithCases(md));
			fwc.addAll(getForwardDeliveriesWithCases(md));

			if (supplierSum.containsKey(md.getSupplierID())) {
				supplierSum.get(md.getSupplierID()).addAll(fwc);
			} else {
				supplierSum.put(md.getSupplierID(), new HashSet<>(fwc));
			}

			deliverySum.put(md.getId(), new HashSet<>(fwc));
		}

		sortedStations = sortByValues(supplierSum);
		sortedDeliveries = sortByValues(deliverySum);
	}

	private static LinkedHashMap<Integer, Set<Integer>> sortByValues(
			HashMap<Integer, Set<Integer>> map) {
		List<Map.Entry<Integer, Set<Integer>>> entries = new LinkedList<>(map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<Integer, Set<Integer>>>() {

			@Override
			public int compare(Entry<Integer, Set<Integer>> o1, Entry<Integer, Set<Integer>> o2) {
				return o2.getValue().size() - o1.getValue().size();
			}
		});

		// LinkedHashMap will keep the keys in the order they are inserted
		// which is currently sorted on natural ordering
		LinkedHashMap<Integer, Set<Integer>> sortedMap = new LinkedHashMap<>();

		for (Map.Entry<Integer, Set<Integer>> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public Set<Integer> getForwardStations(int stationID) {
		Set<Integer> stations = new LinkedHashSet<>();

		if (getAllOutgoing().get(stationID) != null) {
			for (Integer i : getAllOutgoing().get(stationID)) {
				MyDelivery md = allDeliveries.get(i);
				stations.addAll(getForwardStations(md));
			}
		}

		return stations;
	}

	public Set<Integer> getBackwardStations(int stationID) {
		Set<Integer> stations = new LinkedHashSet<>();

		if (getAllIncoming().get(stationID) != null) {
			for (Integer i : getAllIncoming().get(stationID)) {
				MyDelivery md = allDeliveries.get(i);
				stations.addAll(getBackwardStations(md));
			}
		}

		return stations;
	}

	public Set<Integer> getForwardDeliveries(int stationID) {
		Set<Integer> deliveries = new LinkedHashSet<>();

		if (getAllOutgoing().get(stationID) != null) {
			for (Integer i : getAllOutgoing().get(stationID)) {
				MyDelivery md = allDeliveries.get(i);
				deliveries.addAll(getForwardDeliveries(md));
			}
		}

		return deliveries;
	}

	public Set<Integer> getBackwardDeliveries(int stationID) {
		Set<Integer> deliveries = new LinkedHashSet<>();

		if (getAllIncoming().get(stationID) != null) {
			for (Integer i : getAllIncoming().get(stationID)) {
				MyDelivery md = allDeliveries.get(i);
				deliveries.addAll(getBackwardDeliveries(md));
			}
		}

		return deliveries;
	}

	public Set<Integer> getForwardStations2(int deliveryId) {
		return getForwardStations(allDeliveries.get(deliveryId));
	}

	public Set<Integer> getBackwardStations2(int deliveryId) {
		return getBackwardStations(allDeliveries.get(deliveryId));
	}

	public Set<Integer> getForwardDeliveries2(int deliveryId) {
		Set<Integer> f = new HashSet<>(getForwardDeliveries(allDeliveries.get(deliveryId)));

		f.remove(deliveryId);

		return f;
	}

	public Set<Integer> getBackwardDeliveries2(int deliveryId) {
		Set<Integer> b = new HashSet<>(getBackwardDeliveries(allDeliveries.get(deliveryId)));

		b.remove(deliveryId);

		return b;
	}

	public void fillDeliveries(boolean enforceTemporalOrder) {
		this.enforceTemporalOrder = enforceTemporalOrder;
		allIncoming = null;
		allOutgoing = null;
		for (MyDelivery md : allDeliveries.values()) {
			md.resetStatusVariables();
		}
		tcocc();
	}

	public void setCaseDelivery(int deliveryID, double priority) {
		if (caseDeliveries == null)
			caseDeliveries = new HashMap<>();
		if (priority < 0)
			priority = 0;
		if (caseDeliveries.containsKey(deliveryID)) {
			caseSum = caseSum - caseDeliveries.get(deliveryID) + priority;
			caseDeliveries.put(deliveryID, priority);
		} else {
			caseSum = caseSum + priority;
			caseDeliveries.put(deliveryID, priority);
		}
		if (priority == 0)
			caseDeliveries.remove(deliveryID);
		sortedStations = null;
		sortedDeliveries = null;
	}

	public void setCase(int stationID, double priority) {
		if (caseStations == null)
			caseStations = new HashMap<>();
		if (priority < 0)
			priority = 0;
		// else if (priority > 1) priority = 1;
		if (caseStations.containsKey(stationID)) {
			caseSum = caseSum - caseStations.get(stationID) + priority;
			caseStations.put(stationID, priority);
		} else {
			caseSum = caseSum + priority;
			caseStations.put(stationID, priority);
		}
		if (priority == 0)
			caseStations.remove(stationID);
		sortedStations = null;
		sortedDeliveries = null;
	}

	public void setCrossContaminationDelivery(int deliveryID, boolean possible) {
		if (ccDeliveries == null)
			ccDeliveries = new HashSet<>();
		if (possible)
			ccDeliveries.add(deliveryID);
		else if (ccDeliveries.contains(deliveryID))
			ccDeliveries.remove(deliveryID);

		sortedStations = null;
		sortedDeliveries = null;
	}

	public void setCrossContamination(int stationID, boolean possible) {
		if (ccStations == null)
			ccStations = new HashSet<>();
		if (possible)
			ccStations.add(stationID);
		else if (ccStations.contains(stationID))
			ccStations.remove(stationID);

		sortedStations = null;
		sortedDeliveries = null;
	}

	private void tcocc() {
		if (ccStations != null && ccStations.size() > 0) {
			// wegen dem Hollandproblem, damit die Performance nicht leidet,
			// muss hier auf true gesetzt werden...
			// boolean only1_ergo_allCross = false;
			for (Integer key : allDeliveries.keySet()) {
				MyDelivery md = allDeliveries.get(key);
				if (ccStations.contains(md.getRecipientID())) {
					// Map<Integer, HashSet<Integer>> hi = new HashMap<>();
					Set<Integer> mdl = getAllOutgoing().get(md.getRecipientID());
					if (mdl != null) {
						for (Integer i : mdl) {
							MyDelivery d = allDeliveries.get(i);
							if (!enforceTemporalOrder || (is1MaybeNewer(d, md))) {
								// if (only1_ergo_allCross) {
								// if (!hi.containsKey(md.getSupplierID()))
								// hi.put(md.getSupplierID(), new
								// HashSet<Integer>());
								// HashSet<Integer> hs =
								// hi.get(md.getSupplierID());
								// if (!hs.contains(d.getRecipientID())) {
								// md.getAllNextIDs().add(d.getId());
								// d.getAllPreviousIDs().add(md.getId());
								// hs.add(d.getRecipientID());
								// hi.put(md.getSupplierID(), hs);
								// }
								// } else {
								md.getAllNextIDs().add(d.getId());
								d.getAllPreviousIDs().add(md.getId());
								// }
							}
						}
					}
				}
				allDeliveries.put(key, md); // -559268585
			}
		}
		// delivery cc: es werden alle CCs VOR einer Station mit allen CCs NACH
		// einer Station "vermischt"
		// NEW: all incoming-ccs are mixed
		if (ccDeliveries != null && ccDeliveries.size() > 0) {
			for (Integer key : ccDeliveries) {
				MyDelivery md = allDeliveries.get(key);
				for (Integer key2 : ccDeliveries) {
					if (key.intValue() != key2) {
						MyDelivery md2 = allDeliveries.get(key2);
						if (md2.getRecipientID() == md.getRecipientID()) {
							for (Integer idn : md.getAllNextIDs()) {
								md2.getAllNextIDs().add(idn);
								MyDelivery md3 = allDeliveries.get(idn);
								md3.getAllPreviousIDs().add(md.getId());
							}
							for (Integer idn : md2.getAllNextIDs()) {
								md.getAllNextIDs().add(idn);
								MyDelivery md3 = allDeliveries.get(idn);
								md3.getAllPreviousIDs().add(md2.getId());
							}
						}
					}
				}
			}
		}
	}

	// e.g. Jan 2012 vs. 18.Jan 2012 - be generous
	private boolean is1MaybeNewer(MyDelivery md1, MyDelivery md2) {
		Integer year1 = md1.getDeliveryYear();
		Integer year2 = md2.getDeliveryYear();
		if (year1 == null || year2 == null)
			return true;
		if (year1 > year2)
			return true;
		if (year1 < year2)
			return false;
		Integer month1 = md1.getDeliveryMonth();
		Integer month2 = md2.getDeliveryMonth();
		if (month1 == null || month2 == null)
			return true;
		if (month1 > month2)
			return true;
		if (month1 < month2)
			return false;
		Integer day1 = md1.getDeliveryDay();
		Integer day2 = md2.getDeliveryDay();
		if (day1 == null || day2 == null)
			return true;
		if (day1 >= day2)
			return true;
		return false;
	}

	public void mergeStations(Set<Integer> toBeMerged, Integer mergedStationID) {
		if (toBeMerged != null && toBeMerged.size() > 0) {
			for (Integer key : allDeliveries.keySet()) {
				MyDelivery md = allDeliveries.get(key);
				if (toBeMerged.contains(md.getSupplierID())) {
					md.setSupplierID(mergedStationID);
				}
				if (toBeMerged.contains(md.getRecipientID())) {
					md.setRecipientID(mergedStationID);
				}
				allDeliveries.put(key, md);
			}
		}
		sortedStations = null;
		sortedDeliveries = null;
	}

	private void searchFBCases(MyDelivery md, MyHashSet<Integer> stemmingDeliveries) {
		if (!stemmingDeliveries.contains(md.getId())) {
			stemmingDeliveries.add(md.getId());
			Set<Integer> n = md.getAllPreviousIDs();
			for (Integer d : n) {
				MyDelivery dd = allDeliveries.get(d);
				if (dd.getBackwardDeliveries() != null) {
					stemmingDeliveries.addId(d);
				} else {
					searchFBCases(dd, stemmingDeliveries);
				}
			}
		}
	}

	private void searchFFCases(MyDelivery md, MyHashSet<Integer> headingDeliveries) {
		if (!headingDeliveries.contains(md.getId())) {
			headingDeliveries.add(md.getId());
			Set<Integer> n = md.getAllNextIDs();
			for (Integer d : n) {
				MyDelivery dd = allDeliveries.get(d);
				if (dd.getForwardDeliveries() != null) {
					headingDeliveries.addId(d);
				} else {
					searchFFCases(dd, headingDeliveries);
				}
			}
		}
	}

	private Set<Integer> getForwardDeliveries(MyDelivery md) {
		if (md != null) {
			MyHashSet<Integer> forwardDeliveries = md.getForwardDeliveries();
			if (forwardDeliveries == null) {
				forwardDeliveries = new MyHashSet<>();
				searchFFCases(md, forwardDeliveries);
				forwardDeliveries.merge(allDeliveries, MyHashSet.FD);
				md.setForwardDeliveries(forwardDeliveries);
			}
			return forwardDeliveries;
		}
		return null;
	}

	private Set<Integer> getBackwardDeliveries(MyDelivery md) {
		if (md != null) {
			MyHashSet<Integer> backwardDeliveries = md.getBackwardDeliveries();
			if (backwardDeliveries == null) {
				backwardDeliveries = new MyHashSet<>();
				searchFBCases(md, backwardDeliveries);
				backwardDeliveries.merge(allDeliveries, MyHashSet.BD);
				md.setBackwardDeliveries(backwardDeliveries);
			}
			return backwardDeliveries;
		}
		return null;
	}

	private Set<Integer> getBackwardStations(MyDelivery md) {
		Set<Integer> result = null;
		if (md != null) {
			Set<Integer> fd = getBackwardDeliveries(md);
			if (fd != null && fd.size() > 0) {
				result = new HashSet<>();
				for (Integer i : fd) {
					MyDelivery mdn = allDeliveries.get(i);
					result.add(mdn.getSupplierID());
				}
			}
		}
		return result;
	}

	private Set<Integer> getForwardStations(MyDelivery md) {
		Set<Integer> result = null;
		if (md != null) {
			Set<Integer> fd = getForwardDeliveries(md);
			if (fd != null && fd.size() > 0) {
				result = new HashSet<>();
				for (Integer i : fd) {
					MyDelivery mdn = allDeliveries.get(i);
					result.add(mdn.getRecipientID());
				}
			}
		}
		return result;
	}

	private Set<Integer> getForwardStationsWithCases(MyDelivery md) {
		Set<Integer> result = null;
		if (md != null) {
			Set<Integer> fd = getForwardDeliveries(md);
			if (fd != null && fd.size() > 0) {
				result = new HashSet<>();
				for (Integer i : fd) {
					MyDelivery mdn = allDeliveries.get(i);
					if (caseStations.containsKey(mdn.getRecipientID()))
						result.add(mdn.getRecipientID());
				}
			}
		}
		return result;
	}

	private Set<Integer> getForwardDeliveriesWithCases(MyDelivery md) {
		Set<Integer> result = null;
		if (md != null) {
			Set<Integer> fd = getForwardDeliveries(md);
			if (fd != null && fd.size() > 0) {
				result = new HashSet<>();
				for (Integer i : fd) {
					MyDelivery mdn = allDeliveries.get(i);
					if (caseDeliveries.containsKey(mdn.getId()))
						result.add(-mdn.getId());
					// hier minus, damit nachher unterschieden werden kann
					// zwischen Delivery und Station, siehe in Funktion
					// getStationScore bzw. getDeliveryScore
				}
			}
		}
		return result;
	}

	private static void removeEmptyIds(Map<Integer, MyDelivery> deliveries) {
		for (MyDelivery delivery : deliveries.values()) {
			delivery.getAllNextIDs().retainAll(deliveries.keySet());
			delivery.getAllPreviousIDs().retainAll(deliveries.keySet());
		}
	}
}
