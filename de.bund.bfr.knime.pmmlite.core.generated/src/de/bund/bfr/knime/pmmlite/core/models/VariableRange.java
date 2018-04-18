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
/**
 */
package de.bund.bfr.knime.pmmlite.core.models;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Variable Range</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMin <em>Min</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMax <em>Max</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getVariableRange()
 * @model
 * @generated
 */
public interface VariableRange extends EObject {
	/**
	 * Returns the value of the '<em><b>Min</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Min</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Min</em>' attribute.
	 * @see #setMin(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getVariableRange_Min()
	 * @model
	 * @generated
	 */
	Double getMin();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMin <em>Min</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Min</em>' attribute.
	 * @see #getMin()
	 * @generated
	 */
	void setMin(Double value);

	/**
	 * Returns the value of the '<em><b>Max</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Max</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max</em>' attribute.
	 * @see #setMax(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getVariableRange_Max()
	 * @model
	 * @generated
	 */
	Double getMax();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMax <em>Max</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max</em>' attribute.
	 * @see #getMax()
	 * @generated
	 */
	void setMax(Double value);

} // VariableRange
