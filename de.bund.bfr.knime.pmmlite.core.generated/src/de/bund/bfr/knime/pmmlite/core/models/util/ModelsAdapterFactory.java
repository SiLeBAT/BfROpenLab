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
package de.bund.bfr.knime.pmmlite.core.models.util;

import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.common.Nameable;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;

import de.bund.bfr.knime.pmmlite.core.models.*;

import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage
 * @generated
 */
public class ModelsAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ModelsPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelsAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = ModelsPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelsSwitch<Adapter> modelSwitch =
		new ModelsSwitch<Adapter>() {
			@Override
			public Adapter caseModel(Model object) {
				return createModelAdapter();
			}
			@Override
			public Adapter caseModelFormula(ModelFormula object) {
				return createModelFormulaAdapter();
			}
			@Override
			public Adapter caseVariable(Variable object) {
				return createVariableAdapter();
			}
			@Override
			public Adapter caseVariableRange(VariableRange object) {
				return createVariableRangeAdapter();
			}
			@Override
			public Adapter caseParameter(Parameter object) {
				return createParameterAdapter();
			}
			@Override
			public Adapter caseParameterValue(ParameterValue object) {
				return createParameterValueAdapter();
			}
			@Override
			public Adapter casePrimaryModel(PrimaryModel object) {
				return createPrimaryModelAdapter();
			}
			@Override
			public Adapter caseSecondaryModel(SecondaryModel object) {
				return createSecondaryModelAdapter();
			}
			@Override
			public Adapter caseTertiaryModel(TertiaryModel object) {
				return createTertiaryModelAdapter();
			}
			@Override
			public Adapter casePrimaryModelFormula(PrimaryModelFormula object) {
				return createPrimaryModelFormulaAdapter();
			}
			@Override
			public Adapter caseSecondaryModelFormula(SecondaryModelFormula object) {
				return createSecondaryModelFormulaAdapter();
			}
			@Override
			public Adapter caseTertiaryModelFormula(TertiaryModelFormula object) {
				return createTertiaryModelFormulaAdapter();
			}
			@Override
			public Adapter caseRenamings(Renamings object) {
				return createRenamingsAdapter();
			}
			@Override
			public Adapter caseStringToStringMapEntry(Map.Entry<String, String> object) {
				return createStringToStringMapEntryAdapter();
			}
			@Override
			public Adapter caseStringToDoubleMapEntry(Map.Entry<String, Double> object) {
				return createStringToDoubleMapEntryAdapter();
			}
			@Override
			public Adapter caseStringToVariableRangeMapEntry(Map.Entry<String, VariableRange> object) {
				return createStringToVariableRangeMapEntryAdapter();
			}
			@Override
			public Adapter caseStringToParameterValueMapEntry(Map.Entry<String, ParameterValue> object) {
				return createStringToParameterValueMapEntryAdapter();
			}
			@Override
			public Adapter caseStringToRenamingsMapEntry(Map.Entry<String, Renamings> object) {
				return createStringToRenamingsMapEntryAdapter();
			}
			@Override
			public Adapter caseNameable(Nameable object) {
				return createNameableAdapter();
			}
			@Override
			public Adapter caseIdentifiable(Identifiable object) {
				return createIdentifiableAdapter();
			}
			@Override
			public Adapter caseNameableWithUnit(NameableWithUnit object) {
				return createNameableWithUnitAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.Model <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model
	 * @generated
	 */
	public Adapter createModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula <em>Model Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelFormula
	 * @generated
	 */
	public Adapter createModelFormulaAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.Variable <em>Variable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Variable
	 * @generated
	 */
	public Adapter createVariableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange <em>Variable Range</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.VariableRange
	 * @generated
	 */
	public Adapter createVariableRangeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.Parameter <em>Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Parameter
	 * @generated
	 */
	public Adapter createParameterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue <em>Parameter Value</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ParameterValue
	 * @generated
	 */
	public Adapter createParameterValueAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel <em>Primary Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.PrimaryModel
	 * @generated
	 */
	public Adapter createPrimaryModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModel <em>Secondary Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.SecondaryModel
	 * @generated
	 */
	public Adapter createSecondaryModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModel <em>Tertiary Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModel
	 * @generated
	 */
	public Adapter createTertiaryModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula <em>Primary Model Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula
	 * @generated
	 */
	public Adapter createPrimaryModelFormulaAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula <em>Secondary Model Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula
	 * @generated
	 */
	public Adapter createSecondaryModelFormulaAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula <em>Tertiary Model Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula
	 * @generated
	 */
	public Adapter createTertiaryModelFormulaAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.models.Renamings <em>Renamings</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Renamings
	 * @generated
	 */
	public Adapter createRenamingsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>String To String Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.util.Map.Entry
	 * @generated
	 */
	public Adapter createStringToStringMapEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>String To Double Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.util.Map.Entry
	 * @generated
	 */
	public Adapter createStringToDoubleMapEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>String To Variable Range Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.util.Map.Entry
	 * @generated
	 */
	public Adapter createStringToVariableRangeMapEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>String To Parameter Value Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.util.Map.Entry
	 * @generated
	 */
	public Adapter createStringToParameterValueMapEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>String To Renamings Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.util.Map.Entry
	 * @generated
	 */
	public Adapter createStringToRenamingsMapEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.common.Nameable <em>Nameable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.common.Nameable
	 * @generated
	 */
	public Adapter createNameableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.common.Identifiable <em>Identifiable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.common.Identifiable
	 * @generated
	 */
	public Adapter createIdentifiableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit <em>Nameable With Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit
	 * @generated
	 */
	public Adapter createNameableWithUnitAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //ModelsAdapterFactory
