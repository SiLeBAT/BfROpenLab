/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Primary Model Formula</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getIndepVar <em>Indep Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getInitialParam <em>Initial Param</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModelFormula()
 * @model
 * @generated
 */
public interface PrimaryModelFormula extends ModelFormula {
	/**
	 * Returns the value of the '<em><b>Indep Var</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Indep Var</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Indep Var</em>' containment reference.
	 * @see #setIndepVar(Variable)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModelFormula_IndepVar()
	 * @model containment="true"
	 * @generated
	 */
	Variable getIndepVar();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getIndepVar <em>Indep Var</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Indep Var</em>' containment reference.
	 * @see #getIndepVar()
	 * @generated
	 */
	void setIndepVar(Variable value);

	/**
	 * Returns the value of the '<em><b>Initial Param</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Initial Param</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Initial Param</em>' attribute.
	 * @see #setInitialParam(String)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModelFormula_InitialParam()
	 * @model
	 * @generated
	 */
	String getInitialParam();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getInitialParam <em>Initial Param</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Initial Param</em>' attribute.
	 * @see #getInitialParam()
	 * @generated
	 */
	void setInitialParam(String value);

} // PrimaryModelFormula
