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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Warenbestand complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Warenbestand">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="betrieb" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Betrieb" maxOccurs="unbounded"/>
 *         &lt;element name="warenumfang" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Warenumfang"/>
 *         &lt;element name="produkt" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Produkt"/>
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
@XmlType(name = "Warenbestand", propOrder = {
    "betrieb",
    "warenumfang",
    "produkt"
})
public class Warenbestand {

    @XmlElement(required = true)
    protected List<Betrieb> betrieb;
    @XmlElement(required = true)
    protected Warenumfang warenumfang;
    @XmlElement(required = true)
    protected Produkt produkt;
    @XmlAttribute(name = "id")
    protected String id;

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
