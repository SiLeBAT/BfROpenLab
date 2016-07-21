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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren;

import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;


import quick.dbtable.CellComponent;

/**
 * @author Armin
 *
 * evtl. mal irgendwann einbauen: http://www.orbital-computer.de/JComboBox/
 */
public class MyComboBoxEditor implements CellComponent {

	private JComboBox<KeyValue> myCombo = null;
	private JTextArea myTA = null;
	private LinkedHashMap<Object, String> hash = null;
	private LinkedHashMap<Object, KeyValue> hashKVs = null;
	private boolean isRenderer;
	
	public MyComboBoxEditor(LinkedHashMap<Object, String> hash, boolean isRenderer) {
		this.isRenderer = isRenderer;
		refreshHashboxes(hash);
	}

	private void refreshHashboxes(LinkedHashMap<Object, String> hashBox) {
		this.hash = hashBox;
		if (isRenderer) {
			myTA = new JTextArea();
			myTA.setLineWrap(true);
			myTA.setWrapStyleWord(true);
		}
		else {
			myCombo = new JComboBox<>();
			hashKVs = new LinkedHashMap<>();
			for (Object key : hash.keySet()) {
				KeyValue kv = new KeyValue(key, hash.get(key));
				hashKVs.put(key, kv);
				myCombo.addItem(kv);
			}			
			//myCombo.addItem(null);			
		}
	}

	public JComponent getComponent() {
		return isRenderer ? myTA : myCombo;
	}

	public Object getValue() {
		if (isRenderer) {
			//myTA.setToolTipText(myTA.getText());
			return myTA.getText();
		}
		else {
			if (myCombo.getSelectedItem() == null) {
				//System.out.println("NULL");
				return null;
			}
			else {
				KeyValue entry = (KeyValue) myCombo.getSelectedItem();
				return entry.getKey();
			}			
		}
	}

	public void setValue(Object value) {
		if (isRenderer) {
			if (value == null || hash.get(value) == null) myTA.setText("");
			else {
				myTA.setText(hash.get(value).toString());			
			}
			//myTA.setToolTipText(myTA.getText());
		}
		else {
			if (value == null) {
				//System.out.println("setValueNULL");
				myCombo.setSelectedItem(null);
			}
			else {
				myCombo.setSelectedItem((KeyValue) hashKVs.get(value));
			}
		}
	}

  public void addActionListener(ActionListener listener) {
  	if (!isRenderer) myCombo.addActionListener(listener);
  }
}
