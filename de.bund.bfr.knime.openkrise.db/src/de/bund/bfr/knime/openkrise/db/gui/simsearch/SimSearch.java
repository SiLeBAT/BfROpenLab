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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableModel;
import com.google.common.collect.Sets;

import de.bund.bfr.knime.openkrise.common.DeliveryUtils;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearch.SimSet.Type;

public final class SimSearch {
  
  private final static int STATION_NUMBER = 4;
  private final static int RESULT_NUMBER = 5;

  public static class Settings {
    
    public enum Attribute {
      StationName(90), StationAddress(90), StationZip(90), StationStreet(90), StationCity(90), StationHousenumber(0),
      ProductStation(100), ProductDescription(75),
      LotProduct(100), LotNumber(75),
      DeliveryLot(100), DeliveryDate(0), DeliveryRecipient(100);
      
      private final int defaultTreshold;
      
      Attribute(int defaultTreshold) {
        this.defaultTreshold = defaultTreshold;
      }
      
      public int getDefaultTreshold() { return this.defaultTreshold; }
    }
    
    private Set<SimSet.Type> checkList;
    private Map<Attribute, Integer> attributeTresholdMap;
 
    private boolean useLevenshtein;
    private boolean useFormat2017;
    private boolean readOnly;
    
    

    public Settings() {
      this.attributeTresholdMap = new HashMap<>();
      for(Attribute a: Attribute.class.getEnumConstants()) this.attributeTresholdMap.put(a, a.getDefaultTreshold());
      this.checkList = new HashSet<>();
      this.useLevenshtein = false;
      this.useFormat2017 = !DeliveryUtils.hasOnlyPositiveIDs(DBKernel.getLocalConn(true));
      this.readOnly = false;
    }
    
    public Settings(Settings settingsToCopyFrom) {
      this.attributeTresholdMap = settingsToCopyFrom.attributeTresholdMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      this.checkList = settingsToCopyFrom.checkList.stream().collect(Collectors.toSet());
      
      this.useLevenshtein = settingsToCopyFrom.useLevenshtein;
      this.useFormat2017 = settingsToCopyFrom.useFormat2017;
      this.readOnly = false;
    }

    public int getTreshold(Attribute attribute) { return this.attributeTresholdMap.get(attribute); }
    public void setTreshold(Attribute attribute, int treshold) { this.attributeTresholdMap.put(attribute, treshold); }
    
    public boolean isChecked(SimSet.Type simSetType) { return this.checkList.contains(simSetType); }
    public void setChecked(SimSet.Type simSetType, boolean isChecked) {
      if(isChecked) this.checkList.add(simSetType);
      else this.checkList.remove(simSetType);
    }
    
    public void freeze() { this.readOnly = true; }
    public boolean isReadOnly() { return this.readOnly; }
    
    public boolean getUseAllInOneAddress() { return this.useFormat2017; }
    public boolean getUseArrivedDate() { return this.useFormat2017; }
    public boolean getUseLevenshtein() { return this.useLevenshtein; }
  }

  public interface SimSearchListener {
    public void searchFinished();
    public void searchCanceled();
    public void newSimSetFound();
    public void searchProgressed();
    public void dataLoaded(SimSearchTableModel tableModel, int index);
    public void dataSaved(boolean complete);
    public void simSearchError(Exception err);
  }


  public static class SimSet {
	  public static enum Type {
	      STATION, PRODUCT, LOT, DELIVERY
	  }

    //protected SimType simType;
    private Type type;
    private List<Integer> idList;
    private Integer referenceId;
    //private String alignmentReferenceID;

    //public SimSet(SimType simType) {
    private SimSet(Type type, List<Integer> idList) throws Exception {
      this.type = type;
      if(idList==null || idList.isEmpty()) throw(new Exception("The parameter idList may not be null or empty."));
      this.referenceId = idList.get(1);
      this.idList = Collections.unmodifiableList(idList);
    }
    
    public Type getType() { return this.type; }
    public List<Integer> getIdList() { return this.idList; }
    public Integer getReferenceId() { return this.referenceId; }
    
//    void addId(String id) { 
//    	if(idList.isEmpty()) this.referenceId = id;
//    	this.idList.add(id); 
//    }
//    void removeId(String id) { 
//    	this.idList.remove(id);
//    	if(id.equals(this.referenceId)) this.referenceId = null;
//    }
  }

  protected static abstract class DataSource {
    public interface DataSourceListener {
      public void similaritiesFound(SimSet.Type simSetType, List<Integer> idList);
    }
    private DataSourceListener listener;
    private boolean searchStopped;
    
