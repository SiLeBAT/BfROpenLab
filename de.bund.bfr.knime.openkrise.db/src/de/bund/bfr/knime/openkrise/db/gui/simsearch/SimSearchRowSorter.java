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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.DefaultRowSorter;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableRowSorter;

public class SimSearchRowSorter extends RowSorter<SimSearchTableModel>{
    
    private interface MyComparator<T> {
        public int compareObjects(Object o1, Object o2);
        public boolean isMatch(T o, String searchString);
    }
    
    
    private SimSearchTableModel model;
    private int[] modelToView;
    private List<Integer> viewToModel;
    private List<Integer> unfilteredViewToModel;
    private List<? extends RowSorter.SortKey> sortKeys;
    private final MyComparator<?>[] columnComparators;
    private static final Map<Class<?>, MyComparator<?>> comparatorMap;
    //private RowFilter<SimSearchTableModel, Integer> rowFilter;
    private String rowFilterText;
    private Pattern rowFilterPattern;
    private boolean showInactiveRows;
    
    static {
        Map<Class<?>, MyComparator<?>> compMap = new HashMap<>();
        compMap.put(String.class, new MyComparator<String>() {

            @Override
            public int compareObjects(Object o1, Object o2) {
                // TODO Auto-generated method stub
                return ((String) o1).compareTo((String) o2);
            }
            
            public boolean isMatch(String o, String searchString) {
              if(searchString==null || searchString.isEmpty()) {
                return true;
              } else if(o==null || o.isEmpty()) {
                return false;
              } else {
                return o.indexOf(searchString)>=0;
              }
            }
        });
        compMap.put(Integer.class, new MyComparator<Integer>() {

            @Override
            public int compareObjects(Object o1, Object o2) {
                // TODO Auto-generated method stub
                return ((Integer) o1).compareTo((Integer) o2);
            }
            
            public boolean isMatch(Integer o, String searchString) {
              if(searchString==null || searchString.isEmpty()) {
                return true;
              } else if(o==null) {
                return false;
              } else {
                return o.toString().indexOf(searchString)>=0;
              }
            }
            
        });
        compMap.put(Boolean.class, new MyComparator<Boolean>() {

            @Override
            public int compareObjects(Object o1, Object o2) {
                // TODO Auto-generated method stub
                return ((Boolean) o1).compareTo((Boolean) o2);
            }

            @Override
            public boolean isMatch(Boolean o, String searchString) {
              if(searchString==null || searchString.isEmpty()) {
                return true;
              } else if(o==null) {
                return false;
              } else {
                return o.toString().indexOf(searchString)>=0;
              }
            }
            
        });
        compMap.put(Alignment.AlignedSequence.class, new MyComparator<Alignment.AlignedSequence>() {

          @Override
          public int compareObjects(Object o1, Object o2) {
              // TODO Auto-generated method stub
              return ((Alignment.AlignedSequence) o1).compareTo((Alignment.AlignedSequence) o2);
          }

          @Override
          public boolean isMatch(Alignment.AlignedSequence o, String searchString) {
            // TODO Auto-generated method stub
            if(searchString==null || searchString.isEmpty()) {
              return true;
            } else if(o==null || o.getSequence()==null || o.getSequence().isEmpty()) {
              return false;
            } else {
              return o.getSequence().indexOf(searchString)>=0;
            }
          }
          
      });
        
        comparatorMap = Collections.unmodifiableMap(compMap);
    }

