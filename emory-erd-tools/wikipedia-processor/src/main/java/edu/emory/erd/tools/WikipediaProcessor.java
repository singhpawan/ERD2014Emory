package edu.emory.erd.tools;

// Using wiki parser from https://bitbucket.org/dfdeshom/wikixmlj/
import edu.jhu.nlp.wikipedia.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

class WikiLink {
    private String fromPage;
    private String toPage;
    private String linkPhrase;

    WikiLink(String fromPage, String toPage, String linkPhrase) {
        this.fromPage = fromPage.replace("\n", "");
        this.toPage = toPage.replace("\n", "");
        this.linkPhrase = linkPhrase.replace("\n", "");
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
        try {
            WikiXMLParser parser = WikiXMLParserFactory.getSAXParser(filename);
            parser.setPageCallback(new PageCallbackHandler() {
                @Override
                public void process(WikiPage wikiPage) {
                    String title = wikiPage.getTitle();
                    HashMap<String, Vector> links = wikiPage.getLinksWithText();
                    Vector<String> linkedPages = links.get("pageLinks");
                    Vector<String> linkTexts = links.get("pageLinkTexts");
                    for (int i = 0; i < linkedPages.size(); ++i) {
                        System.out.print(new WikiLink(title, linkedPages.get(i), linkTexts.get(i)));
                    }
                }
            });
            parser.parse();
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
