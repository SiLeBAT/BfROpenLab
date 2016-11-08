/**
 */
package de.bund.bfr.knime.pmmlite.core.data;

import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Condition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.Condition#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getCondition()
 * @model
 * @generated
 */
public interface Condition extends NameableWithUnit {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.data.DataPackage#getCondition_Value()
	 * @model
	 * @generated
	 */
	Double getValue();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.data.Condition#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(Double value);

} // Condition
