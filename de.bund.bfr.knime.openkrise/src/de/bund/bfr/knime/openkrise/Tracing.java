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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class Tracing {

	private List<Delivery> deliveries;
	private Map<String, Double> stationWeights;
	private Map<String, Double> deliveryWeights;
	private Set<String> ccStations;
	private Set<String> ccDeliveries;

	private transient Map<String, Delivery> deliveryMap;
	private transient Map<String, Set<String>> incomingDeliveries;
	private transient Map<String, Set<String>> outgoingDeliveries;
	private transient Map<String, Set<String>> backwardDeliveries;
	private transient Map<String, Set<String>> forwardDeliveries;
	private transient double weightSum;

	public Tracing(Collection<Delivery> deliveries) {
		Set<String> allIds = new LinkedHashSet<>();

		for (Delivery d : deliveries) {
			allIds.add(d.getId());
		}

		this.deliveries = new ArrayList<>();

		for (Delivery d : deliveries) {
			Delivery copy = d.copy();

			copy.getAllNextIDs().retainAll(allIds);
			copy.getAllPreviousIDs().retainAll(allIds);
			this.deliveries.add(copy);
		}

		stationWeights = new LinkedHashMap<>();
		ccStations = new LinkedHashSet<>();
		deliveryWeights = new LinkedHashMap<>();
		ccDeliveries = new LinkedHashSet<>();
	}

	public void setStationWeight(String stationID, double priority) {
		if (priority > 0) {
			stationWeights.put(stationID, priority);
		} else {
			stationWeights.remove(stationID);
		}
	}

	public void setDeliveryWeight(String deliveryID, double priority) {
		if (priority > 0) {
			deliveryWeights.put(deliveryID, priority);
		} else {
			deliveryWeights.remove(deliveryID);
		}
	}

	public void setCrossContaminationOfStation(String stationID, boolean possible) {
		if (possible) {
			ccStations.add(stationID);
		} else {
			ccStations.remove(stationID);
		}
	}

	public void setCrossContaminationOfDelivery(String deliveryID, boolean possible) {
		if (possible) {
			ccDeliveries.add(deliveryID);
		} else {
			ccDeliveries.remove(deliveryID);
		}
	}

	public void mergeStations(Set<String> toBeMerged, String mergedStationID) {
		for (Delivery d : deliveries) {
			if (toBeMerged.contains(d.getSupplierID())) {
				d.setSupplierID(mergedStationID);
			}

			if (toBeMerged.contains(d.getRecipientID())) {
				d.setRecipientID(mergedStationID);
			}
		}
	}

	public Result getResult(boolean enforceTemporalOrder) {
		deliveryMap = new LinkedHashMap<>();

		for (Delivery d : deliveries) {
			deliveryMap.put(d.getId(), d.copy());
		}

		incomingDeliveries = new LinkedHashMap<>();
		outgoingDeliveries = new LinkedHashMap<>();

		for (Delivery d : deliveryMap.values()) {
			String recipient = d.getRecipientID();

			if (!incomingDeliveries.containsKey(recipient)) {
				incomingDeliveries.put(recipient, new LinkedHashSet<String>());
			}

			incomingDeliveries.get(recipient).add(d.getId());
		}

		for (Delivery d : deliveryMap.values()) {
			String supplier = d.getSupplierID();

			if (!outgoingDeliveries.containsKey(supplier)) {
				outgoingDeliveries.put(supplier, new LinkedHashSet<String>());
			}

			outgoingDeliveries.get(supplier).add(d.getId());
		}

		backwardDeliveries = new LinkedHashMap<>();
		forwardDeliveries = new LinkedHashMap<>();
		weightSum = 0.0;

		for (double w : stationWeights.values()) {
			weightSum += w;
		}

		for (double w : deliveryWeights.values()) {
			weightSum += w;
		}

		for (String stationId : ccStations) {
			for (String inId : incomingDeliveries.get(stationId)) {
				Delivery in = deliveryMap.get(inId);

				for (String outId : outgoingDeliveries.get(stationId)) {
					if (inId.equals(outId)) {
						continue;
					}

					Delivery out = deliveryMap.get(outId);

					if (!enforceTemporalOrder || (isInTemporalOrder(in, out))) {
						in.getAllNextIDs().add(outId);
						out.getAllPreviousIDs().add(inId);
					}
				}
			}
		}

		// delivery cc: all incoming-ccs are mixed
		for (String in1Id : ccDeliveries) {
			Delivery in1 = deliveryMap.get(in1Id);

			for (String in2Id : ccDeliveries) {
				if (in1Id.equals(in2Id)) {
					continue;
				}

				Delivery in2 = deliveryMap.get(in2Id);

				if (!in1.getRecipientID().equals(in2.getRecipientID())) {
					continue;
				}

				for (String out1Id : in1.getAllNextIDs()) {
					Delivery out1 = deliveryMap.get(out1Id);

					if (!enforceTemporalOrder || (isInTemporalOrder(in2, out1))) {
						in2.getAllNextIDs().add(out1Id);
						out1.getAllPreviousIDs().add(in2Id);
					}
				}

				for (String out2Id : in2.getAllNextIDs()) {
					Delivery out2 = deliveryMap.get(out2Id);

					if (!enforceTemporalOrder || (isInTemporalOrder(in1, out2))) {
						in1.getAllNextIDs().add(out2Id);
						out2.getAllPreviousIDs().add(in1Id);
					}
				}
			}
		}

		Result result = new Result();

		for (String stationId : Sets
				.union(incomingDeliveries.keySet(), outgoingDeliveries.keySet())) {
			result.stationScores.put(stationId, getStationScore(stationId));
			result.forwardStationsByStation.put(stationId, getForwardStations(stationId));
			result.backwardStationsByStation.put(stationId, getBackwardStations(stationId));
			result.forwardDeliveriesByStation.put(stationId, getForwardDeliveries(stationId));
			result.backwardDeliveriesByStation.put(stationId, getBackwardDeliveries(stationId));
		}

		for (Delivery d : deliveryMap.values()) {
			result.deliveryScores.put(d.getId(), getDeliveryScore(d));
			result.forwardStationsByDelivery.put(d.getId(), getForwardStations(d));
			result.backwardStationsByDelivery.put(d.getId(), getBackwardStations(d));
			result.forwardDeliveriesByDelivery.put(d.getId(), getForwardDeliveries(d));
			result.backwardDeliveriesByDelivery.put(d.getId(), getBackwardDeliveries(d));
		}

		return result;
	}

	private double getStationScore(String id) {
		if (weightSum == 0.0) {
			return 0.0;
		}

		double sum = 0.0;

		if (stationWeights.containsKey(id)) {
			sum += stationWeights.get(id);
		}

		for (String stationId : getForwardStations(id)) {
			if (stationWeights.containsKey(stationId)) {
				sum += stationWeights.get(stationId);
			}
		}

		for (String deliveryId : getForwardDeliveries(id)) {
			if (deliveryWeights.containsKey(deliveryId)) {
				sum += deliveryWeights.get(deliveryId);
			}
		}

		return sum / weightSum;
	}

	private double getDeliveryScore(Delivery d) {
		if (weightSum == 0.0) {
			return 0.0;
		}

		double sum = 0.0;

		if (deliveryWeights.containsKey(d.getId())) {
			sum += deliveryWeights.get(d.getId());
		}

		for (String stationId : getForwardStations(d)) {
			if (stationWeights.containsKey(stationId)) {
				sum += stationWeights.get(stationId);
			}
		}

		for (String deliveryId : getForwardDeliveries(d)) {
			if (deliveryWeights.containsKey(deliveryId)) {
				sum += deliveryWeights.get(deliveryId);
			}
		}

		return sum / weightSum;
	}

	private Set<String> getForwardStations(String stationID) {
		Set<String> stations = new LinkedHashSet<>();

		if (outgoingDeliveries.containsKey(stationID)) {
			for (String i : outgoingDeliveries.get(stationID)) {
				stations.addAll(getForwardStations(deliveryMap.get(i)));
			}
		}

		return stations;
	}

	private Set<String> getBackwardStations(String stationID) {
		Set<String> stations = new LinkedHashSet<>();

		if (incomingDeliveries.containsKey(stationID)) {
			for (String i : incomingDeliveries.get(stationID)) {
				stations.addAll(getBackwardStations(deliveryMap.get(i)));
			}
		}

		return stations;
	}

	private Set<String> getForwardDeliveries(String stationID) {
		Set<String> forward = new LinkedHashSet<>();

		if (outgoingDeliveries.containsKey(stationID)) {
			for (String id : outgoingDeliveries.get(stationID)) {
				forward.add(id);
				forward.addAll(getForwardDeliveries(deliveryMap.get(id)));
			}
		}

		return forward;
	}

	private Set<String> getBackwardDeliveries(String stationID) {
		Set<String> backward = new LinkedHashSet<>();

		if (incomingDeliveries.containsKey(stationID)) {
			for (String id : incomingDeliveries.get(stationID)) {
				backward.add(id);
				backward.addAll(getBackwardDeliveries(deliveryMap.get(id)));
			}
		}

		return backward;
	}

	private Set<String> getBackwardDeliveries(Delivery d) {
		Set<String> backward = backwardDeliveries.get(d.getId());

		if (backward != null) {
			return backward;
		}

		backward = new LinkedHashSet<>();

		for (String prev : d.getAllPreviousIDs()) {
			backward.add(prev);
			backward.addAll(getBackwardDeliveries(deliveryMap.get(prev)));
		}

		backwardDeliveries.put(d.getId(), backward);

		return backward;
	}

	private Set<String> getForwardDeliveries(Delivery d) {
		Set<String> forward = forwardDeliveries.get(d.getId());

		if (forward != null) {
			return forward;
		}

		forward = new LinkedHashSet<>();

		for (String next : d.getAllNextIDs()) {
			forward.add(next);
			forward.addAll(getForwardDeliveries(deliveryMap.get(next)));
		}

		forwardDeliveries.put(d.getId(), forward);

		return forward;
	}

	private Set<String> getBackwardStations(Delivery d) {
		Set<String> result = new LinkedHashSet<>();

		result.add(d.getSupplierID());

		for (String id : getBackwardDeliveries(d)) {
			result.add(deliveryMap.get(id).getSupplierID());
		}

		return result;
	}

	private Set<String> getForwardStations(Delivery d) {
		Set<String> result = new LinkedHashSet<>();

		result.add(d.getRecipientID());

		for (String id : getForwardDeliveries(d)) {
			result.add(deliveryMap.get(id).getRecipientID());
		}

		return result;
	}

	// e.g. Jan 2012 vs. 18.Jan 2012 - be generous
	private boolean isInTemporalOrder(Delivery in, Delivery out) {
		Integer yearOut = out.getDepartureYear();
		Integer yearIn = in.getArrivalYear();

		if (yearOut == null || yearIn == null) {
			return true;
		} else if (yearOut > yearIn) {
			return true;
		} else if (yearOut < yearIn) {
			return false;
		}

		Integer monthOut = out.getDepartureMonth();
		Integer monthIn = in.getArrivalMonth();

		if (monthOut == null || monthIn == null) {
			return true;
		} else if (monthOut > monthIn) {
			return true;
		} else if (monthOut < monthIn) {
			return false;
		}

		Integer dayOut = out.getDepartureDay();
		Integer dayIn = in.getArrivalDay();

		if (dayOut == null || dayIn == null) {
			return true;
		} else if (dayOut >= dayIn) {
			return true;
		}

		return false;
	}

	public static final class Result {

		private Map<String, Double> stationScores;
		private Map<String, Double> deliveryScores;
		private Map<String, Set<String>> forwardStationsByStation;
		private Map<String, Set<String>> backwardStationsByStation;
		private Map<String, Set<String>> forwardDeliveriesByStation;
		private Map<String, Set<String>> backwardDeliveriesByStation;
		private Map<String, Set<String>> forwardStationsByDelivery;
		private Map<String, Set<String>> backwardStationsByDelivery;
		private Map<String, Set<String>> forwardDeliveriesByDelivery;
		private Map<String, Set<String>> backwardDeliveriesByDelivery;

		private Result() {
			stationScores = new LinkedHashMap<>();
			deliveryScores = new LinkedHashMap<>();
			forwardStationsByStation = new LinkedHashMap<>();
			backwardStationsByStation = new LinkedHashMap<>();
			forwardDeliveriesByStation = new LinkedHashMap<>();
			backwardDeliveriesByStation = new LinkedHashMap<>();
			forwardStationsByDelivery = new LinkedHashMap<>();
			backwardStationsByDelivery = new LinkedHashMap<>();
			forwardDeliveriesByDelivery = new LinkedHashMap<>();
			backwardDeliveriesByDelivery = new LinkedHashMap<>();
		}

		public Map<String, Double> getStationScores() {
			return stationScores;
		}

		public Map<String, Double> getDeliveryScores() {
			return deliveryScores;
		}

		public Map<String, Set<String>> getForwardStationsByStation() {
			return forwardStationsByStation;
		}

		public Map<String, Set<String>> getBackwardStationsByStation() {
			return backwardStationsByStation;
		}

		public Map<String, Set<String>> getForwardDeliveriesByStation() {
			return forwardDeliveriesByStation;
		}

		public Map<String, Set<String>> getBackwardDeliveriesByStation() {
			return backwardDeliveriesByStation;
		}

		public Map<String, Set<String>> getForwardStationsByDelivery() {
			return forwardStationsByDelivery;
		}

		public Map<String, Set<String>> getBackwardStationsByDelivery() {
			return backwardStationsByDelivery;
		}

		public Map<String, Set<String>> getForwardDeliveriesByDelivery() {
			return forwardDeliveriesByDelivery;
		}

		public Map<String, Set<String>> getBackwardDeliveriesByDelivery() {
			return backwardDeliveriesByDelivery;
		}
	}
}
