/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomDummy {
   
	//private static final Random rnd = new Random();
	private static final char[] SALTCHARS = ((String) "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz").toCharArray();

	public static class DB {
	  
	  private final Random rnd;
	  private final int IDLENGTH = 8;
	  
	  Map<String, Station> stationList = new HashMap<>();
	  Map<String, Station> productList = new HashMap<>();
	  
	  private class Station {
	    private String id = getRandomText(IDLENGTH) ;
	    private String name = "Hersteller " + getRandomText(6);
	    private String address = getRandomText(6) + "allee";
	    private String country = getRandomText(4);
	    private String typeOfBusiness = getRandomText(4)+ "business";
	    private String houseNumber = ((Integer) rnd.nextInt(1000)).toString();
	    private String zipCode = String.format("%05i", rnd.nextInt(99999));
	    
	    private List<String> productList = new ArrayList<>();
	  }
	  
	  private class Product {
        private String id = getRandomText(IDLENGTH) ;
        private String stationId;
        private String itemNumber;
        private String denomination = "Product " + getRandomText(6);
        private String processing = "Processing" + getRandomText(4);
        private String intendedUse;
        private String code;
        private String matrices;
        
        private List<String> lotList = new ArrayList();
      }
	  
	  private class Lot {
        private String id;
        private String productId;
        private String ingredients;
        private String itemNumber;
        private String lotNumber;
        private String amount;
        private String unit;
        
        private List<String> deliveryList = new ArrayList();
      }
	  
	  private class Delivery {
	    private String id;
	    private String lotId;
	    private String dd_day;
	    private String dd_month;
	    private String dd_year;
	    private String ad_day;
	    private String ad_month;
	    private String ad_year;
	    private String amount;
	    private String unit;
	    private String receiverId;
	  }
	  
	  private DB(int stationCount, int productCountPerStation, int lotCountPerProduct, int deliveryCountPerLot) {
	    this.rnd = new Random(Arrays.deepHashCode(new Object[] {stationCount, productCountPerStation, lotCountPerProduct, deliveryCountPerLot}));
	  }
	  
	  private void createRandomData(int stationCount, int productCountPerStation, int lotCountPerProduct, int deliveryCountPerLot) {
	    
	  }
	  
   public String getRandomText(int length) {
        //StringBuffer salt = new StringBuffer();
       char[] res = new char[length];
       for(int i=0; i<length; i++) res[i] = SALTCHARS[rnd.nextInt(SALTCHARS.length)];
       return res.toString();
   }
   
   public String[] getRandomTexts(int count, int length) {
     String[] randomText = new String[count];
     for(int i=0; i<count; ++i) randomText[i] = getRandomText(length);
     return randomText;
   }
   
   public String manipulateText(String text, int number) {
       StringBuffer sb = new StringBuffer(text);
       for(int i=0; i<number; i++) {
           int k = rnd.nextInt(sb.length());
           if(rnd.nextFloat()<0.2) {
               //insert
               sb.insert(rnd.nextInt(sb.length()), SALTCHARS[rnd.nextInt(SALTCHARS.length)]);
           } else {
               //mismatch
               sb.setCharAt(rnd.nextInt(sb.length()), SALTCHARS[rnd.nextInt(SALTCHARS.length)]);
           }
       }
       return sb.toString();
   }
	}
}
