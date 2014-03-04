/*
 * OctaveScriptNode - A KNIME node that runs Octave scripts
 * Copyright (C) 2011 Andre-Patrick Bubel (pvs@andre-bubel.de) and
 *                    Parallel and Distributed Systems Group (PVS),
 *                    University of Heidelberg, Germany
 * Website: http://pvs.ifi.uni-heidelberg.de/
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
 */
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.OctaveObject;

/**
 * This class runs a Octave script with one optional input variable and one
 * result
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class OctaveScriptRunner {

	// The variables that are provided as an input to the script
	private Map<String, OctaveObject> inVariables;

	// The variable name that is returned from the script
	private String outVariableName;

	// The output message from last run
	private String lastOutput;

	public OctaveScriptRunner() {
		this("");
	}

	public OctaveScriptRunner(final OctaveObject inVariable,
			final String inVariableName, final String outVariableName) {
		super();
		this.inVariables = new HashMap<String, OctaveObject>();
		this.addInVariable(inVariableName, inVariable);
		this.outVariableName = outVariableName;
	}

	public OctaveScriptRunner(final String outVariableName) {
		this(null, "", outVariableName);
	}

	/**
	 * Adds a variable. If it already exists, overwrites it.
	 * 
	 * @param name
	 * @param content
	 */
	public void addInVariable(String name, OctaveObject content) {
		this.inVariables.put(name, content);
	}

	/**
	 * @return the variable with given name
	 */
	public OctaveObject getInVariable(String name) {
		return inVariables.get(name);
	}

	public String getOutVariableName() {
		return outVariableName;
	}

	/**
	 * @throws IOException
	 */
	public OctaveObject run(final String script) throws IOException {
		StringWriter errorStringWriter = new StringWriter();
		StringWriter outputStringWriter = new StringWriter();

		OctaveEngineFactory factory = new OctaveEngineFactory();
		factory.setErrorWriter(errorStringWriter);

		OctaveEngine engine = factory.getScriptEngine();
		engine.setWriter(outputStringWriter);

		OctaveObject result = null;

		try {
			for (String variableName : inVariables.keySet())
				if (variableNameIsValid(variableName)
						&& variableContentIsValid(inVariables.get(variableName))) {
					engine.put(variableName, inVariables.get(variableName));
				}

			engine.eval(script);
			result = engine.get(getOutVariableName());
		} catch (OctaveEvalException e) {
			throw e;
		} finally {
			engine.close();
			errorStringWriter.close();
			outputStringWriter.close();
		}

		lastOutput = outputStringWriter.toString();

		return result;
	}

	public String getLastOutput() {
		return lastOutput;
	}

	public void setOutVariableName(final String outVariableName) {
		this.outVariableName = outVariableName;
	}

	private boolean variableContentIsValid(OctaveObject octaveObject) {
		// TODO Auto-generated method stub
		return octaveObject != null;
	}

	private boolean variableNameIsValid(String variableName) {
		// TODO Auto-generated method stub
		return variableName != "";
	}

}
