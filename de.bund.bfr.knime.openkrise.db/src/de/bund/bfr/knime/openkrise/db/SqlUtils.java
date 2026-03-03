package de.bund.bfr.knime.openkrise.db;

import java.util.Arrays;
import java.util.HashSet;

/***
 * This class is supposed to be a temporary solution
 * until all sql statements are replaced by prepared statements
 * 
 * ToDo: Use Only Prepared Statement
 */
public class SqlUtils {
	
//	private static HashSet<String> hashSet = new HashSet<>();
	
	public static String escapeText(String text) {
//		if (text == null) {
//			String trace = String.join("\n", Arrays.asList(Thread.currentThread().getStackTrace()).stream().map(t -> t.toString()).toArray(String[]::new));
//			if (!hashSet.contains(trace)) {
//				System.err.println("String to escape is null: \n" + trace);
//				hashSet.add(trace);
//			}
//		}
		return text == null ? null : text.replaceAll("'", "''");
	}
}
