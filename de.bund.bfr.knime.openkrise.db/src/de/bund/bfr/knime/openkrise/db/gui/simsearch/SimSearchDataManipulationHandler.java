package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.stream.Collectors;

public class SimSearchDataManipulationHandler {
  
  private enum ManipulationType {
    Merge, Unmerge;
  }
  
  private static class DataManipulationSet {
    
    public static class MergeMap implements Cloneable {
      public class MergeException extends Exception {
        private MergeException(String message) {
          super(message);
        }
      }
      private Map<String,String> mergedIntoAssignment;
      private Map<String,String> mergedIntoResult;

      public MergeMap() {
        this.mergedIntoAssignment = new HashMap<>();
        this.mergedIntoResult = new HashMap<>();
      }

      //public String getMergeResult(String id) { return this.mergedIntoResult.get(id); }
      public String getMergeAssignment(String id) { return this.mergedIntoAssignment.get(id); }

      public void mergeInto(String idToMerge, String idToMergeInto) throws MergeException {
        if(mergedIntoAssignment.containsKey(idToMergeInto)) throw(new MergeException("Cascading merges are not allowed."));
        if(mergedIntoAssignment.containsKey(idToMerge)) throw(new MergeException("ID is already merged."));

        mergedIntoAssignment.put(idToMerge, idToMergeInto);
//        mergedIntoResult.entrySet().stream().filter(e -> idToMerge.equals(e.getValue())).forEach(e -> {
//          mergedIntoResult.put(e.getKey(), idToMergeInto);
//        });
      }
      
      public void unmerge(String id) throws MergeException {
        if(mergedIntoAssignment.containsKey(id)) {
          this.mergedIntoAssignment.remove(id);
        } else if (mergedIntoAssignment.containsValue(id)) {
          // ToDo: Improve this be reverse maps
          this.mergedIntoAssignment = mergedIntoAssignment.entrySet().stream().filter(e -> !e.getValue().equals(id)).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        } else {
          throw(new MergeException("Merge was not found."));
        }
      }
      
      @Override
      protected Object clone() throws CloneNotSupportedException {
        MergeMap cloned = (MergeMap)super.clone();
        cloned.mergedIntoResult = this.mergedIntoResult.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return cloned;
      }
    }
    private Map<SimSearch.SimSet.Type, MergeMap> mergeMap;
    private ManipulationType type;
    
    private DataManipulationSet(ManipulationType type) {
      this.mergeMap = new HashMap<>();
      this.type = type;
      for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) this.mergeMap.put(simSetType,  new MergeMap());
    }
    
    private DataManipulationSet(DataManipulationSet dataManipulationSet) {
      this.mergeMap = new HashMap<>();
      for(SimSearch.SimSet.Type type: SimSearch.SimSet.Type.class.getEnumConstants()) this.mergeMap.put(type,  new MergeMap());
    }
  }
  
  private Stack<DataManipulationSet> undo;
  private Stack<DataManipulationSet> redo;
  
  protected SimSearchDataManipulation() {
    this.undo = new Stack<>();
    this.redo = new Stack<>();
  }
  
  private void merge(SimSearch.SimSet.Type simSetType, List<String> idsToMerge, String idToMergeInto) throws DataManipulationSet.MergeMap.MergeException {
    DataManipulationSet manipulationSet = (this.undo.isEmpty()?new DataManipulationSet(ManipulationType.Merge):this.undo.peek()); 
//    try {
      for(String id: idsToMerge) manipulationSet.mergeMap.get(simSetType).mergeInto(id, idToMergeInto); 
//    } catch (DataManipulationSet.MergeMap.MergeException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    this.undo.push(manipulationSet);
    this.redo.clear();
  }
  
  private void unmerge(SimSearch.SimSet.Type simSetType, List<String> idsToUnmerge) throws DataManipulationSet.MergeMap.MergeException {
    DataManipulationSet manipulationSet = (this.undo.isEmpty()?new DataManipulationSet(ManipulationType.Unmerge):this.undo.peek()); 
//    try {
    for(String id: idsToUnmerge) manipulationSet.mergeMap.get(simSetType).unmerge(id); 
//    } catch (DataManipulationSet.MergeMap.MergeException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    this.undo.push(manipulationSet);
    this.redo.clear();
  }
}
