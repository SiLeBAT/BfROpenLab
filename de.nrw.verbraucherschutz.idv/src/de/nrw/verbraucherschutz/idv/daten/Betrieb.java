//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.12.01 um 11:24:07 AM CET 
//


package de.nrw.verbraucherschutz.idv.daten;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Betrieb complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Betrieb">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="betriebsnummer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="egZulassungsnummer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="betriebsname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="strasse" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hausnummer" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="hausnummerZusatz" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="plz" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ort" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="land" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="geoPositionLatitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="geoPositionLongitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="bemerkung" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="typ" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Betrieb", propOrder = {
    "betriebsnummer",
    "egZulassungsnummer",
    "betriebsname",
    "strasse",
    "hausnummer",
    "hausnummerZusatz",
    "plz",
    "ort",
    "land",
    "geoPositionLatitude",
    "geoPositionLongitude",
    "bemerkung"
})
public class Betrieb {

    @XmlElement(required = true)
    protected String betriebsnummer;
    @XmlElement(required = true, nillable = true)
    protected String egZulassungsnummer;
    @XmlElement(required = true)
    protected String betriebsname;
    @XmlElement(required = true, nillable = true)
    protected String strasse;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer hausnummer;
    @XmlElement(required = true, nillable = true)
    protected String hausnummerZusatz;
    @XmlElement(required = true)
    protected String plz;
    @XmlElement(required = true, nillable = true)
    protected String ort;
    @XmlElement(required = true)
    protected String land;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal geoPositionLatitude;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal geoPositionLongitude;
    @XmlElement(required = true, nillable = true)
    protected String bemerkung;
    @XmlAttribute(name = "typ")
    protected String typ;

    /**
     * Ruft den Wert der betriebsnummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBetriebsnummer() {
        return betriebsnummer;
    }

    /**
     * Legt den Wert der betriebsnummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBetriebsnummer(String value) {
        this.betriebsnummer = value;
    }

    /**
     * Ruft den Wert der egZulassungsnummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEgZulassungsnummer() {
        return egZulassungsnummer;
    }

    /**
     * Legt den Wert der egZulassungsnummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEgZulassungsnummer(String value) {
        this.egZulassungsnummer = value;
    }

    /**
     * Ruft den Wert der betriebsname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBetriebsname() {
        return betriebsname;
    }

    /**
     * Legt den Wert der betriebsname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBetriebsname(String value) {
        this.betriebsname = value;
    }

    /**
     * Ruft den Wert der strasse-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrasse() {
        return strasse;
    }

    /**
     * Legt den Wert der strasse-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrasse(String value) {
        this.strasse = value;
    }

    /**
     * Ruft den Wert der hausnummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getHausnummer() {
        return hausnummer;
    }

    /**
     * Legt den Wert der hausnummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHausnummer(Integer value) {
        this.hausnummer = value;
    }

    /**
     * Ruft den Wert der hausnummerZusatz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHausnummerZusatz() {
        return hausnummerZusatz;
    }

    /**
     * Legt den Wert der hausnummerZusatz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHausnummerZusatz(String value) {
        this.hausnummerZusatz = value;
    }

    /**
     * Ruft den Wert der plz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlz() {
        return plz;
    }

    /**
     * Legt den Wert der plz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlz(String value) {
        this.plz = value;
    }

    /**
     * Ruft den Wert der ort-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrt() {
        return ort;
    }

    /**
     * Legt den Wert der ort-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrt(String value) {
        this.ort = value;
    }

    /**
     * Ruft den Wert der land-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLand() {
        return land;
    }

    /**
     * Legt den Wert der land-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLand(String value) {
        this.land = value;
    }

    /**
     * Ruft den Wert der geoPositionLatitude-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getGeoPositionLatitude() {
        return geoPositionLatitude;
    }

    /**
     * Legt den Wert der geoPositionLatitude-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setGeoPositionLatitude(BigDecimal value) {
        this.geoPositionLatitude = value;
    }

    /**
     * Ruft den Wert der geoPositionLongitude-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getGeoPositionLongitude() {
        return geoPositionLongitude;
    }

    /**
     * Legt den Wert der geoPositionLongitude-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setGeoPositionLongitude(BigDecimal value) {
        this.geoPositionLongitude = value;
    }

    /**
     * Ruft den Wert der bemerkung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBemerkung() {
        return bemerkung;
    }

    /**
     * Legt den Wert der bemerkung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBemerkung(String value) {
        this.bemerkung = value;
    }

    /**
     * Ruft den Wert der typ-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTyp() {
        return typ;
    }

    /**
     * Legt den Wert der typ-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTyp(String value) {
        this.typ = value;
    }

}
