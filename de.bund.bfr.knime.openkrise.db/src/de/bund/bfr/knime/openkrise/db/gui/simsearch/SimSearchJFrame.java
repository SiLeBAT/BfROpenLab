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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import de.bund.bfr.knime.openkrise.db.gui.PlausibleDialog4Krise;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearchTable.UndoRedoListener;
//import com.sun.glass.ui.Cursor;
//import DBMergeServer.SimSearch;
//import SimSearch.SimSearchListener;
//import DBMergeServer.SimSearch.SimSearchTableModel;

public class SimSearchJFrame extends JFrame implements SimSearch.SimSearchListener, WindowListener {
	
	private SimSearchTable table;
	
	private JTextField filterText;
	private JButton undoButton;
	private JButton redoButton;
	private JButton ignoreButton;
	
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	
	private JButton navToFirst;
	private JButton navToLast;
	private JButton navBack;
	private JButton navForward;
	
	private JLabel simSetCountLabel;
	
	private JCheckBoxMenuItem showInactiveRowsMenuItem; 
	
	private SimSearch simSearch;
	private SimSearch.Settings simSearchSettings;
	
	private int currentSimSetIndex;
	private boolean userIsWaiting;
	private boolean searchIsOn;
	
	public SimSearchJFrame(Frame owner) {
		this();
		this.setDefaultLookAndFeelDecorated(false);
		this.addWindowListener(this);
		this.initComponents();
		this.userIsWaiting = false;
	}
	
	public SimSearchJFrame() {
		super();
		this.initComponents();
		this.userIsWaiting = false;
	}
	
	public void startSearch() {
		this.simSearchSettings = new SimSearch.Settings();
		this.currentSimSetIndex = -1;
		
		this.simSearch = new SimSearch();
		this.simSearch.addEventListener(this);
		this.setUserIsWaiting(true);
		this.showWaitDialog("Searching for similarities ...");
		this.searchIsOn = true;
		this.simSearch.startSearch(this.simSearchSettings);
	}
	
	
	
	private void initComponents() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.addMenu();
		this.addTopPanel();
		this.addTable();
		this.addBottomPanel();
		this.table.registerRowFilter(this.filterText, null);
		//table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//table.setTransferHandler(handler);
	    
		//table.setDropMode(DropMode.ON_OR_INSERT_ROWS);
		
		
		
		// BorderLayout is the default layout for content pane
		
		
		//table.setDragEnabled(true);
		//table.setFillsViewportHeight(true);
		//table.setAutoCreateRowSorter(true); //XXX

		    //Disable row Cut, Copy, Paste
//		    ActionMap map = table.getActionMap();
//		    Action dummy = new AbstractAction() {
//		      @Override public void actionPerformed(ActionEvent e) { /* Dummy action */ }
//		    };
//		    map.put(TransferHandler.getCutAction().getValue(Action.NAME),   dummy);
//		    map.put(TransferHandler.getCopyAction().getValue(Action.NAME),  dummy);
//		    map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);
		
