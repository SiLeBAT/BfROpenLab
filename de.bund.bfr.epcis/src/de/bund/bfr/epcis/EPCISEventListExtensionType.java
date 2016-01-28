//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.01.28 um 03:29:13 PM CET 
//


package de.bund.bfr.epcis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für EPCISEventListExtensionType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EPCISEventListExtensionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="TransformationEvent" type="{urn:epcglobal:epcis:xsd:1}TransformationEventType"/>
 *         &lt;element name="extension" type="{urn:epcglobal:epcis:xsd:1}EPCISEventListExtension2Type"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EPCISEventListExtensionType", propOrder = {
    "transformationEvent",
    "extension"
})
public class EPCISEventListExtensionType {

    @XmlElement(name = "TransformationEvent")
    protected TransformationEventType transformationEvent;
    protected EPCISEventListExtension2Type extension;

    /**
     * Ruft den Wert der transformationEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransformationEventType }
     *     
     */
    public TransformationEventType getTransformationEvent() {
        return transformationEvent;
    }

    /**
     * Legt den Wert der transformationEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransformationEventType }
     *     
     */
    public void setTransformationEvent(TransformationEventType value) {
        this.transformationEvent = value;
    }

    /**
     * Ruft den Wert der extension-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EPCISEventListExtension2Type }
     *     
     */
    public EPCISEventListExtension2Type getExtension() {
        return extension;
    }

    /**
     * Legt den Wert der extension-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EPCISEventListExtension2Type }
     *     
     */
    public void setExtension(EPCISEventListExtension2Type value) {
        this.extension = value;
    }

}
