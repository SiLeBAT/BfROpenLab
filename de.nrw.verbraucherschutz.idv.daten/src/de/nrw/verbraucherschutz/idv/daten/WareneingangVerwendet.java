//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.10.06 um 01:11:47 AM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für WareneingangVerwendet complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="WareneingangVerwendet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="warenumfang" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Warenumfang"/>
 *       &lt;/sequence>
 *       &lt;attribute name="wareneingangId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WareneingangVerwendet", propOrder = {
    "warenumfang"
})
public class WareneingangVerwendet {

    @XmlElement(required = true, nillable = true)
    protected Warenumfang warenumfang;
    @XmlAttribute(name = "wareneingangId")
    protected String wareneingangId;

    /**
     * Ruft den Wert der warenumfang-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Warenumfang }
     *     
     */
    public Warenumfang getWarenumfang() {
        return warenumfang;
    }

    /**
     * Legt den Wert der warenumfang-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Warenumfang }
     *     
     */
    public void setWarenumfang(Warenumfang value) {
        this.warenumfang = value;
    }

    /**
     * Ruft den Wert der wareneingangId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWareneingangId() {
        return wareneingangId;
    }

    /**
     * Legt den Wert der wareneingangId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWareneingangId(String value) {
        this.wareneingangId = value;
    }

}
