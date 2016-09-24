package de.bund.bfr.knime.openkrise.out;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "TracingXmlOut" Node.
 * 
 *
 * @author BfR
 */
public class TracingXmlOutNodeFactory 
        extends NodeFactory<TracingXmlOutNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public TracingXmlOutNodeModel createNodeModel() {
        return new TracingXmlOutNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<TracingXmlOutNodeModel> createNodeView(final int viewIndex,
            final TracingXmlOutNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new TracingXmlOutNodeDialog();
    }

}

