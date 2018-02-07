package de.codecentric.nbyl.lift_and_openshift_ri;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.Properties;

@Path("/")
public class PropertyResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getProperties() throws IOException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        Properties properties = loadProperties();

        for(String key: properties.stringPropertyNames()) {
            builder.add(key, properties.getProperty(key));
        }

        return builder.build();
    }

    private Properties loadProperties() throws IOException {
        String path = System.getenv("CONFIG_PATH");
        if (path == null) {
            path = "/etc/myconfig";
        }

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(new File(path, "environment.properties"))) {
            properties.load(input);
        }
        return properties;
    }
}
