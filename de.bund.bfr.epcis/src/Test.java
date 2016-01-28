import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.bund.bfr.epcis.EPCISBodyType;
import de.bund.bfr.epcis.EPCISDocumentType;
import de.bund.bfr.epcis.EPCISHeaderType;
import de.bund.bfr.epcis.ObjectFactory;
import de.bund.bfr.epcis.Partner;
import de.bund.bfr.epcis.PartnerIdentification;
import de.bund.bfr.epcis.StandardBusinessDocumentHeader;

public class Test {

	private static final ObjectFactory FACTORY = new ObjectFactory();

	public static void main(String[] args) {	
		// HEADER
		
		Partner bauernhof = createPartner("Bauernhof");
		Partner molkerei = createPartner("Molkerei");
		StandardBusinessDocumentHeader standardHeader = FACTORY.createStandardBusinessDocumentHeader();
		
		standardHeader.setHeaderVersion("First Version");
		standardHeader.getSender().add(bauernhof);
		standardHeader.getReceiver().add(molkerei);
		
		EPCISHeaderType header = FACTORY.createEPCISHeaderType();
		
		header.setStandardBusinessDocumentHeader(standardHeader);
		
		// BODY		
		
		EPCISBodyType body = FACTORY.createEPCISBodyType();		
		
		// DOCUMENT
		
		EPCISDocumentType doc = FACTORY.createEPCISDocumentType();
		
		doc.setEPCISHeader(header);
		doc.setEPCISBody(body);
		
		try {
			Marshaller jaxbMarshaller = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName())
					.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(FACTORY.createEPCISDocument(doc), System.out);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private static Partner createPartner(String id) {
		PartnerIdentification ident = FACTORY.createPartnerIdentification();
				
		ident.setValue(id);
		
		Partner p = FACTORY.createPartner();
		
		p.setIdentifier(ident);
		
		return p;
	}
}
