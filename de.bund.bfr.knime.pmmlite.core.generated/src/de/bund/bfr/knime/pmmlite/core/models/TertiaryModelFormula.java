/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Tertiary Model Formula</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getIndepVars <em>Indep Vars</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getTimeVar <em>Time Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getInitialParam <em>Initial Param</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getPrimaryFormula <em>Primary Formula</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getSecondaryFormulas <em>Secondary Formulas</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getSecondaryRenamings <em>Secondary Renamings</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getAssignments <em>Assignments</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModelFormula()
 * @model
 * @generated
 */
public interface TertiaryModelFormula extends ModelFormula {
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
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModelFormula_IndepVars()
	 * @model containment="true"
	 * @generated
	 */
	EList<Variable> getIndepVars();

	/**
	 * Returns the value of the '<em><b>Time Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Time Var</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Time Var</em>' attribute.
	 * @see #setTimeVar(String)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModelFormula_TimeVar()
	 * @model
	 * @generated
	 */
	String getTimeVar();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getTimeVar <em>Time Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Time Var</em>' attribute.
	 * @see #getTimeVar()
	 * @generated
	 */
	void setTimeVar(String value);

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
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModelFormula_InitialParam()
	 * @model
	 * @generated
	 */
	String getInitialParam();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getInitialParam <em>Initial Param</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Initial Param</em>' attribute.
	 * @see #getInitialParam()
	 * @generated
	 */
	void setInitialParam(String value);

	/**
	 * Returns the value of the '<em><b>Primary Formula</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Primary Formula</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Primary Formula</em>' reference.
	 * @see #setPrimaryFormula(PrimaryModelFormula)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModelFormula_PrimaryFormula()
	 * @model
	 * @generated
	 */
	PrimaryModelFormula getPrimaryFormula();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getPrimaryFormula <em>Primary Formula</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Primary Formula</em>' reference.
	 * @see #getPrimaryFormula()
	 * @generated
	 */
	void setPrimaryFormula(PrimaryModelFormula value);

	/**
	 * Returns the value of the '<em><b>Secondary Formulas</b></em>' reference list.
	 * The list contents are of type {@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Secondary Formulas</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Secondary Formulas</em>' reference list.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModelFormula_SecondaryFormulas()
	 * @model
	 * @generated
	 */
	EList<SecondaryModelFormula> getSecondaryFormulas();

	/**
	 * Returns the value of the '<em><b>Secondary Renamings</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link de.bund.bfr.knime.pmmlite.core.models.Renamings},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Secondary Renamings</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Secondary Renamings</em>' map.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModelFormula_SecondaryRenamings()
	 * @model mapType="de.bund.bfr.knime.pmmlite.core.models.StringToRenamingsMapEntry<org.eclipse.emf.ecore.EString, de.bund.bfr.knime.pmmlite.core.models.Renamings>"
	 * @generated
	 */
	EMap<String, Renamings> getSecondaryRenamings();

	/**
	 * Returns the value of the '<em><b>Assignments</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.String},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Assignments</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Assignments</em>' map.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModelFormula_Assignments()
	 * @model mapType="de.bund.bfr.knime.pmmlite.core.models.StringToStringMapEntry<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString>"
	 * @generated
	 */
	EMap<String, String> getAssignments();

} // TertiaryModelFormula
