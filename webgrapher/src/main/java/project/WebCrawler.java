package project;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

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
        System.out.println(depth + "   " + url);
        if (depth > 5 || !url.contains(domain) || visitedUrls.contains(url)) {
            System.out.println("exited");
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
                    WebPage linkedPage = new WebPage(nextUrl, "");
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
}

class WebPage {
    private String url;
    private String title;

    public WebPage(String url, String title) {
        this.url = url;
        this.title = title;
    }
  
    // Getters and setters
}