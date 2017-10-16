package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;

import org.apache.commons.collections15.Transformer;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.jung.BetterGraphMouse;
import de.bund.bfr.jung.JungListener;
import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.jung.layout.LayoutType;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
//import de.bund.bfr.knime.gis.views.canvas.ExplosionCanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.common.Delivery;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public class ExplosionTracingOsmCanvas extends TracingOsmCanvas implements IExplosionCanvas<LocationNode> {

	private static Logger logger =  Logger.getLogger("de.bund.bfr");
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String gstrKey;
	private Set<LocationNode> boundaryNodes; 
	private Set<LocationNode> nonBoundaryNodes;
	private Set<LocationNode> allBoundaryNodes;
	
//	private BufferedImage image;
	private Polygon boundaryArea;
		
	public static final double BOUNDARY_MARGIN = 0.2;
	
	private Map<String, Set<String>> allCollapsedNodes;

	public ExplosionTracingOsmCanvas(List<LocationNode> nodes, List<Edge<LocationNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties, Map<String, Delivery> deliveries, boolean lotBased, String strKey, Set<String> containedNodes) {
		super(nodes, edges, nodeProperties, edgeProperties, deliveries, lotBased);
		
		logger.finest("entered");
//		this.image = null;
		this.gstrKey = strKey;
		this.boundaryNodes = this.nodes.stream().filter(n -> !containedNodes.contains(n.getId())).collect(Collectors.toSet());
		this.nonBoundaryNodes = this.nodes.stream().filter(n -> containedNodes.contains(n.getId())).collect(Collectors.toSet());
		this.allBoundaryNodes = this.boundaryNodes.stream().collect(Collectors.toSet());
		
//		this.getViewer().addPreRenderPaintable(new PrePaintable(false));
		//this.getViewer().addPostRenderPaintable(new ExplosionCanvasUtils.LabelPaintable(this.getViewer(),strKey,()->call(l->l.closeExplosionViewRequested())));
		this.getViewer().addPostRenderPaintable(
				new ExplosionCanvasUtils.LabelPaintable(
						this.getViewer(),
						strKey,
						()-> Stream.of(getListeners(ExplosionListener.class)).forEach(l->l.closeExplosionViewRequested(this))));
		
		this.boundaryNodes.forEach(n -> this.getViewer().getGraphLayout().lock(n, true));
		
		this.placeNodes(this.nonBoundaryNodes, this.edges);
		//this.placeBoundaryNodes();
		
		logger.finest("leaving");
	}
	
	@Override
	public void applyNodeCollapse() {
		logger.finest("entered");
		super.applyNodeCollapse();
		
		//this.boundaryNodes = Sets.difference(this.nodes, this.nonBoundaryNodes);
		
		Layout<LocationNode, Edge<LocationNode>> layout = this.getViewer().getGraphLayout();
		
		Sets.difference(this.nodes, this.nonBoundaryNodes).forEach(n -> layout.lock(n, true));
		
		Set<LocationNode> allBoundaryNodes = Sets.union(Sets.difference(this.nodes, this.nonBoundaryNodes), this.boundaryNodes);
		
		if(!this.boundaryNodes.isEmpty()) {
			this.boundaryArea = ExplosionCanvasUtils.placeBoundaryNodes(
					allBoundaryNodes, 
					this.nodes, 
					this.edges, 
					this.collapsedNodes, 
					layout, 
					this.getInvalidArea());
		}
		
		// this.placeBoundaryNodes();
		logger.finest("leaving");
	}
	
	@Override
	public void resetNodesAndEdges() {
		logger.finest("entered");
		super.resetNodesAndEdges();
		this.boundaryNodes = Sets.intersection(this.nodes, this.boundaryNodes);
		logger.finest("leaving");
	}
	
	@Override
	public VisualizationImageServer<LocationNode, Edge<LocationNode>> getVisualizationServer(boolean toSvg) {
		VisualizationImageServer<LocationNode, Edge<LocationNode>> server = super.getVisualizationServer(toSvg);
		
//		server.addPreRenderPaintable(new PrePaintable(toSvg));
        server.addPostRenderPaintable(new ExplosionCanvasUtils.LabelPaintable(this.getViewer(),this.gstrKey));
		return server;
	}
	
	private void call(Consumer<CanvasListener> action) {
		Stream.of(getListeners(CanvasListener.class)).forEach(action);
	}
	
