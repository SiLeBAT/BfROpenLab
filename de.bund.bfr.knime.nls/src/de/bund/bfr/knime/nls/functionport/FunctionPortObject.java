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
package de.bund.bfr.knime.nls.functionport;

import java.io.Serializable;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import de.bund.bfr.knime.nls.Function;

public class FunctionPortObject implements PortObject, Serializable {

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(FunctionPortObject.class);

	private static final long serialVersionUID = 1L;

	private Function function;

	public FunctionPortObject(Function function) {
		this.function = function;
	}

	public Function getFunction() {
		return function;
	}

	@Override
	public String getSummary() {
		return "Function Port";
	}

	@Override
	public PortObjectSpec getSpec() {
		return new FunctionPortObjectSpec(function);
	}

	@Override
	public JComponent[] getViews() {
		return new JComponent[] { new FunctionPortObjectView(this) };
	}
}
