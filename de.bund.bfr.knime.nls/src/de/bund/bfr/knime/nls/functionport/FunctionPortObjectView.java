/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.nls.functionport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.NodeView;

import com.google.common.base.Joiner;

import de.bund.bfr.knime.nls.Function;

public class FunctionPortObjectView extends JComponent {

	private static final long serialVersionUID = 1L;

	public FunctionPortObjectView(FunctionPortObject portObject) {
		Function f = portObject.getFunction();
		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		f.getTerms().forEach((depVar, term) -> {
			JLabel label;

			if (f.getTimeVariable() != null) {
				label = new JLabel("d" + depVar + "/d" + f.getTimeVariable() + " = " + term);
			} else {
				label = new JLabel(depVar + " = " + term);
			}

			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(label);
			panel.add(Box.createVerticalStrut(5));
		});

		if (!f.getInitValues().isEmpty()) {
			List<String> initValues = new ArrayList<>();

			f.getInitValues().forEach((depVar, value) -> {
				if (value != null) {
					initValues.add(depVar + "_0=" + value);
				}
			});

			if (!initValues.isEmpty()) {
				JLabel label = new JLabel("Initial Values: " + Joiner.on(", ").join(initValues));

				label.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(label);
				panel.add(Box.createVerticalStrut(5));
			}
		}

		JLabel depVarLabel = new JLabel("Dependent Variable: " + f.getDependentVariable());
		JLabel indepVarLabel = new JLabel(
				"Independent Variables: " + Joiner.on(", ").join(f.getIndependentVariables()));
		JLabel paramVarLabel = new JLabel("Parameters: " + Joiner.on(", ").join(f.getParameters()));

		depVarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		indepVarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		paramVarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.add(depVarLabel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(indepVarLabel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(paramVarLabel);

		setName("Function");
		setLayout(new BorderLayout());
		setBackground(NodeView.COLOR_BACKGROUND);
		add(panel, BorderLayout.CENTER);
		revalidate();
	}
}