	    //JPanel p = new JPanel(new BorderLayout());
	    //p.add(new JScrollPane(table));
	    //p.setBorder(BorderFactory.createTitledBorder("Similarity Search"));
	    //this.getContentPane().add(p);
	    this.setSize(1000, 500);
	    this.setLocationRelativeTo(null);
	    //JScrollPane scrollPane = new JScrollPane(table);
        //this.add(scrollPane);
        //pack();
	}
	
	private void addTopPanel() {
		JPanel topPanel = new JPanel();
		this.getContentPane().add(topPanel, BorderLayout.PAGE_START);
		topPanel.setLayout(new BorderLayout());
		
		JPanel filterPanel = new JPanel();
		topPanel.add(filterPanel, BorderLayout.LINE_START);
		
		JLabel filterLabel = new JLabel("Filter:");
		this.filterText = new JTextField();
		//this.filterText.setA
		this.filterText.setMinimumSize(new Dimension(this.filterText.getHeight()*8, this.filterText.getHeight()));
		Arrays.asList(filterLabel, this.filterText).forEach(c -> filterPanel.add(c));
				
		JPanel undoRedoPanel = new JPanel();
		topPanel.add(undoRedoPanel, BorderLayout.CENTER);
		
		
		
		this.undoButton = new JButton("undo");
		this.undoButton.setToolTipText("undo merge/unmerge operation"); //  bundle.getString("MainFrame.button10.toolTipText"));
        //this.undoButton.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/undo.gif")));
		this.redoButton = new JButton("redo");
		this.redoButton.setToolTipText("redo merge/unmerge operation"); //  bundle.getString("MainFrame.button10.toolTipText"));
        //this.redoButton.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/redo.gif")));
		this.ignoreButton = new JButton("ignore");
		this.ignoreButton.setToolTipText("toggle ignore status"); //  bundle.getString("MainFrame.button10.toolTipText"));
        //this.ignoreButton.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/ignore.gif")));
		Arrays.asList(this.undoButton, this.redoButton, this.ignoreButton).forEach(c -> undoRedoPanel.add(c));
		
		this.ignoreButton.setVisible(false);
		
		ActionListener undoRedoActionListener = new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            SimSearchJFrame.this.processUndoRedoRequest((JButton) e.getSource());
          }
          
        };
        Arrays.asList(this.undoButton, this.redoButton).forEach(b-> b.addActionListener(undoRedoActionListener));
		//topPanel.add(this.ignoreButton, BorderLayout.LINE_END);
		//Dimension dim = this.undoButton.getPreferredSize();
		
		Arrays.asList(filterLabel, this.filterText, this.undoButton, this.redoButton).forEach(c -> c.setEnabled(false));
		filterText.setPreferredSize(new Dimension(this.undoButton.getPreferredSize().height * 5,  this.undoButton.getPreferredSize().height));
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
			b.setEnabled(false);
			b.addActionListener(actionListener);
			navPanel.add(b);
		});
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
	}
	
	private void processNavigationRequest(JButton source) {
	  if(source==this.navToFirst) this.simSearch.loadData(0);
	  else if(source==this.navBack) this.simSearch.loadData(this.currentSimSetIndex-1);
	  else if(source==this.navForward) this.simSearch.loadData(this.currentSimSetIndex+1);
	  else if(source==this.navToLast) this.simSearch.loadData(this.simSearch.getSimSetCount()-1);
	  else { 
	    // do nothing
	  } 
	}
	
	private void processUndoRedoRequest(JButton source) {
      if(source==this.undoButton) this.table.undo();
      else if(source==this.redoButton) this.table.redo();
      else { 
        // do nothing
      } 
    }
	
	private void addTable() {
		//this.table.setEnabled(false);
		//JScrollPane scrollPane = new JScrollPane(this.table);
		//p.setBorder(BorderFactory.createTitledBorder("Similarity Search"));
		//scrollPane.setBorder(BorderFactory.createTitledBorder("Results"));
	    this.table = new SimSearchTable(new UndoRedoListener() {

	      @Override
	      public void anUndoRedoEventOccured() {
	        // TODO Auto-generated method stub
	        SimSearchJFrame.this.redoButton.setEnabled(SimSearchJFrame.this.table.isRedoAvailable());
	        SimSearchJFrame.this.undoButton.setEnabled(SimSearchJFrame.this.table.isUndoAvailable());
	      }
	      
	    });
	  
	    this.table.setBorder(BorderFactory.createTitledBorder("Results"));
		this.getContentPane().add(this.table, BorderLayout.CENTER);
	}
	
	private void addMenu() {
		Border bo = new LineBorder(Color.yellow);
        // Erstellung einer Men�leiste
        JMenuBar bar = new JMenuBar();
        // Wir setzen unsere Umrandung f�r unsere JMenuBar
        bar.setBorder(bo);
        // Erzeugung eines Objektes der Klasse JMenu
        JMenu menu = new JMenu("View");
        this.showInactiveRowsMenuItem =  new JCheckBoxMenuItem("Show inactive rows");
        this.showInactiveRowsMenuItem.setSelected(true);
        this.showInactiveRowsMenuItem.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            SimSearchJFrame.this.table.showInactiveRows(((JCheckBoxMenuItem) arg0.getSource()).isSelected());
          }
          
        });
        menu.add(this.showInactiveRowsMenuItem);
        // Men� wird der Men�leiste hinzugef�gt
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
	    this.simSetCountLabel.setText(this.currentSimSetIndex+1 + " / ?(" + this.simSearch.getSimSetCount() + ")");this.simSetCountLabel.setText(this.currentSimSetIndex+1 + " / ?(" + this.simSearch.getSimSetCount() + ")");
	  } else {
	    this.simSetCountLabel.setText(SimSearchJFrame.this.currentSimSetIndex+1 + " / " + SimSearchJFrame.this.simSearch.getSimSetCount());
	  }
	}
	
	@Override
	public void searchFinished() {
		// TODO Auto-generated method stub
	  SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // Here, we can safely update the GUI
          // because we'll be called from the
          // event dispatch thread
          SimSearchJFrame.this.searchIsOn = false;
          SimSearchJFrame.this.updateSimSetCountLabel();
          //SimSearchJFrame.this.simSetCountLabel.setText(SimSearchJFrame.this.currentSimSetIndex+1 + " / " + SimSearchJFrame.this.simSearch.getSimSetCount());
          SimSearchJFrame.this.navToLast.setEnabled((SimSearchJFrame.this.simSearch.getSimSetCount()>SimSearchJFrame.this.currentSimSetIndex+1));
          SimSearchJFrame.this.setUserIsWaiting(false);
        }
      });
	}

	@Override
	public void searchCanceled() {
		// TODO Auto-generated method stub
	  SimSearchJFrame.this.setUserIsWaiting(false);
	  SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // Here, we can safely update the GUI
          // because we'll be called from the
          // event dispatch thread
          SimSearchJFrame.this.searchIsOn = false;
          SimSearchJFrame.this.updateSimSetCountLabel();
          SimSearchJFrame.this.navToLast.setEnabled((SimSearchJFrame.this.simSearch.getSimSetCount()>SimSearchJFrame.this.currentSimSetIndex+1));
          //SimSearchJFrame.this.simSetCountLabel.setText(SimSearchJFrame.this.currentSimSetIndex+1 + " / ?(" + SimSearchJFrame.this.simSearch.getSimSetCount() + ")");
          //SimSearchJFrame.this.navForward.setEnabled(SimSearchJFrame.this.currentSimSetIndex<SimSearchJFrame.this.simSearch.getSimSetCount()-1);
        }
      });
	}

	@Override
	public void newSimSetFound() {
		// TODO Auto-generated method stub
	  if(simSearch.getSimSetCount()==1) simSearch.loadData(0);
	  SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          // Here, we can safely update the GUI
	          // because we'll be called from the
	          // event dispatch thread
	          SimSearchJFrame.this.updateSimSetCountLabel();
	          //SimSearchJFrame.this.simSetCountLabel.setText(SimSearchJFrame.this.currentSimSetIndex+1 + " / ?(" + SimSearchJFrame.this.simSearch.getSimSetCount() + ")");
	          SimSearchJFrame.this.navForward.setEnabled(SimSearchJFrame.this.currentSimSetIndex<SimSearchJFrame.this.simSearch.getSimSetCount()-1);
	        }
	      });
	}

	@Override
	public void searchProgressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataSaved(boolean complete) {
		// TODO Auto-generated method stub
	  SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // Here, we can safely update the GUI
          // because we'll be called from the
          // event dispatch thread
          SimSearchJFrame.this.reloadView();
        }
      });
	}

	@Override
	public void simSearchError(Exception err) {
		// TODO Auto-generated method stub
		String message = err.getMessage();
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		      // Here, we can safely update the GUI
		      // because we'll be called from the
		      // event dispatch thread
		      err.printStackTrace();
		      JOptionPane.showMessageDialog(SimSearchJFrame.this, err.getCause().toString(), "Error", JOptionPane.ERROR_MESSAGE);
		    }
		  });
	}

  @Override
  public void dataLoaded(SimSearchTableModel tableModel, int index) {
    // TODO Auto-generated method stub
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // Here, we can safely update the GUI
        // because we'll be called from the
        // event dispatch thread
        if(SimSearchJFrame.this.currentSimSetIndex>=0) SimSearchJFrame.this.saveTableSettings();
        SimSearchJFrame.this.setUserIsWaiting(false);
        SimSearchJFrame.this.navToFirst.setEnabled(index>0);
        SimSearchJFrame.this.navBack.setEnabled(index>0);
        SimSearchJFrame.this.navForward.setEnabled(index<SimSearchJFrame.this.simSearch.getSimSetCount()-1);
        SimSearchJFrame.this.navToLast.setEnabled(!SimSearchJFrame.this.searchIsOn && index<SimSearchJFrame.this.simSearch.getSimSetCount()-1);
        SimSearchJFrame.this.currentSimSetIndex=index;
        SimSearchJFrame.this.updateSimSetCountLabel();
        SimSearchJFrame.this.filterText.setEnabled(true);
        SimSearchJFrame.this.table.loadData(tableModel);
      }
    });
  }

@Override
public void windowOpened(WindowEvent e) {
	// TODO Auto-generated method stub
	this.simSearchSettings = new SimSearch.Settings();
	final PlausibleDialog4Krise pd4 = new PlausibleDialog4Krise(this, this.simSearchSettings); 
  	pd4.setVisible(true);
  	if (pd4.okPressed) {
  		this.startSearch();
  	}
}

@Override
public void windowClosing(WindowEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void windowClosed(WindowEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void windowIconified(WindowEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void windowDeiconified(WindowEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void windowActivated(WindowEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void windowDeactivated(WindowEvent e) {
	// TODO Auto-generated method stub
	
}
	
}
