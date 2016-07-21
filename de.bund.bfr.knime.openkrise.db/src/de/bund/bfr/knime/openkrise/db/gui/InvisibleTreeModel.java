/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

class InvisibleTreeModel extends DefaultTreeModel {

	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean filterIsActive;

	  InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren,
	      boolean filterIsActive) {
	    super(root, asksAllowsChildren);
	    this.filterIsActive = filterIsActive;
	  }

	  public boolean isActivatedFilter() {
	    return filterIsActive;
	  }

	  public Object getChild(Object parent, int index) {
	    if (filterIsActive) {
	      if (parent instanceof InvisibleNode) {
	        return ((InvisibleNode) parent).getChildAt(index,
	            filterIsActive);
	      }
	    }
	    return ((TreeNode) parent).getChildAt(index);
	  }

	  public int getChildCount(Object parent) {
	    if (filterIsActive) {
	      if (parent instanceof InvisibleNode) {
	        return ((InvisibleNode) parent).getChildCount(filterIsActive);
	      }
	    }
	    return ((TreeNode) parent).getChildCount();
	  }

	}
