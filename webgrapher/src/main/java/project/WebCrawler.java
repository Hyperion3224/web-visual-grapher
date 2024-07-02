package project;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.ComponentNameProvider;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WebCrawler {
    private final Set<String> visited = new HashSet<>();
    private final SimpleDirectedGraph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

    public void crawl(String url, int depth) {
        if (depth == 0 || visited.contains(url)) return;

        try {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title();
            graph.addVertex(title);

            visited.add(url);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                if (!nextUrl.isEmpty() && !visited.contains(nextUrl)) {
                    String nextTitle = Jsoup.connect(nextUrl).get().title();
                    graph.addVertex(nextTitle);
                    graph.addEdge(title, nextTitle);
                    crawl(nextUrl, depth - 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SimpleDirectedGraph<String, DefaultEdge> getGraph() {
        return graph;
    }

    public void exportGraphToDOT(String filename) {
        ComponentNameProvider<String> vertexIdProvider = name -> name.replaceAll("\\s", "_");
        ComponentNameProvider<String> vertexLabelProvider = name -> name;
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(vertexIdProvider, vertexLabelProvider, null);
        exporter.setVertexAttributeProvider((v) -> Map.of("label", DefaultAttribute.createAttribute(v)));

        try (FileWriter writer = new FileWriter(filename)) {
            exporter.exportGraph(graph, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler();
        crawler.crawl("http://example.com", 2);
        crawler.exportGraphToDOT("web_graph.dot");
        System.out.println("Graph exported to web_graph.dot");
    }
}
