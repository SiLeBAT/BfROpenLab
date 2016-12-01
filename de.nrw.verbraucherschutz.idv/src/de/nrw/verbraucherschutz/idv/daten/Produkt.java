//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.12.01 um 11:24:07 AM CET 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Produkt complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Produkt">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="losNummer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="chargenNummer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="produktBezeichnung" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="handelsname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="artikelnummer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ean" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Produkt", propOrder = {
    "losNummer",
    "chargenNummer",
    "produktBezeichnung",
    "handelsname",
    "artikelnummer",
    "ean"
})
public class Produkt {

    @XmlElement(required = true, nillable = true)
    protected String losNummer;
    @XmlElement(required = true)
    protected String chargenNummer;
    @XmlElement(required = true)
    protected String produktBezeichnung;
    @XmlElement(required = true)
    protected String handelsname;
    @XmlElement(required = true, nillable = true)
    protected String artikelnummer;
    @XmlElement(required = true, nillable = true)
    protected String ean;

    /**
     * Ruft den Wert der losNummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLosNummer() {
        return losNummer;
    }

    /**
     * Legt den Wert der losNummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLosNummer(String value) {
        this.losNummer = value;
    }

    /**
     * Ruft den Wert der chargenNummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChargenNummer() {
        return chargenNummer;
    }

    /**
     * Legt den Wert der chargenNummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChargenNummer(String value) {
        this.chargenNummer = value;
    }

    /**
     * Ruft den Wert der produktBezeichnung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProduktBezeichnung() {
        return produktBezeichnung;
    }

    /**
     * Legt den Wert der produktBezeichnung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProduktBezeichnung(String value) {
        this.produktBezeichnung = value;
    }

    /**
     * Ruft den Wert der handelsname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHandelsname() {
        return handelsname;
    }

    /**
     * Legt den Wert der handelsname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHandelsname(String value) {
        this.handelsname = value;
    }

    /**
     * Ruft den Wert der artikelnummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArtikelnummer() {
        return artikelnummer;
    }

    /**
     * Legt den Wert der artikelnummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArtikelnummer(String value) {
        this.artikelnummer = value;
    }

    /**
     * Ruft den Wert der ean-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEan() {
        return ean;
    }

    /**
     * Legt den Wert der ean-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEan(String value) {
        this.ean = value;
    }

}
