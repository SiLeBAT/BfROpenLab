/*******************************************************************************
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Thöns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * Jörgen Brandt (BfR)
 * Annemarie Käsbohrer (BfR)
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
/**
 * 
 */
package org.hsh.bfr.db.gui.dbtable.editoren;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.hsh.bfr.db.MyTable;
import org.hsh.bfr.db.gui.InfoBox;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;


import quick.dbtable.CellComponent;

/**
 * @author Armin
 *
 */
public class MyTextareaEditor extends JTextArea implements CellComponent, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private MyDBTable myDB = null;
	private JScrollPane myScroller = null;
	private char[] allowedCharsInAdditionToLetterOrDigit;
  
	public MyTextareaEditor(MyDBTable myDB, MyTable myT, Integer i) {
		allowedCharsInAdditionToLetterOrDigit = myT.getAllowedCharsInAdditionToLetterOrDigit() != null && myT.getAllowedCharsInAdditionToLetterOrDigit()[i] != null ? myT.getAllowedCharsInAdditionToLetterOrDigit()[i] : null;
		this.setLineWrap(true);
		this.setWrapStyleWord(true);
		myScroller = new JScrollPane(this);
		filterIM();
		this.addKeyListener(this);
  }

  public void setValue(Object value) {
  	if (value == null) this.setText("");
  	else  this.setText(value.toString());
  }

  public Object getValue() {
	  String result = this.getText();
  	return result;
  }

  public JComponent getComponent() {
     return myScroller;
  }

  public void addActionListener(ActionListener listener) {
  }
  

	private void filterIM() {
		KeyStroke[] disableKeys = new KeyStroke[] {KeyStroke.getKeyStroke("TAB"),KeyStroke.getKeyStroke("ENTER"),KeyStroke.getKeyStroke("ESCAPE")};
				
		InputMap im = new FilteringInputMap(this.getInputMap(WHEN_FOCUSED), disableKeys);
		this.setInputMap(WHEN_FOCUSED, im);				 
		im = new FilteringInputMap(this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), disableKeys);
		this.setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, im);		
	}
	/*
	private void addKeyListener(final MyTextareaEditor mtae) {
    KeyListener keyListener = new KeyListener() {
      public void keyPressed(KeyEvent keyEvent) {
        if ((keyEvent.isAltDown() || keyEvent.isControlDown()) && keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
        	//System.out.println(keyEvent.isConsumed());
        	mtae.insert("\n", mtae.getCaretPosition());
        	keyEvent.consume();
        }
      }
      public void keyReleased(KeyEvent keyEvent) {
        //printIt("Released", keyEvent);
      }
      public void keyTyped(KeyEvent keyEvent) {
      	//printIt("keyTyped", keyEvent);
      }
    };
    this.addKeyListener(keyListener);		
	}
	*/
  public void keyPressed(KeyEvent keyEvent) {
    if ((keyEvent.isAltDown() || keyEvent.isControlDown()) && keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
    	//System.out.println(keyEvent.isConsumed());
    	this.insert("\n", this.getCaretPosition());
    	keyEvent.consume();
    }
    /*
    else if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
    	keyEvent.consume();
    	int row = myDB.getSelectedRow();
    	int col = myDB.getSelectedColumn();
    	if (row < myDB.getRowCount() - 1) {myDB.setRowSelectionInterval(row+1, row+1);myDB.selectCell(row+1, col, true);}
    	else if (col < myDB.getColumnCount() - 1) {myDB.setRowSelectionInterval(row, row);myDB.selectCell(row, col+1, true);}
    	else myDB.clearSelection();
    }
    */
  }
  public void keyReleased(KeyEvent keyEvent) {
    //printIt("Released", keyEvent);
  }
  public void keyTyped(KeyEvent keyEvent) {
	  if (!isVariableCharacter(keyEvent.getKeyChar())) {
	    	keyEvent.consume();
	    	InfoBox ib = new InfoBox("character not allowed... -> '" + keyEvent.getKeyChar() + "'", true, new Dimension(250, 100), null, true);
	    	ib.setVisible(true);
	    }
  }
	private boolean isVariableCharacter(char ch) {
		if (allowedCharsInAdditionToLetterOrDigit == null) return true;
		boolean result = Character.isLetterOrDigit(ch);
		for (char c : allowedCharsInAdditionToLetterOrDigit) {
			if (!result) result = ch == c;
		}
		//return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$' || ch == '\b';
		return result;
	}
}
