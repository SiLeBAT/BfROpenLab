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
import java.awt.Cursor;
import java.awt.Dimension;
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
import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.gui.MainFrame;
import de.bund.bfr.knime.openkrise.db.gui.PlausibleDialog4Krise;


public class SimSearchJFrame extends JDialog implements SimSearch.SimSearchListener {


	private static final long serialVersionUID = 8165724594614226211L;

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

	private SimSearch simSearch;
	private SimSearch.Settings simSearchSettings;

	private int currentSimSetIndex;
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
				SimSearchJFrame.this.processEditSettingsRequest();
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
	    //System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tstartAsyncSearch entered ...");
		Runnable runnable = new Runnable(){

			public void run(){
				boolean searchCompleted = false;
				try {
				    //System.out.println(Thread.currentThread().getId() + "\tSimSearch.startAsyncSearch.asyncThread before search");
					searchCompleted = SimSearchJFrame.this.simSearch.search(settings);
					//System.out.println(Thread.currentThread().getId() + "\tSimSearch.startAsyncSearch.asyncThread after search");
				} catch (Exception err) {
					SimSearchJFrame.this.showError(err, "Similarity search failed."); 
				}      
				SimSearchJFrame.this.finishAsyncSearch(searchCompleted);
			}
		};

		Thread thread = new Thread(runnable);
		thread.start();
		//System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tstartAsyncSearch leaving ...");
	}

	private void finishAsyncSearch(boolean searchCompleted) {
	    //System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tfinishAsyncSearch entered ...");
		if(SwingUtilities.isEventDispatchThread()) {
		  	// 
			this.setUserIsWaiting(false);
			this.searchIsOn = false;
			
			this.updateSimSetCountLabel();
			this.navToLast.setEnabled((this.simSearch.getSimSetCount()>Math.max(0,this.currentSimSetIndex+1)));
			if(this.simSearch.getSimSetCount()==0 && searchCompleted) {
			  JOptionPane.showMessageDialog(null, "No similarities found.", "Similarity search result", 1);
			  processEditSettingsRequest();
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					SimSearchJFrame.this.finishAsyncSearch(searchCompleted);
				}
			});
		}
		//System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tfinishAsyncSearch leaving ...");
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
	
	private boolean preprocessUserSaveOrApplyRequest() {
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
	  //System.out.println(Thread.currentThread().getId() + "\tSimSearchJFrame.blockActions(" + block + ")");
	  blockActions = block;
	}
	
	private void asyncSave(Consumer<Boolean> notifier) {
	  Runnable runnable = new Runnable(){

	    public void run(){
	      //System.out.println("asyncSave.asynThread entered ...");
	      boolean result = false;
	      try {

	        syncStopSearchAndWait();

	        result = syncSave();

	      } catch (Exception err) {
	        SimSearchJFrame.this.showError(err, "Data could not be saved."); 

	      } finally {
	        notifier.accept(result);
	      }
	      //System.out.println("asyncSave.asynThread leaving");
	    }
	  };

	  Thread thread = new Thread(runnable);
	  thread.start();
	}
	
	private void processUserApplyRequest() {
	  if (this.simSearch==null) return;
	  if(areActionsBlocked()) return; 
	  
	  if(!this.preprocessUserSaveOrApplyRequest()) return;
	  blockActions(true);
	  
	  asyncSave(b -> finishUserApplyRequestProcessing(b));
	}
	
	private void finishUserApplyRequestProcessing(boolean applySucceeded) {
	  if(SwingUtilities.isEventDispatchThread()) {
	    // 
	    blockActions(false);
	    reloadData();

	  } else {
	    SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
	        finishUserApplyRequestProcessing(applySucceeded);
	      }
	    });
	  }
    }
	
	private void processUserOkRequest() {
      if (this.simSearch==null) return;
      if(areActionsBlocked()) return; 
      if(!this.preprocessUserSaveOrApplyRequest()) return;
      blockActions(true);
      
      asyncSave(b -> finishUserOkRequestProcessing(b));
    }
    
    private void finishUserOkRequestProcessing(boolean saveSucceeded) {
      if(SwingUtilities.isEventDispatchThread()) {
        //
        blockActions(false);
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
      //System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tasyncStopSearchAndWait entered ...");
      Runnable runnable = new Runnable(){

        public void run(){
          //System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tasyncStopSearchAndWait.asynThread entered ...");
          syncStopSearchAndWait();
          notifier.run();
          //System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tasyncSave.asynThread leaving ...");
        }
      };

      Thread thread = new Thread(runnable);
      thread.start();
      //System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tasyncStopSearchAndWait leaving ...");
    }
    
    private boolean areActionsBlocked() { return blockActions; }
    
    private void processUserCloseRequest() {
      //System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tprocessUserCloseRequest entered ...");
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
      //System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tprocessUserCloseRequest leaving ...");
    }
    
    private void finishUserCloseRequestProcessing() {
//      System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tfinishUserCloseRequestProcessing entered ...");
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
//      System.out.println(Thread.currentThread().getId() + (SwingUtilities.isEventDispatchThread()?"E":"") + "\tfinishUserCloseRequestProcessing leaving ...");
    }

    private void reloadData() {
      int simSetIndex = this.currentSimSetIndex;
      if(this.currentSimSetIndex<0 || this.simSearch.isSimSetIgnored(this.currentSimSetIndex)) simSetIndex = this.simSearch.getIndexOfNextNotIgnoredSimSet(this.currentSimSetIndex);
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
	    if(this.simSearch==null) this.simSetCountLabel.setText("");
	    else if(this.searchIsOn) {
			this.simSetCountLabel.setText(this.simSearch.getNotIgnoredSimSetIndex(currentSimSetIndex)+1 + " / ?(" + (this.simSearch.getNotIgnoredSimSetIndex(this.simSearch.getSimSetCount()-1)+1) + ")");
		} else {
			this.simSetCountLabel.setText(this.simSearch.getNotIgnoredSimSetIndex(currentSimSetIndex)+1 + " / " + (this.simSearch.getNotIgnoredSimSetIndex(this.simSearch.getSimSetCount()-1)+1));
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
			this.simSearchSettings = PlausibleDialog4Krise.editSettings(this, new SimSearch.Settings(), null);
			if(this.simSearchSettings==null) this.dispose();
			else this.startSearch(this.simSearchSettings);
		}
	}

	private void syncStopSearchAndWait() {
      //System.out.println(Thread.currentThread().getId() + "\tstopSearchAndWait entered");
      //System.out.println(Thread.currentThread().getId() + "\tSimSearch.startAsyncSearch.asyncThread before search");
      
      this.simSearch.stopSearch();
      while(this.searchIsOn) 
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      //System.out.println(Thread.currentThread().getId() + "\tstopSearchAndWait leaving");
    }
	
	private void processEditSettingsRequest() {
	  //System.out.println("processOpenSettingsRequest entered ...");
	  SimSearch.Settings newSettings = PlausibleDialog4Krise.editSettings(this, this.simSearchSettings, (h) -> prepareSettingsChange(h));
	  
	  if(newSettings!=null && !newSettings.equals(this.simSearchSettings)) {
	    // settings changed, start new search
	    this.restartSearch(newSettings);
	  }
	  //System.out.println("processOpenSettingsRequest leaving ...");
	}
	
	private void prepareSettingsChange(Consumer<Boolean> continueEditSettings) {
	  //System.out.println("prepareSettingsChange entered ...");
	  boolean saveChanges = false;
	  
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
      }
	  
	  if(this.searchIsOn) {
	    // sha
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
              //System.out.println("prepareSettingsChange.asynThread entered ...");
                boolean result = false;
                try {
                    syncStopSearchAndWait();
                                          
                    result = !finalSaveChanges || syncSave();
                    
                } catch (Exception err) {
                  SimSearchJFrame.this.showError(err, "Settings could not be changed."); 
             
                } finally {
                  continueEditSettings.accept(result);
                }
                //System.out.println("prepareSettingsChange.asynThread leaving");
            }
        };
    
        Thread thread = new Thread(runnable);
        thread.start();
        
        //System.out.println("prepareSettingsChange leaving");
	}

	private void processUserHelpRequest() {
		String help = "<html>\n";

		help += "This view shows the findings of the similarity search.\n";
		help += "The table lists either similar stations, products, lots or deliveries. One line of the result list shows the " + 
				SimSearchJTable.RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + " symbol which refers to the row which was used for the comparison and was found to be similar to all the other rows. ";
		help += "<br>\n";
		help += "Some of the columns may contain colored text or special symbols like " + 
				SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText( SimSearchJTable.AlignmentColumnRenderer.SYMBOL_GAP) + ", " +
				SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText(SimSearchJTable.AlignmentColumnRenderer.SYMBOL_SPACE_DELETE) + ", " + 
				SimSearchJTable.AlignmentColumnRenderer.getColoredNeutralGap() + ".";
		help += " The coloring reflects the difference of the text to the text in the comparison row (" +  
		                    SimSearchJTable.RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + ").";

		help += "<br><h2>Color coding</h2>\n";
		help += "The visualizations of the text comparisons use 3 colors.";
		help += " The color red indicates a difference from a text to the text in the comparison row. Black indicates no difference. And green indicates a gap in a text which is aligned to a character in some other text.";
		help += "The symbols " + SimSearchJTable.AlignmentColumnRenderer.getColoredNeutralGap() + " and " + 
				SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText( SimSearchJTable.AlignmentColumnRenderer.SYMBOL_GAP) + " represent a gap in a text.";

		help += "The symbol " + SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText(SimSearchJTable.AlignmentColumnRenderer.SYMBOL_SPACE_DELETE) +
				" represents a space character in a text which is aligned to a different character or a gap in the comparison text."; 

		help += " The coloring is described in the following example:<br>";
		Alignment.AlignedSequence[] alignedSeq = Alignment.alignSequences(new String[] {"Am Burggraben 128",  "Am Burg grabem 18", "Amm Bruggraben 128"},0);
		try {
			help += "<br><table cellpadding=0>" +
					"<tr><td>Text 1 in comparison row (" + SimSearchJTable.RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + "):&nbsp;</td><td>" + 
					SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[0], false) + "</td></tr>" + 
					"<tr><td>Text 2 in row x:</td><td>" + SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[1], false) + "</td></tr>" + 
					"<tr><td>Text 3 in row y:</td><td>" + SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[2], false) + "</td></tr>" + 
					"</table><br>" ;
		} catch (Exception e) {
			help += "<i>Example is missing.</i>";
		} 

		help += "This example shows an alignment of the 3 texts. Text 3 has a double &lsquo;mm&rsquo; which is different to the text in the comparison row. Text 2 has a space character between the double &lsquo;gg&rsquo; which is not matched by the comparison sequence.";

		help += "<h2>Merging rows</h2>\n";
		help += "To merge stations/products/lots/deliveries you need only to drag the representing row(s) onto the station/product/lot/delivery you want them to be merged into. It is like moving a file (or a selection of files) to a folder in a file browser.<br>" +
		"You can merge any row into any other row (which was not already merged). If e.g.  two rows are representing the same station/product/lot/delivery, you have to decide which row to merge onto which. The comparison row is not to be understood as a preference.<br>" +
		    "Please note, that a drop of a row/rows between other rows does not result in a merge, but in another row ordering.";

		help += "<h2>Ignore rows</h2>\n";
		help += "You can also mark similar rows as different to exclude them from future similarity searches. The button &lsquo;<strong>Comparison station/product/lot/deliveries is unique</strong>&rsquo; marks all rows (that are not already merged) as different to the comparison row.  The button &lsquo;<strong>All stations/products/lots/deliveries are unique</strong>&rsquo; marks all pairs of rows (that are not already merged) as different to each other.";
		help += "The result of this action might be that the view switches to the next finding. ";
		
		help += "<h2>View customization</h2>\n";
        help += "You can disable the showing of already merged rows, by checking the &lsquo;<strong>Hide merged rows</strong>&rsquo; entry in the &lsquo;<strong>View</strong>&rsquo; menu.";
		
		help += "</html>\n";

		InfoBox ib = new InfoBox(this, help, true, new Dimension(800, 400), null, true, true);
		ib.setVisible(true);
	}

}