    protected DataSource(DataSourceListener listener) {
      this.listener = listener;
      this.searchStopped = false;
    }
    public abstract void findSimilarities(Settings settings) throws Exception;
    //public void findSimilarStations();
    public abstract SimSearchDataLoader getDataLoader();
    
    public final void stopSearch() {
      this.searchStopped = true;
    }
    
    public final boolean getSearchStopped() { return this.searchStopped; }
    
    final void newSimilarityMatchFound(SimSet.Type simSetType, List<Integer> idList) {
      if(listener!=null) listener.similaritiesFound(simSetType, idList);
    }
  }
  
  private Settings simSearchSettings;
  private List<SimSet> simSetList;
  //private Map<SimSet.SimType, Map<String, DBEntity>> dbData;
  //private Map<String, StationDBEntity> stationData;
  //private Map<String, ProductDBEntity> productData;
  //private Map<Class<? extends DBEntity>, Map<String, DBEntity>> dbData;
  private boolean searchStopped;
  private List<SimSearchListener> simSearchListenerList;
  private SimSearchDataManipulationHandler dataManipulationHandler;
  private DataSource dataSource;
  
  //private Map<SimSet, String> alignmentReferenceMap;
  
//  private static class RedoUndoAction {
//	//private Map<SimSet.Type, MergeMap> mergeMap;
//	//private Map<SimSet.Type, Set<String>> removeMap;
//	//private Map<SimSet, String> alignmentReferenceMap;
//    
//    
//	  
//	  RedoUndoAction(Map<SimSet.Type, MergeMap> mergeMap, Map<SimSet.Type, Set<String>> removeMap, Map<SimSet, String> alignmentReferenceMap) {
//		  this.mergeMap = mergeMap;
//		  this.removeMap = removeMap;
//		  this.alignmentReferenceMap = alignmentReferenceMap;
//	  }
//  }


  public SimSearch() {
    this.simSearchListenerList = new ArrayList<>();
    this.dataSource = new SimSearchDataSource(new DataSource.DataSourceListener() {
      
      @Override
      public void similaritiesFound(Type simSetType, List<Integer> idList) {
        // TODO Auto-generated method stub
        
        try {
          SimSearch.this.simSetList.add(new SimSearch.SimSet(simSetType, idList));
          SimSearch.this.call(l -> l.newSimSetFound());
        } catch (Exception err) {
          // TODO Auto-generated catch block
          err.printStackTrace();
          SimSearch.this.call(l -> l.simSearchError(err));
        }
        
      }
    });
//    this.dataManipulationHandler = new SimSearchDataManipulationHandler();
  }



  public void startSearch(Settings settings) {
    if(settings==null) return;
    this.simSetList = Collections.synchronizedList(new ArrayList<SimSet>());
    this.dataManipulationHandler = new SimSearchDataManipulationHandler();
    //this.dataSource = new SimSearchDataSource();
    //this.initMaps();
    //this.dbData = new HashMap<>();
//    this.stationData = new HashMap<>();
//    this.productData = new HashMap<>();
    this.searchStopped = false;
    this.simSearchSettings = settings;
    
    Runnable runnable = new Runnable(){

      public void run(){
         SimSearch.this.runSearch();
      }
    };

    Thread thread = new Thread(runnable);
    thread.start();
  }
  
//  private void initMaps() {
//    this.removeMap = new HashMap<>();
//    this.mergeMap = new HashMap<>();
//    for(SimSet.Type simSetType : Arrays.asList(SimSet.Type.class.getEnumConstants())) {
//      this.removeMap.put(simSetType, new HashSet<>());
//      this.mergeMap.put(simSetType, new MergeMap());
//    }
//    this.alignmentReferenceMap = new HashMap<>();
//  }

//  public void mergeInto(SimSet.Type simSetType, List<String> idToMerge, String idToMergeInto) {
//	  
//  }


//  private <X extends DBEntity> void loadData(Class<X> simType, List<String> idList) {
//    idList = idList.stream().filter(s -> !this.dbData.get(simType).containsKey(s)).collect(Collectors.toList());
//    if(!idList.isEmpty()) {
//      if(simType== StationDBEntity.class) {
//        //List<X> resList = loadStationData(idList);
//      } else {
//        call(l -> l.simSearchError(new Exception("Unknown simType")));
//      }
//    }
//  }

