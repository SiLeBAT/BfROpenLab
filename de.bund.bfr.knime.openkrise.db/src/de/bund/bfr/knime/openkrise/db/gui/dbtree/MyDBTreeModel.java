/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui.dbtree;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;

/**
 * @author Armin
 *
 */
class MyDBTreeModel implements TreeModel {

	private LinkedHashMap<Integer, DefaultMutableTreeNode>[] myIDs = null;	
	private LinkedHashMap<Integer, DefaultMutableTreeNode>[] myFilterIDs = null;	
	private LinkedHashMap<String, int[]> knownCodeSysteme = null;	
	private DefaultMutableTreeNode root = null;
	private DefaultMutableTreeNode filteredRoot = null;
	private String filter = "";
	private String[] showOnly = null;

	  
	MyDBTreeModel(final MyTable myT, final String[] showOnly) {
		this.showOnly = showOnly;
		knownCodeSysteme = DBKernel.myDBi.getKnownCodeSysteme();
	  	setTable(myT);
  }

  
  public void setShowOnly(final String[] newShowOnly) {
	  showOnly = newShowOnly;
  }
	@Override
	public Object getChild(final Object parent, final int index) {
		Object result = null;
		if (parent instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode mdtn = (DefaultMutableTreeNode) parent;		
			result = mdtn.getChildAt(index);
		}
		return result;
	}

	@Override
	public int getChildCount(final Object parent) {
		int result = 0;
		if (parent instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode mdtn = (DefaultMutableTreeNode) parent;			
			result = mdtn.getChildCount();
		}
		return result;
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		int result = 0;
		if (parent instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode mdtn = (DefaultMutableTreeNode) parent;		
			if (child instanceof TreeNode) {
				result = mdtn.getIndex((TreeNode) child);
			}
		}
		return result;
	}

	@Override
	public Object getRoot() {
		if (filter.length() == 0 || filteredRoot == null) {
			return root;
		} else {
			return filteredRoot;
		}
	}

	@Override
	public boolean isLeaf(final Object node) {
		boolean result = false;
		if (node instanceof DefaultMutableTreeNode) {
			result = ((DefaultMutableTreeNode) node).isLeaf();
		}
		return result;
	}

  // Since this is not an editable tree model, we never fire any events,
  // so we don't actually have to keep track of interested listeners
	@Override
	public void addTreeModelListener(final TreeModelListener l) {}
	@Override
	public void removeTreeModelListener(final TreeModelListener l) {}

