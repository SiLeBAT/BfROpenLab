//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.08.30 um 04:08:29 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.nrw.verbraucherschutz.idv.daten package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Metadaten_QNAME = new QName("http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument", "metadaten");
    private final static QName _Katalogisiert_QNAME = new QName("http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem", "katalogisiert");
    private final static QName _Content_QNAME = new QName("http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument", "content");
    private final static QName _KatalogWert_QNAME = new QName("http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem", "katalogWert");
    private final static QName _Dokument_QNAME = new QName("http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument", "dokument");
    private final static QName _Kontrollpunktmeldung_QNAME = new QName("http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", "kontrollpunktmeldung");
    private final static QName _Analyseergebnis_QNAME = new QName("http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", "analyseergebnis");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.nrw.verbraucherschutz.idv.daten
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Kontrollpunktmeldung }
     * 
     */
    public Kontrollpunktmeldung createKontrollpunktmeldung() {
        return new Kontrollpunktmeldung();
    }

    /**
     * Create an instance of {@link Analyseergebnis }
     * 
     */
    public Analyseergebnis createAnalyseergebnis() {
        return new Analyseergebnis();
    }

    /**
     * Create an instance of {@link Dokument }
     * 
     */
    public Dokument createDokument() {
        return new Dokument();
    }

    /**
     * Create an instance of {@link Metadaten }
     * 
     */
    public Metadaten createMetadaten() {
        return new Metadaten();
    }

    /**
     * Create an instance of {@link Content }
     * 
     */
    public Content createContent() {
        return new Content();
    }

    /**
     * Create an instance of {@link Referenz }
     * 
     */
    public Referenz createReferenz() {
        return new Referenz();
    }

    /**
     * Create an instance of {@link Warenbewegung }
     * 
     */
    public Warenbewegung createWarenbewegung() {
        return new Warenbewegung();
    }

    /**
     * Create an instance of {@link ZusatzparameterDatum }
     * 
     */
    public ZusatzparameterDatum createZusatzparameterDatum() {
        return new ZusatzparameterDatum();
    }

    /**
     * Create an instance of {@link Kontrollpunktbewertung }
     * 
     */
    public Kontrollpunktbewertung createKontrollpunktbewertung() {
        return new Kontrollpunktbewertung();
    }

    /**
     * Create an instance of {@link Zusatzparameter }
     * 
     */
    public Zusatzparameter createZusatzparameter() {
        return new Zusatzparameter();
    }

    /**
     * Create an instance of {@link Ausloeser }
     * 
     */
    public Ausloeser createAusloeser() {
        return new Ausloeser();
    }

    /**
     * Create an instance of {@link Warenbewegungsbewertung }
     * 
     */
    public Warenbewegungsbewertung createWarenbewegungsbewertung() {
        return new Warenbewegungsbewertung();
    }

    /**
     * Create an instance of {@link Wareneingaenge }
     * 
     */
    public Wareneingaenge createWareneingaenge() {
        return new Wareneingaenge();
    }

    /**
     * Create an instance of {@link AnzahlGebinde }
     * 
     */
    public AnzahlGebinde createAnzahlGebinde() {
        return new AnzahlGebinde();
    }

    /**
     * Create an instance of {@link MengeEinheit }
     * 
     */
    public MengeEinheit createMengeEinheit() {
        return new MengeEinheit();
    }

    /**
     * Create an instance of {@link Wareneingang }
     * 
     */
    public Wareneingang createWareneingang() {
        return new Wareneingang();
    }

    /**
     * Create an instance of {@link Bewertung }
     * 
     */
    public Bewertung createBewertung() {
        return new Bewertung();
    }

    /**
     * Create an instance of {@link Meldung }
     * 
     */
    public Meldung createMeldung() {
        return new Meldung();
    }

    /**
     * Create an instance of {@link Produktionen }
     * 
     */
    public Produktionen createProduktionen() {
        return new Produktionen();
    }

    /**
     * Create an instance of {@link Warenausgaenge }
     * 
     */
    public Warenausgaenge createWarenausgaenge() {
        return new Warenausgaenge();
    }

    /**
     * Create an instance of {@link Lieferung }
     * 
     */
    public Lieferung createLieferung() {
        return new Lieferung();
    }

    /**
     * Create an instance of {@link Warenbestaende }
     * 
     */
    public Warenbestaende createWarenbestaende() {
        return new Warenbestaende();
    }

    /**
     * Create an instance of {@link Warenumfang }
     * 
     */
    public Warenumfang createWarenumfang() {
        return new Warenumfang();
    }

    /**
     * Create an instance of {@link Produkt }
     * 
     */
    public Produkt createProdukt() {
        return new Produkt();
    }

    /**
     * Create an instance of {@link Produktion }
     * 
     */
    public Produktion createProduktion() {
        return new Produktion();
    }

    /**
     * Create an instance of {@link WareneingaengeVerwendet }
     * 
     */
    public WareneingaengeVerwendet createWareneingaengeVerwendet() {
        return new WareneingaengeVerwendet();
    }

    /**
     * Create an instance of {@link Betrieb }
     * 
     */
    public Betrieb createBetrieb() {
        return new Betrieb();
    }

    /**
     * Create an instance of {@link ZusatzparameterZahl }
     * 
     */
    public ZusatzparameterZahl createZusatzparameterZahl() {
        return new ZusatzparameterZahl();
    }

    /**
     * Create an instance of {@link ZusatzparameterText }
     * 
     */
    public ZusatzparameterText createZusatzparameterText() {
        return new ZusatzparameterText();
    }

    /**
     * Create an instance of {@link Warenbestand }
     * 
     */
    public Warenbestand createWarenbestand() {
        return new Warenbestand();
    }

    /**
     * Create an instance of {@link Warenausgang }
     * 
     */
    public Warenausgang createWarenausgang() {
        return new Warenausgang();
    }

    /**
     * Create an instance of {@link WareneingangVerwendet }
     * 
     */
    public WareneingangVerwendet createWareneingangVerwendet() {
        return new WareneingangVerwendet();
    }

    /**
     * Create an instance of {@link Katalogisiert }
     * 
     */
    public Katalogisiert createKatalogisiert() {
        return new Katalogisiert();
    }

    /**
     * Create an instance of {@link KatalogWert }
     * 
     */
    public KatalogWert createKatalogWert() {
        return new KatalogWert();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Metadaten }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument", name = "metadaten")
    public JAXBElement<Metadaten> createMetadaten(Metadaten value) {
        return new JAXBElement<Metadaten>(_Metadaten_QNAME, Metadaten.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Katalogisiert }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem", name = "katalogisiert")
    public JAXBElement<Katalogisiert> createKatalogisiert(Katalogisiert value) {
        return new JAXBElement<Katalogisiert>(_Katalogisiert_QNAME, Katalogisiert.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Content }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument", name = "content")
    public JAXBElement<Content> createContent(Content value) {
        return new JAXBElement<Content>(_Content_QNAME, Content.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KatalogWert }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem", name = "katalogWert")
    public JAXBElement<KatalogWert> createKatalogWert(KatalogWert value) {
        return new JAXBElement<KatalogWert>(_KatalogWert_QNAME, KatalogWert.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Dokument }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument", name = "dokument")
    public JAXBElement<Dokument> createDokument(Dokument value) {
        return new JAXBElement<Dokument>(_Dokument_QNAME, Dokument.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Kontrollpunktmeldung }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", name = "kontrollpunktmeldung")
    public JAXBElement<Kontrollpunktmeldung> createKontrollpunktmeldung(Kontrollpunktmeldung value) {
        return new JAXBElement<Kontrollpunktmeldung>(_Kontrollpunktmeldung_QNAME, Kontrollpunktmeldung.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Analyseergebnis }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", name = "analyseergebnis")
    public JAXBElement<Analyseergebnis> createAnalyseergebnis(Analyseergebnis value) {
        return new JAXBElement<Analyseergebnis>(_Analyseergebnis_QNAME, Analyseergebnis.class, null, value);
    }

}
