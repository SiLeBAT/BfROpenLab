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
package de.bund.bfr.knime.pmmlite.core.common;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;

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
 * @see de.bund.bfr.knime.pmmlite.core.common.CommonFactory
 * @model kind="package"
 * @generated
 */
public interface CommonPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "common";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///de/bund/bfr/knime/pmmlite/core/common.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "de.bund.bfr.knime.pmmlite.core.common";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	CommonPackage eINSTANCE = de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl.init();

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.common.Nameable <em>Nameable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.common.Nameable
	 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getNameable()
	 * @generated
	 */
	int NAMEABLE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMEABLE__NAME = 0;

	/**
	 * The number of structural features of the '<em>Nameable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMEABLE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.common.Identifiable <em>Identifiable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.common.Identifiable
	 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getIdentifiable()
	 * @generated
	 */
	int IDENTIFIABLE = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE__NAME = NAMEABLE__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE__ID = NAMEABLE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Identifiable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE_FEATURE_COUNT = NAMEABLE_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit <em>Nameable With Unit</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit
	 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getNameableWithUnit()
	 * @generated
	 */
	int NAMEABLE_WITH_UNIT = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMEABLE_WITH_UNIT__NAME = NAMEABLE__NAME;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMEABLE_WITH_UNIT__UNIT = NAMEABLE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Nameable With Unit</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMEABLE_WITH_UNIT_FEATURE_COUNT = NAMEABLE_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '<em>Unit</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.PmmUnit
	 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getUnit()
	 * @generated
	 */
	int UNIT = 3;

	/**
	 * The meta object id for the '<em>Transform</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.math.Transform
	 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getTransform()
	 * @generated
	 */
	int TRANSFORM = 4;


	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.common.Nameable <em>Nameable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Nameable</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.common.Nameable
	 * @generated
	 */
	EClass getNameable();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.common.Nameable#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.common.Nameable#getName()
	 * @see #getNameable()
	 * @generated
	 */
	EAttribute getNameable_Name();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.common.Identifiable <em>Identifiable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Identifiable</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.common.Identifiable
	 * @generated
	 */
	EClass getIdentifiable();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.common.Identifiable#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.common.Identifiable#getId()
	 * @see #getIdentifiable()
	 * @generated
	 */
	EAttribute getIdentifiable_Id();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit <em>Nameable With Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Nameable With Unit</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit
	 * @generated
	 */
	EClass getNameableWithUnit();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit#getUnit <em>Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit#getUnit()
	 * @see #getNameableWithUnit()
	 * @generated
	 */
	EAttribute getNameableWithUnit_Unit();

	/**
	 * Returns the meta object for data type '{@link de.bund.bfr.knime.pmmlite.core.PmmUnit <em>Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Unit</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.PmmUnit
	 * @model instanceClass="de.bund.bfr.knime.pmmlite.core.PmmUnit"
	 * @generated
	 */
	EDataType getUnit();

	/**
	 * Returns the meta object for data type '{@link de.bund.bfr.math.Transform <em>Transform</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Transform</em>'.
	 * @see de.bund.bfr.math.Transform
	 * @model instanceClass="de.bund.bfr.math.Transform"
	 * @generated
	 */
	EDataType getTransform();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	CommonFactory getCommonFactory();

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
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.common.Nameable <em>Nameable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.common.Nameable
		 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getNameable()
		 * @generated
		 */
		EClass NAMEABLE = eINSTANCE.getNameable();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NAMEABLE__NAME = eINSTANCE.getNameable_Name();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.common.Identifiable <em>Identifiable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.common.Identifiable
		 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getIdentifiable()
		 * @generated
		 */
		EClass IDENTIFIABLE = eINSTANCE.getIdentifiable();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDENTIFIABLE__ID = eINSTANCE.getIdentifiable_Id();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit <em>Nameable With Unit</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit
		 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getNameableWithUnit()
		 * @generated
		 */
		EClass NAMEABLE_WITH_UNIT = eINSTANCE.getNameableWithUnit();

		/**
		 * The meta object literal for the '<em><b>Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NAMEABLE_WITH_UNIT__UNIT = eINSTANCE.getNameableWithUnit_Unit();

		/**
		 * The meta object literal for the '<em>Unit</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.PmmUnit
		 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getUnit()
		 * @generated
		 */
		EDataType UNIT = eINSTANCE.getUnit();

		/**
		 * The meta object literal for the '<em>Transform</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.math.Transform
		 * @see de.bund.bfr.knime.pmmlite.core.common.impl.CommonPackageImpl#getTransform()
		 * @generated
		 */
		EDataType TRANSFORM = eINSTANCE.getTransform();

	}

} //CommonPackage
