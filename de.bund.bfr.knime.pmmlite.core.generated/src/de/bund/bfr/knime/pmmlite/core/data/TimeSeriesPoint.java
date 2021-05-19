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
package de.bund.bfr.knime.pmmlite.core.data;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Time Series Point</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint#getTime <em>Time</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint#getConcentration <em>Concentration</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeriesPoint()
 * @model
 * @generated
 */
public interface TimeSeriesPoint extends EObject {
	/**
	 * Returns the value of the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Time</em>' attribute.
	 * @see #setTime(double)
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeriesPoint_Time()
	 * @model
	 * @generated
	 */
	double getTime();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint#getTime <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Time</em>' attribute.
	 * @see #getTime()
	 * @generated
	 */
	void setTime(double value);

	/**
	 * Returns the value of the '<em><b>Concentration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Concentration</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Concentration</em>' attribute.
	 * @see #setConcentration(double)
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeriesPoint_Concentration()
	 * @model
	 * @generated
	 */
	double getConcentration();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint#getConcentration <em>Concentration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Concentration</em>' attribute.
	 * @see #getConcentration()
	 * @generated
	 */
	void setConcentration(double value);

} // TimeSeriesPoint
