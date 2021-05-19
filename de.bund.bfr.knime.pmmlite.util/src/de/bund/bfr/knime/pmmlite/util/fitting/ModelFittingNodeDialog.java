/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.util.fitting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.CombineUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.models.Model;
import de.bund.bfr.knime.pmmlite.core.models.ModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.knime.pmmlite.util.fitting.ModelFittingSettings.FittingType;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.IntTextField;

/**
 * <code>NodeDialog</code> for the "ModelFitting" Node.
 * 
 * @author Christian Thoens
 */
public class ModelFittingNodeDialog extends DataAwareNodeDialogPane implements ActionListener {

	private PmmPortObject input;
	private ModelFittingSettings set;

	private JComboBox<FittingType> fittingBox;
	private JCheckBox limitsBox;
	private JCheckBox expertBox;

	private Set<String> modelIds;
	private Map<String, String> modelNames;
	private Map<String, List<String>> modelParams;
	private Map<String, Map<String, Double>> modelMinValues;
	private Map<String, Map<String, Double>> modelMaxValues;

	private JPanel fittingPanel;
	private JPanel expertSettingsPanel;

	private IntTextField nParamSpaceField;
	private IntTextField nLevenbergField;
	private JCheckBox stopWhenSuccessBox;
	private JButton modelRangeButton;
	private JButton rangeButton;
	private JButton clearButton;
	private Map<String, Map<String, DoubleTextField>> minimumFields;
	private Map<String, Map<String, DoubleTextField>> maximumFields;