    public SimSearchRowSorter(SimSearchTableModel model) {
        this.model = model;
        //this.modelToView = new ArrayList<>(model.getRowCount());
        //this.viewToModel = new ArrayList<>(model.getRowCount());
//        for(int i = 0; i< model.getRowCount(); ++i ) {
//            this.modelToView.add(i);
//            this.viewToModel.add(i);
//        }
        this.showInactiveRows = false;
        this.sortKeys = new ArrayList<>();
        this.columnComparators = new MyComparator<?>[model.getColumnCount()];
        for(int i=0; i < model.getColumnCount(); ++i) columnComparators[i] = comparatorMap.get(model.getColumnClass(i));  
        
        this.unfilteredViewToModel = new ArrayList<>(model.getRowCount());
        for(int i = 0; i< model.getRowCount(); ++i ) this.unfilteredViewToModel.add(i);
        this.filterRows();
//        this.setRowFilter(new RowFilter<SimSearchTableModel, Integer>() {
//
//          @Override
//          public boolean include(Entry<? extends SimSearchTableModel, ? extends Integer> arg0) {
//            // TODO Auto-generated method stub
//            
//            this.regexFilter(regex, indices)
//            return true;
//          }
//          
//        });
}
//  @Override
//  public Object getModel() {
//      // TODO Auto-generated method stub
//      return this.model;
//  }
//    public void setRowFilter(RowFilter<SimSearchTableModel, Integer> rowFilter) {
//      this.rowFilter = rowFilter;
//    }

    @Override
    public void toggleSortOrder(int column) {
        // TODO Auto-generated method stub
        
//  if (isSortable(column)) {
        List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
        SortKey sortKey;
        int sortIndex;
        for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
            if (keys.get(sortIndex).getColumn() == column) {
                break;
            }
        }
        if (sortIndex == -1) {
            // Key doesn't exist
            sortKey = new SortKey(column, SortOrder.ASCENDING);
            keys.add(0, sortKey);
        }
        else if (sortIndex == 0) {
            // It's the primary sorting key, toggle it
            keys.set(0, toggle(keys.get(0)));
        }
        else {
            // It's not the first, but was sorted on, remove old
            // entry, insert as first with ascending.
            keys.remove(sortIndex);
            keys.add(0, new SortKey(column, SortOrder.ASCENDING));
        }
//        if (keys.size() > getMaxSortKeys()) {
//            keys = keys.subList(0, getMaxSortKeys());
//        }
        
