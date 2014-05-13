/*******************************************************************************
 * Copyright (c) 2013 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
package de.bund.bfr.knime.update.p2.deploy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import com.sun.xml.internal.messaging.saaj.util.Base64;

public class DeployToBintray {

	private static final String SUBJECT = "thoens";
	private static final String REPO = "test";
	private static final String PACKAGE = "test";

	public static void main(String[] args) {
		System.setProperty("https.proxyHost", "webproxy");
		System.setProperty("https.proxyPort", "8080");

		String user = readFromSystemIn("user");
		String password = readFromSystemIn("password");
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
		String date = dateFormat.format(new Date());		

		try {
			createNewVersion(user, password, date);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File dir = new File("../de.bund.bfr.knime.update.p2");

		System.out.println(Arrays.asList(dir.listFiles()));
	}

	private static String readFromSystemIn(String name) {
		System.out.println(name + ":");

		Scanner in = new Scanner(System.in);
		String value = in.nextLine();

		return value;
	}

	private static void createNewVersion(String user, String password,
			String version) throws IOException {
		String userpass = user + ":" + password;
		String basicAuth = "Basic "
				+ new String(Base64.encode(userpass.getBytes()));
		URL url = new URL("https://api.bintray.com/packages/" + SUBJECT + "/"
				+ REPO + "/" + PACKAGE + "/versions");
		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
		String json = "{\"name\":\"" + version + "\"}";

		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Authorization", basicAuth);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());

		wr.writeBytes(json);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
		}

		in.close();
	}
}
