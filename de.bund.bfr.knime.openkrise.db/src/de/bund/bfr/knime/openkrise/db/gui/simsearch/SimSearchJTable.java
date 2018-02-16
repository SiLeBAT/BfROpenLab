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
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.commons.text.StringEscapeUtils;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.GuiMessages;

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
		
	private SimSearchJTable partnerTable;
	//private SimSearchTable.ViewSettings viewSettings;
	
	public class HeaderRenderer implements TableCellRenderer {
	    private final JTableHeader header;

	    public HeaderRenderer(JTableHeader header) {
	        this.header = header;
	    }

	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        JLabel label = (JLabel) header.getDefaultRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        label.setHorizontalAlignment(SwingConstants.CENTER);
	        label.setText(GuiMessages.getString(table.getColumnName(column)));
	        return label;
	    }
	}
	
	private interface ToolTipRenderer {
	  String createToolTipText(SimSearchTableModel tableModel, int rowIndex, int columnIndex);
	}
	
	private class SimSearchJTableHeader extends JTableHeader {
	  SimSearchJTableHeader(TableColumnModel columnModel) {
	    super(columnModel);
	  }
	  
	  @Override
	  public String getToolTipText(MouseEvent e) {
        //String tip = null;
        java.awt.Point p = e.getPoint();
        
        int viewIndex = columnModel.getColumnIndexAtX(p.x);
        if(viewIndex>=0) {
          int modelIndex = columnModel.getColumn(viewIndex).getModelIndex();
          if(this.table!=null && this.table.getModel()!=null && (this.table.getModel() instanceof SimSearchTableModel)) {
            return GuiMessages.getString(((SimSearchTableModel) this.table.getModel()).getColumnComment(modelIndex));
          }
        }
        return null;
//        Container tmp = this.getParent(); 
//        
//        Object tmp2 = e.getSource();
//        return ""; //    columnToolTips[realIndex];
      }
	  
	  
	}
	
	@Override
	protected JTableHeader createDefaultTableHeader() {
      return new SimSearchJTableHeader(this.getColumnModel());
    }
	 
	SimSearchJTable() { //SimSearchTable.ViewSettings viewSettings) {
	  super();
	  //this.viewSettings = viewSettings;
	  //this.getTableHeader().setAlignmentX(CENTER_ALIGNMENT);
	}
	
	protected void setPartnerTable(SimSearchJTable partnerTable) {
		this.partnerTable = partnerTable;
	}
	
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
	
    /**
     * A custom cell renderer for rendering the "Passed" column.
     */
    public static class DefaultColumnRenderer extends DefaultTableCellRenderer { 
      //private Color defaultBackgroundColor = null;
      private static final Color COLOR_INACTIVEROW_BACKGROUND = Color.LIGHT_GRAY;
      private static Color COLOR_DROPLOCATION_BACKGROUND = null;
      static final int EXTRA_X_MARGIN = 4;
      static final int EXTRA_Y_MARGIN = 2;
      static final int INNER_MARGIN_X = 10;
      static final int INNER_MARGIN_Y = 10;
      
      @Override
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected,
                                            hasFocus, row, column);
        Border border = ((JLabel) c).getBorder();
        if(border!=null) 
          if(!(border instanceof CompoundBorder)) {
            border = new CompoundBorder(border, new EmptyBorder(INNER_MARGIN_X,INNER_MARGIN_Y,INNER_MARGIN_X,INNER_MARGIN_Y));
            ((JLabel) c).setBorder(border);
          }
        JTable.DropLocation dropLocation = table.getDropLocation();
        TransferHandler transferHandler = table.getTransferHandler();
        //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int dropLocationRow = ((SimSearchJTable) table).getDropLocationRow();
