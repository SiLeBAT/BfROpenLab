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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.table.DefaultTableModel;
import com.google.common.collect.Sets;

public final class SimSearch {
  
  private final static int STATION_NUMBER = 4;
  private final static int RESULT_NUMBER = 5;

  public static class Settings {
    private boolean checkStations;
    private int stationNameSim;
    private int stationAddressSim;
    private int stationCountrySim;

    public Settings() {
      this.checkStations = true;
      this.stationNameSim = 90;
      this.stationAddressSim = 90;
      this.stationCountrySim = 90;
    }

    public boolean getCheckStations() { return this.checkStations; }
    public int getStationNameSim() { return this.stationNameSim; }
    public int getStationAddressSim() { return this.stationAddressSim; }
    public int getStationCountrySim() { return this.stationCountrySim; }
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
    private List<String> idList;
    private String referenceId;
    //private String alignmentReferenceID;

    //public SimSet(SimType simType) {
    public SimSet(Type type) {
      this.type = type;
      this.idList = new ArrayList<>();
    }
    
    public Type getType() { return this.type; }
    private List<String> getIdList() { return this.idList; }
    public String getReferenceId() { return this.referenceId; }
    
    void addId(String id) { 
    	if(idList.isEmpty()) this.referenceId = id;
    	this.idList.add(id); 
    }
    void removeId(String id) { 
    	this.idList.remove(id);
    	if(id.equals(this.referenceId)) this.referenceId = null;
    }
  }

  public class MergeMap {
    public class MergeException extends Exception {

    }
    private Map<String,String> mergedIntoAssignment;
    private Map<String,String> mergedIntoResult;

    public MergeMap() {
      this.mergedIntoAssignment = new HashMap<>();
      this.mergedIntoResult = new HashMap<>();
    }

    public String getMergeResult(String id) { return this.mergedIntoResult.get(id); }
    public String getMergeAssignment(String id) { return this.mergedIntoAssignment.get(id); }

    public void mergeInto(String idToMerge, String idToMergeInto) throws MergeException {
      if(mergedIntoAssignment.containsKey(idToMergeInto)) throw(new MergeException());
      if(mergedIntoAssignment.containsKey(idToMerge)) throw(new MergeException());

      mergedIntoAssignment.put(idToMerge, idToMergeInto);
      mergedIntoResult.entrySet().stream().filter(e -> idToMerge.equals(e.getValue())).forEach(e -> {
        mergedIntoResult.put(e.getKey(), idToMergeInto);
      });
    }
  }

  public final static class SimSearchTableModel extends DefaultTableModel{
    
//    public static enum RowStatus {
//      Default(1), mergeToPresentRow(2), mergeToNonPresentRow(4), Remove(8), DerivedRemove(16), SimReference(32), AlignmentReference(64);
//      
//      private int numVal;
//      
//      private RowStatus(int value) {
//        this.numVal = value;
//      }
//      
//      public int getNumVal() {
//        return numVal;
//      }
//      
//    }
    
    /**
     * 
     */
    private static final long serialVersionUID = -4838767897732945130L;

    private final String[] columnNames;
    private final Class<?>[] columnClasses;

    private Object[][] data;

    private boolean[] toRemove;
    private int[] mergeTo;

    private final int rowCount;
    private final int columnCount;
    
    private final static int IDCOLUMN = 1;
    //private int alignmentReferenceRow;
    //private int referenceRow;

    //private final Map<Integer,List<Integer>> mergeList; // row index maps to a list with its merges
    //private final List<Integer> mergeRows;  // list of rows which where merged

    //	public SimSearchTableModel(Object[][] data, String[] columnNames, Class<?>[] columnClasses, boolean[] toRemove, int[] mergeTo) {
    //	 super();
    //	 this.data = data;
    //	 this.columnNames = columnNames;
    //	 this.columnClasses = columnClasses;
    //	 this.toRemove = toRemove;
    //	 this.mergeTo = mergeTo;
    //	 this.columnCount = columnNames.length;
    //	 this.rowCount = this.data.length;
    //	}

    private final SimSet simSet;
    private final SimSearch simSearch;
    
