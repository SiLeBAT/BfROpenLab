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

import static de.bund.bfr.knime.openkrise.generated.public_.Tables.CHARGEN;
import static de.bund.bfr.knime.openkrise.generated.public_.Tables.CHARGENVERBINDUNGEN;
import static de.bund.bfr.knime.openkrise.generated.public_.Tables.LIEFERUNGEN;
import static de.bund.bfr.knime.openkrise.generated.public_.Tables.PRODUKTKATALOG;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.impl.DSL;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class MyNewTracingLoader {

	public static Map<String, Delivery> getNewTracingModel(MyDBI myDBi, Connection conn) {
		Map<String, Delivery> allDeliveries = new HashMap<>();

		Select<Record> deliverySelect = DSL.using(conn, SQLDialect.HSQLDB).select().from(LIEFERUNGEN)
				.leftOuterJoin(CHARGEN).on(LIEFERUNGEN.CHARGE.equal(CHARGEN.ID)).leftOuterJoin(PRODUKTKATALOG)
				.on(CHARGEN.ARTIKEL.equal(PRODUKTKATALOG.ID));

		for (Record r : deliverySelect) {
			Delivery d = new Delivery(r.getValue(LIEFERUNGEN.ID).toString(),
					r.getValue(PRODUKTKATALOG.STATION).toString(), r.getValue(LIEFERUNGEN.EMPFÄNGER).toString(),
					r.getValue(LIEFERUNGEN.DD_DAY), r.getValue(LIEFERUNGEN.DD_MONTH), r.getValue(LIEFERUNGEN.DD_YEAR),
					r.getValue(LIEFERUNGEN.AD_DAY), r.getValue(LIEFERUNGEN.AD_MONTH), r.getValue(LIEFERUNGEN.AD_YEAR));

			allDeliveries.put(d.getId(), d);
		}

		Select<Record> deliveryToDeliverySelect = DSL.using(conn, SQLDialect.HSQLDB).select().from(CHARGENVERBINDUNGEN)
				.leftOuterJoin(LIEFERUNGEN).on(CHARGENVERBINDUNGEN.PRODUKT.equal(LIEFERUNGEN.CHARGE));

		for (Record r : deliveryToDeliverySelect) {
			Delivery from = allDeliveries.get(r.getValue(CHARGENVERBINDUNGEN.ZUTAT).toString());
			Delivery to = allDeliveries.get(r.getValue(LIEFERUNGEN.ID).toString());

			if (from != null && to != null) {
				from.getAllNextIds().add(to.getId());
				to.getAllPreviousIds().add(from.getId());
			}
		}

		return allDeliveries;
	}

	public static boolean serialPossible(Connection conn) {
		HashSet<String> hs = new HashSet<>();
		String sql = "SELECT " + DBKernel.delimitL("Serial") + " FROM " + DBKernel.delimitL("Station");
		try {
			ResultSet rs = DBKernel.getResultSet(conn, sql, false);
			if (rs != null && rs.first()) {
				do {
					if (rs.getObject("Serial") == null) {
						return false;
					}
					String s = rs.getString("Serial");
					if (hs.contains(s))
						return false;
					hs.add(s);
				} while (rs.next());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		hs.clear();
		sql = "SELECT " + DBKernel.delimitL("Serial") + "," + DBKernel.delimitL("UnitEinheit") + " FROM "
				+ DBKernel.delimitL("Lieferungen");
		try {
			boolean alwaysUEkg = true;
			ResultSet rs = DBKernel.getResultSet(conn, sql, false);
			if (rs != null && rs.first()) {
				do {
					if (rs.getObject("Serial") == null) {
						return false;
					}
					String s = rs.getString("Serial");
					if (hs.contains(s))
						return false;
					hs.add(s);
					if (rs.getObject("UnitEinheit") == null || !rs.getString("UnitEinheit").equals("kg"))
						alwaysUEkg = false;
				} while (rs.next());
			}
			if (alwaysUEkg) {
				return false;
				// beim EFSA Importer wurde immer kg eingetragen, später beim
				// bfrnewimporter wurde nur noch "numPU" und "typePU" benutzt
				// und UnitEinheit müsste immer NULL sein, daher ist das ein
				// sehr gutes Indiz daafür, dass wir es mit alten Daten zu tun
				// haben
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
