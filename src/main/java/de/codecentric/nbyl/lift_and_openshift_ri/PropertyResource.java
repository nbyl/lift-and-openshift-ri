package de.codecentric.nbyl.lift_and_openshift_ri;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Path("/")
public class PropertyResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getProperties() throws IOException {
        return serializeProperties(loadConfigurationProperties());
    }

    @GET
    @Path("/gitinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getGitProperties() throws IOException {
        return serializeProperties(loadGitProperties());
    }

    private Properties loadConfigurationProperties() throws IOException {
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

    private Properties loadGitProperties() throws IOException {
        Properties properties = new Properties();
        try (final InputStream stream = this.getClass().getResourceAsStream("git.properties")) {
            properties.load(stream);
        }
        return properties;
    }

    private JsonObject serializeProperties(Properties properties) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        for (String key : properties.stringPropertyNames()) {
            builder.add(key, properties.getProperty(key));
        }

        return builder.build();
    }

}
