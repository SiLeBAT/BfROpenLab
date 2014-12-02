package de.bund.bfr.knime.openkrise;

import java.util.HashSet;
import java.util.Random;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;

import de.bund.bfr.knime.IO;

public class RandomNetworkGenerator {

	private Graph graph = null;
	
	public RandomNetworkGenerator(int numNodes, int maxLinksPerNode) {
		generateFCLNetwork(numNodes, maxLinksPerNode);
	}

	private void generateFCLNetwork(int numNodes, int maxLinksPerNode) {
		graph = new MultiGraph("Barabàsi-Albert");
		// Between 1 and maxLinksPerNode new links per node added.
		BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator(maxLinksPerNode);
		gen.setDirectedEdges(true, true);
		// Generate numNodes nodes:
		gen.addSink(graph);
		gen.begin();
		for (int i = 0; i < numNodes; i++) {
			gen.nextEvents();
		}
		gen.end();
		//graph.display();
	}

	public void getNodes(BufferedDataContainer output33Nodes) {
		for (int i=0;i<graph.getNodeCount();i++) {
			RowKey key = RowKey.createRowKey(i);
			Node node = graph.getNode(i);
			DataCell[] cells = new DataCell[19];
			cells[0] = new IntCell(i);
			cells[1] = new StringCell("Company_" + i);
			cells[2] = DataType.getMissingCell(); // Street
			cells[3] = DataType.getMissingCell(); // Hausnummer
			cells[4] = DataType.getMissingCell(); // Zip
			cells[5] = DataType.getMissingCell(); // Ort
			cells[6] = DataType.getMissingCell(); // district
			cells[7] = DataType.getMissingCell(); // bll
			cells[8] = DataType.getMissingCell(); // country
			cells[9] = DataType.getMissingCell(); // VATnumber
			cells[10] = DataType.getMissingCell(); // Betriebsart
			
			cells[11] = DataType.getMissingCell(); // AnzahlFaelle
			cells[12] = DataType.getMissingCell(); // DatumBeginn
			cells[13] = DataType.getMissingCell(); // DatumHoehepunkt
			cells[14] = DataType.getMissingCell(); // DatumEnde

			cells[15] = DataType.getMissingCell(); // Serial
			boolean ss = node.getInDegree() == 0;
			if (ss) {
				int index = -1;
				for (int j=0;j<node.getOutDegree();j++) {
					if (index < 0) {
						index = node.getLeavingEdge(j).getIndex();
						continue;
					}
					else if (node.getLeavingEdge(j).getIndex() != index) {
						ss = false;
						break;
					}
				}
			}
			cells[16] = ss ? BooleanCell.TRUE : BooleanCell.FALSE; // SimpleSupplier 
			cells[17] = node.getInDegree() == 0 ? BooleanCell.TRUE : BooleanCell.FALSE; // StationStart 
			cells[18] = node.getOutDegree() == 0 ? BooleanCell.TRUE : BooleanCell.FALSE; // StationEnd

			DataRow outputRow = new DefaultRow(key, cells);

			output33Nodes.addRowToTable(outputRow);
		}
	}
	public void getLinks(BufferedDataContainer output33Links) {
		for (int i=0;i<graph.getEdgeCount();i++) {
			RowKey key = RowKey.createRowKey(i);
			Edge edge = graph.getEdge(i);
			DataCell[] cells = new DataCell[20];
			cells[0] = new IntCell(i);
			edge.isDirected();
			cells[1] = new IntCell(edge.getSourceNode().getIndex()); // from
			cells[2] = new IntCell(edge.getTargetNode().getIndex()); // to
			
			cells[3] = DataType.getMissingCell(); // Artikelnummer
			cells[4] = DataType.getMissingCell(); // Bezeichnung
			cells[5] = DataType.getMissingCell(); // Prozessierung
			cells[6] = DataType.getMissingCell(); // IntendedUse
			cells[7] = DataType.getMissingCell(); // ChargenNr
			cells[8] = DataType.getMissingCell(); // MHD
			cells[9] = DataType.getMissingCell(); // Production Date
			cells[10] = DataType.getMissingCell(); // Delivery Data			
			cells[11] = DataType.getMissingCell(); // Amount
			
			cells[12] = new StringCell("Row" + i);
			cells[13] = DataType.getMissingCell(); // Serial
			cells[14] = DataType.getMissingCell(); // OriginCountry

			cells[15] = DataType.getMissingCell(); // EndChain BooleanCell.TRUE : BooleanCell.FALSE;
			cells[16] = DataType.getMissingCell(); // Explanation_EndChain 
			cells[17] = DataType.getMissingCell(); // Contact_Questions_Remarks
			cells[18] = DataType.getMissingCell(); // Further_Traceback
			cells[19] = DataType.getMissingCell(); // MicrobioSample

			DataRow outputRow = new DefaultRow(key, cells);

			output33Links.addRowToTable(outputRow);
		}
	}
	public void getDeliveryDelivery(BufferedDataContainer deliveryDelivery) {
		Random r1 = new Random();
		Random r2 = new Random();
		int lfd = 0;
		for (int i=0;i<graph.getNodeCount();i++) {
			Node node = graph.getNode(i);
			if (node.getInDegree() > 0 && node.getOutDegree() > 0) {
				for (int j=0;j<node.getInDegree();j++) {
					int numConnections = r1.nextInt(node.getOutDegree());
					HashSet<Integer> done = new HashSet<Integer>();
					for (int k = 0; k < numConnections; k++) {
						int toIndex = 0;
						do {
							toIndex = r2.nextInt(node.getOutDegree());
						} while (done.contains(toIndex));
						done.add(toIndex);
						deliveryDelivery.addRowToTable(new DefaultRow(lfd+"", IO.createCell(node.getEnteringEdge(j).getIndex()), IO.createCell(node.getLeavingEdge(toIndex).getIndex())));
						lfd++;
					}
				}
			}
		}
		/*
		int i = 0;

		for (MyDelivery delivery : mnt.getAllDeliveries().values()) {
			for (int next : delivery.getAllNextIDs()) {
				deliveryDelivery.addRowToTable(new DefaultRow(i+"", IO.createCell(delivery.getId()), IO.createCell(next)));
				i++;
			}
		}
		*/
	}
}
