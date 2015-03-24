/*******************************************************************************
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Thöns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * Jörgen Brandt (BfR)
 * Annemarie Käsbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
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
 ******************************************************************************/
/**
 * 
 */
package org.hsh.bfr.db;

/**
 * @author Weiser
 *
 */

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseCarverXML {

	private String[] processNames;
	private int[] processIDs;
	private LinkedHashMap<Integer, Integer> carverXMLIndex_processID = new LinkedHashMap<>();
	private LinkedHashMap<Integer, String> carverXMLIndex_processNames = new LinkedHashMap<>();
	private Vector<Integer[]> org_dst = new Vector <>();	
	
	public ParseCarverXML(String filename) throws SAXException, IOException, ParserConfigurationException {
	    File file = File.createTempFile("ParseCarverXML", ".pex");
	    file.deleteOnExit();
		replaceStrInFile(filename, "utf-16", "utf-8", file);
		//File file = new File(filename);
	  	  
  	  	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	  	DocumentBuilder db = dbf.newDocumentBuilder();
  	  
  	  	Document doc = db.parse(file);
  	  	doc.getDocumentElement().normalize();
  	  
	  	NodeList nodeLst = doc.getElementsByTagName("XmlDiagram");
	  	if (nodeLst.getLength() > 0) {
	  		Element displayTextElmnt = (Element) nodeLst.item(0);
	  	    NodeList displayText = displayTextElmnt.getChildNodes();
	  	    String xmlStr = (((Node) displayText.item(0)).getNodeValue());
	  	    InputStream bais = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
	  	    doc = db.parse(bais);
	  	}
	  	
	  	doIt(doc);
	  	
		//replaceStrInFile(filename, "utf-8", "utf-16");
	}
	private void doIt(Document doc) throws SAXException, IOException, ParserConfigurationException {
		int carverXMLIndex;
  	
		NodeList nodeLst = doc.getElementsByTagName("ProcessElementNode");
  	  	processNames = new String[nodeLst.getLength()];
  	  	processIDs = new int[nodeLst.getLength()];
  	  	//carverXMLIndex2processIDMap = new int[nodeLst.getLength()];
    
	  	  for (int s = 0; s < nodeLst.getLength(); s++) {
	  	    Node cNode = nodeLst.item(s);
	  	    
	  	    if (cNode.getNodeType() == Node.ELEMENT_NODE) {
	  	  
	  	      Element cElmnt = (Element) cNode;
	  	      carverXMLIndex = Integer.parseInt(cElmnt.getAttribute("Index"));
	  	      
	  	      NodeList displayTextElmntLst = cElmnt.getElementsByTagName("DisplayText");
	  	      Element displayTextElmnt = (Element) displayTextElmntLst.item(0);
	  	      NodeList displayText = displayTextElmnt.getChildNodes();
	  	      processNames[s] = displayText.item(0) != null ? ((Node) displayText.item(0)).getNodeValue() : "";
	  	      carverXMLIndex_processNames.put(carverXMLIndex, processNames[s]);
	  	    
		      NodeList processIDElmntLst = cElmnt.getElementsByTagName("ProcessElement");
		      Element processIDElmnt = (Element) processIDElmntLst.item(0);
		      NodeList processID = processIDElmnt.getChildNodes();
		      String ID = ((Node) processID.item(0)).getNodeValue();
		      processIDs[s] = Integer.parseInt(ID.substring(0,ID.length()-1));
			  carverXMLIndex_processID.put(carverXMLIndex, processIDs[s]);
	  	    }
	  	  } 
  	  
	  	  nodeLst = doc.getElementsByTagName("MaterialFlowLink");
	  	  for (int s = 0; s < nodeLst.getLength(); s++) {
	  	    Node cNode = nodeLst.item(s);
	  	    
	  	    if (cNode.getNodeType() == Node.ELEMENT_NODE) {
	  	  
	  	      Element cElmnt = (Element) cNode;
	  	      carverXMLIndex = Integer.parseInt(cElmnt.getAttribute("Index"));
	  	      int ori = Integer.parseInt(cElmnt.getAttribute("Org"));
	          int dst = Integer.parseInt(cElmnt.getAttribute("Dst"));
	  	      
	          // Doppelte Pfeile wegmachen. Haben die irgendeinen Grund?
	          if (!oriDstExists(ori,dst)) org_dst.add(new Integer[]{ori,dst});
	   
	  	    }
	  	  } 
	}
	private boolean oriDstExists(int ori, int dst) {
		boolean result = false;
		for (int i=0;i<org_dst.size();i++) {
			Integer[] int2 = org_dst.get(i);
			if (int2[0] == ori && int2[1] == dst) {
				result = true;
				break;
			}
		}
		return result;
	}
	/*
	private void replaceStrInFile(String filename, String search, String replace) throws IOException {
		replaceStrInFile(filename, search, replace, new File(filename));
	}
	*/
	private void replaceStrInFile(String filename, String search, String replace, File newFile) throws IOException {
		BufferedReader reader = null;
	    BufferedWriter writer = null;
	    StringBuffer buffer = null;

	    buffer = new StringBuffer();
	    reader = new BufferedReader(new FileReader(filename));
	 	String line = null;
	 		
	 	while((line = reader.readLine()) != null) {
	           
	                if(line.contains(search)) {
	                	buffer.append(line.replace(search, replace) + System.getProperty("line.separator"));
	                }
	                else{
	                	buffer.append(line + System.getProperty("line.separator"));
	                }
	   }
	 	reader.close();

	 	writer = new BufferedWriter(new FileWriter(newFile));
	    writer.write(buffer.toString());
	    writer.flush();
	    writer.close();	           
	}	  	  
	  	      
/*
	public int[] getProcessIDs() {
		return this.processIDs;	
	}	
	public String[] getProcessNames() {
		return this.processNames;	
	}	
	*/
	public LinkedHashMap<Integer, Integer> getCarverIDProcessID() {
		return this.carverXMLIndex_processID;
	}
	public LinkedHashMap<Integer, String> getCarverIDProcessName() {
		return this.carverXMLIndex_processNames;
	}
	
	public Vector<Integer[]> getOrgDst() {
		return this.org_dst;
	}
	

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		
		ParseCarverXML P = new ParseCarverXML("C:/Dokumente und Einstellungen/WiggerJ/Eigene Dateien/Carver/Fleisch.xml");
		LinkedHashMap<Integer, Integer> CP = P.getCarverIDProcessID();
		Vector<Integer[]> OD = P.getOrgDst();
		
    	for (Map.Entry<Integer, Integer> entry : CP.entrySet()) {
			System.out.println(	"Carver ID: " + entry.getKey() + 
					" ProcessID: " + entry.getValue());	
    	}
		
		System.out.println();
		
		for (int i=0; i<OD.size(); i++)
		{
			System.out.println(	"Origin: " + OD.get(i)[0] + 
								" Destination: " + OD.get(i)[1]);	
		}
		
	}
	
}

