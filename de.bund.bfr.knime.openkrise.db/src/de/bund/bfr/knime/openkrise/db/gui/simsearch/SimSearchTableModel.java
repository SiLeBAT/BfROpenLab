package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.table.DefaultTableModel;
import com.google.common.collect.Sets;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearch.SimSet;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearch.SimSearchTableModel.IllegalOperationException;

public final static class SimSearchTableModel extends DefaultTableModel{
  
  protected class DataManipulation {
    
  }
  
//public static enum RowStatus {
//  Default(1), mergeToPresentRow(2), mergeToNonPresentRow(4), Remove(8), DerivedRemove(16), SimReference(32), AlignmentReference(64);
//  
//  private int numVal;
//  
//  private RowStatus(int value) {
//    this.numVal = value;
//  }
//  
//  public int getNumVal() {
//    return numVal;
//  }
//  
//}

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

//  public SimSearchTableModel(Object[][] data, String[] columnNames, Class<?>[] columnClasses, boolean[] toRemove, int[] mergeTo) {
//   super();
//   this.data = data;
//   this.columnNames = columnNames;
//   this.columnClasses = columnClasses;
//   this.toRemove = toRemove;
//   this.mergeTo = mergeTo;
//   this.columnCount = columnNames.length;
//   this.rowCount = this.data.length;
//  }

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
//  if(simSet.simType==StationDBEntity.class) {
//    this.loadStationData();
////  } else 
//   // if(simSet.simType==ProductDBEntity.class) {
//    //loadProductData();
//  } else {
//    throw(new Exception());
//  }
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

//private void loadStationData() {
//  List<StationDBEntity> stationList = this.simSearch.loadStationData(this.simSet.idList, true);
//  if(stationList==null) return;
//  this.rowCount = stationList.size();
//  this.columnNames = new String[] {"Status", "ID", "Name", "Address", "Country","Products"};
//  this.columnClasses = new Class<?>[] {Integer.class, String.class, AlignedSequence.class, AlignedSequence.class, String.class, List.class};
//  this.columnCount = this.columnNames.length;
//  this.data = new Object[this.rowCount][this.columnCount];
//  for(int i=0; i < this.rowCount; ++i) {
//    StationDBEntity station = stationList.get(i);
//    Integer status = RowStatus.Empty.getNumVal();  
//    if(station.getID().equals(simSet.alignmentReferenceID)) {
//      this.alignmentReferenceRow = i;
//      this.data[i][0] &= RowStatus.AlignmentReference.getNumVal(); 
//    }
//    
//    this.data[i][1] = station.getName();
//    this.data[i][2] = station.getAddress();
//    this.data[i][3] = station.getCountry();
//    List<ProductDBEntity> productList = this.simSearch.loadProductData(station.getProductIDs(),false);
//    if(productList!=null) this.data[i][4] = productList.stream().map(p -> p.getName()).collect(Collectors.toList());
//  }
//  for(int column=0; column < this.columnCount; ++column) {
//    if(this.columnClasses[column]==AlignedSequence.class) {
//      if(this.simSet.alignmentReferenceID.equals(this.data[column]))
//    }
//  }
//}

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

