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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class MyNewTracingLoader {

	private static HashMap<String, Delivery> allDeliveries;

	private static Field<Object> field(String name) {
		List<String> parts = new ArrayList<>();

		for (String s : Splitter.on(".").split(name.replace("\"", ""))) {
			parts.add("\"" + s + "\"");
		}

		return DSL.field(Joiner.on(".").join(parts));
	}

	private static Table<Record> table(String name) {
		return DSL.table("\"" + name + "\"");
	}

	public static Map<String, Delivery> getNewTracingModel(MyDBI myDBi, Connection conn) {
		allDeliveries = new HashMap<>();
		// Firstly: get all deliveries
		// String sql = "SELECT " + DBKernel.delimitL("ID") + "," +
		// DBKernel.delimitL("Empfänger")
		// + "," + DBKernel.delimitL("Station") + "," +
		// DBKernel.delimitL("dd_day") + ","
		// + DBKernel.delimitL("dd_month") + "," + DBKernel.delimitL("dd_year")
		// + " FROM "
		// + DBKernel.delimitL("Lieferungen") + " LEFT JOIN " +
		// DBKernel.delimitL("Chargen")
		// + " ON " + DBKernel.delimitL("Lieferungen") + "." +
		// DBKernel.delimitL("Charge")
		// + "=" + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID")
		// + " LEFT JOIN " + DBKernel.delimitL("Produktkatalog") + " ON "
		// + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("Artikel") +
		// "="
		// + DBKernel.delimitL("Produktkatalog") + "." +
		// DBKernel.delimitL("ID");
		String sql = DSL
				.using(conn, SQLDialect.HSQLDB)
				.select(field("ID"), field("Empfänger"), field("Station"), field("dd_day"),
						field("dd_month"), field("dd_year")).from(table("Lieferungen"))
				.leftOuterJoin(table("Chargen"))
				.on(field("Lieferungen.Charge").equal(field("Chargen.ID")))
				.leftOuterJoin(table("Produktkatalog"))
				.on(field("Chargen.Artikel").equal(field("Produktkatalog.ID"))).getSQL();

		try {
			ResultSet rs = DBKernel.getResultSet(conn, sql, false);
			if (rs != null && rs.first()) {
				do {
					Delivery md = new Delivery(rs.getObject("ID").toString(), rs.getObject(
							"Station").toString(), rs.getObject("Empfänger").toString(),
							(Integer) rs.getObject("dd_day"), (Integer) rs.getObject("dd_month"),
							(Integer) rs.getObject("dd_year"), (Integer) rs.getObject("ad_day"),
							(Integer) rs.getObject("ad_month"), (Integer) rs.getObject("ad_year"));
					allDeliveries.put(md.getId(), md);
				} while (rs.next());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Secondly: prev/next deliveries
		sql = "SELECT " + DBKernel.delimitL("ZutatLieferungen") + "." + DBKernel.delimitL("ID")
				+ "," + DBKernel.delimitL("ProduktLieferungen") + "." + DBKernel.delimitL("ID")
				+ " FROM " + DBKernel.delimitL("ChargenVerbindungen") + " LEFT JOIN "
				+ DBKernel.delimitL("Lieferungen") + " AS " + DBKernel.delimitL("ZutatLieferungen")
				+ " ON " + DBKernel.delimitL("ZutatLieferungen") + "." + DBKernel.delimitL("ID")
				+ "=" + DBKernel.delimitL("ChargenVerbindungen") + "." + DBKernel.delimitL("Zutat")
				+ " LEFT JOIN " + DBKernel.delimitL("Lieferungen") + " AS "
				+ DBKernel.delimitL("ProduktLieferungen") + " ON "
				+ DBKernel.delimitL("ProduktLieferungen") + "." + DBKernel.delimitL("Charge") + "="
				+ DBKernel.delimitL("ChargenVerbindungen") + "." + DBKernel.delimitL("Produkt");
		try {
			ResultSet rs = DBKernel.getResultSet(conn, sql, false);
			if (rs != null && rs.first()) {
				do {
					Delivery mdZ = allDeliveries.get(rs.getObject(1).toString());
					Delivery mdP = allDeliveries.get(rs.getObject(2).toString());
					if (mdZ != null && mdP != null) {
						mdZ.getAllNextIds().add(mdP.getId());
						mdP.getAllPreviousIds().add(mdZ.getId());
					}
					// if (mdP != null) mdP.addPrevious(mdZ.getId());
				} while (rs.next());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return allDeliveries;
	}

	public static boolean serialPossible(Connection conn) {
		HashSet<String> hs = new HashSet<>();
		String sql = "SELECT " + DBKernel.delimitL("Serial") + " FROM "
				+ DBKernel.delimitL("Station");
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
		sql = "SELECT " + DBKernel.delimitL("Serial") + "," + DBKernel.delimitL("UnitEinheit")
				+ " FROM " + DBKernel.delimitL("Lieferungen");
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
					if (rs.getObject("UnitEinheit") == null
							|| !rs.getString("UnitEinheit").equals("kg"))
						alwaysUEkg = false;
				} while (rs.next());
			}
			if (alwaysUEkg)
				return false; // beim EFSA Importer wurde immer kg eingetragen,
								// später beim bfrnewimporter wurde nur noch
								// "numPU" und "typePU" benutzt und UnitEinheit
								// müsste immer NULL sein, daher ist das ein
								// sehr gutes Indiz daafür, dass wir es mit
								// alten Daten zu tun haben
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
