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
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

	private JCheckBoxMenuItem hideInactiveRowsMenuItem; 
	private JMenu rowHeightMenu;

	private SimSearch simSearch;
	private SimSearch.Settings simSearchSettings;

	private int currentSimSetIndex;
	private boolean userIsWaiting;
	private boolean searchIsOn;
	private boolean closeWindow;

	public SimSearchJFrame(Frame owner) {
		super(owner);
		this.setModal(true);
		this.initComponents();
		this.userIsWaiting = false;
	}

//	public SimSearchJFrame() {
//		this(null);
////		this.initComponents();
////		this.userIsWaiting = false;
//	}

	// GUI setup start

	private void initComponents() {
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				SimSearchJFrame.this.processWindowCloseRequest();
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
		this.hideInactiveRowsMenuItem =  new JCheckBoxMenuItem("Hide merged rows");
		this.hideInactiveRowsMenuItem.setSelected(false);

		menu.add(this.hideInactiveRowsMenuItem);
		
		this.table.addMenuItems(menu);

		bar.add(menu);

		menu = new JMenu("Settings");
		JMenuItem menuItem = new JMenuItem("Show search settings ..");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SimSearchJFrame.this.processOpenSettingsRequest();
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
	
	public void startSearch(SimSearch.Settings settings) {
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
			if(this.closeWindow) this.dispose();
			else {
				this.setUserIsWaiting(false);
				this.searchIsOn = false;
				this.updateSimSetCountLabel();
				this.navToLast.setEnabled((this.simSearch.getSimSetCount()>Math.max(0,this.currentSimSetIndex+1)));
				if(this.simSearch.getSimSetCount()==0 && searchCompleted) JOptionPane.showMessageDialog(null, "No similarities found.", "Similarity search result", 1);
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

	private void processUserSaveRequest() {
		if(this.simSearch!=null) startAsyncSave(false);
	}

	private void processUserApplyRequest() {
		if(this.simSearch!=null) this.startAsyncSave(true);
	}

	private void startAsyncSave(boolean applyOnly) {
		if(this.simSearch.existDataManipulations() && searchIsOn) {
			JOptionPane.showMessageDialog(this, "Data cannot saved before the search is finished.",null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}

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
			this.redoButton.setToolTipText(this.simSearch.getRedoType());

			this.undoButton.setEnabled(this.simSearch.isUndoAvailable());
			this.undoButton.setToolTipText(this.simSearch.getUndoType());

			this.applyButton.setEnabled(this.simSearch.existDataManipulations());


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
		Arrays.asList(this.navBack, this.navForward, this.navToFirst, this.navToLast, this.ignoreSimSetButton, this.ignoreAllPairsInSimSetButton).forEach(b -> b.setEnabled(false));
		this.currentSimSetIndex = -1;
		this.rowHeightMenu.setEnabled(false);
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
		if(this.searchIsOn) {
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
			this.simSearchSettings = PlausibleDialog4Krise.showSettings(this);
			if(this.simSearchSettings==null) this.dispose();
			else this.startSearch(this.simSearchSettings);
		}
	}

	private void processWindowCloseRequest() {
		if(this.simSearch!=null && this.simSearch.existDataManipulations()) {
			switch(JOptionPane.showConfirmDialog(this, "Your changes have not been saved yet. Save?", null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
			case JOptionPane.YES_OPTION:
				this.processUserSaveRequest();
				return;
			case JOptionPane.CANCEL_OPTION:
				return;
			case JOptionPane.NO_OPTION:
				this.closeWindow = true;
				if(this.searchIsOn) {
					this.simSearch.stopSearch();
					return;
				}
				// just continue closing the window
			}
		}
		this.dispose();
	}


	private void processOpenSettingsRequest() {
		PlausibleDialog4Krise.showSettings(this, this.simSearchSettings);
	}

	private void processUserHelpRequest() {
		String help = "<html>\n";

		help += "This view shows the findings of the similarity search.\n";
		help += "The table lists similar stations, products, lots or deliveries. One line of the result list shows the " + 
				SimSearchJTable.RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + " symbol which refers to the row which was found to be similar to all the other rows. ";
		help += "<br>\n";
		help += "Some of the columns may contain colored text or special symbols like " + 
				SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText( SimSearchJTable.AlignmentColumnRenderer.SYMBOL_GAP) + ", " +
				SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText(SimSearchJTable.AlignmentColumnRenderer.SYMBOL_SPACE_DELETE) + ", " + 
				SimSearchJTable.AlignmentColumnRenderer.getColoredNeutralGap() + ".";
		help += " The coloring reflects the difference of the text to the text in the reference row.";

		help += "<br><h2>Color coding</h2>\n";
		help += "The visualizations of the text comparisons use 3 colors.";
		help += " The color red indicates a difference from a text to the text in the reference row. Black indicates no difference. And green indicates a gap in a text which is aligned to a character in some other text.";
		help += "The symbols " + SimSearchJTable.AlignmentColumnRenderer.getColoredNeutralGap() + " and " + 
				SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText( SimSearchJTable.AlignmentColumnRenderer.SYMBOL_GAP) + " represent a gap in a text.";

		help += "The symbol " + SimSearchJTable.AlignmentColumnRenderer.getColoredMismatchText(SimSearchJTable.AlignmentColumnRenderer.SYMBOL_SPACE_DELETE) +
				" represents a space character in a text which is aligned to a different character or a gap in the reference text."; 

		help += " The coloring is described in the following example:<br>";
		Alignment.AlignedSequence[] alignedSeq = Alignment.alignSequences(new String[] {"Am Burggraben 128",  "Am Burg grabem 18", "Amm Bruggraben 128"},0);
		try {
			help += "<br><table cellpadding=0>" +
					"<tr><td>Text 1 in reference row (" + SimSearchJTable.RowHeaderColumnRenderer.HTML_SYMBOL_SIM_REFERENCE + "):&nbsp;</td><td>" + 
					SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[0], false) + "</td></tr>" + 
					"<tr><td>Text 2 in row x:</td><td>" + SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[1], false) + "</td></tr>" + 
					"<tr><td>Text 3 in row y:</td><td>" + SimSearchJTable.AlignmentColumnRenderer.createHtmlCode(alignedSeq[2], false) + "</td></tr>" + 
					"</table><br>" ;
		} catch (Exception e) {
			help += "<i>Example is missing.</i>";
		} 

		help += "This example shows an alignment of the 3 texts. Text 3 has a double &lsquo;mm&rsquo; which is different to the reference text. Text 2 has a space character between the double &lsquo;gg&rsquo; which is not matched by the reference sequence.";

		help += "<h2>Merging rows</h2>\n";
		help += "To merge rows you need only to first select the row(s) you want to merge into another row. Then you drag the selected rows onto the row you want the selected rows to be merged into.";

		help += "<h2>Ignore rows</h2>\n";
		help += "If you can also mark similar rows as different to exclude them from future similarity searches. The button &lsquo;<strong>Ignore similarities</strong>&rsquo; marks all rows (that are not already merged) as different to the reference row.  The button &lsquo;<strong>Ignore all pairs</strong>&rsquo; marks all pairs of rows (that are not already merged) as different to each other.";
		help += "The result of this action might be that the view switches to the next finding. ";

		help += "</html>\n";

		InfoBox ib = new InfoBox(this, help, true, new Dimension(800, 400), null, true, true);
		ib.setVisible(true);
	}

}
