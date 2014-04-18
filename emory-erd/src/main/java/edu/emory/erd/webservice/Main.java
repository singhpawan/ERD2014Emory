package edu.emory.erd.webservice;

import edu.emory.erd.Annotator;
import edu.emory.erd.AnnotatorBase;
import edu.emory.erd.BasicDisambiguator;
import edu.emory.erd.LexiconMentionBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/";

    public static Configuration config;
    public static Annotator annotator;

    static {
        try {
            config = new PropertiesConfiguration("emory-erd.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        String lexiconFileName = Main.config.getString("entityLexiconFile");
        try {
            annotator = new AnnotatorBase(new LexiconMentionBuilder(
                    new FileInputStream(lexiconFileName)), new BasicDisambiguator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in edu.emory.erd.webservice package
        final ResourceConfig rc = new ResourceConfig().packages("edu.emory.erd.webservice");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }
}

