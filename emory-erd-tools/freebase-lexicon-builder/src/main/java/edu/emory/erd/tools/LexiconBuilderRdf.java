package edu.emory.erd.tools;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Builds lexicon from Freebase RDF triples dump file: https://developers.google.com/freebase/data
 */
public class LexiconBuilderRdf {

    public static String stripRdf(String rdfUri) {
        return rdfUri.replace("<http://rdf.freebase.com/ns", "").replace(">", "").replace(".", "/");
    }

    public static void processFreebaseDump(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] triple = reader.readLine().split("\t");
            String objectMid = stripRdf(triple[0]);
            String subjectMid = stripRdf(triple[2]);
            String predicate = stripRdf(triple[1]);
            if (objectMid.contains("/m/")) {
                if (predicate.equals("type/object/key") && subjectMid.contains("/wikipedia/en/")) {
                    // Take wikipedia title substring (15 is the beginning of title)
                    System.out.println(objectMid + "\t" + "wiki\t" +
                            subjectMid.substring(15, subjectMid.lastIndexOf("\"")));
                } else if (predicate.equals("/type/object/name") && subjectMid.endsWith("en")) {
                    String name = subjectMid.substring(1, subjectMid.lastIndexOf("\"@"));
                    System.out.println(objectMid + "\t" + "name\t" + name);
                } else if (predicate.equals("/common/topic/alias") && subjectMid.endsWith("en")) {
                    String name = subjectMid.substring(1, subjectMid.lastIndexOf("\"@"));
                    System.out.println(objectMid + "\t" + "alias\t" + name);
                }
            }
        }
    }

    public static void outputLexicon() {
    }

    public static void main(String[] args) {
        try {
            for (String inputFile : args) {
                processFreebaseDump(inputFile);
            }
        } catch (IOException exc) {
            System.err.println(exc.getMessage());
        }
        outputLexicon();
    }
}
