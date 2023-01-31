/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.image.ImageContent;
import org.knime.core.data.image.ImageValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.ICredentials;
import org.knime.core.node.workflow.VariableType;

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
import de.nrw.verbraucherschutz.idv.daten.Referenz;
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
		//BufferedDataTable nodeTable = (BufferedDataTable) inObjects[0]; // Stations
		BufferedDataTable edgeTable = (BufferedDataTable) inObjects[1]; // Deliveries
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

		Map<String, FlowVariable> fvm = this.getAvailableInputFlowVariables(new VariableType[]{ VariableType.StringType.INSTANCE });
		FlowVariable fv = fvm.get("Fallnummer");
		String fallNummer = fv == null ? null : fv.getStringValue();
		fv = fvm.get("Server");
		String server = fv == null ? null : fv.getStringValue();
		if (server == null || server.trim().isEmpty()) throw new Exception("Server not known");
		fv = fvm.get("ClientID");
		String environment = fv == null ? null : fv.getStringValue();
		if (environment == null || environment.trim().isEmpty()) throw new Exception("Client ID not known");
		fv = fvm.get("Fallbezeichnung");
		String fallBezeichnung = fv == null ? null : fv.getStringValue();
		
		NRW_Exporter e = new NRW_Exporter();
		
		// Fall
		Analyseergebnis ae = new Analyseergebnis();
		
		// Report hierrein
		Dokument doc = new Dokument();
		Content content = new Content();
		content.setContentType("image/png");//"application/pdf");
		content.setValue(baos.toByteArray());
		doc.setContent(content);
		ae.getDokument().add(doc);
		Metadaten md = new Metadaten();
		/*
		String auftragsNummer = "";
		for (int i=0;;i++) {
			if (!fvm.containsKey("Auftragsnummer_" + i)) break;
			FlowVariable fv = fvm.get("Auftragsnummer_" + i);
			Referenz r = new Referenz();
			auftragsNummer = fv.getStringValue(); // "2016-93"
			r.setKey("AUFTRAGNR"); r.setValue(auftragsNummer);
			md.getReferenzen().add(r);
		}
		*/
		Referenz r = new Referenz();
		r.setKey("AUFTRAGNR"); r.setValue(fallNummer);
		md.getReferenzen().add(r);
		
		md.setAutor("BfR");
		md.setDokId(null);
		md.setDokumentName("Analyse Fall Nr. " + fallNummer + ".png");
		md.setBeschreibung("Analyseergebnis des BfRs für den Fall " + fallNummer);
		md.setFormat(".png"); // .pdf
		md.setKategorie("Analyseergebnis");
		md.setLokation(""); // Fall
		md.setMimeType("image/png");//"application/pdf");
		md.setOrganisation("BfR");
		md.setTitel("Analyse Ergebnis Fall Nr. " + fallNummer);
		md.setVersion(getReadableDate()); // "1.0", hier sollte hochgezählt werden
		md.setPubliziertAm(getDate());
		md.setUploadAm(getDate());
		doc.setMetadaten(md);

		ae.setMeldung(getMeldung(fallBezeichnung, fallNummer, fallNummer));
		export(e, server, environment, ae, "bfr_fall_report");
		
		// Bewertungen / Scores	
		int kpnIndex = edgeTable.getSpec().findColumnIndex("Kontrollpunktnummer");
		int weIndex = edgeTable.getSpec().findColumnIndex("WE_ID");
		int waIndex = edgeTable.getSpec().findColumnIndex("WA_ID");
		int typIndex = edgeTable.getSpec().findColumnIndex("BetriebsTyp");
		int scoreIndex = edgeTable.getSpec().findColumnIndex(TracingColumns.LOT_SCORE); // TracingColumns.SCORE

		Analyseergebnis aes = new Analyseergebnis();
		String analyseNummer = "" + System.currentTimeMillis();
		aes.setMeldung(getMeldung(fallBezeichnung, fallNummer, analyseNummer));
		Bewertung b = new Bewertung();
		aes.setBewertung(b);
		HashMap<String, Kontrollpunktbewertung> hm = new HashMap<>();
		for (DataRow row : edgeTable) {
			String auftragsNummer = kpnIndex < 0 ? "" : IO.getCleanString(row.getCell(kpnIndex)); 
			Kontrollpunktbewertung kpb = null;
			if (hm.containsKey(auftragsNummer)) {
				kpb = hm.get(auftragsNummer);
			}
			else {
				kpb = new Kontrollpunktbewertung();
				kpb.setNummer(auftragsNummer);
				b.getKontrollpunktbewertung().add(kpb);
				hm.put(auftragsNummer, kpb);
			}
					
				// Scores
				Double score = scoreIndex < 0 ? 0.0 : IO.getDouble(row.getCell(scoreIndex));
				// WA WE Warenausgang Wareneingang
				String typ = ""; 
				// Der Betrieb entspricht einem der beiden möglichen Betriebstypen eines Wareneingangs ('ORT_ABHOLUNG', LIEFERANT') oder Warenausgangs('KUNDE','ORT_ANLIEFERUNG')
				String betrieb = ""; 
				// id entspricht dem Wert des id Attribute des Wareneingang oder Warenausgang Elements in der Kontrollpunktmeldung.
				String id = weIndex < 0 ? "" : IO.getCleanString(row.getCell(weIndex)); 
				if (id == null) {
					typ = "WA";
					betrieb = "ORT_ANLIEFERUNG"; // KUNDE
					id = waIndex < 0 ? "" : IO.getCleanString(row.getCell(waIndex));
				}
				else {
					typ = "WE";
					betrieb = "ORT_ABHOLUNG"; // LIEFERANT
				}
				if (typIndex >= 0) betrieb = IO.getCleanString(row.getCell(typIndex)); 
				
				Warenbewegungsbewertung wbb = new Warenbewegungsbewertung();
				wbb.setValue(new BigDecimal(score));
				wbb.setBetrieb(betrieb);
				wbb.setId(id);
				wbb.setTyp(typ);
			
			kpb.getWarenbewegungsbewertung().add(wbb);
		}
		
		export(e, server, environment, aes, "bfr_score_report");
	
		return null;
    }
    private boolean export(NRW_Exporter e, String server, String environment, Analyseergebnis ae, String filename) throws Exception {
		ByteArrayOutputStream soap = e.doExport(ae, true);
		if (soap != null) {
		    File tempFile = File.createTempFile(filename, ".soap");
		    FileOutputStream fos = new FileOutputStream(tempFile); 
			try {
			    soap.writeTo(fos);
			} catch(IOException ioe) {
				this.setWarningMessage(ioe.getMessage());
			    ioe.printStackTrace();
			} finally {
			    fos.close();
			}
			return upload(server, environment, tempFile, false);
		}
		else {
			this.setWarningMessage("soap is null");
			return false;
		}
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
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
    
    private Meldung getMeldung(String fallBezeichnung, String fallNummer, String nummer) throws DatatypeConfigurationException {		
    	Meldung meldung = new Meldung();
    	meldung.setFallBezeichnung(fallBezeichnung);
    	meldung.setFallNummer(fallNummer);
    	meldung.setNummer(nummer);
    	meldung.setStatus("GUELTIG");
    	
    	KatalogWert kw = new KatalogWert();
    	kw.setCode("001");
    	kw.setKatalog("AMTSKENNUNG");
    	kw.setScope("BUND");
    	kw.setValue("Bundesinstitut für Risikobewertung");
    	kw.setVersion("1.0");
    	kw.setVerz("BFR");
    	meldung.setMeldendeBehoerde(kw);
    	meldung.setMeldungVom(buildXmlDate());
    	return meldung;
    }
    private XMLGregorianCalendar buildXmlDate() throws DatatypeConfigurationException {
    	Date date = new Date();
        return date==null ? null : DatatypeFactory.newInstance().newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd").format(date));
    }
    private XMLGregorianCalendar getDate() throws DatatypeConfigurationException {
    	GregorianCalendar c = new GregorianCalendar();
    	c.setTimeInMillis(System.currentTimeMillis());
    	XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    	return date2;
    }
    private String getReadableDate() {
    	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    	Date dt = new Date();
    	String S = sdf.format(dt);
    	return S;
    }
    
	private boolean upload(String server, String environment, File file, boolean dryRun) throws Exception {
		boolean result = true;
/*
	    JerseyClient client = new JerseyClientBuilder()
	    		.register(HttpAuthenticationFeature.basic("user", "pass")) // set.getUser(), set.getPass()
	    		.register(MultiPartFeature.class)
	    		.build();
	    JerseyWebTarget t = client.target(UriBuilder.fromUri(server).build()).path("rest").path("items").path("upload");
*/

		URL url = new URL(server);
		String protocol = url.getProtocol();
		String host = url.getHost();
		int port = url.getPort();
		if (port == -1) port = url.getDefaultPort();
		System.out.println(protocol);
		System.out.println(host);
		System.out.println(port);

		String fn = file.getName();
	    fn = fn.substring(0, fn.lastIndexOf("_report") + 7); // die tempnummer am ende des filenamens noch wegoperieren!
		System.out.println(fn);

		ICredentials cp = this.getCredentialsProvider().get("busstop");
    	UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(cp.getLogin(), cp.getPassword().toCharArray());
    	BasicCredentialsProvider provider = new BasicCredentialsProvider();
    	provider.setCredentials(new AuthScope(host, port), credentials);

    	HttpClientBuilder clientbuilder = HttpClients.custom();
    	clientbuilder = clientbuilder.setDefaultCredentialsProvider(provider);
    	CloseableHttpClient httpclient = clientbuilder.build();
	    HttpPost httpPost = new HttpPost(server + "rest/items/upload");
	 
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	    builder.addTextBody("comment", "Analysis from BfR");
	    builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, fn);
	    HttpEntity multipart = builder.build();
	    httpPost.setEntity(multipart);
	    
	    //FileDataBodyPart filePart = new FileDataBodyPart("file", file);
	    //filePart.setContentDisposition(FormDataContentDisposition.name("file").fileName(fn).build());

	    //FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
	    //MultiPart multipartEntity = formDataMultiPart.field("comment", "Analysis from BfR").bodyPart(filePart);

	    if (!dryRun) {
	    	/*
		    //Response response = t.queryParam("environment", environment).request().post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA));
		    System.out.println(response.getStatus() + " \n" + response.readEntity(String.class));

		    response.close();
		    */
		 
		    CloseableHttpResponse response = httpclient.execute(httpPost);
		    
		    int code = response.getCode();
		    
		    response.close();

		    System.out.println(code);
		    result = (code == 200);
		    if (!result) throw new Exception("Upload did not succeed! (Code: " + code + ")");
	    }
	    
	    //formDataMultiPart.close();
	    //multipartEntity.close();
	    
	    return result;
	}
	
}

