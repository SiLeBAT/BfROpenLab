/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui.simsearch;

public class DBInfo {
	public enum TABLE {
		STATION("Station"),
		PRODUCT("Produktkatalog"),
		LOT("Chargen"),
		DELIVERY("Lieferungen"),
		IGNORESIM("IgnorierteAehnlichkeiten"),
		MATRIX("Matrices"),
		AGENT("Agenzien"),
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
      DELIVERY_DELIVEREDON_MONTH("dd_month", TABLE.DELIVERY),
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
