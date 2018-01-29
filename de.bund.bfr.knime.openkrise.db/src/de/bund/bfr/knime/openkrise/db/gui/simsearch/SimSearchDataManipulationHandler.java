package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.Sets;

public class SimSearchDataManipulationHandler {
  
  public interface DataOperationListener {
    public void DataManipulationOccured();
  }
  
  private enum ManipulationType {
    Merge, Unmerge, Ignore, Unignore;
  }
  
  public static class MergeMap {
    
    public class MergeException extends Exception {
      private MergeException(String message) {
        super(message);
      }
    }
    
    private Map<Integer,Integer> mergeIntoAssignment;  //oldId -> newId
    private Map<Integer, Set<Integer>> mergesFromAssignment;     //newId -> {oldIds, ...}

    public MergeMap() {
      this.mergeIntoAssignment = new HashMap<>();
      this.mergesFromAssignment = new HashMap<>();
    }

    public Integer getMergeAssignment(Integer id) { return this.mergeIntoAssignment.get(id); }

    public void mergeInto(Integer idToMerge, Integer idToMergeInto) throws MergeException {
      if(mergeIntoAssignment.containsKey(idToMergeInto)) throw(new MergeException("Cascading merges are not allowed."));
      if(mergeIntoAssignment.containsKey(idToMerge)) throw(new MergeException("ID is already merged."));

      mergeIntoAssignment.put(idToMerge, idToMergeInto);
      if(!mergesFromAssignment.containsKey(idToMergeInto)) this.mergesFromAssignment.put(idToMergeInto, new HashSet<>());
      mergesFromAssignment.get(idToMergeInto).add(idToMerge);
    }
    
    public boolean isMerged(Integer id) {
      return this.mergeIntoAssignment.containsKey(id);
    }
    
    public void unmerge(Integer id) throws MergeException {
      if(mergeIntoAssignment.containsKey(id)) {
        // this is a merge source
        Integer targetId = mergeIntoAssignment.get(id);
        this.mergeIntoAssignment.remove(id);
        this.mergesFromAssignment.get(targetId).remove(id);
        if(this.mergesFromAssignment.get(targetId).isEmpty()) this.mergesFromAssignment.remove(targetId);
        
      } else if (mergesFromAssignment.containsKey(id)) {
        // this is a merge target
        for(Integer mergeSourceId: mergesFromAssignment.get(id)) this.mergeIntoAssignment.remove(mergeSourceId);
        mergesFromAssignment.remove(id);
      } else {
        throw(new MergeException("Merge was not found."));
      }
    }
    
    public int getMergeCount(Integer mergeTragetId) {
      return (this.mergesFromAssignment.containsKey(mergeTragetId)?this.mergesFromAssignment.get(mergeTragetId).size():0);
    }
    
    private MergeMap deepClone() {
      MergeMap cloned = new MergeMap();
      cloned.mergeIntoAssignment =  this.mergeIntoAssignment.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      cloned.mergesFromAssignment = this.mergesFromAssignment.entrySet().stream().collect(Collectors.toMap( e->e.getKey(), e -> new HashSet<>(e.getValue())));
      return cloned;
    }
  }
  
  public static class IgnoreMap {
    private Set<Set<Integer>> idSetsToIgnore;
    private Set<SimSearch.SimSet> simSetsToIgnore;
    
    private IgnoreMap() {
      this.idSetsToIgnore = new HashSet<>();
      this.simSetsToIgnore = new HashSet<>();
    }
  
    private IgnoreMap deepClone() {
      IgnoreMap cloned = new IgnoreMap();
      cloned.idSetsToIgnore =  this.idSetsToIgnore.stream().map(s -> new HashSet<>(s)).collect(Collectors.toSet());
      cloned.simSetsToIgnore = this.simSetsToIgnore.stream().collect(Collectors.toSet());
      return cloned;
    }
    
    private boolean isSimSetIgnored(SimSearch.SimSet simSet) {
      return this.simSetsToIgnore.contains(simSet);
    }
  
    private boolean isSetIgnored(Set<Integer> ids) {
      for(Set<Integer> setToIgnore: idSetsToIgnore) if(setToIgnore.containsAll(ids)) return true;
      return false;
    }
    
    public void setSimSetIgnored(SimSearch.SimSet simSet, boolean ignore) {
      if(this.simSetsToIgnore.contains(simSet)) {
        if(ignore) return;
        this.simSetsToIgnore.remove(simSet);
        this.idSetsToIgnore.remove(new HashSet<>(simSet.getIdList()));
      } else {
        if(!ignore) return;
        this.simSetsToIgnore.add(simSet);
        this.idSetsToIgnore.add(new HashSet<>(simSet.getIdList()));
      }
    }
  }
  
