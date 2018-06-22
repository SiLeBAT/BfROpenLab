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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.GuiMessages;
import sun.swing.SwingUtilities2;

public class SimSearchJTable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7055490193917189461L;

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

	private SimSearchJTable partnerTable;
	private RowResizeAdapter rowResizeAdapter;
	private List<MouseListener> mouseListeners;
	private SimSearchTable.ViewSettings viewSettings;
	private double[] unzoomedRowHeights;

	SimSearchJTable(SimSearchTable.ViewSettings viewSettings) { 
		super();
		this.viewSettings = viewSettings;
	}

	public SimSearchJTable getPartnerTable() { return this.partnerTable; }

	protected void setPartnerTable(SimSearchJTable partnerTable) {
		this.partnerTable = partnerTable;
	}

	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new SimSearchJTableHeader(this.getColumnModel());
	}
	
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

	protected int getPreferredRowHeight(int row) {
		int height = 0;
		for(int column=0; column<this.getColumnCount(); ++column) {
			TableColumn tableColumn = this.getColumnModel().getColumn(column);
			if(tableColumn==null) continue;
			TableCellRenderer renderer = tableColumn.getCellRenderer();
			if(renderer==null) continue;
			Component c = renderer.getTableCellRendererComponent(this, this.getValueAt(row,column), false,
					false, row, column);
			if(c!=null) height = Math.max(height, c.getPreferredSize().height);
		}
		return height;
	}

	public int getPreferredColumnWidth(int column) {

		TableColumn tableColumn = this.getColumnModel().getColumn(column);

		if (tableColumn.getPreferredWidth() == 0) return 0;

		int width = tableColumn.getMinWidth();

		TableCellRenderer renderer = tableColumn.getHeaderRenderer();
		Component comp = this.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(this,
				tableColumn.getHeaderValue(), false, false, 0, 0);
		width = Math.max(width, comp.getPreferredSize().width + 2*viewSettings.getCellMarginX());

		for (int r = 0; r < this.getRowCount(); r++) {
			renderer = this.getCellRenderer(r, column);
			comp = renderer.getTableCellRendererComponent(this, this.getValueAt(r, column), false, false, r, column);
			width = Math.max(width, comp.getPreferredSize().width);
		}

		width = Math.min(width, tableColumn.getMaxWidth());

		return width;
	}
	
