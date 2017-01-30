package de.bund.bfr.knime.network.analyzer;

import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.knime.network.core.api.KPartiteGraphView;
import org.knime.network.core.api.Partition;
import org.knime.network.core.api.PersistentObject;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ClosenessAnalyzerTypeTest {

	@Mock
	KPartiteGraphView<PersistentObject, Partition> view;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void testNumericAnalyzeInternalExecutionMonitorKPartiteGraphViewOfPersistentObjectPartitionPersistentObject() {
		fail("Not yet implemented");
	}
}