  private static class ManipulationState {
    private Map<SimSearch.SimSet.Type, MergeMap> mergeMaps;
    private Map<SimSearch.SimSet.Type, IgnoreMap> ignoreMaps;
    private ManipulationType typeOfLastManipulation;
    
    private ManipulationState(ManipulationType typeOfManipulation, ManipulationState manipulationState) {
      this.typeOfLastManipulation = typeOfManipulation;
      this.mergeMaps = new HashMap<>();
      this.ignoreMaps = new HashMap<>();
      for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) {
        this.mergeMaps.put(simSetType, manipulationState.mergeMaps.get(simSetType).deepClone());
        this.ignoreMaps.put(simSetType, manipulationState.ignoreMaps.get(simSetType).deepClone());
      }
    }

    public ManipulationState(ManipulationType typeOfManipulation) {
      this.typeOfLastManipulation = typeOfManipulation;
      this.mergeMaps = new HashMap<>();
      this.ignoreMaps = new HashMap<>();
      for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) {
        this.mergeMaps.put(simSetType, new MergeMap());
        this.ignoreMaps.put(simSetType, new IgnoreMap());
      }
    }
  }
  
  private Stack<ManipulationState> undoStack;
  private Stack<ManipulationState> redoStack;
  private List<DataOperationListener> dataOperationListeners;
    
  protected SimSearchDataManipulationHandler() {
    this.undoStack = new Stack<>();
    this.redoStack = new Stack<>();
    this.dataOperationListeners = new ArrayList<>();
  }
  
  public void merge(SimSearch.SimSet.Type simSetType, List<Integer> idsToMerge, Integer idToMergeInto) throws MergeMap.MergeException {
    ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Merge):new ManipulationState(ManipulationType.Merge, this.undoStack.peek()));
    for(Integer id: idsToMerge) manipulationState.mergeMaps.get(simSetType).mergeInto(id, idToMergeInto); 
    
    this.undoStack.push(manipulationState);
    this.redoStack.clear();
    this.informListeners();
  }
  
  public void unmerge(SimSearch.SimSet.Type simSetType, List<Integer> idsToUnmerge) throws MergeMap.MergeException {
    ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Unmerge):new ManipulationState(ManipulationType.Unmerge, this.undoStack.peek()));
    for(Integer id: idsToUnmerge) manipulationState.mergeMaps.get(simSetType).unmerge(id); 
    
    this.undoStack.push(manipulationState);
    this.redoStack.clear();
    this.informListeners();
  }
  
  public Integer getMergedInto(SimSearch.SimSet.Type simSetType, Integer id) {
    return (this.undoStack.empty()?null:this.undoStack.peek().mergeMaps.get(simSetType).getMergeAssignment(id));
  }
  
  public boolean isMerged(SimSearch.SimSet.Type simSetType, Integer id) {
    return (this.undoStack.empty()?false:this.undoStack.peek().mergeMaps.get(simSetType).isMerged(id));
  }
  
  public void undo() {
    if(!this.undoStack.isEmpty()) {
      this.redoStack.push(this.undoStack.pop());
      this.informListeners();
    }
  }

  public void redo() {
    if(!this.redoStack.isEmpty()) {
      this.undoStack.push(this.redoStack.pop());
      this.informListeners();
    }
  }
  
  public void informListeners() { for(DataOperationListener listener: this.dataOperationListeners) listener.DataManipulationOccured(); }

  public boolean isUndoAvailable() {
    return !this.undoStack.isEmpty();
  }

  public boolean isRedoAvailable() {
    return !this.redoStack.isEmpty();
  }

  public String getUndoType() {
    if(this.undoStack.isEmpty()) {
      return "";
    } else {
      switch(this.undoStack.peek().typeOfLastManipulation) {
        case Merge:
          return "Undo merge.";
        case Unmerge:
          return "Undo unmerge.";
        case Ignore:
          return "Undo ignore.";
        case Unignore:
          return "Undo unignore.";
        default:
          return "Undo operation.";
      }
    }
  }

  public String getRedoType() {
    if(this.redoStack.isEmpty()) {
      return "";
    } else {
      switch(this.redoStack.peek().typeOfLastManipulation) {
        case Merge:
          return "Redo merge.";
        case Unmerge:
          return "Redo unmerge.";
        case Ignore:
          return "Redo ignore.";
        case Unignore:
          return "Redo unignore.";
        default:
          return "Redo.";
      }
    }
  }
  
  public int getMergeCount(SimSearch.SimSet.Type simSetType, Integer id) {
    if(this.undoStack.isEmpty()) {
      return 0;
    } else {
      return this.undoStack.peek().mergeMaps.get(simSetType).getMergeCount(id);
    }
  }
  
  public void registerDataOperationListener(DataOperationListener listener) {
    this.dataOperationListeners.add(listener);
  }
  
  public Map<SimSearch.SimSet.Type, Map<Integer, Integer>> getMergeMap() {
    Map<SimSearch.SimSet.Type, MergeMap> mergeMaps = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Merge).mergeMaps: undoStack.peek().mergeMaps);
    Map<SimSearch.SimSet.Type, Map<Integer, Integer>> resultMap = new HashMap<>();
    for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) 
      resultMap.put(simSetType, mergeMaps.get(simSetType).mergeIntoAssignment.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    return resultMap;    
  }
  
