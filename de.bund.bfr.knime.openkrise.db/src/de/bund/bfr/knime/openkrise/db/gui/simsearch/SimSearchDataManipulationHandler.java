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
  
  private static class DataManipulation {
    
    public static class MergeMap implements Cloneable {
      public class MergeException extends Exception {
        private MergeException(String message) {
          super(message);
        }
      }
      private Map<Integer,Integer> mergedIntoAssignment;
      private Map<Integer,Integer> mergedIntoResult;

      public MergeMap() {
        this.mergedIntoAssignment = new HashMap<>();
        this.mergedIntoResult = new HashMap<>();
      }

      //public String getMergeResult(String id) { return this.mergedIntoResult.get(id); }
      public Integer getMergeAssignment(Integer id) { return this.mergedIntoAssignment.get(id); }

      public void mergeInto(Integer idToMerge, Integer idToMergeInto) throws MergeException {
        if(mergedIntoAssignment.containsKey(idToMergeInto)) throw(new MergeException("Cascading merges are not allowed."));
        if(mergedIntoAssignment.containsKey(idToMerge)) throw(new MergeException("ID is already merged."));

        mergedIntoAssignment.put(idToMerge, idToMergeInto);
//        mergedIntoResult.entrySet().stream().filter(e -> idToMerge.equals(e.getValue())).forEach(e -> {
//          mergedIntoResult.put(e.getKey(), idToMergeInto);
//        });
      }
      
      public void unmerge(Integer id) throws MergeException {
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
    
    private DataManipulation(ManipulationType type) {
      this.mergeMap = new HashMap<>();
      this.type = type;
      for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) this.mergeMap.put(simSetType,  new MergeMap());
    }
    
    private DataManipulation(DataManipulation dataManipulation) {
      this.mergeMap = new HashMap<>();
      for(SimSearch.SimSet.Type type: SimSearch.SimSet.Type.class.getEnumConstants()) this.mergeMap.put(type,  new MergeMap());
    }
  }
  
  private Stack<DataManipulation> undo;
  private Stack<DataManipulation> redo;
  
  protected SimSearchDataManipulationHandler() {
    this.undo = new Stack<>();
    this.redo = new Stack<>();
  }
  
  public void merge(SimSearch.SimSet.Type simSetType, List<Integer> idsToMerge, Integer idToMergeInto) throws DataManipulation.MergeMap.MergeException {
    DataManipulation manipulation = (this.undo.isEmpty()?new DataManipulation(ManipulationType.Merge):this.undo.peek()); 
//    try {
      for(Integer id: idsToMerge) manipulation.mergeMap.get(simSetType).mergeInto(id, idToMergeInto); 
//    } catch (DataManipulationSet.MergeMap.MergeException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    this.undo.push(manipulation);
    this.redo.clear();
  }
  
  public void unmerge(SimSearch.SimSet.Type simSetType, List<Integer> idsToUnmerge) throws DataManipulation.MergeMap.MergeException {
    DataManipulation manipulation = (this.undo.isEmpty()?new DataManipulation(ManipulationType.Unmerge):this.undo.peek()); 
//    try {
    for(Integer id: idsToUnmerge) manipulation.mergeMap.get(simSetType).unmerge(id); 
//    } catch (DataManipulationSet.MergeMap.MergeException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    this.undo.push(manipulation);
    this.redo.clear();
  }
  
  public Integer getMergedInto(SimSearch.SimSet.Type simSetType, Integer id) {
    return (this.undo.empty()?null:this.undo.peek().mergeMap.get(simSetType).getMergeAssignment(id));
  }
}