        setSortKeys(keys);
//    }
    }
    
    private class MyRowComparator implements Comparator<Integer> {

//        private final int column;
//        private final int sortOrder;
//        private final List<SortKey> sortList;
        
//        protected MyRowComparator(List<SortKey key>) {
//            this.column = key.getColumn();
//            this.sortOrder = (key.getSortOrder()==SortOrder.ASCENDING?1:-1);
//        }
        
        @Override
        public int compare(Integer o1, Integer o2) {
            // assume model index
          int result = 0; 
          
          for(SortKey sortKey : SimSearchRowSorter.this.getSortKeys()) {
            result = SimSearchRowSorter.this.columnComparators[sortKey.getColumn()].compareObjects(
                SimSearchRowSorter.this.model.getValueAt(o1, sortKey.getColumn()),
                SimSearchRowSorter.this.model.getValueAt(o2, sortKey.getColumn()));
            if(result!=0) return result * (sortKey.getSortOrder()==SortOrder.ASCENDING?1:-1);
          }
          return 0;
//            if(SimSearchRowSorter.this.model.getRemove(o1)) {
//                if(!SimSearchRowSorter.this.model.getRemove(o2)) return 1;
//            } else if(SimSearchRowSorter.this.model.getRemove(o2)) return -1;
//            
//            if(SimSearchRowSorter.this.model.getMergeTo(o1)==o2) return 1;
//            if(SimSearchRowSorter.this.model.getMergeTo(o2)==o1) return -1;
//            if(SimSearchRowSorter.this.model.getMergeTo(o1)==SimSearchRowSorter.this.model.getMergeTo(o2) && SimSearchRowSorter.this.model.getMergeTo(o1)>=0) {
//                // do nothing, same mergelist
//            }
//            if(SimSearchRowSorter.this.model.getMergeTo(o1)>=0) o1 = SimSearchRowSorter.this.model.getMergeTo(o1); 
//            if(SimSearchRowSorter.this.model.getMergeTo(o2)>=0) o2 = SimSearchRowSorter.this.model.getMergeTo(o2);
//                
//            
//            if(SimSearchRowSorter.this.model.getValueAt(o1, column)==null) {
//                if(SimSearchRowSorter.this.model.getValueAt(o2, column)==null) {
//                    return Integer.compare(SimSearchRowSorter.this.modelToView.get(o1), SimSearchRowSorter.this.modelToView.get(o2));
//                } else return -1;
//            } else {
//                if(SimSearchRowSorter.this.model.getValueAt(o2, column)==null) {
//                    return 1;
//                } else {
//                    return SimSearchRowSorter.this.columnComparators[column].compareObjects(
//                        SimSearchRowSorter.this.model.getValueAt(o1, column),
//                        SimSearchRowSorter.this.model.getValueAt(o2, column))*this.sortOrder;
//                }
//            }
        }
        
    }
    
    public void sort() {
        //
      List<Integer> alwaysOnTop = new ArrayList<>(); // so far not considered
      
      Map<Integer, List<Integer>> dependentRows = new LinkedHashMap<>();
      for(int row=0; row<this.getModelRowCount(); ++row) {
        if(this.model.isMerged(row)) {
          int mergeToIndex = this.model.getMergeTo(row);
          if(mergeToIndex>=0) {
            if(!dependentRows.containsKey(mergeToIndex)) dependentRows.put(mergeToIndex, new ArrayList<>());
            dependentRows.get(mergeToIndex).add(row);
          }
        }
      }
      
      
      MyRowComparator myRowComparator = new MyRowComparator();
      
      this.unfilteredViewToModel.removeAll(alwaysOnTop);
      Collections.sort(alwaysOnTop, myRowComparator);
      
      for(List<Integer> rowList : dependentRows.values()) {
        Collections.sort(rowList, myRowComparator);
        this.unfilteredViewToModel.removeAll(rowList);
      }
      
      Collections.sort(this.unfilteredViewToModel, new MyRowComparator());
      
      this.unfilteredViewToModel.addAll(0, alwaysOnTop);
      
      for(Map.Entry<Integer, List<Integer>> e: dependentRows.entrySet()) this.unfilteredViewToModel.addAll(this.unfilteredViewToModel.indexOf(e.getKey())+1, e.getValue());
      
      //for(Integer key: dependentRows.keySet()) this.unfilteredViewToModel.addAll(this.unfilteredViewToModel.indexOf(key), )
      
        //Collections.sort(this.viewToModel, new MyRowComparator(key));
        //for(int i=0; i<model.getRowCount(); ++i) modelToView.set(viewToModel.get(i), i);
        //List<? extends RowSorter.SortKey> keys = getSortKeys();
      this.filterRows();  
    }
    
    private void filterRows() {
      this.viewToModel = this.unfilteredViewToModel.stream().collect(Collectors.toList());
      Set<Integer> keepRows = new HashSet<>();
      List<Integer> filterRows = new ArrayList<>();
      
      if(this.rowFilterText!=null || this.rowFilterPattern!=null) {
        for(Integer modelRow : this.viewToModel) {
          boolean bolFilter = true;
          for(int column=0; column<this.model.getColumnCount(); ++column) {
            if(this.model.getValueAt(modelRow, column)!=null && 
                (this.rowFilterText!=null?
                    this.model.getValueAt(modelRow, column).toString().contains(this.rowFilterText):
                    this.rowFilterPattern.matcher(this.model.getValueAt(modelRow, column).toString()).matches())) {
              bolFilter = false;
              break;
            }
          }
          if(bolFilter) {
            filterRows.add(modelRow);
          } else if(this.model.getMergeTo(modelRow)>=0) { 
            // keep mergeTo row
            keepRows.add(this.model.getMergeTo(modelRow));
          }
        }
      } 
      filterRows.removeAll(keepRows);
      this.viewToModel.removeAll(filterRows);
      
      this.modelToView = new int[this.getModelRowCount()];
      Arrays.fill(this.modelToView, -1);
      //this.modelToView = new ArrayList<>(this.viewToModel.size());
      
      //Arrays.fill(a, val);
      for(int i=0; i<this.getViewRowCount(); ++i) modelToView[viewToModel.get(i)] = i;
    }
    
    
    public void showInactiveRows(boolean value) {
      this.showInactiveRows = value;
      this.filterRows();
    }
    
    public void setRowFilter(String text) {
      this.rowFilterPattern = null;
      this.rowFilterText = text;
      this.filterRows();
    }
    
    public void setRowFilter(Pattern pattern) {
      this.rowFilterText = null;
      this.rowFilterPattern = pattern;
      this.filterRows();
    }
    
    private SortKey toggle(SortKey key) {
        if (key.getSortOrder() == SortOrder.ASCENDING) {
            return new SortKey(key.getColumn(), SortOrder.DESCENDING);
        }
        return new SortKey(key.getColumn(), SortOrder.ASCENDING);
    }
    
    
    public void moveRows(int[] rowsToMove, int rowToMoveBefore) {
      List<Integer> unmovedRows = this.unfilteredViewToModel.stream().collect(Collectors.toList());
      List<Integer> movedRows = this.unfilteredViewToModel.stream().collect(Collectors.toList());
      
      List<Integer> rowsToMoveList = new ArrayList<>();
      for(int i=0; i<rowsToMove.length; ++i) rowsToMoveList.add(rowsToMove[i]);
      
      unmovedRows.removeAll(rowsToMoveList);
      movedRows.retainAll(rowsToMoveList);
      
      unmovedRows.addAll(unmovedRows.indexOf(rowToMoveBefore), movedRows);
      this.unfilteredViewToModel = unmovedRows;
      
      this.sortKeys.clear();
      this.sort();
    }
    
    @Override
    public int convertRowIndexToModel(int index) {
        // TODO Auto-generated method stub
        //System.out.println("convertRowIndexToModel: " + index + " -> " + viewToModel.get(index));
        return viewToModel.get(index);
    }

    @Override
    public int convertRowIndexToView(int index) {
        // TODO Auto-generated method stub
        return modelToView[index];
    }
