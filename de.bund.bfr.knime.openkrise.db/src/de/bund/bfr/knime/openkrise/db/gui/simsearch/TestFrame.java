/*
 * Created by JFormDesigner on Thu Jan 11 13:37:37 CET 2018
 */

package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author hendrik.ehrenreich@web.de Ehrenreich
 */
public class TestFrame extends JFrame {
  public TestFrame() {
    initComponents();
  }

  private void initComponents() {
    // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - hendrik.ehrenreich@web.de Ehrenreich
    ResourceBundle bundle = ResourceBundle.getBundle("de.bund.bfr.knime.openkrise.db.gui.PanelProps");
    menuBar1 = new JMenuBar();
    menu1 = new JMenu();
    showInactiveRowsMenuItem = new JCheckBoxMenuItem();
    dialogPane = new JPanel();
    contentPanel = new JPanel();
    panel2 = new JPanel();
    filterPanel = new JPanel();
    filterLabel = new JLabel();
    filterTextField = new JTextField();
    useRegexCheckBox = new JCheckBox();
    undoRedoPanel = new JPanel();
    undoButton = new JButton();
    redoButton = new JButton();
    hSpacer1 = new JPanel(null);
    ignoreButton = new JButton();
    scrollPane1 = new JScrollPane();
    table1 = new JTable();
    buttonBar = new JPanel();
    panel1 = new JPanel();
    navToFirstButton = new JButton();
    navBackwardButton = new JButton();
    navForwardButton = new JButton();
    navToLast = new JButton();
    countLabel = new JLabel();
    panel3 = new JPanel();
    statusLabel = new JLabel();
    okButton = new JButton();
    cancelButton = new JButton();

    //======== this ========
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    //======== menuBar1 ========
    {

      //======== menu1 ========
      {
        menu1.setText(bundle.getString("TestFrame.menu1.text"));

        //---- showInactiveRowsMenuItem ----
        showInactiveRowsMenuItem.setText("show inactive rows");
        menu1.add(showInactiveRowsMenuItem);
      }
      menuBar1.add(menu1);
    }
    setJMenuBar(menuBar1);

    //======== dialogPane ========
    {
      dialogPane.setBorder(Borders.createEmptyBorder("7dlu, 7dlu, 7dlu, 7dlu"));

      // JFormDesigner evaluation mark
      dialogPane.setBorder(new javax.swing.border.CompoundBorder(
        new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
          "JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
          javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
          java.awt.Color.red), dialogPane.getBorder())); dialogPane.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

      dialogPane.setLayout(new BorderLayout());

      //======== contentPanel ========
      {
        contentPanel.setLayout(new FormLayout(
          "left:default:grow",
          "fill:default, $lgap, default"));

        //======== panel2 ========
        {
          panel2.setLayout(new FormLayout(
            "3*(default, $lcgap), default",
            "default"));

          //======== filterPanel ========
          {
            filterPanel.setLayout(new FormLayout(
              "2*(default, $lcgap), default",
              "default"));

            //---- filterLabel ----
            filterLabel.setText("Filter:");
            filterPanel.add(filterLabel, CC.xy(1, 1));

            //---- filterTextField ----
            filterTextField.setText("kjksdjksdjksdjskdjskdjs");
            filterTextField.setPreferredSize(new Dimension(100, 20));
            filterPanel.add(filterTextField, CC.xy(3, 1));

            //---- useRegexCheckBox ----
            useRegexCheckBox.setText("Use regular expressions");
            filterPanel.add(useRegexCheckBox, CC.xy(5, 1));
          }
          panel2.add(filterPanel, CC.xy(1, 1));

          //======== undoRedoPanel ========
          {
            undoRedoPanel.setLayout(new FormLayout(
              "default, $lcgap, default",
              "default"));

            //---- undoButton ----
            undoButton.setText("undo");
            undoRedoPanel.add(undoButton, CC.xy(1, 1));

            //---- redoButton ----
            redoButton.setText("redo");
            undoRedoPanel.add(redoButton, CC.xy(3, 1));
          }
          panel2.add(undoRedoPanel, CC.xy(3, 1));
          panel2.add(hSpacer1, CC.xy(5, 1));

          //---- ignoreButton ----
          ignoreButton.setText(bundle.getString("TestFrame.ignoreButton.text"));
          panel2.add(ignoreButton, CC.xy(7, 1));
        }
        contentPanel.add(panel2, CC.xy(1, 1));

        //======== scrollPane1 ========
        {
          scrollPane1.setViewportView(table1);
        }
        contentPanel.add(scrollPane1, CC.xy(1, 3));
      }
      dialogPane.add(contentPanel, BorderLayout.CENTER);

      //======== buttonBar ========
      {
        buttonBar.setBorder(Borders.createEmptyBorder("5dlu, 0dlu, 0dlu, 0dlu"));
        buttonBar.setLayout(new FormLayout(
          "3*($lcgap, default), $glue, $button, $rgap, $button",
          "pref"));

        //======== panel1 ========
        {
          panel1.setLayout(new FormLayout(
            "3*(default, $lcgap), default",
            "default"));

          //---- navToFirstButton ----
          navToFirstButton.setText("<<");
          panel1.add(navToFirstButton, CC.xy(1, 1));

          //---- navBackwardButton ----
          navBackwardButton.setText("<");
          panel1.add(navBackwardButton, CC.xy(3, 1));

          //---- navForwardButton ----
          navForwardButton.setText(">");
          panel1.add(navForwardButton, CC.xy(5, 1));

          //---- navToLast ----
          navToLast.setText(">>");
          panel1.add(navToLast, CC.xy(7, 1));
        }
        buttonBar.add(panel1, CC.xy(2, 1));

        //---- countLabel ----
        countLabel.setText("1/7");
        buttonBar.add(countLabel, CC.xy(4, 1));

        //======== panel3 ========
        {
          panel3.setLayout(new FormLayout(
            "2*(default, $lcgap), default",
            "default"));

          //---- statusLabel ----
          statusLabel.setText("Similarity search in progress ...");
          panel3.add(statusLabel, CC.xy(5, 1));
        }
        buttonBar.add(panel3, CC.xy(6, 1));

        //---- okButton ----
        okButton.setText("OK");
        buttonBar.add(okButton, CC.xy(8, 1));

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        buttonBar.add(cancelButton, CC.xy(10, 1));
      }
      dialogPane.add(buttonBar, BorderLayout.SOUTH);
    }
    contentPane.add(dialogPane, BorderLayout.CENTER);
    pack();
    setLocationRelativeTo(getOwner());
    // JFormDesigner - End of component initialization  //GEN-END:initComponents
  }

  // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
  // Generated using JFormDesigner Evaluation license - hendrik.ehrenreich@web.de Ehrenreich
  private JMenuBar menuBar1;
  private JMenu menu1;
  private JCheckBoxMenuItem showInactiveRowsMenuItem;
  private JPanel dialogPane;
  private JPanel contentPanel;
  private JPanel panel2;
  private JPanel filterPanel;
  private JLabel filterLabel;
  private JTextField filterTextField;
  private JCheckBox useRegexCheckBox;
  private JPanel undoRedoPanel;
  private JButton undoButton;
  private JButton redoButton;
  private JPanel hSpacer1;
  private JButton ignoreButton;
  private JScrollPane scrollPane1;
  private JTable table1;
  private JPanel buttonBar;
  private JPanel panel1;
  private JButton navToFirstButton;
  private JButton navBackwardButton;
  private JButton navForwardButton;
  private JButton navToLast;
  private JLabel countLabel;
  private JPanel panel3;
  private JLabel statusLabel;
  private JButton okButton;
  private JButton cancelButton;
  // JFormDesigner - End of variables declaration  //GEN-END:variables
}
