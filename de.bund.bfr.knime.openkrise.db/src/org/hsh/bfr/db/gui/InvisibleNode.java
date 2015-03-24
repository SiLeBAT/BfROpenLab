/*******************************************************************************
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Th�ns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * J�rgen Brandt (BfR)
 * Annemarie K�sbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
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
 ******************************************************************************/
package org.hsh.bfr.db.gui;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class InvisibleNode extends DefaultMutableTreeNode {

	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected boolean isVisible;

	  public InvisibleNode() {
	    this(null);
	  }

	  InvisibleNode(Object userObject) {
	    this(userObject, true, true);
	  }

	  private InvisibleNode(Object userObject, boolean allowsChildren,
	      boolean isVisible) {
	    super(userObject, allowsChildren);
	    this.isVisible = isVisible;
	  }

	  TreeNode getChildAt(int index, boolean filterIsActive) {
	    if (!filterIsActive) {
	      return super.getChildAt(index);
	    }
	    if (children == null) {
	      throw new ArrayIndexOutOfBoundsException("node has no children");
	    }

	    int realIndex = -1;
	    int visibleIndex = -1;
	    Enumeration<?> e = children.elements();
	    while (e.hasMoreElements()) {
	      InvisibleNode node = (InvisibleNode) e.nextElement();
	      if (node.isVisible()) {
	        visibleIndex++;
	      }
	      realIndex++;
	      if (visibleIndex == index) {
	        return (TreeNode) children.elementAt(realIndex);
	      }
	    }

	    throw new ArrayIndexOutOfBoundsException("index unmatched");
	    //return (TreeNode)children.elementAt(index);
	  }

	  int getChildCount(boolean filterIsActive) {
	    if (!filterIsActive) {
	      return super.getChildCount();
	    }
	    if (children == null) {
	      return 0;
	    }

	    int count = 0;
	    Enumeration<?> e = children.elements();
	    while (e.hasMoreElements()) {
	      InvisibleNode node = (InvisibleNode) e.nextElement();
	      if (node.isVisible()) {
	        count++;
	      }
	    }

	    return count;
	  }

	  public void setVisible(boolean visible) {
	    this.isVisible = visible;
	  }

	  public boolean isVisible() {
	    return isVisible;
	  }

	}
