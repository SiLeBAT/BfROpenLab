/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import de.bund.bfr.knime.openkrise.common.Delivery;

public class Tracing {

	private static enum ScoreType {
		COMBINED {
			@Override
			public double getWeight(double weight) {
				return weight;
			}
		},
		POSITIVE {
			@Override
			public double getWeight(double weight) {
				return Math.max(weight, 0.0);
			}
		},
		NEGATIVE {
			@Override
			public double getWeight(double weight) {
				return Math.max(-weight, 0.0);
			}
		};

		public abstract double getWeight(double weight);
	}

	private Map<String, Delivery> deliveries;
	private Map<String, Set<String>> lotDeliveries;
	private Map<String, Double> stationWeights;
	private Map<String, Double> deliveryWeights;
	private Set<String> ccStations;
	private Set<String> ccDeliveries;
	private Set<String> killContaminationStations;
	private Set<String> killContaminationDeliveries;
	private Map<String, String> mergedTo;

	private transient Map<String, String> suppliers;
	private transient Map<String, String> recipients;
	private transient SetMultimap<String, String> previousDeliveries;
	private transient SetMultimap<String, String> nextDeliveries;
	private transient SetMultimap<String, String> incomingDeliveries;
	private transient SetMultimap<String, String> outgoingDeliveries;
	private transient Map<String, Set<String>> backwardDeliveries;
	private transient Map<String, Set<String>> forwardDeliveries;
	private transient Map<String, Set<String>> forwardDeliveriesOfLot;
	private transient double positiveWeightSum;
	private transient double negativeWeightSum;

	public Tracing(Iterable<Delivery> deliveries) {
		Set<String> allIds = new LinkedHashSet<>();

		for (Delivery d : deliveries) {
			allIds.add(d.getId());
		}

		this.deliveries = new LinkedHashMap<>();
		this.lotDeliveries = new LinkedHashMap<>();

		for (Delivery d : deliveries) {
			this.deliveries.put(d.getId(), d);
			String lotId = d.getLotId(); // d.getSupplierId() + "_" +
			if (!this.lotDeliveries.containsKey(lotId))
				this.lotDeliveries.put(lotId, new HashSet<>());
			this.lotDeliveries.get(lotId).add(d.getId());
		}

		stationWeights = new LinkedHashMap<>();
		ccStations = new LinkedHashSet<>();
		deliveryWeights = new LinkedHashMap<>();
		ccDeliveries = new LinkedHashSet<>();
		killContaminationStations = new LinkedHashSet<>();
		killContaminationDeliveries = new LinkedHashSet<>();
		mergedTo = new LinkedHashMap<>();
	}

	public void setStationWeight(String stationId, double weight) {
		if (weight == 0.0) {
			stationWeights.remove(stationId);
		} else {
			stationWeights.put(stationId, weight);
		}
	}

	public void setDeliveryWeight(String deliveryId, double weight) {
		if (weight == 0.0) {
			deliveryWeights.remove(deliveryId);
		} else {
			deliveryWeights.put(deliveryId, weight);
		}
	}

	public void setCrossContaminationOfStation(String stationId, boolean enabled) {
		if (enabled) {
			ccStations.add(stationId);
		} else {
			ccStations.remove(stationId);
		}
	}

	public void setCrossContaminationOfDelivery(String deliveryId, boolean enabled) {
		if (enabled) {
			ccDeliveries.add(deliveryId);
		} else {
			ccDeliveries.remove(deliveryId);
		}
	}

	public void setKillContaminationOfStation(String stationId, boolean enabled) {
		if (enabled) {
			killContaminationStations.add(stationId);
		} else {
			killContaminationStations.remove(stationId);
		}
	}

	public void setKillContaminationOfDelivery(String deliveryId, boolean enabled) {
		if (enabled) {
			killContaminationDeliveries.add(deliveryId);
		} else {
			killContaminationDeliveries.remove(deliveryId);
		}
	}

