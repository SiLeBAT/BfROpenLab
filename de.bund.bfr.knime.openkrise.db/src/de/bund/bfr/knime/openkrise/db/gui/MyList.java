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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.GuiMessages;
import de.bund.bfr.knime.openkrise.db.gui.dbtree.MyDBTree;


/**
 * @author Armin
 *
 */
public class MyList extends JTree implements TreeSelectionListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5697967857506372930L;
	private InvisibleNode root = null;
	private LinkedHashMap<Integer, String> myTs = null;
	private HashMap<Integer, Integer> indexMap = null;
	private InvisibleNode[] children = null;
	private MyDBTable myDB = null;
	private MyDBTree myDBTree = null;
	private boolean catchEvent = true;
	
	public MyList() {
		this(null,null);
	}
	MyList(final MyDBTable myDB, final MyDBTree myDBTree) {
		this.myDB = myDB;
		this.myDBTree = myDBTree;
		myTs = DBKernel.myDBi.getTreeStructure();
		if (myDB != null && myDBTree != null) {
		    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);   
		    ImageIcon customLeafIcon = rescaleImage(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Table.gif")), 12);
		    //ImageIcon customOpenIcon = rescaleImage(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Display.gif")), 18);
		    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		    //renderer.setOpenIcon(customOpenIcon);
		    //renderer.setClosedIcon(customClosedIcon);
		    renderer.setLeafIcon(customLeafIcon);
		    this.setCellRenderer(renderer);
	
		    this.setCellRenderer(new DefaultTreeCellRenderer() {
		        /**
				 * 
				 */
				private static final long serialVersionUID = 6600365951951898780L;

				@Override
				public Component getTreeCellRendererComponent(final JTree tree,
		            final Object value, final boolean sel, final boolean expanded, final boolean leaf,
		            final int row, final boolean hasFocus) {
		          super.getTreeCellRendererComponent(tree, value, sel, expanded,
		              leaf, row, hasFocus);
		          if (value instanceof InvisibleNode && !((InvisibleNode) value).isVisible()) {
		            setForeground(Color.yellow);
		          }
		          setText(GuiMessages.getString(getText()));
		          return this;
		        }
		      });
		    ((DefaultTreeCellRenderer)this.getCellRenderer()).setLeafIcon(customLeafIcon);
		    
		    //this.setToggleClickCount(0);
		    
	
		    root = new InvisibleNode(".");
			children = new InvisibleNode[myTs.size()];
			indexMap = new HashMap<>();
			int i=0;
		    for (Integer key : myTs.keySet()) {
			    children[i] = new InvisibleNode(GuiMessages.getString(myTs.get(key)));
				DBKernel.prefs.getBoolean("VIS_NODE_" + children[i], true);
				root.add(children[i]);
				indexMap.put(key, i);
				i++;
		    }
		    this.setModel(new InvisibleTreeModel(root, false, true));
		    ((InvisibleTreeModel) this.getModel()).reload();
		    this.addTreeSelectionListener(this);
		    this.addKeyListener(this);
		}		
	}
	public MyDBTree getMyDBTree() {
		return myDBTree;
	}
	public MyDBTable getMyDBTable() {
		return myDB;
	}

  @Override
  public void valueChanged(final TreeSelectionEvent event) {
    if (catchEvent && myDB != null && myDBTree != null) { // !event.getValueIsAdjusting()    	
    	InvisibleNode selectedInvisibleNode = (InvisibleNode) event.getPath().getLastPathComponent();
    	if (selectedInvisibleNode.getUserObject() instanceof MyTable) {
    		MyTable myT = (MyTable) selectedInvisibleNode.getUserObject();
      		myDB.setTable(myT);
      		myDB.getMyDBPanel().setLeftComponent(myT);
      		//DBKernel.mainFrame.setRC();
      		//myT.restoreProperties(myDB); // myDB.getActualTable()
    		if (DBKernel.showHierarchic(myT.getTablename())) {
    			myDBTree.setTable(myT);  
    			myDBTree.setSelectedID(myDB.getSelectedID());
    			myDB.getMyDBPanel().setTreeVisible(true, null);  
    			//myDBTree.grabFocus();
    		}
    		else {
    			myDB.getMyDBPanel().setTreeVisible(false, null);     
    		}
    		myDB.grabFocus();
    	}
    	else {
    		if (DBKernel.debug && event != null && event.getOldLeadSelectionPath() != null) {
				MyLogger.handleMessage(event.getOldLeadSelectionPath().toString());
			}
	      	catchEvent = false;
	      	this.setSelectionPath(event.getOldLeadSelectionPath());
    	}
    }
  	catchEvent = true;
  }
	
  public boolean setSelection(final String selection) {
  	return walk(this.getModel(), this.getModel().getRoot(), selection);  
  }
  private boolean walk(final TreeModel model, final Object o, final String selection) {
    int cc;
    cc = model.getChildCount(o);
    for (int i=0; i < cc; i++) {
      Object child = model.getChild(o, i);
      if (model.isLeaf(child)) {
      	InvisibleNode inc = (InvisibleNode) child;
        if (selection == null || selection.equals(child.toString())) {
        	if (inc.isVisible) {
        		this.setSelectionPath(new TreePath((inc).getPath()));
        		return true;
        	}
        }
      }
      else {
      	if (walk(model, child, selection)) {
			return true;
		} 
      }
    }
    return false;
  } 
	void addAllTables() {
		LinkedHashMap<String, MyTable> myTables = DBKernel.myDBi.getAllTables();
		for (String key : myTables.keySet()) {
			MyTable myT = myTables.get(key);

			//String tn = myT.getTablename();
			//myTables.put(tn, myT);
			int child = myT.getChild();
			if (indexMap.containsKey(child) && children[indexMap.get(child)] != null) {
				InvisibleNode iNode = new InvisibleNode(myT);
				iNode.setVisible(true);
				children[indexMap.get(child)].add(iNode);
			}
		}
		this.setModel(new InvisibleTreeModel(root, false, true));
		expandAll();	
		checkChildren();
	}
	private void checkChildren() {
		for (int i=0; i < children.length; i++) {
			if (children[i].getChildCount() == 0) {
				children[i].setVisible(false);
			}
		}
		this.updateUI();
	}

  private void expandAll() {
	  //boolean isAdmin = DBKernel.isAdmin();
	  for (int i=0;i < this.getRowCount();i++) {
		  //if (i != 16 && i != 17 && i != 18) this.expandRow(i);
		  //if (!isAdmin || i != 1) {
			this.expandRow(i);
		//}
	  }		
  }

  private ImageIcon rescaleImage(final ImageIcon image, final int length) {
    BufferedImage thumbImage = new BufferedImage(length, length, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = thumbImage.createGraphics();
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics2D.drawImage(image.getImage(), 0, 0, length, length, null);

    return new ImageIcon(thumbImage);
  }
	@Override
	public void keyPressed(final KeyEvent keyEvent) {
    if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
    	if (myDB != null && myDB.getTable() != null) {
    		keyEvent.consume();
    		myDB.getTable().requestFocus();
    	}
    }
	}
	@Override
	public void keyReleased(final KeyEvent keyEvent) {
	}
	@Override
	public void keyTyped(final KeyEvent keyEvent) {
	}
}
