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

import de.bund.bfr.knime.pmmlite.core.common.Identifiable;

import org.eclipse.emf.common.util.EMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getSse <em>Sse</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getMse <em>Mse</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getRmse <em>Rmse</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getR2 <em>R2</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getAic <em>Aic</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getDegreesOfFreedom <em>Degrees Of Freedom</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getVariableRanges <em>Variable Ranges</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getParamValues <em>Param Values</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.Model#getAssignments <em>Assignments</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface Model extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Sse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sse</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sse</em>' attribute.
	 * @see #setSse(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_Sse()
	 * @model
	 * @generated
	 */
	Double getSse();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getSse <em>Sse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sse</em>' attribute.
	 * @see #getSse()
	 * @generated
	 */
	void setSse(Double value);

	/**
	 * Returns the value of the '<em><b>Mse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mse</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mse</em>' attribute.
	 * @see #setMse(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_Mse()
	 * @model
	 * @generated
	 */
	Double getMse();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getMse <em>Mse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mse</em>' attribute.
	 * @see #getMse()
	 * @generated
	 */
	void setMse(Double value);

	/**
	 * Returns the value of the '<em><b>Rmse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rmse</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rmse</em>' attribute.
	 * @see #setRmse(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_Rmse()
	 * @model
	 * @generated
	 */
	Double getRmse();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getRmse <em>Rmse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rmse</em>' attribute.
	 * @see #getRmse()
	 * @generated
	 */
	void setRmse(Double value);

	/**
	 * Returns the value of the '<em><b>R2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>R2</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>R2</em>' attribute.
	 * @see #setR2(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_R2()
	 * @model
	 * @generated
	 */
	Double getR2();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getR2 <em>R2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>R2</em>' attribute.
	 * @see #getR2()
	 * @generated
	 */
	void setR2(Double value);

	/**
	 * Returns the value of the '<em><b>Aic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Aic</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Aic</em>' attribute.
	 * @see #setAic(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_Aic()
	 * @model
	 * @generated
	 */
	Double getAic();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getAic <em>Aic</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Aic</em>' attribute.
	 * @see #getAic()
	 * @generated
	 */
	void setAic(Double value);

	/**
	 * Returns the value of the '<em><b>Degrees Of Freedom</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Degrees Of Freedom</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Degrees Of Freedom</em>' attribute.
	 * @see #setDegreesOfFreedom(Integer)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_DegreesOfFreedom()
	 * @model
	 * @generated
	 */
	Integer getDegreesOfFreedom();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getDegreesOfFreedom <em>Degrees Of Freedom</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Degrees Of Freedom</em>' attribute.
	 * @see #getDegreesOfFreedom()
	 * @generated
	 */
	void setDegreesOfFreedom(Integer value);

	/**
	 * Returns the value of the '<em><b>Variable Ranges</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link de.bund.bfr.knime.pmmlite.core.models.VariableRange},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Variable Ranges</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Variable Ranges</em>' map.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_VariableRanges()
	 * @model mapType="de.bund.bfr.knime.pmmlite.core.models.StringToVariableRangeMapEntry<org.eclipse.emf.ecore.EString, de.bund.bfr.knime.pmmlite.core.models.VariableRange>"
	 * @generated
	 */
	EMap<String, VariableRange> getVariableRanges();

	/**
	 * Returns the value of the '<em><b>Param Values</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Param Values</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Param Values</em>' map.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_ParamValues()
	 * @model mapType="de.bund.bfr.knime.pmmlite.core.models.StringToParameterValueMapEntry<org.eclipse.emf.ecore.EString, de.bund.bfr.knime.pmmlite.core.models.ParameterValue>"
	 * @generated
	 */
	EMap<String, ParameterValue> getParamValues();

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
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getModel_Assignments()
	 * @model mapType="de.bund.bfr.knime.pmmlite.core.models.StringToStringMapEntry<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString>"
	 * @generated
	 */
	EMap<String, String> getAssignments();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	ModelFormula getFormula();

} // Model