	public void mergeStations(Set<String> toBeMerged, String mergedStationId) {
		toBeMerged.forEach(s -> mergedTo.put(s, mergedStationId));
	}

	public Result getResult(boolean enforceTemporalOrder) {
		if (deliveries.isEmpty()) {
			return new Result();
		}

		positiveWeightSum = 0.0;
		negativeWeightSum = 0.0;

		for (double w : Iterables.concat(stationWeights.values(), deliveryWeights.values())) {
			if (w > 0.0) {
				positiveWeightSum += w;
			} else {
				negativeWeightSum -= w;
			}
		}

		suppliers = new LinkedHashMap<>();
		recipients = new LinkedHashMap<>();
		previousDeliveries = LinkedHashMultimap.create();
		nextDeliveries = LinkedHashMultimap.create();
		incomingDeliveries = LinkedHashMultimap.create();
		outgoingDeliveries = LinkedHashMultimap.create();

		for (Delivery d : deliveries.values()) {
			String supplier = mergedTo.containsKey(d.getSupplierId()) ? mergedTo.get(d.getSupplierId())
					: d.getSupplierId();
			String recipient = mergedTo.containsKey(d.getRecipientId()) ? mergedTo.get(d.getRecipientId())
					: d.getRecipientId();

			suppliers.put(d.getId(), supplier);
			recipients.put(d.getId(), recipient);
			previousDeliveries.putAll(d.getId(), Sets.intersection(d.getAllPreviousIds(), deliveries.keySet()));
			nextDeliveries.putAll(d.getId(), Sets.intersection(d.getAllNextIds(), deliveries.keySet()));
			incomingDeliveries.put(recipient, d.getId());
			outgoingDeliveries.put(supplier, d.getId());
		}

		for (String station : ccStations) {
			for (String in : incomingDeliveries.get(station)) {
				for (String out : outgoingDeliveries.get(station)) {
					if (in.equals(out)) {
						continue;
					}

					if (!enforceTemporalOrder || deliveries.get(in).isBefore(deliveries.get(out))) {
						previousDeliveries.put(out, in);
						nextDeliveries.put(in, out);
					}
				}
			}
		}

		for (String in1 : ccDeliveries) {
			for (String in2 : ccDeliveries) {
				if (in1.equals(in2) || !recipients.get(in1).equals(recipients.get(in2))) {
					continue;
				}

				for (String out1 : nextDeliveries.get(in1)) {
					if (!enforceTemporalOrder || deliveries.get(in2).isBefore(deliveries.get(out1))) {
						previousDeliveries.put(out1, in2);
						nextDeliveries.put(in2, out1);
					}
				}

				for (String out2 : nextDeliveries.get(in2)) {
					if (!enforceTemporalOrder || deliveries.get(in1).isBefore(deliveries.get(out2))) {
						previousDeliveries.put(out2, in1);
						nextDeliveries.put(in1, out2);
					}
				}
			}
		}

		for (String station : killContaminationStations) {
			for (String in : incomingDeliveries.get(station)) {
				nextDeliveries.removeAll(in);
			}

			for (String out : outgoingDeliveries.get(station)) {
				previousDeliveries.removeAll(out);
			}

			outgoingDeliveries.removeAll(station);
		}

		for (String delivery : killContaminationDeliveries) {
			for (String next : nextDeliveries.get(delivery)) {
				previousDeliveries.remove(next, delivery);
			}

			nextDeliveries.removeAll(delivery);
			incomingDeliveries.remove(recipients.get(delivery), delivery);
		}

		Result result = new Result();

		backwardDeliveries = new LinkedHashMap<>();
		forwardDeliveries = new LinkedHashMap<>();
		forwardDeliveriesOfLot = new LinkedHashMap<>();

		for (String s : Sets.union(incomingDeliveries.keySet(), outgoingDeliveries.keySet())) {
			result.stationScores.put(s, getStationScore(s, ScoreType.COMBINED));
			result.stationPositiveScores.put(s, getStationScore(s, ScoreType.POSITIVE));
			result.stationNegativeScores.put(s, getStationScore(s, ScoreType.NEGATIVE));
			result.forwardStationsByStation.putAll(s, getForwardStationsOfStation(s));
			result.backwardStationsByStation.putAll(s, getBackwardStationsOfStation(s));
			result.forwardDeliveriesByStation.putAll(s, getForwardDeliveriesOfStation(s));
			result.backwardDeliveriesByStation.putAll(s, getBackwardDeliveriesOfStation(s));
		}

		for (String d : deliveries.keySet()) {
			result.deliveryScores.put(d, getDeliveryScore(d, ScoreType.COMBINED));
			result.deliveryPositiveScores.put(d, getDeliveryScore(d, ScoreType.POSITIVE));
			result.deliveryNegativeScores.put(d, getDeliveryScore(d, ScoreType.NEGATIVE));
			result.forwardStationsByDelivery.putAll(d, getForwardStationsOfDelivery(d));
			result.backwardStationsByDelivery.putAll(d, getBackwardStationsOfDelivery(d));
			result.forwardDeliveriesByDelivery.putAll(d, getForwardDeliveriesOfDelivery(d));
			result.backwardDeliveriesByDelivery.putAll(d, getBackwardDeliveriesOfDelivery(d));
		}

		lotDeliveries.forEach((lot, deliveries) -> {
			for (String d : deliveries) {
				result.lotScores.put(d, getLotScore(lot, ScoreType.COMBINED));
			}
		});

		double maxScore = Math.max(Collections.max(result.stationScores.values()),
				Collections.max(result.deliveryScores.values()));
		double minScore = Math.min(Collections.min(result.stationScores.values()),
				Collections.min(result.deliveryScores.values()));
		double maxAbs = Math.max(maxScore, -minScore);

		if (maxAbs > 0.0) {
			result.scoreNormalizer = maxAbs;
		}

		return result;
	}