  // This method is invoked by the JTree only for editable trees.  
  // This TreeModel does not allow editing, so we do not implement 
  // this method.  The JTree editable property is false by default.
	@Override
	public void valueForPathChanged(final TreePath path, final Object newvalue) {}

	
	  @SuppressWarnings("unchecked")
	private void setTable(final MyTable myT) {
		if (myT != null) {
			root = new DefaultMutableTreeNode(new MyDBTreeNode(0, "", "Codes", false, -1));			
	    // Erst die Codetypen
			Vector<String> codeVec = new Vector<>();
			String sql = "SELECT DISTINCT(" + DBKernel.delimitL("CodeSystem") + ") FROM " + DBKernel.delimitL(DBKernel.getCodesName(myT.getTablename()));
			ResultSet rs = DBKernel.getResultSet(sql, true);
			try {
				if (rs != null && rs.first()) {
					do {
						String cs = rs.getString("CodeSystem");
						if (cs != null) {
							codeVec.add(cs);
						}
					} while (rs.next());
				}
			}
			catch (Exception e) {MyLogger.handleException(e);}

			myIDs = new LinkedHashMap[codeVec.size()];
			myFilterIDs = new LinkedHashMap[codeVec.size()];
			
			// Dann die einzelnen CodeSysteme
			int lfd = 0;
			for (String key : knownCodeSysteme.keySet()) {
				int[] cutSystem = null;
				int i=0;
				for (;i<codeVec.size();i++) {
					if (key.equals(myT.getTablename() + "_" + codeVec.get(i))) {
						cutSystem = knownCodeSysteme.get(key);
						break;
					}
					else if (key.startsWith(myT.getTablename() + "_" + codeVec.get(i) + "_")) {
						break;
					}
				}
				if (i<codeVec.size()) {
					createDTMN(codeVec.get(i), myT, lfd, cutSystem);
					codeVec.remove(i);
					lfd++;
				}
			}
			for (int i=0;i<codeVec.size();i++) {
				createDTMN(codeVec.get(i), myT, lfd, null);
				lfd++;
				//System.err.println("codeVec not added -> " + codeVec.get(i));
			}
		}		
	}
	  private void createDTMN(String codeVec, MyTable myT, int lfd, int[] cutSystem) {
			boolean doIt = (showOnly == null);
			if (!doIt) {
				for (int j=0;j<showOnly.length;j++) {
					if (showOnly[j] != null && showOnly[j].equals(codeVec)) {
						doIt = true;
						break;
					}
				}
			}
			if (doIt) {
				String sql = "SELECT " + DBKernel.delimitL("Code") + "," + DBKernel.delimitL("Basis") + "," + DBKernel.delimitL(myT.getFieldNames()[0]) +
						" FROM " + DBKernel.delimitL(DBKernel.getCodesName(myT.getTablename())) +
						" LEFT JOIN " + DBKernel.delimitL(myT.getTablename()) +
						" ON " + DBKernel.delimitL(DBKernel.getCodesName(myT.getTablename())) + "." + DBKernel.delimitL("Basis") + 
						" = " + DBKernel.delimitL(myT.getTablename()) + "." + DBKernel.delimitL("ID") + 
						" WHERE " + DBKernel.delimitL("CodeSystem") + "='" + codeVec + "'" +
						" ORDER BY " + DBKernel.delimitL("Code") + " ASC, LENGTH(" + DBKernel.delimitL("Code") + ") ASC";
				
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new MyDBTreeNode(0, "", codeVec, false, lfd));
				LinkedHashMap<String, DefaultMutableTreeNode> myCode  = new LinkedHashMap<>();
				myIDs[lfd] = new LinkedHashMap<>();
				myFilterIDs[lfd] = new LinkedHashMap<>();
				readDB(myCode, lfd, dmtn, sql, myT.getTablename() + "_" + codeVec, cutSystem);	
				root.add(dmtn);
			}		  
	  }
	private void readDB(final LinkedHashMap<String, DefaultMutableTreeNode> myCodes, final int codeSystemNum, final DefaultMutableTreeNode root, final String sql, final String tablename_codeSystem, int[] cutSystem) {
		myIDs[codeSystemNum].clear(); myCodes.clear();
	    try {
	  		ResultSet rs = DBKernel.getResultSet(sql, false);
	  		if (rs.first()) {
	  			do {
	  				Integer id = rs.getInt("Basis");
	  				Object oCode = rs.getObject("Code");
		  		    	int[] myCS = cutSystem;
	  					String code = oCode.toString();
		  				if (cutSystem == null && code.length() > 1 && knownCodeSysteme.containsKey(tablename_codeSystem + "_" + code.substring(0, 2))) {
		  					myCS = knownCodeSysteme.get(tablename_codeSystem + "_" + code.substring(0, 2));
		  				}
		  				String cutCode = (myCS == null) ? cutEndZeros(code) : code; // codeSystemIsGS1
		  				String description = rs.getString(3);
		  				if (code == null || code.trim().length() == 0) {
		  					System.err.println("Brümde?");
		  				}
		  				else {
		  					MyDBTreeNode mydbtn = new MyDBTreeNode(id, code, description, false, codeSystemNum);
		  					DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(mydbtn);
		  					if (cutSystem == null && myCS == null) {
		  						root.add(dmtn);
		  					}
		  					else {
			  					DefaultMutableTreeNode n = look4ParentNode(myCodes, cutCode, myCS);
								if (n != null) {
									n.add(dmtn);
								} else {
									root.add(dmtn);
								}
		  					}
							myIDs[codeSystemNum].put(id, dmtn); //  && !myLeafs.containsKey(id)
							myCodes.put(cutCode, dmtn);
		  				}
	  			} while (rs.next());
	  		}  	
	    }
	    catch (SQLException e) {
	    	MyLogger.handleException(e);
	    }			
	}
	private String cutEndZeros(final String code) {
		String result = code;
		if (code != null) {
			while (result.endsWith("0")) {
				result = result.substring(0, result.length() - 1);
			}			
		}
		return result;
	}
	private DefaultMutableTreeNode look4ParentNode(final LinkedHashMap<String, DefaultMutableTreeNode> myCodes, final String code, final int[] cutSystem) {
		DefaultMutableTreeNode result = null;
		String key;
		if (cutSystem == null) {
			for (int i=code.length()-1;i>=0;i--) {
				key = code.substring(0, i);
				if (myCodes.containsKey(key))	{
					result = myCodes.get(key);
					break;
				}
			}
		}
		else {
			for (int i=cutSystem.length-1;i>=0;i--) {
				if (cutSystem[i] <= code.length()) {
					key = code.substring(0, cutSystem[i]);
					if (myCodes.containsKey(key))	{
						result = myCodes.get(key);
						break;
					}
					else {
						if (i > 0) { // sonst Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: -1
							String zeroKey = key;
							for (int j=i;j<cutSystem.length;j++) { // Nullen anfügen, gibts dann nen Parent? Das ist zumindest wichtig bei ADV-01 Matrices. Sieht bei den anderen System auch gut aus
								for (int k=0;k<cutSystem[j]-cutSystem[j-1];k++) {
									zeroKey += "0";
								}
								if (myCodes.containsKey(zeroKey))	{
									result = myCodes.get(zeroKey);
									break;
								}													
							}
							if (result != null) {
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}
	DefaultMutableTreeNode getTreeNode(final int id, final int codeSystemNum) {
		DefaultMutableTreeNode result = null;
		LinkedHashMap<Integer, DefaultMutableTreeNode>[] theIDs = (filter.length() > 0) ? myFilterIDs : myIDs;
		if (codeSystemNum >= 0 && codeSystemNum < theIDs.length) {
			result = theIDs[codeSystemNum].get(id);
		}
		if (result == null) {
			for (int i=0;i<theIDs.length;i++) {
				if (theIDs[i] != null && theIDs[i].get(id) != null) {
					return theIDs[i].get(id);
				}
			}			
		}
		return result;
	}
	
	void checkFilter(final String filter) {
		this.filter = filter;
		if (filter.length() > 0) {
			setInvisible(root);
		    StringTokenizer tok = new StringTokenizer(filter);
		    String[] findStrings = new String[tok.countTokens()];
		    for (int i=0;tok.hasMoreTokens();i++) {
		    	findStrings[i] = tok.nextToken().toLowerCase();
		    }
			checkVisibility(root, findStrings);
			filteredRoot = new DefaultMutableTreeNode(new MyDBTreeNode(0, "", "Codes", false, -1));		
			if (myFilterIDs != null) {
				for (int i=0;i<myFilterIDs.length;i++) {
					if (myFilterIDs[i] != null) {
						myFilterIDs[i].clear();
					}
				}
			}
			populateFilteredNode(root, filteredRoot);			
		}		
	}
	private void setInvisible(final DefaultMutableTreeNode node) {
	    for (int i = 0; i < node.getChildCount(); i++) {
	    	DefaultMutableTreeNode unfilteredChildNode = (DefaultMutableTreeNode) node.getChildAt(i);
	    	((MyDBTreeNode) unfilteredChildNode.getUserObject()).setVisible(false);
	    	setInvisible(unfilteredChildNode);
	    }
	}
	private void checkVisibility(final DefaultMutableTreeNode node, final String[] filter) {
	    for (int i = 0; i < node.getChildCount(); i++) {
	    	DefaultMutableTreeNode unfilteredChildNode = (DefaultMutableTreeNode) node.getChildAt(i);
	    	int j;
	    	for (j=0;j<filter.length;j++) {
	    		if (((MyDBTreeNode) unfilteredChildNode.getUserObject()).toString().toLowerCase().indexOf(filter[j]) < 0) {
					break;
				}
	    	}
	    	if (j == filter.length) {
	    		setVisible(unfilteredChildNode);
	    	}
	    	checkVisibility(unfilteredChildNode, filter);
	    }
	}
	private void setVisible(final DefaultMutableTreeNode node) {
		((MyDBTreeNode) node.getUserObject()).setVisible(true);
		if ((node.getParent() != null)) {
			setVisible((DefaultMutableTreeNode) node.getParent());
		}
	}

	private void populateFilteredNode(final DefaultMutableTreeNode unfilteredNode, final DefaultMutableTreeNode filteredNode) {
    for (int i = 0; i < unfilteredNode.getChildCount(); i++) {
    	DefaultMutableTreeNode unfilteredChildNode = (DefaultMutableTreeNode) unfilteredNode.getChildAt(i);

      if (((MyDBTreeNode) unfilteredChildNode.getUserObject()).isVisible()) {
      	DefaultMutableTreeNode filteredChildNode = (DefaultMutableTreeNode) unfilteredChildNode.clone();
        filteredNode.add(filteredChildNode);
    		Integer id = ((MyDBTreeNode) filteredChildNode.getUserObject()).getID();
    		int codeSystemNum = ((MyDBTreeNode) filteredChildNode.getUserObject()).getCodeSystemNum();
    		if (!myFilterIDs[codeSystemNum].containsKey(id)) {
				myFilterIDs[codeSystemNum].put(id, filteredChildNode);
			}
        populateFilteredNode(unfilteredChildNode, filteredChildNode);
      }
      else {
        populateFilteredNode(unfilteredChildNode, filteredNode);      	
      }
    }
  }
}
