package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.out;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
//import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class MyNamespaceMapper extends NamespacePrefixMapper {

	  private static final String NS1_PREFIX = "tran";
	  private static final String NS1_URI = "http://verbraucherschutz.nrw.de/idv/dienste/2016.2/warenrueckverfolgung/transport";
	  private static final String NS2_PREFIX = "wrv";
	  private static final String NS2_URI = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung";
	  private static final String NS3_PREFIX = "kat";
	  private static final String NS3_URI = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/katalogsystem";
	  private static final String NS4_PREFIX = "dok";
	  private static final String NS4_URI = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument";
	  private static final String NS5_PREFIX = "kom";
	  private static final String NS5_URI = "http://verbraucherschutz.nrw.de/idv/daten/2010.1/kommunikation";

  @Override
  public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
      if(NS1_URI.equals(namespaceUri)) {
          return NS1_PREFIX;
      } else if(NS2_URI.equals(namespaceUri)) {
          return NS2_PREFIX;
      } else if(NS3_URI.equals(namespaceUri)) {
          return NS3_PREFIX;
      } else if(NS4_URI.equals(namespaceUri)) {
          return NS4_PREFIX;
      } else if(NS5_URI.equals(namespaceUri)) {
          return NS5_PREFIX;
      }
      return suggestion;
  }

  @Override
  public String[] getPreDeclaredNamespaceUris() {
      return new String[] {NS1_URI,NS2_URI,NS3_URI,NS4_URI,NS5_URI};
  }
  
  public static String[] getPreDeclaredNamespacePrefixes() {
      return new String[] {NS1_PREFIX,NS2_PREFIX,NS3_PREFIX,NS4_PREFIX,NS5_PREFIX};
  }
}