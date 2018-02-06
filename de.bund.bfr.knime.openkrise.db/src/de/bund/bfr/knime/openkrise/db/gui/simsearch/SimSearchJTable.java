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
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.commons.text.StringEscapeUtils;

public class SimSearchJTable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7055490193917189461L;
//	private SimSearchTableModel tableModel;
	//private static final String ALIGNMENT_COLOR_EDIT = "red";
	//private static final String ALIGNMENT_COLOR_EDIT = "red";
	//private static final String ALIGNMENT_COLOR_EDIT = "red";
//	private static final String HTML_PREFIX = "<html>";
//	private static final String HTML_SUFFIX = "</html>";
//	private enum Color {
//	  Black, Red, 
//	}
	//private static final 
	private enum EditType {
	  None(EnumSet.of(Alignment.EditOperation.None)), 
	  Neutral(EnumSet.of(Alignment.EditOperation.GAP)), 
	  MisMatch(EnumSet.of(Alignment.EditOperation.Replace,Alignment.EditOperation.Insert,Alignment.EditOperation.Delete));
	  
	  private EnumSet<Alignment.EditOperation> editOperations;
	  
	  EditType(EnumSet<Alignment.EditOperation> ops) {
	    this.editOperations = ops;
	  }
	  private boolean matches(Alignment.EditOperation op) {
	    return this.editOperations.contains(op);
	  }
	  
	  private static EditType getType(Alignment.EditOperation op) {
	    return (None.matches(op)?None:(Neutral.matches(op)?Neutral:(MisMatch.matches(op)?MisMatch:null)));
	  }
	}
	
    private static String createHtmlCode(Alignment.AlignedSequence alignedSequence) throws Exception {
      if(alignedSequence==null || alignedSequence.getEditOperations()==null || alignedSequence.getEditOperations().isEmpty()) {
        return "";
      }
      StringBuilder sb = new StringBuilder("<html>");
      //final String SYMBOL_GAP = "&#8911;"; //"&#128;"; //"&#8248;"; //"&#752;"; //"&#x022CF;"; // "&cuwed"; 
      final String SYMBOL_GAP = "&#8743;";
      //final String SYMBOL_SPACE_DELETE = "&#9618;"; //"&#x02592;"; // "&block;"; 
      final String SYMBOL_SPACE_DELETE = "&#8215;";
      final String SYMBOL_SPACE = "&nbsp;";
      int textPos = -1;
      EditType editType = EditType.None;
      for(Alignment.EditOperation op : alignedSequence.getEditOperations()) {
        if(!editType.matches(op)) {
          if(editType!=EditType.None) sb.append("</font>");
          editType = EditType.getType(op);
          if(editType==null) throw(new Exception());
          if(editType!=EditType.None) sb.append(String.format("<font color=\"%s\" style=\"bold\">", (editType==EditType.Neutral?"green":"red")));
        }
        if(op==Alignment.EditOperation.GAP || op==Alignment.EditOperation.Insert) {
          sb.append(SYMBOL_GAP);
        } else {
          if(++textPos>=alignedSequence.getSequence().length()) throw(new Exception());
          char symbol = alignedSequence.getSequence().charAt(textPos);
          if(Character.isWhitespace(symbol)) {
            sb.append((editType==EditType.MisMatch?SYMBOL_SPACE_DELETE:SYMBOL_SPACE));
          } else {
            sb.append(StringEscapeUtils.escapeHtml4(new String(String.valueOf(symbol))));
          }
        }
      }
      sb.append("</html>");
      return sb.toString();
    }  
    
    /**
     * A custom cell renderer for rendering the "Passed" column.
     */
    public static class DefaultColumnRenderer extends DefaultTableCellRenderer { 
      private Color defaultBackgroundColor = null;
      private static final Color inactiveBackgroundColor= Color.LIGHT_GRAY;
      @Override
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {

        super.getTableCellRendererComponent(table, value, isSelected,
                                            hasFocus, row, column);

        Color color = getBackground();
        if(defaultBackgroundColor==null) defaultBackgroundColor = color;
        if(color.equals(defaultBackgroundColor) || color.equals(inactiveBackgroundColor)) {
        
          int modelRow = table.getRowSorter().convertRowIndexToModel(row);
//          Color color = getBackground();
//          JFrame frame;
//          frame.getRootPane().
          //System.out.println("getTableCellRendererComponent:BackgroundColor:" + color.toString());
          if(((SimSearchTableModel) table.getModel()).isMerged(modelRow)) { // || ((SimSearchTableModel) table.getModel()).getRemove(modelRow)) {
            setBackground(inactiveBackgroundColor);
          } else {
            setBackground(defaultBackgroundColor);
          }
        }
        return this;
      }
    }
    
    /**
     * A custom cell renderer for rendering the "Passed" column.
     */
    public static class AlignmentColumnRenderer extends DefaultColumnRenderer { 
    
    	private static Font font;
      
      private String[] data;
      
      AlignmentColumnRenderer(int rowCount) {
        super();
        data = new String[rowCount];
        //this.setFont(new Font(Font.MONOSPACED,Font.PLAIN, this.getFont().getSize()));
      }
      
      @Override
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {

        super.getTableCellRendererComponent(table, value, isSelected,
                                            hasFocus, row, column);

        if(font==null) {
          AlignmentColumnRenderer.font = new Font(Font.MONOSPACED, Font.PLAIN, table.getFont().getSize());
        }
        setFont(font);
        int modelRow = table.getRowSorter().convertRowIndexToModel(row);
        
        if(data[modelRow]==null)
          try {
            data[modelRow] = (value==null? "": createHtmlCode((Alignment.AlignedSequence) value));
          } catch (Exception e) {
            // TODO Auto-generated catch block
            data[modelRow] = String.format("<html><font color=\"red\" style=\"italic\">Could not visualize edits: </font>%s</html>", StringEscapeUtils.escapeHtml4(((Alignment.AlignedSequence) value).getSequence()));
            e.printStackTrace();
          }
        
        //System.out.println("AL:" + data[row]);
        setText(data[modelRow]);
        setHorizontalAlignment(SwingConstants.CENTER);

        return this;
      }
    }
    
    /**
     * A custom cell renderer for rendering the "Passed" column.
     */
    protected static class ListColumnRenderer extends DefaultColumnRenderer {
      
      private String[] data;
      private final String JOIN; 
      
      public ListColumnRenderer(int rowCount, String join) {
        super();
        data = new String[rowCount];
        this.JOIN = join;
      }
      
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {

        super.getTableCellRendererComponent(table, value, isSelected,
                                            hasFocus, row, column);

        int modelRow = table.getRowSorter().convertRowIndexToModel(row);
        if(data[modelRow]==null) data[modelRow] = (value==null? "": String.join(this.JOIN, ((List<String>) value)));
        //System.out.println(data[row]);
        setText(data[modelRow]);
        setHorizontalAlignment(SwingConstants.CENTER);

        return this;
      }
    }
    
    /**
     * A custom cell renderer for rendering the "Passed" column.
     */
    protected static class RowHeaderColumnRenderer extends DefaultTableCellRenderer {
      
      private String[] data;
      private static final String SYMBOL_SIM_REFERENCE = "&#9733;";
      //private static final String SYMBOL_ALIGNMENT_REFERENCE = "A";
      //private static final String SYMBOL_REFERENCE = "T";
      private static final String SYMBOL_MERGEDTO_PRESENT_ROW = "&#8593;";
      private static final String SYMBOL_MERGEDTO_NON_PRESENT_ROW = "&#8625;"; //"&#8592;";
      //private static final String SYMBOL_REMOVE = "<font color=\"red\">x</font>"; //<font color=\"red\">&#10799;</font>";
      private static final String SYMBOL_GAP = "&nbsp;";
      
      public RowHeaderColumnRenderer(int rowCount) {
        super();
        data = new String[rowCount];
//        this.setForeground(//c);
      }
      
      private String createRowHeaderText(JTable table, int modelRow) {
        SimSearchTableModel tableModel = (SimSearchTableModel) table.getModel();
        List<String> tagList = new ArrayList<>();

        if(tableModel.isSimReferenceRow(modelRow)) tagList.add(SYMBOL_SIM_REFERENCE);
//        {
//          tagList.add( (tableModel.isAlignmentReferenceRow(modelRow))?SYMBOL_REFERENCE:SYMBOL_SIM_REFERENCE );
//        } else if (tableModel.isAlignmentReferenceRow(modelRow)) tagList.add(SYMBOL_ALIGNMENT_REFERENCE);
        
        if(tableModel.isMerged(modelRow)) {
          if(tableModel.getMergeTo(modelRow)>=0) {
            tagList.add(SYMBOL_MERGEDTO_PRESENT_ROW);
          } else {
            tagList.add(SYMBOL_MERGEDTO_NON_PRESENT_ROW);
          }
        } 
        
        if(tableModel.getMergeCount(modelRow)>0) tagList.add("&#43;" + tableModel.getMergeCount(modelRow) );
        
//        if(tableModel.getEffectiveRemove(modelRow)) {
//          tagList.add(SYMBOL_REMOVE);
//        }
        
        return "<html>" + String.join(SYMBOL_GAP, tagList) + "</html>";
      }
      
      public void emptyCache() {
        Arrays.fill(data, null);
      }
      
      @Override
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {

        super.getTableCellRendererComponent(table, value, isSelected,
                                            hasFocus, row, column);
        
        if(isSelected) {
          setBackground(UIManager.getColor("Table.selectionBackground"));
          //setBorder(UIManager.getColor("TableHeader.cellBorder"));
        } else {
          setBackground(table.getTableHeader().getBackground());
        }
        //setBackground(table.getTableHeader().getBackground());
        //setBorder(table.getTableHeader().getBorder());
        setFont(new Font("Courier New", Font.PLAIN, table.getTableHeader().getFont().getSize()));
        //setForeground(c);
        
//        table.getTableHeader().getDefaultRenderer()
//        .getTableCellRendererComponent(table, table.getValueAt(
//            row, column), false, false, row, column);

        int modelRow = table.getRowSorter().convertRowIndexToModel(row);
        if(data[modelRow]==null) data[modelRow] = createRowHeaderText(table, modelRow);
        
        //System.out.println(data[row]);
        setText(data[modelRow]);
        setHorizontalAlignment(SwingConstants.CENTER);

        return this;
      }
    }
	
	//private SimSearchTableModel table;
    //private Set<Integer> selectedIds;
    
    //private final boolean isRowHeaderTable;
	
	public SimSearchJTable(boolean isRowHeaderTable) {
	  super();
	  //this.isRowHeaderTable = isRowHeaderTable;
	}
	
