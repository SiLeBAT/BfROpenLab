/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.core.models.impl;

import de.bund.bfr.knime.pmmlite.core.common.CommonFactory;
import de.bund.bfr.knime.pmmlite.core.common.CommonPackage;

import de.bund.bfr.knime.pmmlite.core.models.ModelsPackage;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;

import de.bund.bfr.math.Transform;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Secondary Model Formula</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl#getExpression <em>Expression</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl#getDepVar <em>Dep Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl#getParams <em>Params</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl#getIndepVars <em>Indep Vars</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl#getTransformation <em>Transformation</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SecondaryModelFormulaImpl extends EObjectImpl implements SecondaryModelFormula {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getExpression() <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExpression()
	 * @generated
	 * @ordered
	 */
	protected static final String EXPRESSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getExpression() <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExpression()
	 * @generated
	 * @ordered
	 */
	protected String expression = EXPRESSION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getDepVar() <em>Dep Var</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDepVar()
	 * @generated
	 * @ordered
	 */
	protected Variable depVar;

	/**
	 * The cached value of the '{@link #getParams() <em>Params</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParams()
	 * @generated
	 * @ordered
	 */
	protected EList<Parameter> params;

	/**
	 * The cached value of the '{@link #getIndepVars() <em>Indep Vars</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndepVars()
	 * @generated
	 * @ordered
	 */
	protected EList<Variable> indepVars;

	/**
	 * The default value of the '{@link #getTransformation() <em>Transformation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTransformation()
	 * @generated
	 * @ordered
	 */
	protected static final Transform TRANSFORMATION_EDEFAULT = (Transform)CommonFactory.eINSTANCE.createFromString(CommonPackage.eINSTANCE.getTransform(), "NO_TRANSFORM");

	/**
	 * The cached value of the '{@link #getTransformation() <em>Transformation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTransformation()
	 * @generated
	 * @ordered
	 */
	protected Transform transformation = TRANSFORMATION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SecondaryModelFormulaImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelsPackage.Literals.SECONDARY_MODEL_FORMULA;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.SECONDARY_MODEL_FORMULA__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.SECONDARY_MODEL_FORMULA__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExpression(String newExpression) {
		String oldExpression = expression;
		expression = newExpression;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.SECONDARY_MODEL_FORMULA__EXPRESSION, oldExpression, expression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Variable getDepVar() {
		return depVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDepVar(Variable newDepVar, NotificationChain msgs) {
		Variable oldDepVar = depVar;
		depVar = newDepVar;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR, oldDepVar, newDepVar);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDepVar(Variable newDepVar) {
		if (newDepVar != depVar) {
			NotificationChain msgs = null;
			if (depVar != null)
				msgs = ((InternalEObject)depVar).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR, null, msgs);
			if (newDepVar != null)
				msgs = ((InternalEObject)newDepVar).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR, null, msgs);
			msgs = basicSetDepVar(newDepVar, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR, newDepVar, newDepVar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Parameter> getParams() {
		if (params == null) {
			params = new EObjectContainmentEList<Parameter>(Parameter.class, this, ModelsPackage.SECONDARY_MODEL_FORMULA__PARAMS);
		}
		return params;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Variable> getIndepVars() {
		if (indepVars == null) {
			indepVars = new EObjectContainmentEList<Variable>(Variable.class, this, ModelsPackage.SECONDARY_MODEL_FORMULA__INDEP_VARS);
		}
		return indepVars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Transform getTransformation() {
		return transformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTransformation(Transform newTransformation) {
		Transform oldTransformation = transformation;
		transformation = newTransformation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.SECONDARY_MODEL_FORMULA__TRANSFORMATION, oldTransformation, transformation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR:
				return basicSetDepVar(null, msgs);
			case ModelsPackage.SECONDARY_MODEL_FORMULA__PARAMS:
				return ((InternalEList<?>)getParams()).basicRemove(otherEnd, msgs);
			case ModelsPackage.SECONDARY_MODEL_FORMULA__INDEP_VARS:
				return ((InternalEList<?>)getIndepVars()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelsPackage.SECONDARY_MODEL_FORMULA__NAME:
				return getName();
			case ModelsPackage.SECONDARY_MODEL_FORMULA__ID:
				return getId();
			case ModelsPackage.SECONDARY_MODEL_FORMULA__EXPRESSION:
				return getExpression();
			case ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR:
				return getDepVar();
			case ModelsPackage.SECONDARY_MODEL_FORMULA__PARAMS:
				return getParams();
			case ModelsPackage.SECONDARY_MODEL_FORMULA__INDEP_VARS:
				return getIndepVars();
			case ModelsPackage.SECONDARY_MODEL_FORMULA__TRANSFORMATION:
				return getTransformation();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelsPackage.SECONDARY_MODEL_FORMULA__NAME:
				setName((String)newValue);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__ID:
				setId((String)newValue);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__EXPRESSION:
				setExpression((String)newValue);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR:
				setDepVar((Variable)newValue);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__PARAMS:
				getParams().clear();
				getParams().addAll((Collection<? extends Parameter>)newValue);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__INDEP_VARS:
				getIndepVars().clear();
				getIndepVars().addAll((Collection<? extends Variable>)newValue);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__TRANSFORMATION:
				setTransformation((Transform)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ModelsPackage.SECONDARY_MODEL_FORMULA__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__ID:
				setId(ID_EDEFAULT);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__EXPRESSION:
				setExpression(EXPRESSION_EDEFAULT);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR:
				setDepVar((Variable)null);
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__PARAMS:
				getParams().clear();
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__INDEP_VARS:
				getIndepVars().clear();
				return;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__TRANSFORMATION:
				setTransformation(TRANSFORMATION_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ModelsPackage.SECONDARY_MODEL_FORMULA__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ModelsPackage.SECONDARY_MODEL_FORMULA__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ModelsPackage.SECONDARY_MODEL_FORMULA__EXPRESSION:
				return EXPRESSION_EDEFAULT == null ? expression != null : !EXPRESSION_EDEFAULT.equals(expression);
			case ModelsPackage.SECONDARY_MODEL_FORMULA__DEP_VAR:
				return depVar != null;
			case ModelsPackage.SECONDARY_MODEL_FORMULA__PARAMS:
				return params != null && !params.isEmpty();
			case ModelsPackage.SECONDARY_MODEL_FORMULA__INDEP_VARS:
				return indepVars != null && !indepVars.isEmpty();
			case ModelsPackage.SECONDARY_MODEL_FORMULA__TRANSFORMATION:
				return TRANSFORMATION_EDEFAULT == null ? transformation != null : !TRANSFORMATION_EDEFAULT.equals(transformation);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", id: ");
		result.append(id);
		result.append(", expression: ");
		result.append(expression);
		result.append(", transformation: ");
		result.append(transformation);
		result.append(')');
		return result.toString();
	}

} //SecondaryModelFormulaImpl
