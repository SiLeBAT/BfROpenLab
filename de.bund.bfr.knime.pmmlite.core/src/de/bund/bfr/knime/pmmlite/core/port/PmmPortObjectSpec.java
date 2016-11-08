/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.core.port;

import java.awt.Font;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.eclipse.emf.ecore.EClass;
import org.knime.core.node.port.PortObjectSpec;

import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.data.DataPackage;
import de.bund.bfr.knime.pmmlite.core.models.ModelsPackage;

public enum PmmPortObjectSpec implements PortObjectSpec {
	EMPTY_TYPE("Empty", null),

	DATA_TYPE("Data", DataPackage.Literals.TIME_SERIES),

	PRIMARY_MODEL_FORMULA_TYPE("Primary Model Formula", ModelsPackage.Literals.PRIMARY_MODEL_FORMULA),

	SECONDARY_MODEL_FORMULA_TYPE("Secondary Model Formula", ModelsPackage.Literals.SECONDARY_MODEL_FORMULA),

	TERTIARY_MODEL_FORMULA_TYPE("Tertiary Model Formula", ModelsPackage.Literals.TERTIARY_MODEL_FORMULA),

	PRIMARY_MODEL_TYPE("Primary Model", ModelsPackage.Literals.PRIMARY_MODEL),

	SECONDARY_MODEL_TYPE("Secondary Model", ModelsPackage.Literals.SECONDARY_MODEL),

	TERTIARY_MODEL_TYPE("Tertiary Model", ModelsPackage.Literals.TERTIARY_MODEL);

	private static final long serialVersionUID = 1L;

	private transient String name;
	private transient EClass eClass;

	private PmmPortObjectSpec(String name, EClass eClass) {
		this.name = name;
		this.eClass = eClass;
	}

	public String getName() {
		return name;
	}

	public EClass getEClass() {
		return eClass;
	}

	public boolean isCompatible(Identifiable obj) {
		return (obj == null && eClass == null) || (obj != null && obj.eClass() == eClass);
	}

	@Override
	public JComponent[] getViews() {
		JLabel label = new JLabel(name);

		label.setName("PMM Spec");
		label.setFont(new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize() * 2));
		label.setHorizontalAlignment(SwingConstants.CENTER);

		return new JComponent[] { label };
	}

	@Override
	public String toString() {
		return name;
	}

	public static PmmPortObjectSpec getCompatibleSpec(Identifiable obj) {
		return Stream.of(values()).filter(s -> s.isCompatible(obj)).findAny().get();
	}
}
