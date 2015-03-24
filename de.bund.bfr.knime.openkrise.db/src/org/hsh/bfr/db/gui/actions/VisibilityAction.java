/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package org.hsh.bfr.db.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.gui.InvisibleNode;
import org.hsh.bfr.db.gui.MyList;


public class VisibilityAction  extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5463247920683512461L;
	private MyList myList;
	private String tableName;
	
  public VisibilityAction(String tableName, Icon icon, String toolTip, MyList myList) {
    putValue(Action.NAME, tableName);
    putValue(Action.SHORT_DESCRIPTION, toolTip);
    putValue(Action.SMALL_ICON, icon);
    this.myList = myList;
    this.tableName =  tableName;
  }    

  public void actionPerformed(ActionEvent e) {
	  
	  String childName;
	  //System.err.println("action.. " + tableName);
	  System.out.println(e);
	  InvisibleNode iNode = (InvisibleNode) myList.getModel().getRoot();
	  InvisibleNode iChild;
	  
	  for (int i=0; i<iNode.getChildCount(); i++) {   
		  iChild = (InvisibleNode)iNode.getChildAt(i);
		  childName = iChild.getUserObject().toString();
		  
		  if (childName.equals(tableName)) {
			  iChild.setVisible(!iChild.isVisible());
		  }
		 
		  myList.updateUI();
		
		  DBKernel.prefs.putBoolean("VIS_NODE_" + childName, iChild.isVisible());
			DBKernel.prefs.prefsFlush();
	  }
	 
	  
	}
}
