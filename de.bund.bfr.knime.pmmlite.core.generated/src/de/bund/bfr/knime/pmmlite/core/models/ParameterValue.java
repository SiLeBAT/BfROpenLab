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

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Parameter Value</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getValue <em>Value</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getError <em>Error</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getT <em>T</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getP <em>P</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getCorrelations <em>Correlations</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getParameterValue()
 * @model
 * @generated
 */
public interface ParameterValue extends EObject {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getParameterValue_Value()
	 * @model
	 * @generated
	 */
	Double getValue();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(Double value);

	/**
	 * Returns the value of the '<em><b>Error</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Error</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Error</em>' attribute.
	 * @see #setError(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getParameterValue_Error()
	 * @model
	 * @generated
	 */
	Double getError();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getError <em>Error</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Error</em>' attribute.
	 * @see #getError()
	 * @generated
	 */
	void setError(Double value);

	/**
	 * Returns the value of the '<em><b>T</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>T</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>T</em>' attribute.
	 * @see #setT(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getParameterValue_T()
	 * @model
	 * @generated
	 */
	Double getT();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getT <em>T</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>T</em>' attribute.
	 * @see #getT()
	 * @generated
	 */
	void setT(Double value);

	/**
	 * Returns the value of the '<em><b>P</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>P</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>P</em>' attribute.
	 * @see #setP(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getParameterValue_P()
	 * @model
	 * @generated
	 */
	Double getP();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getP <em>P</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>P</em>' attribute.
	 * @see #getP()
	 * @generated
	 */
	void setP(Double value);

	/**
	 * Returns the value of the '<em><b>Correlations</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.Double},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Correlations</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Correlations</em>' map.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getParameterValue_Correlations()
	 * @model mapType="de.bund.bfr.knime.pmmlite.core.models.StringToDoubleMapEntry<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EDoubleObject>"
	 * @generated
	 */
	EMap<String, Double> getCorrelations();

} // ParameterValue