//	public static void paintNonLatLonArea(Graphics2D g, int w, int h, Shape invalidArea) {
//		BufferedImage invalidAreaImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D imgGraphics = invalidAreaImage.createGraphics();
//
//		imgGraphics.setPaint(CanvasUtils.mixColors(Color.WHITE, Arrays.asList(Color.RED, Color.WHITE),
//				Arrays.asList(1.0, 1.0), false));
//		imgGraphics.fill(invalidArea);
//		imgGraphics.setColor(Color.BLACK);
//		imgGraphics.draw(invalidArea);
//		CanvasUtils.drawImageWithAlpha(g, invalidAreaImage, 75);
//		invalidAreaImage.flush();
//	}
	
	@Override
	protected void placeNodes(Set<LocationNode> nodes, Set<Edge<LocationNode>> edges) {
		
		if(this.nonBoundaryNodes!=null) {
			
			Set<LocationNode> nonBoundaryNodes = Sets.intersection(this.nodes, this.nonBoundaryNodes);
			
			super.placeNodes(nonBoundaryNodes, this.edges.stream().filter(e -> nonBoundaryNodes.contains(e.getTo()) && nonBoundaryNodes.contains(e.getFrom())).collect(Collectors.toSet()));
			
		} else {
			// do nothing
		}
		
	}

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = PointUtils.getBounds(getNodePositions(this.nonBoundaryNodes).values());
		

		if (bounds != null) {
			if(this.boundaryArea!=null) bounds = ExplosionCanvasUtils.getAreaRect(this.boundaryArea);
			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, 2.0));
			transformFinished();
		} else {
			super.resetLayoutItemClicked();
		}
	}
	
	@Override
	public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
		logger.finest("entered");
		
		this.allCollapsedNodes = collapsedNodes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<String>(e.getValue())));
		this.collapsedNodes = ExplosionCanvasUtils.filterCollapsedNodeAccordingToExplosion(
				collapsedNodes, this.gstrKey,
				CanvasUtils.getElementIds(Sets.union(this.nonBoundaryNodes, this.boundaryNodes)));
				
		Sets.difference(this.collapsedNodes.keySet(), collapsedNodes.keySet()).forEach(id -> nodeSaveMap.remove(id));
		
		applyChanges();
		call(l -> l.collapsedNodesChanged(this));
		this.flushImage();
		logger.finest("leaving");
	}
	
//	@Override
//	protected Set<GraphNode> getLayoutableNodes() { return this.nonBoundaryNodes; }
//	//private Set<GraphNode> nonBoundaryNodes() { return Sets.difference(this.nodes,this.boundaryNodes); }
	
//	@Override
//	protected void applyLayout(LayoutType layoutType, Set<GraphNode> nodesForLayout, boolean showProgressDialog) {
//		super.applyLayout(layoutType, nodesForLayout, showProgressDialog, false);
//		//Sets.difference(this.boundaryNodes, this.nodes).forEach(n -> viewer.getGraphLayout().setLocation(n,  new Point2D.Double(Double.NaN, Double.NaN)));
//		// viewer.getGraphLayout().setLocation(node, transform.apply(pos.getX(), pos.getY()));
//		this.repositionBoundaryNodes();
//		//Rectangle viewBounds = this.getViewer().getBounds();
//		//if this.getViewer().getBounds() 
//		Stream.of(getListeners(CanvasListener.class)).forEach(l -> l.layoutProcessFinished(this));
//	}
	
//	@Override
//	public void applyInvisibility() {
//		ExplosionCanvasUtils.removeOuterNodes(nodes, this.nonBoundaryNodes, this.boundaryNodes);
//		super.applyInvisibility();
//	}
	
//	@Override 
//	protected Map<String, Point2D> getNodePositions(Collection<GraphNode> nodes) {
//		logger.finest("entered");
//		Map<String, Point2D> nodePos = super.getNodePositions(nodes.stream().filter(n-> this.nonBoundaryNodes.contains(n) || this.boundaryNodes.contains(n)).collect(Collectors.toList()));
//		logger.finest("leaving");
//		return nodePos;
//	}
	
	private static Point2D getClosestPointOnRect(Point2D pointInRect, Rectangle2D rect) {
		double dx1 = Math.abs(pointInRect.getX() - rect.getMinX());
		double dx2 = Math.abs(pointInRect.getX() - rect.getMaxX());
		double dy1 = Math.abs(pointInRect.getY() - rect.getMinY());
		double dy2 = Math.abs(pointInRect.getY() - rect.getMaxY());
		double min = Collections.min(Arrays.asList(dx1, dx2, dy1, dy2));

		if (dx1 == min) {
			return new Point2D.Double(rect.getMinX(), pointInRect.getY());
		} else if (dx2 == min) {
			return new Point2D.Double(rect.getMaxX(), pointInRect.getY());
		} else if (dy1 == min) {
			return new Point2D.Double(pointInRect.getX(), rect.getMinY());
		} else if (dy2 == min) {
			return new Point2D.Double(pointInRect.getX(), rect.getMaxY());
		}

		throw new RuntimeException("This should not happen");
	}
	
