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
package de.bund.bfr.knime.openkrise.db;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import de.bund.bfr.knime.openkrise.db.imports.SQLScriptImporter;

public class MyDBTablesNew extends MyDBI {

	private static int SystemTabellen_LIST = 0;
	private static int BasisTabellen_LIST = 1;
	private static int Tenazitaet_LIST = 2;
	private static int PMModelle_LIST = 3;
	private static int Krankheitsbilder_LIST = 4;
	private static int Prozessdaten_LIST = 5;
	private static int Nachweissysteme_LIST = 6;
	private static int Lieferketten_LIST = 7;

	private LinkedHashMap<String, MyTable> allTables = new LinkedHashMap<>();
	private HashMap<String, LinkedHashMap<Object, String>> allHashes = new HashMap<>();
	private LinkedHashMap<String, int[]> knownCodeSysteme = null;
	private LinkedHashMap<Integer, String> treeStructure = null;
	private LinkedHashSet<String> allViews = null;
	private LinkedHashMap<String, String> allData = null;
	private String saUser = "SA";//"defad"; // SA
	private String saPass = "";//"de6!§5ddy";
	private String dbServerPath = "";
	private String softwareVersion = "1.8.3";
	
	private boolean isPmm = false;
	private boolean isKrise = true;
	private boolean isSiLeBAT = false;
	
	/*
	 * Still todo:
	 *   DateiSpeicher -> FileStorage
		
		Table "Literatur" in MyNewDoubleKennzahlen.... Shall I make Literatur to a BASE table???
		- PlausibilityChecker
		- Merging
		- Imports

	@Override
	public String getCommentTerm() {
		return "Kommentar";
	}
	@Override
	public String getTestedTerm() {
		return "Geprueft";
	}
	@Override
	public String getScoreTerm() {
		return "Guetescore";
	}
	 */
	
	public MyDBTablesNew() {
		loadHashes();
		loadMyTables();
		loadOther4Db();
		loadOther4Gui();
	}

	@Override
	public void updateCheck(String fromVersion, String toVersion) {
		if (fromVersion.equals("1.7.7") && toVersion.equals("1.7.8")) {
			DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Lieferungen") + " ALTER COLUMN " + DBKernel.delimitL("Explanation_EndChain") + " VARCHAR(16383)", false);
			DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Lieferungen") + " ALTER COLUMN " + DBKernel.delimitL("Contact_Questions_Remarks") + " VARCHAR(16383)", false);
		}
		else if (fromVersion.equals("1.7.8") && toVersion.equals("1.7.9")) {
			DBKernel.sendRequest("DROP VIEW IF EXISTS " + DBKernel.delimitL("EstModelPrimView") + ";", false);
			DBKernel.sendRequest("DROP VIEW IF EXISTS " + DBKernel.delimitL("EstModelSecView") + ";", false);
			new SQLScriptImporter().doImport("/de/bund/bfr/knime/openkrise/db/res/002_EstModelPrimView_179.sql", null, false);
			new SQLScriptImporter().doImport("/de/bund/bfr/knime/openkrise/db/res/002_EstModelSecView_179.sql", null, false);
		}
	}

	@Override
	public String getSA() {
		return saUser;
	}
	@Override
	public String getSAP() {
		return saPass;
	}

