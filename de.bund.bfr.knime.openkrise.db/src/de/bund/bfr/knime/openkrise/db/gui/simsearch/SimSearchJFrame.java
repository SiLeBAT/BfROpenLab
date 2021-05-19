/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.gui.MainFrame;
import de.bund.bfr.knime.openkrise.db.gui.MyList;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;


public class SimSearchJFrame extends JDialog implements SimSearch.SimSearchListener {


	private static final long serialVersionUID = 8165724594614226211L;
	
	private InfoBox infobox = null;

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
	private final Color COLOR_INDEX_DECISION_IS_NEEDED = Color.RED;
	private final Color COLOR_INDEX_DECISION_IS_NOT_NEEDED = new Color(0,150,0);

	private SimSearch simSearch;
	private SimSearch.Settings simSearchSettings;

	private int currentSimSetIndex;
	private SimSearch.SimSet currentSimSet;
	private boolean userIsWaiting;
	private volatile boolean searchIsOn;
	private boolean  blockActions;

	public SimSearchJFrame(Frame owner) {
		super(owner);
		this.setModal(true);
		this.initComponents();
		this.userIsWaiting = false;
		this.searchIsOn = false;
		this.blockActions = false;
	}

	// GUI setup start

	private void initComponents() {
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				SimSearchJFrame.this.processUserCloseRequest();
			}