//	protected void flushImage() {
//		if (image != null) {
//			image.flush();
//			image = null;
//		}
//	}
	
//	@Override
//	public void openExplosionViewItemClicked() {
//		Set<String> selectedNodeIds = getSelectedNodeIds();
//		
//		// exactly one node must be selected
//		if(selectedNodeIds==null || selectedNodeIds.isEmpty() || selectedNodeIds.size()!=1) return;
//		// this node has to be a metanode
//		String selectedNodeId = (String) selectedNodeIds.toArray()[0]; //.iterator().next();
//		if(!this.allCollapsedNodes.keySet().contains(selectedNodeId)) return;
//		
//		call(l -> l.openExplosionViewRequested(this, selectedNodeId)); //, this.allCollapsedNodes.get(selectedNodeId)));
//	}
	
	
	@Override
	protected void paintGis(Graphics2D g, boolean toSvg, boolean onWhiteBackground) {
		super.paintGis(g, toSvg, onWhiteBackground);

		if (this.boundaryArea != null) {
			ExplosionCanvasUtils.paintBoundaryArea(g, getCanvasSize().width, getCanvasSize().height,
					transform.apply(this.boundaryArea));
		}
	}
	
//	private void paintGraph(Graphics2D g, boolean toSvg) {
//		//super.paintGis(g, toSvg, onWhiteBackground);
//		//logger.finest("entered toSvg=" + (toSvg?"true":"false"));
//		if (this.boundaryArea != null) {
//			ExplosionCanvasUtils.paintBoundaryArea(g, getCanvasSize().width, getCanvasSize().height,
//					transform.apply(this.boundaryArea));
//		}
//		//logger.finest("leaving");
//	}
	
//	private void paintGraphImage(Graphics2D g) {
//		//logger.finest("entered");
//		int width = getCanvasSize().width;
//		int height = getCanvasSize().height;
//
//		if (image == null || image.getWidth() != width || image.getHeight() != height) {
//			flushImage();
//			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//			this.paintGraph(image.createGraphics(), false);
//		}
//
//		g.drawImage(image, 0, 0, null);
//		//logger.finest("leaving");
//	}
	
//	private class PrePaintable implements Paintable {
//		
//		private boolean toSvg;
//
//		public PrePaintable(boolean toSvg) {
//			this.toSvg = toSvg;
//		}
//
//		@Override
//		public boolean useTransform() {
//			return false;
//		}
//
//		@Override
//		public void paint(Graphics g) {
//			//logger.finest("entered toSvg=" + (toSvg?"true":"false"));
//			if (toSvg) {
//				ExplosionTracingOsmCanvas.this.paintGraph((Graphics2D) g, true);
//			} else {
//				ExplosionTracingOsmCanvas.this.paintGraphImage((Graphics2D) g);
//			}
//			//logger.finest("leaving");
//		}
//	}
	
