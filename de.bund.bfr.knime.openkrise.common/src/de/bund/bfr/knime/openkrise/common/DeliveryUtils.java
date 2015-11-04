package de.bund.bfr.knime.openkrise.common;

import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.CHARGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.CHARGENVERBINDUNGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.LIEFERUNGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.PRODUKTKATALOG;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.STATION;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
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

	public static boolean isSerialPossible(Connection conn) {
		Set<String> stationSerials = new LinkedHashSet<>();

		for (Record1<String> r : DSL.using(conn, SQLDialect.HSQLDB).select(STATION.SERIAL).from(STATION)) {
			if (r.value1() == null || !stationSerials.add(r.value1())) {
				return false;
			}
		}

		Set<String> deliverySerials = new LinkedHashSet<>();
		boolean alwaysKg = true;

		for (Record2<String, String> r : DSL.using(conn, SQLDialect.HSQLDB)
				.select(LIEFERUNGEN.SERIAL, LIEFERUNGEN.UNITEINHEIT).from(LIEFERUNGEN)) {
			if (r.value1() == null || !deliverySerials.add(r.value1())) {
				return false;
			}

			if (!"kg".equals(r.value2())) {
				alwaysKg = false;
			}
		}

		if (alwaysKg) {
			// beim EFSA Importer wurde immer kg eingetragen, später beim
			// bfrnewimporter wurde nur noch "numPU" und "typePU" benutzt
			// und UnitEinheit müsste immer NULL sein, daher ist das ein
			// sehr gutes Indiz dafür, dass wir es mit alten Daten zu tun
			// haben
			return false;
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
		}

		try {
			if (numberPart.isEmpty()) {
				return factor * value;
			}

			return Double.parseDouble(numberPart) * factor * value;
		} catch (NullPointerException | NumberFormatException e) {
			return null;
		}
	}

	public static String formatDate(Integer day, Integer month, Integer year) {
		if (year == null)
			return null;

		String thisYear = new SimpleDateFormat("yyyy").format(new Date());

		if (year.toString().length() == 2)
			year = year > Integer.parseInt(thisYear.substring(2)) ? 1900 : 2000 + year;

		if (month == null) {
			return year.toString();
		} else if (day == null) {
			return year + "-" + new DecimalFormat("00").format(month);
		}

		return year + "-" + new DecimalFormat("00").format(month) + "-" + new DecimalFormat("00").format(day);
	}

	public static Map<String, Set<String>> getWarnings(Connection conn) {
		boolean useSerialAsID = isSerialPossible(conn);
		SetMultimap<String, String> warnings = LinkedHashMultimap.create();

		getDeliveries(conn, getStationIds(conn, useSerialAsID), getDeliveryIds(conn, useSerialAsID), warnings);

		return Multimaps.asMap(warnings);
	}

	public static Map<String, Delivery> getDeliveries(Connection conn, Map<Integer, String> stationIds,
			Map<Integer, String> deliveryIds, SetMultimap<String, String> warnings) {
		Map<String, Delivery> deliveries = new LinkedHashMap<>();

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
						id + ": " + amountInKg1 + "kg\tvs.\t" + amountInKg2 + "kg");
			}

			Delivery d = new Delivery(deliveryIds.get(id), stationIds.get(from), stationIds.get(to),
					r.getValue(LIEFERUNGEN.DD_DAY), r.getValue(LIEFERUNGEN.DD_MONTH), r.getValue(LIEFERUNGEN.DD_YEAR),
					r.getValue(LIEFERUNGEN.AD_DAY), r.getValue(LIEFERUNGEN.AD_MONTH), r.getValue(LIEFERUNGEN.AD_YEAR),
					lotNumber, r.getValue(LIEFERUNGEN.NUMPU), r.getValue(LIEFERUNGEN.TYPEPU),
					amountInKg1 != null ? amountInKg1 : amountInKg2);

			deliveries.put(d.getId(), d);
		}

		Select<Record> deliveryToDeliverySelect = DSL.using(conn, SQLDialect.HSQLDB).select().from(CHARGENVERBINDUNGEN)
				.leftOuterJoin(LIEFERUNGEN).on(CHARGENVERBINDUNGEN.PRODUKT.equal(LIEFERUNGEN.CHARGE));

		for (Record r : deliveryToDeliverySelect) {
			Delivery from = deliveries.get(deliveryIds.get(r.getValue(CHARGENVERBINDUNGEN.ZUTAT)));
			Delivery to = deliveries.get(deliveryIds.get(r.getValue(LIEFERUNGEN.ID)));

			if (from == null || to == null) {
				continue;
			}

			if (from.getId().equals(to.getId())) {
				warnings.put(INGREDIENT_OF_ITSELF, from.getId());
				continue;
			}

			from.getAllNextIds().add(to.getId());
			to.getAllPreviousIds().add(from.getId());
		}

		checkDates(deliveries, warnings);
		checkAmounts(deliveries, warnings);

		return deliveries;
	}

	private static void checkDates(Map<String, Delivery> deliveries, SetMultimap<String, String> warnings) {
		for (Delivery d : deliveries.values()) {
			for (String nextId : d.getAllNextIds()) {
				Delivery next = deliveries.get(nextId);

				if (!d.isBefore(next)) {
					warnings.put(INCONSISTENT_DATES,
							"In: \"" + d.getId() + "\" (" + formatDate(d.getArrivalDay(), d.getArrivalMonth(),
									d.getArrivalYear()) + ")\tvs.\tOut: \"" + next.getId() + "\" ("
							+ formatDate(next.getDepartureDay(), next.getDepartureMonth(), next.getDepartureYear())
							+ ")");
				}
			}
		}
	}

	private static void checkAmounts(Map<String, Delivery> deliveries, SetMultimap<String, String> warnings) {
		SetMultimap<String, Delivery> deliveriesByLot = LinkedHashMultimap.create();

		for (Delivery d : deliveries.values()) {
			deliveriesByLot.put(d.getLotNumber(), d);
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
				warnings.put(AMOUNTS_INCORRECT, "Lot = " + lot.getKey() + ":\tIn = " + amountIn + " " + unitIn
						+ "\tvs.\tOut = " + amountOut + " " + unitOut);
			} else if (kgIn != null && kgOut != null && areTooDifferent(kgIn, kgOut)) {
				warnings.put(AMOUNTS_INCORRECT,
						"Lot = " + lot.getKey() + ":\tIn = " + kgIn + " " + "kg\tvs.\tOut = " + kgOut + " kg");
			}
		}
	}

	private static boolean areTooDifferent(double amount1, double amount2) {
		return Math.max(amount1, amount2) > 2.0 * Math.min(amount1, amount2);
	}
}
