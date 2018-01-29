package de.bund.bfr.knime.openkrise.db.gui.simsearch;

public class DBInfo {
	public enum TABLE {
		STATION("Station", COLUMN.STATION_ID),
		PRODUCT("Produktkatalog", COLUMN.PRODUCT_ID),
		LOT("Chargen", COLUMN.LOT_ID),
		DELIVERY("Lieferungen", COLUMN.DELIVERY_ID),
//		IGNORESTATIONSIM("IgnorierteStationenAehnlichkeiten"),
//		IGNOREPRODUCTSIM("IgnorierteProduktkatalogAehnlichkeiten"),
//		IGNORELOTSIM("IgnorierteChargenAehnlichkeiten"),
//		IGNOREDELIVERYSIM("IgnorierteLieferungenAehnlichkeiten"),
		IGNORESIM("IgnorierteAehnlichkeiten"),
		MATRIX("Matrices", COLUMN.MATRIX_ID),
		AGENT("Agenzien", COLUMN.AGENT_ID),
		//STATION_AGENT("Station_Agenzien")
		
		; 
		
		private final String name;
		private final COLUMN primaryKey;
	
		TABLE(String name, COLUMN primaryKey) { 
			this.name = name; 
			this.primaryKey = primaryKey;
		}
		TABLE(String name) { 
          this.name = name; 
          this.primaryKey = null;
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
	  STATION_CITY("Ort", TABLE.STATION),
	  STATION_AGENTS("Erregernachweis", TABLE.STATION),
	  
	  PRODUCT_ID("ID", TABLE.PRODUCT),
	  PRODUCT_STATION("Station", TABLE.PRODUCT),
	  PRODUCT_DESCRIPTION("Bezeichnung", TABLE.PRODUCT),
	  
	  LOT_ID("ID", TABLE.LOT),
	  LOT_PRODUCT("Artikel", TABLE.LOT),
	  LOT_NUMBER("ChargenNr", TABLE.LOT),
	  
	  DELIVERY_ID("ID", TABLE.DELIVERY),
	  DELIVERY_LOT("Charge", TABLE.DELIVERY),
	  DELIVERY_ARIVEDON_DAY("ad_day", TABLE.DELIVERY),
	  DELIVERY_ARIVEDON_MONTH("ad_month", TABLE.DELIVERY),
	  DELIVERY_ARIVEDON_YEAR("ad_year", TABLE.DELIVERY),
	  DELIVERY_DELIVEREDON_DAY("dd_day", TABLE.DELIVERY),
      DELIVERY_DELIVEREDON_MONTH("ad_month", TABLE.DELIVERY),
      DELIVERY_DELIVEREDON_YEAR("dd_year", TABLE.DELIVERY),
	  DELIVERY_RECIPIENT("Empfänger", TABLE.DELIVERY),
	  
	  IGNORESIM_ID1("id_1", TABLE.IGNORESIM),
	  IGNORESIM_ID2("id_2", TABLE.IGNORESIM),
	  IGNORESIM_TYPE("type", TABLE.IGNORESIM),
	  
	  MATRIX_ID("id", TABLE.MATRIX),
	  MATRIX_NAME("Matrixname", TABLE.MATRIX),
	  
	  AGENT_ID("id", TABLE.AGENT),
	  AGENT_NAME("Agensname", TABLE.AGENT),
	  
	  
//	  IGNORESTATIONSIM_ID1(IGNORESIM_ID1.getName(), TABLE.IGNORESTATIONSIM),
//	  IGNORESTATIONSIM_ID2(IGNORESIM_ID2.getName(), TABLE.IGNORESTATIONSIM),
//	  IGNOREPRODUCTSIM_ID1(IGNORESIM_ID1.getName(), TABLE.IGNOREPRODUCTSIM),
//	  IGNOREPRODUCTSIM_ID2(IGNORESIM_ID2.getName(), TABLE.IGNOREPRODUCTSIM),
//	  IGNORELOTSIM_ID1(IGNORESIM_ID1.getName(), TABLE.IGNORELOTSIM),
//	  IGNORELOTSIM_ID2(IGNORESIM_ID2.getName(), TABLE.IGNORELOTSIM),
//	  IGNOREDELIVERYSIM_ID1(IGNORESIM_ID1.getName(), TABLE.IGNOREDELIVERYSIM),
//	  IGNOREDELIVERYSIM_ID2(IGNORESIM_ID2.getName(), TABLE.IGNOREDELIVERYSIM),
	  
	  
	  ;
	  
		
		private final String name;
		private final TABLE table;
		COLUMN(String name, TABLE table) { 
			this.name= name; 
			this.table = table;
		}
		public String getName() { return this.name; }
	}

}