	private double getStationScore(String id, ScoreType type) {
		double denom = getDenom(type);

		if (denom == 0.0) {
			return 0.0;
		}

		double sum = type.getWeight(nullToZero(stationWeights.get(id)));

		for (String stationId : getForwardStationsOfStation(id)) {
			sum += type.getWeight(nullToZero(stationWeights.get(stationId)));
		}

		for (String deliveryId : getForwardDeliveriesOfStation(id)) {
			sum += type.getWeight(nullToZero(deliveryWeights.get(deliveryId)));
		}

		return sum / denom;
	}

	private double getDeliveryScore(String id, ScoreType type) {
		double denom = getDenom(type);

		if (denom == 0.0) {
			return 0.0;
		}

		double sum = type.getWeight(nullToZero(deliveryWeights.get(id)));

		for (String stationId : getForwardStationsOfDelivery(id)) {
			sum += type.getWeight(nullToZero(stationWeights.get(stationId)));
		}

		for (String deliveryId : getForwardDeliveriesOfDelivery(id)) {
			sum += type.getWeight(nullToZero(deliveryWeights.get(deliveryId)));
		}

		return sum / denom;
	}

	private double getLotScore(String id, ScoreType type) {
		double denom = getDenom(type);

		if (denom == 0.0) {
			return 0.0;
		}

		double sum = 0.0;

		for (String stationId : getForwardStationsOfLot(id)) {
			sum += type.getWeight(nullToZero(stationWeights.get(stationId)));
		}

		for (String deliveryId : getForwardDeliveriesOfLot(id)) {
			sum += type.getWeight(nullToZero(deliveryWeights.get(deliveryId)));
		}

		return sum / denom;
	}

	private double getDenom(ScoreType type) {
		switch (type) {
		case COMBINED:
			return Math.max(positiveWeightSum, negativeWeightSum);
		case POSITIVE:
			return positiveWeightSum;
		case NEGATIVE:
			return negativeWeightSum;
		default:
			throw new RuntimeException("Unknown ScoreType: " + type);
		}
	}

