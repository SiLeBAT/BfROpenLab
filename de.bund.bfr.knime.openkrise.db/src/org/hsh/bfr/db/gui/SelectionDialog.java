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
package org.hsh.bfr.db.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.gui.actions.VisibilityAction;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SelectionDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7519197156831734827L;
	private MyList myList;
	public SelectionDialog(MyList myList) {
		this.myList = myList;
		initComponents();						
	}

	private void okButtonActionPerformed(ActionEvent e) {
		this.dispose();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		ResourceBundle bundle = ResourceBundle.getBundle("org.hsh.bfr.db.gui.PanelProps");
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		buttonBar = new JPanel();
		okButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setTitle(bundle.getString("SelectionDialog.this.title"));
		setModal(true);
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{	
				DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) myList.getModel().getRoot();
				checkboxes = new JCheckBox[dmt.getChildCount()];
				String rowSpec = "pref, 3dlu, pref, 3dlu";
				for (int i=0; i<checkboxes.length; i++) {
					rowSpec += ",pref, 3dlu";
				}
				contentPanel.setLayout(new FormLayout("left:pref, 6dlu, 50dlu, 4dlu, default", // columns 
		         rowSpec)); 

				//---- label1 ----
				//label1.setText("Auswahl der Tabellen: ");
				//contentPanel.add(label1, cc.xy(1,1)); //cc.xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.FILL));

							
				for (int i=0; i<checkboxes.length; i++) {
					checkboxes[i]= new JCheckBox(dmt.getChildAt(i).toString());			
					checkboxes[i].setSelected(DBKernel.prefs.getBoolean("VIS_NODE_" + checkboxes[i].getText(), true));										
					contentPanel.add(checkboxes[i], cc.xy(1, 5+i*2));
				}
				
				VisibilityAction[] va = new VisibilityAction[checkboxes.length] ;
			    for (int i=0; i<checkboxes.length; i++)
			    {	checkboxes[i].setText(((DefaultMutableTreeNode)myList.getModel().getRoot()).getChildAt(i).toString());
			    	va[i] = new VisibilityAction(checkboxes[i].getText(), null, null, myList);   	
			    	checkboxes[i].setAction(va[i]);	   	
			    }
				
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
				buttonBar.setLayout(new FormLayout(
					"$glue, $button",
					"pref"));

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, cc.xy(2, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(425, checkboxes.length*40 );
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JCheckBox[] checkboxes; //JFW
	private JPanel buttonBar;
	private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables 
}
