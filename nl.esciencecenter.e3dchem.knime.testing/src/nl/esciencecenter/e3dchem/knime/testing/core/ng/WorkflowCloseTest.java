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
 *   19.08.2013 (thor): created
 */
package nl.esciencecenter.e3dchem.knime.testing.core.ng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.junit.rules.ErrorCollector;
import org.knime.core.data.container.BufferTracker;
import org.knime.core.data.util.memory.MemoryAlert;
import org.knime.core.data.util.memory.MemoryAlertListener;
import org.knime.core.data.util.memory.MemoryAlertSystem;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.WorkflowManager;
import org.knime.core.util.Pair;

/**
 * Testcase that closes the workflow and does some additional checks, e.g. if all resources held by the workflow are
 * cleaned up.
 *
 * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
 */
public class WorkflowCloseTest {

    public void run(final ErrorCollector collector, WorkflowTestContext m_context) {
        try {
            m_context.getWorkflowManager().shutdown();
            m_context.getWorkflowManager().getParent().removeNode(m_context.getWorkflowManager().getID());

            List<NodeContainer> openWorkflows = new ArrayList<NodeContainer>(WorkflowManager.ROOT.getNodeContainers());
            openWorkflows.removeAll(m_context.getAlreadyOpenWorkflows());
            if (openWorkflows.size() > 0) {
            	collector.addError(new Throwable(openWorkflows.size()
                        + " dangling workflows detected: " + openWorkflows));
            }

            Collection<Pair<NodeContainer, StackTraceElement[]>> openBuffers =
                BufferTracker.getInstance().getOpenBuffers();
            if (!openBuffers.isEmpty()) {
            	collector.addError(new Throwable(openBuffers.size() + " open buffers detected: "
                    + openBuffers.stream().map(p -> p.getFirst().getNameWithID()).collect(Collectors.joining(", "))));
            }
            BufferTracker.getInstance().clear();
        } catch (Throwable t) {
        	collector.addError(t);
        }
    }

    private void sendMemoryAlert() throws InterruptedException {
        Semaphore sem = new Semaphore(1);
        sem.acquire();
        MemoryAlertSystem.getInstance().addListener(new MemoryAlertListener() {
            @Override
            protected boolean memoryAlert(final MemoryAlert alert) {
                sem.release();
                return true;
            }
        });
        MemoryAlertSystem.getInstance().sendMemoryAlert();
        sem.acquire();
    }
}
