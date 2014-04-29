package edu.emory.erd.util;

import edu.emory.erd.ErdConfig;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.io.*;
import java.util.*;
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
    private HashMap<String, HashMap<String, List<Link>>> inLinks = new HashMap<String, HashMap<String, List<Link>>>();
    private HashMap<String, HashMap<String, List<Link>>> outLinks = new HashMap<String, HashMap<String, List<Link>>>();
    private HashMap<String, String> mid2Wiki = new HashMap<String, String>();

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
        Link newLink = new Link(from, to, phrase);
        links.add(newLink);
        if (!outLinks.containsKey(from)) {
            outLinks.put(from, new HashMap<String, List<Link>>());
        }
        if (!outLinks.get(from).containsKey(to)) {
            outLinks.get(from).put(to, new ArrayList<Link>());
        }
        outLinks.get(from).get(to).add(newLink);

        if (!inLinks.containsKey(to)) {
            inLinks.put(to, new HashMap<String, List<Link>>());
        }
        if (!inLinks.get(to).containsKey(from)) {
            inLinks.get(to).put(from, new ArrayList<Link>());
        }
        inLinks.get(to).get(from).add(newLink);
    }

    /**
     * Returns iterator over all links.
     * @return Iterator for all links in Wikipedia.
     */
    @Override
    public Iterator<Link> iterator() {
        return links.iterator();
    }

    /**
     * Add a mid, wikipedia title pair.
     * @param mid Freebase entity mid.
     * @param wiki Entity wikipedia title.
     */
    public void addMid2Wiki(String mid, String wiki) {
        mid2Wiki.put(mid, normalizeTitle(wiki));
    }

    /**
     * Returns wikipedia title by entity mid. null is returned if no mapping found.
     * @param mid Entity mid.
     * @return Wikipedia page title.
     */
    public String getWikiByMid(String mid) {
        if (!mid2Wiki.containsKey(mid)) {
            return null;
        }
        return mid2Wiki.get(mid);
    }

    /**
     * Returns the number of in-links to a particular Wikipedia entity.
     * @param name Name of the entity.
     * @return The number of in-links.
     */
    public long getInlinksCount(String name) {
        if (!inLinks.containsKey(name)) return 0L;
        return inLinks.get(name).size();
    }

    /**
     * Returns the number of Wikipedia pages that link to both name1 and name2. This method searches through all
     * in links, so it can be slow.
     * @param name1 The name of the first entity.
     * @param name2 The name of the second entity.
     * @return The number of such pages that link to both entities given.
     */
    public long getCommonInLinksCount(String name1, String name2) {
        if (!inLinks.containsKey(name1) || !inLinks.containsKey(name2)) {
            return 0L;
        }
        long res = 0L;
        // TODO: Should we precompute this? Or at lease memorize?
        for (String page : inLinks.get(name1).keySet()) {
            if (inLinks.get(name2).containsKey(page)) {
                ++res;
            }
        }
        // Also add links from name1 page to name2 and vice versa.
        if (inLinks.get(name1).containsKey(name2)) {
            res += inLinks.get(name1).get(name2).size();
        }
        if (inLinks.get(name2).containsKey(name1)) {
            res += inLinks.get(name2).get(name1).size();
        }
        return res;
    }
}
