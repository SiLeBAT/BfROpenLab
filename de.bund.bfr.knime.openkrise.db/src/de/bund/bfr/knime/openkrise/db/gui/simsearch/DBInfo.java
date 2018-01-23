package de.bund.bfr.knime.openkrise.db.gui.simsearch;

public class DBInfo {
	public enum TABLE {
		STATION("Station", COLUMN.STATION_ID),
		PRODUCT("Produktkatalog", COLUMN.PRODUCT_ID),
		LOT("Chargen", COLUMN.LOT_ID),
		DELIVERY("Lieferungen", COLUMN.DELIVERY_ID),
		STATION_IGNORE("IgnorierteStationenAehnlichkeiten"),
		PRODUCT_IGNORE("IgnorierteProduktkatalogAehnlichkeiten"),
		LOT_IGNORE("IgnorierteChargenAehnlichkeiten"),
		DELIVERY_IGNORE("IgnorierteLieferungenAehnlichkeiten")
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
	
//	checkTable4ISM("Lieferungen", new String[]{"Charge","ad_day","ad_month","ad_year","Empfänger"},
//        new int[]{(Integer)pd4.dl.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dd.getValue(),(Integer)pd4.dr.getValue()}, null, null, null)
//    :
//    null;
//}
//else {
//vals4 = pd4.cd.isSelected() ?
//checkTable4ISM("Lieferungen", new String[]{"Charge","dd_day","dd_month","dd_year","Empfänger"},
	public enum COLUMN {
	  STATION_ID("ID", TABLE.STATION),
	  STATION_NAME("Name", TABLE.STATION), 
	  STATION_ADDRESS("Adresse", TABLE.STATION),
	  STATION_ZIP("PLZ", TABLE.STATION), 
	  STATION_STREET("Strasse", TABLE.STATION), 
	  STATION_HOUSENUMBER("Hausnummer", TABLE.STATION),
	  STATION_CITY("Ort", TABLE.STATION),
	  
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
