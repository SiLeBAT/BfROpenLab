package de.bund.bfr.knime.openkrise.db.gui.simsearch;

public class DBInfo {
	public enum TABLE {
		STATION("Station", COLUMN.STATION_ID); 
		
		private final String name;
		private final COLUMN primaryKey;
		TABLE(String name, COLUMN primaryKey) { 
			this.name = name; 
			this.primaryKey = primaryKey;
		}
	    public String getName() { return this.name; }
	    public COLUMN getPrimaryKey() { return this.primaryKey; }
	}
	
	public enum COLUMN {
	  STATION_ID("ID", TABLE.STATION),
	  STATION_NAME("Name", TABLE.STATION), 
	  STATION_ADDRESS("Adresse", TABLE.STATION),
	  STATION_ZIP("PLZ", TABLE.STATION), 
	  STATION_STREET("Strasse", TABLE.STATION), 
	  STATION_HOUSENUMBER("Hausnummer", TABLE.STATION),
	  STATION_CITY("Ort", TABLE.STATION), ;
		
		private final String name;
		private final TABLE table;
		COLUMN(String name, TABLE table) { 
			this.name= name; 
			this.table = table;
		}
		public String getName() { return this.name; }
	}

}
