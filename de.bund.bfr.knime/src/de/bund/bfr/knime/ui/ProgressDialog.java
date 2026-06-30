/*******************************************************************************
 * Copyright (c) 2014-2026 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class ProgressDialog implements IProgressMonitor {

	private static final long serialVersionUID = 2941381450336524968L;

	private Timer updateProgressTimer = null;
	private JDialog dialog;
    private JLabel messageLabel;
    private JProgressBar progressBar;
    
    private int progress_ = 0;
    private JButton cancelButton;
    private boolean isCanceled_ = false;
    private List<IProgressMonitor> subMonitors = new ArrayList<>();

    private ProgressDialog(JFrame owner, JDialog dialog, JLabel messageLabel, JProgressBar progressBar, JButton cancelButton) {
        this.dialog = dialog;
        this.messageLabel = messageLabel;
        this.progressBar = progressBar;
        this.cancelButton = cancelButton;

        cancelButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent ae)
			{
                cancelButton.setEnabled(false);
                setMessage("Canceling operation ...");
                isCanceled_ = true;
                cancelTimer();
            }
		});
        startTimer();
    }

    public boolean isCanceled() {
        return isCanceled_;
    }
    
    private void updateProgressUI() {
    	try {
			if (SwingUtilities.isEventDispatchThread()) {
				progressBar.setValue(progress_);
			} else {
			    SwingUtilities.invokeLater(() -> updateProgressUI());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void setProgress(int progress) {
    	progress_ = progress;
    }

    public void setMessage(String message) {
        try {
			if (SwingUtilities.isEventDispatchThread()) {
			    messageLabel.setText(message);
			} else {
			    SwingUtilities.invokeLater(() -> setMessage(message));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void cancelTimer() {
    	if (updateProgressTimer != null) {
    		updateProgressTimer.cancel();
    		updateProgressTimer = null;
    	}
    }
    
    private void startTimer() {
    	if (updateProgressTimer == null) {
    		updateProgressTimer = new Timer();
    		updateProgressTimer.schedule(new TimerTask() {
    		    @Override
    		    public void run() {
    		    	updateProgressUI();
    		    }
    		} , 0, 200);
    	}
    }

    public void close() {
    	cancelTimer();
        this.dialog.dispose();
        
    }

    public static ProgressDialog showDialog(JFrame owner, String title) { //, int min, int max) {

        JProgressBar progressBar = new JProgressBar(0, 100); //min, max);
        JLabel messageLabel = new JLabel("");
        JButton cancelButton = new JButton("Cancel");

        messageLabel.setHorizontalAlignment(JLabel.CENTER);

        JDialog window = new JDialog(owner);
        window.setTitle(title);
        window.setSize(300, 100);
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.setResizable(false);
        window.setLocationRelativeTo(owner);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel);
        panel.add(progressBar);
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(cancelButton);
        
		window.add(panel);

        ProgressDialog waitingDialog = new ProgressDialog(owner, window, messageLabel, progressBar, cancelButton);
        // we use a default message for packing
        messageLabel.setText("M".repeat(70));
    	window.pack();
    	messageLabel.setText("");
    	window.setLocationRelativeTo(owner);
        window.setModal(true);
    
        SwingUtilities.invokeLater(() -> {
        	window.setVisible(true);
        });
     
        return waitingDialog;
    }
}
