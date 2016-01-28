//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.01.28 um 03:29:13 PM CET 
//


package de.bund.bfr.epcis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ServiceTransaction complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ServiceTransaction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="TypeOfServiceTransaction" type="{http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader}TypeOfServiceTransaction" />
 *       &lt;attribute name="IsNonRepudiationRequired" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IsAuthenticationRequired" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IsNonRepudiationOfReceiptRequired" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IsIntegrityCheckRequired" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IsApplicationErrorResponseRequested" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TimeToAcknowledgeReceipt" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TimeToAcknowledgeAcceptance" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TimeToPerform" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Recurrence" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceTransaction", namespace = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader")
public class ServiceTransaction {

    @XmlAttribute(name = "TypeOfServiceTransaction")
    protected TypeOfServiceTransaction typeOfServiceTransaction;
    @XmlAttribute(name = "IsNonRepudiationRequired")
    protected String isNonRepudiationRequired;
    @XmlAttribute(name = "IsAuthenticationRequired")
    protected String isAuthenticationRequired;
    @XmlAttribute(name = "IsNonRepudiationOfReceiptRequired")
    protected String isNonRepudiationOfReceiptRequired;
    @XmlAttribute(name = "IsIntegrityCheckRequired")
    protected String isIntegrityCheckRequired;
    @XmlAttribute(name = "IsApplicationErrorResponseRequested")
    protected String isApplicationErrorResponseRequested;
    @XmlAttribute(name = "TimeToAcknowledgeReceipt")
    protected String timeToAcknowledgeReceipt;
    @XmlAttribute(name = "TimeToAcknowledgeAcceptance")
    protected String timeToAcknowledgeAcceptance;
    @XmlAttribute(name = "TimeToPerform")
    protected String timeToPerform;
    @XmlAttribute(name = "Recurrence")
    protected String recurrence;

    /**
     * Ruft den Wert der typeOfServiceTransaction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeOfServiceTransaction }
     *     
     */
    public TypeOfServiceTransaction getTypeOfServiceTransaction() {
        return typeOfServiceTransaction;
    }

    /**
     * Legt den Wert der typeOfServiceTransaction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeOfServiceTransaction }
     *     
     */
    public void setTypeOfServiceTransaction(TypeOfServiceTransaction value) {
        this.typeOfServiceTransaction = value;
    }

    /**
     * Ruft den Wert der isNonRepudiationRequired-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsNonRepudiationRequired() {
        return isNonRepudiationRequired;
    }

    /**
     * Legt den Wert der isNonRepudiationRequired-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsNonRepudiationRequired(String value) {
        this.isNonRepudiationRequired = value;
    }

    /**
     * Ruft den Wert der isAuthenticationRequired-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsAuthenticationRequired() {
        return isAuthenticationRequired;
    }

    /**
     * Legt den Wert der isAuthenticationRequired-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsAuthenticationRequired(String value) {
        this.isAuthenticationRequired = value;
    }

    /**
     * Ruft den Wert der isNonRepudiationOfReceiptRequired-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsNonRepudiationOfReceiptRequired() {
        return isNonRepudiationOfReceiptRequired;
    }

    /**
     * Legt den Wert der isNonRepudiationOfReceiptRequired-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsNonRepudiationOfReceiptRequired(String value) {
        this.isNonRepudiationOfReceiptRequired = value;
    }

    /**
     * Ruft den Wert der isIntegrityCheckRequired-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsIntegrityCheckRequired() {
        return isIntegrityCheckRequired;
    }

    /**
     * Legt den Wert der isIntegrityCheckRequired-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsIntegrityCheckRequired(String value) {
        this.isIntegrityCheckRequired = value;
    }

    /**
     * Ruft den Wert der isApplicationErrorResponseRequested-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsApplicationErrorResponseRequested() {
        return isApplicationErrorResponseRequested;
    }

    /**
     * Legt den Wert der isApplicationErrorResponseRequested-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsApplicationErrorResponseRequested(String value) {
        this.isApplicationErrorResponseRequested = value;
    }

    /**
     * Ruft den Wert der timeToAcknowledgeReceipt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeToAcknowledgeReceipt() {
        return timeToAcknowledgeReceipt;
    }

    /**
     * Legt den Wert der timeToAcknowledgeReceipt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeToAcknowledgeReceipt(String value) {
        this.timeToAcknowledgeReceipt = value;
    }

    /**
     * Ruft den Wert der timeToAcknowledgeAcceptance-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeToAcknowledgeAcceptance() {
        return timeToAcknowledgeAcceptance;
    }

    /**
     * Legt den Wert der timeToAcknowledgeAcceptance-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeToAcknowledgeAcceptance(String value) {
        this.timeToAcknowledgeAcceptance = value;
    }

    /**
     * Ruft den Wert der timeToPerform-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeToPerform() {
        return timeToPerform;
    }

    /**
     * Legt den Wert der timeToPerform-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeToPerform(String value) {
        this.timeToPerform = value;
    }

    /**
     * Ruft den Wert der recurrence-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecurrence() {
        return recurrence;
    }

    /**
     * Legt den Wert der recurrence-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecurrence(String value) {
        this.recurrence = value;
    }

}
