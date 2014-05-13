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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import com.sun.xml.internal.messaging.saaj.util.Base64;

public class DeployToBintray {

	private static final String SUBJECT = "thoens";
	private static final String REPO = "test";
	private static final String PACKAGE = "test";

	private static final String ARTIFACTS_JAR = "artifacts.jar";
	private static final String CONTENT_JAR = "content.jar";
	private static final String FEATURES = "features";
	private static final String PLUGINS = "plugins";

	private static final String UPDATE_SITE = "../de.bund.bfr.knime.update.p2";

	public static void main(String[] args) {
		System.setProperty("https.proxyHost", "webproxy");
		System.setProperty("https.proxyPort", "8080");

		File artifactsFile = new File(UPDATE_SITE + "/" + ARTIFACTS_JAR);
		File contentFile = new File(UPDATE_SITE + "/" + CONTENT_JAR);
		File featuresDir = new File(UPDATE_SITE + "/" + FEATURES);
		File pluginsDir = new File(UPDATE_SITE + "/" + PLUGINS);

		if (!artifactsFile.exists() || !contentFile.exists()
				|| !featuresDir.exists() || !pluginsDir.exists()) {
			System.err.println("p2 files cannot be found");
		}

		String user = readFromSystemIn("user");
		String password = readFromSystemIn("password");
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
		String version = dateFormat.format(new Date());

		try {
			createNewVersion(user, password, version);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		try {
			uploadFile(user, password, version, artifactsFile, ARTIFACTS_JAR);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		try {
			uploadFile(user, password, version, contentFile, CONTENT_JAR);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		for (File f : featuresDir.listFiles()) {
			if (f.getName().endsWith(".jar")) {
				try {
					uploadFile(user, password, version, f, PACKAGE + "/"
							+ version + "/" + PLUGINS + "/" + f.getName());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		}

		for (File f : pluginsDir.listFiles()) {
			if (f.getName().endsWith(".jar")) {
				try {
					uploadFile(user, password, version, f, PACKAGE + "/"
							+ version + "/" + FEATURES + "/" + f.getName());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		}
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
		System.out.println("JSON : " + json);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
		}

		in.close();
	}

	private static void uploadFile(String user, String password,
			String version, File file, String toPath) throws IOException {
		String userpass = user + ":" + password;
		String basicAuth = "Basic "
				+ new String(Base64.encode(userpass.getBytes()));
		URL url = new URL("https://api.bintray.com/content/" + SUBJECT + "/"
				+ REPO + "/" + toPath);
		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Authorization", basicAuth);
		con.setRequestMethod("PUT");

		InputStream input = new FileInputStream(file);
		OutputStream output = con.getOutputStream();
		byte[] buffer = new byte[256];
		int bytesRead = 0;

		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}

		output.flush();
		output.close();

		int responseCode = con.getResponseCode();

		System.out.println("\nSending 'PUT' request to URL : " + url);
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