//	@Override 
//	public void setFont(Font font) {
//	  this.
//	}
	
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

	//Implements table cell tool tips.           
	public String getToolTipText(MouseEvent e) {

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

	public void setUnzoomedRowHeights(double[] unzoomedRowHeights) {
		this.unzoomedRowHeights = unzoomedRowHeights;
		for(int i=0; i<this.getRowCount(); ++i) {
		    int zoomedRowHeight = (int) (unzoomedRowHeights[i] * viewSettings.getZoom());
			this.setRowHeight(i, zoomedRowHeight);
			this.partnerTable.setRowHeight(i, zoomedRowHeight);
		}
	}

	public void addMouseListener(MouseListener listener) {
		if(this.mouseListeners==null) this.mouseListeners = new ArrayList<>();
		this.mouseListeners.add(listener);
		super.addMouseListener(listener);
	}

	public void removeMouseListener(MouseListener listener) {
		if(this.mouseListeners==null) this.mouseListeners = new ArrayList<>();
		this.mouseListeners.remove(listener);
		super.removeMouseListener(listener);
	}

	public boolean isRowResizingAllowed() { return this.rowResizeAdapter != null; }

	public void setRowResizingAllowed(boolean value) {
		if(value!=this.isRowResizingAllowed()) {
			if(value) {
				this.rowResizeAdapter = new RowResizeAdapter();
				for(MouseListener listener : this.mouseListeners) super.removeMouseListener(listener);
				this.mouseListeners.add(0,this.rowResizeAdapter);
				for(MouseListener listener : this.mouseListeners) super.addMouseListener(listener);
				this.addMouseMotionListener(this.rowResizeAdapter);
			} else {
				this.removeMouseListener(this.rowResizeAdapter);
				this.removeMouseMotionListener(this.rowResizeAdapter);
				this.rowResizeAdapter = null;
			}
		}
	}

	private class RowResizeAdapter extends MouseAdapter {

		private final Cursor ROW_RESIZE_CURSOR = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);

		private Cursor otherCursor = ROW_RESIZE_CURSOR;
		private int yBeforeDrag;
		private int hBeforeDrag;
		private int rowDragged;
		private final int MIN_ROW_HEIGHT = 5;

		public void mouseMoved(MouseEvent e) {
			JTable table = (JTable) e.getSource();
			int row = -1;
			int column = table.columnAtPoint(e.getPoint());
			if(column==0) row = getResizingRow(e.getPoint(), table);
			if((row>=0) != (table.getCursor()==ROW_RESIZE_CURSOR)) swapCursor(table);
		}

		public void mousePressed(MouseEvent e) {
			JTable table = (JTable) e.getSource();
			int row = -1;
			int column = table.columnAtPoint(e.getPoint());
			if(column==0) row = getResizingRow(e.getPoint(), table);
			if(row>=0) {
				e.consume();
				rowDragged = row;
				yBeforeDrag = e.getPoint().y;
				hBeforeDrag = table.getRowHeight(row);
			}
		}

		private void swapCursor(JTable table) {
			Cursor tmp = table.getCursor();
			table.setCursor(otherCursor);
			otherCursor = tmp;
		}

		private int getResizingRow(Point p, JTable table) {
			int row = table.rowAtPoint(p);
			if (row == -1) {
				return -1;
			}
			Rectangle r = SimSearchJTable.this.getCellRect(row, 0, true);//    getHeaderRect(column);
			r.grow(0, -2);
			if (r.contains(p)) {
				return -1;
			}
			int midPoint = r.y + r.height/2;
			int rowIndex = (p.y < midPoint) ? row - 1 : row;

			return rowIndex;
		}

		@Override
		public void mouseDragged(MouseEvent e) {

			SimSearchJTable table = (SimSearchJTable) e.getSource();
			if(table.getCursor()==ROW_RESIZE_CURSOR) {
				e.consume();
				int delta = e.getPoint().y - yBeforeDrag;
				int rowDraggedModelIndex = table.convertRowIndexToModel(rowDragged);

				int newHeight = Math.max(hBeforeDrag + delta, MIN_ROW_HEIGHT);

				setRowHeight(rowDragged, newHeight);
				table.partnerTable.setRowHeight(rowDragged, newHeight);
				if(unzoomedRowHeights!=null) unzoomedRowHeights[rowDraggedModelIndex] = newHeight/viewSettings.getZoom();
			}
		}

	}

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

		private static final long serialVersionUID = -2117518362174075985L;

		SimSearchJTableHeader(TableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			Point p = e.getPoint();

			int viewIndex = columnModel.getColumnIndexAtX(p.x);
			if(viewIndex>=0) {
				int modelIndex = columnModel.getColumn(viewIndex).getModelIndex();
				if(this.table!=null && this.table.getModel()!=null && (this.table.getModel() instanceof SimSearchTableModel)) {
					return GuiMessages.getString(((SimSearchTableModel) this.table.getModel()).getColumnComment(modelIndex));
				}
			}
			return null;
		}


	}
	

	
	// Cell Renderer start

	public static class DefaultColumnRenderer extends DefaultTableCellRenderer  implements ToolTipRenderer  { 

		private static final long serialVersionUID = -5815150426074228820L;

		private static final Color COLOR_INACTIVEROW_BACKGROUND = Color.LIGHT_GRAY;
		private static final int EXTRA_X_MARGIN = 1; 
		private static final int EXTRA_Y_MARGIN = 1;

		private DefaultColumnRenderer() {
			this.setVerticalAlignment(SwingConstants.TOP);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column) {

			Component c = super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			SimSearchTable.ViewSettings viewSettings = ((SimSearchJTable) table).viewSettings;

			Border border = getBorder();
			if(border!=null) 
				if(!(border instanceof CompoundBorder)) {
					border = new CompoundBorder(border, new EmptyBorder(viewSettings.getCellMarginY(),viewSettings.getCellMarginX(),viewSettings.getCellMarginY(), viewSettings.getCellMarginX()));
					setBorder(border);
					int height = c.getFontMetrics(table.getFont()).getHeight();
					Insets insets = border.getBorderInsets(this);
					this.setPreferredSize(new Dimension(this.getTextWidth(value) + insets.left + insets.right + EXTRA_X_MARGIN, height + insets.top + insets.bottom + EXTRA_Y_MARGIN));
				}

			SimSearchJTable simTable = (SimSearchJTable) table;

			JTable.DropLocation dropLocation = table.getDropLocation();
			if(dropLocation==null) dropLocation = simTable.getPartnerTable().getDropLocation();


			Color fg = null;
			Color bg = null;

			if (dropLocation != null
					&& !dropLocation.isInsertRow()
					&& !dropLocation.isInsertColumn()
					&& dropLocation.getRow() == row) {

				fg = UIManager.getColor("Table.dropCellForeground");
				bg = UIManager.getColor("Table.dropCellBackground");

				isSelected = true;
			}

			if (isSelected) {
				super.setForeground(fg == null ? table.getSelectionForeground()
						: fg);
				super.setBackground(bg == null ? table.getSelectionBackground()
						: bg);
			} else {

				super.setForeground(table.getForeground());

				int modelRow = table.convertRowIndexToModel(row);

				if(((SimSearchTableModel) table.getModel()).isMerged(modelRow)) setBackground(COLOR_INACTIVEROW_BACKGROUND);
				else super.setBackground(table.getBackground());
			}

			return this;
		}

		int getTextWidth(Object value) {
			if(value==null) return 0;

			FontMetrics fm = getFontMetrics(getFont());
			return fm.stringWidth(value.toString());
		}



		@Override
		public String createToolTipText(SimSearchTableModel tableModel, int rowIndex, int columnIndex) {
			Object value = tableModel.getValueAt(rowIndex, columnIndex);
			if(value==null) return null;

			String toolTip = tableModel.getColumnFormatComment(columnIndex);
			if(toolTip==null || toolTip.isEmpty()) return value.toString();


			try {
				return "<html><table cellpadding=\"0\">\n" + 
						"  <tr>\n" + 
						"    <td>" + StringEscapeUtils.escapeHtml4(toolTip) + "</td>\n" + 
						"  </tr>\n" + 
						"  <tr>\n" + 
						"    <td>" + StringEscapeUtils.escapeHtml4(value.toString())  + "</td>\n" + 
						"  </tr>\n" + 
						"</table></font></html>";
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static class AlignmentColumnRenderer extends DefaultColumnRenderer implements ToolTipRenderer { 


		private static final long serialVersionUID = 2667223719310814039L;

		public static final String SYMBOL_GAP = "<b>&#752;</b>";
		public static final String SYMBOL_SPACE_DELETE = "&#8718;";

		private String[] data;
		private int columnWidth = -1;

		public static String getColoredMismatchText(String text) {
			return "<font color=\"red\">" + text + "</font>";
		}

		public static String getColoredNeutralGap() {
			return "<font color=\"green\">" + SYMBOL_GAP + "</font>";
		}

		AlignmentColumnRenderer(int rowCount) {
			super();
			data = new String[rowCount];
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


			int modelRow = table.getRowSorter().convertRowIndexToModel(row);
			int columnWidth = table.getColumnModel().getColumn(column).getWidth();

			if(columnWidth!=this.columnWidth) {
				Arrays.fill(data,null);
				this.columnWidth = columnWidth;
			}

			if(data[modelRow]==null && value!=null) {
				Insets insets = this.getBorder().getBorderInsets(this);	
				int availableWidth = Math.max(0, columnWidth - insets.left - insets.right);
				Alignment.AlignedSequence alignedSeq = (Alignment.AlignedSequence) value;

				Font currentFont = getFont();
				Font monospacedFont = new Font("Monospaced",currentFont.getStyle(), currentFont.getSize());
				FontMetrics fm = getFontMetrics(monospacedFont);
				int nSymbols = (alignedSeq.getEditOperations()==null ? 0 : alignedSeq.getEditOperations().size());
				int textWidth = (nSymbols>0 ? fm.stringWidth(StringUtils.repeat("W",nSymbols)) : 0);

				if(availableWidth<textWidth) {
					fm = getFontMetrics(currentFont);
					int suffixWidth = fm.stringWidth("...");
					nSymbols = (int) Math.floor(nSymbols * Math.max(0,(availableWidth-suffixWidth))/textWidth);
				}
				try {
					data[modelRow] = (nSymbols>0 ? createHtmlCode(alignedSeq, true, nSymbols) : "");

				} catch (Exception e) {

					data[modelRow] = String.format("<html><font color=\"red\" style=\"italic\">Could not visualize edits: </font>%s</html>", StringEscapeUtils.escapeHtml4(((Alignment.AlignedSequence) value).getSequence()));
					e.printStackTrace();
				}
			}

			setText(data[modelRow]);

			return this;
		}

		@Override
		int getTextWidth(Object value) {
			if(value==null) return 0;
			Font currentFont = getFont();
			Font monospacedFont = new Font("Monospaced",currentFont.getStyle(), currentFont.getSize());
			FontMetrics fm = getFontMetrics(monospacedFont);
			Alignment.AlignedSequence alignedSeq = (Alignment.AlignedSequence) value;

			int nSymbols = (alignedSeq.getEditOperations()==null ? 0 : alignedSeq.getEditOperations().size());
			if(nSymbols>0) return fm.stringWidth(StringUtils.repeat("W",nSymbols));
			else return 0;
		}

		public static String createHtmlCode(Alignment.AlignedSequence alignedSequence, boolean addHtmlTag) throws Exception { 
			return createHtmlCode(alignedSequence, addHtmlTag, Integer.MAX_VALUE);
		}

		private static String createHtmlCode(Alignment.AlignedSequence alignedSequence, boolean addHtmlTag, int nSymbols) throws Exception {
			if(alignedSequence==null || alignedSequence.getEditOperations()==null || alignedSequence.getEditOperations().isEmpty()) {
				return "";
			}
			StringBuilder sb = new StringBuilder((addHtmlTag?"<html><nobr>":"")+"<font face=\"Monospaced\">"); //+"<nobr>");
			final String SYMBOL_GAP = "&#752;"; 
			final String SYMBOL_SPACE_DELETE = "&#8718;";
			final String SYMBOL_SPACE = "&nbsp;";
			int textPos = -1;
			EditType editType = EditType.None;
			int iSymbol = -1;
			for(Alignment.EditOperation op : alignedSequence.getEditOperations()) {
				++iSymbol;
				if(iSymbol>=nSymbols) {
					if(editType!=EditType.None) sb.append("</font>");
					break;
				}
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
			sb.append("</font>");
			if(nSymbols<alignedSequence.getEditOperations().size()) sb.append("...");
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
						return "<html><table cellpadding=\"0\">\n" + 
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

						e.printStackTrace();
						return null;
					}
				}
			}
			return null;
		}
	}

	protected static class ListColumnRenderer extends DefaultColumnRenderer {


		private static final long serialVersionUID = 534997659347903722L;

		private JLabel label;
		private List<String> data;

		public ListColumnRenderer(int rowCount) {
			super();
			this.label = new JLabel();
			this.label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}

		@SuppressWarnings("unchecked")
		public Component getTableCellRendererComponent(JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column) {

			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);

			this.label.setFont(this.getFont());
			setText("");

			this.data = (List<String>) value;
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		int getTextWidth(Object value) {
			if(value==null) return 0;
			FontMetrics fm = getFontMetrics(getFont());

			int width = 0;
			for(String text: ((List<String>) value)) width = Math.max(width, fm.stringWidth(text));

			return width;
		}

		public void paint(Graphics g) {
			super.paint(g);
			if(data==null) return;

			Font currentFont = g.getFont();
			// get metrics from the graphics
			FontMetrics metrics = g.getFontMetrics(this.getFont());
			// get the height of a line of text in this
			// font and render context
			int hgt = metrics.getHeight();

			int descent = metrics.getDescent();
			int ascent = hgt-descent; //metrics.getAscent();
			Insets inset = this.getBorder().getBorderInsets(this);

			int linepad = 0; 

			int y = inset.top; 
			int maxWidth = this.getWidth()-inset.left-inset.right; 
			int maxHeight = this.getHeight()-inset.top -inset.bottom;

			g.setFont(this.getFont());

			int yLine;

			Shape oldClip = g.getClip();
			int heightContinueMark = 2*descent; 
			boolean showContinueMark = false;

			g.setClip(new Rectangle(inset.left, inset.top, maxWidth, maxHeight));

			for(int i=0; i<data.size(); ++i) {
				String s = SwingUtilities2.clipStringIfNecessary(this.label, metrics, data.get(i), maxWidth);

				yLine = y + (hgt+linepad)*(i);

				if((yLine + hgt)>(inset.top + maxHeight)) {
					g.setClip(new Rectangle(inset.left, inset.top, maxWidth, maxHeight-heightContinueMark));
					showContinueMark = true;
				}

				g.drawString(s, inset.left, yLine+ascent);
				if(showContinueMark) break;

			}
			if(showContinueMark) {

				g.setClip(new Rectangle(inset.left,inset.top, maxWidth, maxHeight));
				g.drawString("...", inset.left, this.getHeight()-inset.bottom);

			}

			g.setFont(currentFont);
			g.setClip(oldClip);
		}

		@SuppressWarnings("unchecked")
		@Override
		public String createToolTipText(SimSearchTableModel tableModel, int rowIndex, int columnIndex) {
			Object value = tableModel.getValueAt(rowIndex, columnIndex);
			if(value==null) return null;

			String toolTip = tableModel.getColumnFormatComment(columnIndex);

			try {
				StringBuilder sb = new StringBuilder();
				sb.append("<html><table cellpadding=\"0\">\n");
				if(toolTip!=null) sb.append("  <tr>\n" + 
						"    <td>Format: <i>" + StringEscapeUtils.escapeHtml4(toolTip) + "</i></td>\n" + 
						"  </tr>\n");
				for(String element: (List<String>) value) sb.append("  <tr>\n" + 
						"    <td>" + StringEscapeUtils.escapeHtml4(element.toString())  + "</td>\n" + 
						"  </tr>\n");
				sb.append("</table></html>");
				return sb.toString();
			} catch (Exception e) {

				e.printStackTrace();
				return null;
			}
		}
	}

	protected static class RowHeaderColumnRenderer extends DefaultTableCellRenderer implements ToolTipRenderer {


		private static final long serialVersionUID = 4707310290490896146L;

		private String[] data;

		public static final String HTML_SYMBOL_SIM_REFERENCE = "&#9830"; // Karo
		//public static final String HTML_SYMBOL_SIM_REFERENCE = "&#9733;"; // star
		private static final String SYMBOL_MERGEDTO_PRESENT_ROW = "&#8593;";
		private static final String SYMBOL_MERGEDTO_NON_PRESENT_ROW = "&#8625;"; //"&#8592;";
		private static final String SYMBOL_GAP = "&nbsp;";

		public RowHeaderColumnRenderer(int rowCount) {
			super();
			data = new String[rowCount];
		}

		private String createRowHeaderText(JTable table, int modelRow) {
			SimSearchTableModel tableModel = (SimSearchTableModel) table.getModel();
			List<String> tagList = new ArrayList<>();

			if(tableModel.isSimReferenceRow(modelRow)) tagList.add(HTML_SYMBOL_SIM_REFERENCE);

			if(tableModel.isMerged(modelRow)) {
				if(tableModel.getMergeTo(modelRow)>=0) {
					tagList.add(SYMBOL_MERGEDTO_PRESENT_ROW);
				} else {
					tagList.add(SYMBOL_MERGEDTO_NON_PRESENT_ROW);
				}
			} 

			if(tableModel.getMergeCount(modelRow)>0) tagList.add("&#43;" + tableModel.getMergeCount(modelRow) );

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


			SimSearchJTable simTable = (SimSearchJTable) table;

			JTable.DropLocation dropLocation = table.getDropLocation();
			if(dropLocation==null) dropLocation = simTable.getPartnerTable().getDropLocation();

			Color fg = null;
			Color bg = null;

			if (dropLocation != null
					&& !dropLocation.isInsertRow()
					&& !dropLocation.isInsertColumn()
					&& dropLocation.getRow() == row) {

				fg = UIManager.getColor("Table.dropCellForeground");
				bg = UIManager.getColor("Table.dropCellBackground");

				isSelected = true;
			}

			if (isSelected) {
				super.setForeground(fg == null ? table.getSelectionForeground()
						: fg);
				super.setBackground(bg == null ? table.getSelectionBackground()
						: bg);
			} else {

				super.setForeground(table.getTableHeader().getForeground());
				super.setBackground(table.getTableHeader().getBackground());
			}

			int modelRow = table.getRowSorter().convertRowIndexToModel(row);
			if(data[modelRow]==null) data[modelRow] = createRowHeaderText(table, modelRow);

			setText(data[modelRow]);
			setHorizontalAlignment(SwingConstants.CENTER);

			return this;
		}

		@Override
		public String createToolTipText(SimSearchTableModel tableModel, int modelRow, int modelColumn) {
			if(tableModel!=null) {
				List<String> toolTipText = new ArrayList<>();
				if(tableModel.isSimReferenceRow(modelRow)) toolTipText.add(getReferenceRowToolTipText(tableModel)); //"This is the reference row.");
				if(tableModel.isMerged(modelRow)) toolTipText.add("The row was merged.");
				if(tableModel.getMergeCount(modelRow)>0) toolTipText.add(tableModel.getMergeCount(modelRow) + " rows were merged into this row.");
				return (toolTipText.isEmpty()?null:String.join(" ", toolTipText));
			} else return null;
		}
		
		private static String getReferenceRowToolTipText(SimSearchTableModel tableModel) {
		  switch(tableModel.getSimSet().getType()) {
		      case STATION:
		        return "This is the comparison station";
		      case PRODUCT:
		        return "This is the comparison product";
		      case LOT:
		        return "This is the comparison lot";
		      case DELIVERY:
		        return "This is the comparison delivery";
		      default:
		        return null;
		    }
		}
	}
	
	// Cell Renderer end
	
	// this function is required to get to allow a one click drag operation on unselected rows
	// Background:
	// in single row selection mode, drag & drop works with one click on unselected or selected rows
	// in multi row selection mode, the user usually has to select the rows he wants the drag first (he needs 2 clicks at least to drag unselected rows)
	//
	// Solution: if the left mouse button was pressed & shift and ctrl are not pressed & the mouse cursor hovers over an unselected cell the selection is changed to that cell (the system automates the first click)
	@Override
    protected void processMouseEvent(MouseEvent e) {
         if (e.getID() == MouseEvent.MOUSE_PRESSED
                   && SwingUtilities.isLeftMouseButton(e)
                   && !e.isShiftDown() && !e.isControlDown()) {
              Point pt = e.getPoint();
              int row = rowAtPoint(pt);
              int col = columnAtPoint(pt);
              if (row >= 0 && col >= 0 && !super.isCellSelected(row, col))
                   changeSelection(row, col, false, false);
         }
         super.processMouseEvent(e);
    }
	
}
