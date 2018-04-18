/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class D2D {

	private HashMap<String, String> flexibles = new HashMap<>();
	
	private Delivery targetDelivery;
	public Delivery getTargetDelivery() {
		return targetDelivery;
	}

	public void setTargetDelivery(Delivery targetDelivery) {
		this.targetDelivery = targetDelivery;
	}

	public Delivery getIngredient() {
		return ingredient;
	}

	public void setIngredient(Delivery ingredient) {
		this.ingredient = ingredient;
	}

	private Delivery ingredient;

	public void addFlexibleField(String key, String value) {
		flexibles.put(key, value);
	}
	
	public void getId(Integer miDbId, MyDBI mydbi) throws Exception {
		if (ingredient != null && targetDelivery != null) getId(ingredient.getDbId(), targetDelivery.getLot().getDbId(), miDbId, mydbi);
	}

	public void getId(Integer dbId, Integer dbPId, Integer miDbId, MyDBI mydbi) throws Exception {
		String sql = "SELECT " + MyDBI.delimitL("ID") + " FROM " + MyDBI.delimitL("ChargenVerbindungen") +
				" WHERE " + MyDBI.delimitL("Zutat") + "=" + dbId + " AND " + MyDBI.delimitL("Produkt") + "=" + dbPId;
		ResultSet rs = (mydbi != null ? mydbi.getResultSet(sql, false) : DBKernel.getResultSet(sql, false));
		Integer result = null;
		if (rs != null && rs.first()) {
			result = rs.getInt(1);
			rs.close();
		}
		
		if (result != null) {
			sql = "UPDATE " + MyDBI.delimitL("ChargenVerbindungen") + " SET " + MyDBI.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + MyDBI.delimitL("ImportSources") + ")=0,CONCAT(" + MyDBI.delimitL("ImportSources") + ", '" + miDbId + ";'), " + MyDBI.delimitL("ImportSources") + ") WHERE " + MyDBI.delimitL("ID") + "=" + result;
			if (mydbi != null) mydbi.sendRequest(sql, false, false);
			else DBKernel.sendRequest(sql, false);
		}
		else {
			sql = "INSERT INTO " + MyDBI.delimitL("ChargenVerbindungen") +
					" (" + MyDBI.delimitL("Zutat") + "," + MyDBI.delimitL("Produkt") + "," + MyDBI.delimitL("ImportSources") +
					") VALUES (" + dbId + "," + dbPId + ",';" + miDbId + ";')";
			//DBKernel.sendRequest(sql, false);
			@SuppressWarnings("resource")
			Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() > 0) {
				result = (mydbi != null ? mydbi.getLastInsertedID(ps) : DBKernel.getLastInsertedID(ps));
				
				// Further flexible cells
				if (result != null) {
					for (Entry<String, String> es : flexibles.entrySet()) {
						if (es.getValue() != null && !es.getValue().trim().isEmpty()) {
							sql = "DELETE FROM " + MyDBI.delimitL("ExtraFields") +
									" WHERE " + MyDBI.delimitL("tablename") + "='ChargenVerbindungen'" +
									" AND " + MyDBI.delimitL("id") + "=" + result +
									" AND " + MyDBI.delimitL("attribute") + "='" + es.getKey() + "'";
							if (mydbi != null) mydbi.sendRequest(sql, false, false);
							else DBKernel.sendRequest(sql, false);
							sql = "INSERT INTO " + MyDBI.delimitL("ExtraFields") +
									" (" + MyDBI.delimitL("tablename") + "," + MyDBI.delimitL("id") + "," + MyDBI.delimitL("attribute") + "," + MyDBI.delimitL("value") +
									") VALUES ('ChargenVerbindungen'," + result + ",'" + es.getKey() + "','" + es.getValue() + "')";
							if (mydbi != null) mydbi.sendRequest(sql, false, false);
							else DBKernel.sendRequest(sql, false);
						}
					}
				}
			}
		}		

	}
	public static HashSet<Delivery> getIngredients(Integer lotId, MyDBI mydbi) throws SQLException {
		HashSet<Delivery> result = new HashSet<>();
		// Checke, if there are already ingredients defined! Are they the same? No? make warning!
		String sql = "SELECT " + MyDBI.delimitL("Zutat") + "," + MyDBI.delimitL("ChargenNr") + "," + MyDBI.delimitL("Bezeichnung") + "," + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Serial") + "," + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Name") + " FROM " + MyDBI.delimitL("ChargenVerbindungen") + 
				" LEFT JOIN " + MyDBI.delimitL("Lieferungen") + " ON " + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") + 
				" LEFT JOIN " + MyDBI.delimitL("Chargen") + " ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") + 
				" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") + " ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") + 
				" LEFT JOIN " + MyDBI.delimitL("Station") + " ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") + 
				" WHERE " + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Produkt") + "=" + lotId;
		ResultSet rs = (mydbi != null ? mydbi.getResultSet(sql, false) : DBKernel.getResultSet(sql, false));
		if (rs != null && rs.first()) {
			do {
				Delivery d = new Delivery();
				d.setDbId(rs.getInt("Zutat"));
				d.addTargetLotId(rs.getString("ChargenNr"));
				d.setId(rs.getString("Bezeichnung"));
				d.setUnitUnit(rs.getString("Serial"));
				d.setComment(rs.getString("Name"));
				result.add(d);
			} while(rs.next());
			rs.close();
		}	
		return result;
	}
}
