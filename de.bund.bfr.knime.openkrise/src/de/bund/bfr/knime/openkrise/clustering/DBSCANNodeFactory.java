package de.bund.bfr.knime.openkrise.clustering;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DBSCAN" Node.
 * 
 *
 * @author BfR
 */
public class DBSCANNodeFactory 
        extends NodeFactory<DBSCANNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DBSCANNodeModel createNodeModel() {
        return new DBSCANNodeModel();
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
    public NodeView<DBSCANNodeModel> createNodeView(final int viewIndex,
            final DBSCANNodeModel nodeModel) {
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
        return new DBSCANNodeDialog();
    }

}

