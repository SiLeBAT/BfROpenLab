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
package de.bund.bfr.knime.ui;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class AutoSuggestField extends JComboBox<String>implements KeyListener {

	private static final long serialVersionUID = 1L;

	private List<String> list;
	private boolean shouldHide;

	public AutoSuggestField(int columns) {
		list = new ArrayList<>();
		shouldHide = false;

		setEditable(true);
		setSelectedIndex(-1);

		JTextField field = (JTextField) getEditor().getEditorComponent();

		field.setText("");
		field.addKeyListener(this);
		field.setColumns(columns);
	}

	public void setPossibleValues(Set<String> possibleValues) {
		String selected = (String) getSelectedItem();

		if (possibleValues == null) {
			possibleValues = new LinkedHashSet<>();
		}

		this.list = new ArrayList<>(possibleValues);
		removeAllItems();

		for (String s : possibleValues) {
			addItem(s);
		}

		setSelectedItem(selected);
	}

	@Override
	public void keyTyped(final KeyEvent e) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				String text = ((JTextField) e.getComponent()).getText();

				if (text.isEmpty()) {
					String[] array = list.toArray(new String[list.size()]);
					ComboBoxModel<String> m = new DefaultComboBoxModel<>(array);

					setSuggestionModel(AutoSuggestField.this, m, "");
					hidePopup();
				} else {
					ComboBoxModel<String> m = getSuggestedModel(list, text);

					if (m.getSize() == 0 || shouldHide) {
						hidePopup();
					} else {
						setSuggestionModel(AutoSuggestField.this, m, text);
						showPopup();
					}
				}
			}
		});
	}

	@Override
	public void keyPressed(KeyEvent e) {
		JTextField textField = (JTextField) e.getComponent();
		String text = textField.getText();

		shouldHide = false;

		switch (e.getKeyCode()) {
		case KeyEvent.VK_RIGHT:
			for (String s : list) {
				if (s.toLowerCase().startsWith(text.toLowerCase())) {
					textField.setText(s);
					return;
				}
			}

			break;
		case KeyEvent.VK_ENTER:
			if (!list.contains(text)) {
				list.add(text);
				Collections.sort(list);
				setSuggestionModel(AutoSuggestField.this, getSuggestedModel(list, text), text);
			}

			shouldHide = true;
			break;
		case KeyEvent.VK_ESCAPE:
			shouldHide = true;
			break;
		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	private static void setSuggestionModel(JComboBox<String> comboBox, ComboBoxModel<String> mdl, String str) {
		comboBox.setModel(mdl);
		comboBox.setSelectedIndex(-1);
		((JTextField) comboBox.getEditor().getEditorComponent()).setText(str);
	}

	private static ComboBoxModel<String> getSuggestedModel(List<String> list, String text) {
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();

		for (String s : list) {
			if (s.toLowerCase().startsWith(text.toLowerCase())) {
				m.addElement(s);
			}
		}

		return m;
	}
}
