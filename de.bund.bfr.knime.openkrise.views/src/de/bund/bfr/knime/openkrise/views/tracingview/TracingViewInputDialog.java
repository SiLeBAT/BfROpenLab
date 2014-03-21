/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;

public class TracingViewInputDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JCheckBox skipEdgelessNodesBox;
	private JCheckBox joinEdgesBox;
	private JCheckBox exportAsSvgBox;
	private JButton okButton;
	private JButton cancelButton;

	private boolean approved;
	private TracingViewSettings set;

	public TracingViewInputDialog(JComponent owner, TracingViewSettings set) {
		super(SwingUtilities.getWindowAncestor(owner), "Input",
				DEFAULT_MODALITY_TYPE);
		this.set = set;
		approved = false;

		skipEdgelessNodesBox = new JCheckBox("Skip Nodes without Edges");
		skipEdgelessNodesBox.setSelected(set.isSkipEdgelessNodes());
		joinEdgesBox = new JCheckBox("Join Edges with same Source/Target");
		joinEdgesBox.setSelected(set.isJoinEdges());
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.setSelected(set.isExportAsSvg());
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(UI.createOptionsPanel("Node Table",
				Arrays.asList(skipEdgelessNodesBox),
				Arrays.asList(new JLabel())));
		mainPanel.add(UI.createOptionsPanel("Edge Table",
				Arrays.asList(joinEdgesBox), Arrays.asList(new JLabel())));
		mainPanel.add(UI.createOptionsPanel("Miscellaneous",
				Arrays.asList(exportAsSvgBox), Arrays.asList(new JLabel())));

		setLayout(new BorderLayout());
		add(UI.createNorthPanel(mainPanel), BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)),
				BorderLayout.SOUTH);
		setLocationRelativeTo(owner);
		pack();
	}

	public boolean isApproved() {
		return approved;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			approved = true;
			set.setSkipEdgelessNodes(skipEdgelessNodesBox.isSelected());
			set.setJoinEdges(joinEdgesBox.isSelected());
			set.setExportAsSvg(exportAsSvgBox.isSelected());
			dispose();
		} else if (e.getSource() == cancelButton) {
			dispose();
		}
	}

}
