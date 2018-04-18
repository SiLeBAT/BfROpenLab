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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import de.bund.bfr.knime.openkrise.common.DeliveryUtils;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearch.SimSet.Type;

public final class SimSearch {
  
  // Setting object to store the search settings
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
    private boolean ignoreKnownDissimilarities;

    public Settings() {
      this.attributeTresholdMap = new HashMap<>();
      for(Attribute a: Attribute.class.getEnumConstants()) this.attributeTresholdMap.put(a, a.getDefaultTreshold());
      this.checkList = new HashSet<>();
      this.useLevenshtein = false;
      this.useFormat2017 = !DeliveryUtils.hasOnlyPositiveIDs(DBKernel.getLocalConn(true));
      this.readOnly = false;
      this.ignoreKnownDissimilarities = false;
    }
    
    public Settings(Settings settingsToCopyFrom) {
      this.attributeTresholdMap = settingsToCopyFrom.attributeTresholdMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      this.checkList = settingsToCopyFrom.checkList.stream().collect(Collectors.toSet());
      
      this.useLevenshtein = settingsToCopyFrom.useLevenshtein;
      this.useFormat2017 = settingsToCopyFrom.useFormat2017;
      this.ignoreKnownDissimilarities = settingsToCopyFrom.ignoreKnownDissimilarities;
      this.readOnly = false;
    }

    public int getTreshold(Attribute attribute) { return this.attributeTresholdMap.get(attribute); }
    public void setTreshold(Attribute attribute, int treshold) { if(!readOnly) this.attributeTresholdMap.put(attribute, treshold); }
    
    public boolean isChecked(SimSet.Type simSetType) { return this.checkList.contains(simSetType); }
    public void setChecked(SimSet.Type simSetType, boolean isChecked) {
      if(!this.readOnly) {
    	  if(isChecked) this.checkList.add(simSetType);
    	  else this.checkList.remove(simSetType);
      }
    }
    
    public void freeze() { this.readOnly = true; }
    public boolean isReadOnly() { return this.readOnly; }
    
    public boolean getUseAllInOneAddress() { return this.useFormat2017; }
    public boolean getUseArrivedDate() { return this.useFormat2017; }
    
    public boolean getUseLevenshtein() { return this.useLevenshtein; }
    public void setUseLevenshtein(boolean value) { if(!this.readOnly) this.useLevenshtein=value; }
       
