//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.08.30 um 04:08:29 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für lieferung complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="lieferung">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lieferscheinNummer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ausgeliefertAm" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "lieferung", propOrder = {
    "lieferscheinNummer",
    "ausgeliefertAm"
})
public class Lieferung {

    @XmlElement(required = true)
    protected String lieferscheinNummer;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar ausgeliefertAm;

    /**
     * Ruft den Wert der lieferscheinNummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLieferscheinNummer() {
        return lieferscheinNummer;
    }

    /**
     * Legt den Wert der lieferscheinNummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLieferscheinNummer(String value) {
        this.lieferscheinNummer = value;
    }

    /**
     * Ruft den Wert der ausgeliefertAm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAusgeliefertAm() {
        return ausgeliefertAm;
    }

    /**
     * Legt den Wert der ausgeliefertAm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAusgeliefertAm(XMLGregorianCalendar value) {
        this.ausgeliefertAm = value;
    }

}
