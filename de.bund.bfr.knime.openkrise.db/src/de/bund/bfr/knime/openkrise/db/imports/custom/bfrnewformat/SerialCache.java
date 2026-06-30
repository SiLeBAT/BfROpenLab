/*******************************************************************************
 * Copyright (c) 2014-2026 German Federal Institute for Risk Assessment (BfR)
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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

/**
 * The purpose of this class is to cache id serial relations for a db table
 * Background:
 * The Delivery class makes db queries to find upper case serial matches for a given serial (for each imported delivery).
 * This repetitive requerying is very time consuming.
 * 
 * This class is supposed to be a temporary solution to speed up the import process until a 
 * complete refactoring of the db import process is done.
 * 
 * Usage: 
 * To work properly all updates and inserts of of serials have to be mirrored here
 * 
 * ToDo: Refactor db import process
 */
public class SerialCache {
	private Map<Integer, String> id2UCSerialMap = null;
	private HashMap<Integer, String> id2SerialMap = null;
	private HashMap<String, Set<Integer>> ucSerial2IdMap = null;
	private String tableName;
	private boolean invalidated = false;
	
	SerialCache(String tableName) {
		this.tableName = tableName;
	}
	
	private boolean loadFromDB() {
		this.id2SerialMap = new HashMap<>();
		this.id2UCSerialMap = new HashMap<>();
		this.ucSerial2IdMap = new HashMap<>();
		this.invalidated = true;
		
		String sql = "SELECT " + MyDBI.delimitL("ID") + ", " + MyDBI.delimitL("Serial") + " FROM " + MyDBI.delimitL(tableName);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs == null) return false;
		try {
			if (rs.first()) {
				do {
					insert(rs.getInt("ID"), rs.getString("Serial"));
				} while (rs.next());
			}	
			invalidated = false;
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			try { rs.close(); } catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/***
	 * marks the cache as invalid
	 */
	void invalidate() {
		invalidated = true;
	}
		
	private static String getUCSerial(String serial) {
		return serial == null ? null : serial.toUpperCase();
	}
		
	private void insert_(int id, String serial) {
		id2SerialMap.put(id, serial);
		String ucSerial = getUCSerial(serial);
		id2UCSerialMap.put(id, ucSerial);
		Set<Integer> ids = ucSerial2IdMap.get(ucSerial);
		if (ids == null) {
			ids = new HashSet<>();
			ucSerial2IdMap.put(ucSerial, ids);
		}
		ids.add(id);
	}
		
	private void remove_(int id) {
		if (id2UCSerialMap.containsKey(id)) {
			String oldUCSerial = id2UCSerialMap.get(id);
			ucSerial2IdMap.get(oldUCSerial).remove(id);
			id2UCSerialMap.remove(id);
			id2SerialMap.remove(id);
		}
	}
		
	/***
	 * inserts the id serial relation into the cache (if the cache is valid)
	 * @param id
	 * @param serial
	 */
	void insert(int id, String serial) {
		if (!checkCache()) return;
		remove_(id);
		insert_(id, serial);
	}
	
	/***
	 * updates the id serial relation
	 * @param id
	 * @param serial
	 */
	void update(int id, String serial) {
		if (!checkCache()) return;
		if (id2SerialMap.containsKey(id)) {
			remove_(id);
			insert_(id, serial);
		}
	}
	
	private static void throwInvalidCacheException() throws Exception {
		throw new Exception("Invalid Delivery Serial Cache.");
	}
	
	/***
	 * Returns the cached serial for that id. Method throws an exception if the cache is invalid.
	 * @param id
	 * @return
	 * @throws Exception
	 */
	String getSerial(int id) throws Exception {
		if (!this.checkCache()) throwInvalidCacheException();
		
		return id2SerialMap.get(id);
	}
	
	/***
	 * Returns count of deliveries, of which the cached upper case serial matches the upper case of the specified serial
	 * @param serial
	 * @return
	 * @throws Exception
	 */
	int getCountOfLieferungenMatchingUCaseOfSerial(String serial) throws Exception {
		if (!this.checkCache()) throwInvalidCacheException();
		
		String ucSerial = serial == null ? null : serial.toUpperCase();
		Set<Integer> ids = ucSerial2IdMap.get(ucSerial);
		return ids == null ? 0 : ids.size();
	}
	
	private boolean checkCache() {
		if (invalidated) return false;
		if (id2SerialMap == null) loadFromDB();
		return !invalidated;
	}
	
	/***
	 * gets whether the cache is valid or not
	 * @return
	 */
	boolean isValid() {
		return checkCache();
	}
	
	/***
	 * clears the cache
	 */
	void clear() {
		invalidated = false;
		id2SerialMap = null;
		id2UCSerialMap = null;
		ucSerial2IdMap = null;
	}	
}
