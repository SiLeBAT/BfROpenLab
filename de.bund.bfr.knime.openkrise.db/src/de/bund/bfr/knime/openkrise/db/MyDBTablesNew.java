/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
	private String softwareVersion = "1.8.7";
	
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
		loadOther4Gui();
	}

	@Override
	public void updateCheck(final String toVersion) {
		String fromVersion = getVersion4DB();
		if (toVersion.equals(fromVersion)) return;
		if (fromVersion.equals("1.8.2")) {
			fromVersion = "1.8.2.0";
			setVersion2DB(fromVersion);
			if (toVersion.equals(fromVersion)) return;
		}
		if (fromVersion.equals("1.8.2.0")) {
			fromVersion = "1.8.2.0.0";
			setVersion2DB(fromVersion);
			if (toVersion.equals(fromVersion)) return;
		}
		if (fromVersion.equals("1.8.2.0.0")) {
			sendRequest("ALTER TABLE " + delimitL("Lieferungen") + " ADD COLUMN " + delimitL("ad_day") + " INTEGER BEFORE " + delimitL("numPU"), false, false);
			sendRequest("ALTER TABLE " + delimitL("Lieferungen") + " ADD COLUMN " + delimitL("ad_month") + " INTEGER BEFORE " + delimitL("numPU"), false, false);
			sendRequest("ALTER TABLE " + delimitL("Lieferungen") + " ADD COLUMN " + delimitL("ad_year") + " INTEGER BEFORE " + delimitL("numPU"), false, false);
			sendRequest("ALTER TABLE " + delimitL("Lieferungen") + " ADD COLUMN " + delimitL("ImportSources") + " VARCHAR(16383) BEFORE " + delimitL("Kommentar"), false, false);
			sendRequest("ALTER TABLE " + delimitL("ChargenVerbindungen") + " ADD COLUMN " + delimitL("ImportSources") + " VARCHAR(16383) BEFORE " + delimitL("Kommentar"), false, false);
			sendRequest("ALTER TABLE " + delimitL("Chargen") + " ADD COLUMN " + delimitL("ImportSources") + " VARCHAR(16383) BEFORE " + delimitL("Kommentar"), false, false);
			sendRequest("ALTER TABLE " + delimitL("Produktkatalog") + " ADD COLUMN " + delimitL("ImportSources") + " VARCHAR(16383) BEFORE " + delimitL("Kommentar"), false, false);
			sendRequest("ALTER TABLE " + delimitL("Station") + " ADD COLUMN " + delimitL("ImportSources") + " VARCHAR(16383) BEFORE " + delimitL("Kommentar"), false, false);
			getTable("ExtraFields").createTable(getConn());
			getTable("ImportMetadata").createTable(getConn());
			fromVersion = "1.8.3";
			setVersion2DB(fromVersion);
			if (toVersion.equals(fromVersion)) return;
		}
		if (fromVersion.equals("1.8.3")) {
			recreateTriggers();
			fromVersion = "1.8.4";
			setVersion2DB(fromVersion);
			if (toVersion.equals(fromVersion)) return;
		}
		if (fromVersion.equals("1.8.4")) {
			getTable("LookUps").createTable(getConn());
			fromVersion = "1.8.5";
			setVersion2DB(fromVersion);
			if (toVersion.equals(fromVersion)) return;
		}
		if (fromVersion.equals("1.8.5")) {
			sendRequest("ALTER TABLE " + MyDBI.delimitL("Station") + " ADD COLUMN " + MyDBI.delimitL("Adresse") + " VARCHAR(32768)", false, true);
			sendRequest("ALTER TABLE " + MyDBI.delimitL("ExtraFields") + " ALTER COLUMN " + MyDBI.delimitL("value") + " VARCHAR(32768)", false, true);
			fromVersion = "1.8.6";
			setVersion2DB(fromVersion);
			if (toVersion.equals(fromVersion)) return;
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
			sendRequest("DROP TRIGGER " + delimitL("B_" + tableName + "_U"), true, false);
			sendRequest("DROP TRIGGER " + delimitL("B_" + tableName + "_D"), true, false);
			sendRequest("DROP TRIGGER " + delimitL("B_" + tableName + "_I"), true, false);
			sendRequest("DROP TRIGGER " + delimitL("A_" + tableName + "_U"), true, false);
			sendRequest("DROP TRIGGER " + delimitL("A_" + tableName + "_D"), true, false);
			sendRequest("DROP TRIGGER " + delimitL("A_" + tableName + "_I"), true, false);
			if (!tableName.equals("ChangeLog") && !tableName.equals("DateiSpeicher") && !tableName.equals("Infotabelle")) {
				sendRequest("CREATE TRIGGER " + delimitL("A_" + tableName + "_D") + " AFTER DELETE ON " +
						delimitL(tableName) + " FOR EACH ROW " + " CALL " + delimitL(new MyTrigger().getClass().getName()), false, false); // (oneThread ? "QUEUE 0" : "") +    
				sendRequest("CREATE TRIGGER " + delimitL("A_" + tableName + "_I") + " AFTER INSERT ON " +
						delimitL(tableName) + " FOR EACH ROW " + " CALL " + delimitL(new MyTrigger().getClass().getName()), false, false); // (oneThread ? "QUEUE 0" : "") +
				sendRequest("CREATE TRIGGER " + delimitL("A_" + tableName + "_U") + " AFTER UPDATE ON " +
						delimitL(tableName) + " FOR EACH ROW " + " CALL " + delimitL(new MyTrigger().getClass().getName()), false, false); // (oneThread ? "QUEUE 0" : "") +
			}
		}
		sendRequest("DROP TRIGGER " + delimitL("B_USERS_U"), true, false);
		sendRequest("DROP TRIGGER " + delimitL("B_USERS_D"), true, false);
		sendRequest("DROP TRIGGER " + delimitL("B_USERS_I"), true, false);
		sendRequest("CREATE TRIGGER " + delimitL("B_Users_I") + " BEFORE INSERT ON " +
	        		delimitL("Users") + " FOR EACH ROW " + " CALL " + delimitL(new MyTrigger().getClass().getName()), false, false);    	
	        // Zur Überwachung, damit immer mindestens ein Admin übrig bleibt; dasselbe gibts im MyDataChangeListener für Delete Operations!
	        // Außerdem zur Überwachung, daß der eingeloggte User seine Kennung nicht ändert
		sendRequest("CREATE TRIGGER " + delimitL("B_Users_U") + " BEFORE UPDATE ON " +
	        		delimitL("Users") + " FOR EACH ROW " + " CALL " + delimitL(new MyTrigger().getClass().getName()), false, false);   
	}

	@SuppressWarnings("unchecked")
	private void loadMyTables() {
		MyTable cl = new MyTable("ChangeLog",
				new String[]{"Zeitstempel","Username","Tabelle","TabellenID","Alteintrag"},
				new String[]{"DATETIME","VARCHAR(60)","VARCHAR(100)","INTEGER","OTHER"},
				new String[]{null,null,null,null,null},
				new MyTable[]{null,null,null,null,null});
		addTable(cl, -1);
		MyTable bs = new MyTable("DateiSpeicher",
				new String[]{"Zeitstempel","Tabelle","TabellenID","Feld","Dateiname","Dateigroesse","Datei"},
				new String[]{"DATETIME","VARCHAR(100)","INTEGER","VARCHAR(100)","VARCHAR(255)","INTEGER","BLOB(10M)"},
				new String[]{null,null,null,null,null,null,null},
				new MyTable[]{null,null,null,null,null,null,null});
		addTable(bs, -1);
		MyTable us = new MyTable("Users",
				new String[]{"Username","Vorname","Name","Zugriffsrecht"},
				new String[]{"VARCHAR(60)","VARCHAR(30)","VARCHAR(30)","INTEGER"},
				new String[]{null,null,null,null},
				new MyTable[]{null,null,null,null},
				new String[][]{{"Username"}},
				new LinkedHashMap[]{null,null,null,Users.getUserTypesHash()});
		addTable(us, -1); // müsste jetzt doch gehen, oder?...  lieber die Users ganz weg, weil das Editieren auf dem HSQLServer nicht korrekt funktioniert - siehe im Trigger removeAccRight usw., da müsste man erst die sendRequests umstellen auf defaultconnection...		

		MyTable infoTable = new MyTable("Infotabelle",
				new String[]{"Parameter","Wert"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"},
				new String[]{null, null},
				new MyTable[]{null,null},
				new String[][]{{"Parameter"}},
				new LinkedHashMap[]{null,null});
		addTable(infoTable, -1);

		// Katalogtabellen
		MyTable matrix = new MyTable("Matrices", new String[]{"Matrixname","Leitsatznummer","pH","aw","Dichte","Katalogcodes"},
				new String[]{"VARCHAR(255)","VARCHAR(20)","DOUBLE","DOUBLE","DOUBLE","INTEGER"},
				new String[]{"Kulturmedium / Futtermittel / Lebensmittel / Serum / Kot / Gewebe","Leitsatznummer - falls bekannt","pH-Wert über alle Produkte der Warengruppe - falls abschaetzbar","aw-Wert über alle Produkte der Warengruppe - falls abschaetzbar","Dichte der Matrix über alle Produkte der Warengruppe - falls abschaetzbar","Matrixkatalog - Codes"},
				new MyTable[]{null,null,null,null,null,null},
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
		addTable(toxinUrsprung, -1);
		
		LinkedHashMap<Integer, String> btv = new LinkedHashMap<>();
		btv.put(new Integer(1), "Bakterium");	btv.put(new Integer(2), "Toxin"); btv.put(new Integer(3), "Virus");
		LinkedHashMap<Integer, String> h1234 = new LinkedHashMap<>();
		h1234.put(new Integer(1), "eins");	h1234.put(new Integer(2), "zwei");
		h1234.put(new Integer(3), "drei");	h1234.put(new Integer(4), "vier");					
		LinkedHashMap<Integer, String> hPM = new LinkedHashMap<>();
		hPM.put(new Integer(1), "+");	hPM.put(new Integer(2), "-");
		LinkedHashMap<Integer, String> hYN = new LinkedHashMap<>();
		if (getLanguage().equalsIgnoreCase("en")) {hYN.put(new Integer(1), "yes");	hYN.put(new Integer(0), "no");}
		else {hYN.put(new Integer(1), "ja");	hYN.put(new Integer(0), "nein");}
		LinkedHashMap<Boolean, String> hYNB = new LinkedHashMap<>();
		if (getLanguage().equalsIgnoreCase("en")) {hYNB.put(new Boolean(true), "yes");	hYNB.put(new Boolean(false), "no");}
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


		doLieferkettenTabellen(agenzien, matrix);

	}
	@SuppressWarnings("unchecked")
	private void doLieferkettenTabellen(final MyTable agenzien, final MyTable matrix) {
		LinkedHashMap<Boolean, String> hYNB = new LinkedHashMap<>();
		if (getLanguage().equalsIgnoreCase("en")) {hYNB.put(new Boolean(true), "yes");	hYNB.put(new Boolean(false), "no");}
		else {hYNB.put(new Boolean(true), "ja");	hYNB.put(new Boolean(false), "nein");}
				
		MyTable Knoten = new MyTable("Station", new String[]{"Produktkatalog","Name","Adresse","Land","Betriebsart","Strasse","Hausnummer","Postfach","PLZ","Ort","District","Bundesland","Longitude","Latitude","Ansprechpartner","Telefon","Fax","EMail","Webseite","Betriebsnummer","VATnumber","Code",
				"CasePriority","AnzahlFaelle","AlterMin","AlterMax","DatumBeginn","DatumHoehepunkt","DatumEnde","Erregernachweis","Serial","ImportSources"},
				new String[]{"INTEGER","VARCHAR(255)","VARCHAR(16383)","VARCHAR(100)","VARCHAR(255)","VARCHAR(255)","VARCHAR(10)","VARCHAR(20)","VARCHAR(10)","VARCHAR(60)","VARCHAR(255)","VARCHAR(30)","DOUBLE","DOUBLE","VARCHAR(100)","VARCHAR(30)","VARCHAR(30)","VARCHAR(100)","VARCHAR(255)","VARCHAR(50)","VARCHAR(255)","VARCHAR(25)",
				"DOUBLE","INTEGER","INTEGER","INTEGER","DATE","DATE","DATE","INTEGER","VARCHAR(16383)","VARCHAR(16383)"},
				new String[]{null,null,null, null,"z.B. Endverbraucher, Erzeuger, Einzelhändler, Großhändler, Gastronomie, Mensch. Siehe weitere Beispiele ADV Katalog", null,null,null,null,null,null,null,null,null,"Ansprechpartner inkl. Vor und Zuname",null,null,null,null,null,
				null, "interner Code, z.B. NI00",
				"Falldefinition erfüllt (z.B. laut RKI) - Priorität: Wert zwischen 0 und 1",null,null,null,"Datum frühester Erkrankungsbeginn","Datum des Höhepunkt an Neuerkrankungen","Datum letzter Erkrankungsbeginn",null,null,null},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,agenzien,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,null,allHashes.get("County"),null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null},
				new String[]{"INT",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,"Station_Agenzien",null,null},
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
		proce.put("nicht erhitzt und verzehrsfertig (Salate, rohe Produkte)", getLanguage().equalsIgnoreCase("en") ? "not heated and ready-to-eat (e.g. salads)" : "nicht erhitzt und verzehrsfertig (Salate, rohe Produkte)");
		proce.put("erhitzt und verzehrsfertig (fast alles)", getLanguage().equalsIgnoreCase("en") ? "heated and ready-to-eat" : "erhitzt und verzehrsfertig (fast alles)");
		proce.put("erhitzt und nicht verzehrsfähig (Vorprodukte wie eingefrorene Kuchen)", getLanguage().equalsIgnoreCase("en") ? "heated and not-ready-to-eat (e.g. frozen cake)" : "erhitzt und nicht verzehrsfähig (Vorprodukte wie eingefrorene Kuchen)");
		proce.put("nicht erhitzt und nicht verzehrsfähig (Rohwaren, die nicht zum Rohverzehr bestimmt sind wie Fleisch oder Eier)", getLanguage().equalsIgnoreCase("en") ? "not heated and not-ready-to-eat (meat, eggs)" : "nicht erhitzt und nicht verzehrsfähig (Rohwaren, die nicht zum Rohverzehr bestimmt sind wie Fleisch oder Eier)");
		
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
		addTable(ChargenVerbindungen, Lieferketten_LIST);
		Chargen.setForeignField(ChargenVerbindungen, 1);

		MyTable extraFields = new MyTable("ExtraFields",
				new String[]{"tablename","id","attribute","value"},
				new String[]{"VARCHAR(255)","INTEGER","VARCHAR(255)","VARCHAR(32768)"},
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
		MyTable lookup = new MyTable("LookUps",
				new String[]{"type","value"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"},
				new String[]{null,null}, 
				new MyTable[]{null,null},
				new String[][]{{null,null}},
				new LinkedHashMap[]{null,null}, 
				new String[]{null,null});
		addTable(lookup, Lieferketten_LIST);
		//check4Updates_129_130(myList);

		//DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Bundesland") + " = 'NI' WHERE " + DBKernel.delimitL("ID") + " = 167", false);
	}
  
	private void addTable(MyTable myT, int child) {
		myT.setChild(child);
		allTables.put(myT.getTablename(), myT);
	}

	private void loadHashes() {		
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
	}
	private void loadOther4Gui() {
		// knownCodeSysteme
		knownCodeSysteme = new LinkedHashMap<>();

	  	// treeStructure
		treeStructure = new LinkedHashMap<>();

	    //treeStructure.put(SystemTabellen_LIST, "System-Tabellen");
		treeStructure.put(Lieferketten_LIST, "Lieferketten");	  	
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
