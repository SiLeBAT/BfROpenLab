/*******************************************************************************
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Th�ns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * J�rgen Brandt (BfR)
 * Annemarie K�sbohrer (BfR)
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
package org.hsh.bfr.db.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.hsh.bfr.db.MyDBI;
import org.hsh.bfr.db.MyDBTablesNew;
import org.hsh.bfr.db.MyTable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class XmlLoader {

	private static XStream xstream = getXStream();
	
	private static XStream getXStream() {
		XStream xstream = new XStream(null, new XppDriver(),new ClassLoaderReference(MyDBI.class.getClassLoader()));
		xstream.omitField(MyDBI.class, "conn");
		xstream.omitField(MyDBI.class, "dbUsername");
		xstream.omitField(MyDBI.class, "dbPassword");
		xstream.omitField(MyDBI.class, "dbPath");
		xstream.omitField(MyDBI.class, "path2XmlFile");
		xstream.omitField(MyDBI.class, "isServerConnection");
		xstream.omitField(MyDBI.class, "isAdminConnection");
		xstream.omitField(MyDBI.class, "passFalse");
		xstream.omitField(MyDBI.class, "filledHashtables");
		xstream.omitField(MyDBTablesNew.class, "isPmm");
		xstream.omitField(MyDBTablesNew.class, "isKrise");
		xstream.omitField(MyDBTablesNew.class, "isSiLeBAT");
		//xstream.omitField(MyTable.class, "rowHeights");
		xstream.omitField(MyTable.class, "colWidths");
		xstream.omitField(MyTable.class, "sortKeyList");
		xstream.omitField(MyTable.class, "searchString");
		xstream.omitField(MyTable.class, "selectedRow");
		xstream.omitField(MyTable.class, "selectedCol");
		xstream.omitField(MyTable.class, "verticalScrollerPosition");
		xstream.omitField(MyTable.class, "horizontalScrollerPosition");
		xstream.omitField(MyTable.class, "form_SelectedID");
		xstream.omitField(MyTable.class, "caller4Trigger");
		xstream.omitField(MyTable.class, "mnSQL");
		return xstream;
	}
	private static String getXml(MyDBI myDBi) {
		String xml = xstream.toXML(myDBi);		
		return xml;
	}
	public static void save2File(String xmlFile, MyDBI myDBi) {
		try {
			File file = new File(xmlFile);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(getXml(myDBi));
			output.close();
	    }
		catch (IOException e) {
			e.printStackTrace();
	    }
	}
	public static Object getObjectFromFile(String xmlFile) {
		Object result = null;
		BufferedReader br = null;
	    try {
			br = new BufferedReader(new FileReader(xmlFile));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        result = getObject(sb.toString());
	    }
	    catch (Exception e) {}
	    finally {
	        if (br != null) {
				try {
					br.close();
				}
		        catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }
	    return result;
	}
	private static Object getObject(String xml) {
		return xstream.fromXML(xml);
	}
}
