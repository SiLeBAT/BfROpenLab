//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.10.05 um 10:29:10 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Warenbewegung complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Warenbewegung">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="betrieb" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Betrieb" maxOccurs="unbounded"/>
 *         &lt;element name="lieferung" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Lieferung"/>
 *         &lt;element name="warenumfang" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Warenumfang"/>
 *         &lt;element name="produkt" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Produkt"/>
 *         &lt;element name="zusatzparameter" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Zusatzparameter"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="produktionId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Warenbewegung", propOrder = {
    "betrieb",
    "lieferung",
    "warenumfang",
    "produkt",
    "zusatzparameter"
})
@XmlSeeAlso({
    Warenausgang.class,
    Wareneingang.class
})
public class Warenbewegung {

    @XmlElement(required = true)
    protected List<Betrieb> betrieb;
    @XmlElement(required = true)
    protected Lieferung lieferung;
    @XmlElement(required = true)
    protected Warenumfang warenumfang;
    @XmlElement(required = true)
    protected Produkt produkt;
    @XmlElement(required = true, nillable = true)
    protected Zusatzparameter zusatzparameter;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "produktionId")
    protected String produktionId;

    /**
     * Gets the value of the betrieb property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the betrieb property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBetrieb().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Betrieb }
     * 
     * 
     */
    public List<Betrieb> getBetrieb() {
        if (betrieb == null) {
            betrieb = new ArrayList<Betrieb>();
        }
        return this.betrieb;
    }

    /**
     * Ruft den Wert der lieferung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Lieferung }
     *     
     */
    public Lieferung getLieferung() {
        return lieferung;
    }

    /**
     * Legt den Wert der lieferung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Lieferung }
     *     
     */
    public void setLieferung(Lieferung value) {
        this.lieferung = value;
    }

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
     * Ruft den Wert der produkt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Produkt }
     *     
     */
    public Produkt getProdukt() {
        return produkt;
    }

    /**
     * Legt den Wert der produkt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Produkt }
     *     
     */
    public void setProdukt(Produkt value) {
        this.produkt = value;
    }

    /**
     * Ruft den Wert der zusatzparameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Zusatzparameter }
     *     
     */
    public Zusatzparameter getZusatzparameter() {
        return zusatzparameter;
    }

    /**
     * Legt den Wert der zusatzparameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Zusatzparameter }
     *     
     */
    public void setZusatzparameter(Zusatzparameter value) {
        this.zusatzparameter = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der produktionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProduktionId() {
        return produktionId;
    }

    /**
     * Legt den Wert der produktionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProduktionId(String value) {
        this.produktionId = value;
    }

}