//        if(transferHandler!=null && (transferHandler instanceof SimSearchTableRowTransferHandler)) {
//        	SimSearchTableRowTransferHandler simSearchTransferHandler = (SimSearchTableRowTransferHandler) transferHandler;
//        	if(simSearchTransferHandler.isMergeDropLocation()) dropLocationRow = simSearchTransferHandler.getDropLocationRow();
//        }
        //if(table.getTransferHandler()!=null && (table.getTransferHandler() instanceof SimSearchTransferHandler)) mergeRow = ((SimSearchTransferHandler) table.getTransferHandler()).
        //int mergeRow = table.getTransferHandler().
        //if(dropLocation!=null && 
        //		!dropLocation.isInsertRow() && !dropLocation.isInsertColumn() &&
        //		dropLocation.getRow()==row) {
        if(dropLocationRow==row) {
        	if(COLOR_DROPLOCATION_BACKGROUND==null && dropLocation!=null && dropLocation.getColumn()==column) COLOR_DROPLOCATION_BACKGROUND = getBackground();
        	if(COLOR_DROPLOCATION_BACKGROUND!=null) setBackground(COLOR_DROPLOCATION_BACKGROUND);
        	//setBackground(UIManager.getColor("Table.highlight"));
        } 
        //else if(isSelected) {
        //	setBackground(UIManager.getColor("Table.selectionBackground"));
        //} else if(hasFocus) {
        //	setBackground(UIManager.getColor("Table.focusCellBackground"));
        else {
          if(hasFocus && UIManager.getColor("Table.focusCellBackground")!=table.getBackground()) setBackground(UIManager.getColor("Table.focusCellBackground"));
          else if(isSelected && UIManager.getColor("Table.selectionBackground")!=table.getBackground()) setBackground(UIManager.getColor("Table.selectionBackground"));
        	//if(getBackground().equals(COLOR_DROPLOCATION_BACKGROUND)) setBackground(UIManager.getColor("Table.background"));
          
          else {	//if(getBackground().equals(UIManager.getColor("Table.background")))   {
        
            //Color color = getBackground();
            //if(defaultBackgroundColor==null) defaultBackgroundColor = color;
            //if(color.equals(defaultBackgroundColor) || color.equals(inactiveBackgroundColor)) {

            //int modelRow = table.getRowSorter().convertRowIndexToModel(row);
            int modelRow = table.convertRowIndexToModel(row);
            //          Color color = getBackground();
            //          JFrame frame;
            //          frame.getRootPane().
            //System.out.println("getTableCellRendererComponent:BackgroundColor:" + color.toString());
            if(((SimSearchTableModel) table.getModel()).isMerged(modelRow)) { // || ((SimSearchTableModel) table.getModel()).getRemove(modelRow)) {
              setBackground(COLOR_INACTIVEROW_BACKGROUND);
            } else {
              setBackground(null);
            }
            //else {
            //  setBackground(UIManager.getColor("Table.background"));
            //}
          }
        }
        return this;
      }
      
      public int getFittingWidth(int column, JTable table) {
        FontMetrics fm = table.getFontMetrics( table.getFont() );
        int width = fm.stringWidth((String) table.getColumnModel().getColumn(column).getHeaderValue());
        
        for(int row=0; row<table.getRowCount(); ++row) {
            Object o = table.getValueAt(row, column);
            if(o!=null) width = Math.max(width, fm.stringWidth(o.toString()));
        }
        
        return width + ((int) (2*table.getIntercellSpacing().getWidth())) + EXTRA_X_MARGIN + 2*INNER_MARGIN_X;        
      }
      
      public int getFittingHeight(int row, int column, JTable table) {
        FontMetrics fm = table.getFontMetrics( table.getFont() );
        return fm.getHeight() + ((int) (2*table.getIntercellSpacing().getHeight())) + SimSearchTable.ViewSettings.variableYMargin + EXTRA_Y_MARGIN + 2*INNER_MARGIN_Y;      
      }
    }
    
    
    /**
     * A custom cell renderer for rendering the "Passed" column.
     */
    public static class AlignmentColumnRenderer extends DefaultColumnRenderer implements ToolTipRenderer { 
    
    	private static Font font;
    	public static final String SYMBOL_GAP = "&#752;";
    	public static final String SYMBOL_SPACE_DELETE = "&#8718;";//"&#8215;";
    	      
      private String[] data;
      
      public static String getColoredMismatchText(String text) {
        return "<font color=\"red\">" + text + "</font>";
      }
      
      public static String getColoredNeutralGap() {
        return "<font color=\"green\">" + SYMBOL_GAP + "</font>";
      }
      
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

        //if(font==null) {
        //  AlignmentColumnRenderer.font = new Font(Font.MONOSPACED, Font.PLAIN, table.getFont().getSize());
        //}
        //setFont(font);
       
        int modelRow = table.getRowSorter().convertRowIndexToModel(row);
        
        if(data[modelRow]==null)
          try {
            data[modelRow] = (value==null? "": createHtmlCode((Alignment.AlignedSequence) value, true));
          } catch (Exception e) {
            // TODO Auto-generated catch block
            data[modelRow] = String.format("<html><font color=\"red\" style=\"italic\">Could not visualize edits: </font>%s</html>", StringEscapeUtils.escapeHtml4(((Alignment.AlignedSequence) value).getSequence()));
            e.printStackTrace();
          }
        
        //System.out.println("AL:" + data[row]);
        setText(data[modelRow]);
        //setHorizontalAlignment(SwingConstants.CENTER);

        return this;
      }
      
      public static String createHtmlCode(Alignment.AlignedSequence alignedSequence, boolean addHtmlTag) throws Exception {
        if(alignedSequence==null || alignedSequence.getEditOperations()==null || alignedSequence.getEditOperations().isEmpty()) {
          return "";
        }
        StringBuilder sb = new StringBuilder((addHtmlTag?"<html><nobr>":"")+"<font face=\"Monospaced\">"); //+"<nobr>");
        //final String SYMBOL_GAP = "&#8911;"; //"&#128;"; //"&#8248;"; //"&#752;"; //"&#x022CF;"; // "&cuwed"; 
        final String SYMBOL_GAP = "&#752;"; //&#8743;";
        //final String SYMBOL_SPACE_DELETE = "&#9618;"; //"&#x02592;"; // "&block;"; 
        final String SYMBOL_SPACE_DELETE = "&#8718;";//"&#8215;";
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
        //sb.append("</nobr>");
        sb.append("</font>");
        if(addHtmlTag) sb.append("<nobr></html>");
        return sb.toString();
      }  
      
      @Override
      public String createToolTipText(SimSearchTableModel tableModel, int rowIndex, int columnIndex) {
        if(tableModel!=null) {
          Object data = tableModel.getValueAt(rowIndex, columnIndex);
          if(data!=null && (data instanceof Alignment.AlignedSequence)) {
            Alignment.AlignedSequence alignedSeq = (Alignment.AlignedSequence) data;
            if(rowIndex==tableModel.getReferenceRow()) return alignedSeq.getSequence();
            
            if(alignedSeq.getEditOperations()==null) return alignedSeq.getSequence();
            
            Alignment.AlignedSequence alignedRefSeq = (Alignment.AlignedSequence) tableModel.getValueAt(tableModel.getReferenceRow() , columnIndex);
            alignedSeq = new Alignment.AlignedSequence(alignedSeq.getSequence(), new ArrayList<>(alignedSeq.getEditOperations()));
            alignedRefSeq = new Alignment.AlignedSequence(alignedRefSeq.getSequence(), new ArrayList<>(alignedRefSeq.getEditOperations()));
            
            for(int i=alignedSeq.getEditOperations().size()-1; i>=0; --i) 
              if(alignedSeq.getEditOperations().get(i)==Alignment.EditOperation.GAP && alignedRefSeq.getEditOperations().get(i)==Alignment.EditOperation.GAP) {
                alignedSeq.getEditOperations().remove(i);
                alignedRefSeq.getEditOperations().remove(i);
              }
            
            try {
              return "<html><table>\n" + 
                  "  <tr>\n" + 
                  "    <td>" + RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + "</td>\n" + 
                  "    <td>" + createHtmlCode(alignedRefSeq, false)  + "</td>\n" + 
                  "  </tr>\n" + 
                  "  <tr>\n" + 
                  "    <td></td>\n" + 
                  "    <td>" + createHtmlCode(alignedSeq, false)  + "</td>\n" + 
                  "  </tr>\n" + 
                  "</table></html>";
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
              return null;
            }
          }
        }
        return null;
      }
      
      @Override
      public int getFittingWidth(int column, JTable table) {
        int width = table.getFontMetrics(table.getFont()).stringWidth((String) table.getColumnModel().getColumn(column).getHeaderValue());
        
        FontMetrics fm = table.getFontMetrics( new Font("Monospaced",Font.PLAIN,table.getFont().getSize()) );
        
        final double MONOSPACE_CHAR_WIDTH =  ((double) fm.stringWidth(String.join("", Collections.nCopies(100, "W"))))/100;
        
        for(int row=0; row<table.getRowCount(); ++row) {
            Object o = table.getValueAt(row, column);
            if(o!=null && (o instanceof Alignment.AlignedSequence)) {
              Alignment.AlignedSequence alignedSeq = (Alignment.AlignedSequence) o;
              if(alignedSeq.getEditOperations()!=null)   width = Math.max(width, (int) (alignedSeq.getEditOperations().size()*MONOSPACE_CHAR_WIDTH));
              else if(alignedSeq.getSequence()!=null)  width = Math.max(width, (int) (alignedSeq.getSequence().length()*MONOSPACE_CHAR_WIDTH));
            }
        }
        
        return width + ((int) (2*table.getIntercellSpacing().getHeight())) + EXTRA_X_MARGIN + 2*INNER_MARGIN_X;        
      }
    }
    
    /**
     * A custom cell renderer for rendering the "Passed" column.
     */
    protected static class ListColumnRenderer extends DefaultColumnRenderer {
      
      private String[] data;
      //private final String JOIN; 
      
      public ListColumnRenderer(int rowCount) {
        super();
        data = new String[rowCount];
        //this.JOIN = join;
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
        if(data[modelRow]==null) data[modelRow] = (value==null? "": createHtml((List<String>) value)); //   String.join(this.JOIN, ((List<String>) value)));
        //System.out.println(data[row]);
        setText(data[modelRow]);
        //setHorizontalAlignment(SwingConstants.);

        return this;
      }
      
      private String createHtml(List<String> list) {
        StringBuilder sb = new StringBuilder("<html>");
        for(String element: list) sb.append(StringEscapeUtils.escapeHtml4(element) + "<br>");
        sb.append("</html>");
        return sb.toString();
      }
      
      public int getFittingWidth(int column, JTable table) {
        FontMetrics fm = table.getFontMetrics( table.getFont() );
        int width = fm.stringWidth((String) table.getColumnModel().getColumn(column).getHeaderValue());
        
        for(int row=0; row<table.getRowCount(); ++row) {
            Object o = table.getValueAt(row, column);
            if(o!=null) for(String element: (List<String>) o) width = Math.max(width, fm.stringWidth(element));
        }
        
        return width + ((int) (2*table.getIntercellSpacing().getWidth())) + EXTRA_X_MARGIN + 2*INNER_MARGIN_X;        
      }
      
      @Override
      public int getFittingHeight(int row, int column, JTable table) {
        FontMetrics fm = table.getFontMetrics( table.getFont() );
        int width = fm.getHeight(); 
        
        Object o = table.getValueAt(row, column);
        if(o!=null) width *= Math.max(((List<String>) o).size(),1); // width = Math.max(width, fm.stringWidth(element));
        return width + EXTRA_Y_MARGIN + 2*INNER_MARGIN_Y+ SimSearchTable.ViewSettings.variableYMargin + ((int) (2*table.getIntercellSpacing().getHeight()));      
      }
    }
    
    /**
     * A custom cell renderer for rendering the "Passed" column.
     */
    protected static class RowHeaderColumnRenderer extends DefaultTableCellRenderer implements ToolTipRenderer {
      
      private String[] data;
//      private String[] toolTip;
//      private boolean[] toolTipWasSet;
      
      public static final String HTML_SYMBOL_SIM_REFERENCE = "&#9733;";
      //private static final String SYMBOL_ALIGNMENT_REFERENCE = "A";
      //private static final String SYMBOL_REFERENCE = "T";
      private static final String SYMBOL_MERGEDTO_PRESENT_ROW = "&#8593;";
      private static final String SYMBOL_MERGEDTO_NON_PRESENT_ROW = "&#8625;"; //"&#8592;";
      //private static final String SYMBOL_REMOVE = "<font color=\"red\">x</font>"; //<font color=\"red\">&#10799;</font>";
      private static final String SYMBOL_GAP = "&nbsp;";
      
      public RowHeaderColumnRenderer(int rowCount) {
        super();
        data = new String[rowCount];
        //toolTip = new String[rowCount];
//        this.setForeground(//c);
      }
      
      private String createRowHeaderText(JTable table, int modelRow) {
        SimSearchTableModel tableModel = (SimSearchTableModel) table.getModel();
        List<String> tagList = new ArrayList<>();

        if(tableModel.isSimReferenceRow(modelRow)) tagList.add(HTML_SYMBOL_SIM_REFERENCE);
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
        
        
        //setToolTipText("Row: " + row + ", Column: " + column);
        
        if(isSelected) {
          setBackground(UIManager.getColor("Table.selectionBackground"));
          //setBorder(UIManager.getColor("TableHeader.cellBorder"));
        } else {
          setBackground(table.getTableHeader().getBackground());
        }
        //setBackground(table.getTableHeader().getBackground());
        //setBorder(table.getTableHeader().getBorder());
        //setFont(new Font("Courier New", Font.PLAIN, table.getTableHeader().getFont().getSize()));
        //setForeground(c);
        
//        table.getTableHeader().getDefaultRenderer()
//        .getTableCellRendererComponent(table, table.getValueAt(
//            row, column), false, false, row, column);

        int modelRow = table.getRowSorter().convertRowIndexToModel(row);
        if(data[modelRow]==null) data[modelRow] = createRowHeaderText(table, modelRow);
//          toolTip[modelRow] = createToolTip(table, modelRow);
//        }
      
        //System.out.println(data[row]);
        setText(data[modelRow]);
        setHorizontalAlignment(SwingConstants.CENTER);
        
//        setToolTipText(toolTip[modelRow]);
       
        return this;
      }
      
//      private String createToolTip(JTable table, int modelRow) {
//        if(table.getModel()!=null && table.getModel() instanceof SimSearchTableModel) {
//          SimSearchTableModel tableModel = (SimSearchTableModel) table.getModel();
//          List<String> toolTipText = new ArrayList<>();
//          if(tableModel.isSimReferenceRow(modelRow)) toolTipText.add("This is the reference row.");
//          if(tableModel.isMerged(modelRow)) toolTipText.add("The row was merged.");
//          if(tableModel.getMergeCount(modelRow)>0) toolTipText.add(tableModel.getMergeCount(modelRow) + " rows were merged into this row.");
//          return (toolTipText.isEmpty()?null:String.join(" ", toolTipText));
//        } else return null;
//      }
      
      @Override
      public String createToolTipText(SimSearchTableModel tableModel, int modelRow, int modelColumn) {
        if(tableModel!=null) {
          //SimSearchTableModel tableModel = (SimSearchTableModel) table.getModel();
          List<String> toolTipText = new ArrayList<>();
          if(tableModel.isSimReferenceRow(modelRow)) toolTipText.add("This is the reference row.");
          if(tableModel.isMerged(modelRow)) toolTipText.add("The row was merged.");
          if(tableModel.getMergeCount(modelRow)>0) toolTipText.add(tableModel.getMergeCount(modelRow) + " rows were merged into this row.");
          return (toolTipText.isEmpty()?null:String.join(" ", toolTipText));
        } else return null;
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
	public void initColumnRenderer(TableColumn tableColumn) {
	  if(this.getModel()==null || !(this.getModel() instanceof SimSearchTableModel)) return;
	  SimSearchTableModel tableModel = (SimSearchTableModel) this.getModel();
	  if(tableColumn.getModelIndex()==0) {
	    tableColumn.setCellRenderer(new RowHeaderColumnRenderer(tableModel.getRowCount()));
	  } else if(tableModel.getColumnClass(tableColumn.getModelIndex())==Alignment.AlignedSequence.class) {
	    tableColumn.setCellRenderer(new AlignmentColumnRenderer(tableModel.getRowCount()));
	  } else if(tableModel.getColumnClass(tableColumn.getModelIndex()).equals(List.class)) {
	    tableColumn.setCellRenderer(new ListColumnRenderer(tableModel.getRowCount()));
	  } else {
	    tableColumn.setCellRenderer(new DefaultColumnRenderer());
	  }
	  tableColumn.setHeaderRenderer(new HeaderRenderer(this.getTableHeader()));
	}
	
	public void initColumnRenderers() {
	  for(int column=0; column < this.getColumnCount(); ++column) {
        TableColumn tableColumn = this.getColumnModel().getColumn(column);
        this.initColumnRenderer(tableColumn);
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
	
//	@Override
//    public void createDefaultColumnsFromModel() {
//        if(this.getModel()!=null) {
//            super.createDefaultColumnsFromModel();
////            for(int i=0; i<this.getColumnCount(); ++i) {
////            	if(this.getModel().getColumnClass(this.getColumnModel().getColumn(i).getModelIndex())==Alignment.AlignedSequence.class) {
////            		int modelIndex = this.getColumnModel().getColumn(i).getModelIndex();
////            		for(int row=0; row<this.getRowCount(); ++row) {
////            			if(this.getModel().getValueAt(row, modelIndex)!=null && ((Alignment.AlignedSequence) this.getModel().getValueAt(row, modelIndex)).getEditOperations()!=null) {
////            				try {
////								String text = createHtmlCode((Alignment.AlignedSequence) this.getModel().getValueAt(row, modelIndex));
////								break;
////							} catch (Exception e) {
////								// TODO Auto-generated catch block
////								e.printStackTrace();
////							}
////            			}
////            		}
////            	}
////            }
//        }
//    }
	
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
	  if(model!=this.getModel()) this.removeColumns();
	  
	  if(model==null) super.setModel(this.createDefaultDataModel());
	  else super.setModel(model);
	}
	
	public void removeColumns() {
	  TableColumnModel columnModel = this.getColumnModel();
	  while (this.getColumnCount() > 0) this.removeColumn(columnModel.getColumn(0)); 
	}
	
	 //Implement table cell tool tips.           
    public String getToolTipText(MouseEvent e) {
        //String tip = null;
      if(this.getModel()==null || !(this.getModel() instanceof SimSearchTableModel)) return null;
      java.awt.Point p = e.getPoint();
      int rowViewIndex = rowAtPoint(p);
      int colViewIndex = columnAtPoint(p);
      
      TableCellRenderer cellRenderer = this.getColumnModel().getColumn(colViewIndex).getCellRenderer();
      if(cellRenderer!=null && (cellRenderer instanceof ToolTipRenderer)) 
        return ((ToolTipRenderer) cellRenderer).createToolTipText((SimSearchTableModel) this.getModel(), this.convertRowIndexToModel(rowViewIndex), this.convertColumnIndexToModel(colViewIndex));
      else
        return null;
      
    }
	//public boolean isRowHeaderTable() { return this.isRowHeaderTable; }
    @Override
    public void setTableHeader(JTableHeader tableHeader) {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      super.setTableHeader(tableHeader);
    }
    
//    public JTable.DropLocation getTableDropLocation() {
//    	
//    }
//    public int getDropLocationRow() {
//    	if(this.getTransferHandler()!=null && (this.getTransferHandler() instanceof SimSearchTableRowTransferHandler)) return ((SimSearchTableRowTransferHandler) this.getTransferHandler()).getDropRow;
//    }
    public int getDropLocationRow() {
    	if(this.getDropLocation()!=null) return this.getDropLocation().getRow();
    	if(this.partnerTable.getDropLocation()!=null) return this.partnerTable.getDropLocation().getRow();
    	return -1;
    }
    
    public SimSearchJTable getPartnerTable() { return this.partnerTable; }
    
    public void adjustColumnWidth(int column) {
      
      TableColumn tableColumn = this.getColumnModel().getColumn(column);
      
      if(tableColumn.getCellRenderer() instanceof DefaultColumnRenderer) tableColumn.setPreferredWidth(((DefaultColumnRenderer) tableColumn.getCellRenderer()).getFittingWidth(column, this));
      
//      
//      
//      headerRenderer = tableColumn.getHeaderRenderer();
//      Component c = (headerRenderer==null?null:headerRenderer.getTableCellRendererComponent(null, tableColumn.getHeaderValue(), false, false, 0, 0));
//      
//      int preferredWidth = Math.max((c==null?0:c.getPreferredSize().width), tableColumn.getMinWidth());
//      int maxWidth = tableColumn.getMaxWidth();
//      
//      TableCellRenderer cellRenderer = null;
//
//      for (int row = 0; row < this.getRowCount(); row++)
//      {
//          if(cellRenderer==null) cellRenderer = this.getCellRenderer(row, column);
//          if(cellRenderer instanceof RowHeaderColumnRenderer) break;
//          
//          c = this.prepareRenderer(cellRenderer, row, column);
//          int width = c.getPreferredSize().width + this.getIntercellSpacing().width;
//          preferredWidth = Math.max(preferredWidth, width);
//
//          //  We've exceeded the maximum width, no need to check other rows
//
//          if (preferredWidth >= maxWidth)
//          {
//              preferredWidth = maxWidth;
//              break;
//          }
//      }
//
//      tableColumn.setPreferredWidth( preferredWidth );
    }
    
    public void adjustTableColumns() {
      for (int column = 0; column < this.getColumnCount(); column++) this.adjustColumnWidth(column);
    }
}
