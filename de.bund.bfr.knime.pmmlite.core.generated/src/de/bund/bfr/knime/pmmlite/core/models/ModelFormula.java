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
/**
 */
package de.bund.bfr.knime.pmmlite.core.models;

import de.bund.bfr.knime.pmmlite.core.common.Identifiable;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Model Formula</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getExpression <em>Expression</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getDepVar <em>Dep Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getParams <em>Params</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModelFormula()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ModelFormula extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Expression</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Expression</em>' attribute.
	 * @see #setExpression(String)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModelFormula_Expression()
	 * @model
	 * @generated
	 */
	String getExpression();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getExpression <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expression</em>' attribute.
	 * @see #getExpression()
	 * @generated
	 */
	void setExpression(String value);

	/**
	 * Returns the value of the '<em><b>Dep Var</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dep Var</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dep Var</em>' containment reference.
	 * @see #setDepVar(Variable)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModelFormula_DepVar()
	 * @model containment="true"
	 * @generated
	 */
	Variable getDepVar();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getDepVar <em>Dep Var</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dep Var</em>' containment reference.
	 * @see #getDepVar()
	 * @generated
	 */
	void setDepVar(Variable value);

	/**
	 * Returns the value of the '<em><b>Params</b></em>' containment reference list.
	 * The list contents are of type {@link de.bund.bfr.knime.pmmlite.core.models.Parameter}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Params</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Params</em>' containment reference list.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModelFormula_Params()
	 * @model containment="true"
	 * @generated
	 */
	EList<Parameter> getParams();

} // ModelFormula