  public int getSimSetCount() { return (this.simSetList==null?0:this.simSetList.size()); }
  
  
  
  
  
  
  
  
  
//  public SimSearchTableModel loadStationTable(SimSet simSet) {
//	  final String[] columnNames = new String[] {"Status", "ID", "Name", "Address", "Country", "Products"};
//	  final Class<?>[] columnClasses = new Class<?>[] {Integer.class, String.class, 
//		  Alignment.AlignedSequence.class, Alignment.AlignedSequence.class, String.class, List.class};
//	 final int columnCount = columnNames.length;
//     Object[][] data = new Object[simSet.getIdList().size()][columnCount];
//     for(int row=0; row<simSet.getIdList().size(); ++row) {
//    	 data[row][0] = 0;
//    	 data[row][1] = simSet.idList.get(row);
//    	 data[row][2] = RandomDummy.manipulateText("Marmeladen Hersteller", 5);
//    	 data[row][3] = RandomDummy.manipulateText("Am Teichgraben 28, 34567 Heuyerswerder", 7);
//    	 data[row][4] = RandomDummy.manipulateText("Deutschland", 3);
//    	 data[row][5] = Arrays.asList(RandomDummy.getRandomTexts(4, 7));
//     }
//     return new SimSearchTableModel(this, simSet, data, columnNames, columnClasses);
//  }
  
  public void loadData(int simSetIndex) {
    SimSet simSet = this.simSetList.get(simSetIndex);
    if(simSet==null) {
      call(l -> l.simSearchError(new Exception("Simset was not found")));
      return;
    }
    //List<String> idList = simSet.idList.stream().filter(s -> !this.dbData.get(simSet.simType).containsKey(s)).collect(Collectors.toList());

    //		if(!idList.isEmpty()) {
    //			switch(simSet.simType) {
    //			case Station :
    //				loadStationData(idList);
    //			default : 
    //				call(l -> l.simSearchError(new Exception("Unknown simType")));
    //			}
    //		}
    // collect Data, this is a read only list
    //List<? extends DBEntity> resList;
    //		switch(simSet.simType) {
    //		case Station :
    //			resList = new ArrayList<StationDBEntity>();
    //		default : 
    //			throw(new Exception());
    //		}

    //resList = simSet.idList.stream().map(id -> this.dbData.get(SimSet.SimType.Station).get(id)).collect(Collectors.toList());
    //this.dbData.get(SimSet.SimType.Station)
    //		simSet.idList.forEach(id -> resList.add(this.dbData.get(SimSet.SimType.Station).get(id)));
    //		resList.add(new DBEntity("sds"));
    //		resList.add(this.dbData.get(SimSet.SimType.Station).get("sdsd"));
    //SimSearchTableModel tableModel = null;
    SimSearchDataLoader dataLoader = new SimSearchDataLoader(simSet, dataManipulationHandler);
    try {
		dataLoader.loadData();
		SimSearchTableModel tableModel = new SimSearchTableModel(simSet, dataManipulationHandler, dataLoader);
		call(l->l.dataLoaded(tableModel, simSetIndex));
	} catch (Exception err) {
		// TODO Auto-generated catch block
		call(l -> l.simSearchError(err));
		//e.printStackTrace();
	}
    
    
    
    
//    try {
//      if(simSet.getType()==SimSet.Type.STATION) {
//        final SimSearchTableModel tableModel = loadStationTable(simSet);
//        call(l->l.dataLoaded(tableModel, simSetIndex));
//      } 
//    } catch(Exception err) {
//      err.printStackTrace();
//      call(l->l.simSearchError(err));
//    }
  }

//  List<StationDBEntity> loadStationData(List<String> idList) {
//    return this.loadStationData(idList, true);
////    idList.forEach(id -> {
////      if(this.searchStopped) return;
////      this.dbData.get(SimSet.SimType.Station).put(id, new StationDBEntity(
////          id, 
////          RandomDummy.manipulateText("Hersteller GmbH", 4), 
////          RandomDummy.manipulateText("An Teichgraben 8", 4),
////          RandomDummy.manipulateText("Germany",2)));
////    });
//  }
  
//  private List<StationDBEntity> loadStationData(List<String> idList, boolean complete) {
//    List<StationDBEntity> stationList = new ArrayList<>();
////    List<String> productIdList = new ArrayList<>();
//    for(String id: idList) {
//      if(this.searchStopped) return null;
//      StationDBEntity station = this.stationData.get(id);
//      if(station==null || (complete && station.getProductIDs()==null)) {
//        station = loadStationFromRandomSource(id, complete);
////        if(complete && station!=null) {
////          for(String productId: station.getProductIDs()) {
////            ProductDBEntity product = this.productData.get(productId);
////            if(product==null) productIdList.add(productId);
////          }
////        }
//      }
//      if(station!=null) stationList.add(station);
//    }
////    if(!productIdList.isEmpty()) loadProductsFromRandomSource(productIdList, false);
//    
//    return stationList;
//  }
  
//  private StationDBEntity loadStationFromRandomSource(String id, boolean complete) {
//    if(this.searchStopped) return null;
//    StationDBEntity station = new StationDBEntity(
//          id, 
//          RandomDummy.manipulateText("Hersteller GmbH", 4), 
//          RandomDummy.manipulateText("An Teichgraben 8", 4),
//          RandomDummy.manipulateText("Germany",2),
//          Arrays.asList(RandomDummy.getRandomTexts(4, id.length())));
//    for(String productId : station.getProductIDs()) {
//      ProductDBEntity product = new ProductDBEntity(
//          productId, 
//          RandomDummy.manipulateText("Erdbeermarmelade", 4), 
//          id);
//      this.productData.put(product.getID(), product);
//    }
//    this.stationData.put(id, station);
//    return station;
//  }
  
//  private List<ProductDBEntity> loadProductData(List<String> idList, boolean complete) {
//    List<ProductDBEntity> productList = new ArrayList<>();
////    List<String> productIdList = new ArrayList<>();
//    for(String id: idList) {
//      if(this.searchStopped) return null;
//      ProductDBEntity product = this.productData.get(id);
//      
//      if(product!=null) productList.add(product);
//    }
////    if(!productIdList.isEmpty()) loadProductsFromRandomSource(productIdList, false);
//    
//    return productList;
//  }
//  private List<StationDBEntity> loadStationsFromDB(List<String> idList, boolean complete) {
//    List<StationDBEntity> stationList= new ArrayList<>();
//    for(String id: idList) {
//      if(this.searchStopped) return null;
//      StationDBEntity station = new StationDBEntity(
//          id, 
//          RandomDummy.manipulateText("Hersteller GmbH", 4), 
//          RandomDummy.manipulateText("An Teichgraben 8", 4),
//          RandomDummy.manipulateText("Germany",2),
//          Arrays.asList(RandomDummy.getRandomTexts(4, id.length())));
//      this.stationData.put(id, station);
//    }
//    return stationList;
//  }
  
//  private List<StationDBEntity> loadStationData(List<String> idList, boolean loadComplete) {
//    idList.forEach(id -> {
//      if(this.searchStopped) return;
//      this.dbData.get(SimSet.SimType.Station).put(id, new StationDBEntity(
//          id, 
//          RandomDummy.manipulateText("Hersteller GmbH", 4), 
//          RandomDummy.manipulateText("An Teichgraben 8", 4),
//          RandomDummy.manipulateText("Germany",2)));
//    });
//  } 
  
