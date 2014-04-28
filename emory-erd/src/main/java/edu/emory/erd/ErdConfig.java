package edu.emory.erd;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Returns project configuration.
 */
public class ErdConfig {
    private static Configuration config = null;

    // Static initializer
    static {
        try {
            config = new PropertiesConfiguration("emory-erd.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
            config = null;
        }
    }

    public static Configuration getConfig() {
        return config;
    }
}
