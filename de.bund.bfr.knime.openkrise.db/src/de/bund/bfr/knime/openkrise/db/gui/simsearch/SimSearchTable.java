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

import java.awt.Color;
import java.awt.Component;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javafx.scene.input.MouseButton;

public class SimSearchTable extends JScrollPane{
    
//    private static class MyJTable extends JTable {
//
//      
//        @Override
//        public void createDefaultColumnsFromModel() {
//            if(this.getModel()!=null) {
//                super.createDefaultColumnsFromModel();
//            }
//        }
//    }
	
	public static class ViewSettings {
		private List<Integer> frozenColumns;
		private List<Integer> columnOrdering;
		private List<Integer> rowSorting;
		// add sortkey
	}
    private TableColumnModel tableColumnModel;
    private TableColumnModel rowHeaderColumnModel;
    private List<Integer> frozenColumns = Arrays.asList(0,2);
    private Set<Integer> invisibleColumns = new HashSet<>(Arrays.asList(1));
    private SimSearchJTable table;
    private SimSearchJTable rowHeaderColumnTable;
    private ViewSettings viewSettings;
    private JTextField filterTextBox;
    private JCheckBox useRegexFilterCheckBox;
    private Color filterColor;
    
    
    public SimSearchTable() {
        super(new SimSearchJTable());
        this.viewSettings = new ViewSettings();
        // Create a column model for the main table. This model ignores the
        // first
        // column added, and sets a minimum width of 150 pixels for all others.
        this.tableColumnModel = new DefaultTableColumnModel() {
                        
            public void addColumn(TableColumn tc) {
              // Drop the frozen columns . . . that'll be the row header
              if(frozenColumns.contains(tc.getModelIndex()) || invisibleColumns.contains(tc.getModelIndex())) {  
                return;
              }
              tc.setMinWidth(150); // just for looks, really...
              super.addColumn(tc);
            }
          };
        
      this.rowHeaderColumnModel = new DefaultTableColumnModel() {
          
          public void addColumn(TableColumn tc) {
            if(frozenColumns.contains(tc.getModelIndex()) && !invisibleColumns.contains(tc.getModelIndex())) {
              if(tc.getModelIndex()==0) {
                tc.setMaxWidth(tc.getPreferredWidth());
                tc.setMinWidth(tc.getPreferredWidth());
              } else {
                tc.setMinWidth(10);
                tc.setMaxWidth(100);
              }
              //tc.setMaxWidth(tc.getPreferredWidth());
              super.addColumn(tc);
            }
            // Drop the rest of the columns . . . this is the header column
            // only
          }
        };
        //MyTableModel model = new MyTableModel(data,columnNames,columnClasses,remove,mergeTo);
        JViewport view = this.getViewport();
        Component[] components = view.getComponents();
        for (int i = 0; i < components.length; ++i) {
          if (components[i] instanceof SimSearchJTable) {
              this.table = (SimSearchJTable) components[i];
              break;
          }
        }
        //this.table = new MyJTable(); //null, this.tableColumnModel);
        //this.setViewportView(this.table);
        this.table.setColumnModel(this.tableColumnModel);
        this.rowHeaderColumnTable = new SimSearchJTable(); //null, this.rowHeaderColumnModel);
        this.rowHeaderColumnTable.setColumnModel(this.rowHeaderColumnModel);
        this.rowHeaderColumnTable.getTableHeader().setReorderingAllowed(false);
        
        SimSearchTableRowTransferHandler transferHandler = new SimSearchTableRowTransferHandler(this);
        //Arrays.asList(this.table, this.rowHeaderColumnTable).forEach( t -> {
        Arrays.asList(this.table).forEach( t -> {
          t.setTransferHandler(transferHandler);
          t.setDropMode(DropMode.ON_OR_INSERT_ROWS);
          t.setDragEnabled(true);
        });
        
        MouseAdapter mouseAdapter = new MouseAdapter() { 
            public void mouseClicked(MouseEvent e) { 
                mouseClickedOnTableHeader(e); 
            } 
        };
        this.table.getTableHeader().addMouseListener(mouseAdapter);
        this.rowHeaderColumnTable.getTableHeader().addMouseListener(mouseAdapter);
       
        
        //JTable table = new JTable(model, cm);
              
           // Set up the header column and get it hooked up to everything
             // JTable headerColumnTable = new JTable(model, rowHeaderModel);
              //table.createDefaultColumnsFromModel();
              //headerColumnTable.createDefaultColumnsFromModel();

              // Make sure that selections between the main table and the header stay
              // in sync (by sharing the same model)
              table.setSelectionModel(this.rowHeaderColumnTable.getSelectionModel());

              // Make the header column look pretty
              //headerColumn.setBorder(BorderFactory.createEtchedBorder());
              //this.rowHeaderColumnTable.setBackground(Color.lightGray);
              this.rowHeaderColumnTable.setColumnSelectionAllowed(false);
              this.rowHeaderColumnTable.setCellSelectionEnabled(false);
        
              KeyListener keyListener = new KeyListener() {

                @Override
                public void keyPressed(KeyEvent arg0) {
                  
                  if(arg0.getKeyCode() == KeyEvent.VK_DELETE) {
                    SimSearchJTable table = (SimSearchJTable) arg0.getSource();
                    if(table.getSelectedRowCount()>0) {
                      
                      try {
                        if(((SimSearch.SimSearchTableModel) table.getModel()).remove(convertViewRowsToModelRows(table.getSelectedRows()))) {
                          updateRowHeader();
                        }
                      } catch (SimSearch.SimSearchTableModel.IllegalOperationException e) {
                        JOptionPane.showMessageDialog(SimSearchTable.this.getTopLevelAncestor(), e.getMessage());
                      }
                    }
                  }
                }

                @Override
                public void keyReleased(KeyEvent arg0) {}

                @Override
                public void keyTyped(KeyEvent arg0) {}
                
              };
          
              this.table.addKeyListener(keyListener);
              this.rowHeaderColumnTable.addKeyListener(keyListener);
              
              MouseListener mouseListener = new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                  if(e.getButton()==MouseEvent.BUTTON1) {
                    e.consume();
                    SimSearchTable.this.table.updateUI();
                    SimSearchTable.this.rowHeaderColumnTable.updateUI();
                  }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                  // TODO Auto-generated method stub
                  
                }

