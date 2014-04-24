package edu.emory.erd.utils;

import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiPageIterator;
import edu.jhu.nlp.wikipedia.WikiXMLSAXParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

class WikiLink {
    private String fromPage;
    private String toPage;
    private String linkPhrase;

    WikiLink(String fromPage, String toPage, String linkPhrase) {
        this.fromPage = fromPage;
        this.toPage = toPage;
        this.linkPhrase = linkPhrase;
    }

    @Override
    public String toString() {
        return fromPage + "\t" + toPage + "\t" + linkPhrase;
    }
}

/**
 * Processes Wikipedia dump file and extract all links from pages to other pages.
 */
public class WikipediaProcessor {

    public static void ProcessWikipediaDump(String filename) {
        List<WikiLink> wikiLinks = new ArrayList<WikiLink>();

        try {
            WikiXMLSAXParser parser = new WikiXMLSAXParser(filename);
            WikiPageIterator pageIterator = parser.getIterator();
            while (pageIterator.hasMorePages()) {
                WikiPage page = pageIterator.nextPage();
                String title = page.getTitle();
                HashMap<String, Vector> links = page.getLinksWithText();
                Vector<String> linkedPages = links.get("pageLinks");
                Vector<String> linkTexts = links.get("pageLinkTexts");
                for (int i = 0; i < linkedPages.size(); ++i) {
                    wikiLinks.add(new WikiLink(title, linkedPages.get(i), linkTexts.get(i)));
                }
            }
            for (WikiLink link : wikiLinks) {
                System.out.println(link);
            }
        } catch (Exception exc) {
            System.err.println(exc.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java WikipediaProcessor wikipedia_dump_file.xml");
            return;
        }
        ProcessWikipediaDump(args[0]);
    }
}
