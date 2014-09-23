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
package de.bund.bfr.knime.nls.fitting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.nls.functionport.FunctionPortObjectSpec;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.IntTextField;
import de.bund.bfr.math.Integrator;

/**
 * <code>NodeDialog</code> for the "DiffFunctionFitting" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class FittingNodeDialog extends NodeDialogPane implements
		ActionListener, ItemListener {

	private boolean isDiff;
	private FittingSettings set;

	private JPanel mainPanel;
	private JPanel expertPanel;

	private JCheckBox expertBox;

	private IntTextField nParamSpaceField;
	private IntTextField nLevenbergField;
	private JCheckBox stopWhenSuccessBox;
	private JButton clearButton;
	private JCheckBox limitsBox;
	private Map<String, DoubleTextField> minimumFields;
	private Map<String, DoubleTextField> maximumFields;

	private JComboBox<Integrator.Type> typeBox;
	private DoubleTextField stepSizeField;
	private DoubleTextField minStepSizeField;
	private DoubleTextField maxStepSizeField;
	private DoubleTextField absToleranceField;
	private DoubleTextField relToleranceField;

	/**
	 * New pane for configuring the DiffFunctionFitting node.
	 */
	protected FittingNodeDialog(boolean isDiff) {
		this.isDiff = isDiff;
		set = new FittingSettings();
		expertBox = new JCheckBox("Expert Settings");
		expertBox.addActionListener(this);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(UI.createEmptyBorderPanel(expertBox), BorderLayout.NORTH);
		addTab("Options", UI.createNorthPanel(mainPanel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		set.loadSettings(settings);

		expertBox.removeActionListener(this);
		expertBox.setSelected(set.isExpertSettings());
		expertBox.addActionListener(this);

		updateExpertPanel((FunctionPortObjectSpec) specs[0]);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (!nParamSpaceField.isValueValid() || !nLevenbergField.isValueValid()
				|| minimumFields == null || maximumFields == null) {
			throw new InvalidSettingsException("");
		}

		Map<String, Double> minStartValues = new LinkedHashMap<>();
		Map<String, Double> maxStartValues = new LinkedHashMap<>();

		for (String param : minimumFields.keySet()) {
			minStartValues.put(param, minimumFields.get(param).getValue());
			maxStartValues.put(param, maximumFields.get(param).getValue());
		}

		set.setnParameterSpace(nParamSpaceField.getValue());
		set.setnLevenberg(nLevenbergField.getValue());
		set.setEnforceLimits(limitsBox.isSelected());
		set.setExpertSettings(expertBox.isSelected());
		set.setStopWhenSuccessful(stopWhenSuccessBox.isSelected());
		set.setMinStartValues(minStartValues);
		set.setMaxStartValues(maxStartValues);

		if (isDiff) {
			set.setIntegratorType((Integrator.Type) typeBox.getSelectedItem());
			set.setStepSize(stepSizeField.getValue());
			set.setMinStepSize(minStepSizeField.getValue());
			set.setMaxStepSize(maxStepSizeField.getValue());
			set.setAbsTolerance(absToleranceField.getValue());
			set.setRelTolerance(relToleranceField.getValue());
		}

		set.saveSettings(settings);
	}

	private void updateExpertPanel(FunctionPortObjectSpec spec) {
		if (expertPanel != null) {
			mainPanel.remove(expertPanel);
		}

		expertPanel = new JPanel();
		expertPanel.setLayout(new BoxLayout(expertPanel, BoxLayout.Y_AXIS));
		expertPanel.add(createRegressionPanel());
		expertPanel.add(createRangePanel(spec));

		if (isDiff) {
			expertPanel.add(createIntegrationPanel());
		}

		mainPanel.add(expertPanel, BorderLayout.CENTER);
		mainPanel.revalidate();
		mainPanel.repaint();

		Dimension preferredSize = mainPanel.getPreferredSize();

		if (expertBox.isSelected()) {
			expertPanel.setVisible(true);
		} else {
			expertPanel.setVisible(false);
		}

		mainPanel.setPreferredSize(preferredSize);
	}

	private JComponent createRegressionPanel() {
		nParamSpaceField = new IntTextField(false, 8);
		nParamSpaceField.setMinValue(0);
		nParamSpaceField.setMaxValue(1000000);
		nParamSpaceField.setValue(set.getnParameterSpace());
		nLevenbergField = new IntTextField(false, 8);
		nLevenbergField.setMinValue(0);
		nLevenbergField.setMaxValue(100);
		nLevenbergField.setValue(set.getnLevenberg());
		stopWhenSuccessBox = new JCheckBox("Stop When Regression Successful");
		stopWhenSuccessBox.setSelected(set.isStopWhenSuccessful());

		JPanel leftRegressionPanel = new JPanel();

		leftRegressionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
				5));
		leftRegressionPanel.setLayout(new GridLayout(3, 1, 5, 5));
		leftRegressionPanel.add(new JLabel(
				"Maximal Evaluations to Find Start Values"));
		leftRegressionPanel.add(new JLabel(
				"Maximal Executions of the Levenberg Algorithm"));
		leftRegressionPanel.add(stopWhenSuccessBox);

		JPanel rightRegressionPanel = new JPanel();

		rightRegressionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
				5));
		rightRegressionPanel.setLayout(new GridLayout(3, 1, 5, 5));
		rightRegressionPanel.add(nParamSpaceField);
		rightRegressionPanel.add(nLevenbergField);
		rightRegressionPanel.add(new JLabel());

		JPanel regressionPanel = new JPanel();

		regressionPanel.setBorder(new TitledBorder(
				"Nonlinear Regression Parameters"));
		regressionPanel.setLayout(new BorderLayout());
		regressionPanel.add(leftRegressionPanel, BorderLayout.WEST);
		regressionPanel.add(rightRegressionPanel, BorderLayout.EAST);

		return regressionPanel;
	}

	private JComponent createRangePanel(FunctionPortObjectSpec spec) {
		limitsBox = new JCheckBox("Enforce start values as limits");
		limitsBox.setSelected(set.isEnforceLimits());
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		minimumFields = new LinkedHashMap<>();
		maximumFields = new LinkedHashMap<>();

		JPanel northRangePanel = new JPanel();

		northRangePanel.setLayout(new BoxLayout(northRangePanel,
				BoxLayout.Y_AXIS));

		JPanel modelPanel = new JPanel();
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		List<String> params = spec.getFunction().getParameters();

		leftPanel.setLayout(new GridLayout(params.size(), 1));
		rightPanel.setLayout(new GridLayout(params.size(), 1));

		for (String param : params) {
			DoubleTextField minField = new DoubleTextField(true, 8);
			DoubleTextField maxField = new DoubleTextField(true, 8);

			if (set.getMinStartValues().get(param) != null) {
				minField.setValue(set.getMinStartValues().get(param));
			}

			if (set.getMaxStartValues().get(param) != null) {
				maxField.setValue(set.getMaxStartValues().get(param));
			}

			JPanel minMaxPanel = new JPanel();

			minMaxPanel.setLayout(new BoxLayout(minMaxPanel, BoxLayout.X_AXIS));
			minMaxPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			minMaxPanel.add(minField);
			minMaxPanel.add(Box.createHorizontalStrut(5));
			minMaxPanel.add(new JLabel("to"));
			minMaxPanel.add(Box.createHorizontalStrut(5));
			minMaxPanel.add(maxField);

			minimumFields.put(param, minField);
			maximumFields.put(param, maxField);
			leftPanel.add(UI.createEmptyBorderPanel(new JLabel(param)));
			rightPanel.add(minMaxPanel);
		}

		modelPanel.setLayout(new BorderLayout());
		modelPanel.add(leftPanel, BorderLayout.WEST);
		modelPanel.add(rightPanel, BorderLayout.EAST);

		JPanel rangePanel = new JPanel();

		rangePanel.setLayout(new BorderLayout());
		rangePanel.add(modelPanel, BorderLayout.NORTH);

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory
				.createTitledBorder("Specific Start Values for Fitting Procedure - Optional"));
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(clearButton,
				limitsBox)), BorderLayout.NORTH);
		panel.add(new JScrollPane(rangePanel), BorderLayout.CENTER);

		return panel;
	}

	private JComponent createIntegrationPanel() {
		typeBox = new JComboBox<>(Integrator.Type.values());
		typeBox.setSelectedItem(set.getIntegratorType());
		typeBox.addItemListener(this);
		stepSizeField = new DoubleTextField(false, 8);
		stepSizeField.setValue(set.getStepSize());
		minStepSizeField = new DoubleTextField(false, 8);
		minStepSizeField.setValue(set.getMinStepSize());
		maxStepSizeField = new DoubleTextField(false, 8);
		maxStepSizeField.setValue(set.getMaxStepSize());
		absToleranceField = new DoubleTextField(false, 8);
		absToleranceField.setValue(set.getAbsTolerance());
		relToleranceField = new DoubleTextField(false, 8);
		relToleranceField.setValue(set.getRelTolerance());
		updateIntegrationPanel();

		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();

		leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		leftPanel.setLayout(new GridLayout(0, 1, 5, 5));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightPanel.setLayout(new GridLayout(0, 1, 5, 5));

		leftPanel.add(new JLabel("Step Size"));
		leftPanel.add(new JLabel("Min Step Size"));
		leftPanel.add(new JLabel("Max Step Size"));
		leftPanel.add(new JLabel("Absolute Tolerance"));
		leftPanel.add(new JLabel("Relative Tolerance"));
		rightPanel.add(stepSizeField);
		rightPanel.add(minStepSizeField);
		rightPanel.add(maxStepSizeField);
		rightPanel.add(absToleranceField);
		rightPanel.add(relToleranceField);

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder("Integration"));
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createEmptyBorderPanel(typeBox)),
				BorderLayout.NORTH);
		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.EAST);

		return panel;
	}

	private void updateIntegrationPanel() {
		boolean isAdaptiveStepSize = typeBox.getSelectedItem() != Integrator.Type.RUNGE_KUTTA;

		stepSizeField.setEnabled(!isAdaptiveStepSize);
		minStepSizeField.setEnabled(isAdaptiveStepSize);
		maxStepSizeField.setEnabled(isAdaptiveStepSize);
		absToleranceField.setEnabled(isAdaptiveStepSize);
		relToleranceField.setEnabled(isAdaptiveStepSize);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == expertBox) {
			if (expertBox.isSelected()) {
				expertPanel.setVisible(true);
			} else {
				expertPanel.setVisible(false);
			}
		} else if (e.getSource() == clearButton) {
			for (String param : minimumFields.keySet()) {
				minimumFields.get(param).setValue(null);
				maximumFields.get(param).setValue(null);
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == typeBox
				&& e.getStateChange() == ItemEvent.SELECTED) {
			updateIntegrationPanel();
		}
	}
}
