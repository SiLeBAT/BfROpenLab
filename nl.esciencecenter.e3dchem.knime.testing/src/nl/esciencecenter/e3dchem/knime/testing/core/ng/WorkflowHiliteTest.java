/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   04.02.2014 (thor): created
 */
package nl.esciencecenter.e3dchem.knime.testing.core.ng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.rules.ErrorCollector;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.SingleNodeContainer;
import org.knime.core.node.workflow.WorkflowManager;

/**
 * Testcase that hilites rows in all output tables from every node. Errors will very likely not occur in this test but
 * be reported in the log file or result in exceptions in views.
 *
 * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
 */
public class WorkflowHiliteTest extends WorkflowTest {
    public WorkflowHiliteTest(final String workflowName, final IProgressMonitor monitor, final WorkflowTestContext context) {
        super(workflowName, monitor, context);
    }

    public void run(final ErrorCollector collector) {
        try {
            hiliteRows(collector, m_context.getWorkflowManager(), m_context.getTestflowConfiguration());
        } catch (Throwable t) {
            collector.addError(t);
        }
    }

    private void hiliteRows(final ErrorCollector collector, final WorkflowManager manager,
                            final TestflowConfiguration flowConfiguration) {
        for (NodeContainer cont : manager.getNodeContainers()) {
            if (cont instanceof WorkflowManager) {
                hiliteRows(collector, (WorkflowManager)cont, flowConfiguration);
            } else if (cont instanceof SingleNodeContainer) {
                hiliteRows(collector, (SingleNodeContainer)cont, flowConfiguration);

            }
        }
    }

    private void hiliteRows(final ErrorCollector collector, final SingleNodeContainer node,
                            final TestflowConfiguration flowConfiguration) {
        for (int i = 0; i < node.getNrOutPorts(); i++) {
            if (node.getOutputObject(i) instanceof BufferedDataTable) {
                int max = flowConfiguration.getMaxHiliteRows();

                List<RowKey> keys = new ArrayList<RowKey>();
                CloseableRowIterator it = ((BufferedDataTable)node.getOutputObject(i)).iterator();
                while (it.hasNext() && max-- > 0) {
                    keys.add(it.next().getKey());
                }
                it.close();

                HiLiteHandler handler = node.getOutputHiLiteHandler(i);
                // hilite all
                handler.fireHiLiteEvent(new HashSet<RowKey>(keys));
                // unhilite sonme
                handler.fireUnHiLiteEvent(new HashSet<RowKey>(keys.subList((int) (0.1 * keys.size()),
                                                                         (int) Math.ceil(0.6 * keys.size()))));
                // unhilite all
                handler.fireClearHiLiteEvent();
            }
        }
    }
}
