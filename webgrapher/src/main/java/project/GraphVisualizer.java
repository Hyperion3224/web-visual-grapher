package project;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;

public class GraphVisualizer extends JFrame {
    private static final long serialVersionUID = -2707712944901661771L;
    private mxGraphComponent graphComponent;
    private double zoomFactor = 1.5;

    public GraphVisualizer(Graph<WebPage, DefaultEdge> graph) {
        super("Web Crawler Graph Visualization");

        mxGraph mxGraph = new mxGraph();
        Object parent = mxGraph.getDefaultParent();

        mxGraph.getModel().beginUpdate();

        try {
            Map<WebPage, Object> vertexToMxObject = new HashMap<>();

            for (WebPage webpage : graph.vertexSet()) {
                Object mxVertex = mxGraph.insertVertex(parent, null, webpage.getTitle(), 0, 0, 120, 50);
                vertexToMxObject.put(webpage, mxVertex);
            }

            for (DefaultEdge edge : graph.edgeSet()) {
                WebPage source = graph.getEdgeSource(edge);
                WebPage target = graph.getEdgeTarget(edge);
                mxGraph.insertEdge(parent, null, "", vertexToMxObject.get(source), vertexToMxObject.get(target));
            }
        } finally {
            mxGraph.getModel().endUpdate();
        }

        mxIGraphLayout layout = new mxCircleLayout(mxGraph);
        layout.execute(parent);

        graphComponent = new mxGraphComponent(mxGraph);
        getContentPane().add(graphComponent);

        // Add zoom controls
        JPanel zoomPanel = new JPanel();
        JButton zoomInButton = new JButton("Zoom In");
        JButton zoomOutButton = new JButton("Zoom Out");
        JButton resetZoomButton = new JButton("Reset Zoom");

        zoomInButton.addActionListener(e -> zoom(zoomFactor));
        zoomOutButton.addActionListener(e -> zoom(1 / zoomFactor));
        resetZoomButton.addActionListener(e -> graphComponent.zoomActual());

        zoomPanel.add(zoomInButton);
        zoomPanel.add(zoomOutButton);
        zoomPanel.add(resetZoomButton);

        getContentPane().add(zoomPanel, BorderLayout.SOUTH);

        // Add mouse wheel zoom
        graphComponent.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    if (e.getWheelRotation() < 0) {
                        zoom(zoomFactor);
                    } else {
                        zoom(1 / zoomFactor);
                    }
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void zoom(double factor) {
        graphComponent.zoomTo(graphComponent.getGraph().getView().getScale() * factor, true);
    }

    public static void visualize(Graph<WebPage, DefaultEdge> graph) {
        GraphVisualizer frame = new GraphVisualizer(graph);
        frame.setVisible(true);
    }
}