//  public void ignoreSimSet(SimSearch.SimSet simSet) {
//    ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Ignore):new ManipulationState(ManipulationType.Ignore, this.undoStack.peek()));
//    manipulationState.ignoreMaps.get(simSet.getType()).setSimSetIgnored(simSet,true); 
//    
//    this.undoStack.push(manipulationState);
//    this.redoStack.clear();
//    this.informListeners();
//  }
//  
//  public void unignore(SimSearch.SimSet simSet) {
//    ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Unignore):new ManipulationState(ManipulationType.Unignore, this.undoStack.peek()));
//    manipulationState.ignoreMaps.get(simSet.getType()).setSimSetIgnored(simSet,false); 
//    
//    this.undoStack.push(manipulationState);
//    this.redoStack.clear();
//    this.informListeners();
//  }
  
  public boolean isSimSetIgnored(SimSearch.SimSet simSet) {
    if(this.undoStack.isEmpty()) return false;
    return this.undoStack.peek().ignoreMaps.get(simSet.getType()).isSimSetIgnored(simSet);
  }
  
  public boolean isDecisionOpen(SimSearch.SimSet simSet) {
    if(this.undoStack.isEmpty()) {
      return simSet.getIdList().size()>1;
    } else {
      if(this.isSimSetIgnored(simSet)) return false;
      Set<Integer> ids = new HashSet<>(Sets.difference(new HashSet<>(simSet.getIdList()), this.undoStack.peek().mergeMaps.get(simSet.getType()).mergeIntoAssignment.keySet()));
      return this.undoStack.peek().ignoreMaps.get(simSet.getType()).isSetIgnored(ids);
    }
  }
  
  public Map<Integer,Integer> getMergeMap(SimSearch.SimSet.Type simSetType) {
	  if(this.undoStack.isEmpty()) return new HashMap<>();
	  else return this.undoStack.peek().mergeMaps.get(simSetType).mergeIntoAssignment.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
  
  public Map<Integer,List<Integer>> getIgnoreMap(SimSearch.SimSet.Type simSetType) {
	  if(this.undoStack.isEmpty()) {
		  return new HashMap<>(); 
	  } else {
		  Map<Integer,List<Integer>> result = new HashMap<>();
		  MergeMap mergeMap = this.undoStack.peek().mergeMaps.get(simSetType);
		  for(SimSearch.SimSet simSet: this.undoStack.peek().ignoreMaps.get(simSetType).simSetsToIgnore) {
			  if(simSet.getIdList().contains(simSet.getReferenceId()) && !mergeMap.isMerged(simSet.getReferenceId())) {
				  result.put(simSet.getReferenceId(), simSet.getIdList().stream().filter(id -> !mergeMap.isMerged(id) && !id.equals(simSet.getReferenceId())).collect(Collectors.toList()));
			  }
		  }
		  return result;
	  }
  }
  
  public void clearManipulations() {
	  this.redoStack.clear();
	  if(!this.undoStack.isEmpty()) {
		  this.undoStack.clear();
		  this.informListeners();
	  }
  }
  
  public void setSimSetIgnored(SimSearch.SimSet simSet, boolean value) {
	  if(value!=this.isSimSetIgnored(simSet)) {
		  ManipulationType manipulationType = (value?ManipulationType.Ignore:ManipulationType.Unignore);
		  ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(manipulationType):new ManipulationState(manipulationType, this.undoStack.peek()));
		  manipulationState.ignoreMaps.get(simSet.getType()).setSimSetIgnored(simSet,value); 

		  this.undoStack.push(manipulationState);
		  this.redoStack.clear();
		  this.informListeners();
	  }
  }
  
  public boolean isEmpty() {
	  return this.undoStack.isEmpty();
  }
}
