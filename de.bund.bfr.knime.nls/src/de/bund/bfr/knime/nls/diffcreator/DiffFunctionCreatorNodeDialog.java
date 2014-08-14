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
package de.bund.bfr.knime.nls.diffcreator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.StringTextArea;
import de.bund.bfr.knime.ui.StringTextField;
import de.bund.bfr.knime.ui.TextListener;

/**
 * <code>NodeDialog</code> for the "DiffFunctionCreator" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class DiffFunctionCreatorNodeDialog extends NodeDialogPane implements
		ActionListener, TextListener, ItemListener {

	private DiffFunctionCreatorSettings set;
	private List<String> usedIndeps;

	private JPanel mainPanel;
	private JPanel functionPanel;

	private List<StringTextField> depVarFields;
	private List<StringTextArea> termFields;
	private List<DoubleTextField> initialFields;
	private List<JCheckBox> indepVarBoxes;
	private StringTextField diffVarField;

	private JButton addButton;
	private JButton removeButton;

	/**
	 * New pane for configuring the DiffFunctionCreator node.
	 */
	protected DiffFunctionCreatorNodeDialog() {
		set = new DiffFunctionCreatorSettings();
		usedIndeps = new ArrayList<>();

		depVarFields = new ArrayList<>();
		termFields = new ArrayList<>();
		initialFields = new ArrayList<>();
		indepVarBoxes = new ArrayList<>();

		addButton = new JButton("Add Equation");
		addButton.addActionListener(this);
		removeButton = new JButton("Remove Equation");
		removeButton.addActionListener(this);

		functionPanel = createFunctionPanel(1);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(functionPanel, BorderLayout.NORTH);
		mainPanel.add(UI.createWestPanel(UI.createHorizontalPanel(addButton,
				removeButton)), BorderLayout.SOUTH);

		addTab("Options", mainPanel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		set.loadSettings(settings);
		mainPanel.remove(functionPanel);
		functionPanel = createFunctionPanel(Math.max(set.getTerms().size(), 1));
		mainPanel.add(functionPanel, BorderLayout.NORTH);
		mainPanel.revalidate();
		usedIndeps = set.getIndependentVariables();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		updateFunction();
		
		for (StringTextField depVarField : depVarFields) {
			if (!depVarField.isValueValid()) {
				throw new InvalidSettingsException("Dependent Variable Missing");
			}
		}

		for (StringTextArea termField : termFields) {
			if (!termField.isValueValid()) {
				throw new InvalidSettingsException("Formula Missing");
			}
		}

		if (!diffVarField.isValueValid()) {
			throw new InvalidSettingsException("Diff Variable Missing");
		}

		if (DiffFunctionCreatorNodeModel.getAllSymbols(set.getTerms())
				.isEmpty()) {
			throw new InvalidSettingsException("Formula Invalid");
		}

		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addButton) {
			mainPanel.remove(functionPanel);
			functionPanel = createFunctionPanel(set.getTerms().size() + 1);
			mainPanel.add(functionPanel, BorderLayout.NORTH);
			mainPanel.revalidate();
		} else if (e.getSource() == removeButton) {
			mainPanel.remove(functionPanel);
			functionPanel = createFunctionPanel(set.getTerms().size() - 1);
			mainPanel.add(functionPanel, BorderLayout.NORTH);
			mainPanel.revalidate();
		}
	}

	@Override
	public void textChanged(Object source) {
		if (depVarFields.contains(source)) {
			StringTextField depVarField = (StringTextField) source;

			set.getDependentVariables().set(depVarFields.indexOf(source),
					depVarField.getValue());
			mainPanel.remove(functionPanel);
			updateFunction();
			functionPanel = createFunctionPanel(set.getTerms().size());
			mainPanel.add(functionPanel, BorderLayout.NORTH);
			mainPanel.revalidate();
			depVarField.requestFocus();
		} else if (termFields.contains(source)) {
			StringTextArea termField = (StringTextArea) source;

			set.getTerms().set(termFields.indexOf(termField),
					termField.getText());
			mainPanel.remove(functionPanel);
			updateFunction();
			functionPanel = createFunctionPanel(set.getTerms().size());
			mainPanel.add(functionPanel, BorderLayout.NORTH);
			mainPanel.revalidate();
			termField.requestFocus();
		} else if (initialFields.contains(source)) {
			set.getInitialValues().set(initialFields.indexOf(source),
					((DoubleTextField) source).getValue());
		} else if (source == diffVarField && diffVarField.isValueValid()) {
			set.setDiffVariable(diffVarField.getText().trim());
			mainPanel.remove(functionPanel);
			updateFunction();
			functionPanel = createFunctionPanel(set.getTerms().size());
			mainPanel.add(functionPanel, BorderLayout.NORTH);
			mainPanel.revalidate();
			diffVarField.requestFocus();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (indepVarBoxes.contains(e.getSource())) {
			String name = ((JCheckBox) e.getSource()).getText();

			if (e.getStateChange() == ItemEvent.SELECTED) {
				set.getIndependentVariables().add(name);
				usedIndeps.add(name);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				set.getIndependentVariables().remove(name);
				usedIndeps.remove(name);
			}			
		}
	}

	private JPanel createFunctionPanel(int n) {
		updateNumberOfFunctions(n);

		JPanel editPanel = new JPanel();

		editPanel.setLayout(new GridBagLayout());

		for (int i = 0; i < n; i++) {
			editPanel.add(new JLabel("Equation " + (i + 1) + ":"),
					createConstraints(0, i));
			editPanel.add(createFormulaPanel(i), createConstraints(1, i));
		}

		editPanel.add(new JLabel("Diff Variable:"), createConstraints(0, n));
		editPanel.add(createDiffVarPanel(), createConstraints(1, n));
		editPanel.add(new JLabel("Independent Variables:"),
				createConstraints(0, n + 1));
		editPanel.add(createIndepBoxPanel(), createConstraints(1, n + 1));

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder("Function"));
		panel.setLayout(new BorderLayout());
		panel.add(editPanel, BorderLayout.WEST);

		return editPanel;
	}

	private JPanel createFormulaPanel(int i) {
		StringTextField depVarField = new StringTextField(false, 5);

		depVarField.setValue(set.getDependentVariables().get(i));
		depVarField.addTextListener(this);

		JPanel depVarPanel = new JPanel();
		String diffVar = set.getDiffVariable() != null ? set.getDiffVariable()
				: "?";
		JLabel openLabel = new JLabel("(");
		JLabel closeLabel = new JLabel(")");

		UI.setFontSize(openLabel, depVarField.getPreferredSize().height);
		UI.setFontSize(closeLabel, depVarField.getPreferredSize().height);
		depVarPanel.setLayout(new BoxLayout(depVarPanel, BoxLayout.X_AXIS));
		depVarPanel.add(new JLabel("d/d" + diffVar));
		depVarPanel.add(openLabel);
		depVarPanel.add(depVarField);
		depVarPanel.add(closeLabel);

		StringTextArea termField = termFields.get(i);

		if (termField == null || termField.getValue() == null
				|| !termField.getValue().equals(set.getTerms().get(i))) {
			termField = new StringTextArea(false, 1, 100);
			termField.setValue(set.getTerms().get(i));
			termField.addTextListener(this);
		}

		JPanel formulaPanel = new JPanel();

		formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));
		formulaPanel.add(UI.createCenterPanel(depVarPanel));
		formulaPanel.add(new JLabel("="));
		formulaPanel.add(termField);

		depVarFields.set(i, depVarField);
		termFields.set(i, termField);

		if (i != 0) {
			DoubleTextField initialField = new DoubleTextField(false, 5);

			initialField.setValue(set.getInitialValues().get(i));
			initialField.addTextListener(this);

			JPanel initialPanel = new JPanel();
			JLabel initialLabel = new JLabel("Initial Value");

			initialLabel.setPreferredSize(new Dimension(depVarPanel
					.getPreferredSize().width,
					initialLabel.getPreferredSize().height));
			initialPanel
					.setLayout(new BoxLayout(initialPanel, BoxLayout.X_AXIS));
			initialPanel.add(initialLabel);
			initialPanel.add(new JLabel("="));
			initialPanel.add(initialField);

			initialFields.set(i, initialField);

			return UI.createVerticalPanel(formulaPanel, initialPanel);
		} else {
			return formulaPanel;
		}
	}

	private JPanel createIndepBoxPanel() {
		List<String> elements = new ArrayList<>(
				DiffFunctionCreatorNodeModel.getAllSymbols(set.getTerms()));

		elements.removeAll(set.getDependentVariables());
		elements.remove(set.getDiffVariable());
		Collections.sort(elements);

		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		indepVarBoxes.clear();

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
		diffVarField = new StringTextField(false, 5);
		diffVarField.setValue(set.getDiffVariable());
		diffVarField.addTextListener(this);

		return UI.createWestPanel(diffVarField);
	}

	private void updateFunction() {
		List<String> symbols = new ArrayList<>(
				DiffFunctionCreatorNodeModel.getAllSymbols(set.getTerms()));

		symbols.removeAll(set.getDependentVariables());
		symbols.remove(set.getDiffVariable());

		List<String> indeps = new ArrayList<>();

		for (String symbol : symbols) {
			if (set.getIndependentVariables().contains(symbol)
					|| usedIndeps.contains(symbol)) {
				indeps.add(symbol);
			}
		}

		Collections.sort(indeps);
		set.setIndependentVariables(indeps);
	}

	private void updateNumberOfFunctions(int n) {
		setListSize(depVarFields, n);
		setListSize(termFields, n);
		setListSize(initialFields, n);
		setListSize(set.getDependentVariables(), n);
		setListSize(set.getTerms(), n);
		setListSize(set.getInitialValues(), n);

		removeButton.setEnabled(n > 1);
	}

	private static GridBagConstraints createConstraints(int x, int y) {
		return new GridBagConstraints(x, y, 1, 1, 0, 0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0);
	}

	private static void setListSize(List<?> list, int n) {
		while (list.size() > n) {
			list.remove(list.size() - 1);
		}

		while (list.size() < n) {
			list.add(null);
		}
	}
}
