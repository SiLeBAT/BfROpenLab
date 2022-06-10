/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.common;

import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.CHARGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.CHARGENVERBINDUNGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.LIEFERUNGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.PRODUKTKATALOG;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.STATION;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.impl.DSL;

import com.google.common.base.Splitter;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

public class DeliveryUtils {

	private static final String WITHOUT_SUPPLIER = "Deliveries without supplier";
	private static final String WITHOUT_RECIPIENT = "Deliveries without recipient";
	private static final String INCONSISTENT_AMOUNT_DECLARATIONS = "Deliveries with inconsistent amount declarations";
	private static final String INGREDIENT_OF_ITSELF = "Deliveries being ingredient of itself";
	private static final String INCONSISTENT_DATES = "Dates are inconsistent for following deliveries";
	private static final String AMOUNTS_INCORRECT = "Amounts might be incorrect";

	private DeliveryUtils() {
	}

	public static boolean hasUniqueSerials(Connection conn) {
		Set<String> stationSerials = new LinkedHashSet<>();

		for (Record1<String> r : DSL.using(conn, SQLDialect.HSQLDB).select(STATION.SERIAL).from(STATION)) {
			if (r.value1() == null || !stationSerials.add(r.value1())) {
				return false;
			}
		}

		Set<String> deliverySerials = new LinkedHashSet<>();

		for (Record1<String> r : DSL.using(conn, SQLDialect.HSQLDB).select(LIEFERUNGEN.SERIAL).from(LIEFERUNGEN)) {
			if (r.value1() == null || !deliverySerials.add(r.value1())) {
				return false;
			}
		}

		return true;
	}
	public static boolean hasOnlyPositiveIDs(Connection conn) {
		for (Record1<Integer> r : DSL.using(conn, SQLDialect.HSQLDB).select(STATION.ID).from(STATION)) {
			if (r.value1() < 0) return false;
		}

		for (Record1<Integer> r : DSL.using(conn, SQLDialect.HSQLDB).select(LIEFERUNGEN.ID).from(LIEFERUNGEN)) {
			if (r.value1() < 0) return false;
		}

		return true;
	}

	public static Map<Integer, String> getStationIds(Connection conn, boolean useSerialAsID) {
		Map<Integer, String> stationIds = new LinkedHashMap<>();

		for (Record r : DSL.using(conn, SQLDialect.HSQLDB).select().from(STATION)) {
			stationIds.put(r.getValue(STATION.ID),
					useSerialAsID ? r.getValue(STATION.SERIAL) : r.getValue(STATION.ID).toString());
		}

		return stationIds;
	}

	public static Map<Integer, String> getDeliveryIds(Connection conn, boolean useSerialAsID) {
		Map<Integer, String> deliveryIds = new LinkedHashMap<>();

		for (Record r : DSL.using(conn, SQLDialect.HSQLDB).select().from(LIEFERUNGEN)) {
			deliveryIds.put(r.getValue(LIEFERUNGEN.ID),
					useSerialAsID ? r.getValue(LIEFERUNGEN.SERIAL) : r.getValue(LIEFERUNGEN.ID).toString());
		}

		return deliveryIds;
	}