//	@Override
//    public Component prepareRenderer(
//        TableCellRenderer renderer, int row, int col) {
//        if (col == 0 && this.isRowHeaderTable) {
//            return this.getTableHeader().getDefaultRenderer()
//                .getTableCellRendererComponent(this, this.getValueAt(
//                    row, col), false, false, row, col);
//        } else {
//            return super.prepareRenderer(renderer, row, col);
//        }
//    }
//	private int[] convertViewRowsToModelRows(int[] rows) {
//	  for(int i=0; i<rows.length; ++i) rows[i] = this.getRowSorter().convertRowIndexToModel(rows[i]);
//	  return rows;
//	}
	
	public void initCellRenderers(SimSearchTableModel tableModel) {
	  for(int column=0; column < this.getColumnCount(); ++column) {
        int modelColumnIndex = this.convertColumnIndexToModel(column); //    getColumnModel().getColumn(column).getModelIndex(); //    this.getColumn(column).getModelIndex();
        if(modelColumnIndex==0) {
          this.getColumnModel().getColumn(column).setCellRenderer(new RowHeaderColumnRenderer(tableModel.getRowCount()));
        } else if(tableModel.getColumnClass(modelColumnIndex)==Alignment.AlignedSequence.class) {
          this.getColumnModel().getColumn(column).setCellRenderer(new AlignmentColumnRenderer(tableModel.getRowCount()));
        } else if(tableModel.getColumnClass(modelColumnIndex).equals(List.class)) {
          this.getColumnModel().getColumn(column).setCellRenderer(new ListColumnRenderer(tableModel.getRowCount(),"; "));
        } else {
          this.getColumnModel().getColumn(column).setCellRenderer(new DefaultColumnRenderer());
        }
      }
	}
	