    public static class IllegalOperationException extends Exception {
      
      IllegalOperationException(String message) {
        super(message);
      }
    }

    SimSearchTableModel(SimSearch simSearch, SimSet simSet, Object[][] data, String[] columnNames, Class<?>[] columnClasses) {
      super();
      this.simSearch = simSearch;
      this.simSet = simSet;
      this.data = data;
      this.columnNames = columnNames;
      this.columnClasses = columnClasses;
      this.columnCount = columnNames.length;
      this.rowCount = data.length;
      this.createAlignments();
      this.initArrays();
//      if(simSet.simType==StationDBEntity.class) {
//        this.loadStationData();
////      } else 
//       // if(simSet.simType==ProductDBEntity.class) {
//        //loadProductData();
//      } else {
//        throw(new Exception());
//      }
      //SimSearch.this.loadData(, idList);
    }
    
    private void initArrays() {
      this.mergeTo = new int[this.rowCount];
      this.toRemove = new boolean[this.rowCount];
      
      Arrays.fill(this.mergeTo, -1);
      Arrays.fill(this.toRemove, false);
      Map<String, Integer> idToIndexMap = null;
      for(String id : this.simSet.idList) if(simSearch.mergeMap.get(simSet.type).mergedIntoResult.containsKey(id)) {
        if(idToIndexMap==null) idToIndexMap = getIdToRowIndexMap();

        this.mergeTo[idToIndexMap.get(id)] = idToIndexMap.get(simSearch.mergeMap.get(simSet.type).mergedIntoResult.get(id));
      }
      for(String id: Sets.intersection(simSearch.removeMap.get(simSet.type), new HashSet<>(simSet.idList))) {
        if(idToIndexMap==null) idToIndexMap = getIdToRowIndexMap();
        this.toRemove[idToIndexMap.get(id)] = true;
      }
      this.mergeTo[2] = 0;
    }
    
    private Map<String, Integer> getIdToRowIndexMap() {
      Map<String, Integer> idToIndexMap = new HashMap<>();
      for(int row=0; row < this.rowCount; ++row) idToIndexMap.put((String) data[row][IDCOLUMN], row);
      return idToIndexMap;
    }
    
    private void createAlignments() {
    	if(!simSearch.alignmentReferenceMap.containsKey(this.simSet)) {
    		if(this.simSet.getReferenceId()!=null) simSearch.alignmentReferenceMap.put(this.simSet, this.simSet.getReferenceId());
    	}
    	String referenceId = simSearch.alignmentReferenceMap.get(this.simSet);
    	int referenceIndex = -1;
    	if(referenceId!=null && !referenceId.isEmpty()) {
        	for(int row=0; row<this.rowCount; ++row) {
        		if(referenceId.equals((String) data[row][IDCOLUMN])) {
        			referenceIndex = row;
        			break;
        		}
        	}
    	}
    	for(int column=0; column < this.columnCount; ++column) {
    		if(this.columnClasses[column]==Alignment.AlignedSequence.class) {
    			String[] sequences = new String[this.rowCount];
    			for(int row=0; row<this.rowCount; ++row) {
    				sequences[row] = (String) data[row][column];
    			}
    			Alignment.AlignedSequence[] alignedSeqs= Alignment.alignSequences(sequences, referenceIndex);
    			for(int row=0; row<this.rowCount; ++row) {
    				data[row][column] = alignedSeqs[row];
    			}
    		}
    	}
    }
    
    public boolean areRowsDraggable(List<Integer> indexList) {
    	return !indexList.stream().anyMatch(i -> this.isMerged(i));
    }
    
    public boolean isMergeValid(List<Integer> rowsToMerge, int rowToMergeInto) {
    	return false;
    }
    
    public void merge(int[] rowsToMerge, int rowToMergeInto) {
    	
    }
    
    public void unMerge(int[] rowsToUnMerge) {
    	
    }
    
    public boolean isMerged(int row) {
    	return mergeTo[row]>=0;
    }
    
    public int getMergeCount(int row) {
      return -1;
    }
    