	private Set<String> getForwardStationsOfStation(String station) {
		Set<String> stations = new LinkedHashSet<>();

		for (String out : outgoingDeliveries.get(station)) {
			stations.addAll(getForwardStationsOfDelivery(out));
		}

		return stations;
	}

	private Set<String> getBackwardStationsOfStation(String station) {
		Set<String> stations = new LinkedHashSet<>();

		for (String in : incomingDeliveries.get(station)) {
			stations.addAll(getBackwardStationsOfDelivery(in));
		}

		return stations;
	}

	private Set<String> getForwardDeliveriesOfStation(String station) {
		Set<String> forward = new LinkedHashSet<>();

		for (String out : outgoingDeliveries.get(station)) {
			forward.add(out);
			forward.addAll(getForwardDeliveriesOfDelivery(out));
		}

		return forward;
	}

	private Set<String> getBackwardDeliveriesOfStation(String station) {
		Set<String> backward = new LinkedHashSet<>();

		for (String in : incomingDeliveries.get(station)) {
			backward.add(in);
			backward.addAll(getBackwardDeliveriesOfDelivery(in));
		}

		return backward;
	}

	private Set<String> getBackwardDeliveriesOfDelivery(String delivery) {
		Set<String> backward = backwardDeliveries.get(delivery);

		if (backward != null) {
			return backward;
		}

		backward = new LinkedHashSet<>();

		for (String previous : previousDeliveries.get(delivery)) {
			if (!previous.equals(delivery)) {
				backward.add(previous);
				backward.addAll(getBackwardDeliveriesOfDelivery(previous));
			}
		}

		backwardDeliveries.put(delivery, backward);

		return backward;
	}

	private Set<String> getForwardDeliveriesOfDelivery(String delivery) {
		Set<String> forward = forwardDeliveries.get(delivery);

		if (forward != null) {
			return forward;
		}

		forward = new LinkedHashSet<>();

		for (String next : nextDeliveries.get(delivery)) {
			if (!next.equals(delivery)) {
				forward.add(next);
				forward.addAll(getForwardDeliveriesOfDelivery(next));
			}
		}

		forwardDeliveries.put(delivery, forward);

		return forward;
	}

	private Set<String> getForwardDeliveriesOfLot(String lot) {
		Set<String> forward = forwardDeliveriesOfLot.get(lot);

		if (forward != null) {
			return forward;
		}

		forward = new LinkedHashSet<>();

		for (String delivery : lotDeliveries.get(lot)) {
			for (String next : nextDeliveries.get(delivery)) {
				if (!next.equals(delivery)) {
					forward.add(next);
					forward.addAll(getForwardDeliveriesOfDelivery(next));
				}
			}
		}

		forwardDeliveriesOfLot.put(lot, forward);

		return forward;
	}

	private Set<String> getForwardStationsOfLot(String lotId) {
		Set<String> result = new LinkedHashSet<>();

		for (String deliveryId : lotDeliveries.get(lotId)) {
			if (!killContaminationDeliveries.contains(deliveryId)) {
				result.add(recipients.get(deliveryId));
			}

			for (String forward : getForwardDeliveriesOfDelivery(deliveryId)) {
				result.add(recipients.get(forward));
			}
		}

		return result;
	}

	private Set<String> getBackwardStationsOfDelivery(String delivery) {
		Set<String> result = new LinkedHashSet<>();
		String supplier = suppliers.get(delivery);

		if (!killContaminationStations.contains(supplier)) {
			result.add(supplier);
		}

		for (String backward : getBackwardDeliveriesOfDelivery(delivery)) {
			result.add(suppliers.get(backward));
		}

		return result;
	}

	private Set<String> getForwardStationsOfDelivery(String deliveryId) {
		Set<String> result = new LinkedHashSet<>();

		if (!killContaminationDeliveries.contains(deliveryId)) {
			result.add(recipients.get(deliveryId));
		}

		for (String forward : getForwardDeliveriesOfDelivery(deliveryId)) {
			result.add(recipients.get(forward));
		}

		return result;
	}

