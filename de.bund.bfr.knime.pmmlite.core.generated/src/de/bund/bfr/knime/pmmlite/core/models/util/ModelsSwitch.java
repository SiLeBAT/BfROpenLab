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
package de.bund.bfr.knime.pmmlite.core.models.util;

import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.common.Nameable;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;

import de.bund.bfr.knime.pmmlite.core.models.*;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage
 * @generated
 */
public class ModelsSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ModelsPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelsSwitch() {
		if (modelPackage == null) {
			modelPackage = ModelsPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case ModelsPackage.MODEL: {
				Model model = (Model)theEObject;
				T result = caseModel(model);
				if (result == null) result = caseIdentifiable(model);
				if (result == null) result = caseNameable(model);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.MODEL_FORMULA: {
				ModelFormula modelFormula = (ModelFormula)theEObject;
				T result = caseModelFormula(modelFormula);
				if (result == null) result = caseIdentifiable(modelFormula);
				if (result == null) result = caseNameable(modelFormula);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.VARIABLE: {
				Variable variable = (Variable)theEObject;
				T result = caseVariable(variable);
				if (result == null) result = caseNameableWithUnit(variable);
				if (result == null) result = caseNameable(variable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.VARIABLE_RANGE: {
				VariableRange variableRange = (VariableRange)theEObject;
				T result = caseVariableRange(variableRange);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.PARAMETER: {
				Parameter parameter = (Parameter)theEObject;
				T result = caseParameter(parameter);
				if (result == null) result = caseNameable(parameter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.PARAMETER_VALUE: {
				ParameterValue parameterValue = (ParameterValue)theEObject;
				T result = caseParameterValue(parameterValue);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.PRIMARY_MODEL: {
				PrimaryModel primaryModel = (PrimaryModel)theEObject;
				T result = casePrimaryModel(primaryModel);
				if (result == null) result = caseModel(primaryModel);
				if (result == null) result = caseIdentifiable(primaryModel);
				if (result == null) result = caseNameable(primaryModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.SECONDARY_MODEL: {
				SecondaryModel secondaryModel = (SecondaryModel)theEObject;
				T result = caseSecondaryModel(secondaryModel);
				if (result == null) result = caseModel(secondaryModel);
				if (result == null) result = caseIdentifiable(secondaryModel);
				if (result == null) result = caseNameable(secondaryModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.TERTIARY_MODEL: {
				TertiaryModel tertiaryModel = (TertiaryModel)theEObject;
				T result = caseTertiaryModel(tertiaryModel);
				if (result == null) result = caseModel(tertiaryModel);
				if (result == null) result = caseIdentifiable(tertiaryModel);
				if (result == null) result = caseNameable(tertiaryModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.PRIMARY_MODEL_FORMULA: {
				PrimaryModelFormula primaryModelFormula = (PrimaryModelFormula)theEObject;
				T result = casePrimaryModelFormula(primaryModelFormula);
				if (result == null) result = caseModelFormula(primaryModelFormula);
				if (result == null) result = caseIdentifiable(primaryModelFormula);
				if (result == null) result = caseNameable(primaryModelFormula);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.SECONDARY_MODEL_FORMULA: {
				SecondaryModelFormula secondaryModelFormula = (SecondaryModelFormula)theEObject;
				T result = caseSecondaryModelFormula(secondaryModelFormula);
				if (result == null) result = caseModelFormula(secondaryModelFormula);
				if (result == null) result = caseIdentifiable(secondaryModelFormula);
				if (result == null) result = caseNameable(secondaryModelFormula);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.TERTIARY_MODEL_FORMULA: {
				TertiaryModelFormula tertiaryModelFormula = (TertiaryModelFormula)theEObject;
				T result = caseTertiaryModelFormula(tertiaryModelFormula);
				if (result == null) result = caseModelFormula(tertiaryModelFormula);
				if (result == null) result = caseIdentifiable(tertiaryModelFormula);
				if (result == null) result = caseNameable(tertiaryModelFormula);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.RENAMINGS: {
				Renamings renamings = (Renamings)theEObject;
				T result = caseRenamings(renamings);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.STRING_TO_STRING_MAP_ENTRY: {
				@SuppressWarnings("unchecked") Map.Entry<String, String> stringToStringMapEntry = (Map.Entry<String, String>)theEObject;
				T result = caseStringToStringMapEntry(stringToStringMapEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.STRING_TO_DOUBLE_MAP_ENTRY: {
				@SuppressWarnings("unchecked") Map.Entry<String, Double> stringToDoubleMapEntry = (Map.Entry<String, Double>)theEObject;
				T result = caseStringToDoubleMapEntry(stringToDoubleMapEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.STRING_TO_VARIABLE_RANGE_MAP_ENTRY: {
				@SuppressWarnings("unchecked") Map.Entry<String, VariableRange> stringToVariableRangeMapEntry = (Map.Entry<String, VariableRange>)theEObject;
				T result = caseStringToVariableRangeMapEntry(stringToVariableRangeMapEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.STRING_TO_PARAMETER_VALUE_MAP_ENTRY: {
				@SuppressWarnings("unchecked") Map.Entry<String, ParameterValue> stringToParameterValueMapEntry = (Map.Entry<String, ParameterValue>)theEObject;
				T result = caseStringToParameterValueMapEntry(stringToParameterValueMapEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelsPackage.STRING_TO_RENAMINGS_MAP_ENTRY: {
				@SuppressWarnings("unchecked") Map.Entry<String, Renamings> stringToRenamingsMapEntry = (Map.Entry<String, Renamings>)theEObject;
				T result = caseStringToRenamingsMapEntry(stringToRenamingsMapEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseModel(Model object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Model Formula</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Model Formula</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseModelFormula(ModelFormula object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Variable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Variable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseVariable(Variable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Variable Range</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Variable Range</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseVariableRange(VariableRange object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Parameter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseParameter(Parameter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Parameter Value</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Parameter Value</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseParameterValue(ParameterValue object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Primary Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Primary Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePrimaryModel(PrimaryModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Secondary Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Secondary Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSecondaryModel(SecondaryModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tertiary Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tertiary Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTertiaryModel(TertiaryModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Primary Model Formula</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Primary Model Formula</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePrimaryModelFormula(PrimaryModelFormula object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Secondary Model Formula</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Secondary Model Formula</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSecondaryModelFormula(SecondaryModelFormula object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tertiary Model Formula</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tertiary Model Formula</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTertiaryModelFormula(TertiaryModelFormula object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Renamings</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Renamings</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRenamings(Renamings object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>String To String Map Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>String To String Map Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStringToStringMapEntry(Map.Entry<String, String> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>String To Double Map Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>String To Double Map Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStringToDoubleMapEntry(Map.Entry<String, Double> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>String To Variable Range Map Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>String To Variable Range Map Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStringToVariableRangeMapEntry(Map.Entry<String, VariableRange> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>String To Parameter Value Map Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>String To Parameter Value Map Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStringToParameterValueMapEntry(Map.Entry<String, ParameterValue> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>String To Renamings Map Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>String To Renamings Map Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStringToRenamingsMapEntry(Map.Entry<String, Renamings> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Nameable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Nameable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNameable(Nameable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Identifiable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Identifiable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIdentifiable(Identifiable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Nameable With Unit</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Nameable With Unit</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNameableWithUnit(NameableWithUnit object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //ModelsSwitch
