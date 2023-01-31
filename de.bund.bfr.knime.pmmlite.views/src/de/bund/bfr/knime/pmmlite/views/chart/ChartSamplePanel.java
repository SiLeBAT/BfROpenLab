/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views.chart;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.DataTable;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.IntTextField;
import de.bund.bfr.knime.ui.KnimeDialog;
import de.bund.bfr.knime.ui.TextInput;
import de.bund.bfr.knime.ui.TextListener;

public class ChartSamplePanel extends JPanel implements ActionListener, CellEditorListener {

	private static final long serialVersionUID = 1L;

	private static final int ROW_COUNT = 100;
	private static final int DEFAULT_STEP_COUNT = 10;
	private static final double DEFAULT_STEP_SIZE = 10.0;

	private DataTable table;
	private JButton clearButton;
	private JButton stepsButton;

	private String nameX;
	private List<String> namesY;

	public ChartSamplePanel() {
		nameX = null;
		namesY = new ArrayList<>();

		stepsButton = new JButton("Set equidistant steps");
		stepsButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(UI.createEastPanel(UI.createHorizontalPanel(stepsButton, clearButton)), BorderLayout.SOUTH);
		updateTable();
	}

	public void addEditListener(EditListener listener) {
		listenerList.add(EditListener.class, listener);
	}

	public void removeEditListener(EditListener listener) {
		listenerList.remove(EditListener.class, listener);
	}

	public String getNameX() {
		return nameX;
	}

	public void setNameX(String nameX) {
		this.nameX = nameX;
		updateTable();
	}

	public List<String> getNamesY() {
		return namesY;
	}

	public void setNamesY(List<String> namesY) {
		this.namesY = namesY;
		updateTable();
	}

	public List<Double> getValuesX() {
		List<Double> valuesX = new ArrayList<>();

		for (int i = 0; i < ROW_COUNT; i++) {
			valuesX.add(table.getX(i));
		}

		return valuesX;
	}

	public void setValuesX(List<Double> valuesX) {
		for (int i = 0; i < ROW_COUNT; i++) {
			if (i >= valuesX.size()) {
				table.setX(i, null);
			} else {
				table.setX(i, valuesX.get(i));
			}
		}
	}

	public void setDataPoints(Map<String, double[][]> points) {
		Map<String, Map<Double, Double>> pointMap = new LinkedHashMap<>();

		for (Map.Entry<String, double[][]> entry : points.entrySet()) {
			Map<Double, Double> values = new LinkedHashMap<>();
			double[][] ps = entry.getValue();

			if (ps != null && ps.length == 2) {
				for (int i = 0; i < ps[0].length; i++) {
					if (!Double.isNaN(ps[0][i]) && !Double.isNaN(ps[1][i])) {
						values.put(ps[0][i], ps[1][i]);
					}
				}
			}

			pointMap.put(entry.getKey(), values);
		}

		for (Map.Entry<String, Map<Double, Double>> entry : pointMap.entrySet()) {
			for (int i = 0; i < ROW_COUNT; i++) {
				table.setY(i, entry.getKey(), entry.getValue().get(table.getX(i)));
			}
		}

		table.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == clearButton) {
			table.clear();
			table.repaint();
			fireValuesChanged();
		} else if (e.getSource() == stepsButton) {
			StepDialog dialog = new StepDialog(this);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				int stepNumber = dialog.getNumberOfSteps();
				double stepSize = dialog.getStepSize();

				for (int i = 0; i < ROW_COUNT; i++) {
					Double x = null;

					if (i < stepNumber) {
						x = i * stepSize;
					}

					table.setX(i, x);
				}

				table.repaint();
				fireValuesChanged();
			}
		}
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		fireValuesChanged();
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	private void updateTable() {
		List<Double> valuesX = null;

		if (table != null) {
			valuesX = getValuesX();
			remove(table);
		}

		table = new DataTable(ROW_COUNT, true, false, nameX, namesY);
		table.addCellEditorListener(this);
		table.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(table, BorderLayout.CENTER);
		revalidate();

		if (valuesX != null) {
			setValuesX(valuesX);
		}
	}

	private void fireValuesChanged() {
		Stream.of(getListeners(EditListener.class)).forEach(l -> l.valuesChanged(this));
	}

	public static interface EditListener extends EventListener {

		void valuesChanged(ChartSamplePanel source);
	}

	private static class StepDialog extends KnimeDialog implements ActionListener, TextListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private int numberOfSteps;
		private double stepSize;

		private IntTextField numberField;
		private DoubleTextField sizeField;

		private JButton okButton;
		private JButton cancelButton;

		public StepDialog(Component owner) {
			super(owner, "Steps", DEFAULT_MODALITY_TYPE);

			approved = false;
			numberOfSteps = 0;
			stepSize = 0.0;

			numberField = new IntTextField(false, 16);
			numberField.setMinValue(1);
			numberField.setMaxValue(ROW_COUNT);
			numberField.setValue(DEFAULT_STEP_COUNT);
			numberField.addTextListener(this);
			sizeField = new DoubleTextField(false, 16);
			sizeField.setMinValue(0.0);
			sizeField.setValue(DEFAULT_STEP_SIZE);
			sizeField.addTextListener(this);
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			setLayout(new BorderLayout());
			add(UI.createOptionsPanel(Arrays.asList(new JLabel("Number of Steps:"), new JLabel("Step Size:")),
					Arrays.asList(numberField, sizeField)), BorderLayout.CENTER);
			add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

			pack();
			UI.adjustDialog(this);
			setLocationRelativeTo(owner);
			getRootPane().setDefaultButton(okButton);
			setResizable(false);
		}

		public boolean isApproved() {
			return approved;
		}

		public int getNumberOfSteps() {
			return numberOfSteps;
		}

		public double getStepSize() {
			return stepSize;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				approved = true;
				numberOfSteps = numberField.getValue();
				stepSize = sizeField.getValue();
				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			}
		}

		@Override
		public void textChanged(TextInput source) {
			okButton.setEnabled(numberField.isValueValid() && sizeField.isValueValid());
		}
	}

}
