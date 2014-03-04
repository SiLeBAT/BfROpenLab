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
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import org.testng.annotations.Test;

import de.uni_heidelberg.ifi.pvs.knime.octavescriptnode.OctaveScriptRunner;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveObject;
import dk.ange.octave.type.OctaveString;

/**
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class OctaveScriptRunnerTest {

	@Test
	public void allConstructorTest() throws IOException {
		OctaveObject o = new OctaveString("foobar");

		OctaveScriptRunner runner_all = new OctaveScriptRunner(o, "in", "out");

		OctaveObject result = runner_all.run("out = in");
		assertThat(((OctaveString) result).getString(), equalTo("foobar"));
	}

	@Test(expectedExceptions = OctaveEvalException.class)
	public void errorTest() throws IOException {
		OctaveObject o = new OctaveString("foobar");

		OctaveScriptRunner runner = new OctaveScriptRunner(o, "in", "out");

		runner.run("invalid");
		// assertThat(runner.hasError(), equalTo(true));
	}

	@Test
	public void missingVariableTest() throws IOException {
		OctaveScriptRunner runner = new OctaveScriptRunner();

		assertThat(runner.getInVariable("missing"), nullValue());
	}

	@Test
	public void noVariableConstructorTest() throws IOException {
		OctaveScriptRunner runner_no_variable = new OctaveScriptRunner();

		OctaveObject result = runner_no_variable.run("1 == 1");
		assertThat(result, equalTo(null));
	}

	@Test
	public void onlyOutConstructorTest() throws IOException {
		OctaveScriptRunner runner_only_out = new OctaveScriptRunner("out");

		OctaveObject result = runner_only_out.run("out = 'test'");
		assertThat(((OctaveString) result).getString(), equalTo("test"));
	}

	@Test
	public void twoInVariablesTest() throws IOException {
		OctaveScriptRunner runner = new OctaveScriptRunner();

		OctaveObject foo = new OctaveString("foo");
		OctaveObject bar = new OctaveString("bar");

		runner.addInVariable("in1", foo);
		assertThat(runner.getInVariable("in1"), equalTo(foo));

		runner.addInVariable("in2", bar);
		assertThat(runner.getInVariable("in2"), equalTo(bar));

		runner.setOutVariableName("out");

		runner.run("in1");

		runner.run("in2");
	}

	@Test
	public void variableInOutTest() throws IOException {
		OctaveObject o = new OctaveString("foobar");

		OctaveScriptRunner runner = new OctaveScriptRunner(o, "in", "out");

		OctaveObject result = runner.run("out = in");

		assertThat(result, instanceOf(OctaveString.class));
		assertThat(((OctaveString) result).getString(), equalTo("foobar"));
	}

	@Test
	public void variableTest() throws IOException {
		OctaveScriptRunner runner = new OctaveScriptRunner();

		OctaveObject o = new OctaveString("test");

		runner.addInVariable("in", o);
		assertThat(runner.getInVariable("in"), equalTo(o));

		runner.setOutVariableName("out");

		runner.run("in");

		OctaveObject result = runner.run("out = 'foo'");

		assertThat(result, instanceOf(OctaveString.class));
		assertThat(((OctaveString) result).getString(), equalTo("foo"));

		result = runner.run("out = 2");

		assertThat(result, instanceOf(OctaveDouble.class));
		assertThat(((OctaveDouble) result).getSize()[0], equalTo(1));
		assertThat(((OctaveDouble) result).getData()[0], equalTo(2.));
	}

	@Test
	public void outputTest() throws IOException {
		OctaveScriptRunner runner = new OctaveScriptRunner();

		OctaveObject o = new OctaveString("test");

		runner.addInVariable("in", o);
		assertThat(runner.getInVariable("in"), equalTo(o));

		runner.setOutVariableName("out");

		runner.run("in");

		runner.run("out = 3+5\ndisp(out)");

		assertThat(runner.getLastOutput(), equalTo("out =  8\n 8\n"));
	}

}
