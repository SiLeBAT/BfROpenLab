/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.table.DefaultTableModel;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearch.SimSet;

public final class SimSearchTableModel extends DefaultTableModel{
  
  protected class DataManipulation {
    
  }
  

private static final long serialVersionUID = -4838767897732945130L;

private final String[] columnComments;
private final String[] columnFormatComments;
private final String[] columnNames;
private final Class<?>[] columnClasses;

private Object[][] data;

private int[] mergeTo;
private int[][] mergesFrom;
private int[] mergeCount;
private int referenceRow;

private final int rowCount;
private final int columnCount;

private final static int IDCOLUMN = 1;
private final static int NOTMERGED = -2;

private static Map<SimSearch.SimSet.Type, Set<String>> alignColumnsMap;

static {
  alignColumnsMap = new HashMap<>();
  for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) alignColumnsMap.put(simSetType, new HashSet<>());
  alignColumnsMap.get(SimSearch.SimSet.Type.STATION).addAll(Arrays.asList(
      DBInfo.COLUMN.STATION_NAME.getName(), DBInfo.COLUMN.STATION_ADDRESS.getName(), 
      DBInfo.COLUMN.STATION_STREET.getName(), DBInfo.COLUMN.STATION_ZIP.getName(),
      DBInfo.COLUMN.STATION_CITY.getName(), DBInfo.COLUMN.STATION_HOUSENUMBER.getName()));
  alignColumnsMap.get(SimSearch.SimSet.Type.PRODUCT).addAll(Arrays.asList(
      DBInfo.COLUMN.PRODUCT_DESCRIPTION.getName()));
  alignColumnsMap.get(SimSearch.SimSet.Type.LOT).addAll(Arrays.asList(
      DBInfo.COLUMN.LOT_NUMBER.getName()));
  alignColumnsMap.get(SimSearch.SimSet.Type.DELIVERY).addAll(Arrays.asList(
      DBInfo.COLUMN.DELIVERY_ARRIVEDON_DAY.getName(), DBInfo.COLUMN.DELIVERY_ARRIVEDON_MONTH.getName(), 
      DBInfo.COLUMN.DELIVERY_ARRIVEDON_YEAR.getName(),
      DBInfo.COLUMN.DELIVERY_DELIVEREDON_DAY.getName(), DBInfo.COLUMN.DELIVERY_DELIVEREDON_MONTH.getName(), 
      DBInfo.COLUMN.DELIVERY_DELIVEREDON_YEAR.getName()));
}


private final SimSet simSet;
private final SimSearchDataManipulationHandler dataManipulationHandler;

public static class IllegalOperationException extends Exception {
  
	private static final long serialVersionUID = 5036733741036265178L;

	IllegalOperationException(String message) {
    super(message);
  }
}

SimSearchTableModel(SimSet simSet, SimSearchDataManipulationHandler dataManipulationHandler, SimSearch.DataSource.DataLoader dataLoader) {
	  super();

	  this.simSet = simSet;
	  this.dataManipulationHandler = dataManipulationHandler;
	  this.data = dataLoader.data;
	  this.columnNames = dataLoader.columnNames;
	  this.columnNames[0] = "";
	  this.columnClasses = dataLoader.columnClasses;
	  this.columnComments = dataLoader.columnComments;
	  this.columnFormatComments = dataLoader.columnFormatComments;
	  this.columnCount = columnNames.length;
	  this.rowCount = data.length;
	  this.referenceRow = -1;
	  
	  this.initArrays();
	  this.createAlignments();
	}

private void initArrays() {
  this.mergeTo = new int[this.rowCount];
  this.mergesFrom = new int[this.rowCount][];
  this.mergeCount = new int[this.rowCount];
   
  Arrays.fill(this.mergeTo, NOTMERGED); //noMerge
  Arrays.fill(this.mergeCount, 0);
  
  Map<Integer, List<Integer>> mergesFrom = new HashMap<>();
  Map<Integer, Integer> idToIndexMap = getIdToRowIndexMap();
  
  if(idToIndexMap.containsKey(this.simSet.getReferenceId())) this.referenceRow = idToIndexMap.get(this.simSet.getReferenceId());
  
  for(Integer id : idToIndexMap.keySet()) {
    Integer idInto = dataManipulationHandler.getMergedInto(simSet.getType(), id);     
    if(idInto!=null) {
      int idIndex = idToIndexMap.get(id);
            
      Integer idIntoIndex = idToIndexMap.get(idInto);
      
      if(idIntoIndex!=null) {
        this.mergeTo[idIndex] = idIntoIndex;
        if(!mergesFrom.containsKey(idIntoIndex)) mergesFrom.put(idIntoIndex, new ArrayList<>());
        mergesFrom.get(idIntoIndex).add(idIndex);
      } else {
        // outside merge
        this.mergeTo[idIndex] = -1;
      }
    }
  }
  mergesFrom.entrySet().forEach(e -> {
    this.mergesFrom[e.getKey()] = new int[e.getValue().size()];
    for(int i =0; i>e.getValue().size(); ++i)  this.mergesFrom[e.getKey()][i] = e.getValue().get(i);
  });
  for(int i=0; i<this.rowCount; ++i) {
    // ids.contains(this.data[i][IDCOLUMN]);
    this.mergeCount[i] = this.dataManipulationHandler.getMergeCount(this.simSet.getType(),(Integer) this.data[i][IDCOLUMN]);
    if(simSet.getReferenceId().equals(this.getID(i))) this.data[i][0] = Integer.MAX_VALUE;
    else this.data[i][0] = this.mergeCount[i] - (this.isMerged(i)?1:0);
  }
      
  
}

