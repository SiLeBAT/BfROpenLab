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
package de.bund.bfr.knime.openkrise.db.itest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

import de.bund.bfr.knime.openkrise.db.Backup;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;

public class DBUtils {
	static final String DB_CONTENT_START_TAG = "--- DB Content Start ---";
	static final String DB_CONTENT_END_TAG = "--- DB Content End ---";
	
	private static final List<String> DB_TABLE_NAMES = Arrays.asList("Station", "Produktkatalog", "Chargen", "Lieferungen", "ChargenVerbindungen", "ExtraFields");
	
	public static void appendTextForDB(BufferedWriter writer) throws IOException, SQLException {
		
		writer.write(DB_CONTENT_START_TAG + "\n\n");
		
        for (String tablename : DB_TABLE_NAMES) {
        	appendTextForTable(writer, tablename);
        }
        
        writer.write("\n" + DB_CONTENT_END_TAG + "\n");
	}
	
	private static String valueToString(Object value) {
		if (value == null) return "";
		if (value instanceof String) {
			return "\"" + ((String) value)
					.replaceAll("\\\\", "\\\\")
					.replaceAll("\r", "\\r")
					.replaceAll("\n", "\\n")
					.replaceAll("\t", "\\t")
					.replaceAll("\"", "\\\"") + "\"";
		}
		return value.toString();
	}
	
	
	private static void appendTextForTable(BufferedWriter writer, String tableName) throws SQLException, IOException {
		
		String sql = "SELECT * FROM " + MyDBI.delimitL(tableName);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        
        writer.write("Table \"" + tableName + "\":\n");
        
        for (int col = 1; col <= columnsNumber; col++) {
        	if (col > 1) writer.write(" | ");
            writer.write(rsmd.getColumnName(col));
        }
        
        if (rs.isFirst()) {
       
	        do {
	        	
	        	writer.write("\n");
	        	
	            for (int col = 1; col <= columnsNumber; col++) {
	                if (col > 1) writer.write(" | ");
	                Object value = rs.getObject(col);
	                if (value != null) {
	                	writer.write(valueToString(value));
	                
	                }
	            }
	            
	        } while (rs.next());
        }
        
        rs.close();
        writer.write("\n\n");
	}
	
	static DBSettings getCurrentDBSettings() {
		DBSettings settings = new DBSettings();
		settings.path = DBKernel.prefs.get("FC_LAB_SETTINGS_DB_PATH", DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PATH", DBKernel.getInternalDefaultDBPath()));
		CRC32 crc32 = new CRC32();
		crc32.update(settings.path.getBytes());
		long crc32Out = crc32.getValue();
		settings.username = DBKernel.prefs.get("FC_LAB_SETTINGS_DB_USERNAME" + crc32Out, DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_USERNAME" + crc32Out, "SA"));
		settings.password = DBKernel.prefs.get("FC_LAB_SETTINGS_DB_PASSWORD" + crc32Out, DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PASSWORD" + crc32Out, ""));
		settings.readOnly = DBKernel.prefs.getBoolean("FC_LAB_SETTINGS_DB_RO" + crc32Out, DBKernel.prefs.getBoolean("PMM_LAB_SETTINGS_DB_RO" + crc32Out, false));
		return settings;
	}
	
	static void setDBPrefs(DBSettings dbSettings) {
		DBKernel.HSHDB_PATH = dbSettings.path;
		DBKernel.prefs.put("FC_LAB_SETTINGS_DB_PATH", dbSettings.path);
		CRC32 crc32 = new CRC32();
		crc32.update(dbSettings.path.getBytes());
		long crc32Out = crc32.getValue();
		
		DBKernel.prefs.put("FC_LAB_SETTINGS_DB_USERNAME" + crc32Out, dbSettings.username);
		DBKernel.prefs.put("FC_LAB_SETTINGS_DB_PASSWORD" + crc32Out, dbSettings.password);
		DBKernel.prefs.putBoolean("FC_LAB_SETTINGS_DB_RO" + crc32Out, dbSettings.readOnly);
		DBKernel.prefs.prefsFlush();
	}
	
	static void switchToDB(DBSettings dbSettings) {
		System.out.println("Switching to db: \"" + dbSettings.path + "\" ...");
		DBKernel.closeDBConnections(false);
		
		boolean isServer = DBKernel.isHsqlServer(dbSettings.path);
		if (!isServer && !dbSettings.path.endsWith(System.getProperty("file.separator"))) {
			dbSettings.path += System.getProperty("file.separator");
		}
		
		File dbDir = new File(dbSettings.path);
				
		if (!dbDir.exists() || dbDir.list().length == 0) {
			System.out.println("DB \"" + dbSettings.path + "\" does not exist. Creating db ...");
			setDBPrefs(dbSettings);
			MyDBTable myDB = new MyDBTable();
			File temp = DBKernel.getCopyOfInternalDB();
			Backup.doRestore(myDB, temp, true);
		} else {
			if (DBKernel.mainFrame != null) {
				DBKernel.mainFrame.dispose();
				setDBPrefs(dbSettings);
				DBKernel.openDBGUI();
			} else {
				setDBPrefs(dbSettings);
			}
		}
	}
		
	static void startDBTransaction() {
		DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
	}
	
	static void commitDBTransaction() {
		DBKernel.sendRequest("COMMIT", false);
		DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
	}
	
	static void rollbackTransaction() {
		DBKernel.sendRequest("ROLLBACK", false);
		DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
	}
}
