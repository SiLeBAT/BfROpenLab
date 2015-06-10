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
import java.util.Map;
import java.util.Set;

public class MyNewTracing {

	private Map<String, MyDelivery> allDeliveries;
	private Map<String, Set<String>> allIncoming;
	private Map<String, Set<String>> allOutgoing;
	private Map<String, Double> caseStations = null;
	private Map<String, Double> caseDeliveries = null;
	private Set<String> ccStations = null;
	private Set<String> ccDeliveries = null;
	private Map<String, Set<String>> sortedStations = null;
	private Map<String, Set<String>> sortedDeliveries = null;
	private double caseSum = 0;
	private boolean enforceTemporalOrder = false;

	public MyNewTracing(Map<String, MyDelivery> allDeliveries) {
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

	public Map<String, MyDelivery> getAllDeliveries() {
		return allDeliveries;
	}

	private Map<String, Set<String>> getAllIncoming() {
		if (allIncoming == null) {
			allIncoming = new HashMap<>();
			for (MyDelivery d : allDeliveries.values()) {
				String rid = d.getRecipientID();
				if (!allIncoming.containsKey(rid))
					allIncoming.put(rid, new HashSet<String>());
				allIncoming.get(rid).add(d.getId());
			}
		}
		return allIncoming;
	}

	private Map<String, Set<String>> getAllOutgoing() {
		if (allOutgoing == null) {
			allOutgoing = new HashMap<>();
			for (MyDelivery d : allDeliveries.values()) {
				String sid = d.getSupplierID();
				if (!allOutgoing.containsKey(sid))
					allOutgoing.put(sid, new HashSet<String>());
				allOutgoing.get(sid).add(d.getId());
			}
		}
		return allOutgoing;
	}

	public boolean isStationStart(String id) {
		for (MyDelivery d : allDeliveries.values()) {
			if (d.getRecipientID().equals(id)) {
				return false;
			}
		}

		return true;
	}

	public boolean isSimpleSupplier(String id) {
		if (isStationStart(id)) {
			String recId = null;
			for (MyDelivery d : allDeliveries.values()) {
				if (d.getSupplierID().equals(id)) {
					if (recId == null)
						recId = d.getRecipientID();
					else if (!recId.equals(d.getRecipientID()))
						return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean isStationEnd(String id) {
		for (MyDelivery d : allDeliveries.values()) {
			if (d.getSupplierID().equals(id)) {
				return false;
			}
		}

		return true;
	}

	public double getStationScore(String id) {
		if (sortedStations == null)
			getScores();
		if (caseSum > 0 && sortedStations.get(id) != null) {
			double sum = 0;
			for (String key : sortedStations.get(id)) {
				if (key.startsWith("-")) {
					key = key.substring(1);

					if (caseDeliveries.get(key) != null) {
						sum += caseDeliveries.get(key);
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
		return 0.0;
	}

	public double getDeliveryScore(String id) {
		if (sortedDeliveries == null)
			getScores();
		if (caseSum > 0 && sortedDeliveries.get(id) != null) {
			double sum = 0;
			for (String key : sortedDeliveries.get(id)) {
				if (key.startsWith("-"))
					sum += caseDeliveries.get(key.substring(1));
				else
					sum += caseStations.get(key);
			}
			return sum / caseSum;
			// return ((double) sortedDeliveries.get(id).size()) /
			// caseStations.size();
		}
		return 0.0;
	}

	private void getScores() {
		// getForwardStationsWithCases counts for each delivery. But: it might
		// be the case that a station delivers into "different" directions
		// (deliveries), and all of them have cases!!!
		// Therefore, we sum here based on the suppliers (supplierSum), not on
		// the deliveries!!!
		sortedStations = new HashMap<>();
		sortedDeliveries = new HashMap<>();

		for (MyDelivery md : allDeliveries.values()) {
			Set<String> fwc = new HashSet<>();

			fwc.addAll(getForwardStationsWithCases(md));
			fwc.addAll(getForwardDeliveriesWithCases(md));

			if (sortedStations.containsKey(md.getSupplierID())) {
				sortedStations.get(md.getSupplierID()).addAll(fwc);
			} else {
				sortedStations.put(md.getSupplierID(), new HashSet<>(fwc));
			}

			sortedDeliveries.put(md.getId(), new HashSet<>(fwc));
		}
	}

	public Set<String> getForwardStations(String stationID) {
		Set<String> stations = new HashSet<>();

		if (getAllOutgoing().get(stationID) != null) {
			for (String i : getAllOutgoing().get(stationID)) {
				MyDelivery md = allDeliveries.get(i);
				stations.addAll(getForwardStations(md));
			}
		}

		return stations;
	}

	public Set<String> getBackwardStations(String stationID) {
		Set<String> stations = new HashSet<>();

		if (getAllIncoming().get(stationID) != null) {
			for (String i : getAllIncoming().get(stationID)) {
				MyDelivery md = allDeliveries.get(i);
				stations.addAll(getBackwardStations(md));
			}
		}

		return stations;
	}

	public Set<String> getForwardDeliveries(String stationID) {
		Set<String> deliveries = new HashSet<>();

		if (getAllOutgoing().get(stationID) != null) {
			for (String i : getAllOutgoing().get(stationID)) {
				MyDelivery md = allDeliveries.get(i);
				deliveries.addAll(getForwardDeliveries(md));
			}
		}

		return deliveries;
	}

	public Set<String> getBackwardDeliveries(String stationID) {
		Set<String> deliveries = new HashSet<>();

		if (getAllIncoming().get(stationID) != null) {
			for (String i : getAllIncoming().get(stationID)) {
				MyDelivery md = allDeliveries.get(i);
				deliveries.addAll(getBackwardDeliveries(md));
			}
		}

		return deliveries;
	}

	public Set<String> getForwardStations2(String deliveryId) {
		return getForwardStations(allDeliveries.get(deliveryId));
	}

	public Set<String> getBackwardStations2(String deliveryId) {
		return getBackwardStations(allDeliveries.get(deliveryId));
	}

	public Set<String> getForwardDeliveries2(String deliveryId) {
		Set<String> f = new HashSet<>(getForwardDeliveries(allDeliveries.get(deliveryId)));

		f.remove(deliveryId);

		return f;
	}

	public Set<String> getBackwardDeliveries2(String deliveryId) {
		Set<String> b = new HashSet<>(getBackwardDeliveries(allDeliveries.get(deliveryId)));

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

	public void setCaseDelivery(String deliveryID, double priority) {
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

	public void setCase(String stationID, double priority) {
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

	public void setCrossContaminationDelivery(String deliveryID, boolean possible) {
		if (ccDeliveries == null)
			ccDeliveries = new HashSet<>();
		if (possible)
			ccDeliveries.add(deliveryID);
		else if (ccDeliveries.contains(deliveryID))
			ccDeliveries.remove(deliveryID);

		sortedStations = null;
		sortedDeliveries = null;
	}

	public void setCrossContamination(String stationID, boolean possible) {
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
			for (MyDelivery md : allDeliveries.values()) {
				if (ccStations.contains(md.getRecipientID())) {
					// Map<Integer, HashSet<Integer>> hi = new HashMap<>();
					Set<String> mdl = getAllOutgoing().get(md.getRecipientID());
					if (mdl != null) {
						for (String i : mdl) {
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
			}
		}
		// delivery cc: es werden alle CCs VOR einer Station mit allen CCs NACH
		// einer Station "vermischt"
		// NEW: all incoming-ccs are mixed
		if (ccDeliveries != null && ccDeliveries.size() > 0) {
			for (String key : ccDeliveries) {
				MyDelivery md = allDeliveries.get(key);
				for (String key2 : ccDeliveries) {
					if (!key.equals(key2)) {
						MyDelivery md2 = allDeliveries.get(key2);
						if (md2.getRecipientID().equals(md.getRecipientID())) {
							for (String idn : md.getAllNextIDs()) {
								md2.getAllNextIDs().add(idn);
								MyDelivery md3 = allDeliveries.get(idn);
								md3.getAllPreviousIDs().add(md.getId());
							}
							for (String idn : md2.getAllNextIDs()) {
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

	public void mergeStations(Set<String> toBeMerged, String mergedStationID) {
		if (toBeMerged != null && toBeMerged.size() > 0) {
			for (MyDelivery md : allDeliveries.values()) {
				if (toBeMerged.contains(md.getSupplierID())) {
					md.setSupplierID(mergedStationID);
				}
				if (toBeMerged.contains(md.getRecipientID())) {
					md.setRecipientID(mergedStationID);
				}
			}
		}
		sortedStations = null;
		sortedDeliveries = null;
	}

	private void searchFBCases(MyDelivery md, MyHashSet stemmingDeliveries) {
		if (!stemmingDeliveries.contains(md.getId())) {
			stemmingDeliveries.add(md.getId());
			Set<String> n = md.getAllPreviousIDs();
			for (String d : n) {
				MyDelivery dd = allDeliveries.get(d);
				if (dd.getBackwardDeliveries() != null) {
					stemmingDeliveries.addId(d);
				} else {
					searchFBCases(dd, stemmingDeliveries);
				}
			}
		}
	}

	private void searchFFCases(MyDelivery md, MyHashSet headingDeliveries) {
		if (!headingDeliveries.contains(md.getId())) {
			headingDeliveries.add(md.getId());
			Set<String> n = md.getAllNextIDs();
			for (String d : n) {
				MyDelivery dd = allDeliveries.get(d);
				if (dd.getForwardDeliveries() != null) {
					headingDeliveries.addId(d);
				} else {
					searchFFCases(dd, headingDeliveries);
				}
			}
		}
	}

	private Set<String> getForwardDeliveries(MyDelivery md) {
		if (md != null) {
			MyHashSet forwardDeliveries = md.getForwardDeliveries();
			if (forwardDeliveries == null) {
				forwardDeliveries = new MyHashSet();
				searchFFCases(md, forwardDeliveries);
				forwardDeliveries.merge(allDeliveries, MyHashSet.FD);
				md.setForwardDeliveries(forwardDeliveries);
			}
			return forwardDeliveries;
		}
		return null;
	}

	private Set<String> getBackwardDeliveries(MyDelivery md) {
		if (md != null) {
			MyHashSet backwardDeliveries = md.getBackwardDeliveries();
			if (backwardDeliveries == null) {
				backwardDeliveries = new MyHashSet();
				searchFBCases(md, backwardDeliveries);
				backwardDeliveries.merge(allDeliveries, MyHashSet.BD);
				md.setBackwardDeliveries(backwardDeliveries);
			}
			return backwardDeliveries;
		}
		return null;
	}

	private Set<String> getBackwardStations(MyDelivery md) {
		Set<String> result = null;
		if (md != null) {
			Set<String> fd = getBackwardDeliveries(md);
			if (fd != null && fd.size() > 0) {
				result = new HashSet<>();
				for (String i : fd) {
					MyDelivery mdn = allDeliveries.get(i);
					result.add(mdn.getSupplierID());
				}
			}
		}
		return result;
	}

	private Set<String> getForwardStations(MyDelivery md) {
		Set<String> result = null;
		if (md != null) {
			Set<String> fd = getForwardDeliveries(md);
			if (fd != null && fd.size() > 0) {
				result = new HashSet<>();
				for (String i : fd) {
					MyDelivery mdn = allDeliveries.get(i);
					result.add(mdn.getRecipientID());
				}
			}
		}
		return result;
	}

	private Set<String> getForwardStationsWithCases(MyDelivery md) {
		Set<String> result = null;
		if (md != null) {
			Set<String> fd = getForwardDeliveries(md);
			if (fd != null && fd.size() > 0) {
				result = new HashSet<>();
				for (String i : fd) {
					MyDelivery mdn = allDeliveries.get(i);
					if (caseStations.containsKey(mdn.getRecipientID()))
						result.add(mdn.getRecipientID());
				}
			}
		}
		return result;
	}

	private Set<String> getForwardDeliveriesWithCases(MyDelivery md) {
		Set<String> result = null;
		if (md != null) {
			Set<String> fd = getForwardDeliveries(md);
			if (fd != null && fd.size() > 0) {
				result = new HashSet<>();
				for (String i : fd) {
					MyDelivery mdn = allDeliveries.get(i);
					if (caseDeliveries.containsKey(mdn.getId()))
						result.add("-" + mdn.getId());
					// hier minus, damit nachher unterschieden werden kann
					// zwischen Delivery und Station, siehe in Funktion
					// getStationScore bzw. getDeliveryScore
				}
			}
		}
		return result;
	}

	private static void removeEmptyIds(Map<String, MyDelivery> deliveries) {
		for (MyDelivery delivery : deliveries.values()) {
			delivery.getAllNextIDs().retainAll(deliveries.keySet());
			delivery.getAllPreviousIDs().retainAll(deliveries.keySet());
		}
	}

	private boolean serialUsable = false;

	public void setSerialUsable(boolean serialUsable) {
		this.serialUsable = serialUsable;
	}

	public boolean isSerialUsable() {
		return serialUsable;
	}
}
