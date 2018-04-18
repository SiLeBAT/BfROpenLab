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
package de.bund.bfr.knime.pmmlite.core.data;

import de.bund.bfr.knime.pmmlite.core.common.CommonPackage;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see de.bund.bfr.knime.pmmlite.core.data.DataFactory
 * @model kind="package"
 * @generated
 */
public interface DataPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "data";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///de/bund/bfr/knime/pmmlite/core/data.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "de.bund.bfr.knime.pmmlite.core.data";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DataPackage eINSTANCE = de.bund.bfr.knime.pmmlite.core.data.impl.DataPackageImpl.init();

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl <em>Time Series</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl
	 * @see de.bund.bfr.knime.pmmlite.core.data.impl.DataPackageImpl#getTimeSeries()
	 * @generated
	 */
	int TIME_SERIES = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES__NAME = CommonPackage.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES__ID = CommonPackage.IDENTIFIABLE__ID;

	/**
	 * The feature id for the '<em><b>Points</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES__POINTS = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Conditions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES__CONDITIONS = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Organism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES__ORGANISM = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Matrix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES__MATRIX = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Time Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES__TIME_UNIT = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Concentration Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES__CONCENTRATION_UNIT = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Time Series</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES_FEATURE_COUNT = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesPointImpl <em>Time Series Point</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesPointImpl
	 * @see de.bund.bfr.knime.pmmlite.core.data.impl.DataPackageImpl#getTimeSeriesPoint()
	 * @generated
	 */
	int TIME_SERIES_POINT = 1;

	/**
	 * The feature id for the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES_POINT__TIME = 0;

	/**
	 * The feature id for the '<em><b>Concentration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES_POINT__CONCENTRATION = 1;

	/**
	 * The number of structural features of the '<em>Time Series Point</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIME_SERIES_POINT_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.data.impl.ConditionImpl <em>Condition</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.data.impl.ConditionImpl
	 * @see de.bund.bfr.knime.pmmlite.core.data.impl.DataPackageImpl#getCondition()
	 * @generated
	 */
	int CONDITION = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONDITION__NAME = CommonPackage.NAMEABLE_WITH_UNIT__NAME;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONDITION__UNIT = CommonPackage.NAMEABLE_WITH_UNIT__UNIT;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONDITION__VALUE = CommonPackage.NAMEABLE_WITH_UNIT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Condition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONDITION_FEATURE_COUNT = CommonPackage.NAMEABLE_WITH_UNIT_FEATURE_COUNT + 1;


	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries <em>Time Series</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Time Series</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeries
	 * @generated
	 */
	EClass getTimeSeries();

	/**
	 * Returns the meta object for the containment reference list '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getPoints <em>Points</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Points</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getPoints()
	 * @see #getTimeSeries()
	 * @generated
	 */
	EReference getTimeSeries_Points();

	/**
	 * Returns the meta object for the containment reference list '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getConditions <em>Conditions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Conditions</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getConditions()
	 * @see #getTimeSeries()
	 * @generated
	 */
	EReference getTimeSeries_Conditions();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getOrganism <em>Organism</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Organism</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getOrganism()
	 * @see #getTimeSeries()
	 * @generated
	 */
	EAttribute getTimeSeries_Organism();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getMatrix <em>Matrix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Matrix</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getMatrix()
	 * @see #getTimeSeries()
	 * @generated
	 */
	EAttribute getTimeSeries_Matrix();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getTimeUnit <em>Time Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time Unit</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getTimeUnit()
	 * @see #getTimeSeries()
	 * @generated
	 */
	EAttribute getTimeSeries_TimeUnit();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getConcentrationUnit <em>Concentration Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Concentration Unit</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeries#getConcentrationUnit()
	 * @see #getTimeSeries()
	 * @generated
	 */
	EAttribute getTimeSeries_ConcentrationUnit();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint <em>Time Series Point</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Time Series Point</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint
	 * @generated
	 */
	EClass getTimeSeriesPoint();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint#getTime <em>Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint#getTime()
	 * @see #getTimeSeriesPoint()
	 * @generated
	 */
	EAttribute getTimeSeriesPoint_Time();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint#getConcentration <em>Concentration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Concentration</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint#getConcentration()
	 * @see #getTimeSeriesPoint()
	 * @generated
	 */
	EAttribute getTimeSeriesPoint_Concentration();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.data.Condition <em>Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Condition</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.Condition
	 * @generated
	 */
	EClass getCondition();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.data.Condition#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.data.Condition#getValue()
	 * @see #getCondition()
	 * @generated
	 */
	EAttribute getCondition_Value();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	DataFactory getDataFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl <em>Time Series</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl
		 * @see de.bund.bfr.knime.pmmlite.core.data.impl.DataPackageImpl#getTimeSeries()
		 * @generated
		 */
		EClass TIME_SERIES = eINSTANCE.getTimeSeries();

		/**
		 * The meta object literal for the '<em><b>Points</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TIME_SERIES__POINTS = eINSTANCE.getTimeSeries_Points();

		/**
		 * The meta object literal for the '<em><b>Conditions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TIME_SERIES__CONDITIONS = eINSTANCE.getTimeSeries_Conditions();

		/**
		 * The meta object literal for the '<em><b>Organism</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIME_SERIES__ORGANISM = eINSTANCE.getTimeSeries_Organism();

		/**
		 * The meta object literal for the '<em><b>Matrix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIME_SERIES__MATRIX = eINSTANCE.getTimeSeries_Matrix();

		/**
		 * The meta object literal for the '<em><b>Time Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIME_SERIES__TIME_UNIT = eINSTANCE.getTimeSeries_TimeUnit();

		/**
		 * The meta object literal for the '<em><b>Concentration Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIME_SERIES__CONCENTRATION_UNIT = eINSTANCE.getTimeSeries_ConcentrationUnit();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesPointImpl <em>Time Series Point</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesPointImpl
		 * @see de.bund.bfr.knime.pmmlite.core.data.impl.DataPackageImpl#getTimeSeriesPoint()
		 * @generated
		 */
		EClass TIME_SERIES_POINT = eINSTANCE.getTimeSeriesPoint();

		/**
		 * The meta object literal for the '<em><b>Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIME_SERIES_POINT__TIME = eINSTANCE.getTimeSeriesPoint_Time();

		/**
		 * The meta object literal for the '<em><b>Concentration</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIME_SERIES_POINT__CONCENTRATION = eINSTANCE.getTimeSeriesPoint_Concentration();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.data.impl.ConditionImpl <em>Condition</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.data.impl.ConditionImpl
		 * @see de.bund.bfr.knime.pmmlite.core.data.impl.DataPackageImpl#getCondition()
		 * @generated
		 */
		EClass CONDITION = eINSTANCE.getCondition();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONDITION__VALUE = eINSTANCE.getCondition_Value();

	}

} //DataPackage
