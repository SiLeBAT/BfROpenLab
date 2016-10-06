//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.10.06 um 01:11:47 AM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Warenausgaenge complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Warenausgaenge">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="warenausgang" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Warenausgang" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Warenausgaenge", propOrder = {
    "warenausgang"
})
public class Warenausgaenge {

    protected List<Warenausgang> warenausgang;

    /**
     * Gets the value of the warenausgang property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the warenausgang property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWarenausgang().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Warenausgang }
     * 
     * 
     */
    public List<Warenausgang> getWarenausgang() {
        if (warenausgang == null) {
            warenausgang = new ArrayList<Warenausgang>();
        }
        return this.warenausgang;
    }

}
