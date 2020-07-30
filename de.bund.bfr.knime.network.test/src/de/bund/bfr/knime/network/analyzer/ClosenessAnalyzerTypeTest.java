/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.network.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.network.core.api.GraphObjectIterator;
import org.knime.network.core.api.KPartiteGraphView;
import org.knime.network.core.api.Partition;
import org.knime.network.core.api.PersistentObject;
import org.knime.network.core.core.exception.PersistenceException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClosenessAnalyzerTypeTest {

	@Mock
	KPartiteGraphView<PersistentObject, Partition> view;

	private PersistentObject n1;
	private PersistentObject n2;
	private PersistentObject n3;
	private PersistentObject n4;
	private PersistentObject n5;
	private PersistentObject e12;
	private PersistentObject e13;
	private PersistentObject e14;
	private PersistentObject e15;
	private PersistentObject e23;
	private PersistentObject e34;

	@Before
	public void setUp() throws PersistenceException {
		n1 = () -> "n1";
		n2 = () -> "n2";
		n3 = () -> "n3";
		n4 = () -> "n4";
		n5 = () -> "n5";
		e12 = () -> "e12";
		e13 = () -> "e13";
		e14 = () -> "e14";
		e15 = () -> "e15";
		e23 = () -> "e23";
		e34 = () -> "e34";

		PersistentObject[] nodes = { n1, n2, n3, n4, n5 };
		PersistentObject[] edges = { e12, e13, e14, e15, e23, e34 };

		Mockito.when(view.getNoOfNodes()).thenReturn((long) nodes.length);
		Mockito.when(view.getNoOfEdges()).thenReturn((long) edges.length);
		Mockito.when(view.getNodes()).thenReturn(asIterator(nodes));
		Mockito.when(view.getEdges()).thenReturn(asIterator(edges));

		Mockito.when(view.getIncidentNodes(e12)).thenReturn(Arrays.asList(n1, n2));
		Mockito.when(view.getIncidentNodes(e13)).thenReturn(Arrays.asList(n1, n3));
		Mockito.when(view.getIncidentNodes(e14)).thenReturn(Arrays.asList(n1, n4));
		Mockito.when(view.getIncidentNodes(e15)).thenReturn(Arrays.asList(n1, n5));
		Mockito.when(view.getIncidentNodes(e23)).thenReturn(Arrays.asList(n2, n3));
		Mockito.when(view.getIncidentNodes(e34)).thenReturn(Arrays.asList(n3, n4));

		Mockito.when(view.getOutgoingEdges(n1)).thenReturn(Arrays.asList(e12, e13, e14, e15));
		Mockito.when(view.getOutgoingEdges(n2)).thenReturn(Arrays.asList(e12, e23));
		Mockito.when(view.getOutgoingEdges(n3)).thenReturn(Arrays.asList(e13, e23, e34));
		Mockito.when(view.getOutgoingEdges(n4)).thenReturn(Arrays.asList(e14, e34));
		Mockito.when(view.getOutgoingEdges(n5)).thenReturn(Arrays.asList(e15));
	}

	@Test
	public void testWithoutWeights() throws PersistenceException, CanceledExecutionException {
		Mockito.when(view.getEdgeWeight(e12)).thenReturn(1.0);
		Mockito.when(view.getEdgeWeight(e13)).thenReturn(1.0);
		Mockito.when(view.getEdgeWeight(e14)).thenReturn(1.0);
		Mockito.when(view.getEdgeWeight(e15)).thenReturn(1.0);
		Mockito.when(view.getEdgeWeight(e23)).thenReturn(1.0);
		Mockito.when(view.getEdgeWeight(e34)).thenReturn(1.0);

		ClosenessAnalyzerType analyzer = new ClosenessAnalyzerType();

		analyzer.initializeInternal(view, new ExecutionMonitor());

		assertEquals(1.0 / 4.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n1)[0], 0.0);
		assertEquals(1.0 / 6.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n2)[0], 0.0);
		assertEquals(1.0 / 5.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n3)[0], 0.0);
		assertEquals(1.0 / 6.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n4)[0], 0.0);
		assertEquals(1.0 / 7.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n5)[0], 0.0);
	}

	@Test
	public void testWithWeights() throws PersistenceException, CanceledExecutionException {
		Mockito.when(view.getEdgeWeight(e12)).thenReturn(2.0);
		Mockito.when(view.getEdgeWeight(e13)).thenReturn(2.0);
		Mockito.when(view.getEdgeWeight(e14)).thenReturn(2.0);
		Mockito.when(view.getEdgeWeight(e15)).thenReturn(2.0);
		Mockito.when(view.getEdgeWeight(e23)).thenReturn(1.0);
		Mockito.when(view.getEdgeWeight(e34)).thenReturn(1.0);

		ClosenessAnalyzerType analyzer = new ClosenessAnalyzerType();

		analyzer.initializeInternal(view, new ExecutionMonitor());

		assertEquals(1.0 / 8.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n1)[0], 0.0);
		assertEquals(1.0 / 9.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n2)[0], 0.0);
		assertEquals(1.0 / 8.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n3)[0], 0.0);
		assertEquals(1.0 / 9.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n4)[0], 0.0);
		assertEquals(1.0 / 14.0, analyzer.numericAnalyzeInternal(new ExecutionMonitor(), view, n5)[0], 0.0);
	}

	private static GraphObjectIterator<PersistentObject> asIterator(PersistentObject... objects) {
		Iterator<PersistentObject> it = Arrays.asList(objects).iterator();

		return new GraphObjectIterator<PersistentObject>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public PersistentObject next() {
				return it.next();
			}

			@Override
			public Iterator<PersistentObject> iterator() {
				return it;
			}

			@Override
			public void close() {
			}
		};
	}
}