    public boolean isSimReferenceRow(int row) {
      return ((String) this.data[row][IDCOLUMN]).equals(simSet.referenceId);
    }
    
    public boolean isAlignmentReferenceRow(int row) {
      return ((String) this.data[row][IDCOLUMN]).equals(simSearch.alignmentReferenceMap.get(simSet));
    }

//    private void loadStationData() {
//      List<StationDBEntity> stationList = this.simSearch.loadStationData(this.simSet.idList, true);
//      if(stationList==null) return;
//      this.rowCount = stationList.size();
//      this.columnNames = new String[] {"Status", "ID", "Name", "Address", "Country","Products"};
//      this.columnClasses = new Class<?>[] {Integer.class, String.class, AlignedSequence.class, AlignedSequence.class, String.class, List.class};
//      this.columnCount = this.columnNames.length;
//      this.data = new Object[this.rowCount][this.columnCount];
//      for(int i=0; i < this.rowCount; ++i) {
//        StationDBEntity station = stationList.get(i);
//        Integer status = RowStatus.Empty.getNumVal();  
//        if(station.getID().equals(simSet.alignmentReferenceID)) {
//          this.alignmentReferenceRow = i;
//          this.data[i][0] &= RowStatus.AlignmentReference.getNumVal(); 
//        }
//        
//        this.data[i][1] = station.getName();
//        this.data[i][2] = station.getAddress();
//        this.data[i][3] = station.getCountry();
//        List<ProductDBEntity> productList = this.simSearch.loadProductData(station.getProductIDs(),false);
//        if(productList!=null) this.data[i][4] = productList.stream().map(p -> p.getName()).collect(Collectors.toList());
//      }
//      for(int column=0; column < this.columnCount; ++column) {
//        if(this.columnClasses[column]==AlignedSequence.class) {
//          if(this.simSet.alignmentReferenceID.equals(this.data[column]))
//        }
//      }
//    }

    public boolean remove(int[] rows) throws IllegalOperationException{
      if(rows.length>0) {
        //List<Integer> rowList = new ArrayList<>(rows.length);
        //for(int i=0; i<rows.length; ++i) rowList.add(rows[i]);
        Set<String> ids = new HashSet<>(simSearch.mergeMap.get(simSet.type).mergedIntoAssignment.values());
        //if(rowList.stream().anyMatch(i -> ids.contains(data[i][IDCOLUMN]))) {
        for(int i=0; i<rows.length; ++i) if(ids.contains(data[i][IDCOLUMN])) {
          throw(new IllegalOperationException("Remove operation cannot be performed. Because at least one item is the target"));
        }
        for(int i=0; i<rows.length; ++i) {
          simSearch.mergeMap.get(simSet.type).mergedIntoAssignment.remove(data[rows[i]][IDCOLUMN]);
          simSearch.mergeMap.get(simSet.type).mergedIntoResult.remove(data[rows[i]][IDCOLUMN]);
          simSearch.removeMap.get(simSet.type).add((String) data[rows[i]][IDCOLUMN]);
          this.toRemove[rows[i]] = true;
        }
      }
      return true;
    }
    
    public boolean getRemove(int row) { return this.toRemove[row]; }
    
    public boolean getEffectiveRemove(int row) { 
      return this.toRemove[row] || simSearch.removeMap.get(simSet.type).contains(simSearch.mergeMap.get(simSet.type).mergedIntoResult.get(data[row][IDCOLUMN])); 
    }
    
    public int getMergeTo(int row) { return this.mergeTo[row]; }

    @Override
    public Object getValueAt(int row, int column) {
      return data[row][column];
    }

    @Override 
    public int getColumnCount() { return this.columnCount; }

    @Override 
    public int getRowCount() { return this.rowCount; }



    @Override
    public Class<?> getColumnClass(int columnIndex) { return this.columnClasses[columnIndex]; }

    @Override 
    public String getColumnName(int column) { return this.columnNames[column]; }

    @Override
    public boolean isCellEditable(int row, int column) { return false; }
  }

