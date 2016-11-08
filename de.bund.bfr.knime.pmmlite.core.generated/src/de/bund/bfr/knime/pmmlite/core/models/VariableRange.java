/**
 */
package de.bund.bfr.knime.pmmlite.core.models;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Variable Range</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMin <em>Min</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMax <em>Max</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getVariableRange()
 * @model
 * @generated
 */
public interface VariableRange extends EObject {
	/**
	 * Returns the value of the '<em><b>Min</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Min</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Min</em>' attribute.
	 * @see #setMin(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getVariableRange_Min()
	 * @model
	 * @generated
	 */
	Double getMin();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMin <em>Min</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Min</em>' attribute.
	 * @see #getMin()
	 * @generated
	 */
	void setMin(Double value);

	/**
	 * Returns the value of the '<em><b>Max</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Max</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max</em>' attribute.
	 * @see #setMax(Double)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getVariableRange_Max()
	 * @model
	 * @generated
	 */
	Double getMax();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.VariableRange#getMax <em>Max</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max</em>' attribute.
	 * @see #getMax()
	 * @generated
	 */
	void setMax(Double value);

} // VariableRange
