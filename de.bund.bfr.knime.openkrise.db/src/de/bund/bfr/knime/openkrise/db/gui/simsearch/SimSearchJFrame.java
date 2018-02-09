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
package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import de.bund.bfr.knime.openkrise.db.gui.MainFrame;
import de.bund.bfr.knime.openkrise.db.gui.PlausibleDialog4Krise;
//import com.sun.glass.ui.Cursor;
//import DBMergeServer.SimSearch;
//import SimSearch.SimSearchListener;
//import DBMergeServer.SimSearch.SimSearchTableModel;

public class SimSearchJFrame extends JFrame implements SimSearch.SimSearchListener {
  
//  static {
//    //SimSearchJFrame.setDefaultLookAndFeelDecorated(false);
//  }

  private SimSearchTable table;

  private JTextField filterTextField;
  private JLabel filterLabel;
  private JCheckBox useRegularExpressionsFilterCheckBox;
  private JButton undoButton;
  private JButton redoButton;
  private JButton ignoreSimSetButton;
  private JButton ignoreAllPairsInSimSetButton;

  private JButton okButton;
  private JButton cancelButton;
  private JButton applyButton;

  private JButton navToFirst;
  private JButton navToLast;
  private JButton navBack;
  private JButton navForward;

  private JLabel simSetCountLabel;

  private JCheckBoxMenuItem hideInactiveRowsMenuItem; 
  private JMenu showColumnsMenuItem;

  private SimSearch simSearch;
  private SimSearch.Settings simSearchSettings;

  private int currentSimSetIndex;
  private boolean userIsWaiting;
  private boolean searchIsOn;
  private boolean saveIsOn;

  public SimSearchJFrame(Frame owner) {
    super();
    //this();
    //this.addWindowListener(this);
    this.initComponents();
    this.userIsWaiting = false;
  }

  public SimSearchJFrame() {
    this(null);
    this.initComponents();
    this.userIsWaiting = false;
  }

  public void startSearch(SimSearch.Settings settings) {
    this.simSearchSettings = settings; //new SimSearch.Settings();
    this.currentSimSetIndex = -1;

    this.simSearch = new SimSearch();
    this.simSearch.addEventListener(this);
    this.simSearch.registerManipulationStateListener(new SimSearchDataManipulationHandler.ManipulationStateListener() {

      @Override
      public void manipulationStateChanged(boolean reloadRequired) {
        SimSearchJFrame.this.processManipulationStateChanged(reloadRequired);
      }
      
    });
    this.setUserIsWaiting(true);
    this.showWaitDialog("Searching for similarities ...");
    this.searchIsOn = true;
    
    this.startAsyncSearch(this.simSearchSettings);
  }

  private void startAsyncSearch(SimSearch.Settings settings) {
	  Runnable runnable = new Runnable(){

		  public void run(){
			  boolean searchCompleted = false;
			  try {
				  searchCompleted = SimSearchJFrame.this.simSearch.search(settings);
			  } catch (Exception err) {
				  SimSearchJFrame.this.showError(err, "Similarity search failed."); 
			  }      
			  SimSearchJFrame.this.finishAsyncSearch(searchCompleted);
		  }
	  };

	  Thread thread = new Thread(runnable);
	  thread.start();
  }
  
