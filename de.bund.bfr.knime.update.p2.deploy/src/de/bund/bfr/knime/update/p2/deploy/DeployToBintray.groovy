/*******************************************************************************
 * Copyright (c) 2014-2023 Federal Institute for Risk Assessment (BfR), Germany
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

	static USE_PROXY = false
	static PROXY = "webproxy"
	static PORT = 8080

	static SUBJECT = "silebat"
	static REPO = "generic"
	static PACKAGE = "update"

	static ARTIFACTS_JAR = "artifacts.jar"
	static CONTENT_JAR = "content.jar"
	static FEATURES = "features"
	static PLUGINS = "plugins"

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
		println "API Key:"
		def password = new Scanner(System.in).nextLine()
		println ""

		def client = new RESTClient("https://bintray.com/api/v1/")

		client.headers['Authorization'] = 'Basic '+"${user}:${password}".bytes.encodeBase64()

		if (USE_PROXY)
			client.setProxy(PROXY, PORT, null)

		if (featuresDir.exists() && pluginsDir.exists()) {
			def version = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date())
			createVersion(client, version)

			for (File f : featuresDir.listFiles()) {
				uploadFile(client, version, FEATURES, f)
			}

			for (File f : pluginsDir.listFiles()) {
				uploadFile(client, version, PLUGINS, f)
			}
		}

		uploadFile(client, null, "", artifactsFile)
		uploadFile(client, null, "", contentFile)
	}

	static void createVersion(RESTClient client, String version) {
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

	static void uploadFile(RESTClient client, String version, String path, File file) {
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
}
