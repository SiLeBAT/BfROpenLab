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
package de.bund.bfr.knime.testflows;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.UnsupportedWorkflowVersionException;
import org.knime.core.util.LockFailedException;
import org.knime.testing.core.TestrunConfiguration;

import nl.esciencecenter.e3dchem.knime.testing.TestFlowRunner;

public class TestFlows {

	@Rule
	public ErrorCollector collector = new ErrorCollector();
	private TestFlowRunner runner;
	private String path;

	@Before
	public void setUp() {
		TestrunConfiguration runConfiguration = new TestrunConfiguration();

		runConfiguration.setCheckLogMessages(false);
		runConfiguration.setLoadSaveLoad(false);
		runner = new TestFlowRunner(collector, runConfiguration);
		path = TestFlows.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}

	@Test
	public void testAddressCreator() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		//runner.runTestWorkflow(new File(path + "workflows/AddressCreator_Test"));
	}

	//@Test
	public void testDiffFitting() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/DiffFitting_Test"));
	}

	//@Test
	public void testFittingLOD() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Fitting_LOD_Test"));
	}

	//@Test
	public void testFitting() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Fitting_Test"));
	}

	//@Test
	public void testGeocoding() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Geocoding_Test"));
	}

	//@Test
	public void testGISCluster() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/GISCluster_Test"));
	}

	//@Test
	public void testShapefileReader() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/ShapefileReader_Test"));
	}

	//@Test
	public void testSupplyChainReader() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/SupplyChainReader_Test"));
	}

	//@Test
	public void testSupplyChainReaderWithExtrafields() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/SupplyChainReader_Test_With_Extrafields"));
	}

	//@Test
	public void testSupplyChainReaderLotBasedWithExtrafields() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/SupplyChainReader_Test_LotBased_With_Extrafields"));
	}

	//@Test
	public void testSupplyChainReaderWithoutSerials() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/SupplyChainReader_Test_Without_Serials"));
	}

	//@Test
	public void testTracingWithCCOnDeliveries() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_with_CC_on_Deliveries"));
	}

	//@Test
	public void testTracingWithCollapseAndDifferentWeights() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_with_Collapse_and_Different_Weights"));
	}

	//@Test
	public void testTracingWithJoinDeliveries() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_with_Join_Deliveries"));
	}

	//@Test
	public void testTracingWithTemporalOrder() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_with_Temporal_Order"));
	}

	//@Test
	public void testTracingWithoutTemporalOrder() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_without_Temporal_Order"));
	}

	//@Test
	public void testTracingView() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/TracingView_Test"));
	}
}
