package project;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        String startUrl = "https://savetheearth.org/";
        WebCrawler crawler = new WebCrawler(startUrl);

        // Print the structure of the crawled website
        System.out.println("Website structure:");
        crawler.printGraphStructure();

        // Optionally, you can also export the graph to DOT format
        SwingUtilities.invokeLater(() -> {
            GraphVisualizer.visualize(crawler.getGraph());
        });
    }
}