//	public class LabelPaintable implements Paintable, MouseMotionListener, MouseListener {
//
//		private VisualizationViewer<?, ?> viewer;
//
//		private EventListenerList listeners;
//
//		private Rectangle closeRect;
//
//		private boolean closeFocused;
//		
//		//private boolean gbolLabelOnly;
//		private String gstrLabel;
//		private Runnable function;
//
//		public LabelPaintable(VisualizationViewer<?, ?> viewer, String strLabel, Runnable function) {
//			this.viewer = viewer;
//			this.gstrLabel = strLabel + " Explosion View";
//			this.function = function;
//			listeners = new EventListenerList();
//			closeRect = null;
//			closeFocused = false;
//			
//			if(!this.labelOnly()) {
//			  viewer.addMouseMotionListener(this);
//			  viewer.addMouseListener(this);
//			}
//		}
//		
//		public LabelPaintable(VisualizationViewer<?, ?> viewer, String strLabel) {
//			this(viewer, strLabel, null);
////			this.viewer = viewer;
////			this.gstrLabel = strLabel;
////			this.function = function;
////			//this.gbolLabelOnly = labelOnly;
////			listeners = new EventListenerList();
////			closeRect = null;
////			closeFocused = false;
////			
////			if(!this.labelOnly()) {
////			  viewer.addMouseMotionListener(this);
////			  viewer.addMouseListener(this);
////			}
//		}
//
//		public void addChangeListener(JungListener listener) {
//			listeners.add(JungListener.class, listener);
//		}
//
//		public void removeChangeListener(JungListener listener) {
//			listeners.remove(JungListener.class, listener);
//		}
//
//		@Override
//		public void paint(Graphics graphics) {
//			int w = viewer.getSize().width;
//			int h = viewer.getSize().height;
//			int size = 30;
//			int d = 10;
//			int lineD = 8;
//			int lineWidth = 2;
//
//			
//			//int xPlus = w - d - size;
//			//int yPlus = h - size - d - 2 * size;
//			//int xMinus = xPlus;
//			//int yMinus = h - size - d - size;
//						
//			Graphics2D g = (Graphics2D) graphics;
//			
//			Font font = new Font("Default", Font.BOLD, 10);
//			int fontHeight = g.getFontMetrics(font).getHeight();
//			int fontAscent = g.getFontMetrics(font).getAscent();
//			
//			int dy = 2;
//
//			int dx = 5;
//			
//			
//			int sw = (int) font.getStringBounds(this.gstrLabel, g.getFontRenderContext()).getWidth();
//			int sh = (int) font.getStringBounds(this.gstrLabel, g.getFontRenderContext()).getHeight();
//
//			Color currentColor = g.getColor();
//			Stroke currentStroke = g.getStroke();
//
//			if(!this.labelOnly()) this.closeRect = new Rectangle(w/2 + sw/2 + dx - fontAscent/2 - dx/2, dy ,fontAscent,fontAscent);
//			
//			g.setColor(ZoomingPaintable.BACKGROUND);
//			g.fillRect(w/2 - sw/2 - dx - (this.labelOnly()?0:this.closeRect.width/2+dx/2) , 0 , sw + 2*dx + (this.labelOnly()?0:this.closeRect.width+dx), fontHeight + 2*dy);
////			g.setColor(minusFocused ? Color.BLUE : BACKGROUND);
////			g.fillRect(xMinus, yMinus, size, size);
//			g.setColor(Color.BLACK);
//			g.drawRect(w/2 - sw/2 - dx - (this.labelOnly()?0:this.closeRect.width/2+dx/2), 0 , sw + 2*dx + (this.labelOnly()?0:this.closeRect.width+dx), fontHeight + 2*dy);
////			g.drawRect(xMinus, yMinus, size, size);
//			g.setFont(font);
//			g.drawString(this.gstrLabel, w/2 - sw/2 - (this.labelOnly()?0:this.closeRect.width/2+dx/2), dy+fontAscent);
//			if(!this.labelOnly()) {
//				//this.closeRect = new Rectangle(w/2 + sw/2 + dx,dy+fontAscent,fontAscent,fontAscent);
//				g.setStroke(new BasicStroke(lineWidth));
//				if(this.closeFocused) g.setColor(Color.BLUE);
//				g.drawLine(this.closeRect.x,this.closeRect.y,this.closeRect.x+this.closeRect.width,this.closeRect.y+this.closeRect.height);
//				g.drawLine(this.closeRect.x,this.closeRect.y+this.closeRect.height,this.closeRect.x+this.closeRect.width,this.closeRect.y);
//			}
//			g.setColor(currentColor);
//			g.setStroke(currentStroke);
//
////			closeRect = new Rectangle(xPlus, yPlus, size, size);
////			minusRect = new Rectangle(xMinus, yMinus, size, size);
//		}
//
//		private boolean labelOnly() { return this.function==null; }
//		
//		@Override
//		public boolean useTransform() {
//			return false;
//		}
//
//		@Override
//		public void mouseDragged(MouseEvent e) {
//		}
//
//		@Override
//		public void mouseMoved(MouseEvent e) {
//			boolean newCloseFocused = closeRect != null && closeRect.contains(e.getPoint());
//			boolean changed = newCloseFocused != closeFocused;
//
//			closeFocused = newCloseFocused;
////			minusFocused = newMinusFocused;
//
//			if (changed) {
//				BetterGraphMouse<?, ?> graphMouse = (BetterGraphMouse<?, ?>) viewer.getGraphMouse();
//
//				graphMouse.setPickingDeactivated(closeFocused);
//				paint(viewer.getGraphics());
//			}
//		}
//
//		@Override
//		public void mouseClicked(MouseEvent e) {
//			if (e.getButton() == MouseEvent.BUTTON1 && (closeFocused)) {
//				try {
//					//this.function.call();
//					this.function.run();
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
//		}
//
//		@Override
//		public void mousePressed(MouseEvent e) {
//		}
//
//		@Override
//		public void mouseReleased(MouseEvent e) {
//		}
//
//		@Override
//		public void mouseEntered(MouseEvent e) {
//		}
//
//		@Override
//		public void mouseExited(MouseEvent e) {
//		}
//	}

	@Override
	public Set getBoundaryNodes() {
		// TODO Auto-generated method stub
		return this.boundaryNodes;
	}

	@Override
	public void addExplosionListener(ExplosionListener listener) {
		// TODO Auto-generated method stub
		this.listenerList.add(ExplosionListener.class, listener);
	}

	@Override
	public void removeExplosionListener(ExplosionListener listener) {
		// TODO Auto-generated method stub
		this.listenerList.remove(ExplosionListener.class, listener);
	}


}
