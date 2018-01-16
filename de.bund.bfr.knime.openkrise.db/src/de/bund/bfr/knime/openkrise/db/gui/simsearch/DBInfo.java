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
	}
	
	public enum COLUMN {
	  STATION_ID("id", TABLE.STATION),
	  STATION_NAME("name", TABLE.STATION), 
	  STATION_ADDRESS("adresse", TABLE.STATION),
	  STATION_ZIP("plz", TABLE.STATION), 
	  STATION_STREET("strasse", TABLE.STATION), 
	  STATION_HOUSENUMBER("housenumber", TABLE.STATION),
	  STATION_CITY("ort", TABLE.STATION), ;
		
		private final String name;
		private final TABLE table;
		COLUMN(String name, TABLE table) { 
			this.name= name; 
			this.table = table;
		}
		public String getName() { return this.name; }
	}

}
