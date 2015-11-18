/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.update.p2.deploy

import groovyx.net.http.ContentType;
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

import java.text.SimpleDateFormat

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.entity.FileEntity
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.protocol.HttpContext

class DeployToBintray {

	static boolean USE_PROXY = true
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
		def artifactsFile = new File("${UPDATE_SITE}/${ARTIFACTS_JAR}")
		def contentFile = new File("${UPDATE_SITE}/${CONTENT_JAR}")
		def featuresDir = new File("${UPDATE_SITE}/${FEATURES}")
		def pluginsDir = new File("${UPDATE_SITE}/${PLUGINS}")

		if (!artifactsFile.exists() || !contentFile.exists()) {
			println "p2 files cannot be found"
			return
		}

		println "user:"
		def user = new Scanner(System.in).nextLine()
		println "password:"
		def password = new Scanner(System.in).nextLine()
		println ""

		if (featuresDir.exists() && pluginsDir.exists()) {
			def version = new SimpleDateFormat("yyyy_MM_dd").format(new Date())
			createVersion(user, password, version)

			for (File f : featuresDir.listFiles()) {
				uploadFile(user, password, version, FEATURES, f)
			}

			for (File f : pluginsDir.listFiles()) {
				uploadFile(user, password, version, PLUGINS, f)
			}
		}

		uploadFile(user, password, null, "", artifactsFile)
		uploadFile(user, password, null, "", contentFile)
	}

	static void createVersion(String user, String password, String version) {
		def client = getClient(user, password)
		def url = "packages/${SUBJECT}/${REPO}/${PACKAGE}/versions"

		println "Create version"
		println "Name:\t${version}"
		println "URL:\t${url}"
		HttpResponseDecorator response = client.post(
				path: url,
				contentType: ContentType.JSON,
				requestContentType: ContentType.JSON,
				body: [name: version, desc: version])
		println "Status:\t${response.status}"
		println ""
		assert 201 == response.status
	}

	static void uploadFile(String user, String password, String version, String path, File file) {
		def client = getClient(user, password)
		def url = "content/${SUBJECT}/${REPO}/${path}/${file.name}"

		if (version != null) {
			url += ";bt_package=${PACKAGE};bt_version=${version};publish=1"
		}

		println "Upload file"
		println "Name:\t${file.name}"
		println "URL:\t${url}"
		HttpResponseDecorator response = client.put(
				path: url,
				contentType: ContentType.JSON,
				requestContentType: ContentType.BINARY,
				body: new FileInputStream(file))
		println "Status:\t${response.status}"
		println ""
		assert 201 == response.status
	}

	static RESTClient getClient(String user, String password) {
		def bintrayClient = new RESTClient("https://bintray.com/api/v1/")
		AbstractHttpClient http = bintrayClient.client
		def basic = (user + ":" + password).bytes.encodeBase64().toString()

		http.addRequestInterceptor(
				new HttpRequestInterceptor() {
					void process(HttpRequest httpRequest, HttpContext httpContext) {
						httpRequest.addHeader('Cache-Control', 'no-cache, no-store, no-transform, must-revalidate')
						httpRequest.addHeader('Pragma', 'no-cache')
						httpRequest.addHeader('Authorization', 'Basic ' + basic)
					}
				})
		if (USE_PROXY)
			bintrayClient.setProxy(PROXY, PORT, null)
		bintrayClient
	}
}