  private void runSearch() {
    try {
      this.dataSource.findSimilarities(this.simSearchSettings);
    } catch (Exception err) {
      call(l->l.simSearchError(err));
      //e.printStackTrace();
    }
//    if(this.preprocessDatabase()) {
//      if(!this.searchStopped && this.simSearchSettings.getCheckStations()) this.findSimilarStations();
//    }
//    this.postProcessDatabase();
    if(this.searchStopped) {
      call(l->l.searchCanceled());
    } else {
      call(l -> l.searchFinished());
    }
  }

  private void call(Consumer<SimSearchListener> action) {
    this.simSearchListenerList.forEach(action);
    //Stream.of(getListeners(CanvasListener.class)).forEach(action);
  }

//  private void findSimilarStations() {
//    for(int iR = 1; iR <= RESULT_NUMBER; iR++) {
//      if(this.searchStopped) return; 
//      try {
//        Thread.sleep(500);
//      } catch (InterruptedException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//      }
//      SimSet simSet = new SimSet(SimSet.Type.STATION);
//      for(int iS=1; iS<=STATION_NUMBER; iS++) {
//        if(this.searchStopped) return;
//        try {
//          Thread.sleep(100);
//        } catch (InterruptedException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//        }
//        simSet.addId(RandomDummy.getRandomText(8));
//      }
//      this.simSetList.add(simSet);
//      call(l -> l.newSimSetFound());
//    }
//  }


  private boolean preprocessDatabase() {
    return true;
  }
  private void postProcessDatabase() {

  }
  public void stopSearch() {

  }

  public void addEventListener(SimSearchListener listener) {
    this.simSearchListenerList.add(listener);
  }

  public void removeEventListener(SimSearchListener listener) {
    this.simSearchListenerList.remove(listener);
  }
}