  private Settings simSearchSettings;
  private List<SimSet> simSetList;
  //private Map<SimSet.SimType, Map<String, DBEntity>> dbData;
  //private Map<String, StationDBEntity> stationData;
  //private Map<String, ProductDBEntity> productData;
  //private Map<Class<? extends DBEntity>, Map<String, DBEntity>> dbData;
  private boolean searchStopped;
  private List<SimSearchListener> simSearchListenerList;
  

  private Map<SimSet.Type, MergeMap> mergeMap;
  private Map<SimSet.Type, Set<String>> removeMap;
  private Map<SimSet, String> alignmentReferenceMap;
  
  private static class RedoUndoAction {
	  private Map<SimSet.Type, MergeMap> mergeMap;
	  private Map<SimSet.Type, Set<String>> removeMap;
	  private Map<SimSet, String> alignmentReferenceMap;
	  
	  RedoUndoAction(Map<SimSet.Type, MergeMap> mergeMap, Map<SimSet.Type, Set<String>> removeMap, Map<SimSet, String> alignmentReferenceMap) {
		  this.mergeMap = mergeMap;
		  this.removeMap = removeMap;
		  this.alignmentReferenceMap = alignmentReferenceMap;
	  }
  }


  public SimSearch() {
    this.simSearchListenerList = new ArrayList<>();
  }



  public void startSearch(Settings settings) {
    if(settings==null) return;
    this.simSetList = Collections.synchronizedList(new ArrayList<SimSet>());
    this.initMaps();
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
  
  private void initMaps() {
    this.removeMap = new HashMap<>();
    this.mergeMap = new HashMap<>();
    for(SimSet.Type simSetType : Arrays.asList(SimSet.Type.class.getEnumConstants())) {
      this.removeMap.put(simSetType, new HashSet<>());
      this.mergeMap.put(simSetType, new MergeMap());
    }
    this.alignmentReferenceMap = new HashMap<>();
  }

  public void mergeInto(SimSet.Type simSetType, List<String> idToMerge, String idToMergeInto) {
	  
  }


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
  
  public SimSearchTableModel loadStationTable(SimSet simSet) {
	  final String[] columnNames = new String[] {"Status", "ID", "Name", "Address", "Country", "Products"};
	  final Class<?>[] columnClasses = new Class<?>[] {Integer.class, String.class, 
		  Alignment.AlignedSequence.class, Alignment.AlignedSequence.class, String.class, List.class};
	 final int columnCount = columnNames.length;
     Object[][] data = new Object[simSet.getIdList().size()][columnCount];
     for(int row=0; row<simSet.getIdList().size(); ++row) {
    	 data[row][0] = 0;
    	 data[row][1] = simSet.idList.get(row);
    	 data[row][2] = RandomDummy.manipulateText("Marmeladen Hersteller", 5);
    	 data[row][3] = RandomDummy.manipulateText("Am Teichgraben 28, 34567 Heuyerswerder", 7);
    	 data[row][4] = RandomDummy.manipulateText("Deutschland", 3);
    	 data[row][5] = Arrays.asList(RandomDummy.getRandomTexts(4, 7));
     }
     return new SimSearchTableModel(this, simSet, data, columnNames, columnClasses);
  }
  
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
    try {
      if(simSet.getType()==SimSet.Type.STATION) {
        final SimSearchTableModel tableModel = loadStationTable(simSet);
        call(l->l.dataLoaded(tableModel, simSetIndex));
      } 
    } catch(Exception err) {
      err.printStackTrace();
      call(l->l.simSearchError(err));
    }
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
    if(this.preprocessDatabase()) {
      if(!this.searchStopped && this.simSearchSettings.getCheckStations()) this.findSimilarStations();
    }
    this.postProcessDatabase();
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

  private void findSimilarStations() {
    for(int iR = 1; iR <= RESULT_NUMBER; iR++) {
      if(this.searchStopped) return; 
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      SimSet simSet = new SimSet(SimSet.Type.STATION);
      for(int iS=1; iS<=STATION_NUMBER; iS++) {
        if(this.searchStopped) return;
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        simSet.addId(RandomDummy.getRandomText(8));
      }
      this.simSetList.add(simSet);
      call(l -> l.newSimSetFound());
    }
  }


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