	private static double nullToZero(Double score) {
		return score != null ? score : 0.0;
	}

	public static final class Result {

		private Map<String, Double> stationScores;
		private Map<String, Double> stationPositiveScores;
		private Map<String, Double> stationNegativeScores;
		private Map<String, Double> deliveryScores;
		private Map<String, Double> lotScores;
		private Map<String, Double> deliveryPositiveScores;
		private Map<String, Double> deliveryNegativeScores;

		private SetMultimap<String, String> forwardStationsByStation;
		private SetMultimap<String, String> backwardStationsByStation;
		private SetMultimap<String, String> forwardDeliveriesByStation;
		private SetMultimap<String, String> backwardDeliveriesByStation;
		private SetMultimap<String, String> forwardStationsByDelivery;
		private SetMultimap<String, String> backwardStationsByDelivery;
		private SetMultimap<String, String> forwardDeliveriesByDelivery;
		private SetMultimap<String, String> backwardDeliveriesByDelivery;

		private double scoreNormalizer;

		private Result() {
			stationScores = new LinkedHashMap<>();
			stationPositiveScores = new LinkedHashMap<>();
			stationNegativeScores = new LinkedHashMap<>();
			deliveryScores = new LinkedHashMap<>();
			lotScores = new LinkedHashMap<>();
			deliveryPositiveScores = new LinkedHashMap<>();
			deliveryNegativeScores = new LinkedHashMap<>();
			forwardStationsByStation = LinkedHashMultimap.create();
			backwardStationsByStation = LinkedHashMultimap.create();
			forwardDeliveriesByStation = LinkedHashMultimap.create();
			backwardDeliveriesByStation = LinkedHashMultimap.create();
			forwardStationsByDelivery = LinkedHashMultimap.create();
			backwardStationsByDelivery = LinkedHashMultimap.create();
			forwardDeliveriesByDelivery = LinkedHashMultimap.create();
			backwardDeliveriesByDelivery = LinkedHashMultimap.create();
			scoreNormalizer = 1.0;
		}

		public double getStationScore(String id) {
			return nullToZero(stationScores.get(id));
		}

		public double getStationNormalizedScore(String id) {
			return nullToZero(stationScores.get(id)) / scoreNormalizer;
		}

		public double getStationPositiveScore(String id) {
			return nullToZero(stationPositiveScores.get(id));
		}

		public double getStationNegativeScore(String id) {
			return nullToZero(stationNegativeScores.get(id));
		}

		public double getDeliveryScore(String id) {
			return nullToZero(deliveryScores.get(id));
		}

		public double getLotScore(String id) {
			return nullToZero(lotScores.get(id));
		}

		public double getDeliveryNormalizedScore(String id) {
			return nullToZero(deliveryScores.get(id)) / scoreNormalizer;
		}

		public double getDeliveryPositiveScore(String id) {
			return nullToZero(deliveryPositiveScores.get(id));
		}

		public double getDeliveryNegativeScore(String id) {
			return nullToZero(deliveryNegativeScores.get(id));
		}

		public SetMultimap<String, String> getForwardStationsByStation() {
			return forwardStationsByStation;
		}

		public SetMultimap<String, String> getBackwardStationsByStation() {
			return backwardStationsByStation;
		}

		public SetMultimap<String, String> getForwardDeliveriesByStation() {
			return forwardDeliveriesByStation;
		}

		public SetMultimap<String, String> getBackwardDeliveriesByStation() {
			return backwardDeliveriesByStation;
		}

		public SetMultimap<String, String> getForwardStationsByDelivery() {
			return forwardStationsByDelivery;
		}

		public SetMultimap<String, String> getBackwardStationsByDelivery() {
			return backwardStationsByDelivery;
		}

		public SetMultimap<String, String> getForwardDeliveriesByDelivery() {
			return forwardDeliveriesByDelivery;
		}

		public SetMultimap<String, String> getBackwardDeliveriesByDelivery() {
			return backwardDeliveriesByDelivery;
		}
	}
}
