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
      
      public boolean isMerged(Integer id) {
        return this.mergedIntoAssignment.containsKey(id);
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
      
      public int getMergeCount(Integer id) {
        return (int) this.mergedIntoAssignment.values().stream().filter(e -> e.equals(id)).count();
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
  
  private Stack<DataManipulation> undoStack;
  private Stack<DataManipulation> redoStack;
  
  protected SimSearchDataManipulationHandler() {
    this.undoStack = new Stack<>();
    this.redoStack = new Stack<>();
  }
  
  public void merge(SimSearch.SimSet.Type simSetType, List<Integer> idsToMerge, Integer idToMergeInto) throws DataManipulation.MergeMap.MergeException {
    DataManipulation manipulation = (this.undoStack.isEmpty()?new DataManipulation(ManipulationType.Merge):this.undoStack.peek()); 
//    try {
      for(Integer id: idsToMerge) manipulation.mergeMap.get(simSetType).mergeInto(id, idToMergeInto); 
//    } catch (DataManipulationSet.MergeMap.MergeException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    this.undoStack.push(manipulation);
    this.redoStack.clear();
  }
  
  public void unmerge(SimSearch.SimSet.Type simSetType, List<Integer> idsToUnmerge) throws DataManipulation.MergeMap.MergeException {
    DataManipulation manipulation = (this.undoStack.isEmpty()?new DataManipulation(ManipulationType.Unmerge):this.undoStack.peek()); 
//    try {
    for(Integer id: idsToUnmerge) manipulation.mergeMap.get(simSetType).unmerge(id); 
//    } catch (DataManipulationSet.MergeMap.MergeException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    this.undoStack.push(manipulation);
    this.redoStack.clear();
  }
  
  public Integer getMergedInto(SimSearch.SimSet.Type simSetType, Integer id) {
    return (this.undoStack.empty()?null:this.undoStack.peek().mergeMap.get(simSetType).getMergeAssignment(id));
  }
  
  public boolean isMerged(SimSearch.SimSet.Type simSetType, Integer id) {
    return (this.undoStack.empty()?false:this.undoStack.peek().mergeMap.get(simSetType).isMerged(id));
  }
  
  public void undo() {
    if(!this.undoStack.isEmpty()) this.redoStack.push(this.undoStack.pop());
  }

  public void redo() {
    if(!this.redoStack.isEmpty()) this.undoStack.push(this.redoStack.pop());
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
      switch(this.undoStack.peek().type) {
        case Merge:
          return "Undo merge operation.";
        case Unmerge:
          return "Undo unmerge operation.";
        default:
          return "Undo operation.";
      }
    }
  }

  public String getRedoType() {
    if(this.redoStack.isEmpty()) {
      return "";
    } else {
      switch(this.redoStack.peek().type) {
        case Merge:
          return "Redo merge operation.";
        case Unmerge:
          return "Redo unmerge operation.";
        default:
          return "Redo operation.";
      }
    }
  }
  
  public int getMergeCount(SimSearch.SimSet.Type simSetType, Integer id) {
    if(this.undoStack.isEmpty()) {
      return 0;
    } else {
      return this.undoStack.peek().mergeMap.get(simSetType).getMergeCount(id);
    }
  }
}
