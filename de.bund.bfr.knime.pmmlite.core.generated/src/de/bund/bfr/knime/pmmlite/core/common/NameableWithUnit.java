/**
 */
package de.bund.bfr.knime.pmmlite.core.common;

import de.bund.bfr.knime.pmmlite.core.PmmUnit;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Nameable With Unit</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit#getUnit <em>Unit</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.common.CommonPackage#getNameableWithUnit()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface NameableWithUnit extends Nameable {
	/**
	 * Returns the value of the '<em><b>Unit</b></em>' attribute.
	 * The default value is <code>"NO_TRANSFORM(NO_UNIT)"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Unit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unit</em>' attribute.
	 * @see #setUnit(PmmUnit)
	 * @see de.bund.bfr.knime.pmmlite.core.common.CommonPackage#getNameableWithUnit_Unit()
	 * @model default="NO_TRANSFORM(NO_UNIT)" dataType="de.bund.bfr.knime.pmmlite.core.common.Unit"
	 * @generated
	 */
	PmmUnit getUnit();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit#getUnit <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit</em>' attribute.
	 * @see #getUnit()
	 * @generated
	 */
	void setUnit(PmmUnit value);

} // NameableWithUnit
