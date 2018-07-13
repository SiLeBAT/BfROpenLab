package de.bund.bfr.knime.openkrise.db.gui.simsearch;

public class DBInfo {
	public enum TABLE {
		STATION("Station"),
		PRODUCT("Produktkatalog"),
		LOT("Chargen"),
		DELIVERY("Lieferungen"),
//		IGNORESTATIONSIM("IgnorierteStationenAehnlichkeiten"),
//		IGNOREPRODUCTSIM("IgnorierteProduktkatalogAehnlichkeiten"),
//		IGNORELOTSIM("IgnorierteChargenAehnlichkeiten"),
//		IGNOREDELIVERYSIM("IgnorierteLieferungenAehnlichkeiten"),
		IGNORESIM("IgnorierteAehnlichkeiten"),
		MATRIX("Matrices"),
		AGENT("Agenzien"),
		//STATION_AGENT("Station_Agenzien")
		LOTLINK("ChargenVerbindungen"),
		
		; 
		
		private final String name;
	
		TABLE(String name) { this.name = name; 	}
		
	    public String getName() { return this.name; }
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
	  PRODUCT_LOTS("Chargen", TABLE.PRODUCT),
	  
	  LOT_ID("ID", TABLE.LOT),
	  LOT_PRODUCT("Artikel", TABLE.LOT),
	  LOT_NUMBER("ChargenNr", TABLE.LOT),
	  LOT_INGREDIENTS("Zutaten", TABLE.LOT),
	  LOT_DELIVERIES("Lieferungen", TABLE.LOT),
	  LOT_MHDDAY("MHD_day", TABLE.LOT),
	  LOT_MHDMONTH("MHD_month", TABLE.LOT),
	  LOT_MHDYEAR("MHD_year", TABLE.LOT),
	  LOT_PRODUCTIONDAY("pd_day", TABLE.LOT),
	  LOT_PRODUCTIONMONTH("pd_month", TABLE.LOT),
	  LOT_PRODUCTIONYEAR("pd_year", TABLE.LOT),
	  
	  DELIVERY_ID("ID", TABLE.DELIVERY),
	  DELIVERY_LOT("Charge", TABLE.DELIVERY),
	  DELIVERY_ARRIVEDON_DAY("ad_day", TABLE.DELIVERY),
	  DELIVERY_ARRIVEDON_MONTH("ad_month", TABLE.DELIVERY),
	  DELIVERY_ARRIVEDON_YEAR("ad_year", TABLE.DELIVERY),
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
	  
	  LOTLINK_ID("id", TABLE.LOTLINK),
	  LOTLINK_INGREDIENT("Zutat", TABLE.LOTLINK),
	  LOTLINK_PRODUCT("Produkt", TABLE.LOTLINK),
	  
	  
	  
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
		public String getFullName() { return this.table.name + "." +  this.name; } 
	}

}
