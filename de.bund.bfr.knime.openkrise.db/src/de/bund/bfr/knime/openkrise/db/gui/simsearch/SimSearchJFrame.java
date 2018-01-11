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
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
//import com.sun.glass.ui.Cursor;
//import DBMergeServer.SimSearch;
//import SimSearch.SimSearchListener;
//import DBMergeServer.SimSearch.SimSearchTableModel;

public class SimSearchJFrame extends JFrame implements SimSearch.SimSearchListener {
	
	private final SimSearchTable table = new SimSearchTable();
	
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
	
	private SimSearch simSearch;
	private SimSearch.Settings simSearchSettings;
	
	private int currentSimSetIndex;
	private boolean userIsWaiting;
	
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
	    this.setSize(320, 240);
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
		this.redoButton = new JButton("redo");
		this.ignoreButton = new JButton("ignore");
		Arrays.asList(this.undoButton, this.redoButton, this.ignoreButton).forEach(c -> undoRedoPanel.add(c));
		
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
		Arrays.asList(this.navToFirst, this.navBack, this.navForward, this.navToLast).forEach(b -> {
			b.setEnabled(false);
			navPanel.add(b);
		});
		
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
	
	private void addTable() {
		//this.table.setEnabled(false);
		//JScrollPane scrollPane = new JScrollPane(this.table);
		//p.setBorder(BorderFactory.createTitledBorder("Similarity Search"));
		//scrollPane.setBorder(BorderFactory.createTitledBorder("Results"));
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
        JMenu menu = new JMenu("Ich bin ein JMenu");
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
	
	@Override
	public void searchFinished() {
		// TODO Auto-generated method stub
	  SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // Here, we can safely update the GUI
          // because we'll be called from the
          // event dispatch thread
          SimSearchJFrame.this.simSetCountLabel.setText(SimSearchJFrame.this.currentSimSetIndex+1 + " / " + SimSearchJFrame.this.simSearch.getSimSetCount());
          SimSearchJFrame.this.navToLast.setEnabled((SimSearchJFrame.this.simSearch.getSimSetCount()>SimSearchJFrame.this.currentSimSetIndex));
          SimSearchJFrame.this.setUserIsWaiting(false);
        }
      });
	}

	@Override
	public void searchCanceled() {
		// TODO Auto-generated method stub
	  SimSearchJFrame.this.setUserIsWaiting(false);
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
	          SimSearchJFrame.this.simSetCountLabel.setText(SimSearchJFrame.this.currentSimSetIndex+1 + " / ?(" + SimSearchJFrame.this.simSearch.getSimSetCount() + ")");
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
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		      // Here, we can safely update the GUI
		      // because we'll be called from the
		      // event dispatch thread
		      JOptionPane.showMessageDialog(SimSearchJFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        SimSearchJFrame.this.currentSimSetIndex=index;
        SimSearchJFrame.this.filterText.setEnabled(true);
        SimSearchJFrame.this.table.loadData(tableModel);
      }
    });
  }
	
}
