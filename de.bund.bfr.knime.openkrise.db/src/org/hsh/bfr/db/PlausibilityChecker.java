/*******************************************************************************
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Thöns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * Jörgen Brandt (BfR)
 * Annemarie Käsbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
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
 ******************************************************************************/
/**
 * 
 */
package org.hsh.bfr.db;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.gui.dbtable.header.GuiMessages;

/**
 * @author Weiser
 *
 */
public class PlausibilityChecker {

	public static String getTableRepeats(MyTable myT) {
		String result = null;
		String[] fn = myT.getFieldNames();
		String[] ft = myT.getFieldTypes();
		String cols = "";
		for (int i=0;i<fn.length;i++) {
			if (!ft[i].startsWith("BLOB(")) {
				cols += "," + DBKernel.delimitL(fn[i]);				
			}
		}
		if (cols.length() > 0) {
			cols = cols.substring(1);
			result = "SELECT COUNT(*)," + cols + " FROM " + DBKernel.delimitL(myT.getTablename()) + " GROUP BY " + cols + " HAVING COUNT(*) > 1";
		}
		return result;
	}
	public static String[] getValuePlausible(String tablename, String fieldname, Object newValue) {
		String[] result = null;
		if (tablename.equals("Literatur")) {
			if (fieldname.equals("Jahr")) {
				Integer jahr = 0;
				try {
					jahr = (Integer) newValue;
				}
				catch (Exception e) {}
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date()); //heute
				int diesjahr = cal.get(Calendar.YEAR);
				if (jahr < 1100 && jahr > diesjahr) {
					result = new String[1];
					result[0] = GuiMessages.getString("Merkwürdige Jahresangabe...");
				}
			}
		}
		return result;
	}
	private static String getDKSQL2(String tablename, String fieldname, String where, String idConf) {
		String sql = "SELECT " + idConf +
			" FROM " + DBKernel.delimitL(tablename) + " LEFT JOIN " +
			DBKernel.delimitL("DoubleKennzahlen") + " ON " + DBKernel.delimitL(tablename) + "." + DBKernel.delimitL(fieldname) +
			" = " + DBKernel.delimitL("DoubleKennzahlen") + "." + DBKernel.delimitL("ID") +
			" WHERE (" + where + ")";
		return sql;
	}
	private static void getDKValuePlausible(MyTable myT, Vector<String[]> result, String idConf) {
  		MyTable[] foreignFields = myT.getForeignFields();
  		if (foreignFields != null) {
  			for (int i=0;i<foreignFields.length;i++) {
    			if (foreignFields[i] != null && foreignFields[i].getTablename().equals("DoubleKennzahlen")) {
    				String fieldname = myT.getFieldNames()[i];
    				if (fieldname.equalsIgnoreCase("ph")) {
    					result.add(new String[] {
    						getDKSQL(myT.getTablename(), fieldname, -1, 15, idConf),
    						GuiMessages.getString("Der pH-Wert muss zwischen -1 und 15 liegen!")
    					});
    				}
    				else if (fieldname.equalsIgnoreCase("aw")) {
    					result.add(new String[] {
    							getDKSQL(myT.getTablename(), fieldname, 0, 1, idConf),
    							GuiMessages.getString("Der aw-Wert muss zwischen 0 und 1 liegen!")
        					});
    				}
    				else if (fieldname.equalsIgnoreCase("Temperatur")) {
    					result.add(new String[] {
    							getDKSQL(myT.getTablename(), fieldname, -273, 1000, idConf),
    							GuiMessages.getString("Die Temperatur muss zwischen -273 und 1000 liegen!")
        					});
    				}
    				
					result.add(new String[] {
							getDKSQL2(myT.getTablename(),
									fieldname,
									DBKernel.delimitL("Wiederholungen") + " <= 1",
									idConf),
	    							fieldname + ": " + GuiMessages.getString("Wenn die Anzahl der Wiederholungen = 1 ist, dann sollte ein Einzelwert eingetragen werden! Anzahl Wiederholungen < 1 machen keinen Sinn!")
    					});
					result.add(new String[] {
							getDKSQL2(myT.getTablename(),
									fieldname,
									DBKernel.delimitL("Wiederholungen") + " IS NULL AND (" +
									//DBKernel.delimitL("Wert") + " IS NOT NULL AND " + DBKernel.delimitL("Wert_typ") + " IS NOT NULL AND " + DBKernel.delimitL("Wert_typ") + " > 1 OR " + es gibt leider viele Mittelwertangaben ohne die Anzahl der Wiederholungen
									//DBKernel.delimitL("Standardabweichung") + " IS NOT NULL OR " + 
									DBKernel.delimitL("UCL95") + " IS NOT NULL OR " + DBKernel.delimitL("LCL95") + " IS NOT NULL OR " +
									DBKernel.delimitL("Verteilung") + " IS NOT NULL)",
									idConf), //  OR " + DBKernel.delimitL("Verteilung") + " = ''
	    							fieldname + ": " + GuiMessages.getString("Wenn die Anzahl der Wiederholungen nicht definiert ist, dann kann MW/Median/UCL/LCL/SD/Verteilung nicht gegeben sein!")
    					});
					result.add(new String[] {
							getDKSQL2(myT.getTablename(),
									fieldname,
									DBKernel.delimitL("Wiederholungen") + " > 1 AND " +
									DBKernel.delimitL("Wert") + " IS NOT NULL AND (" + DBKernel.delimitL("Wert_typ") + " IS NULL OR " + DBKernel.delimitL("Wert_typ") + " = 1)",
									idConf),
	    							fieldname + ": " + GuiMessages.getString("Wenn die Anzahl der Wiederholungen > 1 ist, dann kann ein Einzelwert nicht gegeben sein!")
    					});
					/*
					result.add(new String[] {
							getDKSQL2(myT.getTablename(), fieldname, DBKernel.delimitL("Wert") + " IS NULL AND " +
									DBKernel.delimitL("Minimum") + " IS NULL AND " +
									DBKernel.delimitL("Maximum") + " IS NULL"),
	    							(idField.equalsIgnoreCase("ID") ? "" : DBKernel.delimitL(myT.getTablename()) + ", ") +
									fieldname + ": Wenigstens einer der Werte Wert, Minimum, Maximum muss angegeben sein!"
    					});
    					*/
    			}
  			}
  		}
	}
	private static String getDKSQL(String tablename, String fieldname, int min, int max, String idConf) {
		String sql = "SELECT " + idConf +
			" FROM " + DBKernel.delimitL(tablename) + " LEFT JOIN " +
			DBKernel.delimitL("DoubleKennzahlen") + " ON " + DBKernel.delimitL(tablename) + "." + DBKernel.delimitL(fieldname) +
			" = " + DBKernel.delimitL("DoubleKennzahlen") + "." + DBKernel.delimitL("ID") +
			" WHERE (" + DBKernel.delimitL("Wert") + " < " + min + " OR " + DBKernel.delimitL("Wert") + " > " + max + " OR " +
			DBKernel.delimitL("Standardabweichung") + " < " + min + " OR " + DBKernel.delimitL("Standardabweichung") + " > " + max + " OR " +
			DBKernel.delimitL("Minimum") + " < " + min + " OR " + DBKernel.delimitL("Minimum") + " > " + max + " OR " +
			DBKernel.delimitL("Maximum") + " < " + min + " OR " + DBKernel.delimitL("Maximum") + " > " + max + " OR " +
			DBKernel.delimitL("LCL95") + " < " + min + " OR " + DBKernel.delimitL("LCL95") + " > " + max + " OR " +
			DBKernel.delimitL("UCL95") + " < " + min + " OR " + DBKernel.delimitL("UCL95") + " > " + max + " OR " +
			DBKernel.delimitL("Exponent") + " IS NOT NULL AND " + DBKernel.delimitL("Exponent") + " != 0 OR " +
			DBKernel.delimitL("Standardabweichung_exp") + " IS NOT NULL AND " + DBKernel.delimitL("Standardabweichung_exp") + " != 0 OR " +
			DBKernel.delimitL("Minimum_exp") + " IS NOT NULL AND " + DBKernel.delimitL("Minimum_exp") + " != 0 OR " +
			DBKernel.delimitL("Maximum_exp") + " IS NOT NULL AND " + DBKernel.delimitL("Maximum_exp") + " != 0 OR " +
			DBKernel.delimitL("LCL95_exp") + " IS NOT NULL AND " + DBKernel.delimitL("LCL95_exp") + " != 0 OR " +
			DBKernel.delimitL("UCL95_exp") + " < " + min + " OR " + DBKernel.delimitL("UCL95_exp") + " > " + max + ")";
		return sql;
	}
	public static Vector<String[]> getPlausibilityRow(MyDBTable table, MyTable myT, int row, String idField) {
		String tablename = myT.getTablename();
		Vector<String[]> result = new Vector<>();
		String idConf;
		if (idField.equalsIgnoreCase("ID")) {
			idConf = DBKernel.delimitL(tablename) + "." + DBKernel.delimitL("ID") + " AS " + DBKernel.delimitL("ID");
		}
		else {
			idConf = DBKernel.delimitL(tablename) + "." + DBKernel.delimitL(idField) + " AS " + DBKernel.delimitL("ID") + "," +
						DBKernel.delimitL(tablename) + "." + DBKernel.delimitL("ID") + " AS " + DBKernel.delimitL("ID2");
		}
		getDKValuePlausible(myT, result, idConf);
		if (tablename.equals("Matrices")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Matrixname") + " IS NULL",
						GuiMessages.getString("Ein Matrixname sollte angegeben werden!")
						});
			}
		}
		else if (tablename.equals("Agenzien")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Agensname") + " IS NULL",
						GuiMessages.getString("Ein Agensname sollte angegeben werden!")
						});
			}
		}
		else if (tablename.equals("Methoden")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Name") + " IS NULL",
						GuiMessages.getString("Ein Name sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Referenz") + " IS NULL",
						GuiMessages.getString("Eine Referenz sollte angegeben werden!")
						});
			}
		}
		else if (tablename.equals("Kontakte")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Name") + " IS NULL",
						GuiMessages.getString("Ein Name sollte angegeben werden!")
						});
				if (!DBKernel.isKrise) {
					result.add(new String[]{
							"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
							" WHERE " + DBKernel.delimitL("EMail") + " IS NULL OR " + DBKernel.delimitL("Telefon") + " IS NULL",
							GuiMessages.getString("Eine E-Mail Adresse oder eine Telefonnummer sollte angegeben werden!")
							});
				}
			}
		}
		else if (tablename.equals("Literatur")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Erstautor") + " IS NULL OR " + DBKernel.delimitL("Jahr") + " IS NULL OR " + DBKernel.delimitL("Titel") + " IS NULL",
						GuiMessages.getString("Erstautor, Jahr, Titel sollten angegeben werden!")
						});
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date()); //heute
				int jahr = cal.get(Calendar.YEAR);
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Jahr") + " < 1100 OR " + DBKernel.delimitL("Jahr") + " > " + jahr,
						GuiMessages.getString("Merkwürdige Jahresangabe...")
						});
			}
		}
		else if (tablename.equals("Krankheitsbilder")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("Referenz") + " IS NULL",
						GuiMessages.getString("Eine Referenz sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("Agens") + " IS NULL",
						GuiMessages.getString("Ein Agens sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Krankheitsverlauf") + " IS NOT NULL AND " + DBKernel.delimitL("Zielpopulation") + " IS NULL",
						GuiMessages.getString("Bei Dateneingabe in \"Krankheitsverlauf\" muss Dateneingabe in \"Zielpopulation\" vorhanden sein")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Inkubationszeit") + " IS NOT NULL AND " + DBKernel.delimitL("IZ_Einheit") + " IS NULL" +
								" OR " + DBKernel.delimitL("Inkubationszeit") + " IS NULL AND " + DBKernel.delimitL("IZ_Einheit") + " IS NOT NULL",
						GuiMessages.getString("Bei Dateneingabe in \"Inkubationszeit\" muss Dateneingabe in \"IZ_Einheit\" erfolgen; " +
						"falls in \"Inkubationszeit\" keine Eingabe vorhanden ist, darf auch in \"IZ_Einheit\" kein Eintrag vorhanden sein")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Symptomdauer") + " IS NOT NULL AND " + DBKernel.delimitL("SD_Einheit") + " IS NULL" +
								" OR " + DBKernel.delimitL("Symptomdauer") + " IS NULL AND " + DBKernel.delimitL("SD_Einheit") + " IS NOT NULL",
						GuiMessages.getString("Bei Dateneingabe in \"Symptomdauer\" muss Dateneingabe in \"SD_Einheit\" erfolgen; " +
						"falls in \"Symptomdauer\" keine Eingabe vorhanden ist, darf auch in \"SD_Einheit\" kein Eintrag vorhanden sein")
						});				
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Infektionsdosis") + " IS NOT NULL AND " + DBKernel.delimitL("ID_Einheit") + " IS NULL" +
								" OR " + DBKernel.delimitL("Infektionsdosis") + " IS NULL AND " + DBKernel.delimitL("ID_Einheit") + " IS NOT NULL",
						GuiMessages.getString("Bei Dateneingabe in \"Infektionsdosis\" muss Dateneingabe in \"ID_Einheit\" erfolgen; " +
						"falls in \"Infektionsdosis\" keine Eingabe vorhanden ist, darf auch in \"ID_Einheit\" kein Eintrag vorhanden sein")
						});				
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Letalitaetsdosis50") + " IS NOT NULL AND (" +
									DBKernel.delimitL("LD50_Einheit") + " IS NULL OR " + DBKernel.delimitL("LD50_Organismus") + " IS NULL OR " +
									DBKernel.delimitL("LD50_Aufnahmeroute") + " IS NULL)" +
								" OR " + DBKernel.delimitL("Letalitaetsdosis50") + " IS NULL AND (" +
									DBKernel.delimitL("LD50_Einheit") + " IS NOT NULL OR " + DBKernel.delimitL("LD50_Organismus") + " IS NOT NULL OR " +
									DBKernel.delimitL("LD50_Aufnahmeroute") + " IS NOT NULL)",
						GuiMessages.getString("Bei Dateneingabe in \"Letalitaetsdosis50\" muss Dateneingabe in \"LD50_Einheit\",\"LD50_Organismus\" und \"LD50_Aufnahmeroute\" erfolgen; " +
						"falls in \"Letalitaetsdosis50\" keine Eingabe vorhanden ist, darf auch in \"LD50_Einheit\",\"LD50_Organismus\" und \"LD50_Aufnahmeroute\" kein Eintrag vorhanden sein")
						});				
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Letalitaetsdosis100") + " IS NOT NULL AND (" +
									DBKernel.delimitL("LD100_Einheit") + " IS NULL OR " + DBKernel.delimitL("LD100_Organismus") + " IS NULL OR " +
									DBKernel.delimitL("LD100_Aufnahmeroute") + " IS NULL)" +
								" OR " + DBKernel.delimitL("Letalitaetsdosis100") + " IS NULL AND (" +
									DBKernel.delimitL("LD100_Einheit") + " IS NOT NULL OR " + DBKernel.delimitL("LD100_Organismus") + " IS NOT NULL OR " +
									DBKernel.delimitL("LD100_Aufnahmeroute") + " IS NOT NULL)",
						GuiMessages.getString("Bei Dateneingabe in \"Letalitaetsdosis100\" muss Dateneingabe in \"LD100_Einheit\",\"LD100_Organismus\" und \"LD100_Aufnahmeroute\" erfolgen; " +
						"falls in \"Letalitaetsdosis100\" keine Eingabe vorhanden ist, darf auch in \"LD100_Einheit\",\"LD100_Organismus\" und \"LD100_Aufnahmeroute\" kein Eintrag vorhanden sein")
						});				
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Letalitaet") + " IS NOT NULL AND " + DBKernel.delimitL("Therapie_Letal") + " IS NULL" +
								" OR " + DBKernel.delimitL("Letalitaet") + " IS NULL AND " + DBKernel.delimitL("Therapie_Letal") + " IS NOT NULL",
						GuiMessages.getString("Falls Angabe in \"Letalitaet\" muss Dateneingabe in \"Therapie_Letal\" getroffen werden; falls in \"Letalitaet\" keine Eingabe vorhanden ist, darf auch in \"Therapie_Letal\" kein Eintrag vorhanden sein")
						});				
			}
		}
		else if (tablename.equals("Zertifizierungssysteme")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Anbieter") + " IS NULL",
						GuiMessages.getString("Ein Anbieter sollte angegeben werden!")
						});
			}
		}
		else if (tablename.equals("Labore")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Kontakt") + " IS NULL",
						GuiMessages.getString("Ein Kontakt sollte angegeben werden!")
						});
			}
		}
		/*
		else if (tablename.equals("DoubleKennzahlen")) {
			if (table == null) {
			}
			else if (row < 0) {
				String ergebnis = null;
				MyTable theT = table.getActualTable();
				for (int k=0;k<table.getColumnCount();k++) {
					if (DBKernel.isNewDBL(theT, k)) {
						String sName = table.getColumn(k).getColumnName();
						for (int i=0;i<table.getRowCount();i++) {
							Object id = table.getValueAt(i, k);
							ergebnis = checkPlausibleDBL(id, theT.getTablename(), sName, (Integer) table.getValueAt(i, 0));
						}
					}
				}
				//System.out.println("ergebnis: " + ergebnis);
				if (ergebnis != null && ergebnis.trim().length() > 0) {
					result = new Vector<String[]>();
					result.add(new String[]{ergebnis});
				}
			}
			else {
				result = null;
			}
		}
		*/
		else if (tablename.equals("Messwerte")) {
			/* eigentlich schon, aber erstmal zu viele Fehler...
			result.add(new String[]{
					"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
					" WHERE " + DBKernel.delimitL("Versuchsbedingungen") + " IS NULL",
					"Jeder Messwert muss zu einer Versuchsbedingung gehören!"
					});
					*/
			result.add(new String[]{
					"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
					" WHERE " + DBKernel.delimitL("Delta") + " AND " + DBKernel.delimitL("Zeit") + " = 0",
					GuiMessages.getString("Für den Zeitpunkt 0 kann das Delta Feld nicht angehakt sein!")
					});
			result.add(new String[]{
					"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
					" WHERE " + DBKernel.delimitL("Zeit") + " IS NULL",
					GuiMessages.getString("Zeit muss angegeben werden!")
					});
			result.add(new String[]{
					"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
					" WHERE " + DBKernel.delimitL("ZeitEinheit") + " IS NULL",
					GuiMessages.getString("Zeiteinheit muss angegeben werden!")
					});
			result.add(new String[]{
					"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
					" WHERE " + DBKernel.delimitL("Konzentration") + " IS NULL",
					GuiMessages.getString("Konzentration muss angegeben werden!")
					});
			result.add(new String[]{
					"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
					" WHERE " + DBKernel.delimitL("Konz_Einheit") + " IS NULL",
					GuiMessages.getString("Die Einheit für die Konzentration sollte eingetragen werden!")
					});
			if (table == null) {
			}
			else if (row < 0) {
				String ergebnis = "";
				for (int i=0;i<table.getRowCount();i++) {
					int id = (Integer) table.getValueAt(i, 0);
					for (int j=0;j<result.size();j++) {
						String[] res = result.get(j);
						String sql = res[0] +  " AND " + DBKernel.delimitL(tablename) + "." + DBKernel.delimitL("ID") + "=" + id;
						ResultSet rs = DBKernel.getResultSet(sql, false);
						try {
							if (rs != null && rs.first()) {
								ergebnis += tablename + " (ID=" + id + "): " + res[1] + "\n";
							}
						}
						catch (Exception e) {MyLogger.handleException(e);}
					}
				}
				//System.out.println("ergebnis: " + ergebnis);
				if (ergebnis.trim().length() > 0) {
					result = new Vector<>();
					result.add(new String[]{ergebnis});
				}
			}
			else {
				result = null;
			}
	  	}
		else if (tablename.equals("Versuchsbedingungen")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Referenz") + " IS NULL",
						GuiMessages.getString("Eine Referenz in den Versuchsbedingungen sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Agens") + " IS NULL",
						GuiMessages.getString("Ein Agens in den Versuchsbedingungen sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Matrix") + " IS NULL",
						GuiMessages.getString("Eine Matrix in den Versuchsbedingungen sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Temperatur") + " IS NULL",
						GuiMessages.getString("Temperatur muss angegeben werden!")
						});
			}
		}
		else if (tablename.equals("Kits")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Kosten") + " IS NOT NULL AND " + DBKernel.delimitL("KostenEinheit") + " IS NULL",
						GuiMessages.getString("Die Einheit für die Kosten sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("ZertifikatNr") + " IS NOT NULL AND (" + DBKernel.delimitL("Gueltigkeit") + " IS NULL OR " + DBKernel.delimitL("Zertifizierungssystem") + " IS NULL)",
						GuiMessages.getString("Wenn die ZertifikatNummer angegeben ist, dann sollte auch deren Gültigkeit und das Zertifizierungssystem angegeben sein!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Testanbieter") + " IS NULL OR " + DBKernel.delimitL("AnbieterAngebot") + " IS NULL",
						GuiMessages.getString("Einen Testanbieter und ein AnbieterAngebot sollte es immer geben! Wenn es ein InHouse Kit ist, dann sollte als Kontakt die eigene Adresse angegeben werden und als Angebot interne Informationen")
						});
			}
		}		
		else if (tablename.equals("Aufbereitungsverfahren")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Dauer") + " IS NULL OR " + DBKernel.delimitL("DauerEinheit") + " IS NULL",
						GuiMessages.getString("Die Dauer und deren Einheit sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Personalressourcen") + " IS NOT NULL AND " + DBKernel.delimitL("ZeitEinheit") + " IS NULL",
						GuiMessages.getString("Die Zeiteinheit für die Personalressourcen sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Kosten") + " IS NULL OR " + DBKernel.delimitL("KostenEinheit") + " IS NULL",
						GuiMessages.getString("Die Kosten und deren Einheit sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Matrix") + " IS NULL",
						GuiMessages.getString("Eine Matrix sollte angegeben werden!")
						});
			}
		}
		else if (tablename.equals("Nachweisverfahren")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Kosten") + " IS NULL OR " + DBKernel.delimitL("KostenEinheit") + " IS NULL",
						GuiMessages.getString("Die Kosten und deren Einheit sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Agens") + " IS NULL",
						GuiMessages.getString("Ein Agens sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Dauer") + " IS NULL OR " + DBKernel.delimitL("DauerEinheit") + " IS NULL",
						GuiMessages.getString("Die Dauer und deren Einheit sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Personalressourcen") + " IS NOT NULL AND " + DBKernel.delimitL("ZeitEinheit") + " IS NULL",
						GuiMessages.getString("Die Zeiteinheit für die Personalressourcen sollte eingetragen werden!")
						});
			}
		}
		else if (tablename.equals("Aufbereitungs_Nachweisverfahren")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Nachweisgrenze") + " IS NOT NULL AND " + DBKernel.delimitL("NG_Einheit") + " IS NULL",
						GuiMessages.getString("Die Einheit für die Nachweisgrenze sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Referenz") + " IS NULL",
						GuiMessages.getString("Eine Referenz sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Nachweisverfahren") + " IS NULL",
						GuiMessages.getString("Ein Nachweisverfahren sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Aufbereitungsverfahren") + " IS NULL",
						GuiMessages.getString("Ein Aufbereitungsverfahren sollte angegeben werden!")
						});
			}
		}
		else if (tablename.equals("Labor_Aufbereitungs_Nachweisverfahren")) {
			if (table == null) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Durchsatz") + " IS NOT NULL AND " + DBKernel.delimitL("DurchsatzEinheit") + " IS NULL",
						GuiMessages.getString("Die Einheit für den Durchsatz sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Kosten") + " IS NULL OR " + DBKernel.delimitL("KostenEinheit") + " IS NULL",
						GuiMessages.getString("Die Kosten und deren Einheit sollte eingetragen werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("ZertifikatNr") + " IS NOT NULL AND (" + DBKernel.delimitL("Gueltigkeit") + " IS NULL OR " + DBKernel.delimitL("Zertifizierungssystem") + " IS NULL)",
						GuiMessages.getString("Wenn die ZertifikatNummer angegeben ist, dann sollte auch deren Gültigkeit und das Zertifizierungssystem angegeben sein!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Labor") + " IS NULL",
						GuiMessages.getString("Ein Labor sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Aufbereitungs_Nachweisverfahren") + " IS NULL",
						GuiMessages.getString("Ein Aufbereitungs_Nachweisverfahren sollte angegeben werden!")
						});
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("LaborAngebot") + " IS NULL",
						GuiMessages.getString("Ein LaborAngebot sollte angegeben werden!")
						});
			}
		}
		if (table == null) {
			if (!myT.getHideScore()) {
				result.add(new String[]{
						"SELECT " + idConf + " FROM " + DBKernel.delimitL(tablename) +
						" WHERE " + DBKernel.delimitL("Guetescore") + " IS NOT NULL AND (" + DBKernel.delimitL("Kommentar") + " IS NULL OR LENGTH(LTRIM(" + DBKernel.delimitL("Kommentar") + "))=0)",
						GuiMessages.getString("Wenn die Ampel geschaltet ist, MUSS ein Kommentar dazu abgegeben werden!")
						});				
			}
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	private static Double getDouble(String strVal) {
		Double val = null;
		try {
			val = Double.valueOf(strVal);						
		}
		catch (Exception e) {}
		return val;
	}
	@SuppressWarnings("unused")
	private static String checkPlausibleDBL(Object kzID, String tname, String spaltenname, Integer tableID) {
		String ergebnis = null;		
		if (kzID != null) {
			String msg = GuiMessages.getString("Wenn die Anzahl der Wiederholungen = 1 ist, dann sollte ein Einzelwert eingetragen werden! Anzahl Wiederholungen < 1 machen keinen Sinn!");
			String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("DoubleKennzahlen") +
			" WHERE " + DBKernel.delimitL("Wiederholungen") + " <= 1 AND " + DBKernel.delimitL("ID") + "=" + kzID;
			ResultSet rs = DBKernel.getResultSet(sql, false);
			try {
				if (rs != null && rs.first()) {
					ergebnis += tname + " (ID=" + tableID + ", " + spaltenname + "): " + msg + "\n";
				}
			}
			catch (Exception e) {MyLogger.handleException(e);}

			msg = GuiMessages.getString("Wenn die Anzahl der Wiederholungen nicht definiert ist, dann kann MW/Median/UCL/LCL/SD/Verteilung nicht gegeben sein!");
			sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("DoubleKennzahlen") +
			" WHERE " + DBKernel.delimitL("ID") + "=" + kzID + " AND " +
			DBKernel.delimitL("Wiederholungen") + " IS NULL AND (" +
			DBKernel.delimitL("Wert") + " IS NOT NULL AND " + DBKernel.delimitL("Wert_typ") + " IS NOT NULL AND " + DBKernel.delimitL("Wert_typ") + " > 1 OR " +
			DBKernel.delimitL("UCL95") + " IS NOT NULL OR " + DBKernel.delimitL("LCL95") + " IS NOT NULL OR " +
			DBKernel.delimitL("Standardabweichung") + " IS NOT NULL OR " + DBKernel.delimitL("Verteilung") + " IS NOT NULL)";
			rs = DBKernel.getResultSet(sql, false);
			try {
				if (rs != null && rs.first()) {
					ergebnis += tname + " (ID=" + tableID + ", " + spaltenname + "): " + msg + "\n";
				}
			}
			catch (Exception e) {MyLogger.handleException(e);}

			msg = GuiMessages.getString("Wenn die Anzahl der Wiederholungen > 1 ist, dann kann ein Einzelwert nicht gegeben sein!");
			sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("DoubleKennzahlen") +
			" WHERE " + DBKernel.delimitL("ID") + "=" + kzID + " AND " +
			DBKernel.delimitL("Wiederholungen") + " > 1 AND " +
			DBKernel.delimitL("Wert") + " IS NOT NULL AND (" + DBKernel.delimitL("Wert_typ") + " IS NULL OR " + DBKernel.delimitL("Wert_typ") + " = 1)";
			rs = DBKernel.getResultSet(sql, false);
			try {
				if (rs != null && rs.first()) {
					ergebnis += tname + " (ID=" + tableID + ", " + spaltenname + "): " + msg + "\n";
				}
			}
			catch (Exception e) {MyLogger.handleException(e);}

			msg = GuiMessages.getString("Wenigstens einer der Werte Wert, Minimum, Maximum muss angegeben sein!");
			sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("DoubleKennzahlen") +
			" WHERE " + DBKernel.delimitL("ID") + "=" + kzID + " AND " +
			DBKernel.delimitL("Wert") + " IS NULL AND " +
			DBKernel.delimitL("Minimum") + " IS NULL AND " +
			DBKernel.delimitL("Maximum") + " IS NULL";
			rs = DBKernel.getResultSet(sql, false);
			try {
				if (rs != null && rs.first()) {
					ergebnis += tname + " (ID=" + tableID + ", " + spaltenname + "): " + msg + "\n";
				}
			}
			catch (Exception e) {MyLogger.handleException(e);}
		}
		return ergebnis;
	}
}
