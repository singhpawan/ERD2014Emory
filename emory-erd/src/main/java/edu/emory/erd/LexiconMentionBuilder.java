package edu.emory.erd;

import edu.emory.erd.types.*;
import edu.emory.erd.util.NlpUtils;
import edu.emory.erd.util.WikipediaLinks;
import javassist.compiler.Lex;
import opennlp.tools.util.Span;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds entity mentions using sliding window and lexicon.
 */
public class LexiconMentionBuilder implements MentionBuilder {
    private PatriciaTrie<List<String>> lexicon;
    private MultiKeyMap<String, Long> entityPhraseCount;  // the number of mentions of entity with phrase
    private HashMap<String, Long> entityCount;  // the number of mentions for each entity.
    private long totalCount; // total number of mentions.

    // a map from wikipedia entity name to Freebase mid.
    private HashMap<String, String> wiki2mid = new HashMap<String, String>();

    public LexiconMentionBuilder() throws IOException {
        this(new FileInputStream(ErdConfig.getConfig().getString("entityLexiconFile")));
    }

    public LexiconMentionBuilder(InputStream lexiconStream) throws IOException {
        // Initialize fields.
        lexicon = new PatriciaTrie<List<String>>();
        entityPhraseCount = MultiKeyMap.multiKeyMap(new HashedMap<MultiKey<? extends String>, Long>());
        entityCount = new HashMap<String, Long>();
        totalCount = 0;

        System.out.println("Reading lexicon...");
        BufferedReader lexiconReader = new BufferedReader(new InputStreamReader(lexiconStream));
        while (lexiconReader.ready()) {
            String[] line = lexiconReader.readLine().split("\t");
            String entityId = line[0];
            for (int i = 2; i < line.length; i+=2) {
                String phrase = line[i-1];
                Long count = Long.parseLong(line[i]);
                addCounts(entityId, phrase, count);
                addLexiconEntry(phrase, entityId);
            }
        }
        System.out.println("Reading wikipedia links...");
        addWikipediaLinks();
        System.out.println("Reading Freebase entity names...");
        readFreebaseEntityNames();
    }

    private void addWikipediaLinks() throws IOException {
        WikipediaLinks links = WikipediaLinks.getWikipediaLinks();
        for (WikipediaLinks.Link link : links) {
            if (!wiki2mid.containsKey(link.to)) {
                System.err.println("Missing wiki title: " + link.to);
                continue;
            }
            addLexiconEntry(link.phrase, wiki2mid.get(link.to));
        }
    }

    /**
     * Reads Freebase entity names extracted by LexiconBuilderRdf tool from emory-erd-tools.
     * @throws IOException
     */
    private void readFreebaseEntityNames() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(ErdConfig.getConfig().getString("freebaseNamesFile"))));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t");
            if (fields[1].equals("name") || fields[1].equals("alias")) {
                addLexiconEntry(fields[2], fields[0]);
                addCounts(fields[0], fields[2], 1L);
            } else { // Also read wikipedia links
                wiki2mid.put(WikipediaLinks.normalizeTitle(fields[2]), fields[0]);
            }
        }
    }

    /**
     * Adds count to the number of mentions overall, for a phrase and for entity-phrase pair.
     * @param entityId id of mentioned entity.
     * @param phrase mention phrase.
     * @param count mentions count.
     */
    private void addCounts(String entityId, String phrase, Long count) {
        totalCount += count;
        if (!entityCount.containsKey(entityId)) {
            entityCount.put(entityId, 0L);
        }
        entityCount.put(entityId, entityCount.get(entityId) + 1L);
        if (!entityPhraseCount.containsKey(entityId, phrase)) {
            entityPhraseCount.put(entityId, phrase, 0L);
        }
        entityPhraseCount.put(entityId, phrase, entityPhraseCount.get(entityId, phrase) + 1);
    }

    /**
     * Adds entityId with the given phrase to the lexicon.
     * @param phrase A phrase used to refer to the entity.
     * @param entityId The referred entity.
     */
    private void addLexiconEntry(String phrase, String entityId) {
        if (!lexicon.containsKey(phrase))
            lexicon.put(phrase, new ArrayList<String>());
        lexicon.get(phrase).add(entityId);
    }

    /**
     * Returns confidence score for entity mention.
     * @param entityId mentioned entity.
     * @param phrase mention phrase.
     * @return A score between 0 and 1.
     */
    private double getMentionScore(String entityId, String phrase) {
        if (!entityPhraseCount.containsKey(entityId, phrase))
            // TODO: Use smoothing.
            return 0.0;
        return 1.0 * entityPhraseCount.get(entityId, phrase) / totalCount;
    }

    @Override
    public List<Annotation> buildMentions(Text document) {
        List<Annotation> res = new ArrayList<Annotation>();

        // Go over all sentences.
        for (Sentence sentence : document.getSentences()) {
            int currentSpanStartWord = 0;
            int currentSpanEndWord = 1;
            // Go over all words.
            while (currentSpanStartWord < sentence.getWordsCount()) {
                Span currentSpan = new Span(sentence.getWordSpan(currentSpanStartWord).getStart(),
                        sentence.getWordSpan(currentSpanEndWord - 1).getEnd());
                String phrase = document.getSpanText(currentSpan);
                // If we have a mention.
                if (lexicon.containsKey(phrase)) {
                    for (String entityId : lexicon.get(phrase)) {
                        res.add(new Annotation(document, currentSpan, new EntityInfo(entityId, entityId),
                                getMentionScore(entityId, phrase)));
                    }
                   ++currentSpanEndWord;
                } else if (lexicon.prefixMap(phrase).size() == 0) {
                    // If current phrase is not a prefix of any possible mention.
                    ++currentSpanStartWord;
                    currentSpanEndWord = currentSpanStartWord + 1;
                } else {
                    // If current phrase is a prefix of some mention.
                    ++currentSpanEndWord;
                }
                if (currentSpanEndWord > sentence.getWordsCount()) {
                    ++currentSpanStartWord;
                    currentSpanEndWord = currentSpanStartWord + 1;
                }
            }
        }
        return res;
    }
}
