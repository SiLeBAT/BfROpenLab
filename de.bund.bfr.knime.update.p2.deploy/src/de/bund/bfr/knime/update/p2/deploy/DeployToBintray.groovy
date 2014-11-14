/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.update.p2.deploy

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import groovyx.net.http.HttpResponseDecorator;
import groovyx.net.http.RESTClient

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.entity.FileEntity
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.protocol.HttpContext

class DeployToBintray {

	static String PROXY = "webproxy"
	static int PORT = 8080

	static String SUBJECT = "silebat"
	static String REPO = "generic"
	static String PACKAGE = "update"

	static String ARTIFACTS_JAR = "artifacts.jar"
	static String CONTENT_JAR = "content.jar"
	static String FEATURES = "features"
	static String PLUGINS = "plugins"

	static String UPDATE_SITE = "../de.bund.bfr.knime.update.p2"

	static main(args) {
		File artifactsFile = new File("${UPDATE_SITE}/${ARTIFACTS_JAR}")
		File contentFile = new File("${UPDATE_SITE}/${CONTENT_JAR}")
		File featuresDir = new File("${UPDATE_SITE}/${FEATURES}")
		File pluginsDir = new File("${UPDATE_SITE}/${PLUGINS}")

		if (!artifactsFile.exists() || !contentFile.exists() || !featuresDir.exists() || !pluginsDir.exists()) {
			println "p2 files cannot be found"
			return
		}

		println "user:"
		String user = new Scanner(System.in).nextLine()
		println "password:"
		String password = new Scanner(System.in).nextLine()
		println ""

		String version = new SimpleDateFormat("yyyy_MM_dd").format(new Date())

		createVersion(user, password, version)
		uploadFile(user, password, null, "", artifactsFile)
		uploadFile(user, password, null, "", contentFile)

		for (File f : featuresDir.listFiles()) {
			uploadFile(user, password, version, FEATURES, f)
		}

		for (File f : pluginsDir.listFiles()) {
			uploadFile(user, password, version, PLUGINS, f)
		}
	}

	static void createVersion(String user, String password, String version) {
		RESTClient client = getClient(user, password)
		String url = "packages/${SUBJECT}/${REPO}/${PACKAGE}/versions"
		println "Create version"
		println "Name:\t${version}"
		println "URL:\t${url}"
		HttpResponseDecorator response = client.post(
				path: url,
				contentType: "application/json",
				requestContentType: "application/json",
				body: [name: version, desc: version])
		println "Status:\t${response.status}"
		println ""
		assert 201 == response.status
	}

	static void uploadFile(String user, String password, String version, String path, File file) {
		RESTClient client = getClient(user, password)
		String url = "content/${SUBJECT}/${REPO}/${path}/${file.name}"

		if (version != null) {
			url += ";bt_package=${PACKAGE};bt_version=${version};publish=1"
		}

		println "Upload file"
		println "Name:\t${file.name}"
		println "URL:\t${url}"
		client.encoder.putAt("application/file", { f -> new FileEntity(f, "application/file") })
		HttpResponseDecorator response = client.put(
				path: url, body: file, contentType: "application/json", requestContentType: "application/file")
		println "Status:\t${response.status}"
		println ""
		assert 201 == response.status
	}

	static RESTClient getClient(String user, String password) {
		RESTClient bintrayClient = new RESTClient("https://api.bintray.com/")
		AbstractHttpClient http = bintrayClient.client
		String basic = (user + ":" + password).bytes.encodeBase64().toString()

		http.addRequestInterceptor(
				new HttpRequestInterceptor() {
					void process(HttpRequest httpRequest, HttpContext httpContext) {
						httpRequest.addHeader('Cache-Control', 'no-cache, no-store, no-transform, must-revalidate')
						httpRequest.addHeader('Pragma', 'no-cache')
						httpRequest.addHeader('Authorization', 'Basic ' + basic)
					}
				})
		bintrayClient.setProxy(PROXY, PORT, null)
		bintrayClient
	}
}
