/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;

public class SimSearchDataManipulationHandler {
  
  public interface ManipulationStateListener {
    public void manipulationStateChanged(boolean reloadRequired);
  }
  
  private enum ManipulationType {
    Merge, Unmerge, Ignore, Unignore;
  }
  
  public static class MergeMap {
    
    public class MergeException extends Exception {
      
		private static final long serialVersionUID = -583464476829861354L;

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

    private void setMissing(List<Integer> ids) {
    	this.mergeIntoAssignment.keySet().removeAll(ids);
    	this.mergesFromAssignment.keySet().removeAll(ids);

    	for(Integer id: new ArrayList<>(this.mergesFromAssignment.keySet())) {
    		Set<Integer> mergedIds = this.mergesFromAssignment.get(id);
    		mergedIds.removeAll(ids);
    		if(mergedIds.isEmpty()) this.mergesFromAssignment.remove(id);
    	}
    }

    private boolean isEmpty() { return this.mergeIntoAssignment.isEmpty(); }

    public boolean equals(MergeMap mergeMap) {
    	if(this.mergeIntoAssignment.size()!=mergeMap.mergeIntoAssignment.size()) return false;
    	if(!this.mergeIntoAssignment.keySet().containsAll(mergeMap.mergeIntoAssignment.keySet())) return false;
    	for(Entry<Integer, Integer> entry:this.mergeIntoAssignment.entrySet()) if(mergeMap.mergeIntoAssignment.get(entry.getKey())!=entry.getValue()) return false;
    	return true;
    }
  }
  
  public static class IgnoreMap {
    private Set<Set<Integer>> idSetsToIgnore;
    //private Set<SimSearch.SimSet> simSetsToIgnore;
    
    private IgnoreMap() {
      this.idSetsToIgnore = new HashSet<>();
      //this.simSetsToIgnore = new HashSet<>();
    }
  
    private IgnoreMap deepClone() {
      IgnoreMap cloned = new IgnoreMap();
      cloned.idSetsToIgnore =  this.idSetsToIgnore.stream().map(s -> new HashSet<>(s)).collect(Collectors.toSet());
      //cloned.simSetsToIgnore = this.simSetsToIgnore.stream().collect(Collectors.toSet());
      return cloned;
    }
    
//    private boolean isSimSetIgnored(SimSearch.SimSet simSet) {
//      return this.simSetsToIgnore.contains(simSet);
//    }
//  
//    private boolean isSetIgnored(Set<Integer> ids) {
//      for(Set<Integer> setToIgnore: idSetsToIgnore) if(setToIgnore.containsAll(ids)) return true;
//      return false;
//    }
    
//    public void setSimSetIgnored(SimSearch.SimSet simSet, boolean ignore) {
//      if(this.simSetsToIgnore.contains(simSet)) {
//        if(ignore) return;
//        this.simSetsToIgnore.remove(simSet);
//        this.idSetsToIgnore.remove(new HashSet<>(simSet.getIdList()));
//      } else {
//        if(!ignore) return;
//        this.simSetsToIgnore.add(simSet);
//        this.idSetsToIgnore.add(new HashSet<>(simSet.getIdList()));
//      }
//    }
    
    private void ignoreIdSet(Set<Integer> ids) {
      this.idSetsToIgnore.add(ids);
    }
    
    private void setMissing(List<Integer> missingIds) {
      List<Set<Integer>> ignoreSets = new ArrayList<>(this.idSetsToIgnore);
      for(Set<Integer> idSet: ignoreSets) {
        if(!Collections.disjoint(idSet, missingIds)) {
          this.idSetsToIgnore.remove(idSet);
          idSet.removeAll(missingIds);
          if(idSet.size()>1) this.idSetsToIgnore.add(idSet);
        }
      }
    }
    
    private boolean isEmpty() { return this.idSetsToIgnore.isEmpty(); }
    
    public boolean equals(IgnoreMap ignoreMap) {
      if(this.idSetsToIgnore.size()!=ignoreMap.idSetsToIgnore.size()) return false;
      return this.idSetsToIgnore.containsAll(ignoreMap.idSetsToIgnore);
    }
    
    private Set<Integer> getIgnoredIds(Integer referenceId) {
      Set<Integer> idsToIgnore = new HashSet<>();
      for(Set<Integer> idSet: this.idSetsToIgnore) if(idSet.contains(referenceId)) idsToIgnore.addAll(idSet);
      idsToIgnore.remove(referenceId);
      return idsToIgnore;
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
    
    public boolean isEmpty() {
      for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) {
        if(!this.mergeMaps.get(simSetType).isEmpty()) return false;
        if(!this.ignoreMaps.get(simSetType).isEmpty()) return false;
      }
      return true;
    }
    
