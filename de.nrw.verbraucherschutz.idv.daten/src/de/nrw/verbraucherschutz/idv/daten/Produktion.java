//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.08.31 um 01:45:20 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für produktion complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="produktion">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="produkt" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}produkt"/>
 *         &lt;element name="produktionsumfang" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}warenumfang"/>
 *         &lt;element name="produziertAm" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="wareneingaengeVerwendet" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}wareneingaengeVerwendet"/>
 *         &lt;element name="zusatzparameter" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}zusatzparameter"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "produktion", propOrder = {
    "produkt",
    "produktionsumfang",
    "produziertAm",
    "wareneingaengeVerwendet",
    "zusatzparameter"
})
public class Produktion {

    @XmlElement(required = true)
    protected Produkt produkt;
    @XmlElement(required = true)
    protected Warenumfang produktionsumfang;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar produziertAm;
    @XmlElement(required = true)
    protected WareneingaengeVerwendet wareneingaengeVerwendet;
    @XmlElement(required = true, nillable = true)
    protected Zusatzparameter zusatzparameter;
    @XmlAttribute(name = "id")
    protected String id;

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
     * Ruft den Wert der produktionsumfang-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Warenumfang }
     *     
     */
    public Warenumfang getProduktionsumfang() {
        return produktionsumfang;
    }

    /**
     * Legt den Wert der produktionsumfang-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Warenumfang }
     *     
     */
    public void setProduktionsumfang(Warenumfang value) {
        this.produktionsumfang = value;
    }

    /**
     * Ruft den Wert der produziertAm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getProduziertAm() {
        return produziertAm;
    }

    /**
     * Legt den Wert der produziertAm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setProduziertAm(XMLGregorianCalendar value) {
        this.produziertAm = value;
    }

    /**
     * Ruft den Wert der wareneingaengeVerwendet-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link WareneingaengeVerwendet }
     *     
     */
    public WareneingaengeVerwendet getWareneingaengeVerwendet() {
        return wareneingaengeVerwendet;
    }

    /**
     * Legt den Wert der wareneingaengeVerwendet-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link WareneingaengeVerwendet }
     *     
     */
    public void setWareneingaengeVerwendet(WareneingaengeVerwendet value) {
        this.wareneingaengeVerwendet = value;
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

}
