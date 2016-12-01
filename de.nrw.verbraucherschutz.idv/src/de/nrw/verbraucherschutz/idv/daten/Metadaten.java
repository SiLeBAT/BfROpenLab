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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für metadaten complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="metadaten">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dokId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dokumentName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="autor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="uploadAm" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="titel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="kategorie" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="beschreibung" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mimeType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="organisation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lokation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="format" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="publiziertAm" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="referenzen" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument}referenz" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "metadaten", namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument", propOrder = {
    "dokId",
    "dokumentName",
    "autor",
    "uploadAm",
    "titel",
    "kategorie",
    "beschreibung",
    "mimeType",
    "organisation",
    "lokation",
    "format",
    "publiziertAm",
    "version",
    "referenzen"
})
public class Metadaten {

    @XmlElement(required = true, nillable = true)
    protected String dokId;
    @XmlElement(required = true)
    protected String dokumentName;
    @XmlElement(required = true)
    protected String autor;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar uploadAm;
    @XmlElement(required = true)
    protected String titel;
    protected String kategorie;
    protected String beschreibung;
    protected String mimeType;
    protected String organisation;
    protected String lokation;
    protected String format;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar publiziertAm;
    @XmlElement(required = true)
    protected String version;
    protected List<Referenz> referenzen;

    /**
     * Ruft den Wert der dokId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDokId() {
        return dokId;
    }

    /**
     * Legt den Wert der dokId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDokId(String value) {
        this.dokId = value;
    }

    /**
     * Ruft den Wert der dokumentName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDokumentName() {
        return dokumentName;
    }

    /**
     * Legt den Wert der dokumentName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDokumentName(String value) {
        this.dokumentName = value;
    }

    /**
     * Ruft den Wert der autor-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAutor() {
        return autor;
    }

    /**
     * Legt den Wert der autor-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAutor(String value) {
        this.autor = value;
    }

    /**
     * Ruft den Wert der uploadAm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUploadAm() {
        return uploadAm;
    }

    /**
     * Legt den Wert der uploadAm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUploadAm(XMLGregorianCalendar value) {
        this.uploadAm = value;
    }

    /**
     * Ruft den Wert der titel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitel() {
        return titel;
    }

    /**
     * Legt den Wert der titel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitel(String value) {
        this.titel = value;
    }

    /**
     * Ruft den Wert der kategorie-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKategorie() {
        return kategorie;
    }

    /**
     * Legt den Wert der kategorie-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKategorie(String value) {
        this.kategorie = value;
    }

    /**
     * Ruft den Wert der beschreibung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBeschreibung() {
        return beschreibung;
    }

    /**
     * Legt den Wert der beschreibung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBeschreibung(String value) {
        this.beschreibung = value;
    }

    /**
     * Ruft den Wert der mimeType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Legt den Wert der mimeType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMimeType(String value) {
        this.mimeType = value;
    }

    /**
     * Ruft den Wert der organisation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisation() {
        return organisation;
    }

    /**
     * Legt den Wert der organisation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisation(String value) {
        this.organisation = value;
    }

    /**
     * Ruft den Wert der lokation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLokation() {
        return lokation;
    }

    /**
     * Legt den Wert der lokation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLokation(String value) {
        this.lokation = value;
    }

    /**
     * Ruft den Wert der format-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Legt den Wert der format-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Ruft den Wert der publiziertAm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getPubliziertAm() {
        return publiziertAm;
    }

    /**
     * Legt den Wert der publiziertAm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setPubliziertAm(XMLGregorianCalendar value) {
        this.publiziertAm = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the referenzen property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenzen property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenzen().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Referenz }
     * 
     * 
     */
    public List<Referenz> getReferenzen() {
        if (referenzen == null) {
            referenzen = new ArrayList<Referenz>();
        }
        return this.referenzen;
    }

}