    public boolean getIgnoreKnownDissimilarities() { return this.ignoreKnownDissimilarities; }
    public void setIgnoreKnownDissimilarities(boolean value) { if(!this.readOnly) this.ignoreKnownDissimilarities = value; }
  }

  public interface SimSearchListener {
    public void newSimSetFound();
  }


  public static class SimSet {
	  public static enum Type {
	      STATION(0), PRODUCT(1), LOT(2), DELIVERY(3);
		  
		  private final int value;
		  
		  Type(int value) {
			  this.value = value;
		  }
		  
		  public int getValue() { return this.value; }
	  }

    private Type type;
    private List<Integer> idList;
    private Integer referenceId;
    
    private SimSet(Type type, List<Integer> idList) throws Exception {
      this.type = type;
      if(idList==null || idList.isEmpty()) throw(new Exception("The parameter idList may not be null or empty."));
      this.referenceId = idList.get(1);
      this.idList = Collections.unmodifiableList(idList);
    }
    
    public Type getType() { return this.type; }
    public List<Integer> getIdList() { return this.idList; }
    public Integer getReferenceId() { return this.referenceId; }
    

    public void setMissing(List<Integer> missingIds) {
      List<Integer> newIds = new ArrayList<>(this.idList);
      newIds.removeAll(missingIds);
      this.idList = Collections.unmodifiableList(newIds);
    }
  }

  // SimSearch required a data source which provides search the search results and an interface to load the corresponding data
  protected static abstract class DataSource {
    public interface DataSourceListener {
      public void similaritiesFound(SimSet.Type simSetType, List<Integer> idList) throws Exception;
      public void missingIdsDetected(SimSet.Type simSetType, List<Integer> idList);
    }
    
    // Class for foreign fields in db tables
    protected static class ForeignField {
    	private int id;
    	private String label;
    	private ForeignField(Integer id, String label) {
    		this.id = id;
    		this.label = label;
    	}
    	public int getId() { return this.id; }
    	public String getLabel() { return this.label; }
    	public void setLabel(String value) { this.label = value; }
    	@Override
    	public String toString() { return this.label; }
    }
    
    // class to load the data
    protected static abstract class DataLoader {
    	ForeignField createForeignField(Integer id, String label) { return new ForeignField(id, label); }
    	public abstract void loadData() throws Exception;
    	public String[] columnNames;
    	public Class<?>[] columnClasses;
    	public Object[][] data;
    	public String[] columnComments;
    	public String[] columnFormatComments;
    	public int rowCount;
    	public int columnCount;
    }
    
    DataSourceListener listener;
    private volatile boolean searchStopped;
    
    protected DataSource(DataSourceListener listener) {
      this.listener = listener;
      this.searchStopped = false;
    }
    
    public abstract void findSimilarities(Settings settings) throws Exception;
    
    public abstract SimSearchDataLoader createDataLoader(SimSet simSet, SimSearchDataManipulationHandler dataManipulationsHandler);
    
    public abstract boolean save(SimSearchDataManipulationHandler dataManipulations) throws Exception;
    
    public final void stopSearch() {
      this.searchStopped = true;
    }
    
    public final boolean getSearchStopped() { return this.searchStopped; }
    
    final void newSimilarityMatchFound(SimSet.Type simSetType, List<Integer> idList) throws Exception {
      if(listener!=null) this.listener.similaritiesFound(simSetType, idList);
    }
  }
  
  private Settings simSearchSettings;
  private List<SimSet> simSetList;
  private List<SimSearchListener> simSearchListenerList;
  private SimSearchDataManipulationHandler dataManipulationHandler;
  private DataSource dataSource;


  public SimSearch() {
    this.simSearchListenerList = new ArrayList<>();
    this.dataSource = new SimSearchDataSource(new DataSource.DataSourceListener() {
      
      @Override
      public void similaritiesFound(Type simSetType, List<Integer> idList) throws Exception {
        
          SimSearch.this.simSetList.add(new SimSearch.SimSet(simSetType, idList));
          SimSearch.this.call(l -> l.newSimSetFound());
        
      }

      @Override
      public void missingIdsDetected(Type simSetType, List<Integer> missingIds) {
        for(int i=0; i<SimSearch.this.simSetList.size(); ++i) {
          SimSearch.SimSet simSet = SimSearch.this.simSetList.get(i);
          if(simSet.getType().equals(simSetType)) simSet.setMissing(missingIds);
        }
        SimSearch.this.dataManipulationHandler.setMissing(simSetType, missingIds);
      }
    });
    this.dataManipulationHandler = new SimSearchDataManipulationHandler();
  }
  
  public void registerManipulationStateListener(SimSearchDataManipulationHandler.ManipulationStateListener listener) {
    this.dataManipulationHandler.registerDataOperationListener(listener);
  }

  public boolean search(Settings settings) throws Exception {
    if(settings==null) return false;
    settings.freeze();
    this.simSetList = Collections.synchronizedList(new ArrayList<SimSet>());
    //this.searchStopped = false;
    this.simSearchSettings = settings;
    this.dataSource.findSimilarities(this.simSearchSettings);
    return true;
  }
  
  public boolean save() throws Exception {
    if(this.dataSource!=null) {
    	if(this.dataSource.save(this.dataManipulationHandler)) {
    		// remove discarded simsets
    		this.simSetList.removeIf(simSet -> this.dataManipulationHandler.isSimSetIgnored(simSet));
    		
    		// remove merge ids from simsets
    		for(SimSearch.SimSet simSet: this.simSetList) simSet.setMissing(simSet.getIdList().stream().filter(id -> this.dataManipulationHandler.isMerged(simSet.getType(), id)).collect(Collectors.toList()));        //getIdList().removeIf(id -> this.dataManipulationHandler.isMerged(simSet.getType(), id));
    		
    		// remove simsets which are cleared
    		this.simSetList.removeIf(simSet -> simSet.idList.size()<=1 || !simSet.idList.contains(simSet.referenceId));
    		
    		this.dataManipulationHandler.clearManipulations();
    		return true;
    	}
    }
    return false;
  }

  public int getSimSetCount() { return (this.simSetList==null?0:this.simSetList.size()); }
  
  public int getSimSetIndex(SimSet simSet) { return (this.simSetList==null?-1:this.simSetList.indexOf(simSet)); }
  
  public int getNotIgnoredSimSetIndex(int totalIndex) {
    int index = -1;
    for(int i=totalIndex; i>=0; --i) if(!dataManipulationHandler.isSimSetIgnored(this.simSetList.get(i))) ++index;
    
    return index;
  }
  
  public boolean isSimSetIgnored(int index) { return dataManipulationHandler.isSimSetIgnored(this.simSetList.get(index)); }
  
  public int getIndexOfNextNotIgnoredSimSet(int totalIndex) {
    //int index = -1;
    int n = this.simSetList.size();
    for(int i=totalIndex+1; i<n; ++i) if(!dataManipulationHandler.isSimSetIgnored(this.simSetList.get(i))) return i;
    for(int i=0; i<totalIndex; ++i) if(!dataManipulationHandler.isSimSetIgnored(this.simSetList.get(i))) return i;
    if(totalIndex>=0 && dataManipulationHandler.isSimSetIgnored(this.simSetList.get(totalIndex))) return -1;
    else return totalIndex;
  }
  
  public int getIndexOfPreviousNotIgnoredSimSet(int totalIndex) {
    //int index = -1;
    int n = this.simSetList.size();
    for(int i=totalIndex-1; i>=0; --i) if(!dataManipulationHandler.isSimSetIgnored(this.simSetList.get(i))) return i;
    for(int i=n-1; i>totalIndex; --i) if(!dataManipulationHandler.isSimSetIgnored(this.simSetList.get(i))) return i;
    if(totalIndex>=0 && dataManipulationHandler.isSimSetIgnored(this.simSetList.get(totalIndex))) return -1;
    else return totalIndex;
  }
  
  public SimSet getSimSet(int index) { return (this.simSetList==null || this.simSetList.size()<=index?null:this.simSetList.get(index)); }
  
  public SimSearchTableModel loadData(int simSetIndex) throws Exception {
    SimSet simSet = this.simSetList.get(simSetIndex);
    if(simSet==null) throw(new Exception("Simset was not found"));
    
    SimSearch.DataSource.DataLoader dataLoader = this.dataSource.createDataLoader(simSet, dataManipulationHandler);
   
	dataLoader.loadData();
	return new SimSearchTableModel(simSet, dataManipulationHandler, dataLoader);
  }
  
  public boolean existDataManipulations() {
	  return (this.dataManipulationHandler==null?false:!this.dataManipulationHandler.isEmpty());
  }
  
  public boolean isUndoAvailable() { return this.dataManipulationHandler.isUndoAvailable(); }
  public boolean isRedoAvailable() { return this.dataManipulationHandler.isRedoAvailable(); }
  
  public String getUndoType() { return this.dataManipulationHandler.getUndoType(); }
  public String getRedoType() { return this.dataManipulationHandler.getRedoType(); }
  
  public void redo() { this.dataManipulationHandler.redo(); }
  public void undo() { this.dataManipulationHandler.undo(); }

  private void call(Consumer<SimSearchListener> action) {
    this.simSearchListenerList.forEach(action);
  }

  public void stopSearch() {
	  if(this.dataSource!=null) this.dataSource.stopSearch();
  }
  
  public void addEventListener(SimSearchListener listener) {
    this.simSearchListenerList.add(listener);
  }

  public void removeEventListener(SimSearchListener listener) {
    this.simSearchListenerList.remove(listener);
  }
}
