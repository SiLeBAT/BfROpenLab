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

import java.awt.Color;
import java.util.Arrays;

import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

public abstract class TypedTextField extends JTextField implements TextInput {

	private static final long serialVersionUID = 1L;

	private DocumentListener documentListener;
	private boolean optional;
	protected boolean valueValid;

	public TypedTextField(boolean optional, int columns) {
		super(columns);

		this.optional = optional;
		valueValid = true;
		getDocument().addDocumentListener(documentListener = new DocumentActionListener(e -> textChanged()));
	}

	@Override
	public abstract Object getValue();

	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public boolean isValueValid() {
		return valueValid;
	}

	@Override
	public void addTextListener(TextListener listener) {
		listenerList.add(TextListener.class, listener);
	}

	@Override
	public void removeTextListener(TextListener listener) {
		listenerList.remove(TextListener.class, listener);
	}

	@Override
	public Color getForeground() {
		if (!valueValid && isEnabled()) {
			return Color.RED;
		}

		return super.getForeground();
	}

	@Override
	public Color getBackground() {
		if (!valueValid && isEnabled() && getDocument() != null && getText().trim().isEmpty()) {
			return Color.RED;
		}

		return super.getBackground();
	}

	protected void textChanged() {
		Arrays.asList(listenerList.getListeners(TextListener.class)).forEach(l -> l.textChanged(this));
	}

	protected void setTextWithoutListener(String text) {
		getDocument().removeDocumentListener(documentListener);
		setText(text);
		getDocument().addDocumentListener(documentListener);
	}
}
