//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.01.28 um 03:29:13 PM CET 
//


package de.bund.bfr.epcis;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * 
 * 			base type for all EPCIS events.
 * 			
 * 
 * <p>Java-Klasse für EPCISEventType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EPCISEventType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="recordTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="eventTimeZoneOffset" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="baseExtension" type="{urn:epcglobal:epcis:xsd:1}EPCISEventExtensionType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EPCISEventType", propOrder = {
    "eventTime",
    "recordTime",
    "eventTimeZoneOffset",
    "baseExtension"
})
@XmlSeeAlso({
    TransformationEventType.class,
    TransactionEventType.class,
    ObjectEventType.class,
    QuantityEventType.class,
    AggregationEventType.class
})
public abstract class EPCISEventType {

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar eventTime;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar recordTime;
    @XmlElement(required = true)
    protected String eventTimeZoneOffset;
    protected EPCISEventExtensionType baseExtension;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Ruft den Wert der eventTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEventTime() {
        return eventTime;
    }

    /**
     * Legt den Wert der eventTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEventTime(XMLGregorianCalendar value) {
        this.eventTime = value;
    }

    /**
     * Ruft den Wert der recordTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRecordTime() {
        return recordTime;
    }

    /**
     * Legt den Wert der recordTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRecordTime(XMLGregorianCalendar value) {
        this.recordTime = value;
    }

    /**
     * Ruft den Wert der eventTimeZoneOffset-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventTimeZoneOffset() {
        return eventTimeZoneOffset;
    }

    /**
     * Legt den Wert der eventTimeZoneOffset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventTimeZoneOffset(String value) {
        this.eventTimeZoneOffset = value;
    }

    /**
     * Ruft den Wert der baseExtension-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EPCISEventExtensionType }
     *     
     */
    public EPCISEventExtensionType getBaseExtension() {
        return baseExtension;
    }

    /**
     * Legt den Wert der baseExtension-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EPCISEventExtensionType }
     *     
     */
    public void setBaseExtension(EPCISEventExtensionType value) {
        this.baseExtension = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
