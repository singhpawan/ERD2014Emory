package edu.emory.erd.util;

import edu.emory.erd.types.Annotation;
import edu.emory.erd.types.Text;
import edu.emory.erd.util.NlpUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class NamedEntityRecognizerTest {

    
    @Test
    public void testTokenizer() {
        String[] words = NlpUtils.tokenize("This is just a single sentence about U.S.A.");
        assertEquals(8, words.length);
    }

    @Test
    public void testTokenizerWithExtraSpaces() {
        String[] words = NlpUtils.tokenize("This \n sentence: has 5 words!!");
        // Actually, punctuation will be considered a word here, so 8 in total.
        assertEquals(8, words.length);
    }

    @Test
    public void testEntityDetector()throws Exception {
   /*     List<Annotation> annotations =
                NlpUtils.detectEntities(new Text("Michael Jackson was born in a Possum City. He was also called Mike."));
	System.out.println("WE ARE TESTING THE ENTITY DETECTOR!!");
	



	List<Annotation> annotations2 =
                NlpUtils.detectEntities(new Text("Michael Jackson was born in a Possum City. He was also called Mike."));

*/
	File f = new File("./src/test/resources/sample_file.txt");
	Scanner in = new Scanner(f);
	
	PrintWriter writer = new PrintWriter("Output.txt", "UTF-8");

		

	while(in.hasNextLine()){
	String str=in.nextLine();
	System.out.println(str);
	List<Annotation> annotations =NlpUtils.detectEntities(new Text(str));
	for(int i=0;i<annotations.size();i++)
		writer.println(annotations.get(i).getMentionText()+"\t"+annotations.get(i).getEntityInfo().getId()+"\t"+annotations.get(i).getScore());
		//System.out.println(annotations.get(i).getSpanText());

	}
	writer.close();
        /*assertEquals(3, annotations.size());
        assertEquals("PERSON", annotations.get(0).getEntityInfo().getId());
        assertEquals("LOCATION", annotations.get(1).getEntityInfo().getId());
        assertEquals("PERSON", annotations.get(2).getEntityInfo().getId());
        assertEquals(30, annotations.get(1).getSpan().getStart());
        assertEquals(41, annotations.get(1).getSpan().getEnd());
	
	*/


	//System.out.println(annotations.get(0).getEntityInfo().getId());
	//System.out.println(annotations.get(1).getEntityInfo().getId());
	//System.out.println(annotations.get(1).getEntityInfo());
    }

@Test
    public void testSentenceDetector() {
        String[] sents = NlpUtils.detectSentences("This is sentence one. This is sentence two.");
        assertEquals(2, sents.length);
	for(int i=0; i<sents.length; i++)
		System.out.println(sents[i]);
    }

    @Test
    public void testSentenceDetectorWithAcronym() {
        String[] sents = NlpUtils.detectSentences("This is just a single sentence about U.S.A.");
        assertEquals(1, sents.length);
	for(int i=0; i<sents.length; i++)
		System.out.println(sents[i]);
    }

    @Test
    public void testSentenceDetectorWithLineBreaks() {
        String[] sents = NlpUtils.detectSentences("This is sentence 1... \n\nThis is my sentence two.");
        assertEquals(2, sents.length);
	for(int i=0; i<sents.length; i++)
		System.out.println(sents[i]);
	

    }

    /**
     * Checks tokenizer.
     */


	    @Before
    public void setUp() throws Exception {
	

    }

    @After
    public void tearDown() throws Exception {

    }




}
