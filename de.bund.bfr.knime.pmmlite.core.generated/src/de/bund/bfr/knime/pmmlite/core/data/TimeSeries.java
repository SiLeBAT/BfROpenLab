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
package de.bund.bfr.knime.pmmlite.core.data;

import de.bund.bfr.knime.pmmlite.core.PmmUnit;

import de.bund.bfr.knime.pmmlite.core.common.Identifiable;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Time Series</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getPoints <em>Points</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getConditions <em>Conditions</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getOrganism <em>Organism</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getMatrix <em>Matrix</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getTimeUnit <em>Time Unit</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getConcentrationUnit <em>Concentration Unit</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeries()
 * @model
 * @generated
 */
public interface TimeSeries extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Points</b></em>' containment reference list.
	 * The list contents are of type {@link de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Points</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Points</em>' containment reference list.
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeries_Points()
	 * @model containment="true"
	 * @generated
	 */
	EList<TimeSeriesPoint> getPoints();

	/**
	 * Returns the value of the '<em><b>Conditions</b></em>' containment reference list.
	 * The list contents are of type {@link de.bund.bfr.knime.pmmlite.core.data.Condition}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Conditions</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Conditions</em>' containment reference list.
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeries_Conditions()
	 * @model containment="true"
	 * @generated
	 */
	EList<Condition> getConditions();

	/**
	 * Returns the value of the '<em><b>Organism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Organism</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Organism</em>' attribute.
	 * @see #setOrganism(String)
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeries_Organism()
	 * @model
	 * @generated
	 */
	String getOrganism();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getOrganism <em>Organism</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Organism</em>' attribute.
	 * @see #getOrganism()
	 * @generated
	 */
	void setOrganism(String value);

	/**
	 * Returns the value of the '<em><b>Matrix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Matrix</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Matrix</em>' attribute.
	 * @see #setMatrix(String)
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeries_Matrix()
	 * @model
	 * @generated
	 */
	String getMatrix();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getMatrix <em>Matrix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Matrix</em>' attribute.
	 * @see #getMatrix()
	 * @generated
	 */
	void setMatrix(String value);

	/**
	 * Returns the value of the '<em><b>Time Unit</b></em>' attribute.
	 * The default value is <code>"NO_TRANSFORM(NO_UNIT)"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Time Unit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Time Unit</em>' attribute.
	 * @see #setTimeUnit(PmmUnit)
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeries_TimeUnit()
	 * @model default="NO_TRANSFORM(NO_UNIT)" dataType="de.bund.bfr.knime.pmmlite.core.common.Unit"
	 * @generated
	 */
	PmmUnit getTimeUnit();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getTimeUnit <em>Time Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Time Unit</em>' attribute.
	 * @see #getTimeUnit()
	 * @generated
	 */
	void setTimeUnit(PmmUnit value);

	/**
	 * Returns the value of the '<em><b>Concentration Unit</b></em>' attribute.
	 * The default value is <code>"NO_TRANSFORM(NO_UNIT)"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Concentration Unit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Concentration Unit</em>' attribute.
	 * @see #setConcentrationUnit(PmmUnit)
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getTimeSeries_ConcentrationUnit()
	 * @model default="NO_TRANSFORM(NO_UNIT)" dataType="de.bund.bfr.knime.pmmlite.core.common.Unit"
	 * @generated
	 */
	PmmUnit getConcentrationUnit();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getConcentrationUnit <em>Concentration Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Concentration Unit</em>' attribute.
	 * @see #getConcentrationUnit()
	 * @generated
	 */
	void setConcentrationUnit(PmmUnit value);

} // TimeSeries
