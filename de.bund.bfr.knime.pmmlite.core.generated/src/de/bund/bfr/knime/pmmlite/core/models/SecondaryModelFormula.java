/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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

import de.bund.bfr.math.Transform;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Secondary Model Formula</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula#getIndepVars <em>Indep Vars</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula#getTransformation <em>Transformation</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getSecondaryModelFormula()
 * @model
 * @generated
 */
public interface SecondaryModelFormula extends ModelFormula {
	/**
	 * Returns the value of the '<em><b>Indep Vars</b></em>' containment reference list.
	 * The list contents are of type {@link de.bund.bfr.knime.pmmlite.core.models.Variable}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Indep Vars</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Indep Vars</em>' containment reference list.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getSecondaryModelFormula_IndepVars()
	 * @model containment="true"
	 * @generated
	 */
	EList<Variable> getIndepVars();

	/**
	 * Returns the value of the '<em><b>Transformation</b></em>' attribute.
	 * The default value is <code>"NO_TRANSFORM"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Transformation</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Transformation</em>' attribute.
	 * @see #setTransformation(Transform)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getSecondaryModelFormula_Transformation()
	 * @model default="NO_TRANSFORM" dataType="de.bund.bfr.knime.pmmlite.core.common.Transform"
	 * @generated
	 */
	Transform getTransformation();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula#getTransformation <em>Transformation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Transformation</em>' attribute.
	 * @see #getTransformation()
	 * @generated
	 */
	void setTransformation(Transform value);

} // SecondaryModelFormula
