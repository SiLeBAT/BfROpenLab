/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.nls.creator;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.google.common.collect.Ordering;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.StringTextArea;
import de.bund.bfr.knime.ui.StringTextField;
import de.bund.bfr.math.MathUtils;

/**
 * <code>NodeDialog</code> for the "FunctionCreator" Node.
 * 
 * @author Christian Thoens
 */
public class FunctionCreatorNodeDialog extends NodeDialogPane {

	private FunctionCreatorSettings set;
	private List<String> usedIndeps;

	private JPanel mainPanel;
	private JPanel functionPanel;

	private StringTextField depVarField;
	private StringTextArea termField;

	/**
	 * New pane for configuring the FormulaCreator node.
	 */
	protected FunctionCreatorNodeDialog() {
		set = new FunctionCreatorSettings();
		usedIndeps = new ArrayList<>();

		functionPanel = createFunctionPanel();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(functionPanel, BorderLayout.NORTH);

		addTab("Options", UI.createWestPanel(mainPanel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		set.loadSettings(settings);
		mainPanel.remove(functionPanel);
		functionPanel = createFunctionPanel();
		mainPanel.add(functionPanel, BorderLayout.NORTH);
		mainPanel.revalidate();
		usedIndeps = set.getIndependentVariables();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		updateFunction();

		if (!depVarField.isValueValid()) {
			throw new InvalidSettingsException("Dependent Variable Missing");
		}

		if (!termField.isValueValid()) {
			throw new InvalidSettingsException("Formula Missing");
		}

		if (MathUtils.getSymbols(set.getTerm()).isEmpty()) {
			throw new InvalidSettingsException("Formula Invalid");
		}

		if (set.getIndependentVariables().isEmpty()) {
			throw new InvalidSettingsException("Independent Variables Missing");
		}

		set.saveSettings(settings);
	}

	private void termTextChanged() {
		set.setTerm(termField.getValue());
		mainPanel.remove(functionPanel);
		updateFunction();
		functionPanel = createFunctionPanel();
		mainPanel.add(functionPanel, BorderLayout.NORTH);
		mainPanel.revalidate();
		mainPanel.repaint();
		termField.requestFocus();
	}

	private JPanel createFunctionPanel() {
		JPanel editPanel = new JPanel();

		editPanel.setLayout(new GridBagLayout());
		editPanel.add(new JLabel("Term:"), UI.westConstraints(0, 0));
		editPanel.add(createFormulaPanel(), UI.westConstraints(1, 0));
		editPanel.add(new JLabel("Independent Variable:"), UI.westConstraints(0, 1));
		editPanel.add(createIndepBoxPanel(), UI.westConstraints(1, 1));

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder("Function"));
		panel.setLayout(new BorderLayout());
		panel.add(editPanel, BorderLayout.WEST);

		return editPanel;
	}

	private JPanel createFormulaPanel() {
		depVarField = new StringTextField(false, 10);
		depVarField.setValue(set.getDependentVariable());
		depVarField.addTextListener(e -> set.setDependentVariable(depVarField.getValue()));

		if (termField == null || !Objects.equals(termField.getValue(), set.getTerm())) {
			termField = new StringTextArea(false, 3, 100);
			termField.setValue(set.getTerm());
			termField.addTextListener(e -> termTextChanged());
		}

		JPanel formulaPanel = new JPanel();

		formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));
		formulaPanel.add(UI.createCenterPanel(depVarField));
		formulaPanel.add(new JLabel("="));
		formulaPanel.add(termField);

		return formulaPanel;
	}

	private JPanel createIndepBoxPanel() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		for (String el : Ordering.natural().sortedCopy(MathUtils.getSymbols(set.getTerm()))) {
			JCheckBox box = new JCheckBox(el);

			if (set.getIndependentVariables().contains(el)) {
				box.setSelected(true);
			} else {
				box.setSelected(false);
			}

			box.addItemListener(e -> indepVarChanged(box));
			panel.add(box);
		}

		return panel;
	}

	private void indepVarChanged(JCheckBox box) {
		if (box.isSelected()) {
			set.getIndependentVariables().add(box.getText());
			usedIndeps.add(box.getText());
		} else {
			set.getIndependentVariables().remove(box.getText());
			usedIndeps.remove(box.getText());
		}
	}

	private void updateFunction() {
		Set<String> indeps = new LinkedHashSet<>();

		for (String symbol : MathUtils.getSymbols(set.getTerm())) {
			if (set.getIndependentVariables().contains(symbol) || usedIndeps.contains(symbol)) {
				indeps.add(symbol);
			}
		}

		set.setIndependentVariables(Ordering.natural().sortedCopy(indeps));
	}
}
