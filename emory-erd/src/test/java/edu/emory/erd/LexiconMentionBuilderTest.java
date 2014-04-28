package edu.emory.erd;

import edu.emory.erd.BasicDisambiguator;
import edu.emory.erd.Disambiguator;
import edu.emory.erd.LexiconMentionBuilder;
import edu.emory.erd.MentionBuilder;
import edu.emory.erd.types.Annotation;
import edu.emory.erd.types.AnnotationSet;
import edu.emory.erd.types.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
public class LexiconMentionBuilderTest {

    MentionBuilder mentionBuilder;
    Disambiguator disambiguator;
    List<Annotation> annotations;

    @Before
    public void setUp() throws Exception {
        mentionBuilder = new LexiconMentionBuilder(
                LexiconMentionBuilderTest.class.getResourceAsStream("/entity_lexicon.txt"));
        annotations = mentionBuilder.buildMentions(new Text("I saw Mark Byford, David Clarke in the Possum town"));
        disambiguator = new BasicDisambiguator();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testMentionBuilder() {
        // Mark, Mark Byford, Byford, Possum town
        assertEquals(7, annotations.size());
        assertEquals("/m/02qgwd", annotations.get(0).getEntityInfo().getId());
    }

    @Test
    public void testDisambiguator() {
        List<AnnotationSet> annotationSets = disambiguator.disambiguate(annotations);
        assertEquals(1, annotationSets.size());
        assertEquals(3, annotationSets.get(0).getAnnotationsCount());
    }

}
