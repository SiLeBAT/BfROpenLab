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
package de.bund.bfr.knime.pmmlite.core.models;

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
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsFactory
 * @model kind="package"
 * @generated
 */
public interface ModelsPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "models";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///de/bund/bfr/knime/pmmlite/core/models.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "de.bund.bfr.knime.pmmlite.core.models";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelsPackage eINSTANCE = de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl.init();

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.Model <em>Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getModel()
	 * @generated
	 */
	int MODEL = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__NAME = CommonPackage.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__ID = CommonPackage.IDENTIFIABLE__ID;

	/**
	 * The feature id for the '<em><b>Sse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__SSE = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Mse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__MSE = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Rmse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__RMSE = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>R2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__R2 = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Aic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__AIC = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Degrees Of Freedom</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__DEGREES_OF_FREEDOM = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Variable Ranges</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__VARIABLE_RANGES = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Param Values</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__PARAM_VALUES = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Assignments</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__ASSIGNMENTS = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_FEATURE_COUNT = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula <em>Model Formula</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelFormula
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getModelFormula()
	 * @generated
	 */
	int MODEL_FORMULA = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_FORMULA__NAME = CommonPackage.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_FORMULA__ID = CommonPackage.IDENTIFIABLE__ID;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_FORMULA__EXPRESSION = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Dep Var</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_FORMULA__DEP_VAR = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Params</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_FORMULA__PARAMS = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Model Formula</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_FORMULA_FEATURE_COUNT = CommonPackage.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.VariableImpl <em>Variable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.VariableImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getVariable()
	 * @generated
	 */
	int VARIABLE = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VARIABLE__NAME = CommonPackage.NAMEABLE_WITH_UNIT__NAME;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VARIABLE__UNIT = CommonPackage.NAMEABLE_WITH_UNIT__UNIT;

	/**
	 * The number of structural features of the '<em>Variable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VARIABLE_FEATURE_COUNT = CommonPackage.NAMEABLE_WITH_UNIT_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.VariableRangeImpl <em>Variable Range</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.VariableRangeImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getVariableRange()
	 * @generated
	 */
	int VARIABLE_RANGE = 3;

	/**
	 * The feature id for the '<em><b>Min</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VARIABLE_RANGE__MIN = 0;

	/**
	 * The feature id for the '<em><b>Max</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VARIABLE_RANGE__MAX = 1;

	/**
	 * The number of structural features of the '<em>Variable Range</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VARIABLE_RANGE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterImpl <em>Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ParameterImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getParameter()
	 * @generated
	 */
	int PARAMETER = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__NAME = CommonPackage.NAMEABLE__NAME;

	/**
	 * The feature id for the '<em><b>Min</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__MIN = CommonPackage.NAMEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Max</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__MAX = CommonPackage.NAMEABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_FEATURE_COUNT = CommonPackage.NAMEABLE_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl <em>Parameter Value</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getParameterValue()
	 * @generated
	 */
	int PARAMETER_VALUE = 5;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Error</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__ERROR = 1;

	/**
	 * The feature id for the '<em><b>T</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__T = 2;

	/**
	 * The feature id for the '<em><b>P</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__P = 3;

	/**
	 * The feature id for the '<em><b>Correlations</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__CORRELATIONS = 4;

	/**
	 * The number of structural features of the '<em>Parameter Value</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl <em>Primary Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getPrimaryModel()
	 * @generated
	 */
	int PRIMARY_MODEL = 6;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__NAME = MODEL__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__ID = MODEL__ID;

	/**
	 * The feature id for the '<em><b>Sse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__SSE = MODEL__SSE;

	/**
	 * The feature id for the '<em><b>Mse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__MSE = MODEL__MSE;

	/**
	 * The feature id for the '<em><b>Rmse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__RMSE = MODEL__RMSE;

	/**
	 * The feature id for the '<em><b>R2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__R2 = MODEL__R2;

	/**
	 * The feature id for the '<em><b>Aic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__AIC = MODEL__AIC;

	/**
	 * The feature id for the '<em><b>Degrees Of Freedom</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__DEGREES_OF_FREEDOM = MODEL__DEGREES_OF_FREEDOM;

	/**
	 * The feature id for the '<em><b>Variable Ranges</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__VARIABLE_RANGES = MODEL__VARIABLE_RANGES;

	/**
	 * The feature id for the '<em><b>Param Values</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__PARAM_VALUES = MODEL__PARAM_VALUES;

	/**
	 * The feature id for the '<em><b>Assignments</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__ASSIGNMENTS = MODEL__ASSIGNMENTS;

	/**
	 * The feature id for the '<em><b>Formula</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__FORMULA = MODEL_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL__DATA = MODEL_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Primary Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FEATURE_COUNT = MODEL_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelImpl <em>Secondary Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getSecondaryModel()
	 * @generated
	 */
	int SECONDARY_MODEL = 7;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__NAME = MODEL__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__ID = MODEL__ID;

	/**
	 * The feature id for the '<em><b>Sse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__SSE = MODEL__SSE;

	/**
	 * The feature id for the '<em><b>Mse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__MSE = MODEL__MSE;

	/**
	 * The feature id for the '<em><b>Rmse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__RMSE = MODEL__RMSE;

	/**
	 * The feature id for the '<em><b>R2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__R2 = MODEL__R2;

	/**
	 * The feature id for the '<em><b>Aic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__AIC = MODEL__AIC;

	/**
	 * The feature id for the '<em><b>Degrees Of Freedom</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__DEGREES_OF_FREEDOM = MODEL__DEGREES_OF_FREEDOM;

	/**
	 * The feature id for the '<em><b>Variable Ranges</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__VARIABLE_RANGES = MODEL__VARIABLE_RANGES;

	/**
	 * The feature id for the '<em><b>Param Values</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__PARAM_VALUES = MODEL__PARAM_VALUES;

	/**
	 * The feature id for the '<em><b>Assignments</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__ASSIGNMENTS = MODEL__ASSIGNMENTS;

	/**
	 * The feature id for the '<em><b>Formula</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__FORMULA = MODEL_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL__DATA = MODEL_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Secondary Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FEATURE_COUNT = MODEL_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelImpl <em>Tertiary Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getTertiaryModel()
	 * @generated
	 */
	int TERTIARY_MODEL = 8;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__NAME = MODEL__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__ID = MODEL__ID;

	/**
	 * The feature id for the '<em><b>Sse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__SSE = MODEL__SSE;

	/**
	 * The feature id for the '<em><b>Mse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__MSE = MODEL__MSE;

	/**
	 * The feature id for the '<em><b>Rmse</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__RMSE = MODEL__RMSE;

	/**
	 * The feature id for the '<em><b>R2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__R2 = MODEL__R2;

	/**
	 * The feature id for the '<em><b>Aic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__AIC = MODEL__AIC;

	/**
	 * The feature id for the '<em><b>Degrees Of Freedom</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__DEGREES_OF_FREEDOM = MODEL__DEGREES_OF_FREEDOM;

	/**
	 * The feature id for the '<em><b>Variable Ranges</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__VARIABLE_RANGES = MODEL__VARIABLE_RANGES;

	/**
	 * The feature id for the '<em><b>Param Values</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__PARAM_VALUES = MODEL__PARAM_VALUES;

	/**
	 * The feature id for the '<em><b>Assignments</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__ASSIGNMENTS = MODEL__ASSIGNMENTS;

	/**
	 * The feature id for the '<em><b>Formula</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__FORMULA = MODEL_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL__DATA = MODEL_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Tertiary Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FEATURE_COUNT = MODEL_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl <em>Primary Model Formula</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getPrimaryModelFormula()
	 * @generated
	 */
	int PRIMARY_MODEL_FORMULA = 9;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FORMULA__NAME = MODEL_FORMULA__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FORMULA__ID = MODEL_FORMULA__ID;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FORMULA__EXPRESSION = MODEL_FORMULA__EXPRESSION;

	/**
	 * The feature id for the '<em><b>Dep Var</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FORMULA__DEP_VAR = MODEL_FORMULA__DEP_VAR;

	/**
	 * The feature id for the '<em><b>Params</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FORMULA__PARAMS = MODEL_FORMULA__PARAMS;

	/**
	 * The feature id for the '<em><b>Indep Var</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FORMULA__INDEP_VAR = MODEL_FORMULA_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Initial Param</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FORMULA__INITIAL_PARAM = MODEL_FORMULA_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Primary Model Formula</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PRIMARY_MODEL_FORMULA_FEATURE_COUNT = MODEL_FORMULA_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl <em>Secondary Model Formula</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getSecondaryModelFormula()
	 * @generated
	 */
	int SECONDARY_MODEL_FORMULA = 10;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FORMULA__NAME = MODEL_FORMULA__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FORMULA__ID = MODEL_FORMULA__ID;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FORMULA__EXPRESSION = MODEL_FORMULA__EXPRESSION;

	/**
	 * The feature id for the '<em><b>Dep Var</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FORMULA__DEP_VAR = MODEL_FORMULA__DEP_VAR;

	/**
	 * The feature id for the '<em><b>Params</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FORMULA__PARAMS = MODEL_FORMULA__PARAMS;

	/**
	 * The feature id for the '<em><b>Indep Vars</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FORMULA__INDEP_VARS = MODEL_FORMULA_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Transformation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FORMULA__TRANSFORMATION = MODEL_FORMULA_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Secondary Model Formula</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SECONDARY_MODEL_FORMULA_FEATURE_COUNT = MODEL_FORMULA_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl <em>Tertiary Model Formula</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getTertiaryModelFormula()
	 * @generated
	 */
	int TERTIARY_MODEL_FORMULA = 11;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__NAME = MODEL_FORMULA__NAME;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__ID = MODEL_FORMULA__ID;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__EXPRESSION = MODEL_FORMULA__EXPRESSION;

	/**
	 * The feature id for the '<em><b>Dep Var</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__DEP_VAR = MODEL_FORMULA__DEP_VAR;

	/**
	 * The feature id for the '<em><b>Params</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__PARAMS = MODEL_FORMULA__PARAMS;

	/**
	 * The feature id for the '<em><b>Indep Vars</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__INDEP_VARS = MODEL_FORMULA_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Time Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__TIME_VAR = MODEL_FORMULA_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Initial Param</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__INITIAL_PARAM = MODEL_FORMULA_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Primary Formula</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__PRIMARY_FORMULA = MODEL_FORMULA_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Secondary Formulas</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__SECONDARY_FORMULAS = MODEL_FORMULA_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Secondary Renamings</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__SECONDARY_RENAMINGS = MODEL_FORMULA_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Assignments</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA__ASSIGNMENTS = MODEL_FORMULA_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>Tertiary Model Formula</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TERTIARY_MODEL_FORMULA_FEATURE_COUNT = MODEL_FORMULA_FEATURE_COUNT + 7;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.RenamingsImpl <em>Renamings</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.RenamingsImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getRenamings()
	 * @generated
	 */
	int RENAMINGS = 12;

	/**
	 * The feature id for the '<em><b>Map</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RENAMINGS__MAP = 0;

	/**
	 * The number of structural features of the '<em>Renamings</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RENAMINGS_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToStringMapEntryImpl <em>String To String Map Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToStringMapEntryImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToStringMapEntry()
	 * @generated
	 */
	int STRING_TO_STRING_MAP_ENTRY = 13;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_STRING_MAP_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_STRING_MAP_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>String To String Map Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_STRING_MAP_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToDoubleMapEntryImpl <em>String To Double Map Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToDoubleMapEntryImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToDoubleMapEntry()
	 * @generated
	 */
	int STRING_TO_DOUBLE_MAP_ENTRY = 14;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_DOUBLE_MAP_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_DOUBLE_MAP_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>String To Double Map Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_DOUBLE_MAP_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToVariableRangeMapEntryImpl <em>String To Variable Range Map Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToVariableRangeMapEntryImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToVariableRangeMapEntry()
	 * @generated
	 */
	int STRING_TO_VARIABLE_RANGE_MAP_ENTRY = 15;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_VARIABLE_RANGE_MAP_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_VARIABLE_RANGE_MAP_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>String To Variable Range Map Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_VARIABLE_RANGE_MAP_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToParameterValueMapEntryImpl <em>String To Parameter Value Map Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToParameterValueMapEntryImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToParameterValueMapEntry()
	 * @generated
	 */
	int STRING_TO_PARAMETER_VALUE_MAP_ENTRY = 16;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_PARAMETER_VALUE_MAP_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_PARAMETER_VALUE_MAP_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>String To Parameter Value Map Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_PARAMETER_VALUE_MAP_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToRenamingsMapEntryImpl <em>String To Renamings Map Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToRenamingsMapEntryImpl
	 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToRenamingsMapEntry()
	 * @generated
	 */
	int STRING_TO_RENAMINGS_MAP_ENTRY = 17;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_RENAMINGS_MAP_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_RENAMINGS_MAP_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>String To Renamings Map Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_RENAMINGS_MAP_ENTRY_FEATURE_COUNT = 2;


	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.Model <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model
	 * @generated
	 */
	EClass getModel();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getSse <em>Sse</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sse</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getSse()
	 * @see #getModel()
	 * @generated
	 */
	EAttribute getModel_Sse();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getMse <em>Mse</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mse</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getMse()
	 * @see #getModel()
	 * @generated
	 */
	EAttribute getModel_Mse();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getRmse <em>Rmse</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Rmse</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getRmse()
	 * @see #getModel()
	 * @generated
	 */
	EAttribute getModel_Rmse();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getR2 <em>R2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>R2</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getR2()
	 * @see #getModel()
	 * @generated
	 */
	EAttribute getModel_R2();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getAic <em>Aic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Aic</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getAic()
	 * @see #getModel()
	 * @generated
	 */
	EAttribute getModel_Aic();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getDegreesOfFreedom <em>Degrees Of Freedom</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Degrees Of Freedom</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getDegreesOfFreedom()
	 * @see #getModel()
	 * @generated
	 */
	EAttribute getModel_DegreesOfFreedom();

	/**
	 * Returns the meta object for the map '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getVariableRanges <em>Variable Ranges</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Variable Ranges</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getVariableRanges()
	 * @see #getModel()
	 * @generated
	 */
	EReference getModel_VariableRanges();

	/**
	 * Returns the meta object for the map '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getParamValues <em>Param Values</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Param Values</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getParamValues()
	 * @see #getModel()
	 * @generated
	 */
	EReference getModel_ParamValues();

	/**
	 * Returns the meta object for the map '{@link de.bund.bfr.knime.pmmlite.core.models.Model#getAssignments <em>Assignments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Assignments</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Model#getAssignments()
	 * @see #getModel()
	 * @generated
	 */
	EReference getModel_Assignments();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula <em>Model Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model Formula</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelFormula
	 * @generated
	 */
	EClass getModelFormula();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getExpression <em>Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Expression</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getExpression()
	 * @see #getModelFormula()
	 * @generated
	 */
	EAttribute getModelFormula_Expression();

	/**
	 * Returns the meta object for the containment reference '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getDepVar <em>Dep Var</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Dep Var</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getDepVar()
	 * @see #getModelFormula()
	 * @generated
	 */
	EReference getModelFormula_DepVar();

	/**
	 * Returns the meta object for the containment reference list '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getParams <em>Params</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Params</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelFormula#getParams()
	 * @see #getModelFormula()
	 * @generated
	 */
	EReference getModelFormula_Params();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.Variable <em>Variable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Variable</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Variable
	 * @generated
	 */
	EClass getVariable();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange <em>Variable Range</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Variable Range</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.VariableRange
	 * @generated
	 */
	EClass getVariableRange();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMin <em>Min</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Min</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMin()
	 * @see #getVariableRange()
	 * @generated
	 */
	EAttribute getVariableRange_Min();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMax <em>Max</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMax()
	 * @see #getVariableRange()
	 * @generated
	 */
	EAttribute getVariableRange_Max();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.Parameter <em>Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Parameter
	 * @generated
	 */
	EClass getParameter();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.Parameter#getMin <em>Min</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Min</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Parameter#getMin()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Min();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.Parameter#getMax <em>Max</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Parameter#getMax()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Max();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue <em>Parameter Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter Value</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ParameterValue
	 * @generated
	 */
	EClass getParameterValue();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getValue()
	 * @see #getParameterValue()
	 * @generated
	 */
	EAttribute getParameterValue_Value();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getError <em>Error</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Error</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getError()
	 * @see #getParameterValue()
	 * @generated
	 */
	EAttribute getParameterValue_Error();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getT <em>T</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>T</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getT()
	 * @see #getParameterValue()
	 * @generated
	 */
	EAttribute getParameterValue_T();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getP <em>P</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>P</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getP()
	 * @see #getParameterValue()
	 * @generated
	 */
	EAttribute getParameterValue_P();

	/**
	 * Returns the meta object for the map '{@link de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getCorrelations <em>Correlations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Correlations</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ParameterValue#getCorrelations()
	 * @see #getParameterValue()
	 * @generated
	 */
	EReference getParameterValue_Correlations();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel <em>Primary Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Primary Model</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.PrimaryModel
	 * @generated
	 */
	EClass getPrimaryModel();

	/**
	 * Returns the meta object for the reference '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel#getFormula <em>Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Formula</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.PrimaryModel#getFormula()
	 * @see #getPrimaryModel()
	 * @generated
	 */
	EReference getPrimaryModel_Formula();

	/**
	 * Returns the meta object for the reference '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Data</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.PrimaryModel#getData()
	 * @see #getPrimaryModel()
	 * @generated
	 */
	EReference getPrimaryModel_Data();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModel <em>Secondary Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Secondary Model</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.SecondaryModel
	 * @generated
	 */
	EClass getSecondaryModel();

	/**
	 * Returns the meta object for the reference '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModel#getFormula <em>Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Formula</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.SecondaryModel#getFormula()
	 * @see #getSecondaryModel()
	 * @generated
	 */
	EReference getSecondaryModel_Formula();

	/**
	 * Returns the meta object for the reference list '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModel#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Data</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.SecondaryModel#getData()
	 * @see #getSecondaryModel()
	 * @generated
	 */
	EReference getSecondaryModel_Data();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModel <em>Tertiary Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tertiary Model</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModel
	 * @generated
	 */
	EClass getTertiaryModel();

	/**
	 * Returns the meta object for the reference '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModel#getFormula <em>Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Formula</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModel#getFormula()
	 * @see #getTertiaryModel()
	 * @generated
	 */
	EReference getTertiaryModel_Formula();

	/**
	 * Returns the meta object for the reference list '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModel#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Data</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModel#getData()
	 * @see #getTertiaryModel()
	 * @generated
	 */
	EReference getTertiaryModel_Data();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula <em>Primary Model Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Primary Model Formula</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula
	 * @generated
	 */
	EClass getPrimaryModelFormula();

	/**
	 * Returns the meta object for the containment reference '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getIndepVar <em>Indep Var</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Indep Var</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getIndepVar()
	 * @see #getPrimaryModelFormula()
	 * @generated
	 */
	EReference getPrimaryModelFormula_IndepVar();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getInitialParam <em>Initial Param</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Initial Param</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getInitialParam()
	 * @see #getPrimaryModelFormula()
	 * @generated
	 */
	EAttribute getPrimaryModelFormula_InitialParam();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula <em>Secondary Model Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Secondary Model Formula</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula
	 * @generated
	 */
	EClass getSecondaryModelFormula();

	/**
	 * Returns the meta object for the containment reference list '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula#getIndepVars <em>Indep Vars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Indep Vars</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula#getIndepVars()
	 * @see #getSecondaryModelFormula()
	 * @generated
	 */
	EReference getSecondaryModelFormula_IndepVars();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula#getTransformation <em>Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Transformation</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula#getTransformation()
	 * @see #getSecondaryModelFormula()
	 * @generated
	 */
	EAttribute getSecondaryModelFormula_Transformation();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula <em>Tertiary Model Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tertiary Model Formula</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula
	 * @generated
	 */
	EClass getTertiaryModelFormula();

	/**
	 * Returns the meta object for the containment reference list '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getIndepVars <em>Indep Vars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Indep Vars</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getIndepVars()
	 * @see #getTertiaryModelFormula()
	 * @generated
	 */
	EReference getTertiaryModelFormula_IndepVars();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getTimeVar <em>Time Var</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time Var</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getTimeVar()
	 * @see #getTertiaryModelFormula()
	 * @generated
	 */
	EAttribute getTertiaryModelFormula_TimeVar();

	/**
	 * Returns the meta object for the attribute '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getInitialParam <em>Initial Param</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Initial Param</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getInitialParam()
	 * @see #getTertiaryModelFormula()
	 * @generated
	 */
	EAttribute getTertiaryModelFormula_InitialParam();

	/**
	 * Returns the meta object for the reference '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getPrimaryFormula <em>Primary Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Primary Formula</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getPrimaryFormula()
	 * @see #getTertiaryModelFormula()
	 * @generated
	 */
	EReference getTertiaryModelFormula_PrimaryFormula();

	/**
	 * Returns the meta object for the reference list '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getSecondaryFormulas <em>Secondary Formulas</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Secondary Formulas</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getSecondaryFormulas()
	 * @see #getTertiaryModelFormula()
	 * @generated
	 */
	EReference getTertiaryModelFormula_SecondaryFormulas();

	/**
	 * Returns the meta object for the map '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getSecondaryRenamings <em>Secondary Renamings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Secondary Renamings</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getSecondaryRenamings()
	 * @see #getTertiaryModelFormula()
	 * @generated
	 */
	EReference getTertiaryModelFormula_SecondaryRenamings();

	/**
	 * Returns the meta object for the map '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getAssignments <em>Assignments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Assignments</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula#getAssignments()
	 * @see #getTertiaryModelFormula()
	 * @generated
	 */
	EReference getTertiaryModelFormula_Assignments();

	/**
	 * Returns the meta object for class '{@link de.bund.bfr.knime.pmmlite.core.models.Renamings <em>Renamings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Renamings</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Renamings
	 * @generated
	 */
	EClass getRenamings();

	/**
	 * Returns the meta object for the map '{@link de.bund.bfr.knime.pmmlite.core.models.Renamings#getMap <em>Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Map</em>'.
	 * @see de.bund.bfr.knime.pmmlite.core.models.Renamings#getMap()
	 * @see #getRenamings()
	 * @generated
	 */
	EReference getRenamings_Map();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>String To String Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String To String Map Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueDataType="org.eclipse.emf.ecore.EString"
	 * @generated
	 */
	EClass getStringToStringMapEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToStringMapEntry()
	 * @generated
	 */
	EAttribute getStringToStringMapEntry_Key();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToStringMapEntry()
	 * @generated
	 */
	EAttribute getStringToStringMapEntry_Value();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>String To Double Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String To Double Map Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueDataType="org.eclipse.emf.ecore.EDoubleObject"
	 * @generated
	 */
	EClass getStringToDoubleMapEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToDoubleMapEntry()
	 * @generated
	 */
	EAttribute getStringToDoubleMapEntry_Key();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToDoubleMapEntry()
	 * @generated
	 */
	EAttribute getStringToDoubleMapEntry_Value();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>String To Variable Range Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String To Variable Range Map Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueType="de.bund.bfr.knime.pmmlite.core.models.VariableRange" valueContainment="true"
	 * @generated
	 */
	EClass getStringToVariableRangeMapEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToVariableRangeMapEntry()
	 * @generated
	 */
	EAttribute getStringToVariableRangeMapEntry_Key();

	/**
	 * Returns the meta object for the containment reference '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToVariableRangeMapEntry()
	 * @generated
	 */
	EReference getStringToVariableRangeMapEntry_Value();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>String To Parameter Value Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String To Parameter Value Map Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueType="de.bund.bfr.knime.pmmlite.core.models.ParameterValue" valueContainment="true"
	 * @generated
	 */
	EClass getStringToParameterValueMapEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToParameterValueMapEntry()
	 * @generated
	 */
	EAttribute getStringToParameterValueMapEntry_Key();

	/**
	 * Returns the meta object for the containment reference '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToParameterValueMapEntry()
	 * @generated
	 */
	EReference getStringToParameterValueMapEntry_Value();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>String To Renamings Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String To Renamings Map Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueType="de.bund.bfr.knime.pmmlite.core.models.Renamings" valueContainment="true"
	 * @generated
	 */
	EClass getStringToRenamingsMapEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToRenamingsMapEntry()
	 * @generated
	 */
	EAttribute getStringToRenamingsMapEntry_Key();

	/**
	 * Returns the meta object for the containment reference '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToRenamingsMapEntry()
	 * @generated
	 */
	EReference getStringToRenamingsMapEntry_Value();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelsFactory getModelsFactory();

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
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.Model <em>Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.Model
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getModel()
		 * @generated
		 */
		EClass MODEL = eINSTANCE.getModel();

		/**
		 * The meta object literal for the '<em><b>Sse</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL__SSE = eINSTANCE.getModel_Sse();

		/**
		 * The meta object literal for the '<em><b>Mse</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL__MSE = eINSTANCE.getModel_Mse();

		/**
		 * The meta object literal for the '<em><b>Rmse</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL__RMSE = eINSTANCE.getModel_Rmse();

		/**
		 * The meta object literal for the '<em><b>R2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL__R2 = eINSTANCE.getModel_R2();

		/**
		 * The meta object literal for the '<em><b>Aic</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL__AIC = eINSTANCE.getModel_Aic();

		/**
		 * The meta object literal for the '<em><b>Degrees Of Freedom</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL__DEGREES_OF_FREEDOM = eINSTANCE.getModel_DegreesOfFreedom();

		/**
		 * The meta object literal for the '<em><b>Variable Ranges</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL__VARIABLE_RANGES = eINSTANCE.getModel_VariableRanges();

		/**
		 * The meta object literal for the '<em><b>Param Values</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL__PARAM_VALUES = eINSTANCE.getModel_ParamValues();

		/**
		 * The meta object literal for the '<em><b>Assignments</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL__ASSIGNMENTS = eINSTANCE.getModel_Assignments();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.ModelFormula <em>Model Formula</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.ModelFormula
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getModelFormula()
		 * @generated
		 */
		EClass MODEL_FORMULA = eINSTANCE.getModelFormula();

		/**
		 * The meta object literal for the '<em><b>Expression</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_FORMULA__EXPRESSION = eINSTANCE.getModelFormula_Expression();

		/**
		 * The meta object literal for the '<em><b>Dep Var</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_FORMULA__DEP_VAR = eINSTANCE.getModelFormula_DepVar();

		/**
		 * The meta object literal for the '<em><b>Params</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_FORMULA__PARAMS = eINSTANCE.getModelFormula_Params();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.VariableImpl <em>Variable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.VariableImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getVariable()
		 * @generated
		 */
		EClass VARIABLE = eINSTANCE.getVariable();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.VariableRangeImpl <em>Variable Range</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.VariableRangeImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getVariableRange()
		 * @generated
		 */
		EClass VARIABLE_RANGE = eINSTANCE.getVariableRange();

		/**
		 * The meta object literal for the '<em><b>Min</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VARIABLE_RANGE__MIN = eINSTANCE.getVariableRange_Min();

		/**
		 * The meta object literal for the '<em><b>Max</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VARIABLE_RANGE__MAX = eINSTANCE.getVariableRange_Max();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterImpl <em>Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ParameterImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getParameter()
		 * @generated
		 */
		EClass PARAMETER = eINSTANCE.getParameter();

		/**
		 * The meta object literal for the '<em><b>Min</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER__MIN = eINSTANCE.getParameter_Min();

		/**
		 * The meta object literal for the '<em><b>Max</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER__MAX = eINSTANCE.getParameter_Max();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl <em>Parameter Value</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getParameterValue()
		 * @generated
		 */
		EClass PARAMETER_VALUE = eINSTANCE.getParameterValue();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER_VALUE__VALUE = eINSTANCE.getParameterValue_Value();

		/**
		 * The meta object literal for the '<em><b>Error</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER_VALUE__ERROR = eINSTANCE.getParameterValue_Error();

		/**
		 * The meta object literal for the '<em><b>T</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER_VALUE__T = eINSTANCE.getParameterValue_T();

		/**
		 * The meta object literal for the '<em><b>P</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER_VALUE__P = eINSTANCE.getParameterValue_P();

		/**
		 * The meta object literal for the '<em><b>Correlations</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PARAMETER_VALUE__CORRELATIONS = eINSTANCE.getParameterValue_Correlations();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl <em>Primary Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getPrimaryModel()
		 * @generated
		 */
		EClass PRIMARY_MODEL = eINSTANCE.getPrimaryModel();

		/**
		 * The meta object literal for the '<em><b>Formula</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PRIMARY_MODEL__FORMULA = eINSTANCE.getPrimaryModel_Formula();

		/**
		 * The meta object literal for the '<em><b>Data</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PRIMARY_MODEL__DATA = eINSTANCE.getPrimaryModel_Data();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelImpl <em>Secondary Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getSecondaryModel()
		 * @generated
		 */
		EClass SECONDARY_MODEL = eINSTANCE.getSecondaryModel();

		/**
		 * The meta object literal for the '<em><b>Formula</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SECONDARY_MODEL__FORMULA = eINSTANCE.getSecondaryModel_Formula();

		/**
		 * The meta object literal for the '<em><b>Data</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SECONDARY_MODEL__DATA = eINSTANCE.getSecondaryModel_Data();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelImpl <em>Tertiary Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getTertiaryModel()
		 * @generated
		 */
		EClass TERTIARY_MODEL = eINSTANCE.getTertiaryModel();

		/**
		 * The meta object literal for the '<em><b>Formula</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TERTIARY_MODEL__FORMULA = eINSTANCE.getTertiaryModel_Formula();

		/**
		 * The meta object literal for the '<em><b>Data</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TERTIARY_MODEL__DATA = eINSTANCE.getTertiaryModel_Data();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl <em>Primary Model Formula</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelFormulaImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getPrimaryModelFormula()
		 * @generated
		 */
		EClass PRIMARY_MODEL_FORMULA = eINSTANCE.getPrimaryModelFormula();

		/**
		 * The meta object literal for the '<em><b>Indep Var</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PRIMARY_MODEL_FORMULA__INDEP_VAR = eINSTANCE.getPrimaryModelFormula_IndepVar();

		/**
		 * The meta object literal for the '<em><b>Initial Param</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PRIMARY_MODEL_FORMULA__INITIAL_PARAM = eINSTANCE.getPrimaryModelFormula_InitialParam();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl <em>Secondary Model Formula</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.SecondaryModelFormulaImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getSecondaryModelFormula()
		 * @generated
		 */
		EClass SECONDARY_MODEL_FORMULA = eINSTANCE.getSecondaryModelFormula();

		/**
		 * The meta object literal for the '<em><b>Indep Vars</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SECONDARY_MODEL_FORMULA__INDEP_VARS = eINSTANCE.getSecondaryModelFormula_IndepVars();

		/**
		 * The meta object literal for the '<em><b>Transformation</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SECONDARY_MODEL_FORMULA__TRANSFORMATION = eINSTANCE.getSecondaryModelFormula_Transformation();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl <em>Tertiary Model Formula</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.TertiaryModelFormulaImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getTertiaryModelFormula()
		 * @generated
		 */
		EClass TERTIARY_MODEL_FORMULA = eINSTANCE.getTertiaryModelFormula();

		/**
		 * The meta object literal for the '<em><b>Indep Vars</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TERTIARY_MODEL_FORMULA__INDEP_VARS = eINSTANCE.getTertiaryModelFormula_IndepVars();

		/**
		 * The meta object literal for the '<em><b>Time Var</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TERTIARY_MODEL_FORMULA__TIME_VAR = eINSTANCE.getTertiaryModelFormula_TimeVar();

		/**
		 * The meta object literal for the '<em><b>Initial Param</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TERTIARY_MODEL_FORMULA__INITIAL_PARAM = eINSTANCE.getTertiaryModelFormula_InitialParam();

		/**
		 * The meta object literal for the '<em><b>Primary Formula</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TERTIARY_MODEL_FORMULA__PRIMARY_FORMULA = eINSTANCE.getTertiaryModelFormula_PrimaryFormula();

		/**
		 * The meta object literal for the '<em><b>Secondary Formulas</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TERTIARY_MODEL_FORMULA__SECONDARY_FORMULAS = eINSTANCE.getTertiaryModelFormula_SecondaryFormulas();

		/**
		 * The meta object literal for the '<em><b>Secondary Renamings</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TERTIARY_MODEL_FORMULA__SECONDARY_RENAMINGS = eINSTANCE.getTertiaryModelFormula_SecondaryRenamings();

		/**
		 * The meta object literal for the '<em><b>Assignments</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TERTIARY_MODEL_FORMULA__ASSIGNMENTS = eINSTANCE.getTertiaryModelFormula_Assignments();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.RenamingsImpl <em>Renamings</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.RenamingsImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getRenamings()
		 * @generated
		 */
		EClass RENAMINGS = eINSTANCE.getRenamings();

		/**
		 * The meta object literal for the '<em><b>Map</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RENAMINGS__MAP = eINSTANCE.getRenamings_Map();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToStringMapEntryImpl <em>String To String Map Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToStringMapEntryImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToStringMapEntry()
		 * @generated
		 */
		EClass STRING_TO_STRING_MAP_ENTRY = eINSTANCE.getStringToStringMapEntry();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_STRING_MAP_ENTRY__KEY = eINSTANCE.getStringToStringMapEntry_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_STRING_MAP_ENTRY__VALUE = eINSTANCE.getStringToStringMapEntry_Value();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToDoubleMapEntryImpl <em>String To Double Map Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToDoubleMapEntryImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToDoubleMapEntry()
		 * @generated
		 */
		EClass STRING_TO_DOUBLE_MAP_ENTRY = eINSTANCE.getStringToDoubleMapEntry();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_DOUBLE_MAP_ENTRY__KEY = eINSTANCE.getStringToDoubleMapEntry_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_DOUBLE_MAP_ENTRY__VALUE = eINSTANCE.getStringToDoubleMapEntry_Value();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToVariableRangeMapEntryImpl <em>String To Variable Range Map Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToVariableRangeMapEntryImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToVariableRangeMapEntry()
		 * @generated
		 */
		EClass STRING_TO_VARIABLE_RANGE_MAP_ENTRY = eINSTANCE.getStringToVariableRangeMapEntry();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_VARIABLE_RANGE_MAP_ENTRY__KEY = eINSTANCE.getStringToVariableRangeMapEntry_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STRING_TO_VARIABLE_RANGE_MAP_ENTRY__VALUE = eINSTANCE.getStringToVariableRangeMapEntry_Value();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToParameterValueMapEntryImpl <em>String To Parameter Value Map Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToParameterValueMapEntryImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToParameterValueMapEntry()
		 * @generated
		 */
		EClass STRING_TO_PARAMETER_VALUE_MAP_ENTRY = eINSTANCE.getStringToParameterValueMapEntry();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_PARAMETER_VALUE_MAP_ENTRY__KEY = eINSTANCE.getStringToParameterValueMapEntry_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STRING_TO_PARAMETER_VALUE_MAP_ENTRY__VALUE = eINSTANCE.getStringToParameterValueMapEntry_Value();

		/**
		 * The meta object literal for the '{@link de.bund.bfr.knime.pmmlite.core.models.impl.StringToRenamingsMapEntryImpl <em>String To Renamings Map Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.StringToRenamingsMapEntryImpl
		 * @see de.bund.bfr.knime.pmmlite.core.models.impl.ModelsPackageImpl#getStringToRenamingsMapEntry()
		 * @generated
		 */
		EClass STRING_TO_RENAMINGS_MAP_ENTRY = eINSTANCE.getStringToRenamingsMapEntry();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_RENAMINGS_MAP_ENTRY__KEY = eINSTANCE.getStringToRenamingsMapEntry_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STRING_TO_RENAMINGS_MAP_ENTRY__VALUE = eINSTANCE.getStringToRenamingsMapEntry_Value();

	}

} //ModelsPackage
