package edu.emory.erd.webservice;

import edu.emory.erd.Annotator;
import edu.emory.erd.AnnotatorBase;
import edu.emory.erd.BasicDisambiguator;
import edu.emory.erd.LexiconMentionBuilder;
import edu.emory.erd.types.Annotation;
import edu.emory.erd.types.AnnotationSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


/**
 * Root resource (exposed at "erd" path)
 */
@Path("erd")
public class ErdResource {

    /**
     * Method handling HTTP POST requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * return String that will be returned as a text/plain response.
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String annotatePost(@FormParam("runID") String runId,
                           @FormParam("TextID") String textId,
                           @FormParam("Text") String text) {
        return "Got it!";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String annotateGet(@QueryParam("runID") String runId,
                              @QueryParam("TextID") String textId,
                              @QueryParam("Text") String text) {
        List<AnnotationSet> annotations = Main.annotator.annotate(text);
        StringBuilder res = new StringBuilder();
        for (AnnotationSet set : annotations) {
            for (Annotation annotation : set) {
                res.append(annotation.getMentionText() + "\t" + annotation.getEntityInfo().getId() + "\n");
            }
        }
        return res.toString();
    }
}
