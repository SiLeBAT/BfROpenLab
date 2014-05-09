/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
package de.bund.bfr.knime.nls.functioncreator;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.StringTextArea;
import de.bund.bfr.knime.ui.StringTextField;
import de.bund.bfr.knime.ui.TextListener;
import de.bund.bfr.math.MathUtilities;

/**
 * <code>NodeDialog</code> for the "FunctionCreator" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class FunctionCreatorNodeDialog extends NodeDialogPane implements
		TextListener, ItemListener {

	private FunctionCreatorSettings set;
	private List<String> usedIndeps;

	private JPanel mainPanel;
	private JPanel functionPanel;

	private StringTextField depVarField;
	private StringTextArea termField;
	private List<JCheckBox> indepVarBoxes;
	private JComboBox<String> diffVarBox;

	/**
	 * New pane for configuring the FormulaCreator node.
	 */
	protected FunctionCreatorNodeDialog() {
		set = new FunctionCreatorSettings();
		usedIndeps = new ArrayList<String>();

		functionPanel = createFunctionPanel();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(functionPanel, BorderLayout.NORTH);

		addTab("Options", UI.createWestPanel(mainPanel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		set.loadSettings(settings);
		mainPanel.remove(functionPanel);
		functionPanel = createFunctionPanel();
		mainPanel.add(functionPanel, BorderLayout.NORTH);
		mainPanel.revalidate();
		usedIndeps = set.getIndependentVariables();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (set.getTerm() == null) {
			throw new InvalidSettingsException("Formula Missing");
		}

		if (MathUtilities.getSymbols(set.getTerm()).isEmpty()) {
			throw new InvalidSettingsException("Formula Invalid");
		}

		if (set.getDependentVariable() == null) {
			throw new InvalidSettingsException("Dependent Variable Missing");
		}

		if (set.getIndependentVariables().isEmpty()) {
			throw new InvalidSettingsException("Independent Variables Missing");
		}

		set.saveSettings(settings);
	}

	@Override
	public void textChanged(Object source) {
		if (source == depVarField) {
			set.setDependentVariable(depVarField.getValue());
		} else if (source == termField) {
			set.setTerm(termField.getValue());
			mainPanel.remove(functionPanel);
			updateFunction();
			functionPanel = createFunctionPanel();
			mainPanel.add(functionPanel);
			mainPanel.revalidate();
			termField.requestFocus();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (indepVarBoxes != null && indepVarBoxes.contains(e.getSource())) {
			String name = ((JCheckBox) e.getSource()).getText();

			if (e.getStateChange() == ItemEvent.SELECTED) {
				set.getIndependentVariables().add(name);
				usedIndeps.add(name);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				set.getIndependentVariables().remove(name);
				usedIndeps.remove(name);
			}

			updateFunction();
			mainPanel.remove(functionPanel);
			functionPanel = createFunctionPanel();
			mainPanel.add(functionPanel);
			mainPanel.revalidate();
		} else if (e.getSource() == diffVarBox) {
			set.setDiffVariable((String) diffVarBox.getSelectedItem());
		}
	}

	private JPanel createFunctionPanel() {
		JPanel editPanel = new JPanel();

		editPanel.setLayout(new GridBagLayout());
		editPanel.add(new JLabel("Term:"), createConstraints(0, 0));
		editPanel.add(createFormulaPanel(), createConstraints(1, 0));
		editPanel.add(new JLabel("Independent Variable:"),
				createConstraints(0, 1));
		editPanel.add(createIndepBoxPanel(), createConstraints(1, 1));
		editPanel.add(new JLabel("Diff Variable:"), createConstraints(0, 2));
		editPanel.add(createDiffVarPanel(), createConstraints(1, 2));

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder("Function"));
		panel.setLayout(new BorderLayout());
		panel.add(editPanel, BorderLayout.WEST);

		return editPanel;
	}

	private JPanel createFormulaPanel() {
		depVarField = new StringTextField(false, 10);
		depVarField.setValue(set.getDependentVariable());
		depVarField.addTextListener(this);

		if (termField == null || termField.getValue() == null
				|| !termField.getValue().equals(set.getTerm())) {
			termField = new StringTextArea(false, 3, 100);
			termField.setValue(set.getTerm());
			termField.addTextListener(this);
		}

		JPanel formulaPanel = new JPanel();

		formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));
		formulaPanel.add(UI.createCenterPanel(depVarField));
		formulaPanel.add(new JLabel("="));
		formulaPanel.add(termField);

		return formulaPanel;
	}

	private JPanel createIndepBoxPanel() {
		List<String> elements = MathUtilities.getSymbols(set.getTerm());

		Collections.sort(elements);

		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		indepVarBoxes = new ArrayList<JCheckBox>();

		for (String el : elements) {
			JCheckBox box = new JCheckBox(el);

			if (set.getIndependentVariables().contains(el)) {
				box.setSelected(true);
			} else {
				box.setSelected(false);
			}

			box.addItemListener(this);
			panel.add(box);
			indepVarBoxes.add(box);
		}

		return panel;
	}

	private JPanel createDiffVarPanel() {
		diffVarBox = new JComboBox<String>(set.getIndependentVariables()
				.toArray(new String[0]));
		diffVarBox.insertItemAt(null, 0);
		diffVarBox.setSelectedItem(set.getDiffVariable());
		diffVarBox.addItemListener(this);

		return UI.createWestPanel(diffVarBox);
	}

	private void updateFunction() {
		List<String> params = MathUtilities.getSymbols(set.getTerm());
		List<String> indeps = new ArrayList<String>();

		for (String indep : set.getIndependentVariables()) {
			if (params.contains(indep)) {
				indeps.add(indep);
				params.remove(indep);
			}
		}

		for (int i = 0; i < params.size(); i++) {
			if (usedIndeps.contains(params.get(i))) {
				indeps.add(params.get(i));
				params.remove(i);
				i--;
			}
		}

		Collections.sort(indeps);
		Collections.sort(params);
		set.setIndependentVariables(indeps);
	}

	private static GridBagConstraints createConstraints(int x, int y) {
		return new GridBagConstraints(x, y, 1, 1, 0, 0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0);
	}
}
