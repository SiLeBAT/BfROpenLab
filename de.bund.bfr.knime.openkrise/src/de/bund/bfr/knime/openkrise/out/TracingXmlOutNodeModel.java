package de.bund.bfr.knime.openkrise.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.GregorianCalendar;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.image.ImageContent;
import org.knime.core.data.image.ImageValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.db.imports.custom.nrw.out.NRW_Exporter;
import de.nrw.verbraucherschutz.idv.daten.Analyseergebnis;
import de.nrw.verbraucherschutz.idv.daten.Bewertung;
import de.nrw.verbraucherschutz.idv.daten.Content;
import de.nrw.verbraucherschutz.idv.daten.Dokument;
import de.nrw.verbraucherschutz.idv.daten.KatalogWert;
import de.nrw.verbraucherschutz.idv.daten.Kontrollpunktbewertung;
import de.nrw.verbraucherschutz.idv.daten.Meldung;
import de.nrw.verbraucherschutz.idv.daten.Metadaten;
import de.nrw.verbraucherschutz.idv.daten.Warenbewegungsbewertung;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of TracingXmlOut.
 * 
 *
 * @author BfR
 */
public class TracingXmlOutNodeModel extends NodeModel {
 	static final String CFGKEY_SAVE = "Save";
    static final String DEFAULT_SAVE = "";

    private final SettingsModelString m_save =
        new SettingsModelString(TracingXmlOutNodeModel.CFGKEY_SAVE,
                    TracingXmlOutNodeModel.DEFAULT_SAVE);
    

    /**
     * Constructor for the node model.
     */
    protected TracingXmlOutNodeModel() {   
		super(new PortType[] {BufferedDataTable.TYPE, BufferedDataTable.TYPE, ImagePortObject.TYPE},
				new PortType[] {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final PortObject[] inObjects,
            final ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = (BufferedDataTable) inObjects[0]; // Stations
		//BufferedDataTable edgeTable = (BufferedDataTable) inObjects[1]; // Deliveries
		ImagePortObject imageObj = (ImagePortObject) inObjects[2];
		DataCell imageCellDC = imageObj.toDataCell();

	    if (!(imageCellDC instanceof ImageValue)) {
	        throw new InvalidSettingsException("Image object does not produce"
	                + " valid image object but "
	                + imageCellDC.getClass().getName());
	    }

	    ImageValue v = (ImageValue) imageCellDC;
	    ImageContent m_content = v.getImageContent();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    m_content.save(baos);
	    baos.close();

		Analyseergebnis ae = new Analyseergebnis();
		ae.setMeldung(getMeldung());
		
		// Report hierrein
		Dokument doc = new Dokument();
		Content content = new Content();
		content.setContentType("TracingView");
		content.setValue(baos.toByteArray());
		doc.setContent(content);
		ae.getDokument().add(doc);
		Metadaten md = new Metadaten();
		md.setAutor("BfR");
		md.setDokId("");
		md.setDokumentName("");
		md.setBeschreibung("");
		md.setFormat("");
		md.setKategorie("");
		md.setLokation("");
		md.setMimeType("");
		md.setOrganisation("");
		md.setTitel("");
		md.setVersion("");
		md.setPubliziertAm(getDate());
		md.setUploadAm(getDate());
		doc.setMetadaten(md);
		
		// Scores hierrein
		Kontrollpunktbewertung kpb = new Kontrollpunktbewertung(); 
		kpb.setNummer("");
		int idIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.STATION_ID);
		int nameIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.STATION_NAME);
		int tobIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.STATION_TOB);
		int scoreIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.SCORE);
		for (DataRow row : nodeTable) {
			Double score = scoreIndex < 0 ? 0.0 : IO.getDouble(row.getCell(scoreIndex));
			String name = nameIndex < 0 ? "" : IO.getCleanString(row.getCell(nameIndex));
			String id = idIndex < 0 ? "" : IO.getCleanString(row.getCell(idIndex));
			String tob = tobIndex < 0 ? "" : IO.getCleanString(row.getCell(tobIndex));
			Warenbewegungsbewertung wbb = new Warenbewegungsbewertung();
			wbb.setValue(new BigDecimal(score));
			wbb.setBetrieb(name);
			wbb.setId(id);
			wbb.setTyp(tob);
			kpb.getWarenbewegungsbewertung().add(wbb);
		}		
		Bewertung b = new Bewertung();
		ae.setBewertung(b);
		b.setKontrollpunktbewertung(kpb);
		NRW_Exporter e = new NRW_Exporter();
		ByteArrayOutputStream soap = e.doExport(ae, m_save.getStringValue(), true);
		if (soap != null) {
		    File tempFile = File.createTempFile("t", ".soap");
		    FileOutputStream fos = new FileOutputStream(tempFile); 
			try {
			    soap.writeTo(fos);
			} catch(IOException ioe) {
				this.setWarningMessage(ioe.getMessage());
			    ioe.printStackTrace();
			} finally {
			    fos.close();
			}
			upload("bfr_admin", "Ifupofetu843", tempFile, 0);
		}
		else {
			this.setWarningMessage("soap is null");
		}

		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_save.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_save.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_save.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }
    
    private Meldung getMeldung() throws DatatypeConfigurationException {
    	Meldung meldung = new Meldung();
    	meldung.setFallBezeichnung("BBK");
    	meldung.setFallNummer("2016-111");
    	meldung.setNummer("2016-123");
    	meldung.setStatus("GUELTIG");
    	KatalogWert kw = new KatalogWert();
    	kw.setCode("123456");
    	kw.setKatalog("001");
    	kw.setScope("BUND");
    	kw.setValue("Amt für BfR");
    	kw.setVersion("1.28");
    	kw.setVerz("ADV");
    	meldung.setMeldendeBehoerde(kw);
    	meldung.setMeldungVom(getDate());
    	return meldung;
    }
    private XMLGregorianCalendar getDate() throws DatatypeConfigurationException {
    	GregorianCalendar c = new GregorianCalendar();
    	c.setTimeInMillis(System.currentTimeMillis());
    	XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    	return date2;
    }
	
	private void upload(String usr, String pwd, File file, long id) throws Exception {
	    final Client client = ClientBuilder.newBuilder()
	    		.register(HttpAuthenticationFeature.basic(usr, pwd))
	    		.register(MultiPartFeature.class)
	    		.build();
	    WebTarget t = client.target(UriBuilder.fromUri("https://foodrisklabs.bfr.bund.de/de.bund.bfr.busstopp/").build()).path("rest").path("items").path(""+id).path("upload");

	    FileDataBodyPart filePart = new FileDataBodyPart("file", file);
	    filePart.setContentDisposition(FormDataContentDisposition.name("file").fileName("report.soap").build()); // file.getName()

	    FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
	    MultiPart multipartEntity = formDataMultiPart.bodyPart(filePart);

	    Response response = t.request().post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA));
	    System.out.println(response.getStatus() + " \n" + response.readEntity(String.class));

	    response.close();
	    formDataMultiPart.close();
	    multipartEntity.close();
	}
	
}

