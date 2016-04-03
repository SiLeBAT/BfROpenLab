package de.bund.bfr.busstopp.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.w3c.dom.Document;

public class Tester {
    private static final String TRUSTSTORE_FILE = "C:/Users/weiser/tomcat/keystore/client.jks";
    private static final String TRUSTSTORE_PASSWORD = "bfrbfr";
    private static final String APP_URL = "https://localhost:8443/de.bund.bfr.busstopp";
    
	static {
	    //for localhost testing only
	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){

	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {
	            if (hostname.equals("localhost")) {
	                return true;
	            }
	            return false;
	        }
	    });
	}

    public static void main(String[] args) throws Exception {

	  URL defaultImage = Tester.class.getResource("/de/bund/bfr/busstopp/client/userdata.xml");
	  File file = new File(defaultImage.toURI());
	  System.out.println(file.exists());
	  DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	  DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	  Document document = documentBuilder.parse(file);
	  String usr = document.getElementsByTagName("user").item(0).getTextContent();
	  String pwd = document.getElementsByTagName("password").item(0).getTextContent();	  
	  
    ClientConfig config = new ClientConfig();
    config.register(MultiPartFeature.class);
    config.property(ClientProperties.FOLLOW_REDIRECTS, true);
    Client client = ClientBuilder.newClient(config);
    client.register(HttpAuthenticationFeature.basic(usr, pwd));
    //getCertClient(usr, pwd);
    
    WebTarget service = client.target(getBaseURI());
    //((Builder) service).header(HttpHeaders.USER_AGENT, "application/json");
    
    // Get the Todos
    System.out.println(service.path("rest").path("items").request().accept(MediaType.TEXT_XML).get(String.class));

    // Get XML for application
    System.out.println(service.path("rest").path("items").request().accept(MediaType.APPLICATION_XML).get(String.class));

    //Delete ItemLoader with id 1
    Response response = service.path("rest").path("items").path("1459295825378").request().delete(); System.out.println("Form response " + response.getStatus() + "\n" + response.readEntity(String.class));

    //Upload a ItemLoader
    //upload(usr, pwd, "C:/Users/Armin/Desktop/Pressemitteilung.docx", "kommentar_gs");
    //upload(usr, pwd, "C:/Users/weiser/Desktop/NRW.txt", "kommentar_e154");
    upload(usr, pwd, "C:/Users/weiser/Desktop/lims_todo.txt", "kommentar_tel");
  }

  private static URI getBaseURI() {
	    return UriBuilder.fromUri("http://localhost:8080/de.bund.bfr.busstopp").build();
	    //return UriBuilder.fromUri("https://foodrisklabs.bfr.bund.de/de.bund.bfr.busstopp/").build();
  }
  private static Client getCertClient(String usr, String pwd) throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
	  SslConfigurator sslConfig = SslConfigurator.newInstance()
		        .trustStoreFile(TRUSTSTORE_FILE)
		        .trustStorePassword(TRUSTSTORE_PASSWORD)
		        .keyStoreFile(TRUSTSTORE_FILE)
		        .keyPassword(TRUSTSTORE_PASSWORD);
		 
		SSLContext sslContext = sslConfig.createSSLContext();
		

      Client client = ClientBuilder.newBuilder().sslContext(sslContext).build();
      //client.register(HttpAuthenticationFeature.digest(usr, pwd));

      WebTarget service = client.target(APP_URL); // APP_URL
      System.out.println(service.request().get());
      
      System.out.println(service.path("rest").path("items").request().accept(MediaType.TEXT_XML).get(String.class));
      //Response response = service.request().get();
      //System.out.println(response.readEntity(String.class));
      
      
	  return client;	  
  }
  
	public static void upload(String usr, String pwd, String fileName, String comment) throws Exception {
	    final Client client = ClientBuilder.newBuilder()
	    		.register(HttpAuthenticationFeature.basic(usr, pwd))
	    		.register(MultiPartFeature.class)
	    		.build();
	    WebTarget t = client.target(getBaseURI()).path("rest").path("items").path("upload");

	    File f = new File(fileName);
	    FileDataBodyPart filePart = new FileDataBodyPart("file", f);
	    filePart.setContentDisposition(FormDataContentDisposition.name("file").fileName(f.getName()).build());

	    FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
	    MultiPart multipartEntity = formDataMultiPart.field("comment", comment).bodyPart(filePart);

	    Response response = t.request().post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA));
	    System.out.println(response.getStatus() + " \n" + response.readEntity(String.class));

	    response.close();
	    formDataMultiPart.close();
	    multipartEntity.close();
	}
} 