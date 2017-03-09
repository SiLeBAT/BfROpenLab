package de.bund.bfr.knime.openkrise;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "Station2LotConverter" Node.
 * 
 *
 * @author BfR
 */
public class Station2LotConverterNodeFactory 
        extends NodeFactory<Station2LotConverterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Station2LotConverterNodeModel createNodeModel() {
        return new Station2LotConverterNodeModel();
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
    public NodeView<Station2LotConverterNodeModel> createNodeView(final int viewIndex,
            final Station2LotConverterNodeModel nodeModel) {
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

