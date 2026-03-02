/*******************************************************************************
 * Copyright (c) 2014-2025 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db;

import java.sql.PreparedStatement;

/***
 * The purpose of this class is to prevent recurrent codes similar to this:
 * 
 * if (myDBi != null) myDBi.sendRequest( ...
 * else DBKernel.sendRequest(... 
 * 
 * temporary solution until db import is refactored
 * 
 * ToDo: Refactor DB Import
 */
public class DBUtils {
	public static boolean sendRequest(final MyDBI  myDBi, final String sql, final boolean suppressWarnings) {
		if (myDBi != null) return myDBi.sendRequest(sql, suppressWarnings, false); 
		return DBKernel.sendRequest(sql, suppressWarnings);
	}

	public static boolean sendRequest(final MyDBI  myDBi, final String sql, final boolean suppressWarnings, final boolean fetchAdminInCase) {
		if (myDBi != null) return myDBi.sendRequest(sql, suppressWarnings, fetchAdminInCase); 
		return DBKernel.sendRequest(sql, suppressWarnings, fetchAdminInCase);
	}
	
	public static int getRowCount(final MyDBI  myDBi, final String tableName, final String where) {
		if (myDBi != null) return myDBi.getRowCount(tableName, where); 
		return DBKernel.getRowCount(tableName, where); 
	}
	
	public static Integer getLastInsertedID(final MyDBI  myDBi, final PreparedStatement psmt) {
		if (myDBi != null) return myDBi.getLastInsertedID(psmt); 
		return DBKernel.getLastInsertedID(psmt);
	}
}