	@Override
	public void setSA_P(String user, String pass) {
		saUser = user;
		saPass = pass;
	}
	@Override
	public String getDbServerPath() {
		return dbServerPath;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	
	@Override
	public LinkedHashMap<Integer, String> getTreeStructure() {		
		return treeStructure;
	}

	@Override
	public LinkedHashMap<String, int[]> getKnownCodeSysteme() {
		return knownCodeSysteme;
	}

	@Override
	public LinkedHashMap<Object, String> getHashMap(final String key) {
		if (allHashes.containsKey(key)) {
			return allHashes.get(key);
		}
		return null;
	}
	
	@Override
	public LinkedHashMap<String, MyTable> getAllTables() {
		return allTables;
	}

	@Override
	public void recreateTriggers() {
		for(String key : allTables.keySet()) {
			String tableName = allTables.get(key).getTablename();
			DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_" + tableName + "_U"), false);
			DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_" + tableName + "_D"), false);
			DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_" + tableName + "_I"), false);
			DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("A_" + tableName + "_U"), false);
			DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("A_" + tableName + "_D"), false);
			DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("A_" + tableName + "_I"), false);
			if (!tableName.equals("ChangeLog") && !tableName.equals("DateiSpeicher") && !tableName.equals("Infotabelle")) {
				DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_D") + " AFTER DELETE ON " +
						DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false); // (oneThread ? "QUEUE 0" : "") +    
				DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_I") + " AFTER INSERT ON " +
						DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false); // (oneThread ? "QUEUE 0" : "") +
				DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_U") + " AFTER UPDATE ON " +
						DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false); // (oneThread ? "QUEUE 0" : "") +
			}
		}
		DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_USERS_U"), false);
		DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_USERS_D"), false);
		DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_USERS_I"), false);
		DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("B_Users_I") + " BEFORE INSERT ON " +
	        		DBKernel.delimitL("Users") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false);    	
	        // Zur Überwachung, damit immer mindestens ein Admin übrig bleibt; dasselbe gibts im MyDataChangeListener für Delete Operations!
	        // Außerdem zur Überwachung, daß der eingeloggte User seine Kennung nicht ändert
		DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("B_Users_U") + " BEFORE UPDATE ON " +
	        		DBKernel.delimitL("Users") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false);   
	        // Zur Überwachung, damit eine importierte xml Datei nicht gelöscht werden kann!
		DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("B_ProzessWorkflow_U") + " BEFORE UPDATE ON " +
	        		DBKernel.delimitL("ProzessWorkflow") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false);    	
	}

	@SuppressWarnings("unchecked")
	private void loadMyTables() {
		MyTable cl = new MyTable("ChangeLog",
				new String[]{"Zeitstempel","Username","Tabelle","TabellenID","Alteintrag"},
				new String[]{"DATETIME","VARCHAR(60)","VARCHAR(100)","INTEGER","OTHER"},
				new String[]{null,null,null,null,null},
				new MyTable[]{null,null,null,null,null});
		addTable(cl, SystemTabellen_LIST);
		MyTable bs = new MyTable("DateiSpeicher",
				new String[]{"Zeitstempel","Tabelle","TabellenID","Feld","Dateiname","Dateigroesse","Datei"},
				new String[]{"DATETIME","VARCHAR(100)","INTEGER","VARCHAR(100)","VARCHAR(255)","INTEGER","BLOB(10M)"},
				new String[]{null,null,null,null,null,null,null},
				new MyTable[]{null,null,null,null,null,null,null});
		addTable(bs, SystemTabellen_LIST);
		MyTable us = new MyTable("Users",
				new String[]{"Username","Vorname","Name","Zugriffsrecht"},
				new String[]{"VARCHAR(60)","VARCHAR(30)","VARCHAR(30)","INTEGER"},
				new String[]{null,null,null,null},
				new MyTable[]{null,null,null,null},
				new String[][]{{"Username"}},
				new LinkedHashMap[]{null,null,null,Users.getUserTypesHash()});
		addTable(us, SystemTabellen_LIST); // müsste jetzt doch gehen, oder?...  lieber die Users ganz weg, weil das Editieren auf dem HSQLServer nicht korrekt funktioniert - siehe im Trigger removeAccRight usw., da müsste man erst die sendRequests umstellen auf defaultconnection...		

		MyTable infoTable = new MyTable("Infotabelle",
				new String[]{"Parameter","Wert"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"},
				new String[]{null, null},
				new MyTable[]{null,null},
				new String[][]{{"Parameter"}},
				new LinkedHashMap[]{null,null});
		addTable(infoTable, -1);

		// Paper, SOP, LA, Manual/Handbuch, Laborbuch
		LinkedHashMap<Integer, String> lt = new LinkedHashMap<>();
	    lt.put(new Integer(1), "Paper");
	    lt.put(new Integer(2), "SOP");
	    lt.put(new Integer(3), "LA");
	    lt.put(new Integer(4), "Handbuch");
	    lt.put(new Integer(5), "Laborbuch");
	    lt.put(new Integer(6), "Buch");
	    lt.put(new Integer(7), "Webseite");
	    lt.put(new Integer(8), "Bericht");
		MyTable literatur = new MyTable("Literatur", new String[]{"Erstautor","Jahr","Titel","Abstract","Journal","Volume","Issue","Seite","FreigabeModus","Webseite","Literaturtyp","Paper"},
				new String[]{"VARCHAR(100)","INTEGER","VARCHAR(1023)","VARCHAR(16383)","VARCHAR(255)","VARCHAR(50)","VARCHAR(50)","INTEGER","INTEGER","VARCHAR(255)","INTEGER","BLOB(10M)"},
				new String[]{"Erstautor der Publikation","Veröffentlichungsjahr","Titel des Artikels","Abstract/Zusammenfassung des Artikels","Journal / Buchtitel / ggf. Webseite / Veranstaltung etc.",null,null,"Seitenzahl_Beginn","Auswahl ob diese Information oeffentlich zugaenglich sein soll: nie, nur in der Krise, immer - Auswahlbox",null,"Auswahl zwischen Paper, SOP, LA, Handbuch/Manual, Laborbuch","Originaldatei"},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,null},
				new String[][]{{"Erstautor","Jahr","Titel"}},
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,allHashes.get("Freigabe"),null,lt,null},
				null,
				new String[]{null,null,null,null,null,null,null,null,null,null,null,"*.pdf, *.doc"},
				new LinkedList<>(Arrays.asList("Erstautor"," (","Jahr",")")));
		addTable(literatur, DBKernel.isKrise ? -1 : (DBKernel.isKNIME ? BasisTabellen_LIST : 66));

		LinkedHashMap<Integer, String> wt = new LinkedHashMap<>();
		wt.put(new Integer(1), "Einzelwert");
		wt.put(new Integer(2), "Mittelwert");
		wt.put(new Integer(3), "Median");
		MyTable newDoubleTable = new MyTable("DoubleKennzahlen",
				new String[]{"Wert","Exponent","Wert_typ","Wert_g","Wiederholungen","Wiederholungen_g","Standardabweichung","Standardabweichung_exp","Standardabweichung_g",
				"Minimum","Minimum_exp","Minimum_g","Maximum","Maximum_exp","Maximum_g",
				"LCL95","LCL95_exp","LCL95_g","UCL95","UCL95_exp","UCL95_g",
				"Verteilung","Verteilung_g","Funktion (Zeit)","Funktion (Zeit)_g","Funktion (x)","x","Funktion (x)_g",
				"Undefiniert (n.d.)","Referenz"},
				new String[]{"DOUBLE","DOUBLE","INTEGER","BOOLEAN","DOUBLE","BOOLEAN","DOUBLE","DOUBLE","BOOLEAN",
				"DOUBLE","DOUBLE","BOOLEAN","DOUBLE","DOUBLE","BOOLEAN",
				"DOUBLE","DOUBLE","BOOLEAN","DOUBLE","DOUBLE","BOOLEAN",
				"VARCHAR(1023)","BOOLEAN","VARCHAR(1023)","BOOLEAN","VARCHAR(1023)","VARCHAR(25)","BOOLEAN",
				"BOOLEAN","INTEGER"},
				new String[]{"Wert","Exponent zur Basis 10, falls vorhanden\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte","Wert_typ ist entweder Einzelwert, Mittelwert oder Median","Der Einzelwert wurde nicht wirklich gemessen, sondern geschaetzt (ja=geschaetzt, nein=gemessen)","Anzahl der Wiederholungsmessungen/technischen Replikate für diesen Wert","geschaetzt","Standardabweichung des gemessenen Wertes - Eintrag nur bei Mehrfachmessungen möglich","Exponent zur Basis 10 für die Standardabweichung, falls vorhanden","geschaetzt",
				"Minimum","Exponent zur Basis 10 für das Minimum, falls vorhanden","geschaetzt","Maximum","Exponent zur Basis 10 für das Maximum, falls vorhanden","geschaetzt",
				"Untere Konfidenzgrenze des gemessenen Wertes (95%-KI) - Eintrag nur bei Mehrfachmessungen möglich","Exponent zur Basis 10 für LCL95, falls vorhanden","geschaetzt","Obere Konfidenzgrenze des gemessenen Wertes (95%-KI) - Eintrag nur bei Mehrfachmessungen möglich","Exponent zur Basis 10 für UCL95, falls vorhanden","geschaetzt",
				"Verteilung der Werte bei Mehrfachmessungen, z.B. Normalverteilung. Anzugeben ist die entsprechende Funktion in R, z.B. rnorm(n, mean = 0, sd = 1)","geschaetzt","'Parameter'/Zeit-Profil. Funktion des Parameters in Abhaengigkeit von der Zeit.\nFuer das Parsen wird die Klasse http://math.hws.edu/javamath/javadoc/edu/hws/jcm/data/Parser.html benutzt.","geschaetzt","'Parameter'/x-Profil. Funktion des Parameters in Abhängigkeit des anzugebenden x-Parameters.\nFür das Parsen wird die Klasse http://math.hws.edu/javamath/javadoc/edu/hws/jcm/data/Parser.html benutzt.","Der zugehoerige x-Parameter: Bezugsgroesse für eine Funktion, z.B. Temperatur in Abhaengigkeit von pH, dann ist die Bezugsgroesse pH.","geschaetzt",
				"Undefiniert (n.d.)","Referenz zu diesen Kennzahlen"},
				new MyTable[]{null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,null,
				null,literatur},
				null,
				new LinkedHashMap[]{null,null,wt,null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,null,
				null,null},
				null,
				null,
				new LinkedList<>(Arrays.asList("Wert","*10^","Exponent")));
		addTable(newDoubleTable, -1);

		// Katalogtabellen
		MyTable matrix = new MyTable("Matrices", new String[]{"Matrixname","Leitsatznummer","pH","aw","Dichte","Katalogcodes"},
				new String[]{"VARCHAR(255)","VARCHAR(20)","DOUBLE","DOUBLE","DOUBLE","INTEGER"},
				new String[]{"Kulturmedium / Futtermittel / Lebensmittel / Serum / Kot / Gewebe","Leitsatznummer - falls bekannt","pH-Wert über alle Produkte der Warengruppe - falls abschaetzbar","aw-Wert über alle Produkte der Warengruppe - falls abschaetzbar","Dichte der Matrix über alle Produkte der Warengruppe - falls abschaetzbar","Matrixkatalog - Codes"},
				new MyTable[]{null,null,newDoubleTable,newDoubleTable,newDoubleTable,null},
				null,
				null,
				new String[]{null,null,null,null,null,"INT"},
				null,
				new LinkedList<>(Arrays.asList("Matrixname")));
		addTable(matrix, -1);
		
		MyTable toxinUrsprung = new MyTable("ToxinUrsprung", new String[]{"Ursprung"},
				new String[]{"VARCHAR(255)"},
				new String[]{null},
				new MyTable[]{null});
		if (isSiLeBAT) addTable(toxinUrsprung, -1);
		
		LinkedHashMap<Integer, String> btv = new LinkedHashMap<>();
		btv.put(new Integer(1), "Bakterium");	btv.put(new Integer(2), "Toxin"); btv.put(new Integer(3), "Virus");
		LinkedHashMap<Integer, String> h1234 = new LinkedHashMap<>();
		h1234.put(new Integer(1), "eins");	h1234.put(new Integer(2), "zwei");
		h1234.put(new Integer(3), "drei");	h1234.put(new Integer(4), "vier");					
		LinkedHashMap<Integer, String> hPM = new LinkedHashMap<>();
		hPM.put(new Integer(1), "+");	hPM.put(new Integer(2), "-");
		LinkedHashMap<Integer, String> hYN = new LinkedHashMap<>();
		if (DBKernel.getLanguage().equalsIgnoreCase("en")) {hYN.put(new Integer(1), "yes");	hYN.put(new Integer(0), "no");}
		else {hYN.put(new Integer(1), "ja");	hYN.put(new Integer(0), "nein");}
		LinkedHashMap<Boolean, String> hYNB = new LinkedHashMap<>();
		if (DBKernel.getLanguage().equalsIgnoreCase("en")) {hYNB.put(new Boolean(true), "yes");	hYNB.put(new Boolean(false), "no");}
		else {hYNB.put(new Boolean(true), "ja");	hYNB.put(new Boolean(false), "nein");}
		LinkedHashMap<Integer, String> hYNT = new LinkedHashMap<>();
		hYNT.put(new Integer(1), "mit Therapie");hYNT.put(new Integer(0), "ohne Therapie");hYNT.put(new Integer(2), "Keine Angabe");
		
		MyTable agenzien = new MyTable("Agenzien",
				new String[]{"Agensname","Kurzbezeichnung","WissenschaftlicheBezeichnung",
				"Klassifizierung","Familie","Gattung","Spezies","Subspezies_Subtyp",
				"Risikogruppe","Humanpathogen","Ursprung","Gramfaerbung",
				"CAS_Nummer","Carver_Nummer","FaktSheet","Katalogcodes"}, // Weitere Differenzierungsmerkmale [Stamm; Biovar, Serovar etc.]	lieber als AgensDetail
				new String[]{"VARCHAR(255)","VARCHAR(30)","VARCHAR(255)",
				"INTEGER","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)",
				"INTEGER","INTEGER","INTEGER","INTEGER",
				"VARCHAR(20)","VARCHAR(20)","BLOB(10M)","INTEGER"},
				new String[]{null,null,null,
				null,null,null,null,null,
				null,null,"Ursprung - nur bei Toxinen, z.B. Bakterium, Pflanzensamen","Gramfaerbung - nur bei Bakterien",
				null,null,"Datenblatt","Agenskatalog - Codes"},
				new MyTable[]{null,null,null,
				null,null,null,null,null,
				null,null,toxinUrsprung,null,
				null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,
				btv,null,null,null,null,
				h1234,hYN,null,hPM,
				null,null,null,null},
				new String[]{null,null,null,
				null,null,null,null,null,
				null,null,null,null,
				null,null,null,"INT"},
				new String[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,"*.pdf, *.doc",null},
				new LinkedList<>(Arrays.asList("Agensname")));
		addTable(agenzien, -1);
		MyTable normen = new MyTable("Methodennormen", new String[]{"Name","Beschreibung"},
				new String[]{"VARCHAR(255)","VARCHAR(1023)"},
				new String[]{"Name der Norm","Beschreibung der Norm"},
				new MyTable[]{null,null},
				null,
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("Name")));
		if (isSiLeBAT) addTable(normen, -1);
		MyTable methoden = new MyTable("Methoden", new String[]{"Name","Beschreibung","Referenz","Norm","Katalogcodes"},
				new String[]{"VARCHAR(1023)","VARCHAR(1023)","INTEGER","INTEGER","INTEGER"},
				new String[]{"Name des Nachweisverfahrens","Beschreibung des Nachweisverfahrens","Verweis auf Literaturstelle","Zugehörige Normen, z.B. ISO, DIN, CEN, etc.","Methodenkatalog - Codes"}, // ,"Angabe, ob Testreagenzien auch inhouse produziert werden können"
				new MyTable[]{null,null,literatur,normen,null},
				null,
				null,
				new String[]{null,null,null,"Methoden_Normen","INT"});
		if (isSiLeBAT) addTable(methoden, DBKernel.getUsername().equals("buschulte") ? Krankheitsbilder_LIST : -1);
		MyTable methoden_Normen = new MyTable("Methoden_Normen",
				new String[]{"Methoden","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{methoden,normen,null},
				null,
				new LinkedHashMap[]{null,null,null},
				null,
				null,
				new LinkedList<>(Arrays.asList("Norm_Nummer")));
		if (isSiLeBAT) addTable(methoden_Normen, -1);
		
		MyTable matrix_OG = new MyTable("Codes_Matrices", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, GS1, BLS, ADV oder auch selfmade)","Hierarchischer Code","Zugehörige Matrix"},
				new MyTable[]{null,null,matrix},
				new String[][]{{"CodeSystem","Code"}},
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("CodeSystem"," -> ","Code")));
		addTable(matrix_OG, -1); // -1
		matrix.setForeignField(matrix_OG, 5);
		MyTable agenzienkategorie = new MyTable("Codes_Agenzien", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, ADV oder auch selfmade)","Hierarchischer Code","Zugehöriges Agens"},
				new MyTable[]{null,null,agenzien},
				new String[][]{{"CodeSystem","Code"}},
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("CodeSystem"," -> ","Code")));
		addTable(agenzienkategorie, -1);
		agenzien.setForeignField(agenzienkategorie, 15);
		MyTable methoden_OG = new MyTable("Codes_Methoden", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(40)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, BLV oder auch selfmade)","Hierarchischer Code","Zugehörige Methode"},
				new MyTable[]{null,null,methoden},
				new String[][]{{"CodeSystem","Code"}},
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("CodeSystem"," -> ","Code")));
		if (isSiLeBAT) addTable(methoden_OG, -1); // -1
		methoden.setForeignField(methoden_OG, 4);

		MyTable ComBaseImport = new MyTable("ComBaseImport", new String[]{"Referenz","Agensname","Agenskatalog","b_f","Matrixname","Matrixkatalog"},
				new String[]{"INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)","VARCHAR(255)","INTEGER"},
				new String[]{null,null,null,null,null,null},
				new MyTable[]{literatur,null,agenzien,null,null,matrix});
		if (isSiLeBAT) addTable(ComBaseImport, -1);
		MyTable adressen = new MyTable("Kontakte",
				new String[]{"Name","Strasse","Hausnummer","Postfach","PLZ","Ort","Bundesland","Land","Ansprechpartner","Telefon","Fax","EMail","Webseite"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(10)","VARCHAR(20)","VARCHAR(10)","VARCHAR(60)","VARCHAR(30)","VARCHAR(100)","VARCHAR(100)","VARCHAR(30)","VARCHAR(30)","VARCHAR(100)","VARCHAR(255)"},
				new String[]{"Name der Firma / Labor / Einrichtung", null,null,null,null,null,null,null,"Ansprechpartner inkl. Vor und Zuname",null,null,null,null},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,allHashes.get("County"),null,null,null,null,null,null},
				null,
				null,
				new LinkedList<>(Arrays.asList("Name","Strasse","Ort")));
		if (isSiLeBAT) addTable(adressen, DBKernel.isKrise ? -1 : (DBKernel.isKNIME ? -1 : BasisTabellen_LIST));
		
		MyTable symptome = new MyTable("Symptome", new String[]{"Bezeichnung","Beschreibung","Bezeichnung_engl","Beschreibung_engl"},
				new String[]{"VARCHAR(50)","VARCHAR(255)","VARCHAR(50)","VARCHAR(255)"},
				new String[]{"Kurzform auf deutsch","Ausführliche Beschreibung auf deutsch","Kurzform auf englisch","Ausführliche Beschreibung auf englisch"},
				new MyTable[]{null,null,null,null},
				null,
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("Bezeichnung_engl","Bezeichnung")));
		if (isSiLeBAT) addTable(symptome, -1);

		MyTable risikogruppen = new MyTable("Risikogruppen", new String[]{"Bezeichnung","Beschreibung"},
				new String[]{"VARCHAR(50)","VARCHAR(255)"},
				new String[]{"Kurzform","Ausführliche Beschreibung"},
				new MyTable[]{null,null},
				null,
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("Bezeichnung")));
		if (isSiLeBAT) addTable(risikogruppen, -1);

		MyTable tierkrankheiten = new MyTable("Tierkrankheiten", new String[]{"VET_Code","Kurzbezeichnung","Krankheitsart"},
				new String[]{"VARCHAR(255)","VARCHAR(50)","VARCHAR(255)"},
				new String[]{null,"Kurzform","Ausführliche Beschreibung"},
				new MyTable[]{null,null,null});
		if (isSiLeBAT) addTable(tierkrankheiten, -1);
		
		MyTable krankheiten = null;
		if (isSiLeBAT) krankheiten = generateICD10Tabellen();
		
		LinkedHashMap<Object, String> h1 = new LinkedHashMap<>();
		h1.put("Human", "Human");h1.put("Kaninchen", "Kaninchen");h1.put("Maus", "Maus");h1.put("Ratte", "Ratte");
		h1.put("Meerschweinchen", "Meerschweinchen");h1.put("Primaten", "Primaten");h1.put("sonst. Säugetier", "sonst. Säugetier");
		LinkedHashMap<Object, String> h2 = new LinkedHashMap<>();
		h2.put("inhalativ", "inhalativ");					
		h2.put("oral", "oral");					
		h2.put("dermal", "dermal");		
		h2.put("Blut/Serum/Körperflüssigkeit", "Blut/Serum/Körperflüssigkeit");		
		h2.put("hämatogen", "hämatogen");							
		h2.put("transplazental", "transplazental");							
		h2.put("kutan", "kutan");					
		h2.put("venerisch", "venerisch");							
		h2.put("transkutan", "transkutan");							
		h2.put("intraperitoneal", "intraperitoneal");							
		h2.put("intravenös", "intravenös");							
		h2.put("subkutan", "subkutan");							
		h2.put("intramuskulär", "intramuskulär");							
		h2.put("Injektion", "Injektion");							
		LinkedHashMap<Object, String> h3 = new LinkedHashMap<>();
		h3.put("akut", "akut");					
		h3.put("chronisch", "chronisch");					
		h3.put("perkaut", "perkaut");		
		h3.put("subakut", "subakut");		
		LinkedHashMap<Object, String> k1 = new LinkedHashMap<>();
		k1.put("A", "A");k1.put("B", "B");k1.put("C", "C");					
		LinkedHashMap<Object, String> k2 = new LinkedHashMap<>();
		k2.put("1", "1");k2.put("1*", "1*");k2.put("1**", "1**");
		k2.put("2", "2");k2.put("2*", "2*");k2.put("2**", "2**");
		k2.put("3", "3");k2.put("3*", "3*");k2.put("3**", "3**");
		k2.put("4", "4");k2.put("4*", "4*");k2.put("4**", "4**");
		
		MyTable diagnostik = new MyTable("Krankheitsbilder", new String[]{"Referenz","Agens","AgensDetail","Risikokategorie_CDC","BioStoffV",
				"Krankheit","Symptome",
				"Zielpopulation","Aufnahmeroute","Krankheitsverlauf",
				"Risikogruppen",
				"Inzidenz","Inzidenz_Alter",
				"Inkubationszeit","IZ_Einheit",
				"Symptomdauer","SD_Einheit",
				"Infektionsdosis","ID_Einheit",
				"Letalitaetsdosis50","LD50_Einheit","LD50_Organismus","LD50_Aufnahmeroute",
				"Letalitaetsdosis100","LD100_Einheit","LD100_Organismus","LD100_Aufnahmeroute",
				"Meldepflicht",
				"Morbiditaet","Mortalitaet",
				"Letalitaet","Therapie_Letal","Ausscheidungsdauer",
				"ansteckend","Therapie","Antidot","Impfung","Todeseintritt",
				"Spaetschaeden","Komplikationen"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","VARCHAR(10)","VARCHAR(10)",
				"INTEGER","INTEGER",
				"VARCHAR(100)","VARCHAR(255)","VARCHAR(50)",
				"INTEGER",
				"DOUBLE","DOUBLE",
				"DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)","VARCHAR(100)","VARCHAR(255)",
				"DOUBLE","VARCHAR(50)","VARCHAR(100)","VARCHAR(255)",
				"BOOLEAN",
				"DOUBLE","DOUBLE",
				"DOUBLE","INTEGER","VARCHAR(50)",
				"BOOLEAN","BOOLEAN","BOOLEAN","BOOLEAN","VARCHAR(50)",
				"VARCHAR(255)","VARCHAR(255)"
				},
				new String[]{"Referenz - Verweis auf Tabelle Literatur","Agens - Verweis auf Tabelle Agenzien","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. Serovartyp","Risikokategorie laut Einstufung des Centers for Disease Control and Prevention (CDC)","Schutzstufen gemaess Verordnung über Sicherheit und Gesundheitsschutz bei Taetigkeiten mit biologischen Arbeitsstoffen (Biostoffverordnung - BioStoffV)",
				"Bezeichnung der Krankheit, wenn möglich Verweis auf die Internationale Klassifikation der Krankheiten 10. Revision (ICD10-Kodes)","Auswahl aus hinterlegtem Katalog mit der Möglichkeit, neue Symptome einzufügen; Mehrfachnennungen möglich",
				"Zielpopulation","Aufnahmeroute","Art des Krankheitsverlaufs",
				"Risikogruppen - Mehrfachnennungen möglich",
				"Anzahl der Neuerkrankungsfaelle/100.000/Jahr in Deutschland","Angabe der Altersgruppe, falls die Inzidenz altersbezogen angegeben ist",
				"Zeitraum von Aufnahme des Agens bis zum Auftreten des/r ersten Symptome/s","Inkubationszeit-Einheit",
				"Symptomdauer","Symptomdauer-Einheit",
				"Infektionsdosis oraler Aufnahme","Infektionsdosis-Einheit",
				"Letalitaetsdosis LD50: mittlere Dosis einer Substanz, die bei 50 % der Exponierten zum Tode führt","Letalitaetsdosis50-Einheit","Bei welchem Organismus wurden die Untersuchungen zu LD50 gemacht?","Welche Aufnahmeroute wurde gewaehlt?",
				"Letalitaetsdosis LD100: mittlere Dosis einer Substanz, die bei 100 % der Exponierten zum Tode führt","Letalitaetsdosis100-Einheit","Bei welchem Organismus wurden die Untersuchungen zu LD100 gemacht?","Welche Aufnahmeroute wurde gewaehlt?",
				"Meldepflicht nach Infektionsschutzgesetz",
				"Krankheitshaeufigkeit pro Jahr in Deutschland, falls nicht im Kommentarfeld anders vermerkt - Angabe in Prozent","Prozentualer Anteil der Todesfaelle, bezogen auf die Gesamtzahl der Bevoelkerung in Deutschland, falls nicht im Kommentarfeld anders vermerkt",
				"Verhaeltnis der Todesfaelle zur Anzahl der Erkrankten, angegeben in Prozent","Letalitaet: mit bzw. ohne Therapie","Ungefaehre, moegliche Dauer des Ausscheidens des Erregers",
				"Krankheit ist von Mensch zu Mensch übertragbar","Therapiemöglichkeit besteht","Antidotgabe möglich","Schutzimpfung verfügbar","Moeglicher Zeitraum vom Symptombeginn bis zum Eintritt des Todes",
				"Moegliche Spaetschaeden","Moegliche unguenstige Beeinflussung oder Verschlimmerung des Krankheitszustandes"},
				new MyTable[]{literatur,agenzien,null,null,null,
				krankheiten,symptome,
				null,null,null,
				risikogruppen,
				newDoubleTable,newDoubleTable,
				newDoubleTable,null,
				newDoubleTable,null,
				newDoubleTable,null,
				newDoubleTable,null,null,null,
				newDoubleTable,null,null,null,
				null,
				newDoubleTable,newDoubleTable,
				newDoubleTable,null,null,
				null,null,null,null,null,
				null,null},
				null,
				new LinkedHashMap[]{null,null,null,k1,k2,
				null,null,
				h1,h2,h3,
				null,
				null,null,
				null,allHashes.get("Time"),
				null,allHashes.get("Time"),
				null,allHashes.get("Dosis"),
				null,allHashes.get("Dosis"),h1,h2,
				null,allHashes.get("Dosis"),h1,h2,
				hYNB,
				null,null,
				null,hYNT,allHashes.get("Time"),
				hYNB,hYNB,hYNB,hYNB,allHashes.get("Time"),
				null,null},
				new String[]{null,null,null,null,null,
				null,"Krankheitsbilder_Symptome",
				null,null,null,
				"Krankheitsbilder_Risikogruppen",
				null,null,
				null,null,
				null,null,
				null,null,
				null,null,null,null,
				null,null,null,null,
				null,
				null,null,
				null,null,null,
				null,null,null,null,null,
				null,null});
		if (isSiLeBAT) addTable(diagnostik, Krankheitsbilder_LIST);
		MyTable krankheitsbildersymptome = new MyTable("Krankheitsbilder_Symptome",
				new String[]{"Krankheitsbilder","Symptome"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{diagnostik,symptome},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(krankheitsbildersymptome, -1);
		MyTable krankheitsbilderrisikogruppen = new MyTable("Krankheitsbilder_Risikogruppen",
				new String[]{"Krankheitsbilder","Risikogruppen"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{diagnostik,risikogruppen},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(krankheitsbilderrisikogruppen, -1);
		MyTable agensmatrices = new MyTable("Agenzien_Matrices", // ,"natürliches Vorkommen in Lebensmitteln in D"
				new String[]{"Agens","Matrix","Referenz"},
				new String[]{"INTEGER","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{agenzien,matrix,literatur},
				new LinkedHashMap[]{null,null,null});
		if (isSiLeBAT) addTable(agensmatrices, Krankheitsbilder_LIST);
		
		MyTable zertifikate = new MyTable("Zertifizierungssysteme", new String[]{"Bezeichnung","Abkuerzung","Anbieter"},
				new String[]{"VARCHAR(255)","VARCHAR(20)","INTEGER"},
				new String[]{"Vollstaendiger Name zum Zertifizierungssystem","Abkürzung für Zertifizierungssystem","Anbieter des Zertifizierungssystems - Verweis auf die Kontakttabelle"},
				new MyTable[]{null,null,adressen});
		if (isSiLeBAT) addTable(zertifikate, DBKernel.isKNIME ? -1 : BasisTabellen_LIST);
		
		MyTable methodiken = new MyTable("Methodiken", new String[]{"Name","Beschreibung","Kurzbezeichnung","WissenschaftlicheBezeichnung","Katalogcodes"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(30)","VARCHAR(255)","INTEGER"},
				new String[]{"Name der Methodik","Beschreibung der Methodik",null,null,"Methodenkatalog - Codes"},
				new MyTable[]{null,null,null,null,null},
				null,
				null,
				new String[]{null,null,null,null,"INT"},
				null,
				new LinkedList<>(Arrays.asList("Name")));
		if (isSiLeBAT) addTable(methodiken, -1);
		MyTable methodiken_OG = new MyTable("Codes_Methodiken", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung","Hierarchischer Code","Zugehörige Methode"},
				new MyTable[]{null,null,methodiken},
				new String[][]{{"CodeSystem","Code"}},
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("CodeSystem"," -> ","Code")));
		if (isSiLeBAT) addTable(methodiken_OG, -1); // -1
		methodiken.setForeignField(methodiken_OG, 4);
		h1 = new LinkedHashMap<>();
		h1.put("NRL", "NRL"); h1.put("Konsiliarlabor", "Konsiliarlabor"); h1.put("staatlich", "staatlich"); h1.put("GPV", "GPV"); h1.put("privat", "privat"); h1.put("sonstiges", "sonstiges");	// GPV = Gegenprobensachverständiger	
		MyTable labore = new MyTable("Labore", new String[]{"Kontakt","HIT_Nummer","ADV_Nummer",
				"privat_staatlich","Matrices","Untersuchungsart","Agenzien"},
				new String[]{"INTEGER","BIGINT","VARCHAR(10)",
				"VARCHAR(20)","INTEGER","INTEGER","INTEGER"},
				new String[]{"Verweis auf die Kontakttabelle - Tabelle enthaelt auch Betriebslabore","HIT-Nummer","ADV-Nummer",
				"Ist das Labor privat, staatlich, oder sogar ein NRL (Nationales Referenz Labor) oder ein GPV (Gegenprobensachverstaendigen Labor) oder etwas anderes?","Matrices, auf die das Labor spezialisiert ist. Mehrfachnennungen möglich.","Art der Untersuchung, Methoden. Mehrfachnennungen möglich.","Agenzien, die das Labor untersucht und für die die genutzten Methodiken bekannt sind. Mehrfachnennungen möglich."},
				new MyTable[]{adressen,null,null,null,matrix,methodiken,agenzien},
				new String[][]{{"HIT_Nummer"},{"ADV_Nummer"}},
				new LinkedHashMap[]{null,null,null,h1,null,null,null},
				new String[]{null,null,null,null,"Labore_Matrices","Labore_Methodiken","Labore_Agenzien"});
		if (isSiLeBAT) addTable(labore, DBKernel.isKNIME ? -1 : BasisTabellen_LIST);
		MyTable labore_Methodiken = new MyTable("Labore_Methodiken",
				new String[]{"Labore","Methodiken"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore,methodiken},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(labore_Methodiken, -1);
		MyTable labore_Matrices = new MyTable("Labore_Matrices",
				new String[]{"Labore","Matrices"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore,matrix},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(labore_Matrices, -1);
		MyTable labore_Agenzien = new MyTable("Labore_Agenzien",
				new String[]{"Labore","Agenzien","Methodiken"},
				new String[]{"INTEGER","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{labore,agenzien,methodiken},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,"Labore_Agenzien_Methodiken"});
		if (isSiLeBAT) addTable(labore_Agenzien, -1);
		MyTable labore_Agenzien_Methodiken = new MyTable("Labore_Agenzien_Methodiken",
				new String[]{"Labore_Agenzien","Methodiken"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore_Agenzien,methodiken},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(labore_Agenzien_Methodiken, -1);

		MyTable Konzentrationseinheiten = new MyTable("Einheiten", new String[]{"Einheit","Beschreibung",
				"name","kind of property / quantity","notation case sensitive","convert to","conversion function / factor",
				"inverse conversion function / factor","object type","display in GUI as","MathML string","Priority for display in GUI"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"
				,"VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)"
				,"VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(16383)","BOOLEAN"},
				new String[]{null,null,
				null,null,null,null,null,
				null,null,null,null,null},
				new MyTable[]{null,null,
				null,null,null,null,null,
				null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,
				null,null,null,null,null,
				null,null,null,null,null},
				new String[]{null,null,
				null,null,null,null,null,
				null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("display in GUI as")));
		if (isSiLeBAT || isPmm) addTable(Konzentrationseinheiten, BasisTabellen_LIST);
		MyTable SonstigeParameter = new MyTable("SonstigeParameter", new String[]{"Parameter","Beschreibung","Kategorie"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(255)"},
				new String[]{null,null,null},
				new MyTable[]{null,null,null},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Parameter")),
				null,
				new char[][]{{'_','$','\b'},null,null});
		if (isSiLeBAT || isPmm) addTable(SonstigeParameter, DBKernel.isKNIME ? BasisTabellen_LIST : -1);
		h1 = new LinkedHashMap<>();
	    h1.put("Fest", "Fest"); h1.put("Flüssig", "Flüssig"); h1.put("Gasförmig", "Gasförmig");		
		//min, avg, max
		

		MyTable kits = new MyTable("Kits", new String[]{"Bezeichnung","Testanbieter","ZertifikatNr","Gueltigkeit","Zertifizierungssystem","AnbieterAngebot","Kosten","KostenEinheit",
				"Einheiten","Probenmaterial","Aufbereitungsverfahren","Nachweisverfahren",
				"Extraktionssystem_Bezeichnung","DNA_Extraktion","RNA_Extraktion","Protein_Extraktion","Extraktionstechnologie",
				"Quantitativ","Identifizierung","Typisierung",
				"Methoden","Matrix","MatrixDetail","Agens","AgensDetail",
				"Spezialequipment","Laienpersonal",
				"Format","Katalognummer"},
				new String[]{"VARCHAR(50)","INTEGER","VARCHAR(50)","DATE","INTEGER","BLOB(10M)","DOUBLE","VARCHAR(50)",
				"INTEGER","VARCHAR(255)","BOOLEAN","BOOLEAN",
				"VARCHAR(255)","BOOLEAN","BOOLEAN","BOOLEAN","VARCHAR(255)",
				"BOOLEAN","BOOLEAN","BOOLEAN",
				"INTEGER","INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)",
				"BOOLEAN","BOOLEAN",
				"VARCHAR(255)","VARCHAR(255)"},
				new String[]{"Bezeichnung des Kits","Verweis auf Eintrag in Kontakttabelle - falls Testanbieter vorhanden","Zertifikatnummer - falls vorhanden","Gültigkeitsdatum des Zertifikats - falls vorhanden","Zertifizierungsanbieter - Verweis auf Tabelle Zertifizierungssysteme","Das Angebot kann ein individuelles Angebot, ein Katalogeintrag, eine E-Mail oder auch ein anderes Dokument des Testanbieters sein, moeglicherweise auch mit Angabe der Gueltigkeit des Angebots","Kosten für das Kit - Angabe ohne Mengenrabbatte. Geschaetzte Materialkosten, falls inhouse","Waehrung für die Kosten - Auswahlbox",
				"Anzahl der Kits pro Bestellung",null,null,null,
				null,null,null,null,null,
				"Quantitativ",null,null,
				null,null,null,null,null,
				null,null,
				null,null},
				new MyTable[]{null,adressen,null,null,zertifikate,null,null,null,
				null,null,null,null,
				null,null,null,null,null,
				null,null,null,
				methodiken,matrix,null,agenzien,null,
				null,null,
				null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,allHashes.get("Currency"),
				null,null,hYNB,hYNB,
				null,null,null,null,null,
				hYNB,hYNB,hYNB,
				null,null,null,null,null,
				hYNB,hYNB,
				null,null},
				null,
				new String[]{null,null,null,null,null,"*.pdf, *.doc",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null},
				new LinkedList<>(Arrays.asList("Bezeichnung")));
		if (isSiLeBAT) addTable(kits, Nachweissysteme_LIST);

		MyTable aufbereitungsverfahren = new MyTable("Aufbereitungsverfahren",
				new String[]{"Bezeichnung","Kurzbezeichnung","WissenschaftlicheBezeichnung",
				"Aufkonzentrierung","DNA_Extraktion","RNA_Extraktion","Protein_Extraktion",
				"Homogenisierung","Zelllyse",
				"Matrix","MatrixDetail","Agens","AgensDetail",
				"Kits","Dauer","DauerEinheit","Personalressourcen","ZeitEinheit",
				"Kosten","KostenEinheit",
				"Normen","SOP_LA","Spezialequipment","Laienpersonal","Referenz"},
				new String[]{"VARCHAR(255)","VARCHAR(30)","VARCHAR(255)",
				"BOOLEAN","BOOLEAN","BOOLEAN","BOOLEAN",
				"BOOLEAN","BOOLEAN",
				"INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)",
				"INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"INTEGER","BLOB(10M)","BOOLEAN","BOOLEAN","INTEGER"},
				new String[]{null,null,null,null,null,null,null,null,null,
				"Verweis auf Matrix in Matrixtabelle","Details zur Matrix, die durch den Katalog nicht abgebildet werden",
				"Verweis auf Eintrag in Agens-Tabelle","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. welches Serovar",
				"Notwendige Kits - hier können mehrere Kits ausgewaehlt werden","Dauer des Verfahrens (von Beginn bis Vorliegen des Ergebnisses)","Zeiteinheit der Dauer","Wie gross ist der durchschnittliche TA-Zeitaufwand zur Durchfuehrung des Nachweisverfahrens - geschaetzt?","Zeiteinheit für Zeitaufwand für Personal - Auswahlbox",
				"Geschaetzte Materialkosten, zusaetzlich zu den Kitkosten","Waehrung für die Kosten - Auswahlbox",
				"Zugehörige Normen, z.B. ISO, DIN, CEN, etc.","Standard Operating Procedure oder Laboranweisung - falls vorhanden","Wird für das Verfahren Spezialequipment benötigt? Details bitte ins Kommentarfeld eintragen","Kann das Verfahren ohne Fachpersonal durchgeführt werden? Details bitte ins Kommentarfeld eintragen","Referenz, gegebenenfalls Laborbuch"},
				new MyTable[]{null,null,null,null,null,null,null,null,null,
				matrix,null,agenzien,null,
				kits,null,null,null,null,
				null,null,
				normen,null,null,null,literatur},
				null,
				new LinkedHashMap[]{null,null,null,
				null,null,null,null,
				null,null,
				null,null,null,null,
				null,null,allHashes.get("Time"),null,allHashes.get("Time"),
				null,allHashes.get("Currency"),
				null,null,null,null,null},
				new String[]{null,null,null,
				null,null,null,null,
				null,null,
				null,null,null,null,
				"Aufbereitungsverfahren_Kits",null,null,null,null,
				null,null,
				"Aufbereitungsverfahren_Normen",null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Bezeichnung")));
		if (isSiLeBAT) addTable(aufbereitungsverfahren, Nachweissysteme_LIST);
		MyTable aufbereitungsverfahren_Kits = new MyTable("Aufbereitungsverfahren_Kits",
				new String[]{"Aufbereitungsverfahren","Kits"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{aufbereitungsverfahren,kits},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(aufbereitungsverfahren_Kits, -1);
		MyTable aufbereitungsverfahren_Normen = new MyTable("Aufbereitungsverfahren_Normen",
				new String[]{"Aufbereitungsverfahren","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{aufbereitungsverfahren,normen,null},
				new LinkedHashMap[]{null,null,null});
		if (isSiLeBAT) addTable(aufbereitungsverfahren_Normen, -1);

		MyTable nachweisverfahren = new MyTable("Nachweisverfahren",
				new String[]{"Bezeichnung",
				"Quantitativ","Identifizierung","Typisierung",
				"Methoden",
				"Matrix","MatrixDetail",
				"Agens","AgensDetail",
				"Kits","Dauer","DauerEinheit","Personalressourcen","ZeitEinheit",
				"Kosten","KostenEinheit",
				"Normen","SOP_LA","Spezialequipment","Laienpersonal","Referenz"},
				new String[]{"VARCHAR(255)",
				"BOOLEAN","BOOLEAN","BOOLEAN",
				"INTEGER",
				"INTEGER","VARCHAR(255)",
				"INTEGER","VARCHAR(255)",
				"INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"INTEGER","BLOB(10M)","BOOLEAN","BOOLEAN","INTEGER"},
				new String[]{null,"Handelt es sich um eine quantitative Methode? Falls nein, ist es automatisch eine qualitative Methode!","Handelt es sich um eine Methode zur Identifizierung des Agens?","Handelt es sich um eine Methode zur Typisierung des Agens?",
				"Methoden. Verweis auf Tabelle Methodiken",
				"Verweis auf Matrix in Matrixtabelle","Details zur Matrix, die durch den Katalog nicht abgebildet werden",
				"Verweis auf Eintrag in Agens-Tabelle","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. welches Serovar",
				"Notwendige Kits - hier können mehrere Kits ausgewaehlt werden","Dauer des Verfahrens (von Beginn bis Vorliegen des Ergebnisses)","Zeiteinheit der Dauer","Wie gross ist der durchschnittliche TA-Zeitaufwand zur Durchfuehrung des Nachweisverfahrens - geschaetzt?","Zeiteinheit für Zeitaufwand für Personal - Auswahlbox",
				"Geschaetzte Materialkosten, zusaetzlich zu den Kitkosten","Waehrung für die Kosten - Auswahlbox",
				"Zugehörige Normen, z.B. ISO, DIN, CEN, etc.","Standard Operating Procedure oder Laboranweisung - falls vorhanden","Wird für das Verfahren Spezialequipment benötigt? Details bitte ins Kommentarfeld eintragen","Kann das Verfahren ohne Fachpersonal durchgeführt werden? Details bitte ins Kommentarfeld eintragen","Referenz, gegebenenfalls Laborbuch"},
				new MyTable[]{null,
				null,null,null,
				methodiken,
				matrix,null,
				agenzien,null,
				kits,null,null,null,null,
				null,null,
				normen,null,null,null,literatur},
				null,
				new LinkedHashMap[]{null,
				null,null,null,
				null,
				null,null,
				null,null,
				null,null,allHashes.get("Time"),null,allHashes.get("Time"),
				null,allHashes.get("Currency"),null,null,null},
				new String[]{null,
				null,null,null,
				null,
				null,null,
				null,null,
				"Nachweisverfahren_Kits",null,null,null,null,
				null,null,
				"Nachweisverfahren_Normen",null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Bezeichnung")));
		if (isSiLeBAT) addTable(nachweisverfahren, Nachweissysteme_LIST);
		MyTable nachweisverfahren_Kits = new MyTable("Nachweisverfahren_Kits",
				new String[]{"Nachweisverfahren","Kits"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{nachweisverfahren,kits},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(nachweisverfahren_Kits, -1);
		MyTable nachweisverfahren_Normen = new MyTable("Nachweisverfahren_Normen",
				new String[]{"Nachweisverfahren","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{nachweisverfahren,normen,null},
				new LinkedHashMap[]{null,null,null});
		if (isSiLeBAT) addTable(nachweisverfahren_Normen, -1);

		MyTable aufbereitungs_nachweisverfahren = new MyTable("Aufbereitungs_Nachweisverfahren",
				new String[]{"Aufbereitungsverfahren","Nachweisverfahren","Nachweisgrenze","NG_Einheit","Sensitivitaet","Spezifitaet","Effizienz","Wiederfindungsrate","Referenz"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","INTEGER"},
				new String[]{null,null,"Nachweisgrenze des Verfahrens bezogen auf die Konzentration des Agens auf/in der Ausgangsmatrix","Einheit der Konzentration der Nachweisgrenze - Auswahlbox","Mittlere zu erwartende Sensitivitaet (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Mittlere zu erwartende Spezifitaet (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Mittlere zu erwartende Effizienz (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Wiederfindungsrate","Referenz für alle Angaben im Datensatz"},
				new MyTable[]{aufbereitungsverfahren,nachweisverfahren,newDoubleTable,Konzentrationseinheiten,null,null,null,null,literatur});
		if (isSiLeBAT) addTable(aufbereitungs_nachweisverfahren, Nachweissysteme_LIST);

		MyTable labor_aufbereitungs_nachweisverfahren = new MyTable("Labor_Aufbereitungs_Nachweisverfahren",
				new String[]{"Labor","Aufbereitungs_Nachweisverfahren","ZertifikatNr","Gueltigkeit","Zertifizierungssystem","Durchsatz","DurchsatzEinheit","Kosten","KostenEinheit","FreigabeModus","AuftragsAnnahme","SOP","LaborAngebot"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)","DATE","INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)","INTEGER","BOOLEAN","BOOLEAN","BLOB(10M)"},
				new String[]{"Verweis zum Eintrag in Labor-Tabelle","Verweis zum Eintrag in Kombi-Tabelle Aufbereitungs_Nachweisverfahren","Zertifikatnummer - falls vorhanden","Gültigkeitsdatum des Zertifikats - falls vorhanden","Zertifizierungsanbieter - Verweis auf Tabelle Zertifizierungssysteme","Angaben zum Durchsatz des Labors für das Verfahren - sollte im LaborAngebot angegeben sein","Einheit des Durchsatzes - Auswahlbox","Kosten pro Probe/Einzelansatz - ohne Rabatte - sollte im LaborAngebot angegeben sein","Waehrung für die Kosten - Auswahlbox","Auswahl ob diese Information oeffentlich zugaenglich sein soll: nie, nur in der Krise, immer - Auswahlbox","Nimmt das Labor auch externe Auftraege an?","Existiert eine SOP zu dem Verfahren bei dem Labor?","Das Angebot kann ein individuelles Angebot, ein Katalogeintrag, eine E-Mail oder auch ein anderes Dokument des Labors sein, moeglicherweise auch mit Angabe der Gueltigkeit des Angebots"},
				new MyTable[]{labore,aufbereitungs_nachweisverfahren,null,null,zertifikate,null,null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,allHashes.get("Speed"),null,allHashes.get("Currency"),allHashes.get("Freigabe"),null,null,null});
		if (isSiLeBAT) addTable(labor_aufbereitungs_nachweisverfahren, Nachweissysteme_LIST);

	
		h1 = new LinkedHashMap<>();
	    h1.put("in", "in"); h1.put("on", "on");	
	    /*
		h2 = new LinkedHashMap<Object, String>();
	    h2.put("BfR", "BfR");	
	    h2.put("ComBase", "ComBase");
	    h2.put("FLI", "FLI");	
	    h2.put("MRI", "MRI");	
	    h2.put("Andere", "Andere");	
	    */
		MyTable tenazity_raw_data = new MyTable("Versuchsbedingungen", new String[]{"Referenz","Agens","AgensDetail","Matrix","EAN","MatrixDetail",
				"Messwerte",
				"Temperatur","pH","aw","CO2","Druck","Luftfeuchtigkeit",
				"in_on","Sonstiges","ExperimentalDetails","Nachweisverfahren","FreigabeModus",
				"ID_CB","Organismus_CB","environment_CB","b_f_CB","b_f_details_CB"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)","VARCHAR(255)",
				"INTEGER",
				"DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE",
				"CHAR(2)","INTEGER","VARCHAR(16383)","INTEGER","INTEGER",
				"VARCHAR(50)","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)"},
				new String[]{"Verweis auf die zugehörige Literatur","Verweis auf den Erregerkatalog","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. Stamm, Serovar","Auswahl der Matrix","EAN-Nummer aus SA2-Datenbank - falls bekannt","Details zur Matrix, die durch den Katalog nicht abgebildet werden",
				"zugehörige Messwerte",
				"Experimentelle Bedingung: Temperatur in Grad Celcius","Experimentelle Bedingung: pH-Wert","Experimentelle Bedingung: aw-Wert","Experimentelle Bedingung: CO2 [ppm]","Experimentelle Bedingung: Druck [bar]","Experimentelle Bedingung: Luftfeuchtigkeit [%]",
				"Auf der Oberflaeche oder in der Matrix drin gemessen bzw. entnommen? - Auswahlbox (auf / innen)","Sonstige experimentelle Rahmenbedingungen in der Umgebung. Aber auch Facetten der Matrix.\nEs öffnet sich ein Fenster, in dem an die Combase angelehnte Parameter eingetragen werden können, vgl. Feld condition in der Combase:\nhttp://www.combase.cc/CB_TechnDescription.pdf",null,"Das benutzte Nachweisverfahren","Auswahl ob diese Information oeffentlich zugaenglich sein soll: nie, nur in der Krise, immer - Auswahlbox",
				"Eindeutige ID aus der Combase Datenbank - bei eigenen Eintraegen bleibt das Feld leer","Eintrag aus der Combase Datenbank - bei eigenen Eintraegen bleibt das Feld leer","Eintrag aus der Combase Datenbank - bei eigenen Eintraegen bleibt das Feld leer","Eintrag aus der Combase Datenbank - bei eigenen Eintraegen bleibt das Feld leer","Eintrag aus der Combase Datenbank - bei eigenen Eintraegen bleibt das Feld leer"},
				new MyTable[]{literatur,agenzien,null,matrix,null,null,
				null,
				newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,
				null,SonstigeParameter,null,aufbereitungs_nachweisverfahren,null,
				null,null,null,null,null},
				new String[][]{{"ID_CB"}},
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,null,null,
					null,h1,null,null,null,allHashes.get("Freigabe"),null,null,null,null,null},
				new String[]{null,null,null,null,null,null,"INT",null,null,null,null,null,null,
					null,"Versuchsbedingungen_Sonstiges",null,null,null,null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Agens","Matrix")));
		if (isSiLeBAT || isPmm) addTable(tenazity_raw_data, Tenazitaet_LIST);
		MyTable tenazity_measured_vals = new MyTable("Messwerte", new String[]{"Versuchsbedingungen","Zeit","ZeitEinheit",
				"Delta","Konzentration","Konz_Einheit",
				"Temperatur","pH","aw","CO2","Druck","Luftfeuchtigkeit",
				"Sonstiges"},
				new String[]{"INTEGER","DOUBLE","INTEGER",
				"BOOLEAN","DOUBLE","INTEGER",
				"DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE",
				"INTEGER"},
				new String[]{"Verweis auf die Tabelle mit den experimentellen Versuchsbedingungen","Vergangene Zeit nach Versuchsbeginn","Masseinheit der Zeit - Auswahlbox",
				"Falls angehakt:\nin den folgenden Feldern sind die Veraenderungen der Konzentration des Erregers im Vergleich zum Startzeitpunkt eingetragen.\nDabei bedeutet eine positive Zahl im Feld 'Konzentration' eine Konzentrationserhöhung, eine negative Zahl eine Konzentrationsreduzierung.","Konzentration des Erregers - Entweder ist die absolute Konzentration bzw. der Mittelwert bei Mehrfachmessungen hier einzutragen ODER die Konzentrationsaenderung, falls das Delta-Feld angehakt ist","Einheit zu den Konzentrationsangaben, auch der Logarithmus ist hier auswaehlbar - Auswahlbox",
				"Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: Temperatur in Grad Celcius","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: pH-Wert","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: aw-Wert","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: CO2 [ppm]","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: Druck [bar]","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: Luftfeuchtigkeit [%]",
				"Sonstige experimentelle Rahmenbedingungen in der Umgebung, aber auch Facetten der Matrix, falls abweichend von den festen Versuchsbedingungen.\nEs öffnet sich ein Fenster, in dem an die Combase angelehnte Parameter eingetragen werden können, vgl. Feld condition in der Combase:\nhttp://www.combase.cc/CB_TechnDescription.pdf"},
				new MyTable[]{tenazity_raw_data,newDoubleTable,Konzentrationseinheiten,null,newDoubleTable,Konzentrationseinheiten,
				newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,
				SonstigeParameter},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,
					null,null,null},
				new String[]{null,null,null,null,null,null,null,null,null,null,null,null,
					"Messwerte_Sonstiges"});
		if (isSiLeBAT || isPmm) addTable(tenazity_measured_vals, DBKernel.isKrise ? -1 : (DBKernel.isKNIME ? Tenazitaet_LIST : -1));
		tenazity_raw_data.setForeignField(tenazity_measured_vals, 6);

		MyTable Versuchsbedingungen_Sonstiges = new MyTable("Versuchsbedingungen_Sonstiges",
				new String[]{"Versuchsbedingungen","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{tenazity_raw_data,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("SonstigeParameter",": ","Wert"," ","Einheit")));
		if (isSiLeBAT || isPmm) addTable(Versuchsbedingungen_Sonstiges, -1);
		MyTable Messwerte_Sonstiges = new MyTable("Messwerte_Sonstiges",
				new String[]{"Messwerte","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{tenazity_measured_vals,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("SonstigeParameter",": ","Wert"," ","Einheit")));
		if (isSiLeBAT || isPmm) addTable(Messwerte_Sonstiges, -1);

		MyTable importedCombaseData = new MyTable("ImportedCombaseData",
				new String[]{"CombaseID","Literatur","Versuchsbedingung"},
				new String[]{"VARCHAR(100)","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{null,literatur,tenazity_raw_data},
				new String[][]{{"CombaseID","Literatur","Versuchsbedingung"}},
				new LinkedHashMap[]{null,null,null});
		if (isSiLeBAT || isPmm) addTable(importedCombaseData, -1);

		// Prozessdaten:
		MyTable betriebe = new MyTable("Produzent", new String[]{"Kontaktadresse","Betriebsnummer"},
				new String[]{"INTEGER","VARCHAR(50)"},
				new String[]{"Verweis auf Eintraege in Tabelle Kontakte mit Lebensmittel-Betrieben, Landwirten etc","Betriebsnummer aus BALVI-System sofern vorhanden"},
				new MyTable[]{adressen,null},
				null,
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("Kontaktadresse")));
		if (isSiLeBAT) addTable(betriebe, -1);
		MyTable betrieb_matrix_produktion = new MyTable("Betrieb_Matrix_Produktion", new String[]{"Betrieb","Matrix","EAN","Produktionsmenge","Einheit","Referenz","Anteil","lose"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","DOUBLE","VARCHAR(50)","INTEGER","DOUBLE","BOOLEAN"},
				new String[]{"Verweis auf die Basistabelle der Betriebe","Verweis auf die Matricestabelle","EAN-Nummer aus SA2-Datenbank - falls bekannt","Produktionsmenge des Lebensmittels","Verweis auf Basistabelle Masseinheiten","Verweis auf Literaturstelle","Anteil in %",null},
				new MyTable[]{betriebe,matrix,null,null,null,literatur,null,null},
				new LinkedHashMap[]{null,null,null,null,allHashes.get("Weight"),null,null,null});
		if (isSiLeBAT) addTable(betrieb_matrix_produktion, -1);
		MyTable prozessElemente = new MyTable("ProzessElemente",
				new String[]{"Prozess_ID","ProzessElement","ProzessElementKategorie","ProzessElementSubKategorie","ProzessElement_engl","ProzessElementKategorie_engl","ProzessElementSubKategorie_engl"},
				new String[]{"INTEGER","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)"},
				new String[]{"Prozess_ID in CARVER","Bezeichnung des Vorgangs bei der Prozessierung","Bezeichnung für die Kategorie, in die der Prozess einzuordnen ist","Bezeichnung der Unterkategorie für eine genauere Spezifizierung des Vorgangs","Wie ProzessElement, aber englische Bezeichnung","Wie ProzessElementKategorie, aber englische Bezeichnung","Wie ProzessElementSubKategorie, aber englische Bezeichnung"},
				new MyTable[]{null,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null},
				null,
				null,
				new LinkedList<>(Arrays.asList("ProzessElement")));
		if (isSiLeBAT) addTable(prozessElemente, Prozessdaten_LIST);
		h1 = new LinkedHashMap<>();
	    h1.put("EAN (betriebsspezifisch)", "EAN (betriebsspezifisch)");					
	    h1.put("Produktklasse (überbetrieblich)", "Produktklasse (überbetrieblich)");					
	    h1.put("Produktgruppe (überbetrieblich und produktübergreifen)", "Produktgruppe (überbetrieblich und produktübergreifen)");		
	    LinkedHashMap<Object, String> h4 = new LinkedHashMap<>();
	    h4.put(1, "Kilogramm");					
	    h4.put(2, "Gramm");					
	    h4.put(7, "Liter");					
	    h4.put(24, "Prozent (%)");					
	    h4.put(25, "Promille (‰)");					
	    h4.put(35, "Stück");					
	    
		MyTable prozessFlow = new MyTable("ProzessWorkflow",
				new String[]{"Name","Autor","Datum","Beschreibung","Firma","Produktmatrix","EAN","Prozessdaten","XML","Referenz"}, // ,"#Chargenunits","Unitmenge","UnitEinheit"
				new String[]{"VARCHAR(60)","VARCHAR(60)","DATE","VARCHAR(1023)","INTEGER","INTEGER","VARCHAR(255)","INTEGER","BLOB(10M)","INTEGER"}, // ,"DOUBLE","DOUBLE","INTEGER"
				new String[]{"Eigene Bezeichnung für den Workflow","Name des Eintragenden",null,"Beschreibung des Workflows in Prosa-Text","Verweis auf den Betrieb aus der Tabelle Produzent","Verweis auf Matrixkatalog","EAN-Nummer aus SA2-Datenbank - falls bekannt",null,"Ablage der CARVER XML Datei, die den Workflow abbildet",null}, // ,null,null,null
				new MyTable[]{null,null,null,null,betriebe,matrix,null,null,null,literatur}, // null,null,null,
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null}, // ,null,null,h4
				new String[]{null,null,null,null,null,null,null,"INT",null,"ProzessWorkflow_Literatur"},
				new String[]{null,null,null,null,null,null,null,null,"*.xml",null});
		if (isSiLeBAT) addTable(prozessFlow, Prozessdaten_LIST);
		MyTable prozessFlowReferenzen = new MyTable("ProzessWorkflow_Literatur",
				new String[]{"ProzessWorkflow","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{prozessFlow,literatur},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(prozessFlowReferenzen, -1);		
		
		MyTable Kostenkatalog = new MyTable("Kostenkatalog",
				new String[]{"Kostenart","Kostenunterart","Beschreibung","Einheit"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(25)"},
				new String[]{null,null,null,"Einheit pro Bezugseinheit (pro Liter Endprodukt)"},
				new MyTable[]{null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null},
				new String[]{null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Kostenunterart"," <","Einheit",">")));
		if (isSiLeBAT) addTable(Kostenkatalog, -1);
		MyTable Kostenkatalogpreise = new MyTable("Kostenkatalogpreise",
				new String[]{"Kostenkatalog","Betrieb","Datum","Preis","Waehrung"},
				new String[]{"INTEGER","INTEGER","DATE","DOUBLE","VARCHAR(50)"},
				new String[]{null,null,"Preis wurde erhoben am...",null,null},
				new MyTable[]{Kostenkatalog,betriebe,null,newDoubleTable,null},
				null,
				new LinkedHashMap[]{null,null,null,null,allHashes.get("Currency")},
				new String[]{null,null,null,null,null});
		if (isSiLeBAT) addTable(Kostenkatalogpreise, DBKernel.getUsername().equals("burchardi") || DBKernel.getUsername().equals("defad") ? 66 : -1);

		MyTable prozessdaten = new MyTable("Prozessdaten",
				new String[]{"Referenz","Workflow","Bezugsgruppe","Prozess_CARVER","ProzessDetail",
				"Kapazitaet","KapazitaetEinheit","KapazitaetEinheitBezug",
				"Dauer","DauerEinheit",
				"Zutaten",
				"Temperatur","pH","aw","CO2","Druck","Luftfeuchtigkeit",
				"Sonstiges","Tenazitaet","Kosten"}, // "Luftfeuchtigkeit [%]","Kochsalzgehalt [%]",
				new String[]{"INTEGER","INTEGER","VARCHAR(60)","INTEGER","VARCHAR(255)",
				"DOUBLE","INTEGER","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"INTEGER",
				"DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE",
				"INTEGER","INTEGER","INTEGER"},
				new String[]{"Verweise auf Eintraege aus der Tabelle Literatur, die diesen Prozessschritt beschreiben","Verweis auf einen Eintrag aus der Tabelle Workflow, zu dem dieser Prozessschritt gehört","Auswahlbox: EAN (betriebsspezifisch), Produktgruppe (überbetrieblich), Produktklasse (überbetrieblich)","Verweis auf einen Eintrag aus der Tabelle ProzessElemente, der den Prozesschritt benennt","DetailInformation zu diesem Prozessschritt",
				"Fassungsvermögen des Prozesselements, z.B. Volumen, Gewicht",null,"Bei einem kontinuierlichen Prozess muss die zeitliche Bezugsgroesse angegeben werden. Bei einem abgeschotteten Prozess bleibt das Feld leer",
				"Dauer des Prozessschritts","Einheit der Dauer",
				"Verweis auf Eintrag aus der Tabelle Zutatendaten, der Menge und Art der Zutat spezifiziert",
				"Temperatur - in °C!!!",null,null,null,"Druck - in [bar]!!!","Luftfeuchtigkeit - Einheit bitte in [%]",
				"Sonstige experimentelle Rahmenbedingungen in der Umgebung. Aber auch Facetten der Matrix.\nEs öffnet sich ein Fenster, in dem an die Combase angelehnte Parameter eingetragen werden können, vgl. Feld condition in der Combase:\nhttp://www.combase.cc/CB_TechnDescription.pdf",
				"Tenazitaetsdaten, falls vorliegend",null},
				new MyTable[]{literatur,prozessFlow,null,prozessElemente,null,
				newDoubleTable,null,null,
				newDoubleTable,null,
				null,
				newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,
				SonstigeParameter, agenzien, Kostenkatalog},
				null,
				new LinkedHashMap[]{null,null,h1,null,null,
				null,h4,allHashes.get("Time"),
				null,allHashes.get("Time"),
				null,
				null,null,null,null,null,null,
				null,null,null},
				new String[]{"Prozessdaten_Literatur",null,null,null,null,
				null,null,null,
				null,null,
				"INT",
				null,null,null,null,null,null,
				"Prozessdaten_Sonstiges","Prozessdaten_Messwerte","Prozessdaten_Kosten"},
				null,
				new LinkedList<>(Arrays.asList("Prozess_CARVER","ProzessDetail")));
		if (isSiLeBAT) addTable(prozessdaten, DBKernel.isKNIME ? Prozessdaten_LIST : -1); // Prozessdaten_LIST
		prozessFlow.setForeignField(prozessdaten, 7);
		MyTable prozessReferenzen = new MyTable("Prozessdaten_Literatur",
				new String[]{"Prozessdaten","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{prozessdaten,literatur},
				new LinkedHashMap[]{null,null});
		if (isSiLeBAT) addTable(prozessReferenzen, -1);		
		MyTable Prozessdaten_Sonstiges = new MyTable("Prozessdaten_Sonstiges",
				new String[]{"Prozessdaten","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{prozessdaten,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("SonstigeParameter",": ","Wert"," ","Einheit")));
		if (isSiLeBAT) addTable(Prozessdaten_Sonstiges, -1);
		MyTable Prozessdaten_Messwerte = new MyTable("Prozessdaten_Messwerte",
				new String[]{"Prozessdaten","ExperimentID","Agens","Zeit","ZeitEinheit","Konzentration","Einheit","Konzentration_GKZ","Einheit_GKZ"},
				new String[]{"INTEGER","INTEGER","INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","INTEGER","DOUBLE","INTEGER"},
				new String[]{null,null,null,"Zeitpunkt der Messung relativ zum Prozessschritt,\nd.h. falls die Messung z.B. gleich zu Beginn des Prozessschrittes gemacht wird,\ndann ist hier 0 einzutragen!\nUnabhaengig davon wie lange der gesamte Prozess schon laeuft!",null,"Konzentration des Agens","Konzentration - Einheit","Gesamtkeimzahl","Gesamtkeimzahl-Einheit"},
				new MyTable[]{prozessdaten,null,agenzien,newDoubleTable,null,newDoubleTable,Konzentrationseinheiten,newDoubleTable,Konzentrationseinheiten},
				null,
				new LinkedHashMap[]{null,null,null,null,allHashes.get("Time"),null,null,null,null},
				new String[]{null,null,null,null,null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Konzentration"," ","Einheit")));
		if (isSiLeBAT) addTable(Prozessdaten_Messwerte, -1);
		MyTable Prozessdaten_Kosten = new MyTable("Prozessdaten_Kosten",
				new String[]{"Prozessdaten","Kostenkatalog","Menge"},
				new String[]{"INTEGER","INTEGER","DOUBLE"},
				new String[]{null,null,null,},
				new MyTable[]{prozessdaten,Kostenkatalog,newDoubleTable},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Menge")));
		if (isSiLeBAT) addTable(Prozessdaten_Kosten, -1);

		MyTable prozessLinks = new MyTable("Prozess_Verbindungen",
				new String[]{"Ausgangsprozess","Zielprozess"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{prozessdaten,prozessdaten},
				new LinkedHashMap[]{null,null});
		MyTable Verpackungen = new MyTable("Verpackungsmaterial", new String[]{"Kode","Verpackung"},
				new String[]{"VARCHAR(10)","VARCHAR(100)"},
				new String[]{null,null},
				new MyTable[]{null,null},
				null,
				new LinkedHashMap[]{null,null},
				new String[]{null,null});
		if (isSiLeBAT) addTable(Verpackungen, -1);
		if (isSiLeBAT) addTable(prozessLinks, -1);
		h1 = new LinkedHashMap<>();
	    h1.put("Zutat", "Zutat");					
	    h1.put("Produkt", "Produkt");					
		MyTable zutatendaten = new MyTable("Zutatendaten",
				new String[]{"Prozessdaten","Zutat_Produkt","Units","Unitmenge","UnitEinheit","Vorprozess",
				"Matrix","EAN","MatrixDetail","Verpackung","Temperatur","pH","aw","CO2","Druck","Luftfeuchtigkeit","Sonstiges","Kosten"},
				new String[]{"INTEGER","VARCHAR(10)","DOUBLE","DOUBLE","INTEGER","INTEGER",
				"INTEGER","VARCHAR(255)","VARCHAR(255)","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","INTEGER","INTEGER"},
				new String[]{"Verweis auf Eintraege aus der Tabelle Zutatendaten (Bedeutung nur für interne Verarbeitung)","Auswahl ob es sich um eine Zutat oder ein Produkt","Groesse einer Charge","Mengengroesse pro Chargenelement","Einheit eines Chargenelements","Produkt des Vorprozesses",
				null,"EAN-Nummer aus SA2-Datenbank - falls bekannt","Details zur Matrix, die durch den Katalog nicht abgebildet werden",null,"Temperatur in Grad Celcius","pH-Wert","aw-Wert","CO2 [ppm]","Druck [bar]","Luftfeuchtigkeit - Einheit bitte in [%]","Sonstige Rahmenbedingungen in der Umgebung. Aber auch Facetten der Matrix.\nEs öffnet sich ein Fenster, in dem an die Combase angelehnte Parameter eingetragen werden können, vgl. Feld condition in der Combase:\nhttp://www.combase.cc/CB_TechnDescription.pdf",null},
				new MyTable[]{prozessdaten,null,newDoubleTable,newDoubleTable,null,null,
				matrix,null,null,Verpackungen,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,SonstigeParameter,Kostenkatalog}, // prozessLinks
				null,
				new LinkedHashMap[]{null,h1,null,null,h4,null,
				null,null,null,null,null,null,null,null,null,null,null,null},
				new String[]{null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,null,"Zutatendaten_Sonstiges","Zutatendaten_Kosten"},
				null,
				new LinkedList<>(Arrays.asList("Matrix","Vorprozess")),
				new String[]{null, null, null, null, null, "Vorprozess.Prozessdaten=Prozess_Verbindungen.Ausgangsprozess WHERE Prozess_Verbindungen.Zielprozess=Prozessdaten; AND " + DBKernel.delimitL("Zutat_Produkt") + "='Produkt'",
							null, null, null, null, null, null, null, null, null, null, null, null});
		if (isSiLeBAT) addTable(zutatendaten, -1);
		prozessdaten.setForeignField(zutatendaten, 10);
		zutatendaten.setForeignField(zutatendaten, 5);
		
		MyTable Zutatendaten_Sonstiges = new MyTable("Zutatendaten_Sonstiges",
				new String[]{"Zutatendaten","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{zutatendaten,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("SonstigeParameter",": ","Wert"," ","Einheit")));
		if (isSiLeBAT) addTable(Zutatendaten_Sonstiges, -1);
		MyTable Zutatendaten_Kosten = new MyTable("Zutatendaten_Kosten",
				new String[]{"Zutatendaten","Kostenkatalog","Menge"},
				new String[]{"INTEGER","INTEGER","DOUBLE"},
				new String[]{null,null,null,},
				new MyTable[]{zutatendaten,Kostenkatalog,newDoubleTable},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Menge")));
		if (isSiLeBAT) addTable(Zutatendaten_Kosten, -1);
		
		if (isPmm || isSiLeBAT) generateStatUpModellTables(literatur, tenazity_raw_data, allHashes.get("Time"), Konzentrationseinheiten, hYNB);

		if (isKrise) doLieferkettenTabellen(agenzien, matrix, h4);

	}
	@SuppressWarnings("unchecked")
	private void doLieferkettenTabellen(final MyTable agenzien, final MyTable matrix, final LinkedHashMap<Object, String> h4) {
		LinkedHashMap<Boolean, String> hYNB = new LinkedHashMap<>();
		if (DBKernel.getLanguage().equalsIgnoreCase("en")) {hYNB.put(new Boolean(true), "yes");	hYNB.put(new Boolean(false), "no");}
		else {hYNB.put(new Boolean(true), "ja");	hYNB.put(new Boolean(false), "nein");}
				
		MyTable Knoten = new MyTable("Station", new String[]{"Produktkatalog","Name","Strasse","Hausnummer","Postfach","PLZ","Ort","District","Bundesland","Land","Longitude","Latitude","Ansprechpartner","Telefon","Fax","EMail","Webseite","Betriebsnummer","Betriebsart","VATnumber","Code",
				"CasePriority","AnzahlFaelle","AlterMin","AlterMax","DatumBeginn","DatumHoehepunkt","DatumEnde","Erregernachweis","Serial","ImportSources"},
				new String[]{"INTEGER","VARCHAR(255)","VARCHAR(255)","VARCHAR(10)","VARCHAR(20)","VARCHAR(10)","VARCHAR(60)","VARCHAR(255)","VARCHAR(30)","VARCHAR(100)","DOUBLE","DOUBLE","VARCHAR(100)","VARCHAR(30)","VARCHAR(30)","VARCHAR(100)","VARCHAR(255)","VARCHAR(50)","VARCHAR(255)","VARCHAR(255)","VARCHAR(25)",
				"DOUBLE","INTEGER","INTEGER","INTEGER","DATE","DATE","DATE","INTEGER","VARCHAR(16383)","VARCHAR(16383)"},
				new String[]{null,null, null,null,null,null,null,null,null,null,null,null,"Ansprechpartner inkl. Vor und Zuname",null,null,null,null,null,
				"z.B. Endverbraucher, Erzeuger, Einzelhändler, Großhändler, Gastronomie, Mensch. Siehe weitere Beispiele ADV Katalog", null, "interner Code, z.B. NI00",
				"Falldefinition erfüllt (z.B. laut RKI) - Priorität: Wert zwischen 0 und 1",null,null,null,"Datum frühester Erkrankungsbeginn","Datum des Höhepunkt an Neuerkrankungen","Datum letzter Erkrankungsbeginn",null,null,null},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,agenzien,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,allHashes.get("County"),null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null},
				new String[]{"INT",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,"Station_Agenzien",null,null},
				null,
				new LinkedList<>(Arrays.asList("Name")));
		addTable(Knoten, Lieferketten_LIST);
		MyTable Agensnachweis = new MyTable("Station_Agenzien", new String[]{"Station","Erreger","Labornachweis","AnzahlLabornachweise"},
				new String[]{"INTEGER","INTEGER","BOOLEAN","INTEGER"},
				new String[]{null,null,"Labornachweise vorhanden?",null},
				new MyTable[]{Knoten,agenzien,null,null},
				null,
				new LinkedHashMap[]{null,null,hYNB,null},
				null,
				null,
				new LinkedList<>(Arrays.asList("AnzahlLabornachweise")));
		addTable(Agensnachweis, -1);
		LinkedHashMap<String, String> proce = new LinkedHashMap<>();
		proce.put("nicht erhitzt und verzehrsfertig (Salate, rohe Produkte)", DBKernel.getLanguage().equalsIgnoreCase("en") ? "not heated and ready-to-eat (e.g. salads)" : "nicht erhitzt und verzehrsfertig (Salate, rohe Produkte)");
		proce.put("erhitzt und verzehrsfertig (fast alles)", DBKernel.getLanguage().equalsIgnoreCase("en") ? "heated and ready-to-eat" : "erhitzt und verzehrsfertig (fast alles)");
		proce.put("erhitzt und nicht verzehrsfähig (Vorprodukte wie eingefrorene Kuchen)", DBKernel.getLanguage().equalsIgnoreCase("en") ? "heated and not-ready-to-eat (e.g. frozen cake)" : "erhitzt und nicht verzehrsfähig (Vorprodukte wie eingefrorene Kuchen)");
		proce.put("nicht erhitzt und nicht verzehrsfähig (Rohwaren, die nicht zum Rohverzehr bestimmt sind wie Fleisch oder Eier)", DBKernel.getLanguage().equalsIgnoreCase("en") ? "not heated and not-ready-to-eat (meat, eggs)" : "nicht erhitzt und nicht verzehrsfähig (Rohwaren, die nicht zum Rohverzehr bestimmt sind wie Fleisch oder Eier)");
		MyTable Produzent_Artikel = new MyTable("Produktkatalog", // Produzent_Artikel
				new String[]{"Station","Artikelnummer","Bezeichnung","Prozessierung","IntendedUse","Code","Matrices","Chargen","Serial","ImportSources"},
				new String[]{"INTEGER","VARCHAR(255)","VARCHAR(1023)","VARCHAR(255)","VARCHAR(255)","VARCHAR(25)","INTEGER","INTEGER","VARCHAR(16383)","VARCHAR(16383)"},
				new String[]{null,null,null,"gekocht? geschüttelt? gerührt?","wozu ist der Artikel gedacht? Was soll damit geschehen?","interner Code",null,null,null,null},
				new MyTable[]{Knoten,null,null,null,null,null,matrix,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,proce,null,null,null,null,null,null},
				new String[]{null,null,null,null,null,null,"Produktkatalog_Matrices","INT",null,null},
				null,
				new LinkedList<>(Arrays.asList("Artikelnummer",": ","Bezeichnung")));
		addTable(Produzent_Artikel, Lieferketten_LIST);
		Knoten.setForeignField(Produzent_Artikel, 0);
		MyTable Produktmatrices = new MyTable("Produktkatalog_Matrices", new String[]{"Produktkatalog","Matrix"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{Produzent_Artikel,matrix},
				null,
				new LinkedHashMap[]{null,null});
		addTable(Produktmatrices, -1);
		
		MyTable Chargen = new MyTable("Chargen",
				new String[]{"Artikel","Zutaten","ChargenNr","Menge","Einheit","Lieferungen","MHD_day","MHD_month","MHD_year","pd_day","pd_month","pd_year","Serial","OriginCountry","MicrobioSample","ImportSources"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","DOUBLE","VARCHAR(50)","INTEGER","INTEGER","INTEGER","INTEGER","INTEGER","INTEGER","INTEGER","VARCHAR(16383)","VARCHAR(255)","VARCHAR(255)","VARCHAR(16383)"},
				new String[]{null,null,null,null,null,null,"Best before - day","Best before - month","Best before - year","production date - day","production date - month","production date - year",null,null,null,null},
				new MyTable[]{Produzent_Artikel,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null},
				new String[]{null,"INT",null,null,null,"INT",null,null,null,null,null,null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("pd_day",".","pd_month",".","pd_year","; ","ChargenNr","; ","Artikel")));
		addTable(Chargen, Lieferketten_LIST);
		Produzent_Artikel.setForeignField(Chargen, 7);
		
		MyTable Lieferungen = new MyTable("Lieferungen", // Artikel_Lieferung
				new String[]{"Charge","dd_day","dd_month","dd_year","ad_day","ad_month","ad_year","numPU","typePU", // "Artikel","ChargenNr","MHD",
					"Unitmenge","UnitEinheit","Empfänger","Serial","EndChain","Explanation_EndChain","Contact_Questions_Remarks","Further_Traceback","ImportSources"}, // ,"Vorprodukt","Zielprodukt"
				new String[]{"INTEGER","INTEGER","INTEGER","INTEGER","INTEGER","INTEGER","INTEGER","DOUBLE","VARCHAR(255)",
					"DOUBLE","VARCHAR(50)","INTEGER","VARCHAR(16383)","VARCHAR(255)","VARCHAR(16383)","VARCHAR(16383)","VARCHAR(255)","VARCHAR(16383)"}, // ,"INTEGER","INTEGER"
				new String[]{null,"Delivery date - day","Delivery date - month","Delivery date - year","Arrival date - day","Arrival date - month","Arrival date - year","number of packing units","type of packing units","total amount","total amount unit",null,null,null,null,null,null,null}, // ,null,null
				new MyTable[]{Chargen,null,null,null,null,null,null,null,null,null,null,Knoten,null,null,null,null,null,null}, // ,null,null
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null}, // ,null,null
				new String[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("dd_day",".","dd_month",".","dd_year","; ","Unitmenge"," ","UnitEinheit","; ","Charge")));
		addTable(Lieferungen, Lieferketten_LIST);
		Chargen.setForeignField(Lieferungen, 5);
		
		MyTable ChargenVerbindungen = new MyTable("ChargenVerbindungen",
				new String[]{"Zutat","Produkt","MixtureRatio","ImportSources"},
				new String[]{"INTEGER","INTEGER","DOUBLE","VARCHAR(16383)"},
				new String[]{null,null,"Mixture Ratio (prozentualer Anteil von der Zutat im Zielprodukt,\nz.B. Zielprodukt = Sprout mixture, Zutat = alfalfa sprouts => z.B. 0.33 (33%))",null},
				new MyTable[]{Lieferungen,Chargen,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null},
				new String[]{null,null,null,null},
				null,
				new LinkedList<>(Arrays.asList("Zutat")),
				new String[]{"Zutat.Empfänger=Produkt.Artikel.Station", null, null,null});
		addTable(ChargenVerbindungen, DBKernel.debug ? Lieferketten_LIST : -1);
		Chargen.setForeignField(ChargenVerbindungen, 1);

		MyTable extraFields = new MyTable("ExtraFields",
				new String[]{"tablename","id","attribute","value"},
				new String[]{"VARCHAR(255)","INTEGER","VARCHAR(255)","VARCHAR(255)"},
				new String[]{null,null,null,null}, 
				new MyTable[]{null,null,null,null},
				new String[][]{{"tablename","id","attribute","value"}},
				new LinkedHashMap[]{null,null,null,null}, 
				new String[]{null,null,null,null});
		addTable(extraFields, Lieferketten_LIST);
		MyTable fclXlsSources = new MyTable("ImportMetadata",
				new String[]{"filename","reporter","date","remarks"},
				new String[]{"VARCHAR(2048)","VARCHAR(255)","VARCHAR(255)","VARCHAR(2048)"},
				new String[]{null,null,null,null}, 
				new MyTable[]{null,null,null,null},
				new String[][]{{"filename"}},
				new LinkedHashMap[]{null,null,null,null}, 
				new String[]{null,null,null,null});
		addTable(fclXlsSources, Lieferketten_LIST);
		//check4Updates_129_130(myList);

		//DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Bundesland") + " = 'NI' WHERE " + DBKernel.delimitL("ID") + " = 167", false);
	}
	private MyTable generateICD10Tabellen() {
		MyTable ICD10_Kapitel = new MyTable("ICD10_Kapitel", new String[]{"KapNr","KapTi"},
				new String[]{"VARCHAR(2)","VARCHAR(110)"},
				new String[]{"Kapitelnummer, 2 Zeichen","Kapiteltitel, bis zu 110 Zeichen"},
				new MyTable[]{null,null},
				new String[][]{{"KapNr"}},
				null,
				new String[]{null,null});
		addTable(ICD10_Kapitel, -1);		
		MyTable ICD10_Gruppen = new MyTable("ICD10_Gruppen", new String[]{"GrVon","GrBis","KapNr","GrTi"},
				new String[]{"VARCHAR(3)","VARCHAR(3)","INTEGER","VARCHAR(210)"},
				new String[]{"erster Dreisteller der Gruppe, 3 Zeichen","letzter Dreisteller der Gruppe, 3 Zeichen","Kapitelnummer, 2 Zeichen","Gruppentitel, bis zu 210 Zeichen"},
				new MyTable[]{null,null,ICD10_Kapitel,null},
				new String[][]{{"GrVon"}},
				null,
				new String[]{null,null,null,null});
		addTable(ICD10_Gruppen, -1);		
		MyTable ICD10_MorbL = new MyTable("ICD10_MorbL", new String[]{"MorbLCode","MorbLTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schluesselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MorbLCode"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MorbL, -1);		
		MyTable ICD10_MortL1Grp = new MyTable("ICD10_MortL1Grp", new String[]{"MortL1GrpCode","MortL1GrpTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Gruppenschluesselnummer","Gruppentitel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL1GrpCode"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MortL1Grp, -1);		
		MyTable ICD10_MortL1 = new MyTable("ICD10_MortL1", new String[]{"MortL1Code","MortL1GrpCode","MortL1Ti"},
				new String[]{"VARCHAR(5)","INTEGER","VARCHAR(255)"},
				new String[]{"Schluesselnummer","Gruppenschluesselnummer","Titel"},
				new MyTable[]{null,ICD10_MortL1Grp,null},
				new String[][]{{"MortL1Code"}},
				null,
				new String[]{null,null,null});
		addTable(ICD10_MortL1, -1);		
		MyTable ICD10_MortL2 = new MyTable("ICD10_MortL2", new String[]{"MortL2Code","MortL2Ti"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schluesselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL2Code"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MortL2, -1);		
		MyTable ICD10_MortL3Grp = new MyTable("ICD10_MortL3Grp", new String[]{"MortL3GrpCode","MortL3GrpTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Gruppenschluesselnummer","Gruppentitel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL3GrpCode"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MortL3Grp, -1);		
		MyTable ICD10_MortL3 = new MyTable("ICD10_MortL3", new String[]{"MortL3Code","MortL3GrpCode","MortL3Ti"},
				new String[]{"VARCHAR(5)","INTEGER","VARCHAR(255)"},
				new String[]{"Schluesselnummer","Gruppenschluesselnummer","Titel"},
				new MyTable[]{null,ICD10_MortL3Grp,null},
				new String[][]{{"MortL3Code"}},
				null,
				new String[]{null,null,null});
		addTable(ICD10_MortL3, -1);		
		MyTable ICD10_MortL4 = new MyTable("ICD10_MortL4", new String[]{"MortL4Code","MortL4Ti"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schluesselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL4Code"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MortL4, -1);		
		MyTable ICD10_Kodes = new MyTable("ICD10_Kodes", new String[]{"Ebene","Ort","Art",
				"KapNr","GrVon","Code","NormCode","CodeOhnePunkt",
				"Titel","P295","P301",
				"MortL1Code","MortL2Code","MortL3Code","MortL4Code","MorbLCode",
				"SexCode","SexFehlerTyp",
				"AltUnt",
				"AltUntNeu",
				"AltOb","AltObNeu",
				"AltFehlerTyp","Exot","Belegt",
				"IfSGMeldung","IfSGLabor"},
				new String[]{"VARCHAR(1)","VARCHAR(1)","VARCHAR(1)","INTEGER","INTEGER","VARCHAR(7)","VARCHAR(6)","VARCHAR(5)",
				"VARCHAR(255)","VARCHAR(1)","VARCHAR(1)","INTEGER","INTEGER","INTEGER","INTEGER","INTEGER","VARCHAR(1)","VARCHAR(1)","VARCHAR(3)",
				"VARCHAR(4)","VARCHAR(3)","VARCHAR(4)","VARCHAR(1)","VARCHAR(1)","VARCHAR(1)","VARCHAR(1)","VARCHAR(1)"},
				new String[]{"Klassifikationsebene, 1 Zeichen: 3 = Dreisteller; 4 = Viersteller; 5 = Fünfsteller","Ort der Schluesselnummer im Klassifikationsbaum, 1 Zeichen: T = terminale Schluesselnummer (kodierbarer Endpunkt); N = nichtterminale Schluesselnummer (kein kodierbarer Endpunkt)","Art der Vier- und Fünfsteller: X = explizit aufgeführt (praekombiniert); S = per Subklassifikation (postkombiniert)",
				"Kapitelnummer","erster Dreisteller der Gruppe","Schluesselnummer ohne eventuelles Kreuz, bis zu 7 Zeichen","Schluesselnummer ohne Strich, Stern und  Ausrufezeichen, bis zu 6 Zeichen","Schluesselnummer ohne Punkt, Strich, Stern und Ausrufezeichen, bis zu 5 Zeichen",
				"Klassentitel, bis zu 255 Zeichen","Verwendung der Schluesselnummer nach Paragraph 295: P = zur Primaerverschluesselung zugelassene Schluesselnummer; O = nur als Sternschluesselnummer zugelassen; Z = nur als Ausrufezeichenschluesselnummer zugelassen; V = nicht zur Verschluesselung zugelassen","Verwendung der Schluesselnummer nach Paragraph 301: P = zur Primaerverschluesselung zugelassen; O = nur als Sternschluesselnummer zugelassen; Z = nur als Ausrufezeichenschluesselnummer zugelassen; V = nicht zur Verschluesselung zugelassen",
				"Bezug zur Mortalitaetsliste 1","Bezug zur Mortalitaetsliste 2","Bezug zur Mortalitaetsliste 3","Bezug zur Mortalitaetsliste 4","Bezug zur Morbiditaetsliste",
				"Geschlechtsbezug der Schluesselnummer: 9 = kein Geschlechtsbezug; M = maennlich; W = weiblich", "Art des Fehlers bei Geschlechtsbezug: 9 = irrelevant; M = Muss-Fehler; K = Kann-Fehler",
				"untere Altersgrenze für eine Schluesselnummer: 999     = irrelevant; 000     = unter 1 vollendeten Tag; 001-006 = 1 Tag bis unter 7 Tage; 011-013 = 7 Tage bis unter 28 Tage; also 011 =  7-13 Tage (1 Woche bis unter 2 Wochen); 012 = 14-20 Tage (2 Wochen bis unter 3 Wochen); 013 = 21-27 Tage (3 Wochen bis unter einem Monat); 101-111 = 28 Tage bis unter 1 Jahr; also 101 = 28 Tage bis Ende des 2. Lebensmonats; 102 = Anfang bis Ende des 3. Lebensmonats; 103 = Anfang bis Ende des 4. Lebensmonats; usw. bis; 111 = Anfang des 12. Lebensmonats bis unter 1 Jahr; 201-299 = 1 Jahr bis unter 100 Jahre; 300-324 = 100 Jahre bis unter 125 Jahre",
				"untere Altersgrenze für eine Schluesselnummer, alternatives Format: 9999    = irrelevant; t000 - t365 = 0 Tage bis unter 1 Jahr; j001 - j124 = 1 Jahr bis unter 124 Jahre",
				"obere Altersgrenze für eine Schluesselnummer, wie bei Feld 'AltUnt'","obere Altersgrenze für eine Schluesselnummer,alternatives Format wie bei Feld 'AltUntNeu'",
				"Art des Fehlers bei Altersbezug: 9 = irrelevant; M = Muss-Fehler; K = Kann-Fehler","Krankheit in Mitteleuropa sehr selten? J = Ja; N = Nein","Schluesselnummer mit Inhalt belegt? J = Ja; N = Nein (--> Kann-Fehler auslösen!)",
				"IfSG-Meldung, kennzeichnet, dass bei Diagnosen,die mit dieser Schluesselnummer kodiert sind, besonders auf die Arzt-Meldepflicht nach dem Infektionsschutzgesetz IfSG) hinzuweisen ist: J = Ja; N = Nein","IfSG-Labor, kennzeichnet, dass bei Laboruntersuchungen zu diesen Diagnosen die Laborausschlussziffer des EBM (32006) gewaehlt werden kann: J = Ja; N = Nein"},
				new MyTable[]{null,null,null,ICD10_Kapitel,ICD10_Gruppen,null,null,null,null,null,null,ICD10_MortL1,ICD10_MortL2,ICD10_MortL3,ICD10_MortL4,ICD10_MorbL,
				null,null,null,null,null,null,null,null,null,null,null},
				new String[][]{{"Code"},{"NormCode"},{"CodeOhnePunkt"}},
				null,
				null,
				null,
				new LinkedList<>(Arrays.asList("Titel")));
		addTable(ICD10_Kodes, Krankheitsbilder_LIST);
		return ICD10_Kodes;
	}	
	@SuppressWarnings("unchecked")
	private void generateStatUpModellTables(final MyTable literatur, final MyTable tenazity_raw_data, final LinkedHashMap<Object, String> hashZeit, final MyTable Konzentrationseinheiten, LinkedHashMap<Boolean, String> hYNB) {
		MyTable PMMLabWorkflows = new MyTable("PMMLabWorkflows", new String[]{"Workflow"},
				new String[]{"BLOB(100M)"},
				new String[]{null},
				new MyTable[]{null},
				null,
				new LinkedHashMap[]{null},
				null,
				new String[]{"*.zip"});
		addTable(PMMLabWorkflows, -1);	
		MyTable DataSource = new MyTable("DataSource", new String[]{"Table","TableID","SourceDBUUID","SourceID"},
				new String[]{"VARCHAR(255)","INTEGER","VARCHAR(255)","INTEGER"},
				new String[]{null,null,null,null},
				new MyTable[]{null,null,null,null},
				new String[][]{{"Table","TableID","SourceDBUUID","SourceID"}},
				new LinkedHashMap[]{null,null,null,null},
				null);
		addTable(DataSource, -1);	

		LinkedHashMap<Object, String> hashLevel = new LinkedHashMap<>();
		hashLevel.put(1, "primary");					
		hashLevel.put(2, "secondary");	

		LinkedHashMap<Object, String> hashTyp = new LinkedHashMap<>();
		hashTyp.put(1, "Kovariable");			// independent ?		
		hashTyp.put(2, "Parameter");	
		hashTyp.put(3, "Response");	// dependent ?	
		hashTyp.put(4, "StartParameter");	
		MyTable Parametertyp = new MyTable("Parametertyp", new String[]{"Parametertyp"},
				new String[]{"INTEGER"},
				new String[]{null},
				new MyTable[]{null},
				null,
				new LinkedHashMap[]{hashTyp},
				new String[]{null});
		addTable(Parametertyp, -1);
		MyTable Modellkatalog = new MyTable("Modellkatalog", new String[]{"Name","Notation","Level","Klasse","Typ","Eingabedatum",
				"eingegeben_von","Beschreibung","Formel","Ableitung","Software",
				"Parameter","Referenzen","visible"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","INTEGER","INTEGER","VARCHAR(255)","DATE",
				"VARCHAR(255)","VARCHAR(1023)","VARCHAR(1023)","INTEGER","VARCHAR(255)",
				"INTEGER","INTEGER","BOOLEAN"},
				new String[]{null,null,"1: primary, 2:secondary","1:growth, 2:inactivation, 3:survival,\n4:growth/inactivation, 5:inactivation/survival, 6: growth/survival,\n7:growth/inactivation/survival\n8: T, 9: pH, 10:aw, 11:T/pH, 12:T/aw, 13:pH/aw, 14:T/pH/aw",null,null,"Ersteller des Datensatzes","Beschreibung des Modells","zugrundeliegende Formel für das Modell","Ableitung","schreibt den Schaetzknoten vor",
				"Parameterdefinitionen, die dem Modell zugrunde liegen: abhaengige Variable, unabhaengige Variable, Parameter","Referenzen, die dem Modell zugrunde liegen",null},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,
				null,literatur,null},
				null,
				new LinkedHashMap[]{null,null,hashLevel,allHashes.get("ModelType"),null,null,null,null,null,null,null,
				null,null,hYNB},
				new String[] {null,null,null,null,null,null,null,null,null,null,null,
						"INT","Modell_Referenz",null},
				//new String[] {"not null","not null",null,null,null,"not null",
				new String[] {null,null,null,null,null,null,null,
				null,null,null,null,
				null,null,null});
		addTable(Modellkatalog, PMModelle_LIST);		
		MyTable ModellkatalogParameter = new MyTable("ModellkatalogParameter", new String[]{"Modell","Parametername","Parametertyp",
				"ganzzahl","min","max","optimalValue","Einheit","Beschreibung"},
				new String[]{"INTEGER","VARCHAR(127)","INTEGER",
				"BOOLEAN","DOUBLE","DOUBLE","DOUBLE","INTEGER","VARCHAR(1023)"},
				new String[]{null,null,"1: Kovariable, 2: Parameter, 3: Response, 4: StartParameter",
				"TRUE, falls der Parameter ganzzahlig sein muss",null,null,null,null,null},
				new MyTable[]{Modellkatalog,null,null,null,null,null,null,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,hashTyp,null,null,null,null,null,null},
				null,
				//new String[] {"not null","not null","default 1","default FALSE","default null","default null",null});
				new String[] {"not null",null,"default 1","default FALSE","default null","default null",null,null,null},
				new LinkedList<>(Arrays.asList("Parametername"," (","Parametertyp", ")")));
		addTable(ModellkatalogParameter, DBKernel.isKNIME ? PMModelle_LIST : -1);	
		Modellkatalog.setForeignField(ModellkatalogParameter, 11);
		MyTable Modell_Referenz = new MyTable("Modell_Referenz", new String[]{"Modell","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{Modellkatalog,literatur},
				null,
				new LinkedHashMap[]{null,null},
				null,
				new String[] {"not null","not null"});
		addTable(Modell_Referenz, DBKernel.isKNIME ? PMModelle_LIST : -1);		
		
		MyTable GeschaetzteModelle = new MyTable("GeschaetzteModelle", new String[]{"Name","Versuchsbedingung","Modell",
				"Response","manuellEingetragen","Rsquared","RSS","RMS","AIC","BIC","Score",
				"Referenzen","GeschaetzteParameter","GeschaetzteParameterCovCor","GueltigkeitsBereiche","PMML","PMMLabWF","FreigabeModus"},
				new String[]{"VARCHAR(255)","INTEGER","INTEGER","INTEGER","BOOLEAN","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","INTEGER",
				"INTEGER","INTEGER","INTEGER","INTEGER","BLOB(10M)","INTEGER","INTEGER"},
				new String[]{null,null,null,"Response, verweist auf die Tabelle ModellkatalogParameter","wurde das Modell manuell eingetragen oder ist es eine eigene Schaetzung basierend auf den internen Algorithmen und den in den Messwerten hinterlegten Rohdaten","r^2 oder Bestimmtheitsmass der Schaetzung","Variation der Residuen",null,null,null,"subjektiver Score zur Bewertung der Schaetzung",
				"Referenzen, aus denen diese Modellschaetzung entnommen wurde","Verweis auf die Tabelle ModellkatalogParameter mit den geschaetzten Parametern","Verweis auf die Tabelle ModellkatalogParameterCovCor mit den Korrelationen der geschaetzten Parameter","Gültigkeitsbereiche für Sekundaermodelle",null,null,null},
				new MyTable[]{null,tenazity_raw_data,Modellkatalog,
				ModellkatalogParameter,null,null,null,null,null,null,null,
				literatur,ModellkatalogParameter,null,ModellkatalogParameter,null,PMMLabWorkflows,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,allHashes.get("Freigabe")},
				new String[] {null,null,null,
				null,null,null,null,null,null,null,null,
				"GeschaetztesModell_Referenz","GeschaetzteParameter","INT","GueltigkeitsBereiche",null,null,null},
				new String[] {null,null,null,null,"default FALSE",null,null,null,null,null,null,
				null,null,null,null,null,null,null},
				new LinkedList<>(Arrays.asList("Name","Versuchsbedingung","Modell")));
				//new String[] {null,"not null",null,"default FALSE",null,null,null,
				//null,null,null});
		addTable(GeschaetzteModelle, PMModelle_LIST);		
		MyTable GeschaetztesModell_Referenz = new MyTable("GeschaetztesModell_Referenz", new String[]{"GeschaetztesModell","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{GeschaetzteModelle,literatur},
				null,
				new LinkedHashMap[]{null,null},
				null,
				new String[] {"not null","not null"});
		addTable(GeschaetztesModell_Referenz, DBKernel.isKNIME ? PMModelle_LIST : -1);	
		MyTable GeschaetzteParameter = new MyTable("GeschaetzteParameter", new String[]{"GeschaetztesModell","Parameter",
				"Wert","ZeitEinheit","Einheit","KI.unten","KI.oben","SD","StandardError","t","p"},
				new String[]{"INTEGER","INTEGER",
				"DOUBLE","INTEGER","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE"},
				new String[]{null,null,null,null,null,null,null,null,null,null,null},
				new MyTable[]{GeschaetzteModelle,ModellkatalogParameter,null,Konzentrationseinheiten,Konzentrationseinheiten,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,null},
				null,
				new String[] {"not null","not null",null,null,null,null,null,null,null,null,null},
				new LinkedList<>(Arrays.asList("Parameter",": ","Wert")));
		addTable(GeschaetzteParameter, DBKernel.isKNIME ? PMModelle_LIST : -1);	
		MyTable GueltigkeitsBereiche = new MyTable("GueltigkeitsBereiche", new String[]{"GeschaetztesModell","Parameter",
				"Gueltig_von","Gueltig_bis","Gueltig_optimal"},
				new String[]{"INTEGER","INTEGER",
				"DOUBLE","DOUBLE","DOUBLE"},
				new String[]{null,null,null,null,null},
				new MyTable[]{GeschaetzteModelle,ModellkatalogParameter,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				null,
				new String[] {"not null","not null",null,null,null},
				new LinkedList<>(Arrays.asList("Parameter"," ","[","Gueltig_von"," ","Gueltig_bis","]")));
		addTable(GueltigkeitsBereiche, DBKernel.isKNIME ? PMModelle_LIST : -1);
		MyTable VarParMaps = new MyTable("VarParMaps", new String[]{"GeschaetztesModell","VarPar","VarParMap"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{GeschaetzteModelle,ModellkatalogParameter,null},
				null,
				new LinkedHashMap[]{null,null,null},
				null,
				new String[] {null,null,null});
		addTable(VarParMaps, -1);	
		MyTable GeschaetzteParameterCovCor = new MyTable("GeschaetzteParameterCovCor", new String[]{"param1","param2",
				"GeschaetztesModell","cor","Wert"},
				new String[]{"INTEGER","INTEGER","INTEGER","BOOLEAN","DOUBLE"},
				new String[]{null,null,null,null,null},
				new MyTable[]{GeschaetzteParameter,GeschaetzteParameter,GeschaetzteModelle,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				null,
				null);//new String[] {"not null","not null","not null","not null",null});
		addTable(GeschaetzteParameterCovCor, DBKernel.isKNIME ? PMModelle_LIST : -1);		
		GeschaetzteModelle.setForeignField(GeschaetzteParameterCovCor, 13);
		MyTable GlobalModels = new MyTable("GlobalModels",
				new String[]{"Modellname"},
				new String[]{"VARCHAR(255)"},
				new String[]{null},
				new MyTable[]{null},
				null,
				new LinkedHashMap[]{null},
				null,
				null);
		addTable(GlobalModels, DBKernel.isKNIME ? PMModelle_LIST : -1);		
		MyTable Sekundaermodelle_Primaermodelle = new MyTable("Sekundaermodelle_Primaermodelle",
				new String[]{"GeschaetztesPrimaermodell","GeschaetztesSekundaermodell","GlobalModel"},
				new String[]{"INTEGER","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{GeschaetzteModelle,GeschaetzteModelle,GlobalModels},
				null,
				new LinkedHashMap[]{null,null,null},
				null,
				new String[] {"not null","not null",null});
		addTable(Sekundaermodelle_Primaermodelle, DBKernel.isKNIME ? PMModelle_LIST : -1);		
	}
  
	private void addTable(MyTable myT, int child) {
		myT.setChild(child);
		allTables.put(myT.getTablename(), myT);
	}

	private void loadHashes() {		
		LinkedHashMap<Object, String> hashZeit = new LinkedHashMap<>();
		hashZeit.put("Sekunde", DBKernel.getLanguage().equals("en") ? "Second(s)" : "Sekunde(n) [s][sec]");					
		hashZeit.put("Minute", DBKernel.getLanguage().equals("en") ? "Minute(s)" : "Minute(n)");					
		hashZeit.put("Stunde", DBKernel.getLanguage().equals("en") ? "Hour(s)" : "Stunde(n)");		
		hashZeit.put("Tag", DBKernel.getLanguage().equals("en") ? "Day(s)" : "Tag(e)");		
		hashZeit.put("Woche", DBKernel.getLanguage().equals("en") ? "Week(s)" : "Woche(n)");		
		hashZeit.put("Monat", DBKernel.getLanguage().equals("en") ? "Month(s)" : "Monat(e)");		
		hashZeit.put("Jahr", DBKernel.getLanguage().equals("en") ? "Year(s)" : "Jahr(e)");			  
		allHashes.put("Time", hashZeit);

		LinkedHashMap<Object, String> hashGeld = new LinkedHashMap<>();
		hashGeld.put("Dollar", "Dollar ($)");					
		hashGeld.put("Euro", "Euro (€)");					
		allHashes.put("Currency", hashGeld);

		LinkedHashMap<Object, String> hashGewicht = new LinkedHashMap<>();
		hashGewicht.put("Milligramm", DBKernel.getLanguage().equals("en") ? "Milligrams (mg)" : "Milligramm (mg)");					
		hashGewicht.put("Gramm", DBKernel.getLanguage().equals("en") ? "Grams (g)" : "Gramm (g)");					
		hashGewicht.put("Kilogramm", DBKernel.getLanguage().equals("en") ? "Kilograms (kg)" : "Kilogramm (kg)");					
		hashGewicht.put("Tonne", DBKernel.getLanguage().equals("en") ? "Tons (t)" : "Tonne (t)");					
		allHashes.put("Weight", hashGewicht);

		LinkedHashMap<Object, String> hashSpeed = new LinkedHashMap<>();
		hashSpeed.put("pro Stunde", DBKernel.getLanguage().equals("en") ? "per hour (1/h)" : "pro Stunde (1/h)");					
		hashSpeed.put("pro Tag", DBKernel.getLanguage().equals("en") ? "per day (1/d)" : "pro Tag (1/d)");							
		allHashes.put("Speed", hashSpeed);

		LinkedHashMap<Object, String> hashDosis = new LinkedHashMap<>();
		hashDosis.put("Sporenzahl", "Sporenzahl");					
		hashDosis.put("KBE pro g", "KBE (cfu) pro Gramm (KBE/g)");					
		hashDosis.put("KBE pro ml", "KBE (cfu) pro Milliliter (KBE/ml)");					
		hashDosis.put("PBE pro g", "PBE (pfu) pro Gramm (PBE/g)");					
		hashDosis.put("PBE pro ml", "PBE (pfu) pro Milliliter (PBE/ml)");					
		hashDosis.put("Milligramm", "Milligramm (mg)");							
		hashDosis.put("Mikrogramm", "Mikrogramm (\u00B5g)");							
		hashDosis.put("\u00B5g/kg/KG", "\u00B5g/kg/KG");							
		hashDosis.put("Anzahl", "Anzahl (Viren, Bakterien, Parasiten, Organismen, ...)");	
		allHashes.put("Dosis", hashDosis);

		LinkedHashMap<Object, String> hashFreigabe = new LinkedHashMap<>();
		hashFreigabe.put(0, DBKernel.getLanguage().equals("en") ? "never" : "gar nicht");					
		hashFreigabe.put(1, DBKernel.getLanguage().equals("en") ? "crisis" : "Krise");					
		hashFreigabe.put(2, DBKernel.getLanguage().equals("en") ? "always" : "immer");					
		allHashes.put("Freigabe", hashFreigabe);
		
		LinkedHashMap<Object, String> hashBundesland = new LinkedHashMap<>();
		hashBundesland.put("Baden-Württemberg", "Baden-Württemberg");
		hashBundesland.put("Bayern", "Bayern");
		hashBundesland.put("Berlin", "Berlin");
		hashBundesland.put("Brandenburg", "Brandenburg");
		hashBundesland.put("Bremen", "Bremen");
		hashBundesland.put("Hamburg", "Hamburg");
		hashBundesland.put("Hessen", "Hessen");
		hashBundesland.put("Mecklenburg-Vorpommern", "Mecklenburg-Vorpommern");
		hashBundesland.put("Niedersachsen", "Niedersachsen");
		hashBundesland.put("Nordrhein-Westfalen", "Nordrhein-Westfalen");
		hashBundesland.put("Rheinland-Pfalz", "Rheinland-Pfalz");
		hashBundesland.put("Saarland", "Saarland");
		hashBundesland.put("Sachsen", "Sachsen");
		hashBundesland.put("Sachsen-Anhalt", "Sachsen-Anhalt");
		hashBundesland.put("Schleswig-Holstein", "Schleswig-Holstein");
		hashBundesland.put("Thüringen", "Thüringen");
		allHashes.put("County", hashBundesland);

		LinkedHashMap<Object, String> hashModelType = new LinkedHashMap<>();
		hashModelType.put(0, "unknown");					
		hashModelType.put(1, "growth");					
		hashModelType.put(2, "inactivation");	
		hashModelType.put(3, "survival");					
		hashModelType.put(4, "growth/inactivation");	
		hashModelType.put(5, "inactivation/survival");					
		hashModelType.put(6, "growth/survival");	
		hashModelType.put(7, "growth/inactivation/survival");					
		hashModelType.put(8, "T");	
		hashModelType.put(9, "pH");	
		hashModelType.put(10, "aw");	
		hashModelType.put(11, "T/pH");	
		hashModelType.put(12, "T/aw");	
		hashModelType.put(13, "pH/aw");	
		hashModelType.put(14, "T/pH/aw");	
		allHashes.put("ModelType", hashModelType);
	}
	private void loadOther4Gui() {
		// knownCodeSysteme
		knownCodeSysteme = new LinkedHashMap<>();
	  	// TOP
	  	knownCodeSysteme.put("Agenzien_TOP", new int[]{2,4}); // 
	  	knownCodeSysteme.put("Matrices_TOP", new int[]{2,4}); // 
	  	knownCodeSysteme.put("Methoden_TOP", new int[]{2,4}); // 

	  	knownCodeSysteme.put("Matrices_GS1", new int[]{2,3}); // 0001
	  	knownCodeSysteme.put("Matrices_ADV_01", new int[]{2,3,5,7}); // 01-011123
	  	knownCodeSysteme.put("Matrices_ADV_14", new int[]{2,3,5,7}); // 14-011123
	  	knownCodeSysteme.put("Matrices_ADV_15", new int[]{2,3,5,7});   // 15-011123 2,3,6
	  	knownCodeSysteme.put("Matrices_ADV_20", new int[]{2,3,5,7}); // 20-011123
	  	knownCodeSysteme.put("Matrices_BLS", new int[]{1,3,4,5,6}); // A011123
	  	knownCodeSysteme.put("Matrices_FA", new int[]{2,4,6,8,10,12,14,16,18}); // 
	  	knownCodeSysteme.put("Agenzien_ADV", new int[]{2,4}); // 0102123
	  	knownCodeSysteme.put("Matrices_SiLeBAT", new int[]{2,4,6,8,10});
	  	knownCodeSysteme.put("Agenzien_SiLeBAT", new int[]{2,4,6,8,10});
	  	knownCodeSysteme.put("Methodiken_SiLeBAT", new int[]{2,4,6,8,10});
	  	knownCodeSysteme.put("Matrices_Extra", new int[]{2,4,6,8,10});
	  	knownCodeSysteme.put("Agenzien_Extra", new int[]{2,4,6,8,10});
	  	knownCodeSysteme.put("Methodiken_Extra", new int[]{2,4,6,8,10});
	  	// Agenzien_VET
	  	knownCodeSysteme.put("Methoden_BVL", new int[]{2,3,5,6,8,9}); // 

	  	knownCodeSysteme.put("Methodiken_BfR", new int[]{2,4,6}); // 
	  	
	  	knownCodeSysteme.put("Matrices_Combase", new int[]{2,4,6}); // 
	  	knownCodeSysteme.put("Agenzien_Combase", new int[]{2,4,6}); // 		
	  	
	  	knownCodeSysteme.put("Agenzien_PMF", null);
	  	knownCodeSysteme.put("Matrices_PMF", null);
	  	
	  	// treeStructure
		treeStructure = new LinkedHashMap<>();

	    boolean isAdmin  = DBKernel.myDBi == null ? true : DBKernel.myDBi.isAdmin();
		if (isAdmin) treeStructure.put(SystemTabellen_LIST, "System-Tabellen");
		if (!DBKernel.getUsername().equals("burchardi")) treeStructure.put(BasisTabellen_LIST, "Basis-Tabellen");
		if (!DBKernel.getUsername().equals("burchardi")) treeStructure.put(Tenazitaet_LIST, "Tenazitaet");
		if (!DBKernel.getUsername().equals("burchardi")) treeStructure.put(PMModelle_LIST, "PMModelle");
		if (!DBKernel.isKNIME) treeStructure.put(Krankheitsbilder_LIST, "Krankheitsbilder");
		if (!DBKernel.isKNIME) treeStructure.put(Prozessdaten_LIST, "Prozessdaten");
		if (!DBKernel.isKNIME) treeStructure.put(Nachweissysteme_LIST, "Nachweissysteme");
		if (DBKernel.isKrise) treeStructure.put(Lieferketten_LIST, "Lieferketten");	  	
	}
	private void loadOther4Db() {
		if (isPmm) {
			allViews = new LinkedHashSet<>();
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/02_create_doublekennzahleneinfach.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/04_create_versuchsbedingungeneinfach_156.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/001_SonstigesEinfach_160.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/03_create_messwerteeinfach_164.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/001_LitEmView.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/001_LitMView.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/001_ParamVarView_175.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/001_IndepVarView_170.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/001_DepVarView_170.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/001_VarParMapView.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/002_EstModelPrimView_179.sql");
			allViews.add("/de/bund/bfr/knime/openkrise/db/res/002_EstModelSecView_179.sql");
			
			allData = new LinkedHashMap<>();
			allData.put("/de/bund/bfr/knime/openkrise/db/res/CombaseRawDataImport.sql", null);
			allData.put("/de/bund/bfr/knime/openkrise/db/res/PmmInitData.sql", "\r\n");
		}
	}
	public void addViews() {
		if (allViews != null) {
			for (String s : allViews) {
				new SQLScriptImporter().doImport(s, null, false);
			}
		}
	}
	public void addData() {
		if (allData != null) {
			for (String s : allData.keySet()) {
				String delimiter = allData.get(s);
				if (delimiter == null) new SQLScriptImporter().doImport(s, null, false);
				else new SQLScriptImporter(delimiter).doImport(s, null, false);
			}
		}
	}
}
