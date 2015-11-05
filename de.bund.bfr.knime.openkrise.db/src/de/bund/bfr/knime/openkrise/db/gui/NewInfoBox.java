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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;

/**
 * @author Armin
 *
 */
public class NewInfoBox extends JDialog {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private String inhalt = "";

  public NewInfoBox(String inhalt, boolean keyDispose, Dimension dim, Font font) {
	  this(inhalt, keyDispose, dim, font, false);
  }
  public NewInfoBox(String inhalt, boolean keyDispose, Dimension dim, Font font, boolean modal) {
	  this(DBKernel.mainFrame, inhalt, keyDispose, dim, font, modal);
  }
  public NewInfoBox(JDialog owner, String inhalt, boolean keyDispose, Dimension dim, Font font, boolean modal) {
	    super(owner, "Info", modal);	  
	    init(owner.getLocation(), owner.getSize(), inhalt, keyDispose, dim, font, modal);
  }
  public NewInfoBox(Frame owner, String inhalt, boolean keyDispose, Dimension dim, Font font, boolean modal) {
    super(owner, "Info", modal);
    init(owner.getLocation(), owner.getSize(), inhalt, keyDispose, dim, font, modal);
  }
  private void init(Point loc, Dimension siz, String inhalt, boolean keyDispose, Dimension dim, Font font, boolean modal) {
	  System.out.println(inhalt);
	  
	    try {
	        this.inhalt = inhalt;
	        this.setResizable(false);
	        this.setSize(dim);
	        this.setLocation(loc.x + (siz.width - this.getWidth())/2, loc.y + (siz.height - this.getHeight())/2);
	        initComponents();
	        if (font != null) {
	        	infoTextArea.setFont(font);
	        }	        
	        infoTextArea.setEditable(false);
	        if (keyDispose) {
	  	      infoTextArea.addKeyListener(new KeyAdapter() {
	  	        public void keyPressed(KeyEvent e) {
	  	        	if (!e.isAltDown() && !e.isControlDown()) {
		  	        	e.consume();
			  	          dispose();	  	        		
	  	        	}
	  	        }
	  	      });
	  	      this.addKeyListener(new KeyAdapter() {
	  	        public void keyPressed(KeyEvent e) {
	  	        	if (!e.isAltDown() && !e.isControlDown()) {
		  	        	e.consume();
			  	          dispose();	  	        		
	  	        	}
	  	        }
	  	      });
	        }
	      }
	      catch(Exception e) {
	      	MyLogger.handleException(e);
	      }	  
  }

  private void okButton_actionPerformed(ActionEvent e) {
    dispose();
  }

  private void initComponents() {
          // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
          // Generated using JFormDesigner Evaluation license
          dialogPane = new JPanel();
          contentPane = new JPanel();
          scroller = new JScrollPane();
          infoTextArea = new JEditorPane();
          infoTextArea.setContentType("text/html");
          
          HTMLEditorKit kit = new HTMLEditorKit();
          
          kit.getStyleSheet().addRule("* { font-family:monospace; font-size:10px; }");
          kit.getStyleSheet().addRule("#warning { color:orange; }");
          kit.getStyleSheet().addRule("#error { color:red; }");
          kit.getStyleSheet().addRule("h1 { font-size:12px; }");
          kit.getStyleSheet().addRule("h2 { font-size:11px; }");
          kit.getStyleSheet().addRule("h1, h2 { margin-top:0px; margin-bottom:0px; }");
          
          infoTextArea.setEditorKit(kit);
          
          buttonBar = new JPanel();
          okButton = new JButton();
          CellConstraints cc = new CellConstraints();

          //======== this ========
          Container contentPane2 = getContentPane();
          contentPane2.setLayout(new BorderLayout());

          //======== dialogPane ========
          {
                  dialogPane.setBorder(Borders.DIALOG);
                  dialogPane.setLayout(new BorderLayout());

                  //======== contentPane ========
                  {
                          contentPane.setLayout(new FormLayout(
                                  "default:grow",
                                  "fill:default:grow"));

                          //======== scroller ========
                          {

                                  //---- infoTextArea ----
                                  infoTextArea.setText(inhalt);
                                  scroller.setViewportView(infoTextArea);
                          }
                          contentPane.add(scroller, cc.xy(1, 1));
                  }
                  dialogPane.add(contentPane, BorderLayout.CENTER);

                  //======== buttonBar ========
                  {
                          buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
                          buttonBar.setLayout(new FormLayout(
                                  new ColumnSpec[] {
                                		  FormSpecs.GLUE_COLSPEC,
                                		  FormSpecs.BUTTON_COLSPEC
                                  },
                                  RowSpec.decodeSpecs("pref")));

                          //---- okButton ----
                              okButton.addActionListener(new java.awt.event.ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                  okButton_actionPerformed(e);
                                }
                              });
                          okButton.setText("OK");
                          buttonBar.add(okButton, cc.xy(2, 1));
                  }
                  dialogPane.add(buttonBar, BorderLayout.SOUTH);
          }
          contentPane2.add(dialogPane, BorderLayout.CENTER);
          // JFormDesigner - End of component initialization  //GEN-END:initComponents
  }

  // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
  // Generated using JFormDesigner Evaluation license
  private JPanel dialogPane;
  private JPanel contentPane;
  private JScrollPane scroller;
  private JEditorPane infoTextArea;
  private JPanel buttonBar;
  private JButton okButton;
  // JFormDesigner - End of variables declaration  //GEN-END:variables
}
