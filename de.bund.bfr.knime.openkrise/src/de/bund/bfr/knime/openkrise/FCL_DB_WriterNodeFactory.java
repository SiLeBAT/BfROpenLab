package de.bund.bfr.knime.openkrise;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FCL_DB_Writer" Node.
 * 
 *
 * @author BfR
 */
public class FCL_DB_WriterNodeFactory 
        extends NodeFactory<FCL_DB_WriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FCL_DB_WriterNodeModel createNodeModel() {
        return new FCL_DB_WriterNodeModel();
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
    public NodeView<FCL_DB_WriterNodeModel> createNodeView(final int viewIndex,
            final FCL_DB_WriterNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return null;
    }

}

