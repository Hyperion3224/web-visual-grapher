package project;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import org.jgrapht.traverse.BreadthFirstIterator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.Iterator;

public class WebCrawler {
    private Graph<WebPage, DefaultEdge> siteGraph;
    private Set<String> visitedUrls;
    private String domain;

    public WebCrawler(String startUrl) {
        siteGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        visitedUrls = new HashSet<>();
        domain = extractDomain(startUrl);
        crawl(startUrl, 0);
    }

    private void crawl(String url, int depth) {
        //System.out.println(depth + "   " + url);
        if (depth > 5 || !url.contains(domain) || visitedUrls.contains(url)) {
            return;
        }

        visitedUrls.add(url);

        try {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title();

            WebPage page = new WebPage(url, title);
            siteGraph.addVertex(page);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                if (!visitedUrls.contains(nextUrl)) {
                    WebPage linkedPage = new WebPage(nextUrl, Jsoup.connect(nextUrl).get().title());
                    siteGraph.addVertex(linkedPage);
                    siteGraph.addEdge(page, linkedPage);
                    crawl(nextUrl, depth + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            if (domain != null) {
                return domain.startsWith("www.") ? domain.substring(4) : domain;
            }
        } catch (URISyntaxException e) {
            System.err.println("Invalid URL: " + url);
        }
        return "";
    }

    // Additional methods for graph manipulation and export

    public Graph<WebPage, DefaultEdge> getGraph() {
        return siteGraph;
    }

    public String exportToDOT() {
        DOTExporter<WebPage, DefaultEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.getTitle()));
            return map;
        });

        Writer writer = new StringWriter();
        exporter.exportGraph(siteGraph, writer);
        return writer.toString();
    }

    public void printGraphStructure() {
        Iterator<WebPage> iter = new BreadthFirstIterator<>(siteGraph);
        while (iter.hasNext()) {
            WebPage vertex = iter.next();
            System.out.println("Vertex " + vertex.getTitle() + " is connected to:");
            for (DefaultEdge edge : siteGraph.outgoingEdgesOf(vertex)) {
                WebPage target = siteGraph.getEdgeTarget(edge);
                System.out.println("  " + target.getTitle());
            }
        }
    }
}

class WebPage {
    private String url;
    private String title;

    public WebPage(String url, String title) {
        this.url = url;
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebPage webPage = (WebPage) o;
        return Objects.equals(url, webPage.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    // Getters and setters

    public String getUrl(){
        return url;
    }

    public String getTitle(){
        return title;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void setTitle(String title){
        this.title = title;
    }
    
}