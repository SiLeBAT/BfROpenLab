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
package de.bund.bfr.knime.pmmlite.core.models.impl;

import de.bund.bfr.knime.pmmlite.core.models.ModelsPackage;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;

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
 * An implementation of the model object '<em><b>Primary Model Formula</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl#getExpression <em>Expression</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl#getDepVar <em>Dep Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl#getParams <em>Params</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl#getIndepVar <em>Indep Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl#getInitialParam <em>Initial Param</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PrimaryModelFormulaImpl extends EObjectImpl implements PrimaryModelFormula {
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
	 * The cached value of the '{@link #getIndepVar() <em>Indep Var</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndepVar()
	 * @generated
	 * @ordered
	 */
	protected Variable indepVar;

	/**
	 * The default value of the '{@link #getInitialParam() <em>Initial Param</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInitialParam()
	 * @generated
	 * @ordered
	 */
	protected static final String INITIAL_PARAM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getInitialParam() <em>Initial Param</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInitialParam()
	 * @generated
	 * @ordered
	 */
	protected String initialParam = INITIAL_PARAM_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PrimaryModelFormulaImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelsPackage.Literals.PRIMARY_MODEL_FORMULA;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL_FORMULA__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL_FORMULA__ID, oldId, id));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL_FORMULA__EXPRESSION, oldExpression, expression));
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
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR, oldDepVar, newDepVar);
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
				msgs = ((InternalEObject)depVar).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR, null, msgs);
			if (newDepVar != null)
				msgs = ((InternalEObject)newDepVar).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR, null, msgs);
			msgs = basicSetDepVar(newDepVar, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR, newDepVar, newDepVar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Parameter> getParams() {
		if (params == null) {
			params = new EObjectContainmentEList<Parameter>(Parameter.class, this, ModelsPackage.PRIMARY_MODEL_FORMULA__PARAMS);
		}
		return params;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Variable getIndepVar() {
		return indepVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetIndepVar(Variable newIndepVar, NotificationChain msgs) {
		Variable oldIndepVar = indepVar;
		indepVar = newIndepVar;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR, oldIndepVar, newIndepVar);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIndepVar(Variable newIndepVar) {
		if (newIndepVar != indepVar) {
			NotificationChain msgs = null;
			if (indepVar != null)
				msgs = ((InternalEObject)indepVar).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR, null, msgs);
			if (newIndepVar != null)
				msgs = ((InternalEObject)newIndepVar).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR, null, msgs);
			msgs = basicSetIndepVar(newIndepVar, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR, newIndepVar, newIndepVar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getInitialParam() {
		return initialParam;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInitialParam(String newInitialParam) {
		String oldInitialParam = initialParam;
		initialParam = newInitialParam;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL_FORMULA__INITIAL_PARAM, oldInitialParam, initialParam));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR:
				return basicSetDepVar(null, msgs);
			case ModelsPackage.PRIMARY_MODEL_FORMULA__PARAMS:
				return ((InternalEList<?>)getParams()).basicRemove(otherEnd, msgs);
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR:
				return basicSetIndepVar(null, msgs);
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
			case ModelsPackage.PRIMARY_MODEL_FORMULA__NAME:
				return getName();
			case ModelsPackage.PRIMARY_MODEL_FORMULA__ID:
				return getId();
			case ModelsPackage.PRIMARY_MODEL_FORMULA__EXPRESSION:
				return getExpression();
			case ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR:
				return getDepVar();
			case ModelsPackage.PRIMARY_MODEL_FORMULA__PARAMS:
				return getParams();
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR:
				return getIndepVar();
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INITIAL_PARAM:
				return getInitialParam();
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
			case ModelsPackage.PRIMARY_MODEL_FORMULA__NAME:
				setName((String)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__ID:
				setId((String)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__EXPRESSION:
				setExpression((String)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR:
				setDepVar((Variable)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__PARAMS:
				getParams().clear();
				getParams().addAll((Collection<? extends Parameter>)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR:
				setIndepVar((Variable)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INITIAL_PARAM:
				setInitialParam((String)newValue);
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
			case ModelsPackage.PRIMARY_MODEL_FORMULA__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__ID:
				setId(ID_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__EXPRESSION:
				setExpression(EXPRESSION_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR:
				setDepVar((Variable)null);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__PARAMS:
				getParams().clear();
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR:
				setIndepVar((Variable)null);
				return;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INITIAL_PARAM:
				setInitialParam(INITIAL_PARAM_EDEFAULT);
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
			case ModelsPackage.PRIMARY_MODEL_FORMULA__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ModelsPackage.PRIMARY_MODEL_FORMULA__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ModelsPackage.PRIMARY_MODEL_FORMULA__EXPRESSION:
				return EXPRESSION_EDEFAULT == null ? expression != null : !EXPRESSION_EDEFAULT.equals(expression);
			case ModelsPackage.PRIMARY_MODEL_FORMULA__DEP_VAR:
				return depVar != null;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__PARAMS:
				return params != null && !params.isEmpty();
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INDEP_VAR:
				return indepVar != null;
			case ModelsPackage.PRIMARY_MODEL_FORMULA__INITIAL_PARAM:
				return INITIAL_PARAM_EDEFAULT == null ? initialParam != null : !INITIAL_PARAM_EDEFAULT.equals(initialParam);
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
		result.append(", initialParam: ");
		result.append(initialParam);
		result.append(')');
		return result.toString();
	}

} //PrimaryModelFormulaImpl