public Integer getID(int row) {
  return (Integer) this.data[row][IDCOLUMN];
}

public boolean isInactive(int row) {
  return this.isMerged(row);
}

private Map<Integer, Integer> getIdToRowIndexMap() {
  Map<Integer, Integer> idToIndexMap = new HashMap<>();
  for(int row=0; row < this.rowCount; ++row) idToIndexMap.put((Integer) data[row][IDCOLUMN], row);
  return idToIndexMap;
}

public int getReferenceRow() { return this.referenceRow; }

private void createAlignments() {
  List<Integer> alignColumns = new ArrayList<>(); 
  for(int column=1; column<this.columnCount; ++column) if(alignColumnsMap.get(this.simSet.getType()).contains(this.columnNames[column])) alignColumns.add(column);
  
  List<Integer> idList = new ArrayList<>();
  for(int row=0; row<this.rowCount; ++row) idList.add(this.getID(row));
  
  int referenceIndex = idList.indexOf(this.simSet.getReferenceId());
  
  for(int column:alignColumns) {
    
    if(referenceIndex<0) {
      
      for(int row=0; row<this.rowCount; ++row) this.data[row][column] = new Alignment.AlignedSequence((String) this.data[row][column]);
      
    } else {
      
      String[] sequences = new String[this.rowCount];
      for(int row=0; row<this.rowCount; ++row) {
        sequences[row] = (data[row][column]==null?null:data[row][column].toString());
      }
      Alignment.AlignedSequence[] alignedSeqs= Alignment.alignSequences(sequences, referenceIndex);
      for(int row=0; row<this.rowCount; ++row) {
        data[row][column] = alignedSeqs[row];
      }
    }
    this.columnClasses[column] = Alignment.AlignedSequence.class;
  }
  
}

public boolean areRowsDraggable(List<Integer> indexList) {
    return !indexList.stream().anyMatch(i -> this.isMerged(i));
}

public boolean isMergeValid(List<Integer> rowsToMerge, int rowToMergeInto) {
    return false;
}

public boolean isMerged(int row) {
    return mergeTo[row]!=NOTMERGED;
}

public int getMergeCount(int row) {
  return this.mergeCount[row];
}

public boolean isSimReferenceRow(int row) {
  return ((Integer) this.data[row][IDCOLUMN]).equals(simSet.getReferenceId());
}

public boolean isAlignmentReferenceRow(int row) {
  return false; 
}

public boolean isMergeValid(int[] rowsToMerge, int rowToMergeInto) {
  if(IntStream.of(rowsToMerge).anyMatch(i -> i==rowToMergeInto)) return false;
  if(this.dataManipulationHandler.isMerged(this.simSet.getType(), (Integer) this.data[rowToMergeInto][IDCOLUMN])) {
    return false;
  } else {
    for(int i=0; i<rowsToMerge.length; ++i) if(this.dataManipulationHandler.isMerged(this.simSet.getType(), (Integer) this.data[rowsToMerge[i]][IDCOLUMN])) return false;
  }
  return true;
}

public void mergeRows(int[] rowsToMerge, int rowToMergeInto) {
  if(this.isMergeValid(rowsToMerge,rowToMergeInto)) {
    List<Integer> idsToMerge = new ArrayList<>();
    for(int i=0; i< rowsToMerge.length; ++i) idsToMerge.add((Integer) this.data[rowsToMerge[i]][IDCOLUMN]);
    Integer idToMergeInto = (Integer) this.data[rowToMergeInto][IDCOLUMN];
    try {
      this.dataManipulationHandler.merge(this.simSet.getType(), idsToMerge, idToMergeInto);
      this.initArrays();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

public void unmergeRows(int[] rowsToUnmerge) {
	List<Integer> idsToUnmerge = IntStream.of(rowsToUnmerge).map(i -> this.getID(i)).boxed().collect(Collectors.toList());
    try {
      this.dataManipulationHandler.unmerge(this.simSet.getType(), idsToUnmerge);       
    } catch (Exception e) {
      e.printStackTrace();
    }
	  
}

public void undo() {
  this.dataManipulationHandler.undo();
  this.initArrays();
}

public void redo() {
  this.dataManipulationHandler.redo();
  this.initArrays();
}

public boolean isUndoAvailable() {
  return this.dataManipulationHandler.isUndoAvailable();
}

public boolean isRedoAvailable() {
  return this.dataManipulationHandler.isRedoAvailable();
}

public boolean isSimSetIgnored() {
  return this.dataManipulationHandler.isSimSetIgnored(simSet);
}


public boolean isSimSetIgnoreAvailable() {
  return this.dataManipulationHandler.isSimSetIgnoreAvailable(simSet);
}

public void ignoreSimSet() {
  this.dataManipulationHandler.ignoreSimSet(this.simSet);
}

public void ignoreAllPairsInSimSet() {
  this.dataManipulationHandler.ignoreAllPairsInSimSet(this.simSet);
}

public String getUndoType() {
  return this.dataManipulationHandler.getUndoType();
}

public String getRedoType() {
  return this.dataManipulationHandler.getRedoType();
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
public String getColumnName(int column) { 
	return this.columnNames[column];
}

public String getColumnComment(int column) { 
  return this.columnComments[column];
}

public String getColumnFormatComment(int column) { 
	return this.columnFormatComments[column];
}

@Override
public boolean isCellEditable(int row, int column) { return false; }

public List<String> getColumnNames() {
  return Arrays.asList(this.columnNames);
}

public SimSearch.SimSet getSimSet() { return this.simSet; }
}



