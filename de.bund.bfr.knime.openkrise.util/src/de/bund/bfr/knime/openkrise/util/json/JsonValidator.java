/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.util.json;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

public class JsonValidator {

	public static class SchemaValidationException extends Exception {
		/**
		 *
		 */
		private static final long serialVersionUID = -7620611556917902144L;

		public SchemaValidationException(String message, Exception originatingException) {
			super(message);
		}
	}

	private static final String SCHEMA_FOLDER = "res";
	private static final String JSON_SCHEMA = "schema-v1.json";

	private static JsonNode loadJsonSchema() throws SchemaValidationException {
		final String pkgName = Utils.class.getPackage().getName();
		final String PKGBASE = '/' + pkgName.replace(".", "/");
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);

		final String resourceName = PKGBASE + "/" + SCHEMA_FOLDER + "/" + JSON_SCHEMA;

		URL url = JsonValidator.class.getResource(resourceName);


		if (url == null) {

			throw new SchemaValidationException("Could not find JSON schema.", null);

		} else {

			try {

				InputStream resourceAsStream = JsonValidator.class.getResourceAsStream(resourceName);
				JsonSchema jsonSchema = factory.getSchema(resourceAsStream);
				return jsonSchema.getSchemaNode();

			} catch(Exception ex) {
				throw new SchemaValidationException("JSON schema could not be loaded.", ex);
			}
		}
	}

	public static boolean isJsonValid(JsonNode jsonNode) throws SchemaValidationException {
		final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);

		try {
			final JsonNode schemaJsonNode = loadJsonSchema();

			final JsonSchema schema = factory.getSchema(schemaJsonNode);

			Set<ValidationMessage> validationMsgSet = schema.validate(jsonNode);

			if (validationMsgSet == null) {

				throw new SchemaValidationException("JSON validation failed.", null);

			} else if (!validationMsgSet.isEmpty()) {

				Iterator<ValidationMessage> iterator = validationMsgSet.iterator();

				while(iterator.hasNext()) {
					ValidationMessage vm = iterator.next();
					throw new SchemaValidationException("JSON is not valid (" + vm.getMessage() + ").", null);
				}

				throw new SchemaValidationException("JSON is not valid (" + validationMsgSet.toString() + ").", null);
			}

			return true;

		} catch(SchemaValidationException ex) {

			throw ex;

		} catch(Exception ex) {

			throw new SchemaValidationException("JSON schema could not be processed.", ex );
		}
	}
}
