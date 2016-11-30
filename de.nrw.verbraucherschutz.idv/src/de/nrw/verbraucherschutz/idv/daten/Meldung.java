//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.30 um 03:35:33 PM CET 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für Meldung complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Meldung">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="meldendeBehoerde" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/katalogsystem}KatalogWert"/>
 *         &lt;element name="nummer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fallNummer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fallBezeichnung" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="meldungVom" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Meldung", propOrder = {
    "meldendeBehoerde",
    "nummer",
    "status",
    "fallNummer",
    "fallBezeichnung",
    "meldungVom"
})
public class Meldung {

    @XmlElement(required = true)
    protected KatalogWert meldendeBehoerde;
    @XmlElement(required = true)
    protected String nummer;
    @XmlElement(required = true)
    protected String status;
    @XmlElement(required = true)
    protected String fallNummer;
    @XmlElement(required = true)
    protected String fallBezeichnung;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar meldungVom;

    /**
     * Ruft den Wert der meldendeBehoerde-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link KatalogWert }
     *     
     */
    public KatalogWert getMeldendeBehoerde() {
        return meldendeBehoerde;
    }

    /**
     * Legt den Wert der meldendeBehoerde-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link KatalogWert }
     *     
     */
    public void setMeldendeBehoerde(KatalogWert value) {
        this.meldendeBehoerde = value;
    }

    /**
     * Ruft den Wert der nummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNummer() {
        return nummer;
    }

    /**
     * Legt den Wert der nummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNummer(String value) {
        this.nummer = value;
    }

    /**
     * Ruft den Wert der status-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Legt den Wert der status-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Ruft den Wert der fallNummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFallNummer() {
        return fallNummer;
    }

    /**
     * Legt den Wert der fallNummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFallNummer(String value) {
        this.fallNummer = value;
    }

    /**
     * Ruft den Wert der fallBezeichnung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFallBezeichnung() {
        return fallBezeichnung;
    }

    /**
     * Legt den Wert der fallBezeichnung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFallBezeichnung(String value) {
        this.fallBezeichnung = value;
    }

    /**
     * Ruft den Wert der meldungVom-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMeldungVom() {
        return meldungVom;
    }

    /**
     * Legt den Wert der meldungVom-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMeldungVom(XMLGregorianCalendar value) {
        this.meldungVom = value;
    }

}
