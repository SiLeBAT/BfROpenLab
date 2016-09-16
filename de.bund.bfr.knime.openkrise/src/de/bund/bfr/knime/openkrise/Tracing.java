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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import de.bund.bfr.knime.openkrise.common.Delivery;

public class Tracing {

	private static enum ScoreType {
		COMBINED, POSITIVE, NEGATIVE
	}

	private List<Delivery> deliveries;
	private Map<String, Double> stationWeights;
	private Map<String, Double> deliveryWeights;
	private Set<String> ccStations;
	private Set<String> ccDeliveries;
	private Set<String> killContaminationStations;
	private Set<String> killContaminationDeliveries;

	private transient Map<String, Delivery> deliveryMap;
	private transient SetMultimap<String, String> incomingDeliveries;
	private transient SetMultimap<String, String> outgoingDeliveries;
	private transient Map<String, Set<String>> backwardDeliveries;
	private transient Map<String, Set<String>> forwardDeliveries;
	private transient double positiveWeightSum;
	private transient double negativeWeightSum;

	public Tracing(Collection<Delivery> deliveries) {
		Set<String> allIds = new LinkedHashSet<>();

		for (Delivery d : deliveries) {
			allIds.add(d.getId());
		}

		this.deliveries = new ArrayList<>();

		for (Delivery d : deliveries) {
			Delivery copy = d.copy();

			copy.getAllNextIds().retainAll(allIds);
			copy.getAllPreviousIds().retainAll(allIds);
			this.deliveries.add(copy);
		}

		stationWeights = new LinkedHashMap<>();
		ccStations = new LinkedHashSet<>();
		deliveryWeights = new LinkedHashMap<>();
		ccDeliveries = new LinkedHashSet<>();
		killContaminationStations = new LinkedHashSet<>();
		killContaminationDeliveries = new LinkedHashSet<>();
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
		for (Delivery d : deliveries) {
			if (toBeMerged.contains(d.getSupplierId())) {
				d.setSupplierId(mergedStationId);
			}

			if (toBeMerged.contains(d.getRecipientId())) {
				d.setRecipientId(mergedStationId);
			}
		}
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

		Set<String> stationIds = new LinkedHashSet<>();

		deliveryMap = new LinkedHashMap<>();
		incomingDeliveries = LinkedHashMultimap.create();
		outgoingDeliveries = LinkedHashMultimap.create();

		for (Delivery d : deliveries) {
			stationIds.add(d.getRecipientId());
			stationIds.add(d.getSupplierId());
			deliveryMap.put(d.getId(), d.copy());
			incomingDeliveries.put(d.getRecipientId(), d.getId());
			outgoingDeliveries.put(d.getSupplierId(), d.getId());
		}

		for (String stationId : ccStations) {
			for (String inId : incomingDeliveries.get(stationId)) {
				Delivery in = deliveryMap.get(inId);

				for (String outId : outgoingDeliveries.get(stationId)) {
					if (inId.equals(outId)) {
						continue;
					}

					Delivery out = deliveryMap.get(outId);

					if (!enforceTemporalOrder || in.isBefore(out)) {
						in.getAllNextIds().add(outId);
						out.getAllPreviousIds().add(inId);
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

				if (!in1.getRecipientId().equals(in2.getRecipientId())) {
					continue;
				}

				for (String out1Id : in1.getAllNextIds()) {
					Delivery out1 = deliveryMap.get(out1Id);

					if (!enforceTemporalOrder || in2.isBefore(out1)) {
						in2.getAllNextIds().add(out1Id);
						out1.getAllPreviousIds().add(in2Id);
					}
				}

				for (String out2Id : in2.getAllNextIds()) {
					Delivery out2 = deliveryMap.get(out2Id);

					if (!enforceTemporalOrder || in1.isBefore(out2)) {
						in1.getAllNextIds().add(out2Id);
						out2.getAllPreviousIds().add(in1Id);
					}
				}
			}
		}

		for (String stationId : killContaminationStations) {
			for (String inId : incomingDeliveries.get(stationId)) {
				deliveryMap.get(inId).getAllNextIds().clear();
			}

			for (String outId : outgoingDeliveries.get(stationId)) {
				deliveryMap.get(outId).getAllPreviousIds().clear();
			}

			outgoingDeliveries.removeAll(stationId);
		}

		for (String deliveryId : killContaminationDeliveries) {
			Delivery d = deliveryMap.get(deliveryId);

			for (String next : d.getAllNextIds()) {
				deliveryMap.get(next).getAllPreviousIds().remove(deliveryId);
			}

			d.getAllNextIds().clear();
			incomingDeliveries.get(d.getRecipientId()).remove(deliveryId);
		}

		Result result = new Result();

		backwardDeliveries = new LinkedHashMap<>();
		forwardDeliveries = new LinkedHashMap<>();

		for (String stationId : stationIds) {
			result.stationScores.put(stationId, getStationScore(stationId, ScoreType.COMBINED));
			result.stationPositiveScores.put(stationId, getStationScore(stationId, ScoreType.POSITIVE));
			result.stationNegativeScores.put(stationId, getStationScore(stationId, ScoreType.NEGATIVE));
			result.forwardStationsByStation.putAll(stationId, getForwardStations(stationId));
			result.backwardStationsByStation.putAll(stationId, getBackwardStations(stationId));
			result.forwardDeliveriesByStation.putAll(stationId, getForwardDeliveries(stationId));
			result.backwardDeliveriesByStation.putAll(stationId, getBackwardDeliveries(stationId));
		}

		for (Delivery d : deliveryMap.values()) {
			result.deliveryScores.put(d.getId(), getDeliveryScore(d, ScoreType.COMBINED));
			result.deliveryPositiveScores.put(d.getId(), getDeliveryScore(d, ScoreType.POSITIVE));
			result.deliveryNegativeScores.put(d.getId(), getDeliveryScore(d, ScoreType.NEGATIVE));
			result.forwardStationsByDelivery.putAll(d.getId(), getForwardStations(d));
			result.backwardStationsByDelivery.putAll(d.getId(), getBackwardStations(d));
			result.forwardDeliveriesByDelivery.putAll(d.getId(), getForwardDeliveries(d));
			result.backwardDeliveriesByDelivery.putAll(d.getId(), getBackwardDeliveries(d));
		}

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

		double sum = getWeight(stationWeights.get(id), type);

		for (String stationId : getForwardStations(id)) {
			sum += getWeight(stationWeights.get(stationId), type);
		}

		for (String deliveryId : getForwardDeliveries(id)) {
			sum += getWeight(deliveryWeights.get(deliveryId), type);
		}

		return sum / denom;
	}

	private double getDeliveryScore(Delivery d, ScoreType type) {
		double denom = getDenom(type);

		if (denom == 0.0) {
			return 0.0;
		}

		double sum = getWeight(deliveryWeights.get(d.getId()), type);

		for (String stationId : getForwardStations(d)) {
			sum += getWeight(stationWeights.get(stationId), type);
		}

		for (String deliveryId : getForwardDeliveries(d)) {
			sum += getWeight(deliveryWeights.get(deliveryId), type);
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

	private Set<String> getForwardStations(String stationID) {
		Set<String> stations = new LinkedHashSet<>();

		for (String id : outgoingDeliveries.get(stationID)) {
			stations.addAll(getForwardStations(deliveryMap.get(id)));
		}

		return stations;
	}

	private Set<String> getBackwardStations(String stationID) {
		Set<String> stations = new LinkedHashSet<>();

		for (String id : incomingDeliveries.get(stationID)) {
			stations.addAll(getBackwardStations(deliveryMap.get(id)));
		}

		return stations;
	}

	private Set<String> getForwardDeliveries(String stationID) {
		Set<String> forward = new LinkedHashSet<>();

		for (String id : outgoingDeliveries.get(stationID)) {
			forward.add(id);
			forward.addAll(getForwardDeliveries(deliveryMap.get(id)));
		}

		return forward;
	}

	private Set<String> getBackwardDeliveries(String stationID) {
		Set<String> backward = new LinkedHashSet<>();

		for (String id : incomingDeliveries.get(stationID)) {
			backward.add(id);
			backward.addAll(getBackwardDeliveries(deliveryMap.get(id)));
		}

		return backward;
	}

	private Set<String> getBackwardDeliveries(Delivery d) {
		Set<String> backward = backwardDeliveries.get(d.getId());

		if (backward != null) {
			return backward;
		}

		backward = new LinkedHashSet<>();

		for (String prev : d.getAllPreviousIds()) {
			if (!prev.equals(d.getId())) {
				backward.add(prev);
				backward.addAll(getBackwardDeliveries(deliveryMap.get(prev)));
			}
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

		for (String next : d.getAllNextIds()) {
			if (!next.equals(d.getId())) {
				forward.add(next);
				forward.addAll(getForwardDeliveries(deliveryMap.get(next)));
			}
		}

		forwardDeliveries.put(d.getId(), forward);

		return forward;
	}

	private Set<String> getBackwardStations(Delivery d) {
		Set<String> result = new LinkedHashSet<>();

		if (!killContaminationStations.contains(d.getSupplierId())) {
			result.add(d.getSupplierId());
		}

		for (String id : getBackwardDeliveries(d)) {
			result.add(deliveryMap.get(id).getSupplierId());
		}

		return result;
	}

	private Set<String> getForwardStations(Delivery d) {
		Set<String> result = new LinkedHashSet<>();

		if (!killContaminationDeliveries.contains(d.getId())) {
			result.add(d.getRecipientId());
		}

		for (String id : getForwardDeliveries(d)) {
			result.add(deliveryMap.get(id).getRecipientId());
		}

		return result;
	}

	private static double getWeight(Double weight, ScoreType type) {
		if (weight == null) {
			return 0.0;
		}

		switch (type) {
		case COMBINED:
			return weight;
		case POSITIVE:
			return weight > 0.0 ? weight : 0.0;
		case NEGATIVE:
			return weight < 0.0 ? -weight : 0.0;
		default:
			throw new RuntimeException("Unknown ScoreType: " + type);
		}
	}

	public static final class Result {

		private Map<String, Double> stationScores;
		private Map<String, Double> stationPositiveScores;
		private Map<String, Double> stationNegativeScores;
		private Map<String, Double> deliveryScores;
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

		private static double nullToZero(Double score) {
			return score != null ? score : 0.0;
		}
	}
}
