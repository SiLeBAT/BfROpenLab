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
package de.bund.bfr.knime.nls.functionport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;

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

		for (Map.Entry<String, String> entry : f.getTerms().entrySet()) {
			JLabel label = new JLabel(entry.getKey() + " = " + entry.getValue());

			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(label);
			panel.add(Box.createVerticalStrut(5));
		}

		if (!f.getInitialValues().isEmpty()) {
			String s = "Initial Values: ";
			boolean hasInitialValues = false;

			for (Map.Entry<String, Double> entry : f.getInitialValues()
					.entrySet()) {
				if (entry.getValue() != null) {
					s += entry.getKey() + "=" + entry.getValue() + ", ";
					hasInitialValues = true;
				}
			}

			if (hasInitialValues) {
				JLabel label = new JLabel(s.substring(0, s.length() - 2));

				label.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(label);
				panel.add(Box.createVerticalStrut(5));
			}
		}

		JLabel depVarLabel = new JLabel("Dependent Variable: "
				+ f.getDependentVariable());
		JLabel indepVarLabel = new JLabel("Independent Variables: "
				+ Joiner.on(", ").join(f.getIndependentVariables()));
		JLabel paramVarLabel = new JLabel("Parameters: "
				+ Joiner.on(", ").join(f.getParameters()));
		JLabel diffVarLabel = new JLabel("Differential Variable: "
				+ f.getDiffVariable());

		depVarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		indepVarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		paramVarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		diffVarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.add(depVarLabel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(indepVarLabel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(paramVarLabel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(diffVarLabel);

		setName("Function");
		setLayout(new BorderLayout());
		setBackground(NodeView.COLOR_BACKGROUND);
		add(panel, BorderLayout.CENTER);
		revalidate();
	}
}
