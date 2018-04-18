/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.port.IncompatibleObjectException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

public class PrimaryJoiner implements Joiner {

	private List<PrimaryModelFormula> formulas;
	private List<TimeSeries> data;

	public PrimaryJoiner(PmmPortObject in1, PmmPortObject in2) {
		this.formulas = in1.getData(PrimaryModelFormula.class);
		this.data = in2.getData(TimeSeries.class);
	}

	@Override
	public JComponent createPanel(String assignments) {
		JPanel panel = new JPanel();
		JPanel topPanel = new JPanel();

		panel.setLayout(new BorderLayout());
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		for (PrimaryModelFormula formula : formulas) {
			String depVar = formula.getDepVar().getName();
			String indepVar = formula.getIndepVar().getName();
			JComboBox<String> depBox = new JComboBox<>(new String[] { PmmUtils.CONCENTRATION });
			JComboBox<String> indepBox = new JComboBox<>(new String[] { PmmUtils.TIME });

			depBox.setSelectedIndex(0);
			indepBox.setSelectedIndex(0);
			depBox.setEditable(false);
			indepBox.setEditable(false);

			topPanel.add(UI.createTitledPanel(UI.createWestPanel(
					UI.createHorizontalPanel(new JLabel(depVar + ":"), depBox, new JLabel(indepVar + ":"), indepBox)),
					formula.getName()));
		}

		panel.add(topPanel, BorderLayout.NORTH);

		return panel;
	}

	@Override
	public String getAssignments() {
		return null;
	}

	@Override
	public PmmPortObject getOutput(String assignments) throws UnitException, IncompatibleObjectException {
		List<PrimaryModel> result = new ArrayList<>();

		for (PrimaryModelFormula formula : formulas) {
			for (TimeSeries series : data) {
				PmmUtils.convertTo(0.0, series.getConcentrationUnit(), formula.getDepVar().getUnit());
				PmmUtils.convertTo(0.0, series.getTimeUnit(), formula.getIndepVar().getUnit());

				PrimaryModel joined = ModelsFactory.eINSTANCE.createPrimaryModel();

				joined.setFormula(formula);
				joined.setData(series);
				joined.getAssignments().put(formula.getDepVar().getName(), PmmUtils.CONCENTRATION);
				joined.getAssignments().put(formula.getIndepVar().getName(), PmmUtils.TIME);
				joined.setName(formula.getName() + "_" + series.getName());
				PmmUtils.setId(joined);
				result.add(joined);
			}
		}

		return PmmPortObject.createListObject(result, PmmPortObjectSpec.PRIMARY_MODEL_TYPE);
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	public static PmmPortObjectSpec getOutputType(PmmPortObjectSpec in1, PmmPortObjectSpec in2)
			throws InvalidSettingsException {
		if (in1 == PmmPortObjectSpec.PRIMARY_MODEL_FORMULA_TYPE && in2 == PmmPortObjectSpec.DATA_TYPE) {
			return PmmPortObjectSpec.PRIMARY_MODEL_TYPE;
		} else if (in2 == PmmPortObjectSpec.PRIMARY_MODEL_FORMULA_TYPE && in1 == PmmPortObjectSpec.DATA_TYPE) {
			throw new InvalidSettingsException("Please switch the ports!");
		} else {
			throw new InvalidSettingsException("Wrong input!");
		}
	}

	public static boolean isValidInput(PmmPortObjectSpec in1, PmmPortObjectSpec in2) {
		try {
			getOutputType(in1, in2);
		} catch (InvalidSettingsException e) {
			return false;
		}

		return true;
	}
}