                @Override
                public void mouseExited(MouseEvent e) {
                  // TODO Auto-generated method stub
                  
                }

                @Override
                public void mousePressed(MouseEvent e) {
                  // TODO Auto-generated method stub
                  
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                  // TODO Auto-generated method stub
                  
                }
                
              };
              
              this.table.getTableHeader().addMouseListener(mouseListener);
              // Put it in a viewport that we can control a bit
              JViewport jv = new JViewport();
              jv.setView(this.rowHeaderColumnTable);
              jv.setPreferredSize(this.rowHeaderColumnTable.getMaximumSize());

              // With out shutting off autoResizeMode, our tables won't scroll
              // correctly (horizontally, anyway)
              table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
              
              this.setRowHeader(jv);
              
              this.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, this.rowHeaderColumnTable
                  .getTableHeader());
              
    }
    
    private int[] convertViewRowsToModelRows(int[] rows) {
      for(int i=0; i<rows.length; ++i) rows[i] = this.table.getRowSorter().convertRowIndexToModel(rows[i]);
      return rows;
    }
    
    protected void updateRowHeader() {
      ((SimSearchJTable.RowHeaderColumnRenderer)  this.rowHeaderColumnTable.getColumnModel().getColumn(0).getCellRenderer()).emptyCache();
      this.table.updateUI();
      this.rowHeaderColumnTable.updateUI();
    }
    
