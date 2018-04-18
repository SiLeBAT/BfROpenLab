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

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage
 * @generated
 */
public interface ModelsFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelsFactory eINSTANCE = de.bund.bfr.knime.pmmlite.core.models.impl.ModelsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Variable</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Variable</em>'.
	 * @generated
	 */
	Variable createVariable();

	/**
	 * Returns a new object of class '<em>Variable Range</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Variable Range</em>'.
	 * @generated
	 */
	VariableRange createVariableRange();

	/**
	 * Returns a new object of class '<em>Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Parameter</em>'.
	 * @generated
	 */
	Parameter createParameter();

	/**
	 * Returns a new object of class '<em>Parameter Value</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Parameter Value</em>'.
	 * @generated
	 */
	ParameterValue createParameterValue();

	/**
	 * Returns a new object of class '<em>Primary Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Primary Model</em>'.
	 * @generated
	 */
	PrimaryModel createPrimaryModel();

	/**
	 * Returns a new object of class '<em>Secondary Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Secondary Model</em>'.
	 * @generated
	 */
	SecondaryModel createSecondaryModel();

	/**
	 * Returns a new object of class '<em>Tertiary Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tertiary Model</em>'.
	 * @generated
	 */
	TertiaryModel createTertiaryModel();

	/**
	 * Returns a new object of class '<em>Primary Model Formula</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Primary Model Formula</em>'.
	 * @generated
	 */
	PrimaryModelFormula createPrimaryModelFormula();

	/**
	 * Returns a new object of class '<em>Secondary Model Formula</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Secondary Model Formula</em>'.
	 * @generated
	 */
	SecondaryModelFormula createSecondaryModelFormula();

	/**
	 * Returns a new object of class '<em>Tertiary Model Formula</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tertiary Model Formula</em>'.
	 * @generated
	 */
	TertiaryModelFormula createTertiaryModelFormula();

	/**
	 * Returns a new object of class '<em>Renamings</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Renamings</em>'.
	 * @generated
	 */
	Renamings createRenamings();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ModelsPackage getModelsPackage();

} //ModelsFactory