//
//  @Override
//  public void setSortKeys(List keys) {
//      // TODO Auto-generated method stub
//      super.setSortKeys(keys);
//  }
////
//  @Override
//  public List getSortKeys() {
//      // TODO Auto-generated method stub
//      return super.getSortKeys();
//  }
//
    @Override
    public int getViewRowCount() {
        // TODO Auto-generated method stub
        return this.viewToModel.size();
    }

    @Override
    public int getModelRowCount() {
        // TODO Auto-generated method stub
        return this.model.getRowCount();
    }

//  @Override
//  public void modelStructureChanged() {
//      // TODO Auto-generated method stub
//      
//  }
//
//  @Override
//  public void allRowsChanged() {
//      // TODO Auto-generated method stub
//      
//  }
//
//  @Override
//  public void rowsInserted(int firstRow, int endRow) {
//      // TODO Auto-generated method stub
//      
//  }
//
//  @Override
//  public void rowsDeleted(int firstRow, int endRow) {
//      // TODO Auto-generated method stub
//      
//  }
//
//  @Override
//  public void rowsUpdated(int firstRow, int endRow) {
//      // TODO Auto-generated method stub
//      
//  }
//
//  @Override
//  public void rowsUpdated(int firstRow, int endRow, int column) {
//      // TODO Auto-generated method stub
//      
//  }
    
    
@Override
public SimSearchTableModel getModel() {
    // TODO Auto-generated method stub
    return this.model;
}

@Override
public void setSortKeys(List<? extends SortKey> keys) {
    // TODO Auto-generated method stub
    this.sortKeys = keys;
    this.sort();
}

@Override
public void modelStructureChanged() {
    // TODO Auto-generated method stub
    
}
@Override
public void allRowsChanged() {
    // TODO Auto-generated method stub
    
}
@Override
public void rowsInserted(int firstRow, int endRow) {
    // TODO Auto-generated method stub
    
}
@Override
public void rowsDeleted(int firstRow, int endRow) {
    // TODO Auto-generated method stub
    
}
@Override
public void rowsUpdated(int firstRow, int endRow) {
    // TODO Auto-generated method stub
    
}
@Override
public void rowsUpdated(int firstRow, int endRow, int column) {
    // TODO Auto-generated method stub
    
}

@Override
public List<? extends SortKey> getSortKeys() {
    // TODO Auto-generated method stub
    return this.sortKeys;
}

}
