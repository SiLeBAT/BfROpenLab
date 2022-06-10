/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

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
		
		URL url = JsonValidator.class.getResource(PKGBASE + "/" + SCHEMA_FOLDER + "/" + JSON_SCHEMA);
		
		if (url == null) {
			
			throw new SchemaValidationException("Could not find JSON schema.", null);
			
		} else {
			
			try {
				return JsonLoader.fromURL(url);	
				
			} catch(IOException ex) {
				throw new SchemaValidationException("JSON schema could not be loaded.", ex);
			}
		}
	}
	
	public static boolean isJsonValid(JsonNode jsonNode) throws SchemaValidationException {
		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		
		try {
			final JsonNode schemaJson = loadJsonSchema();

			final JsonSchema schema = factory.getJsonSchema(schemaJson);


			ProcessingReport report = schema.validate(jsonNode);
			
			if (report == null) {
				
				throw new SchemaValidationException("JSON validation failed.", null);
				
			} else if (!report.isSuccess()) {
				
				Iterator<ProcessingMessage> iterator = report.iterator();
				
				while(iterator.hasNext()) {
					ProcessingMessage pm = iterator.next();
					if (pm.getLogLevel() == LogLevel.ERROR || pm.getLogLevel() == LogLevel.FATAL ) {
						throw new SchemaValidationException("JSON is not valid (" + formatProcessingMessage(pm) + ").", null);
					}
				}
				
				throw new SchemaValidationException("JSON is not valid (" + report.toString() + ").", null);
			}
	    
			return true;
			
		} catch(ProcessingException ex) {
			
			throw new SchemaValidationException("JSON schema could not be processed.", ex );
		}	
	}
	
	private static String formatProcessingMessage(ProcessingMessage pm) {
		JsonNode jsonNode = pm.asJson();
		
		if (pm.getMessage().equals("err.common.typeNoMatch") ) {
			try {
				String instancePointer = jsonNode.get("instance").get("pointer").asText();
				String expectedType = arrNodeToString(jsonNode.get("expected"), " | ");
				String foundType = jsonNode.get("found").asText();
				return "Invalid type for '" + instancePointer + "' (expected: " + expectedType + ", found: " + foundType + ").";
			} catch(Exception ex) {
			}
		} else if (pm.getMessage().equals("err.common.object.missingMembers")) {
			String instancePointer = jsonNode.get("instance").get("pointer").asText();
			String missingProps = "'" + arrNodeToString(jsonNode.get("missing"), "', '") + "'";
			return "Object '" + instancePointer + "' is missing propert(y/ies) " + missingProps + ".";
		}
		return pm.toString();
	}
	
	private static String arrNodeToString(JsonNode arrNode, String joiner) {
		List<String> list = new ArrayList<>(); 
		arrNode.iterator().forEachRemaining((node) -> list.add(node.asText()));
		return list.stream().collect(Collectors.joining(joiner));
	}
}