//    public boolean areRowsMovableTo(int[] rowsSource, int rowTarget) {
//      if(this.table==null) return false;
//      for(int i=0; i<rowsSource.length; ++i) if(rowsSource[i]==rowTarget) return false;
//      
//      
//      int modelTarget = this.convertViewRowsToModelRows(new int[] {rowTarget})[0];
//      int mergeTarget = ((SimSearch.SimSearchTableModel) this.table.getModel()).getMergeTo(modelTarget);
//      
//      rowsSource = this.convertViewRowsToModelRows(rowsSource);
//      if(mergeTarget>=0) {
//        
//        return false;
//      } else {
//        
//      }
//    }
    
    private void textFilterChanged() {
      
      if(this.filterTextBox==null || this.table.getRowSorter()==null) return;
      
      if(this.useRegexFilterCheckBox!=null && this.useRegexFilterCheckBox.isSelected()) {
        try {
          Pattern pattern = Pattern.compile( this.filterTextBox.getText() );
          this.filterTextBox.setBackground(Color.white);
          ((SimSearchRowSorter) this.table.getRowSorter()).setRowFilter(pattern);
        } catch(PatternSyntaxException e) {
          ((SimSearchRowSorter) this.table.getRowSorter()).setRowFilter("");
          this.filterTextBox.setBackground(Color.RED);
        }
      } else {
        ((SimSearchRowSorter) this.table.getRowSorter()).setRowFilter(this.filterTextBox.getText());
        this.filterTextBox.setBackground(Color.white);
      }
      
      this.table.updateUI();
      this.rowHeaderColumnTable.updateUI();
//      SimSearchRowSorter rowSorter = (SimSearchRowSorter) this.table.getRowSorter();
//      this.table.setRowSorter(null);
//      this.rowHeaderColumnTable.setRowSorter(null);
//      this.table.setRowSorter(rowSorter);
//      this.rowHeaderColumnTable.setRowSorter(rowSorter);
    }

    
    public void registerRowFilter(JTextField filterTextBox, JCheckBox useRegExFilterCheckBox) {
      
      if(filterTextBox!=null) {
        
        this.filterTextBox = filterTextBox;
        this.filterColor = this.filterTextBox.getBackground();
        this.filterTextBox.getDocument().addDocumentListener(new DocumentListener() {

          @Override
          public void changedUpdate(DocumentEvent e) {
            // TODO Auto-generated method stub
            SimSearchTable.this.textFilterChanged();
          }

          @Override
          public void insertUpdate(DocumentEvent e) {
            // TODO Auto-generated method stub
            SimSearchTable.this.textFilterChanged();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            // TODO Auto-generated method stub
            SimSearchTable.this.textFilterChanged();
          }
          
        });
//        this.filterTextBox.addKeyListener(new KeyListener() {
//          }
//        );
//      
//        this.filterTextBox.addActionListener(new ActionListener() {
//  
//          @Override
//          public void actionPerformed(ActionEvent arg0) {
//            // TODO Auto-generated method stub
//            SimSearchTable.this.textFilterChanged(); //((JTextField) arg0.getSource()).getText());
//          }
//          
//        });
        if(useRegExFilterCheckBox!=null) {
          this.useRegexFilterCheckBox = useRegExFilterCheckBox;
                  
          this.useRegexFilterCheckBox.addChangeListener(new ChangeListener()  {
            @Override
            public void stateChanged(ChangeEvent arg0) {
              // TODO Auto-generated method stub
              SimSearchTable.this.textFilterChanged();
            }
          }
          );   
      
        }
      }
    }
    
    
    
    public void updateView() {
        
    }
    
    public void loadData(SimSearch.SimSearchTableModel tableModel) {
    	this.loadData(tableModel, this.viewSettings);
    }
    public void loadData(SimSearch.SimSearchTableModel tableModel, ViewSettings viewSettings) {
        this.table.setModel(tableModel);
        this.rowHeaderColumnTable.setModel(tableModel);
        //this.table.createDefaultColumnsFromModel();
        //this.rowHeaderColumnTable.createDefaultColumnsFromModel();
        
        //this.table.getColumn(table.getColumnName(0)).sizeWidthToFit();
        //this.table.getTableHeader().addMouseListener(new );
        
        table.setRowSorter(new SimSearchRowSorter(tableModel));
        this.rowHeaderColumnTable.setRowSorter(table.getRowSorter());
        
        
     // Put it in a viewport that we can control a bit
      JViewport jv = new JViewport();
      jv.setView(this.rowHeaderColumnTable);
      jv.setPreferredSize(this.rowHeaderColumnTable.getMaximumSize());
      this.setRowHeader(jv);
          
      this.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, this.rowHeaderColumnTable
                  .getTableHeader());
    }
    
    private void mouseClickedOnTableHeader(MouseEvent e) {
        if(e.getClickCount()==2 && e.getButton()==MouseEvent.BUTTON1) {
            JTableHeader tableHeader = (JTableHeader) e.getSource();
            // search TableColumn and resize it
            
//          int columnIndex = this.table.getTableHeader().columnAtPoint(e.getPoint()); 
//          Component comp = tableHeader.getComponentAt(e.getPoint());
//          
            System.out.println("Mouse double clicked on " + e.getSource().toString()+"\n");
//          System.out.println("Mouse double clicked on " + comp.toString() +"\n");
        }
    }
}
