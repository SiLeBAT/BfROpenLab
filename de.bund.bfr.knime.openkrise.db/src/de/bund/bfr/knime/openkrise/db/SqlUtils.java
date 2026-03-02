package de.bund.bfr.knime.openkrise.db;

/***
 * This class is supposed to be a temporary solution
 * until all sql statements are replaced by prepared statements
 * 
 * ToDo: Use Only Prepared Statement
 */
public class SqlUtils {
	public static String escapeText(String text) {
		return text.replaceAll("'", "''");
	}
}
