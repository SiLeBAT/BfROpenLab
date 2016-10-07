package de.bund.bfr.knime.openkrise.common;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

public class Constants {

	public static URI getServerURI() {
		//return UriBuilder.fromUri("https://foodrisklabs.bfr.bund.de/de.bund.bfr.busstopp/").build();
		return UriBuilder.fromUri("http://localhost:8080/de.bund.bfr.busstopp/").build();
	}
	public static String getServerUsr() {
		return "";
	}
	public static String getServerPwd() {
		return "";		
	}
}