	public static Double getAmountInKg(Double value, String unit) {
		if (value == null && unit != null) {
			int did = unit.trim().indexOf(" ");
			try {
				return did > 0 ? Double.parseDouble(unit.trim().substring(0, did)) : null;
			} catch (NumberFormatException e) {
				return null;
			}
		}
		if (value == null || unit == null) {
			return null;
		}

		List<String> units = Arrays.asList("kg", "g", "t");
		String unitPart = null;

		for (String part : Splitter.on(" ").split(unit)) {
			if (part.matches(".*\\d.*") || units.contains(part.toLowerCase())) {
				if (unitPart != null) {
					return null;
				}

				unitPart = part;
			}
		}

		if (unitPart == null) {
			return null;
		}

		String numberPart = null;
		Double factor = null;

		if (unitPart.toLowerCase().endsWith("kg")) {
			numberPart = unitPart.substring(0, unitPart.length() - 2);
			factor = 1.0;
		} else if (unitPart.toLowerCase().endsWith("g")) {
			numberPart = unitPart.substring(0, unitPart.length() - 1);
			factor = 0.001;
		} else if (unitPart.toLowerCase().endsWith("t")) {
			numberPart = unitPart.substring(0, unitPart.length() - 1);
			factor = 1000.0;
		} else {
			return null;
		}

		if (numberPart.isEmpty()) {
			return factor * value;
		}

		try {
			return Double.parseDouble(numberPart) * factor * value;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String formatDate(Integer day, Integer month, Integer year) {
		if (year == null) {
			return null;
		}

		Date date = new GregorianCalendar(year, month != null ? month - 1 : 0, day != null ? day : 1).getTime();

		if (month == null) {
			return new SimpleDateFormat("yyyy").format(date);
		} else if (day == null) {
			return new SimpleDateFormat("yyyy-MM").format(date);
		} else {
			return new SimpleDateFormat("yyyy-MM-dd").format(date);
		}
	}

	public static Map<String, Set<String>> getWarnings(Connection conn) {
		boolean useSerialAsID = hasUniqueSerials(conn);
		SetMultimap<String, String> warnings = LinkedHashMultimap.create();

		getDeliveries(conn, getStationIds(conn, useSerialAsID), getDeliveryIds(conn, useSerialAsID), warnings);

		return Multimaps.asMap(warnings);
	}

	public static List<Delivery> getDeliveries(Connection conn, Map<Integer, String> stationIds,
			Map<Integer, String> deliveryIds, SetMultimap<String, String> warnings) {
		Map<String, Delivery.Builder> builders = new LinkedHashMap<>();
		Select<Record> deliverySelect = DSL.using(conn, SQLDialect.HSQLDB).select().from(LIEFERUNGEN)
				.leftOuterJoin(CHARGEN).on(LIEFERUNGEN.CHARGE.equal(CHARGEN.ID)).leftOuterJoin(PRODUKTKATALOG)
				.on(CHARGEN.ARTIKEL.equal(PRODUKTKATALOG.ID));

		for (Record r : deliverySelect) {
			Integer id = r.getValue(LIEFERUNGEN.ID);
			Integer from = r.getValue(PRODUKTKATALOG.STATION);
			Integer to = r.getValue(LIEFERUNGEN.EMPFÄNGER);
			boolean invalid = false;

			if (from == null) {
				warnings.put(WITHOUT_SUPPLIER, deliveryIds.get(id));
				invalid = true;
			}

			if (to == null) {
				warnings.put(WITHOUT_RECIPIENT, deliveryIds.get(id));
				invalid = true;
			}

			if (invalid) {
				continue;
			}

			String lotNumber = r.getValue(CHARGEN.CHARGENNR) != null ? r.getValue(CHARGEN.CHARGENNR)
					: r.getValue(CHARGEN.ID).toString();
			Double amountInKg1 = getAmountInKg(r.getValue(LIEFERUNGEN.UNITMENGE), r.getValue(LIEFERUNGEN.UNITEINHEIT));
			Double amountInKg2 = getAmountInKg(r.getValue(LIEFERUNGEN.NUMPU), r.getValue(LIEFERUNGEN.TYPEPU));

			if (amountInKg1 != null && amountInKg2 != null && !amountInKg1.equals(amountInKg2)) {
				warnings.put(INCONSISTENT_AMOUNT_DECLARATIONS,
						id + ": " + amountInKg1 + " kg vs. " + amountInKg2 + " kg");
			}

			builders.put(deliveryIds.get(id),
					new Delivery.Builder(deliveryIds.get(id), stationIds.get(from), stationIds.get(to))
							.departure(r.getValue(LIEFERUNGEN.DD_YEAR), r.getValue(LIEFERUNGEN.DD_MONTH),
									r.getValue(LIEFERUNGEN.DD_DAY))
							.arrival(r.getValue(LIEFERUNGEN.AD_YEAR), r.getValue(LIEFERUNGEN.AD_MONTH),
									r.getValue(LIEFERUNGEN.AD_DAY))
							.lot(lotNumber).lotId(r.getValue(CHARGEN.ID).toString())
							.amount(r.getValue(LIEFERUNGEN.NUMPU), r.getValue(LIEFERUNGEN.TYPEPU),
									amountInKg1 != null ? amountInKg1 : amountInKg2));
		}

		SetMultimap<String, String> previousDeliveries = LinkedHashMultimap.create();
		SetMultimap<String, String> nextDeliveries = LinkedHashMultimap.create();
		Select<Record> deliveryToDeliverySelect = DSL.using(conn, SQLDialect.HSQLDB).select().from(CHARGENVERBINDUNGEN)
				.leftOuterJoin(LIEFERUNGEN).on(CHARGENVERBINDUNGEN.PRODUKT.equal(LIEFERUNGEN.CHARGE));

		for (Record r : deliveryToDeliverySelect) {
			String from = deliveryIds.get(r.getValue(CHARGENVERBINDUNGEN.ZUTAT));
			String to = deliveryIds.get(r.getValue(LIEFERUNGEN.ID));

			if (!builders.containsKey(from) || !builders.containsKey(to)) {
				continue;
			}

			if (from.equals(to)) {
				warnings.put(INGREDIENT_OF_ITSELF, from);
				continue;
			}

			previousDeliveries.put(to, from);
			nextDeliveries.put(from, to);
		}

		Map<String, Delivery> deliveries = new LinkedHashMap<>();

		builders.forEach((id, builder) -> deliveries.put(id,
				builder.connectedDeliveries(previousDeliveries.get(id), nextDeliveries.get(id)).build()));

		checkDates(deliveries, warnings);
		checkAmounts(deliveries, warnings);

		return new ArrayList<>(deliveries.values());
	}

	private static void checkDates(Map<String, Delivery> deliveries, SetMultimap<String, String> warnings) {
		for (Delivery d : deliveries.values()) {
			for (String nextId : d.getAllNextIds()) {
				Delivery next = deliveries.get(nextId);

				if (!d.isBefore(next)) {
					warnings.put(INCONSISTENT_DATES, "In: \"" + d.getLot() + "\" ("
							+ formatDate(d.getArrivalDay(), d.getArrivalMonth(), d.getArrivalYear()) + ") vs. Out: \""
							+ next.getLot() + "\" ("
							+ formatDate(next.getDepartureDay(), next.getDepartureMonth(), next.getDepartureYear())
							+ ")");
				}
			}
		}
	}

	private static void checkAmounts(Map<String, Delivery> deliveries, SetMultimap<String, String> warnings) {
		SetMultimap<String, Delivery> deliveriesByLot = LinkedHashMultimap.create();

		for (Delivery d : deliveries.values()) {
			deliveriesByLot.put(d.getLot(), d);
		}

		for (Map.Entry<String, Set<Delivery>> lot : Multimaps.asMap(deliveriesByLot).entrySet()) {
			Set<String> ingredients = new LinkedHashSet<>();
			Double kgOut = 0.0;
			Double amountOut = 0.0;
			String unitOut = null;

			for (Delivery d : lot.getValue()) {
				ingredients = d.getAllPreviousIds();

				if (kgOut != null) {
					if (d.getAmountInKg() != null) {
						kgOut += d.getAmountInKg();
					} else {
						kgOut = null;
					}
				}

				if (amountOut != null) {
					if (d.getAmount() != null && d.getUnit() != null
							&& (unitOut == null || unitOut.equals(d.getUnit()))) {
						amountOut += d.getAmount();
						unitOut = d.getUnit();
					} else {
						amountOut = null;
					}
				}
			}

			if (ingredients.isEmpty()) {
				continue;
			}

			Double kgIn = 0.0;
			Double amountIn = 0.0;
			String unitIn = null;

			for (String prev : ingredients) {
				Delivery d = deliveries.get(prev);

				if (kgIn != null) {
					if (d.getAmountInKg() != null) {
						kgIn += d.getAmountInKg();
					} else {
						kgIn = null;
					}
				}

				if (amountIn != null) {
					if (d.getAmount() != null && d.getUnit() != null
							&& (unitIn == null || unitIn.equals(d.getUnit()))) {
						amountIn += d.getAmount();
						unitIn = d.getUnit();
					} else {
						amountIn = null;
					}
				}
			}

			if (amountIn != null && unitIn != null && amountOut != null && unitOut != null && unitIn.equals(unitOut)
					&& areTooDifferent(amountIn, amountOut)) {
				warnings.put(AMOUNTS_INCORRECT,
						"Lot=" + lot.getKey() + ": In=" + amountIn + " vs. Out=" + amountOut + " (" + unitOut + ")");
			} else if (kgIn != null && kgOut != null && areTooDifferent(kgIn, kgOut)) {
				warnings.put(AMOUNTS_INCORRECT, "Lot=" + lot.getKey() + ": In=" + kgIn + "kg vs. Out=" + kgOut + "kg");
			}
		}
	}

	private static boolean areTooDifferent(double amount1, double amount2) {
		return Math.max(amount1, amount2) > 2.0 * Math.min(amount1, amount2);
	}
}
