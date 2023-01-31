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

import de.bund.bfr.knime.pmmlite.core.models.ModelsPackage;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Renamings;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Tertiary Model Formula</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getExpression <em>Expression</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getDepVar <em>Dep Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getParams <em>Params</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getIndepVars <em>Indep Vars</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getTimeVar <em>Time Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getInitialParam <em>Initial Param</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getPrimaryFormula <em>Primary Formula</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getSecondaryFormulas <em>Secondary Formulas</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getSecondaryRenamings <em>Secondary Renamings</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl#getAssignments <em>Assignments</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TertiaryModelFormulaImpl extends EObjectImpl implements TertiaryModelFormula {
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
	 * The default value of the '{@link #getTimeVar() <em>Time Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimeVar()
	 * @generated
	 * @ordered
	 */
	protected static final String TIME_VAR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTimeVar() <em>Time Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimeVar()
	 * @generated
	 * @ordered
	 */
	protected String timeVar = TIME_VAR_EDEFAULT;

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
	 * The cached value of the '{@link #getPrimaryFormula() <em>Primary Formula</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrimaryFormula()
	 * @generated
	 * @ordered
	 */
	protected PrimaryModelFormula primaryFormula;

	/**
	 * The cached value of the '{@link #getSecondaryFormulas() <em>Secondary Formulas</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSecondaryFormulas()
	 * @generated
	 * @ordered
	 */
	protected EList<SecondaryModelFormula> secondaryFormulas;

	/**
	 * The cached value of the '{@link #getSecondaryRenamings() <em>Secondary Renamings</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSecondaryRenamings()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, Renamings> secondaryRenamings;

	/**
	 * The cached value of the '{@link #getAssignments() <em>Assignments</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssignments()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> assignments;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TertiaryModelFormulaImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelsPackage.Literals.TERTIARY_MODEL_FORMULA;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.TERTIARY_MODEL_FORMULA__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.TERTIARY_MODEL_FORMULA__ID, oldId, id));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.TERTIARY_MODEL_FORMULA__EXPRESSION, oldExpression, expression));
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
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR, oldDepVar, newDepVar);
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
				msgs = ((InternalEObject)depVar).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR, null, msgs);
			if (newDepVar != null)
				msgs = ((InternalEObject)newDepVar).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR, null, msgs);
			msgs = basicSetDepVar(newDepVar, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR, newDepVar, newDepVar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Parameter> getParams() {
		if (params == null) {
			params = new EObjectContainmentEList<Parameter>(Parameter.class, this, ModelsPackage.TERTIARY_MODEL_FORMULA__PARAMS);
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
			indepVars = new EObjectContainmentEList<Variable>(Variable.class, this, ModelsPackage.TERTIARY_MODEL_FORMULA__INDEP_VARS);
		}
		return indepVars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTimeVar() {
		return timeVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTimeVar(String newTimeVar) {
		String oldTimeVar = timeVar;
		timeVar = newTimeVar;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.TERTIARY_MODEL_FORMULA__TIME_VAR, oldTimeVar, timeVar));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.TERTIARY_MODEL_FORMULA__INITIAL_PARAM, oldInitialParam, initialParam));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PrimaryModelFormula getPrimaryFormula() {
		if (primaryFormula != null && primaryFormula.eIsProxy()) {
			InternalEObject oldPrimaryFormula = (InternalEObject)primaryFormula;
			primaryFormula = (PrimaryModelFormula)eResolveProxy(oldPrimaryFormula);
			if (primaryFormula != oldPrimaryFormula) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelsPackage.TERTIARY_MODEL_FORMULA__PRIMARY_FORMULA, oldPrimaryFormula, primaryFormula));
			}
		}
		return primaryFormula;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PrimaryModelFormula basicGetPrimaryFormula() {
		return primaryFormula;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPrimaryFormula(PrimaryModelFormula newPrimaryFormula) {
		PrimaryModelFormula oldPrimaryFormula = primaryFormula;
		primaryFormula = newPrimaryFormula;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.TERTIARY_MODEL_FORMULA__PRIMARY_FORMULA, oldPrimaryFormula, primaryFormula));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<SecondaryModelFormula> getSecondaryFormulas() {
		if (secondaryFormulas == null) {
			secondaryFormulas = new EObjectResolvingEList<SecondaryModelFormula>(SecondaryModelFormula.class, this, ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_FORMULAS);
		}
		return secondaryFormulas;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, Renamings> getSecondaryRenamings() {
		if (secondaryRenamings == null) {
			secondaryRenamings = new EcoreEMap<String,Renamings>(ModelsPackage.Literals.STRING_TO_RENAMINGS_MAP_ENTRY, StringToRenamingsMapEntryImpl.class, this, ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_RENAMINGS);
		}
		return secondaryRenamings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, String> getAssignments() {
		if (assignments == null) {
			assignments = new EcoreEMap<String,String>(ModelsPackage.Literals.STRING_TO_STRING_MAP_ENTRY, StringToStringMapEntryImpl.class, this, ModelsPackage.TERTIARY_MODEL_FORMULA__ASSIGNMENTS);
		}
		return assignments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR:
				return basicSetDepVar(null, msgs);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PARAMS:
				return ((InternalEList<?>)getParams()).basicRemove(otherEnd, msgs);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INDEP_VARS:
				return ((InternalEList<?>)getIndepVars()).basicRemove(otherEnd, msgs);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_RENAMINGS:
				return ((InternalEList<?>)getSecondaryRenamings()).basicRemove(otherEnd, msgs);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ASSIGNMENTS:
				return ((InternalEList<?>)getAssignments()).basicRemove(otherEnd, msgs);
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
			case ModelsPackage.TERTIARY_MODEL_FORMULA__NAME:
				return getName();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ID:
				return getId();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__EXPRESSION:
				return getExpression();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR:
				return getDepVar();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PARAMS:
				return getParams();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INDEP_VARS:
				return getIndepVars();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__TIME_VAR:
				return getTimeVar();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INITIAL_PARAM:
				return getInitialParam();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PRIMARY_FORMULA:
				if (resolve) return getPrimaryFormula();
				return basicGetPrimaryFormula();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_FORMULAS:
				return getSecondaryFormulas();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_RENAMINGS:
				if (coreType) return getSecondaryRenamings();
				else return getSecondaryRenamings().map();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ASSIGNMENTS:
				if (coreType) return getAssignments();
				else return getAssignments().map();
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
			case ModelsPackage.TERTIARY_MODEL_FORMULA__NAME:
				setName((String)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ID:
				setId((String)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__EXPRESSION:
				setExpression((String)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR:
				setDepVar((Variable)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PARAMS:
				getParams().clear();
				getParams().addAll((Collection<? extends Parameter>)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INDEP_VARS:
				getIndepVars().clear();
				getIndepVars().addAll((Collection<? extends Variable>)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__TIME_VAR:
				setTimeVar((String)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INITIAL_PARAM:
				setInitialParam((String)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PRIMARY_FORMULA:
				setPrimaryFormula((PrimaryModelFormula)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_FORMULAS:
				getSecondaryFormulas().clear();
				getSecondaryFormulas().addAll((Collection<? extends SecondaryModelFormula>)newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_RENAMINGS:
				((EStructuralFeature.Setting)getSecondaryRenamings()).set(newValue);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ASSIGNMENTS:
				((EStructuralFeature.Setting)getAssignments()).set(newValue);
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
			case ModelsPackage.TERTIARY_MODEL_FORMULA__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ID:
				setId(ID_EDEFAULT);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__EXPRESSION:
				setExpression(EXPRESSION_EDEFAULT);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR:
				setDepVar((Variable)null);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PARAMS:
				getParams().clear();
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INDEP_VARS:
				getIndepVars().clear();
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__TIME_VAR:
				setTimeVar(TIME_VAR_EDEFAULT);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INITIAL_PARAM:
				setInitialParam(INITIAL_PARAM_EDEFAULT);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PRIMARY_FORMULA:
				setPrimaryFormula((PrimaryModelFormula)null);
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_FORMULAS:
				getSecondaryFormulas().clear();
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_RENAMINGS:
				getSecondaryRenamings().clear();
				return;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ASSIGNMENTS:
				getAssignments().clear();
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
			case ModelsPackage.TERTIARY_MODEL_FORMULA__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__EXPRESSION:
				return EXPRESSION_EDEFAULT == null ? expression != null : !EXPRESSION_EDEFAULT.equals(expression);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__DEP_VAR:
				return depVar != null;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PARAMS:
				return params != null && !params.isEmpty();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INDEP_VARS:
				return indepVars != null && !indepVars.isEmpty();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__TIME_VAR:
				return TIME_VAR_EDEFAULT == null ? timeVar != null : !TIME_VAR_EDEFAULT.equals(timeVar);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__INITIAL_PARAM:
				return INITIAL_PARAM_EDEFAULT == null ? initialParam != null : !INITIAL_PARAM_EDEFAULT.equals(initialParam);
			case ModelsPackage.TERTIARY_MODEL_FORMULA__PRIMARY_FORMULA:
				return primaryFormula != null;
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_FORMULAS:
				return secondaryFormulas != null && !secondaryFormulas.isEmpty();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__SECONDARY_RENAMINGS:
				return secondaryRenamings != null && !secondaryRenamings.isEmpty();
			case ModelsPackage.TERTIARY_MODEL_FORMULA__ASSIGNMENTS:
				return assignments != null && !assignments.isEmpty();
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
		result.append(", timeVar: ");
		result.append(timeVar);
		result.append(", initialParam: ");
		result.append(initialParam);
		result.append(')');
		return result.toString();
	}

} //TertiaryModelFormulaImpl