  private void finishAsyncSearch(boolean searchCompleted) {
	  if(SwingUtilities.isEventDispatchThread()) {
		  // 
		  this.setUserIsWaiting(false);
		  this.searchIsOn = false;
	      this.updateSimSetCountLabel();
	      this.navToLast.setEnabled((this.simSearch.getSimSetCount()>this.currentSimSetIndex+1));
	      //if(this.simSearch.getSimSetCount()==0 && searchCompleted) JOptionPane.showConfirmDialog(this, "No similarities found.", "Similarity search result", JOptionPane.NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
	      if(this.simSearch.getSimSetCount()==0 && searchCompleted) JOptionPane.showMessageDialog(null, "No similarities found.", "Similarity search result", 1);
	      //JOptionPane.
	      
	  } else {
		  SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
				  SimSearchJFrame.this.finishAsyncSearch(searchCompleted);
			  }
		  });
	  }
  }
  
  
  private void initComponents() {
    //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        SimSearchJFrame.this.processWindowCloseRequest();
      }

      @Override
      public void windowOpened(WindowEvent e) {
        //System.out.println("windowOpended");
        SimSearchJFrame.this.processWindowOpenedEvent();
      }
    });
    
    try {
      this.setIconImage(ImageIO.read(MainFrame.class.getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Eye.gif")));
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    this.setTitle("Similarity Search");
    this.addMenu();
    this.addTopPanel();
    this.addTable();
    this.addBottomPanel();
    this.table.registerRowTextFilter(this.filterTextField, null);
    this.table.registerInactiveRowFilterSwitch(this.hideInactiveRowsMenuItem);
    this.table.registerSimSetIgnoreButtons(this.ignoreSimSetButton, this.ignoreAllPairsInSimSetButton);
  
    Arrays.asList(this.filterLabel, this.filterTextField, this.useRegularExpressionsFilterCheckBox,  
    		this.navToFirst,this.navBack, this.navForward, this.navToLast).forEach(c -> c.setEnabled(false));
    
    this.setSize(1000, 500);
    this.setLocationRelativeTo(null);
  }

  private void addTopPanel() {
    JPanel topPanel = new JPanel();
    this.getContentPane().add(topPanel, BorderLayout.PAGE_START);
    topPanel.setLayout(new BorderLayout());

    JPanel filterPanel = new JPanel();
    topPanel.add(filterPanel, BorderLayout.LINE_START);

    this.filterLabel = new JLabel("Filter:");
    this.filterTextField = new JTextField();
    //this.filterText.setA
    //this.filterTextField.setMinimumSize(new Dimension(this.filterTextField.getHeight()*8, this.filterTextField.getHeight()));
    this.useRegularExpressionsFilterCheckBox = new JCheckBox("Use regular expressions");
    
    Arrays.asList(filterLabel, this.filterTextField, this.useRegularExpressionsFilterCheckBox).forEach(c -> filterPanel.add(c));

    JPanel undoRedoPanel = new JPanel();
    topPanel.add(undoRedoPanel, BorderLayout.CENTER);



    this.undoButton = new JButton("undo");
    this.undoButton.setToolTipText("undo merge/unmerge operation"); //  bundle.getString("MainFrame.button10.toolTipText"));
    //this.undoButton.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/undo.gif")));
    this.redoButton = new JButton("redo");
    this.redoButton.setToolTipText("redo merge/unmerge operation"); //  bundle.getString("MainFrame.button10.toolTipText"));
    //this.redoButton.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/redo.gif")));
    Arrays.asList(this.undoButton, this.redoButton).forEach(c -> undoRedoPanel.add(c));
    
    JPanel ignorePanel = new JPanel();
    //topPanel.add(ignorePanel, BorderLayout.LINE_END);
    this.ignoreSimSetButton = new JButton("Ignore SimSet");   //new JToggleButton("Ignore");
    this.ignoreSimSetButton.setToolTipText("Mark all rows as different to the reference row."); //  bundle.getString("MainFrame.button10.toolTipText"));
    this.ignoreAllPairsInSimSetButton = new JButton("Ignore all pairs.");
    this.ignoreAllPairsInSimSetButton.setToolTipText("Mark all rows as different to the reference row."); 
    Arrays.asList(ignoreSimSetButton, ignoreAllPairsInSimSetButton).forEach(b -> {
      ignorePanel.add(b);
      b.setEnabled(false);
    });
    //this.ignoreButton.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/ignore.gif")));
    topPanel.add(ignorePanel, BorderLayout.LINE_END); //    this.ignoreButton.forEach(c -> undoRedoPanel.add(c));

    //this.ignoreButton.setVisible(false);

    ActionListener undoRedoActionListener = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        SimSearchJFrame.this.processUndoRedoRequest((JButton) e.getSource());
      }

    };
    Arrays.asList(this.undoButton, this.redoButton).forEach(b-> b.addActionListener(undoRedoActionListener));
    //topPanel.add(this.ignoreButton, BorderLayout.LINE_END);
    //Dimension dim = this.undoButton.getPreferredSize();

    Arrays.asList(this.undoButton, this.redoButton).forEach(c -> c.setEnabled(false));
    
    filterTextField.setPreferredSize(new Dimension(this.undoButton.getPreferredSize().height * 5,  this.undoButton.getPreferredSize().height));
    //this.ignoreButton.setPreferredSize(new Dimension(this.ignoreButton.getPreferredSize().width,  this.undoButton.getPreferredSize().height));
  }

  private void addBottomPanel() {
    JPanel bottomPanel = new JPanel();
    this.getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
    //bottomPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
    bottomPanel.setLayout(new BorderLayout());
    JPanel navPanel = new JPanel();
    bottomPanel.add(navPanel, BorderLayout.LINE_START);

    this.navToFirst = new JButton();
    this.navToFirst.setText("<<");
    this.navToLast = new JButton();
    this.navToLast.setText(">>");
    this.navBack = new JButton();
    this.navBack.setText("<");
    this.navForward = new JButton();
    this.navForward.setText(">");
    List<JButton> navButtonList = Arrays.asList(this.navToFirst, this.navBack, this.navForward, this.navToLast);
    ActionListener actionListener = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        SimSearchJFrame.this.processNavigationRequest((JButton) arg0.getSource());
      }

    };

    navButtonList.forEach(b -> {
      //b.setEnabled(false);
      b.addActionListener(actionListener);
      navPanel.add(b);
    });
    navPanel.setEnabled(false);
    //		final double maxWidth = Collections.max(navButtonList.stream().map(b -> b.getPreferredSize().getWidth()).collect(Collectors.toList()));
    //		navButtonList.forEach(b -> b.getPreferredSize().setSize(maxWidth, b.getPreferredSize().getHeight()));
    //		
    this.simSetCountLabel = new JLabel();
    this.simSetCountLabel.setForeground(Color.BLUE);
    bottomPanel.add(this.simSetCountLabel, BorderLayout.CENTER);

    JPanel controlPanel = new JPanel();
    bottomPanel.add(controlPanel, BorderLayout.LINE_END);

    this.okButton = new JButton("Ok");
    this.applyButton = new JButton("Apply");
    this.cancelButton = new JButton("Cancel");
    Arrays.asList(this.okButton, this.applyButton, this.cancelButton).forEach(b -> {
      controlPanel.add(b);
    });
    this.cancelButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        SimSearchJFrame.this.processWindowCloseRequest();
      }

    });
    this.okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        SimSearchJFrame.this.processUserSaveRequest();
      }
    });
    this.applyButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        SimSearchJFrame.this.processUserApplyRequest();
      }
    });
    this.applyButton.setEnabled(false);
  }
  
  private void registerCancelAndOkButtons() {
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL");
		getRootPane().getActionMap().put("CANCEL", //cancelButton.getAction());
		new AbstractAction(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	SimSearchJFrame.this.processWindowCloseRequest();
		    }
		});
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
		getRootPane().getActionMap().put("ENTER", //okButton.getAction());
		new AbstractAction(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	SimSearchJFrame.this.processUserSaveRequest();
		    }
		});
	}

  private void processNavigationRequest(JButton source) {
    int index = -1;
    if(source==this.navToFirst) index = this.simSearch.getIndexOfNextNotIgnoredSimSet(-1);
    else if(source==this.navBack) index = this.simSearch.getIndexOfPreviousNotIgnoredSimSet(this.currentSimSetIndex); //this.startAsyncDataLoad(this.currentSimSetIndex-1);
    else if(source==this.navForward) index = this.simSearch.getIndexOfNextNotIgnoredSimSet(this.currentSimSetIndex); //this.startAsyncDataLoad(this.currentSimSetIndex+1);
    else if(source==this.navToLast) this.simSearch.getIndexOfPreviousNotIgnoredSimSet(-1); //this.startAsyncDataLoad(this.simSearch.getSimSetCount()-1);
    else { 
      // do nothing
      return;
    } 
    if(index>=0) this.startAsyncDataLoad(index);
  }

  private void processUndoRedoRequest(JButton source) {
    if(source==this.undoButton) this.simSearch.undo();
    else if(source==this.redoButton) this.simSearch.redo();
    else { 
      // do nothing
    } 
  }
  
  private void processManipulationStateChanged(boolean reloadRequired) {
    if(SwingUtilities.isEventDispatchThread()) {
      // 
      this.redoButton.setEnabled(this.simSearch.isRedoAvailable());
      this.undoButton.setEnabled(this.simSearch.isUndoAvailable());
      //this.ignoreButton.setSelected(this.table.isSimSetIgnored());
      this.applyButton.setEnabled(this.simSearch.existDataManipulations());
      
      //int simSetIndex = this.currentSimSetIndex;
      
      if(reloadRequired) {
        int simSetIndex = this.currentSimSetIndex;
        if(this.currentSimSetIndex<0 || this.simSearch.isSimSetIgnored(this.currentSimSetIndex)) simSetIndex = this.simSearch.getIndexOfNextNotIgnoredSimSet(this.currentSimSetIndex);
        if(simSetIndex>=0) this.startAsyncDataLoad(simSetIndex);
        else this.clearTable();
      }
  
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          SimSearchJFrame.this.processManipulationStateChanged(reloadRequired);
        }
      });
    }
  }
  
  private void clearTable() {
    this.table.clear();
    Arrays.asList(this.navBack, this.navForward, this.navToFirst, this.navToLast).forEach(b -> b.setEnabled(false));
    this.currentSimSetIndex = -1;
    this.showColumnsMenuItem.setEnabled(false);
    this.updateSimSetCountLabel();
  }

  private void addTable() {
    this.table = new SimSearchTable(loadViewSettings());

    this.table.setBorder(BorderFactory.createTitledBorder("Results"));
    this.getContentPane().add(this.table, BorderLayout.CENTER);
  }
  
  private SimSearchTable.ViewSettings loadViewSettings() {
    return new SimSearchTable.ViewSettings();
  }

  private void addMenu() {
    Border bo = new LineBorder(Color.yellow);
    // Erstellung einer Men�leiste
    JMenuBar bar = new JMenuBar();
    // Wir setzen unsere Umrandung f�r unsere JMenuBar
    bar.setBorder(bo);
    // Erzeugung eines Objektes der Klasse JMenu
    JMenu menu = new JMenu("View");
    this.hideInactiveRowsMenuItem =  new JCheckBoxMenuItem("Hide inactive rows");
    this.hideInactiveRowsMenuItem.setSelected(false);
    this.showColumnsMenuItem = new JMenu("Show Columns");
    this.showColumnsMenuItem.setEnabled(false);
    //menu.add(this.hideInactiveRowsMenuItem);
    for(JMenuItem menuItem : Arrays.asList(this.hideInactiveRowsMenuItem,this.showColumnsMenuItem)) menu.add(menuItem);
    
    // Men� wird der Men�leiste hinzugef�gt
    bar.add(menu);
    
    menu = new JMenu("Settings");
    JMenuItem menuItem = new JMenuItem("Show search settings ..");
    menuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        // TODO Auto-generated method stub
        SimSearchJFrame.this.processOpenSettingsRequest();
      }

    });
    menu.add(menuItem);
    bar.add(menu);
    
    menu = new JMenu("Help");
    menuItem = new JMenuItem("Help..");
    menu.add(menuItem);
    bar.add(menu);
   
    // Men�leiste wird f�r den Dialog gesetzt
    this.setJMenuBar(bar);
  }

  private void reloadView() {

  }

  private void setUserIsWaiting(boolean isWaiting) {
    if(this.userIsWaiting!=isWaiting) {
      if(isWaiting) {
        //this.setCursor(Cursor.getPredefinedCursor(Cursor.CURSOR_WAIT)
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      } else {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
      this.userIsWaiting = isWaiting;
    }
  }

  private void showWaitDialog(String text) {

  }

  private void saveTableSettings() {

  }

  private void loadTableSettings() {

  }

  private void updateSimSetCountLabel() {
    if(this.searchIsOn) {
      this.simSetCountLabel.setText(this.simSearch.getNotIgnoredSimSetIndex(currentSimSetIndex)+1 + " / ?(" + (this.simSearch.getNotIgnoredSimSetIndex(this.simSearch.getSimSetCount()-1)+1) + ")");
    } else {
      this.simSetCountLabel.setText(this.simSearch.getNotIgnoredSimSetIndex(currentSimSetIndex)+1 + " / " + (this.simSearch.getNotIgnoredSimSetIndex(this.simSearch.getSimSetCount()-1)+1));
    }
  }
  
  private void startAsyncDataLoad(int simSetIndex) {
	  Runnable runnable = new Runnable(){

		  public void run(){
		      int newSimSetIndex = simSetIndex;
			  try {
				  SimSearchTableModel tableModel = SimSearchJFrame.this.simSearch.loadData(newSimSetIndex);
				  while(tableModel.isSimSetIgnored()) {
				    newSimSetIndex = SimSearchJFrame.this.simSearch.getIndexOfNextNotIgnoredSimSet(newSimSetIndex);
				    if(newSimSetIndex>=0) {
				      tableModel = SimSearchJFrame.this.simSearch.loadData(newSimSetIndex);
				    } else {
				      tableModel = null;
				    }
				  }
				  SimSearchJFrame.this.finishAsyncDataLoad(tableModel, newSimSetIndex);
			  } catch (Exception err) {
				  SimSearchJFrame.this.showError(err, "Loading data from database failed."); 
				  SimSearchJFrame.this.finishAsyncDataLoad(null, newSimSetIndex);
			  }      
		  }
	  };

	  Thread thread = new Thread(runnable);
	  thread.start();
  }
  
  private void finishAsyncDataLoad(SimSearchTableModel tableModel, int simSetIndex) {
	  if(SwingUtilities.isEventDispatchThread()) {
	      this.showColumnsMenuItem.removeAll();
	      this.showColumnsMenuItem.setEnabled(false);
		  if(tableModel!=null) {
//			  if(this.currentSimSetIndex>=0) this.saveTableSettings();
//			  else {
		      if(this.currentSimSetIndex<0) Arrays.asList(this.filterLabel, this.filterTextField, this.useRegularExpressionsFilterCheckBox, this.ignoreSimSetButton, this.ignoreAllPairsInSimSetButton).forEach(c -> c.setEnabled(true));
//			  }
			    this.setUserIsWaiting(false);
			    this.navToFirst.setEnabled(simSetIndex>this.simSearch.getIndexOfPreviousNotIgnoredSimSet(simSetIndex));
			    this.navBack.setEnabled(this.navToFirst.isEnabled());
			    this.navForward.setEnabled(simSetIndex<this.simSearch.getIndexOfNextNotIgnoredSimSet(simSetIndex));
			    this.navToLast.setEnabled(!this.searchIsOn &&this.navForward.isEnabled());
			    this.currentSimSetIndex=simSetIndex;
			    this.updateSimSetCountLabel();
			    //this.filterText.setEnabled(true);
			    this.table.loadData(tableModel);
			    //this.ignoreButton.setSelected(this.table.isSimSetIgnored());
			    this.table.addShowColumnsSubMenuItems(this.showColumnsMenuItem);
			    this.showColumnsMenuItem.setEnabled(true);
			    
		  } else {
//		    this.ignoreAllPairsInSimSetButton.setEnabled(false);
//		    this.ignoreSimSetButton.setEnabled(false);
		    
//		    this.currentSimSetIndex = -1;
//		    this.table.clear();
		    this.clearTable();
		  }
	  } else {
		  SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
				  SimSearchJFrame.this.finishAsyncDataLoad(tableModel, simSetIndex);
			  }
		  });
	  }
  }

  @Override
  public void newSimSetFound() {
    if(simSearch.getSimSetCount()==1) this.startAsyncDataLoad(0);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        SimSearchJFrame.this.updateSimSetCountLabel();
        SimSearchJFrame.this.navForward.setEnabled(SimSearchJFrame.this.currentSimSetIndex<SimSearchJFrame.this.simSearch.getSimSetCount()-1);
      }
    });
  }

  @Override
  public void searchProgressed() {
    // TODO Auto-generated method stub

  }

  private String formatError(Exception err) {
	  return (err.getMessage()==null || err.getMessage().isEmpty()?err.getClass().getSimpleName():err.getMessage());
  }
  private void showError(Exception err, String plainMessage) {
	  if(SwingUtilities.isEventDispatchThread()) {
		  err.printStackTrace();
		  JOptionPane.showMessageDialog(this, plainMessage + " " + formatError(err), "Error", JOptionPane.ERROR_MESSAGE);
	  } else {
		  SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		        SimSearchJFrame.this.showError(err, plainMessage);
		      }
		    });
	  }
  }

  public void processWindowOpenedEvent() {
    if(this.simSearchSettings==null ) {
      // Initial openEvent
      this.simSearchSettings = PlausibleDialog4Krise.showSettings(this);
      if(this.simSearchSettings==null) this.dispose();
      else this.startSearch(this.simSearchSettings);
    }
  }

  private void processWindowCloseRequest() {
    this.dispose();
  }
  
  private void finishAsyncSave(boolean applyOnly, boolean completed, SimSearch.SimSet simSet) {
	  if(SwingUtilities.isEventDispatchThread()) {
		  if(completed) {
			  if(!applyOnly) {
				  this.dispose();
			  } else {
				  int newSimSetIndex = this.simSearch.getSimSetIndex(simSet);
				  if(newSimSetIndex>0) {
					  this.startAsyncDataLoad(newSimSetIndex);
				  } else if(this.simSearch.getSimSetCount()>0) {
					  this.startAsyncDataLoad(0);
				  } else {
					  Arrays.asList(navToFirst, navBack, navForward, navToLast).forEach(b -> b.setEnabled(false));
					  this.table.clear();
				  }
			  }
		  }  
	  } else {
		  SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
				  SimSearchJFrame.this.finishAsyncSave(applyOnly, completed, simSet);
			  }
		  });
	  }
  }
  
  private void processUserSaveRequest() {
	  if(this.simSearch!=null) startAsyncSave(false);
  }
  
  private void startAsyncSave(boolean applyOnly) {
	  SimSearch.SimSet simSet = this.simSearch.getSimSet(this.currentSimSetIndex);
	  Runnable runnable = new Runnable(){

		  public void run(){
			  try {
				boolean result = SimSearchJFrame.this.simSearch.save();
				SimSearchJFrame.this.finishAsyncSave(applyOnly, result, simSet);  
			} catch (Exception err) {
				SimSearchJFrame.this.showError(err, "Saving data to database failed.");
				SimSearchJFrame.this.finishAsyncSave(applyOnly, false, simSet);  
			}       
		  }
	  };

	  Thread thread = new Thread(runnable);
	  thread.start();
  }
  
  private void processUserApplyRequest() {
	  if(this.simSearch!=null) this.startAsyncSave(true);
  }
  
  private void processOpenSettingsRequest() {
    PlausibleDialog4Krise.showSettings(this, this.simSearchSettings);
  }
 
}
