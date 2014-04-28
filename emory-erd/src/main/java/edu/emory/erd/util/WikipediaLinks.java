package edu.emory.erd.util;

import edu.emory.erd.ErdConfig;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;


/**
 * Reads and stores information on wikipedia links from pages to other pages.
 */
public class WikipediaLinks implements Iterable<WikipediaLinks.Link> {
    /**
     * Represents a link from entity to entity.
     */
    public class Link {
        public String from;
        public String to;
        public String phrase;

        public Link(String from, String to, String phrase) {
            this.from = from;
            this.to = to;
            this.phrase = phrase;
        }

        @Override
        public String toString() {
            return this.from + "\t" + this.to + "\t" + this.phrase;
        }
    }

    private static WikipediaLinks self = null;
    private List<Link> links;
    private MultiKeyMap<String, List<Integer>> fromToLinks;  // A list of links from key1 to key2

    /**
     * Creates an instance of WikipediaLinks
     */
    public static WikipediaLinks getWikipediaLinks() {
        if (self != null) {
            return self;
        }
        // There might be file not found exception.
        try {
            String fileName = ErdConfig.getConfig().getString("wikipediaLinksFile");
            return self = new WikipediaLinks(fileName.endsWith(".gz") ?
                    new GZIPInputStream(new FileInputStream(fileName)) :
                    new FileInputStream(fileName));
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        return self = null;
    }

    /**
     * Normalizes title of a wikipedia title. It replaces all Unicode quoted chars with actual chars, makes it lowercase
     * and replaces spaces with underscore.
     * @param title Title to normalize.
     * @return Normalized Wikipedia title.
     */
    public static String normalizeTitle(String title) {
        return NlpUtils.unquoteFreebaseName(title).toLowerCase().replace(" ", "_");
    }

    /**
     * Reads wikipedia links from a stream. Format: <fromPage>\t<toPage>\t<phrase>
     * @param wikipediaLinksStream A stream to read wikipedia links from.
     * @throws IOException
     */
    private WikipediaLinks(InputStream wikipediaLinksStream) throws IOException {
        links = new ArrayList<Link>();
        fromToLinks = MultiKeyMap.multiKeyMap(new HashedMap<MultiKey<? extends String>, List<Integer>>());

        BufferedReader reader = new BufferedReader(new InputStreamReader(wikipediaLinksStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] link = line.split("\t");
            String fromWiki = WikipediaLinks.normalizeTitle(link[0]);
            String toWiki = WikipediaLinks.normalizeTitle(link[1]);
            addLink(fromWiki, toWiki, link[2]);
        }
    }

    /**
     * Adds a Wikipedia link to the internal data structures.
     * @param from A title of a page where the link is.
     * @param to A title of a page the link points to.
     * @param phrase Phrase of the link.
     */
    private void addLink(String from, String to, String phrase) {
        int index = links.size();
        links.add(new Link(from, to, phrase));
        if (!fromToLinks.containsKey(from, to)) {
            fromToLinks.put(from, to, new ArrayList<Integer>());
        }
        fromToLinks.get(from, to).add(index);
    }

    /**
     * Returns iterator over all links.
     * @return Iterator for all links in Wikipedia.
     */
    @Override
    public Iterator<Link> iterator() {
        return links.iterator();
    }
}
