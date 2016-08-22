package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.in;

import java.io.File;

import javax.swing.JProgressBar;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.bund.bfr.knime.openkrise.db.imports.MyImporter;

public class KontrollpunktMeldung implements MyImporter {
	
	public static void main(String[] args) {
		new KontrollpunktMeldung().doImport("", null, true);
	}
	
	@Override
	public boolean doImport(String filename, JProgressBar progress, boolean showResults) {
		boolean result = true;
    	String path = this.getClass().getResource("/de/bund/bfr/knime/openkrise/db/imports/custom/nrw/in/xml/l2b_kontrollpunktmeldung-v0.2.xml").getFile();
    	File xmlDatei = new File(path);
		try {
	    	// root element
	        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	    	Document doc = docBuilder.newDocument();
	    	doc.setXmlStandalone(true);

	    	path = this.getClass().getResource("/de/bund/bfr/knime/openkrise/db/imports/custom/nrw/in/dienste/de.nrw.verbraucherschutz.idv.daten.2016.2.20160818.warenrueckverfolgung.transport.schema.xsd").getFile();
	    	File xsdDatei = new File(path);
	        	                	        	
	    	// Validation method 1
	    	    
	    	final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	    	final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	    	final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    	factory.setNamespaceAware(true);
	    	factory.setValidating(true);
	    	         
	    	factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

	    	// Set the schema file
	    	factory.setAttribute(JAXP_SCHEMA_SOURCE, xsdDatei);

	    	try {
	    		DocumentBuilder parser = factory.newDocumentBuilder();
	    		parser.setErrorHandler(new MyErrorHandler(this));
	    		parser.parse(xmlDatei.getAbsolutePath()); // xmlFile.getStringValue()
	    	    }
	    	catch (SAXException e) {
	            result = false;
	    	    e.printStackTrace();
	    	}

	    	
	    	// Validation method 2

	    	Source schemaFile = new StreamSource(xsdDatei); // xsdFile.getStringValue()
	        Source xmlDateiSource = new StreamSource(xmlDatei);
	        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = schemaFactory.newSchema(schemaFile);
	        Validator validator = schema.newValidator();
	        
	        try{
	            validator.validate(xmlDateiSource);
	            System.out.println(xmlDateiSource.getSystemId() + " is valid");
	        }
	        catch (SAXException e) {
	            result = false;
	            System.out.println(xmlDateiSource.getSystemId() + " is NOT valid");
	            System.out.println("Reason: " + e.getLocalizedMessage());
	        }			
		}
		catch (Exception e) {e.printStackTrace();}
		if (result) System.out.println("xml is valid!!!");
		return result;
	}

    class MyErrorHandler implements ErrorHandler {
    	private KontrollpunktMeldung kpm;
    	MyErrorHandler(KontrollpunktMeldung kpm) {
    		this.kpm = kpm;
    	}
        public void warning(SAXParseException exception) throws SAXException {
            // Bring things to a crashing halt
        	String message = "**Parsing Warning**" +
                    "  Line:    " + 
                    exception.getLineNumber() + "" +
                 "  URI:     " + 
                    exception.getSystemId() + "" +
                 "  Message: " + 
                    exception.getMessage();
            System.out.println(message);        
    		//kpm.setWarningMessage(message);
            throw new SAXException("Warning encountered");
        }
        public void error(SAXParseException exception) throws SAXException {
            // Bring things to a crashing halt
        	String message = "**Parsing Error**" +
                    "  Line:    " + 
                    exception.getLineNumber() + "" +
                 "  URI:     " + 
                    exception.getSystemId() + "" +
                 "  Message: " + 
                    exception.getMessage();
            System.out.println(message);        
    		//kpm.setWarningMessage(message);
            throw new SAXException("Error encountered");
        }
        public void fatalError(SAXParseException exception) throws SAXException {
            // Bring things to a crashing halt
        	String message = "**Parsing Fatal Error**" +
                    "  Line:    " + 
                    exception.getLineNumber() + "" +
                 "  URI:     " + 
                    exception.getSystemId() + "" +
                 "  Message: " + 
                    exception.getMessage();
            System.out.println(message);        
    		//kpm.setWarningMessage(message);
            throw new SAXException("Fatal Error encountered");
        }
    }
}
