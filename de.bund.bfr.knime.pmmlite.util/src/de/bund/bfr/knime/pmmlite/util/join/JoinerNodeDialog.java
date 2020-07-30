/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.util.join;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.knime.pmmlite.util.join.JoinerSettings.JoinType;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * <code>NodeDialog</code> for the "PmmJoiner" Node.
 * 
 * @author Christian Thoens
 */
public class JoinerNodeDialog extends DataAwareNodeDialogPane implements ActionListener {

	private JComboBox<JoinType> joinerBox;
	private JPanel joinerPanel;

	private Joiner joiner;

	private PmmPortObject input1;
	private PmmPortObject input2;

	private JoinerSettings set;

	/**
	 * New pane for configuring the PmmJoiner node.
	 */
	protected JoinerNodeDialog() {
		set = new JoinerSettings();

		joinerBox = new JComboBox<>(JoinType.values());
		joinerBox.addActionListener(this);
		joinerPanel = new JPanel();
		joinerPanel.setBorder(BorderFactory.createTitledBorder("Join Options"));
		joinerPanel.setLayout(new BorderLayout());

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createBorderPanel(joinerBox)), BorderLayout.NORTH);
		panel.add(joinerPanel, BorderLayout.CENTER);
		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		set.loadSettings(settings);
		input1 = (PmmPortObject) input[0];
		input2 = (PmmPortObject) input[1];

		if (set.getJoinType() == null) {
			PmmPortObjectSpec spec1 = input1.getSpec();
			PmmPortObjectSpec spec2 = input2.getSpec();

			if (PrimaryJoiner.isValidInput(spec1, spec2)) {
				set.setJoinType(JoinType.PRIMARY_JOIN);
			} else if (SecondaryJoiner.isValidInput(spec1, spec2)) {
				set.setJoinType(JoinType.SECONDARY_JOIN);
			} else if (TertiaryJoiner.isValidInput(spec1, spec2)) {
				set.setJoinType(JoinType.TERTIARY_JOIN);
			} else if (FormulaJoiner.isValidInput(spec1, spec2)) {
				set.setJoinType(JoinType.FORMULA_JOIN);
			} else {
				set.setJoinType(JoinType.PRIMARY_JOIN);
			}
		}

		initGUI();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (joiner == null) {
			throw new InvalidSettingsException("Invalid Join Type");
		} else if (joiner.getErrorMessage() != null) {
			throw new InvalidSettingsException(joiner.getErrorMessage());
		}

		set.setAssignments(joiner.getAssignments());
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		set.setJoinType((JoinType) joinerBox.getSelectedItem());
		initGUI();
	}

	private void initGUI() {
		String error = "";

		joinerBox.removeActionListener(this);
		joinerBox.setSelectedItem(set.getJoinType());
		joinerBox.addActionListener(this);
		joiner = null;

		PmmPortObjectSpec spec1 = input1.getSpec();
		PmmPortObjectSpec spec2 = input2.getSpec();

		try {
			switch (set.getJoinType()) {
			case PRIMARY_JOIN:
				PrimaryJoiner.getOutputType(spec1, spec2);
				joiner = new PrimaryJoiner(input1, input2);
				break;
			case SECONDARY_JOIN:
				SecondaryJoiner.getOutputType(spec1, spec2);
				joiner = new SecondaryJoiner(input1, input2);
				break;
			case TERTIARY_JOIN:
				TertiaryJoiner.getOutputType(spec1, spec2);
				joiner = new TertiaryJoiner(input1, input2);
				break;
			case FORMULA_JOIN:
				FormulaJoiner.getOutputType(spec1, spec2);
				joiner = new FormulaJoiner(input1, input2);
				break;
			default:
				throw new RuntimeException("Unknown join type: " + set.getJoinType());
			}
		} catch (InvalidSettingsException e) {
			error = e.getMessage();
		}

		joinerPanel.removeAll();

		if (joiner != null) {
			joinerPanel.add(joiner.createPanel(set.getAssignments()), BorderLayout.CENTER);
			joinerPanel.revalidate();
		} else {
			if (joinerBox.isValid()) {
				Dialogs.showErrorMessage(joinerBox, "Data is not valid for " + set.getJoinType());
			}

			joinerPanel.add(new JLabel(error), BorderLayout.CENTER);
			joinerPanel.revalidate();
		}
	}
}