			@Override
			public void windowOpened(WindowEvent e) {
				SimSearchJFrame.this.processWindowOpenedEvent();
			}
		});

		try {
			this.setIconImage(ImageIO.read(MainFrame.class.getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Eye.gif")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.setTitle("Similarity Search");
		
		this.addTopPanel();
		this.addTable();
		this.addMenu();
		this.addBottomPanel();
		this.table.registerRowTextFilter(this.filterTextField, this.useRegularExpressionsFilterCheckBox);
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

		this.useRegularExpressionsFilterCheckBox = new JCheckBox("Use regular expressions");

		Arrays.asList(filterLabel, this.filterTextField, this.useRegularExpressionsFilterCheckBox).forEach(c -> filterPanel.add(c));

		JPanel undoRedoPanel = new JPanel();
		topPanel.add(undoRedoPanel, BorderLayout.CENTER);



		this.undoButton = new JButton("undo");
		this.undoButton.setToolTipText("Undo operation."); //  bundle.getString("MainFrame.button10.toolTipText"));
		this.redoButton = new JButton("redo");
		this.redoButton.setToolTipText("Redo operation."); //  bundle.getString("MainFrame.button10.toolTipText"));
		Arrays.asList(this.undoButton, this.redoButton).forEach(c -> undoRedoPanel.add(c));

		JPanel ignorePanel = new JPanel();
		this.ignoreSimSetButton = new JButton("<html>Comparison row (" + SimSearchJTable.RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + ") is unique</html>");   //new JToggleButton("Ignore");
		this.ignoreSimSetButton.setToolTipText("Mark all rows as different to the comparison row."); //  bundle.getString("MainFrame.button10.toolTipText"));
		this.ignoreAllPairsInSimSetButton = new JButton("All rows are unique");
		this.ignoreAllPairsInSimSetButton.setToolTipText("Mark all rows as pairwise different."); 
		Arrays.asList(ignoreSimSetButton, ignoreAllPairsInSimSetButton).forEach(b -> {
			ignorePanel.add(b);
			b.setEnabled(false);
		});

		topPanel.add(ignorePanel, BorderLayout.LINE_END); 


		ActionListener undoRedoActionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SimSearchJFrame.this.processUndoRedoRequest((JButton) e.getSource());
			}

		};
		Arrays.asList(this.undoButton, this.redoButton).forEach(b-> b.addActionListener(undoRedoActionListener));

		Arrays.asList(this.undoButton, this.redoButton).forEach(c -> c.setEnabled(false));

		filterTextField.setPreferredSize(new Dimension(this.undoButton.getPreferredSize().height * 5,  this.undoButton.getPreferredSize().height));
	}

	private void addBottomPanel() {
		JPanel bottomPanel = new JPanel();
		this.getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
		bottomPanel.setLayout(new BorderLayout());
		JPanel navPanel = new JPanel();
		bottomPanel.add(navPanel, BorderLayout.LINE_START);

		this.navToFirst = new JButton();
		this.navToFirst.setText("<<");
		this.navToFirst.setToolTipText("Show first findings.");
		this.navToLast = new JButton();
		this.navToLast.setText(">>");
		this.navToLast.setToolTipText("Show last findings.");
		this.navBack = new JButton();
		this.navBack.setText("<");
		this.navBack.setToolTipText("Show previous findings.");
		this.navForward = new JButton();
		this.navForward.setText(">");
		this.navForward.setToolTipText("Show next findings.");

		List<JButton> navButtonList = Arrays.asList(this.navToFirst, this.navBack, this.navForward, this.navToLast);
		ActionListener actionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SimSearchJFrame.this.processNavigationRequest((JButton) arg0.getSource());
			}

		};

		navButtonList.forEach(b -> {
			b.addActionListener(actionListener);
			navPanel.add(b);
		});
		navPanel.setEnabled(false);

		this.simSetCountLabel = new JLabel();
		this.simSetCountLabel.setForeground(Color.RED);
		Font font = this.simSetCountLabel.getFont();
		this.simSetCountLabel.setFont(font.deriveFont(Font.BOLD, font.getSize()+1));
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
				SimSearchJFrame.this.processUserCloseRequest();
			}

		});
		this.okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SimSearchJFrame.this.processUserOkRequest();
			}
		});
		this.applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SimSearchJFrame.this.processUserApplyRequest();
			}
		});
		this.applyButton.setEnabled(false);
		this.registerCancelAndOkButtons();
	}

	private void registerCancelAndOkButtons() {

		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL");
		getRootPane().getActionMap().put("CANCEL", this.cancelButton.getAction());

		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
		getRootPane().getActionMap().put("ENTER", this.okButton.getAction());

	}

	private void addTable() {
		this.table = new SimSearchTable(loadViewSettings());

		this.table.setBorder(BorderFactory.createTitledBorder("Results"));
		this.getContentPane().add(this.table, BorderLayout.CENTER);
	}

	private void addMenu() {
		Border bo = new LineBorder(Color.yellow);

		JMenuBar bar = new JMenuBar();

		bar.setBorder(bo);

		JMenu menu = new JMenu("View");
		
		this.table.addMenuItems(menu);

		bar.add(menu);

		menu = new JMenu("Settings");
		JMenuItem menuItem = new JMenuItem("Search settings ..");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SimSearchJFrame.this.processEditSettingsRequest(true);
			}

		});
		menu.add(menuItem);
		bar.add(menu);

		menu = new JMenu("Help");
		menuItem = new JMenuItem("Help..");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SimSearchJFrame.this.processUserHelpRequest();
			}

		});
		menu.add(menuItem);
		bar.add(menu);

		this.setJMenuBar(bar);
	}

	// GUI setup end
	
	private void restartSearch(SimSearch.Settings settings) {
	  if(simSearch!=null) {
        this.simSearch.removeEventListener(this);
        //ToDo: check whether to unregister ManipulationState listeners to
        this.simSearch = null;
        this.updateSimSetIndependentButtons();
        this.clearTable();
      }
	  startSearch(settings);
	}
	
	public void startSearch(SimSearch.Settings settings) {
	    if(blockActions || searchIsOn) return;
	    
	    if(simSearch!=null) {
	      restartSearch(settings);
	      return;
	    }
	      	    
		this.simSearchSettings = settings; 
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
			this.navToLast.setEnabled((this.simSearch.getSimSetCount()>Math.max(0,this.currentSimSetIndex+1)));
			if(this.simSearch.getSimSetCount()==0 && searchCompleted) {
			  JOptionPane.showMessageDialog(null, "No similarities found.", "Similarity search result", 1);
			  processEditSettingsRequest(false);
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					SimSearchJFrame.this.finishAsyncSearch(searchCompleted);
				}
			});
		}
	}

	private void processNavigationRequest(JButton source) {
		int index = -1;
		if(source==this.navToFirst) index = this.simSearch.getIndexOfNextNotIgnoredSimSet(-1);
		else if(source==this.navBack) index = this.simSearch.getIndexOfPreviousNotIgnoredSimSet(this.currentSimSetIndex); 
		else if(source==this.navForward) index = this.simSearch.getIndexOfNextNotIgnoredSimSet(this.currentSimSetIndex); 
		else if(source==this.navToLast) index = this.simSearch.getIndexOfPreviousNotIgnoredSimSet(-1); 
		else { 
			// do nothing
			return;
		} 
		if(index>=0) this.startAsyncDataLoad(index);
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
			
			if(tableModel!=null) {
				
				if(this.currentSimSetIndex<0) Arrays.asList(this.filterLabel, this.filterTextField, this.useRegularExpressionsFilterCheckBox, this.ignoreSimSetButton, this.ignoreAllPairsInSimSetButton).forEach(c -> c.setEnabled(true));
				
				this.setUserIsWaiting(false);
				this.navToFirst.setEnabled(simSetIndex>this.simSearch.getIndexOfPreviousNotIgnoredSimSet(simSetIndex));
				this.navBack.setEnabled(this.navToFirst.isEnabled());
				this.navForward.setEnabled(simSetIndex<this.simSearch.getIndexOfNextNotIgnoredSimSet(simSetIndex));
				this.navToLast.setEnabled(!this.searchIsOn &&this.navForward.isEnabled());
				this.currentSimSetIndex=simSetIndex;
				this.currentSimSet = this.simSearch.getSimSet(simSetIndex);
				this.updateSimSetCountLabel();
				
				this.table.loadData(tableModel);
				
			} else {
				
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
	
	private boolean preprocessUserSaveOrApplyRequest(boolean applyOnly) {
	  if(!applyOnly && simSearch.isDecisionNeeded()) {
	    switch(JOptionPane.showConfirmDialog(this, "There are still open decisions left, which will be discarded. Proceed?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
          case JOptionPane.NO_OPTION:
            return false;
          case JOptionPane.YES_OPTION:
            break;
          default:
        }
	  }
	  if(this.simSearch.existDataManipulations() && searchIsOn) {
        switch(JOptionPane.showConfirmDialog(this, "Before the data can be saved the search will be stopped. Proceed?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
          case JOptionPane.NO_OPTION:
            return false;
          case JOptionPane.YES_OPTION:
            return true;
          default:
            return true;
        }
      } else return true;
	}

	private void blockActions(boolean block) {
	  blockActions = block;
	}
	
	private void asyncSave(Consumer<Boolean> notifier) {
	  Runnable runnable = new Runnable(){

	    public void run(){
	      
	      boolean result = false;
	      try {

	        syncStopSearchAndWait();

	        result = syncSave();

	      } catch (Exception err) {
	        SimSearchJFrame.this.showError(err, "Data could not be saved."); 

	      } finally {
	        notifier.accept(result);
	      }
	    }
	  };

	  Thread thread = new Thread(runnable);
	  thread.start();
	}
	
	private void processUserApplyRequest() {
	  if (this.simSearch==null) return;
	  if(areActionsBlocked()) return; 
	  
	  if(!this.preprocessUserSaveOrApplyRequest(true)) return;
	  blockActions(true);
	  
	  asyncSave(b -> finishUserApplyRequestProcessing(b));
	}
	
	private void finishUserApplyRequestProcessing(boolean applySucceeded) {
	  if(SwingUtilities.isEventDispatchThread()) {
	    // 
	    blockActions(false);
	    
	    refreshMainFrame();
	    
	    reloadData();

	  } else {
	    SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
	        finishUserApplyRequestProcessing(applySucceeded);
	      }
	    });
	  }
    }
	
	private void refreshMainFrame() {
	  // call:
	  // DBKernel.mainFrame.getMyList().getMyDBTable().setTable();
	 
	  MainFrame mainFrame = DBKernel.mainFrame;
	  if(mainFrame==null) return;
	  
	  MyList myList = mainFrame.getMyList();
	  if(myList==null) return;
	  
	  MyDBTable myDBTable = myList.getMyDBTable();
	  if(myDBTable==null) return;
	  
	  myDBTable.setTable();
	}
	
	private void processUserOkRequest() {
      if (this.simSearch==null) return;
      if(areActionsBlocked()) return; 
      if(!this.preprocessUserSaveOrApplyRequest(false)) return;
      blockActions(true);
      
      asyncSave(b -> finishUserOkRequestProcessing(b));
    }
    
    private void finishUserOkRequestProcessing(boolean saveSucceeded) {
      if(SwingUtilities.isEventDispatchThread()) {
        //
        blockActions(false);
        
        refreshMainFrame();
        
        if(saveSucceeded) this.dispose();
        else reloadData();
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            finishUserOkRequestProcessing(saveSucceeded);
          }
        });
      }
    }
    
    private void asyncStopSearchAndWait(Runnable notifier) {
  
      Runnable runnable = new Runnable(){

        public void run(){
          
          syncStopSearchAndWait();
          notifier.run();
          
        }
      };

      Thread thread = new Thread(runnable);
      thread.start();
    }
    
    private boolean areActionsBlocked() { return blockActions; }
    
    private void processUserCloseRequest() {
      
      if (this.simSearch==null) return;
      if(areActionsBlocked()) return; 
      
      if(this.simSearch.existDataManipulations()) {

        switch(JOptionPane.showConfirmDialog(this, "Your changes have not been saved yet. Save?", null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
          case JOptionPane.YES_OPTION:
            this.processUserOkRequest();
            return;
          case JOptionPane.CANCEL_OPTION:
            return;
          case JOptionPane.NO_OPTION:
            // just continue closing the window
            break;
        }
        
      } else if(searchIsOn) {
        
        switch(JOptionPane.showConfirmDialog(this, "The search is still running and will be stopped. Abort nevertheless?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
          case JOptionPane.YES_OPTION:
           // just continue closing the window
            break;
          case JOptionPane.NO_OPTION:
            return;
        }
      }
      
      blockActions(true);
      
      asyncStopSearchAndWait(new Runnable() { 
        public void run() {
          finishUserCloseRequestProcessing();
        }
      });
      
    }
    
    private void finishUserCloseRequestProcessing() {
      if(SwingUtilities.isEventDispatchThread()) {
        //
        blockActions(false);
        this.dispose();
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            finishUserCloseRequestProcessing();
          }
        });
      }
    }
    
    private void reloadData() {
      
      int simSetIndex = this.simSearch.getSimSetIndex(currentSimSet);    //
      if(simSetIndex!=this.currentSimSetIndex) simSetIndex = -1;     // save or apply occured
      if(simSetIndex>=simSearch.getSimSetCount()) simSetIndex = -1;
      if(simSetIndex<0 || this.simSearch.isSimSetIgnored(simSetIndex)) simSetIndex = this.simSearch.getIndexOfNextNotIgnoredSimSet(simSetIndex);
      if(simSetIndex>=0) this.startAsyncDataLoad(simSetIndex);
      else this.clearTable();
    }
	
	private boolean syncSave() {
	  boolean result = false;
	  try {
	    result = SimSearchJFrame.this.simSearch.save();
	  } catch (Exception err) {
	    SimSearchJFrame.this.showError(err, "Saving data to database failed.");
	  } finally {   
	  }
	  return result;
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

			this.updateSimSetIndependentButtons();

			if(reloadRequired) reloadData();

		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					SimSearchJFrame.this.processManipulationStateChanged(reloadRequired);
				}
			});
		}
	}
	
	private void updateSimSetIndependentButtons() {
	  this.redoButton.setEnabled(this.simSearch!=null && this.simSearch.isRedoAvailable());
      this.redoButton.setToolTipText((this.simSearch!=null?this.simSearch.getRedoType():null));

      this.undoButton.setEnabled(this.simSearch!=null && this.simSearch.isUndoAvailable());
      this.undoButton.setToolTipText((this.simSearch!=null?this.simSearch.getUndoType():null));

      this.applyButton.setEnabled(this.simSearch!=null && this.simSearch.existDataManipulations());
	}

	private void clearTable() {
		this.table.clear();
		Arrays.asList(this.navBack, this.navForward, this.navToFirst, this.navToLast, this.ignoreSimSetButton, this.ignoreAllPairsInSimSetButton).forEach(b -> b.setEnabled(false));
		this.currentSimSetIndex = -1;
		this.updateSimSetCountLabel();
	}



	private SimSearchTable.ViewSettings loadViewSettings() {
		return new SimSearchTable.ViewSettings();
	}

	private void setUserIsWaiting(boolean isWaiting) {
		if(this.userIsWaiting!=isWaiting) {
			if(isWaiting) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			} else {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			this.userIsWaiting = isWaiting;
		}
	}

	private void showWaitDialog(String text) {
		// TODO: show here a wait message box
	}

	private void updateSimSetCountLabel() {
	    if(this.simSearch==null) {
	      this.simSetCountLabel.setText("");
	      this.simSetCountLabel.setToolTipText("");
	    } else if(this.searchIsOn) {
			this.simSetCountLabel.setText(this.simSearch.getNotIgnoredSimSetIndex(currentSimSetIndex)+1 + " / ?(" + (this.simSearch.getNotIgnoredSimSetIndex(this.simSearch.getSimSetCount()-1)+1) + ")");
			this.simSetCountLabel.setForeground(COLOR_INDEX_DECISION_IS_NEEDED);
			this.simSetCountLabel.setToolTipText("The search is not finished yet.");
		} else {
			this.simSetCountLabel.setText(this.simSearch.getNotIgnoredSimSetIndex(currentSimSetIndex)+1 + " / " + (this.simSearch.getNotIgnoredSimSetIndex(this.simSearch.getSimSetCount()-1)+1));
			if(simSearch.isDecisionNeeded()) {
			  this.simSetCountLabel.setForeground(COLOR_INDEX_DECISION_IS_NEEDED);
			  this.simSetCountLabel.setToolTipText("Open decisions left.");
			} else {
			  this.simSetCountLabel.setForeground(COLOR_INDEX_DECISION_IS_NOT_NEEDED);
			  this.simSetCountLabel.setToolTipText("No open decisions left.");
			}
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
			this.simSearchSettings = SimSearchSettingsDialog.editSettings(this, new SimSearch.Settings(), null);
			if(this.simSearchSettings==null) this.dispose();
			else this.startSearch(this.simSearchSettings);
		}
	}

	private void syncStopSearchAndWait() {
      this.simSearch.stopSearch();
      while(this.searchIsOn) 
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        
        e.printStackTrace();
      }
    }
	
	private void processEditSettingsRequest(boolean isUserRequest) {
	 
	  SimSearch.Settings newSettings = SimSearchSettingsDialog.editSettings(this, this.simSearchSettings, (h) -> prepareSettingsChange(h));
	  
	  if(newSettings!=null) {
	    if(!newSettings.equals(this.simSearchSettings)) {
	      // settings changed, start new search
	      this.restartSearch(newSettings);
	    } else if(this.simSearch.getSimSetCount()==0 && !searchIsOn) {
	      // settings didn't change, but last search yielded zero count so restart
	      this.restartSearch(newSettings);
	    }
	  } else {
	    // Settings dialog was canceled
	    if(!isUserRequest) {
	      // If the SettingsDialog was automatically opened and canceled by the user the complete Search window shall close 
	      processUserCloseRequest();
	    }
	  }
	}
	
	private void prepareSettingsChange(Consumer<Boolean> continueEditSettings) {
	  boolean saveChanges = false;
	  boolean isFirstQuestion = true;
	  
	  if(this.simSearch!=null && this.simSearch.existDataManipulations()) {
	    // ask whether to apply changes
    	  switch(JOptionPane.showConfirmDialog(this, "A new search will be started. Your changes have not been saved yet. Save?", null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
    	    case JOptionPane.CANCEL_OPTION:
    	      continueEditSettings.accept(false);
              return;
    	    case JOptionPane.YES_OPTION:
    	      saveChanges = true;
    	      break;
    	    case JOptionPane.NO_OPTION:
    	      // just continue
    	      break;
    	  }
    	  isFirstQuestion = false;
      }
	  
	  if(this.simSearch!=null && this.simSearch.isDecisionNeeded()) {
          // ask whether to discard undecided questions
	      String question = "There are still open decisions left, which will be discarded. Change settings nevertheless?";
	      if(isFirstQuestion) question = "A new search will be started. " + question; 
          switch(JOptionPane.showConfirmDialog(this, question, null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            case JOptionPane.YES_OPTION:
              // just continue
              break;
            case JOptionPane.NO_OPTION:
              // abort
              continueEditSettings.accept(false);
              return;
          }
          isFirstQuestion = false;
      }
	  
	  if(this.searchIsOn) {
	    // stop previous search?
	    switch(JOptionPane.showConfirmDialog(this, "The previous search is still running and will be stopped. Change settings nevertheless?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
          case JOptionPane.NO_OPTION:
            continueEditSettings.accept(false);
            return;
          case JOptionPane.YES_OPTION:
            // just continue
            break;
        }
	  }
	  
	  final boolean finalSaveChanges = saveChanges;
	  
	  Runnable runnable = new Runnable(){
    
            public void run(){
                boolean result = false;
                try {
                    syncStopSearchAndWait();
                                          
                    result = !finalSaveChanges || syncSave();
                    
                } catch (Exception err) {
                  SimSearchJFrame.this.showError(err, "Settings could not be changed."); 
             
                } finally {
                  continueEditSettings.accept(result);
                }
            }
        };
    
        Thread thread = new Thread(runnable);
        thread.start();
	}

	
	private void processUserHelpRequest() {
      showHelp();  
    }

	protected void showHelp() {
	  if(infobox==null) openHelp();
	  else infobox.toFront();
	}

  private void openHelp() {
    String help = "<html>\n";

    help += "<h1>Similarity Search</h1>";
    help += "A similarity search is used to get rid of duplicate entries in the database. It is recommended after importing data into the database. Otherwise, the food chain shown in the Tracing View graph might not reflect the true food chain and tracing might be less successful.";
    help += "<br><br><h2>Background</h2>";
    help += "An important prerequisite for tracing analysis is to have good data quality. Ideally, each individual station, product, lot and delivery is represented only once in the database. As FoodChain-Lab identifies stations (and also products, lots and deliveries) on the basis of their properties (name and address), a second import of the identical station but spelled slightly different would result in a new entry for this station. It would now be a duplicate and for a correct analysis duplicates should be merged to one entry using the similarity search.";
    help +="<br>The algorithm used to find similarities is the Dice's similarity coefficient (see https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient).";
    help += "<br><br>";
    help += "<h2>Similarity search settings</h2>";
    help += "Choose the category to be checked for similarity: Station, Product, Lot or Delivery. To change the percentage of similarity for each parameter either click into the white boxes and type the desired value in or use the up and down arrows. Parameters which are shown greyed out cannot be changed and are only displayed to inform on all the parameters which are considered in a similarity search.";
    help += "For the category ‘Station’ two parameters can be adjusted (‘Name’ and ‘Address’). Both are connected via a logical AND meaning that only those stations will be shown as similar for which both similarity parameter values are true at the same time.";
    help += "<br>";
    help += "<h3>Examples:</h3>";
    help += "<ul>";
    help += "<li>50% similarity means that half of the letters of a name should be identical. Now, for every entry the search algorithm lists all other entries having 50% similarity.</li>";
    help += "<li>To find stations with names that are at least 90% identical and at the same time have at least 80% similarity in their addresses enter ‘Name’ = 90 and ‘Address’ = 80.</li>";
    help += "<li>To find stations with identical addresses the station names are irrelevant and thus ‘Name’ = 0% and ‘Address’ = 100% will result in stations (e.g. companies) which are located at the same address. This is helpful if several different data providers send information on identical companies but with varying names (e.g. ‘Luxury Cakes Limited’ and ‘Luxury Cakes Ltd.’, both with identical addresses).</li>";
    help += "<li>A value of 100 means that two items are treated as 'similar' only if they are to 100% identical. Product names ‘Strawberry’ and ‘Strawberries’ would not be listed as similar in this case.</li>";
    help += "<li>A value of 0 means that two items are always treated as 'similar'. With this setting, even the two product names ‘strawberry’ and ‘meat’ would be shown in the results table.</li>";
    help += "</ul>";
    help += "<br>";
    help += "Check option 'Also show results previously marked as unique' to disable filtering of similarities which have already been decided on.";
    help += "<br>";
    help += "<br>Example:<br>";
    help += "Two stations were displayed as being similar but as they were in reality different, the user clicked the button ‘All stations are unique’. After some time the user is not so sure anymore whether this decision had been correct. Using the same parameter settings for ‘Name’ and ‘Address’ as in the previous search these two stations will be displayed in the results table again if 'Also show results previously marked as unique' is checked.";
    help += "<br><br>";
    help += "<h2>Search Results</h2>";
    help += "In the resulting table all differences are highlighted. Now the user must decide whether there are any duplicates (or whether all items are unique). Duplicates should be merged.";
    help += "<br>";
    help += "<br>Symbols:";
    help += "<ul>";
    help += "<li>‘" + SimSearchJTable.RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + "’ in the first column indicates the comparison row. This entry was used for comparison with all other entries in the database. The comparison row is not to be understood as a preference.</li>";
    help += "<li>‘" + SimSearchJTable.AlignmentColumnRenderer.SYMBOL_GAP + "’ represents a gap in a text. This gap is necessary to be able to align this text to another one.</li>";
    help += "<li>‘" + SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText(SimSearchJTable.AlignmentColumnRenderer.SYMBOL_SPACE_DELETE) + "’ represents a space character in a text which is aligned to a different character or a gap in the comparison text.</li>";
    help += "</ul>";
    help += "<br>";
    help += "Colours:<br>";
    help += "The colouring reflects the difference of the text to the text in the comparison row:<br>";
    help += "<ul>";
    help += "<li>Black indicates no difference.</li>"; 
    help += "<li>Red indicates a difference to the text in the comparison row.</li>"; 
    help += "<li>Green indicates a gap in a text which is aligned to a character in some other text.</li>";
    help += "</ul>";
    help += "<br>";

    help += "Example:";
    Alignment.AlignedSequence[] alignedSeq = Alignment.alignSequences(new String[] {"Am Burggraben 128",  "Am Burg grabem 18", "Amm Bruggraben 128"},0);
    try {
        help += "<br><table cellpadding=0>" +
                "<tr><td>Address 1 in comparison row (" + SimSearchJTable.RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + "):&nbsp;</td><td>" + 
                SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[0], false) + "</td></tr>" + 
                "<tr><td>Address 2 in row x:</td><td>" + SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[1], false) + "</td></tr>" + 
                "<tr><td>Address 3 in row y:</td><td>" + SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[2], false) + "</td></tr>" + 
                "</table><br>" ;
    } catch (Exception e) {
        help += "<i>Example is missing.</i>";
    } 

    help += "This example shows an alignment of three addresses. Address 3 has a double ‘mm’ which is different to the address in the comparison row. Address 2 has a space character between the double ‘gg’ which is not matched by the comparison sequence."; 
    help += "<br><br>"; 
    help += "Navigating through search results:<br>"; 
    help += "Using the ‘&lt;’ or ‘>’ buttons the user can switch to the previous or next result. With the buttons ‘&lt;&lt;’ and ‘>>’ it is possible to jump to the first or last result."; 
    help += "<br><br>";  
    help += "Please note: '…' below a text within a cell indicates a multiline text. To see the complete text either use the mouse to display the tooltip of the cell, or increase the row size by dragging the bottom border of the first column."; 
    

    help += "<br><br>";
    help += "<h2>Merging</h2>";
    help += "Two rows which belong to the identical element (station, product, lot or delivery) shall be merged. This can be accomplished by a drag-and-drop action: Left click and hold the duplicate row and drop it onto the row that should be kept. It is like moving a file (or a selection of files) to a folder in a file browser. Any row can be merged into any other row (which has not been merged, yet).";
    help += "<br>Merging can be undone by clicking the <b>undo</b> button above the table or by choosing <b>unmerge</b> in the context menu (right-click on a merged row)."; 
    help += "<br><br>"; 
    help += "Please note that dropping one or more rows <b>between</b> other rows does not result in a merge, but in a changed row order."; 
    
    help += "<br><br>";
    help += "<h2>Marking as being unique</h2>";
    help += "If only the comparison row is unique, click the button ‘<b>Comparison station/product/lot/deliveries is unique</b>’ to mark all rows (that are not already merged) as different to the comparison row.";
    help += "<br>If all rows are unique click the button ‘<b>All stations/products/lots/deliveries are unique</b>’ to mark all pairs of rows (that are not already merged) as different to each other."; 
    help += "<br>";
    help += "<br>After pressing one of the two buttons the table automatically switches to the next result.";
    help += "<br>";
    help += "<br>Once marked as unique, those results are not shown again in later similarity searches within this database. In order to show ‘markings as being unique’ the option ‘<b>Also show results previously marked as unique</b>‘ in the search settings can be checked."; 

    help += "<br>";
    help += "<h3>Filtering</h3>";
    help += "In the top left corner there is a filter panel. Items in the table can be filtered by entering a filter text. For a more sophisticated filtering check the option “use regular expressions”.";

    help += "<br><br>";
    help += "<h2>View Options</h2>";
    help += "<ul margin-bottom=0>";
      help += "<li>Rows which have been merged can be hidden in the results table by checking ‘<b>Hide merged rows</b>’ in the View menu.</li>";
      help += "<li>Rows can be:";
        help += "<ul margin-top=0 margin-bottom=0>";
          help += "<li>Sorted: by left clicking on the column the rows shall be sorted with respect to it, a second click reverses the ordering</li>";
          help += "<li>Resized: by dragging its bottom border in the first column</li>";
        help += "</ul></li>";
      help += "<li>The font size can be increased or decreased by clicking the respective option in <b>Table Font Size</b> in the ‘<b>View</b>’ menu or via pressing <b>Ctrl+Plus</b> or <b>Ctrl+Minus</b>.</li>"; 
      help += "<li>Columns can be";
        help += "<ul>";
          help += "<li>Moved: Drag & drop on column headline with the left mouse button</li>";
          help += "<li>Hidden/shown: Right mouse button on column headline, show columns, check column names</li>";
          help += "<li>Frozen/unfrozen: Right mouse button on column headline, Freeze/Unfreeze. Frozen columns are moved to the left of the table. (Please note: There can be only one frozen column.)</li>";
          help += "<li>Resized: By dragging the right side of its header</li>";
        help += "</ul>";
      help += "</li>";
    help += "</ul>";
    help += "</html>\n";

    InfoBox ib = new InfoBox(this, help, true, new Dimension(800, 400), null, false, true);
    infobox = ib;
    ib.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        infobox = null;
      }
    });
    //ib.setModalityType(ModalityType.MODELESS);
    //ib.setModalExclusionType(null);
    ib.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
    ib.setResizable(true);
    ib.setVisible(true);
    ib.toFront();
  }
  
  
}