//	@Override
//	public TableModel getModel() { return this.tableModel; }
//	
//	@Override
//	public void setModel(TableModel model) {
//	  if(model == null) {
//	    this.tableModel = null;
//	    while(this.getColumnCount()>0) this.removeColumn(this.getColumnModel().getColumn(0));
//	  } else if(model instanceof SimSearchTableModel) {
//	    this.tableModel = (SimSearchTableModel) model;
//	    super.setModel(model);
//	  } else super.setModel(model);
//    	 // int tmp_n = this.getColumnCount();
////    	  for(int column=0; column < this.getColumnCount(); ++column) {
////    //	    if(model.getColumnClass( modelColumnIndex)==Alignment.AlignedSequence.class) {
////    //          this.getColumn(column).setCellRenderer(new AlignmentColumnRenderer(model.getRowCount()));
////    //        } else if(model.getColumnClass(modelColumnIndex).equals(List.class)) {
////    //          this.getColumn(column).setCellRenderer(new ListColumnRenderer(model.getRowCount(),"; "));
////    //        }
////    	  //for(int column = 0; column < model.getColumnCount(); ++column) 
////    	    //@SuppressWarnings("unused")
////    	    //this.getColumnClass(column)
////            //TableColumn tableColumn =   this.getColumn(column);
////    	    int modelColumnIndex = this.getColumnModel().getColumn(column).getModelIndex(); //    this.getColumn(column).getModelIndex();
////    	    TableColumn tmp_1 = this.getColumnModel().getColumn(column);
////    	    TableColumn tmp_2 = this.getColumn(tmp_1.getIdentifier());
////    	    boolean tmp_a = tmp_1==tmp_2;
////    	    if(modelColumnIndex==0) {
////    	      this.getColumnModel().getColumn(column).setCellRenderer(new RowHeaderColumnRenderer(model.getRowCount()));
////    	    } else if(model.getColumnClass(modelColumnIndex)==Alignment.AlignedSequence.class) {
////    	      this.getColumnModel().getColumn(column).setCellRenderer(new AlignmentColumnRenderer(model.getRowCount()));
////    	    } else if(model.getColumnClass(modelColumnIndex).equals(List.class)) {
////    	      this.getColumnModel().getColumn(column).setCellRenderer(new ListColumnRenderer(model.getRowCount(),"; "));
////    	    } else {
////    	      this.getColumnModel().getColumn(column).setCellRenderer(new DefaultColumnRenderer());
////    	    }
////    	  }
////	  }
////	  for(int i=0; i<this.getColumnCount(); ++i) System.out.println(this.getColumnModel().getColumn(i).getIdentifier() + ":" + this.getColumnModel().getColumn(i).getCellRenderer());
////	  if(2<this.getColumnCount()) {
////	    this.getColumnModel().getColumn(2).setCellRenderer(new PassedColumnRenderer());
////	    System.out.println(this.getColumnModel().getColumn(2).getIdentifier() + ":" + this.getColumnModel().getColumn(2).getCellRenderer());
////	  }
//	}
	
	
	public List<Integer> getSelectedIds() {
	  if(!(super.getModel() instanceof SimSearchTableModel)) return null;
	  SimSearchTableModel tableModel = (SimSearchTableModel) super.getModel();
	  List<Integer> idList = new ArrayList<>();
	  for(int row=0; row<this.getRowCount(); ++row) if(this.isRowSelected(row)) idList.add(tableModel.getID(this.convertRowIndexToModel(row)));
	  return idList;
	}

	public void applyIdSelection(List<Integer> idList) {
	  if(!(super.getModel() instanceof SimSearchTableModel)) return;
	  SimSearchTableModel tableModel = (SimSearchTableModel) super.getModel();
	  for(int row=0; row<this.getRowCount(); ++row) if(idList.contains(tableModel.getID(this.convertRowIndexToModel(row)))) this.addRowSelectionInterval(row, row);
	}
	
	@Override
    public void createDefaultColumnsFromModel() {
        if(this.getModel()!=null) {
            super.createDefaultColumnsFromModel();
//            for(int i=0; i<this.getColumnCount(); ++i) {
//            	if(this.getModel().getColumnClass(this.getColumnModel().getColumn(i).getModelIndex())==Alignment.AlignedSequence.class) {
//            		int modelIndex = this.getColumnModel().getColumn(i).getModelIndex();
//            		for(int row=0; row<this.getRowCount(); ++row) {
//            			if(this.getModel().getValueAt(row, modelIndex)!=null && ((Alignment.AlignedSequence) this.getModel().getValueAt(row, modelIndex)).getEditOperations()!=null) {
//            				try {
//								String text = createHtmlCode((Alignment.AlignedSequence) this.getModel().getValueAt(row, modelIndex));
//								break;
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//            			}
//            		}
//            	}
//            }
        }
    }
	
//	@Override
//    public Component prepareRenderer(
//        TableCellRenderer renderer, int row, int col) {
//        if (col == 0) {
//            return this.getTableHeader().getDefaultRenderer()
//                .getTableCellRendererComponent(this, this.getValueAt(
//                    row, col), false, false, row, col);
//        } else {
//            return super.prepareRenderer(renderer, row, col);
//        }
//    }
	
	@Override
	public void setModel(TableModel model) {
	  if(model==null) super.setModel(this.createDefaultDataModel());
	  else super.setModel(model);
	}
	
	public void removeColumns() {
	  TableColumnModel columnModel = this.getColumnModel();
	  while (columnModel.getColumnCount() > 0) { columnModel.removeColumn(columnModel.getColumn(0)); }
	}
	
	//public boolean isRowHeaderTable() { return this.isRowHeaderTable; }
}
