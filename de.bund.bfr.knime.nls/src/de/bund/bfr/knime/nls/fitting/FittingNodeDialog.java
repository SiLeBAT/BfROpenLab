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
package de.bund.bfr.knime.nls.fitting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
public class FittingNodeDialog extends NodeDialogPane implements ActionListener {

	private FittingSettings set;
	private boolean isDiff;

	private JPanel mainPanel;
	private JPanel expertPanel;

	private JCheckBox expertBox;

	private DoubleTextField stepSizeField;
	private IntTextField nParamSpaceField;
	private IntTextField nLevenbergField;
	private JCheckBox stopWhenSuccessBox;
	private JButton clearButton;
	private JCheckBox limitsBox;
	private Map<String, DoubleTextField> minimumFields;
	private Map<String, DoubleTextField> maximumFields;

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
		mainPanel.add(UI.createHorizontalPanel(expertBox), BorderLayout.NORTH);
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
		if (isDiff && !stepSizeField.isValid()) {
			throw new InvalidSettingsException("");
		}

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

		set.setStepSize(isDiff ? stepSizeField.getValue() : Double.NaN);
		set.setnParameterSpace(nParamSpaceField.getValue());
		set.setnLevenberg(nLevenbergField.getValue());
		set.setEnforceLimits(limitsBox.isSelected());
		set.setExpertSettings(expertBox.isSelected());
		set.setStopWhenSuccessful(stopWhenSuccessBox.isSelected());
		set.setMinStartValues(minStartValues);
		set.setMaxStartValues(maxStartValues);

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

		List<JComponent> leftComps = new ArrayList<JComponent>(Arrays.asList(
				new JLabel("Maximal Evaluations to Find Start Values"),
				new JLabel("Maximal Executions of the Levenberg Algorithm"),
				stopWhenSuccessBox));
		List<JComponent> rightComps = new ArrayList<JComponent>(Arrays.asList(
				nParamSpaceField, nLevenbergField, new JLabel()));

		if (isDiff) {
			stepSizeField = new DoubleTextField(false, 8);
			stepSizeField.setMinValue(Double.MIN_NORMAL);
			stepSizeField.setValue(set.getStepSize());
			leftComps.add(0, new JLabel("Integration Step Size"));
			rightComps.add(0, stepSizeField);
		}

		return UI.createOptionsPanel("Nonlinear Regression Parameters",
				leftComps, rightComps);
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
			leftPanel.add(UI.createHorizontalPanel(new JLabel(param)));
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
}
