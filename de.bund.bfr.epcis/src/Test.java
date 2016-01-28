import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.bund.bfr.epcis.EPCISDocumentType;
import de.bund.bfr.epcis.ObjectFactory;

public class Test {

	private static final ObjectFactory FACTORY = new ObjectFactory();

	public static void main(String[] args) {
		EPCISDocumentType doc = FACTORY.createEPCISDocumentType();

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
}
