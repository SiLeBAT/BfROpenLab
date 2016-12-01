//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.12.01 um 11:24:07 AM CET 
//


package de.nrw.verbraucherschutz.idv.daten;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für WareneingaengeVerwendet complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="WareneingaengeVerwendet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="wareneingangVerwendet" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}WareneingangVerwendet" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WareneingaengeVerwendet", propOrder = {
    "wareneingangVerwendet"
})
public class WareneingaengeVerwendet {

    protected List<WareneingangVerwendet> wareneingangVerwendet;

    /**
     * Gets the value of the wareneingangVerwendet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wareneingangVerwendet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWareneingangVerwendet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WareneingangVerwendet }
     * 
     * 
     */
    public List<WareneingangVerwendet> getWareneingangVerwendet() {
        if (wareneingangVerwendet == null) {
            wareneingangVerwendet = new ArrayList<WareneingangVerwendet>();
        }
        return this.wareneingangVerwendet;
    }

}
