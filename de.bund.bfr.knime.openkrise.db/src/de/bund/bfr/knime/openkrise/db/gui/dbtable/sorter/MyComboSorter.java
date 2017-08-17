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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter;

import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * @author Armin
 *
 */
public class MyComboSorter implements Comparator<Integer> {

	private LinkedHashMap<Object, String>[] hashBox = null;
	private int pos;
	
	public MyComboSorter(LinkedHashMap<Object, String>[] hashBox, int pos) {		
		this.hashBox = hashBox;
		this.pos = pos;
	}
	
  @Override
  public int compare(Integer o1, Integer o2) {
  	//System.out.println("MyComboSorter");
  	if (o1 == null && o2 == null) return 0;
  	else if (o1 == null) return 1;
  	else if (o2 == null) return -1;
  	else if (hashBox[pos] == null) return 0;
  	else if (hashBox[pos].get(o1) == null && hashBox[pos].get(o2) == null) return 0;
  	else if (hashBox[pos].get(o1) == null) return 1;
  	else if (hashBox[pos].get(o2) == null) return -1;
  	else return hashBox[pos].get(o1).compareTo(hashBox[pos].get(o2));
  }
}
