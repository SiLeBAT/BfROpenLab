/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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