	/**
	 * New pane for configuring the ModelFitting node.
	 */
	protected ModelFittingNodeDialog() {
		set = new ModelFittingSettings();

		fittingBox = new JComboBox<>(FittingType.values());
		fittingPanel = new JPanel();
		fittingPanel.setLayout(new BorderLayout());

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createBorderPanel(fittingBox)), BorderLayout.NORTH);
		panel.add(fittingPanel, BorderLayout.CENTER);

		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		set.loadSettings(settings);
		this.input = (PmmPortObject) input[0];

		switch (this.input.getSpec()) {
		case PRIMARY_MODEL_TYPE:
			set.setFittingType(FittingType.PRIMARY_FITTING);
			break;
		case SECONDARY_MODEL_TYPE:
			if (set.getFittingType() == null || set.getFittingType() == FittingType.PRIMARY_FITTING) {
				set.setFittingType(FittingType.SECONDARY_FITTING);
			}

			break;
		case TERTIARY_MODEL_TYPE:
			set.setFittingType(FittingType.TERTIARY_FITTING);
			break;
		case DATA_TYPE:
		case EMPTY_TYPE:
		case PRIMARY_MODEL_FORMULA_TYPE:
		case SECONDARY_MODEL_FORMULA_TYPE:
		case TERTIARY_MODEL_FORMULA_TYPE:
			throw new NotConfigurableException("Unsupported input type: " + this.input.getSpec());
		default:
			throw new RuntimeException("Unknown input type: " + this.input.getSpec());
		}

		fittingBox.setSelectedItem(set.getFittingType());
		fittingBox.addActionListener(this);

		try {
			initGUI();
		} catch (UnitException e) {
			throw new NotConfigurableException(e.getMessage());
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (!nParamSpaceField.isValueValid() || !nLevenbergField.isValueValid() || minimumFields == null
				|| maximumFields == null) {
			throw new InvalidSettingsException("");
		}

		Map<String, Map<String, Double>> minStartValues = new LinkedHashMap<>();
		Map<String, Map<String, Double>> maxStartValues = new LinkedHashMap<>();

		for (Map.Entry<String, List<String>> entry : modelParams.entrySet()) {
			Map<String, DoubleTextField> minFields = minimumFields.get(entry.getKey());
			Map<String, DoubleTextField> maxFields = maximumFields.get(entry.getKey());
			Map<String, Double> minValues = new LinkedHashMap<>();
			Map<String, Double> maxValues = new LinkedHashMap<>();

			for (String param : entry.getValue()) {
				minValues.put(param, minFields.get(param).getValue());
				maxValues.put(param, maxFields.get(param).getValue());
			}

			minStartValues.put(entry.getKey(), minValues);
			maxStartValues.put(entry.getKey(), maxValues);
		}

		set.setFittingType((FittingType) fittingBox.getSelectedItem());
		set.setnParameterSpace(nParamSpaceField.getValue());
		set.setnLevenberg(nLevenbergField.getValue());
		set.setEnforceLimits(limitsBox.isSelected());
		set.setExpertSettings(expertBox.isSelected());
		set.setStopWhenSuccessful(stopWhenSuccessBox.isSelected());
		set.setMinStartValues(minStartValues);
		set.setMaxStartValues(maxStartValues);
		set.saveSettings(settings);
	}

	private void readTable(List<? extends Model> dataModels) {
		modelIds = new LinkedHashSet<>();
		modelNames = new LinkedHashMap<>();
		modelParams = new LinkedHashMap<>();
		modelMinValues = new LinkedHashMap<>();
		modelMaxValues = new LinkedHashMap<>();

		for (Model dataModel : dataModels) {
			ModelFormula model = dataModel.getFormula();
			String id = model.getId();

			if (modelIds.add(id)) {
				List<String> paramNames = new ArrayList<>();
				Map<String, Double> min = new LinkedHashMap<>();
				Map<String, Double> max = new LinkedHashMap<>();

				for (Parameter param : model.getParams()) {
					paramNames.add(param.getName());
					min.put(param.getName(), param.getMin());
					max.put(param.getName(), param.getMax());
				}

				modelNames.put(id, model.getName());
				modelParams.put(id, paramNames);
				modelMinValues.put(id, min);
				modelMaxValues.put(id, max);
			}
		}
	}

	private JComponent createOptionsPanel() {
		limitsBox = new JCheckBox("Enforce limits of Formula Definition");
		limitsBox.setSelected(set.isEnforceLimits());
		expertBox = new JCheckBox("Expert Settings");
		expertBox.setSelected(set.isExpertSettings());
		expertBox.addActionListener(this);

		JPanel optionsPanel = new JPanel();

		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		optionsPanel.add(UI.createWestPanel(UI.createBorderPanel(limitsBox)));
		optionsPanel.add(UI.createWestPanel(UI.createBorderPanel(expertBox)));

		return optionsPanel;
	}

	private JComponent createRegressionPanel() {
		nParamSpaceField = new IntTextField(false, 16);
		nParamSpaceField.setMinValue(0);
		nParamSpaceField.setMaxValue(1000000);
		nParamSpaceField.setValue(set.getnParameterSpace());
		nLevenbergField = new IntTextField(false, 16);
		nLevenbergField.setMinValue(1);
		nLevenbergField.setMaxValue(100);
		nLevenbergField.setValue(set.getnLevenberg());
		stopWhenSuccessBox = new JCheckBox("Stop When Regression Successful");
		stopWhenSuccessBox.setSelected(set.isStopWhenSuccessful());

		JPanel leftRegressionPanel = new JPanel();

		leftRegressionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		leftRegressionPanel.setLayout(new GridLayout(3, 1, 5, 5));
		leftRegressionPanel.add(new JLabel("Maximal Evaluations to Find Start Values"));
		leftRegressionPanel.add(new JLabel("Maximal Executions of the Levenberg Algorithm"));
		leftRegressionPanel.add(stopWhenSuccessBox);

		JPanel rightRegressionPanel = new JPanel();

		rightRegressionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightRegressionPanel.setLayout(new GridLayout(3, 1, 5, 5));
		rightRegressionPanel.add(nParamSpaceField);
		rightRegressionPanel.add(nLevenbergField);
		rightRegressionPanel.add(new JLabel());

		JPanel regressionPanel = new JPanel();

		regressionPanel.setBorder(new TitledBorder("Nonlinear Regression Parameters"));
		regressionPanel.setLayout(new BorderLayout());
		regressionPanel.add(leftRegressionPanel, BorderLayout.WEST);
		regressionPanel.add(rightRegressionPanel, BorderLayout.EAST);

		return regressionPanel;
	}

	private JComponent createRangePanel() {
		modelRangeButton = new JButton("Use Range from Formula Definition");
		modelRangeButton.addActionListener(this);
		rangeButton = new JButton("Fill Empty Fields");
		rangeButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		minimumFields = new LinkedHashMap<>();
		maximumFields = new LinkedHashMap<>();

		JPanel northRangePanel = new JPanel();

		northRangePanel.setLayout(new BoxLayout(northRangePanel, BoxLayout.Y_AXIS));

		for (String id : modelIds) {
			List<Component> leftComponents = new ArrayList<>();
			List<Component> rightComponents = new ArrayList<>();
			List<String> params = modelParams.get(id);
			Map<String, DoubleTextField> minFields = new LinkedHashMap<>();
			Map<String, DoubleTextField> maxFields = new LinkedHashMap<>();
			Map<String, Double> minStartValues = set.getMinStartValues().get(id);
			Map<String, Double> maxStartValues = set.getMaxStartValues().get(id);

			for (String param : params) {
				DoubleTextField minField = new DoubleTextField(true, 16);
				DoubleTextField maxField = new DoubleTextField(true, 16);
				Double min = modelMinValues.get(id).get(param);
				Double max = modelMaxValues.get(id).get(param);

				if (minStartValues != null && minStartValues.containsKey(param)) {
					minField.setValue(minStartValues.get(param));
				} else {
					minField.setValue(min);
				}

				if (maxStartValues != null && maxStartValues.containsKey(param)) {
					maxField.setValue(maxStartValues.get(param));
				} else {
					maxField.setValue(max);
				}

				String rangeString;

				if (min != null && max != null) {
					rangeString = " (" + min + " to " + max + "):";
				} else if (min != null) {
					rangeString = " (" + min + " to ):";
				} else if (max != null) {
					rangeString = " ( to " + max + "):";
				} else {
					rangeString = ":";
				}

				minFields.put(param, minField);
				maxFields.put(param, maxField);
				leftComponents.add(new JLabel(param + rangeString));
				rightComponents
						.add(UI.createEastPanel(UI.createHorizontalPanel(false, minField, new JLabel("to"), maxField)));
			}

			minimumFields.put(id, minFields);
			maximumFields.put(id, maxFields);
			northRangePanel.add(UI.createOptionsPanel(modelNames.get(id), leftComponents, rightComponents));
		}

		JPanel rangePanel = new JPanel();

		rangePanel.setLayout(new BorderLayout());
		rangePanel.add(northRangePanel, BorderLayout.NORTH);

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder("Specific Start Values for Fitting Procedure - Optional"));
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(modelRangeButton, rangeButton, clearButton)),
				BorderLayout.NORTH);
		panel.add(new JScrollPane(rangePanel), BorderLayout.CENTER);

		return panel;
	}

	private void initGUI() throws UnitException {
		List<? extends Model> dataModels = null;
		FittingType type = (FittingType) fittingBox.getSelectedItem();
		PmmPortObjectSpec spec = input.getSpec();

		if (type == FittingType.PRIMARY_FITTING && spec == PmmPortObjectSpec.PRIMARY_MODEL_TYPE
				|| type == FittingType.SECONDARY_FITTING && spec == PmmPortObjectSpec.SECONDARY_MODEL_TYPE
				|| type == FittingType.TERTIARY_FITTING && spec == PmmPortObjectSpec.TERTIARY_MODEL_TYPE) {
			dataModels = input.getData(Model.class);
		} else if (type == FittingType.TERTIARY_FITTING && spec == PmmPortObjectSpec.SECONDARY_MODEL_TYPE) {
			dataModels = CombineUtils.combine(input.getData(SecondaryModel.class));
		}

		if (dataModels != null && !dataModels.isEmpty()) {
			readTable(dataModels);
			expertSettingsPanel = new JPanel();
			expertSettingsPanel.setLayout(new BorderLayout());
			expertSettingsPanel.add(createRegressionPanel(), BorderLayout.NORTH);
			expertSettingsPanel.add(createRangePanel(), BorderLayout.CENTER);

			fittingPanel.removeAll();
			fittingPanel.add(createOptionsPanel(), BorderLayout.NORTH);
			fittingPanel.add(expertSettingsPanel, BorderLayout.CENTER);
			fittingPanel.revalidate();
			fittingPanel.repaint();

			Dimension preferredSize = fittingPanel.getPreferredSize();

			expertSettingsPanel.setVisible(expertBox.isSelected());
			fittingPanel.setPreferredSize(preferredSize);
		} else {
			JLabel label = new JLabel("Data is not valid for " + fittingBox.getSelectedItem());

			label.setHorizontalAlignment(SwingConstants.CENTER);

			fittingPanel.removeAll();
			fittingPanel.add(label, BorderLayout.CENTER);
			fittingPanel.revalidate();

			if (fittingBox.isValid()) {
				Dialogs.showErrorMessage(fittingBox, "Data is not valid for " + fittingBox.getSelectedItem());
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fittingBox) {
			try {
				initGUI();
			} catch (UnitException ex) {
			}
		} else if (e.getSource() == expertBox) {
			if (expertBox.isSelected()) {
				expertSettingsPanel.setVisible(true);
			} else {
				expertSettingsPanel.setVisible(false);
			}
		} else if (e.getSource() == modelRangeButton) {
			for (Map.Entry<String, List<String>> entry : modelParams.entrySet()) {
				Map<String, DoubleTextField> minFields = minimumFields.get(entry.getKey());
				Map<String, DoubleTextField> maxFields = maximumFields.get(entry.getKey());
				Map<String, Double> minValues = modelMinValues.get(entry.getKey());
				Map<String, Double> maxValues = modelMaxValues.get(entry.getKey());

				for (String param : entry.getValue()) {
					minFields.get(param).setValue(minValues.get(param));
					maxFields.get(param).setValue(maxValues.get(param));
				}
			}
		} else if (e.getSource() == rangeButton) {
			for (Map.Entry<String, List<String>> entry : modelParams.entrySet()) {
				Map<String, DoubleTextField> minFields = minimumFields.get(entry.getKey());
				Map<String, DoubleTextField> maxFields = maximumFields.get(entry.getKey());

				for (String param : entry.getValue()) {
					DoubleTextField minField = minFields.get(param);
					DoubleTextField maxField = maxFields.get(param);

					if (minField.getValue() == null && maxField.getValue() == null) {
						minField.setValue(-1000000.0);
						maxField.setValue(1000000.0);
					}
				}
			}
		} else if (e.getSource() == clearButton) {
			for (Map<String, DoubleTextField> fields : minimumFields.values()) {
				for (DoubleTextField field : fields.values()) {
					field.setValue(null);
				}
			}

			for (Map<String, DoubleTextField> fields : maximumFields.values()) {
				for (DoubleTextField field : fields.values()) {
					field.setValue(null);
				}
			}
		}
	}
}