    public boolean equals(ManipulationState manipulationState) {
      for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) {
        if(!this.mergeMaps.get(simSetType).equals(manipulationState.mergeMaps.get(simSetType))) return false;
        if(!this.ignoreMaps.get(simSetType).equals(manipulationState.ignoreMaps.get(simSetType))) return false;
      }
      return true;
    }
  }
  
  private Stack<ManipulationState> undoStack;
  private Stack<ManipulationState> redoStack;
  private List<ManipulationStateListener> manipulationStateListeners;
  private Map<SimSearch.SimSet, Boolean> cachedIgnoreStatus;
  private Map<SimSearch.SimSet, Boolean> cachedDecisionNeededStatus;
    
  protected SimSearchDataManipulationHandler() {
    this.undoStack = new Stack<>();
    this.redoStack = new Stack<>();
    this.manipulationStateListeners = new ArrayList<>();
    this.cachedIgnoreStatus = new HashMap<>();
    this.cachedDecisionNeededStatus = new HashMap<>();
  }
  
  public void merge(SimSearch.SimSet.Type simSetType, List<Integer> idsToMerge, Integer idToMergeInto) throws MergeMap.MergeException {
    ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Merge):new ManipulationState(ManipulationType.Merge, this.undoStack.peek()));
    for(Integer id: idsToMerge) manipulationState.mergeMaps.get(simSetType).mergeInto(id, idToMergeInto); 
    
    this.undoStack.push(manipulationState);
    this.redoStack.clear();
    this.fireManipulationStateChangedEvent(true);
  }
  
  public void unmerge(SimSearch.SimSet.Type simSetType, List<Integer> idsToUnmerge) throws MergeMap.MergeException {
    ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Unmerge):new ManipulationState(ManipulationType.Unmerge, this.undoStack.peek()));
    MergeMap mergeMap = manipulationState.mergeMaps.get(simSetType);
    
    Set<Integer> mergeContainerIds = Sets.intersection(mergeMap.mergesFromAssignment.keySet(), new HashSet<>(idsToUnmerge));
    Set<Integer> containedIds = new HashSet<>();
    for(Integer id: mergeContainerIds) containedIds.addAll(mergeMap.mergesFromAssignment.get(id));
    idsToUnmerge.removeAll(containedIds); // because they don't need to be unmerged twice
    for(Integer id: idsToUnmerge) mergeMap.unmerge(id); 
    
    this.undoStack.push(manipulationState);
    this.redoStack.clear();
    this.fireManipulationStateChangedEvent(true);
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
      this.fireManipulationStateChangedEvent(true);
    }
  }

  public void redo() {
    if(!this.redoStack.isEmpty()) {
      this.undoStack.push(this.redoStack.pop());
      this.fireManipulationStateChangedEvent(true);
    }
  }
  
  public void fireManipulationStateChangedEvent(boolean reloadRequired) { 
    this.cachedIgnoreStatus = new HashMap<>();
    this.cachedDecisionNeededStatus = new HashMap<>();
    for(ManipulationStateListener listener: this.manipulationStateListeners) listener.manipulationStateChanged(reloadRequired); 
  }

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
          return "Undo merge operation.";
        case Unmerge:
          return "Undo unmerge operation.";
        case Ignore:
          return "Undo ignore operation.";
        case Unignore:
          return "Undo unignore operation.";
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
          return "Redo merge operation.";
        case Unmerge:
          return "Redo unmerge operation.";
        case Ignore:
          return "Redo ignore operation.";
        case Unignore:
          return "Redo unignore operation.";
        default:
          return "Redo operation.";
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
  
  public void registerDataOperationListener(ManipulationStateListener listener) {
    this.manipulationStateListeners.add(listener);
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
  
//  public boolean isSimSetIgnored(SimSearch.SimSet simSet) {
//    if(this.undoStack.isEmpty()) return false;
//    return this.undoStack.peek().ignoreMaps.get(simSet.getType()).isSimSetIgnored(simSet);
//  }
  
//  public boolean isDecisionOpen(SimSearch.SimSet simSet) {
//    if(this.undoStack.isEmpty()) {
//      return simSet.getIdList().size()>1;
//    } else {
//      if(this.isSimSetIgnored(simSet)) return false;
//      Set<Integer> ids = new HashSet<>(Sets.difference(new HashSet<>(simSet.getIdList()), this.undoStack.peek().mergeMaps.get(simSet.getType()).mergeIntoAssignment.keySet()));
//      return this.undoStack.peek().ignoreMaps.get(simSet.getType()).isSetIgnored(ids);
//    }
//  }
  
  public Map<Integer,Integer> getMergeMap(SimSearch.SimSet.Type simSetType) {
	  if(this.undoStack.isEmpty()) return new HashMap<>();
	  else return this.undoStack.peek().mergeMaps.get(simSetType).mergeIntoAssignment.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
  
  /**
   * get the ignore map for a specified simset type from user changes
   * <p>
   * Please note: The map is not symmetric, which means:
   * If map.get(id1).contains(id2) it does not follow that map.get(id2).contains(id1)
   * In this sense the map is not complete
   * 
   * @return :  map: id of an item -> ids of items the key item was marked to be different to
   */
  public Map<Integer,Set<Integer>> getIgnoreMap(SimSearch.SimSet.Type simSetType) {
	  if(this.undoStack.isEmpty()) {
		  return new HashMap<>(); 
	  } else {
		  Map<Integer,Set<Integer>> result = new HashMap<>();
		  
		  for(Set<Integer> idSet : this.undoStack.peek().ignoreMaps.get(simSetType).idSetsToIgnore) {
		    Set<Integer> ignoreIds = new HashSet<>(idSet);
		    
		    for(int id: idSet) {
		    
		      ignoreIds.remove(id);
		      
		      if(!ignoreIds.isEmpty()) {
		        if(!result.containsKey(id)) result.put(id, new HashSet<>());
		        result.get(id).addAll(ignoreIds);
		      }
		    }
		  }

		  return result;
	  }
  }
  
  public void clearManipulations() {
	  this.redoStack.clear();
	  if(!this.undoStack.isEmpty()) {
		  this.undoStack.clear();
		  this.fireManipulationStateChangedEvent(false);
	  }
  }
  
  public boolean isSimSetIgnored(SimSearch.SimSet simSet) {
    if(this.cachedIgnoreStatus.containsKey(simSet)) return this.cachedIgnoreStatus.get(simSet);
    boolean result;
    if(this.undoStack.isEmpty()) {
      result = simSet.getIdList().size()==1 || !simSet.getIdList().contains(simSet.getReferenceId());
    } else {
      MergeMap mergeMap = this.undoStack.peek().mergeMaps.get(simSet.getType());
      if(mergeMap.isMerged(simSet.getReferenceId()) || 
          (mergeMap.mergesFromAssignment.containsKey(simSet.getReferenceId()) && 
              !(Sets.intersection(mergeMap.mergesFromAssignment.get(simSet.getReferenceId()), new HashSet<>(simSet.getIdList())).isEmpty()))) {
        result = false;
      } else {
        Set<Integer> ids = getIgnorableIdsFromSimSet(simSet);
        result = ids.isEmpty();
      }
    }
    this.cachedIgnoreStatus.put(simSet,  result);
    return result;
  }
  
  public boolean isDecisionNeededForSimSet(SimSearch.SimSet simSet) {
   if(this.cachedDecisionNeededStatus.containsKey(simSet)) return this.cachedDecisionNeededStatus.get(simSet);
   
   boolean result = true;
   
   if(isSimSetIgnored(simSet)) {
     // the simset is already ignored
     result = false;
   } else if(this.undoStack.isEmpty()) {
     // only one entry left in the list or the reference id is not anymore in the list
     result = !(simSet.getIdList().size()==1 || !simSet.getIdList().contains(simSet.getReferenceId()));
   } else {
     MergeMap mergeMap = this.undoStack.peek().mergeMaps.get(simSet.getType());
     
     if(mergeMap.isMerged(simSet.getReferenceId())) {
       // reference was already merged
       result = false;
     } else {
       IgnoreMap ignoreMap = this.undoStack.peek().ignoreMaps.get(simSet.getType());
       Set<Integer> mergedIds = mergeMap.mergesFromAssignment.get(simSet.getReferenceId());
       if(mergedIds==null) mergedIds = new HashSet<>();
       Set<Integer> ignoredIds = ignoreMap.getIgnoredIds(simSet.getReferenceId());
       if(ignoredIds==null) ignoredIds = new HashSet<>();
       result = Sets.difference(new HashSet<>(simSet.getIdList()), Sets.union(mergedIds, ignoredIds)).size()>1; //>1 because the reference id itself is in the set
     }
   }
   this.cachedDecisionNeededStatus.put(simSet,  result);
   return result;
  }
  
  public boolean isSimSetIgnoreAvailable(SimSearch.SimSet simSet) {
    if(this.undoStack.isEmpty()) return !(simSet.getIdList().size()==1 || !simSet.getIdList().contains(simSet.getReferenceId()));
    Set<Integer> ids = getIgnorableIdsFromSimSet(simSet);
    return !ids.isEmpty();
  }
  
  public void ignoreSimSet(SimSearch.SimSet simSet) {
    if(!this.isSimSetIgnored(simSet) && this.isSimSetIgnoreAvailable(simSet)) {
      ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Ignore):new ManipulationState(ManipulationType.Ignore, this.undoStack.peek()));
      Set<Integer> ids = getIgnorableIdsFromSimSet(simSet);
      
      for(Integer id: ids) manipulationState.ignoreMaps.get(simSet.getType()).ignoreIdSet(new HashSet<>(Arrays.asList(id, simSet.getReferenceId())));
      
      this.undoStack.push(manipulationState);
      this.redoStack.clear();
      this.fireManipulationStateChangedEvent(true);
    }
  }
  
  public void ignoreAllPairsInSimSet(SimSearch.SimSet simSet) {
    if(!this.isSimSetIgnored(simSet) && this.isSimSetIgnoreAvailable(simSet)) {
      ManipulationState manipulationState = (undoStack.isEmpty()?new ManipulationState(ManipulationType.Ignore):new ManipulationState(ManipulationType.Ignore, this.undoStack.peek()));
      Set<Integer> ids = getIgnorableIdsFromSimSet(simSet);
      ids.add(simSet.getReferenceId());
      manipulationState.ignoreMaps.get(simSet.getType()).ignoreIdSet(ids);
      
      this.undoStack.push(manipulationState);
      this.redoStack.clear();
      this.fireManipulationStateChangedEvent(true);
    }
  }
 
  private Set<Integer> getIgnorableIdsFromSimSet(SimSearch.SimSet simSet) {
    Set<Integer> ids = new HashSet<>(simSet.getIdList());
    ids.remove(simSet.getReferenceId());
    if(!undoStack.isEmpty()) {
      MergeMap mergeMap = this.undoStack.peek().mergeMaps.get(simSet.getType());
      ids.removeAll(mergeMap.mergeIntoAssignment.keySet());
      if(mergeMap.isMerged(simSet.getReferenceId())) ids.remove(mergeMap.getMergeAssignment(simSet.getReferenceId()));
      
      //ignoreMap
      IgnoreMap ignoreMap = this.undoStack.peek().ignoreMaps.get(simSet.getType());
      for(Set<Integer> idSet: ignoreMap.idSetsToIgnore) if(idSet.contains(simSet.getReferenceId())) ids.removeAll(idSet);
    }
    return ids;
  }
  
  public boolean isEmpty() {
	  return this.undoStack.isEmpty();
  }
  
  public void setMissing(SimSearch.SimSet.Type simSetType, List<Integer> missingIds) {
    this.updateUndoRedoStacks(simSetType, missingIds);
    this.fireManipulationStateChangedEvent(false);
  }
  
  private void updateUndoRedoStacks(SimSearch.SimSet.Type simSetType, List<Integer> missingIds) {
    // update manipulationStates according to missing Ids 
    for(ManipulationState manipulationState: this.undoStack) {
      MergeMap mergeMap = manipulationState.mergeMaps.get(simSetType);
      mergeMap.setMissing(missingIds);
      IgnoreMap ignoreMap = manipulationState.ignoreMaps.get(simSetType);
      ignoreMap.setMissing(missingIds);
    }
    
    Arrays.asList(this.undoStack, this.redoStack).forEach(stack -> {
      for(int i=stack.size(); i>=1; --i) if(stack.get(i).equals(stack.get(i-1))) stack.remove(i);});
    
    if(this.undoStack.size()==1 && this.undoStack.peek().isEmpty()) this.undoStack.clear();
    
    if(!this.undoStack.isEmpty() && 
        !this.redoStack.isEmpty() && this.undoStack.peek().equals(this.redoStack.peek())) this.redoStack.pop();
      
  }
  
  public Set<Integer> getIgnoreList(SimSearch.SimSet simSet) {
    if(this.undoStack.isEmpty()) return new HashSet<>();
    
    return this.undoStack.peek().ignoreMaps.get(simSet.getType()).getIgnoredIds(simSet.getReferenceId());
  }
}
